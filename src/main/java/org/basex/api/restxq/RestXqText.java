package org.basex.api.restxq;

import static org.basex.util.Token.*;

import org.basex.query.item.*;
import org.basex.util.*;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class RestXqText {
  /** RESTful annotation URI. */
  static final byte[] RESTXQURI = token("http://exquery.org/ns/rest/annotation/");

  /** Annotation "path". */
  static final QNm PATH = qnm("rest:path");
  /** Annotation "GET". */
  static final QNm GET = qnm("rest:GET");
  /** Annotation "POST". */
  static final QNm POST = qnm("rest:POST");
  /** Annotation "PUT". */
  static final QNm PUT = qnm("rest:PUT");
  /** Annotation "DELETE". */
  static final QNm DELETE = qnm("rest:DELETE");
  /** Annotation "produces". */
  static final QNm PRODUCES = qnm("rest:produces");
  /** Annotation "consumes". */
  static final QNm CONSUMES = qnm("rest:consumes");

  /** Error message. */
  static final String STATIC_ERROR = "Error: %\nModule: %\nFunction: %";
  /** Error message. */
  static final String UNEXPECTED_ERROR = "Unexpected error: %";
  /** Error message. */
  static final String NOT_FOUND = "No XQuery function found to process the request.";
  /** Error message. */
  static final String SINGLE_STRING = "Annotation % must contain a single string.";
  /** Error message. */
  static final String STEP_SYNTAX = "Invalid template found: \"%\".";
  /** Error message. */
  static final String OUTPUT_STRING =
      "Output parameter \"%\" must contain a single string.";
  /** Error message. */
  static final String UNKNOWN_SER = "Unknown serialization parameter \"%\".";

  /**
   * Creates a new QName, using the RESTful annotations URI.
   * @param n name
   * @return QName
   */
  static QNm qnm(final String n) {
    return new QNm(Token.token(n), RESTXQURI);
  }
}
