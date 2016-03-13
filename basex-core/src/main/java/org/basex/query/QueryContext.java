package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.up.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.ft.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.DateTime;
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * This class organizes both static and dynamic properties that are specific to a
 * single query.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class QueryContext extends Proc implements Closeable {
  /** The evaluation stack. */
  public final QueryStack stack = new QueryStack();
  /** Static variables. */
  public final Variables vars = new Variables();
  /** Functions. */
  public final StaticFuncs funcs = new StaticFuncs();
  /** Externally bound variables. */
  private final HashMap<QNm, Value> bindings = new HashMap<>();

  /** Parent query context. */
  private final QueryContext parent;

  /** Query info. */
  public final QueryInfo info;
  /** Database context. */
  public final Context context;

  /** Query resources. */
  public QueryResources resources;
  /** HTTP context. */
  public Object http;
  /** Pending updates. */
  public Updates updates;

  /** Global database options (will be reassigned after query execution). */
  final HashMap<Option<?>, Object> staticOpts = new HashMap<>();
  /** Temporary query options (key/value pairs), supplied by option declarations. */
  final StringList tempOpts = new StringList();

  /** Current context value. */
  public Value value;
  /** Current context position. */
  public long pos = 1;
  /** Current context size. */
  public long size = 1;

  /** Full-text position data (needed for highlighting full-text results). */
  public FTPosData ftPosData = Prop.gui ? new FTPosData() : null;
  /** Available collations. */
  public TokenObjMap<Collation> collations;
  /** Current full-text token. */
  public FTLexer ftToken;
  /** Current full-text options. */
  private FTOpt ftOpt;
  /** Full-text token positions (needed for highlighting full-text results). */
  public int ftPos;
  /** Scoring flag. */
  public boolean scoring;

  /** Current Date. */
  public Item date;
  /** Current DateTime. */
  public Item datm;
  /** Current Time. */
  public Item time;
  /** Current timezone. */
  public Item zone;
  /** Current nanoseconds. */
  public long nano;

  /** Strings to lock defined by read-lock option. */
  public final StringList readLocks = new StringList(0);
  /** Strings to lock defined by write-lock option. */
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

  /** Parsed modules, containing the file path and module uri. */
  public final TokenMap modParsed = new TokenMap();
  /** Pre-declared modules, containing module uri and their file paths (required for test APIs). */
  final TokenMap modDeclared = new TokenMap();
  /** Stack of module files that are currently parsed. */
  final TokenList modStack = new TokenList();

  /** Initial context value. */
  MainModule ctxItem;

  /** Root expression of the query. */
  public MainModule root;

  /** Serialization parameters. */
  private SerializerOptions serParams;
  /** Indicates if the default serialization parameters are used. */
  private boolean defaultOutput;

  /** Indicates if the query has been compiled. */
  private boolean compiled;
  /** Indicates if the query context has been closed. */
  private boolean closed;

  /**
   * Constructor.
   * @param qcParent parent context
   */
  public QueryContext(final QueryContext qcParent) {
    this(qcParent.context, qcParent, qcParent.info);
    listen = qcParent.listen;
    resources = qcParent.resources;
    http = qcParent.http;
    updates = qcParent.updates;
  }

  /**
   * Constructor.
   * @param context database context
   */
  public QueryContext(final Context context) {
    this(context, null, null);
    resources = new QueryResources(this);
  }

  /**
   * Constructor.
   * @param context database context
   * @param parent parent context (can be {@code null})
   * @param info query info
   */
  private QueryContext(final Context context, final QueryContext parent, final QueryInfo info) {
    this.context = context;
    this.parent = parent;
    this.info = info != null ? info : new QueryInfo(this);
  }

  /**
   * Parses the specified query.
   * @param query query string
   * @param path file path (may be {@code null})
   * @param sc static context (may be {@code null})
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
   * @param sc static context (may be {@code null})
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
   * @param sc static context (may be {@code null})
   * @return main module
   * @throws QueryException query exception
   */
  public MainModule parseMain(final String query, final String path, final StaticContext sc)
      throws QueryException {

    info.query = query;
    final QueryParser qp = new QueryParser(query, path, this, sc);
    root = qp.parseMain();
    if(updating) {
      updating = (qp.sc.mixUpdates && qp.sc.dynFuncCall) || root.expr.has(Flag.UPD);
    }
    return root;
  }

  /**
   * Parses the specified module.
   * @param query query string
   * @param path file path (may be {@code null})
   * @param sc static context (may be {@code null})
   * @return name of module
   * @throws QueryException query exception
   */
  public LibraryModule parseLibrary(final String query, final String path, final StaticContext sc)
      throws QueryException {

    info.query = query;
    try {
      return new QueryParser(query, path, this, sc).parseLibrary(true);
    } finally {
      // library module itself is not updating
      updating = false;
    }
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
    if(compiled) return;

    try {
      // set database options
      final StringList opts = tempOpts;
      final int os = opts.size();
      for(int o = 0; o < os; o += 2) {
        try {
          context.options.assign(opts.get(o).toUpperCase(Locale.ENGLISH), opts.get(o + 1));
        } catch(final BaseXException ex) {
          throw BASX_VALUE_X_X.get(null, opts.get(o), opts.get(o + 1));
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
          // only {@link ParseExpr} instances may lead to a missing context
          throw ex.error() == NOCTX_X ? CIRCCTX.get(ctxItem.info) : ex;
        }
      } else {
        // cache the initial context nodes
        final DBNodes nodes = context.current();
        if(nodes != null) {
          if(!context.perm(Perm.READ, nodes.data().meta.name))
            throw BASX_PERM_X.get(null, Perm.READ);
          value = resources.compile(nodes);
        }
      }

      // if specified, convert context value to specified type
      // [LW] should not be necessary
      if(value != null && root.sc.contextType != null) {
        value = root.sc.contextType.promote(this, root.sc, null, value, true);
      }

      // dynamic compilation
      analyze();
      info.runtime = true;
    } finally {
      compiled = true;
    }
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
    compile();

    try {
      // no updates: iterate through results
      if(!updating) return root.iter(this);

      // cache results
      ItemList results = root.cache(this);

      if(updates != null) {
        // only perform updates if no parent context exists
        if(parent == null) {
          // create copies of results that will be modified by an update operation
          final ItemList cache = updates.cache;
          final HashSet<Data> datas = updates.prepare(this);
          final StringList dbs = updates.databases();
          check(results, datas, dbs);
          check(cache, datas, dbs);

          // invalidate current node set in context, apply updates
          if(context.data() != null) context.invalidate();
          updates.apply(this);

          // append cached outputs
          if(!cache.isEmpty()) {
            if(results.isEmpty()) results = cache;
            else results.add(cache.value());
          }
        }
      }
      return results.iter();

    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      throw BASX_STACKOVERFLOW.get(null);
    }
  }

  /**
   * Checks the specified results, and replaces nodes with their copies if they will be
   * affected by update operations.
   * @param results node cache
   * @param datas data references
   * @param dbs database names
   * @throws QueryException query exception
   */
  private void check(final ItemList results, final HashSet<Data> datas, final StringList dbs)
      throws QueryException {

    final long cs = results.size();
    for(int c = 0; c < cs; c++) {
      final Item it = results.get(c);
      // all updates are performed on database nodes
      if(it instanceof FItem) throw BASX_FITEM_X.get(null, it);
      if(it instanceof DBNode) {
        final Data data = it.data();
        if(datas.contains(data) || !data.inMemory() && dbs.contains(data.meta.name)) {
          results.set(c, ((DBNode) it).dbNodeCopy(context.options));
        }
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
   * Returns a reference to the updates.
   * @return updates
   */
  public Updates updates() {
    if(updates == null) updates = new Updates(false);
    return updates;
  }

  /**
   * Returns the current data reference of the context value or {@code null}.
   * @return data reference
   */
  public Data data() {
    return value != null ? value.data() : null;
  }

  @Override
  public void databases(final LockResult lr) {
    lr.read.add(readLocks);
    lr.write.add(writeLocks);
    // use global locking if referenced databases cannot be statically determined
    if(root == null || !root.databases(lr, this) ||
       ctxItem != null && !ctxItem.databases(lr, this)) {
      if(updating) lr.writeAll = true;
      else lr.readAll = true;
    }
    // replace collection lock with context lock
    if(lr.read.delete(Docs.COLL)) lr.read.add(DBLocking.CONTEXT);
    if(lr.write.delete(Docs.COLL)) lr.write.add(DBLocking.CONTEXT);
  }

  /**
   * Binds the HTTP context.
   * @param val HTTP context
   */
  public void http(final Object val) {
    http = val;
  }

  /**
   * Binds the context value, using the same rules as for
   * {@link #bind(String, Object, String, StaticContext) binding variables}.
   * @param val value to be bound
   * @param type type (may be {@code null})
   * @param sc static context
   * @throws QueryException query exception
   */
  public void context(final Object val, final String type, final StaticContext sc)
      throws QueryException {
    context(cast(val, type), sc);
  }

  /**
   * Binds the context value.
   * @param val value to be bound
   * @param sc static context
   */
  public void context(final Value val, final StaticContext sc) {
    ctxItem = MainModule.get(val, new VarScope(sc), null, null, sc, null);
  }

  /**
   * Binds a value to a global variable. The specified type is interpreted as follows:
   * <ul>
   *   <li> If {@code "json"} is specified, the value is converted according to the rules
   *        specified in {@link JsonMapConverter}.</li>
   *   <li> Otherwise, the type is cast to the specified XDM type.</li>
   * </ul>
   * If the value is an XQuery {@link Value}, it is directly assigned.
   * Otherwise, it is cast to the XQuery data model, using a Java/XQuery mapping.
   * @param name name of variable
   * @param val value to be bound
   * @param type type (may be {@code null})
   * @param sc static context
   * @throws QueryException query exception
   */
  public void bind(final String name, final Object val, final String type, final StaticContext sc)
      throws QueryException {
    bind(name, cast(val, type), sc);
  }

  /**
   * Binds a value to a global variable.
   * @param name name of variable
   * @param val value to be bound
   * @param sc static context
   * @throws QueryException query exception
   */
  public void bind(final String name, final Value val, final StaticContext sc)
      throws QueryException {
    final byte[] n = token(name);
    bindings.put(QNm.resolve(indexOf(n, '$') == 0 ? substring(n, 1) : n, sc), val);
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
    (parent != null ? parent.info : info).evalInfo(string);
  }

  /**
   * Returns info on query compilation and evaluation.
   * @return query info
   */
  public String info() {
    return info.toString(this);
  }

  /**
   * Returns query-specific or default serialization parameters.
   * @return serialization parameters
   */
  public SerializerOptions serParams() {
    if(serParams == null) {
      serParams = new SerializerOptions(context.options.get(MainOptions.SERIALIZER));
      defaultOutput = root != null;
    }
    return serParams;
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
   * Assigns full-text options.
   * @param opt full-text options
   */
  public void ftOpt(final FTOpt opt) {
    ftOpt = opt;
  }

  /**
   * Creates and returns an XML query plan (expression tree) for this query.
   * @return query plan
   */
  public FElem plan() {
    // only show root node if functions or variables exist
    final FElem e = new FElem(QueryText.PLAN);
    e.add(QueryText.COMPILED, token(compiled));
    if(root != null) {
      for(final StaticScope scp : QueryCompiler.usedDecls(root)) scp.plan(e);
      root.plan(e);
    } else {
      funcs.plan(e);
      vars.plan(e);
    }
    return e;
  }

  /**
   * Indicates that the query contains any updating expressions.
   */
  public void updating() {
    updating = true;
  }

  @Override
  public void close() {
    if(closed) return;
    closed = true;
    if(parent == null) {
      // topmost query: close resources
      resources.close();
    } else {
      // otherwise, adopt update reference (may have been initialized by sub query)
      parent.updates = updates;
    }
    // reassign original database options
    for(final Entry<Option<?>, Object> e : staticOpts.entrySet()) {
      context.options.put(e.getKey(), e.getValue());
    }
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
   * Caches and returns the result of the specified query. If all nodes are of the same database
   * instance, the returned value will be of type {@link DBNodes}.
   * @param max maximum number of results to cache (negative: return all values)
   * @return resulting value
   * @throws QueryException query exception
   */
  Value cache(final int max) throws QueryException {
    final int mx = max >= 0 ? max : Integer.MAX_VALUE;

    // evaluates the query
    final Iter ir = iter();
    final ItemList cache;
    Item it;

    // check if all results belong to the database of the input context
    final Data data = resources.globalData();
    if(defaultOutput && data != null) {
      final IntList pres = new IntList();
      while((it = ir.next()) != null && it.data() == data && pres.size() < mx) {
        checkStop();
        pres.add(((DBNode) it).pre());
      }

      // all results processed: return compact node sequence
      final int ps = pres.size();
      if(it == null || ps == mx) return new DBNodes(data, pres.finish()).ftpos(ftPosData);

      // otherwise, add nodes to standard iterator
      cache = new ItemList();
      for(int p = 0; p < ps; p++) cache.add(new DBNode(data, pres.get(p)));
      cache.add(it);
    } else {
      cache = new ItemList();
    }

    // use standard iterator
    while((it = ir.next()) != null && cache.size() < mx) {
      checkStop();
      it.materialize(null);
      cache.add(it);
    }
    return cache.value();
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Casts a value to the specified type.
   * See {@link #bind(String, Object, String, StaticContext)} for more infos.
   * @param val value to be cast
   * @param type type (may be {@code null})
   * @return cast value
   * @throws QueryException query exception
   */
  private Value cast(final Object val, final String type) throws QueryException {
    final StaticContext sc = root != null ? root.sc : new StaticContext(this);

    // String input
    Object vl = val;
    if(vl instanceof String) {
      final String string = (String) vl;
      final StringList strings = new StringList(1);
      // strings containing multiple items (value \1 ...)
      if(string.indexOf('\1') == -1) {
        strings.add(string);
      } else {
        strings.add(string.split("\1"));
        vl = strings.toArray();
      }

      // sub types overriding the global value (value \2 type)
      if(string.indexOf('\2') != -1) {
        final ValueBuilder vb = new ValueBuilder();
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
      return vl instanceof Value ? (Value) vl : JavaFunction.toValue(vl, this, sc);
    }

    // convert to json
    if(type.equalsIgnoreCase(MainParser.JSON.name())) {
      try {
        final JsonParserOptions jp = new JsonParserOptions();
        jp.set(JsonOptions.FORMAT, JsonFormat.MAP);
        return JsonConverter.get(jp).convert(token(vl.toString()), null);
      } catch(final QueryIOException ex) {
        throw ex.getCause();
      }
    }

    // test for empty sequence
    if(type.equals(QueryText.EMPTY_SEQUENCE + "()")) return Empty.SEQ;

    // convert to the specified type
    // [LW] type should be parsed properly
    final QNm nm = new QNm(token(type.replaceAll("\\(.*?\\)$", "")), sc);
    if(!nm.hasURI() && nm.hasPrefix()) throw NOURI_X.get(null, nm.string());

    Type tp;
    if(type.endsWith(")")) {
      if(nm.eq(AtomType.ITEM.name)) tp = AtomType.ITEM;
      else tp = NodeType.find(nm);
      if(tp == null) tp = FuncType.find(nm);
    } else {
      tp = ListType.find(nm);
      if(tp == null) tp = AtomType.find(nm, false);
    }
    if(tp == null) throw WHICHTYPE_X.get(null, type);

    // cast XDM values
    if(vl instanceof Value) {
      // cast single item
      if(vl instanceof Item) return tp.cast((Item) vl, this, sc, null);
      // cast sequence
      final Value v = (Value) vl;
      final ValueBuilder seq = new ValueBuilder();
      for(final Item i : v) seq.add(tp.cast(i, this, sc, null));
      return seq.value();
    }

    if(vl instanceof String[]) {
      // cast string array
      final String[] strings = (String[]) vl;
      final ValueBuilder seq = new ValueBuilder();
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
      final Date dt = Calendar.getInstance().getTime();
      final String ymd = DateTime.format(dt, DateTime.DATE);
      final String hms = DateTime.format(dt, DateTime.TIME);
      final String zon = DateTime.format(dt, DateTime.ZONE);
      final String znm = zon.substring(0, 3), zns = zon.substring(3);
      time = new Tim(token(hms + znm + ':' + zns), null);
      date = new Dat(token(ymd + znm + ':' + zns), null);
      datm = new Dtm(token(ymd + 'T' + hms + znm + ':' + zns), null);
      zone = new DTDur(Strings.toInt(znm), Strings.toInt(zns));
      nano = System.nanoTime();
    }
    return this;
  }
}
