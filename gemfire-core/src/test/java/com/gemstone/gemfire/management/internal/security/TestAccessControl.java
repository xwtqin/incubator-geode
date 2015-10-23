package com.gemstone.gemfire.management.internal.security;

import hydra.Log;

import java.security.Principal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.operations.OperationContext;
import com.gemstone.gemfire.cache.operations.OperationContext.OperationCode;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.management.internal.security.ResourceOperationContext.ResourceOperationCode;
import com.gemstone.gemfire.security.AccessControl;
import com.gemstone.gemfire.security.NotAuthorizedException;

public class TestAccessControl implements AccessControl {
  
  private Principal principal=null;
  
  public static Map<String,Set<ResourceOperationCode>> resourceOpCodeMap = new HashMap<String,Set<ResourceOperationCode>>();
  public static Map<String,Set<OperationCode>> opCodeMap = new HashMap<String,Set<OperationCode>>();
  
  public static void grantResourceOp(String user, ResourceOperationCode code) {
    Set<ResourceOperationCode> list = resourceOpCodeMap.get(user);
    if(list==null) {
      list = new HashSet<ResourceOperationCode>();
      resourceOpCodeMap.put(user,list);
    }
    list.add(code);
  }
  
  public static void grantCacheOp(String user, OperationCode code) {
    Set<OperationCode> list = opCodeMap.get(user);
    if(list==null) {
      list = new HashSet<OperationCode>();
      opCodeMap.put(user,list);
    }
    list.add(code);
  }
  
  public static boolean hasAccessToResourceOp(String user, ResourceOperationCode code) {
    Set<ResourceOperationCode> list = resourceOpCodeMap.get(user);
    return list.contains(code);
  }
  
  public static boolean revokeCacheOp(String user, OperationCode code) {
    Set<OperationCode> list = opCodeMap.get(user);
    boolean removed = list.remove(code);

    if (!removed) {
      Log.getLogWriter().warning("Code " + code + " was not found for REVOKE access");
    }
    return removed;
  }

  public static boolean revokeResourceOp(String user, ResourceOperationCode code) {
    Set<ResourceOperationCode> list = resourceOpCodeMap.get(user);
    boolean removed = list.remove(code);
    if (!removed) {
      Log.getLogWriter().warning("Code " + code + " was not found for REVOKE access");
    }
    return removed;
  }
  
  public static AccessControl create(){
    return new TestAccessControl();
  }

  @Override
  public void close() {
    
  }

  @Override
  public void init(Principal principal, DistributedMember remoteMember, Cache cache) throws NotAuthorizedException {
    this.principal = principal;    
  }

  @Override
  public boolean authorizeOperation(String regionName, OperationContext context) {    
    Log.getLogWriter().info("Context Received " + context);
    Log.getLogWriter().info("Principal " + principal.getName());    
    boolean flag = false;
    if (!context.getOperationCode().equals(OperationCode.RESOURCE)) {
      if (opCodeMap.containsKey(principal.getName())) {
        Set<OperationCode> codes = opCodeMap.get(principal.getName());
        for (OperationCode code : codes) {
          Log.getLogWriter().info("Checking OpCode=" + code);
          flag = code.equals(context.getOperationCode());
          if (flag) {
            Log.getLogWriter().info("For Principal " + principal.getName() + " Found Granted CacheOp=" + code);
            break;
          }
        }
      }

      if (flag) {        
        return true;
      }
    } else {
      if (resourceOpCodeMap.containsKey(principal.getName())) {
        Set<ResourceOperationCode> codes = resourceOpCodeMap.get(principal.getName());
        ResourceOperationContext ctx = (ResourceOperationContext) context;
        flag = false;
        for (ResourceOperationCode code : codes) {
          Log.getLogWriter().info("Checking ResourceOpCode=" + code);
          flag = code.allowedOp(ctx.getResourceOperationCode());
          if (flag) {
            Log.getLogWriter().info("For Principal " + principal.getName() + " Found Granted ResourceOp=" + code);
            return true;
          }
        }
        Log.getLogWriter().info("For Principal " + principal.getName() + " authorizeOperation failed");
        return false;
      } else {
        Log.getLogWriter().info("For Principal " + principal.getName() + " authorizeOperation failed");
        return false;
      }
    }
    Log.getLogWriter().info("For Principal " + principal.getName() + " authorizeOperation failed");
    return false;
  }

  

}
