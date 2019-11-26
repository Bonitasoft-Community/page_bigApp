package com.bonitasoft.custompage.bigApp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.FlowNodeDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.command.CommandCriterion;
import org.bonitasoft.engine.command.CommandDescriptor;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.platform.StartNodeException;
import org.bonitasoft.engine.platform.StopNodeException;

import com.bonitasoft.custompage.bigApp.cmdtimer.CmdCreateTimer;

public class Timer {

		static Logger logger = Logger.getLogger(Timer.class.getName());

		public final static String cstResultTimerError = "timererror";
		public final static String cstResultTimerStatus = "timerstatus";
		public final static String cstResultListTimers = "listtimers";
		public final static String cstTimerProcessdefinitionid = "processdefinitionid";
		public final static String cstTimerFlownodeInstanceId = "flownodeinstanceid";
		public final static String cstTimerNewTimerDate = "newtimerdate";
		public final static String cstTimerNewTimerDateSt = "newtimerdatest";
		public final static String cstTimerProcessDefinitionName = "processdefinitionname";
		public final static String cstTimerProcessDefinitionVersion = "processdefinitionversion";
		public final static String cstTimerEventDefinitionName = "eventdefinitionname";
		public final static String cstTimerEventDefinitionId = "eventdefinitionid";
		public final static String cstTimerStatus = "status";

		public static enum MethodResetTimer {
				Handle, Recreate, RetryTask, ExecuteTask
		};

		private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

		/**
		 * 
		 * @return
		 */
		public static HashMap<String, Object> getMissingTimers(boolean includeDate, ProcessAPI processAPI) {
				HashMap<String, Object> result = new HashMap<String, Object>();

				try {
						String sqlRequest = " select flownode_instance.name            as timername," //

										+ " flownode_instance.id                     as flownodeinstanceid, " //
										+ " flownode_instance.flownodedefinitionid   as flownodefinitionid, " //
										+ " flownode_instance.statename  						 as statename, " //
										+ " process_instance.id                      as baseprocessinstanceid, " //
										+ " process_instance.rootprocessinstanceid 	 as rootprocessinstanceid," //
										+ " process_instance.processdefinitionid     as baseprocessdefinitionid," //
										+ " rootprocess_instance.processdefinitionid as rootprocessdefinitionid," //
										+ " process_definition.name                  as baseprocessdefinitionname," //
										+ " process_definition.version               as baseprocessdefinitionversion," //
										+ " rootprocess_definition.name              as rootprocessdefinitionname," //
										+ " rootprocess_definition.version           as rootprocessdefinitionversion"

										+ " from flownode_instance" + " left join process_instance on (flownode_instance.rootcontainerid = process_instance.id)" //
										+ " left join process_instance as rootprocess_instance on (process_instance.rootprocessinstanceid = rootprocess_instance.id)" //
										+ " left join process_definition on (process_instance.processdefinitionid = process_definition.processid)" //
										+ " left join process_definition as rootprocess_definition on (rootprocess_instance.processdefinitionid = rootprocess_definition.processid)" //

										+ " where CAST( flownode_instance.id AS text) not in (select SUBSTRING(job_name, 10, 9) from qrtz_triggers) and kind = 'intermediateCatchEvent' and (statename ='waiting' or statename='failed')";

						logger.info("Search process with request[" + sqlRequest + "]");
						Context ctx = new InitialContext();
						DataSource ds = (DataSource) ctx.lookup("java:/comp/env/bonitaSequenceManagerDS");
						Connection con = ds.getConnection();
						Statement stmt = con.createStatement();

						ResultSet rs = stmt.executeQuery(sqlRequest);
						ArrayList<HashMap<String, Object>> listTimers = new ArrayList<HashMap<String, Object>>();
						while (rs.next()) {
								HashMap<String, Object> oneTimer = new HashMap<String, Object>();
								oneTimer.put("name", rs.getObject("timername"));
								oneTimer.put("statename", rs.getObject("statename"));
								oneTimer.put("baseprocessinstanceid", rs.getObject("baseprocessinstanceid"));
								oneTimer.put("rootprocessinstanceid", rs.getObject("rootprocessinstanceid"));

								oneTimer.put("baseprocessdefinitionid", rs.getObject("baseprocessdefinitionid"));
								oneTimer.put("rootprocessdefinitionid", rs.getObject("rootprocessdefinitionid"));

								oneTimer.put("baseprocessdefinitionname", rs.getObject("baseprocessdefinitionname"));
								oneTimer.put("baseprocessdefinitionversion", rs.getObject("baseprocessdefinitionversion"));
								oneTimer.put("rootprocessdefinitionname", rs.getObject("rootprocessdefinitionname"));
								oneTimer.put("rootprocessdefinitionversion", rs.getObject("rootprocessdefinitionversion"));

								oneTimer.put(cstTimerFlownodeInstanceId, rs.getObject("flownodeinstanceid"));
								oneTimer.put(cstTimerEventDefinitionId, rs.getObject("flownodefinitionid"));
								oneTimer.put("processinstanceid", oneTimer.get("rootprocessinstanceid") != null ? oneTimer.get("rootprocessinstanceid") : oneTimer.get("baseprocessinstanceid"));
								oneTimer.put(cstTimerProcessdefinitionid, oneTimer.get("rootprocessdefinitionid") != null ? oneTimer.get("rootprocessdefinitionid") : oneTimer.get("baseprocessdefinitionid"));
								oneTimer.put(cstTimerProcessDefinitionName, oneTimer.get("rootprocessdefinitionname") != null ? oneTimer.get("rootprocessdefinitionname") : oneTimer.get("baseprocessdefinitionname"));
								oneTimer.put(cstTimerProcessDefinitionVersion, oneTimer.get("rootprocessdefinitionversion") != null ? oneTimer.get("rootprocessdefinitionversion") : oneTimer.get("baseprocessdefinitionversion"));

								// retrieve the original timer

								listTimers.add(oneTimer);

						}
						rs.close();
						stmt.close();
						con = null;

						// now, complete the timer by some API request
						listTimers = completeTimerList(includeDate, listTimers, processAPI);

						HashMap<Long, FlowNodeDefinition> cacheFlowNode = new HashMap<Long, FlowNodeDefinition>();
						for (HashMap<String, Object> oneTimer : listTimers) {
								long flowNodeInstanceId = (Long) oneTimer.get(cstTimerFlownodeInstanceId);
								long flowNodeDefinitionId = (Long) oneTimer.get(cstTimerEventDefinitionId);
								long processDefinitionId = (Long) oneTimer.get(cstTimerProcessdefinitionid);

								FlowNodeDefinition flowNodeDefinition = cacheFlowNode.get(flowNodeDefinitionId);
								if (flowNodeDefinition == null) {
										DesignProcessDefinition designProcessDefinition = processAPI.getDesignProcessDefinition(processDefinitionId);
										flowNodeDefinition = designProcessDefinition.getFlowElementContainer().getFlowNode(flowNodeDefinitionId);
										cacheFlowNode.put(flowNodeDefinitionId, flowNodeDefinition);
								}
								if (flowNodeDefinition != null)
										oneTimer.put(cstTimerEventDefinitionName, flowNodeDefinition.getName());

						}

						/*
						 * HashMap<String,Object> oneTimer= new HashMap<String,Object>();
						 * listTimers.add( oneTimer); oneTimer.put("name","timername");
						 * 
						 * oneTimer.put("processinstanceid", "rootprocessinstanceid");
						 * oneTimer.put("processdefinitionid", "rootprocessdefinitionid");
						 * oneTimer.put("processdefinitionname",
						 * "rootprocessdefinitionname");
						 * oneTimer.put("processdefinitionversion",
						 * "rootprocessdefinitionversion");
						 */

						result.put(cstResultListTimers, listTimers);
						// process nme
						// process version
						// processID
						// task nam�e
						// processinstance
						// date de creation

						result.put(cstResultTimerStatus, "Found " + listTimers.size() + " timer" + (listTimers.size() > 1 ? "s" : ""));

				} catch (Exception e) {
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						String exceptionDetails = sw.toString();

						logger.severe("Exception " + e.toString() + " at " + exceptionDetails);
						
						result.put(cstResultTimerError, errorForJson( e.toString()));

				}
				return result;
		}

		/**
		 * create the missing timer
		 * 
		 * @param inputStreamJarFile
		 * @return
		 */
		public static HashMap<String, Object> createMissingTimers(MethodResetTimer methodResetTimer, InputStream inputStreamJarFile, ProcessAPI processAPI, CommandAPI commandAPI, PlatformAPI platformAPI) {
				HashMap<String, Object> result = new HashMap<String, Object>();

				// HashMap<String, Object> missingTimers = getSimulation(processAPI); //
				// getMissingTimers(
				// processAPI);
				HashMap<String, Object> missingTimers = getMissingTimers(true, processAPI);

				ArrayList<HashMap<String, Object>> listMissingTimer = (ArrayList<HashMap<String, Object>>) missingTimers.get(cstResultListTimers);
				int nbTimersToCreate = listMissingTimer.size();

				try {

						if (MethodResetTimer.RetryTask.equals(methodResetTimer)) {
								for (HashMap<String, Object> oneTimer : listMissingTimer) {
										// create the timer

										Long flowNodeInstanceId = (Long) oneTimer.get(Timer.cstTimerFlownodeInstanceId);

										processAPI.retryTask(flowNodeInstanceId);
								}

						} else if (MethodResetTimer.ExecuteTask.equals(methodResetTimer)) {
								for (HashMap<String, Object> oneTimer : listMissingTimer) {

										Long flowNodeInstanceId = (Long) oneTimer.get(Timer.cstTimerFlownodeInstanceId);

										processAPI.executeFlowNode(flowNodeInstanceId);
								}

						} else {

								CommandDescriptor command = deployCommand("TowTruckCreateTimer", "Create timer", "com.bonitasoft.custompage.bigApp.cmdtimer.CmdCreateTimer", inputStreamJarFile, "custompagelongboard", commandAPI, platformAPI);

								HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
								parameters.put(CmdCreateTimer.cstParamListMissingTimer, listMissingTimer);
								parameters.put(CmdCreateTimer.cstParamMethodReset, methodResetTimer.toString());

								Serializable resultCommand = commandAPI.execute(command.getId(), parameters);
								HashMap<String, Object> resultCommandHashmap = (HashMap<String, Object>) resultCommand;
								if (resultCommandHashmap == null) {
										logger.info("#### Timer : Can't access the command");
										result.put("timererror", "Can't access the command");
								} else {
										// retrieve again the missing timer
										missingTimers = getMissingTimers(false, processAPI);
										result.put(cstResultListTimers, missingTimers.get(cstResultListTimers));
										result.put(cstResultTimerStatus, "Found " + nbTimersToCreate + " missing timer, create: " + missingTimers.get(cstResultTimerStatus));
										result.put(cstResultTimerStatus, resultCommandHashmap.get(cstResultTimerStatus));
										result.put(cstResultTimerError, errorForJson( (String) resultCommandHashmap.get(cstResultTimerError)) );
								}
						}
				} catch (Exception e) {
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						String exceptionDetails = sw.toString();

						logger.severe("Exception " + e.toString() + " at " + exceptionDetails);
						result.put(cstResultTimerError, errorForJson(e.toString()));

				}
				// calculate again the list
				missingTimers = getMissingTimers(false, processAPI);
				listMissingTimer = (ArrayList<HashMap<String, Object>>) missingTimers.get(cstResultListTimers);
				result.put(cstResultListTimers, listMissingTimer);

				return result;
		}

		/**
		 * only for test reason : list the time and then delete it.
		 * 
		 * @param processAPI
		 * @return
		 */
		public static HashMap<String, Object> deleteTimers(ProcessAPI processAPI ) {
				HashMap<String, Object> result = new HashMap<String, Object>();

				Context ctx = null;
				DataSource ds = null;
				Connection con = null;
				Statement stmt = null;

				try {

						ctx = new InitialContext();
						ds = (DataSource) ctx.lookup("java:/comp/env/bonitaSequenceManagerDS");
						con = ds.getConnection();
						stmt = con.createStatement();

						String sqlRequest = "select id from flownode_instance where kind ='intermediateCatchEvent' and statename='waiting'";

						ResultSet rs = stmt.executeQuery(sqlRequest);
						HashMap<Long, HashMap<String, Object>> listTimersByFlowNode = new HashMap<Long, HashMap<String, Object>>();
						String sqlRequestWhere = "1=0";
						while (rs.next()) {
								long flowNodeId = Long.valueOf(rs.getObject("id").toString());
								sqlRequestWhere += " or job_name like '%" + flowNodeId + "%'";
								HashMap<String, Object> record = new HashMap<String, Object>();
								record.put("flownodeid", flowNodeId);
								listTimersByFlowNode.put(flowNodeId, record);
						}
						rs.close();

						// # get triggerNamer from the flownodeinstandeid
						// select trigger_name from qrtz_triggers where job_name like '%9800004%';
						sqlRequest = "select trigger_name, job_name from qrtz_triggers where " + sqlRequestWhere;
						String sqlRequestWhereTriggerName = "1=0";
						String sqlRequestWhereJobTriggerName = "1=0";

						rs = stmt.executeQuery(sqlRequest);
						while (rs.next()) {
								String triggerName = rs.getObject("trigger_name").toString();
								String jobName = rs.getObject("job_name").toString();

								// ok, let's retrieve the correct id
								try {
										String flowNodeIdSt = jobName.substring("Timer_EV_".length());
										long flowNodeId = Long.valueOf(flowNodeIdSt);
										HashMap<String, Object> record = listTimersByFlowNode.get(flowNodeId);
										record.put("trigger_name", triggerName);
										record.put("job_name", jobName);
								} catch (Exception e) {
								}
								;

								sqlRequestWhereTriggerName += " or (trigger_name like '" + triggerName + "') ";
								sqlRequestWhereJobTriggerName += " or (jobtriggername like '" + triggerName + "') ";

						}

						// => OneShotTrigger-6516600448295620001

						// delete now

						// # Delete entry in qrtz_simple_triggers
						// --delete from qrtz_simple_triggers where trigger_name like
						// 'OneShotTrigger-6516600448295620001';
						stmt.executeUpdate("delete from qrtz_simple_triggers where " + sqlRequestWhereTriggerName);

						// # Delete entry in qrtz_triggers
						// delete from qrtz_triggers where trigger_name like
						// 'OneShotTrigger-6516600448295620001';
						stmt.executeUpdate("delete from qrtz_triggers where " + sqlRequestWhereTriggerName);

						// # Delete entry from event_trigger_instance
						// delete from event_trigger_instance where
						// jobtriggername='OneShotTrigger-6516600448295620001';
						stmt.executeUpdate("delete from event_trigger_instance where " + sqlRequestWhereJobTriggerName);

						con.commit();

						result.put("message", "Delete [" + listTimersByFlowNode.size() + "] timers");

				} catch (Exception e) {
						result.put("message", "Error " + e.toString());
				} finally {
						if (stmt != null)
								try {
										stmt.close();
								} catch (SQLException e) {
								}
						con = null;

				}
				HashMap<String, Object> missingTimers = getMissingTimers(true, processAPI);

				ArrayList<HashMap<String, Object>> listMissingTimer = (ArrayList<HashMap<String, Object>>) missingTimers.get(cstResultListTimers);
				result.put(cstResultListTimers, listMissingTimer);

				
				return result;
		}

		/**
		 * 
		 * @param commandName
		 * @param commandDescription
		 * @param className
		 * @param inputStreamJarFile
		 * @param jarName
		 * @param commandAPI
		 * @return
		 * @throws IOException
		 * @throws AlreadyExistsException
		 * @throws CreationException
		 * @throws CommandNotFoundException
		 * @throws DeletionException
		 */
		private static CommandDescriptor deployCommand(String commandName, String commandDescription, String className, InputStream inputStreamJarFile, String jarName, CommandAPI commandAPI, PlatformAPI platFormAPI) throws IOException, AlreadyExistsException, CreationException, CommandNotFoundException, DeletionException {

				String message = "";

				try {
						// pause the engine to deploy a command
						if (platFormAPI != null)
								platFormAPI.stopNode();

						List<CommandDescriptor> listCommands = commandAPI.getAllCommands(0, 1000, CommandCriterion.NAME_ASC);
						for (CommandDescriptor command : listCommands) {
								if (commandName.equals(command.getName()))
										commandAPI.unregister(command.getId());
						}

						/*
						 * File commandFile = new File(jarFileServer); FileInputStream fis =
						 * new FileInputStream(commandFile); byte[] fileContent = new
						 * byte[(int) commandFile.length()]; fis.read(fileContent);
						 * fis.close();
						 */
						ByteArrayOutputStream fileContent = new ByteArrayOutputStream();
						byte[] buffer = new byte[10000];
						int nbRead = 0;
						while ((nbRead = inputStreamJarFile.read(buffer)) > 0) {
								fileContent.write(buffer, 0, nbRead);
						}

						try {
								commandAPI.removeDependency(jarName);
						} catch (Exception e) {
						}
						;

						message += "Adding jarName [" + jarName + "] size[" + fileContent.size() + "]...";
						commandAPI.addDependency(jarName, fileContent.toByteArray());
						message += "Done.";

						message += "Registering...";
						CommandDescriptor commandDescriptor = commandAPI.register(commandName, commandDescription, className);

						if (platFormAPI != null)
								platFormAPI.startNode();
						return commandDescriptor;

				} catch (StopNodeException | StartNodeException e1) {
						logger.severe("Can't stop or start [" + e1.toString() + "]");
						message += e1.toString();
						return null;
				}
		}

		/**
		 * 
		 * @param processAPI
		 * @return
		 */
		private static HashMap<String, Object> getSimulation(ProcessAPI processAPI) {
				HashMap<String, Object> result = new HashMap<String, Object>();
				ArrayList<HashMap<String, Object>> listTimersToCreate = new ArrayList<HashMap<String, Object>>();
				result.put(cstResultListTimers, listTimersToCreate);

				HashMap<String, Object> oneTimer = new HashMap<String, Object>();
				listTimersToCreate.add(oneTimer);
				oneTimer.put("name", "Timer1");

				oneTimer.put(cstTimerFlownodeInstanceId, 20004L);
				oneTimer.put(cstTimerEventDefinitionId, -4896849640881680909L);
				oneTimer.put("processinstanceid", 1002L);

				oneTimer.put(cstTimerProcessdefinitionid, 6220401256700704985L);
				oneTimer.put(cstTimerProcessDefinitionName, "Timer");
				oneTimer.put(cstTimerProcessDefinitionVersion, "1.0");

				completeTimerList(true, listTimersToCreate, processAPI);

				return result;
		}

		/**
		 * 
		 * @param listTimers
		 * @param processAPI
		 * @return
		 */
		private static ArrayList<HashMap<String, Object>> completeTimerList(boolean includeDate, ArrayList<HashMap<String, Object>> listTimers, ProcessAPI processAPI) {

				HashMap<Long, FlowNodeDefinition> cacheFlowNode = new HashMap<Long, FlowNodeDefinition>();
				for (HashMap<String, Object> oneTimer : listTimers) {
						long flowNodeInstanceId = (Long) oneTimer.get(cstTimerFlownodeInstanceId);
						long flowNodeDefinitionId = (Long) oneTimer.get(cstTimerEventDefinitionId);
						long processDefinitionId = (Long) oneTimer.get(cstTimerProcessdefinitionid);

						FlowNodeDefinition flowNodeDefinition = cacheFlowNode.get(flowNodeDefinitionId);
						if (flowNodeDefinition == null) {
								DesignProcessDefinition designProcessDefinition;
								try {
										designProcessDefinition = processAPI.getDesignProcessDefinition(processDefinitionId);
										flowNodeDefinition = designProcessDefinition.getFlowElementContainer().getFlowNode(flowNodeDefinitionId);
										cacheFlowNode.put(flowNodeDefinitionId, flowNodeDefinition);
								} catch (ProcessDefinitionNotFoundException e) {
										logger.severe("FlowNode not found");
										oneTimer.put(cstTimerStatus, "Can't find process");
								}
						}
						try {
								if (flowNodeDefinition != null)
										oneTimer.put(cstTimerEventDefinitionName, flowNodeDefinition.getName());
								if (flowNodeDefinition instanceof IntermediateCatchEventDefinition) {
										IntermediateCatchEventDefinition intermediateCatchEventDefinition = (IntermediateCatchEventDefinition) flowNodeDefinition;
										List<TimerEventTriggerDefinition> listEventTriggerDefinition = intermediateCatchEventDefinition.getTimerEventTriggerDefinitions();
										for (TimerEventTriggerDefinition timerEventTriggerDefinition : listEventTriggerDefinition) {
												TimerType timeType = timerEventTriggerDefinition.getTimerType();
												Expression expression = timerEventTriggerDefinition.getTimerExpression();

												if (timeType == TimerType.DURATION) {
														String expressionContentSt = expression.getContent();
														if (expressionContentSt.endsWith("L"))
																expressionContentSt = expressionContentSt.substring(0, expressionContentSt.length() - 1);

														Long expressionContent = Long.valueOf(expressionContentSt);
														Calendar c = Calendar.getInstance();
														c.add(Calendar.SECOND, (int) (expressionContent / 1000));
														if (includeDate)
																oneTimer.put(cstTimerNewTimerDate, c.getTime());
														oneTimer.put(cstTimerNewTimerDateSt, sdf.format(c.getTime()));
												}
										}
								}
						} catch (Exception e) {
								logger.severe("During calculing the new date " + e.toString());
								oneTimer.put(cstTimerStatus, "During calculing the new date " + e.toString());
						}
						if (oneTimer.get(cstTimerNewTimerDate) == null && includeDate)
								oneTimer.put(cstTimerStatus, "Can't calculate date, no TriggerDefinition attach to this activity");

				}
				return listTimers;
		}
		private static String errorForJson( String error )
		{
				if (error==null)
						return null;
				error = error.replaceAll("\\[", "");
				error = error.replaceAll("\\]", "");
				error = error.replaceAll(";", "");
				error = error.replaceAll("'", "");
				error = error.replaceAll("\"", "");
				error = error.replaceAll("\\(", " ");
				error = error.replaceAll("\\)", " ");
				error = error.replaceAll(":", " ");
				error = error.replaceAll("\\|", " ");
				if (error.length()>30)
				{
						// error = error.substring(0,30);
				}
				
				
				return error;
		}

}
