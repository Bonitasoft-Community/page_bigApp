import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.execution.work.ExecuteFlowNodes;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import java.io.StringWriter;
import java.io.PrintWriter;

/* ****************************************************************** */
/*                                                                    */
/* Name: SlqCall                                                      */
/*                                                                    */
/* Description: Execute a Sql Call to get the list of Process         */
/*                                                                    */
/* ****************************************************************** */

/**
* PARAMETERS: put your FLOWNODE ID list in 'flownodesIds' variable
**/

String filterOnProcess= {{filterProcess;type=STRING}};
String filterOnVersion= {{filterVersion;type=STRING}};

List<Map<String,Object>> listProcess = {{currentProcess;
type:sql;
sqlrequest:all:SELECT * from process_definition;
selecttop:2
}}

int tenantId = 1;
String message="Start ";
ProcessAPI processAPI = apiAccessor.getProcessAPI();

message+= "Nb process detected: "+listProcess.size()
for (Map record : listProcess)
{
  Long processDefinitionId= record.get("PROCESSID");
  message+="\n ProcessId="+processDefinitionId;
  try
  {
    ProcessDefinition processDefinition= processAPI.getProcessDefinition(processDefinitionId);
    message+=" Name="+processDefinition.getName()+" Version="+processDefinition.getVersion();
  }
  // may be disable
  catch(Exception e)
  {
    message+=" exception "+e.getMessage() 
  }
}
message+=" Tne end;"
return message;
