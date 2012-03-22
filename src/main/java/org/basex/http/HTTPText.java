package org.basex.http;

import org.basex.core.*;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public interface HTTPText {
  /** WWW-Authentication string. */
  String WWW_AUTHENTICATE = "WWW-Authenticate";
  /** HTTP header: Authorization. */
  String AUTHORIZATION = "Authorization";
  /** HTTP basic authentication. */
  String BASIC = "Basic";

  /** HTTP String. */
  String HTTP = "HTTP";

  /** Configuration: database user. */
  String DBUSER = Prop.DBPREFIX + "user";
  /** Configuration: database user password. */
  String DBPASS = Prop.DBPREFIX + "password";
  /** Configuration: operation mode: "local", "client" or default ({@code null}). */
  String DBMODE = Prop.DBPREFIX + "mode";
  /** Configuration: verbose mode. */
  String DBVERBOSE = Prop.DBPREFIX + "verbose";

  /** Mode: local. */
  String LOCAL = "local";
  /** Mode: client. */
  String CLIENT = "client";

  /** Error: no password. */
  String NOPASSWD = "No username/password specified.";
  /** Error: unsupported authorization method. */
  String WHICHAUTH = "Unsupported Authorization method: %.";
  /** Error: only allow local or client mode. */
  String INVMODE = "You cannot use both local and client mode.";
  /** Error message. */
  String UNEXPECTED = "Unexpected error: %";
}
