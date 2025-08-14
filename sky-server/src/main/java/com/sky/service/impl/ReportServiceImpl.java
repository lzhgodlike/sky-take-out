package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        log.info("查询营业额数据，从{}到{}", begin, end);
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
        dateList.deleteCharAt(dateList.length() - 1);
        turnoverList.deleteCharAt(turnoverList.length() - 1);
        turnoverReportVO.setDateList(dateList.toString());
        turnoverReportVO.setTurnoverList(turnoverList.toString());
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
        userReportVO.setDateList(dateList.substring(0, dateList.length() - 1));
        userReportVO.setTotalUserList(totalUserList.substring(0, totalUserList.length() - 1));
        userReportVO.setNewUserList(newUserList.substring(0, newUserList.length() - 1));
        log.info("用户报表：{}", userReportVO);
        return userReportVO;
    }
}
