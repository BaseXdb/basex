package org.basex.api.restxq;

import static org.basex.util.Token.*;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
interface RestXqText {
  /** Annotation "path". */
  byte[] PATH = token("path");
  /** Annotation "GET". */
  byte[] GET = token("GET");
  /** Annotation "POST". */
  byte[] POST = token("POST");
  /** Annotation "PUT". */
  byte[] PUT = token("PUT");
  /** Annotation "DELETE". */
  byte[] DELETE = token("DELETE");
  /** Annotation "produces". */
  byte[] PRODUCES = token("produces");
  /** Annotation "consumes". */
  byte[] CONSUMES = token("consumes");

  /** Error message. */
  String SINGLE_STRING = "Annotation % must contain a single string.";
  /** Error message. */
  String INVALID_TEMPLATE = "Invalid path template: \"%\".";
  /** Error message. */
  String OUTPUT_STRING = "Output parameter \"%\" must contain a single string.";
  /** Error message. */
  String UNKNOWN_SER = "Unknown serialization parameter \"%\".";
  /** Error message. */
  String NOT_SUPPORTED = "Annotation % is invalid or not supported.";
  /** Error message. */
  String INVALID_VAR = "Invalid variable name: $%.";
  /** Error message. */
  String UNKNOWN_VAR = "Variable $% is not specified as argument.";
  /** Error message. */
  String VAR_ASSIGNED = "Variable $% is specified more than once.";
  /** Error message. */
  String VAR_UNDEFINED = "Variable $% is not assigned by the annotations.";
  /** Error message. */
  String ANN_MISSING = "Annotation % is missing.";
  /** Error message. */
  String VAR_ATOMIC = "Variable $% must inherit from %.";

  /** Error message. */
  String NOT_FOUND = "No XQuery function found to process the request.";
  /** Error message. */
  String UNEXPECTED_ERROR = "Unexpected error: %";
}
