package org.basex.http.web;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;

/**
 * This class assembles texts which are used in the Web classes.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public interface WebText {
  /** WADL prefix. */
  byte[] WADL_PREFIX = token("wadl");
  /** WADL namespace. */
  byte[] WADL_URI = token("http://wadl.dev.java.net/2009/02");

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
  String INV_ENCODING_X = "URL is invalid: \"%\".";
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
  String UNKNOWN_PARAMETER_X = "%";
  /** Error message. */
  String UNEXP_NODE_X = "Unexpected node: %.";
  /** Error message. */
  String HEAD_METHOD = "HEAD method must return a single 'restxq:response' element.";
  /** Error message. */
  String METHOD_VALUE_X = "Method % does not allow values.";
  /** Error message. */
  String BODY_TYPE_X_X = "Body cannot be parsed as %: %.";
  /** Error message. */
  String PATH_CONFLICT_X_X = "Multiple services defined for path \"%\":%";
  /** Error message. */
  String ERROR_CONFLICT_X_X = "Multiple services defined for error \"%\":%";
  /** Error message. */
  String ERROR_QS_X = "Invalid quality factor: qs=%";
  /** Error message. */
  String NO_VALUE_X = "'%' element has no string value.";

  /** QName. */
  QNm Q_STATUS = new QNm("status");
  /** QName. */
  QNm Q_REASON = new QNm("reason");
  /** QName. */
  QNm Q_MESSAGE = new QNm("message");
  /** QName. */
  QNm Q_NAME = new QNm("name");
  /** QName. */
  QNm Q_VALUE = new QNm("value");
  /** QName. */
  QNm Q_TYPE = new QNm("type");
  /** QName. */
  QNm Q_STYLE = new QNm("style");
  /** QName. */
  QNm Q_BASE = new QNm("base");
  /** QName. */
  QNm Q_PATH = new QNm("path");
  /** QName. */
  QNm Q_MEDIA_TYPE = new QNm("mediaType");

  /** Serializer node test. */
  NameTest T_OUTPUT_SERIAL = new NameTest(FuncOptions.Q_SERIALIZTION_PARAMETERS);
  /** HTTP Response test. */
  NameTest T_HTTP_RESPONSE = new NameTest(new QNm("response", HTTP_URI));
  /** RESTXQ Response test. */
  NameTest T_REST_RESPONSE = new NameTest(new QNm("response", REST_URI));
  /** RESTXQ Forward test. */
  NameTest T_REST_FORWARD = new NameTest(new QNm("forward", REST_URI));
  /** HTTP Header test. */
  NameTest T_HTTP_HEADER = new NameTest(new QNm("header", HTTP_URI));
}
