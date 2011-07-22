package org.basex.api.jaxrx;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.basex.core.Context;
import org.basex.server.LocalSession;
import org.basex.server.LoginException;
import org.basex.server.Session;
import org.basex.util.Util;
import org.jaxrx.core.JaxRxException;
import org.jaxrx.core.ResourcePath;

import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;

/**
 * 
 * @author 524506
 * 
 */
public final class SessionFactory {

  /**
   * Context used to create local sessions
   */
  private static Context context;

  /**
   * Should local sessions be created.
   */
  private static boolean localSession = false;

  static {
    localSession = Boolean.parseBoolean(System.getProperty(
        "org.basex.jaxrx.local", "false"));
    if(localSession) {
      context = new Context();
    }
  }

  /**
   * Prevent instance creation
   */
  private SessionFactory() {}

  /**
   * 
   * @param path provides authentication information for client sessions
   * @return a local or client session depending upon properties.
   */
  static Session getSession(ResourcePath path) {
    if(localSession) {
      return new LocalSession(context);
    }
    try {
      return JaxRxServer.login(path);
    } catch(final LoginException ex) {
      final ResponseBuilder rb = new ResponseBuilderImpl();
      rb.header(HttpHeaders.WWW_AUTHENTICATE, "Basic ");
      rb.status(401);
      rb.entity("Username/password is wrong.");
      throw new JaxRxException(rb.build());
    } catch(final Exception ex) {
      Util.stack(ex);
      throw new JaxRxException(ex);
    }
  }
}
