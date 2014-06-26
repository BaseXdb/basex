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
  final HashMap<Option<?>, Object> tOptions = new HashMap<>();
  /** Original options. */
  private final HashMap<Option<?>, Object> oOptions = new HashMap<>();
  /** Optimization options. */
  private final HashMap<String, String> qOptions;
  /** Input info. */
  private final InputInfo info;

  /**
   * Constructor.
   * @param qOptions options
   * @param info input info
   */
  DBOptions(final HashMap<String, String> qOptions, final InputInfo info) {
    this.qOptions = qOptions;
    this.info = info;
  }

  /**
   * Checks the validity of the assigned database options.
   * @param create create or optimize database
   * @throws QueryException query exception
   */
  void check(final boolean create) throws QueryException {
    for(final Entry<String, String> entry : qOptions.entrySet()) {
      final String key = entry.getKey();
      if(!eq(key, K_N_OPT) && !eq(key, K_B_OPT) && !eq(key, K_S_OPT) ||
         !create && eq(key, K_B_OPT[K_B_OPT.length - 1])) throw BASX_OPTIONS.get(info, key);

      final String value = entry.getValue();
      if(eq(key, K_N_OPT)) {
        if(toInt(value) < 0) throw BASX_VALUE.get(info, key, value);
      } else if(eq(key, K_B_OPT)) {
        if(!Util.yes(value) && !Util.no(value)) throw BASX_VALUE.get(info, key, value);
      }
    }
  }

  /**
   * Caches original options and assigns cached options.
   * @param opts main options
   */
  void assign(final MainOptions opts) {
    for(int o = 0; o < K_N_OPT.length; o++) if(qOptions.containsKey(K_N_OPT[o]))
      tOptions.put(N_OPT[o], toInt(qOptions.get(K_N_OPT[o])));
    for(int o = 0; o < K_B_OPT.length; o++) if(qOptions.containsKey(K_B_OPT[o]))
      tOptions.put(B_OPT[o], Util.yes(qOptions.get(K_B_OPT[o])));
    for(int o = 0; o < K_S_OPT.length; o++) if(qOptions.containsKey(K_S_OPT[o]))
      tOptions.put(S_OPT[o], qOptions.get(K_S_OPT[o]));

    for(final Option<?> option : tOptions.keySet()) oOptions.put(option, opts.get(option));
    setOptions(tOptions, opts);
  }

  /**
   * Restores original options.
   * @param opts main options
   */
  void reset(final MainOptions opts) {
    setOptions(oOptions, opts);
  }

  /**
   * Assigns the specified options.
   * @param map options to be assigned
   * @param opts main options
   */
  private void setOptions(final HashMap<Option<?>, Object> map, final MainOptions opts) {
    for(final Entry<Option<?>, Object> e : map.entrySet()) opts.put(e.getKey(), e.getValue());
  }
}
