package com.architecture.cqrs.query;

/**
 * CQRS - 用户订单列表查询
 */
public class UserOrdersQuery {

    private final String userId;

    public UserOrdersQuery(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
