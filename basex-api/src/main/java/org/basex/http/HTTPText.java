package org.basex.http;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public interface HTTPText {
  /** HTTP String. */
  String HTTP = "HTTP";
  /** Websocket String. */
  String WS = "WebSocket";
  /** WEB-INF directory. */
  String WEB_INF = "WEB-INF/";
  /** Path to jetty configuration file. */
  String JETTYCONF = WEB_INF + "jetty.xml";
  /** Path to web configuration file. */
  String WEBCONF = WEB_INF + "web.xml";

  /** Authentication error. */
  String WRONGAUTH_X = "% authentication expected.";
  /** Unexpected error. */
  String UNEXPECTED_X = "Unexpected error: %";
}
