package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
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

    @Autowired
    WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = createLocalDateList(begin, end);

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        // 一次查出区间内所有已完成订单
        //TODO 后期可通过修改sql语句进行分组 减少stream流的使用 让代码更优雅
        List<Orders> ordersList = orderMapper.getTurnoverStatistics(beginTime, endTime, Orders.COMPLETED);

        // 按日期汇总营业额
        Map<LocalDate, BigDecimal> turnoverMap = ordersList.stream()
                .collect(Collectors.groupingBy(
                        orders -> orders.getOrderTime().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, Orders::getAmount, BigDecimal::add)));

        // 按日期列表逐天取值，没有订单的日期补 0
        List<BigDecimal> turnoverList = dateList.stream()
                .map(date -> turnoverMap.getOrDefault(date, BigDecimal.ZERO))
                .toList();

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
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
    @Transactional
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> localDateList = createLocalDateList(begin, end);

        List<Integer> orderCountList = new ArrayList<>();

        List<Integer> validOrderCountList = new ArrayList<>();

        for (LocalDate localDate : localDateList) {

            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);

            Integer orderCount = getOrderCount(beginTime, endTime, null);

            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }

        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();

        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();

        double orderCompletionRate = 0.0;

        if (totalOrderCount != 0) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }


        return OrderReportVO.builder()
                .dateList(StringUtils.join(localDateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    private Integer getOrderCount(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        return orderMapper.getOrderCount(beginTime, endTime, status);
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

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSalesTop10(beginTime, endTime);


        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(goodsSalesDTOList.stream()
                        .map(GoodsSalesDTO::getName).toList(), ","))
                .numberList(StringUtils.join(goodsSalesDTOList.stream()
                        .map(GoodsSalesDTO::getNumber).toList(), ","))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse httpServletResponse) {

        try {

            LocalDate dateBegin = LocalDate.now().minusDays(30);
            LocalDate dateEnd = LocalDate.now().minusDays(1);

            LocalDateTime beginTime = LocalDateTime.of(dateBegin ,LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(dateEnd, LocalTime.MAX);

            BusinessDataVO businessDataVO = workspaceService.getBusinessData(beginTime, endTime);

            InputStream resourceAsStream = this.getClass()
                    .getClassLoader()
                    .getResourceAsStream("template\\运营数据报表模板.xlsx");

            XSSFWorkbook xssfWorkbook = null;
            if (resourceAsStream == null) {
                return;
            }
            xssfWorkbook = new XSSFWorkbook(resourceAsStream);

            XSSFSheet sheetAt = xssfWorkbook.getSheetAt(0);

            sheetAt.getRow(1)
                    .getCell(1)
                    .setCellValue("时间:" + dateBegin + "至" + dateEnd);

            XSSFRow row = sheetAt.getRow(3);

            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());

            row = sheetAt.getRow(4);

            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();


            for (int i = 0; i < 30; i++) {

                LocalDate time = dateBegin.plusDays(i);

                BusinessDataVO businessData = workspaceService.getBusinessData(
                        LocalDateTime.of(time, LocalTime.MIN),
                        LocalDateTime.of(time, LocalTime.MAX));

                row = sheetAt.getRow(7 + i);

                row.getCell(1).setCellValue(time.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            xssfWorkbook.write(servletOutputStream);
            servletOutputStream.close();
            xssfWorkbook.close();

        } catch (IOException e) {

            throw new RuntimeException(e);

        }

    }
}
