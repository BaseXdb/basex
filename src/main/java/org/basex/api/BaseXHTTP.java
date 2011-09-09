package org.basex.api;

import static org.basex.api.HTTPText.*;
import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.BaseXServer;
import org.basex.api.jaxrx.JaxRxServer;
import org.basex.api.webdav.WebDAVServer;
import org.basex.core.MainProp;
import org.basex.core.Prop;
import org.basex.util.Args;
import org.basex.util.Util;
import org.mortbay.jetty.Server;

/**
 * This is the abstract main class for the API starter classes.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class BaseXHTTP {
  /** Database context. */
  private final HTTPContext http = HTTPContext.get();
  /** Activate WebDAV. */
  private boolean webdav = true;
  /** Activate JAX-RX. */
  private boolean jaxrx = true;
  /** Database server. */
  private BaseXServer server;
  /** HTTP server. */
  private Server jetty;
  /** Quiet flag. */
  private boolean quiet;
  /** Stopped flag. */
  private boolean stopped;

  /**
   * Main method, launching the HTTP services.
   * Command-line arguments are listed with the {@code -h} argument.
   * @param args command-line arguments
   */
  public static void main(final String[] args) {
    try {
      new BaseXHTTP(args);
    } catch(final Exception ex) {
      Util.errln(ex);
      System.exit(1);
    }
  }

  /**
   * Constructor.
   * @param args command-line arguments
   * @throws Exception exception
   */
  public BaseXHTTP(final String... args) throws Exception {
    parseArguments(args);

    final MainProp mprop = http.context.mprop;
    if(stopped) {
      stop(mprop.num(MainProp.SERVERPORT), mprop.num(MainProp.EVENTPORT));
      return;
    }

    if(http.client) {
      server = new BaseXServer(http.context, quiet ? "-z" : "");
    } else {
      Util.outln(CONSOLE + SERVERSTART, SERVERMODE);
    }

    jetty = new Server(http.context.mprop.num(MainProp.HTTPPORT));
    // [CG] currently, only one of the two servers will work
    if(webdav) new WebDAVServer(jetty);
    if(jaxrx) new JaxRxServer(jetty);
    jetty.start();
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  public void stop() throws Exception {
    if(jetty != null) jetty.stop();
    if(server != null) server.quit();
  }

  /**
   * Stops the server.
   * @param port server port
   * @param eport event port
   * @throws IOException I/O exception
   */
  public static void stop(final int port, final int eport) throws IOException {
    BaseXServer.stop(port, eport);
  }

  /**
   * Parses the command-line arguments, specified by the user.
   * @param args command-line arguments
   * @throws IOException I/O exception
   */
  protected void parseArguments(final String[] args) throws IOException {
    final Args arg = new Args(args, this, HTTPINFO, Util.info(CONSOLE, HTTP));
    final StringBuilder serial = new StringBuilder();

    final MainProp mprop = http.context.mprop;
    while(arg.more()) {
      if(arg.dash()) {
        final char c = arg.next();
        switch(c) {
          case 'c':
            System.setProperty(DBCLIENT, Boolean.TRUE.toString());
            break;
          case 'd':
            mprop.set(MainProp.DEBUG, true);
            break;
          case 'e':
            mprop.set(MainProp.EVENTPORT, arg.num());
            break;
          case 'h':
            mprop.set(MainProp.HTTPPORT, arg.num());
            break;
          case 'J':
            jaxrx = false;
            break;
          case 'n':
            mprop.set(MainProp.HOST, arg.num());
            break;
          case 'p':
            final int p = arg.num();
            mprop.set(MainProp.PORT, p);
            mprop.set(MainProp.SERVERPORT, p);
            break;
          case 'P':
            System.setProperty(DBPASS, arg.string());
            break;
          case 'S':
            if(serial.length() != 0) serial.append(',');
            serial.append(arg);
            break;
          case 'U':
            System.setProperty(DBUSER, arg.string());
            break;
          case 'W':
            webdav = false;
            break;
          case 'z':
            quiet = true;
            break;
          default:
            arg.usage();
        }
      } else {
        if(arg.string().equalsIgnoreCase("stop")) {
          stopped = true;
        } else {
          arg.usage();
        }
      }
    }
    if(serial.length() != 0) {
      http.context.prop.set(Prop.SERIALIZER, serial.toString());
    }
    http.update();
  }
}
