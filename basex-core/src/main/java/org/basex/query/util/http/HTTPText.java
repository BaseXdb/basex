package org.basex.query.util.http;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.value.item.*;

/**
 * HTTP strings.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Rositsa Shadura
 */
public interface HTTPText {
  /** HTTP header: Authorization. */
  String AUTHORIZATION = "Authorization";
  /** HTTP basic authentication. */
  String BASIC = "Basic";

  /** Payload string. */
  String PAYLOAD = "payload";

  /** Content-Disposition. */
  byte[] CONTENT_DISPOSITION = token("Content-Disposition");
  /** Dashes. */
  byte[] DASHES = token("--");
  /** Character string. */
  String CHARSET_IS = "charset=";
  /** Boundary string. */
  String BOUNDARY_IS = "boundary=";
  /** Name string. */
  String NAME_IS = "name=";
  /** Filename string. */
  String FILENAME_IS = "filename=";

  /** Module prefix. */
  String PREFIX = "http";
  /** QName. */
  QNm Q_BODY = QNm.get(PREFIX, "body", HTTPURI);
  /** QName. */
  QNm Q_RESPONSE = QNm.get(PREFIX, "response", HTTPURI);
  /** QName. */
  QNm Q_HEADER = QNm.get(PREFIX, "header", HTTPURI);
  /** QName. */
  QNm Q_MULTIPART = QNm.get(PREFIX, "multipart", HTTPURI);

  /** Request attribute: HTTP method. */
  byte[] METHOD = token("method");
  /** Request attribute: username. */
  byte[] USERNAME = token("username");
  /** Request attribute: password. */
  byte[] PASSWORD = token("password");
  /** Request attribute: send-authorization. */
  byte[] SEND_AUTHORIZATION = token("send-authorization");

  /** Request attribute: href. */
  byte[] HREF = token("href");
  /** Request attribute: status-only. */
  byte[] STATUS_ONLY = token("status-only");
  /** Request attribute: override-media-type. */
  byte[] OVERRIDE_MEDIA_TYPE = token("override-media-type");
  /** Request attribute: follow-redirect. */
  byte[] FOLLOW_REDIRECT = token("follow-redirect");
  /** Request attribute: timeout. */
  byte[] TIMEOUT = token("timeout");
  /** Body attribute: src. */
  byte[] SRC = token("src");
  /** Body attribute: media-type. */
  byte[] MEDIA_TYPE = token("media-type");
  /** boundary marker. */
  byte[] BOUNDARY = token("boundary");

  /** HTTP method TRACE. */
  byte[] TRACE = token("trace");
  /** HTTP method DELETE. */
  byte[] DELETE = token("delete");

  /** Carriage return/line feed. */
  byte[] CRLF = { '\r', '\n' };
  /** Default multipart boundary. */
  byte[] DEFAULT_BOUND = token("1BEF0A57BE110FD467A");

  /** HTTP header Content-Type lower case. */
  byte[] CONTENT_TYPE_LC = token("content-type");

  /** Response attribute: status. */
  byte[] STATUS = token("status");
  /** Response attribute: message. */
  byte[] MESSAGE = token("message");

  // Serialization methods defined by the EXPath specification.
  /** Method http:base64Binary. */
  byte[] BASE64 = token("http:base64Binary");
  /** Method http:hexBinary. */
  byte[] HEXBIN = token("http:hexBinary");

  /** Header attribute: name. */
  byte[] NAME = token("name");
  /** Header attribute: value. */
  byte[] VALUE = token("value");
}
