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

  /** Original options. */
  protected final HashMap<Object[], Object> oprops = new HashMap<Object[], Object>();
  /** New options. */
  protected final HashMap<Object[], Object> nprops = new HashMap<Object[], Object>();


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
  protected void initOptions() {
    for(int o = 0; o < K_N_OPT.length; o++) if(options.contains(K_N_OPT[o]))
      nprops.put(N_OPT[o], toInt(options.get(K_N_OPT[o])));
    for(int o = 0; o < K_B_OPT.length; o++) if(options.contains(K_B_OPT[o]))
      nprops.put(B_OPT[o], eq(options.get(K_B_OPT[o]), TRUE));
    for(int o = 0; o < K_S_OPT.length; o++) if(options.contains(K_S_OPT[o]))
      nprops.put(S_OPT[o], string(options.get(K_S_OPT[o])));
  }

  /**
   * Caches original options and assigns cached options.
   */
  protected void assignOptions() {
    final Prop prop = qc.context.prop;
    for(final Object[] key : nprops.keySet()) {
      oprops.put(key, prop.get(key[0].toString()));
    }
    setProps(nprops);
  }

  /**
   * Restores original options.
   */
  protected void resetOptions() {
    setProps(oprops);
  }

  /**
   * Assigns the specified options.
   * @param props property map
   */
  private void setProps(final HashMap<Object[], Object> props) {
    final Prop prop = qc.context.prop;
    for(final Map.Entry<Object[], Object> e : props.entrySet()) {
      prop.setObject(e.getKey()[0].toString(), e.getValue());
    }
  }
}
