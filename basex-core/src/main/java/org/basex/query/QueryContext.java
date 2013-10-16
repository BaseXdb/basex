package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.*;

import org.basex.build.*;
import org.basex.build.JsonOptions.JsonFormat;
import org.basex.build.JsonOptions.JsonSpec;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.core.Context;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.Flag;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.util.pkg.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * This class organizes both static and dynamic properties that are specific to a
 * single query.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class QueryContext extends Proc {
  /** URL pattern (matching Clark and EQName notation). */
  private static final Pattern BIND =
      Pattern.compile("^((\"|')(.*?)\\2:|(\\{(.*?)\\}))(.+)$");

  /** Static context of an expression. */
  public StaticContext sc;
  /** Static variables. */
  public final Variables vars = new Variables();
  /** Functions. */
  public final StaticFuncs funcs = new StaticFuncs();
  /** Externally bound variables. */
  public final HashMap<QNm, Expr> bindings = new HashMap<QNm, Expr>();

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
  /** Global database options (will be reassigned after query execution). */
  public final HashMap<Option, Object> staticOpts = new HashMap<Option, Object>();
  /** Temporary query options (key/value pairs), supplied by option declarations. */
  public final StringList tempOpts = new StringList();

  /** Current context value. */
  public Value value;
  /** Current context position. */
  public long pos = 1;
  /** Current context size. */
  public long size = 1;
  /** Optional initial context set. */
  Nodes nodes;

  /** Available collations. */
  public TokenObjMap<Collation> collations;
  /** Current full-text token. */
  public FTLexer ftToken;
  /** Current full-text options. */
  private FTOpt ftOpt;
  /** Full-text position data (needed for highlighting full-text results). */
  public FTPosData ftPosData;
  /** Full-text token positions (needed for highlighting full-text results). */
  public int ftPos;

  /** Current Date. */
  public Item date;
  /** Current DateTime. */
  public Item dtm;
  /** Current Time. */
  public Item time;
  /** Current timezone. */
  public Item zone;

  /** Strings to lock defined by lock:read option. */
  public StringList readLocks = new StringList(0);
  /** Strings to lock defined by lock:write option. */
  public StringList writeLocks = new StringList(0);

  /** Pending updates. */
  public Updates updates;
  /** Pending output. */
  public final ValueBuilder output = new ValueBuilder();

  /** Compilation flag: current node has leaves. */
  public boolean leaf;

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
  /** Stack of module files that are currently parsed. */
  final TokenList modStack = new TokenList();

  /** Serializer options. */
  SerializerOptions serialOpts;
  /** Initial context value. */
  public MainModule ctxItem;
  /** Module loader. */
  public final ModuleLoader modules;
  /** Opened connections to relational databases. */
  JDBCConnections jdbc;
  /** Opened connections to relational databases. */
  ClientSessions sessions;
  /** Root expression of the query. */
  MainModule root;
  /** Original query. */
  String query;

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
    inf = ctx.options.get(MainOptions.QUERYINFO) || Prop.debug;
    sc = new StaticContext(ctx.options.get(MainOptions.XQUERY3));
    maxCalls = ctx.options.get(MainOptions.TAILCALLS);
    modules = new ModuleLoader(ctx);
  }

  /**
   * Parses the specified query.
   * @param qu input query
   * @param path file path (may be {@code null})
   * @return main module
   * @throws QueryException query exception
   */
  public MainModule parseMain(final String qu, final String path) throws QueryException {
    query = qu;
    root = new QueryParser(qu, path, this).parseMain();
    return root;
  }

  /**
   * Parses the specified module.
   * @param qu input query
   * @param path file path (may be {@code null})
   * @return name of module
   * @throws QueryException query exception
   */
  public LibraryModule parseLibrary(final String qu, final String path) throws QueryException {
    query = qu;
    return new QueryParser(qu, path, this).parseLibrary(true);
  }

  /**
   * Sets the main module (root expression).
   * @param rt main module
   */
  public void mainModule(final MainModule rt) {
    root = rt;
    updating = rt.expr.has(Flag.UPD);
  }

  /**
   * Compiles and optimizes the expression.
   * @throws QueryException query exception
   */
  public void compile() throws QueryException {
    // set database options
    final StringList o = tempOpts;
    for(int s = 0; s < o.size(); s += 2) {
      try {
        context.options.assign(o.get(s).toUpperCase(Locale.ENGLISH), o.get(s + 1));
      } catch(final BaseXException ex) {
        BASX_VALUE.thrw(null, o.get(s), o.get(s + 1));
      }
    }

    // bind external variables
    vars.bindExternal(this, bindings);

    if(ctxItem != null) {
      // evaluate initial expression
      try {
        ctxItem.compile(this);
        value = ctxItem.value(this);
      } catch(final QueryException ex) {
        if(ex.err() != NOCTX) throw ex;
        // only {@link ParseExpr} instances may cause this error
        CIRCCTX.thrw(ctxItem.info);
      }
    } else if(nodes != null) {
      // add full-text container reference
      if(nodes.ftpos != null) ftPosData = new FTPosData();
      // cache the initial context nodes
      resource.compile(nodes);
    }

    // if specified, convert context item to specified type
    // [LW] should not be necessary
    if(value != null && sc.initType != null) {
      value = sc.initType.funcConvert(this, null, value);
    }

    // dynamic compilation
    analyze();

    // dump resulting query
    if(inf) {
      info.add(NL).add(QUERY).add(COL).add(NL).add(
          QueryProcessor.removeComments(query, Integer.MAX_VALUE)).add(NL);
      if(compInfo) info.add(NL + OPTIMIZED_QUERY + COL + NL + funcs + root + NL);
    }
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
      //context.downgrade(this, updates.databases());
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

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(readLocks);
    lr.write.add(writeLocks);
    if(root == null || !root.databases(lr, this)) {
      if(updating) lr.writeAll = true;
      else lr.readAll = true;
    }
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
      ctxItem = new MainModule(cast(val, type), new VarScope(), null);
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
   * If the value is an XQuery value {@link Value}, it is directly assigned.
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
      info.add(NL).add(COMPILING).add(COL).add(NL);
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
      info.add(NL).add(EVALUATING).add(COL).add(NL);
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
   * @throws BaseXException database exception
   */
  public SerializerOptions serParams(final boolean optional) throws BaseXException {
    // if available, return parameters specified by the query
    if(serialOpts != null) return serialOpts;
    // retrieve global parameters
    final String serial = context.options.get(MainOptions.SERIALIZER);
    if(optional && serial.isEmpty()) return null;
    // otherwise, if requested, return default parameters
    return new SerializerOptions(serial);
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

    // reassign original database options
    for(final Entry<Option, Object> e : staticOpts.entrySet())
      context.options.put(e.getKey(), e.getValue());

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
    int max = context.options.get(MainOptions.MAXHITS);
    if(max < 0) max = Integer.MAX_VALUE;

    // evaluates the query
    final Iter ir = iter();
    final ValueBuilder vb = new ValueBuilder();
    Item it;

    // check if all results belong to the database of the input context
    if(serialOpts == null && nodes != null) {
      final IntList pre = new IntList();

      while((it = ir.next()) != null) {
        checkStop();
        if(it.data() != nodes.data) break;
        if(pre.size() < max) pre.add(((DBNode) it).pre);
      }

      final int ps = pre.size();
      if(it == null || ps == max) {
        // all nodes have been processed: return GUI-friendly nodeset
        return ps == 0 ? vb : new Nodes(pre.toArray(), nodes.data, ftPosData);
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
    final FElem e = new FElem(QueryText.PLAN);
    funcs.plan(e);
    vars.plan(e);
    root.plan(e);
    doc.add(e);
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Binds an expression to a global variable.
   * @param name name of variable
   * @param e value to be bound
   */
  private void bind(final String name, final Expr e) {
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

    // [LW] better throw an error
    if(nm.isEmpty() || !XMLToken.isNCName(ln)) return;

    // bind variable
    bindings.put(uri.length == 0 ? new QNm(ln, this) : new QNm(ln, uri), e);
  }

  /**
   * Casts a value to the specified type.
   * See {@link #bind(String, Object, String)} for more infos.
   * @param val value to be cast
   * @param type data type (may be {@code null})
   * @return cast value
   * @throws QueryException query exception
   */
  private Expr cast(final Object val, final String type) throws QueryException {
    // return original value
    if(type == null || type.isEmpty())
      return val instanceof Expr ? (Expr) val : JavaMapping.toValue(val, this);

    // convert to json
    try {
      if(type.equalsIgnoreCase(MainParser.JSON.toString())) {
        final JsonParserOptions jp = new JsonParserOptions();
        jp.set(JsonOptions.SPEC, JsonSpec.ECMA_262);
        jp.set(JsonOptions.FORMAT, JsonFormat.MAP);
        return JsonConverter.convert(Token.token(val.toString()), jp);
      }
    } catch(final QueryIOException ex) {
      throw ex.getCause();
    }

    // convert to the specified type
    final QNm nm = new QNm(token(type.replaceAll("\\(.*?\\)$", "")), this);
    if(!nm.hasURI() && nm.hasPrefix()) NOURI.thrw(null, nm.string());

    Type t = null;
    if(type.endsWith(")")) {
      t = NodeType.find(nm);
    } else {
      t = ListType.find(nm);
      if(t == null) t = AtomType.find(nm, false);
    }
    if(t == null) NOTYPE.thrw(null, type);
    return t.cast(val, this, null);
  }

  /**
   * Gets the value currently bound to the given variable.
   * @param var variable
   * @return bound value
   */
  public Value get(final Var var) {
    return stack.get(var);
  }

  /**
   * Binds an expression to a local variable.
   * @param vr variable
   * @param vl expression to be bound
   * @param ii input info
   * @throws QueryException exception
   */
  public void set(final Var vr, final Value vl, final InputInfo ii) throws QueryException {
    stack.set(vr, vl, this, ii);
  }

  /**
   * Initializes the static date and time context of a query if not done yet.
   * @return self reference
   * @throws QueryException query exception
   */
  public QueryContext initDateTime() throws QueryException {
    if(time == null) {
      final Date d = Calendar.getInstance().getTime();
      final String zon = DateTime.format(d, DateTime.ZONE);
      final String ymd = DateTime.format(d, DateTime.DATE);
      final String hms = DateTime.format(d, DateTime.TIME);
      final String zn = zon.substring(0, 3) + ':' + zon.substring(3);
      time = new Tim(Token.token(hms + zn), null);
      date = new Dat(Token.token(ymd + zn), null);
      dtm = new Dtm(Token.token(ymd + 'T' + hms + zn), null);
      zone = new DTDur(toInt(zon.substring(0, 3)), toInt(zon.substring(3)));
    }
    return this;
  }
}
