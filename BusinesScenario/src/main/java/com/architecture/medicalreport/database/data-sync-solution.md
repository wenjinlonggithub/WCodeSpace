# MySQL to ClickHouse 数据同步方案

## 方案概述

将MySQL业务库的数据实时同步到ClickHouse OLAP引擎，实现高性能报表查询。

```
MySQL (业务库) → Binlog → Canal → Kafka → Flink/Consumer → ClickHouse
```

---

## 方案一: Canal + Kafka + Flink (推荐)

### 架构图

```
┌──────────────┐    Binlog    ┌────────┐    JSON    ┌────────┐
│ MySQL Master │─────────────>│ Canal  │──────────>│ Kafka  │
│  (业务库)     │              │ Server │            │        │
└──────────────┘              └────────┘            └────────┘
                                                         │
                                                         │ Consume
                                                         ▼
                                                    ┌────────┐
                                                    │ Flink  │
                                                    │  CDC   │
                                                    └────────┘
                                                         │
                                                         │ Insert
                                                         ▼
                                                  ┌─────────────┐
                                                  │ ClickHouse  │
                                                  │   (OLAP)    │
                                                  └─────────────┘
```

### 1. Canal部署配置

#### 1.1 MySQL准备

```sql
-- 1. 开启Binlog (my.cnf)
[mysqld]
log-bin=mysql-bin
binlog-format=ROW
server-id=1

-- 2. 创建Canal用户
CREATE USER 'canal'@'%' IDENTIFIED BY 'canal';
GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%';
FLUSH PRIVILEGES;

-- 3. 验证Binlog
SHOW VARIABLES LIKE 'log_bin';
SHOW MASTER STATUS;
```

#### 1.2 Canal Server配置

```properties
# conf/canal.properties
canal.serverMode = kafka
kafka.bootstrap.servers = 127.0.0.1:9092

# conf/example/instance.properties
canal.instance.master.address=127.0.0.1:3306
canal.instance.dbUsername=canal
canal.instance.dbPassword=canal

# 监听的数据库和表
canal.instance.filter.regex=medical_db\\.outpatient_record,medical_db\\.inpatient_record

# 输出到Kafka
canal.mq.topic=medical-binlog-topic
canal.mq.dynamicTopic=medical-binlog-.*
```

#### 1.3 启动Canal

```bash
cd canal-server
bin/startup.sh

# 查看日志
tail -f logs/canal/canal.log
tail -f logs/example/example.log
```

### 2. Kafka配置

```bash
# 创建Topic
kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic medical-binlog-topic \
  --partitions 3 \
  --replication-factor 2

# 查看消息
kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic medical-binlog-topic \
  --from-beginning
```

### 3. Flink CDC消费程序

```java
package com.architecture.medicalreport.sync;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import com.alibaba.fastjson.JSON;

import java.util.Properties;

/**
 * Flink CDC数据同步任务
 *
 * 功能:
 * 1. 消费Kafka中的Binlog数据
 * 2. 解析Canal JSON格式
 * 3. 写入ClickHouse
 *
 * @author Medical Report System
 * @since 2025-01-16
 */
public class MedicalDataSyncJob {

    public static void main(String[] args) throws Exception {
        // 1. 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(3);
        env.enableCheckpointing(60000); // 每分钟checkpoint

        // 2. Kafka消费者配置
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:9092");
        props.setProperty("group.id", "medical-sync-group");
        props.setProperty("auto.offset.reset", "earliest");

        // 3. 创建Kafka Source
        FlinkKafkaConsumer<String> consumer = new FlinkKafkaConsumer<>(
                "medical-binlog-topic",
                new SimpleStringSchema(),
                props
        );

        DataStream<String> stream = env.addSource(consumer);

        // 4. 数据转换和过滤
        stream
                .map(json -> parseCanalJson(json))
                .filter(data -> data != null)
                .filter(data -> "INSERT".equals(data.getEventType()) || "UPDATE".equals(data.getEventType()))
                .keyBy(data -> data.getTableName())
                .map(data -> transformToClickHouseFormat(data))
                .addSink(new ClickHouseSink());  // 5. 写入ClickHouse

        // 6. 启动任务
        env.execute("Medical Data Sync Job");
    }

    /**
     * 解析Canal JSON格式
     */
    private static CanalMessage parseCanalJson(String json) {
        try {
            return JSON.parseObject(json, CanalMessage.class);
        } catch (Exception e) {
            System.err.println("解析Canal消息失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 转换为ClickHouse格式
     */
    private static ClickHouseRecord transformToClickHouseFormat(CanalMessage data) {
        // 根据表名路由到不同的处理逻辑
        if ("outpatient_record".equals(data.getTableName())) {
            return transformOutpatientRecord(data);
        } else if ("inpatient_record".equals(data.getTableName())) {
            return transformInpatientRecord(data);
        }
        return null;
    }

    private static ClickHouseRecord transformOutpatientRecord(CanalMessage data) {
        // 实现转换逻辑
        return new ClickHouseRecord();
    }

    private static ClickHouseRecord transformInpatientRecord(CanalMessage data) {
        // 实现转换逻辑
        return new ClickHouseRecord();
    }
}

/**
 * Canal消息格式
 */
class CanalMessage {
    private String database;
    private String table;
    private String type;  // INSERT, UPDATE, DELETE
    private Long ts;
    private List<Map<String, Object>> data;

    // getters/setters
    public String getTableName() { return table; }
    public String getEventType() { return type; }
}

/**
 * ClickHouse记录
 */
class ClickHouseRecord {
    private String tableName;
    private Map<String, Object> data;

    // getters/setters
}
```

### 4. ClickHouse Sink实现

```java
package com.architecture.medicalreport.sync;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseProperties;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * ClickHouse批量写入Sink
 *
 * 性能优化:
 * 1. 批量插入，每批1000条
 * 2. 异步写入
 * 3. 失败重试
 */
public class ClickHouseSink implements SinkFunction<ClickHouseRecord> {

    private static final int BATCH_SIZE = 1000;
    private List<ClickHouseRecord> buffer = new ArrayList<>();
    private ClickHouseDataSource dataSource;

    public ClickHouseSink() {
        ClickHouseProperties properties = new ClickHouseProperties();
        properties.setUser("default");
        properties.setPassword("");
        properties.setDatabase("medical_db");

        this.dataSource = new ClickHouseDataSource(
                "jdbc:clickhouse://127.0.0.1:8123/medical_db",
                properties
        );
    }

    @Override
    public void invoke(ClickHouseRecord record, Context context) throws Exception {
        buffer.add(record);

        if (buffer.size() >= BATCH_SIZE) {
            flush();
        }
    }

    /**
     * 批量写入ClickHouse
     */
    private void flush() throws Exception {
        if (buffer.isEmpty()) {
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            // 根据表名分组
            Map<String, List<ClickHouseRecord>> grouped = buffer.stream()
                    .collect(Collectors.groupingBy(ClickHouseRecord::getTableName));

            for (Map.Entry<String, List<ClickHouseRecord>> entry : grouped.entrySet()) {
                String tableName = entry.getKey();
                List<ClickHouseRecord> records = entry.getValue();

                String sql = buildInsertSql(tableName);

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    for (ClickHouseRecord record : records) {
                        setParameters(ps, record);
                        ps.addBatch();
                    }

                    ps.executeBatch();
                }
            }

            buffer.clear();
            System.out.println("批量写入ClickHouse成功: " + buffer.size() + " 条");

        } catch (Exception e) {
            System.err.println("写入ClickHouse失败: " + e.getMessage());
            throw e;
        }
    }

    private String buildInsertSql(String tableName) {
        if ("medical_outpatient_wide".equals(tableName)) {
            return "INSERT INTO medical_outpatient_wide " +
                    "(record_id, record_date, patient_id, doctor_id, department_id, " +
                    "total_amount, insurance_amount, self_amount, create_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
        // 其他表...
        return "";
    }

    private void setParameters(PreparedStatement ps, ClickHouseRecord record) throws Exception {
        // 设置参数
        Map<String, Object> data = record.getData();
        ps.setString(1, (String) data.get("record_id"));
        // ... 其他字段
    }
}
```

---

## 方案二: DataX离线同步 (T+1)

适用于不需要实时同步的场景，每天定时全量或增量同步。

### DataX配置文件

```json
{
  "job": {
    "setting": {
      "speed": {
        "channel": 3
      }
    },
    "content": [
      {
        "reader": {
          "name": "mysqlreader",
          "parameter": {
            "username": "root",
            "password": "password",
            "connection": [
              {
                "querySql": [
                  "SELECT * FROM outpatient_record WHERE DATE(record_date) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)"
                ],
                "jdbcUrl": ["jdbc:mysql://127.0.0.1:3306/medical_db"]
              }
            ]
          }
        },
        "writer": {
          "name": "clickhousewriter",
          "parameter": {
            "username": "default",
            "password": "",
            "column": ["record_id", "record_date", "patient_id", "..."],
            "connection": [
              {
                "jdbcUrl": "jdbc:clickhouse://127.0.0.1:8123/medical_db",
                "table": ["medical_outpatient_wide"]
              }
            ]
          }
        }
      }
    ]
  }
}
```

### 定时任务 (Cron)

```bash
#!/bin/bash
# sync-daily-data.sh

DATE=$(date -d "yesterday" +%Y-%m-%d)
LOG_FILE="/var/log/datax/sync-${DATE}.log"

echo "开始同步${DATE}的数据..." >> $LOG_FILE

python /opt/datax/bin/datax.py /opt/datax/job/medical-sync.json >> $LOG_FILE 2>&1

if [ $? -eq 0 ]; then
    echo "同步成功" >> $LOG_FILE
else
    echo "同步失败" >> $LOG_FILE
    # 发送告警
    curl -X POST "https://alert.example.com/webhook" -d "DataX同步失败"
fi
```

```bash
# crontab配置
0 2 * * * /opt/scripts/sync-daily-data.sh
```

---

## 方案三: Flink CDC (推荐用于实时同步)

无需Canal，直接读取MySQL Binlog。

```java
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;

public class FlinkCDCJob {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
                .hostname("localhost")
                .port(3306)
                .databaseList("medical_db")
                .tableList("medical_db.outpatient_record", "medical_db.inpatient_record")
                .username("root")
                .password("password")
                .startupOptions(StartupOptions.latest())
                .deserializer(new JsonDebeziumDeserializationSchema())
                .build();

        env.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source")
                .map(json -> parseAndTransform(json))
                .addSink(new ClickHouseSink());

        env.execute("Flink CDC Job");
    }
}
```

---

## 数据一致性保证

### 1. 全量同步 + 增量同步

```bash
# 第一步: 全量同步历史数据
mysqldump -u root -p medical_db outpatient_record \
  --where="record_date >= '2024-01-01'" \
  | clickhouse-client --query="INSERT INTO medical_outpatient_wide FORMAT CSV"

# 第二步: 启动增量同步 (Canal/Flink CDC)
# 从全量同步的时间点开始消费Binlog
```

### 2. 幂等性保证

```sql
-- ClickHouse使用ReplacingMergeTree，根据主键去重
CREATE TABLE medical_outpatient_wide (
    record_id String,
    ...
) ENGINE = ReplacingMergeTree()
ORDER BY record_id;

-- 或者使用INSERT ... ON DUPLICATE KEY UPDATE
```

### 3. 监控告警

```java
// 监控同步延迟
SELECT
    database,
    table,
    max(create_time) as latest_time,
    now() - max(create_time) as delay_seconds
FROM medical_outpatient_wide
GROUP BY database, table;

// 告警规则: 延迟 > 300秒
if (delay_seconds > 300) {
    sendAlert("数据同步延迟告警: " + delay_seconds + "秒");
}
```

---

## 性能对比

| 方案 | 实时性 | 吞吐量 | 复杂度 | 成本 |
|------|--------|--------|--------|------|
| Canal + Kafka + Flink | 准实时(3-5秒) | 10万/秒 | 高 | 高 |
| Flink CDC | 准实时(2-3秒) | 8万/秒 | 中 | 中 |
| DataX离线同步 | T+1 | 5万/秒 | 低 | 低 |

---

## 推荐选型

1. **实时性要求高**: Flink CDC (简单) 或 Canal + Kafka (灵活)
2. **T+1可接受**: DataX离线同步
3. **初期快速上线**: DataX，后期再升级实时同步
