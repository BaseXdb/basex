package org.basex.util.http;

import static org.basex.util.http.HTTPText.*;

import java.util.*;

import org.basex.core.StaticOptions.AuthMethod;
import org.basex.query.util.list.*;

/**
 * Container for parsed data from {@code <http:request/>}.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Rositsa Shadura
 */
public final class Request {
  /** Request attributes. */
  public final EnumMap<RequestAttribute, String> attributes = new EnumMap<>(RequestAttribute.class);
  /** Request headers. */
  public final HashMap<String, String> headers = new HashMap<>();
  /** Body or multipart attributes. */
  public final HashMap<String, String> payloadAtts = new HashMap<>();
  /** Payload. */
  public final ItemList payload = new ItemList();
  /** Parts in case of multipart request. */
  public final ArrayList<Part> parts = new ArrayList<>();
  /** Indicator for multipart request. */
  public boolean isMultipart;

  /** Authentication method (Default: basic authentication). */
  AuthMethod authMethod = AuthMethod.BASIC;

  /**
   * Returns the value of the specified attribute.
   * @param name name of request attribute
   * @return value or {@code null}
   */
  String attribute(final RequestAttribute name) {
    return attributes.get(name);
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
