package org.basex.http;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public interface HTTPText {
  /** WWW-Authentication string. */
  String WWW_AUTHENTICATE = "WWW-Authenticate";
  /** HTTP header: Authorization. */
  String AUTHORIZATION = "Authorization";
  /** HTTP basic authentication. */
  String BASIC = "Basic";
  /** HTTP digest authentication. */
  String DIGEST = "Digest";

  /** HTTP String. */
  String HTTP = "HTTP";
  /** WEB-INF directory. */
  String WEB_INF = "WEB-INF/";
  /** Path to jetty configuration file. */
  String JETTYCONF = WEB_INF + "jetty.xml";
  /** Path to web configuration file. */
  String WEBCONF = WEB_INF + "web.xml";

  /** Error: no password. */
  String NOPASSWD = "No username/password specified.";
  /** Error: unsupported authorization method. */
  String WHICHAUTH = "Unsupported Authorization method: %.";
  /** Error message. */
  String UNEXPECTED = "Unexpected error: %";
}
