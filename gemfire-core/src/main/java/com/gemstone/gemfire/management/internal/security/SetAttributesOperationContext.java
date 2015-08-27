package com.gemstone.gemfire.management.internal.security;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to encapsulate Context passed AccessControl Plugin for each of the
 * attributes in attribute list passed to setAttributes call on given MBean  
 * 
 * @author tushark
 * @since 9.0
 */
public class SetAttributesOperationContext extends ResourceOperationContext {
  
  private Map<String,ResourceOperationContext> contextMap = null;
  
  public SetAttributesOperationContext(){
    contextMap = new HashMap<String,ResourceOperationContext>();
  }
  
  public void addAttribute(String attr, ResourceOperationContext setterContext) {
    this.contextMap.put(attr, setterContext);
  }
  
  public Map<String,ResourceOperationContext> getAttributesContextMap(){
    return contextMap;
  }

  @Override
  public ResourceOperationCode getResourceOperationCode() {    
    return null;
  }

  @Override
  public OperationCode getOperationCode() {    
    return null;
  }

}
