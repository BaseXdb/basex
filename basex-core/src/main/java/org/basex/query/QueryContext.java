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
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class QueryContext extends Proc {
  /** URL pattern (matching Clark and EQName notation). */
  private static final Pattern BIND = Pattern.compile("^((\"|')(.*?)\\2:|Q?(\\{(.*?)\\}))(.+)$");

  /** The evaluation stack. */
  public final QueryStack stack = new QueryStack();
  /** Static variables. */
  public final Variables vars = new Variables();
  /** Functions. */
  public final StaticFuncs funcs = new StaticFuncs();
  /** Externally bound variables. */
  private final HashMap<QNm, Expr> bindings = new HashMap<>();

  /** Parent query context. */
  private final QueryContext qcParent;
  /** Query info. */
  public final QueryInfo info;
  /** Database context. */
  public final Context context;

  /** Query resources. */
  public QueryResources resources;
  /** HTTP context. */
  public Object http;

  /** Cached stop word files. */
  public HashMap<String, IO> stop;
  /** Cached thesaurus files. */
  public HashMap<String, IO> thes;
  /** Global database options (will be reassigned after query execution). */
  public final HashMap<Option<?>, Object> staticOpts = new HashMap<>();
  /** Temporary query options (key/value pairs), supplied by option declarations. */
  public final StringList tempOpts = new StringList();

  /** Current context value. */
  public Value value;
  /** Current context position. */
  public long pos = 1;
  /** Current context size. */
  public long size = 1;
  /** Globally available nodes (may be {@code null}). */
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
  public final StringList readLocks = new StringList(0);
  /** Strings to lock defined by lock:write option. */
  public final StringList writeLocks = new StringList(0);

  /** Number of successive tail calls. */
  public int tailCalls;
  /** Maximum number of successive tail calls (will be set before compilation). */
  public int maxCalls;

  /** Function for the next tail call. */
  private XQFunction tailFunc;
  /** Arguments for the next tail call. */
  private Value[] args;
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

  /** Root expression of the query. */
  public MainModule root;

  /** Indicates if the query context has been closed. */
  private boolean closed;

  /**
   * Constructor.
   * @param qcParent parent context
   */
  public QueryContext(final QueryContext qcParent) {
    this(qcParent.context, qcParent);
    listen = qcParent.listen;
    resources = qcParent.resources;
  }

  /**
   * Constructor.
   * @param context database context
   */
  public QueryContext(final Context context) {
    this(context, null);
    resources = new QueryResources(this);
  }

  /**
   * Constructor.
   * @param context database context
   * @param qcParent parent context (optional)
   */
  private QueryContext(final Context context, final QueryContext qcParent) {
    this.context = context;
    this.qcParent = qcParent;
    info = new QueryInfo(this);
  }

  /**
   * Parses the specified query.
   * @param query query string
   * @param path file path (may be {@code null})
   * @param sc static context
   * @return main module
   * @throws QueryException query exception
   */
  public StaticScope parse(final String query, final String path, final StaticContext sc)
      throws QueryException {
    return parse(query, QueryProcessor.isLibrary(query), path, sc);
  }

  /**
   * Parses the specified query.
   * @param query query string
   * @param library library/main module
   * @param path file path (may be {@code null})
   * @param sc static context
   * @return main module
   * @throws QueryException query exception
   */
  public StaticScope parse(final String query, final boolean library, final String path,
      final StaticContext sc) throws QueryException {
    return library ? parseLibrary(query, path, sc) : parseMain(query, path, sc);
  }

  /**
   * Parses the specified query.
   * @param query query string
   * @param path file path (may be {@code null})
   * @param sc static context
   * @return main module
   * @throws QueryException query exception
   */
  public MainModule parseMain(final String query, final String path, final StaticContext sc)
      throws QueryException {

    info.query = query;
    root = new QueryParser(query, path, this, sc).parseMain();
    updating = updating && root.expr.has(Flag.UPD);
    return root;
  }

  /**
   * Parses the specified module.
   * @param query query string
   * @param path file path (may be {@code null})
   * @param sc static context
   * @return name of module
   * @throws QueryException query exception
   */
  public LibraryModule parseLibrary(final String query, final String path, final StaticContext sc)
      throws QueryException {

    info.query = query;
    return new QueryParser(query, path, this, sc).parseLibrary(true);
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
   * Checks function calls and variable references.
   * @param main main module
   * @param sc static context
   * @throws QueryException query exception
   */
  void check(final MainModule main, final StaticContext sc) throws QueryException {
    // check function calls and variable references
    funcs.check(this);
    vars.check();

    // check placement of updating expressions if any have been found
    if(!sc.mixUpdates && updating) {
      funcs.checkUp();
      vars.checkUp();
      if(main != null) main.expr.checkUp();
    }
  }

  /**
   * Compiles and optimizes the expression.
   * @throws QueryException query exception
   */
  public void compile() throws QueryException {
    if(nodes == null) nodes = context.current();

    // set database options
    final StringList o = tempOpts;
    for(int s = 0; s < o.size(); s += 2) {
      try {
        context.options.assign(o.get(s).toUpperCase(Locale.ENGLISH), o.get(s + 1));
      } catch(final BaseXException ex) {
        throw BASX_VALUE.get(null, o.get(s), o.get(s + 1));
      }
    }
    // set tail call option after assignment database option
    maxCalls = context.options.get(MainOptions.TAILCALLS);

    // bind external variables
    vars.bindExternal(this, bindings);

    if(ctxItem != null) {
      // evaluate initial expression
      try {
        ctxItem.compile(this);
        value = ctxItem.cache(this).value();
      } catch(final QueryException ex) {
        if(ex.err() != NOCTX) throw ex;
        // only {@link ParseExpr} instances may cause this error
        throw CIRCCTX.get(ctxItem.info);
      }
    } else if(nodes != null) {
      // add full-text container reference
      if(nodes.ftpos != null) ftPosData = new FTPosData();
      // cache the initial context nodes
      resources.compile(nodes);
    }

    // if specified, convert context item to specified type
    // [LW] should not be necessary
    if(value != null && root.sc.initType != null) {
      value = root.sc.initType.promote(this, root.sc, null, value, true);
    }

    // dynamic compilation
    analyze();
    info.runtime = true;
  }

  /**
   * Compiles all used functions and the root expression.
   * @throws QueryException query exception
   */
  private void analyze() throws QueryException {
    try {
      // compile the expression
      if(root != null) QueryCompiler.compile(this, root);
      // compile global functions.
      else funcs.compile(this);
    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      throw BASX_STACKOVERFLOW.get(null, ex);
    }
  }

  /**
   * Returns a result iterator.
   * @return result iterator
   * @throws QueryException query exception
   */
  public Iter iter() throws QueryException {
    try {
      // no updates: iterate through results
      if(!updating) return root.iter(this);

      // cache results
      ValueBuilder cache = root.cache(this);

      final Updates updates = resources.updates;
      if(updates != null) {
        // if parent context exists, updates will be performed by main context
        if(qcParent == null) {
          final ValueBuilder output = resources.output;
          final StringList dbs = updates.databases();
          final HashSet<Data> datas = updates.prepare();

          // copy nodes that will be affected by an update operation
          copy(cache, datas, dbs);
          copy(output, datas, dbs);

          if(context.data() != null) context.invalidate();
          updates.apply();

          // append cached outputs
          if(output.size() != 0) {
            if(cache.size() == 0) cache = output;
            else cache.add(output.value());
          }
        }
      }
      return cache;

    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      throw BASX_STACKOVERFLOW.get(null);
    }
  }

  /**
   * Creates copies of nodes that will be affected by an update operation.
   * @param cache node cache
   * @param datas data references
   * @param dbs database names
   */
  private void copy(final ValueBuilder cache, final HashSet<Data> datas, final StringList dbs) {
    final long cs = cache.size();
    for(int c = 0; c < cs; c++) {
      final Item it = cache.get(c);
      if(!(it instanceof DBNode)) continue;
      final Data data = it.data();
      if(datas.contains(data) || !data.inMemory() && dbs.contains(data.meta.name)) {
        cache.set(c, ((DBNode) it).dbCopy(context.options));
      }
    }
  }

  /**
   * Evaluates the specified expression and returns an iterator.
   * @param expr expression to be evaluated
   * @return iterator
   * @throws QueryException query exception
   */
  public Iter iter(final Expr expr) throws QueryException {
    checkStop();
    return expr.iter(this);
  }

  /**
   * Evaluates the specified expression and returns a value.
   * @param expr expression to be evaluated
   * @return value
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
    if(root == null || !root.databases(lr, this) ||
       ctxItem != null && !ctxItem.databases(lr, this)) {

      if(updating) lr.writeAll = true;
      else lr.readAll = true;
    }
  }

  /**
   * Binds the HTTP context.
   * @param val HTTP context
   */
  public void http(final Object val) {
    http = val;
  }

  /**
   * Binds a value to the context item, using the same rules as for
   * {@link #bind(String, Object, String) binding variables}.
   * @param val value to be bound
   * @param type data type (may be {@code null})
   * @param sc static context
   * @throws QueryException query exception
   */
  public void context(final Object val, final String type, final StaticContext sc)
      throws QueryException {
    context(cast(val, type), sc);
  }

  /**
   * Binds a value to the context item.
   * @param val value to be bound
   * @param sc static context
   */
  public void context(final Value val, final StaticContext sc) {
    ctxItem = new MainModule(val, new VarScope(sc), null, sc);
  }

  /**
   * Binds a value to a global variable. The specified type is interpreted as follows:
   * <ul>
   * <li>If {@code "json"} is specified, the value is converted according to the rules
   *     specified in {@link JsonMapConverter}.</li>
   * <li>Otherwise, the type is cast to the specified XDM data type.</li>
   * </ul>
   * If the value is an XQuery {@link Value}, it is directly assigned.
   * Otherwise, it is cast to the XQuery data model, using a Java/XQuery mapping.
   * @param name name of variable
   * @param val value to be bound
   * @param type data type (may be {@code null})
   * @throws QueryException query exception
   */
  public void bind(final String name, final Object val, final String type) throws QueryException {
    bind(name, cast(val, type));
  }

  /**
   * Binds a value to a global variable.
   * @param name name of variable
   * @param val value to be bound
   * @throws QueryException query exception
   */
  public void bind(final String name, final Value val) throws QueryException {
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
    if(nm.isEmpty() || !XMLToken.isNCName(ln)) throw BINDNAME.get(null, nm);

    // bind variable
    bindings.put(new QNm(ln, uri), val);
  }

  /**
   * Adds some compilation info.
   * @param string evaluation info
   * @param ext text text extensions
   */
  public void compInfo(final String string, final Object... ext) {
    info.compInfo(string,  ext);
  }

  /**
   * Adds some evaluation info.
   * @param string evaluation info
   */
  public void evalInfo(final String string) {
    (qcParent != null ? qcParent.info : info).evalInfo(string);
  }

  /**
   * Returns info on query compilation and evaluation.
   * @return query info
   */
  public String info() {
    return info.toString(this);
  }

  /**
   * Returns the query-specific or global serialization parameters.
   * @return serialization parameters
   */
  public SerializerOptions serParams() {
    return serialOpts != null ? serialOpts : context.options.get(MainOptions.SERIALIZER);
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
   * Indicates that the query contains any updating expressions.
   */
  public void updating() {
    updating = true;
  }

  /**
   * Closes the query context.
   */
  public void close() {
    // close only once
    if(closed) return;

    if(qcParent == null) {
      closed = true;
      resources.close();
    }

    // reassign original database options
    for(final Entry<Option<?>, Object> e : staticOpts.entrySet())
      context.options.put(e.getKey(), e.getValue());
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
    if(root != null) {
      for(final StaticScope scp : QueryCompiler.usedDecls(root)) scp.plan(e);
    } else {
      funcs.plan(e);
      vars.plan(e);
    }
    root.plan(e);
    doc.add(e);
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Casts a value to the specified type.
   * See {@link #bind(String, Object, String)} for more infos.
   * @param val value to be cast
   * @param type data type (may be {@code null})
   * @return cast value
   * @throws QueryException query exception
   */
  private Value cast(final Object val, final String type) throws QueryException {
    final StaticContext sc = root != null ? root.sc : new StaticContext(context);

    // String input
    Object vl = val;
    if(vl instanceof String) {
      final String string = (String) vl;
      final StringList strings = new StringList(1);
      // strings containing multiple items (value \1 ...)
      if(string.indexOf('\1') != -1) {
        strings.add(string.split("\1"));
        vl = strings.toArray();
      } else {
        strings.add(string);
      }

      // sub types overriding the global value (value \2 type)
      if(string.indexOf('\2') != -1) {
        final ValueBuilder vb = new ValueBuilder(strings.size());
        for(final String str : strings) {
          final int i = str.indexOf('\2');
          final String s = i == -1 ? str : str.substring(0, i);
          final String t = i == -1 ? type : str.substring(i + 1);
          vb.add(cast(s, t));
        }
        return vb.value();
      }
    }

    // no type specified: return original value or convert Java object
    if(type == null || type.isEmpty()) {
      return vl instanceof Value ? (Value) vl : JavaMapping.toValue(vl, this, sc);
    }

    // convert to json
    if(type.equalsIgnoreCase(MainParser.JSON.name())) {
      try {
        final JsonParserOptions jp = new JsonParserOptions();
        jp.set(JsonOptions.SPEC, JsonSpec.ECMA_262);
        jp.set(JsonOptions.FORMAT, JsonFormat.MAP);
        final JsonConverter conv = JsonConverter.get(jp);
        conv.convert(token(vl.toString()), null);
        return conv.finish();
      } catch(final QueryIOException ex) {
        throw ex.getCause();
      }
    }

    // test for empty sequence
    if(type.equals(QueryText.EMPTY_SEQUENCE + "()")) return Empty.SEQ;

    // convert to the specified type
    // [LW] type should be parsed properly
    final QNm nm = new QNm(token(type.replaceAll("\\(.*?\\)$", "")), sc);
    if(!nm.hasURI() && nm.hasPrefix()) throw NOURI.get(null, nm.string());

    Type tp;
    if(type.endsWith(")")) {
      if(nm.eq(AtomType.ITEM.name)) tp = AtomType.ITEM;
      else tp = NodeType.find(nm);
      if(tp == null) tp = FuncType.find(nm);
    } else {
      tp = ListType.find(nm);
      if(tp == null) tp = AtomType.find(nm, false);
    }
    if(tp == null) throw NOTYPE.get(null, type);

    // cast XDM values
    if(vl instanceof Value) {
      // cast single item
      if(vl instanceof Item) return tp.cast((Item) vl, this, sc, null);
      // cast sequence
      final Value v = (Value) vl;
      final ValueBuilder seq = new ValueBuilder((int) v.size());
      for(final Item i : v) seq.add(tp.cast(i, this, sc, null));
      return seq.value();
    }

    if(vl instanceof String[]) {
      // cast string array
      final String[] strings = (String[]) vl;
      final ValueBuilder seq = new ValueBuilder(strings.length);
      for(final String s : strings) seq.add(tp.cast(s, this, sc, null));
      return seq.value();
    }

    // cast any other object to XDM
    return tp.cast(vl, this, sc, null);
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
   * Registers a tail-called function and its arguments to this query context.
   * @param fn function to call
   * @param arg arguments to pass to {@code fn}
   */
  public void registerTailCall(final XQFunction fn, final Value[] arg) {
    tailFunc = fn;
    args = arg;
  }

  /**
   * Returns and clears the currently registered tail-call function.
   * @return function to call if present, {@code null} otherwise
   */
  public XQFunction pollTailCall() {
    final XQFunction fn = tailFunc;
    tailFunc = null;
    return fn;
  }

  /**
   * Returns and clears registered arguments of a tail-called function.
   * @return argument values if a tail call was registered, {@code null} otherwise
   */
  public Value[] pollTailArgs() {
    final Value[] as = args;
    args = null;
    return as;
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
      time = new Tim(token(hms + zn), null);
      date = new Dat(token(ymd + zn), null);
      dtm = new Dtm(token(ymd + 'T' + hms + zn), null);
      zone = new DTDur(toInt(zon.substring(0, 3)), toInt(zon.substring(3)));
    }
    return this;
  }
}
