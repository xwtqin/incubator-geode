package com.gemstone.gemfire.management.internal.security;

/**
 * 
 * ResourceOperationContext passed to AccessControlMBean for Authorization calls made
 * from AccessControlMBean
 * 
 * @author tushark
 * @since 9.0
 *
 */
public class AccessControlContext extends ResourceOperationContext {
  
  private ResourceOperationCode code;
  
  public AccessControlContext(String code){
    this.code = ResourceOperationCode.parse(code);
  }

  @Override
  public ResourceOperationCode getResourceOperationCode() {
    return code;
  }

  @Override
  public OperationCode getOperationCode() {   
    return OperationCode.RESOURCE;
  }
  
  public static AccessControlContext ACCESS_GRANTED_CONTEXT = new AccessControlContext(ResourceConstants.LIST_DS);

}

