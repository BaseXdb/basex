package org.basex.http;

/**
 * This enumeration contains the supported HTTP methods.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public enum HTTPMethod {
  /** GET method. */
  GET,
  /** POST method. */
  POST(true),
  /** PUT method. */
  PUT(true),
  /** DELETE method. */
  DELETE,
  /** HEAD method. */
  HEAD,
  /** OPTIONS method. */
  OPTIONS;

  /** Flag showing, if body can be present in the HTTP request with the current method. */
  public final boolean bodyAllowed;

  private HTTPMethod() { this.bodyAllowed = false; }
  private HTTPMethod(final boolean bodyAllowed) { this.bodyAllowed = bodyAllowed; }

  /**
   * Finds the specified method, or returns {@code null}.
   * @param name method name
   * @return method
   */
  public static HTTPMethod get(final String name) {
    for(final HTTPMethod m : values()) if(m.name().equals(name)) return m;
    return null;
  }
}
