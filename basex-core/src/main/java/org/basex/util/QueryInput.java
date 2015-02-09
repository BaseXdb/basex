package org.basex.util;

import org.basex.core.*;
import org.basex.io.*;

/**
 * This class references input passed on in a query.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class QueryInput {
  /** Original input string. */
  public final String original;
  /** Input reference. */
  public final IO input;
  /** Optional database path. */
  public String path = "";
  /** Optional database name. */
  public String db;

  /**
   * Constructor.
   * @param original original input
   */
  public QueryInput(final String original) {
    this.original = original;
    input = IO.get(original);

    // checks if the specified input reference is a valid database name
    if(Databases.validName(original)) {
      db = original;
    } else {
      final int s = original.indexOf('/');
      if(s > 0 && original.indexOf(':') == -1) {
        final String n = original.substring(0, s);
        if(Databases.validName(n)) {
          path = original.substring(s + 1);
          db = n;
        }
      }
    }
  }

  @Override
  public String toString() {
    return original;
  }
}
