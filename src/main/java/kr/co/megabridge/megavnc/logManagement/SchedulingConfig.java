package kr.co.megabridge.megavnc.logManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;

@Configuration
public class SchedulingConfig  implements SchedulingConfigurer {
    @Value("${cron.expression}")
    private String cronExpression;

    private LogService logService;

    @Autowired
    public void SchedulingConfiguration(LogService logService) {
        this.logService = logService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(1));
        taskRegistrar.addCronTask(logService::deleteOldLogs, cronExpression);
    }
}
