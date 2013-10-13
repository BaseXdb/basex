package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Update primitive for adding documents to databases.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public abstract class DBNew extends BasicOperation {
  /** Numeric index options. */
  protected static final Option[] N_OPT = { MainOptions.MAXCATS, MainOptions.MAXLEN,
    MainOptions.INDEXSPLITSIZE, MainOptions.FTINDEXSPLITSIZE };
  /** Boolean index options. */
  protected static final Option[] B_OPT = { MainOptions.TEXTINDEX, MainOptions.ATTRINDEX,
    MainOptions.FTINDEX, MainOptions.STEMMING, MainOptions.CASESENS, MainOptions.DIACRITICS,
    MainOptions.UPDINDEX };
  /** String index options. */
  protected static final Option[] S_OPT = { MainOptions.LANGUAGE, MainOptions.STOPWORDS };
  /** Names of numeric index options. */
  protected static final String[] K_N_OPT = new String[N_OPT.length];
  /** Names of boolean index options. */
  protected static final String[] K_B_OPT = new String[B_OPT.length];
  /** NAmes of numeric index options. */
  protected static final String[] K_S_OPT = new String[S_OPT.length];

  static {
    // initialize options arrays
    final int n = N_OPT.length, b = B_OPT.length, s = S_OPT.length;
    for(int o = 0; o < n; o++) K_N_OPT[o] = N_OPT[o].name().toLowerCase(Locale.ENGLISH);
    for(int o = 0; o < b; o++) K_B_OPT[o] = B_OPT[o].name().toLowerCase(Locale.ENGLISH);
    for(int o = 0; o < s; o++) K_S_OPT[o] = S_OPT[o].name().toLowerCase(Locale.ENGLISH);
  }

  /** Query context. */
  protected final QueryContext qc;
  /** Inputs to add. */
  protected List<NewInput> inputs;
  /** Optimization options. */
  protected HashMap<String, String> options;
  /** Insertion sequence. */
  protected Data md;

  /** Original options. */
  protected final HashMap<Option, Object> oprops = new HashMap<Option, Object>();
  /** New options. */
  protected final HashMap<Option, Object> nprops = new HashMap<Option, Object>();


  /**
   * Constructor.
   * @param t type of update
   * @param d target database
   * @param c query context
   * @param ii input info
   */
  public DBNew(final TYPE t, final Data d, final QueryContext c, final InputInfo ii) {
    super(t, d, ii);
    qc = c;
  }

  /**
   * Inserts all documents to be added to a temporary database.
   * @param dt target database
   * @param name name of database
   * @throws QueryException query exception
   */
  protected final void addDocs(final MemData dt, final String name) throws QueryException {
    md = dt;
    final long ds = inputs.size();
    for(int i = 0; i < ds; i++) {
      md.insert(md.meta.size, -1, data(inputs.get(i), name));
      // clear list to recover memory
      inputs.set(i, null);
    }
    inputs = null;
  }

  /**
   * Creates a {@link DataClip} instance for the specified document.
   * @param ni new database input
   * @param dbname name of database
   * @return database clip
   * @throws QueryException query exception
   */
  private DataClip data(final NewInput ni, final String dbname) throws QueryException {
    // add document node
    final Context ctx = qc.context;
    if(ni.node != null) {
      final MemData mdata = (MemData) ni.node.dbCopy(ctx.options).data;
      mdata.update(0, Data.DOC, ni.path);
      return new DataClip(mdata);
    }

    // add input
    final IOFile dbpath = ctx.globalopts.dbpath(string(ni.dbname));
    final Parser p = new DirParser(ni.io, ctx.options, dbpath).target(string(ni.path));
    final MemBuilder b = new MemBuilder(dbname, p);
    try {
      return new DataClip(b.build());
    } catch(final IOException ex) {
      throw IOERR.thrw(info, ex);
    }
  }

  /**
   * Checks the validity of the assigned database options.
   * @param create create or optimize database
   * @throws QueryException query exception
   */
  protected final void check(final boolean create) throws QueryException {
    for(final Entry<String, String> entry : options.entrySet()) {
      final String key = entry.getKey();
      if(!eq(key, K_N_OPT) && !eq(key, K_B_OPT) && !eq(key, K_S_OPT) ||
         !create && eq(key, K_B_OPT[K_B_OPT.length - 1])) BASX_OPTIONS.thrw(info, key);
      final String v = entry.getValue();
      if(eq(key, K_N_OPT)) {
        if(toInt(v) < 0) BASX_VALUE.thrw(info, key, v);
      } else if(eq(key, K_B_OPT)) {
        if(Util.yes(v)) options.put(key, Text.TRUE);
        else if(Util.no(v)) options.put(key, Text.FALSE);
        else BASX_VALUE.thrw(info, key, v);
      }
    }
  }

  /**
   * Assigns indexing options.
   */
  protected void initOptions() {
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
  protected void assignOptions() {
    final MainOptions opts = qc.context.options;
    for(final Option option : nprops.keySet()) oprops.put(option, opts.get(option));
    setOptions(nprops);
  }

  /**
   * Restores original options.
   */
  protected void resetOptions() {
    setOptions(oprops);
  }

  /**
   * Assigns the specified options.
   * @param map options map
   */
  private void setOptions(final HashMap<Option, Object> map) {
    final MainOptions opts = qc.context.options;
    for(final Map.Entry<Option, Object> e : map.entrySet()) opts.put(e.getKey(), e.getValue());
  }
}
