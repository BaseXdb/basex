package org.basex.http.webdav.impl;

import java.util.*;
import java.util.Map.*;


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
  private final HashMap<String, String> bindings = new HashMap<String, String>();

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
  WebDAVQuery bind(final String name, final String value) {
    bindings.put(name, value);
    return this;
  }

  /**
   * Returns the hash map entries.
   * @return self reference
   */
  Set<Map.Entry<String, String>> entries() {
    return bindings.entrySet();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final Entry<String, String> entry : entries()) {
      sb.append("declare variable $").append(entry.getKey()).append(" external;");
    }
    return sb.append(query).toString();
  }
}
