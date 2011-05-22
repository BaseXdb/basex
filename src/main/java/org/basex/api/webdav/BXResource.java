package org.basex.api.webdav;

import static org.basex.util.Token.*;
import org.basex.core.Context;
import org.basex.core.User;
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
  /** Database context. */
  protected Context ctx;

  @Override
  public Object authenticate(final String user, final String password) {
    final User u = ctx.users.get(user);
    if(u != null && eq(u.password, token(md5(password)))) return user;
    return null;
  }

  @Override
  public boolean authorise(final Request request, final Method method,
      final Auth auth) {
    if(auth != null) {
      final String user = (String) auth.getTag();
      final User u = ctx.users.get(user);
      // [DP] WebDAV: check if user has sufficient privileges
      if(u != null) return true;
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
