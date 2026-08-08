package org.basex.core.jobs;

import java.util.*;

import org.basex.io.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Query job specification.
 *
 * @author BaseX Team, BSD License
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
  /** URI resolver of the calling module (can be {@code null}). */
  final UriResolver resolver;
  /** Function to be invoked instead of the query; discarded after the job was started. */
  FItem function;
  /** Arguments of the invoked function; discarded after the job was started. */
  Value[] args;

  /**
   * Constructor for a query.
   * @param options job options
   * @param bindings variable bindings
   * @param content query content
   * @param resolver URI resolver of the calling module (can be {@code null})
   */
  public QueryJobSpec(final JobOptions options, final HashMap<String, Value> bindings,
      final IOContent content, final UriResolver resolver) {
    this.options = options;
    this.bindings = bindings;
    // a scheduled job outlives the application it was started from, which may be replaced meanwhile
    this.resolver = scheduled(options) ? null : resolver;
    query = content.toString();
    simple = content.url().isEmpty();
    function = null;
    args = null;
  }

  /**
   * Constructor for a function to be invoked.
   * @param options job options
   * @param function function item
   * @param args function arguments
   */
  public QueryJobSpec(final JobOptions options, final FItem function, final Value[] args) {
    this.options = options;
    this.function = function;
    this.args = args;
    bindings = new HashMap<>();
    resolver = null;
    query = function.funcIdentity();
    simple = true;
  }

  /**
   * Indicates if a job will be evaluated later or repeatedly.
   * @param options job options
   * @return result of check
   */
  public static boolean scheduled(final JobOptions options) {
    for(final StringOption option :
        new StringOption[] { JobOptions.START, JobOptions.INTERVAL, JobOptions.CRON }) {
      final String value = options.get(option);
      if(value != null && !value.isEmpty()) return true;
    }
    return false;
  }

  @Override
  public boolean equals(final Object obj) {
    // only services are compared, and they never invoke a function
    return this == obj || obj instanceof final QueryJobSpec spec && query.equals(spec.query) &&
        bindings.equals(spec.bindings) && options.toString().equals(spec.options.toString());
  }

  @Override
  public String toString() {
    return Util.className(this) + '[' + options + ',' + query + ']';
  }
}
