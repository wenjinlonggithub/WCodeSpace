# Kafka 架构设计图

## 1. 整体系统架构

```mermaid
graph TB
    subgraph "客户端层"
        P1[Producer App 1]
        P2[Producer App 2]
        P3[Producer App N]
        C1[Consumer App 1]
        C2[Consumer App 2]
        C3[Consumer App N]
    end
    
    subgraph "Kafka集群"
        subgraph "Broker节点"
            B1[Broker 1<br/>Leader for P1,P4<br/>Follower for P2,P3]
            B2[Broker 2<br/>Leader for P2,P5<br/>Follower for P1,P4]
            B3[Broker 3<br/>Leader for P3,P6<br/>Follower for P2,P5]
        end
        
        subgraph "Topic分区"
            T1[Topic: user-events<br/>分区: 0,1,2]
            T2[Topic: order-events<br/>分区: 0,1,2,3]
            T3[Topic: payment-events<br/>分区: 0,1]
        end
    end
    
    subgraph "协调服务"
        Z1[ZooKeeper 1]
        Z2[ZooKeeper 2]
        Z3[ZooKeeper 3]
    end
    
    subgraph "监控运维"
        M1[Kafka Manager]
        M2[JMX Metrics]
        M3[Log Aggregation]
    end
    
    P1 --> B1
    P2 --> B2
    P3 --> B3
    
    B1 --> C1
    B2 --> C2
    B3 --> C3
    
    B1 <--> Z1
    B2 <--> Z2
    B3 <--> Z3
    
    B1 --> M2
    B2 --> M2
    B3 --> M2
    
    style B1 fill:#e3f2fd
    style B2 fill:#e3f2fd
    style B3 fill:#e3f2fd
    style Z1 fill:#f3e5f5
    style Z2 fill:#f3e5f5
    style Z3 fill:#f3e5f5
```

## 2. Broker内部架构

```mermaid
graph TD
    subgraph "Kafka Broker"
        subgraph "网络层"
            NIO[NIO Selector<br/>网络I/O处理]
            REQ[Request Handler<br/>请求处理线程池]
        end
        
        subgraph "日志管理"
            LM[Log Manager<br/>日志管理器]
            LS[Log Segment<br/>日志段]
            IDX[Index Files<br/>索引文件]
        end
        
        subgraph "副本管理"
            RM[Replica Manager<br/>副本管理器]
            RF[Replica Fetcher<br/>副本同步线程]
            ISR[ISR管理<br/>同步副本集]
        end
        
        subgraph "控制器"
            KC[Kafka Controller<br/>集群控制器]
            PM[Partition Manager<br/>分区管理]
            LDR[Leader Election<br/>Leader选举]
        end
        
        subgraph "存储层"
            DISK[磁盘存储<br/>.log .index .timeindex]
            CACHE[Page Cache<br/>操作系统缓存]
        end
        
        subgraph "ZooKeeper客户端"
            ZK[ZK Client<br/>元数据同步]
        end
    end
    
    NIO --> REQ
    REQ --> LM
    REQ --> RM
    LM --> LS
    LS --> IDX
    LS --> DISK
    DISK --> CACHE
    RM --> RF
    RM --> ISR
    KC --> PM
    KC --> LDR
    KC --> ZK
    
    style NIO fill:#e1f5fe
    style LM fill:#e8f5e8
    style RM fill:#fff3e0
    style KC fill:#fce4ec
```

## 3. 分区副本架构

```mermaid
graph TD
    subgraph "Topic: orders (副本因子=3)"
        subgraph "分区0"
            P0L[Leader<br/>Broker-1]
            P0F1[Follower<br/>Broker-2]
            P0F2[Follower<br/>Broker-3]
        end
        
        subgraph "分区1"
            P1L[Leader<br/>Broker-2]
            P1F1[Follower<br/>Broker-1]
            P1F2[Follower<br/>Broker-3]
        end
        
        subgraph "分区2"
            P2L[Leader<br/>Broker-3]
            P2F1[Follower<br/>Broker-1]
            P2F2[Follower<br/>Broker-2]
        end
    end
    
    subgraph "ISR (In-Sync Replicas)"
        ISR0[分区0 ISR<br/>{1,2,3}]
        ISR1[分区1 ISR<br/>{1,2,3}]
        ISR2[分区2 ISR<br/>{1,2,3}]
    end
    
    P0L --> P0F1
    P0L --> P0F2
    P1L --> P1F1
    P1L --> P1F2
    P2L --> P2F1
    P2L --> P2F2
    
    P0L --> ISR0
    P1L --> ISR1
    P2L --> ISR2
    
    style P0L fill:#c8e6c9
    style P1L fill:#c8e6c9
    style P2L fill:#c8e6c9
    style ISR0 fill:#fff9c4
    style ISR1 fill:#fff9c4
    style ISR2 fill:#fff9c4
```

## 4. 消费者组架构

```mermaid
graph TD
    subgraph "Consumer Group: order-processors"
        CG[Group Coordinator<br/>Broker-2]
        
        subgraph "Consumer实例"
            C1[Consumer-1<br/>消费分区0,1]
            C2[Consumer-2<br/>消费分区2,3]
            C3[Consumer-3<br/>消费分区4,5]
        end
        
        subgraph "Offset管理"
            OT[__consumer_offsets<br/>Topic]
            OM[Offset Manager<br/>偏移量管理]
        end
    end
    
    subgraph "Topic分区"
        P0[分区0]
        P1[分区1]
        P2[分区2]
        P3[分区3]
        P4[分区4]
        P5[分区5]
    end
    
    CG --> C1
    CG --> C2
    CG --> C3
    
    C1 --> P0
    C1 --> P1
    C2 --> P2
    C2 --> P3
    C3 --> P4
    C3 --> P5
    
    C1 --> OM
    C2 --> OM
    C3 --> OM
    OM --> OT
    
    style CG fill:#e1f5fe
    style OM fill:#f3e5f5
    style OT fill:#fff3e0
```

## 5. 数据存储架构

```mermaid
graph TD
    subgraph "Kafka数据目录"
        subgraph "Topic: user-events-0"
            L1[00000000000000000000.log<br/>消息数据文件]
            I1[00000000000000000000.index<br/>偏移量索引]
            T1[00000000000000000000.timeindex<br/>时间戳索引]
            S1[00000000000000001000.log<br/>下一个段文件]
        end
        
        subgraph "Topic: user-events-1"
            L2[00000000000000000000.log]
            I2[00000000000000000000.index]
            T2[00000000000000000000.timeindex]
        end
        
        subgraph "系统Topic"
            CO[__consumer_offsets<br/>消费者偏移量]
            CT[__transaction_state<br/>事务状态]
        end
    end
    
    subgraph "存储特性"
        SEQ[顺序写入<br/>高性能]
        COMP[日志压缩<br/>节省空间]
        RET[数据保留<br/>时间/大小策略]
        REP[数据复制<br/>高可用]
    end
    
    L1 --> SEQ
    L1 --> COMP
    L1 --> RET
    L1 --> REP
    
    style L1 fill:#e8f5e8
    style I1 fill:#e3f2fd
    style T1 fill:#fff3e0
    style CO fill:#fce4ec
```

## 6. 网络通信架构

```mermaid
graph LR
    subgraph "Producer"
        PA[Producer API]
        PB[Producer Buffer]
        PS[Partitioner<br/>分区器]
        PN[Network Client]
    end
    
    subgraph "Kafka Protocol"
        REQ[Request/Response<br/>协议]
        BATCH[批量传输]
        COMP[压缩算法]
        ACK[确认机制]
    end
    
    subgraph "Broker"
        NL[Network Layer<br/>网络层]
        RH[Request Handler<br/>请求处理]
        API[Kafka API<br/>处理器]
        RESP[Response<br/>响应]
    end
    
    subgraph "Consumer"
        CF[Consumer Fetcher]
        CB[Consumer Buffer]
        CA[Consumer API]
        CC[Coordinator Client]
    end
    
    PA --> PB
    PB --> PS
    PS --> PN
    PN --> REQ
    
    REQ --> BATCH
    BATCH --> COMP
    COMP --> ACK
    
    ACK --> NL
    NL --> RH
    RH --> API
    API --> RESP
    
    RESP --> CF
    CF --> CB
    CB --> CA
    CA --> CC
    
    style REQ fill:#e1f5fe
    style BATCH fill:#e8f5e8
    style COMP fill:#fff3e0
    style ACK fill:#fce4ec
```

## 7. 高可用架构设计

```mermaid
graph TD
    subgraph "多数据中心部署"
        subgraph "数据中心A"
            ZKA[ZooKeeper集群A<br/>3节点]
            BRA[Broker集群A<br/>3节点]
        end
        
        subgraph "数据中心B"
            ZKB[ZooKeeper集群B<br/>3节点]
            BRB[Broker集群B<br/>3节点]
        end
        
        subgraph "数据中心C"
            ZKC[ZooKeeper集群C<br/>1节点]
            BRC[Broker集群C<br/>1节点]
        end
    end
    
    subgraph "容灾机制"
        REP[跨数据中心副本]
        MIRROR[MirrorMaker<br/>数据同步]
        BACKUP[定期备份]
        MONITOR[监控告警]
    end
    
    subgraph "故障恢复"
        AUTO[自动故障转移]
        MANUAL[手动切换]
        ROLLBACK[数据回滚]
    end
    
    ZKA <--> ZKB
    ZKB <--> ZKC
    ZKC <--> ZKA
    
    BRA --> REP
    BRB --> REP
    BRC --> REP
    
    BRA --> MIRROR
    BRB --> MIRROR
    
    REP --> AUTO
    MIRROR --> MANUAL
    BACKUP --> ROLLBACK
    
    style ZKA fill:#e1f5fe
    style ZKB fill:#e1f5fe
    style ZKC fill:#e1f5fe
    style REP fill:#c8e6c9
    style AUTO fill:#fff3e0
```