package org.basex.http;

import static javax.servlet.http.HttpServletResponse.*;

/**
 * Enumeration with HTTP codes and error messages.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public enum HTTPCode {
  /** Error: 201 (created). */
  CREATED_X(SC_CREATED, "%"),

  /** Error: 400 (bad request). */
  BAD_REQUEST_X(SC_BAD_REQUEST, "%"),
  /** Error 400, "Unknown parameter". */
  UNKNOWN_PARAM_X(SC_BAD_REQUEST, "Unknown parameter: %."),
  /** Error 400, "Multiple operations supplied". */
  MULTIPLE_OPS_X(SC_BAD_REQUEST, "Multiple operations supplied: %."),
  /** Error 400, "Multiple contexts supplied.". */
  MULTIPLE_CONTEXTS(SC_BAD_REQUEST, "Multiple contexts supplied."),

  /** Error: 404 (not found). */
  NOT_FOUND_X(SC_NOT_FOUND, "%"),
  /** Error: 404, "No path specified". */
  NO_DATABASE_SPECIFIED(SC_NOT_FOUND, "No database specified."),
  /** Error: 404, "Service not found". */
  SERVICE_NOT_FOUND(SC_NOT_FOUND, "Service not found."),

  /** Error: 500, "RESTXQ path cannot be resolved.". */
  NO_RESTXQ_DIRECTORY(SC_INTERNAL_SERVER_ERROR, "RESTXQ directory does not exist."),
  /** Error 501, "Method not supported.". */
  METHOD_NOT_SUPPORTED_X(SC_NOT_IMPLEMENTED, "Method not supported: %.");

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
