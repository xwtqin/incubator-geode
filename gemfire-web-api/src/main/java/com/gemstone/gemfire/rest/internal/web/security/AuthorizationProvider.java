package com.gemstone.gemfire.rest.internal.web.security;

import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.operations.ExecuteFunctionOperationContext;
import com.gemstone.gemfire.cache.operations.OperationContext;
import com.gemstone.gemfire.cache.operations.OperationContext.OperationCode;
import com.gemstone.gemfire.cache.operations.QueryOperationContext;
import com.gemstone.gemfire.cache.operations.RestAPIsOperationContext;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.internal.cache.GemFireCacheImpl;
import com.gemstone.gemfire.internal.cache.tier.sockets.ClientProxyMembershipID;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;
import com.gemstone.gemfire.internal.security.AuthorizeRequest;
import com.gemstone.gemfire.internal.security.AuthorizeRequestPP;
import com.gemstone.gemfire.management.internal.RestAgent;
import com.gemstone.gemfire.rest.internal.web.exception.GemfireRestException;
import com.gemstone.gemfire.security.AccessControl;
import com.gemstone.gemfire.security.NotAuthorizedException;

public class AuthorizationProvider {
  
  //private static ConcurrentHashMap<String, AuthorizeRequest> tokenToAuthzRequestMap = new ConcurrentHashMap<String, AuthorizeRequest>();
  //protected static final String AUTH_METADATA_REGION = "__TokenToAuthzRequest__";
  
  public static void init(){
    
    if(isSecurityEnabled() == false)
      return;
    
    Map<String, Object> envMap = (Map<String, Object>)RestRequestFilter.getEnvironment();
    String authToken = getAuthToken();
    Principal principal = (Principal)envMap.get("principal");
    
    final Region<String, List<Object>> tokenToAuthzRequestRegion = RestAgent.getAuthzRegion(RestAgent.AUTH_METADATA_REGION);
    
    if(!tokenToAuthzRequestRegion.containsKey(authToken)){
      //Step-2 initialize access-control for the principal. 
      //Create the AuthorizeRequest instance and store it.(key: token, value: AuthorizeRequest)
      //String acMethodCreateName = "templates.security.DummyAuthorization.create";
      InternalDistributedSystem ids = InternalDistributedSystem.getConnectedInstance();
      
      String acMethodCreateName = ids.getProperties()
                             .getProperty(DistributionConfig.SECURITY_CLIENT_ACCESSOR_NAME);
      
      String postAuthzFactoryName = ids.getProperties()
                             .getProperty(DistributionConfig.SECURITY_CLIENT_ACCESSOR_PP_NAME);
      
      AccessControl authz = null;
      //TODO: Discuss about, what should be the distributed member for the REST client
      DistributedMember dm = null;
      AuthorizeRequest authzRequest = null;
      AuthorizeRequestPP postAuthzRequest = null;
      List<Object> authzObjects = new ArrayList<Object>();
      
      //Pre authorization initialization.
      if (acMethodCreateName != null && acMethodCreateName.length() > 0) {
        try {
          // AccessControl is instantiated and initialized here.
          authzRequest = new AuthorizeRequest(acMethodCreateName, dm,
               principal, GemFireCacheImpl.getInstance());
          
        }catch (NotAuthorizedException nae) {
          throw new NotAuthorizedException("Not Authorized to perform operation!");
        }catch (ClassNotFoundException cnf) {
          throw new GemfireRestException("Server has encountered the problem while initializing the Authorization callbacks!");
        }catch (NoSuchMethodException nsm) {
          throw new GemfireRestException("Server has encountered the problem while initializing the Authorization callbacks!");
        }catch (IllegalAccessException iae) {
          throw new GemfireRestException("Server has encountered the problem while initializing the Authorization callbacks!");
        }catch (InvocationTargetException ite) {
          throw new GemfireRestException("Server has encountered the problem while initializing the Authorization callbacks!");
        }
      }
      authzObjects.add(0, authzRequest);
      
      //Post authorization initialization.
      if (postAuthzFactoryName != null && postAuthzFactoryName.length() > 0) {
        try{ 
          postAuthzRequest = new AuthorizeRequestPP(
                    postAuthzFactoryName, ClientProxyMembershipID.getNewProxyMembership(GemFireCacheImpl.getInstance().getDistributedSystem()), principal, GemFireCacheImpl.getInstance());
          
          //TODO: Discuss on ClientProxyMembershipID() for REST CLIENTs
        }catch (NotAuthorizedException nae) {
          throw new NotAuthorizedException("Not Authorized to perform operation!");
        }catch (ClassNotFoundException cnf) {
          throw new GemfireRestException("Server has encountered the problem while initializing the Authorization callbacks!");
        }catch (NoSuchMethodException nsm) {
          throw new GemfireRestException("Server has encountered the problem while initializing the Authorization callbacks!");
        }catch (IllegalAccessException iae) {
          throw new GemfireRestException("Server has encountered the problem while initializing the Authorization callbacks!");
        }catch (InvocationTargetException ite) {
          throw new GemfireRestException("Server has encountered the problem while initializing the Authorization callbacks!");
        }
      }
      authzObjects.add(1, postAuthzRequest);
      
      tokenToAuthzRequestRegion.put(authToken, authzObjects);
    }
  }
  
  public static void listRegionsAuthorize(OperationCode opCode, boolean isRestOperation, String opType)throws NotAuthorizedException {
    RestAPIsOperationContext restContext = new RestAPIsOperationContext(opCode, isRestOperation);
    authorizeRestOperation(null/*regionName*/, restContext, opType);
  }
  
  public static void keySetAuthorize(String region){ 
    AuthorizeRequest ar = RestAgent.getAuthorizeRequest(getAuthToken());
    if(ar != null){
      ar.keySetAuthorize(region);
    }
  }
  
  public static void deleteAuthorize(String region, final String[] keys, Object callbackArg){
    AuthorizeRequest ar = RestAgent.getAuthorizeRequest(getAuthToken());
    if(ar != null) {
      for(final Object key : keys){
        ar.destroyAuthorize(region, key, null);
      }
    }
  }
  
  public static void deleteAllAuthorize(String region, Object callbackArg){
    AuthorizeRequest ar = RestAgent.getAuthorizeRequest(getAuthToken());
    if(ar != null){
      ar.clearAuthorize(region, callbackArg);
    }
  }
  
  public static void putAuthorize(String regionName, String key,
    String json, boolean isObject, Object callbackArg, byte opType){
    //TODO: add isJson, similar to isObject
    AuthorizeRequest ar = RestAgent.getAuthorizeRequest(getAuthToken());
    if(ar != null){
      ar.putAuthorize(regionName, key, json, isObject, /*isJson*/ callbackArg, opType);
    }
  }
  
  public static void putAuthorizePP(String regionName, String key,
    String json, boolean isObject, Object callbackArg, byte opType){
    //TODO: add isJson, similar to isObject
    AuthorizeRequest ar = RestAgent.getAuthorizeRequest(getAuthToken());
    if(ar != null){
      ar.putAuthorize(regionName, key, json, isObject, /*isJson*/ callbackArg, opType);
    }
  }
  
  public static void getAllAuthorize(String regionName, Set allKeys, Object callbackArg ){
    AuthorizeRequest ar = RestAgent.getAuthorizeRequest(getAuthToken());
    if(ar != null) {
      for(final Object key : allKeys){
        ar.getAuthorize(regionName, key, callbackArg);
      }
    }  
  }
  
  public static void getAuthorize(String regionName, String[] keys, Object callbackArg ){
    AuthorizeRequest ar = RestAgent.getAuthorizeRequest(getAuthToken());
    if(ar != null) {
      for(final Object key : keys){
        ar.getAuthorize(regionName, key, callbackArg);
      }
    }
  }

  public static void putAllAuthorize(String regionName, String json, Object callbackArg){
    AuthorizeRequest ar = RestAgent.getAuthorizeRequest(getAuthToken());
    if(ar != null) {
      //TODO: add support for passing json array. isJson=true
      //ar.putAllAuthorize(regionName, json, callbackArg);
    }
  }
  
  public static QueryOperationContext queryAuthorize(String queryString, Set regionNames, Object[] queryParams){
    AuthorizeRequest ar = RestAgent.getAuthorizeRequest(getAuthToken());
    if(ar != null) {
      return ar.queryAuthorize(queryString, regionNames, queryParams);
    }
    return null;
  }
  
  public static QueryOperationContext queryAuthorizePP(String queryString, Set regionNames, Object queryResult, QueryOperationContext queryContext, Object[] queryParams){
    AuthorizeRequestPP arPP = RestAgent.getAuthorizeRequestPP(getAuthToken());
    if(arPP != null) {
      return arPP.queryAuthorize(queryString, regionNames, queryResult, queryContext, queryParams);
    }
    return null;
  }
  
  public static void listQueriesAuthorize(OperationCode opCode, boolean isRestOperation, String opType) 
      throws NotAuthorizedException {
    RestAPIsOperationContext restContext = new RestAPIsOperationContext(opCode, isRestOperation);
    authorizeRestOperation(null/*regionName*/, restContext, opType);
  }
  
  public static void createQueryAuthorize(OperationCode opCode, boolean isRestOperation, String opType, String queryId, String oqlStatement) 
      throws NotAuthorizedException {
    RestAPIsOperationContext restContext = new RestAPIsOperationContext(opCode, isRestOperation, queryId, oqlStatement);
    authorizeRestOperation(null/*regionName*/, restContext, opType);
  }
  
  public static void updateQueryAuthorize(OperationCode opCode, boolean isRestOperation, String opType, String queryId, String oqlStatement) 
      throws NotAuthorizedException {
    RestAPIsOperationContext restContext = new RestAPIsOperationContext(opCode, isRestOperation, queryId, oqlStatement);
    authorizeRestOperation(null/*regionName*/, restContext, opType);
  }
  
  public static void deleteQueryAuthorize(OperationCode opCode, boolean isRestOperation, String opType, String queryId) 
      throws NotAuthorizedException {
    RestAPIsOperationContext restContext = new RestAPIsOperationContext(opCode, isRestOperation, queryId, null /*oqlStatement*/);
    authorizeRestOperation(null/*regionName*/, restContext, opType);
  }
 
  public static /*RestAPIsOperationContext*/void listFunctionsAuthorize(OperationCode opCode, boolean isRestOperation, String opType) 
      throws NotAuthorizedException {
    RestAPIsOperationContext restContext = new RestAPIsOperationContext(opCode, isRestOperation);
    authorizeRestOperation(null/*regionName*/, restContext, opType);
  }
  
  public static ExecuteFunctionOperationContext executeFunctionAuthorize(String functionName, String region,
                                              Set keySet, Object arguments, boolean optimizeForWrite){
    AuthorizeRequest ar = RestAgent.getAuthorizeRequest(getAuthToken());
    if(ar != null) {
      return ar.executeFunctionAuthorize(functionName, region, keySet, arguments, optimizeForWrite);
    }
    return null;
  }
  
  public static void executeFunctionAuthorizePP(Object oneResult, ExecuteFunctionOperationContext executeContext){
    AuthorizeRequestPP arPP = RestAgent.getAuthorizeRequestPP(getAuthToken());
    if(arPP != null) {
      arPP.executeFunctionAuthorize(oneResult, executeContext);
    }
  }
  
  private static String getAuthToken(){
    Map<String, Object> envMap = (Map<String, Object>)RestRequestFilter.getEnvironment();
    return (String)envMap.get("authToken");
  }
  
  public static boolean isSecurityEnabled(){
    Map<String, Object> envMap = (Map<String, Object>)RestRequestFilter.getEnvironment();
    boolean isSecurityEnabled = (boolean) envMap.get("isSecurityEnabled");
    
    if(isSecurityEnabled == true) {
      return true;
    }
     
    return false;
  }
  
  private static void authorizeRestOperation( String regionName, OperationContext restContext, String opType){
    AuthorizeRequest ar = RestAgent.getAuthorizeRequest(getAuthToken());
    if(ar != null) {
      if (!ar.getAuthzCallback().authorizeOperation(null, restContext)) {
        String errStr = "Not authorized to perfom" +  opType + "operation on the cache";
        ar.getLogger().warning( LocalizedStrings.TWO_ARG_COLON, new Object[] {ar, errStr});
        if (ar.isPrincipalSerializable()) {
          throw new NotAuthorizedException(errStr, ar.getPrincipal());
        }
        else {
          throw new NotAuthorizedException(errStr);
        }
      }
      else {
        if (ar.getLogger().finestEnabled()) {
          ar.getLogger().finest(ar.toString()
              + ": Authorized to perform" + opType + "operation on cache");
        }
      } 
    }
  }
  
  public static void getAuthorizePP(String regionName, Object key, Object result ){
    AuthorizeRequestPP arPP = RestAgent.getAuthorizeRequestPP(getAuthToken());
    if(arPP != null) {
      arPP.getAuthorize(regionName, key, result, true, null);
    }
  }

  public static void keySetAuthorizePP(String regionName, Set keySet) {
    AuthorizeRequestPP arPP = RestAgent.getAuthorizeRequestPP(getAuthToken());
    if(arPP != null) {
      arPP.keySetAuthorize(regionName, keySet, null);
    }
  }
}
