package org.basex.http.webdav.impl;

import java.util.*;

/**
 * Query builder.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class WebDAVQuery {
  /** String query. */
  private final String query;
  /** Bindings. */
  private final HashMap<String, Object> bindings = new HashMap<String, Object>();

  /**
   * Constructor.
   * @param qu query string
   */
  WebDAVQuery(final String qu) {
    query = qu;
  }

  /**
   * Binds a variable.
   * @param name name of variable (without '$' sign).
   * @param value value of variable
   * @return self reference
   */
  WebDAVQuery bind(final String name, final Object value) {
    bindings.put(name, value);
    return this;
  }

  /**
   * Returns the hash map entries.
   * @return self reference
   */
  Set<Map.Entry<String, Object>> entries() {
    return bindings.entrySet();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final String v : bindings.keySet()) {
      sb.append("declare variable $").append(v).append(" external;");
    }
    return sb.append(query).toString();
  }
}
