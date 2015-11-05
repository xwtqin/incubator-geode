package com.gemstone.gemfire.management.internal.security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.operations.OperationContext;
import com.gemstone.gemfire.cache.operations.OperationContext.OperationCode;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.management.internal.security.ResourceOperationContext.ResourceOperationCode;
import com.gemstone.gemfire.security.AccessControl;
import com.gemstone.gemfire.security.NotAuthorizedException;

public class CustomAccessControl implements AccessControl{
  
  private Principal principal;
  
  private boolean close = false;
  
  private List<OperationCode> baseOpCodes = new ArrayList<OperationCode>();

  public static AccessControl create() {
    return new CustomAccessControl();
  }
  
  @Override
  public void init(Principal principal, DistributedMember remoteMember, Cache cache) throws NotAuthorizedException {
    this.principal = principal;
    baseOpCodes.add(OperationCode.GET);
    baseOpCodes.add(OperationCode.QUERY);
    baseOpCodes.add(OperationCode.PUT);
    baseOpCodes.add(OperationCode.REMOVEALL);
    baseOpCodes.add(OperationCode.DESTROY);
    baseOpCodes.add(OperationCode.CLOSE_CQ);
    baseOpCodes.add(OperationCode.REGION_CREATE);
    baseOpCodes.add(OperationCode.REGION_DESTROY);
    baseOpCodes.add(OperationCode.EXECUTE_FUNCTION);
    
  }

  @Override
  public boolean authorizeOperation(String regionName, OperationContext context) {
    System.out.println("OperationContext = "+ context);
    //This access control only validates OperationCode.RESOURCE
    if (context.getOperationCode().equals(OperationCode.RESOURCE) || baseOpCodes.contains(context.getOperationCode())) {
      
      String principalUser = principal.getName();
      
      ResourceOperationContext rContext = (ResourceOperationContext) context;
      ResourceOperationCode rCode = rContext.getResourceOperationCode();
      
      System.out.println("ResourceOperationContext = "+ rContext + " , rCode ordinal = "
          +rCode + ", principalUser = "+principalUser);      
      
      if(rCode.equals(ResourceOperationCode.LIST_DS))
        return true;
      
      if (principalUser.equals("ADMIN_" + rCode)) {
        if (!ResourceOperationCode.ADMIN.allowedOp(rCode)) {
          return false;
        }
      }    
      if (principalUser.equals("AUTHC_"+rCode.toOrdinal())) {
        return true;
      } else {
        return false;
      }

    

    }
    return false;
  }

  @Override
  public void close() {
   this.close = true;
  }

  public boolean isClose(){
    return this.close;
  }

}
