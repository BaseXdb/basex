package org.basex.http.ws.stomp;

/**
 * The Exception witch is thrown if not all required Headers are set.
 * @author BaseX Team 2005-18, BSD License
 */
public class HeadersException extends Exception {

  /**
   * Constructor.
   * */
  public HeadersException() {
    super();
  }

  /**
   * Constructor.
   * @param message String
   * */
  public HeadersException(final String message) {
    super(message);
  }
}