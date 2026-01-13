-- OKX Finance System Database Schema

-- Create databases
CREATE DATABASE IF NOT EXISTS okx_user CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS okx_account CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS okx_trading CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS okx_wallet CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================
-- User Service Database
-- ============================================
USE okx_user;

CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT 'User ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Username',
    `password` VARCHAR(100) NOT NULL COMMENT 'Password hash',
    `email` VARCHAR(100) COMMENT 'Email',
    `phone` VARCHAR(20) COMMENT 'Phone number',
    `real_name` VARCHAR(50) COMMENT 'Real name',
    `id_card` VARCHAR(30) COMMENT 'ID card number',
    `kyc_level` TINYINT DEFAULT 0 COMMENT 'KYC level: 0-未认证, 1-初级, 2-中级, 3-高级',
    `status` TINYINT DEFAULT 1 COMMENT 'Status: 0-禁用, 1-正常',
    `api_key` VARCHAR(50) COMMENT 'API Key',
    `api_secret` VARCHAR(100) COMMENT 'API Secret hash',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `deleted` TINYINT DEFAULT 0 COMMENT 'Soft delete flag',
    INDEX `idx_username` (`username`),
    INDEX `idx_email` (`email`),
    INDEX `idx_api_key` (`api_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User table';

-- ============================================
-- Account Service Database
-- ============================================
USE okx_account;

CREATE TABLE IF NOT EXISTS `account` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT 'Account ID',
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `currency` VARCHAR(20) NOT NULL COMMENT 'Currency code',
    `available_balance` DECIMAL(36, 18) DEFAULT 0 COMMENT 'Available balance',
    `frozen_balance` DECIMAL(36, 18) DEFAULT 0 COMMENT 'Frozen balance',
    `total_balance` DECIMAL(36, 18) DEFAULT 0 COMMENT 'Total balance',
    `account_type` TINYINT DEFAULT 1 COMMENT 'Account type: 1-现货, 2-合约, 3-杠杆',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `deleted` TINYINT DEFAULT 0 COMMENT 'Soft delete flag',
    UNIQUE KEY `uk_user_currency` (`user_id`, `currency`, `account_type`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_currency` (`currency`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Account table';

CREATE TABLE IF NOT EXISTS `transaction` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT 'Transaction ID',
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `account_id` BIGINT NOT NULL COMMENT 'Account ID',
    `transaction_type` VARCHAR(20) NOT NULL COMMENT 'Transaction type',
    `currency` VARCHAR(20) NOT NULL COMMENT 'Currency code',
    `amount` DECIMAL(36, 18) NOT NULL COMMENT 'Amount',
    `balance_after` DECIMAL(36, 18) NOT NULL COMMENT 'Balance after transaction',
    `status` VARCHAR(20) DEFAULT 'SUCCESS' COMMENT 'Status',
    `reference_id` VARCHAR(100) COMMENT 'Reference ID',
    `remark` VARCHAR(200) COMMENT 'Remark',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_account_id` (`account_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Transaction history table';

-- ============================================
-- Trading Service Database
-- ============================================
USE okx_trading;

CREATE TABLE IF NOT EXISTS `order` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT 'Order ID',
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `order_id` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Order ID string',
    `symbol` VARCHAR(20) NOT NULL COMMENT 'Trading pair',
    `order_type` VARCHAR(20) NOT NULL COMMENT 'Order type: LIMIT, MARKET, etc.',
    `side` VARCHAR(10) NOT NULL COMMENT 'Side: BUY, SELL',
    `price` DECIMAL(36, 18) COMMENT 'Price',
    `quantity` DECIMAL(36, 18) NOT NULL COMMENT 'Quantity',
    `executed_quantity` DECIMAL(36, 18) DEFAULT 0 COMMENT 'Executed quantity',
    `executed_amount` DECIMAL(36, 18) DEFAULT 0 COMMENT 'Executed amount',
    `status` VARCHAR(20) NOT NULL COMMENT 'Order status',
    `time_in_force` VARCHAR(10) DEFAULT 'GTC' COMMENT 'Time in force',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    `deleted` TINYINT DEFAULT 0 COMMENT 'Soft delete flag',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_symbol` (`symbol`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Order table';

CREATE TABLE IF NOT EXISTS `trade` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT 'Trade ID',
    `trade_id` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Trade ID string',
    `order_id` VARCHAR(50) NOT NULL COMMENT 'Order ID',
    `symbol` VARCHAR(20) NOT NULL COMMENT 'Trading pair',
    `price` DECIMAL(36, 18) NOT NULL COMMENT 'Trade price',
    `quantity` DECIMAL(36, 18) NOT NULL COMMENT 'Trade quantity',
    `side` VARCHAR(10) NOT NULL COMMENT 'Side',
    `fee` DECIMAL(36, 18) DEFAULT 0 COMMENT 'Fee',
    `fee_currency` VARCHAR(20) COMMENT 'Fee currency',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Trade time',
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_symbol` (`symbol`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Trade table';

-- ============================================
-- Wallet Service Database
-- ============================================
USE okx_wallet;

CREATE TABLE IF NOT EXISTS `deposit_address` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT 'Address ID',
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `currency` VARCHAR(20) NOT NULL COMMENT 'Currency code',
    `chain` VARCHAR(20) NOT NULL COMMENT 'Blockchain',
    `address` VARCHAR(100) NOT NULL COMMENT 'Deposit address',
    `tag` VARCHAR(50) COMMENT 'Address tag/memo',
    `status` TINYINT DEFAULT 1 COMMENT 'Status: 0-禁用, 1-正常',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    UNIQUE KEY `uk_user_currency` (`user_id`, `currency`, `chain`),
    INDEX `idx_address` (`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Deposit address table';

CREATE TABLE IF NOT EXISTS `deposit` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT 'Deposit ID',
    `deposit_id` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Deposit ID string',
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `currency` VARCHAR(20) NOT NULL COMMENT 'Currency code',
    `chain` VARCHAR(20) NOT NULL COMMENT 'Blockchain',
    `amount` DECIMAL(36, 18) NOT NULL COMMENT 'Deposit amount',
    `tx_hash` VARCHAR(100) COMMENT 'Transaction hash',
    `address` VARCHAR(100) NOT NULL COMMENT 'Deposit address',
    `confirmations` INT DEFAULT 0 COMMENT 'Confirmations',
    `required_confirmations` INT DEFAULT 6 COMMENT 'Required confirmations',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'Status: PENDING, CONFIRMED, FAILED',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_tx_hash` (`tx_hash`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Deposit table';

CREATE TABLE IF NOT EXISTS `withdrawal` (
    `id` BIGINT NOT NULL PRIMARY KEY COMMENT 'Withdrawal ID',
    `withdrawal_id` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Withdrawal ID string',
    `user_id` BIGINT NOT NULL COMMENT 'User ID',
    `currency` VARCHAR(20) NOT NULL COMMENT 'Currency code',
    `chain` VARCHAR(20) NOT NULL COMMENT 'Blockchain',
    `amount` DECIMAL(36, 18) NOT NULL COMMENT 'Withdrawal amount',
    `fee` DECIMAL(36, 18) DEFAULT 0 COMMENT 'Withdrawal fee',
    `actual_amount` DECIMAL(36, 18) NOT NULL COMMENT 'Actual amount',
    `address` VARCHAR(100) NOT NULL COMMENT 'Withdrawal address',
    `tag` VARCHAR(50) COMMENT 'Address tag/memo',
    `tx_hash` VARCHAR(100) COMMENT 'Transaction hash',
    `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT 'Status: PENDING, PROCESSING, COMPLETED, FAILED, REJECTED',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_tx_hash` (`tx_hash`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Withdrawal table';

-- ============================================
-- Insert sample data for testing
-- ============================================

USE okx_user;
-- Sample user (password: 123456, MD5 hash)
INSERT INTO `user` (`id`, `username`, `password`, `email`, `phone`, `kyc_level`, `status`)
VALUES (1000000000000001, 'test_user', 'e10adc3949ba59abbe56e057f20f883e', 'test@okx.com', '13800138000', 1, 1);
