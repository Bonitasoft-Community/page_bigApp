import org.bonitasoft.engine.api.ProcessAPI;

/* ****************************************************************** */
/*                                                                    */
/* Name: Ping                                                         */
/*                                                                    */
/* Description: just get a parameters, then print it                  */
/*                                                                    */
/* ****************************************************************** */

StringBuffer result= new StringBuffer(" start ");
ProcessAPI processAPI = apiAccessor.getProcessAPI();
// ProcessAPI processAPI = apiClient.getProcessAPI();
// ProcessAPI processAPI = restAPIContext.getApiClient().getProcessAPI();
if (processAPI==null)
	result.append( " NoProcessAPI" );
else
{
	long instances = processAPI.getNumberOfProcessInstances();

	result.append( "get ProcessAPI: ");
	result.append( String.valueOf( instances ));
	result.append(" : hello ");
	result.append({{FirstName;tips:Give the first name to retrieve it in the result}})
	result.append({{LastName}});
}
return result.toString();
