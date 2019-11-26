package com.bonitasoft.custompage.bigApp.groovymaintenance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.log.event.BEvent;
import org.bonitasoft.log.event.BEvent.Level;

public class PlaceHolder {

  private final static BEvent SUSPICIOUS_GROOVY = new BEvent(GroovyMaintenance.class.getName(), 2, Level.APPLICATIONERROR, "Suspicious groovy",
      "The groovy loaded is suspicious",
      "The groovy may be not correct, and execution may be not expected",
      "Check the Groovy code");

  /**
   * in a string, detect all place holder
   * 
   * @param groovySource
   * @param markBegin
   * @param markEnd
   * @return
   */
  public List<BEvent> listEventsDetection;;

  public Set<String> detection(String source, String markBegin, String markEnd) {
    listEventsDetection = new ArrayList<BEvent>();
    Set<String> detectionKey = new HashSet<String>();
    if (source == null)
      return detectionKey;

    int scanPosition = 0;
    while (scanPosition < source.length()) {
      int nextPlaceHolder = source.indexOf(markBegin, scanPosition);
      if (nextPlaceHolder == -1)
        scanPosition = source.length();
      else {
        scanPosition = nextPlaceHolder + 2;
        int endPlaceHolder = source.indexOf(markEnd, nextPlaceHolder + 2);
        if (endPlaceHolder != -1) {
          String placeHolderKey = source.substring(nextPlaceHolder + 2, endPlaceHolder);
          detectionKey.add(placeHolderKey);
          scanPosition = endPlaceHolder;
        } else
          listEventsDetection.add(new BEvent(SUSPICIOUS_GROOVY, "Place Holder starts {{ at position [" + nextPlaceHolder + "] and not finish"));
      }
    }
    return detectionKey;
  }

  /**
   * replace in source all the placeholder
   * 
   * @param source
   * @param mapPlaceHolder
   * @param markBegin
   * @param markEnd
   * @return
   */
  public String replacePlaceHolder(String source, Map<String, Object> mapPlaceHolder, String markBegin, String markEnd) {
    if (source == null)
      return "";

    // replace all place holder now
    for (String key : mapPlaceHolder.keySet()) {
      int loop = 0;
      // do not use the replaceAll : the place holder can be interpreted as a expression
      String keyToSearch = markBegin + key + markEnd;
      while (source.contains(keyToSearch) && loop < 100) {
        Object valueToReplace = mapPlaceHolder.get(key);
        if (valueToReplace == null)
          valueToReplace = "";

        source = source.replace(keyToSearch, valueToReplace.toString());
        loop++;
      }
    }
    return source;
  }

}
