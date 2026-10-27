package org.basex.query;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Query-specific database options.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class QueryOptions {
  /** Query context. */
  private final QueryContext qc;
  /** Global database options (will be reassigned after query execution). */
  private final HashMap<Option<?>, Object> cachedOpts = new HashMap<>();
  /** Options supplied by option declarations. */
  private final HashSet<Option<?>> declared = new HashSet<>();
  /** Query-local options: declarations and active pragmas (can be {@code null}). */
  private MainOptions queryOpts;

  /**
   * Constructor.
   * @param qc query context
   */
  QueryOptions(final QueryContext qc) {
    this.qc = qc;
  }

  /**
   * Adopts the query-local options of a parent query.
   * @param parent options of the parent query
   */
  void inherit(final QueryOptions parent) {
    if(parent.queryOpts != null) queryOpts = new MainOptions(parent.queryOpts);
  }

  /**
   * Remembers a parsed database option.
   * @param name name of option
   * @param value value of option
   * @param parser query parser
   * @throws QueryException query exception
   */
  void add(final String name, final String value, final QueryParser parser) throws QueryException {
    final String key = name.toUpperCase(Locale.ENGLISH);
    final MainOptions options = qc.context.options;
    final Option<?> option = options.option(key);
    if(option == null) throw BASEX_OPTIONS_X.get(parser.info(), options.similar(name));
    if(MainOptions.admin(option)) qc.checkPerm(Perm.ADMIN, null, key, parser.info());

    assign(queryOpts(), key, value, parser.info());
    declared.add(option);
  }

  /**
   * Compiles all options.
   */
  void compile() {
    // cache old database options at compile time; assign local ones
    final MainOptions mopts = qc.context.options;
    for(final Option<?> option : declared) {
      cachedOpts.put(option, mopts.get(option));
      mopts.put(option, queryOpts.get(option));
    }
  }

  /**
   * Assigns a pragma value to the global and query-local options.
   * @param option option
   * @param value value
   * @param info input info (can be {@code null})
   * @return old values
   * @throws QueryException query exception
   */
  Object[] assign(final Option<?> option, final String value, final InputInfo info)
      throws QueryException {
    final MainOptions mopts = qc.context.options, qopts = queryOpts();
    final Object[] old = { mopts.get(option), qopts.get(option) };
    assign(mopts, option.name(), value, info);
    qopts.put(option, mopts.get(option));
    return old;
  }

  /**
   * Assigns an option value.
   * @param options main options
   * @param name name of option
   * @param value value of option
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  private static void assign(final MainOptions options, final String name, final String value,
      final InputInfo info) throws QueryException {
    try {
      options.assign(name, value);
    } catch(final BaseXException ex) {
      throw BASEX_OPTIONS_X_X.get(info, name, value).cause(ex);
    }
  }

  /**
   * Restores the values that were replaced by a pragma.
   * @param option option
   * @param old old values
   */
  void restore(final Option<?> option, final Object[] old) {
    qc.context.options.put(option, old[0]);
    queryOpts.put(option, old[1]);
  }

  /**
   * Returns new main options with the query-local and default options.
   * @return main options
   */
  MainOptions dbOptions() {
    final MainOptions mopts = queryOpts != null ? new MainOptions(queryOpts) : new MainOptions();
    return mopts.adoptAccess(qc.context.options);
  }

  /**
   * Returns the query-local options.
   * @return query-local options
   */
  private MainOptions queryOpts() {
    if(queryOpts == null) queryOpts = new MainOptions();
    return queryOpts;
  }

  /**
   * Reassigns original options.
   */
  void close() {
    cachedOpts.forEach(qc.context.options::put);
  }
}
