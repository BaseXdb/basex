package org.basex.io;

import org.basex.data.*;

/**
 * This class references input passed on in a query.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class QueryInput {
  /** Original input string. */
  public String original = "";
  /** Optional database path. */
  public String path = "";
  /** Optional database name. */
  public String db;
  /** Input reference. */
  public IO io;

  /**
   * Constructor.
   * @param input input path
   */
  public QueryInput(final String input) {
    original = input;
    io = IO.get(input);

    // checks if the specified input reference is a valid database name
    if(MetaData.validName(input, false)) {
      db = input;
    } else {
      final int s = input.indexOf('/');
      if(s > 0 && input.indexOf(':') == -1) {
        final String n = input.substring(0, s);
        if(MetaData.validName(n, false)) {
          path = input.substring(s + 1);
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
