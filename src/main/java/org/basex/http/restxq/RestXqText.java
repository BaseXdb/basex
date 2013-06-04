package org.basex.http.restxq;

import static org.basex.util.Token.*;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public interface RestXqText {
  /** Token "path". */
  byte[] PATH = token("path");
  /** Token "produces". */
  byte[] PRODUCES = token("produces");
  /** Token "consumes". */
  byte[] CONSUMES = token("consumes");
  /** Token "query-param". */
  byte[] QUERY_PARAM = token("query-param");
  /** Token "form-param". */
  byte[] FORM_PARAM = token("form-param");
  /** Token "header-param". */
  byte[] HEADER_PARAM = token("header-param");
  /** Token "cookie-param". */
  byte[] COOKIE_PARAM = token("cookie-param");

  /** Token "header". */
  byte[] HEADER = token("header");
  /** Token "response". */
  byte[] RESPONSE = token("response");
  /** Token "status". */
  byte[] STATUS = token("status");
  /** Token "reason". */
  byte[] REASON = token("reason");
  /** Token "message". */
  byte[] MESSAGE = token("message");
  /** Token "name". */
  byte[] NAME = token("name");
  /** Token "value". */
  byte[] VALUE = token("value");
  /** Token "redirect". */
  byte[] REDIRECT = token("redirect");
  /** Token "forward". */
  byte[] FORWARD = token("forward");

  /** WADL namespace. */
  String WADL_URI = "http://wadl.dev.java.net/2009/02";
  /** XHTML namespace. */
  String XHTML_URL = "http://www.w3.org/1999/xhtml";

  /** Error message. */
  String ANN_MISSING = "Annotation % is missing.";
  /** Error message. */
  String ANN_TWICE = "Annotation %% is specified more than once.";
  /** Error message. */
  String ANN_UNKNOWN = "Annotation %% is invalid or not supported.";
  /** Error message. */
  String ANN_PARAMS = "Annotation %% requires at least % parameter(s).";
  /** Error message. */
  String ANN_STRING = "Single string expected for %%, found: %.";
  /** Error message. */
  String INV_TEMPLATE = "Invalid path template: \"%\".";
  /** Error message. */
  String INV_VARNAME = "Invalid variable name: $%.";
  /** Error message. */
  String INV_VARTYPE = "Variable $% must inherit from %.";
  /** Error message. */
  String UNKNOWN_VAR = "Variable $% is not specified as argument.";
  /** Error message. */
  String VAR_ASSIGNED = "Variable $% is specified more than once.";
  /** Error message. */
  String VAR_UNDEFINED = "Variable $% is not assigned by the annotations.";
  /** Error message. */
  String UNKNOWN_SER = "Unknown serialization parameter \"%\".";
  /** Error message. */
  String UNEXP_NODE = "Unexpected node: %.";
  /** Error message. */
  String HEAD_METHOD = "HEAD method must only return one 'restxq:response' element.";
  /** Error message. */
  String METHOD_VALUE = "Method % does not allow values.";
  /** Error message. */
  String INPUT_CONV = "Input could not be converted: %";
  /** Error message. */
  String PATH_CONFLICT = "Several functions assigned to path \"%\":%";
  /** Error message. */
  String NO_VALUE = "'%' element has no string value.";
}
