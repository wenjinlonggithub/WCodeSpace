package com.learning.mybatis.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * MyBatis配置类
 * 
 * 配置MyBatis相关的Bean和事务管理
 */
@Configuration
@EnableTransactionManagement
@MapperScan("com.learning.mybatis.mapper")
public class MyBatisConfig {
    
    @Autowired
    private DataSource dataSource;
    
    /**
     * 配置SqlSessionFactory
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        
        // 设置MyBatis配置文件位置
        factoryBean.setConfigLocation(
            new PathMatchingResourcePatternResolver().getResource("classpath:mybatis-config.xml"));
        
        // 设置mapper xml文件位置
        factoryBean.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml"));
        
        // 设置类型别名包
        factoryBean.setTypeAliasesPackage("com.learning.mybatis.entity");
        
        return factoryBean.getObject();
    }
    
    /**
     * 配置事务管理器
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }
}