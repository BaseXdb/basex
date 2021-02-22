package org.basex.core.jobs;

import java.util.*;

import org.basex.io.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Query job specification.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class QueryJobSpec {
  /** Variable bindings. */
  final HashMap<String, Value> bindings;
  /** Options. */
  final JobsOptions options;
  /** Query. */
  final String query;
  /** Simple query without URI. */
  final boolean simple;

  /**
   * Constructor.
   * @param options options
   * @param bindings variable bindings
   * @param io query
   */
  public QueryJobSpec(final JobsOptions options, final HashMap<String, Value> bindings,
      final IOContent io) {
    this.options = options;
    this.bindings = bindings;
    query = Token.string(io.read());
    simple = io.url().isEmpty();
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof QueryJobSpec)) return false;
    final QueryJobSpec spec = (QueryJobSpec) obj;
    return options.toString().equals(spec.options.toString()) && query.equals(spec.query) &&
        bindings.equals(spec.bindings);
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + options + ',' + query + ']';
  }
}
