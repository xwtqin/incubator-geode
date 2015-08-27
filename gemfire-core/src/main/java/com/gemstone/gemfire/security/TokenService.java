/**
 * 
 */
package com.gemstone.gemfire.security;

import java.security.Principal;

/**
 * This interface provides APIs to generate and validate tokens for Geode REST
 * clients.
 * <p>
 * A single instance of the implementing class per cache will created by Geode
 * and these methods will be invoked on that same instance. Synchronization, if
 * any, needs to be handled by the implementation itself.
 */
public interface TokenService {

  /**
   * Generates an unique token for the given principal. Geode REST service keeps
   * track of each token and its associated client and only validates this token
   * for each subsequent request from the same REST client, thus eliminating the
   * need to explicitly authenticate it every time.
   * 
   * <p>
   * If a REST client sends its credentials in its subsequent requests, instead
   * of the issued token, the Geode REST service treats it as a new client's
   * request and issues a new token for it.
   * 
   * @param principal
   *          the principal which the token is to be generated for.
   * @return the generated token for the given principal.
   */
  public String generateToken(Principal principal);

  /**
   * Verifies that the provided token is a valid one for the provided principal.
   * Optionally, it may replace this token by returning a new one. Thus, a new
   * token is exchanged for each request from the client.
   * 
   * <p>
   * The REST client must send this returned token in its next request instead
   * of the credentials. The method throws appropriate authentication exception
   * if the token is not valid.
   * <p>
   * The implementation may expire the tokens after regular interval so as to
   * minimise its possible misuse.
   * 
   * @param token
   *          the token to be validated. This is the token the client had
   *          received in the previous response from the Geode REST service and
   *          has sent it back in its current request to the Geode REST service.
   * @param principal
   *          the principal associated with the given token.
   * @return the validated token. The implementation may also issue a new token
   *         replacing earlier one for the client.
   */
  public String validateToken(String token, Principal principal)
      throws AuthenticationRequiredException, AuthenticationFailedException;

}
