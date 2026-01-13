package com.okx.finance.common.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 * 存储用户的基本信息、认证信息和API密钥
 *
 * <p>用户状态流转：
 * 注册 → 邮箱验证 → KYC认证 → 正常使用
 *
 * <p>KYC等级说明：
 * 0 - 未认证（限制交易额度）
 * 1 - 初级认证（基本实名）
 * 2 - 中级认证（身份证认证）
 * 3 - 高级认证（视频认证）
 *
 * @author OKX Finance Team
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    /**
     * 用户名
     * 长度：3-50个字符
     * 唯一约束，用于登录
     */
    private String username;

    /**
     * 密码
     * 存储MD5加密后的密码哈希值
     * 原始密码不存储在数据库中
     */
    private String password;

    /**
     * 邮箱地址
     * 用于接收通知和找回密码
     * 建议使用唯一约束
     */
    private String email;

    /**
     * 手机号码
     * 用于短信验证和通知
     * 格式：11位中国大陆手机号
     */
    private String phone;

    /**
     * 真实姓名
     * KYC认证时填写，用于实名认证
     */
    private String realName;

    /**
     * 身份证号码
     * KYC认证时填写，需要符合中国身份证格式
     * 存储时建议加密
     */
    private String idCard;

    /**
     * KYC认证等级
     * 0-未认证，1-初级，2-中级，3-高级
     * 等级越高，交易额度越大
     */
    private Integer kycLevel;

    /**
     * 账户状态
     * 0-禁用（冻结），1-正常，2-锁定（异常登录）
     * 禁用状态无法登录和交易
     */
    private Integer status;

    /**
     * API密钥
     * 用于API接口调用的身份标识
     * 32位随机字符串，不重复
     */
    private String apiKey;

    /**
     * API密钥密码
     * 存储MD5加密后的密钥哈希值
     * 配合apiKey使用，用于API签名验证
     */
    private String apiSecret;
}
