package com.gemstone.gemfire.test.dunit;

import static com.gemstone.gemfire.test.dunit.Wait.waitForCriterion;

import java.io.File;

import com.gemstone.gemfire.distributed.DistributedSystem;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;
import com.gemstone.gemfire.distributed.internal.membership.jgroup.MembershipManagerHelper;
import com.gemstone.org.jgroups.Event;
import com.gemstone.org.jgroups.JChannel;
import com.gemstone.org.jgroups.stack.Protocol;

public class DistributedSystemSupport {

  protected DistributedSystemSupport() {
  }
  
  /**
   * Crash the cache in the given VM in such a way that it immediately stops communicating with
   * peers.  This forces the VM's membership manager to throw a ForcedDisconnectException by
   * forcibly terminating the JGroups protocol stack with a fake EXIT event.<p>
   * 
   * NOTE: if you use this method be sure that you clean up the VM before the end of your
   * test with disconnectFromDS() or disconnectAllFromDS().
   */
  public static boolean crashDistributedSystem(VM vm) { // TODO: move
    return (Boolean)vm.invoke(new SerializableCallable("crash distributed system") {
      public Object call() throws Exception {
        DistributedSystem msys = InternalDistributedSystem.getAnyInstance();
        crashDistributedSystem(msys);
        return true;
      }
    });
  }
  
  /**
   * Crash the cache in the given VM in such a way that it immediately stops communicating with
   * peers.  This forces the VM's membership manager to throw a ForcedDisconnectException by
   * forcibly terminating the JGroups protocol stack with a fake EXIT event.<p>
   * 
   * NOTE: if you use this method be sure that you clean up the VM before the end of your
   * test with disconnectFromDS() or disconnectAllFromDS().
   */
  public static void crashDistributedSystem(final DistributedSystem msys) { // TODO: move
    MembershipManagerHelper.inhibitForcedDisconnectLogging(true);
    MembershipManagerHelper.playDead(msys);
    JChannel c = MembershipManagerHelper.getJChannel(msys);
    Protocol udp = c.getProtocolStack().findProtocol("UDP");
    udp.stop();
    udp.passUp(new Event(Event.EXIT, new RuntimeException("killing member's ds")));
    try {
      MembershipManagerHelper.getJChannel(msys).waitForClose();
    }
    catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      // attempt rest of work with interrupt bit set
    }
    MembershipManagerHelper.inhibitForcedDisconnectLogging(false);
    WaitCriterion wc = new WaitCriterion() {
      public boolean done() {
        return !msys.isConnected();
      }
      public String description() {
        return "waiting for distributed system to finish disconnecting: " + msys;
      }
    };
//    try {
      waitForCriterion(wc, 10000, 1000, true);
//    } finally {
//      dumpMyThreads(getLogWriter());
//    }
  }

  /** get the host name to use for a server cache in client/server dunit
   * testing
   * @param host
   * @return the host name
   */
  public static String getServerHostName(Host host) {
    return System.getProperty("gemfire.server-bind-address") != null?
        System.getProperty("gemfire.server-bind-address")
        : host.getHostName();
  }

  /** 
   * Delete locator state files.  Use this after getting a random port
   * to ensure that an old locator state file isn't picked up by the
   * new locator you're starting.
   * 
   * @param ports
   */
  public static void deleteLocatorStateFile(final int... ports) {
    for (int i=0; i<ports.length; i++) {
      final File stateFile = new File("locator"+ports[i]+"state.dat");
      if (stateFile.exists()) {
        stateFile.delete();
      }
    }
  }

}
