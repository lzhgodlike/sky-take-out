package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        end = end.plusDays(1);
        List<TurnoverReportVO> turnoverReportVOList = orderMapper.getByBeginAndEndTime(begin, end);
        log.info("查询结果：{}", turnoverReportVOList);
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        StringBuilder dateList = new StringBuilder();
        StringBuilder turnoverList = new StringBuilder();
        int index = 0;
        for (int i = 0; i < end.toEpochDay() - begin.toEpochDay(); i++) {
            dateList.append(begin.plusDays(i)).append(",");
            if (index < turnoverReportVOList.size() && turnoverReportVOList.get(index).getDateList().equals(begin.plusDays(i).toString())) {
                turnoverList.append(turnoverReportVOList.get(index).getTurnoverList()).append(",");
                log.info("begin:{}", begin.plusDays(i));
                index += 1;
            } else {
                turnoverList.append(0).append(",");
            }
        }
        if (dateList.length() > 0) {
            dateList.deleteCharAt(dateList.length() - 1);
            turnoverReportVO.setDateList(dateList.toString());
        }
        if (turnoverList.length() > 0) {
            turnoverList.deleteCharAt(turnoverList.length() - 1);
            turnoverReportVO.setTurnoverList(turnoverList.toString());
        }
        log.info("营业额数据:{}", turnoverReportVO);
        return turnoverReportVO;
    }

    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        end = end.plusDays(1);
        List<Map<String, Object>> dailyRows = userMapper.getByBeginAndEndTime(begin, end);
        log.info("用户数据行:{}", dailyRows);

        Map<LocalDate, Integer> dailyCountMap = new HashMap<>();
        for (Map<String, Object> row : dailyRows) {
            Object dateObj = row.get("dateList");
            Object usersObj = row.get("users");
            if (dateObj != null) {
                LocalDate dateKey = LocalDate.parse(String.valueOf(dateObj));
                int count = usersObj == null ? 0 : ((Number) usersObj).intValue();
                dailyCountMap.put(dateKey, count);
            }
        }
        UserReportVO userReportVO = new UserReportVO();
        StringBuilder dateList = new StringBuilder();
        StringBuilder totalUserList = new StringBuilder();
        StringBuilder newUserList = new StringBuilder();
        int cumulative = userMapper.getTotalUntilDateByDate(begin);
        for (LocalDate date = begin; date.isBefore(end); date = date.plusDays(1)) {
            dateList.append(date).append(",");
            int newCount = dailyCountMap.getOrDefault(date, 0);
            cumulative += newCount;
            totalUserList.append(cumulative).append(",");
            newUserList.append(newCount).append(",");
        }
        if (dateList.length() > 0) {
            userReportVO.setDateList(dateList.substring(0, dateList.length() - 1));
        }
        if (totalUserList.length() > 0) {
            userReportVO.setTotalUserList(totalUserList.substring(0, totalUserList.length() - 1));
        }
        if (newUserList.length() > 0) {
            userReportVO.setNewUserList(newUserList.substring(0, newUserList.length() - 1));
        }
        log.info("用户报表：{}", userReportVO);
        return userReportVO;
    }

    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        end = end.plusDays(1);
        List<Orders> orders = orderMapper.getByBeginAndEndTimeOne(begin, end);
        OrderReportVO orderReportVO = new OrderReportVO();
        StringBuilder dateList = new StringBuilder();
        StringBuilder orderCountList = new StringBuilder();
        StringBuilder validOrderCountList = new StringBuilder();
//        int totalOrderCount = 0;
        int validOrderCount = 0;
        for (LocalDate date = begin; date.isBefore(end); date = date.plusDays(1)) {
            Integer orderCount = 0;
            Integer validOrderCountL = 0;
            dateList.append(date).append(",");
            for (Orders order : orders) {
                if (order.getOrderTime().toLocalDate().equals(date)) {
                    orderCount += 1;
                    if (order.getStatus() > Orders.TO_BE_CONFIRMED && order.getStatus() <= Orders.COMPLETED && order.getPayStatus() != Orders.REFUND) {
                        validOrderCountL += 1;
                    }
                }
            }
            validOrderCount += validOrderCountL;
            orderCountList.append(orderCount).append(",");
            validOrderCountList.append(validOrderCountL).append(",");
        }
        orderReportVO.setTotalOrderCount(orders.size());
        if (dateList.length() > 0) {
            orderReportVO.setDateList(dateList.substring(0, dateList.length() - 1));
        }
        if (orderCountList.length() > 0) {
            orderReportVO.setOrderCountList(orderCountList.substring(0, orderCountList.length() - 1));
        }
        if (validOrderCountList.length() > 0) {
            orderReportVO.setValidOrderCountList(validOrderCountList.substring(0, validOrderCountList.length() - 1));
        }
        orderReportVO.setValidOrderCount(validOrderCount);
        if (orders.size() > 0) {
            orderReportVO.setOrderCompletionRate(validOrderCount * 1.0 / orders.size());
        } else orderReportVO.setOrderCompletionRate(0.0);
        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        end = end.plusDays(1);
        List<GoodsSalesDTO> top10 = orderMapper.getSalesTop10(begin, end);
        SalesTop10ReportVO turnoverReportVO = new SalesTop10ReportVO();
        StringBuilder nameList = new StringBuilder();
        StringBuilder numberList = new StringBuilder();
        for (GoodsSalesDTO dto : top10) {
            nameList.append(dto.getName()).append(",");
            numberList.append(dto.getNumber()).append(",");
        }
        if (nameList.length() > 0) {
            turnoverReportVO.setNameList(nameList.substring(0, nameList.length() - 1));
        } else turnoverReportVO.setNameList("null, null, null, null, null, null, null, null, null, null");
        if (numberList.length() > 0) {
            turnoverReportVO.setNumberList(numberList.substring(0, numberList.length() - 1));
        } else turnoverReportVO.setNumberList("0, 0, 0, 0, 0, 0, 0, 0, 0, 0");
        return turnoverReportVO;
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
//        LocalDateTime.of(begin, LocalTime.MIN);
//        LocalDateTime.of(end, LocalTime.MAX);
        BusinessDataVO dataVO = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间：" + begin + "至" + end);
            sheet.getRow(3).getCell(2).setCellValue(dataVO.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(dataVO.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(dataVO.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(dataVO.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(dataVO.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                BusinessDataVO dataVO1 = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                sheet.getRow(7 + i).getCell(1).setCellValue(date.toString());
                sheet.getRow(7 + i).getCell(2).setCellValue(dataVO1.getTurnover());
                sheet.getRow(7 + i).getCell(3).setCellValue(dataVO1.getValidOrderCount());
                sheet.getRow(7 + i).getCell(4).setCellValue(dataVO1.getOrderCompletionRate());
                sheet.getRow(7 + i).getCell(5).setCellValue(dataVO1.getUnitPrice());
                sheet.getRow(7 + i).getCell(6).setCellValue(dataVO1.getNewUsers());
            }

            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            out.flush();
            excel.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
