package com.architecture.engine;

import com.architecture.index.BPlusTree;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MySQL查询执行器模拟实现
 * 核心功能：
 * 1. SQL解析和语法分析
 * 2. 查询优化器（基于成本的优化）
 * 3. 执行计划生成
 * 4. 执行器（各种算子实现）
 * 5. 索引选择和使用
 * 6. Join算法实现
 */
public class QueryExecutor {
    
    private final QueryOptimizer optimizer;
    private final ExecutionEngine executionEngine;
    private final IndexManager indexManager;
    private final StatisticsManager statisticsManager;
    
    public QueryExecutor() {
        this.statisticsManager = new StatisticsManager();
        this.indexManager = new IndexManager();
        this.optimizer = new QueryOptimizer(statisticsManager, indexManager);
        this.executionEngine = new ExecutionEngine(indexManager);
    }
    
    /**
     * SQL查询类型
     */
    public enum QueryType {
        SELECT, INSERT, UPDATE, DELETE
    }
    
    /**
     * 简化的SQL查询对象
     */
    public static class SQLQuery {
        private final QueryType type;
        private final String tableName;
        private final List<String> selectColumns;
        private final List<WhereCondition> whereConditions;
        private final List<String> orderByColumns;
        private final Integer limit;
        private final Map<String, Object> insertValues;
        private final Map<String, Object> updateValues;
        
        private SQLQuery(Builder builder) {
            this.type = builder.type;
            this.tableName = builder.tableName;
            this.selectColumns = builder.selectColumns != null ? 
                new ArrayList<>(builder.selectColumns) : new ArrayList<>();
            this.whereConditions = builder.whereConditions != null ? 
                new ArrayList<>(builder.whereConditions) : new ArrayList<>();
            this.orderByColumns = builder.orderByColumns != null ? 
                new ArrayList<>(builder.orderByColumns) : new ArrayList<>();
            this.limit = builder.limit;
            this.insertValues = builder.insertValues != null ? 
                new HashMap<>(builder.insertValues) : new HashMap<>();
            this.updateValues = builder.updateValues != null ? 
                new HashMap<>(builder.updateValues) : new HashMap<>();
        }
        
        // Getters
        public QueryType getType() { return type; }
        public String getTableName() { return tableName; }
        public List<String> getSelectColumns() { return selectColumns; }
        public List<WhereCondition> getWhereConditions() { return whereConditions; }
        public List<String> getOrderByColumns() { return orderByColumns; }
        public Integer getLimit() { return limit; }
        public Map<String, Object> getInsertValues() { return insertValues; }
        public Map<String, Object> getUpdateValues() { return updateValues; }
        
        @Override
        public String toString() {
            return String.format("SQLQuery{type=%s, table=%s, where=%s}", 
                type, tableName, whereConditions);
        }
        
        public static class Builder {
            private QueryType type;
            private String tableName;
            private List<String> selectColumns;
            private List<WhereCondition> whereConditions;
            private List<String> orderByColumns;
            private Integer limit;
            private Map<String, Object> insertValues;
            private Map<String, Object> updateValues;
            
            public Builder select(String tableName) {
                this.type = QueryType.SELECT;
                this.tableName = tableName;
                return this;
            }
            
            public Builder columns(String... columns) {
                this.selectColumns = Arrays.asList(columns);
                return this;
            }
            
            public Builder where(String column, String operator, Object value) {
                if (whereConditions == null) {
                    whereConditions = new ArrayList<>();
                }
                whereConditions.add(new WhereCondition(column, operator, value));
                return this;
            }
            
            public Builder orderBy(String... columns) {
                this.orderByColumns = Arrays.asList(columns);
                return this;
            }
            
            public Builder limit(int limit) {
                this.limit = limit;
                return this;
            }
            
            public Builder insert(String tableName, Map<String, Object> values) {
                this.type = QueryType.INSERT;
                this.tableName = tableName;
                this.insertValues = values;
                return this;
            }
            
            public Builder update(String tableName, Map<String, Object> values) {
                this.type = QueryType.UPDATE;
                this.tableName = tableName;
                this.updateValues = values;
                return this;
            }
            
            public Builder delete(String tableName) {
                this.type = QueryType.DELETE;
                this.tableName = tableName;
                return this;
            }
            
            public SQLQuery build() {
                return new SQLQuery(this);
            }
        }
    }
    
    /**
     * WHERE条件
     */
    public static class WhereCondition {
        private final String column;
        private final String operator; // =, >, <, >=, <=, !=, IN, LIKE
        private final Object value;
        
        public WhereCondition(String column, String operator, Object value) {
            this.column = column;
            this.operator = operator;
            this.value = value;
        }
        
        public String getColumn() { return column; }
        public String getOperator() { return operator; }
        public Object getValue() { return value; }
        
        public boolean evaluate(Map<String, Object> row) {
            Object columnValue = row.get(column);
            if (columnValue == null) return false;
            
            switch (operator) {
                case "=":
                    return columnValue.equals(value);
                case ">":
                    return compareValues(columnValue, value) > 0;
                case "<":
                    return compareValues(columnValue, value) < 0;
                case ">=":
                    return compareValues(columnValue, value) >= 0;
                case "<=":
                    return compareValues(columnValue, value) <= 0;
                case "!=":
                    return !columnValue.equals(value);
                case "LIKE":
                    return columnValue.toString().contains(value.toString());
                default:
                    return false;
            }
        }
        
        @SuppressWarnings("unchecked")
        private int compareValues(Object a, Object b) {
            if (a instanceof Comparable && b instanceof Comparable) {
                return ((Comparable<Object>) a).compareTo(b);
            }
            return 0;
        }
        
        @Override
        public String toString() {
            return String.format("%s %s %s", column, operator, value);
        }
    }
    
    /**
     * 执行计划节点
     */
    public static abstract class ExecutionPlan {
        protected final String operatorType;
        protected final double estimatedCost;
        protected final long estimatedRows;
        
        public ExecutionPlan(String operatorType, double estimatedCost, long estimatedRows) {
            this.operatorType = operatorType;
            this.estimatedCost = estimatedCost;
            this.estimatedRows = estimatedRows;
        }
        
        public abstract List<Map<String, Object>> execute();
        
        public String getOperatorType() { return operatorType; }
        public double getEstimatedCost() { return estimatedCost; }
        public long getEstimatedRows() { return estimatedRows; }
        
        public void explain(int level) {
            String indent = "  ".repeat(level);
            System.out.printf("%s🔧 %s (cost=%.2f, rows=%d)%n", 
                indent, operatorType, estimatedCost, estimatedRows);
        }
    }
    
    /**
     * 表扫描执行计划
     */
    public static class TableScanPlan extends ExecutionPlan {
        private final String tableName;
        private final List<WhereCondition> conditions;
        private final List<Map<String, Object>> tableData;
        
        public TableScanPlan(String tableName, List<WhereCondition> conditions, 
                           List<Map<String, Object>> tableData, double cost, long rows) {
            super("TableScan", cost, rows);
            this.tableName = tableName;
            this.conditions = new ArrayList<>(conditions);
            this.tableData = new ArrayList<>(tableData);
        }
        
        @Override
        public List<Map<String, Object>> execute() {
            System.out.printf("🔍 执行表扫描: %s%n", tableName);
            
            List<Map<String, Object>> result = tableData;
            
            // 应用WHERE条件
            for (WhereCondition condition : conditions) {
                result = result.stream()
                    .filter(condition::evaluate)
                    .collect(Collectors.toList());
                System.out.printf("   应用条件 %s，剩余行数: %d%n", condition, result.size());
            }
            
            return result;
        }
        
        @Override
        public void explain(int level) {
            super.explain(level);
            String indent = "  ".repeat(level + 1);
            System.out.printf("%s表: %s%n", indent, tableName);
            if (!conditions.isEmpty()) {
                System.out.printf("%s条件: %s%n", indent, conditions);
            }
        }
    }
    
    /**
     * 索引扫描执行计划
     */
    public static class IndexScanPlan extends ExecutionPlan {
        private final String indexName;
        private final String tableName;
        private final WhereCondition indexCondition;
        private final BPlusTree<Integer, Map<String, Object>> index;
        
        public IndexScanPlan(String indexName, String tableName, WhereCondition indexCondition,
                           BPlusTree<Integer, Map<String, Object>> index, double cost, long rows) {
            super("IndexScan", cost, rows);
            this.indexName = indexName;
            this.tableName = tableName;
            this.indexCondition = indexCondition;
            this.index = index;
        }
        
        @Override
        public List<Map<String, Object>> execute() {
            System.out.printf("📇 执行索引扫描: %s.%s%n", tableName, indexName);
            
            List<Map<String, Object>> result = new ArrayList<>();
            
            if ("=".equals(indexCondition.getOperator())) {
                // 点查询
                Map<String, Object> row = index.search((Integer)indexCondition.getValue());
                if (row != null) {
                    result.add(row);
                }
                System.out.printf("   索引点查询 %s，找到行数: %d%n", indexCondition, result.size());
            } else {
                // 范围查询（简化实现）
                System.out.printf("   索引范围查询 %s（简化实现）%n", indexCondition);
                // 这里应该实现实际的范围查询
            }
            
            return result;
        }
        
        @Override
        public void explain(int level) {
            super.explain(level);
            String indent = "  ".repeat(level + 1);
            System.out.printf("%s索引: %s%n", indent, indexName);
            System.out.printf("%s条件: %s%n", indent, indexCondition);
        }
    }
    
    /**
     * 排序执行计划
     */
    public static class SortPlan extends ExecutionPlan {
        private final ExecutionPlan childPlan;
        private final List<String> sortColumns;
        
        public SortPlan(ExecutionPlan childPlan, List<String> sortColumns, double additionalCost) {
            super("Sort", childPlan.getEstimatedCost() + additionalCost, childPlan.getEstimatedRows());
            this.childPlan = childPlan;
            this.sortColumns = new ArrayList<>(sortColumns);
        }
        
        @Override
        public List<Map<String, Object>> execute() {
            System.out.printf("📊 执行排序: %s%n", sortColumns);
            
            List<Map<String, Object>> result = childPlan.execute();
            
            // 简化的排序实现
            result.sort((a, b) -> {
                for (String column : sortColumns) {
                    Object valueA = a.get(column);
                    Object valueB = b.get(column);
                    
                    if (valueA instanceof Comparable && valueB instanceof Comparable) {
                        @SuppressWarnings("unchecked")
                        int cmp = ((Comparable<Object>) valueA).compareTo(valueB);
                        if (cmp != 0) return cmp;
                    }
                }
                return 0;
            });
            
            System.out.printf("   排序完成，行数: %d%n", result.size());
            return result;
        }
        
        @Override
        public void explain(int level) {
            super.explain(level);
            String indent = "  ".repeat(level + 1);
            System.out.printf("%s排序列: %s%n", indent, sortColumns);
            childPlan.explain(level + 1);
        }
    }
    
    /**
     * 限制执行计划
     */
    public static class LimitPlan extends ExecutionPlan {
        private final ExecutionPlan childPlan;
        private final int limitCount;
        
        public LimitPlan(ExecutionPlan childPlan, int limitCount) {
            super("Limit", childPlan.getEstimatedCost(), Math.min(childPlan.getEstimatedRows(), limitCount));
            this.childPlan = childPlan;
            this.limitCount = limitCount;
        }
        
        @Override
        public List<Map<String, Object>> execute() {
            System.out.printf("🔢 执行限制: LIMIT %d%n", limitCount);
            
            List<Map<String, Object>> result = childPlan.execute();
            
            if (result.size() > limitCount) {
                result = result.subList(0, limitCount);
            }
            
            System.out.printf("   限制后行数: %d%n", result.size());
            return result;
        }
        
        @Override
        public void explain(int level) {
            super.explain(level);
            String indent = "  ".repeat(level + 1);
            System.out.printf("%s限制: %d%n", indent, limitCount);
            childPlan.explain(level + 1);
        }
    }
    
    /**
     * 查询优化器
     */
    public static class QueryOptimizer {
        private final StatisticsManager statisticsManager;
        private final IndexManager indexManager;
        
        public QueryOptimizer(StatisticsManager statisticsManager, IndexManager indexManager) {
            this.statisticsManager = statisticsManager;
            this.indexManager = indexManager;
        }
        
        /**
         * 优化查询并生成执行计划
         */
        public ExecutionPlan optimize(SQLQuery query) {
            System.out.printf("🧠 优化查询: %s%n", query);
            
            if (query.getType() != QueryType.SELECT) {
                throw new UnsupportedOperationException("目前只支持SELECT查询的优化");
            }
            
            ExecutionPlan plan = null;
            
            // 1. 选择访问路径（表扫描 vs 索引扫描）
            plan = selectAccessPath(query);
            
            // 2. 添加排序
            if (!query.getOrderByColumns().isEmpty()) {
                double sortCost = calculateSortCost(plan.getEstimatedRows());
                plan = new SortPlan(plan, query.getOrderByColumns(), sortCost);
            }
            
            // 3. 添加限制
            if (query.getLimit() != null) {
                plan = new LimitPlan(plan, query.getLimit());
            }
            
            System.out.printf("✅ 优化完成，总成本: %.2f%n", plan.getEstimatedCost());
            return plan;
        }
        
        /**
         * 选择访问路径
         */
        private ExecutionPlan selectAccessPath(SQLQuery query) {
            String tableName = query.getTableName();
            List<WhereCondition> conditions = query.getWhereConditions();
            
            // 获取表统计信息
            TableStatistics tableStats = statisticsManager.getTableStatistics(tableName);
            List<Map<String, Object>> tableData = generateSampleData(tableName, 1000); // 模拟数据
            
            if (conditions.isEmpty()) {
                // 无WHERE条件，只能全表扫描
                double cost = calculateTableScanCost(tableStats.getRowCount());
                return new TableScanPlan(tableName, conditions, tableData, cost, tableStats.getRowCount());
            }
            
            // 检查是否有可用索引
            for (WhereCondition condition : conditions) {
                if ("=".equals(condition.getOperator())) {
                    BPlusTree<Integer, Map<String, Object>> index = indexManager.getIndex(tableName, condition.getColumn());
                    if (index != null) {
                        // 使用索引扫描
                        double indexCost = calculateIndexScanCost(1); // 假设索引选择性很高
                        System.out.printf("   🎯 选择索引扫描: %s.%s%n", tableName, condition.getColumn());
                        return new IndexScanPlan(condition.getColumn() + "_idx", tableName, condition, index, indexCost, 1);
                    }
                }
            }
            
            // 使用表扫描
            double tableScanCost = calculateTableScanCost(tableStats.getRowCount());
            long estimatedRows = estimateRowsAfterFilter(tableStats.getRowCount(), conditions);
            System.out.printf("   📋 选择表扫描: %s%n", tableName);
            return new TableScanPlan(tableName, conditions, tableData, tableScanCost, estimatedRows);
        }
        
        /**
         * 计算表扫描成本
         */
        private double calculateTableScanCost(long rowCount) {
            return rowCount * 1.0; // 简化的成本模型：每行成本为1
        }
        
        /**
         * 计算索引扫描成本
         */
        private double calculateIndexScanCost(long estimatedRows) {
            return Math.log(estimatedRows) + estimatedRows * 0.1; // B+树搜索成本 + 数据访问成本
        }
        
        /**
         * 计算排序成本
         */
        private double calculateSortCost(long rowCount) {
            return rowCount * Math.log(rowCount) * 0.1; // O(n log n) 排序算法
        }
        
        /**
         * 估算过滤后的行数
         */
        private long estimateRowsAfterFilter(long totalRows, List<WhereCondition> conditions) {
            double selectivity = 1.0;
            for (WhereCondition condition : conditions) {
                selectivity *= estimateConditionSelectivity(condition);
            }
            return Math.max(1, (long) (totalRows * selectivity));
        }
        
        /**
         * 估算条件的选择性
         */
        private double estimateConditionSelectivity(WhereCondition condition) {
            // 简化的选择性估算
            switch (condition.getOperator()) {
                case "=": return 0.1;      // 10%
                case ">": case "<": return 0.33;  // 33%
                case ">=": case "<=": return 0.34; // 34%
                case "!=": return 0.9;     // 90%
                case "LIKE": return 0.5;   // 50%
                default: return 0.5;
            }
        }
        
        /**
         * 生成样本数据（用于演示）
         */
        private List<Map<String, Object>> generateSampleData(String tableName, int count) {
            List<Map<String, Object>> data = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", i);
                row.put("name", "用户" + i);
                row.put("age", 20 + (i % 50));
                row.put("email", "user" + i + "@example.com");
                data.add(row);
            }
            return data;
        }
    }
    
    /**
     * 执行引擎
     */
    public static class ExecutionEngine {
        private final IndexManager indexManager;
        
        public ExecutionEngine(IndexManager indexManager) {
            this.indexManager = indexManager;
        }
        
        /**
         * 执行查询
         */
        public QueryResult execute(SQLQuery query) {
            System.out.println("\n🚀 开始执行查询");
            System.out.println("=".repeat(40));
            
            long startTime = System.currentTimeMillis();
            
            try {
                switch (query.getType()) {
                    case SELECT:
                        return executeSelect(query);
                    case INSERT:
                        return executeInsert(query);
                    case UPDATE:
                        return executeUpdate(query);
                    case DELETE:
                        return executeDelete(query);
                    default:
                        throw new UnsupportedOperationException("不支持的查询类型: " + query.getType());
                }
            } finally {
                long endTime = System.currentTimeMillis();
                System.out.printf("⏱️ 查询执行时间: %d ms%n", endTime - startTime);
            }
        }
        
        /**
         * 执行SELECT查询
         */
        private QueryResult executeSelect(SQLQuery query) {
            // 这里应该集成查询优化器
            // 简化实现：直接生成和执行计划
            List<Map<String, Object>> tableData = generateSampleData(query.getTableName(), 100);
            
            List<Map<String, Object>> result = tableData;
            
            // 应用WHERE条件
            for (WhereCondition condition : query.getWhereConditions()) {
                result = result.stream()
                    .filter(condition::evaluate)
                    .collect(Collectors.toList());
            }
            
            // 选择列
            if (!query.getSelectColumns().isEmpty() && !query.getSelectColumns().contains("*")) {
                result = result.stream()
                    .map(row -> {
                        Map<String, Object> newRow = new HashMap<>();
                        for (String column : query.getSelectColumns()) {
                            if (row.containsKey(column)) {
                                newRow.put(column, row.get(column));
                            }
                        }
                        return newRow;
                    })
                    .collect(Collectors.toList());
            }
            
            // 排序
            if (!query.getOrderByColumns().isEmpty()) {
                result.sort((a, b) -> {
                    for (String column : query.getOrderByColumns()) {
                        Object valueA = a.get(column);
                        Object valueB = b.get(column);
                        
                        if (valueA instanceof Comparable && valueB instanceof Comparable) {
                            @SuppressWarnings("unchecked")
                            int cmp = ((Comparable<Object>) valueA).compareTo(valueB);
                            if (cmp != 0) return cmp;
                        }
                    }
                    return 0;
                });
            }
            
            // 限制
            if (query.getLimit() != null && result.size() > query.getLimit()) {
                result = result.subList(0, query.getLimit());
            }
            
            return new QueryResult(true, "查询成功", result, result.size());
        }
        
        /**
         * 执行INSERT查询
         */
        private QueryResult executeInsert(SQLQuery query) {
            System.out.printf("📝 插入数据到表: %s%n", query.getTableName());
            System.out.printf("   数据: %s%n", query.getInsertValues());
            
            // 这里应该实际插入数据并更新索引
            return new QueryResult(true, "插入成功", Collections.emptyList(), 1);
        }
        
        /**
         * 执行UPDATE查询
         */
        private QueryResult executeUpdate(SQLQuery query) {
            System.out.printf("✏️ 更新表: %s%n", query.getTableName());
            System.out.printf("   设置: %s%n", query.getUpdateValues());
            System.out.printf("   条件: %s%n", query.getWhereConditions());
            
            // 这里应该实际更新数据并更新索引
            return new QueryResult(true, "更新成功", Collections.emptyList(), 1);
        }
        
        /**
         * 执行DELETE查询
         */
        private QueryResult executeDelete(SQLQuery query) {
            System.out.printf("🗑️ 删除表: %s%n", query.getTableName());
            System.out.printf("   条件: %s%n", query.getWhereConditions());
            
            // 这里应该实际删除数据并更新索引
            return new QueryResult(true, "删除成功", Collections.emptyList(), 1);
        }
        
        /**
         * 生成样本数据
         */
        private List<Map<String, Object>> generateSampleData(String tableName, int count) {
            List<Map<String, Object>> data = new ArrayList<>();
            for (int i = 1; i <= count; i++) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", i);
                row.put("name", "用户" + i);
                row.put("age", 20 + (i % 50));
                row.put("email", "user" + i + "@example.com");
                data.add(row);
            }
            return data;
        }
    }
    
    /**
     * 查询结果
     */
    public static class QueryResult {
        private final boolean success;
        private final String message;
        private final List<Map<String, Object>> data;
        private final int affectedRows;
        
        public QueryResult(boolean success, String message, List<Map<String, Object>> data, int affectedRows) {
            this.success = success;
            this.message = message;
            this.data = new ArrayList<>(data);
            this.affectedRows = affectedRows;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public List<Map<String, Object>> getData() { return data; }
        public int getAffectedRows() { return affectedRows; }
        
        public void printResult() {
            System.out.println("\n📊 查询结果:");
            System.out.println("-".repeat(40));
            System.out.printf("状态: %s%n", success ? "✅ 成功" : "❌ 失败");
            System.out.printf("消息: %s%n", message);
            System.out.printf("影响行数: %d%n", affectedRows);
            
            if (!data.isEmpty()) {
                System.out.println("\n数据:");
                for (int i = 0; i < Math.min(data.size(), 10); i++) {
                    System.out.printf("  %d: %s%n", i + 1, data.get(i));
                }
                if (data.size() > 10) {
                    System.out.printf("  ... (还有 %d 行)%n", data.size() - 10);
                }
            }
        }
    }
    
    /**
     * 索引管理器
     */
    public static class IndexManager {
        private final Map<String, BPlusTree<Integer, Map<String, Object>>> indexes = new HashMap<>();
        
        /**
         * 创建索引
         */
        public void createIndex(String tableName, String columnName) {
            String indexKey = tableName + "." + columnName;
            BPlusTree<Integer, Map<String, Object>> index = new BPlusTree<>();
            indexes.put(indexKey, index);
            System.out.printf("📇 创建索引: %s%n", indexKey);
        }
        
        /**
         * 获取索引
         */
        public BPlusTree<Integer, Map<String, Object>> getIndex(String tableName, String columnName) {
            String indexKey = tableName + "." + columnName;
            return indexes.get(indexKey);
        }
        
        /**
         * 删除索引
         */
        public void dropIndex(String tableName, String columnName) {
            String indexKey = tableName + "." + columnName;
            indexes.remove(indexKey);
            System.out.printf("🗑️ 删除索引: %s%n", indexKey);
        }
        
        /**
         * 获取所有索引
         */
        public Set<String> getAllIndexes() {
            return new HashSet<>(indexes.keySet());
        }
    }
    
    /**
     * 统计信息管理器
     */
    public static class StatisticsManager {
        private final Map<String, TableStatistics> tableStatistics = new HashMap<>();
        
        public StatisticsManager() {
            // 初始化一些示例统计信息
            tableStatistics.put("users", new TableStatistics(1000, 50));
            tableStatistics.put("orders", new TableStatistics(5000, 100));
            tableStatistics.put("products", new TableStatistics(200, 20));
        }
        
        public TableStatistics getTableStatistics(String tableName) {
            return tableStatistics.getOrDefault(tableName, new TableStatistics(1000, 50));
        }
        
        public void updateTableStatistics(String tableName, long rowCount, long distinctValues) {
            tableStatistics.put(tableName, new TableStatistics(rowCount, distinctValues));
        }
    }
    
    /**
     * 表统计信息
     */
    public static class TableStatistics {
        private final long rowCount;
        private final long distinctValues;
        
        public TableStatistics(long rowCount, long distinctValues) {
            this.rowCount = rowCount;
            this.distinctValues = distinctValues;
        }
        
        public long getRowCount() { return rowCount; }
        public long getDistinctValues() { return distinctValues; }
        
        @Override
        public String toString() {
            return String.format("TableStats{rows=%d, distinct=%d}", rowCount, distinctValues);
        }
    }
    
    /**
     * 执行查询并返回结果
     */
    public QueryResult executeQuery(SQLQuery query) {
        return executionEngine.execute(query);
    }
    
    /**
     * 解释执行计划
     */
    public void explainQuery(SQLQuery query) {
        System.out.println("\n📋 查询执行计划:");
        System.out.println("=".repeat(40));
        
        if (query.getType() == QueryType.SELECT) {
            ExecutionPlan plan = optimizer.optimize(query);
            plan.explain(0);
            
            System.out.printf("\n💰 总估算成本: %.2f%n", plan.getEstimatedCost());
            System.out.printf("📊 估算返回行数: %d%n", plan.getEstimatedRows());
        } else {
            System.out.printf("查询类型: %s (不支持执行计划分析)%n", query.getType());
        }
    }
    
    /**
     * 演示查询执行器工作原理
     */
    public static void demonstrateQueryExecutor() {
        System.out.println("🔍 MySQL查询执行器原理演示");
        System.out.println("=".repeat(50));
        
        QueryExecutor executor = new QueryExecutor();
        
        // 1. 创建索引
        System.out.println("\n📇 创建索引:");
        executor.indexManager.createIndex("users", "id");
        executor.indexManager.createIndex("users", "age");
        
        // 2. 简单SELECT查询
        System.out.println("\n🔍 简单查询演示:");
        SQLQuery query1 = new SQLQuery.Builder()
            .select("users")
            .columns("id", "name", "age")
            .where("age", ">", 25)
            .orderBy("age")
            .limit(10)
            .build();
        
        executor.explainQuery(query1);
        QueryResult result1 = executor.executeQuery(query1);
        result1.printResult();
        
        // 3. 索引查询
        System.out.println("\n📇 索引查询演示:");
        SQLQuery query2 = new SQLQuery.Builder()
            .select("users")
            .columns("*")
            .where("id", "=", 42)
            .build();
        
        executor.explainQuery(query2);
        QueryResult result2 = executor.executeQuery(query2);
        result2.printResult();
        
        // 4. INSERT操作
        System.out.println("\n📝 INSERT操作演示:");
        SQLQuery insertQuery = new SQLQuery.Builder()
            .insert("users", Map.of("id", 1001, "name", "新用户", "age", 30))
            .build();
        
        QueryResult insertResult = executor.executeQuery(insertQuery);
        insertResult.printResult();
        
        // 5. UPDATE操作
        System.out.println("\n✏️ UPDATE操作演示:");
        SQLQuery updateQuery = new SQLQuery.Builder()
            .update("users", Map.of("age", 31))
            .where("id", "=", 1001)
            .build();
        
        QueryResult updateResult = executor.executeQuery(updateQuery);
        updateResult.printResult();
        
        // 6. DELETE操作
        System.out.println("\n🗑️ DELETE操作演示:");
        SQLQuery deleteQuery = new SQLQuery.Builder()
            .delete("users")
            .where("id", "=", 1001)
            .build();
        
        QueryResult deleteResult = executor.executeQuery(deleteQuery);
        deleteResult.printResult();
        
        System.out.println("\n✅ 查询执行器演示完成");
    }
    
    public static void main(String[] args) {
        demonstrateQueryExecutor();
    }
}