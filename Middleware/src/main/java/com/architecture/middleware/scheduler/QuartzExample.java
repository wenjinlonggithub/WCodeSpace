package com.architecture.middleware.scheduler;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuartzExample {

    @Autowired
    private Scheduler scheduler;

    public void scheduleJob(String jobName, String jobGroup, String cronExpression) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(SampleJob.class)
                .withIdentity(jobName, jobGroup)
                .usingJobData("message", "Hello from " + jobName)
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobName + "Trigger", jobGroup)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        System.out.println("Scheduled job: " + jobName + " with cron: " + cronExpression);
    }

    public void deleteJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.deleteJob(jobKey);
        System.out.println("Deleted job: " + jobName);
    }

    public void pauseJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.pauseJob(jobKey);
        System.out.println("Paused job: " + jobName);
    }

    public void resumeJob(String jobName, String jobGroup) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.resumeJob(jobKey);
        System.out.println("Resumed job: " + jobName);
    }

    public static class SampleJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            String message = dataMap.getString("message");
            System.out.println("Quartz job executed: " + message + " at " + new java.util.Date());
        }
    }
}