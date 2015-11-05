package com.gemstone.gemfire.rest.internal.web.controllers;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import com.gemstone.gemfire.LogWriter;
import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.operations.OperationContext;
import com.gemstone.gemfire.cache.operations.OperationContext.OperationCode;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.security.AccessControl;
import com.gemstone.gemfire.security.NotAuthorizedException;

/**
 * A dummy implementation of the <code>AccessControl</code> interface that
 * allows authorization depending on the format of the <code>Principal</code>
 * string.
 * 
 * @author Nilkanth Patel
 * @since 9.0
 */

public class CustomRestAPIsAuthorization implements AccessControl {

  private Set allowedOps;

  private DistributedMember remoteDistributedMember;

  private LogWriter logger;

  public static final OperationCode[] READER_OPS = { OperationCode.GET,
      OperationCode.QUERY, OperationCode.EXECUTE_CQ, OperationCode.CLOSE_CQ,
      OperationCode.STOP_CQ, OperationCode.REGISTER_INTEREST,
      OperationCode.UNREGISTER_INTEREST, OperationCode.KEY_SET,
      OperationCode.CONTAINS_KEY, OperationCode.EXECUTE_FUNCTION,
      OperationCode.LIST };

  public static final OperationCode[] WRITER_OPS = { OperationCode.PUT, OperationCode.PUTALL, 
      OperationCode.DESTROY, OperationCode.INVALIDATE, OperationCode.REGION_CLEAR,
      OperationCode.CREATE_QUERY, OperationCode.UPDATE_QUERY, OperationCode.DELETE_QUERY};

  public CustomRestAPIsAuthorization() {
    this.allowedOps = new HashSet(20);
  }

  public static AccessControl create() {
    return new CustomRestAPIsAuthorization();
  }

  private void addReaderOps() {

    for (int index = 0; index < READER_OPS.length; index++) {
      this.allowedOps.add(READER_OPS[index]);
    }
  }

  private void addWriterOps() {

    for (int index = 0; index < WRITER_OPS.length; index++) {
      this.allowedOps.add(WRITER_OPS[index]);
    }
  }

  public void init(Principal principal, 
                   DistributedMember remoteMember,
                   Cache cache) throws NotAuthorizedException {

    if (principal != null) {
      String name = principal.getName().toLowerCase();
      if (name != null) {
        if (name.equals("root") || name.equals("admin")
            || name.equals("administrator")) {
          addReaderOps();
          addWriterOps();
          this.allowedOps.add(OperationCode.REGION_CREATE);
          this.allowedOps.add(OperationCode.REGION_DESTROY);
          this.allowedOps.add(OperationCode.LIST);
          this.allowedOps.add(OperationCode.CREATE_QUERY);
          this.allowedOps.add(OperationCode.UPDATE_QUERY);
          this.allowedOps.add(OperationCode.DELETE_QUERY);
        }
        else if (name.startsWith("writer")) {
          addWriterOps();
        }
        else if (name.startsWith("reader")) {
          addReaderOps();
        }
      }
    }
    this.remoteDistributedMember = remoteMember;
    this.logger = cache.getSecurityLogger();
  }

  public boolean authorizeOperation(String regionName, OperationContext context) {

    if(context.isPostOperation()) {
      OperationCode opCode = context.getOperationCode();
      this.logger.fine("Invoked authorize operation for [" + opCode
        + "] in region [" + regionName + "] for client: " + remoteDistributedMember);
      //TODO: Add post authz specific filtering
      return this.allowedOps.contains(opCode);
    }else {
      OperationCode opCode = context.getOperationCode();
      this.logger.fine("Invoked authorize operation for [" + opCode
        + "] in region [" + regionName + "] for client: " + remoteDistributedMember);
      return this.allowedOps.contains(opCode);
    }
  }

  public void close() {

    this.allowedOps.clear();
  }

}