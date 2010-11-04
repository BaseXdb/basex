package org.basex.api.jaxrx;

import static org.basex.core.Text.*;
import org.basex.BaseXServer;
import org.basex.core.Main;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.basex.server.Session;
import org.basex.util.Args;
import org.basex.util.Util;
import org.jaxrx.JettyServer;

/**
 * This is the starter class for running the REST server,
 * based on the JAX-RX interface.
 * A database server and the Jetty server is launched by the constructor.
 * The Jetty server listens for HTTP requests, which are then sent to JAX-RX.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class JaxRxServer extends Main {
  /** Database server. */
  private BaseXServer server;
  /** Jetty server. */
  private JettyServer jetty;
  /** Quiet mode (no logging). */
  private boolean quiet;

  /**
   * Main method, launching the JAX-RX/REST implementation.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    new JaxRxServer(args);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  public JaxRxServer(final String... args) {
    if(!parseArguments(args)) return;

    // set default ports and paths
    set(BXJaxRx.RESTPATH, context.prop.get(Prop.RESTPATH), false);
    set(BXJaxRx.SERVERPORT, context.prop.num(Prop.SERVERPORT), false);
    set(BXJaxRx.SERIALIZER, context.prop.get(Prop.SERIALIZER), false);

    final int rp = context.prop.num(Prop.RESTPORT);
    Util.outln(RESTSTART, rp);

    // store configuration in system properties
    // if a property has already been set, the new settings will be ignored

    // set user (use 'admin' as default)
    final boolean user = System.getProperty(BXJaxRx.USER) != null;
    if(!user) set(BXJaxRx.USER, Text.ADMIN, false);
    // set password (use 'admin' as default, or request on command line)
    final String pass = System.getProperty(BXJaxRx.PASSWORD);
    String p = pass != null ? pass : user ? null : Text.ADMIN;
    while(p == null) {
      Util.out(SERVERPW + COLS);
      p = password();
    }
    set(BXJaxRx.PASSWORD, p, false);

    // define path and name of the JAX-RX implementation.
    set("org.jaxrx.systemName", Text.NAMELC, false);
    set("org.jaxrx.systemPath", BXJaxRx.class.getName(), false);

    // start database server (if not done yet)
    server = new BaseXServer(quiet ? "-z" : "");

    // start Jetty server (if not done yet)
    try {
      jetty = new JettyServer(rp);
    } catch(final Exception ex) {
      Util.server(ex);
    }
  }

  /**
   * Stops the servers.
   */
  public void stop() {
    server.stop();
    jetty.stop();
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

  @Override
  public boolean parseArguments(final String[] args) {
    String serial = "";
    final Args arg = new Args(args, this, RESTINFO);
    while(arg.more()) {
      if(arg.dash()) {
        final char c = arg.next();
        if(c == 'p') {
          // parse server port
          set(BXJaxRx.SERVERPORT, arg.num(), true);
        } else if(c == 'P') {
          // specify password
          set(BXJaxRx.PASSWORD, arg.string(), true);
        } else if(c == 'r') {
          // parse rest server port
          context.prop.set(Prop.RESTPORT, arg.num());
        } else if(c == 's') {
          // set/add serialization parameter
          serial += "," + arg.string();
          set(BXJaxRx.SERIALIZER, serial, true);
        } else if(c == 'U') {
          // specify user name
          set(BXJaxRx.USER, arg.string(), true);
        } else if(c == 'z') {
          // suppress logging
          quiet = true;
        } else {
          arg.check(false);
        }
      }
    }
    return arg.finish();
  }

  @Override
  protected Session session() {
    // not called
    return null;
  }
}
