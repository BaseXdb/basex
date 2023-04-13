package org.basex.core.jobs;

import java.util.*;

import org.basex.io.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Query job specification.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class QueryJobSpec {
  /** Variable bindings. */
  final HashMap<String, Value> bindings;
  /** Options. */
  final JobOptions options;
  /** Query. */
  final String query;
  /** Simple query without URI. */
  final boolean simple;

  /**
   * Constructor.
   * @param options job options
   * @param bindings variable bindings
   * @param content query content
   */
  public QueryJobSpec(final JobOptions options, final HashMap<String, Value> bindings,
      final IOContent content) {
    this.options = options;
    this.bindings = bindings;
    this.query = content.toString();
    simple = content.url().isEmpty();
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
