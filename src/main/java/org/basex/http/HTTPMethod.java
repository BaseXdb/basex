package org.basex.http;


/**
 * This enumeration contains the supported HTTP methods.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public enum HTTPMethod {
  /** GET method. */
  GET,
  /** POST method. */
  POST,
  /** PUT method. */
  PUT,
  /** DELETE method. */
  DELETE,
  /** HEAD method. */
  HEAD,
  /** OPTIONS method. */
  OPTIONS;

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
