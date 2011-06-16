package org.basex.api.webdav;

import org.basex.core.User;
import org.basex.server.ClientSession;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;

/**
 * Base class for all WebDAV resources.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public abstract class BXResource implements Resource {

  protected String user;
  protected String pass;
  /** Resource factory. */
  protected BXResourceFactory fact;

  @Override
  public Object authenticate(final String user, final String password) {
    this.user = user;
    this.pass = password;
    return user;
  }

  @Override
  public boolean authorise(final Request request, final Method method,
      final Auth auth) {
    if(auth != null) {
      final String user = (String) auth.getTag();
      // [DP] WebDAV: check if user has sufficient privileges
      if(user != null) return true;
    }
    return false;
  }

  @Override
  public String checkRedirect(final Request request) {
    return null;
  }

  @Override
  public String getRealm() {
    return "BaseX";
  }

  @Override
  public String getUniqueId() {
    return null;
  }
}
