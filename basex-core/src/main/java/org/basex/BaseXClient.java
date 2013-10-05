package org.basex;

import static org.basex.core.Text.*;

import java.io.*;

import org.basex.core.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * This is the starter class for the client console mode.
 * All input is sent to the server instance.
 *
 * @author BaseX Team 2005-12, BSD License
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
  protected boolean sa() {
    return false;
  }

  @Override
  protected Session session() throws IOException {
    if(session == null) {
      // user/password input
      String user = context.globalopts.get(GlobalOptions.USER);
      String pass = context.globalopts.get(GlobalOptions.PASSWORD);
      while(user.isEmpty()) {
        Util.out(USERNAME + COLS);
        user = Util.input();
      }
      while(pass.isEmpty()) {
        Util.out(PASSWORD + COLS);
        pass = Util.password();
      }
      session = new ClientSession(context, user, pass, out);
    }
    return session;
  }
}
