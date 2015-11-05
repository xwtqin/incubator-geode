package com.gemstone.gemfire.rest.internal.web.controllers;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.gemstone.gemfire.security.AuthenticationFailedException;
import com.gemstone.gemfire.security.AuthenticationRequiredException;
import com.gemstone.gemfire.security.TokenService;

public class DummyTokenService implements TokenService {
      
  public static Map<String, Integer> requestTracker = new HashMap<String, Integer>();
  public static int counter = 0;
  
  private static List<String> issuedAuthTokens = new ArrayList<String>();
  
  public static TokenService create() {
    return new DummyTokenService();
  }

  public DummyTokenService() {
  }
  private synchronized String generateUUIDBasedToken(){
    return  UUID.randomUUID().toString();
  }
  
  @Override
  public synchronized String generateToken(Principal principal) {
    requestTracker.put(principal.getName(), 1);
    System.out.println("Nilkanth generateToken counter1 = "+ counter);
    String token = generateUUIDBasedToken();
    issuedAuthTokens.add(token);
    return token;
  }

  @Override
  public synchronized String validateToken(String token, Principal principal)
      throws AuthenticationRequiredException, AuthenticationFailedException {
 
    int requestCount = requestTracker.get(principal.getName()).intValue() + 1;
    
    try {
      if(issuedAuthTokens.contains(token)){
        
        //Refresh token
        System.out.println("Nilkanth requestTracker.get(principal.getName() = " + requestTracker.get(principal.getName()));
        if(requestTracker != null && principal != null && 
          requestTracker.get(principal.getName()) % 5 == 0){
          //increment the request count
          requestTracker.put(principal.getName(), requestCount); 
          System.out.println("NIlkanth: validateToken RefreshedToken requestTracker.count = "+ requestTracker.get(principal.getName()));
          
          String refreshedToken = generateUUIDBasedToken();
          issuedAuthTokens.add(refreshedToken);
          return refreshedToken;
        }
      }
    }catch(AuthenticationRequiredException ar) {
      throw new AuthenticationRequiredException("Nilkanth: Authentication required!");
    }catch(AuthenticationFailedException af) {
      throw new AuthenticationFailedException("Nilkanth: Authentication failed! Invalid authTOken found!");
    }
    
    
    requestTracker.put(principal.getName(), requestCount);
    System.out.println("NIlkanth: validateToken Sametoken requestTracker.count = "+ requestTracker.get(principal.getName()));
    
    return token;
  }
  
}
