package org.basex.http.web;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * This class assembles texts which are used in the Web classes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public interface WebText {
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
  /** Token "forward". */
  byte[] FORWARD = token("forward");

  /** Permission token. */
  String ALLOW = "allow";
  /** Permission token. */
  String PATH = "path";
  /** Permission token. */
  String METHOD = "method";
  /** Permission token. */
  String AUTHORIZATION = "authorization";

  /** WebSocket string. */
  String WEBSOCKET = "WebSocket";
  /** RESTXQ string. */
  String RESTXQ = "RESTXQ";
  /** WADL prefix. */
  String WADL = "wadl:";
  /** WADL namespace. */
  String WADL_URI = "http://wadl.dev.java.net/2009/02";
  /** XHTML namespace. */
  String XHTML_URL = "http://www.w3.org/1999/xhtml";
  /** Init call. */
  String INIT = ".init";

  /** Error message. */
  String ANN_MISSING = "Path annotation missing.";
  /** Error message. */
  String ANN_CONFLICT_X = "Conflicting annotations: %.";
  /** Error message. */
  String ANN_BODYVAR = "More than one body request variable specified.";
  /** Error message. */
  String ANN_TWICE_X_X = "Annotation %% is specified twice.";
  /** Error message. */
  String INV_TEMPLATE_X = "Invalid path template: \"%\".";
  /** Error message. */
  String INV_ENCODING_X = "Invalid URL encoding: \"%\".";
  /** Error message. */
  String INV_VARNAME_X = "Invalid variable name: $%.";
  /** Error message. */
  String INV_CODE_X = "Invalid error code: %.";
  /** Error message. */
  String INV_PRECEDENCE_X_X = "Errors must be of the same precedence (\"%\" vs \"%\").";
  /** Error message. */
  String INV_ERR_TWICE_X = "The same error has been specified twice: \"%\".";
  /** Error message. */
  String INV_NONS_X = "No namespace declared for '%'.";
  /** Error message. */
  String ARG_TYPE_X_X_X = "% must be %, supplied: %.";
  /** Error message. */
  String PARAM_MISSING_X = "Parameter $% missing in function declaration.";
  /** Error message. */
  String PARAM_DUPL_X = "$% is specified more than once.";
  /** Error message. */
  String VAR_UNDEFINED_X = "No binding defined for $%.";
  /** Error message. */
  String UNKNOWN_SER_X = "Unknown serialization parameter: %.";
  /** Error message. */
  String UNEXP_NODE_X = "Unexpected node: %.";
  /** Error message. */
  String HEAD_METHOD = "HEAD method must return a single 'restxq:response' element.";
  /** Error message. */
  String METHOD_VALUE_X = "Method % does not allow values.";
  /** Error message. */
  String BODY_TYPE_X_X = "Request body must be of type \"%\", supplied: %";
  /** Error message. */
  String PATH_CONFLICT_X_X = "Multiple services defined for path \"%\":%";
  /** Error message. */
  String ERROR_CONFLICT_X_X = "Multiple services defined for error \"%\":%";
  /** Error message. */
  String ERROR_QS_X = "Invalid quality factor: qs=%";
  /** Error message. */
  String NO_VALUE_X = "'%' element has no string value.";

  /** QName. */
  QNm Q_STATUS = new QNm(STATUS);
  /** QName. */
  QNm Q_REASON = new QNm(REASON);
  /** QName. */
  QNm Q_MESSAGE = new QNm(MESSAGE);
  /** QName. */
  QNm Q_NAME = new QNm(NAME);
  /** QName. */
  QNm Q_VALUE = new QNm(VALUE);

  /** Serializer node test. */
  NameTest OUTPUT_SERIAL = new NameTest(FuncOptions.Q_SPARAM);
  /** HTTP Response test. */
  NameTest HTTP_RESPONSE = new NameTest(new QNm(RESPONSE, HTTP_URI));
  /** RESTXQ Response test. */
  NameTest REST_RESPONSE = new NameTest(new QNm(RESPONSE, REST_URI));
  /** RESTXQ Forward test. */
  NameTest REST_FORWARD = new NameTest(new QNm(FORWARD, REST_URI));
  /** HTTP Header test. */
  NameTest HTTP_HEADER = new NameTest(new QNm(HEADER, HTTP_URI));
}
