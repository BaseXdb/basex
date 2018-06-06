package org.basex.http.webdav;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.basex.api.client.*;
import org.basex.core.cmd.*;

/**
 * WebDAV query.
 *
 * @author BaseX Team 2005-18, BSD License
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
   * Executes the query and returns the result as string.
   * @param session user session
   * @return result
   * @throws IOException I/O execution
   */
  String execute(final Session session) throws IOException {
    final XQuery xquery = new XQuery(toString());
    for(final Entry<String, String> entry : bindings.entrySet()) {
      xquery.bind(entry.getKey(), entry.getValue());
    }
    return session.execute(xquery);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    for(final String name : bindings.keySet()) {
      sb.append("declare variable $").append(name).append(" external;");
    }
    return sb.append(query).toString();
  }
}
