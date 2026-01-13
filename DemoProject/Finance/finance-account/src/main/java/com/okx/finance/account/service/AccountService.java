package com.okx.finance.account.service;

import com.okx.finance.account.dto.TransferRequest;
import com.okx.finance.account.mapper.AccountMapper;
import com.okx.finance.common.dto.Result;
import com.okx.finance.common.entity.Account;
import com.okx.finance.common.util.JwtUtil;
import com.okx.finance.common.util.SnowflakeIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户服务类
 * 处理用户账户相关的核心业务逻辑
 *
 * <p>主要功能：
 * 1. 余额查询：查询单币种余额、查询所有币种余额
 * 2. 账户划转：不同币种之间的资金转换
 * 3. 资金冻结：下单时冻结资金
 * 4. 资金解冻：撤单时解冻资金
 * 5. 账户初始化：自动为用户创建新币种账户
 *
 * <p>账户体系：
 * - 每个用户可以拥有多个币种账户（BTC、ETH、USDT等）
 * - 每个账户包含：可用余额（availableBalance）+ 冻结余额（frozenBalance）= 总余额（totalBalance）
 * - 账户类型：1-现货账户、2-合约账户、3-杠杆账户
 *
 * <p>资金流转：
 * <pre>
 * 下单流程：
 *   可用余额 → 冻结余额（资金被锁定，不可用于其他交易）
 *
 * 成交流程：
 *   冻结余额 → 扣除（资金从系统中减少）
 *   对方账户 ← 增加（资金转移到对手方）
 *
 * 撤单流程：
 *   冻结余额 → 可用余额（资金解锁，可重新使用）
 * </pre>
 *
 * <p>事务处理：
 * - transfer、freeze、unfreeze方法使用@Transactional保证数据一致性
 * - 使用FOR UPDATE锁定行记录，防止并发问题
 * - 余额不足时回滚事务
 *
 * <p>安全措施：
 * - 所有操作都需要Token验证
 * - 使用数据库行锁防止并发更新导致的数据不一致
 * - 金额计算使用BigDecimal，避免浮点数精度问题
 * - 余额检查，防止负数余额出现
 *
 * @author OKX Finance Team
 * @version 1.0
 * @see AccountMapper
 * @see Account
 * @see SnowflakeIdGenerator
 */
@Service
public class AccountService {

    /**
     * 账户数据访问对象
     * 用于执行数据库操作（增删改查）
     */
    @Autowired
    private AccountMapper accountMapper;

    /**
     * 雪花算法ID生成器
     * 机器ID为2，用于生成账户ID
     * 保证账户ID全局唯一且趋势递增
     */
    private final SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(2);

    /**
     * 查询单币种余额
     * 获取用户指定币种的账户余额信息
     *
     * <p>查询流程：
     * 1. 验证Token有效性（身份认证）
     * 2. 从Token中提取用户ID
     * 3. 查询指定币种的账户信息
     * 4. 如果账户不存在，自动创建初始账户
     * 5. 返回账户信息
     *
     * <p>自动创建账户：
     * - 如果用户首次查询某个币种的余额，系统会自动创建该币种账户
     * - 初始余额为0（availableBalance=0, frozenBalance=0, totalBalance=0）
     * - 账户类型默认为1（现货账户）
     *
     * <p>返回字段：
     * - id：账户ID
     * - userId：用户ID
     * - currency：币种（如BTC、ETH、USDT）
     * - availableBalance：可用余额
     * - frozenBalance：冻结余额
     * - totalBalance：总余额
     * - accountType：账户类型（1-现货，2-合约，3-杠杆）
     *
     * @param token JWT Token，用于身份验证
     * @param currency 币种代码（如BTC、ETH、USDT）
     * @return Result对象，成功返回账户信息，失败返回错误信息
     *         - 401 "Invalid token"：Token无效或过期
     */
    public Result<?> getBalance(String token, String currency) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        Account account = accountMapper.findByUserIdAndCurrency(userId, currency);

        if (account == null) {
            account = createAccount(userId, currency);
        }

        return Result.success(account);
    }

    /**
     * 查询所有币种余额
     * 获取用户所有币种的账户余额信息
     *
     * <p>查询流程：
     * 1. 验证Token有效性（身份认证）
     * 2. 从Token中提取用户ID
     * 3. 查询该用户的所有账户
     * 4. 返回账户列表
     *
     * <p>使用场景：
     * - 资产总览页面
     * - 计算用户总资产价值
     * - 导出账户报表
     *
     * <p>返回数据：
     * 账户列表，每个账户包含：
     * - id：账户ID
     * - userId：用户ID
     * - currency：币种
     * - availableBalance：可用余额
     * - frozenBalance：冻结余额
     * - totalBalance：总余额
     * - accountType：账户类型
     *
     * @param token JWT Token，用于身份验证
     * @return Result对象，成功返回账户列表，失败返回错误信息
     *         - 401 "Invalid token"：Token无效或过期
     */
    public Result<?> getAllBalances(String token) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        List<Account> accounts = accountMapper.findByUserId(userId);

        return Result.success(accounts);
    }

    /**
     * 账户划转
     * 将资金从一个币种账户转移到另一个币种账户（带汇率转换）
     *
     * <p>划转流程：
     * 1. 验证Token有效性（身份认证）
     * 2. 从Token中提取用户ID
     * 3. 查询源账户（使用FOR UPDATE行锁）
     * 4. 检查源账户余额是否充足
     * 5. 查询或创建目标账户（使用FOR UPDATE行锁）
     * 6. 从源账户扣除金额
     * 7. 按汇率计算目标金额
     * 8. 向目标账户增加金额
     * 9. 更新数据库
     * 10. 提交事务
     *
     * <p>汇率转换：
     * - 目标金额 = 源金额 × 汇率
     * - 例如：转账100 USDT，汇率0.000016（1 USDT = 0.000016 BTC），目标账户将收到0.0016 BTC
     * - 汇率由前端传入，建议从市场数据服务获取实时汇率
     *
     * <p>事务处理：
     * - 使用@Transactional注解保证原子性
     * - 使用FOR UPDATE行锁防止并发问题
     * - 任何步骤失败都会回滚整个事务
     *
     * <p>余额关系：
     * 源账户：
     * - availableBalance减少
     * - totalBalance减少
     * - frozenBalance不变
     *
     * 目标账户：
     * - availableBalance增加
     * - totalBalance增加
     * - frozenBalance不变
     *
     * <p>安全措施：
     * - 余额检查，防止透支
     * - 行锁机制，防止并发问题
     * - BigDecimal计算，避免精度损失
     *
     * @param token JWT Token，用于身份验证
     * @param request 划转请求，包含源币种、目标币种、金额、汇率
     * @return Result对象，成功返回"Transfer successful"，失败返回错误信息
     *         - 401 "Invalid token"：Token无效或过期
     *         - "Account not found"：源账户不存在
     *         - "Insufficient balance"：余额不足
     */
    @Transactional
    public Result<?> transfer(String token, TransferRequest request) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        Account fromAccount = accountMapper.findByUserIdAndCurrencyForUpdate(userId, request.getFromCurrency());

        if (fromAccount == null) {
            return Result.error("Account not found");
        }

        BigDecimal amount = new BigDecimal(request.getAmount());
        if (fromAccount.getAvailableBalance().compareTo(amount) < 0) {
            return Result.error("Insufficient balance");
        }

        Account toAccount = accountMapper.findByUserIdAndCurrencyForUpdate(userId, request.getToCurrency());
        if (toAccount == null) {
            toAccount = createAccount(userId, request.getToCurrency());
        }

        fromAccount.setAvailableBalance(fromAccount.getAvailableBalance().subtract(amount));
        fromAccount.setTotalBalance(fromAccount.getTotalBalance().subtract(amount));
        accountMapper.update(fromAccount);

        BigDecimal convertedAmount = amount.multiply(new BigDecimal(request.getRate()));
        toAccount.setAvailableBalance(toAccount.getAvailableBalance().add(convertedAmount));
        toAccount.setTotalBalance(toAccount.getTotalBalance().add(convertedAmount));
        accountMapper.update(toAccount);

        return Result.success("Transfer successful");
    }

    /**
     * 冻结资金
     * 将可用余额转移到冻结余额（用于下单）
     *
     * <p>冻结流程：
     * 1. 验证Token有效性（身份认证）
     * 2. 从Token中提取用户ID
     * 3. 查询账户（使用FOR UPDATE行锁）
     * 4. 检查可用余额是否充足
     * 5. 从可用余额中扣除金额
     * 6. 向冻结余额中增加金额
     * 7. 更新数据库
     * 8. 提交事务
     *
     * <p>使用场景：
     * - 下单时：冻结足够的资金以保证订单可以成交
     *   - BUY订单：冻结计价货币（USDT），金额 = price × quantity
     *   - SELL订单：冻结基础货币（BTC），金额 = quantity
     * - 提现时：冻结提现金额，防止重复提现
     *
     * <p>余额关系：
     * - availableBalance减少（资金被锁定）
     * - frozenBalance增加（资金进入冻结状态）
     * - totalBalance不变（只是状态转换）
     *
     * <p>事务处理：
     * - 使用@Transactional注解保证原子性
     * - 使用FOR UPDATE行锁防止并发问题
     * - 余额不足时回滚事务
     *
     * <p>示例：
     * 用户下单买入1 BTC，价格50000 USDT
     * - 需要冻结50000 USDT
     * - USDT账户：availableBalance -= 50000, frozenBalance += 50000
     *
     * @param token JWT Token，用于身份验证
     * @param currency 币种代码（如BTC、ETH、USDT）
     * @param amount 冻结金额（字符串格式，避免精度问题）
     * @return Result对象，成功返回"Freeze successful"，失败返回错误信息
     *         - 401 "Invalid token"：Token无效或过期
     *         - "Account not found"：账户不存在
     *         - "Insufficient balance"：可用余额不足
     */
    @Transactional
    public Result<?> freeze(String token, String currency, String amount) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        Account account = accountMapper.findByUserIdAndCurrencyForUpdate(userId, currency);

        if (account == null) {
            return Result.error("Account not found");
        }

        BigDecimal freezeAmount = new BigDecimal(amount);
        if (account.getAvailableBalance().compareTo(freezeAmount) < 0) {
            return Result.error("Insufficient balance");
        }

        account.setAvailableBalance(account.getAvailableBalance().subtract(freezeAmount));
        account.setFrozenBalance(account.getFrozenBalance().add(freezeAmount));
        accountMapper.update(account);

        return Result.success("Freeze successful");
    }

    /**
     * 解冻资金
     * 将冻结余额转移回可用余额（用于撤单）
     *
     * <p>解冻流程：
     * 1. 验证Token有效性（身份认证）
     * 2. 从Token中提取用户ID
     * 3. 查询账户（使用FOR UPDATE行锁）
     * 4. 检查冻结余额是否充足
     * 5. 从冻结余额中扣除金额
     * 6. 向可用余额中增加金额
     * 7. 更新数据库
     * 8. 提交事务
     *
     * <p>使用场景：
     * - 撤单时：解冻之前冻结的资金
     *   - 完全撤单：解冻全部冻结金额
     *   - 部分成交后撤单：解冻未成交部分的金额
     * - 订单过期：自动解冻超时订单的资金
     * - 提现失败：解冻提现金额
     *
     * <p>余额关系：
     * - frozenBalance减少（资金解除锁定）
     * - availableBalance增加（资金恢复可用）
     * - totalBalance不变（只是状态转换）
     *
     * <p>事务处理：
     * - 使用@Transactional注解保证原子性
     * - 使用FOR UPDATE行锁防止并发问题
     * - 冻结余额不足时回滚事务
     *
     * <p>示例：
     * 用户撤销买单（1 BTC，价格50000 USDT）
     * - 需要解冻50000 USDT
     * - USDT账户：frozenBalance -= 50000, availableBalance += 50000
     *
     * @param token JWT Token，用于身份验证
     * @param currency 币种代码（如BTC、ETH、USDT）
     * @param amount 解冻金额（字符串格式，避免精度问题）
     * @return Result对象，成功返回"Unfreeze successful"，失败返回错误信息
     *         - 401 "Invalid token"：Token无效或过期
     *         - "Account not found"：账户不存在
     *         - "Insufficient frozen balance"：冻结余额不足
     */
    @Transactional
    public Result<?> unfreeze(String token, String currency, String amount) {
        if (!JwtUtil.validateToken(token)) {
            return Result.error(401, "Invalid token");
        }

        Long userId = JwtUtil.getUserId(token);
        Account account = accountMapper.findByUserIdAndCurrencyForUpdate(userId, currency);

        if (account == null) {
            return Result.error("Account not found");
        }

        BigDecimal unfreezeAmount = new BigDecimal(amount);
        if (account.getFrozenBalance().compareTo(unfreezeAmount) < 0) {
            return Result.error("Insufficient frozen balance");
        }

        account.setFrozenBalance(account.getFrozenBalance().subtract(unfreezeAmount));
        account.setAvailableBalance(account.getAvailableBalance().add(unfreezeAmount));
        accountMapper.update(account);

        return Result.success("Unfreeze successful");
    }

    /**
     * 创建账户（内部方法）
     * 为用户创建新的币种账户
     *
     * <p>创建流程：
     * 1. 生成唯一账户ID（雪花算法）
     * 2. 设置用户ID和币种
     * 3. 初始化余额为0
     * 4. 设置账户类型为1（现货账户）
     * 5. 保存到数据库
     * 6. 返回账户对象
     *
     * <p>初始值：
     * - id：雪花算法生成的唯一ID
     * - userId：用户ID
     * - currency：币种代码
     * - availableBalance：0
     * - frozenBalance：0
     * - totalBalance：0
     * - accountType：1（现货账户）
     *
     * <p>调用时机：
     * - getBalance方法中，如果账户不存在则自动创建
     * - transfer方法中，如果目标账户不存在则自动创建
     *
     * <p>注意事项：
     * - 这是一个私有方法，仅供内部调用
     * - 不验证Token，调用方需要先验证
     * - 不检查币种是否合法，调用方需要保证
     *
     * @param userId 用户ID
     * @param currency 币种代码（如BTC、ETH、USDT）
     * @return 新创建的账户对象
     */
    private Account createAccount(Long userId, String currency) {
        Account account = new Account();
        account.setId(idGenerator.nextId());
        account.setUserId(userId);
        account.setCurrency(currency);
        account.setAvailableBalance(BigDecimal.ZERO);
        account.setFrozenBalance(BigDecimal.ZERO);
        account.setTotalBalance(BigDecimal.ZERO);
        account.setAccountType(1);

        accountMapper.insert(account);
        return account;
    }
}
