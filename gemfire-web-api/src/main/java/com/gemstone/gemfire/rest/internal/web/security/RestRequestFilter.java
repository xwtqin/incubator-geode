package com.gemstone.gemfire.rest.internal.web.security;

import java.io.IOException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;

import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.internal.ClassLoadUtil;
import com.gemstone.gemfire.internal.i18n.LocalizedStrings;
import com.gemstone.gemfire.internal.security.AuthorizeRequest;
import com.gemstone.gemfire.management.internal.RestAgent;
import com.gemstone.gemfire.security.AuthenticationFailedException;
import com.gemstone.gemfire.security.Authenticator;
import com.gemstone.gemfire.security.TokenService;

public class RestRequestFilter  implements Filter {
 
  public static final String AUTH_TOKEN_HEADER = "security-gfrest-authtoken";
  public static final String SECURITY_PROPS_PREFIX = "security-"; 
  
  public static final String AUTH_TOKEN = "authToken";
  public static final String AUTH_PRINCIPAL = "principal";
  public static final String IS_REST_APIS_SECURITY_ENABLED = "isSecurityEnabled";
  
  private static final ThreadLocal<Map<String, Object>> ENV = new ThreadLocal<Map<String, Object>>() {
    @Override
    protected Map<String, Object> initialValue() {
      return Collections.emptyMap();
    }
  };

  public static Map<String, Object>  getEnvironment() {
    return ENV.get();
  }

  private Properties getHeadersInfo(HttpServletRequest request) {
    
    Properties props = new Properties();

    Enumeration headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String key = (String) headerNames.nextElement();
      if(key.startsWith(SECURITY_PROPS_PREFIX)){
        props.setProperty(key, request.getHeader(key));
      }
    }

    return props;
  }
  
  public void init(FilterConfig fConfig) throws ServletException {}
 
  private Principal verifyCredentials(Properties props, InternalDistributedSystem ids){
              
    //String authCreateName = "templates.security.DummyAuthenticator.create";
    String methodName = ids.getProperties()
                           .getProperty(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME);
      
    Authenticator auth = null;
    try {
      if (methodName == null || methodName.length() == 0) {
        return null;
      }
      Method instanceGetter = ClassLoadUtil.methodFromName(methodName);
      auth = (Authenticator)instanceGetter.invoke(null, (Object[])null);
      
    }catch (Exception ex) {
      throw new AuthenticationFailedException(
            LocalizedStrings.HandShake_FAILED_TO_ACQUIRE_AUTHENTICATOR_OBJECT.toLocalizedString(), ex);
    }
      
    if (auth == null) {
      throw new AuthenticationFailedException(
          LocalizedStrings.HandShake_AUTHENTICATOR_INSTANCE_COULD_NOT_BE_OBTAINED.toLocalizedString()); 
    }
    
    auth.init(props, ids.getLogWriter(), ids.getSecurityLogWriter());
    Principal principal;
    try {
      principal = auth.authenticate(props, ids.getDistributedMember());
    }catch(AuthenticationFailedException ex){
      throw ex;
    }catch (Exception e){
      throw new AuthenticationFailedException("Authentication Failed", e);
    }
    finally {
      auth.close();
    }
       
    return principal;      
  }
     
  private String generateToken(Principal principal, InternalDistributedSystem ids){
    
    String tokenServiceName = ids.getProperties()
                           .getProperty(DistributionConfig.SECURITY_REST_TOKEN_SERVICE_NAME);
      
    TokenService tokenService = null;
    try {
      if (tokenServiceName == null || tokenServiceName.length() == 0) {
        return null;
      }
      Method instanceGetter = ClassLoadUtil.methodFromName(tokenServiceName);
      tokenService = (TokenService)instanceGetter.invoke(null, (Object[])null);
      
      return tokenService.generateToken(principal);
    }catch (Exception ex) {
      throw new AuthenticationFailedException(
            "Failed to acquire TokenService object", ex);
    }
  }
  
  private String validateToken(String authToken, Principal principal, InternalDistributedSystem ids){
    
    //String authCreateName = "templates.security.DummyAuthenticator.create";
    String tokenServiceName = ids.getProperties()
                           .getProperty(DistributionConfig.SECURITY_REST_TOKEN_SERVICE_NAME);
      
    TokenService tokenService = null;
    try {
      if (tokenServiceName == null || tokenServiceName.length() == 0) {
        return null;
      }
      
      Method instanceGetter = ClassLoadUtil.methodFromName(tokenServiceName);
      tokenService = (TokenService)instanceGetter.invoke(null, (Object[])null);
      
      return tokenService.validateToken(authToken, principal);
    }catch (Exception ex) {
      //Remove the invalid token from the state
      RestAgent.closeAuthz(authToken);
      RestAgent.removeAuthzEntry(authToken);
      throw new AuthenticationFailedException(
            "Invalid authentication token found", ex);
    }
  }
  

  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    
    Map<String, Object>  map = new HashMap<String, Object>();
    boolean isSecurityEnabled = false;
    
    InternalDistributedSystem ids = InternalDistributedSystem.getConnectedInstance();
    
    String authMethodName = ids.getProperties()
    .getProperty(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME);
    
    //TODO: find the props on which we can conclude that security is enabled/configured or not!
    if (!StringUtils.isEmpty(authMethodName)) {
      //DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      HttpServletResponse httpResponse = (HttpServletResponse) response;
      
      String authToken = httpRequest.getHeader(AUTH_TOKEN_HEADER);
      Principal principal= null;
          
      if(StringUtils.isEmpty(authToken)){ 
        //authToken is not present in the REST request.
        //fetch security headers starting with "security-"
        Properties props = getHeadersInfo(httpRequest);
        if(props.size() > 0){
          //Attempt authentication
          principal = verifyCredentials(props, ids);
        
          authToken =  generateToken(principal, ids); 
          
        }else {
          //If creds or token not present in request header, 401 Authentication required response
          throw new AuthenticationFailedException("Authentication required.!");
        }
      }else { 
        //Case: Token present in the request header
        final Region<String, List<Object>> tokenToAuthzRequestRegion = RestAgent.getAuthzRegion(RestAgent.AUTH_METADATA_REGION);
        //TODO: add getter to fetch principal.
        principal = RestAgent.getPrincipalForToken(authToken);
        String refreshedToken = validateToken(authToken, principal, ids);
        
        //Check whether TokenService has refreshed the token, If so, Update the AuthZ map
        if(!authToken.equals(refreshedToken)){
          List<Object> authObjects= tokenToAuthzRequestRegion.get(authToken);
          RestAgent.removeAuthzEntry(authToken);
          RestAgent.addAuthzEntry(refreshedToken, authObjects);
          authToken = refreshedToken;
        }
      }
      
      //Add entries in ThreadLocal  
      map.put(AUTH_TOKEN, authToken);
      map.put(AUTH_PRINCIPAL, principal);
      isSecurityEnabled = true;
    }
      
    map.put("isSecurityEnabled", isSecurityEnabled);
    ENV.set(map);
    
    chain.doFilter(request, response);
  }
  
  protected void setAuthTokenHeader(final ServletResponse response) {
    
    HttpServletResponse httpResponse = (HttpServletResponse) response;
    Map<String, Object> envMap = (Map<String, Object>)RestRequestFilter.getEnvironment();
    boolean isSecurityEnabled = (boolean) envMap.get(IS_REST_APIS_SECURITY_ENABLED);
    String authToken = (String)envMap.get(AUTH_TOKEN);
    
    if(isSecurityEnabled == false)
      return;
  
    httpResponse.addHeader(AUTH_TOKEN_HEADER, authToken);
  }
  
  public void destroy() {}
    
  public boolean isValid(String authToken){
    if(!StringUtils.hasText(authToken))
    {
      return false;
    }
    return true;
  }
}
