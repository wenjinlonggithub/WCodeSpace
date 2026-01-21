package com.architecture.gateway.predicate;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * 自定义时间段断言工厂
 *
 * 核心原理：
 * - 继承AbstractRoutePredicateFactory
 * - 实现apply方法返回断言逻辑
 * - 支持配置参数
 *
 * 业务场景：
 * 1. 限时活动：只在特定时间段开放
 * 2. 系统维护：维护时间拒绝访问
 * 3. 分时收费：不同时段走不同路由
 */
@Component
public class TimeBasedRoutePredicateFactory
        extends AbstractRoutePredicateFactory<TimeBasedRoutePredicateFactory.Config> {

    public TimeBasedRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            LocalDateTime now = LocalDateTime.now();

            // 工作日判断
            if (config.isWorkdayOnly()) {
                DayOfWeek dayOfWeek = now.getDayOfWeek();
                if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
                    return false;
                }
            }

            // 时间段判断
            int currentHour = now.getHour();
            return currentHour >= config.getStartHour() && currentHour < config.getEndHour();
        };
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("startHour", "endHour", "workdayOnly");
    }

    /**
     * 配置类
     */
    public static class Config {
        private int startHour = 0;
        private int endHour = 24;
        private boolean workdayOnly = false;

        public int getStartHour() {
            return startHour;
        }

        public void setStartHour(int startHour) {
            this.startHour = startHour;
        }

        public int getEndHour() {
            return endHour;
        }

        public void setEndHour(int endHour) {
            this.endHour = endHour;
        }

        public boolean isWorkdayOnly() {
            return workdayOnly;
        }

        public void setWorkdayOnly(boolean workdayOnly) {
            this.workdayOnly = workdayOnly;
        }
    }
}

/**
 * 使用示例（application.yml）：
 *
 * spring:
 *   cloud:
 *     gateway:
 *       routes:
 *         # 限时秒杀活动路由
 *         - id: flash_sale_route
 *           uri: lb://sale-service
 *           predicates:
 *             - Path=/api/flash-sale/**
 *             # 只在工作日的9:00-18:00开放
 *             - TimeBased=9,18,true
 *
 *         # 夜间维护路由
 *         - id: maintenance_route
 *           uri: lb://maintenance-service
 *           predicates:
 *             - Path=/api/**
 *             # 凌晨2:00-4:00走维护服务
 *             - TimeBased=2,4,false
 *
 * 业务案例：
 *
 * 1. 电商秒杀活动
 *    - 只在活动时间段开放秒杀入口
 *    - 其他时间返回活动未开始提示
 *
 * 2. 银行系统
 *    - 工作时间走实时处理
 *    - 非工作时间走异步批处理
 *
 * 3. 教育平台
 *    - 考试时间段禁止某些操作
 *    - 节假日关闭部分服务
 */
