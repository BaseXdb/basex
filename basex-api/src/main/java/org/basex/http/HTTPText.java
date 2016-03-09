package org.basex.http;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public interface HTTPText {
  /** HTTP String. */
  String HTTP = "HTTP";
  /** WEB-INF directory. */
  String WEB_INF = "WEB-INF/";
  /** Path to jetty configuration file. */
  String JETTYCONF = WEB_INF + "jetty.xml";
  /** Path to web configuration file. */
  String WEBCONF = WEB_INF + "web.xml";

  /** Error: credentials missing. */
  String NOUSERNAME = "No username specified.";
  /** Error: unsupported authorization method. */
  String WHICHAUTH = "Unsupported authentication method: %.";
  /** Error: digest authorization. */
  String DIGESTAUTH = "Digest authentication expected.";
  /** Error message. */
  String UNEXPECTED = "Unexpected error: %";
}
