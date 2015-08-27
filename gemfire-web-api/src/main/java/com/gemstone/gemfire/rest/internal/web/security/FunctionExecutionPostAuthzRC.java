package com.gemstone.gemfire.rest.internal.web.security;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.gemstone.gemfire.cache.execute.Function;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.ResultCollector;
import com.gemstone.gemfire.cache.operations.ExecuteFunctionOperationContext;
import com.gemstone.gemfire.distributed.DistributedMember;
import com.gemstone.gemfire.security.NotAuthorizedException;

public class FunctionExecutionPostAuthzRC implements ResultCollector {

  private ArrayList<Object> resultList = new ArrayList<Object>();
  
  private ExecuteFunctionOperationContext functionAuthzContext;
  
  public FunctionExecutionPostAuthzRC() {
  
  }
  
  public FunctionExecutionPostAuthzRC(ExecuteFunctionOperationContext context) {
    this.functionAuthzContext = context;
    this.functionAuthzContext.setIsPostOperation(true);
  }

  /**
   * Adds a single function execution result from a remote node to the
   * ResultCollector
   * 
   * @param distributedMember
   * @param resultOfSingleExecution
   */
  public synchronized void addResult(DistributedMember distributedMember,
      Object resultOfSingleExecution) {
    //Post authorization here
    if(AuthorizationProvider.isSecurityEnabled()){
      try{
        functionAuthzContext.setResult(resultOfSingleExecution);
        AuthorizationProvider.executeFunctionAuthorizePP(resultOfSingleExecution, functionAuthzContext);
      }catch(NotAuthorizedException nae){
        throw new NotAuthorizedException("Not Authorized to get results!");
      }
    }
    this.resultList.add(functionAuthzContext.getResult() );
  }

  /**
   * Waits if necessary for the computation to complete, and then retrieves its
   * result.<br>
   * If {@link Function#hasResult()} is false, upon calling
   * {@link ResultCollector#getResult()} throws {@link FunctionException}.
   * 
   * @return the Object computed result
   * @throws FunctionException
   *                 if something goes wrong while retrieving the result
   */
  public Object getResult() throws FunctionException {
    return this.resultList; // this is full result
  }

  /**
   * Call back provided to caller, which is called after function execution is
   * complete and caller can retrieve results using
   * {@link ResultCollector#getResult()}
   * 
   */
  public void endResults() {
  }

  /**
   * Waits if necessary for at most the given time for the computation to
   * complete, and then retrieves its result, if available. <br>
   * If {@link Function#hasResult()} is false, upon calling
   * {@link ResultCollector#getResult()} throws {@link FunctionException}.
   * 
   * @param timeout
   *                the maximum time to wait
   * @param unit
   *                the time unit of the timeout argument
   * @return Object computed result
   * @throws FunctionException
   *                 if something goes wrong while retrieving the result
   */
  public Object getResult(long timeout, TimeUnit unit)
      throws FunctionException {
    return this.resultList;
  }

  /**
   * GemFire will invoke this method before re-executing function (in case of
   * Function Execution HA) This is to clear the previous execution results from
   * the result collector
   * 
   */
  public void clearResults() {
    this.resultList.clear();
  }
}

