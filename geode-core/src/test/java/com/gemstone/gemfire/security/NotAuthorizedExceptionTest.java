package com.gemstone.gemfire.security;

import com.gemstone.gemfire.test.junit.categories.UnitTest;
import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.Serializable;
import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link NotAuthorizedException}.
 */
@Category(UnitTest.class)
public class NotAuthorizedExceptionTest {

  @Test
  public void isSerializable() throws Exception {
    assertThat(NotAuthorizedException.class).isInstanceOf(Serializable.class);
  }

  @Test
  public void canBeSerialized() throws Exception {
    String message = "my message";
    NotAuthorizedException instance = new NotAuthorizedException(message);

    NotAuthorizedException cloned = (NotAuthorizedException) SerializationUtils.clone(instance);

    assertThat(cloned).hasMessage(message);
  }

  @Test
  public void canBeSerializedWithThrowable() throws Exception {
    String message = "my message";
    Throwable cause = new Exception("the cause");
    NotAuthorizedException instance = new NotAuthorizedException(message, cause);

    NotAuthorizedException cloned = (NotAuthorizedException) SerializationUtils.clone(instance);

    assertThat(cloned).hasMessage(message);
    assertThat(cloned).hasCause(cause);
  }

  @Test
  public void canBeSerializedWithPrincipal() throws Exception {
    String message = "my message";
    Principal mockPrincipal = mock(Principal.class);
    NotAuthorizedException instance = new NotAuthorizedException(message, mockPrincipal);

    NotAuthorizedException cloned = (NotAuthorizedException) SerializationUtils.clone(instance);

    assertThat(cloned).hasMessage(message);
    assertThat(cloned.getPrincipal()).isEqualTo(mockPrincipal);
  }
}
