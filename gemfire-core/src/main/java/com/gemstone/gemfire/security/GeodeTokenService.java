/**
 * 
 */
package com.gemstone.gemfire.security;

import java.security.Principal;
import java.util.Random;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.ExpirationAction;
import com.gemstone.gemfire.cache.ExpirationAttributes;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.RegionFactory;
import com.gemstone.gemfire.cache.RegionShortcut;

/**
 * This implementation provides a simple token service, generating and managing
 * string tokens based on <code>java.util.Random</code>. The generated tokens
 * are stored in a Geode <code>Region</code>. Older or idle tokens are expired
 * periodically.
 * <p>
 * A new token is issued with each invocation of
 * {@link GeodeTokenService#validateToken(String, Principal)}.
 */
public class GeodeTokenService implements TokenService {

  private Region<String, Principal> tokenStore;

  private Random tokenGenerator = null;

  private int seedBase;
  
  private long firstToken;

  public GeodeTokenService() {
    // Create a region with expiration attributes.
    Cache cache = CacheFactory.getAnyInstance();
    RegionFactory<String, Principal> rf = cache.createRegionFactory(RegionShortcut.REPLICATE);

    // Remove a token after 30 minutes.
    rf.setEntryTimeToLive(new ExpirationAttributes(30*60, ExpirationAction.DESTROY));
    // Remove idle tokens after 5 minutes.
    rf.setEntryIdleTimeout(new ExpirationAttributes(5*60, ExpirationAction.DESTROY));
    
    this.tokenStore = rf.create("geode_token_store");

    this.seedBase = cache.getDistributedSystem().getDistributedMember().hashCode();
    initializeTokenGenerator();
  }

  private void initializeTokenGenerator() {
    this.tokenGenerator = new Random(this.seedBase + System.currentTimeMillis());
    this.firstToken = this.tokenGenerator.nextLong();
  }

  public static GeodeTokenService create() {
    return new GeodeTokenService();
  }

  @Override
  public String generateToken(Principal principal) {
    String token = generateTokenString(principal);
    this.tokenStore.put(token, principal);
    return token;
  }

  @Override
  public String validateToken(String token, Principal principal)
      throws AuthenticationRequiredException, AuthenticationFailedException {
    Principal savedPrincipal = this.tokenStore.get(token);

    if (savedPrincipal != null && savedPrincipal.equals(principal)) {
      // I know this guy. Refresh the token for this client.
      this.tokenStore.remove(token);
      token = generateTokenString(savedPrincipal);
      this.tokenStore.put(token, savedPrincipal);
      return token;
    }

    this.tokenStore.remove(token);
    String msg = "Authentication failed.";

    throw savedPrincipal == null ? new AuthenticationRequiredException(msg)
        : new AuthenticationFailedException(msg);
  }

  private synchronized String generateTokenString(Principal principal) {
    long token = this.tokenGenerator.nextLong();
    if (token == this.firstToken) {
      // We have run out of tokens. Re-initialise the token generator.
      initializeTokenGenerator();
      // Invalidate all the existing tokens and force authenticated REST clients
      // to re-authenticate themselves.
      this.tokenStore.clear();
      token = this.tokenGenerator.nextLong();
    }
    return String.valueOf(token);
  }

}
