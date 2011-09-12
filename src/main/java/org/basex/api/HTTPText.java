package org.basex.api;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public interface HTTPText {
  /** HTTP String. */
  String HTTP = "HTTP";
  /** Servlet string. */
  String SERVLET = "Servlet";

  /** Configuration: client flag. */
  String DBCLIENT = "org.basex.client";
  /** Configuration: database user. */
  String DBUSER = "org.basex.user";
  /** Configuration: database user password. */
  String DBPASS = "org.basex.password";

  /** Authorization string. */
  String AUTHORIZATION = "Authorization";
  /** Basic string. */
  String BASIC = "Basic";

  /** Error: no password. */
  String NOPASSWD = "No username/password specified.";
  /** Error: unsupported authorization method. */
  String WHICHAUTH = "Unsupported authorization method: %.";

}
