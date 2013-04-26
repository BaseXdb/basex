package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;

import org.basex.core.*;
import org.basex.core.Context;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.serial.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.util.json.*;
import org.basex.query.util.json.JsonParser.Spec;
import org.basex.query.util.pkg.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class organizes both static and dynamic properties that are specific to a
 * single query.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class QueryContext extends Progress {
  /** URL pattern (matching Clark and EQName notation). */
  private static final Pattern BIND =
      Pattern.compile("^((\"|')(.*?)\\2:|(\\{(.*?)\\}))(.+)$");

  /** Static context of an expression. */
  public StaticContext sc;
  /** Static variables. */
  public final Variables vars = new Variables();
  /** Functions. */
  public final UserFuncs funcs = new UserFuncs();

  /** Query resources. */
  public final QueryResources resource = new QueryResources(this);
  /** Database context. */
  public final Context context;

  /** HTTP context. */
  public Object http;

  /** Cached stop word files. */
  public HashMap<String, IO> stop;
  /** Cached thesaurus files. */
  public HashMap<String, IO> thes;
  /** Local options (key/value pairs), set by option declarations. */
  public final StringList dbOptions = new StringList();
  /** Global options (will be set after query execution). */
  public final HashMap<String, Object> globalOpt = new HashMap<String, Object>();

  /** Current context value. */
  public Value value;
  /** Current context position. */
  public long pos = 1;
  /** Current context size. */
  public long size = 1;
  /** Optional initial context set. */
  Nodes nodes;

  /** Current full-text options. */
  private FTOpt ftOpt;
  /** Current full-text token. */
  public FTLexer fttoken;

  /** Current Date. */
  public Item date;
  /** Current DateTime. */
  public Item dtm;
  /** Current Time. */
  public Item time;
  /** Current timezone. */
  public Item zone;

  /** Full-text position data (needed for highlighting of full-text results). */
  public FTPosData ftpos;
  /** Full-text token counter (needed for highlighting of full-text results). */
  public byte ftoknum;

  /** Strings to lock defined by lock:read option. */
  public StringList userReadLocks = new StringList(0);
  /** Strings to lock defined by lock:write option. */
  public StringList userWriteLocks = new StringList(0);

  /** Pending updates. */
  public Updates updates;
  /** Pending output. */
  public final ValueBuilder output = new ValueBuilder();

  /** Compilation flag: current node has leaves. */
  public boolean leaf;
  /** Compilation flag: GFLWOR clause performs grouping. */
  public boolean grouping;

  /** Number of successive tail calls. */
  public int tailCalls;
  /** Maximum number of successive tail calls. */
  public final int maxCalls;
  /** Counter for variable IDs. */
  public int varIDs;

  /** Pre-declared modules, containing module uri and their file paths. */
  final TokenMap modDeclared = new TokenMap();
  /** Parsed modules, containing the file path and module uri. */
  final TokenMap modParsed = new TokenMap();

  /** Serializer options. */
  SerializerProp serProp;
  /** Initial context value. */
  public Expr ctxItem;
  /** Module loader. */
  public final ModuleLoader modules;
  /** Opened connections to relational databases. */
  JDBCConnections jdbc;
  /** Opened connections to relational databases. */
  ClientSessions sessions;
  /** Root expression of the query. */
  MainModule root;

  /** String container for verbose query info. */
  private final TokenBuilder info = new TokenBuilder();
  /** Indicates if verbose query info was requested. */
  private final boolean inf;
  /** Indicates if some compilation info has been output. */
  private boolean compInfo;
  /** Indicates if some evaluation info has been output. */
  private boolean evalInfo;
  /** Indicates if the query context has been closed. */
  private boolean closed;

  /** The evaluation stack. */
  public final QueryStack stack = new QueryStack();

  /**
   * Constructor.
   * @param ctx database context
   */
  public QueryContext(final Context ctx) {
    context = ctx;
    nodes = ctx.current();
    inf = ctx.prop.is(Prop.QUERYINFO) || Prop.debug;
    sc = new StaticContext(ctx.prop.is(Prop.XQUERY3));
    maxCalls = ctx.prop.num(Prop.TAILCALLS);
    modules = new ModuleLoader(ctx);
  }

  /**
   * Parses the specified query.
   * @param qu input query
   * @param path file path (may be {@code null})
   * @throws QueryException query exception
   */
  public void parse(final String qu, final String path) throws QueryException {
    root = new QueryParser(qu, path, this).parseMain();
  }

  /**
   * Parses the specified module.
   * @param qu input query
   * @param path file path (may be {@code null})
   * @return name of module
   * @throws QueryException query exception
   */
  public QNm module(final String qu, final String path) throws QueryException {
    return new QueryParser(qu, path, this).parseModule(EMPTY);
  }

  /**
   * Sets the main module (root expression).
   * @param rt main module
   */
  public void mainModule(final MainModule rt) {
    root = rt;
    updating = root.expr.uses(Use.UPD);

  }

  /**
   * Compiles and optimizes the expression.
   * @throws QueryException query exception
   */
  public void compile() throws QueryException {
    // set database options
    final StringList o = dbOptions;
    for(int s = 0; s < o.size(); s += 2) context.prop.set(o.get(s), o.get(s + 1));

    if(ctxItem != null) {
      // evaluate initial expression
      try {
        value = ctxItem.compile(this, new VarScope()).value(this);
      } catch(final QueryException ex) {
        if(ex.err() != XPNOCTX) throw ex;
        // only {@link ParseExpr} instances may cause this error
        CIRCCTX.thrw(((ParseExpr) ctxItem).info);
      }
    } else if(nodes != null) {
      // add full-text container reference
      if(nodes.ftpos != null) ftpos = new FTPosData();
      // cache the initial context nodes
      resource.compile(nodes);
    }

    // if specified, convert context item to specified type
    // [LW] should not be necessary
    if(value != null && sc.initType != null) {
      value = SeqType.get(sc.initType, Occ.ONE).funcConvert(this, null, value);
    }

    // dynamic compilation
    analyze();

    // dump resulting query
    if(inf && compInfo) info.add(NL + OPTIMIZED_QUERY_C + NL + funcs + root + NL);
  }

  /**
   * Compiles all used functions and the root expression.
   * @throws QueryException query exception
   */
  public void analyze() throws QueryException {
    try {
      // compile the expression
      if(root != null) QueryCompiler.compile(this, root);
      // compile global functions.
      else funcs.compile(this);
    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      BASX_STACKOVERFLOW.thrw(null, ex);
    }
  }

  /**
   * Returns a result iterator.
   * @return result iterator
   * @throws QueryException query exception
   */
  public Iter iter() throws QueryException {
    try {
      // evaluate lazily if query will perform no updates
      return updating ? value().iter() : root.iter(this);
    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      throw BASX_STACKOVERFLOW.thrw(null);
    }
  }

  /**
   * Returns the result value.
   * @return result value
   * @throws QueryException query exception
   */
  public Value value() throws QueryException {
    try {
      final Value v = root.value(this);
      final Value u = update();
      return u != null ? u : v;
    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      throw BASX_STACKOVERFLOW.thrw(null);
    }
  }

  /**
   * Performs updates.
   * @return resulting value
   * @throws QueryException query exception
   */
  public Value update() throws QueryException {
    if(updating) {
      context.downgrade(this, updates.databases());
      updates.apply();
      if(updates.size() != 0 && context.data() != null) context.update();
      if(output.size() != 0) return output.value();
    }
    return null;
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
   * Evaluates the specified expression and returns an iterator.
   * @param expr expression to be evaluated
   * @return iterator
   * @throws QueryException query exception
   */
  public Value value(final Expr expr) throws QueryException {
    checkStop();
    return expr.value(this);
  }

  /**
   * Returns the current data reference of the context value, or {@code null}.
   * @return data reference
   */
  public Data data() {
    return value != null ? value.data() : null;
  }

  /**
   * Returns the databases that may be touched by this query. The returned information
   * will be more accurate if the function is called after parsing the query.
   * @see Progress#databases(LockResult)
   */
  @Override
  public void databases(final LockResult lr) {
    if(null != root)
      root.databases(lr, this);
    else lr.writeAll = true;
  }

  /**
   * Binds a value to the context item, using the same rules as for
   * {@link #bind binding variables}.
   * @param val value to be bound
   * @param type data type (may be {@code null})
   * @throws QueryException query exception
   */
  public void context(final Object val, final String type) throws QueryException {
    // bind http context to extra variable
    if(val.getClass().getName().equals("org.basex.http.HTTPContext")) {
      http = val;
    } else {
      ctxItem = cast(val, type);
    }
  }

  /**
   * Binds a value to a global variable. The specified type is interpreted as follows:
   * <ul>
   * <li>If {@code "json"} is specified, the value is converted according to the rules
   *     specified in {@link JsonMapConverter}.</li>
   * <li>If {@code "xml"} is specified, the value is converted to a document node.</li>
   * <li>Otherwise, the type is interpreted as atomic XDM data type.</li>
   * </ul>
   * If the value is an XQuery expression or value {@link Expr}, it is directly assigned.
   * Otherwise, it is cast to the XQuery data model, using a Java/XQuery mapping.
   * @param name name of variable
   * @param val value to be bound
   * @param type data type (may be {@code null})
   * @throws QueryException query exception
   */
  public void bind(final String name, final Object val, final String type)
      throws QueryException {
    bind(name, cast(val, type));
  }

  /**
   * Adds some compilation info.
   * @param string evaluation info
   * @param ext text text extensions
   */
  public void compInfo(final String string, final Object... ext) {
    if(!inf) return;
    if(!compInfo) {
      info.add(NL).add(COMPILING_C).add(NL);
      compInfo = true;
    }
    info.add(LI).addExt(string, ext).add(NL);
  }

  /**
   * Adds some evaluation info.
   * @param string evaluation info
   */
  public void evalInfo(final String string) {
    if(!inf) return;
    if(!evalInfo) {
      info.add(NL).add(EVALUATING_C).add(NL);
      evalInfo = true;
    }
    info.add(LI).add(string.replaceAll("\r?\n\\s*", " ")).add(NL);
  }

  /**
   * Returns info on query compilation and evaluation.
   * @return query info
   */
  public String info() {
    return info.toString();
  }

  /**
   * Returns JDBC connections.
   * @return jdbc connections
   */
  public JDBCConnections jdbc() {
    if(jdbc == null) jdbc = new JDBCConnections();
    return jdbc;
  }

  /**
   * Returns client sessions.
   * @return client session
   */
  public ClientSessions sessions() {
    if(sessions == null) sessions = new ClientSessions();
    return sessions;
  }

  /**
   * Returns the serialization parameters used for and specified by this query.
   * @param optional if {@code true}, a {@code null} reference is returned if no
   *   parameters have been specified
   * @return serialization parameters
   */
  public SerializerProp serParams(final boolean optional) {
    // if available, return parameters specified by the query
    if(serProp != null) return serProp;
    // retrieve global parameters
    final String serial = context.prop.get(Prop.SERIALIZER);
    if(optional && serial.isEmpty()) return null;
    // otherwise, if requested, return default parameters
    return new SerializerProp(serial);
  }

  /**
   * Returns the current full-text options. Creates a new instance if called first.
   * @return full-text options
   */
  public FTOpt ftOpt() {
    if(ftOpt == null) ftOpt = new FTOpt();
    return ftOpt;
  }

  /**
   * Sets full-text options.
   * @param opt full-text options
   */
  public void ftOpt(final FTOpt opt) {
    ftOpt = opt;
  }

  /**
   * Sets the updating flag.
   * @param up updating flag
   */
  public void updating(final boolean up) {
    if(updates == null) updates = new Updates();
    updating = up;
  }

  /**
   * Closes the query context.
   */
  public void close() {
    // close only once
    if(closed) return;
    closed = true;

    // reset database properties to initial value
    for(final Entry<String, Object> e : globalOpt.entrySet()) {
      context.prop.setObject(e.getKey(), e.getValue());
    }
    // close database connections
    resource.close();
    // close JDBC connections
    if(jdbc != null) jdbc.close();
    // close client sessions
    if(sessions != null) sessions.close();
    // close dynamically loaded JAR files
    modules.close();
  }

  @Override
  public String tit() {
    return SAVE;
  }

  @Override
  public String det() {
    return PLEASE_WAIT_D;
  }

  @Override
  public double prog() {
    return 0;
  }

  // CLASS METHODS ======================================================================

  /**
   * Evaluates the expression with the specified context set.
   * @return resulting value
   * @throws QueryException query exception
   */
  Result execute() throws QueryException {
    // limit number of hits to be returned and displayed
    int max = context.prop.num(Prop.MAXHITS);
    if(max < 0) max = Integer.MAX_VALUE;

    // evaluates the query
    final Iter ir = iter();
    final ValueBuilder vb = new ValueBuilder();
    Item it;

    // check if all results belong to the database of the input context
    if(serProp == null && nodes != null) {
      final IntList pre = new IntList();

      while((it = ir.next()) != null) {
        checkStop();
        if(it.data() != nodes.data) break;
        if(pre.size() < max) pre.add(((DBNode) it).pre);
      }

      final int ps = pre.size();
      if(it == null || ps == max) {
        // all nodes have been processed: return GUI-friendly nodeset
        return ps == 0 ? vb : new Nodes(pre.toArray(), nodes.data, ftpos);
      }

      // otherwise, add nodes to standard iterator
      for(int p = 0; p < ps; ++p) vb.add(new DBNode(nodes.data, pre.get(p)));
      vb.add(it);
    }

    // use standard iterator
    while((it = ir.next()) != null) {
      checkStop();
      if(vb.size() < max) vb.add(it.materialize(null));
    }
    return vb;
  }

  /**
   * Recursively builds a query plan.
   * @param doc root node
   */
  void plan(final FDoc doc) {
    // only show root node if functions or variables exist
    final FElem e = new FElem(PLAN);
    funcs.plan(e);
    vars.plan(e);
    root.plan(e);
    doc.add(e);
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Binds a value to a global variable. If the value is an {@link Expr}
   * instance, it is directly assigned. Otherwise, it is first cast to the
   * appropriate XQuery type.
   * @param name name of variable
   * @param val value to be bound
   * @return the variable if it could be bound, {@code null} otherwise
   * @throws QueryException query exception
   */
  private StaticVar bind(final String name, final Value val) throws QueryException {
    // remove optional $ prefix
    String nm = name.indexOf('$') == 0 ? name.substring(1) : name;
    byte[] uri = EMPTY;

    // check for namespace declaration
    final Matcher m = BIND.matcher(nm);
    if(m.find()) {
      String u = m.group(3);
      if(u == null) u = m.group(5);
      uri = token(u);
      nm = m.group(6);
    }
    final byte[] ln = token(nm);
    if(nm.isEmpty() || !XMLToken.isNCName(ln)) return null;

    // bind variable
    final QNm qnm = uri.length == 0 ? new QNm(ln, this) : new QNm(ln, uri);
    return vars.bind(qnm, val, this, null);
  }

  /**
   * Casts a value to the specified type.
   * See {@link #bind(String, Object, String)} for more infos.
   * @param val value to be cast
   * @param type data type (may be {@code null})
   * @return cast value
   * @throws QueryException query exception
   */
  private Value cast(final Object val, final String type) throws QueryException {
    // return original value
    if(type == null || type.isEmpty()) {
      return val instanceof Value ? (Value) val : JavaMapping.toValue(val);
    }

    // convert to json
    if(type.equalsIgnoreCase(JSONSTR)) {
      return new JsonMapConverter(Spec.ECMA_262, true, null).convert(val.toString());
    }

    // convert to the specified type
    final QNm nm = new QNm(token(type.replaceAll("\\(.*?\\)$", "")), this);
    if(!nm.hasURI() && nm.hasPrefix()) NOURI.thrw(null, nm);

    Type t = null;
    if(type.endsWith(")")) {
      t = NodeType.find(nm);
    } else {
      t = ListType.find(nm);
      if(t == null) t = AtomType.find(nm, false);
    }
    if(t == null) NOTYPE.thrw(null, type);
    return t.cast(val, null);
  }

  /**
   * Gets the value currently bound to the given variable.
   * @param var variable
   * @return bound value
   */
  public Value get(final Var var) {
    final Value val = stack.get(var);
    if(val == null) throw Util.notexpected(var);
    return val;
  }

  /**
   * Binds an expression to a local variable.
   * @param vr variable
   * @param vl expression to be bound
   * @param ii input info
   * @throws QueryException exception
   */
  public void set(final Var vr, final Value vl, final InputInfo ii)
      throws QueryException {
    stack.set(vr, vl, this, ii);
  }

  /**
   * Checks if there's a value bound to the given variable.
   * @param vr variable
   * @return {@code true} is a value is bound, {@code false} otherwise
   */
  public boolean isBound(final Var vr) {
    return stack.get(vr) != null;
  }
}
