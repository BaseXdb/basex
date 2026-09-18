package org.basex.util.http;

import static org.basex.query.QueryError.*;
import static org.basex.util.http.HTTPText.*;

import java.net.http.*;
import java.time.*;
import java.util.*;

import org.basex.core.StaticOptions.AuthMethod;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.util.list.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Container for parsed data from {@code <http:request/>}.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public final class Request {
  /** Default maximum number of redirects. */
  public static final int MAX_REDIRECTS = 5;

  /** Request headers. */
  public final TreeMap<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  /** Body or multipart attributes. */
  public final HashMap<String, String> payloadAtts = new HashMap<>();
  /** Payload. */
  public final ItemList payload = new ItemList();
  /** Parts in case of multipart request. */
  public final ArrayList<Part> parts = new ArrayList<>();
  /** Indicator for multipart request. */
  public boolean isMultipart;

  /** HTTP method (can be {@code null}). */
  public String method;
  /** Target URL (can be {@code null}). */
  public String href;
  /** Query parameters, appended to the URL (can be {@code null}). */
  public String query;
  /** Timeout (can be {@code null}). */
  public Duration timeout;
  /** Media type that replaces the one of the response (can be {@code null}). */
  public String overrideMediaType;
  /** Character encoding that replaces the one of the response (can be {@code null}). */
  public String charset;
  /** Username (can be {@code null}). */
  public String username;
  /** Password (can be {@code null}). */
  public String password;
  /** Authentication method. */
  public AuthMethod authMethod = AuthMethod.BASIC;
  /** Send credentials before a challenge is received. */
  public boolean sendAuthorization;
  /** Maximum number of redirects ({@code 0}: do not follow redirects). */
  public int redirects = MAX_REDIRECTS;
  /** Representation of the response body. */
  public BodyMode bodyMode = BodyMode.PARSE;
  /** Use the cookie store of the query. */
  public boolean cookies;
  /** Proxy URI, empty string for a direct connection (can be {@code null}). */
  public String proxy;
  /** Verify the certificate of an HTTPS server. */
  public boolean verify = true;
  /** Key stores for HTTPS connections (can be {@code null}). */
  public Certificates.Stores certificates;
  /** Parse options of the HTTP Client Module 2.0 (can be {@code null}). */
  public XQMap parseOptions;
  /** XML parser options (can be {@code null}). */
  public String xml;
  /** CSV parser options (can be {@code null}). */
  public String csv;
  /** JSON parser options (can be {@code null}). */
  public String json;
  /** HTML parser options (can be {@code null}). */
  public String html;

  /**
   * Checks if the HTTP method and the header fields are accepted by the HTTP client.
   * @return error message, or {@code null} if the request is valid
   */
  String invalid() {
    final HttpRequest.Builder rb = HttpRequest.newBuilder();
    try {
      rb.method(method, HttpRequest.BodyPublishers.noBody());
      headers.forEach((name, value) -> {
        // suppressed header fields are not sent
        if(value != null) rb.header(name, value);
      });
      return null;
    } catch(final IllegalArgumentException ex) {
      return ex.getMessage();
    }
  }

  /**
   * Assigns a timeout.
   * @param value number of seconds
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  void timeout(final String value, final InputInfo info) throws QueryException {
    final double seconds = Token.toDouble(Token.token(value));
    if(!(seconds > 0)) throw HC_REQ_X.get(info, "Invalid timeout: " + value);
    timeout(seconds, info);
  }

  /**
   * Assigns a timeout.
   * @param seconds number of seconds
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  void timeout(final double seconds, final InputInfo info) throws QueryException {
    if(!(seconds > 0)) throw HC_REQ_X.get(info, "Invalid timeout: " + seconds);
    timeout = Duration.ofNanos((long) (seconds * 1000000000));
  }

  /**
   * Assigns the maximum number of redirects.
   * @param value boolean, or maximum number of redirects
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  void redirects(final String value, final InputInfo info) throws QueryException {
    if(Strings.isTrue(value)) {
      redirects = MAX_REDIRECTS;
    } else if(Strings.isFalse(value)) {
      redirects = 0;
    } else {
      final int max = Strings.toInt(value);
      if(max == Integer.MIN_VALUE)
        throw HC_REQ_X.get(info, "Invalid number of redirects: " + value);
      redirects(max, info);
    }
  }

  /**
   * Assigns the maximum number of redirects.
   * @param max maximum number of redirects
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  void redirects(final long max, final InputInfo info) throws QueryException {
    if(max < 0) throw HC_REQ_X.get(info, "Invalid number of redirects: " + max);
    redirects = (int) Math.min(max, Integer.MAX_VALUE);
  }

  /**
   * Assigns a proxy.
   * @param value proxy URI, empty string for a direct connection
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  void proxy(final String value, final InputInfo info) throws QueryException {
    if(!value.isEmpty() && IOUrl.proxy(value) == null)
      throw HC_REQ_X.get(info, "Invalid proxy: " + value);
    proxy = value;
  }

  /**
   * Assigns an authentication method. Scheme names are compared case-insensitively.
   * @param name scheme name
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  void authMethod(final String name, final InputInfo info) throws QueryException {
    for(final AuthMethod auth : AuthMethod.values()) {
      if(auth.toString().equalsIgnoreCase(name)) {
        authMethod = auth;
        return;
      }
    }
    throw HC_REQ_X.get(info, "Invalid authentication method: " + name);
  }

  /**
   * Returns a valid boundary.
   * @return boundary string
   */
  String boundary() {
    final String boundary = payloadAtts.get(BOUNDARY);
    return boundary == null || boundary.isEmpty() ? DEFAULT_BOUNDARY : boundary;
  }
}
