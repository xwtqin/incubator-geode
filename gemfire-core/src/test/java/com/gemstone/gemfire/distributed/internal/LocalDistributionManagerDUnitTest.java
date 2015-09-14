/*=========================================================================
 * Copyright (c) 2010-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * one or more patents listed at http://www.pivotal.io/patents.
 *=========================================================================
 */
package com.gemstone.gemfire.distributed.internal;

import static com.gemstone.gemfire.test.dunit.DUnitTestRule.*;
import static com.gemstone.gemfire.test.dunit.Threads.*;
import static com.gemstone.gemfire.test.dunit.Wait.*;
import static org.assertj.core.api.StrictAssertions.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.gemstone.gemfire.test.junit.categories.DistributedTest;
import com.gemstone.gemfire.test.junit.categories.MembershipTest;
import com.gemstone.gemfire.distributed.internal.membership.InternalDistributedMember;
import com.gemstone.gemfire.test.dunit.AsyncInvocation;
import com.gemstone.gemfire.test.dunit.DUnitTestRule;
import com.gemstone.gemfire.test.dunit.Host;
import com.gemstone.gemfire.test.dunit.SerializableRunnable;
import com.gemstone.gemfire.test.dunit.VM;
import com.gemstone.gemfire.test.dunit.WaitCriterion;

/**
 * This class tests the functionality of a {@link
 * LocalDistributionManager}.
 *
 * @author David Whitlock
 *
 * @since 2.1
 */
@Category({ DistributedTest.class, MembershipTest.class })
@SuppressWarnings("serial")
public class LocalDistributionManagerDUnitTest implements Serializable {

  @Rule
  public final DUnitTestRule dunitTestRule = DUnitTestRule.build();

  @Before
  public void before() {
    disconnectAllFromDS();
  }
  
  /** A <code>TestMembershipListener</code> used in this VM */
  protected static TestMembershipListener listener = null;

  /**
   * Creates a connection to the distributed system in the given
   * {@link VM}.  Configures the connection for the VM.
   */
  protected void createSystem(VM vm) {
    vm.invoke(new SerializableRunnable("Connect to distributed system") {
        public void run() {
          getSystem(); 
        }
      });
  }

  /**
   * Do we see all of the distribution managers in the distributed
   * system?
   */
  @Test
  public void testCountDMs() {
    Host host = Host.getHost(0);
    VM vm0 = host.getVM(0);
    VM vm1 = host.getVM(1);

    createSystem(vm0);
    vm0.invoke(new SerializableRunnable("Count DMs") {
        public void run() {
          DM dm = getSystem().getDistributionManager();
          assertEquals(1, 
                       dm.getNormalDistributionManagerIds().size());
        }
      });
    
    createSystem(vm1);
    vm1.invoke(new SerializableRunnable("Count DMs Again") {
        public void run() {
          DM dm = getSystem().getDistributionManager();
          assertEquals(2, 
                       dm.getNormalDistributionManagerIds().size());
        }
      });
  }

  /**
   * Test that messages that are sent are received in a reasonable
   * amount of time.
   */
  @Test
  public void testSendMessage() throws Exception {
    Host host = Host.getHost(0);
    VM vm0 = host.getVM(0);
    VM vm1 = host.getVM(1);

    createSystem(vm0);
    createSystem(vm1);

    vm0.invoke(new SerializableRunnable("Send message") {
        public void run() {
          DM dm = getSystem().getDistributionManager();
          assertEquals("For DM " + dm.getId(), 
                       1, dm.getOtherNormalDistributionManagerIds().size());
          FirstMessage message = new FirstMessage();
          dm.putOutgoing(message);
        }
      });

    vm1.invoke(new SerializableRunnable("Was message received?") {
        public void run() {
          WaitCriterion ev = new WaitCriterion() {
            public boolean done() {
              return FirstMessage.received;
            }
            public String description() {
              return null;
            }
          };
          waitForCriterion(ev, 3 * 1000, 200, true);
          FirstMessage.received = false;
        }
      });
  }

  /**
   * Tests the new non-shared {@link ReplyProcessor21}
   */
  @Ignore("Fails with too many responses. TODO: confirm if this is still a valid test.")
  @Test
  public void testReplyProcessor() throws Exception {
    Host host = Host.getHost(0);
    VM vm0 = host.getVM(0);
    VM vm1 = host.getVM(1);

    createSystem(vm0);
    createSystem(vm1);

    vm0.invoke(new SerializableRunnable("Send request") {
        public void run() {
          // Send a request, wait for a response
          DM dm = getSystem().getDistributionManager();
          int expected = dm.getOtherNormalDistributionManagerIds().size();
          assertEquals("For DM " + dm.getId(), 1, expected);

          Response.totalResponses = 0;

          Request request = new Request();
          ReplyProcessor21 processor = new ReplyProcessor21(getSystem(), 
              dm.getOtherNormalDistributionManagerIds()); 
          request.processorId = processor.getProcessorId();
          dm.putOutgoing(request);
          try {
            processor.waitForReplies();

          } catch (Exception ex) {
            fail("While waiting for replies", ex);
          }

          assertEquals(expected, Response.totalResponses);
        }
      });
    
  }

  /**
   * Does the {@link MembershipListener#memberJoined} method get
   * invoked? 
   */
  @Test
  public void testMemberJoinedAndDeparted() throws Exception {

    Host host = Host.getHost(0);
    VM vm0 = host.getVM(0);
    VM vm1 = host.getVM(1);

    createSystem(vm0);
    vm0.invoke(new SerializableRunnable("Install listener") {
        public void run() {
          DM dm = getSystem().getDistributionManager();
          listener = new TestMembershipListener();
          dm.addMembershipListener(listener);
        }
      });
    createSystem(vm1);

    vm0.invoke(new SerializableRunnable("Verify member joining") {
        public void run() {
          WaitCriterion ev = new WaitCriterion() {
            public boolean done() {
              return listener.memberJoined();
            }
            public String description() {
              return null;
            }
          };
          waitForCriterion(ev, 3 * 1000, 200, true);
        }
      });
    vm1.invoke(new SerializableRunnable("Disconnect from system") {
        public void run() {
          getSystem().disconnect();
        }
      });

    vm0.invoke(new SerializableRunnable("Verify member departing") {
        public void run() {
          WaitCriterion ev = new WaitCriterion() {
            public boolean done() {
              return listener.memberDeparted();
            }
            public String description() {
              return null;
            }
          };
          waitForCriterion(ev, 3 * 1000, 200, true);
        }
      });
  }

  /**
   * Tests that the reply processor gets signaled when members go
   * away. 
   */
  @Test
  public void testMembersDepartWhileWaiting() throws Exception {

    Host host = Host.getHost(0);
    VM vm0 = host.getVM(0);
    VM vm1 = host.getVM(1);

    createSystem(vm0);
    createSystem(vm1);

    AsyncInvocation ai0 =
      vm0.invokeAsync(new SerializableRunnable("Send message and wait") {
          public void run() {
            DM dm = getSystem().getDistributionManager();
            OnlyGFDMReply message = new OnlyGFDMReply();
            ReplyProcessor21 processor = new ReplyProcessor21(getSystem(), 
                dm.getOtherNormalDistributionManagerIds());
            message.processorId = processor.getProcessorId();
            dm.putOutgoing(message);

            try {
              processor.waitForReplies();
              
            } catch (Exception ex) {
              fail("While waiting for replies", ex);
            }
          }
        });

    vm1.invoke(new SerializableRunnable("Disconnect from system") {
        public void run() {
          getSystem().disconnect();
        }
      });

    join(ai0, 30 * 1000);
    if (ai0.exceptionOccurred()) {
      fail("got exception", ai0.getException());
    }
  }

  /**
   * A message that is send, and when received, sets a
   * <code>boolean</code> static field.
   *
   * @see LocalDistributionManagerDUnitTest#testSendMessage
   */
  public static class FirstMessage extends SerialDistributionMessage {

    /** Has a <code>FirstMessage</code> be received? */
    public static boolean received = false;

    public FirstMessage() { }   // For Externalizable

    public void process(DistributionManager dm) {
      received = true;
    }
    public int getDSFID() {
      return NO_FIXED_ID;
    }
  }

  /**
   * A request that is replied to with a {@link Response}
   *
   * @see LocalDistributionManagerDUnitTest#testReplyProcessor
   */
  public static class Request extends SerialDistributionMessage
    implements MessageWithReply {

    /** The id of the processor to process the response */
    int processorId;

    public Request() { }        // For Externizable

    public int getProcessorId() {
      return this.processorId;
    }

    /**
     * Reply with a {@link Response}
     */
    public void process(DistributionManager dm) {
      Response response = new Response();
      response.processorId = this.processorId;
      response.setRecipient(this.getSender());
      dm.putOutgoing(response);
    }

    public int getDSFID() {
      return NO_FIXED_ID;
    }

    public void toData(DataOutput out) throws IOException {
      super.toData(out);
      out.writeInt(this.processorId);
    }

    public void fromData(DataInput in)
      throws ClassNotFoundException, IOException {
      super.fromData(in);
      this.processorId = in.readInt();
    }

    public String toString() {
      return "Request with processor " + this.processorId;
    }
  }

  /**
   * A response to a {@link Request}
   *
   * @see LocalDistributionManagerDUnitTest#testReplyProcessor
   */
  public static class Response extends SerialDistributionMessage {
    /** The total number of responses that have been received */
    static int totalResponses = 0;

    /** The id of the processor to process the response */
    int processorId;

    public Response() { }       // For Externalizable

    /**
     * Alert the {@link ReplyProcess21} that this reply has been
     * received.
     */
    public void process(DistributionManager dm) {
      // Look up the processor
      ReplyProcessor21 processor =
        ReplyProcessor21.getProcessor(this.processorId);
      assertNotNull("Null processor!", processor);
      synchronized (Response.class) {
        totalResponses++;
      }
      processor.process(this);
    }

    public int getDSFID() {
      return NO_FIXED_ID;
    }

    public void toData(DataOutput out) throws IOException {
      super.toData(out);
      out.writeInt(this.processorId);
    }

    public void fromData(DataInput in)
      throws ClassNotFoundException, IOException {
      super.fromData(in);
      this.processorId = in.readInt();
    }

    public String toString() {
      return "Response with processor " + this.processorId;
    }
  }

  /**
   * A <code>MembershipListener</code> that remembers when members
   * join and depart.
   */
  static class TestMembershipListener implements MembershipListener {

    /** Has a member joined recently? */
    private boolean joined = false;

    /** Has a member departed recently? */
    private boolean departed = false;

    public void memberJoined(InternalDistributedMember id) {
      this.joined = true;
    }

    public void memberDeparted(InternalDistributedMember id, boolean crashed) {
      this.departed = true;
    }

    /**
     * Gets (and then forgets) whether or not a member has recently
     * joined the distributed system.
     */
    public boolean memberJoined() {
      boolean b = this.joined;
      this.joined = false;
      return b;
    }

    public void quorumLost(Set<InternalDistributedMember> failures, List<InternalDistributedMember> remaining) {
    }

    public void memberSuspect(InternalDistributedMember id,
        InternalDistributedMember whoSuspected) {
    }
    
    /**
     * Gets (and then forgets) whether or not a member has recently
     * departed the distributed system.
     */
    public boolean memberDeparted() {
      boolean b = this.departed;
      this.departed = false;
      return b;
    }
  }

  /**
   * A message that only GemFire distribution managers reply to.
   */
  public static class OnlyGFDMReply extends SerialDistributionMessage
    implements MessageWithReply {

    /** The id of the processor that processes the reply */
    protected int processorId;

    public int getProcessorId() {
      return this.processorId;
    }

    public OnlyGFDMReply() { }  // For Externalizable

    public void process(DistributionManager dm) {
    }

    public int getDSFID() {
      return NO_FIXED_ID;
    }

    public void toData(DataOutput out) throws IOException {
      super.toData(out);
      out.writeInt(this.processorId);
    }

    public void fromData(DataInput in)
      throws ClassNotFoundException, IOException {
      super.fromData(in);
      this.processorId = in.readInt();
    }

    public String toString() {
      return "Only GFDM replies with processor " + this.processorId;
    }
  }

}
