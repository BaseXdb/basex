package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.basex.core.Context;
import org.basex.core.Progress;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.FTPosData;
import org.basex.data.Nodes;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.data.SerializerException;
import org.basex.data.SerializerProp;
import org.basex.io.IO;
import org.basex.query.expr.Expr;
import org.basex.query.expr.ParseExpr;
import org.basex.query.item.DBNode;
import org.basex.query.item.Dat;
import org.basex.query.item.Dtm;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.item.Tim;
import org.basex.query.item.Uri;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemCache;
import org.basex.query.up.Updates;
import org.basex.query.util.Err;
import org.basex.query.util.Functions;
import org.basex.query.util.Namespaces;
import org.basex.query.util.Variables;
import org.basex.query.util.format.DecFormatter;
import org.basex.util.IntList;
import org.basex.util.StringList;
import org.basex.util.TokenBuilder;
import org.basex.util.TokenObjMap;
import org.basex.util.Util;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTOpt;

/**
 * This abstract query expression provides the architecture for a compiled
 * query. // *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class QueryContext extends Progress {
  /** Functions. */
  public final Functions funcs = new Functions();
  /** Variables. */
  public final Variables vars = new Variables();
  /** Namespaces. */
  public Namespaces ns = new Namespaces();

  /** Query resources. */
  public final QueryResources resource;
  /** Database context. */
  public final Context context;
  /** Query string. */
  public String query;
  /** XQuery version flag. */
  public boolean xquery3;

  /** Cached stop word files. */
  public HashMap<String, String> stop;
  /** Cached thesaurus files. */
  public HashMap<String, String> thes;

  /** Reference to the root expression. */
  public Expr root;
  /** Current context value. */
  public Value value;
  /** Current context position. */
  public long pos;
  /** Current context size. */
  public long size;

  /** Current full-text options. */
  public FTOpt ftopt;
  /** Current full-text token. */
  public FTLexer fttoken;

  /** Current Date. */
  public Dat date;
  /** Current DateTime. */
  public Dtm dtm;
  /** Current Time. */
  public Tim time;

  /** Decimal-format declarations. */
  public TokenObjMap<DecFormatter> decFormats =
    new TokenObjMap<DecFormatter>();
  /** Default function namespace. */
  public byte[] nsFunc = FNURI;
  /** Default element namespace. */
  public byte[] nsElem = EMPTY;
  /** Static Base URI. */
  public Uri baseURI = Uri.EMPTY;
  /** Default collation. */
  public Uri collation = Uri.uri(URLCOLL);

  /** Default boundary-space. */
  public boolean spaces;
  /** Empty Order mode. */
  public boolean orderGreatest;
  /** Preserve Namespaces. */
  public boolean nsPreserve = true;
  /** Inherit Namespaces. */
  public boolean nsInherit = true;
  /** Ordering mode. */
  public boolean ordered;
  /** Construction mode. */
  public boolean construct;

  /** Full-text position data (needed for highlighting of full-text results). */
  public FTPosData ftpos;
  /** Full-text token counter (needed for highlighting of full-text results). */
  public byte ftoknum;

  /** Copied nodes, resulting from transform expression. */
  public final Set<Data> copiedNods = new HashSet<Data>();
  /** Pending updates. */
  public Updates updates = new Updates(false);
  /** Indicates if this query performs updates. */
  public boolean updating;

  /** Compilation flag: current node has leaves. */
  public boolean leaf;
  /** Compilation flag: GFLWOR clause performs grouping. */
  public boolean grouping;

  /** List of modules. */
  final StringList modules = new StringList();
  /** List of loaded modules. */
  final StringList modLoaded = new StringList();
  /** Serializer options. */
  SerializerProp serProp;
  /** Initial context set (default: null). */
  Nodes nodes;

  /** Initial context value type. */
  SeqType initType;
  /** Initial context value. */
  Expr initExpr;

  /** String container for query background information. */
  private final TokenBuilder info = new TokenBuilder();
  /** Info flag. */
  private final boolean inf;
  /** Optimization flag. */
  private boolean firstOpt = true;
  /** Evaluation flag. */
  private boolean firstEval = true;

  /** Counter for variable IDs. */
  private volatile int varIDs;

  /**
   * Constructor.
   * @param ctx context reference
   */
  public QueryContext(final Context ctx) {
    resource = new QueryResources(this);
    context = ctx;
    nodes = ctx.current;
    ftopt = new FTOpt();
    xquery3 = ctx.prop.is(Prop.XQUERY3);
    inf = ctx.prop.is(Prop.QUERYINFO);
    if(ctx.query != null) baseURI = Uri.uri(token(ctx.query.url()));
  }

  /**
   * Parses the specified query.
   * @param q input query
   * @throws QueryException query exception
   */
  public void parse(final String q) throws QueryException {
    root = new QueryParser(q, this).parse(base(), null);
    query = q;
  }

  /**
   * Parses the specified module.
   * @param q input query
   * @throws QueryException query exception
   */
  public void module(final String q) throws QueryException {
    new QueryParser(q, this).parse(base(), Uri.EMPTY);
  }

  /**
   * Compiles and optimizes the expression.
   * @throws QueryException query exception
   */
  public void compile() throws QueryException {
    // dump compilation info
    if(inf) compInfo(NL + QUERYCOMP);

    if(initExpr != null) {
      // evaluate initial expression
      try {
        value = initExpr.value(this);
      } catch(final QueryException ex) {
        // only {@link ParseExpr} instance will throw an exception
        final Err err = ex.err();
        if(err != Err.XPNOCTX) throw ex;
        CTXINIT.thrw(((ParseExpr) initExpr).input, ex.getMessage());
      }
    } else if(nodes != null) {
      // add full-text container reference
      if(nodes.ftpos != null) ftpos = new FTPosData();
      // cache the initial context nodes
      resource.compile(nodes);
    }

    // if specified, convert context item to specified type
    if(value != null && initType != null) {
      value = initType.cast(value, this, null);
    }

    try {
      // compile global functions.
      // variables will be compiled if called for the first time
      funcs.comp(this);
      // compile the expression
      root = root.comp(this);
    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      XPSTACK.thrw(null);
    }

    // dump resulting query
    if(inf) info.add(NL + QUERYRESULT + funcs + root + NL);
  }

  /**
   * Evaluates the expression with the specified context set.
   * @return resulting value
   * @throws QueryException query exception
   */
  protected Result eval() throws QueryException {
    // evaluates the query
    final Iter it = iter();
    final ItemCache ir = new ItemCache();
    Item i;

    // check if all results belong to the database of the input context
    if(nodes != null) {
      final Data data = nodes.data;
      final IntList pre = new IntList();

      while((i = it.next()) != null) {
        checkStop();
        if(!(i instanceof DBNode)) break;
        final DBNode n = (DBNode) i;
        if(n.data != data) break;
        pre.add(((DBNode) i).pre);
      }

      // completed... return standard nodeset with full-text positions
      final int ps = pre.size();
      if(i == null) return ps == 0 ? ir :
          new Nodes(pre.toArray(), data, ftpos).checkRoot();

      // otherwise, add nodes to standard iterator
      for(int p = 0; p < ps; ++p) ir.add(new DBNode(data, pre.get(p)));
      ir.add(i);
    }

    // use standard iterator
    while((i = it.next()) != null) {
      checkStop();
      ir.add(i);
    }
    return ir;
  }

  /**
   * Returns a result iterator.
   * @return result iterator
   * @throws QueryException query exception
   */
  public Iter iter() throws QueryException {
    try {
      final Iter iter = iter(root);
      if(!updating) return iter;

      final Value v = iter.finish();
      updates.apply(this);
      if(context.data != null) context.update();
      return v.iter(this);
    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      throw XPSTACK.thrw(null);
    }
  }

  /**
   * Recursively serializes the query plan.
   * @param ser serializer
   * @throws Exception exception
   */
  protected void plan(final Serializer ser) throws Exception {
    // only show root node if functions or variables exist
    final boolean r = funcs.size() != 0 || vars.global().size != 0;
    if(r) ser.openElement(PLAN);
    funcs.plan(ser);
    root.plan(ser);
    if(r) ser.closeElement();
  }

  /**
   * Evaluates the specified expression and returns an iterator.
   * @param e expression to be evaluated
   * @return iterator
   * @throws QueryException query exception
   */
  public Iter iter(final Expr e) throws QueryException {
    checkStop();
    return e.iter(this);
  }

  /**
   * Copies properties of the specified context.
   * @param ctx context
   */
  public void copy(final QueryContext ctx) {
    baseURI = ctx.baseURI;
    spaces = ctx.spaces;
    construct = ctx.construct;
    nsInherit = ctx.nsInherit;
    nsPreserve = ctx.nsPreserve;
    collation = ctx.collation;
    nsElem = ctx.nsElem;
    nsFunc = ctx.nsFunc;
    orderGreatest = ctx.orderGreatest;
    ordered = ctx.ordered;
  }

  /**
   * Adds some optimization info.
   * @param string evaluation info
   * @param ext text text extensions
   */
  public void compInfo(final String string, final Object... ext) {
    if(!inf) return;
    if(!firstOpt) info.add(QUERYSEP);
    firstOpt = false;
    info.addExt(string, ext);
    info.add(NL);
  }

  /**
   * Adds some evaluation info.
   * @param string evaluation info
   */
  public void evalInfo(final byte[] string) {
    if(!inf) return;
    if(firstEval) info.add(NL + QUERYEVAL + NL);
    info.add(QUERYSEP).add(string).add(NL);
    firstEval = false;
  }

  /**
   * Generates the next unique variable ID.
   * @return variable ID
   */
  public int nextVarID() {
    return varIDs++;
  }

  /**
   * Returns an IO representation of the base uri.
   * @return IO reference
   */
  public IO base() {
    return baseURI != Uri.EMPTY ? IO.get(string(baseURI.atom())) : null;
  }

  /**
   * Returns info on query compilation and evaluation.
   * @return query info
   */
  String info() {
    return info.toString();
  }

  /**
   * Returns the serialization properties.
   * @return serialization properties
   * @throws SerializerException serializer exception
   */
  public SerializerProp serProp() throws SerializerException {
    // if available, use local query properties
    if(serProp != null) return serProp;
    // otherwise, apply global serialization option
    final SerializerProp sp = new SerializerProp(
        context.prop.get(Prop.SERIALIZER));
    if(context.prop.is(Prop.WRAPOUTPUT)) {
      sp.set(SerializerProp.S_WRAP_PREFIX, NAMELC);
      sp.set(SerializerProp.S_WRAP_URI, URL);
    }
    return sp;
  }

  @Override
  public String tit() {
    return QUERYEVAL;
  }

  @Override
  public String det() {
    return QUERYEVAL;
  }

  @Override
  public double prog() {
    return 0;
  }

  @Override
  public String toString() {
    return Util.name(this) + '[' + base() + ']';
  }
}
