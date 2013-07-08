package org.basex.query.up.primitives;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Update primitive for adding documents to databases.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class DBNew extends BasicOperation {
  /** Numeric index options. */
  protected static final Object[][] N_OPT = { Prop.MAXCATS, Prop.MAXLEN,
    Prop.INDEXSPLITSIZE, Prop.FTINDEXSPLITSIZE };
  /** Boolean index options. */
  protected static final Object[][] B_OPT = { Prop.TEXTINDEX, Prop.ATTRINDEX,
    Prop.FTINDEX, Prop.STEMMING, Prop.CASESENS, Prop.DIACRITICS,  Prop.UPDINDEX };
  /** String index options. */
  protected static final Object[][] S_OPT = { Prop.LANGUAGE, Prop.STOPWORDS };
  /** Keys of numeric index options. */
  protected static final byte[][] K_N_OPT = new byte[N_OPT.length][];
  /** Keys of boolean index options. */
  protected static final byte[][] K_B_OPT = new byte[B_OPT.length][];
  /** Keys of numeric index options. */
  protected static final byte[][] K_S_OPT = new byte[S_OPT.length][];

  static {
    // initialize options arrays
    final int n = N_OPT.length, b = B_OPT.length, s = S_OPT.length;
    for(int o = 0; o < n; o++) K_N_OPT[o] = lc(token(N_OPT[o][0].toString()));
    for(int o = 0; o < b; o++) K_B_OPT[o] = lc(token(B_OPT[o][0].toString()));
    for(int o = 0; o < s; o++) K_S_OPT[o] = lc(token(S_OPT[o][0].toString()));
  }

  /** Query context. */
  protected final QueryContext qc;
  /** Inputs to add. */
  protected List<NewInput> inputs;
  /** Optimization options. */
  protected TokenMap options;
  /** Insertion sequence. */
  protected Data md;

  /** Cached numeric options. */
  private int[] onums;
  /** Cached boolean options. */
  private boolean[] obools;
  /** Cached string options. */
  private String[] ostrs;


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
  protected final void addDocs(final MemData dt, final String name)
      throws QueryException {

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
      final MemData mdata = (MemData) ni.node.dbCopy(ctx.prop).data;
      mdata.update(0, Data.DOC, ni.path);
      return new DataClip(mdata);
    }

    // add input
    final IOFile dbpath = ctx.mprop.dbpath(string(ni.dbname));
    final Parser p = new DirParser(ni.io, ctx.prop, dbpath).target(string(ni.path));
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
    for(final byte[] key : options) {
      if(!eq(key, K_N_OPT) && !eq(key, K_B_OPT) && !eq(key, K_S_OPT) ||
         !create && eq(key, K_B_OPT[K_B_OPT.length - 1])) BASX_OPTIONS.thrw(info, key);
      final String v = string(options.get(key));
      if(eq(key, K_N_OPT)) {
        if(toInt(v) < 0) BASX_VALUE.thrw(info, key, v);
      } else if(eq(key, K_B_OPT)) {
        if(eqic(v, Text.YES, Text.TRUE, Text.ON)) options.put(key, Token.TRUE);
        else if(eqic(v, Text.NO, Text.FALSE, Text.OFF)) options.put(key, Token.FALSE);
        else BASX_VALUE.thrw(info, key, v);
      }
    }
  }

  /**
   * Assigns indexing options.
   */
  protected void assignOptions() {
    final Prop prop = qc.context.prop;
    final int ns = N_OPT.length;
    onums = new int[ns];
    for(int o = 0; o < ns; o++) onums[o] = prop.num(N_OPT[o]);
    final int[] nnums = onums.clone();
    for(int o = 0; o < ns; o++) if(options.contains(K_N_OPT[o]))
      nnums[o] = toInt(options.get(K_N_OPT[o]));

    final int bs = B_OPT.length;
    obools = new boolean[bs];
    for(int o = 0; o < bs; o++) obools[o] = prop.is(B_OPT[o]);
    final boolean[] nbools = obools.clone();
    for(int o = 0; o < bs; o++) if(options.contains(K_B_OPT[o]))
      nbools[o] = eq(options.get(K_B_OPT[o]), TRUE);

    final int ss = S_OPT.length;
    ostrs = new String[ss];
    for(int o = 0; o < ss; o++) ostrs[o] = prop.get(S_OPT[o]);
    final String[] nstrs = ostrs.clone();
    for(int o = 0; o < ss; o++) if(options.contains(K_S_OPT[o]))
      nstrs[o] = string(options.get(K_S_OPT[o]));
    set(prop, nnums, nbools, nstrs);
  }

  /**
   * Restores original indexing options.
   */
  protected void resetOptions() {
    set(qc.context.prop, onums, obools, ostrs);
  }

  /**
   * Assigns the specified options.
   * @param prop properties
   * @param nums numbers
   * @param bools booleans
   * @param strs strings
   */
  protected void set(final Prop prop, final int[] nums, final boolean[] bools,
      final String[] strs) {
    for(int o = 0; o < nums.length;  o++) prop.set(N_OPT[o], nums[o]);
    for(int o = 0; o < bools.length; o++) prop.set(B_OPT[o], bools[o]);
    for(int o = 0; o < strs.length;  o++) prop.set(S_OPT[o], strs[o]);
  }
}
