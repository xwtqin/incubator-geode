/*
 * =========================================================================
 *  Copyright (c) 2002-2014 Pivotal Software, Inc. All Rights Reserved.
 *  This product is protected by U.S. and international copyright
 *  and intellectual property laws. Pivotal products are covered by
 *  more patents listed at http://www.pivotal.io/patents.
 * ========================================================================
 */
package com.gemstone.gemfire.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import junit.framework.AssertionFailedError;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.distributed.internal.DistributionConfig;
import com.gemstone.gemfire.distributed.internal.DistributionConfigImpl;
import com.gemstone.gemfire.test.junit.categories.IntegrationTest;

/**
 * Test that DistributionConfigImpl handles SSL options correctly.
 * 
 */
@Category(IntegrationTest.class)
public class SSLConfigJUnitTest {

  private static final Properties SSL_PROPS_MAP     = new Properties();
  private static final Properties CLUSTER_SSL_PROPS_MAP     = new Properties();
  private static final Properties CLUSTER_SSL_PROPS_SUBSET_MAP     = new Properties();
  private static final Properties JMX_SSL_PROPS_MAP = new Properties();
  private static final Properties JMX_SSL_PROPS_SUBSET_MAP = new Properties();
  private static final Properties SERVER_SSL_PROPS_MAP = new Properties();
  private static final Properties SERVER_PROPS_SUBSET_MAP = new Properties();
  private static final Properties GATEWAY_SSL_PROPS_MAP = new Properties();
  private static final Properties GATEWAY_PROPS_SUBSET_MAP = new Properties();
  

  static {
    
    SSL_PROPS_MAP.put("javax.net.ssl.keyStoreType", "jks");
    SSL_PROPS_MAP.put("javax.net.ssl.keyStore", "/export/gemfire-configs/gemfire.keystore");
    SSL_PROPS_MAP.put("javax.net.ssl.keyStorePassword", "gemfire-key-password");
    SSL_PROPS_MAP.put("javax.net.ssl.trustStore", "/export/gemfire-configs/gemfire.truststore");
    SSL_PROPS_MAP.put("javax.net.ssl.trustStorePassword", "gemfire-trust-password");
    
    // SSL Properties for GemFire in-cluster connections
    CLUSTER_SSL_PROPS_MAP.put("cluster-ssl-keystore-type", "jks");
    CLUSTER_SSL_PROPS_MAP.put("cluster-ssl-keystore", "/export/gemfire-configs/gemfire.keystore");
    CLUSTER_SSL_PROPS_MAP.put("cluster-ssl-keystore-password", "gemfire-key-password");
    CLUSTER_SSL_PROPS_MAP.put("cluster-ssl-truststore", "/export/gemfire-configs/gemfire.truststore");
    CLUSTER_SSL_PROPS_MAP.put("cluster-ssl-truststore-password", "gemfire-trust-password");

     // Partially over-ridden SSL Properties for cluster
    CLUSTER_SSL_PROPS_SUBSET_MAP.put("cluster-ssl-keystore", "/export/gemfire-configs/gemfire.keystore");
    CLUSTER_SSL_PROPS_SUBSET_MAP.put("cluster-ssl-truststore", "/export/gemfire-configs/gemfire.truststore");
    
    // SSL Properties for GemFire JMX Manager connections
    JMX_SSL_PROPS_MAP.put("jmx-manager-ssl-keystore-type", "jks");
    JMX_SSL_PROPS_MAP.put("jmx-manager-ssl-keystore", "/export/gemfire-configs/manager.keystore");
    JMX_SSL_PROPS_MAP.put("jmx-manager-ssl-keystore-password", "manager-key-password");
    JMX_SSL_PROPS_MAP.put("jmx-manager-ssl-truststore", "/export/gemfire-configs/manager.truststore");
    JMX_SSL_PROPS_MAP.put("jmx-manager-ssl-truststore-password", "manager-trust-password");
    
    // SSL Properties for GemFire CacheServer connections
    SERVER_SSL_PROPS_MAP.put("server-ssl-keystore-type", "jks");
    SERVER_SSL_PROPS_MAP.put("server-ssl-keystore", "/export/gemfire-configs/cacheserver.keystore");
    SERVER_SSL_PROPS_MAP.put("server-ssl-keystore-password", "cacheserver-key-password");
    SERVER_SSL_PROPS_MAP.put("server-ssl-truststore", "/export/gemfire-configs/cacheserver.truststore");
    SERVER_SSL_PROPS_MAP.put("server-ssl-truststore-password", "cacheserver-trust-password");
    
   // SSL Properties for GemFire gateway connections
    GATEWAY_SSL_PROPS_MAP.put("gateway-ssl-keystore-type", "jks");
    GATEWAY_SSL_PROPS_MAP.put("gateway-ssl-keystore", "/export/gemfire-configs/gateway.keystore");
    GATEWAY_SSL_PROPS_MAP.put("gateway-ssl-keystore-password", "gateway-key-password");
    GATEWAY_SSL_PROPS_MAP.put("gateway-ssl-truststore", "/export/gemfire-configs/gateway.truststore");
    GATEWAY_SSL_PROPS_MAP.put("gateway-ssl-truststore-password", "gateway-trust-password");

    // Partially over-ridden SSL Properties for GemFire JMX Manager connections
    JMX_SSL_PROPS_SUBSET_MAP.put("jmx-manager-ssl-keystore", "/export/gemfire-configs/manager.keystore");
    JMX_SSL_PROPS_SUBSET_MAP.put("jmx-manager-ssl-truststore", "/export/gemfire-configs/manager.truststore");
    
    // Partially over-ridden SSL Properties for GemFire CacheServer connections
    SERVER_PROPS_SUBSET_MAP.put("server-ssl-keystore", "/export/gemfire-configs/cacheserver.keystore");
    SERVER_PROPS_SUBSET_MAP.put("server-ssl-truststore", "/export/gemfire-configs/cacheserver.truststore");
    
    // Partially over-ridden SSL Properties for GemFire gateway connections
    GATEWAY_PROPS_SUBSET_MAP.put("gateway-ssl-keystore", "/export/gemfire-configs/gateway.keystore");
    GATEWAY_PROPS_SUBSET_MAP.put("gateway-ssl-truststore", "/export/gemfire-configs/gateway.truststore");

  }
  
  //----- test methods ------

  @Test
  public void testMCastPortWithSSL() throws Exception {
    Properties props = new Properties( );
    // default mcast-port is not 0.
    props.setProperty( "ssl-enabled", "true" );
    try {
      new DistributionConfigImpl( props );
      fail("Expected IllegalArgumentException");
    } catch ( IllegalArgumentException e ) {
      if (! e.toString().matches( ".*Could not set \"ssl-enabled.*" ) ) {
        throw new Exception( "did not get expected exception, got this instead...", e );
      }
    }
  }
  
  @Test
  public void testMCastPortWithClusterSSL() throws Exception {
    Properties props = new Properties( );
    // default mcast-port is not 0.
    props.setProperty( "cluster-ssl-enabled", "true" );
    try {
      new DistributionConfigImpl( props );
      fail("Expected IllegalArgumentException");
    } catch ( IllegalArgumentException e ) {
      if (! e.toString().matches( ".*Could not set \"cluster-ssl-enabled.*" ) ) {
        throw new Exception( "did not get expected exception, got this instead...", e );
      }
    }
  }
  
  @Test
  public void testConfigCopyWithSSL( ) {
    DistributionConfigImpl config = new DistributionConfigImpl( new Properties() );
    isEqual( config.getSSLEnabled(), false );
    isEqual( config.getSSLProtocols(), "any" );
    isEqual( config.getSSLCiphers(), "any" );
    isEqual( config.getSSLRequireAuthentication(), true );
    
    Properties props = new Properties();
    props.setProperty("ssl-ciphers", "RSA_WITH_GARBAGE" );
    props.setProperty("ssl-protocols", "SSLv7" );
    props.setProperty("ssl-require-authentication", String.valueOf( false ) );
    props.setProperty("ssl-enabled", String.valueOf( true ) );
    props.setProperty("mcast-port", "0" );
    config = new DistributionConfigImpl( props );
    isEqual( config.getSSLEnabled(), true );
    isEqual( config.getSSLCiphers(), "RSA_WITH_GARBAGE" );
    isEqual( config.getSSLProtocols(), "SSLv7" );
    isEqual( config.getSSLRequireAuthentication(), false );
  }
  
  @Test
  /**
   * Make sure that the old ssl-* properties work correctly
   * with the new cluster-ssl-* properties.
   */
  public void testDeprecatedSSLWithCluster() {
    Properties props = new Properties();
    props.setProperty("ssl-ciphers", "RSA_WITH_GARBAGE" );
    props.setProperty("ssl-protocols", "SSLv7" );
    props.setProperty("ssl-require-authentication", String.valueOf( false ) );
    props.setProperty("ssl-enabled", String.valueOf( true ) );
    props.setProperty("mcast-port", "0" );
    DistributionConfigImpl config = new DistributionConfigImpl( props );
    isEqual( config.getClusterSSLEnabled(), true );
    isEqual( config.getClusterSSLCiphers(), "RSA_WITH_GARBAGE" );
    isEqual( config.getClusterSSLProtocols(), "SSLv7" );
    isEqual( config.getClusterSSLRequireAuthentication(), false );
    
    // now do the same thing but just set cluster-ssl-enabled
    props.setProperty("ssl-ciphers", "RSA_WITH_GARBAGE" );
    props.setProperty("ssl-protocols", "SSLv7" );
    props.setProperty("ssl-require-authentication", String.valueOf( false ) );
    props.setProperty("cluster-ssl-enabled", String.valueOf( true ) );
    props.setProperty("mcast-port", "0" );
    config = new DistributionConfigImpl( props );
    isEqual( config.getClusterSSLEnabled(), true );
    isEqual( config.getClusterSSLCiphers(), "RSA_WITH_GARBAGE" );
    isEqual( config.getClusterSSLProtocols(), "SSLv7" );
    isEqual( config.getClusterSSLRequireAuthentication(), false );
  }
  
  @Test
  public void testClusterSSL( ) throws Exception {
    Properties props = new Properties();
    props.setProperty("cluster-ssl-ciphers", "RSA_WITH_GARBAGE" );
    props.setProperty("cluster-ssl-protocols", "SSLv7" );
    props.setProperty("cluster-ssl-require-authentication", String.valueOf( false ) );
    props.setProperty("cluster-ssl-keystore", "clusterKeyStore");
    props.setProperty("cluster-ssl-keystore-type", "clusterKeyStoreType");
    props.setProperty("cluster-ssl-keystore-password", "clusterKeyStorePassword");
    props.setProperty("cluster-ssl-truststore", "clusterTrustStore");
    props.setProperty("cluster-ssl-truststore-password", "clusterTrustStorePassword");
    props.setProperty("javax.net.ssl.FOO", "BAR");
    props.setProperty("cluster-ssl-enabled", String.valueOf( true ) );
    props.setProperty("mcast-port", "0" );
    DistributionConfigImpl config = new DistributionConfigImpl( props );
    isEqual( config.getClusterSSLEnabled(), true );
    isEqual( config.getClusterSSLCiphers(), "RSA_WITH_GARBAGE" );
    isEqual( config.getClusterSSLProtocols(), "SSLv7" );
    isEqual( config.getClusterSSLRequireAuthentication(), false );
    isEqual( config.getClusterSSLKeyStore(), "clusterKeyStore" );
    isEqual( config.getClusterSSLKeyStoreType(), "clusterKeyStoreType" );
    isEqual( config.getClusterSSLKeyStorePassword(), "clusterKeyStorePassword" );
    isEqual( config.getClusterSSLTrustStore(), "clusterTrustStore" );
    isEqual( config.getClusterSSLTrustStorePassword(), "clusterTrustStorePassword" );
    Properties expectedSSLProps = new Properties();
    expectedSSLProps.setProperty("javax.net.ssl.keyStore", "clusterKeyStore");
    expectedSSLProps.setProperty("javax.net.ssl.keyStoreType", "clusterKeyStoreType");
    expectedSSLProps.setProperty("javax.net.ssl.keyStorePassword", "clusterKeyStorePassword");
    expectedSSLProps.setProperty("javax.net.ssl.trustStore", "clusterTrustStore");
    expectedSSLProps.setProperty("javax.net.ssl.trustStorePassword", "clusterTrustStorePassword");
    expectedSSLProps.setProperty("javax.net.ssl.FOO", "BAR");
    isEqual( expectedSSLProps, config.getClusterSSLProperties() );
  }
  
  @Test
  public void testServerSSL( ) throws Exception {
    Properties props = new Properties();
    props.setProperty("server-ssl-ciphers", "RSA_WITH_GARBAGE" );
    props.setProperty("server-ssl-protocols", "SSLv7" );
    props.setProperty("server-ssl-require-authentication", String.valueOf( false ) );
    props.setProperty("server-ssl-keystore", "serverKeyStore");
    props.setProperty("server-ssl-keystore-type", "serverKeyStoreType");
    props.setProperty("server-ssl-keystore-password", "serverKeyStorePassword");
    props.setProperty("server-ssl-truststore", "serverTrustStore");
    props.setProperty("server-ssl-truststore-password", "serverTrustStorePassword");
    props.setProperty("javax.net.ssl.FOO", "BAR");
    props.setProperty("server-ssl-enabled", String.valueOf( true ) );
    props.setProperty("mcast-port", "0" );
    DistributionConfigImpl config = new DistributionConfigImpl( props );
    isEqual( config.getServerSSLEnabled(), true );
    isEqual( config.getServerSSLCiphers(), "RSA_WITH_GARBAGE" );
    isEqual( config.getServerSSLProtocols(), "SSLv7" );
    isEqual( config.getServerSSLRequireAuthentication(), false );
    isEqual( config.getServerSSLKeyStore(), "serverKeyStore" );
    isEqual( config.getServerSSLKeyStoreType(), "serverKeyStoreType" );
    isEqual( config.getServerSSLKeyStorePassword(), "serverKeyStorePassword" );
    isEqual( config.getServerSSLTrustStore(), "serverTrustStore" );
    isEqual( config.getServerSSLTrustStorePassword(), "serverTrustStorePassword" );
    Properties expectedSSLProps = new Properties();
    expectedSSLProps.setProperty("javax.net.ssl.keyStore", "serverKeyStore");
    expectedSSLProps.setProperty("javax.net.ssl.keyStoreType", "serverKeyStoreType");
    expectedSSLProps.setProperty("javax.net.ssl.keyStorePassword", "serverKeyStorePassword");
    expectedSSLProps.setProperty("javax.net.ssl.trustStore", "serverTrustStore");
    expectedSSLProps.setProperty("javax.net.ssl.trustStorePassword", "serverTrustStorePassword");
    expectedSSLProps.setProperty("javax.net.ssl.FOO", "BAR");
    isEqual( expectedSSLProps, config.getServerSSLProperties() );
  }
  
  @Test
  public void testGatewaySSL( ) throws Exception {
    Properties props = new Properties();
    props.setProperty("gateway-ssl-ciphers", "RSA_WITH_GARBAGE" );
    props.setProperty("gateway-ssl-protocols", "SSLv7" );
    props.setProperty("gateway-ssl-require-authentication", String.valueOf( false ) );
    props.setProperty("gateway-ssl-keystore", "gatewayKeyStore");
    props.setProperty("gateway-ssl-keystore-type", "gatewayKeyStoreType");
    props.setProperty("gateway-ssl-keystore-password", "gatewayKeyStorePassword");
    props.setProperty("gateway-ssl-truststore", "gatewayTrustStore");
    props.setProperty("gateway-ssl-truststore-password", "gatewayTrustStorePassword");
    props.setProperty("javax.net.ssl.FOO", "BAR");
    props.setProperty("gateway-ssl-enabled", String.valueOf( true ) );
    props.setProperty("mcast-port", "0" );
    DistributionConfigImpl config = new DistributionConfigImpl( props );
    isEqual( config.getGatewaySSLEnabled(), true );
    isEqual( config.getGatewaySSLCiphers(), "RSA_WITH_GARBAGE" );
    isEqual( config.getGatewaySSLProtocols(), "SSLv7" );
    isEqual( config.getGatewaySSLRequireAuthentication(), false );
    isEqual( config.getGatewaySSLKeyStore(), "gatewayKeyStore" );
    isEqual( config.getGatewaySSLKeyStoreType(), "gatewayKeyStoreType" );
    isEqual( config.getGatewaySSLKeyStorePassword(), "gatewayKeyStorePassword" );
    isEqual( config.getGatewaySSLTrustStore(), "gatewayTrustStore" );
    isEqual( config.getGatewaySSLTrustStorePassword(), "gatewayTrustStorePassword" );
    Properties expectedSSLProps = new Properties();
    expectedSSLProps.setProperty("javax.net.ssl.keyStore", "gatewayKeyStore");
    expectedSSLProps.setProperty("javax.net.ssl.keyStoreType", "gatewayKeyStoreType");
    expectedSSLProps.setProperty("javax.net.ssl.keyStorePassword", "gatewayKeyStorePassword");
    expectedSSLProps.setProperty("javax.net.ssl.trustStore", "gatewayTrustStore");
    expectedSSLProps.setProperty("javax.net.ssl.trustStorePassword", "gatewayTrustStorePassword");
    expectedSSLProps.setProperty("javax.net.ssl.FOO", "BAR");
    isEqual( expectedSSLProps, config.getGatewaySSLProperties() );
  }
  
  @Test
  public void testJmxManagerSSL( ) throws Exception {
    Properties props = new Properties();
    props.setProperty("jmx-manager-ssl-ciphers", "RSA_WITH_GARBAGE" );
    props.setProperty("jmx-manager-ssl-protocols", "SSLv7" );
    props.setProperty("jmx-manager-ssl-require-authentication", String.valueOf( false ) );
    props.setProperty("jmx-manager-ssl-keystore", "jmx-managerKeyStore");
    props.setProperty("jmx-manager-ssl-keystore-type", "jmx-managerKeyStoreType");
    props.setProperty("jmx-manager-ssl-keystore-password", "jmx-managerKeyStorePassword");
    props.setProperty("jmx-manager-ssl-truststore", "jmx-managerTrustStore");
    props.setProperty("jmx-manager-ssl-truststore-password", "jmx-managerTrustStorePassword");
    props.setProperty("javax.net.ssl.FOO", "BAR");
    props.setProperty("jmx-manager-ssl-enabled", String.valueOf( true ) );
    props.setProperty("mcast-port", "0" );
    DistributionConfigImpl config = new DistributionConfigImpl( props );
    isEqual( config.getJmxManagerSSLEnabled(), true );
    isEqual( config.getJmxManagerSSLCiphers(), "RSA_WITH_GARBAGE" );
    isEqual( config.getJmxManagerSSLProtocols(), "SSLv7" );
    isEqual( config.getJmxManagerSSLRequireAuthentication(), false );
    isEqual( config.getJmxManagerSSLKeyStore(), "jmx-managerKeyStore" );
    isEqual( config.getJmxManagerSSLKeyStoreType(), "jmx-managerKeyStoreType" );
    isEqual( config.getJmxManagerSSLKeyStorePassword(), "jmx-managerKeyStorePassword" );
    isEqual( config.getJmxManagerSSLTrustStore(), "jmx-managerTrustStore" );
    isEqual( config.getJmxManagerSSLTrustStorePassword(), "jmx-managerTrustStorePassword" );
    Properties expectedSSLProps = new Properties();
    expectedSSLProps.setProperty("javax.net.ssl.keyStore", "jmx-managerKeyStore");
    expectedSSLProps.setProperty("javax.net.ssl.keyStoreType", "jmx-managerKeyStoreType");
    expectedSSLProps.setProperty("javax.net.ssl.keyStorePassword", "jmx-managerKeyStorePassword");
    expectedSSLProps.setProperty("javax.net.ssl.trustStore", "jmx-managerTrustStore");
    expectedSSLProps.setProperty("javax.net.ssl.trustStorePassword", "jmx-managerTrustStorePassword");
    expectedSSLProps.setProperty("javax.net.ssl.FOO", "BAR");
    isEqual( expectedSSLProps, config.getJmxSSLProperties() );
  }
  
  @Test
  public void testHttpServiceSSL( ) throws Exception {
    Properties props = new Properties();
    props.setProperty("http-service-ssl-ciphers", "RSA_WITH_GARBAGE" );
    props.setProperty("http-service-ssl-protocols", "SSLv7" );
    props.setProperty("http-service-ssl-require-authentication", String.valueOf( false ) );
    props.setProperty("http-service-ssl-keystore", "http-serviceKeyStore");
    props.setProperty("http-service-ssl-keystore-type", "http-serviceKeyStoreType");
    props.setProperty("http-service-ssl-keystore-password", "http-serviceKeyStorePassword");
    props.setProperty("http-service-ssl-truststore", "http-serviceTrustStore");
    props.setProperty("http-service-ssl-truststore-password", "http-serviceTrustStorePassword");
    props.setProperty("javax.net.ssl.FOO", "BAR");
    props.setProperty("http-service-ssl-enabled", String.valueOf( true ) );
    props.setProperty("mcast-port", "0" );
    DistributionConfigImpl config = new DistributionConfigImpl( props );
    isEqual( config.getHttpServiceSSLEnabled(), true );
    isEqual( config.getHttpServiceSSLCiphers(), "RSA_WITH_GARBAGE" );
    isEqual( config.getHttpServiceSSLProtocols(), "SSLv7" );
    isEqual( config.getHttpServiceSSLRequireAuthentication(), false );
    isEqual( config.getHttpServiceSSLKeyStore(), "http-serviceKeyStore" );
    isEqual( config.getHttpServiceSSLKeyStoreType(), "http-serviceKeyStoreType" );
    isEqual( config.getHttpServiceSSLKeyStorePassword(), "http-serviceKeyStorePassword" );
    isEqual( config.getHttpServiceSSLTrustStore(), "http-serviceTrustStore" );
    isEqual( config.getHttpServiceSSLTrustStorePassword(), "http-serviceTrustStorePassword" );
    Properties expectedSSLProps = new Properties();
    expectedSSLProps.setProperty("javax.net.ssl.keyStore", "http-serviceKeyStore");
    expectedSSLProps.setProperty("javax.net.ssl.keyStoreType", "http-serviceKeyStoreType");
    expectedSSLProps.setProperty("javax.net.ssl.keyStorePassword", "http-serviceKeyStorePassword");
    expectedSSLProps.setProperty("javax.net.ssl.trustStore", "http-serviceTrustStore");
    expectedSSLProps.setProperty("javax.net.ssl.trustStorePassword", "http-serviceTrustStorePassword");
    expectedSSLProps.setProperty("javax.net.ssl.FOO", "BAR");
    isEqual( expectedSSLProps, config.getHttpServiceSSLProperties() );
  }
  
  /**
   * Test that a javax.net.ssl.* property has lower precedence than the corresponding cluster-ssl-* property
   */
  @Test
  public void testJavaxDoesNotOverrideCluster() {
    Properties props = new Properties();
    props.setProperty("cluster-ssl-keystore", "clusterKeyStore");
    props.setProperty("cluster-ssl-keystore-type", "clusterKeyStoreType");
    props.setProperty("cluster-ssl-keystore-password", "clusterKeyStorePassword");
    props.setProperty("cluster-ssl-truststore", "clusterTrustStore");
    props.setProperty("cluster-ssl-truststore-password", "clusterTrustStorePassword");
    props.setProperty("javax.net.ssl.keyStore", "BOGUS");
    props.setProperty("javax.net.ssl.keyStoreType", "BOGUS");
    props.setProperty("javax.net.ssl.keyStorePassword", "BOGUS");
    props.setProperty("javax.net.ssl.trustStore", "BOGUS");
    props.setProperty("javax.net.ssl.trustStorePassword", "BOGUS");
    props.setProperty("cluster-ssl-enabled", String.valueOf( true ) );
    props.setProperty("mcast-port", "0" );
    DistributionConfigImpl config = new DistributionConfigImpl( props );
    isEqual( config.getClusterSSLEnabled(), true );
    isEqual( config.getClusterSSLKeyStore(), "clusterKeyStore" );
    isEqual( config.getClusterSSLKeyStoreType(), "clusterKeyStoreType" );
    isEqual( config.getClusterSSLKeyStorePassword(), "clusterKeyStorePassword" );
    isEqual( config.getClusterSSLTrustStore(), "clusterTrustStore" );
    isEqual( config.getClusterSSLTrustStorePassword(), "clusterTrustStorePassword" );
    Properties expectedSSLProps = new Properties();
    expectedSSLProps.setProperty("javax.net.ssl.keyStore", "clusterKeyStore");
    expectedSSLProps.setProperty("javax.net.ssl.keyStoreType", "clusterKeyStoreType");
    expectedSSLProps.setProperty("javax.net.ssl.keyStorePassword", "clusterKeyStorePassword");
    expectedSSLProps.setProperty("javax.net.ssl.trustStore", "clusterTrustStore");
    expectedSSLProps.setProperty("javax.net.ssl.trustStorePassword", "clusterTrustStorePassword");
    isEqual( expectedSSLProps, config.getClusterSSLProperties() );
  }

  /**
   * Test that a javax.net.ssl.* property has lower precedence than the corresponding server-ssl-* property
   */
  @Test
  public void testJavaxDoesNotOverrideServer() {
    Properties props = new Properties();
    props.setProperty("server-ssl-keystore", "serverKeyStore");
    props.setProperty("server-ssl-keystore-type", "serverKeyStoreType");
    props.setProperty("server-ssl-keystore-password", "serverKeyStorePassword");
    props.setProperty("server-ssl-truststore", "serverTrustStore");
    props.setProperty("server-ssl-truststore-password", "serverTrustStorePassword");
    props.setProperty("javax.net.ssl.keyStore", "BOGUS");
    props.setProperty("javax.net.ssl.keyStoreType", "BOGUS");
    props.setProperty("javax.net.ssl.keyStorePassword", "BOGUS");
    props.setProperty("javax.net.ssl.trustStore", "BOGUS");
    props.setProperty("javax.net.ssl.trustStorePassword", "BOGUS");
    props.setProperty("server-ssl-enabled", String.valueOf( true ) );
    props.setProperty("mcast-port", "0" );
    DistributionConfigImpl config = new DistributionConfigImpl( props );
    isEqual( config.getServerSSLEnabled(), true );
    isEqual( config.getServerSSLKeyStore(), "serverKeyStore" );
    isEqual( config.getServerSSLKeyStoreType(), "serverKeyStoreType" );
    isEqual( config.getServerSSLKeyStorePassword(), "serverKeyStorePassword" );
    isEqual( config.getServerSSLTrustStore(), "serverTrustStore" );
    isEqual( config.getServerSSLTrustStorePassword(), "serverTrustStorePassword" );
    Properties expectedSSLProps = new Properties();
    expectedSSLProps.setProperty("javax.net.ssl.keyStore", "serverKeyStore");
    expectedSSLProps.setProperty("javax.net.ssl.keyStoreType", "serverKeyStoreType");
    expectedSSLProps.setProperty("javax.net.ssl.keyStorePassword", "serverKeyStorePassword");
    expectedSSLProps.setProperty("javax.net.ssl.trustStore", "serverTrustStore");
    expectedSSLProps.setProperty("javax.net.ssl.trustStorePassword", "serverTrustStorePassword");
    isEqual( expectedSSLProps, config.getServerSSLProperties() );
  }

  /**
   * Test that a javax.net.ssl.* property has lower precedence than the corresponding gateway-ssl-* property
   */
  @Test
  public void testJavaxDoesNotOverrideGateway() {
    Properties props = new Properties();
    props.setProperty("gateway-ssl-keystore", "gatewayKeyStore");
    props.setProperty("gateway-ssl-keystore-type", "gatewayKeyStoreType");
    props.setProperty("gateway-ssl-keystore-password", "gatewayKeyStorePassword");
    props.setProperty("gateway-ssl-truststore", "gatewayTrustStore");
    props.setProperty("gateway-ssl-truststore-password", "gatewayTrustStorePassword");
    props.setProperty("javax.net.ssl.keyStore", "BOGUS");
    props.setProperty("javax.net.ssl.keyStoreType", "BOGUS");
    props.setProperty("javax.net.ssl.keyStorePassword", "BOGUS");
    props.setProperty("javax.net.ssl.trustStore", "BOGUS");
    props.setProperty("javax.net.ssl.trustStorePassword", "BOGUS");
    props.setProperty("gateway-ssl-enabled", String.valueOf( true ) );
    props.setProperty("mcast-port", "0" );
    DistributionConfigImpl config = new DistributionConfigImpl( props );
    isEqual( config.getGatewaySSLEnabled(), true );
    isEqual( config.getGatewaySSLKeyStore(), "gatewayKeyStore" );
    isEqual( config.getGatewaySSLKeyStoreType(), "gatewayKeyStoreType" );
    isEqual( config.getGatewaySSLKeyStorePassword(), "gatewayKeyStorePassword" );
    isEqual( config.getGatewaySSLTrustStore(), "gatewayTrustStore" );
    isEqual( config.getGatewaySSLTrustStorePassword(), "gatewayTrustStorePassword" );
    Properties expectedSSLProps = new Properties();
    expectedSSLProps.setProperty("javax.net.ssl.keyStore", "gatewayKeyStore");
    expectedSSLProps.setProperty("javax.net.ssl.keyStoreType", "gatewayKeyStoreType");
    expectedSSLProps.setProperty("javax.net.ssl.keyStorePassword", "gatewayKeyStorePassword");
    expectedSSLProps.setProperty("javax.net.ssl.trustStore", "gatewayTrustStore");
    expectedSSLProps.setProperty("javax.net.ssl.trustStorePassword", "gatewayTrustStorePassword");
    isEqual( expectedSSLProps, config.getGatewaySSLProperties() );
  }

  /**
   * Test that a javax.net.ssl.* property has lower precedence than the corresponding jmx-manager-ssl-* property
   */
  @Test
  public void testJavaxDoesNotOverrideJmxManager() {
    Properties props = new Properties();
    props.setProperty("jmx-manager-ssl-keystore", "jmx-managerKeyStore");
    props.setProperty("jmx-manager-ssl-keystore-type", "jmx-managerKeyStoreType");
    props.setProperty("jmx-manager-ssl-keystore-password", "jmx-managerKeyStorePassword");
    props.setProperty("jmx-manager-ssl-truststore", "jmx-managerTrustStore");
    props.setProperty("jmx-manager-ssl-truststore-password", "jmx-managerTrustStorePassword");
    props.setProperty("javax.net.ssl.keyStore", "BOGUS");
    props.setProperty("javax.net.ssl.keyStoreType", "BOGUS");
    props.setProperty("javax.net.ssl.keyStorePassword", "BOGUS");
    props.setProperty("javax.net.ssl.trustStore", "BOGUS");
    props.setProperty("javax.net.ssl.trustStorePassword", "BOGUS");
    props.setProperty("jmx-manager-ssl-enabled", String.valueOf( true ) );
    props.setProperty("mcast-port", "0" );
    DistributionConfigImpl config = new DistributionConfigImpl( props );
    isEqual( config.getJmxManagerSSLEnabled(), true );
    isEqual( config.getJmxManagerSSLKeyStore(), "jmx-managerKeyStore" );
    isEqual( config.getJmxManagerSSLKeyStoreType(), "jmx-managerKeyStoreType" );
    isEqual( config.getJmxManagerSSLKeyStorePassword(), "jmx-managerKeyStorePassword" );
    isEqual( config.getJmxManagerSSLTrustStore(), "jmx-managerTrustStore" );
    isEqual( config.getJmxManagerSSLTrustStorePassword(), "jmx-managerTrustStorePassword" );
    Properties expectedSSLProps = new Properties();
    expectedSSLProps.setProperty("javax.net.ssl.keyStore", "jmx-managerKeyStore");
    expectedSSLProps.setProperty("javax.net.ssl.keyStoreType", "jmx-managerKeyStoreType");
    expectedSSLProps.setProperty("javax.net.ssl.keyStorePassword", "jmx-managerKeyStorePassword");
    expectedSSLProps.setProperty("javax.net.ssl.trustStore", "jmx-managerTrustStore");
    expectedSSLProps.setProperty("javax.net.ssl.trustStorePassword", "jmx-managerTrustStorePassword");
    isEqual( expectedSSLProps, config.getJmxSSLProperties() );
  }

  /**
   * Test that a javax.net.ssl.* property has lower precedence than the corresponding http-service-ssl-* property
   */
  @Test
  public void testJavaxDoesNotOverrideHttpService() {
    Properties props = new Properties();
    props.setProperty("http-service-ssl-keystore", "http-serviceKeyStore");
    props.setProperty("http-service-ssl-keystore-type", "http-serviceKeyStoreType");
    props.setProperty("http-service-ssl-keystore-password", "http-serviceKeyStorePassword");
    props.setProperty("http-service-ssl-truststore", "http-serviceTrustStore");
    props.setProperty("http-service-ssl-truststore-password", "http-serviceTrustStorePassword");
    props.setProperty("javax.net.ssl.keyStore", "BOGUS");
    props.setProperty("javax.net.ssl.keyStoreType", "BOGUS");
    props.setProperty("javax.net.ssl.keyStorePassword", "BOGUS");
    props.setProperty("javax.net.ssl.trustStore", "BOGUS");
    props.setProperty("javax.net.ssl.trustStorePassword", "BOGUS");
    props.setProperty("http-service-ssl-enabled", String.valueOf( true ) );
    props.setProperty("mcast-port", "0" );
    DistributionConfigImpl config = new DistributionConfigImpl( props );
    isEqual( config.getHttpServiceSSLEnabled(), true );
    isEqual( config.getHttpServiceSSLKeyStore(), "http-serviceKeyStore" );
    isEqual( config.getHttpServiceSSLKeyStoreType(), "http-serviceKeyStoreType" );
    isEqual( config.getHttpServiceSSLKeyStorePassword(), "http-serviceKeyStorePassword" );
    isEqual( config.getHttpServiceSSLTrustStore(), "http-serviceTrustStore" );
    isEqual( config.getHttpServiceSSLTrustStorePassword(), "http-serviceTrustStorePassword" );
    Properties expectedSSLProps = new Properties();
    expectedSSLProps.setProperty("javax.net.ssl.keyStore", "http-serviceKeyStore");
    expectedSSLProps.setProperty("javax.net.ssl.keyStoreType", "http-serviceKeyStoreType");
    expectedSSLProps.setProperty("javax.net.ssl.keyStorePassword", "http-serviceKeyStorePassword");
    expectedSSLProps.setProperty("javax.net.ssl.trustStore", "http-serviceTrustStore");
    expectedSSLProps.setProperty("javax.net.ssl.trustStorePassword", "http-serviceTrustStorePassword");
    isEqual( expectedSSLProps, config.getHttpServiceSSLProperties() );
  }

  @Test
  public void testClusterDefaultConfig() throws Exception {
    DistributionConfigImpl config = new DistributionConfigImpl( new Properties() );
    isEqual( config.getClusterSSLEnabled(), false );
    isEqual( config.getClusterSSLProtocols(), "any" );
    isEqual( config.getClusterSSLCiphers(), "any" );
    isEqual( config.getClusterSSLRequireAuthentication(), true );
    isEqual( config.getClusterSSLKeyStore(), "" );
    isEqual( config.getClusterSSLKeyStoreType(), "" );
    isEqual( config.getClusterSSLKeyStorePassword(), "" );
    isEqual( config.getClusterSSLTrustStore(), "" );
    isEqual( config.getClusterSSLTrustStorePassword(), "" );
    isEqual( config.getClusterSSLProperties(), new Properties() );
  }

  @Test
  public void testManagerDefaultConfig() throws Exception {
    DistributionConfigImpl config = new DistributionConfigImpl( new Properties() );
    isEqual( config.getJmxManagerSSLEnabled(), false );
    isEqual( config.getJmxManagerSSLProtocols(), "any" );
    isEqual( config.getJmxManagerSSLCiphers(), "any" );
    isEqual( config.getJmxManagerSSLRequireAuthentication(), true );
    isEqual( config.getJmxManagerSSLKeyStore(), "" );
    isEqual( config.getJmxManagerSSLKeyStoreType(), "" );
    isEqual( config.getJmxManagerSSLKeyStorePassword(), "" );
    isEqual( config.getJmxManagerSSLTrustStore(), "" );
    isEqual( config.getJmxManagerSSLTrustStorePassword(), "" );
    isEqual( config.getJmxSSLProperties(), new Properties() );
  }
  
  @Test
  public void testCacheServerDefaultConfig() throws Exception {
    DistributionConfigImpl config = new DistributionConfigImpl( new Properties() );
    isEqual( config.getServerSSLEnabled(), false );
    isEqual( config.getServerSSLProtocols(), "any" );
    isEqual( config.getServerSSLCiphers(), "any" );
    isEqual( config.getServerSSLRequireAuthentication(), true );
    isEqual( config.getServerSSLKeyStore(), "" );
    isEqual( config.getServerSSLKeyStoreType(), "" );
    isEqual( config.getServerSSLKeyStorePassword(), "" );
    isEqual( config.getServerSSLTrustStore(), "" );
    isEqual( config.getServerSSLTrustStorePassword(), "" );
    isEqual( config.getServerSSLProperties(), new Properties() );
  }
  
  @Test
  public void testGatewayDefaultConfig() throws Exception {
    DistributionConfigImpl config = new DistributionConfigImpl( new Properties() );
    isEqual( config.getGatewaySSLEnabled(), false );
    isEqual( config.getGatewaySSLProtocols(), "any" );
    isEqual( config.getGatewaySSLCiphers(), "any" );
    isEqual( config.getGatewaySSLRequireAuthentication(), true );
    isEqual( config.getGatewaySSLKeyStore(), "" );
    isEqual( config.getGatewaySSLKeyStoreType(), "" );
    isEqual( config.getGatewaySSLKeyStorePassword(), "" );
    isEqual( config.getGatewaySSLTrustStore(), "" );
    isEqual( config.getGatewaySSLTrustStorePassword(), "" );
    isEqual( config.getGatewaySSLProperties(), new Properties() );
  }
  
  @Test
  public void testHttpServiceDefaultConfig() throws Exception {
    DistributionConfigImpl config = new DistributionConfigImpl( new Properties() );
    isEqual( config.getHttpServiceSSLEnabled(), false );
    isEqual( config.getHttpServiceSSLProtocols(), "any" );
    isEqual( config.getHttpServiceSSLCiphers(), "any" );
    isEqual( config.getHttpServiceSSLRequireAuthentication(), false );
    isEqual( config.getHttpServiceSSLKeyStore(), "" );
    isEqual( config.getHttpServiceSSLKeyStoreType(), "" );
    isEqual( config.getHttpServiceSSLKeyStorePassword(), "" );
    isEqual( config.getHttpServiceSSLTrustStore(), "" );
    isEqual( config.getHttpServiceSSLTrustStorePassword(), "" );
    isEqual( config.getHttpServiceSSLProperties(), new Properties() );
  }
  

  @Test
  @SuppressWarnings("deprecation")
  public void testManagerConfig() throws Exception {
    boolean sslenabled = false;
    String  sslprotocols = "any";
    String  sslciphers = "any";
    boolean requireAuth = true;

    boolean jmxManagerSsl = true;
    boolean jmxManagerSslenabled = true;
    String  jmxManagerSslprotocols = "SSLv7";
    String  jmxManagerSslciphers = "RSA_WITH_GARBAGE";
    boolean jmxManagerSslRequireAuth = true;

    Properties gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_NAME, "true");
    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_ENABLED_NAME, "false");
    try{
      new DistributionConfigImpl( gemFireProps );
      fail("Expected IllegalArgumentException");
    }catch(IllegalArgumentException e){
      if (! e.toString().contains( "Gemfire property \'jmx-manager-ssl\' and \'jmx-manager-ssl-enabled\' can not be used at the same time")) {
        throw new Exception( "did not get expected exception, got this instead...", e );
      }
    }
    // make sure they can both be set to the same value
    gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_NAME, "true");
    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_ENABLED_NAME, "true");
    new DistributionConfigImpl( gemFireProps );
    gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_NAME, "false");
    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_ENABLED_NAME, "false");
    new DistributionConfigImpl( gemFireProps );
    
    gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, String.valueOf(sslenabled));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(requireAuth));

    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_NAME, String.valueOf(jmxManagerSsl));
    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_PROTOCOLS_NAME, jmxManagerSslprotocols);
    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_CIPHERS_NAME, jmxManagerSslciphers);
    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(jmxManagerSslRequireAuth));

    DistributionConfigImpl config = new DistributionConfigImpl( gemFireProps );
    isEqual( config.getClusterSSLEnabled(), sslenabled );
    isEqual( config.getClusterSSLProtocols(), sslprotocols );
    isEqual( config.getClusterSSLCiphers(), sslciphers );
    isEqual( config.getClusterSSLRequireAuthentication(), requireAuth );

    isEqual( config.getJmxManagerSSLEnabled(), jmxManagerSslenabled );
    isEqual( config.getJmxManagerSSLProtocols(), jmxManagerSslprotocols );
    isEqual( config.getJmxManagerSSLCiphers(), jmxManagerSslciphers );
    isEqual( config.getJmxManagerSSLRequireAuthentication(), jmxManagerSslRequireAuth );
  }
  
  
  @Test
  public void testCacheServerConfig() throws Exception {
    boolean sslenabled = false;
    String  sslprotocols = "any";
    String  sslciphers = "any";
    boolean requireAuth = true;

    boolean cacheServerSslenabled = true;
    String  cacheServerSslprotocols = "SSLv7";
    String  cacheServerSslciphers = "RSA_WITH_GARBAGE";
    boolean cacheServerSslRequireAuth = true;

    Properties gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, String.valueOf(sslenabled));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(requireAuth));

    gemFireProps.put(DistributionConfig.SERVER_SSL_ENABLED_NAME, String.valueOf(cacheServerSslenabled));
    gemFireProps.put(DistributionConfig.SERVER_SSL_PROTOCOLS_NAME, cacheServerSslprotocols);
    gemFireProps.put(DistributionConfig.SERVER_SSL_CIPHERS_NAME, cacheServerSslciphers);
    gemFireProps.put(DistributionConfig.SERVER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(cacheServerSslRequireAuth));

    DistributionConfigImpl config = new DistributionConfigImpl( gemFireProps );
    isEqual( config.getClusterSSLEnabled(), sslenabled );
    isEqual( config.getClusterSSLProtocols(), sslprotocols );
    isEqual( config.getClusterSSLCiphers(), sslciphers );
    isEqual( config.getClusterSSLRequireAuthentication(), requireAuth );

    isEqual( config.getServerSSLEnabled(), cacheServerSslenabled );
    isEqual( config.getServerSSLProtocols(), cacheServerSslprotocols );
    isEqual( config.getServerSSLCiphers(), cacheServerSslciphers );
    isEqual( config.getServerSSLRequireAuthentication(), cacheServerSslRequireAuth );
  }

  @Test
  public void testGatewayConfig() throws Exception {
    boolean sslenabled = false;
    String  sslprotocols = "any";
    String  sslciphers = "any";
    boolean requireAuth = true;

    boolean gatewaySslenabled = true;
    String  gatewaySslprotocols = "SSLv7";
    String  gatewaySslciphers = "RSA_WITH_GARBAGE";
    boolean gatewaySslRequireAuth = true;

    Properties gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, String.valueOf(sslenabled));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(requireAuth));

    gemFireProps.put(DistributionConfig.GATEWAY_SSL_ENABLED_NAME, String.valueOf(gatewaySslenabled));
    gemFireProps.put(DistributionConfig.GATEWAY_SSL_PROTOCOLS_NAME, gatewaySslprotocols);
    gemFireProps.put(DistributionConfig.GATEWAY_SSL_CIPHERS_NAME, gatewaySslciphers);
    gemFireProps.put(DistributionConfig.GATEWAY_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(gatewaySslRequireAuth));

    DistributionConfigImpl config = new DistributionConfigImpl( gemFireProps );
    isEqual( config.getClusterSSLEnabled(), sslenabled );
    isEqual( config.getClusterSSLProtocols(), sslprotocols );
    isEqual( config.getClusterSSLCiphers(), sslciphers );
    isEqual( config.getClusterSSLRequireAuthentication(), requireAuth );

    isEqual( config.getGatewaySSLEnabled(), gatewaySslenabled );
    isEqual( config.getGatewaySSLProtocols(), gatewaySslprotocols );
    isEqual( config.getGatewaySSLCiphers(), gatewaySslciphers );
    isEqual( config.getGatewaySSLRequireAuthentication(), gatewaySslRequireAuth );
  }
  
  @Test
  @SuppressWarnings("deprecation")
  public void testCustomizedClusterSslConfig() throws Exception {
    
    boolean sslenabled = true;
    String  sslprotocols = "SSLv1";
    String  sslciphers = "RSA_WITH_NOTHING";
    boolean requireAuth = true;

    boolean clusterSslenabled = true;
    String  clusterSslprotocols = "SSLv7";
    String  clusterSslciphers = "RSA_WITH_GARBAGE";
    boolean clusterSslRequireAuth = true;
    
    //sslEnabled and clusterSSLEnabled set at the same time
    Properties gemFireProps = new Properties();
    gemFireProps.setProperty( "mcast-port", "0" );
    gemFireProps.put(DistributionConfig.SSL_ENABLED_NAME, "true");
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, "false");
    DistributionConfigImpl config = null;
    try{
      config = new DistributionConfigImpl( gemFireProps );
      fail("Expected IllegalArgumentException");
    }catch(IllegalArgumentException e){
      if (! e.toString().contains( "Gemfire property \'ssl-enabled\' and \'cluster-ssl-enabled\' can not be used at the same time")) {
        throw new Exception( "did not get expected exception, got this instead...", e );
      }
    }
    
    //ssl-protocol and cluster-ssl-protocol set at the same time
    gemFireProps = new Properties();
    gemFireProps.setProperty( "mcast-port", "0" );
    gemFireProps.put(DistributionConfig.SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, "true");
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, clusterSslprotocols);
    try{
      config = new DistributionConfigImpl( gemFireProps );
      fail("Expected IllegalArgumentException");
    }catch(IllegalArgumentException e){
      if (! e.toString().contains( "Gemfire property \'ssl-protocols\' and \'cluster-ssl-protocols\' can not be used at the same time") ) {
        throw new Exception( "did not get expected exception, got this instead...", e );
      }
    }
    // make sure they can both be set to the same value
    gemFireProps = new Properties();
    gemFireProps.setProperty( "mcast-port", "0" );
    gemFireProps.put(DistributionConfig.SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, "true");
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, sslprotocols);
    config = new DistributionConfigImpl( gemFireProps );
    
    //ssl-cipher and cluster-ssl-cipher set at the same time
    gemFireProps = new Properties();
    gemFireProps.setProperty( "mcast-port", "0" );
    gemFireProps.put(DistributionConfig.SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, "true");
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, clusterSslciphers);
    try{
      config = new DistributionConfigImpl( gemFireProps );
      fail("Expected IllegalArgumentException");
    }catch(IllegalArgumentException e){
      if (! e.toString().contains( "Gemfire property \'ssl-cipher\' and \'cluster-ssl-cipher\' can not be used at the same time") ) {
        throw new Exception( "did not get expected exception, got this instead...", e );
      }
    }
    // make sure they can both be set to the same value
    gemFireProps = new Properties();
    gemFireProps.setProperty( "mcast-port", "0" );
    gemFireProps.put(DistributionConfig.SSL_CIPHERS_NAME, clusterSslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, "true");
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, clusterSslciphers);
    config = new DistributionConfigImpl( gemFireProps );
    
  //ssl-require-authentication and cluster-ssl-require-authentication set at the same time
    gemFireProps = new Properties();
    gemFireProps.setProperty( "mcast-port", "0" );
    gemFireProps.put(DistributionConfig.SSL_REQUIRE_AUTHENTICATION_NAME, "true");
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, "true");
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, "false");
    try{
      config = new DistributionConfigImpl( gemFireProps );
      fail("Expected IllegalArgumentException");
    }catch(IllegalArgumentException e){
      if (! e.toString().contains( "Gemfire property \'ssl-require-authentication\' and \'cluster-ssl-require-authentication\' can not be used at the same time") ) {
        throw new Exception( "did not get expected exception, got this instead...", e );
      }
    }
    // make sure they can both be set to the same value
    gemFireProps = new Properties();
    gemFireProps.setProperty( "mcast-port", "0" );
    gemFireProps.put(DistributionConfig.SSL_REQUIRE_AUTHENTICATION_NAME, "false");
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, "true");
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, "false");
    config = new DistributionConfigImpl( gemFireProps );

    
    // only ssl-* properties provided. same should reflect in cluster-ssl properties
    gemFireProps = new Properties();
    gemFireProps.setProperty("mcast-port", "0");
    gemFireProps.put(DistributionConfig.SSL_ENABLED_NAME, String.valueOf(sslenabled));
    gemFireProps.put(DistributionConfig.SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(requireAuth));
    gemFireProps.put(DistributionConfig.SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.SSL_PROTOCOLS_NAME, sslprotocols);

    gemFireProps.putAll(getGfSecurityPropertiesSSL());
    
    config = new DistributionConfigImpl(gemFireProps);

    isEqual(sslenabled, config.getSSLEnabled());
    isEqual(sslprotocols, config.getSSLProtocols());
    isEqual(sslciphers, config.getSSLCiphers());
    isEqual(requireAuth, config.getSSLRequireAuthentication());

    isEqual(sslenabled, config.getClusterSSLEnabled());
    isEqual(sslprotocols, config.getClusterSSLProtocols());
    isEqual(sslciphers, config.getClusterSSLCiphers());
    isEqual(requireAuth, config.getClusterSSLRequireAuthentication());
    
    Properties sslProperties = config.getSSLProperties();
    isEqual( SSL_PROPS_MAP , sslProperties);

    Properties clusterSSLProperties = config.getClusterSSLProperties();
    isEqual( SSL_PROPS_MAP, clusterSSLProperties );
    
    //only cluster-ssl-properties provided.
    gemFireProps = new Properties();
    gemFireProps.setProperty("mcast-port", "0");
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, String.valueOf(clusterSslenabled));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(clusterSslRequireAuth));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, clusterSslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, clusterSslprotocols);

    gemFireProps.putAll(getGfSecurityPropertiesCluster(false));
    
    config = new DistributionConfigImpl(gemFireProps);

    isEqual(clusterSslenabled, config.getClusterSSLEnabled());
    isEqual(clusterSslprotocols, config.getClusterSSLProtocols());
    isEqual(clusterSslciphers, config.getClusterSSLCiphers());
    isEqual(clusterSslRequireAuth, config.getClusterSSLRequireAuthentication());
    
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore"), config.getClusterSSLKeyStore() );
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getClusterSSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getClusterSSLKeyStorePassword());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore"), config.getClusterSSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"), config.getClusterSSLTrustStorePassword());
    
    clusterSSLProperties = config.getClusterSSLProperties();
    isEqual( SSL_PROPS_MAP, clusterSSLProperties );
    
  }
  
  @Test
  public void testCustomizedManagerSslConfig() throws Exception {
    boolean sslenabled = false;
    String  sslprotocols = "any";
    String  sslciphers = "any";
    boolean requireAuth = true;

    boolean jmxManagerSslenabled = true;
    String  jmxManagerSslprotocols = "SSLv7";
    String  jmxManagerSslciphers = "RSA_WITH_GARBAGE";
    boolean jmxManagerSslRequireAuth = true;

    Properties gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, String.valueOf(sslenabled));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(requireAuth));

    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_ENABLED_NAME, String.valueOf(jmxManagerSslenabled));
    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_PROTOCOLS_NAME, jmxManagerSslprotocols);
    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_CIPHERS_NAME, jmxManagerSslciphers);
    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(jmxManagerSslRequireAuth));

    gemFireProps.putAll(getGfSecurityPropertiesJMX(false /*partialJmxSslConfigOverride*/));

    DistributionConfigImpl config = new DistributionConfigImpl( gemFireProps );
    isEqual( config.getClusterSSLEnabled(), sslenabled );
    isEqual( config.getClusterSSLProtocols(), sslprotocols );
    isEqual( config.getClusterSSLCiphers(), sslciphers );
    isEqual( config.getClusterSSLRequireAuthentication(), requireAuth );

    isEqual( config.getJmxManagerSSLEnabled(), jmxManagerSslenabled );
    isEqual( config.getJmxManagerSSLProtocols(), jmxManagerSslprotocols );
    isEqual( config.getJmxManagerSSLCiphers(), jmxManagerSslciphers );
    isEqual( config.getJmxManagerSSLRequireAuthentication(), jmxManagerSslRequireAuth );

    isEqual( JMX_SSL_PROPS_MAP.get("jmx-manager-ssl-keystore") , config.getJmxManagerSSLKeyStore());
    isEqual( JMX_SSL_PROPS_MAP.get("jmx-manager-ssl-keystore-type"), config.getJmxManagerSSLKeyStoreType());
    isEqual( JMX_SSL_PROPS_MAP.get("jmx-manager-ssl-keystore-password"), config.getJmxManagerSSLKeyStorePassword());
    isEqual( JMX_SSL_PROPS_MAP.get("jmx-manager-ssl-truststore"), config.getJmxManagerSSLTrustStore());
    isEqual( JMX_SSL_PROPS_MAP.get("jmx-manager-ssl-truststore-password"),config.getJmxManagerSSLTrustStorePassword());
  }
  
  @Test
  public void testCustomizedCacheServerSslConfig() throws Exception {
    boolean sslenabled = false;
    String  sslprotocols = "any";
    String  sslciphers = "any";
    boolean requireAuth = true;

    boolean cacheServerSslenabled = true;
    String  cacheServerSslprotocols = "SSLv7";
    String  cacheServerSslciphers = "RSA_WITH_GARBAGE";
    boolean cacheServerSslRequireAuth = true;

    Properties gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, String.valueOf(sslenabled));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(requireAuth));

    gemFireProps.put(DistributionConfig.SERVER_SSL_ENABLED_NAME, String.valueOf(cacheServerSslenabled));
    gemFireProps.put(DistributionConfig.SERVER_SSL_PROTOCOLS_NAME, cacheServerSslprotocols);
    gemFireProps.put(DistributionConfig.SERVER_SSL_CIPHERS_NAME, cacheServerSslciphers);
    gemFireProps.put(DistributionConfig.SERVER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(cacheServerSslRequireAuth));

    gemFireProps.putAll(getGfSecurityPropertiesforCS(false));

    DistributionConfigImpl config = new DistributionConfigImpl( gemFireProps );
    isEqual( config.getClusterSSLEnabled(), sslenabled );
    isEqual( config.getClusterSSLProtocols(), sslprotocols );
    isEqual( config.getClusterSSLCiphers(), sslciphers );
    isEqual( config.getClusterSSLRequireAuthentication(), requireAuth );

    isEqual( config.getServerSSLEnabled(), cacheServerSslenabled );
    isEqual( config.getServerSSLProtocols(), cacheServerSslprotocols );
    isEqual( config.getServerSSLCiphers(), cacheServerSslciphers );
    isEqual( config.getServerSSLRequireAuthentication(), cacheServerSslRequireAuth );

    isEqual( SERVER_SSL_PROPS_MAP.get("server-ssl-keystore") , config.getServerSSLKeyStore());
    isEqual( SERVER_SSL_PROPS_MAP.get("server-ssl-keystore-type"), config.getServerSSLKeyStoreType());
    isEqual( SERVER_SSL_PROPS_MAP.get("server-ssl-keystore-password"), config.getServerSSLKeyStorePassword());
    isEqual( SERVER_SSL_PROPS_MAP.get("server-ssl-truststore"), config.getServerSSLTrustStore());
    isEqual( SERVER_SSL_PROPS_MAP.get("server-ssl-truststore-password"),config.getServerSSLTrustStorePassword());
  }

  @Test
  public void testCustomizedGatewaySslConfig() throws Exception {
    boolean sslenabled = false;
    String  sslprotocols = "any";
    String  sslciphers = "any";
    boolean requireAuth = true;

    boolean gatewaySslenabled = true;
    String  gatewaySslprotocols = "SSLv7";
    String  gatewaySslciphers = "RSA_WITH_GARBAGE";
    boolean gatewaySslRequireAuth = true;

    Properties gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, String.valueOf(sslenabled));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(requireAuth));

    gemFireProps.put(DistributionConfig.GATEWAY_SSL_ENABLED_NAME, String.valueOf(gatewaySslenabled));
    gemFireProps.put(DistributionConfig.GATEWAY_SSL_PROTOCOLS_NAME, gatewaySslprotocols);
    gemFireProps.put(DistributionConfig.GATEWAY_SSL_CIPHERS_NAME, gatewaySslciphers);
    gemFireProps.put(DistributionConfig.GATEWAY_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(gatewaySslRequireAuth));

    gemFireProps.putAll(getGfSecurityPropertiesforGateway(false));

    DistributionConfigImpl config = new DistributionConfigImpl( gemFireProps );
    isEqual( config.getClusterSSLEnabled(), sslenabled );
    isEqual( config.getClusterSSLProtocols(), sslprotocols );
    isEqual( config.getClusterSSLCiphers(), sslciphers );
    isEqual( config.getClusterSSLRequireAuthentication(), requireAuth );

    isEqual( config.getGatewaySSLEnabled(), gatewaySslenabled );
    isEqual( config.getGatewaySSLProtocols(), gatewaySslprotocols );
    isEqual( config.getGatewaySSLCiphers(), gatewaySslciphers );
    isEqual( config.getGatewaySSLRequireAuthentication(), gatewaySslRequireAuth );

    isEqual( GATEWAY_SSL_PROPS_MAP.get("gateway-ssl-keystore") , config.getGatewaySSLKeyStore());
    isEqual( GATEWAY_SSL_PROPS_MAP.get("gateway-ssl-keystore-type"), config.getGatewaySSLKeyStoreType());
    isEqual( GATEWAY_SSL_PROPS_MAP.get("gateway-ssl-keystore-password"), config.getGatewaySSLKeyStorePassword());
    isEqual( GATEWAY_SSL_PROPS_MAP.get("gateway-ssl-truststore"), config.getGatewaySSLTrustStore());
    isEqual( GATEWAY_SSL_PROPS_MAP.get("gateway-ssl-truststore-password"),config.getGatewaySSLTrustStorePassword());
    
  }
  
  @Test
  public void testPartialCustomizedManagerSslConfig() throws Exception {
    boolean sslenabled = false;
    String  sslprotocols = "any";
    String  sslciphers = "any";
    boolean requireAuth = true;

    boolean jmxManagerSslenabled = true;
    String  jmxManagerSslprotocols = "SSLv7";
    String  jmxManagerSslciphers = "RSA_WITH_GARBAGE";
    boolean jmxManagerSslRequireAuth = true;

    Properties gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, String.valueOf(sslenabled));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(requireAuth));

    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_ENABLED_NAME, String.valueOf(jmxManagerSslenabled));
    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_PROTOCOLS_NAME, jmxManagerSslprotocols);
    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_CIPHERS_NAME, jmxManagerSslciphers);
    gemFireProps.put(DistributionConfig.JMX_MANAGER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(jmxManagerSslRequireAuth));

    gemFireProps.putAll(getGfSecurityPropertiesJMX(true /*partialJmxSslConfigOverride*/));

    DistributionConfigImpl config = new DistributionConfigImpl( gemFireProps );
    isEqual( config.getClusterSSLEnabled(), sslenabled );
    isEqual( config.getClusterSSLProtocols(), sslprotocols );
    isEqual( config.getClusterSSLCiphers(), sslciphers );
    isEqual( config.getClusterSSLRequireAuthentication(), requireAuth );

    isEqual( config.getJmxManagerSSLEnabled(), jmxManagerSslenabled );
    isEqual( config.getJmxManagerSSLProtocols(), jmxManagerSslprotocols );
    isEqual( config.getJmxManagerSSLCiphers(), jmxManagerSslciphers );
    isEqual( config.getJmxManagerSSLRequireAuthentication(), jmxManagerSslRequireAuth );

    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore"), config.getClusterSSLKeyStore() );
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getClusterSSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getClusterSSLKeyStorePassword());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore"), config.getClusterSSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"), config.getClusterSSLTrustStorePassword());
    
    isEqual( JMX_SSL_PROPS_SUBSET_MAP.get("jmx-manager-ssl-keystore") , config.getJmxManagerSSLKeyStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getJmxManagerSSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getJmxManagerSSLKeyStorePassword());
    isEqual( JMX_SSL_PROPS_SUBSET_MAP.get("jmx-manager-ssl-truststore"), config.getJmxManagerSSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"),config.getJmxManagerSSLTrustStorePassword());
  }
  
  
  @Test
  public void testPartialCustomizedCacheServerSslConfig() throws Exception {
    boolean sslenabled = false;
    String  sslprotocols = "any";
    String  sslciphers = "any";
    boolean requireAuth = true;

    boolean cacheServerSslenabled = true;
    String  cacheServerSslprotocols = "SSLv7";
    String  cacheServerSslciphers = "RSA_WITH_GARBAGE";
    boolean cacheServerSslRequireAuth = true;

    Properties gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, String.valueOf(sslenabled));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(requireAuth));

    gemFireProps.put(DistributionConfig.SERVER_SSL_ENABLED_NAME, String.valueOf(cacheServerSslenabled));
    gemFireProps.put(DistributionConfig.SERVER_SSL_PROTOCOLS_NAME, cacheServerSslprotocols);
    gemFireProps.put(DistributionConfig.SERVER_SSL_CIPHERS_NAME, cacheServerSslciphers);
    gemFireProps.put(DistributionConfig.SERVER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(cacheServerSslRequireAuth));

    gemFireProps.putAll(getGfSecurityPropertiesforCS(true));

    DistributionConfigImpl config = new DistributionConfigImpl( gemFireProps );
    isEqual( config.getClusterSSLEnabled(), sslenabled );
    isEqual( config.getClusterSSLProtocols(), sslprotocols );
    isEqual( config.getClusterSSLCiphers(), sslciphers );
    isEqual( config.getClusterSSLRequireAuthentication(), requireAuth );

    isEqual( config.getServerSSLEnabled(), cacheServerSslenabled );
    isEqual( config.getServerSSLProtocols(), cacheServerSslprotocols );
    isEqual( config.getServerSSLCiphers(), cacheServerSslciphers );
    isEqual( config.getServerSSLRequireAuthentication(), cacheServerSslRequireAuth );

    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore"), config.getClusterSSLKeyStore() );
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getClusterSSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getClusterSSLKeyStorePassword());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore"), config.getClusterSSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"), config.getClusterSSLTrustStorePassword());
    
    isEqual( SERVER_PROPS_SUBSET_MAP.get("server-ssl-keystore") , config.getServerSSLKeyStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getServerSSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getServerSSLKeyStorePassword());
    isEqual( SERVER_PROPS_SUBSET_MAP.get("server-ssl-truststore"), config.getServerSSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"),config.getServerSSLTrustStorePassword());
  }
  
  @Test
  public void testPartialCustomizedGatewaySslConfig() throws Exception {
    boolean sslenabled = false;
    String  sslprotocols = "any";
    String  sslciphers = "any";
    boolean requireAuth = true;

    boolean gatewaySslenabled = true;
    String  gatewaySslprotocols = "SSLv7";
    String  gatewaySslciphers = "RSA_WITH_GARBAGE";
    boolean gatewaySslRequireAuth = true;

    Properties gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, String.valueOf(sslenabled));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(requireAuth));

    gemFireProps.put(DistributionConfig.GATEWAY_SSL_ENABLED_NAME, String.valueOf(gatewaySslenabled));
    gemFireProps.put(DistributionConfig.GATEWAY_SSL_PROTOCOLS_NAME, gatewaySslprotocols);
    gemFireProps.put(DistributionConfig.GATEWAY_SSL_CIPHERS_NAME, gatewaySslciphers);
    gemFireProps.put(DistributionConfig.GATEWAY_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(gatewaySslRequireAuth));

    gemFireProps.putAll(getGfSecurityPropertiesforGateway(true));

    DistributionConfigImpl config = new DistributionConfigImpl( gemFireProps );
    isEqual( config.getClusterSSLEnabled(), sslenabled );
    isEqual( config.getClusterSSLProtocols(), sslprotocols );
    isEqual( config.getClusterSSLCiphers(), sslciphers );
    isEqual( config.getClusterSSLRequireAuthentication(), requireAuth );

    isEqual( config.getGatewaySSLEnabled(), gatewaySslenabled );
    isEqual( config.getGatewaySSLProtocols(), gatewaySslprotocols );
    isEqual( config.getGatewaySSLCiphers(), gatewaySslciphers );
    isEqual( config.getGatewaySSLRequireAuthentication(), gatewaySslRequireAuth );
    
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore"), config.getClusterSSLKeyStore() );
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getClusterSSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getClusterSSLKeyStorePassword());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore"), config.getClusterSSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"), config.getClusterSSLTrustStorePassword());
    
    isEqual( GATEWAY_PROPS_SUBSET_MAP.get("gateway-ssl-keystore") , config.getGatewaySSLKeyStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getGatewaySSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getGatewaySSLKeyStorePassword());
    isEqual( GATEWAY_PROPS_SUBSET_MAP.get("gateway-ssl-truststore"), config.getGatewaySSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"),config.getGatewaySSLTrustStorePassword());

  }
  
  @Test
  public void testP2pSSLPropsOverriden_ServerPropsNotOverriden(){
    boolean sslenabled = true;
    String  sslprotocols = "overrriden";
    String  sslciphers = "overrriden";
    boolean requireAuth = true;

    boolean cacheServerSslenabled = false;
    String  cacheServerSslprotocols = "SSLv7";
    String  cacheServerSslciphers = "RSA_WITH_GARBAGE";
    boolean cacheServerSslRequireAuth = false;

    Properties gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.MCAST_PORT_NAME,"0");
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, String.valueOf(sslenabled));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(requireAuth));

    gemFireProps.putAll(getGfSecurityPropertiesforCS(true));

    DistributionConfigImpl config = new DistributionConfigImpl( gemFireProps );
    isEqual( config.getClusterSSLEnabled(), sslenabled );
    isEqual( config.getClusterSSLProtocols(), sslprotocols );
    isEqual( config.getClusterSSLCiphers(), sslciphers );
    isEqual( config.getClusterSSLRequireAuthentication(), requireAuth );

    isEqual( config.getServerSSLEnabled(), sslenabled );
    isEqual( config.getServerSSLProtocols(), sslprotocols );
    isEqual( config.getServerSSLCiphers(), sslciphers );
    isEqual( config.getServerSSLRequireAuthentication(), requireAuth );
    
    assertFalse(config.getServerSSLEnabled()==cacheServerSslenabled);
    assertFalse(config.getServerSSLProtocols().equals(cacheServerSslprotocols));
    assertFalse(config.getServerSSLCiphers().equals(cacheServerSslciphers));
    assertFalse(config.getServerSSLRequireAuthentication()==cacheServerSslRequireAuth);
    
    System.out.println(config.toLoggerString());

    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore"), config.getClusterSSLKeyStore() );
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getClusterSSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getClusterSSLKeyStorePassword());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore"), config.getClusterSSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"), config.getClusterSSLTrustStorePassword());
    
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore") , config.getServerSSLKeyStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getServerSSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getServerSSLKeyStorePassword());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore"), config.getServerSSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"),config.getServerSSLTrustStorePassword());
    
  }
  
  @Test
  public void testP2pSSLPropsOverriden_ServerPropsOverriden(){
    boolean sslenabled = true;
    String  sslprotocols = "overrriden";
    String  sslciphers = "overrriden";
    boolean requireAuth = true;

    boolean cacheServerSslenabled = false;
    String  cacheServerSslprotocols = "SSLv7";
    String  cacheServerSslciphers = "RSA_WITH_GARBAGE";
    boolean cacheServerSslRequireAuth = false;

    Properties gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.MCAST_PORT_NAME,"0");
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, String.valueOf(sslenabled));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(requireAuth));
    
    gemFireProps.put(DistributionConfig.SERVER_SSL_ENABLED_NAME, String.valueOf(cacheServerSslenabled));
    gemFireProps.put(DistributionConfig.SERVER_SSL_PROTOCOLS_NAME, cacheServerSslprotocols);
    gemFireProps.put(DistributionConfig.SERVER_SSL_CIPHERS_NAME, cacheServerSslciphers);
    gemFireProps.put(DistributionConfig.SERVER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(cacheServerSslRequireAuth));

    gemFireProps.putAll(getGfSecurityPropertiesforCS(true));

    DistributionConfigImpl config = new DistributionConfigImpl( gemFireProps );
    isEqual( config.getClusterSSLEnabled(), sslenabled );
    isEqual( config.getClusterSSLProtocols(), sslprotocols );
    isEqual( config.getClusterSSLCiphers(), sslciphers );
    isEqual( config.getClusterSSLRequireAuthentication(), requireAuth );

    isEqual( config.getServerSSLEnabled(), cacheServerSslenabled );
    isEqual( config.getServerSSLProtocols(), cacheServerSslprotocols );
    isEqual( config.getServerSSLCiphers(), cacheServerSslciphers );
    isEqual( config.getServerSSLRequireAuthentication(), cacheServerSslRequireAuth );
    
    assertFalse(config.getServerSSLEnabled()==sslenabled);
    assertFalse(config.getServerSSLProtocols().equals(sslprotocols));
    assertFalse(config.getServerSSLCiphers().equals(sslciphers));
    assertFalse(config.getServerSSLRequireAuthentication()==requireAuth);
    
    System.out.println(config.toLoggerString());

    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore"), config.getClusterSSLKeyStore() );
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getClusterSSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getClusterSSLKeyStorePassword());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore"), config.getClusterSSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"), config.getClusterSSLTrustStorePassword());
    
    isEqual( SERVER_PROPS_SUBSET_MAP.get("server-ssl-keystore") , config.getServerSSLKeyStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getServerSSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getServerSSLKeyStorePassword());
    isEqual( SERVER_PROPS_SUBSET_MAP.get("server-ssl-truststore"), config.getServerSSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"),config.getServerSSLTrustStorePassword());
  }
  
  @Test
  public void testClusterSSLPropsOverriden_GatewayPropsNotOverriden(){
    boolean sslenabled = true;
    String  sslprotocols = "overrriden";
    String  sslciphers = "overrriden";
    boolean requireAuth = true;

    boolean gatewayServerSslenabled = false;
    String  gatewayServerSslprotocols = "SSLv7";
    String  gatewayServerSslciphers = "RSA_WITH_GARBAGE";
    boolean gatewayServerSslRequireAuth = false;

    Properties gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.MCAST_PORT_NAME,"0");
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, String.valueOf(sslenabled));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(requireAuth));

    gemFireProps.putAll(getGfSecurityPropertiesforGateway(true));

    DistributionConfigImpl config = new DistributionConfigImpl( gemFireProps );
    isEqual( config.getClusterSSLEnabled(), sslenabled );
    isEqual( config.getClusterSSLProtocols(), sslprotocols );
    isEqual( config.getClusterSSLCiphers(), sslciphers );
    isEqual( config.getClusterSSLRequireAuthentication(), requireAuth );

    isEqual( config.getGatewaySSLEnabled(), sslenabled );
    isEqual( config.getGatewaySSLProtocols(), sslprotocols );
    isEqual( config.getGatewaySSLCiphers(), sslciphers );
    isEqual( config.getGatewaySSLRequireAuthentication(), requireAuth );
    
    assertFalse(config.getGatewaySSLEnabled()==gatewayServerSslenabled);
    assertFalse(config.getGatewaySSLProtocols().equals(gatewayServerSslprotocols));
    assertFalse(config.getGatewaySSLCiphers().equals(gatewayServerSslciphers));
    assertFalse(config.getGatewaySSLRequireAuthentication()==gatewayServerSslRequireAuth);
    
    System.out.println(config.toLoggerString());

    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore"), config.getClusterSSLKeyStore() );
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getClusterSSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getClusterSSLKeyStorePassword());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore"), config.getClusterSSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"), config.getClusterSSLTrustStorePassword());
    
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore") , config.getGatewaySSLKeyStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getGatewaySSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getGatewaySSLKeyStorePassword());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore"), config.getGatewaySSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"),config.getGatewaySSLTrustStorePassword());
    
  }
  
  @Test
  public void testP2pSSLPropsOverriden_GatewayPropsOverriden(){
    boolean sslenabled = true;
    String  sslprotocols = "overrriden";
    String  sslciphers = "overrriden";
    boolean requireAuth = true;

    boolean gatewayServerSslenabled = false;
    String  gatewayServerSslprotocols = "SSLv7";
    String  gatewayServerSslciphers = "RSA_WITH_GARBAGE";
    boolean gatewayServerSslRequireAuth = false;

    Properties gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.MCAST_PORT_NAME,"0");
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, String.valueOf(sslenabled));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(requireAuth));
    
    gemFireProps.put(DistributionConfig.GATEWAY_SSL_ENABLED_NAME, String.valueOf(gatewayServerSslenabled));
    gemFireProps.put(DistributionConfig.GATEWAY_SSL_PROTOCOLS_NAME, gatewayServerSslprotocols);
    gemFireProps.put(DistributionConfig.GATEWAY_SSL_CIPHERS_NAME, gatewayServerSslciphers);
    gemFireProps.put(DistributionConfig.GATEWAY_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(gatewayServerSslRequireAuth));

    gemFireProps.putAll(getGfSecurityPropertiesforGateway(true));

    DistributionConfigImpl config = new DistributionConfigImpl( gemFireProps );
    isEqual( config.getClusterSSLEnabled(), sslenabled );
    isEqual( config.getClusterSSLProtocols(), sslprotocols );
    isEqual( config.getClusterSSLCiphers(), sslciphers );
    isEqual( config.getClusterSSLRequireAuthentication(), requireAuth );

    isEqual( config.getGatewaySSLEnabled(), gatewayServerSslenabled );
    isEqual( config.getGatewaySSLProtocols(), gatewayServerSslprotocols );
    isEqual( config.getGatewaySSLCiphers(), gatewayServerSslciphers );
    isEqual( config.getGatewaySSLRequireAuthentication(), gatewayServerSslRequireAuth );
    
    System.out.println(config.toLoggerString());

    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore"), config.getClusterSSLKeyStore() );
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getClusterSSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getClusterSSLKeyStorePassword());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore"), config.getClusterSSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"), config.getClusterSSLTrustStorePassword());
    
    isEqual( GATEWAY_PROPS_SUBSET_MAP.get("gateway-ssl-keystore") , config.getGatewaySSLKeyStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getGatewaySSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getGatewaySSLKeyStorePassword());
    isEqual( GATEWAY_PROPS_SUBSET_MAP.get("gateway-ssl-truststore"), config.getGatewaySSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"),config.getGatewaySSLTrustStorePassword());
    
  }
  
  @Test
  public void testP2pSSLPropsOverriden_JMXPropsNotOverriden(){
    boolean sslenabled = true;
    String  sslprotocols = "overrriden";
    String  sslciphers = "overrriden";
    boolean requireAuth = true;

    boolean jmxManagerSslenabled = false;
    String  jmxManagerSslprotocols = "SSLv7";
    String  jmxManagerSslciphers = "RSA_WITH_GARBAGE";
    boolean jmxManagerSslRequireAuth = false;

    Properties gemFireProps = new Properties();
    gemFireProps.put(DistributionConfig.MCAST_PORT_NAME,"0");
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_ENABLED_NAME, String.valueOf(sslenabled));
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_PROTOCOLS_NAME, sslprotocols);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_CIPHERS_NAME, sslciphers);
    gemFireProps.put(DistributionConfig.CLUSTER_SSL_REQUIRE_AUTHENTICATION_NAME, String.valueOf(requireAuth));

    gemFireProps.putAll(getGfSecurityPropertiesJMX(true));

    DistributionConfigImpl config = new DistributionConfigImpl( gemFireProps );
    isEqual( config.getClusterSSLEnabled(), sslenabled );
    isEqual( config.getClusterSSLProtocols(), sslprotocols );
    isEqual( config.getClusterSSLCiphers(), sslciphers );
    isEqual( config.getClusterSSLRequireAuthentication(), requireAuth );

    isEqual( config.getJmxManagerSSLEnabled(), sslenabled );
    isEqual( config.getJmxManagerSSLProtocols(), sslprotocols );
    isEqual( config.getJmxManagerSSLCiphers(), sslciphers );
    isEqual( config.getJmxManagerSSLRequireAuthentication(), requireAuth );
    
    assertFalse(config.getJmxManagerSSLEnabled()==jmxManagerSslenabled);
    assertFalse(config.getJmxManagerSSLProtocols().equals(jmxManagerSslprotocols));
    assertFalse(config.getJmxManagerSSLCiphers().equals(jmxManagerSslciphers));
    assertFalse(config.getJmxManagerSSLRequireAuthentication()==jmxManagerSslRequireAuth);
    
    System.out.println(config.toLoggerString());

    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore"), config.getClusterSSLKeyStore() );
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getClusterSSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getClusterSSLKeyStorePassword());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore"), config.getClusterSSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"), config.getClusterSSLTrustStorePassword());
    
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore") , config.getJmxManagerSSLKeyStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-type"), config.getJmxManagerSSLKeyStoreType());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-keystore-password"), config.getJmxManagerSSLKeyStorePassword());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore"), config.getJmxManagerSSLTrustStore());
    isEqual( CLUSTER_SSL_PROPS_MAP.get("cluster-ssl-truststore-password"),config.getJmxManagerSSLTrustStorePassword()); 
    
  }
  
  private static Properties getGfSecurityPropertiesSSL() {
    Properties gfSecurityProps = new Properties();

    Set<Entry<Object, Object>> entrySet = SSL_PROPS_MAP.entrySet();
    for (Entry<Object, Object> entry : entrySet) {
      gfSecurityProps.put(entry.getKey(), entry.getValue());
    }

    return gfSecurityProps;
  }
  
  private static Properties getGfSecurityPropertiesCluster(boolean partialClusterSslConfigOverride) {
    Properties gfSecurityProps = new Properties();

    Set<Entry<Object, Object>> entrySet = SSL_PROPS_MAP.entrySet();
    for (Entry<Object, Object> entry : entrySet) {
      gfSecurityProps.put(entry.getKey(), entry.getValue());
    }

    if (partialClusterSslConfigOverride) {
      entrySet = CLUSTER_SSL_PROPS_SUBSET_MAP.entrySet();
    } else {
      entrySet = CLUSTER_SSL_PROPS_MAP.entrySet();
    }
    for (Entry<Object, Object> entry : entrySet) {
      gfSecurityProps.put(entry.getKey(), entry.getValue());
    }
    return gfSecurityProps;
  }
  
  private static Properties getGfSecurityPropertiesJMX(boolean partialJmxSslConfigOverride) {
    Properties gfSecurityProps = new Properties();

    Set<Entry<Object, Object>> entrySet = CLUSTER_SSL_PROPS_MAP.entrySet();
    for (Entry<Object, Object> entry : entrySet) {
      gfSecurityProps.put(entry.getKey(), entry.getValue());
    }

    if (partialJmxSslConfigOverride) {
      entrySet = JMX_SSL_PROPS_SUBSET_MAP.entrySet();
    } else {
      entrySet = JMX_SSL_PROPS_MAP.entrySet();
    }
    for (Entry<Object, Object> entry : entrySet) {
      // Add "-jmx" suffix for JMX Manager properties.
      gfSecurityProps.put(entry.getKey(), entry.getValue());
    }

    return gfSecurityProps;
  }
  
  private static Properties getGfSecurityPropertiesforCS(boolean partialCSSslConfigOverride) {
    Properties gfSecurityProps = new Properties();

    Set<Entry<Object, Object>> entrySet = CLUSTER_SSL_PROPS_MAP.entrySet();
    for (Entry<Object, Object> entry : entrySet) {
      gfSecurityProps.put(entry.getKey(), entry.getValue());
    }

    if (partialCSSslConfigOverride) {
      entrySet = SERVER_PROPS_SUBSET_MAP.entrySet();
    } else {
      entrySet = SERVER_SSL_PROPS_MAP.entrySet();
    }
    for (Entry<Object, Object> entry : entrySet) {
      // Add "-cacheserver" suffix for CacheServer properties.
      gfSecurityProps.put(entry.getKey(), entry.getValue());
    }
    //gfSecurityProps.list(System.out);
    return gfSecurityProps;
  }

  private static Properties getGfSecurityPropertiesforGateway(boolean partialGatewaySslConfigOverride) {
    Properties gfSecurityProps = new Properties();

    Set<Entry<Object, Object>> entrySet = CLUSTER_SSL_PROPS_MAP.entrySet();
    for (Entry<Object, Object> entry : entrySet) {
      gfSecurityProps.put(entry.getKey(), entry.getValue());
    }

    if (partialGatewaySslConfigOverride) {
      entrySet = GATEWAY_PROPS_SUBSET_MAP.entrySet();
    } else {
      entrySet = GATEWAY_SSL_PROPS_MAP.entrySet();
    }
    for (Entry<Object, Object> entry : entrySet) {
      gfSecurityProps.put(entry.getKey(), entry.getValue());
    }
    //gfSecurityProps.list(System.out);
    return gfSecurityProps;
  }
  
  public void isEqual( boolean a, boolean e ) throws AssertionFailedError {
    assertEquals( a, e );
  }
  
  public void isEqual( Object a, Object e ) throws AssertionFailedError {
    assertEquals( a, e );
  } 
  
}
