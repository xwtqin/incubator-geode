package com.gemstone.gemfire.rest.internal.web.controllers;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import com.gemstone.gemfire.cache.AttributesFactory;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.DataPolicy;
import com.gemstone.gemfire.cache.RegionAttributes;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.RegionShortcut;
import com.gemstone.gemfire.cache.execute.FunctionService;
import com.gemstone.gemfire.distributed.ServerLauncher;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.internal.AvailablePortHelper;
import com.gemstone.gemfire.internal.SocketCreator;
import com.gemstone.gemfire.management.internal.ManagementConstants;
import com.gemstone.gemfire.test.junit.categories.UnitTest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.experimental.categories.Category;
@Category(UnitTest.class)
public class RestAPIsSecurityJUnitTest extends TestCase implements UserType {

  private Cache c;

  private String hostName;
  
  private String baseURL;
  
  private int restServicePort;
  
  final int LIST_ALL_NAMED_QUERIES_INDEX = 45;
  final List<Integer> VALID_400_URL_INDEXS = Arrays.asList(2, 9, 13, 17,22);
  final List<Integer> VALID_404_URL_INDEXS = Arrays.asList(3, 6, 7, 10, 14, 18, 23, 28, 32, 35, 37, 38, 40, 41, 54, 56);
  final List<Integer> VALID_409_URL_INDEXS = Arrays.asList(1, 21, 90, 104, 110);
  final List<Integer> VALID_405_URL_INDEXS = Arrays.asList(26, 33);
  final List<Integer> VALID_401_URL_INDEXS = Arrays.asList(63, 64, 66, 67, 68, 69, 76, 77, 78, 84, 85, 86, 91, 95, 96, 97, 98, 99, 100, 101, 105, 106, 107, 108, 109);
  final List<Integer> Query_URL_INDEXS = Arrays.asList(LIST_ALL_NAMED_QUERIES_INDEX, 46, 47, 48);
  
  @Override
  @SuppressWarnings("deprecation")
  public void setUp() throws Exception {
    this.restServicePort = AvailablePortHelper.getRandomAvailableTCPPort();
    
    try {
      InetAddress addr = SocketCreator.getLocalHost();
      this.hostName = addr.getHostName();
    } catch (UnknownHostException ex) {
      this.hostName = ManagementConstants.DEFAULT_HOST_NAME;
    }
    
    ServerLauncher serverLauncher = new ServerLauncher.Builder()
    .set("mcast-port", "0")
    .setServerBindAddress(this.hostName)
    .setServerPort(AvailablePortHelper.getRandomAvailableTCPPort())
    .set("start-dev-rest-api", "true")
    .set("http-service-port", String.valueOf(this.restServicePort))
    .set("http-service-bind-address", this.hostName)
    .set(DistributionConfig.SECURITY_CLIENT_AUTHENTICATOR_NAME, "com.gemstone.gemfire.rest.internal.web.controllers.CustomRestAPIsAuthenticator.create")
    .set(DistributionConfig.SECURITY_CLIENT_ACCESSOR_NAME, "com.gemstone.gemfire.rest.internal.web.controllers.CustomRestAPIsAuthorization.create")
    .set(DistributionConfig.SECURITY_CLIENT_ACCESSOR_PP_NAME, "com.gemstone.gemfire.rest.internal.web.controllers.CustomRestAPIsAuthorization.create")
    .set(DistributionConfig.SECURITY_REST_TOKEN_SERVICE_NAME, "com.gemstone.gemfire.rest.internal.web.controllers.DummyTokenService.create")
    .setPdxReadSerialized(true)
    .build();
    
    serverLauncher.start();
    
    this.baseURL = "http://" + this.hostName + ":" + this.restServicePort;
    this.c = CacheFactory.getAnyInstance();
    
    /*
    //Debug code
    this.baseURL = "http://" + "localhost" + ":" + "8080";
   
    this.c = (GemFireCacheImpl) new CacheFactory().set("mcast-port", "0")
        .set("rest-service-http-port", String.valueOf(this.restServicePort))
        .set("rest-service-bind-address", this.hostName)
        //.set("log-file", "./restJunitLogs/my.log")
        .setPdxReadSerialized(true).create();
    */
    
    /*
    this.c = (GemFireCacheImpl) new CacheFactory().set("mcast-port", "0")
        .set("rest-service-http-port", "8080")
        .set("rest-service-bind-address", "localhost")
        //.set("log-file", "./restJunitLogs/my.log")
        .setPdxReadSerialized(true).create();
    */
    
    //start cache-server, Gemfire cache clients will connect it
    /*
    BridgeServer server = c.addBridgeServer();
    final int serverPort = 40405;
    server.setPort(serverPort);
    server.start();
    */
    
    final AttributesFactory<String, String> attributesFactory = new AttributesFactory<>();
    attributesFactory.setDataPolicy(DataPolicy.REPLICATE);

    // Create region, customers
    final RegionAttributes<String, String> regionAttributes = attributesFactory
        .create();
    c.createRegion(RestAPIsTestData.CUSTOMER_REGION, regionAttributes);
    
    //Debug code
    //c.createRegion(PEOPLE_REGION, regionAttributes);
    
    // Create region, items
    attributesFactory.setDataPolicy(DataPolicy.PARTITION);
    c.createRegion(RestAPIsTestData.ITEM_REGION, regionAttributes);
     
    // Create region, /orders
    final AttributesFactory<Object, Object> af2 = new AttributesFactory<>();
    af2.setDataPolicy(DataPolicy.PARTITION);
    final RegionAttributes<Object, Object> rAttributes2 = af2.create();
    
    c.createRegion(RestAPIsTestData.ORDER_REGION, rAttributes2);
   
    // Create region, primitiveKVStore
    final AttributesFactory<Object, Object> af1 = new AttributesFactory<>();
    af1.setDataPolicy(DataPolicy.PARTITION);
    final RegionAttributes<Object, Object> rAttributes = af1.create();
    
    c.createRegion(RestAPIsTestData.PRIMITIVE_KV_STORE_REGION, rAttributes);
   
    RegionFactory<String,Object> rf = c.createRegionFactory(RegionShortcut.REPLICATE);
    rf.setDataPolicy(DataPolicy.EMPTY);
    //rf.setCacheLoader(new SimpleCacheLoader());
    rf.setCacheLoader(RestAPIsTestData.getSimpleCacheLoader());
    rf.setCacheWriter(new SampleCacheWriter());
    rf.create(RestAPIsTestData.EMPTY_REGION);
    
    // Register functions here
    FunctionService.registerFunction(new GetAllEntries());
    FunctionService.registerFunction(new GetRegions());
    FunctionService.registerFunction(new PutKeyFunction());
    FunctionService.registerFunction(new GetDeliveredOrders());
    FunctionService.registerFunction(new AddFreeItemToOrders());
  }

  @Override
  public void tearDown() {
    // shutdown and clean up the manager node.
    //this.c.close();
    ServerLauncher.getInstance().stop();
  }
  
  private HttpHeaders setRequestHeaders(int index) {
    List<MediaType> acceptableMediaTypes = new ArrayList<>();

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(acceptableMediaTypes);
    headers.setContentType(MediaType.APPLICATION_JSON);
    
    //find the user type and get the token if available
    String uType = (String)RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.USER_CREDENTIAL];
    System.out.println("--------------------------------------------------------");
    System.out.println("Index: " + index + "  UserType = "+ uType);
    boolean isUserTypeExists = RestAPIsTestData.userToTokenMap.containsKey(uType);
    System.out.println(" isUserTypeExists = "+ isUserTypeExists);
    
    if(isUserTypeExists) {
      String authToken =   RestAPIsTestData.userToTokenMap.get(uType);
      System.out.println("Request has sent with authTOken: " + authToken);
      headers.set("security-gfrest-authtoken", authToken);
    }else {
      System.out.println("Request has sent using creds");
      if(RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.USER_CREDENTIAL] == UserType.ADMIN){ 
        headers.set("security-username", "admin");
        headers.set("security-password", "admin");
      }else if(RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.USER_CREDENTIAL] == UserType.READER) {
        headers.set("security-username", "reader");
        headers.set("security-password", "reader");
   
      }else if(RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.USER_CREDENTIAL] == UserType.WRITER) {
        headers.set("security-username", "writer");
        headers.set("security-password", "writer");
      }else if(RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.USER_CREDENTIAL] == UserType.USER) {
        headers.set("security-username", "user");
        headers.set("security-password", "user");
      }else if(RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.USER_CREDENTIAL] == UserType.UNAUTHENTICATED) {
        headers.set("security-username", "unauthenticated");
        headers.set("security-password", "unauthenticated");
      }else {
        //log
      }
    }
    return headers;
  }

 
  public void testCreateAsJson() { 
    executeQueryTestCases();
  }
    
  private void validateGetAllResult(int index, ResponseEntity<String> result){
    if(index == 27  || index == 29  || index == 30) {
      try {
        new JSONObject(result.getBody()).getJSONArray("customers");
      } catch (JSONException e) {
        fail("Caught JSONException in validateGetAllResult :: " + e.getMessage());
      }
    }
  }
  
  private void verifyRegionSize(int index, ResponseEntity<String> result) {
    
    if(index == 59 ) {
      HttpHeaders headers = result.getHeaders();
      String value = headers.getFirst("Resource-Count");
      assertEquals(Integer.parseInt(value), 55);
    }
  }
  
  private void validateQueryResult(int index, ResponseEntity<String> result){
    
    if(Query_URL_INDEXS.contains(index)) {
      //..RestAPIsTestData.queryResultByIndex = new HashMap<>();
      //initializeQueryTestData();  
      QueryResultData queryResult =  RestAPIsTestData.queryResultByIndex.get(index);   

      //Check whether received response contains expected query IDs.
      if(index == 45 ) { 
        
        try {
          JSONObject jsonObject = new JSONObject(result.getBody());
          JSONArray jsonArray = new JSONArray(jsonObject.get("queries").toString());
          
          for (int i=0; i< jsonArray.length(); i++) {  
            assertTrue("PREPARE_PARAMETERIZED_QUERY: function IDs are not matched", queryResult.getResult().contains(jsonArray.getJSONObject(i).getString("id")));
          }
        } catch (JSONException e) {
          fail("Caught JSONException in validateQueryResult :: " + e.getMessage());
        }
      }
      else if (index == 46 || index == 47 || index == 48) {
        
        JSONArray jsonArray;
        try {
          jsonArray = new JSONArray(result.getBody());
          //verify query result size
          assertEquals(queryResult.getResultSize(), jsonArray.length());
        } catch (JSONException e) {
          fail("Caught JSONException in validateQueryResult :: " + e.getMessage());
        }
        
      }
        
    }
  }
  
  private String addExpectedException (int index) {
  
    String expectedEx =  "appears to have started a thread named";
    if (index == 4 || index == 5 || index == 24) {
      expectedEx = "java.lang.UnsupportedOperationException";
      c.getLogger().info("<ExpectedException action=add>" + expectedEx + "</ExpectedException>");
      return expectedEx;
    } else if (index == 7) {
      expectedEx = "com.gemstone.gemfire.cache.TimeoutException";
      c.getLogger().info("<ExpectedException action=add>" + expectedEx + "</ExpectedException>");
      return expectedEx;
    } else if (index == 11 || index == 15) {
      expectedEx = "com.gemstone.gemfire.cache.CacheWriterException";
      c.getLogger().info("<ExpectedException action=add>" + expectedEx + "</ExpectedException>");
      return expectedEx;
    }else if (index == 19) {
      expectedEx = "java.lang.IllegalArgumentException";
      c.getLogger().info("<ExpectedException action=add>" + expectedEx + "</ExpectedException>");
      return expectedEx;
    }else if (index == 38 || index == 41 ) {
      expectedEx = "com.gemstone.gemfire.cache.RegionDestroyedException";
      c.getLogger().info("<ExpectedException action=add>" + expectedEx + "</ExpectedException>");
      return expectedEx;
    }
    
    return expectedEx;
    
  }
  
  private void executeQueryTestCases() {

    
    HttpEntity<Object> entity;
    
    int totalRequests = RestAPIsTestData.TEST_DATA.length;
    String expectedEx = null;
      
      for (int index=0; index < totalRequests; index++) { 
      //Debug code  
      c.getLogger().info("----------------------------------------------");
      HttpHeaders headers = setRequestHeaders(index);
      c.getLogger().info("Index:" + index+ " " +  RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.METHOD_INDEX] + " " + RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.URL_INDEX]);
                 
       if(index == 50){
         System.out.println("Debug Here...!!");
       }
       
       try {    
          expectedEx = addExpectedException(index);
          final String restRequestUrl = RestTestUtils.createRestURL(this.baseURL, RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.URL_INDEX]);  
          
          entity = new HttpEntity<>(RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.REQUEST_BODY_INDEX], headers);
          ResponseEntity<String> result = RestTestUtils.getRestTemplate().exchange(
              restRequestUrl,
              (HttpMethod)RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.METHOD_INDEX], entity, String.class);
        
          //System.out.println("NIL SUCCESS OUTPUT body : " + result.getBody());
          //System.out.println("NIL SUCCESS STATUS CODE : " + result.getStatusCode());
          //Retrieve the "security-gfrest-authtoken" header for the userType and store the token for subsequent request.
          String authToken = result.getHeaders().getFirst("security-gfrest-authtoken");
          System.out.println(": SUCCESSFUL authToken received "+ authToken);
          
          String uType = (String)RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.USER_CREDENTIAL];
          if(!StringUtils.isEmpty(authToken)){
            RestAPIsTestData.userToTokenMap.put(uType, authToken);
          }
          /*
          else if (RestAPIsTestData.userToTokenMap.containsKey(uType)){
            RestAPIsTestData.userToTokenMap.remove(uType);
          }else {
            
          }
          */
          //validate Content-length header value
          if(result.hasBody()){
            assertEquals(result.getHeaders().getContentLength(), result.getBody().length());
          }
          
          validateGetAllResult(index, result);
          validateQueryResult(index, result);
          
          assertEquals(result.getStatusCode(), RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.STATUS_CODE_INDEX]);
          assertEquals(result.hasBody(), ((Boolean)RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.RESPONSE_HAS_BODY_INDEX]).booleanValue());
          
          verifyRegionSize(index, result);
          //TODO:
          //verify location header
          
        } catch (HttpClientErrorException e) {
          /*String authToken = e.getResponseHeaders().getFirst("security-gfrest-authtoken");
          String uType = (String)RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.USER_CREDENTIAL];
          System.out.println(": HttpClientErrorException authToken received "+ authToken);
          if(!StringUtils.isEmpty(authToken)){
            RestAPIsTestData.userToTokenMap.put(uType, authToken);
          }
          */
          //System.out.println(" HttpClientErrorException e.BODY = " + e.getResponseBodyAsString());
          //System.out.println(" HttpClientErrorException e.getStatusCode() = " + e.getStatusCode());
          
          String authToken = e.getResponseHeaders().getFirst("security-gfrest-authtoken");
          //System.out.println(": HttpClientErrorException authToken received "+ authToken);
          
          String uType = (String)RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.USER_CREDENTIAL];
          if(!StringUtils.isEmpty(authToken)){
            RestAPIsTestData.userToTokenMap.put(uType, authToken);
          }
          /*
          HttpHeaders heads = e.getResponseHeaders();
          boolean isExist = heads.containsKey("security-gfrest-authtoken");
          //System.out.println(" : isExist = " + isExist + "  AND size = " + heads.size());
          
          for(Object item1 : heads.keySet()){
            System.out.println("header Name = " + item1.toString());
          }
          
          List<String> values = heads.get("security-gfrest-authtoken");
          
          if(values != null) {
            System.out.println(" : values.size() = " + values.size());
            for(Object item : values){
              System.out.println("item = " + item.toString());
            }
          }else {
            System.out.println(" : values is NULL!");
          }
          */
          /*
          for(int i=0; i< heads.size(); i++){
            heads.get("security-gfrest-authtoken")
          }
          */
          
          if( VALID_409_URL_INDEXS.contains(index)) { 
            //create-409, conflict testcase. [create on already existing key]
            
            assertEquals(e.getStatusCode(), RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.STATUS_CODE_INDEX]);
            assertEquals(StringUtils.hasText(e.getResponseBodyAsString()),((Boolean)RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.RESPONSE_HAS_BODY_INDEX]).booleanValue());
            
          }else if (VALID_400_URL_INDEXS.contains(index)) { 
            // 400, Bad Request testcases. [create with malformed Json]
            
            assertEquals(e.getStatusCode(), RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.STATUS_CODE_INDEX]);
            assertEquals(StringUtils.hasText(e.getResponseBodyAsString()), ((Boolean)RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.RESPONSE_HAS_BODY_INDEX]).booleanValue());
            
          }
          else if(VALID_404_URL_INDEXS.contains(index) ) { 
            // create-404, Not Found testcase. [create on not-existing region]
            
            assertEquals(e.getStatusCode(), RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.STATUS_CODE_INDEX]);
            assertEquals(StringUtils.hasText(e.getResponseBodyAsString()), ((Boolean)RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.RESPONSE_HAS_BODY_INDEX]).booleanValue());
           
          }
          else if(VALID_405_URL_INDEXS.contains(index) ) { 
            // create-404, Not Found testcase. [create on not-existing region]
            System.out.println(" CODE = " + e.getStatusCode());
            System.out.println(" hasBODY =" + StringUtils.hasText(e.getResponseBodyAsString()));
            
            assertEquals(e.getStatusCode(), RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.STATUS_CODE_INDEX]);
            assertEquals(StringUtils.hasText(e.getResponseBodyAsString()), ((Boolean)RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.RESPONSE_HAS_BODY_INDEX]).booleanValue());
          }
          else if(VALID_401_URL_INDEXS.contains(index)){
            assertEquals(e.getStatusCode(), RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.STATUS_CODE_INDEX]);
          }
          else {
          fail( "Index:" + index+ " " +  RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.METHOD_INDEX] + " " + RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.URL_INDEX] + " should not have thrown exception ");
          }
          
        }catch (HttpServerErrorException se) { 
          
          System.out.println(": HttpServerErrorException Body received "+ se.getResponseBodyAsString());
          
          String authToken = se.getResponseHeaders().getFirst("security-gfrest-authtoken");
          String uType = (String)RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.USER_CREDENTIAL];
          System.out.println(": HttpServerErrorException authToken received "+ authToken);
          if(!StringUtils.isEmpty(authToken)){
            RestAPIsTestData.userToTokenMap.put(uType, authToken);
          }
          //index=4, create- 500, INTERNAL_SERVER_ERROR testcase. [create on Region with DataPolicy=Empty set]
          //index=7, create- 500, INTERNAL_SERVER_ERROR testcase. [Get, attached cache loader throws Timeout exception]
          //index=11, put- 500, [While doing R.put, CacheWriter.beforeCreate() has thrown CacheWriterException]
          //.... and more test cases
          assertEquals(se.getStatusCode(), RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.STATUS_CODE_INDEX]);
          assertEquals(StringUtils.hasText(se.getResponseBodyAsString()), ((Boolean)RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.RESPONSE_HAS_BODY_INDEX]).booleanValue());
          
        }
        catch (Exception e) {
          
          fail("caught Exception in executeQueryTestCases " + "Index:" + index+ " " +  RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.METHOD_INDEX] + " " + RestAPIsTestData.TEST_DATA[index][RestAPIsTestData.URL_INDEX] + " :: Unexpected ERROR...!!" + e.getMessage());
        }finally {
          c.getLogger().info("<ExpectedException action=remove>" + expectedEx + "</ExpectedException>");
        }
        
      } 
      
      System.out.println(" PRINTING userToToken MAP");
      for(Map.Entry<String, String> entry : RestAPIsTestData.userToTokenMap.entrySet()){
        System.out.println("KEY : " + entry.getKey() + "  VALUE: " + entry.getValue());
      }
      
      
  }
  
}
