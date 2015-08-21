package com.gemstone.gemfire.test.dunit;

import java.net.UnknownHostException;

import com.gemstone.gemfire.internal.SocketCreator;

public class NetworkSupport {
  
  protected NetworkSupport() {
  }

  /** get the IP literal name for the current host, use this instead of  
   * "localhost" to avoid IPv6 name resolution bugs in the JDK/machine config.
   * @return an ip literal, this method honors java.net.preferIPvAddresses
   */
  public static String getIPLiteral() { // TODO: move
    try {
      return SocketCreator.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      throw new Error("problem determining host IP address", e);
    }
  }
}
