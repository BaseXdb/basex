package org.basex.http;

import java.io.*;
import java.util.*;

import javax.servlet.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * HTTP context information.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class HTTPContext {
  /** Database context. */
  private static volatile Context context;
  /** Initialized failed. */
  private static IOException exception;
  /** Initialization flag. */
  private static boolean init;
  /** Server instance. */
  private static BaseXServer server;

  /** Private constructor. */
  private HTTPContext() { }

  // STATIC METHODS ===============================================================================

  /**
   * Returns the database context. Creates a new instance if not done so before.
   * @return database context
   */
  public static Context context() {
    if(context == null) context = new Context(true);
    return context;
  }

  /**
   * Initializes the HTTP context, based on the initial servlet context.
   * Parses all context parameters and passes them on to the database context.
   * @param sc servlet context
   * @throws IOException I/O exception
   */
  public static synchronized void init(final ServletContext sc) throws IOException {
    // check if servlet context has already been initialized
    if(init) return;
    init = true;

    final String webapp = sc.getRealPath("/");
    // system property (requested in Prop#homePath)
    System.setProperty(Prop.PATH, webapp);
    // global option (will later be assigned to StaticOptions#WEBPATH)
    Prop.put(StaticOptions.WEBPATH, webapp);

    // set all parameters that start with "org.basex." as global options
    final Enumeration<String> en = sc.getInitParameterNames();
    while(en.hasMoreElements()) {
      final String name = en.nextElement();
      String value = sc.getInitParameter(name);
      if(name.startsWith(Prop.DBPREFIX) && name.endsWith("path") && !new File(value).isAbsolute()) {
        // prefix relative path with absolute servlet path
        Util.debug(name.toUpperCase(Locale.ENGLISH) + ": " + value);
        value = new IOFile(webapp, value).path();
      }
      Prop.put(name, value);
    }

    // create context, update options
    if(context == null) {
      context = new Context(false);
    } else {
      context.soptions.setSystem();
      context.options.setSystem();
    }

    // start server instance
    if(!context.soptions.get(StaticOptions.HTTPLOCAL)) {
      try {
        server = new BaseXServer(context, "-D");
      } catch(final IOException ex) {
        exception = ex;
        throw ex;
      }
    }
  }

  /**
   * Returns an exception that may have been caught by the initialization of the database server.
   * @return exception
   */
  public static IOException exception() {
    return exception;
  }

  /**
   * Closes the database context.
   */
  public static synchronized void close() {
    if(server != null) {
      server.stop();
      server = null;
    }
    if(context != null) {
      context.close();
      context = null;
    }
  }

  /**
   * Tries to convert the specified value to a string.
   * @param value value
   * @return return string or {@code null}
   */
  public static byte[] token(final Object value) {
    if(value instanceof Str) return ((Str) value).string();
    if(value instanceof Atm) return ((Atm) value).string(null);
    return null;
  }
}
