package com.gemstone.gemfire.management.internal.security;

/**
 * Interface for AccessControlMBean
 * @author tushark
 * @since 9.0
 */
public interface AccessControlMXBean {

  @ResourceOperation(resource=Resource.MEMBER, operation=ResourceConstants.LIST_DS)
  public boolean authorize(String role);
  
}
