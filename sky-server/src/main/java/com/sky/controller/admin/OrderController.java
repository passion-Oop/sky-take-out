package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Slf4j
@Tag(name = "订单管理", description = "订单管理相关接口")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/details/{id}")
    @Operation(summary = "根据编号查询订单")
    public Result<OrderVO> getOrdersById(@PathVariable Long id) {
        log.info("接收到查询订单编号为：{} 的订单", id);
        return Result.success(orderService.getById(id));
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消订单")
    public Result<Void> cancelOrder(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("接收到取消订单编号为：{} 的订单", ordersCancelDTO);
        orderService.cancelOrderWithAdmin(ordersCancelDTO);
        return Result.success();
    }

    @GetMapping("/statistics")
    @Operation(summary = "订单统计")
    public Result<?> getStatistics() {
        log.info("接收到订单统计请求");
        OrderStatisticsVO orderStatisticsVO = orderService.getStatisticsVO();
        return Result.success(orderStatisticsVO);
    }

    @PutMapping("/complete/{id}")
    @Operation(summary = "完成订单")
    public Result<Void> completeOrder(@PathVariable Long id) {
        log.info("接收到完成订单编号为：{} 的订单", id);
        orderService.completeOrder(id);
        return Result.success();
    }

    @PutMapping("/rejection")
    @Operation(summary = "拒绝订单")
    public Result<Void> rejectionOrder(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("接收到拒绝订单编号为：{} 的订单", ordersCancelDTO);
        orderService.rejectionOrder(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/confirm")
    @Operation(summary = "接受订单")
    public Result<Void> confirmOrder(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接收到接受订单编号为：{} 的订单", ordersConfirmDTO.getId());
        orderService.confirmOrder(ordersConfirmDTO.getId());
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @Operation(summary = "派送接单")
    public Result<Void> deliveryOrder(@PathVariable Long id) {
        log.info("接收到派送接单编号为：{} 的订单", id);
        orderService.deliveryOrder(id);
        return Result.success();
    }

    @GetMapping("/conditionSearch")
    @Operation(summary = "条件查询订单")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("接收到条件查询订单请求 {}", ordersPageQueryDTO);
        PageResult pageResult = orderService.adminPageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }



}
