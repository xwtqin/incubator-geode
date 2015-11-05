package com.gemstone.gemfire.management.internal.security;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;

import javax.management.ObjectName;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.internal.AvailablePort;
import com.gemstone.gemfire.internal.cache.GemFireCacheImpl;
import com.gemstone.gemfire.management.internal.MBeanJMXAdapter;
import com.gemstone.gemfire.test.junit.categories.UnitTest;


/**
 * TODO : Add more tests for error-catch, different type of results etc
 * 
 * @author tushark
 *
 */
@Category(UnitTest.class)
public class HeadlessGfshJUnitTest {

  @SuppressWarnings({ "unused", "deprecation", "unused" })
  @Test
  public void testHeadlessGfshTest() throws ClassNotFoundException, IOException, InterruptedException {    
    GemFireCacheImpl cache = null;
    DistributedSystem ds = null;
    Properties pr = new Properties();
    pr.put("name", "testHeadlessGfshTest");
    pr.put(DistributionConfig.JMX_MANAGER_NAME, "true");
    pr.put(DistributionConfig.JMX_MANAGER_START_NAME, "true");
    int port = AvailablePort.getRandomAvailablePort(AvailablePort.SOCKET);
    pr.put(DistributionConfig.JMX_MANAGER_PORT_NAME, String.valueOf(port));
    pr.put(DistributionConfig.HTTP_SERVICE_PORT_NAME, "0");
    pr.put(DistributionConfig.MCAST_PORT_NAME,"0");
    
    ds = DistributedSystem.connect(pr);
    cache = (GemFireCacheImpl) CacheFactory.create(ds);
    ObjectName name = MBeanJMXAdapter.getDistributedSystemName();
    
    HeadlessGfsh gfsh = new HeadlessGfsh("Test",25);
    for(int i=0;i<5;i++) {
      gfsh.executeCommand("connect --jmx-manager=localhost["+port+"]");
      Object result = gfsh.getResult();
      assertTrue(gfsh.isConnectedAndReady());
      assertNotNull(result);
      gfsh.clear();
      gfsh.executeCommand("list members");
      result = gfsh.getResult();
      assertNotNull(result);
      gfsh.executeCommand("disconnect");
      gfsh.getResult();
    }
    
    long l1 = System.currentTimeMillis();
    gfsh.executeCommand("exit");
    long l2 = System.currentTimeMillis();
    gfsh.getResult();
    long l3 = System.currentTimeMillis();
    System.out.println("L3-l2="+ (l3-l2) + " Total time= " + (l3-l1)/1000);
    gfsh.terminate();    
    cache.close();
    ds.disconnect();
  }

}
