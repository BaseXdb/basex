package org.basex.http.restxq;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public interface RestXqText {
  /** Token "error". */
  byte[] ERROR = token("error");
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
  /** Token "query-param". */
  byte[] ERROR_PARAM = token("error-param");
  /** Token "method". */
  byte[] METHOD = token("method");

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

  /** WADL prefix. */
  String WADL = "wadl:";
  /** WADL namespace. */
  String WADL_URI = "http://wadl.dev.java.net/2009/02";
  /** XHTML namespace. */
  String XHTML_URL = "http://www.w3.org/1999/xhtml";

  /** Error message. */
  String ANN_MISSING = "Annotation %% or %% missing.";
  /** Error message. */
  String ANN_BODYVAR = "More than one body request variable specified.";
  /** Error message. */
  String ANN_TWICE = "Annotation %% is specified more than once.";
  /** Error message. */
  String ANN_UNKNOWN = "Annotation %% is invalid or not supported.";
  /** Error message. */
  String ANN_ATLEAST = "Annotation %% requires at least % parameter(s).";
  /** Error message. */
  String ANN_STRING = "Single string expected for %%, found: %.";
  /** Error message. */
  String INV_TEMPLATE = "Invalid path template: \"%\".";
  /** Error message. */
  String INV_VARNAME = "Invalid variable name: $%.";
  /** Error message. */
  String INV_CODE = "Invalid error code: '%'.";
  /** Error message. */
  String INV_PRIORITY = "Errors must be of the same priority (\"%\" vs \"%\").";
  /** Error message. */
  String INV_ERR_SAME = "The same error has been specified twice: \"%\".";
  /** Error message. */
  String INV_NONS = "No namespace declared for '%'.";
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
  String HEAD_METHOD = "HEAD method must return a single 'restxq:response' element.";
  /** Error message. */
  String METHOD_VALUE = "Method % does not allow values.";
  /** Error message. */
  String INPUT_CONV = "Input could not be converted: %";
  /** Error message. */
  String PATH_CONFLICT = "Path \"%\" assigned to several functions:%";
  /** Error message. */
  String ERROR_CONFLICT = "Error \"%\" matched by several functions:%";
  /** Error message. */
  String NO_VALUE = "'%' element has no string value.";

  /** QName. */
  QNm Q_STATUS = QNm.get(STATUS);
  /** QName. */
  QNm Q_REASON = QNm.get(REASON);
  /** QName. */
  QNm Q_MESSAGE = QNm.get(MESSAGE);
  /** QName. */
  QNm Q_NAME = QNm.get(NAME);
  /** QName. */
  QNm Q_VALUE = QNm.get(VALUE);

  /** Serializer node test. */
  NodeTest OUTPUT_SERIAL = new NodeTest(FuncOptions.Q_SPARAM);
  /** HTTP Response test. */
  NodeTest HTTP_RESPONSE = new NodeTest(QNm.get(RESPONSE, HTTP_URI));
  /** RESTXQ Response test. */
  NodeTest REST_RESPONSE = new NodeTest(QNm.get(RESPONSE, REST_URI));
  /** RESTXQ Redirect test. */
  NodeTest REST_REDIRECT = new NodeTest(QNm.get(REDIRECT, REST_URI));
  /** RESTXQ Forward test. */
  NodeTest REST_FORWARD = new NodeTest(QNm.get(FORWARD, REST_URI));
  /** HTTP Header test. */
  NodeTest HTTP_HEADER = new NodeTest(QNm.get(HEADER, HTTP_URI));
}
