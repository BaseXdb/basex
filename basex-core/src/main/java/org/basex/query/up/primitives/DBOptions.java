package org.basex.query.up.primitives;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Contains various helper variables and methods for database operations.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DBOptions {
  /** Runtime options. */
  private final HashMap<Option<?>, Object> map = new HashMap<>();
  /** Query context. */
  private final QueryContext qc;
  /** Input info (can be {@code null}). */
  private final InputInfo info;

  /**
   * Constructor.
   * @param options supplied options
   * @param qc query context
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public DBOptions(final Options options, final QueryContext qc, final InputInfo info)
      throws QueryException {

    this.qc = qc;
    this.info = info;
    for(final Option<?> option : options) {
      final Object value = options.get(option);
      if(value == null) continue;
      if(MainOptions.admin(option)) {
        qc.checkPerm(Perm.ADMIN, null, option.name().toLowerCase(Locale.ENGLISH), info);
      }
      // nested options may be shared by several evaluations
      map.put(option, value instanceof final Options opts ? opts.copy() : value);
    }
  }

  /**
   * Assigns the specified option if it has not been assigned before.
   * @param option option
   * @param value value
   */
  public void assignIfAbsent(final Option<?> option, final Object value) {
    map.putIfAbsent(option, value);
  }

  /**
   * Returns main options with the runtime, query-local and default options.
   * @return main options
   */
  public MainOptions mainOptions() {
    final MainOptions opts = qc.dbOptions();
    map.forEach(opts::put);
    return opts;
  }

  /**
   * Returns main options with the runtime, query-local and default options and checks indexing.
   * @param data existing database (can be {@code null})
   * @return main options
   * @throws QueryException query exception
   */
  public MainOptions indexingOptions(final Data data) throws QueryException {
    final MainOptions opts = mainOptions();
    try {
      IndexBuilder.check(opts, data);
    } catch(final IOException ex) {
      throw BASEX_OPTIONS_X.get(info, ex);
    }
    return opts;
  }
}
