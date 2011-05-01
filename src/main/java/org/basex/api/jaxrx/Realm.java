package org.basex.api.jaxrx;

import static org.basex.util.Token.*;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.basex.core.Context;
import org.basex.core.User;

/**
 * This class is responsible to perform authentication/authorization for REST
 * requests.
 * 
 * @author Lukas Lewandowski, University of Konstanz.
 * 
 */
public class Realm extends AuthorizingRealm {

  
  @Override
  protected AuthorizationInfo doGetAuthorizationInfo(
      final PrincipalCollection principalCollection) {
    // Authorization will be performed by BaseX and not needed here.
    return null;
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(
      final AuthenticationToken authenticationToken)
      throws AuthenticationException {
    final UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
    token.setRememberMe(false);
    final String username = token.getUsername();
    // perform authentication if user exists in BaseX
    Context cxt = new Context();
    User usr = cxt.users.get(username);
    if(usr != null) {
      AuthenticationInfo info = new SimpleAuthenticationInfo(username,
          string(usr.password), getName());
      cxt.close();
      return info;
    }

    // throw AuthenticationException when user not authenticated within BaseX.
    throw new AuthenticationException();
  }

}
