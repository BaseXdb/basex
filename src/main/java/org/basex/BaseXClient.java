package org.basex;

import static org.basex.core.Text.*;
import java.io.IOException;
import org.basex.server.ClientSession;
import org.basex.server.Session;
import org.basex.util.Util;

/**
 * This is the starter class for the client console mode.
 * All input is sent to the server instance.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class BaseXClient extends BaseX {
  /**
   * Main method of the database client, launching a client instance.
   * Command-line arguments are listed with the {@code -h} argument.
   * @param args command-line arguments
   */
  public static void main(final String... args) {
    new BaseXClient(args);
  }

  /**
   * Constructor.
   * @param args command-line arguments
   */
  public BaseXClient(final String... args) {
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
      while(user == null) {
        Util.out(SERVERUSER + COLS);
        user = input();
      }
      while(pass == null) {
        Util.out(SERVERPW + COLS);
        pass = password();
      }
      session = new ClientSession(context, user, pass, out);
    }
    return session;
  }
}
