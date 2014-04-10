package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.Map.Entry;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Contains various helper variables and methods for database operations.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class DBOptions {
  /** Numeric index options. */
  private static final NumberOption[] N_OPT = { MainOptions.MAXCATS, MainOptions.MAXLEN,
    MainOptions.INDEXSPLITSIZE, MainOptions.FTINDEXSPLITSIZE };
  /** Boolean index options. */
  private static final BooleanOption[] B_OPT = { MainOptions.TEXTINDEX, MainOptions.ATTRINDEX,
    MainOptions.FTINDEX, MainOptions.STEMMING, MainOptions.CASESENS, MainOptions.DIACRITICS,
    MainOptions.UPDINDEX };
  /** String index options. */
  private static final StringOption[] S_OPT = { MainOptions.LANGUAGE, MainOptions.STOPWORDS };
  /** Names of numeric index options. */
  private static final String[] K_N_OPT = new String[N_OPT.length];
  /** Names of boolean index options. */
  private static final String[] K_B_OPT = new String[B_OPT.length];
  /** NAmes of numeric index options. */
  private static final String[] K_S_OPT = new String[S_OPT.length];

  static {
    // initialize options arrays
    final int n = N_OPT.length, b = B_OPT.length, s = S_OPT.length;
    for(int o = 0; o < n; o++) K_N_OPT[o] = N_OPT[o].name().toLowerCase(Locale.ENGLISH);
    for(int o = 0; o < b; o++) K_B_OPT[o] = B_OPT[o].name().toLowerCase(Locale.ENGLISH);
    for(int o = 0; o < s; o++) K_S_OPT[o] = S_OPT[o].name().toLowerCase(Locale.ENGLISH);
  }

  /** New options. */
  final HashMap<Option<?>, Object> nprops = new HashMap<>();
  /** Original options. */
  private final HashMap<Option<?>, Object> oprops = new HashMap<>();
  /** Optimization options. */
  private final HashMap<String, String> options;
  /** Query context. */
  final QueryContext qc;
  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param qc query context
   * @param options options
   * @param info input info
   */
  DBOptions(final QueryContext qc, final HashMap<String, String> options,
      final InputInfo info) {
    this.options = options;
    this.qc = qc;
    this.info = info;
  }

  /**
   * Checks the validity of the assigned database options.
   * @param create create or optimize database
   * @throws QueryException query exception
   */
  void check(final boolean create) throws QueryException {
    for(final Entry<String, String> entry : options.entrySet()) {
      final String key = entry.getKey();
      if(!eq(key, K_N_OPT) && !eq(key, K_B_OPT) && !eq(key, K_S_OPT) ||
         !create && eq(key, K_B_OPT[K_B_OPT.length - 1])) throw BASX_OPTIONS.get(info, key);
      final String v = entry.getValue();
      if(eq(key, K_N_OPT)) {
        if(toInt(v) < 0) throw BASX_VALUE.get(info, key, v);
      } else if(eq(key, K_B_OPT)) {
        if(Util.yes(v)) options.put(key, Text.TRUE);
        else if(Util.no(v)) options.put(key, Text.FALSE);
        else throw BASX_VALUE.get(info, key, v);
      }
    }
  }

  /**
   * Assigns indexing options.
   */
  void initOptions() {
    for(int o = 0; o < K_N_OPT.length; o++) if(options.containsKey(K_N_OPT[o]))
      nprops.put(N_OPT[o], toInt(options.get(K_N_OPT[o])));
    for(int o = 0; o < K_B_OPT.length; o++) if(options.containsKey(K_B_OPT[o]))
      nprops.put(B_OPT[o], Util.yes(options.get(K_B_OPT[o])));
    for(int o = 0; o < K_S_OPT.length; o++) if(options.containsKey(K_S_OPT[o]))
      nprops.put(S_OPT[o], options.get(K_S_OPT[o]));
  }

  /**
   * Caches original options and assigns cached options.
   */
  void assignOptions() {
    final MainOptions opts = qc.context.options;
    for(final Option<?> option : nprops.keySet()) oprops.put(option, opts.get(option));
    setOptions(nprops);
  }

  /**
   * Restores original options.
   */
  void resetOptions() {
    setOptions(oprops);
  }

  /**
   * Assigns the specified options.
   * @param map options map
   */
  private void setOptions(final HashMap<Option<?>, Object> map) {
    final MainOptions opts = qc.context.options;
    for(final Entry<Option<?>, Object> e : map.entrySet()) opts.put(e.getKey(), e.getValue());
  }
}
