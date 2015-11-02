package com.vmware.gemfire.tools.pulse.tests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import static org.junit.Assert.*;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class IntegrateSecUITest {
  
  private final static String jmxPropertiesFile = System.getProperty("pulse.propfile");
  private static String path = System.getProperty("pulse.war");
  private static Tomcat tomcat = null;
  private static Server server = null;
  private static String pulseURL = null;
  private static String logoutURL = null;
  private static String loginURL = null;
  public static WebDriver driver;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    try {      
      //Enable Integrated Security Profile
      System.setProperty("spring.profiles.active", "pulse.authentication.gemfire");
      //assumes jmx port is 1099 in pulse war file
      server = Server.createServer(1099, jmxPropertiesFile, true);
      String host = "localhost";// InetAddress.getLocalHost().getHostAddress();
      int port = 8080;
      String context = "/pulse";      
      tomcat = TomcatHelper.startTomcat(host, port, context, path);
      pulseURL = "http://" + host + ":" + port + context;
      logoutURL = "http://" + host + ":" + port  + context + "/pulse/clusterLogout";
      loginURL = "http://" + host + ":" + port  + context + "/Login.html";
      Thread.sleep(5000); // wait till tomcat settles down
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      fail("Error " + e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
      fail("Error " + e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
      fail("Error " + e.getMessage());
    }

    driver = new FirefoxDriver();
    driver.manage().window().maximize();
    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
    driver.get(pulseURL);  
  }
  
  /**
   * This test only tests test extensions (ie. properties file based testbed) written for integrate security
   *    1. PropsBackedAuthenticator
   *    2. AccessControlMbean
   * 
   * @throws IOException
   */
  @Test
  public void testServerAuthentication() throws IOException {
    JMXConnector jmxc = attemptConnect("dataRead", "dataRead" , true);
    jmxc.close();
    attemptConnect("dataRead", "dataRead12321" , false);
  }
  
  @Test
  public void testServerAuthorization() throws IOException, JMException {        
    try {
      JMXConnector cc = attemptConnect("dataRead", "dataRead" , true);
      testLevel(cc,"PULSE_DASHBOARD", true);
      testLevel(cc,"PULSE_DATABROWSER", false);
      cc.close();
      
      cc = attemptConnect("dataWrite", "dataWrite" , true);
      testLevel(cc,"PULSE_DASHBOARD", true);
      testLevel(cc,"PULSE_DATABROWSER", true);
      cc.close();
      
      cc = attemptConnect("admin", "admin" , true);
      testLevel(cc,"PULSE_DASHBOARD", true);
      testLevel(cc,"PULSE_DATABROWSER", true);
      cc.close();
      
    } catch (SecurityException e) {
      fail("Authentication failed " + e.getMessage());
    }
  }
  
  /**
   * Test pulse authentication through pulse login page and clusterLogout  
   */
  @Test
  public void testPulseAuthentication() throws InterruptedException {
    login("dataRead", "dataRead", true, true);
    login("dataRead", "dataRead1234", true, false);
    logout();
  }

  /**
   * Test pulse authorization through pulse data browser page  
   */
  @Test
  public void testPulseAuthorization() throws InterruptedException {
    login("dataWrite", "dataWrite", false,true);
    navigateToDataBrowerPage(true);    
    login("dataRead", "dataRead", true,true);
    navigateToDataBrowerPage(false);
  }
  
  private JMXConnector attemptConnect(String user, String password, boolean expectSuccess) throws IOException {
    String[] creds = { user, password };
    Map<String, Object> env = new HashMap<String, Object>();
    env.put(JMXConnector.CREDENTIALS, creds);
    try {
      JMXConnector cc = JMXConnectorFactory.connect(getURL(), env);
      MBeanServerConnection mbsc = cc.getMBeanServerConnection();
      if(!expectSuccess)
        fail("Expected Authentication to fail");
      return cc;
    } catch (SecurityException e) {
      if(expectSuccess)
        fail("Authentication failed " + e.getMessage());
    }
    return null;
  }
  
  private JMXServiceURL getURL() throws IOException {    
    return new JMXServiceURL("service:jmx:rmi://localhost/jndi/rmi://localhost:1099/jmxrmi");
  }
  
  private void testLevel(JMXConnector jmxc, String role, boolean expectTrue) throws IOException, JMException {
    MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();      
    ObjectName accObjName = new ObjectName(AccessControl.OBJECT_NAME_ACCESSCONTROL);
    boolean hasAccess = (Boolean)mbsc.invoke(accObjName, "authorize", 
        new Object[]{role}, new String[] {String.class.getName()});
    if(expectTrue) {
      assertTrue(hasAccess);
    }
    else {
      if(hasAccess)
        fail("Expected role "+ role + " rejection but user return true");
    }     
  }
  
  private void navigateToDataBrowerPage(boolean expectSuccess) {
    WebElement element = driver.findElement(By.linkText("Data Browser"));
    element.click();
    if(expectSuccess) {
      WebElement dbHeader = (new WebDriverWait(driver, 10))
          .until(new ExpectedCondition<WebElement>() {
            @Override
            public WebElement apply(WebDriver d) {
              return d.findElement(By.xpath("//*[@id=\"canvasWidth\"]/div[4]/div[1]/div[2]/label"));
            }
          });
      assertNotNull(dbHeader);      
    }
  }

  private void logout() {
    driver.get(logoutURL); 
    validateLoginPage();
  }
  
  private void validateSuccessfulLogin() {
    WebElement userNameOnPulsePage = (new WebDriverWait(driver, 10))
        .until(new ExpectedCondition<WebElement>() {
          @Override
          public WebElement apply(WebDriver d) {
            return d.findElement(By.id("userName"));
          }
        });
    assertNotNull(userNameOnPulsePage);
  }
  
  private void validateLoginPage() {
    WebElement userNameOnPulseLoginPage = (new WebDriverWait(driver, 10))
        .until(new ExpectedCondition<WebElement>() {
          @Override
          public WebElement apply(WebDriver d) {
            return d.findElement(By.id("user_name"));
          }
        });
    assertNotNull(userNameOnPulseLoginPage);
  }
  
  private void login(String userName, String password, boolean logoutFirst, boolean expectSuccess) throws InterruptedException {
    if(logoutFirst) {
      logout();
    }
    WebElement userNameElement = driver.findElement(By.id("user_name"));
    WebElement passwordElement = driver.findElement(By.id("user_password"));
    userNameElement.sendKeys(userName);
    passwordElement.sendKeys(password);
    passwordElement.submit();
    if(expectSuccess) {
      validateSuccessfulLogin();
    } else {
      //We expect login to be unsucceesful so it should go back to Login.html
      validateLoginPage();
    }    
  }
  
  
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    if(driver!=null)
      driver.close();
    try {
      if (tomcat != null) {
        tomcat.stop();
        tomcat.destroy();
      }
      System.out.println("Tomcat Stopped");
      if (server != null) {
        server.stop();
      }
      System.out.println("Server Stopped");
    } catch (LifecycleException e) {
      e.printStackTrace();
    }
  }

}