package com.bonitasoft.custompage.bigApp.cmdtimer;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SStartEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerDefinition;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.event.SCatchEventInstance;
import org.bonitasoft.engine.execution.event.TimerEventHandlerStrategy;
import org.bonitasoft.engine.execution.job.JobNameBuilder;
import org.bonitasoft.engine.jobs.TriggerTimerEventJob;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.builder.SJobDescriptorBuilderFactory;
import org.bonitasoft.engine.scheduler.builder.SJobParameterBuilderFactory;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.OneShotTrigger;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.service.TenantServiceAccessor;

import com.bonitasoft.custompage.bigApp.Timer;
import com.bonitasoft.custompage.bigApp.Timer.MethodResetTimer;

;

public class CmdCreateTimer extends TenantCommand {

		private static String cstResultListEvents = "ListEvents";
		private static String cstResultEventId = "Id";
		private static String cstResultEventStateName = "StateName";
		private static String cstResultTimeInMs = "TIMEINMS";
		private static String cstResultExpl = "Expl";
		private static String cstResultEventJobName = "JobName";
		public static String cstResultEventJobClassName = "JobClassName";
		private static String cstResultEventJobParam = "JobParam";
		private static String cstResultEventJobIsStillSchedule = "JobIsStillSchedule";

		public static String cstResultStatus_FAIL = "FAIL";
		public static String cstResultStatus_OK = "OK";
		public static String cstResultStatus_PING = "PING";

		public static String cstParamListMissingTimer = "listtimer";
		public static String cstParamMethodReset = "methodreset";
		public static String cstParamPing = "ping";

		static Logger logger = Logger.getLogger(CmdCreateTimer.class.getName());

		/**
		 * Change the time of an timer. parameters are tenantid : optional, 1 per
		 * default activityid : name of the activity ELSE the activityName +
		 * processinstanceid shoud be provided activityname (if not activityid is
		 * given) processinstanceid processinstanceid of the case to change
		 * timername the name of the boundary timer newtimerdate the new date of
		 * this process intance. Format is yyyyMMdd HH:MM:ss
		 */
		public Serializable execute(Map<String, Serializable> parameters, TenantServiceAccessor serviceAccessor) throws SCommandParameterizationException, SCommandExecutionException {

				TechnicalLoggerService technicalLoggerService = serviceAccessor.getTechnicalLoggerService();
				HashMap<String, Object> finalStatus = new HashMap<String, Object>();
				finalStatus.put(cstResultTimeInMs, System.currentTimeMillis());
				ArrayList<HashMap<String, Object>> listEvents = new ArrayList<HashMap<String, Object>>();
				HashMap<String, Object> result = new HashMap<String, Object>();
				result.put(cstResultListEvents, listEvents);
				logger.info("Start command CmdCreateTimer");
				// ------------------- ping ?
				Object ping = parameters.get(cstParamPing);
				if (ping != null) {
						logger.info("CmdCreateTimer: ping");
						result.put(Timer.cstResultTimerStatus, cstResultStatus_PING);
						return result;
				}

				// ------------------- service
				ProcessDefinitionService processDefinitionService = serviceAccessor.getProcessDefinitionService();
				ProcessInstanceService processInstanceService = serviceAccessor.getProcessInstanceService();
				SchedulerService schedulerService = serviceAccessor.getSchedulerService();
				EventInstanceService eventInstanceService = serviceAccessor.getEventInstanceService();

				// ------------------- parameter
				List<HashMap<String, Object>> listMissingTimer = (List<HashMap<String, Object>>) parameters.get(cstParamListMissingTimer);
				MethodResetTimer methodResetTimer = MethodResetTimer.valueOf( (String) parameters.get(cstParamMethodReset));
				if (methodResetTimer == null)
						methodResetTimer = Timer.MethodResetTimer.Handle;

				// is
				StringBuffer errorCommand = new StringBuffer();
				if (!  (Timer.MethodResetTimer.Handle.equals(methodResetTimer) || Timer.MethodResetTimer.Recreate.equals(methodResetTimer))) {
						errorCommand.append("UnknowCommand["+methodResetTimer+"]  expected[" + Timer.MethodResetTimer.Handle + "," + Timer.MethodResetTimer.Recreate+"]");

				} else {
						// ------------------- objects linked to the process
						for (HashMap<String, Object> oneTimer : listMissingTimer) {
								// create the timer

								Long processDefinitionId = (Long) oneTimer.get(Timer.cstTimerProcessdefinitionid);
								String processDefinitionName = (String) oneTimer.get(Timer.cstTimerProcessDefinitionName);
								String processDefinitionVersion = (String) oneTimer.get(Timer.cstTimerProcessDefinitionVersion);
								Long flowNodeInstanceId = (Long) oneTimer.get(Timer.cstTimerFlownodeInstanceId);
								String eventDefinitionName = (String) oneTimer.get(Timer.cstTimerEventDefinitionName);
								Long eventDefinitionId = (Long) oneTimer.get(Timer.cstTimerEventDefinitionId);
								Date newTimerDate = (Date) oneTimer.get(Timer.cstTimerNewTimerDate);

								logger.info("CmdCreateTimer: Create timer processDefinitionId[" + processDefinitionId //
												+ "] processDefinitionName[" + processDefinitionName //
												+ "] processDefinitionVersion[" + processDefinitionVersion //
												+ "] flowNodeInstanceId[" + flowNodeInstanceId //
												+ "] eventDefinitionName[" + eventDefinitionName //
												+ "] eventDefinitionId[" + eventDefinitionId //
												+ "] newTimerDate[" + newTimerDate + "]");

								// -------------------- delete the current schedule
								final String jobName = "Timer_Ev_" + flowNodeInstanceId;
								try {

										if (Timer.MethodResetTimer.Handle.equals(methodResetTimer)) {

												final SCatchEventInstance sCatchEventInstance = (SCatchEventInstance) serviceAccessor.getActivityInstanceService().getFlowNodeInstance(flowNodeInstanceId);
												final SProcessDefinition processDefinition = serviceAccessor.getProcessDefinitionService().getProcessDefinition(sCatchEventInstance.getProcessDefinitionId());
												final SEventDefinition sEventDefinition = (SEventDefinition) processDefinition.getProcessContainer().getFlowNode(sCatchEventInstance.getFlowNodeDefinitionId());
												TimerEventHandlerStrategy timerEventHandlerStrategy = new TimerEventHandlerStrategy(serviceAccessor.getExpressionResolverService(), serviceAccessor.getSchedulerService(), serviceAccessor.getEventInstanceService(), serviceAccessor.getTechnicalLoggerService());
												final SEventTriggerDefinition sEventTriggerDefinition = sEventDefinition.getEventTriggers().get(0);

												timerEventHandlerStrategy.handleCatchEvent(processDefinition, sEventDefinition, sCatchEventInstance, sEventTriggerDefinition);
										} else if (Timer.MethodResetTimer.Recreate.equals(methodResetTimer)) {
												Random randomGenerator = new Random();

												Trigger trigger = new OneShotTrigger("OneShotTrigger-" + randomGenerator.nextInt(9999999), newTimerDate);

												SJobDescriptor jobDescriptor = getJobDescriptor(jobName);

												List<SJobParameter> jobParameters = getJobParameters(flowNodeInstanceId, processDefinitionId, eventDefinitionName, eventDefinitionId);
												schedulerService.schedule(jobDescriptor, jobParameters, trigger);
										}
										else 
										{
												logger.severe("Unknow command ["+ (String) parameters.get(cstParamMethodReset)+"]");										
										}
								} catch (SBonitaException e) {
										StringWriter sw = new StringWriter();
										e.printStackTrace(new PrintWriter(sw));
										String exceptionDetails = sw.toString();

										errorCommand.append("Timer [" + processDefinitionName + "(" + processDefinitionVersion + ")] flowNodeId[" + flowNodeInstanceId + "]: " + e.toString() + ";");
										logger.severe("During create a new time [" + e.toString() + " : " + exceptionDetails);
								}

						} // end loop

				}

				result.put(Timer.cstResultTimerStatus, errorCommand.length() == 0 ? "OK" : "FAILED");
				result.put(Timer.cstResultTimerError, errorCommand.toString());
				logger.info("End command CmdCreateTimer");

				return result;
		}

		/**
		 * copy from the TImerEventHandlerStrategy.java
		 * 
		 * @param processDefinition
		 * @param eventDefinition
		 * @param eventInstance
		 * @return
		 */
		private List<SJobParameter> getJobParameters(Long flowNodeInstanceId, Long processDefinitionId, String eventDefinitionName, Long eventDefinitionId) {
				final List<SJobParameter> jobParameters = new ArrayList<SJobParameter>();
				jobParameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("processDefinitionId", processDefinitionId).done());
				jobParameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("containerType", SFlowElementsContainerType.PROCESS.name()).done());
				jobParameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("eventType", eventDefinitionName).done());
				jobParameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("targetSFlowNodeDefinitionId", eventDefinitionId).done());
				jobParameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("flowNodeInstanceId", flowNodeInstanceId).done());

				return jobParameters;
		}

		/**
		 * return the job timer name
		 * 
		 * @param processDefinitionId
		 * @param sCatchEventInstance
		 * @param sEventDefinition
		 * @return
		 */
		private String getTimerEventJobName(long processDefinitionId, SEventDefinition sEventDefinition, SCatchEventInstance sCatchEventInstance) {
				return JobNameBuilder.getTimerEventJobName(processDefinitionId, sEventDefinition, sCatchEventInstance);
		}

		/**
		 * copy from the TImerEventHandlerStrategy.java
		 * 
		 * @param processDefinition
		 * @param eventDefinition
		 * @param eventInstance
		 * @return
		 */
		private List<SJobParameter> getJobParameters(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition, final SCatchEventInstance eventInstance) {
				final List<SJobParameter> jobParameters = new ArrayList<SJobParameter>();
				jobParameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("processDefinitionId", processDefinition.getId()).done());
				jobParameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("containerType", SFlowElementsContainerType.PROCESS.name()).done());
				jobParameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("eventType", eventDefinition.getType().name()).done());
				jobParameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("targetSFlowNodeDefinitionId", eventDefinition.getId()).done());
				if (SFlowNodeType.START_EVENT.equals(eventDefinition.getType())) {
						final SStartEventDefinition startEvent = (SStartEventDefinition) eventDefinition;
						jobParameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("isInterrupting", startEvent.isInterrupting()).done());
				}
				if (eventInstance != null) {
						jobParameters.add(BuilderFactory.get(SJobParameterBuilderFactory.class).createNewInstance("flowNodeInstanceId", eventInstance.getId()).done());
				}
				return jobParameters;
		}

		/**
		 * copy from the TImerEventHandlerStrategy.java
		 */
		private SJobDescriptor getJobDescriptor(final String jobName) {
				return BuilderFactory.get(SJobDescriptorBuilderFactory.class).createNewInstance(TriggerTimerEventJob.class.getName(), jobName, false).done();
		}

}
