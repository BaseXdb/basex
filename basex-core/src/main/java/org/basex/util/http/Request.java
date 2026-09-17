package org.basex.util.http;

import static org.basex.util.http.HTTPText.*;

import java.time.*;
import java.util.*;

import org.basex.core.StaticOptions.AuthMethod;
import org.basex.query.util.list.*;

/**
 * Container for parsed data from {@code <http:request/>}.
 *
 * @author BaseX Team, BSD License
 * @author Rositsa Shadura
 */
public final class Request {
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
  /** Timeout (can be {@code null}). */
  public Duration timeout;
  /** Media type that replaces the one of the response (can be {@code null}). */
  public String overrideMediaType;
  /** Username (can be {@code null}). */
  public String username;
  /** Password (can be {@code null}). */
  public String password;
  /** Authentication method. */
  public AuthMethod authMethod = AuthMethod.BASIC;
  /** Send credentials before a challenge is received. */
  public boolean sendAuthorization;
  /** Follow redirects. */
  public boolean followRedirect = true;
  /** Discard the response body. */
  public boolean statusOnly;
  /** Use the cookie store of the query. */
  public boolean cookies;
  /** CSV parser options (can be {@code null}). */
  public String csv;
  /** JSON parser options (can be {@code null}). */
  public String json;
  /** HTML parser options (can be {@code null}). */
  public String html;

  /**
   * Returns a valid boundary.
   * @return boundary string
   */
  String boundary() {
    final String boundary = payloadAtts.get(BOUNDARY);
    return boundary == null || boundary.isEmpty() ? DEFAULT_BOUNDARY : boundary;
  }
}
