package com.vmware.gemfire.tools.pulse.internal.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import com.vmware.gemfire.tools.pulse.internal.data.Repository;
import com.vmware.gemfire.tools.pulse.internal.log.PulseLogWriter;

/**
 * Handler is used to close jmx connection maintained at user-level
 * @author tushark
 *
 */
public class LogoutHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {

  public LogoutHandler(String defaultTargetURL) {
    this.setDefaultTargetUrl(defaultTargetURL);
  }

  public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    PulseLogWriter LOGGER = PulseLogWriter.getLogger();
    LOGGER.fine("Invoked #LogoutHandler ...");
    if (Repository.get().isUseGemFireCredentials()) {
      GemFireAuthentication gemauthentication = (GemFireAuthentication) authentication;
      gemauthentication.getJmxc().close();
      LOGGER.info("#LogoutHandler : Closing GemFireAuthentication JMX Connection...");
    }
    super.onLogoutSuccess(request, response, authentication);
  }

}
