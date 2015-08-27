/*=========================================================================
 * Copyright (c) 2002-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * more patents listed at http://www.pivotal.io/patents.
 *=========================================================================
 */

package com.gemstone.gemfire.rest.internal.web.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.gemstone.gemfire.cache.LowMemoryException;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.Execution;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.cache.operations.OperationContext.OperationCode;
import com.gemstone.gemfire.internal.cache.GemFireCacheImpl;
import com.gemstone.gemfire.internal.logging.LogService;
import com.gemstone.gemfire.internal.security.AuthorizeRequest;
import com.gemstone.gemfire.internal.util.ArrayUtils;
import com.gemstone.gemfire.rest.internal.web.controllers.support.RestServersResultCollector;
import com.gemstone.gemfire.rest.internal.web.exception.GemfireRestException;
import com.gemstone.gemfire.rest.internal.web.security.AuthorizationProvider;
import com.gemstone.gemfire.rest.internal.web.security.RestRequestFilter;
import com.gemstone.gemfire.rest.internal.web.util.JSONUtils;
import com.gemstone.gemfire.security.NotAuthorizedException;
import org.json.JSONException;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

/**
 * The CommonCrudController serves REST Requests related to listing regions, 
 * listing keys in region, delete keys or delete all data in region.
 * <p/>
 * @author Nilkanth Patel, john blum
 * @since 8.0
 */

@SuppressWarnings("unused")
public abstract class CommonCrudController extends AbstractBaseController {
  
  private static final Logger logger = LogService.getLogger();
  
  /**
   * list all available resources (Regions) in the GemFire cluster
   * @return JSON document containing result
   */
  @RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE })
  @ApiOperation(
    value = "list all resources (Regions)",
    notes = "List all available resources (Regions) in the GemFire cluster",
    response  = void.class
  )
  @ApiResponses( {
    @ApiResponse( code = 200, message = "OK." ),
    @ApiResponse( code = 500, message = "GemFire throws an error or exception." )   
  } )
  public ResponseEntity<?> regions() {
    
    if(logger.isDebugEnabled()){
      logger.debug("Listing all resources (Regions) in GemFire...");
    }
    
    final HttpHeaders headers = new HttpHeaders();  
    headers.setLocation(toUri());
    
    //Do request(Pre) authorization if security is enabled.
    if(AuthorizationProvider.isSecurityEnabled()){
      setAuthTokenHeader(headers);
      AuthorizationProvider.init();
      try{
        AuthorizationProvider.listRegionsAuthorize(OperationCode.LIST, true, "LIST_REGIONS");
      }catch(NotAuthorizedException nae){
        return new ResponseEntity<String>(headers, HttpStatus.UNAUTHORIZED);
      }
    }
   
    final Set<Region<?, ?>> regions = getCache().rootRegions();
    String listRegionsAsJson =  JSONUtils.formulateJsonForListRegions(regions, "regions");
    
    return new ResponseEntity<String>(listRegionsAsJson, headers, HttpStatus.OK);
  }
  
  /**
   * List all keys for the given region in the GemFire cluster
   * @param region gemfire region
   * @return JSON document containing result
   */
  @RequestMapping(method = RequestMethod.GET, value = "/{region}/keys", 
                  produces = { MediaType.APPLICATION_JSON_VALUE } )
  @ApiOperation(
    value = "list all keys",
    notes = "List all keys in region",
    response  = void.class
  )
  @ApiResponses( {
    @ApiResponse( code = 200, message = "OK" ),
    @ApiResponse( code = 404, message = "Region does not exist" ),
    @ApiResponse( code = 500, message = "GemFire throws an error or exception" )   
  } )
  public ResponseEntity<?> keys(@PathVariable("region") String region){ 
    
    if(logger.isDebugEnabled()){
      logger.debug("Listing all Keys in Region ({})...", region);
    }
    
    region = decode(region);
    final HttpHeaders headers = new HttpHeaders();  
    headers.setLocation(toUri(region, "keys"));
    
    //Request(Pre) authorization if security is enabled.
    if(AuthorizationProvider.isSecurityEnabled()){
      setAuthTokenHeader(headers);
      AuthorizationProvider.init();
      try{
        AuthorizationProvider.keySetAuthorize(region);
      }catch(NotAuthorizedException nae) {
        return new ResponseEntity<String>(headers, HttpStatus.UNAUTHORIZED);
      }
    }
    
    Set<Object> keys = getRegion(region).keySet();
    
    //Post authorization
    if(AuthorizationProvider.isSecurityEnabled()){
      try{
        AuthorizationProvider.keySetAuthorizePP(region, keys);
      }catch(NotAuthorizedException nae) {
        return new ResponseEntity<String>(headers, HttpStatus.UNAUTHORIZED);
      }
    }
    String listKeysAsJson =  JSONUtils.formulateJsonForListKeys(keys.toArray(), "keys");
    return new ResponseEntity<String>(listKeysAsJson, headers, HttpStatus.OK);
  }
  
  /**
   * Delete data for single key or specific keys in region
   * @param region gemfire region
   * @param keys for which data is requested
   * @return JSON document containing result
   */
  @RequestMapping(method = RequestMethod.DELETE, value = "/{region}/{keys}",
                  produces = { MediaType.APPLICATION_JSON_VALUE } )
  @ApiOperation(
    value = "delete data for key(s)",
    notes = "Delete data for single key or specific keys in region",
    response  = void.class
  )
  @ApiResponses( {
    @ApiResponse( code = 200, message = "OK" ),
    @ApiResponse( code = 404, message = "Region or key(s) does not exist" ),
    @ApiResponse( code = 500, message = "GemFire throws an error or exception" )      
  } )
  public ResponseEntity<?> delete(@PathVariable("region") String region,
                                  @PathVariable("keys") final String[] keys){     
    if(logger.isDebugEnabled()){
      logger.debug("Delete data for key {} on region {}", ArrayUtils.toString((Object[])keys), region);
    }
    
    region = decode(region);
    final HttpHeaders headers = new HttpHeaders(); 
    
    //Do request(Pre) authorization if security is enabled.
    if(AuthorizationProvider.isSecurityEnabled()){
      setAuthTokenHeader(headers);
      AuthorizationProvider.init();
      try{
        AuthorizationProvider.deleteAuthorize(region, keys, null);
      }catch(NotAuthorizedException nae) {
        return new ResponseEntity<String>(headers, HttpStatus.UNAUTHORIZED);
      }
    }
    
    deleteValues(region, (Object[])keys);
    return new ResponseEntity<Object>(headers, HttpStatus.OK);
  }

  /**
   * Delete all data in region
   * @param region gemfire region
   * @return JSON document containing result
   */
  @RequestMapping(method = RequestMethod.DELETE, value = "/{region}")
  @ApiOperation(
    value = "delete all data",
    notes = "Delete all data in the region",
    response  = void.class
  )
  @ApiResponses( {
    @ApiResponse( code = 200, message = "OK" ),
    @ApiResponse( code = 404, message = "Region does not exist" ),
    @ApiResponse( code = 500, message = "if GemFire throws an error or exception" )   
  } )
  public ResponseEntity<?> delete(@PathVariable("region") String region) {
    
    if(logger.isDebugEnabled()){
      logger.debug("Deleting all data in Region ({})...", region);
    }
    
    region = decode(region);
    final HttpHeaders headers = new HttpHeaders(); 
    
    //Do request(Pre) authorization if security is enabled.
    if(AuthorizationProvider.isSecurityEnabled()){
      setAuthTokenHeader(headers);
      AuthorizationProvider.init();
      try{
        AuthorizationProvider.deleteAllAuthorize(region, null);
      }catch(NotAuthorizedException nae) {
        return new ResponseEntity<String>(headers, HttpStatus.UNAUTHORIZED);
      }
    }
    
    deleteValues(region);
    return new ResponseEntity<Object>(headers, HttpStatus.OK);
  }
  
  @RequestMapping(method = { RequestMethod.GET, RequestMethod.HEAD }, value = "/ping")
  @ApiOperation(
    value = "Check Rest service status ",
    notes = "Check whether gemfire REST service is up and running!",
    response  = void.class
  )
  @ApiResponses( {
    @ApiResponse( code = 200, message = "OK" ),
    @ApiResponse( code = 500, message = "if GemFire throws an error or exception" )   
  } )
  public ResponseEntity<?> ping() {
    // Request Authorization not required.
    return new ResponseEntity<Object>(HttpStatus.OK);
  }
  
  @RequestMapping(method = { RequestMethod.GET }, value = "/servers")
  @ApiOperation(
    value = "fetch all REST enabled servers in the DS",
    notes = "Find all gemfire node where developer REST service is up and running!",
    response  = void.class
  )
  @ApiResponses( {
    @ApiResponse( code = 200, message = "OK" ),
    @ApiResponse( code = 500, message = "if GemFire throws an error or exception" )   
  } )
  public ResponseEntity<?> servers() {
    //Request Authorization not required.
    Execution function = null;
      
    if(logger.isDebugEnabled()){
      logger.debug("Executing function to get REST enabled gemfire nodes in the DS!");
    }
    
    try {
      function = FunctionService.onMembers(getAllMembersInDS());
    } catch(FunctionException fe) {
      throw new GemfireRestException("Disributed system does not contain any valid data node that can host REST service!", fe);
    }
        
    try {
      final ResultCollector<?, ?> results = function.withCollector(new RestServersResultCollector()).execute(GemFireCacheImpl.FIND_REST_ENABLED_SERVERS_FUNCTION_ID);
      Object functionResult = results.getResult();
      
      if(functionResult instanceof List<?>) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(toUri("servers"));
        try {    
          String functionResultAsJson = JSONUtils.convertCollectionToJson((ArrayList<Object>)functionResult);
          return new ResponseEntity<String>(functionResultAsJson, headers, HttpStatus.OK);  
        } catch (JSONException e) {
          throw new GemfireRestException("Could not convert function results into Restful (JSON) format!", e);
        }
      }else {
        throw new GemfireRestException("Function has returned results that could not be converted into Restful (JSON) format!");
      }
      
    } catch(ClassCastException cce){
      throw new GemfireRestException("Key is of an inappropriate type for this region!", cce);
    } catch(NullPointerException npe){
      throw new GemfireRestException("Specified key is null and this region does not permit null keys!", npe);
    } catch(LowMemoryException lme){
      throw new GemfireRestException("Server has encountered low memory condition!", lme);
    } catch (IllegalArgumentException ie) {
      throw new GemfireRestException("Input parameter is null! ", ie);
    }catch (FunctionException fe){
      throw new GemfireRestException("Server has encountered error while executing the function!", fe);
    }
    
  }
  
}
