package com.gemstone.gemfire.test.dunit.standalone;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteDUnitVMIF extends Remote {

  public MethExecutorResult executeMethodOnObject(Object o, String methodName) throws RemoteException;

  public MethExecutorResult executeMethodOnObject(Object o, String methodName, Object[] args) throws RemoteException;

  public MethExecutorResult executeMethodOnClass(String name, String methodName, Object[] args) throws RemoteException;

}
