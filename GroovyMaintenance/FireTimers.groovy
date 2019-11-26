import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.execution.work.ExecuteFlowNodes;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import java.io.StringWriter;
import java.io.PrintWriter;

/* ****************************************************************** */
/*                                                                    */
/* Name: FireTimers_1.1.5                                             */
/*                                                                    */
/* Version: 1.1.5                                             	      */
/*                                                                    */
/* Description: Detect all timers not fired, then fired them          */
/*                                                                    */
/* ****************************************************************** */

/**
* PARAMETERS: put your FLOWNODE ID list in 'flownodesIds' variable
**/

List<Long> flownodesIds = null; // { { ListFlowNodes;tips:Give a list of FlowNodes;type:json } };

List<String> listQuartzJobs = {{ListQuartz;
  type:sql;
  sqlrequest:all:SELECT job_name FROM QRTZ_TRIGGERS, FLOWNODE_INSTANCE  WHERE ( NEXT_FIRE_TIME < @@systemcurrenttimemillis@@ - 60000 OR START_TIME <> NEXT_FIRE_TIME ) AND TRIGGER_STATE = 'WAITING' AND TRIGGER_TYPE = 'SIMPLE' AND FLOWNODE_INSTANCE.ID = CAST( SUBSTR (job_name, 10) as INT);				
  colnameresult:uppercase;
  selecttop:200
}}

int tenantId = 1;
StringWriter strW = new StringWriter();
PrintWriter pw = new PrintWriter(strW);


final PlatformServiceAccessor platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
final TransactionService transactionService = platformServiceAccessor.getTransactionService();
final TenantServiceAccessor tenantServiceAccessor = platformServiceAccessor.getTenantServiceAccessor(tenantId);


try {
	int numberExecutionWithSuccess=0;
	String flowNodesAlreadyTrigger="";
	for (Map record : listQuartzJobs)
	{
		// use the INTERNAL api: the public API will not execute a trigger if the status is still WAITING.
		String triggerName = record.get("JOB_NAME");

		// format is according case 22385:
		// For every flownode id extracted from the list of triggers obtained with the query given by Poorav: 
		// (e.g. Trigger: job_name = 'Timer_Ev_111111' ==> flownode ID: 111111)
		if (triggerName.startsWith("Timer_Ev_" ))
		{
			triggerName = triggerName.substring("Timer_Ev_".length());
						
						
			Set<Long> setFlowNode = new HashSet<Long>();
        		setFlowNode.add( Long.valueOf( triggerName ));
			try
			{
				ExecuteFlowNodes executeFlowNode= new ExecuteFlowNodes(tenantServiceAccessor, setFlowNode.iterator());
				transactionService.executeInTransaction(executeFlowNode);
				numberExecutionWithSuccess++;
			} catch(Exception e)
			{
				// the Scheduler execute the flownode before ?
				flowNodesAlreadyTrigger+=triggerName+",";
			}
		}
	}
	if (flowNodesAlreadyTrigger.length()>0)
		pw.println("Flownode was already executed: "+flowNodesAlreadyTrigger+"<br>");

	
	
	pw.println(" ====> S U C C E S S, executed "+numberExecutionWithSuccess+"/"+listQuartzJobs.size()+" flownodes");
} catch (Exception e) {
	pw.println(" ====> failure " + e.getMessage());
	e.printStackTrace(pw);
}





return strW.toString();
