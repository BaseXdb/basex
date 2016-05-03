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
  public final IO input;
  /** Database name ({@code null} indicates that no name can be extracted from original path). */
  public String dbName;
  /** Database path (empty string indicates root). */
  public String dbPath = "";

  /**
   * Constructor.
   * @param original original input string
   */
  public QueryInput(final String original) {
    this.original = original;
    input = IO.get(original);

    if(Databases.validName(original)) {
      // the specified input is no valid database name
      dbName = original;
    } else {
      // extract name and path from input string
      final int s = original.indexOf('/');
      if(s > 0 && original.indexOf(':') == -1) {
        final String n = original.substring(0, s);
        if(Databases.validName(n)) {
          dbName = n;
          dbPath = original.substring(s + 1);
        }
      }
    }
  }

  @Override
  public String toString() {
    return original;
  }
}
