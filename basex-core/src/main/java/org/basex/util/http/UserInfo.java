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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UserInfo {
  /** Supported digest algorithms, indexed by their names in challenges. */
  private static final Map<String, String> DIGESTS =
    Map.of(MD5, "MD5", "SHA-256", "SHA-256", "SHA-512-256", "SHA-512/256");
  /** Suffix of session algorithms. */
  private static final String SESS = "-SESS";
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
   * @param uri URI
   */
  public UserInfo(final URI uri) {
    this(uri, null);
  }

  /**
   * Constructor.
   * @param uri URI
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
   * Checks if credentials are available.
   * @return result of check
   */
  private boolean credentials() {
    return username != null && password != null;
  }

  /**
   * Assigns a basic authentication string.
   * @param rb HTTP request builder
   */
  public void basic(final HttpRequest.Builder rb) {
    if(username != null && password != null) rb.header(AUTHORIZATION,
        AuthMethod.BASIC + " " + Base64.encode(username + ':' + password));
  }

  /**
   * Answers a challenge by creating a request with an authentication header.
   * @param sent request that has been sent
   * @param response HTTP response
   * @return new request, or {@code null} if the challenge is not answered
   */
  public HttpRequest assign(final HttpRequest sent, final HttpResponse<?> response) {
    // no credentials available, server does not expect authentication: skip
    if(!credentials() || response.statusCode() != 401) return null;

    // challenge after redirects: answer it only if it comes from the original origin
    final HttpRequest last = response.request();
    final URI target = last.uri();
    if(!sameOrigin(uri, target)) return null;

    final String value = request.authMethod == AuthMethod.BASIC ?
      Base64.encode(username + ':' + password) : digest(response.headers(), last);
    if(value == null) return null;

    // redirect without body (e.g. 303): drop the content type of the original request
    final Optional<HttpRequest.BodyPublisher> body = last.bodyPublisher();
    final boolean bodyless = !last.method().equals(sent.method()) && body.isEmpty();
    final HttpRequest.Builder rb = HttpRequest.newBuilder(sent,
      (name, v) -> !(bodyless && name.equalsIgnoreCase(CONTENT_TYPE)));
    if(!target.equals(sent.uri())) rb.uri(target);
    if(!last.method().equals(sent.method())) {
      rb.method(last.method(), body.orElse(HttpRequest.BodyPublishers.noBody()));
    }
    return rb.header(AUTHORIZATION, request.authMethod + " " + value).build();
  }

  /**
   * Returns the credentials for the first supported digest challenge.
   * @param headers response headers
   * @param last request that was answered with the challenge
   * @return credentials or {@code null}
   */
  private String digest(final HttpHeaders headers, final HttpRequest last) {
    for(final String header : headers.allValues(WWW_AUTHENTICATE)) {
      for(final EnumMap<RequestAttribute, String> auth : Client.challenges(header)) {
        if(!request.authMethod.toString().equalsIgnoreCase(auth.get(AUTH_METHOD))) continue;

        // supported algorithms: MD5, SHA-256, SHA-512-256, optionally with session suffix
        final String algorithm = auth.getOrDefault(ALGORITHM, MD5);
        String name = algorithm.toUpperCase(Locale.ENGLISH);
        final boolean sess = name.endsWith(SESS);
        if(sess) name = name.substring(0, name.length() - SESS.length());
        final String algo = DIGESTS.get(name);
        if(algo == null) continue;

        // quality of protection: "auth", or none (RFC 2069); "auth-int" is not supported
        String qop = null;
        final String qops = auth.get(QOP);
        if(qops != null) {
          for(final String q : Strings.split(qops, ',')) {
            if(q.trim().equals(AUTH)) qop = AUTH;
          }
          if(qop == null) continue;
        } else if(sess) {
          continue;
        }

        final String realm = auth.get(REALM), nonce = auth.get(NONCE), opaque = auth.get(OPAQUE);
        if(realm == null || nonce == null) continue;

        final String path = last.uri().getRawPath(), query = last.uri().getRawQuery();
        final String target = (path == null || path.isEmpty() ? "/" : path) +
            (query != null ? "?" + query : "");
        final String nc = "00000001", cnonce = Strings.md5(Long.toString(System.nanoTime()));

        String ha1 = Strings.hash(username + ':' + realm + ':' + password, algo);
        if(sess) ha1 = Strings.hash(ha1 + ':' + nonce + ':' + cnonce, algo);
        final String ha2 = Strings.hash(last.method() + ':' + target, algo);
        final String rsp = Strings.hash(ha1 + ':' + nonce + ':' +
            (qop != null ? nc + ':' + cnonce + ':' + qop + ':' : "") + ha2, algo);

        final StringBuilder sb = new StringBuilder();
        sb.append(USERNAME).append('=').append(Client.quote(username)).append(',');
        sb.append(REALM).append('=').append(Client.quote(realm)).append(',');
        sb.append(NONCE).append('=').append(Client.quote(nonce)).append(',');
        sb.append(URI).append('=').append(Client.quote(target)).append(',');
        if(qop != null) {
          sb.append(QOP).append('=').append(qop).append(',');
          sb.append(NC).append('=').append(nc).append(',');
          sb.append(CNONCE).append('=').append(Client.quote(cnonce)).append(',');
        }
        sb.append(RESPONSE).append('=').append(Client.quote(rsp)).append(',');
        sb.append(ALGORITHM).append('=').append(algorithm);
        // include the opaque value only if the server provided one
        if(opaque != null) sb.append(',').append(OPAQUE).append('=').append(Client.quote(opaque));
        return sb.toString();
      }
    }
    return null;
  }

  /**
   * Checks if two URIs have the same origin.
   * @param uri1 first URI
   * @param uri2 second URI
   * @return result of check
   */
  private static boolean sameOrigin(final URI uri1, final URI uri2) {
    return String.valueOf(uri1.getScheme()).equalsIgnoreCase(String.valueOf(uri2.getScheme())) &&
      String.valueOf(uri1.getHost()).equalsIgnoreCase(String.valueOf(uri2.getHost())) &&
      port(uri1) == port(uri2);
  }

  /**
   * Returns the effective port of a URI.
   * @param uri URI
   * @return port
   */
  private static int port(final URI uri) {
    final int port = uri.getPort();
    return port != -1 ? port : "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
  }
}
