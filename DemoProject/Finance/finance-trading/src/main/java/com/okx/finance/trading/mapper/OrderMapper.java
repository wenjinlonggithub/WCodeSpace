package com.okx.finance.trading.mapper;

import com.okx.finance.common.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Insert("INSERT INTO `order` (id, user_id, order_id, symbol, order_type, side, price, quantity, " +
            "executed_quantity, executed_amount, status, time_in_force, create_time) " +
            "VALUES (#{id}, #{userId}, #{orderId}, #{symbol}, #{orderType}, #{side}, #{price}, #{quantity}, " +
            "#{executedQuantity}, #{executedAmount}, #{status}, #{timeInForce}, NOW())")
    void insert(Order order);

    @Select("SELECT * FROM `order` WHERE order_id = #{orderId} AND deleted = 0")
    Order findByOrderId(String orderId);

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<Order> findByUserId(Long userId);

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} AND symbol = #{symbol} AND deleted = 0 ORDER BY create_time DESC")
    List<Order> findByUserIdAndSymbol(@Param("userId") Long userId, @Param("symbol") String symbol);

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} AND status = #{status} AND deleted = 0 ORDER BY create_time DESC")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} AND symbol = #{symbol} AND status = #{status} " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<Order> findByUserIdAndSymbolAndStatus(@Param("userId") Long userId,
                                                 @Param("symbol") String symbol,
                                                 @Param("status") String status);

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} AND status IN ('NEW', 'PARTIALLY_FILLED') " +
            "AND deleted = 0 ORDER BY create_time DESC")
    List<Order> findOpenOrdersByUserId(Long userId);

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} AND symbol = #{symbol} " +
            "AND status IN ('NEW', 'PARTIALLY_FILLED') AND deleted = 0 ORDER BY create_time DESC")
    List<Order> findOpenOrdersByUserIdAndSymbol(@Param("userId") Long userId, @Param("symbol") String symbol);

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} AND deleted = 0 " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Order> findHistoryByUserId(@Param("userId") Long userId,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    @Select("SELECT * FROM `order` WHERE user_id = #{userId} AND symbol = #{symbol} AND deleted = 0 " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Order> findHistoryByUserIdAndSymbol(@Param("userId") Long userId,
                                              @Param("symbol") String symbol,
                                              @Param("offset") int offset,
                                              @Param("size") int size);

    @Update("UPDATE `order` SET executed_quantity = #{executedQuantity}, executed_amount = #{executedAmount}, " +
            "status = #{status}, update_time = NOW() WHERE id = #{id}")
    void update(Order order);
}
