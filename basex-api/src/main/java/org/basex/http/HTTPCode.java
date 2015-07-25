package org.basex.http;

import static javax.servlet.http.HttpServletResponse.*;

/**
 * Enumeration with HTTP codes and error messages.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public enum HTTPCode {
  /** Error: 201 (created). */
  CREATED_X(SC_CREATED, "%"),

  /** Error: 400 (bad request). */
  BAD_REQUEST_X(SC_BAD_REQUEST, "%"),
  /** Error 400, "Only one operation can be specified". */
  ONEOP(SC_BAD_REQUEST, "Only one operation can be specified."),
  /** Error 400, "Unknown parameter: '%'". */
  UNKNOWN_PARAM_X(SC_BAD_REQUEST, "Unknown parameter: '%'."),
  /** Error 400, "Multiple context values specified.". */
  MULTIPLE_CONTEXT_X(SC_BAD_REQUEST, "Multiple context values specified."),

  /** Error: 404 (not found). */
  NOT_FOUND_X(SC_NOT_FOUND, "%"),
  /** Error: 404, "No path specified.". */
  NO_PATH(SC_NOT_FOUND, "No path specified."),
  /** Error: 404, "No function found to process the request.". */
  NO_XQUERY(SC_NOT_FOUND, "No function found that matches the request."),
  /** Error: 404, "RESTXQ directory not found.". */
  NO_RESTXQ(SC_NOT_FOUND, "RESTXQ directory not found."),

  /** Error 501, "Method not supported: %.". */
  NOT_IMPLEMENTED_X(SC_NOT_IMPLEMENTED, "Method not supported: %.");

  /** Status code. */
  final int code;
  /** Error description. */
  final String desc;

  /**
   * Constructor.
   * @param code status code
   * @param desc description
   */
  HTTPCode(final int code, final String desc) {
    this.code = code;
    this.desc = desc;
  }

  /**
   * Returns an HTTP exception.
   * @param ext extended info
   * @return HTTP exception
   */
  public HTTPException get(final Object... ext) {
    return new HTTPException(this, ext);
  }
}
