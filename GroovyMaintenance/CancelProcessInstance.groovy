
import org.bonitasoft.engine.api.ProcessAPI ;


ProcessAPI processAPI = apiAccessor.getProcessAPI();
try
{
   String processInstanceSt = {{processInstanceId;tips:This process instance will be cancelled (archived)}}
   if (processInstanceSt == null)
   {
      return "Please give a processInstanceId";
   }
   long processInstance = Long.valueOf( processInstanceSt );
   processAPI.cancelProcessInstance( processInstance ) ;
   return "Case "+processInstance+" was successfully cancelled (archived)";
}
catch(Exception e)
{
    return e.getMessage();
}
