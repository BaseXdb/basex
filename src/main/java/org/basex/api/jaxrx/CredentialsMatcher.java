/**
 * 
 */
package org.basex.api.jaxrx;

import static org.basex.util.Token.*;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;

/**
 * This class is responsible to perform credentials matching.
 * 
 * @author Lukas Lewandowski, University of Konstanz.
 * 
 */
public class CredentialsMatcher extends SimpleCredentialsMatcher {

  @Override
  public boolean doCredentialsMatch(final AuthenticationToken token,
      final AuthenticationInfo info) {
    // stored pw in BaseX
    final String pw = (String) info.getCredentials();
    // pw from request
    final String rpw = md5(new String((char[]) token.getCredentials()));

    // set pw to property
    System.setProperty("org.basex.user", (String) token.getPrincipal());
    System.setProperty("org.basex.password",
        new String((char[]) token.getCredentials()));

    return rpw.equals(pw);
  }
}
