/*=========================================================================
 * Copyright (c) 2002-2014 Pivotal Software, Inc. All Rights Reserved.
 * This product is protected by U.S. and international copyright
 * and intellectual property laws. Pivotal products are covered by
 * more patents listed at http://www.pivotal.io/patents.
 *=========================================================================
 */

package com.gemstone.gemfire.cache.operations;


/**
 * Encapsulates a REST APIs specific operations for both the pre-operation and
 * post-operation cases.
 * 
 * @author Nilkanth Patel
 * @since 9.0
 */
public class RestAPIsOperationContext extends OperationContext {
  
  private boolean restOperation;
  
  private OperationCode opCode;
  
  private String queryId;
  private String oqlStatement;
  
  public RestAPIsOperationContext( OperationCode opCode, boolean restOperation) {
    this.opCode = opCode;
    this.restOperation = restOperation;
  }
  
  public RestAPIsOperationContext( OperationCode opCode, boolean restOperation,
                                  String queryId, String oqlStatement) {
    this.opCode = opCode;
    this.restOperation = restOperation;
    this.queryId = queryId;
    this.oqlStatement = oqlStatement;
  }
  
  /**
   * True if the context is for REST APIs specific operation.
   */
  public boolean isRestAPIsOperation() {
    return this.restOperation;
  }

  /**
   * Set the REST APIs specific operation flag to true.
   */
  public void setRestAPIsOperation() {
    this.restOperation = true;
  }

  @Override
  public OperationCode getOperationCode() {
    return this.opCode;
  }

  @Override
  public boolean isPostOperation() {
    return false;
  }
}