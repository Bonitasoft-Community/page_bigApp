import com.bonitasoft.custompage.bigApp.environment.EnvironmentDetails
import com.bonitasoft.custompage.bigApp.groovymaintenance.GroovyMaintenance
import com.bonitasoft.custompage.bigApp.logs.Logs
import com.bonitasoft.custompage.bigApp.setupconfiguration.SetupConfiguration
import com.bonitasoft.engine.api.TenantAPIAccessor
import org.bonitasoft.engine.api.*
import org.bonitasoft.engine.session.APISession
import org.bonitasoft.serverconfiguration.CollectResultDecoZip
import org.bonitasoft.web.extension.ResourceProvider
import org.bonitasoft.web.extension.page.PageContext
import org.bonitasoft.web.extension.page.PageResourceProvider
import org.bonitasoft.web.extension.rest.RestAPIContext
import org.codehaus.groovy.control.CompilerConfiguration
import org.json.simple.JSONValue

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

public class Actions {

    private static Logger logger = Logger.getLogger("org.bonitasoft.custompage.bigApp.groovy");


    public final static String GROOVY_REST_API_CONTEXT = "restAPIContext";
    public final static String GROOVY_API_ACCESSOR = "apiAccessor";
    public final static String GROOVY_API_CLIENT = "apiClient";


    /**
     * build the apiAccessor access
     *
     */
    public static class MyApiAccessor {
        APISession session;

        public ProcessAPI getProcessAPI() {
            logger.info("CALL GetprocessAPI()");
            return TenantAPIAccessor.getProcessAPI(session);
        }

        public IdentityAPI getIdentityAPI() {
            return TenantAPIAccessor.getIdentityAPI(session);
        }

        public CommandAPI getCommandAPI() {
            return TenantAPIAccessor.getCommandAPI(session);
        }

        public BusinessDataAPI getBusinessDataAPI() {
            return TenantAPIAccessor.getBusinessDataAPI(session);
        }

        public PageAPI getCustomPageAPI() {
            return TenantAPIAccessor.getCustomPageAPI(session);
        }

        public ApplicationAPI getLivingApplicationAPI() {
            return TenantAPIAccessor.getLivingApplicationAPI(session);
        }

        public LoginAPI getLoginAPI() {
            return TenantAPIAccessor.getLoginAPI(session);
        }

        public ProfileAPI getProfileAPI() {
            return TenantAPIAccessor.getProfileAPI(session);
        }

        public TenantAdministrationAPI getTenantAdministrationAPI() {
            return TenantAPIAccessor.getTenantAdministrationAPI(session);
        }

        public ThemeAPI getThemeAPI() {
            return TenantAPIAccessor.getThemeAPI(session);
        }

        public refresh() {
            TenantAPIAccessor.refresh();
        }
    }

    /**
     * build the APIClient access
     *
     */
    public static class MyAPIClient extends APIClient {
        APISession session;

        public ProcessAPI getProcessAPI() {
            logger.info("CALL MyAPIClient.GetprocessAPI()");
            return TenantAPIAccessor.getProcessAPI(session);
        }

        public IdentityAPI getIdentityAPI() {
            return TenantAPIAccessor.getIdentityAPI(session);
        }

        public CommandAPI getCommandAPI() {
            return TenantAPIAccessor.getCommandAPI(session);
        }

        public BusinessDataAPI getBusinessDataAPI() {
            return TenantAPIAccessor.getBusinessDataAPI(session);
        }

        public PageAPI getCustomPageAPI() {
            return TenantAPIAccessor.getCustomPageAPI(session);
        }

        public PermissionAPI getPermissionAPI() {
            return null; // does not exist
        }

        public ProfileAPI getProfileAPI() {
            return TenantAPIAccessor.getProfileAPI(session);
        }

        public APISession getSession() {
            return session;
        }

        public TenantAdministrationAPI getTenantAdministrationAPI() {
            return TenantAPIAccessor.getTenantAdministrationAPI(session);
        }

        public ThemeAPI getThemeAPI() {
            return TenantAPIAccessor.getThemeAPI(session);
        }
    }

    /**
     * build the RestAPIContext access
     *
     */
    public static class MyRestContext implements RestAPIContext {
        public MyAPIClient myApiClient;
        public Locale locale;
        public PageResourceProvider resourceProvider;

        public APIClient getApiClient() {
            logger.info("CALL MyRestContext.getApiClient()");
            return myApiClient;
        }

        public APISession getApiSession() {
            return myApiClient.session;
        }

        public Locale getLocale() {
            return locale;
        }

        public ResourceProvider getResourceProvider() {
            return resourceProvider;
        }
    }

    public static Index.ActionAnswer doAction(HttpServletRequest request, String paramJsonSt, HttpServletResponse response, PageResourceProvider pageResourceProvider, PageContext pageContext) {

        logger.info("#### bigappCustomPage:Actions start");
        Index.ActionAnswer actionAnswer = new Index.ActionAnswer();
        try {
            String action = request.getParameter("action");
            logger.info("#### bigappCustomPage:Actions  action is[" + action + "] !");
            if (action == null || action.length() == 0) {
                actionAnswer.isManaged = false;
                logger.info("#### bigappCustomPage:Actions END No Actions");
                return actionAnswer;
            }
            actionAnswer.isManaged = true;

            HttpSession httpSession = request.getSession();
            APISession session = pageContext.getApiSession();
            ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
            IdentityAPI identityApi = TenantAPIAccessor.getIdentityAPI(session);
            CommandAPI commandAPI = TenantAPIAccessor.getCommandAPI(session);
            File pageDirectory = pageResourceProvider.getPageDirectory();

            if ("getLogs".equals(action)) {
                actionAnswer.responseMap.put("logs", Logs.getLogs());
            } else if ("getEnvironment".equals(action)) {
                Map<String, Object> result = new HashMap<String, Object>();
                result = EnvironmentDetails.getEnvironment(session);
                actionAnswer.setResponse(result);
            } else if ("getSetupConfiguration".equals(action)) {
                String listLogs = request.getParameter("listLogs");
                Boolean pullConfActivated = Boolean.parseBoolean(request.getParameter("pullConfActivated"));

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                LocalDateTime now = LocalDateTime.now();

                response.addHeader("content-disposition", "attachment; filename=BigAppExtract" + dtf.format(now) + ".zip");
                response.addHeader("content-type", "application/zip");

                CollectResultDecoZip.ResultZip resultZip = SetupConfiguration.getSetupConfiguration(pageDirectory, session, listLogs, pullConfActivated);

                OutputStream output = response.getOutputStream();
                resultZip.zipContent.writeTo(output);

                output.flush();
                output.close();
                return actionAnswer;

            } else if ("groovyload".equals(action)) {
                String groovyCode = request.getParameter("code");
                actionAnswer.responseMap = GroovyMaintenance.getGroovyMaintenance(request, groovyCode, pageDirectory);
            } else if ("groovyinterpretation".equals(action)) {
                String paramJsonPartial = request.getParameter("paramjson");
                logger.info("collect_add paramJsonPartial=[" + paramJsonPartial + "]");

                String accumulateJson = (String) httpSession.getAttribute("accumulate");
                if (accumulateJson == null)
                    accumulateJson = "";
                String firstUrl = request.getParameter("firstUrl");

                if ("1".equals(firstUrl))
                    accumulateJson = "";

                if (paramJsonPartial != null)
                    accumulateJson += paramJsonPartial;
                //already decode by Tomcat  java.net.URLDecoder.decode(paramJsonPartial, "UTF-8");


                logger.info("Final Accumulator: accumulateJson=[" + accumulateJson + "]");

                Map<String, Object> groovyParameters = JSONValue.parse(accumulateJson);

                // reset the accumulator
                httpSession.setAttribute("accumulate", "");

                String groovySrc = null;
                String type = groovyParameters.get("type");
                groovySrc = groovyParameters.get("src");
                logger.info("#### bigappCustomPage:GroovyInterpretation directSrc startby[" + (groovySrc.length() > 10 ? groovySrc.substring(0, 8) + "..." : groovySrc) + "]");
                actionAnswer.responseMap = GroovyMaintenance.getGroovyIntepretation(request, groovySrc, pageDirectory);
            } else if ("groovyexecute".equals(action)) {
                String paramJsonPartial = request.getParameter("paramjson");
                logger.info("collect_add paramJsonPartial=[" + paramJsonPartial + "]");

                String accumulateJson = (String) httpSession.getAttribute("accumulate");
                if (accumulateJson == null)
                    accumulateJson = "";

                String firstUrl = request.getParameter("firstUrl");
                logger.info("FirstUrl=" + firstUrl);
                if ("1".equals(firstUrl)) {
                    accumulateJson = "";
                }

                logger.info("accumulateJson=" + accumulateJson);

                if (paramJsonPartial != null)
                    accumulateJson += paramJsonPartial;
                //already decode by Tomcat  java.net.URLDecoder.decode(paramJsonPartial, "UTF-8");


                logger.info("Final Accumulator: accumulateJson=[" + accumulateJson + "]");

                Map<String, Object> groovyParameters = JSONValue.parse(accumulateJson);

                // reset the accumulator
                httpSession.setAttribute("accumulate", "");


                String groovySrc = null;
                String type = groovyParameters.get("type");
                if (type != null && ("src".equals(type) || "srcparameter".equals(type) || type.length() == 0)) {
                    groovySrc = groovyParameters.get("src");
                    logger.info("#### bigappCustomPage:GroovyExecution directSrc startby[" + (groovySrc.length() > 10 ? groovySrc.substring(0, 8) + "..." : groovySrc) + "]");

                }

                // Actions actions = new Actions();
                try {
                    // actionAnswer.responseMap.put("result", actions.executeGroovy(groovySrc, pageResourceProvider, pageContext));
                    Binding binding = getBinding(pageResourceProvider, pageContext);
                    actionAnswer.responseMap = GroovyMaintenance.executeGroovyMaintenance(request, groovySrc, (List) groovyParameters.getAt("placeholder"), binding);


                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    pw.println(e.getMessage());
                    e.printStackTrace(pw);
                    pw.flush();
                    actionAnswer.responseMap.put("exception", sw.toString());
                    pw.close();
                    sw.close();
                }
            } else if ("groovyrest".equals(action)) {
                // first, load the code
                String groovyCode = request.getParameter("code");
                actionAnswer.responseMap = GroovyMaintenance.getGroovyMaintenance(request, groovyCode, pageDirectory);

                String status = actionAnswer.responseMap.get("status");

                if ("DOWNLOADED".equals(status)) {
                    // second, execute
                    Binding binding = getBinding(pageResourceProvider, pageContext);
                    List<Map<String, Object>> groovyParameters = new ArrayList();
                    logger.info("#### bigappCustomPage:Request.getParametersName=" + request.getParameterNames());

                    for (String parameterName : request.getParameterNames()) {
                        Map oneParameter = ["name": parameterName, "value": request.getParameter(parameterName)];
                        groovyParameters.add(oneParameter);
                        logger.info("#### bigappCustomPage:Parameter[" + parameterName + "] value=[" + request.getParameter(parameterName) + "]");

                    }

                    actionAnswer.responseMap = GroovyMaintenance.executeGroovyMaintenance(request, null, (List) groovyParameters, binding);

                }


                // collect mechanism
            } else if ("collect_add".equals(action)) {
                String paramJsonPartial = request.getParameter("paramjson");
                logger.info("collect_add paramJsonPartial=[" + paramJsonPartial + "]");

                String accumulateJson = (String) httpSession.getAttribute("accumulate");
                if (accumulateJson == null)
                    accumulateJson = "";

                String firstUrl = request.getParameter("firstUrl");
                if ("1".equals(firstUrl))
                    accumulateJson = "";

                accumulateJson += paramJsonPartial;
                // Tomcat already decode java.net.URLDecoder.decode(paramJsonPartial, "UTF-8");
                httpSession.setAttribute("accumulate", accumulateJson);
                actionAnswer.responseMap.put("status", "ok");
            } else {
                actionAnswer.responseMap.put("timerstatus", "Unknow command [" + action + "]");
            }

            logger.info("#### bigappCustomPage:Actions END responseMap =" + actionAnswer.responseMap.size());
            return actionAnswer;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionDetails = sw.toString();
            logger.severe("#### bigappCustomPage:Groovy Exception [" + e.toString() + "] at " + exceptionDetails);
            actionAnswer.isResponseMap = true;
            actionAnswer.responseMap.put("Error", "bigappCustomPage:Groovy Exception [" + e.toString() + "] at " + exceptionDetails);
            return actionAnswer;
        }
    }


    public String executeGroovy(String script, PageResourceProvider pageResourceProvider, PageContext pageContext) throws Exception {
        MyApiAccessor myApiAccessor = new MyApiAccessor();
        myApiAccessor.session = pageContext.getApiSession();
        MyAPIClient myAPIClient = new MyAPIClient();
        myAPIClient.session = pageContext.getApiSession();

        MyRestContext myRestContext = new MyRestContext();
        myRestContext.myApiClient = myAPIClient;
        myRestContext.locale = pageContext.getLocale();
        myRestContext.resourceProvider = pageResourceProvider;

        Binding binding = new Binding();
        binding.setVariable(GROOVY_REST_API_CONTEXT, myRestContext);
        binding.setVariable(GROOVY_API_ACCESSOR, myApiAccessor);
        binding.setVariable(GROOVY_API_CLIENT, myAPIClient);

        // GroovyShell shell = new GroovyShell(getClass().getClassLoader(), binding);
        CompilerConfiguration conf = new CompilerConfiguration();
        GroovyShell shell = new GroovyShell(binding, conf);

        String result = shell.evaluate(script);
        // logger.info("#### bigappCustomPage:Result ="+result);
        if (result == null)
            result = "Script was executed with success, but do not return any result."
        return result;

    }


    private static Binding getBinding(PageResourceProvider pageResourceProvider, PageContext pageContext) {
        MyApiAccessor myApiAccessor = new MyApiAccessor();
        myApiAccessor.session = pageContext.getApiSession();
        MyAPIClient myAPIClient = new MyAPIClient();
        myAPIClient.session = pageContext.getApiSession();

        MyRestContext myRestContext = new MyRestContext();
        myRestContext.myApiClient = myAPIClient;
        myRestContext.locale = pageContext.getLocale();
        myRestContext.resourceProvider = pageResourceProvider;

        Binding binding = new Binding();
        binding.setVariable(GROOVY_REST_API_CONTEXT, myRestContext);
        binding.setVariable(GROOVY_API_ACCESSOR, myApiAccessor);
        binding.setVariable(GROOVY_API_CLIENT, myAPIClient);
        return binding;
    }


}
