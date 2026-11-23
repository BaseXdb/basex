package org.basex.query;

import static org.basex.query.QueryError.*;

import java.util.*;
import java.util.concurrent.*;

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
public final class QueryOptions {
  /** Query context. */
  private final QueryContext qc;
  /** Options supplied by option declarations. */
  private final HashSet<Option<?>> declared = new HashSet<>();
  /** Current state. */
  private State state = new State(null, null);

  /** Option state. */
  public static final class State {
    /** Query-local options: declarations and active pragmas (can be {@code null}). */
    private final MainOptions queryOpts;
    /** Sealed effective options (can be {@code null}). */
    private final MainOptions effective;
    /** States derived by pragmas. */
    private final Map<Key, Derived> derived = new ConcurrentHashMap<>();

    /**
     * Constructor.
     * @param queryOpts query-local options (can be {@code null})
     * @param effective sealed effective options (can be {@code null})
     */
    private State(final MainOptions queryOpts, final MainOptions effective) {
      this.queryOpts = queryOpts;
      this.effective = effective;
    }
  }

  /**
   * State derived by a pragma.
   * @param base effective options the pragma was applied to
   * @param state derived state
   */
  private record Derived(MainOptions base, State state) { }

  /**
   * Key of a state derived by a pragma.
   * @param option option
   * @param value value
   */
  private record Key(Option<?> option, String value) { }

  /**
   * Constructor.
   * @param qc query context
   */
  QueryOptions(final QueryContext qc) {
    this.qc = qc;
  }

  /**
   * Returns the current state.
   * @return state
   */
  State state() {
    return state;
  }

  /**
   * Assigns a state.
   * @param st state
   */
  void state(final State st) {
    state = st;
  }

  /**
   * Returns the effective options.
   * @return options
   */
  MainOptions get() {
    return state.effective != null ? state.effective : qc.context.options;
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
    final MainOptions options = get();
    final Option<?> option = options.option(key);
    if(option == null) throw BASEX_OPTIONS_X.get(parser.info(), options.similar(name));
    if(MainOptions.admin(option)) qc.checkPerm(Perm.ADMIN, null, key, parser.info());

    final MainOptions qopts = queryOpts();
    assign(qopts, key, value, parser.info());
    state = new State(qopts, state.effective);
    declared.add(option);
  }

  /**
   * Compiles all options.
   */
  void compile() {
    if(declared.isEmpty()) return;
    final MainOptions mopts = new MainOptions(get());
    for(final Option<?> option : declared) mopts.put(option, state.queryOpts.get(option));
    state = new State(state.queryOpts, mopts.seal());
  }

  /**
   * Assigns a pragma value to the effective and query-local options.
   * @param option option
   * @param value value
   * @param info input info (can be {@code null})
   * @return previous state
   * @throws QueryException query exception
   */
  State assign(final Option<?> option, final String value, final InputInfo info)
      throws QueryException {

    final State old = state;
    final MainOptions base = get();
    final Key key = new Key(option, value);
    Derived derived = old.derived.get(key);
    if(derived == null || derived.base != base) {
      final MainOptions mopts = new MainOptions(base), qopts = queryOpts();
      assign(mopts, option.name(), value, info);
      qopts.put(option, mopts.get(option));
      derived = new Derived(base, new State(qopts.seal(), mopts.seal()));
      old.derived.put(key, derived);
    }
    state = derived.state;
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
   * Returns new main options with the query-local and default options.
   * @return main options
   */
  MainOptions dbOptions() {
    return queryOpts().adoptAccess(get());
  }

  /**
   * Returns a copy of the query-local options.
   * @return query-local options
   */
  private MainOptions queryOpts() {
    return state.queryOpts != null ? new MainOptions(state.queryOpts) : new MainOptions();
  }
}
