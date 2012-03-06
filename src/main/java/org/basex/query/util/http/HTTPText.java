package org.basex.query.util.http;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import org.basex.query.item.*;

/**
 * HTTP strings.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 */
public interface HTTPText {
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
  byte[] CRLF = token("\r\n");
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

  /** http:multipart element. */
  QNm HTTP_MULTIPART = new QNm("http:multipart", HTTPURI);
  /** http:body element. */
  QNm HTTP_BODY = new QNm("http:body", HTTPURI);
  /** http:response element. */
  QNm HTTP_RESPONSE = new QNm("http:response", HTTPURI);
  /** http:header element. */
  QNm HTTP_HEADER = new QNm("http:header", HTTPURI);

  /** Header attribute: name. */
  QNm Q_NAME = new QNm("name");
  /** Header attribute: value. */
  QNm Q_VALUE = new QNm("value");
  /** part element. */
  QNm Q_PART = new QNm("part");
  /** Body attribute: media-type. */
  QNm Q_MEDIA_TYPE = new QNm(MEDIA_TYPE);
  /** boundary marker. */
  QNm Q_BOUNDARY = new QNm(BOUNDARY);
  /** Header attribute: name. */
  QNm Q_STATUS = new QNm(STATUS);
  /** Header attribute: name. */
  QNm Q_MESSAGE = new QNm(MESSAGE);

  /** Multipart string. */
  String MULTIPART = "multipart";
  /** Payload string. */
  String PAYLOAD = "payload";
}
