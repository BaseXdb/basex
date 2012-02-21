package org.basex.api;

import static org.basex.api.HTTPText.*;

import java.io.*;

import javax.servlet.http.*;

import org.basex.core.*;
import org.basex.server.*;
import org.basex.util.*;

/**
 * This class offers methods for managing login data and creating database
 * sessions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class HTTPSession {
  /** Database context. */
  private static final Context CONTEXT = new Context();
  /** User name. */
  private String user;
  /** Password. */
  private String pass;

  /**
   * Constructor.
   * @param req HTTP servlet request
   * @throws LoginException login exception
   */
  public HTTPSession(final HttpServletRequest req) throws LoginException {
    this(login(req));
  }

  /**
   * Constructor.
   * @param u user
   * @param p password
   */
  public HTTPSession(final String u, final String p) {
    this(new String[] { u, p });
  }

  /**
   * Constructor.
   * @param login login data
   */
  private HTTPSession(final String[] login) {
    final String suser = System.getProperty(DBUSER);
    final String spass = System.getProperty(DBPASS);
    user = suser != null ? suser : login[0];
    pass = spass != null ? spass : login[1];
  }

  /**
   * Updates the user and password combination.
   * @param u user
   * @param p password
   */
  public void update(final String u, final String p) {
    user = u;
    pass = p;
  }

  /**
   * Returns a new session instance. By default, a {@link LocalSession}
   * instance will be generated. A {@link ClientSession} will be created
   * if "client" has been chosen an operation mode.
   * @return session
   * @throws IOException I/O exception
   */
  public Session login() throws IOException {
    if(user == null || pass == null) throw new LoginException(NOPASSWD);
    return CLIENT.equals(System.getProperty(DBMODE)) ?
        new ClientSession(CONTEXT, user, pass) :
        new LocalSession(CONTEXT, user, pass);
  }

  /**
   * Returns the static database context.
   * @return database context
   */
  public static Context context() {
    return CONTEXT;
  }

  /**
   * Returns login data from the HTTP header.
   * @param req servlet request
   * @return login/password combination, or two {@code null} strings
   * @throws LoginException login exception
   */
  private static String[] login(final HttpServletRequest req)
      throws LoginException {

    final String auth = req.getHeader(AUTHORIZATION);
    if(auth != null) {
      final String[] values = auth.split(" ");
      if(values[0].equals(BASIC)) {
        final String[] cred = Base64.decode(values[1]).split(":", 2);
        if(cred.length != 2) throw new LoginException(NOPASSWD);
        return cred;
      }
      throw new LoginException(WHICHAUTH, values[0]);
    }
    return new String[2];
  }
}
