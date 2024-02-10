package org.basex.util.http;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.value.item.*;

/**
 * HTTP constants.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Rositsa Shadura
 */
public interface HTTPText {
  /** HTTP string. */
  String HTTP = "HTTP";

  /** WEB-INF directory. */
  String WEB_INF = "WEB-INF/";
  /** Path to jetty configuration file. */
  String JETTYCONF = WEB_INF + "jetty.xml";
  /** Path to web configuration file. */
  String WEBCONF = WEB_INF + "web.xml";

  /** Authentication error. */
  String WRONGAUTH_X = "% authentication expected.";
  /** Unexpected error. */
  String UNEXPECTED_X = "Unexpected error: %";

  /** DBA client id. */
  String DBA_CLIENT_ID = "dba";
  /** Client id. */
  String CLIENT_ID = "id";
  /** Forwarded request context. */
  String FORWARD = "forward";

  /** HTTP header string. */
  String WWW_AUTHENTICATE = "WWW-Authenticate";
  /** HTTP header string. */
  String AUTHORIZATION = "Authorization";
  /** HTTP header string. */
  String CONTENT_ENCODING = "Content-Encoding";
  /** HTTP header string. */
  String CONTENT_TYPE = "Content-Type";
  /** HTTP header string. */
  String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
  /** HTTP header string. */
  String CACHE_CONTROL = "Cache-Control";
  /** HTTP header string. */
  String SERVER_TIMING = "Server-Timing";
  /** HTTP header string. */
  String PRAGMA = "Pragma";
  /** HTTP header string. */
  String EXPIRES = "Expires";
  /** HTTP header string. */
  String LOCATION = "Location";
  /** HTTP header string. */
  String ACCEPT = "Accept";
  /** HTTP header string. */
  String ALLOW = "Allow";

  /** Content-Disposition (lower case). */
  byte[] CONTENT_DISPOSITION = token("content-disposition");
  /** Dashes. */
  byte[] DASHES = token("--");

  /** Character string. */
  String CHARSET = "charset";

  /** Default multipart boundary. */
  String DEFAULT_BOUNDARY = "1BEF0A57BE110FD467A";
  /** Boundary marker. */
  String BOUNDARY = "boundary";
  /** Body attribute: src. */
  String SRC = "src";

  /** MD5. */
  String MD5 = "MD5";
  /** MD5-sess. */
  String MD5_SESS = MD5 + "-sess";
  /** Auth. */
  String AUTH = "auth";
  /** Auth-int. */
  String AUTH_INT = "auth-int";

  /** Content encoding: gzip. */
  String GZIP = "gzip";

  /** QName. */
  QNm Q_REST_RESPONSE = new QNm(REST_PREFIX, "response", REST_URI);
  /** QName. */
  QNm Q_REST_FORWARD = new QNm(REST_PREFIX, "forward", REST_URI);

  /** QName. */
  QNm Q_HTTP_BODY = new QNm(HTTP_PREFIX, "body", HTTP_URI);
  /** QName. */
  QNm Q_HTTP_RESPONSE = new QNm(HTTP_PREFIX, "response", HTTP_URI);
  /** QName. */
  QNm Q_HTTP_HEADER = new QNm(HTTP_PREFIX, "header", HTTP_URI);
  /** QName. */
  QNm Q_HTTP_MULTIPART = new QNm(HTTP_PREFIX, "multipart", HTTP_URI);

  /** QName. */
  QNm Q_BOUNDARY = new QNm("boundary");
  /** QName. */
  QNm Q_STATUS = new QNm("status");
  /** QName. */
  QNm Q_MESSAGE = new QNm("message");
  /** QName. */
  QNm Q_MEDIA_TYPE = new QNm("media-type");
  /** QName. */
  QNm Q_NAME = new QNm("name");
  /** QName. */
  QNm Q_VALUE = new QNm("value");

  /** Binary string. */
  String BINARY = "binary";
  /** Base64 string. */
  String BASE64 = "base64";

  /** Carriage return/line feed. */
  byte[] CRLF = { '\r', '\n' };
}
