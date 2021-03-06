package com.github.reflectoring.infiniboard.harvester.source;

import com.github.reflectoring.infiniboard.harvester.scheduling.SchedulingService;
import java.util.Map;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * This base class of SourceJobs gets information from the quartz JobExecutionContext and calls
 * internal method with Spring application context and SourceJob configuration data.
 */
public abstract class SourceJob implements Job {

  private static final Logger LOG = LoggerFactory.getLogger(SourceJob.class);

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    LOG.debug("executing job '{}' ", getClass().getSimpleName());
    JobDataMap configuration = context.getJobDetail().getJobDataMap();
    ApplicationContext applicationContext =
        (ApplicationContext) configuration.get(SchedulingService.PARAM_CONTEXT);
    SchedulingService schedulingService = applicationContext.getBean(SchedulingService.class);

    JobKey jobKey = context.getJobDetail().getKey();
    try {
      if (schedulingService.canSourceJobBeExecuted(jobKey.getGroup())) {
        executeInternal(applicationContext, jobKey, configuration);
      }
    } catch (SchedulerException e) {
      LOG.error("error on check if a job can be canceled", e);
      // handling?
    }
  }

  /**
   * called after checking if the job could be run (the associated widget exists)
   *
   * @param context spring context to access beans
   * @param jobKey job name and widget identifier
   * @param configuration job configuration
   */
  protected abstract void executeInternal(
      ApplicationContext context, JobKey jobKey, Map<String, Object> configuration);
}
