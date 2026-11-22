package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    UserMapper userMapper;

    @Override
    @Transactional
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {

        // 生成 begin 到 end 的每一天（含首尾）
        List<LocalDate> localDateList = createLocalDateList(begin, end);


        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<Orders> ordersList = orderMapper.getTurnoverStatistics(beginTime, endTime, Orders.COMPLETED);
        // 当天没有已完成订单时 sum 返回 null，补 0 保证前端每天都有值
        Map<LocalDate, BigDecimal> collect = ordersList.stream()
                .collect(Collectors.groupingBy(
                        orders -> orders.getOrderTime().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Orders::getAmount, BigDecimal::add)));

        List<BigDecimal> amountList = localDateList.stream().map(localDate -> collect.getOrDefault(localDate, BigDecimal.ZERO)).toList();

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(localDateList, ","))
                .turnoverList(StringUtils.join(amountList, ","))
                .build();
    }

    private static @NonNull List<LocalDate> createLocalDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> localDateList = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            localDateList.add(date);
        }
        return localDateList;
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
//        OrderReportV orderMapper.getOrdersStatistics();
        return null;
    }

    @Override
    @Transactional
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        // 生成 begin 到 end 的每一天（含首尾）
        List<LocalDate> localDateList = createLocalDateList(begin, end);

        List<Integer> newUserList = new ArrayList<>();

        List<Integer> totalUSerList = new ArrayList<>();



        for (LocalDate localDate : localDateList) {
            Map<String, LocalDateTime> map = new HashMap<>();

            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);


            map.put("begin", beginTime);
            Integer totalUser = userMapper.countByMap(map);
            totalUSerList.add(totalUser);

            map.put("end", endTime);
            Integer newUser = userMapper.countByMap(map);
            newUserList.add(newUser);

        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(localDateList, ","))
                .totalUserList(StringUtils.join(totalUSerList.reversed(), ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }
}
