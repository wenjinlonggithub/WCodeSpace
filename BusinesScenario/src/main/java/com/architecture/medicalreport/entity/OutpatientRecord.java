package com.architecture.medicalreport.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 门诊记录实体
 *
 * @author Medical Report System
 * @since 2025-01-16
 */
@Data
public class OutpatientRecord {

    /** 主键ID */
    private Long id;

    /** 记录ID */
    private String recordId;

    /** 就诊日期 */
    private LocalDate recordDate;

    /** 就诊时间 */
    private LocalDateTime recordTime;

    /** 患者ID */
    private String patientId;

    /** 患者姓名 */
    private String patientName;

    /** 年龄 */
    private Integer patientAge;

    /** 性别 M:男 F:女 */
    private String patientGender;

    /** 医生ID */
    private String doctorId;

    /** 医生姓名 */
    private String doctorName;

    /** 科室ID */
    private String departmentId;

    /** 科室名称 */
    private String departmentName;

    /** 诊断 */
    private String diagnosis;

    /** 处方金额 */
    private BigDecimal prescriptionAmount;

    /** 检查费 */
    private BigDecimal examAmount;

    /** 治疗费 */
    private BigDecimal treatmentAmount;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 医保类型 self:自费 medical:医保 commercial:商业保险 */
    private String insuranceType;

    /** 医保支付金额 */
    private BigDecimal insuranceAmount;

    /** 自费金额 */
    private BigDecimal selfAmount;

    /** 支付状态 0:未支付 1:已支付 2:已退费 */
    private Integer paymentStatus;

    /** 支付方式 cash:现金 alipay:支付宝 wechat:微信 card:银行卡 */
    private String paymentMethod;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
