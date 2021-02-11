package org.basex.query;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * This class references input passed on in a query. It can be a URI or a database path.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class QueryInput {
  /** Original input string (resource URI, database path or XML string). */
  public final String original;
  /** Input reference. */
  public final IO io;

  /** Database name (if {@code null}, no name could be extracted from original path). */
  public String dbName;
  /** Database path (empty string indicates root). */
  public String dbPath = "";

  /**
   * Constructor.
   * @param original original input string
   * @param sc static context
   */
  public QueryInput(final String original, final StaticContext sc) {
    this.original = original;
    io = sc.resolve(original);

    // check if the specified input string can be rewritten to a database name and path
    String name = Strings.startsWith(original, '/') ? original.substring(1) : original, path = "";
    final int s = name.indexOf('/');
    if(s != -1) {
      path = name.substring(s + 1);
      name = name.substring(0, s);
    }
    if(Databases.validName(name)) {
      dbName = name;
      dbPath = path;
    }
  }

  @Override
  public String toString() {
    return original;
  }
}
