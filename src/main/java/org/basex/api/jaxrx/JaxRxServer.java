package org.basex.api.jaxrx;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.basex.BaseXServer;
import org.basex.core.MainProp;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.basex.server.ClientSession;
import org.basex.util.Args;
import org.basex.util.Base64;
import org.basex.util.Util;
import org.jaxrx.JettyServer;
import org.jaxrx.core.JaxRxConstants;
import org.jaxrx.core.JaxRxException;
import org.jaxrx.core.ResourcePath;

import com.sun.jersey.core.spi.factory.ResponseBuilderImpl;

/**
 * This is the starter class for running the JAX-RX server, based on the JAX-RX
 * interface. A database server and the Jetty server is launched by the
 * constructor. The Jetty server listens for HTTP requests, which are then sent
 * to JAX-RX.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class JaxRxServer extends BaseXServer {
  /** Configuration: User. */
  static final String USER = "org.basex.user";
  /** Configuration: Password. */
  static final String PASSWORD = "org.basex.password";
  /** Configuration: Server port. */
  static final String SERVERPORT = "org.basex.serverport";
  /** Configuration: JAX-RX path. */
  static final String JAXRXPATH = "org.basex.jaxrxpath";
  /** Configuration: local flag. */
  static final String LOCAL = "org.basex.jaxrx.local";
  /** Configuration: serializer options. */
  static final String SERIALIZER = "org.jaxrx.parameter.output";
  /** JAX-RX String. */
  static final String JAXRX = "JAX-RX";

  /** Jetty server. */
  private JettyServer jetty;
  /** Optional user. */
  private String user;
  /** Optional password. */
  private String pass;

  /**
   * Main method, launching the JAX-RX implementation.
   * Command-line arguments are listed with the {@code -h} argument.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    try {
      new JaxRxServer(args);
    } catch(final IOException ex) {
      Util.errln(ex);
      System.exit(1);
    }
  }

  /**
   * Constructor.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  public JaxRxServer(final String... args) throws IOException {
    super(args);
    if(service || stopped) return;

    // set default ports and paths
    set(JAXRXPATH, context.mprop.get(MainProp.JAXRXPATH), false);
    set(SERVERPORT, context.mprop.num(MainProp.SERVERPORT), false);
    set(SERIALIZER, context.prop.get(Prop.SERIALIZER), false);

    // retrieve password on command-line if only the user was specified
    String p = pass;
    if(user != null) {
      while(p == null) {
        Util.out(SERVERPW + COLS);
        p = password();
      }
    }
    // set data as system properties
    set(USER, user == null ? "" : user, user != null);
    set(PASSWORD, p == null ? "" : p, p != null);

    // try to login with specified data
    try {
      if(!System.getProperty(USER).isEmpty()) login(null).close();
    } catch(final Exception ex) {
      Util.errln(ex.getMessage());
      quit();
      return;
    }

    // define path and name of the JAX-RX implementation.
    set(JaxRxConstants.NAMEPROP, Text.NAMELC, false);
    set(JaxRxConstants.PATHPROP, BXJaxRx.class.getName(), false);

    // start Jetty server (if not done yet)
    try {
      jetty = new JettyServer(context.mprop.num(MainProp.JAXRXPORT));
      Util.outln(JAXRX + ' ' + SERVERSTART);
    } catch(final Exception ex) {
      Util.errln(ex);
    }
  }

  @Override
  public void quit() throws IOException {
    super.quit();
    if(jetty != null) jetty.stop();
  }

  @Override
  public void stop() throws IOException {
    quit();
  }

  /**
   * Sets the specified value if property has not been set yet.
   * @param key property key
   * @param value property value
   * @param force force setting
   */
  private void set(final String key, final Object value, final boolean force) {
    if(force || System.getProperty(key) == null) {
      System.setProperty(key, value.toString());
    }
  }

  /**
   * Logs in and returns a client session.
   * @param path resource path; may be {@code null}
   * @return client session
   * @throws Exception exception
   */
  static ClientSession login(final ResourcePath path) throws Exception {
    String[] id = updateIdentity(path);
    if(id == null) id = new String[] {
        System.getProperty(JaxRxServer.USER),
        System.getProperty(JaxRxServer.PASSWORD)
    };
    final int p = Integer.parseInt(System.getProperty(JaxRxServer.SERVERPORT));
    return new ClientSession(Text.LOCALHOST, p, id[0], id[1]);
  }

  /**
   * Reads user identity and user credentials from HTTP header.
   * @param path {@link ResourcePath} instance.
   * @return login/password combination or {@code null} if no user was
   *         specified.
   */
  private static String[] updateIdentity(final ResourcePath path) {
    if(path == null) return null;
    final HttpHeaders headers = path.getHttpHeaders();
    final List<String> authorization =
      headers.getRequestHeader(HttpHeaders.AUTHORIZATION);
    if(authorization != null) {
      for(final String value : authorization) {
        final String[] values = value.split(" ");
        if(values[0].equalsIgnoreCase("basic")) {
          final String[] cred = Base64.decode(values[1]).split(":", 2);
          if(cred.length < 2) {
            final ResponseBuilder rb = new ResponseBuilderImpl();
            rb.header(HttpHeaders.WWW_AUTHENTICATE, "Basic ");
            rb.status(401);
            rb.entity("No password specified.");
            throw new JaxRxException(rb.build());
          }
          return cred;
        }
        throw new JaxRxException(500, "Unsupported authorization mode.");
      }
    }
    return null;
  }

  @Override
  public void parseArguments(final String[] args) throws IOException {
    final Args arg = new Args(args, this, JAXRXINFO, Util.info(CONSOLE, JAXRX));
    boolean daemon = false;
    final StringBuilder serial = new StringBuilder();
    while(arg.more()) {
      if(arg.dash()) {
        final char c = arg.next();
        if(c == 'D') {
          // hidden flag: daemon mode
          daemon = true;
        } else if(c == 'j') {
          // parse JAX-RX server port
          context.mprop.set(MainProp.JAXRXPORT, arg.num());
        } else if(c == 'p') {
          // parse server port
          set(SERVERPORT, arg.num(), true);
        } else if(c == 'P') {
          // specify password
          pass = arg.string();
        } else if(c == 's') {
          // set service flag
          service = !daemon;
        } else if(c == 'S') {
          // set/add serialization parameter
          if(serial.length() != 0) serial.append(',');
          serial.append(arg);
          set(SERIALIZER, serial, true);
        } else if(c == 'U') {
          // specify user name
          user = arg.string();
        } else if(c == 'z') {
          // suppress logging
          quiet = true;
        } else {
          arg.usage();
        }
      } else {
        if(arg.string().equalsIgnoreCase("stop")) {
          stop(context.mprop.num(MainProp.SERVERPORT),
               context.mprop.num(MainProp.EVENTPORT));
          stopped = true;
        } else {
          arg.usage();
        }
      }
    }
  }
}
