package org.basex.util.http;

import static org.basex.util.http.HTTPText.*;
import static org.basex.util.http.RequestAttribute.*;

import java.net.*;
import java.net.http.*;
import java.util.*;

import org.basex.core.StaticOptions.*;
import org.basex.util.*;
import org.basex.util.Base64;

/**
 * HTTP user information.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class UserInfo {
  /** Original URI. */
  private final URI uri;
  /** Request information (can be {@code null}). */
  private final Request request;

  /** Username. */
  private String username;
  /** Password. */
  private String password;

  /**
   * Constructor.
   * @param uri uri
   */
  public UserInfo(final URI uri) {
    this(uri, null);
  }

  /**
   * Constructor.
   * @param uri uri
   * @param request info (can be {@code null})
   */
  public UserInfo(final URI uri, final Request request) {
    this.uri = uri;
    this.request = request;

    final String ui = uri.getUserInfo();
    if(ui != null) {
      // adopt credentials from the URL
      final String[] creds = Strings.split(ui, ':', 2);
      if(creds.length == 2) {
        username = creds[0];
        password = creds[1];
      }
    } else if(request != null) {
      username = request.attribute(USERNAME);
      password = request.attribute(PASSWORD);
    }
  }

  /**
   * Assigns a basic authentication string.
   * @param rb HTTP request builder
   */
  public void basic(final HttpRequest.Builder rb) {
    if(username != null && password != null) rb.header(HTTPText.AUTHORIZATION,
        AuthMethod.BASIC + " " + Base64.encode(username + ':' + password));
  }

  /**
   * Answers a challenge by adding an authentication header to a request.
   * @param rb HTTP request builder
   * @param response HTTP response
   * @return success flag
   */
  public boolean assign(final HttpRequest.Builder rb, final HttpResponse<?> response) {
    // no credentials available, server does not expect authentication: skip
    if(username == null || password == null || response.statusCode() != 401) return false;

    final String value;
    if(request.authMethod == AuthMethod.BASIC) {
      value = Base64.encode(username + ':' + password);
    } else {
      // server provides no authentication data: skip
      final Optional<String> header = response.headers().firstValue(WWW_AUTHENTICATE);
      if(header.isEmpty()) return false;
      // server returns other authentication method: skip
      final EnumMap<RequestAttribute, String> auth = Client.authHeaders(header.get());
      if(!auth.get(AUTH_METHOD).equals(request.authMethod.toString())) return false;

      final String realm =
          auth.get(REALM),
          nonce = auth.get(NONCE),
          qop = auth.get(QOP),
          nc = "00000001",
          cnonce = Strings.md5(Long.toString(System.nanoTime())),
          ha1 = Strings.md5(username + ':' + realm + ':' + password),
          ha2 = Strings.md5(request.attribute(METHOD) + ':' + uri),
          rsp = Strings.md5(ha1 + ':' + nonce + ':' + nc + ':' + cnonce + ':' + qop + ':' + ha2);
      value = USERNAME + "=\"" + username + "\","
        + REALM + "=\"" + realm + "\","
        + NONCE + "=\"" + nonce + "\","
        + URI + "=\"" + uri + "\","
        + QOP + '=' + qop + ','
        + NC + '=' + nc + ','
        + CNONCE + "=\"" + cnonce + "\","
        + RESPONSE + "=\"" + rsp + "\","
        + ALGORITHM + '=' + MD5 + ','
        + OPAQUE + "=\"" + auth.get(OPAQUE) + '"';
    }
    rb.header(HTTPText.AUTHORIZATION, request.authMethod + " " + value);
    return true;
  }
}
