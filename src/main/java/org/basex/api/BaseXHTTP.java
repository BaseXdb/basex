package org.basex.api;

import static org.basex.api.HTTPText.*;
import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.BaseXServer;
import org.basex.api.webdav.WebDAVServer;
import org.basex.core.MainProp;
import org.basex.core.Prop;
import org.basex.util.Args;
import org.basex.util.Util;

/**
 * This is the abstract main class for the API starter classes.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class BaseXHTTP {
  /** Database context. */
  private final HTTPContext http = HTTPContext.get();
  /** Database server. */
  private BaseXServer server;
  /** WebDAV server. */
  private WebDAVServer webdav;
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

    if(http.client) server = new BaseXServer(http.context, quiet ? "-z" : "");
    webdav = new WebDAVServer(http);
    //jaxrx = new JaxRxServer(http);
  }

  /**
   * Stops the server.
   * @throws Exception exception
   */
  public void stop() throws Exception {
    if(webdav != null) webdav.stop();
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
            http.client = true;
            System.setProperty(DBCLIENT, Boolean.TRUE.toString());
            break;
          case 'd':
            mprop.set(MainProp.DEBUG, true);
            break;
          case 'e':
            mprop.set(MainProp.EVENTPORT, arg.num());
            break;
          case 'j':
            mprop.set(MainProp.JAXRXPORT, arg.num());
            break;
          case 'n':
            mprop.set(MainProp.HOST, arg.num());
            break;
          case 'p':
            final int p = arg.num();
            mprop.set(MainProp.PORT, p);
            mprop.set(MainProp.SERVERPORT, p);
            System.setProperty(DBPORT, Integer.toString(p));
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
          case 'w':
            mprop.set(MainProp.WEBDAVPORT, arg.num());
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
  }
}
