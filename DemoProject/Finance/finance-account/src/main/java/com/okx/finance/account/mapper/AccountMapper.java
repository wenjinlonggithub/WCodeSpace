package com.okx.finance.account.mapper;

import com.okx.finance.common.entity.Account;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AccountMapper {

    @Insert("INSERT INTO account (id, user_id, currency, available_balance, frozen_balance, total_balance, account_type, create_time) " +
            "VALUES (#{id}, #{userId}, #{currency}, #{availableBalance}, #{frozenBalance}, #{totalBalance}, #{accountType}, NOW())")
    void insert(Account account);

    @Select("SELECT * FROM account WHERE user_id = #{userId} AND currency = #{currency} AND deleted = 0")
    Account findByUserIdAndCurrency(@Param("userId") Long userId, @Param("currency") String currency);

    @Select("SELECT * FROM account WHERE user_id = #{userId} AND currency = #{currency} AND deleted = 0 FOR UPDATE")
    Account findByUserIdAndCurrencyForUpdate(@Param("userId") Long userId, @Param("currency") String currency);

    @Select("SELECT * FROM account WHERE user_id = #{userId} AND deleted = 0")
    List<Account> findByUserId(Long userId);

    @Update("UPDATE account SET available_balance = #{availableBalance}, frozen_balance = #{frozenBalance}, " +
            "total_balance = #{totalBalance}, update_time = NOW() WHERE id = #{id}")
    void update(Account account);
}
