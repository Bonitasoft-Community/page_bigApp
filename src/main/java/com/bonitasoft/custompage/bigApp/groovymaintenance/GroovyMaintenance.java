package com.bonitasoft.custompage.bigApp.groovymaintenance;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;
import org.bonitasoft.log.event.BEventFactory;
import org.bonitasoft.store.BonitaStore;
import org.bonitasoft.store.BonitaStore.DetectionParameters;
import org.bonitasoft.store.BonitaStore.UrlToDownload;
import org.bonitasoft.store.BonitaStoreAPI;
import org.bonitasoft.store.BonitaStoreGit;
import org.bonitasoft.store.StoreResult;
import org.bonitasoft.store.artefact.Artefact;
import org.bonitasoft.store.artefact.Artefact.TypeArtefact;
import org.bonitasoft.store.toolbox.LoggerStore;
import org.codehaus.groovy.control.CompilerConfiguration;

import com.bonitasoft.custompage.bigApp.groovymaintenance.AttributeHolder.TypeAttribute;


import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class GroovyMaintenance {

  public static Logger logger = Logger.getLogger(AttributeHolder.class.getName());

  
  private final static BEvent NO_CODE_FOUND = new BEvent(GroovyMaintenance.class.getName(), 1, Level.APPLICATIONERROR, "This Groovy does code does not exists",
      "No groovy is found with this code.",
      "Nothing to propose",
      "Check the code (and the content of the git repository)");

  private final static BEvent NO_GROOVY_SOURCE = new BEvent(GroovyMaintenance.class.getName(), 2, Level.APPLICATIONERROR, "No Groovy Source",
      "No groovy source is not found",
      "No groovy script is executed",
      "Check the session : you may be disconnected");

  private final static BEvent GROOVY_DOWNLOADED = BEvent.getInstanceShortSuccess(GroovyMaintenance.class.getName(), 3, "Groovy Downloaded");

  private final static BEvent GROOVY_EXECUTED = BEvent.getInstanceShortSuccess(GroovyMaintenance.class.getName(), 4, "Groovy executed with success");

  private final static BEvent GROOVY_EXECUTION_FAILED = new BEvent(GroovyMaintenance.class.getName(), 5, Level.APPLICATIONERROR, "Groovy executioopn failed",
      "The groovy send an error",
      "Script is not executed",
      "Check the result");

  public static Map<String, Object> getGroovyIntepretation(HttpServletRequest request, String groovySource, File pageDirectory) {
      ResultInterpretation resultInterpretation = interpretation( groovySource, request, "");
      resultInterpretation.result.put("listevents", BEventFactory.getHtml(resultInterpretation.listEvents));
      return resultInterpretation.result;
  }
  
  
  /*
   * Load a groovy maintenance for Github
   */
  public static Map<String, Object> getGroovyMaintenance(HttpServletRequest request, String groovyCode, File pageDirectory) {
    Map<String, Object> result = new HashMap<String, Object>();
    List<BEvent> listEvents = new ArrayList<BEvent>();

    LoggerStore logBox = new LoggerStore();
    try {
      BonitaStoreAPI bonitaStoreAPI = new BonitaStoreAPI();

      // for debug reason, let's place the directory BEFORE
      List<BonitaStore> listStores = new ArrayList<BonitaStore>();

      File grovvyMaintenanceFile = new File(pageDirectory.getAbsolutePath() + "/GroovyMaintenance");
      BonitaStore bonitaStoreDisk = bonitaStoreAPI.getDirectoryStore(grovvyMaintenanceFile);
      listStores.add(bonitaStoreDisk);

      BonitaStoreGit bonitaStoreGit = bonitaStoreAPI.getGitStore(BonitaStoreAPI.CommunityGithubUserName, BonitaStoreAPI.CommunityGithubPassword, "https://api.github.com/repos/Bonitasoft-Community/page_towtruck");
      bonitaStoreGit.setSpecificRepository("/contents/GroovyMaintenance");
      listStores.add(bonitaStoreGit);

      DetectionParameters detectionParameters = new BonitaStore.DetectionParameters();
      detectionParameters.listTypeArtefact = Arrays.asList(TypeArtefact.GROOVY);
      Artefact groovyArtefact = null;

      for (BonitaStore bonitaStore : listStores) {
        StoreResult storeResult = bonitaStore.getListArtefacts(detectionParameters, logBox);
        listEvents.addAll(storeResult.getEvents());
        groovyArtefact = storeResult.getArtefactByName(groovyCode);
        if (groovyArtefact != null) {
          break;
        }
      }

      if (groovyArtefact == null) {
        listEvents.add(new BEvent(NO_CODE_FOUND, "Code[" + groovyCode + "]"));
      } else {
        List<Map<String, Object>> listPlaceHolder = new ArrayList<Map<String, Object>>();
        Map<String, AttributeHolder> mapPlaceHolder = new HashMap<String, AttributeHolder>();

        // load it
        StoreResult storeResult = groovyArtefact.getProvider().downloadArtefact(groovyArtefact, UrlToDownload.URLDOWNLOAD, logBox);

        listEvents.addAll(storeResult.getEvents());
        
        if (!BEventFactory.isError(storeResult.getEvents())) {
            ResultInterpretation resultInterpretation = interpretation( storeResult.content, request, groovyCode);
            result.putAll( resultInterpretation.result);
            result.put("directRestApi", getDirectRestApi( groovyCode, resultInterpretation.mapPlaceHolder));
            listEvents.addAll( resultInterpretation.listEvents);
        }   
         
        

      }
    } catch (Exception e) {
      result.put("status", "FAILED");
      logBox.logException("getGroovyMaintenance", e);
    }
    result.put("listevents", BEventFactory.getHtml(listEvents));

    
    return result;
  }

  public static class ResultInterpretation 
  {
      Map<String, Object> result = new HashMap<String, Object>();
      List<BEvent> listEvents = new ArrayList<BEvent>();
      Map<String, AttributeHolder> mapPlaceHolder = new HashMap<String, AttributeHolder>();
      List<Map<String, Object>> listPlaceHolder = new ArrayList<Map<String, Object>>();
      
  }
  
  /**
   * 
   * @param groovySource
   * @return
   */
  private static ResultInterpretation interpretation(String groovySource, HttpServletRequest request, String groovyCode ) {
      ResultInterpretation resultInterpretation = new ResultInterpretation();
      // search Title and Description
      resultInterpretation.result.put("title", detectCartouche("Name", groovySource));
      resultInterpretation.result.put("description", detectCartouche("Description", groovySource));

      // detect all place holders
      PlaceHolder placeHolder = new PlaceHolder();
      Set<String> setDetectionKey = placeHolder.detection(groovySource, "{{", "}}");
      for (String key : setDetectionKey) {

        AttributeHolder attribut = new AttributeHolder(key);
        resultInterpretation.listEvents.addAll(attribut.decodeAttribute());

        resultInterpretation.mapPlaceHolder.put(attribut.name, attribut);
      }
      // do a first execution, after detection
      for (AttributeHolder attribut : resultInterpretation.mapPlaceHolder.values()) {
          resultInterpretation.listEvents.addAll(attribut.execute(resultInterpretation.mapPlaceHolder));
      }

      for (AttributeHolder attribut : resultInterpretation.mapPlaceHolder.values()) {
        if (attribut.isForm())
            resultInterpretation.listPlaceHolder.add(attribut.getForm());
      } // end detection place holder
      if (!BEventFactory.isError(resultInterpretation.listEvents))
      {
          resultInterpretation.result.put("status", "DOWNLOADED");          
          resultInterpretation.listEvents.add(GROOVY_DOWNLOADED);
      }
      else
          resultInterpretation.result.put("status", "NOTEXIST");          
      
      // we saved the groovy in the Tomcat session
      if (request != null) {
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("groovy", groovySource);
        
        Map<String,String> mapPlaceHolderSt = new HashMap<String,String>();
        for (AttributeHolder attribut : resultInterpretation.mapPlaceHolder.values()) {
          mapPlaceHolderSt.put(attribut.name, attribut.serialize());
        }
        
        httpSession.setAttribute("placeholder", mapPlaceHolderSt);
        httpSession.setAttribute("groovyCode", groovyCode);
      }
      resultInterpretation.result.put("placeholder", resultInterpretation.listPlaceHolder);
    
      return resultInterpretation;
  }
  /**
   * execute a Groovy Maintenance code
   * 
   * @param request
   * @param groovySource
   * @param placeHolder
   * @param binding
   * @return
   */
  public static Map<String, Object> executeGroovyMaintenance(HttpServletRequest request, String groovySource, List<Map<String, String>> valuePlaceHolders, Binding binding) {

    Map<String, Object> result = new HashMap<String, Object>();
    List<BEvent> listEvents = new ArrayList<BEvent>();
    String groovyCode="";
    Map<String, AttributeHolder> mapPlaceHolder = new HashMap<String, AttributeHolder>();
    HttpSession httpSession = (request==null ? null : request.getSession());
    if (groovySource == null && httpSession != null) {
      groovySource = (String) httpSession.getAttribute("groovy");
    }
    
    // place holder is in the session every time
    if (httpSession!=null) {
      Map<String, String> mapPlaceHolderSt = (Map) httpSession.getAttribute("placeholder");
      // rebuild the attributes
      for (String keyAttribute : mapPlaceHolderSt.values()) {
        AttributeHolder attribute = AttributeHolder.getInstanceFromSerialisation(keyAttribute);
        mapPlaceHolder.put(attribute.name, attribute);
      }
    }
    
    // code is in the session
    if (httpSession!=null) {
      groovyCode = (String) httpSession.getAttribute("groovyCode");
    }

    if (groovySource == null) {
      listEvents.add(NO_GROOVY_SOURCE);
    } else {
      String groovySourceResolved = groovySource;
      
       
      
      if (valuePlaceHolders != null) {

        // place the value
        for (Map<String, String> value : valuePlaceHolders) {
          AttributeHolder attribut = mapPlaceHolder.get(value.get("name"));
          if (attribut == null)
            continue;
          listEvents.addAll(attribut.setHumanValue(value.get("value")));
        }
      }

      // execute now the different attributes
      for (AttributeHolder attribut : mapPlaceHolder.values()) {
        listEvents.addAll(attribut.execute(mapPlaceHolder));
      }
      // now, get the value 
      Map<String, Object> mapValuePlaceHolder = new HashMap<String, Object>();
      for (AttributeHolder attribut : mapPlaceHolder.values()) {
        mapValuePlaceHolder.put(attribut.key, attribut.getValue());
      }
      PlaceHolder placerHolder = new PlaceHolder();
      groovySourceResolved = placerHolder.replacePlaceHolder(groovySource, mapValuePlaceHolder, "{{", "}}");
      
      result.put("directRestApi", getDirectRestApi( groovyCode, mapPlaceHolder));
      String groovySourceResolvedHtml = groovySourceResolved;
      groovySourceResolvedHtml=groovySourceResolvedHtml.replace("\n", "<br>");
      result.put("groovyResolved",groovySourceResolvedHtml);
      try {
        // now execute it
        CompilerConfiguration conf = new CompilerConfiguration();
        GroovyShell shell = new GroovyShell(binding, conf);
        long timeBegin = System.currentTimeMillis();
        Object resultExecution = shell.evaluate(groovySourceResolved);
        long timeEnd = System.currentTimeMillis();
        // logger.info("#### towtruckCustomPage:Result ="+result);
        if (resultExecution == null)
          resultExecution = "Script was executed with success, but do not return any result.";
        result.put("result", resultExecution);
        listEvents.add(new BEvent(GROOVY_EXECUTED, "Executed in " + (timeEnd - timeBegin) + " ms"));
        result.put("status", "OK");          

      } catch (Exception e) {
        listEvents.add(new BEvent(GROOVY_EXECUTION_FAILED, e, ""));
        result.put("exception", "Error " + e.getMessage());
        result.put("status", "FAILED");          
        logger.severe("customPageTwoTruck.executeGroovyMaintenance" + e.getMessage());

      }
    }
    result.put("listevents", BEventFactory.getHtml(listEvents));

    return result;
  }

  /**
   * detectCartouche
   * 
   * @param detector
   * @param groovySource
   * @return
   */
  private static String detectCartouche(String detector, String groovySource) {
    detector = "/* " + detector + ":";
    int titlePosition = groovySource.indexOf(detector);
    if (titlePosition != -1) {
      int endTitlePosition = groovySource.indexOf("*/", titlePosition);
      String title = groovySource.substring(titlePosition + detector.length(), endTitlePosition);
      title = title.replaceAll("\t", " ");
      return title.trim();
    }
    return null;
  }

  
  private static String getDirectRestApi(String groovyCode, Map<String, AttributeHolder> mapPlaceHolder  ) {
    // calculate the direct RestApiCAll
    String directRestApi="/bonita/portal/custom-page/custompage_bigapp/?page=custompage_bigapp&action=groovyrest&code="+groovyCode;
    for (AttributeHolder attribut : mapPlaceHolder.values()) {
      if ( ! TypeAttribute.SQL.equals( attribut.type ))
      {
        directRestApi+="&"+attribut.name+"="+attribut.getShortValue();
      }
    }
    return directRestApi;
  
  }
  /**
   * to test it
   * 
   * @param args
   */
  public static void main(final String[] args) {

    Map<String, Object> result = getGroovyMaintenance(null, "Ping", new File("d:/test"));
    System.out.println("Result = " + result);
  }
}
