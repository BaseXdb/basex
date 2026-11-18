package org.basex.query;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * This class references input passed on in a query. It can be a URI or a database path.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryInput {
  /** Original input string (resource URI, database path or XML string). */
  public final String original;
  /** Static context. */
  private final StaticContext sc;
  /** Input reference (can be {@code null}). */
  private IO io;
  /** Resolution error (can be {@code null}). */
  private QueryException error;
  /** Indicates if the input reference has been resolved. */
  private boolean resolved;
  /** Indicates if the input points to an existing database (can be {@code null}). */
  private Boolean exists;

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
    this.sc = sc;

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

  /**
   * Returns the input reference, which is resolved on first access.
   * @param info input info (can be {@code null})
   * @return input reference ({@code null} if the input cannot be resolved)
   * @throws QueryException query exception
   */
  public IO io(final InputInfo info) throws QueryException {
    if(!resolved) {
      resolved = true;
      try {
        io = sc.location(original, null, info);
      } catch(final QueryException ex) {
        error = ex;
      }
    }
    if(error != null) throw error.copy();
    return io;
  }

  /**
   * Indicates if the input points to an existing database.
   * @param soptions static options
   * @return result of check
   */
  public boolean dbExists(final StaticOptions soptions) {
    if(exists == null) exists = dbName != null && soptions.dbExists(dbName);
    return exists;
  }

  @Override
  public String toString() {
    return original;
  }
}
