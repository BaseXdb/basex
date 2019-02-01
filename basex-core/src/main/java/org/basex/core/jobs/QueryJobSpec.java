package org.basex.core.jobs;

import java.util.*;

import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Query job specification.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class QueryJobSpec {
  /** Variable bindings. */
  public final HashMap<String, Value> bindings;
  /** Options. */
  public final JobsOptions options;
  /** Query. */
  public final String query;

  /**
   * Constructor.
   * @param options options
   * @param bindings variable bindings
   * @param query query string
   */
  public QueryJobSpec(final JobsOptions options, final HashMap<String, Value> bindings,
      final byte[] query) {
    this.options = options;
    this.bindings = bindings;
    this.query = Token.string(query);
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
