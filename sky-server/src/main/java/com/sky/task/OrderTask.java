package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?") // 每隔1分钟执行一次
    public void processTimeoutOrder() {
        log.info("处理超时订单任务执行：{}", LocalDateTime.now());
        List<Orders> orders = orderMapper.getByStatusAndTime(Orders.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(15));
        if (orders != null && orders.size() > 0) {
            for (Orders order : orders) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("支付超时，取消订单");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
                log.info("取消订单：{}", order.getId());
            }
        }
    }

    @Scheduled(cron = "0 0 1 * * ?") // 每日1点执行一次
//    @Scheduled(cron = "*/5 * * * * ?") // 每隔5秒执行一次
    public void processDeliveryOrder() {
        log.info("处理处于待派送状态的订单:{}", LocalDateTime.now());
        List<Orders> orders = orderMapper.getByStatusAndTime(Orders.DELIVERY_IN_PROGRESS, LocalDateTime.now().minusMinutes(60));
        if (orders != null && orders.size() > 0) {
            for (Orders order : orders) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
                log.info("订单状态修改为已完成：{}", order.getId());
            }
        }
    }
}
