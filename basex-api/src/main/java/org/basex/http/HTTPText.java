package org.basex.http;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public interface HTTPText {
  /** WWW-Authentication string. */
  String WWW_AUTHENTICATE = "WWW-Authenticate";
  /** HTTP header: Authorization. */
  String AUTHORIZATION = "Authorization";
  /** HTTP header: Accept. */
  String ACCEPT = "Accept";

  /** HTTP String. */
  String HTTP = "HTTP";
  /** WEB-INF directory. */
  String WEB_INF = "WEB-INF/";
  /** Path to jetty configuration file. */
  String JETTYCONF = WEB_INF + "jetty.xml";
  /** Path to web configuration file. */
  String WEBCONF = WEB_INF + "web.xml";

  /** Error: no password. */
  String INVALIDCREDS = "No username/password specified.";
  /** Error: unsupported authorization method. */
  String WHICHAUTH = "Unsupported Authorization method: %.";
  /** Error message. */
  String UNEXPECTED = "Unexpected error: %";

  // Digest keys

  /** User name. */
  String USERNAME = "username";
  /** Response. */
  String RESPONSE = "response";
  /** Realm. */
  String REALM = "realm";
  /** Nonce. */
  String NONCE = "nonce";
  /** Client nonce. */
  String CNONCE = "cnonce";
  /** Nonce counter. */
  String NC = "nc";
  /** Algorithm. */
  String ALGORITHM = "algorithm";
  /** QOP. */
  String MD5_SESS = "md5-sess";
  /** URI. */
  String URI = "uri";
  /** QOP. */
  String QOP = "qop";
  /** Auth. */
  String AUTH = "auth";
  /** Auth-int. */
  String AUTH_INT = "auth-int";
}
