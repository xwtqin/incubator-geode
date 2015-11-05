package com.gemstone.gemfire.rest.internal.web.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import com.gemstone.gemfire.cache.CacheLoader;
import com.gemstone.gemfire.cache.CacheWriter;
import com.gemstone.gemfire.cache.CacheWriterException;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.LoaderHelper;
import com.gemstone.gemfire.cache.RegionDestroyedException;
import com.gemstone.gemfire.cache.RegionEvent;
import com.gemstone.gemfire.cache.TimeoutException;

class SimpleCacheLoader implements CacheLoader<String, Object>, Declarable {
  
  public Object load(LoaderHelper helper) {
    //throws TimeoutException  
    throw new TimeoutException("Could not load, Request Timedout...!!");
  }
  public void close() {  
  
  }
  @Override
  public void init(Properties props) {
    
  }
}

class SampleCacheWriter  implements CacheWriter<String, Object> {

  @Override
  public void close() { }

  @Override
  public void beforeUpdate(EntryEvent event) throws CacheWriterException { }

  @Override
  public void beforeCreate(EntryEvent event) throws CacheWriterException {
    throw new CacheWriterException("Put request failed as gemfire has thrown error...!!");
  }

  @Override
  public void beforeDestroy(EntryEvent event) throws CacheWriterException {
    throw new RegionDestroyedException("Region has been destroyed already...!!", "dummyRegion");
  }

  @Override
  public void beforeRegionDestroy(RegionEvent event) throws CacheWriterException { }

  @Override
  public void beforeRegionClear(RegionEvent event) throws CacheWriterException { } 
}

enum QueryType {LIST_ALL_NAMED_QUERY, EXECUTE_NAMED_QUERY, EXECUTE_ADHOC_QUERY }

class QueryResultData
{
  private int queryIndex;
  private QueryType type; 
  private int resultSize;
  private List<String> result;
  
  public QueryResultData() {
  }

  @SuppressWarnings("unused")
  public QueryResultData(int index, QueryType type, int size, List<String> result){
    this.queryIndex = index;
    this.type = type;
    this.resultSize = size;
    this.result = result;
  }

  public QueryType getType() {
    return type;
  }

  public void setType(QueryType type) {
    this.type = type;
  }

  public int getQueryIndex() {
    return queryIndex;
  }

  public void setQueryIndex(int queryIndex) {
    this.queryIndex = queryIndex;
  }

  public int getResultSize() {
    return resultSize;
  }

  public void setResultSize(int resultSize) {
    this.resultSize = resultSize;
  }

  public List<String> getResult() {
    return result;
  }

  public void setResult(List<String> result) {
    this.result = result;
  }
  
}

interface UserType {
  public String USER = "user";
  public String READER = "reader";
  public String WRITER = "writer";
  public String ADMIN = "admin";
  public String UNAUTHENTICATED = "unauthenticated";
}

public class RestAPIsTestData implements UserType{
  
  public static final String CUSTOMER_REGION = "customers";
  public static final String ITEM_REGION = "items";
  public static final String ORDER_REGION = "orders";
  public static final String PRIMITIVE_KV_STORE_REGION = "primitiveKVStore";
  public static final String UNKNOWN_REGION = "unknown_region";
  public static final String EMPTY_REGION = "empty_region";
  
  //public static enum UserType {USER, READER, WRITER, ADMIN, UNAUTHENTICATED }
  
  public static ConcurrentHashMap<String, String> userToTokenMap= new ConcurrentHashMap<String, String>();
  
  public static final String ORDER1_AS_JSON = "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Order\","
      + "\"purchaseOrderNo\": 111," + "\"customerId\": 101,"
      + "\"description\": \"Purchase order for company - A\"," + "\"orderDate\": \"01/10/2014\"," + "\"deliveryDate\": \"01/20/2014\","
      + "\"contact\": \"Nilkanthkumar N Patel\","
      + "\"email\": \"npatel@pivotal.io\"," + "\"phone\": \"020-2048096\"," + "\"totalPrice\": 205,"
      + "\"items\":" + "[" + "{" + "\"itemNo\": 1,"
      + "\"description\": \"Product-1\"," + "\"quantity\": 5,"
      + "\"unitPrice\": 10," + "\"totalPrice\": 50" + "}," + "{"
      + "\"itemNo\": 1," + "\"description\": \"Product-2\","
      + "\"quantity\": 10," + "\"unitPrice\": 15.5," + "\"totalPrice\": 155"
      + "}" + "]" + "}";
  
  public static final String MALFORMED_JSON = "{"
      + "\"@type\" \"com.gemstone.gemfire.rest.internal.web.controllers.Order\","
      + "\"purchaseOrderNo\": 111," + "\"customerId\": 101,"
      + "\"description\": \"Purchase order for company - A\"," + "\"orderDate\": \"01/10/2014\"," + "\"deliveryDate\": \"01/20/2014\","
      + "\"contact\": \"Nilkanthkumar N Patel\","
      + "\"email\": \"npatel@pivotal.io\"," + "\"phone\": \"020-2048096\"," + "\"totalPrice\": 205,"
      + "\"items\":" + "[" + "{" + "\"itemNo\": 1,"
      + "\"description\": \"Product-1\"," + "\"quantity\": 5,"
      + "\"unitPrice\": 10," + "\"totalPrice\": 50" + "}," + "{"
      + "\"itemNo\": 1," + "\"description\": \"Product-2\","
      + "\"quantity\": 10," + "\"unitPrice\": 15.5," + "\"totalPrice\": 155"
      + "}" + "]" + "}";
  
  public static final String ORDER2_AS_JSON = "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Order\","
      + "\"purchaseOrderNo\": 112," + "\"customerId\": 102,"
      + "\"description\": \"Purchase order for company - B\"," + "\"orderDate\": \"02/10/2014\"," + "\"deliveryDate\": \"02/20/2014\","
      + "\"contact\": \"John Blum\","
      + "\"email\": \"jblum@pivotal.io\"," + "\"phone\": \"01-2048096\"," + "\"totalPrice\": 225,"
      + "\"items\":" + "[" + "{" + "\"itemNo\": 1,"
      + "\"description\": \"Product-3\"," + "\"quantity\": 6,"
      + "\"unitPrice\": 20," + "\"totalPrice\": 120" + "}," + "{"
      + "\"itemNo\": 2," + "\"description\": \"Product-4\","
      + "\"quantity\": 10," + "\"unitPrice\": 10.5," + "\"totalPrice\": 105"
      + "}" + "]" + "}";
  
  public static final String ORDER2_UPDATED_AS_JSON = "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Order\","
      + "\"purchaseOrderNo\": 1112," + "\"customerId\": 102,"
      + "\"description\": \"Purchase order for company - B\","  + "\"orderDate\": \"02/10/2014\"," + "\"deliveryDate\": \"02/20/2014\","
      + "\"contact\": \"John Blum\","
      + "\"email\": \"jblum@pivotal.io\"," + "\"phone\": \"01-2048096\"," + "\"totalPrice\": 350,"
      + "\"items\":" + "[" + "{" + "\"itemNo\": 1,"
      + "\"description\": \"Product-AAAA\"," + "\"quantity\": 10,"
      + "\"unitPrice\": 20," + "\"totalPrice\": 200" + "}," + "{"
      + "\"itemNo\": 2," + "\"description\": \"Product-BBB\","
      + "\"quantity\": 15," + "\"unitPrice\": 10," + "\"totalPrice\": 150"
      + "}" + "]" + "}";
  
  public static final String CUSTOMER_LIST1_AS_JSON = "[" 
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 1,"
      + " \"firstName\": \"Vishal\","
      + " \"lastName\": \"Roa\"" 
      + "},"
      +"{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 2,"
      + " \"firstName\": \"Nilkanth\","
      + " \"lastName\": \"Patel\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 3,"
      + " \"firstName\": \"Avinash Dongre\","
      + " \"lastName\": \"Roa\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 4,"
      + " \"firstName\": \"Avinash Dongre\","
      + " \"lastName\": \"Roa\"" 
      + "}"
      + "]";
      
  public static final String CUSTOMER_LIST_AS_JSON = "[" 
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 101,"
      + " \"firstName\": \"Vishal\","
      + " \"lastName\": \"Roa\"" 
      + "},"
      +"{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 102,"
      + " \"firstName\": \"Nilkanth\","
      + " \"lastName\": \"Patel\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 103,"
      + " \"firstName\": \"Avinash Dongre\","
      + " \"lastName\": \"Roa\"" 
      + "},"
      +"{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 104,"
      + " \"firstName\": \"John\","
      + " \"lastName\": \"Blum\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 105,"
      + " \"firstName\": \"Shankar\","
      + " \"lastName\": \"Hundekar\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 106,"
      + " \"firstName\": \"Amey\","
      + " \"lastName\": \"Barve\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 107,"
      + " \"firstName\": \"Vishal\","
      + " \"lastName\": \"Roa\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 108,"
      + " \"firstName\": \"Supriya\","
      + " \"lastName\": \"Pillai\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 109,"
      + " \"firstName\": \"Tushar\","
      + " \"lastName\": \"khairnar\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 110,"
      + " \"firstName\": \"Rishitesh\","
      + " \"lastName\": \"Mishra\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 111,"
      + " \"firstName\": \"Ajay\","
      + " \"lastName\": \"Pandey\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 112,"
      + " \"firstName\": \"Suyog\","
      + " \"lastName\": \"Bokare\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 113,"
      + " \"firstName\": \"Rajesh\","
      + " \"lastName\": \"kumar\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 114,"
      + " \"firstName\": \"swati\","
      + " \"lastName\": \"sawant\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 115,"
      + " \"firstName\": \"sonal\","
      + " \"lastName\": \"Agrawal\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 116,"
      + " \"firstName\": \"Amogh\","
      + " \"lastName\": \"Shetkar\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 117,"
      + " \"firstName\": \"Viren\","
      + " \"lastName\": \"Balaut\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 118,"
      + " \"firstName\": \"Namrata\","
      + " \"lastName\": \"Tanvi\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 119,"
      + " \"firstName\": \"Rahul\","
      + " \"lastName\": \"Diyekar\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 120,"
      + " \"firstName\": \"Varun\","
      + " \"lastName\": \"Agrawal\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 121,"
      + " \"firstName\": \"Hemant\","
      + " \"lastName\": \"Bhanavat\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 122,"
      + " \"firstName\": \"Sunil\","
      + " \"lastName\": \"jigyasu\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 123,"
      + " \"firstName\": \"Sumedh\","
      + " \"lastName\": \"wale\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 124,"
      + " \"firstName\": \"saobhik\","
      + " \"lastName\": \"chaudhari\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 125,"
      + " \"firstName\": \"Ketki\","
      + " \"lastName\": \"Naidu\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 126,"
      + " \"firstName\": \"YOgesh\","
      + " \"lastName\": \"Mahajan\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 127,"
      + " \"firstName\": \"Surinder\","
      + " \"lastName\": \"Bindra\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 128,"
      + " \"firstName\": \"sandip\","
      + " \"lastName\": \"kasbe\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 129,"
      + " \"firstName\": \"shivam\","
      + " \"lastName\": \"Panada\"" 
      + "},"
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Customer\","
      +"\"customerId\": 130,"
      + " \"firstName\": \"Preeti\","
      + " \"lastName\": \"Kumari\"" 
      + "},"
      + "{"
      +"\"customerId\": 131,"
      + " \"firstName\": \"Vishal31\","
      + " \"lastName\": \"Roa31\"" 
      + "},"
      +"{"
      +"\"customerId\": 132,"
      + " \"firstName\": \"Nilkanth32\","
      + " \"lastName\": \"Patel32\"" 
      + "},"
      + "{"
      +"\"customerId\": 133,"
      + " \"firstName\": \"Avinash33\","
      + " \"lastName\": \"Dongre33\"" 
      + "},"
      +"{"
      +"\"customerId\": 134,"
      + " \"firstName\": \"John34\","
      + " \"lastName\": \"Blum34\"" 
      + "},"
      + "{"
      +"\"customerId\": 135,"
      + " \"firstName\": \"Shankar35\","
      + " \"lastName\": \"Hundekar35\"" 
      + "},"
      + "{"
      +"\"customerId\": 136,"
      + " \"firstName\": \"Amey36\","
      + " \"lastName\": \"Barve36\"" 
      + "},"
      + "{"
      +"\"customerId\": 137,"
      + " \"firstName\": \"Vishal37\","
      + " \"lastName\": \"Roa37\"" 
      + "},"
      + "{"
      +"\"customerId\": 138,"
      + " \"firstName\": \"Supriya38\","
      + " \"lastName\": \"Pillai38\"" 
      + "},"
      + "{"
      +"\"customerId\": 139,"
      + " \"firstName\": \"Tushar39\","
      + " \"lastName\": \"khairnar39\"" 
      + "},"
      + "{"
      +"\"customerId\": 140,"
      + " \"firstName\": \"Rishitesh40\","
      + " \"lastName\": \"Mishra40\"" 
      + "},"
      + "{"
      +"\"customerId\": 141,"
      + " \"firstName\": \"Ajay41\","
      + " \"lastName\": \"Pandey41\"" 
      + "},"
      + "{"
      +"\"customerId\": 142,"
      + " \"firstName\": \"Suyog42\","
      + " \"lastName\": \"Bokare42\"" 
      + "},"
      + "{"
      +"\"customerId\": 143,"
      + " \"firstName\": \"Rajesh43\","
      + " \"lastName\": \"kumar43\"" 
      + "},"
      + "{"
      +"\"customerId\": 144,"
      + " \"firstName\": \"swati44\","
      + " \"lastName\": \"sawant44\"" 
      + "},"
      + "{"
      +"\"customerId\": 145,"
      + " \"firstName\": \"sonal45\","
      + " \"lastName\": \"Agrawal45\"" 
      + "},"
      + "{"
      +"\"customerId\": 146,"
      + " \"firstName\": \"Amogh46\","
      + " \"lastName\": \"Shetkar46\"" 
      + "},"
      + "{"
      +"\"customerId\": 147,"
      + " \"firstName\": \"Viren47\","
      + " \"lastName\": \"Balaut47\"" 
      + "},"
      + "{"
      +"\"customerId\": 148,"
      + " \"firstName\": \"Namrata48\","
      + " \"lastName\": \"Tanvi48\"" 
      + "},"
      + "{"
      +"\"customerId\": 149,"
      + " \"firstName\": \"Rahul49\","
      + " \"lastName\": \"Diyekar49\"" 
      + "},"
      + "{"
      +"\"customerId\": 150,"
      + " \"firstName\": \"Varun50\","
      + " \"lastName\": \"Agrawal50\"" 
      + "},"
      + "{"
      +"\"customerId\": 151,"
      + " \"firstName\": \"Hemant50\","
      + " \"lastName\": \"Bhanavat50\"" 
      + "},"
      + "{"
      +"\"customerId\": 152,"
      + " \"firstName\": \"Sunil52\","
      + " \"lastName\": \"jigyasu52\"" 
      + "},"
      + "{"
      +"\"customerId\": 153,"
      + " \"firstName\": \"Sumedh53\","
      + " \"lastName\": \"wale53\"" 
      + "},"
      + "{"
      +"\"customerId\": 154,"
      + " \"firstName\": \"saobhik54\","
      + " \"lastName\": \"chaudhari54\"" 
      + "},"
      + "{"
      +"\"customerId\": 155,"
      + " \"firstName\": \"Ketki55\","
      + " \"lastName\": \"Naidu55\"" 
      + "},"
      + "{"
      +"\"customerId\": 156,"
      + " \"firstName\": \"YOgesh56\","
      + " \"lastName\": \"Mahajan56\"" 
      + "},"
      + "{"
      +"\"customerId\": 157,"
      + " \"firstName\": \"Surinder57\","
      + " \"lastName\": \"Bindra57\"" 
      + "},"
      + "{"
      +"\"customerId\": 158,"
      + " \"firstName\": \"sandip58\","
      + " \"lastName\": \"kasbe58\"" 
      + "},"
      + "{"
      +"\"customerId\": 159,"
      + " \"firstName\": \"shivam59\","
      + " \"lastName\": \"Panada59\"" 
      + "},"
      + "{"
      +"\"customerId\": 160,"
      + " \"firstName\": \"Preeti60\","
      + " \"lastName\": \"Kumari60\"" 
      + "}"
      + "]";
  
  public static final String ORDER_AS_CASJSON = "{"
      + "\"@old\" :" 
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Order\","
      + "\"purchaseOrderNo\": 111," + "\"customerId\": 101,"
      + "\"description\": \"Purchase order for company - A\"," + "\"orderDate\": \"01/10/2014\"," + "\"deliveryDate\": \"01/20/2014\","
      + "\"contact\": \"Nilkanthkumar N Patel\","
      + "\"email\": \"npatel@pivotal.io\"," + "\"phone\": \"020-2048096\"," + "\"totalPrice\": 205,"
      + "\"items\":" + "[" + "{" + "\"itemNo\": 1,"
      + "\"description\": \"Product-1\"," + "\"quantity\": 5,"
      + "\"unitPrice\": 10," + "\"totalPrice\": 50" + "}," + "{"
      + "\"itemNo\": 1," + "\"description\": \"Product-2\","
      + "\"quantity\": 10," + "\"unitPrice\": 15.5," + "\"totalPrice\": 155"
      + "}" + "]" 
      + "},"
      + "\"@new\" :" 
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Order\","
      + "\"purchaseOrderNo\": 11101," + "\"customerId\": 101,"
      + "\"description\": \"Purchase order for company - A\"," + "\"orderDate\": \"01/10/2014\"," + "\"deliveryDate\": \"01/20/2014\","
      + "\"contact\": \"Nilkanthkumar N Patel\","
      + "\"email\": \"npatel@pivotal.io\"," + "\"phone\": \"020-2048096\"," + "\"totalPrice\": 205,"
      + "\"items\":" 
      + "[" 
        + "{" 
          + "\"itemNo\": 1,"
          +  "\"description\": \"Product-1\","
          + "\"quantity\": 5,"
          + "\"unitPrice\": 10,"
          + "\"totalPrice\": 50" 
        + "}," 
          + "{" 
          + "\"itemNo\": 3,"
          +  "\"description\": \"Product-3\","
          + "\"quantity\": 10,"
          + "\"unitPrice\": 100,"
          + "\"totalPrice\": 1000" 
          + "}," 
        + "{"
          + "\"itemNo\": 1,"
          + "\"description\": \"Product-2\","
          + "\"quantity\": 10,"
          + "\"unitPrice\": 15.5,"
          + "\"totalPrice\": 155"
        + "}"
      + "]" 
      + "}"
      + "}";
      
  public static final String MALFORMED_CAS_JSON = "{"
      + "\"@old\" :" 
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Order\","
      + "\"purchaseOrderNo\": 111," + "\"customerId\": 101,"
      + "\"description\": \"Purchase order for company - A\"," + "\"orderDate\": \"01/10/2014\"," + "\"deliveryDate\": \"01/20/2014\","
      + "\"contact\": \"Nilkanthkumar N Patel\","
      + "\"email\": \"npatel@pivotal.io\"," + "\"phone\": \"020-2048096\"," + "\"totalPrice\": 205,"
      + "\"items\":" + "[" + "{" + "\"itemNo\": 1,"
      + "\"description\": \"Product-1\"," + "\"quantity\": 5,"
      + "\"unitPrice\": 10," + "\"totalPrice\": 50" + "}," + "{"
      + "\"itemNo\": 1," + "\"description\": \"Product-2\","
      + "\"quantity\": 10," + "\"unitPrice\": 15.5," + "\"totalPrice\": 155"
      + "}" + "]" 
      + "},"
       
      + "{"
      + "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Order\","
      + "\"purchaseOrderNo\": 11101," + "\"customerId\": 101,"
      + "\"description\": \"Purchase order for company - A\"," + "\"orderDate\": \"01/10/2014\"," + "\"deliveryDate\": \"01/20/2014\","
      + "\"contact\": \"Nilkanthkumar N Patel\","
      + "\"email\": \"npatel@pivotal.io\"," + "\"phone\": \"020-2048096\"," + "\"totalPrice\": 205,"
      + "\"items\":" 
      + "[" 
        + "{" 
          + "\"itemNo\": 1,"
          +  "\"description\": \"Product-1\","
          + "\"quantity\": 5,"
          + "\"unitPrice\": 10,"
          + "\"totalPrice\": 50" 
        + "}," 
          + "{" 
          + "\"itemNo\": 3,"
          +  "\"description\": \"Product-3\","
          + "\"quantity\": 10,"
          + "\"unitPrice\": 100,"
          + "\"totalPrice\": 1000" 
          + "}," 
        + "{"
          + "\"itemNo\": 1,"
          + "\"description\": \"Product-2\","
          + "\"quantity\": 10,"
          + "\"unitPrice\": 15.5,"
          + "\"totalPrice\": 155"
        + "}"
      + "]" 
      + "}"
      + "}";
  
  public static final String[][] PARAMETERIZED_QUERIES = new String[][] {
      {
        "selectOrders",
        "SELECT DISTINCT o FROM /orders o, o.items item WHERE item.quantity > $1 AND item.totalPrice > $2" },
      { 
        "selectCustomer",
        "SELECT c FROM /customers c WHERE c.customerId = $1" },
      {
        "selectHighRollers",
        "SELECT DISTINCT c FROM /customers c, /orders o, o.items item WHERE item.totalprice > $1 AND c.customerId = o.customerId" 
      },
      {
        "testQuery",
        "SELECT DISTINCT c from /customers c where lastName=$1"
      },
      {
        "findSelectedCustomers",
        "SELECT * from /customers where customerId  IN SET ($1, $2, $3)"
      },
      {
        "invalidQuery",
        "This is invalid string"
      }
  };
  
  public static final String QUERY_ARGS2 = "{"
      + "\"@type\": \"int\","
      + "\"@value\": 101"
      + "}";
  
  public static final String QUERY_ARGS1 = "["
      +"{"
      + "\"@type\": \"int\","
      + "\"@value\": 2"
      + "},"
      +"{"
      + "\"@type\": \"double\","
      + "\"@value\": 110.00"
      + "}"
      + "]";

  @SuppressWarnings("unused")
  public static final String QUERY_ARGS3 = "["
      +"{"
      + "\"@type\": \"String\","
      + "\"@value\": \"Agrawal\""
      + "}"
      + "]";

  @SuppressWarnings("unused")
  public static final String QUERY_ARGS4 = "["
      +"{"
      + "\"@type\": \"int\","
      + "\"@value\": 20"
      + "},"
      +"{"
      + "\"@type\": \"int\","
      + "\"@value\": 120"
      + "},"
      +"{"
      + "\"@type\": \"int\","
      + "\"@value\": 130"
      + "}"
      + "]";
  
 
  public static final String FUNCTION_ARGS1 = "["
    +    "{"
    +        "\"@type\": \"double\","
    +        "\"@value\": 210"
    +    "},"
    +    "{"
    +        "\"@type\": \"com.gemstone.gemfire.rest.internal.web.controllers.Item\","
    +        "\"itemNo\": \"599\","
    +        "\"description\": \"Part X Free on Bumper Offer\","
    +        "\"quantity\": \"2\","
    +        "\"unitprice\": \"5\","
    +        "\"totalprice\": \"10.00\""
    +    "}"
    +"]";

  public static List<String> queryIds;
  static{
    queryIds = new ArrayList<>();
    for (int i=0; i < PARAMETERIZED_QUERIES.length; i++ ){
      queryIds.add(PARAMETERIZED_QUERIES[i][0]) ;
    }
  }
  
  public static final Map<Integer, QueryResultData> queryResultByIndex;
  static{
    queryResultByIndex = new HashMap<Integer, QueryResultData>();
    queryResultByIndex.put(45, new QueryResultData(45, QueryType.LIST_ALL_NAMED_QUERY, 2, queryIds));
    queryResultByIndex.put(46, new QueryResultData(46, QueryType.EXECUTE_NAMED_QUERY, 2, null));
    queryResultByIndex.put(47, new QueryResultData(47, QueryType.EXECUTE_NAMED_QUERY, 0, null));
    queryResultByIndex.put(48, new QueryResultData(48, QueryType.EXECUTE_ADHOC_QUERY, 55, null));
    
  }
  
  public static SimpleCacheLoader getSimpleCacheLoader(){
     return new SimpleCacheLoader();
  }
  
  public static SampleCacheWriter getSampleCacheWriter(){
    return new SampleCacheWriter();
  }
  
  private static String URL_CREATE_ON_KEY_1 = "/orders?key=1";
  private static String KEY_1_LOCATION_HEADER = "/orders/1";
  private static String KEY_2_LOCATION_HEADER = "/orders/2";
  private static String GETALL_ENTRIES_LOCATION_HEADER_ = "/customers";
  private static String LIST_KEYS_LOCATION_HEADER = "/customers"; 
  
  private static String URL_CREATE_ON_KEY_K1 = "/orders?key=k1";
  private static String URL_CREATE_ON_UNKNOWN_REGION = "/"+ RestAPIsTestData.UNKNOWN_REGION + "?key=k1";
  private static String URL_CREATE_ON_REGION_HAVING_DATAPLOLICY_EMPTY = "/"+ RestAPIsTestData.EMPTY_REGION + "?key=k1";
  
  private static String URL_GET_ON_KEY_1 = "/orders/1";
  private static String URL_GET_ON_UNKNOWN_REGION = "/"+ RestAPIsTestData.UNKNOWN_REGION + "/1";
                                                    
  private static String URL_GET_ON_UNKNOWN_KEY = "/"+ RestAPIsTestData.EMPTY_REGION + "/unknown";
  
  private static String URL_PUT_ON_KEY_2 = "/orders/2";
  private static String URL_PUT_MALFORMED_JSON_ON_KEY_3 = "/orders/3";
  private static String URL_PUT_ON_UNKNOWN_REGION = "/"+ RestAPIsTestData.UNKNOWN_REGION + "/k1";
  private static String URL_PUT_ON_REGION_HAVING_DATAPLOLICY_EMPTY ="/"+ RestAPIsTestData.EMPTY_REGION + "/k1";
  
  private static String URL_PUTALL_ON_KEYS ="/customers/1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60";
  private static String URL_PUTALL_MALFORMED_JSON ="/customers/3,4,5,6";
  private static String URL_PUTALL_ON_UNKNOWN_REGION = "/"+ RestAPIsTestData.UNKNOWN_REGION + "/3,4,5,6";
  private static String URL_PUTALL_ON_REGION_HAVING_DATAPLOLICY_EMPTY = "/"+ RestAPIsTestData.EMPTY_REGION + "/3,4,5,6";
  
  private static String URL_REPLACE_ON_KEY_2 ="/orders/2?op=REPLACE";
  private static String URL_REPLACE_ON_UNKNOWN_REGION = "/"+ RestAPIsTestData.UNKNOWN_REGION + "/k1?op=rePlace";
  private static String URL_REPLACE_ON_REGION_HAVING_DATAPLOLICY_EMPTY ="/"+ RestAPIsTestData.EMPTY_REGION + "/k1?op=REPLACE";
  
  private static String URL_CAS_ON_KEY_1 ="/orders/1?op=CAS";
  private static String URL_CAS_ON_KEY_2 = "/orders/2?op=CAS";  
  private static String URL_CAS_ON_UNKNOWN_REGION = "/"+ RestAPIsTestData.UNKNOWN_REGION + "/k1?op=CAS";  
  private static String URL_CAS_ON_REGION_HAVING_DATAPLOLICY_EMPTY ="/"+ RestAPIsTestData.EMPTY_REGION + "/k1?op=cAs";
  
  private static String URL_LIST_ALL_REGIONS =""; //<HOSTNAME:PORT/gemfire-api/v1>
  
  private static String URL_GETALL_DEFAULT = "/customers";
  private static String URL_GETALL_ON_UNKNOWN_REGION ="/" + RestAPIsTestData.UNKNOWN_REGION;
  private static String URL_GETALL_ALL_ENTRIES = "/customers?limit=ALL"; 
  private static String URL_GETALL_LIMITED_ENTRIES = "/customers?limit=10";
  
  private static String URL_LIST_KEYS = "/customers/keys";
  private static String URL_LIST_KEYS_ON_UNKNOWN_REGION = "/"+ RestAPIsTestData.UNKNOWN_REGION +"/keys";
  
  private static String URL_GET_ON_SPECIFIC_KEYS = "/customers/1,2,3,4,5,6,7,8,9,10";
  private static String URL_GET_SPECIFIC_KEYS_DATA_ON_UNKNOWN_REGION = "/" + RestAPIsTestData.UNKNOWN_REGION + "/1,2,3,4,5,6,7,8,9,10";
  
  private static String URL_DELETE_ON_KEY_1 = "/customers/1";
  private static String URL_DELETE_ON_UNKNOWN_REGION = "/"+ RestAPIsTestData.UNKNOWN_REGION + "/1";
  private static String URL_DELETE_ON_REGION_HAVING_DATAPLOLICY_EMPTY ="/" + RestAPIsTestData.EMPTY_REGION + "/1";
  private static String URL_DELETE_SPECIFIC_KEYS = "/customers/2,3,4,5";
  private static String URL_DELETE_SPECIFIC_KEYS_ON_UNKNOWN_REGION = "/" + RestAPIsTestData.UNKNOWN_REGION + "/2,3,4,5";
  private static String URL_DELETE_SPECIFIC_KEYS_ON_REGION_HAVING_DATAPLOLICY_EMPTY ="/" + RestAPIsTestData.EMPTY_REGION + "/2,3,4,5";
  
  private static String URL_CREATE_QUERY1 = "/queries?id=" +  RestAPIsTestData.PARAMETERIZED_QUERIES[0][0] + "&q=" + RestAPIsTestData.PARAMETERIZED_QUERIES[0][1];
  private static String QUERY1_LOCATION_HEADER = "/queries/" + RestAPIsTestData.PARAMETERIZED_QUERIES[0][0];
  private static String URL_CREATE_QUERY2 ="/queries?id=" +  RestAPIsTestData.PARAMETERIZED_QUERIES[1][0] + "&q=" + RestAPIsTestData.PARAMETERIZED_QUERIES[1][1];
  private static String QUERY2_LOCATION_HEADER = "/queries/" + RestAPIsTestData.PARAMETERIZED_QUERIES[1][0];
  private static String URL_CREATE_QUERY3 ="/queries?id=" +  RestAPIsTestData.PARAMETERIZED_QUERIES[2][0] + "&q=" + RestAPIsTestData.PARAMETERIZED_QUERIES[2][1];
  private static String QUERY3_LOCATION_HEADER = "/queries/" + RestAPIsTestData.PARAMETERIZED_QUERIES[2][0];
  private static String URL_LIST_QUERIES = "/queries";
  private static String LIST_QUERIES_LOCATION_HEADER = "/queries";
  
  private static String URL_EXECUTE_QUERY1 = "/queries/" + RestAPIsTestData.PARAMETERIZED_QUERIES[0][0];
  private static String URL_EXECUTE_QUERY2 = "/queries/" + RestAPIsTestData.PARAMETERIZED_QUERIES[1][0];
  private static String URL_EXECUTE_ADHOC_QUERY1 = "/queries/adhoc?q=SELECT * FROM /customers";
  private static String URL_CREATE_QUERY4 = "/queries?id=" +  RestAPIsTestData.PARAMETERIZED_QUERIES[3][0] + "&q=" + RestAPIsTestData.PARAMETERIZED_QUERIES[3][1];
  private static String QUERY4_LOCATION_HEADER ="/queries/" + RestAPIsTestData.PARAMETERIZED_QUERIES[3][0];
      
  private static String URL_LIST_FUNCTIONS = "/functions";
  private static String LIST_FUNCTIONS_LOCATION_HEADER = "/functions";
  private static String URL_EXECUTE_FUNCTION_ONREGION = "/functions/AddFreeItemToOrders?onRegion=orders";
  private static String EXECUTE_FUNCTION_ONREGION_LOCATION_HEADER = "/functions/AddFreeItemToOrders";
  
  private static String URL_UPDATE_QUERY1_WITH_INVALID_OQL = "/queries/" +  RestAPIsTestData.PARAMETERIZED_QUERIES[0][0] + "?q=" + RestAPIsTestData.PARAMETERIZED_QUERIES[4][1];
  private static String URL_EXECUTE_INVALID_QUERY = "/queries/" + RestAPIsTestData.PARAMETERIZED_QUERIES[0][0];
  private static String URL_UPDATE_UNKNOWN_QUERY = "/queries/" +  "invalidQuery" + "?q=" + RestAPIsTestData.PARAMETERIZED_QUERIES[4][1];
  
  private static String URL_DELETE_QUERY4 ="/queries/" + RestAPIsTestData.PARAMETERIZED_QUERIES[3][0];
  private static String URL_PING_SERVICE = "/ping";
  
  private static String URL_CREATE_QUERY4_USING_REQUESTBODY = "/queries?id=" +  RestAPIsTestData.PARAMETERIZED_QUERIES[3][0];
  private static String URL_UPDATE_QUERY4_USING_REQUESTBODY = "/queries/" +  RestAPIsTestData.PARAMETERIZED_QUERIES[3][0];
      
  public static final int METHOD_INDEX = 0;
  public static final int URL_INDEX = 1;
  public static final int REQUEST_BODY_INDEX = 2;
  public static final int STATUS_CODE_INDEX = 3;
  public static final int LOCATION_HEADER_INDEX = 4;
  public static final int RESPONSE_HAS_BODY_INDEX = 5;
  public static final int USER_CREDENTIAL = 6;
  
  public static Object TEST_DATA[][]={ 
    { //0. create - 200 ok
      HttpMethod.POST,  
      URL_CREATE_ON_KEY_1,  
      RestAPIsTestData.ORDER1_AS_JSON, 
      HttpStatus.CREATED,               
      KEY_1_LOCATION_HEADER,
      false, 
      UserType.ADMIN
    },
    { //1. create - 409 conflict
      HttpMethod.POST,  
      URL_CREATE_ON_KEY_1, 
      RestAPIsTestData.ORDER1_AS_JSON,  
      HttpStatus.CONFLICT,              
      KEY_1_LOCATION_HEADER,
      true,
      UserType.ADMIN
    },
    { //2. create - 400 bad Req for malformed Json
      HttpMethod.POST,  
      URL_CREATE_ON_KEY_K1, 
      RestAPIsTestData.MALFORMED_JSON,   
      HttpStatus.BAD_REQUEST,           
      null,
      true,
      UserType.ADMIN
    },    
    { //3. create - 404, Not Found, for Region not exist
      HttpMethod.POST,  
      URL_CREATE_ON_UNKNOWN_REGION,
      RestAPIsTestData.ORDER1_AS_JSON,
      HttpStatus.NOT_FOUND,             
      null,
      true,
      UserType.ADMIN
    },    
    { //4. create - 500 creating entry on region having DataPolicy=Empty
      HttpMethod.POST,  
      URL_CREATE_ON_REGION_HAVING_DATAPLOLICY_EMPTY, 
      RestAPIsTestData.ORDER1_AS_JSON, 
      HttpStatus.INTERNAL_SERVER_ERROR, 
      null,
      true,
      UserType.ADMIN
    },    
    { //5. Get data for key - 200 ok
      HttpMethod.GET,   
      URL_GET_ON_KEY_1,
      null,
      HttpStatus.OK,                    
      KEY_1_LOCATION_HEADER,
      true,
      UserType.ADMIN
    },
    { //6. Get data for key - 404 region not exist
      HttpMethod.GET,   
      URL_GET_ON_UNKNOWN_REGION,
      null,
      HttpStatus.NOT_FOUND,             
      null,
      true,
      UserType.ADMIN
    },
    { //7. Get data for Non-existing key - 404, Resource NOT FOUND.
      HttpMethod.GET,   
      URL_GET_ON_UNKNOWN_KEY,
      null,
      HttpStatus.INTERNAL_SERVER_ERROR, 
      null,
      true,
      UserType.ADMIN
    },    
    { //8.  Put - 200 Ok, successful
      HttpMethod.PUT,   
      URL_PUT_ON_KEY_2, 
      RestAPIsTestData.ORDER2_AS_JSON,
      HttpStatus.OK,                    
      KEY_2_LOCATION_HEADER,  
      false,
      UserType.ADMIN
    },
    { //9.  Put - 400 Bad Request, Malformed JSOn
      HttpMethod.PUT,   
      URL_PUT_MALFORMED_JSON_ON_KEY_3,
      RestAPIsTestData.MALFORMED_JSON, 
      HttpStatus.BAD_REQUEST,          
      null,
      true,
      UserType.ADMIN
    },
    { //10. Put - 404 Not Found, Region does not exist
      HttpMethod.PUT,   
      URL_PUT_ON_UNKNOWN_REGION,
      RestAPIsTestData.ORDER2_AS_JSON, 
      HttpStatus.NOT_FOUND,        
      null,
      true,
      UserType.ADMIN
    },
    { //11. Put - 500, Gemfire throws exception
      HttpMethod.PUT,   
      URL_PUT_ON_REGION_HAVING_DATAPLOLICY_EMPTY,
      RestAPIsTestData.ORDER2_AS_JSON,
      HttpStatus.INTERNAL_SERVER_ERROR,
      null,
      true,
      UserType.ADMIN
    },    
    { //12. putAll - 200 Ok
      HttpMethod.PUT,   
      URL_PUTALL_ON_KEYS,
      RestAPIsTestData.CUSTOMER_LIST_AS_JSON,
      HttpStatus.OK,   
      null,
      false,
      UserType.ADMIN
    },
    { //13. putAll - 400 bad Request, amlformed Json
      HttpMethod.PUT,   
      URL_PUTALL_MALFORMED_JSON,
      RestAPIsTestData.MALFORMED_JSON,
      HttpStatus.BAD_REQUEST,  
      null,
      true,
      UserType.ADMIN
    },
    { //14. putAll - 404 Not Found, Region Does not exist
      HttpMethod.PUT,   
      URL_PUTALL_ON_UNKNOWN_REGION,
      RestAPIsTestData.CUSTOMER_LIST1_AS_JSON,
      HttpStatus.NOT_FOUND,     
      null,
      true,
      UserType.ADMIN
    },
    { //15. putAll - 500, Gemfire throws exception
      HttpMethod.PUT,   
      URL_PUTALL_ON_REGION_HAVING_DATAPLOLICY_EMPTY,
      RestAPIsTestData.CUSTOMER_LIST1_AS_JSON,
      HttpStatus.INTERNAL_SERVER_ERROR, 
      null,
      true,
      UserType.ADMIN   
    },
    { //16. PUT?op=REPLACE, 200 Ok test case
      HttpMethod.PUT,
      URL_REPLACE_ON_KEY_2,
      RestAPIsTestData.ORDER2_UPDATED_AS_JSON,
      HttpStatus.OK,
      KEY_2_LOCATION_HEADER,
      false,
      UserType.ADMIN             
    },
    { //17. Put?op=REPLACE, 400 Bad Request, Malformed JSOn
      HttpMethod.PUT,   
      URL_REPLACE_ON_KEY_2,
      RestAPIsTestData.MALFORMED_JSON, 
      HttpStatus.BAD_REQUEST,          
      null,
      true,
      UserType.ADMIN
    },
    { //18. Put?op=REPLACE, 404 Not Found, Region does not exist
      HttpMethod.PUT,   
      URL_REPLACE_ON_UNKNOWN_REGION,
      RestAPIsTestData.ORDER2_AS_JSON, 
      HttpStatus.NOT_FOUND,        
      null,
      true,
      UserType.ADMIN
    },
    { //19. Put?op=REPLACE, 500 testcase, Gemfire exception
      HttpMethod.PUT,   
      URL_REPLACE_ON_REGION_HAVING_DATAPLOLICY_EMPTY,
      RestAPIsTestData.ORDER2_AS_JSON,
      HttpStatus.INTERNAL_SERVER_ERROR,
      null,
      true,
      UserType.ADMIN
    },
    { //20. Put?op=CAS, 200 OK testcase.
      HttpMethod.PUT,   
      URL_CAS_ON_KEY_1,
      RestAPIsTestData.ORDER_AS_CASJSON,
      HttpStatus.OK,
      KEY_1_LOCATION_HEADER,
      false,
      UserType.ADMIN
    },
    { //21. Put?op=CAS, 409 OK testcase.
      HttpMethod.PUT,   
      URL_CAS_ON_KEY_2,
      RestAPIsTestData.ORDER_AS_CASJSON,
      HttpStatus.CONFLICT,
      KEY_2_LOCATION_HEADER,
      true,
      UserType.ADMIN
    },
    { //22. Put?op=CAS, 400 Bad Request, Malformed JSOn
      HttpMethod.PUT,   
      URL_CAS_ON_KEY_2,
      RestAPIsTestData.MALFORMED_CAS_JSON, 
      HttpStatus.BAD_REQUEST,          
      null,
      true,
      UserType.ADMIN
    },
    { //23. Put?op=CAS, 404 Not Found, Region does not exist
      HttpMethod.PUT,   
      URL_CAS_ON_UNKNOWN_REGION,
      RestAPIsTestData.ORDER_AS_CASJSON, 
      HttpStatus.NOT_FOUND,        
      null,
      true,
      UserType.ADMIN
    },
    { //24. Put?op=cAs, 500 testcase, Gemfire exception
      HttpMethod.PUT,   
      URL_CAS_ON_REGION_HAVING_DATAPLOLICY_EMPTY,
      RestAPIsTestData.ORDER_AS_CASJSON,
      HttpStatus.INTERNAL_SERVER_ERROR,
      null,
      true,
      UserType.ADMIN
    },
    { //25. Get - List all regions/resources - 200 ok testcase
      HttpMethod.GET,   
      URL_LIST_ALL_REGIONS,
      null,
      HttpStatus.OK,
      RestTestUtils.GEMFIRE_REST_API_WEB_SERVICE_URL,
      true,
      UserType.ADMIN
    },
    { //26. List all regions/resources - 405 testcase.
      HttpMethod.POST,   
      URL_LIST_ALL_REGIONS,
      null,
      HttpStatus.METHOD_NOT_ALLOWED,
      null,
      false,
      UserType.ADMIN
    },   
    { //27. GetAll - read all data for region - 200 ok, Default test case [No limit param specified].
      HttpMethod.GET,   
      URL_GETALL_DEFAULT,
      null,
      HttpStatus.OK,
      null,
      true,
      UserType.ADMIN
    },
    { //28 GetAll - read all data on non-existing region - 404 NOT FOUND testcase.
      HttpMethod.GET,   
      URL_GETALL_ON_UNKNOWN_REGION,
      null,
      HttpStatus.NOT_FOUND,
      null,
      true,
      UserType.ADMIN
    },
    { //29 GetAll - read all data for region - limit=ALL testcase.
      HttpMethod.GET,   
      URL_GETALL_ALL_ENTRIES,
      null,
      HttpStatus.OK,
      null,
      true,
      UserType.ADMIN
    },
    { //30 GetAll - read data for fixed number of keys - limit=<NUMBER> testcase.
      HttpMethod.GET,   
      URL_GETALL_LIMITED_ENTRIES,
      null,
      HttpStatus.OK,
      null,
      true,
      UserType.ADMIN
    },
    { //31. Get keys - List all keys in region - 200 ok testcase
      HttpMethod.GET,   
      URL_LIST_KEYS,
      null,
      HttpStatus.OK,
      LIST_KEYS_LOCATION_HEADER, /*Location Header*/
      true,
      UserType.ADMIN
    },
    { //32. Get keys - List all keys for region which does not exist - 404 NOt Found testcase
      HttpMethod.GET,   
      URL_LIST_KEYS_ON_UNKNOWN_REGION,
      null,
      HttpStatus.NOT_FOUND,
      null,
      true,
      UserType.ADMIN
    },
    { //33. Get keys - 405 testcase, if any HTTP request method other than GET (e.g. only POST, NOT PUT, DELETE, as for them its a valid op) is used
      HttpMethod.POST,   
      URL_LIST_KEYS,
      null,
      HttpStatus.METHOD_NOT_ALLOWED,
      null,
      false,
      UserType.ADMIN
    },
    { //34. Read data for the specific keys. 200 Ok testcase.
      HttpMethod.GET,   
      URL_GET_ON_SPECIFIC_KEYS,
      null,
      HttpStatus.OK,
      null,
      true,
      UserType.ADMIN
    },
    {
    //35. Read data for the specific keys. 404 Ok testcase.
      HttpMethod.GET,   
      URL_GET_SPECIFIC_KEYS_DATA_ON_UNKNOWN_REGION,
      null,
      HttpStatus.NOT_FOUND,
      null,
      true,
      UserType.ADMIN
    },
    { //36. delete data for key in region. 200 Ok testcase
      HttpMethod.DELETE,   
      URL_DELETE_ON_KEY_1,
      null,
      HttpStatus.OK,
      URL_DELETE_ON_KEY_1, //location header
      false,
      UserType.ADMIN
    },
    { //37. delete data for key with non-existing region. 404 Not Found, testcase.
      HttpMethod.DELETE,   
      URL_DELETE_ON_UNKNOWN_REGION,
      null,
      HttpStatus.NOT_FOUND,
      null,
      true,
      UserType.ADMIN
    },
    { //38. delete data for key, 500 - Gemfire throws exception testcase.
      HttpMethod.DELETE,   
      URL_DELETE_ON_REGION_HAVING_DATAPLOLICY_EMPTY,
      null,
      HttpStatus.NOT_FOUND,
      null,
      true,
      UserType.ADMIN
    },
    { //39. delete data for set of keys, 200 Ok, testcase.
      HttpMethod.DELETE,   
      URL_DELETE_SPECIFIC_KEYS,
      null,
      HttpStatus.OK,
      null,
      false,
      UserType.ADMIN
    },
    { //40. delete data for set of keys, 404 Region NOT Found, testcase.
      HttpMethod.DELETE,   
      URL_DELETE_SPECIFIC_KEYS_ON_UNKNOWN_REGION,
      null,
      HttpStatus.NOT_FOUND,
      null,
      true,
      UserType.ADMIN
    },
    { //41. delete data for set of keys, 500 Gemfire throws exception testcase.
      HttpMethod.DELETE,   
      URL_DELETE_SPECIFIC_KEYS_ON_REGION_HAVING_DATAPLOLICY_EMPTY,
      null,
      HttpStatus.NOT_FOUND,
      null,
      true,
      UserType.ADMIN
    },
    { //42. create parameterized named query
      HttpMethod.POST,
      URL_CREATE_QUERY1,
      null,
      HttpStatus.CREATED,
      QUERY1_LOCATION_HEADER,
      false,
      UserType.ADMIN
    },
    { //43. create parameterized named query 
      HttpMethod.POST,
      URL_CREATE_QUERY2,
      null,
      HttpStatus.CREATED,
      QUERY2_LOCATION_HEADER,
      false,
      UserType.ADMIN
    },
    { //44. create parameterized named query
      HttpMethod.POST,
      URL_CREATE_QUERY3,
      null,
      HttpStatus.CREATED,
      QUERY3_LOCATION_HEADER,
      false,
      UserType.ADMIN
    },
    { //45. list all named/parameterized queries
      //NOTE: query result = 3. old index=8.
      HttpMethod.GET,
      URL_LIST_QUERIES,
      null,
      HttpStatus.OK,
      LIST_QUERIES_LOCATION_HEADER,
      true,
      UserType.ADMIN
    },
    { //46. Run the specified named query passing in args for query parameters in request body
      //Note: Query Result = 2, Old index=9
      HttpMethod.POST,
      URL_EXECUTE_QUERY1,
      RestAPIsTestData.QUERY_ARGS1,
      HttpStatus.OK,
      QUERY1_LOCATION_HEADER,
      true,
      UserType.ADMIN
    },
    { //47. Run the specified named query passing in args for query parameters in request body
      //Note: Query size = 1, old index = 10
      HttpMethod.POST,
      URL_EXECUTE_QUERY2,
      RestAPIsTestData.QUERY_ARGS2,
      HttpStatus.OK,
      QUERY2_LOCATION_HEADER,
      true,
      UserType.ADMIN
    },
    { //48. Run an unnamed (unidentified), ad-hoc query passed as a URL parameter
      HttpMethod.GET,
      URL_EXECUTE_ADHOC_QUERY1, 
      null,
      HttpStatus.OK,
      null,
      true,
      UserType.ADMIN
    },
    { //49. list all functions available in the GemFire cluster
      HttpMethod.GET,
      URL_LIST_FUNCTIONS,
      null,
      HttpStatus.OK,
      LIST_FUNCTIONS_LOCATION_HEADER,
      true,
      UserType.ADMIN
    },
    { //50. Execute function with args on availabl nodes in the GemFire cluster
      HttpMethod.POST,
      URL_EXECUTE_FUNCTION_ONREGION,
      RestAPIsTestData.FUNCTION_ARGS1,
      HttpStatus.OK,
      EXECUTE_FUNCTION_ONREGION_LOCATION_HEADER,
      true,
      UserType.ADMIN
    },
    { //51. create parameterized named query "testQuery"
      HttpMethod.POST,
      URL_CREATE_QUERY4,
      null,
      HttpStatus.CREATED,
      QUERY4_LOCATION_HEADER,
      false,
      UserType.ADMIN
    },
    { //52. update parameterized named query "testQuery"
      HttpMethod.PUT,
      URL_UPDATE_QUERY1_WITH_INVALID_OQL,
      null,
      HttpStatus.OK,
      null,
      false,
      UserType.ADMIN
    },
    { //53. Run the updated named query passing in args for query parameters in request body
      HttpMethod.POST,
      URL_EXECUTE_INVALID_QUERY,
      RestAPIsTestData.QUERY_ARGS1,
      HttpStatus.INTERNAL_SERVER_ERROR,
      null,
      true,
      UserType.ADMIN
    },
    { //54. update unknown parameterized named query 
      HttpMethod.PUT,
      URL_UPDATE_UNKNOWN_QUERY,
      null,
      HttpStatus.NOT_FOUND,
      null,
      true,
      UserType.ADMIN
    },
    { //55. DELETE parameterized named query with invalid queryString
      HttpMethod.DELETE,
      URL_DELETE_QUERY4,
      null,
      HttpStatus.OK,
      null,
      false,
      UserType.ADMIN
    },
    { //56. DELETE Non-existing parameterized named
      HttpMethod.DELETE,
      URL_DELETE_QUERY4,
      null,
      HttpStatus.NOT_FOUND,
      null,
      true,
      UserType.ADMIN
    },
    { //57. Ping the REST service using HTTP HEAD
      HttpMethod.HEAD,
      URL_PING_SERVICE,
      null,
      HttpStatus.OK,
      null,
      false,
      UserType.ADMIN
    },
    { //58. Ping the REST service using HTTP GET
      HttpMethod.GET,
      URL_PING_SERVICE,
      null,
      HttpStatus.OK,
      null,
      false,
      UserType.ADMIN
    },
    { //59. Get the total number of entries in region
      HttpMethod.HEAD,
      URL_GETALL_DEFAULT,
      null,
      HttpStatus.OK,
      null,
      false,
      UserType.ADMIN
    },
    { //60. create parameterized named query "testQuery", passing it in request-body 
      HttpMethod.POST,
      URL_CREATE_QUERY4_USING_REQUESTBODY,
      RestAPIsTestData.PARAMETERIZED_QUERIES[3][1],
      HttpStatus.CREATED,
      QUERY4_LOCATION_HEADER,
      false,
      UserType.ADMIN
    },
    { //61. update parameterized named query, passing it in request-body 
      HttpMethod.PUT,
      URL_UPDATE_QUERY4_USING_REQUESTBODY,
      RestAPIsTestData.PARAMETERIZED_QUERIES[4][1],
      HttpStatus.OK,
      null,
      false,
      UserType.ADMIN
    },
    { // 62. LIST parameterized queries
      HttpMethod.GET,
      URL_LIST_QUERIES,
      null,
      HttpStatus.OK,
      null,
      true,
      UserType.ADMIN
    },
    
    //FOr user type = reader
    { //63. create - 200 ok
      HttpMethod.POST,  
      URL_CREATE_ON_KEY_1,  
      RestAPIsTestData.ORDER1_AS_JSON, 
      HttpStatus.UNAUTHORIZED,               
      KEY_1_LOCATION_HEADER,
      false, 
      UserType.READER
    },
    { //64. create - 409 conflict
      HttpMethod.POST,  
      URL_CREATE_ON_KEY_1, 
      RestAPIsTestData.ORDER1_AS_JSON,  
      HttpStatus.UNAUTHORIZED,           
      KEY_1_LOCATION_HEADER,
      true,
      UserType.READER
    },
    { //65. Get data for key - 200 ok
      HttpMethod.GET,   
      URL_GET_ON_KEY_1,
      null,
      HttpStatus.OK,                    
      KEY_1_LOCATION_HEADER,
      true,
      UserType.READER
    },    
    { //66.  Put - 200 Ok, successful
      HttpMethod.PUT,   
      URL_PUT_ON_KEY_2, 
      RestAPIsTestData.ORDER2_AS_JSON,
      HttpStatus.UNAUTHORIZED,                    
      KEY_2_LOCATION_HEADER,  
      false,
      UserType.READER
    },    
    { //67. putAll - 200 Ok
      //TODO:Code complete for putAll authorize
      HttpMethod.PUT,   
      URL_PUTALL_ON_KEYS,
      RestAPIsTestData.CUSTOMER_LIST_AS_JSON,
      HttpStatus.OK,   
      null,
      false,
      UserType.READER
    },
    { //68. PUT?op=REPLACE, 200 Ok test case
      HttpMethod.PUT,
      URL_REPLACE_ON_KEY_2,
      RestAPIsTestData.ORDER2_UPDATED_AS_JSON,
      HttpStatus.UNAUTHORIZED,
      KEY_2_LOCATION_HEADER,
      false,
      UserType.READER             
    },
    { //69. Put?op=CAS, 200 OK testcase.
      HttpMethod.PUT,   
      URL_CAS_ON_KEY_1,
      RestAPIsTestData.ORDER_AS_CASJSON,
      HttpStatus.UNAUTHORIZED,
      KEY_1_LOCATION_HEADER,
      false,
      UserType.READER
    },
    { //70. Get - List all regions/resources - 200 ok testcase
      HttpMethod.GET,   
      URL_LIST_ALL_REGIONS,
      null,
      HttpStatus.OK,
      RestTestUtils.GEMFIRE_REST_API_WEB_SERVICE_URL,
      true,
      UserType.READER
    },   
    { //71. GetAll - read all data for region - 200 ok, Default test case [No limit param specified].
      HttpMethod.GET,   
      URL_GETALL_DEFAULT,
      null,
      HttpStatus.OK,
      null,
      true,
      UserType.READER
    },
    { //72 GetAll - read all data for region - limit=ALL testcase.
      HttpMethod.GET,   
      URL_GETALL_ALL_ENTRIES,
      null,
      HttpStatus.OK,
      null,
      true,
      UserType.READER
    },
    { //73 GetAll - read data for fixed number of keys - limit=<NUMBER> testcase.
      HttpMethod.GET,   
      URL_GETALL_LIMITED_ENTRIES,
      null,
      HttpStatus.OK,
      null,
      true,
      UserType.READER
    },
    { //74. Get keys - List all keys in region - 200 ok testcase
      HttpMethod.GET,   
      URL_LIST_KEYS,
      null,
      HttpStatus.OK,
      LIST_KEYS_LOCATION_HEADER, /*Location Header*/
      true,
      UserType.READER
    },
    { //75. Read data for the specific keys. 200 Ok testcase.
      HttpMethod.GET,   
      URL_GET_ON_SPECIFIC_KEYS,
      null,
      HttpStatus.OK,
      null,
      true,
      UserType.READER
    },
    { //76. delete data for key in region. 200 Ok testcase
      HttpMethod.DELETE,   
      URL_DELETE_ON_KEY_1,
      null,
      HttpStatus.UNAUTHORIZED,
      URL_DELETE_ON_KEY_1, //location header
      false,
      UserType.READER
    },
    { //77. delete data for set of keys, 200 Ok, testcase.
      HttpMethod.DELETE,   
      URL_DELETE_SPECIFIC_KEYS,
      null,
      HttpStatus.UNAUTHORIZED,
      null,
      false,
      UserType.READER
    },
    { //78. create parameterized named query
      HttpMethod.POST,
      URL_CREATE_QUERY1,
      null,
      HttpStatus.UNAUTHORIZED,
      QUERY1_LOCATION_HEADER,
      false,
      UserType.READER
    },
    { //79. list all named/parameterized queries
      //NOTE: query result = 3. old index=8.
      HttpMethod.GET,
      URL_LIST_QUERIES,
      null,
      HttpStatus.OK,
      LIST_QUERIES_LOCATION_HEADER,
      true,
      UserType.READER
    },
    { //80. Run the specified named query passing in args for query parameters in request body
      //Note: Query Result = 2, Old index=9
      HttpMethod.POST,
      URL_EXECUTE_QUERY2,
      RestAPIsTestData.QUERY_ARGS2,
      HttpStatus.OK,
      QUERY1_LOCATION_HEADER,
      true,
      UserType.READER
    },
    { //81. Run an unnamed (unidentified), ad-hoc query passed as a URL parameter
      HttpMethod.GET,
      URL_EXECUTE_ADHOC_QUERY1, 
      null,
      HttpStatus.OK,
      null,
      true,
      UserType.READER
    },
    { //82. list all functions available in the GemFire cluster
      HttpMethod.GET,
      URL_LIST_FUNCTIONS,
      null,
      HttpStatus.OK,
      LIST_FUNCTIONS_LOCATION_HEADER,
      true,
      UserType.READER
    },
    { //83. Execute function with args on availabl nodes in the GemFire cluster
      HttpMethod.POST,
      URL_EXECUTE_FUNCTION_ONREGION,
      RestAPIsTestData.FUNCTION_ARGS1,
      HttpStatus.OK,
      EXECUTE_FUNCTION_ONREGION_LOCATION_HEADER,
      true,
      UserType.READER
    },
    { //84. create parameterized named query "testQuery"
      HttpMethod.POST,
      URL_CREATE_QUERY4,
      null,
      HttpStatus.UNAUTHORIZED,
      QUERY4_LOCATION_HEADER,
      false,
      UserType.READER
    },
    { //85. update parameterized named query "testQuery"
      HttpMethod.PUT,
      URL_UPDATE_QUERY1_WITH_INVALID_OQL,
      null,
      HttpStatus.UNAUTHORIZED,
      null,
      false,
      UserType.READER
    },
    { //86. DELETE parameterized named query with invalid queryString
      HttpMethod.DELETE,
      URL_DELETE_QUERY4,
      null,
      HttpStatus.UNAUTHORIZED,
      null,
      false,
      UserType.READER
    },
    { //87. Ping the REST service using HTTP HEAD
      HttpMethod.HEAD,
      URL_PING_SERVICE,
      null,
      HttpStatus.OK,
      null,
      false,
      UserType.READER
    },
    { //88. Ping the REST service using HTTP GET
      HttpMethod.GET,
      URL_PING_SERVICE,
      null,
      HttpStatus.OK,
      null,
      false,
      UserType.READER
    },
    /*,
    { //89. Get the total number of entries in region
      HttpMethod.HEAD,
      URL_GETALL_DEFAULT,
      null,
      HttpStatus.OK,
      null,
      false,
      UserType.READER
    }
    */
    //usertype Reader ends
    
    //usertype Writer
    { //89. create - 200 ok
      HttpMethod.PUT,  
      URL_PUT_ON_KEY_2,  
      RestAPIsTestData.ORDER1_AS_JSON, 
      HttpStatus.OK,               
      KEY_1_LOCATION_HEADER,
      false, 
      UserType.WRITER
    },
    { //90. create - 409 conflict
      HttpMethod.POST,  
      URL_CREATE_ON_KEY_1, 
      RestAPIsTestData.ORDER1_AS_JSON,  
      HttpStatus.CONFLICT,           
      KEY_1_LOCATION_HEADER,
      true,
      UserType.WRITER
    },
    { //91. Get data for key - 200 ok
      HttpMethod.GET,   
      URL_GET_ON_KEY_1,
      null,
      HttpStatus.UNAUTHORIZED,                    
      KEY_1_LOCATION_HEADER,
      false,
      UserType.WRITER
    },    
    { //92.  Put - 200 Ok, successful
      HttpMethod.PUT,   
      URL_PUT_ON_KEY_2, 
      RestAPIsTestData.ORDER2_AS_JSON,
      HttpStatus.OK,                    
      KEY_2_LOCATION_HEADER,  
      false,
      UserType.WRITER
    },    
    { //93. putAll - 200 Ok
      //TODO:Code complete for putAll authorize
      HttpMethod.PUT,   
      URL_PUTALL_ON_KEYS,
      RestAPIsTestData.CUSTOMER_LIST_AS_JSON,
      HttpStatus.OK,   
      null,
      false,
      UserType.WRITER
    },
    { //94. PUT?op=REPLACE, 200 Ok test case
      HttpMethod.PUT,
      URL_REPLACE_ON_KEY_2,
      RestAPIsTestData.ORDER2_UPDATED_AS_JSON,
      HttpStatus.OK,
      KEY_2_LOCATION_HEADER,
      false,
      UserType.WRITER             
    },
    { //95. Put?op=CAS, 200 OK testcase.
      HttpMethod.PUT,   
      URL_CAS_ON_KEY_1,
      RestAPIsTestData.ORDER_AS_CASJSON,
      HttpStatus.CONFLICT,
      KEY_1_LOCATION_HEADER,
      false,
      UserType.WRITER
    },
    { //96. Get - List all regions/resources - 200 ok testcase
      HttpMethod.GET,   
      URL_LIST_ALL_REGIONS,
      null,
      HttpStatus.UNAUTHORIZED,
      RestTestUtils.GEMFIRE_REST_API_WEB_SERVICE_URL,
      false,
      UserType.WRITER
    },   
    { //97. GetAll - read all data for region - 200 ok, Default test case [No limit param specified].
      HttpMethod.GET,   
      URL_GETALL_DEFAULT,
      null,
      HttpStatus.UNAUTHORIZED,
      null,
      false,
      UserType.WRITER
    },
    { //98 GetAll - read all data for region - limit=ALL testcase.
      HttpMethod.GET,   
      URL_GETALL_ALL_ENTRIES,
      null,
      HttpStatus.UNAUTHORIZED,
      null,
      false,
      UserType.WRITER
    },
    { //99 GetAll - read data for fixed number of keys - limit=<NUMBER> testcase.
      HttpMethod.GET,   
      URL_GETALL_LIMITED_ENTRIES,
      null,
      HttpStatus.UNAUTHORIZED,
      null,
      false,
      UserType.WRITER
    },
    { //100. Get keys - List all keys in region - 200 ok testcase
      HttpMethod.GET,   
      URL_LIST_KEYS,
      null,
      HttpStatus.UNAUTHORIZED,
      LIST_KEYS_LOCATION_HEADER, /*Location Header*/
      false,
      UserType.WRITER
    },
    { //101. Read data for the specific keys. 200 Ok testcase.
      HttpMethod.GET,   
      URL_GET_ON_SPECIFIC_KEYS,
      null,
      HttpStatus.UNAUTHORIZED,
      null,
      false,
      UserType.WRITER
    },
    { //102. delete data for key in region. 200 Ok testcase
      HttpMethod.DELETE,   
      URL_DELETE_ON_KEY_1,
      null,
      HttpStatus.OK,
      URL_DELETE_ON_KEY_1, //location header
      false,
      UserType.WRITER
    },
    { //103. delete data for set of keys, 200 Ok, testcase.
      HttpMethod.DELETE,   
      URL_DELETE_SPECIFIC_KEYS,
      null,
      HttpStatus.OK,
      null,
      false,
      UserType.WRITER
    },
    { //104. create parameterized named query
      HttpMethod.POST,
      URL_CREATE_QUERY1,
      null,
      HttpStatus.CONFLICT,
      QUERY1_LOCATION_HEADER,
      true,
      UserType.WRITER
    },
    { //105. list all named/parameterized queries
      //NOTE: query result = 3. old index=8.
      HttpMethod.GET,
      URL_LIST_QUERIES,
      null,
      HttpStatus.UNAUTHORIZED,
      LIST_QUERIES_LOCATION_HEADER,
      false,
      UserType.WRITER
    },
    { //106. Run the specified named query passing in args for query parameters in request body
      //Note: Query Result = 2, Old index=9
      HttpMethod.POST,
      URL_EXECUTE_QUERY2,
      RestAPIsTestData.QUERY_ARGS2,
      HttpStatus.UNAUTHORIZED,
      QUERY1_LOCATION_HEADER,
      false,
      UserType.WRITER
    },
    { //107. Run an unnamed (unidentified), ad-hoc query passed as a URL parameter
      HttpMethod.GET,
      URL_EXECUTE_ADHOC_QUERY1, 
      null,
      HttpStatus.UNAUTHORIZED,
      null,
      false,
      UserType.WRITER
    },
    { //108. list all functions available in the GemFire cluster
      HttpMethod.GET,
      URL_LIST_FUNCTIONS,
      null,
      HttpStatus.UNAUTHORIZED,
      LIST_FUNCTIONS_LOCATION_HEADER,
      false,
      UserType.WRITER
    },
    { //109. Execute function with args on availabl nodes in the GemFire cluster
      HttpMethod.POST,
      URL_EXECUTE_FUNCTION_ONREGION,
      RestAPIsTestData.FUNCTION_ARGS1,
      HttpStatus.UNAUTHORIZED,
      EXECUTE_FUNCTION_ONREGION_LOCATION_HEADER,
      false,
      UserType.WRITER
    },
    { //110. create parameterized named query "testQuery"
      HttpMethod.POST,
      URL_CREATE_QUERY4,
      null,
      HttpStatus.CONFLICT,
      QUERY4_LOCATION_HEADER,
      true,
      UserType.WRITER
    },
    { //111. update parameterized named query "testQuery"
      HttpMethod.PUT,
      URL_UPDATE_QUERY1_WITH_INVALID_OQL,
      null,
      HttpStatus.OK,
      null,
      false,
      UserType.WRITER
    },
    { //112. DELETE parameterized named query with invalid queryString
      HttpMethod.DELETE,
      URL_DELETE_QUERY4,
      null,
      HttpStatus.OK,
      null,
      false,
      UserType.WRITER
    },
    { //113. Ping the REST service using HTTP HEAD
      HttpMethod.HEAD,
      URL_PING_SERVICE,
      null,
      HttpStatus.OK,
      null,
      false,
      UserType.WRITER
    },
    { //114. Ping the REST service using HTTP GET
      HttpMethod.GET,
      URL_PING_SERVICE,
      null,
      HttpStatus.OK,
      null,
      false,
      UserType.WRITER
    }//usertype Write ends here
  };
  
}
