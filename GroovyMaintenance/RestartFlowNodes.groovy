import java.util.logging.Level
import java.util.logging.Logger

import org.bonitasoft.engine.api.ProcessAPI;

import org.bonitasoft.engine.execution.work.ExecuteFlowNodes;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.TransactionService;

import java.io.StringWriter;
import java.io.PrintWriter;

/* ******************************************************************* */
/*                                                                     */
/* Name: Restarting flow nodes                                         */
/*                                                                     */
/* Description: Use Engine initialization internals to generate works  */
/*                                                                     */
/* ******************************************************************* */

Logger logger = Logger.getLogger("org.bonitasoft.groovymaintenance.executeflownode");

StringBuffer analysis = new StringBuffer();

// flow nodes
// Attention, at this moment, the list may be a list of whatever - soon more control
List<Object> flowNodeIds= {{Flownode_Id_List;tips:Please give a list of nodes id, separated by a comma;type:LIST;default:124, 323}};

int tenantId = 1;


final PlatformServiceAccessor platformServiceAccessor = ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
final TransactionService transactionService = platformServiceAccessor.getTransactionService();
final TenantServiceAccessor tenantServiceAccessor = platformServiceAccessor.getTenantServiceAccessor(tenantId);

analysis.append("Support Restarting flow nodes ... ");
try {
  final List<Long> oneFlowNodeIdList = new ArrayList<>();
  boolean enterLoop = true;
  for (Object flowNodeId : flowNodeIds) {
    if (!enterLoop) {
      analysis.append(", ");
    } else {
      enterLoop=false;
    }
    analysis.append(flowNodeId.toString());
    oneFlowNodeIdList.clear();
    oneFlowNodeIdList.add((Long) flowNodeId);
    final Iterator<Long> iterator = oneFlowNodeIdList.iterator();
    try {
      transactionService.executeInTransaction(new ExecuteFlowNodes(tenantServiceAccessor, iterator));
      while (iterator.hasNext()) {
        transactionService.executeInTransaction(new ExecuteFlowNodes(tenantServiceAccessor, iterator));
      }
    } catch (Exception e1) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e1.printStackTrace(pw);
      analysis.append(" (FAILURE) - "+ e1.getMessage() + "\n" + sw.toString() + "\n");
      pw.close();
      sw.close();
    }
  }
} catch (Exception e2) {
  StringWriter sw = new StringWriter();
  PrintWriter pw = new PrintWriter(sw);
  e2.printStackTrace(pw);
  analysis.append("\nException: "+ e2.getMessage() + "\n" + sw.toString());
  pw.close();
  sw.close();
}
logger.log(Level.INFO, analysis.toString());

return analysis.toString();


