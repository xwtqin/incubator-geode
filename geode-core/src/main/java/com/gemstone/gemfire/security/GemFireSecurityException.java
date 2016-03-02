/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gemstone.gemfire.security;

import com.gemstone.gemfire.GemFireException;

import javax.naming.NamingException;
import java.io.*;

/**
 * The base class for all com.gemstone.gemfire.security package related
 * exceptions.
 * 
 * @author Sumedh Wale
 * @since 5.5
 */
public class GemFireSecurityException extends GemFireException {
  private static final long serialVersionUID = 3814254578203076926L;

  /**
   * Constructs instance of <code>SecurityException</code> with error message.
   * 
   * @param message
   *                the error message
   */
  public GemFireSecurityException(String message) {
    super(message);
  }

  /**
   * Constructs instance of <code>SecurityException</code> with error message
   * and cause.
   * 
   * @param message
   *                the error message
   * @param cause
   *                a <code>Throwable</code> that is a cause of this exception
   */
  public GemFireSecurityException(String message, Throwable cause) {
    super(message, cause);
  }

  protected boolean isSerializable(final Object object) {
    if (object == null) {
      return true;
    }
    return object.getClass().isInstance(Serializable.class);
  }

  protected Object getResolvedObj() {
    if (getCause() != null && getCause().getClass().isInstance(NamingException.class)) {
      return ((javax.naming.NamingException) getCause()).getResolvedObj();
    }
    return null;
  }

  private void writeObject(final ObjectOutputStream stream) throws IOException {
    final Object resolvedObj = getResolvedObj();
    if (isSerializable(resolvedObj)) {
      stream.defaultWriteObject();
    } else {
      final NamingException namingException = (NamingException) getCause();
      namingException.setResolvedObj(null);
      try {
        stream.defaultWriteObject();
      } finally {
        namingException.setResolvedObj(resolvedObj);
      }
    }
  }
}
