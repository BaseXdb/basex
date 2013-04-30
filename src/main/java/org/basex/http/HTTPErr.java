package org.basex.http;

import static javax.servlet.http.HttpServletResponse.*;

/**
 * This class contains all query error messages.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public enum HTTPErr {
  /** Error: 201 (created). */
  CREATED_X(SC_CREATED, "%"),

  /** Error: 400 (bad request). */
  BAD_REQUEST_X(SC_BAD_REQUEST, "%"),
  /** Error 400, "Only one operation can be specified". */
  ONEOP(SC_BAD_REQUEST, "Only one operation can be specified."),
  /** Error 400, "Unknown parameter: '%'". */
  UNKNOWN_PARAM_X(SC_BAD_REQUEST, "Unknown parameter: '%'."),
  /** Error 400, "Invalid parameters: '%'". */
  INVALID_PARAM_X(SC_BAD_REQUEST, "Parameters cannot be decoded: %."),
  /** Error 400, "Multiple context items specified.". */
  MULTIPLE_CONTEXT_X(SC_BAD_REQUEST, "Multiple context items specified."),

  /** Error: 404 (not found). */
  NOT_FOUND_X(SC_NOT_FOUND, "%"),
  /** Error: 404, "No path specified.". */
  NO_PATH(SC_NOT_FOUND, "No path specified."),
  /** Error: 404, "No function found to process the request.". */
  NO_XQUERY(SC_NOT_FOUND, "No function found that matches the request."),

  /** Error 501, "Method not supported: %.". */
  NOT_IMPLEMENTED_X(SC_NOT_IMPLEMENTED, "Method not supported: %.");

  /** Status code. */
  final int code;
  /** Error description. */
  final String desc;

  /**
   * Constructor.
   * @param c status code
   * @param d description
   */
  private HTTPErr(final int c, final String d) {
    code = c;
    desc = d;
  }

  /**
   * Throws an HTTP exception.
   * @param ext extended info
   * @return HTTP exception
   * @throws HTTPException HTTP exception
   */
  public HTTPException thrw(final Object... ext) throws HTTPException {
    throw new HTTPException(this, ext);
  }
}
