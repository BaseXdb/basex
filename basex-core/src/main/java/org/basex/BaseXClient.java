package org.basex;

import static org.basex.core.Text.*;

import java.io.*;
import java.net.*;

import org.basex.api.client.*;
import org.basex.core.*;
import org.basex.util.*;

/**
 * This is the starter class for the client console mode.
 * All input is sent to the server instance.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class BaseXClient extends BaseX {
  /**
   * Main method of the database client, launching a client instance.
   * Command-line arguments are listed with the {@code -h} argument.
   * @param args command-line arguments
   */
  public static void main(final String... args) {
    try {
      new BaseXClient(args);
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
  public BaseXClient(final String... args) throws IOException {
    super(args);
  }

  @Override
  protected boolean local() {
    return false;
  }

  @Override
  protected Session init() throws IOException {
    // user/password input
    String user = context.soptions.get(StaticOptions.USER);
    String pass = context.soptions.get(StaticOptions.PASSWORD);
    while(user.isEmpty()) {
      Util.out(USERNAME + COLS);
      user = Util.input();
    }
    while(pass.isEmpty()) {
      Util.out(PASSWORD + COLS);
      pass = Util.password();
    }

    final String host = context.soptions.get(StaticOptions.HOST);
    final int port = context.soptions.get(StaticOptions.PORT);
    try {
      return new ClientSession(host, port, user, pass, out);
    } catch(final ConnectException ex) {
      throw new BaseXException(CONNECTION_ERROR_X, port);
    }
  }
}
