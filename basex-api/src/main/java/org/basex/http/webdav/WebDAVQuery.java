package org.basex.http.webdav;

import java.util.*;
import java.util.Map.Entry;

/**
 * Query builder.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class WebDAVQuery {
  /** String query. */
  private final String query;
  /** Bindings. */
  private final HashMap<String, String> bindings = new HashMap<>();

  /**
   * Constructor.
   * @param query query string
   */
  WebDAVQuery(final String query) {
    this.query = query;
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
  Set<Entry<String, String>> entries() {
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
