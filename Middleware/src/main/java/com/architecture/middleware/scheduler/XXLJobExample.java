package com.architecture.middleware.scheduler;

import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class XXLJobExample {

    @XxlJob("demoJobHandler")
    public void demoJobHandler() {
        System.out.println("XXL-Job demo job executed at: " + new java.util.Date());
    }

    @XxlJob("dataProcessJob")
    public void dataProcessJob() {
        System.out.println("XXL-Job data process job started");
        
        try {
            Thread.sleep(5000);
            System.out.println("Data processing completed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @XxlJob("parameterJob")
    public void parameterJob() {
        String param = com.xxl.job.core.context.XxlJobHelper.getJobParam();
        System.out.println("XXL-Job parameter job with param: " + param);
    }

    @XxlJob("shardingJob")
    public void shardingJob() {
        int shardIndex = com.xxl.job.core.context.XxlJobHelper.getShardIndex();
        int shardTotal = com.xxl.job.core.context.XxlJobHelper.getShardTotal();
        
        System.out.println("XXL-Job sharding job - Current shard: " + shardIndex + "/" + shardTotal);
    }
}