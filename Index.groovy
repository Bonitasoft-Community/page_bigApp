import org.bonitasoft.web.extension.page.PageContext;
import org.bonitasoft.web.extension.page.PageController;
import org.bonitasoft.web.extension.page.PageResourceProvider;

import org.json.simple.JSONValue

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.util.logging.Logger

public class Index implements PageController {

  private static Logger loggerCustomPage= Logger.getLogger("org.bonitasoft.custompage.bigApp.groovy");
  
  
  public static class ActionAnswer
  {
    /*
     * if true, the answer is managed by the action (else, it should be an HTML call)
     */
    public boolean isManaged=false;
    /*
     * if true, the response is in responseMap, and a JSON is necessary
     */
    public boolean isResponseMap=true;
    /*
     * the response under a Map 
     */
    public Map<String,Object> responseMap =new HashMap<String,Object>();
    public void setResponse(Map<String,Object> response )
    {
      responseMap = response;
      isResponseMap=true;
    }
    
  }
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response, PageResourceProvider pageResourceProvider, PageContext pageContext) {
    
    try {
      String requestParamJson= request.getParameter("paramjson");
      String requestParamJsonSt = (requestParamJson==null ? null : java.net.URLDecoder.decode(requestParamJson, "UTF-8"));

      
      Index.ActionAnswer actionAnswer = Actions.doAction( request, requestParamJsonSt,  response, pageResourceProvider, pageContext );
      if (! actionAnswer.isManaged)
      {
        loggerCustomPage.info("#### CustomPage:Groovy NoAction, return index.html" );
        runTheBonitaIndexDoGet( request, response,pageResourceProvider,pageContext);
        return;
      }
      loggerCustomPage.info("#### CustomPage:Groovy , ResponseMap="+actionAnswer.responseMap.size() );
      
      if (actionAnswer.responseMap.size()>0)
      {
        response.setCharacterEncoding("UTF-8");
        response.addHeader("content-type", "application/json");
        
        PrintWriter out = response.getWriter()
        String jsonSt = JSONValue.toJSONString( actionAnswer.responseMap );
        out.write( jsonSt );
        loggerCustomPage.info("#### ##############################CustomPage: return json["+jsonSt+"]" );
        out.flush();
        out.close();
        return;
      }
      // assuming the DoAction did the job (export a ZIP file for example)
      return;
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String exceptionDetails = sw.toString();
      loggerCustomPage.severe("#### bigappCustomPage:Groovy Exception ["+e.toString()+"] at "+exceptionDetails);
    }
  }
  
  /** -------------------------------------------------------------------------
   *
   *getIntegerParameter
   * 
   */
   public static getIntegerParameter(HttpServletRequest request, String paramName, Integer defaultValue)
  {
    String valueParamSt = request.getParameter(paramName);
    if (valueParamSt==null  || valueParamSt.length()==0)
    {
      return defaultValue;
    }
    try
    {
      return Integer.valueOf( valueParamSt );
    }
    catch( Exception e)
    {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String exceptionDetails = sw.toString();
      
      loggerCustomPage.severe("#### LongBoardCustomPage:Groovy LongBoard: getinteger : Exception "+e.toString()+" on  ["+valueParamSt+"] at "+exceptionDetails );
      return defaultValue;
    }
  }
  /** -------------------------------------------------------------------------
   *
   *getBooleanParameter
   * 
   */
  public static Boolean getBooleanParameter(HttpServletRequest request, String paramName, Boolean defaultValue)
  {
    String valueParamSt = request.getParameter(paramName);
    if (valueParamSt==null  || valueParamSt.length()==0)
    {
      return defaultValue;
    }
    try
    {
      return  Boolean.valueOf( valueParamSt );
    }
    catch( Exception e)
    {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      String exceptionDetails = sw.toString();
      
      loggerCustomPage.severe("#### LongBoardCustomPage:Groovy LongBoard: getBoolean : Exception "+e.toString()+" on  ["+valueParamSt+"] at "+exceptionDetails );
      return defaultValue;
    }
  }
  
  /** -------------------------------------------------------------------------
   *
   *runTheBonitaIndexDoGet
   * 
   */
  private void runTheBonitaIndexDoGet(HttpServletRequest request, HttpServletResponse response, PageResourceProvider pageResourceProvider, PageContext pageContext) {
    try {
        def String indexContent;
        pageResourceProvider.getResourceAsStream("index.html").withStream { InputStream s->
            indexContent = s.getText()
        }
        
        File pageDirectory = pageResourceProvider.getPageDirectory();
                 
        // def String pageResource="pageResource?&page="+ request.getParameter("page")+"&location=";
        // indexContent= indexContent.replace("@_USER_LOCALE_@", request.getParameter("locale"));
        // indexContent= indexContent.replace("@_PAGE_RESOURCE_@", pageResource);
        indexContent= indexContent.replace("@_CURRENTTIMEMILIS_@", String.valueOf(System.currentTimeMillis()));
        indexContent= indexContent.replace("@_PAGEDIRECTORY_@", pageDirectory.getAbsolutePath()) ;
                 
              
        response.setCharacterEncoding("UTF-8");
        response.addHeader("content-type", "text/html");

        PrintWriter out = response.getWriter();
        out.print(indexContent);
        out.flush();
        out.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
    }

}
