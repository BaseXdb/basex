package org.basex.util.http;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.value.item.*;

/**
 * HTTP strings.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Rositsa Shadura
 */
public interface HttpText {
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
  /** Filename string. */
  String FILENAME = "filename";

  /** Default multipart boundary. */
  String DEFAULT_BOUNDARY = "1BEF0A57BE110FD467A";
  /** Boundary marker. */
  String BOUNDARY = "boundary";
  /** HTTP method TRACE. */
  String TRACE = "TRACE";
  /** HTTP method DELETE. */
  String DELETE = "DELETE";
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
  QNm Q_BODY = new QNm(HTTP_PREFIX, "body", HTTP_URI);
  /** QName. */
  QNm Q_RESPONSE = new QNm(HTTP_PREFIX, "response", HTTP_URI);
  /** QName. */
  QNm Q_HEADER = new QNm(HTTP_PREFIX, "header", HTTP_URI);
  /** QName. */
  QNm Q_MULTIPART = new QNm(HTTP_PREFIX, "multipart", HTTP_URI);

  /** Carriage return/line feed. */
  byte[] CRLF = { '\r', '\n' };

  /** Response attribute: status. */
  byte[] STATUS = token("status");
  /** Response attribute: message. */
  byte[] MESSAGE = token("message");

  /** Header attribute: name. */
  String NAME = "name";
  /** Header attribute: value. */
  String VALUE = "value";

  /** Binary string. */
  String BINARY = "binary";
  /** Base64 string. */
  String BASE64 = "base64";

  /** Request attributes. */
  enum Request {
    /** NC. */ NC,
    /** QOP. */ QOP,
    /** URI. */ URI,
    /** Href. */ HREF,
    /** Nonce. */ NONCE,
    /** Realm. */ REALM,
    /** Opaque. */ OPAQUE,
    /** Cnonce. */ CNONCE,
    /** Method. */ METHOD,
    /** Timeout. */ TIMEOUT,
    /** Response. */ RESPONSE,
    /** Password. */ PASSWORD,
    /** Username. */ USERNAME,
    /** Algorithm. */ ALGORITHM,
    /** Auth-method. */ AUTH_METHOD,
    /** Status-only. */ STATUS_ONLY,
    /** Follow-redirect. */ FOLLOW_REDIRECT,
    /** Send-authorization. */ SEND_AUTHORIZATION,
    /** Override-media-type. */ OVERRIDE_MEDIA_TYPE;

    /** Cached enums (faster). */
    public static final Request[] VALUES = values();

    /**
     * Returns an enum for the specified string.
     * @param key key
     * @return enum or {@code null}
     */
    public static Request get(final String key) {
      for(final Request r : VALUES) {
        if(key.equals(r.toString())) return r;
      }
      return null;
    }

    @Override
    public String toString() {
      return name().toLowerCase(Locale.ENGLISH).replace('_', '-');
    }
  }
}
