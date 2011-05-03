package org.basex.api.webdav;

import static org.basex.util.Token.*;

import org.basex.core.Context;
import org.basex.core.User;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Request.Method;

/**
 * Base class for all WebDAV resources.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura, Dimitar Popov
 */
public abstract class BXResource implements Resource {
  /** Database context. */
  protected Context ctx;

  @Override
  public Object authenticate(final String user, final String password) {
    final User u = ctx.users.get(user);
    if(u != null && eq(u.password, token(password))) return user;
    return null;
  }

  @Override
  public boolean authorise(final Request request, final Method method,
      final Auth auth) {
    // TODO Auto-generated method stub
    return true;
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
