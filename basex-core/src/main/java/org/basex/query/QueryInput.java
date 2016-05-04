package org.basex.query;

import org.basex.core.*;
import org.basex.io.*;

/**
 * This class references input passed on in a query. It can be a file path or a database name.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class QueryInput {
  /** Original input string. */
  public final String original;
  /** Input reference. */
  public final IO io;
  /** Database name ({@code null} indicates that no name can be extracted from original path). */
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

    final IO baseIO = sc.baseIO();
    io = baseIO == null ? IO.get(original) : baseIO.merge(original);

    // check if the specified input string can be rewritten to a database name and path
    String name = original, path = "";
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
