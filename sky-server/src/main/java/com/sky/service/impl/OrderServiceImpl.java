package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    ShoppingCartMapper shoppingCartMapper;

    @Autowired
    AddressBookMapper addressBookMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.baidu.ak}")
    private String ak;

    @Autowired
    private WebSocketServer webSocketServer;


    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        if (CollectionUtils.isEmpty(shoppingCartList)) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());

        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        checkOutOfRange(addressBook.getCityCode() + addressBook.getDistrictName() + addressBook.getDetail());

        Orders orders = new Orders();
        User user = userMapper.getById(userId);
        BeanUtils.copyProperties(ordersSubmitDTO, orders);

        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setUserName(user.getName());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getDetail());
        orders.setConsignee(addressBook.getConsignee());
        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO 订单支付DTO
     * @return 支付信息
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal("0.01"), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo 订单号
     */
    @Override
    @Transactional
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //通过websocket向服务端推送消息
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        // 管理端前端读取的是 content 字段（见 app.js 的 n.content），键名不能写成中文“订单号”
        map.put("content", "订单号：" + outTradeNo);

        String jsonString = JSON.toJSONString(map);

        webSocketServer.sendToAllClient(jsonString);
    }

    @Override
    @Transactional
    public PageResult userPageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {

        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> ordersPage = orderMapper.batchQuery(ordersPageQueryDTO);

        if (CollectionUtils.isEmpty(ordersPage)) {
            return new PageResult(0, new ArrayList<>());
        }

        List<Long> userIdList = ordersPage.stream().map(Orders::getId).toList();

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderIds(userIdList);
        Map<Long, List<OrderDetail>> detailMap = orderDetailList.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        List<OrderVO> orderVOList = new ArrayList<>();

        for (Orders orders : ordersPage) {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDetailList(detailMap.getOrDefault(orders.getId(), new ArrayList<>()));
            orderVOList.add(orderVO);
        }

        return new PageResult(ordersPage.getTotal(), orderVOList);
    }

    @Override
    public OrderVO getById(Long id) {
        Orders orders = orderMapper.select(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void cancelOrderWithUser(Long id) {
        orderMapper.update(Orders.builder()
                .id(id)
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .amount(new BigDecimal("0.00"))
                .build());
    }

    @Override
    @Transactional
    public void repetition(Long id) {
        Orders orders = orderMapper.select(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getById(id);

        // 主键置空，新订单id由数据库生成
        orders.setId(null);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(null);
        orders.setCancelTime(null);
        orders.setCancelReason(null);
        orders.setRejectionReason(null);
        orders.setDeliveryTime(null);
        orderMapper.insert(orders);

        // 明细需要挂到新订单上，必须在insert之后取新id
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setId(null);
            orderDetail.setOrderId(orders.getId());
        }
        orderDetailMapper.insertBatch(orderDetailList);
    }

    @Override
    public void cancelOrderWithAdmin(OrdersCancelDTO ordersCancelDTO) {
        orderMapper.update(Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .amount(new BigDecimal("0.00"))
                .cancelReason(ordersCancelDTO.getCancelReason())
                .build());
    }

    @Override
    public OrderStatisticsVO getStatisticsVO() {
        return orderMapper.getStatisticsVO();
    }

    @Override
    public void completeOrder(Long id) {
        orderMapper.update(Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build());
    }

    @Override
    public void rejectionOrder(OrdersCancelDTO ordersCancelDTO) {
        orderMapper.update(Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .cancelReason(ordersCancelDTO.getCancelReason())
                .build());
    }

    @Override
    public void confirmOrder(Long id) {
        orderMapper.update(Orders.builder()
                .id(id)
                .status(Orders.CONFIRMED)
                .build());
    }

    @Override
    public void deliveryOrder(Long id) {
        orderMapper.update(Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build());
    }

    @Override
    @Transactional
    public PageResult adminPageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {

        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> ordersPage = orderMapper.batchQuery(ordersPageQueryDTO);

        if (CollectionUtils.isEmpty(ordersPage)) {
            return new PageResult(0, new ArrayList<>());
        }

        List<Long> orderIdList = ordersPage.stream().map(Orders::getId).toList();

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderIds(orderIdList);

        Map<Long, List<OrderDetail>> orderDetailMap = orderDetailList.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        List<OrderVO> orderVOList = new ArrayList<>();

        for (Orders orders : ordersPage) {

            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);

            List<OrderDetail> details = orderDetailMap.getOrDefault(orders.getId(), new ArrayList<>());
            orderVO.setOrderDishes(details.stream()
                    .map(s -> s.getName() + "*" + s.getNumber())
                    .collect(Collectors.joining(",")));

            orderVOList.add(orderVO);
        }

        return new PageResult(ordersPage.getTotal(), orderVOList);
    }

    @Override
    public void reminder(Long id) {

        Orders orders = orderMapper.select(id);

        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("type", 2);
        map.put("orderId", id);
        // 管理端前端读取的是 content 字段（见 app.js 的 n.content），键名不能写成中文“订单号”
        map.put("content", "订单号：" + orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    private void checkOutOfRange(String address) {
        Map<String, String> map = new HashMap<>();
        map.put("address", shopAddress);
        map.put("output", "json");
        map.put("ak", ak);

        // 百度路线规划接口要求坐标为 "纬度,经度" 顺序
        String shopLatLng = getLatLng(map, "店铺地址解析失败");

        map.put("address", address);

        String userLatLng = getLatLng(map, "收货地址解析失败");

        Map<String, String> directionliteMap = new HashMap<>();

        directionliteMap.put("origin", shopLatLng);
        directionliteMap.put("destination", userLatLng);
        directionliteMap.put("ak", ak);

        String routePlan = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", directionliteMap);

        JSONObject routePlanJsonObject = JSONObject.parseObject(routePlan);

        String routeStatus = routePlanJsonObject.getString("status");
        if (!"0".equals(routeStatus)) {
            // 同样把百度返回的错误码和描述带出来，避免只看到笼统的"规划失败"
            throw new OrderBusinessException("配送路线规划失败：" + routePlanJsonObject.getString("message")
                    + "（status=" + routeStatus + "）");
        }

        JSONObject result = routePlanJsonObject.getJSONObject("result");
        JSONArray jsonArray = result.getJSONArray("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.getFirst()).get("distance");

        if (distance > 5000) {
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }

    private String getLatLng (Map<String, String> map, String msg) {
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);

        JSONObject jsonObject = JSONObject.parseObject(shopCoordinate);

        String status = jsonObject.getString("status");
        if (!"0".equals(status)) {
            // 把百度返回的 status 和 message 一并抛出，便于定位问题（如 AK 无权访问、配额超限等）
            throw new OrderBusinessException(msg + "：" + jsonObject.getString("message") + "（status=" + status + "）");
        }

            JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");

        // 注意：百度路线规划接口的坐标顺序是 "纬度,经度"，与地理编码返回字段的 lng/lat 正好相反
        return location.getString("lat") + "," + location.getString("lng");
    }


}
