package org.basex.http.webdav;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.basex.build.*;
import org.basex.data.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * WebDAV query.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
final class WebDAVQuery {
  /** Path to WebDAV module. */
  private static final String FILE = "xquery/webdav.xqm";
  /** Path to WebDAV database. */
  private static Data db;
  /** Module contents. */
  private static String module;

  /** String query. */
  private final String query;
  /** Bindings. */
  private final HashMap<String, String> bindings = new HashMap<>();

  static {
    try {
      final ClassLoader cl = WebDAVQuery.class.getClassLoader();
      final InputStream is = cl.getResourceAsStream(FILE);
      if(is == null) throw new IOException("WebDAV module not found: " + FILE);
      module = string(new IOStream(is).read());

      final IO io = new IOContent("<webdav/>");
      io.name("~webdav~");
      db = MemBuilder.build(io);
    } catch(final Exception ex) {
      throw Util.notExpected("Could not initialize WebDAV");
    }
  }

  /**
   * Checks if the database contains any lock entries.
   * @return result of check
   */
  static boolean hasLocks() {
    return db.meta.size > 2;
  }

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
   * Returns all variable bindings.
   * @return variable bindings
   */
  Set<Entry<String, String>> entries() {
    return bindings.entrySet();
  }

  /**
   * Executes a query and returns the resulting items.
   * @param conn HTTP connection
   * @return resulting values
   * @throws IOException error during query execution
   */
  Value execute(final HTTPConnection conn) throws IOException {
    try(QueryProcessor qp = new QueryProcessor(toString(), conn.context)) {
      qp.qc.resources.addData(db);
      for(final Entry<String, String> entry : entries()) {
        qp.bind(entry.getKey(), entry.getValue());
      }
      qp.qc.parseLibrary(module, FILE, qp.sc);
      return qp.value();
    } catch(final QueryException ex) {
      throw new QueryIOException(ex);
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("declare option db:mainmem 'true';");
    for(final String v : bindings.keySet()) {
      sb.append("declare variable $").append(v).append(" external;");
    }
    return sb.append(query).toString();
  }
}
