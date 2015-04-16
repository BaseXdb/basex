package org.basex.util.http;

import java.util.*;

import org.basex.core.StaticOptions.AuthMethod;
import org.basex.query.util.list.*;
import org.basex.util.http.HttpText.*;

/**
 * Container for parsed data from {@code <http:request/>}.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Rositsa Shadura
 */
public final class HttpRequest {
  /** Request attributes. */
  public final EnumMap<Request, String> attributes = new EnumMap<>(Request.class);
  /** Request headers. */
  public final HashMap<String, String> headers = new HashMap<>();
  /** Body or multipart attributes. */
  public final HashMap<String, String> payloadAttrs = new HashMap<>();
  /** Body content. */
  public final ItemList bodyContent = new ItemList();
  /** Parts in case of multipart request. */
  public final ArrayList<Part> parts = new ArrayList<>();
  /** Indicator for multipart request. */
  public boolean isMultipart;

  /** Authentication method (Default: basic authentication). */
  public AuthMethod authMethod = AuthMethod.BASIC;

  /**
   * Returns the value of the specified attribute.
   * @param name name of request attribute
   * @return value
   */
  public String attribute(final Request name) {
    return attributes.get(name);
  }

  /**
   * Container for parsed data from <part/> element.
   * @author BaseX Team 2005-15, BSD License
   * @author Rositsa Shadura
   */
  public static class Part {
    /** Part headers. */
    public final HashMap<String, String> headers = new HashMap<>();
    /** Attributes of part body. */
    public final HashMap<String, String> bodyAttrs = new HashMap<>();
    /** Content of part body. */
    public final ItemList bodyContent = new ItemList();
  }
}
