/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.gemstone.gemfire.security.util;

import java.security.Principal;
import java.util.Properties;

import com.gemstone.gemfire.security.templates.DummyAuthenticator;
import com.gemstone.gemfire.security.templates.UserPasswordAuthInit;
import com.gemstone.gemfire.security.util.CredentialGenerator;

public class DummyCredentialGenerator extends CredentialGenerator {

  private static final String DUMMY_AUTHENTICATOR_CREATE_NAME = DummyAuthenticator.class.getName() + ".create";

  private static final String USER_PASSWORD_AUTH_INIT_CREATE_NAME = UserPasswordAuthInit.class.getName() + ".create";
  
  public DummyCredentialGenerator() {
  }

  @Override
  protected Properties initialize() throws IllegalArgumentException {
    return null;
  }

  @Override
  public ClassCode classCode() {
    return ClassCode.DUMMY;
  }

  @Override
  public String getAuthInit() {
    return USER_PASSWORD_AUTH_INIT_CREATE_NAME;
  }

  @Override
  public String getAuthenticator() {
    return DUMMY_AUTHENTICATOR_CREATE_NAME;
  }

  @Override
  public Properties getValidCredentials(int index) {

    String[] validGroups = new String[] { "admin", "user", "reader", "writer" };
    String[] admins = new String[] { "root", "admin", "administrator" };

    Properties props = new Properties();
    int groupNum = (index % validGroups.length);
    String userName;
    if (groupNum == 0) {
      userName = admins[index % admins.length];
    }
    else {
      userName = validGroups[groupNum] + (index / validGroups.length);
    }
    props.setProperty(UserPasswordAuthInit.USER_NAME, userName);
    props.setProperty(UserPasswordAuthInit.PASSWORD, userName);
    return props;
  }

  @Override
  public Properties getValidCredentials(Principal principal) {

    String userName = principal.getName();
    if (DummyAuthenticator.testValidName(userName)) {
      Properties props = new Properties();
      props.setProperty(UserPasswordAuthInit.USER_NAME, userName);
      props.setProperty(UserPasswordAuthInit.PASSWORD, userName);
      return props;
    }
    else {
      throw new IllegalArgumentException("Dummy: [" + userName + "] is not a valid user");
    }
  }

  @Override
  public Properties getInvalidCredentials(int index) {

    Properties props = new Properties();
    props.setProperty(UserPasswordAuthInit.USER_NAME, "invalid" + index);
    props.setProperty(UserPasswordAuthInit.PASSWORD, "none");
    return props;
  }

}
