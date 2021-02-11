package org.basex.util.http;

/**
 * This enumeration contains basic HTTP methods.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public enum HttpMethod {
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

  /** Flag showing if body can be present in the HTTP request with the current method. */
  public final boolean body;

  /** Default constructor. */
  HttpMethod() {
    this(false);
  }

  /**
   * Constructor, specifying a body flag.
   * @param body body flag
   */
  HttpMethod(final boolean body) {
    this.body = body;
  }

  /**
   * Returns an enum for the specified string.
   * @param name method name
   * @return enum or {@code null}
   */
  public static HttpMethod get(final String name) {
    for(final HttpMethod method : values()) {
      if(method.name().equals(name)) return method;
    }
    return null;
  }
}
