package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController("userOrderController")
@RequestMapping("/user/order")
@Tag(name = "订单相关", description = "订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @Operation(summary = "用户提交订单相关接口")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户提交订单: {}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @Operation(summary = "订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        orderService.paySuccess(ordersPaymentDTO.getOrderNumber());
        return Result.success();
    }

    @GetMapping("/historyOrders")
    @Operation(summary = "历史订单查询")
    public Result<PageResult> historyOrder(OrdersPageQueryDTO ordersPageQueryDTO) {
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        log.info("历史订单分页查询: {}", ordersPageQueryDTO);
        PageResult pageResult = orderService.userPageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/orderDetail/{id}")
    @Operation(summary = "查询订单详情")
    public Result<OrderVO> OrderVOList(@PathVariable Long id) {
        log.info("根据查询订单详情: {}", id);
        OrderVO orderVO = orderService.getById(id);
        return Result.success(orderVO);
    }

    @PutMapping("/cancel/{id}")
    @Operation(summary = "取消订单")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        log.info("取消订单id：{}", id);
        orderService.cancelOrderWithUser(id);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    @Operation(summary = "用户重复下单")
    public Result<Void> repetition(@PathVariable Long id) {
        log.info("用户重复下单: {}", id);
        orderService.repetition(id);
        return Result.success();
    }

    @GetMapping("/reminder/{id}")
    @Operation(summary = "用户催单")
    public Result<Void> reminder(@PathVariable Long id){
        log.info("用户催单:{}", id);
        orderService.reminder(id);
        return Result.success();
    }

}
