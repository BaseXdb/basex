package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.core.jobs.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.scope.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
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
import org.basex.util.ft.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * This class organizes both static and dynamic properties that are specific to a
 * single query.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class QueryContext extends Job implements Closeable {
  /** The evaluation stack. */
  public final QueryStack stack = new QueryStack();
  /** Static variables. */
  public final Variables vars = new Variables();
  /** Functions. */
  public final StaticFuncs funcs = new StaticFuncs();
  /** Externally bound variables. */
  private final HashMap<QNm, Value> bindings = new HashMap<>();

  /** Parent query context. */
  public final QueryContext parent;
  /** Query info. */
  public final QueryInfo info;
  /** Database context. */
  public final Context context;

  /** Query resources. */
  public QueryResources resources;
  /** HTTP connection. */
  public Object http;
  /** Update container. */
  public Updates updates;

  /** Global database options (will be reassigned after query execution). */
  final HashMap<Option<?>, Object> staticOpts = new HashMap<>();
  /** Temporary query options (key/value pairs), supplied by option declarations. */
  final StringList tempOpts = new StringList();

  /** Current context value. */
  public QueryFocus focus = new QueryFocus();

  /** Full-text position data (needed for highlighting full-text results). */
  public FTPosData ftPosData = Prop.gui ? new FTPosData() : null;
  /** Current full-text lexer. */
  public FTLexer ftLexer;
  /** Current full-text options. */
  private FTOpt ftOpt;
  /** Full-text token positions (needed for highlighting full-text results). */
  public int ftPos;
  /** Scoring flag. */
  public boolean scoring;

  /** Current Date. */
  public Dat date;
  /** Current DateTime. */
  public Dtm datm;
  /** Current Time. */
  public Tim time;
  /** Current timezone. */
  public DTDur zone;
  /** Current nanoseconds. */
  public long nano;

  /** Available collations. */
  public TokenObjMap<Collation> collations;

  /** Strings to lock defined by read-lock option. */
  public final LockList readLocks = new LockList();
  /** Strings to lock defined by write-lock option. */
  public final LockList writeLocks = new LockList();

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
  public MainModule ctxItem;
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
   * @param parent parent context
   */
  public QueryContext(final QueryContext parent) {
    this(parent.context, parent, parent.info);
    parent.pushJob(this);
    resources = parent.resources;
    http = parent.http;
    updates = parent.updates;
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
   * @param uri base URI (may be {@code null})
   * @param sc static context (may be {@code null})
   * @return main module
   * @throws QueryException query exception
   */
  public Module parse(final String query, final String uri, final StaticContext sc)
      throws QueryException {
    return parse(query, QueryProcessor.isLibrary(query), uri, sc);
  }

  /**
   * Parses the specified query.
   * @param query query string
   * @param library library/main module
   * @param uri base URI (may be {@code null})
   * @param sc static context (may be {@code null})
   * @return main module
   * @throws QueryException query exception
   */
  public Module parse(final String query, final boolean library, final String uri,
      final StaticContext sc) throws QueryException {
    return library ? parseLibrary(query, uri, sc) : parseMain(query, uri, sc);
  }

  /**
   * Parses the specified query.
   * @param query query string
   * @param uri base URI (may be {@code null})
   * @param sc static context (may be {@code null})
   * @return main module
   * @throws QueryException query exception
   */
  public MainModule parseMain(final String query, final String uri, final StaticContext sc)
      throws QueryException {

    info.query = query;
    final QueryParser qp = new QueryParser(query, uri, this, sc);
    root = qp.parseMain();
    // updating expression: check if an updating expression is left in the expression tree
    if(updating) updating = (qp.sc.mixUpdates && qp.sc.dynFuncCall) || root.expr.has(Flag.UPD);
    return root;
  }

  /**
   * Parses the specified module.
   * @param query query string
   * @param uri base URI (may be {@code null})
   * @param sc static context (may be {@code null})
   * @return name of module
   * @throws QueryException query exception
   */
  public LibraryModule parseLibrary(final String query, final String uri, final StaticContext sc)
      throws QueryException {

    info.query = query;
    try {
      return new QueryParser(query, uri, this, sc).parseLibrary(true);
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
    checkStop();
    if(compiled) return;

    final CompileContext cc = new CompileContext(this);
    try {
      // set database options
      final StringList opts = tempOpts;
      final int os = opts.size();
      for(int o = 0; o < os; o += 2) {
        final String key = opts.get(o), val = opts.get(o + 1);
        try {
          context.options.assign(key.toUpperCase(Locale.ENGLISH), val);
        } catch(final BaseXException ex) {
          Util.debug(ex);
          throw BASEX_OPTIONS_X_X.get(null, key, val);
        }
      }
      // set tail call option after assignment database option
      maxCalls = context.options.get(MainOptions.TAILCALLS);

      // bind external variables
      vars.bindExternal(this, bindings);

      if(ctxItem != null) {
        // evaluate initial expression
        try {
          ctxItem.comp(cc);
          focus.value = ctxItem.cache(this).value();
        } catch(final QueryException ex) {
          // only {@link ParseExpr} instances may lead to a missing context
          throw ex.error() == NOCTX_X ? CIRCCTX.get(ctxItem.info) : ex;
        }
      } else {
        // cache the initial context nodes
        final DBNodes nodes = context.current();
        if(nodes != null) {
          final String name = nodes.data().meta.name;
          if(!context.perm(Perm.READ, name))
            throw BASEX_PERMISSION_X_X.get(null, Perm.READ, name);
          focus.value = resources.compile(nodes);
        }
      }

      // if specified, convert context value to specified type
      // [LW] should not be necessary
      if(focus.value != null && root.sc.contextType != null) {
        focus.value = root.sc.contextType.promote(focus.value, null, this, root.sc, null, true);
      }

      try {
        // compile the expression
        if(root != null) QueryCompiler.compile(cc, root);
        // compile global functions.
        else funcs.compile(cc);
      } catch(final StackOverflowError ex) {
        Util.debug(ex);
        throw BASEX_OVERFLOW.get(null, ex);
      }

      info.runtime = true;
    } finally {
      compiled = true;
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
      ItemList items = root.cache(this);

      // only perform updates if no parent context exists
      if(updates != null && parent == null) {
        // create copies of results that will be modified by an update operation
        final ItemList items2 = updates.items;
        final HashSet<Data> datas = updates.prepare(this);
        final StringList dbs = updates.databases();
        check(items, datas, dbs);
        check(items2, datas, dbs);

        // invalidate current node set in context, apply updates
        if(context.data() != null) context.invalidate();
        updates.apply(this);

        // append cached outputs
        if(!items2.isEmpty()) {
          if(items.isEmpty()) {
            items = items2;
          } else {
            for(final Item item : items2) items.add(item);
          }
        }
      }
      return items.iter();

    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      throw BASEX_OVERFLOW.get(null);
    }
  }

  /**
   * Checks the specified results, and replaces nodes with their copies if they will be
   * affected by update operations.
   * @param items node cache
   * @param datas data references
   * @param dbs database names
   * @throws QueryException query exception
   */
  private void check(final ItemList items, final HashSet<Data> datas, final StringList dbs)
      throws QueryException {

    final long is = items.size();
    for(int i = 0; i < is; i++) {
      final Item item = items.get(i);
      // all updates are performed on database nodes
      if(item instanceof FItem) throw BASEX_FUNCTION_X.get(null, item);
      final Data data = item.data();
      if(data != null && (datas.contains(data) ||
          !data.inMemory() && dbs.contains(data.meta.name))) {
        items.set(i, ((DBNode) item).dbNodeCopy(context.options, this));
      }
    }
  }

  /**
   * Checks if evaluation has been stopped and returns the next item of an iterator.
   * @param iter iterator
   * @return item or {@code null}
   * @throws QueryException query exception
   */
  public Item next(final Iter iter) throws QueryException {
    checkStop();
    return iter.next();
  }

  /**
   * Returns a reference to the updates container.
   * @return updates container
   */
  public synchronized Updates updates() {
    if(updates == null) updates = new Updates(false);
    return updates;
  }

  @Override
  public void addLocks() {
    final Locks locks = jc().locks;
    final LockList read = locks.reads, write = locks.writes;
    read.add(readLocks);
    write.add(writeLocks);
    // use global locking if referenced databases cannot be statically determined
    if(root == null || !root.databases(locks, this) ||
       ctxItem != null && !ctxItem.databases(locks, this)) {
      (updating ? write : read).addGlobal();
    }
  }

  /**
   * Binds the HTTP connection.
   * @param val HTTP connection
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
    ctxItem = MainModule.get(new VarScope(sc), val, null, null, null);
  }

  /**
   * Binds a value to a global variable. The specified type is interpreted as follows:
   * <ul>
   *   <li> If {@code "json"} is specified, the value is converted according to the rules
   *        specified in {@link JsonXQueryConverter}.</li>
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
   * Adds some evaluation info.
   * @param string evaluation info
   */
  public void evalInfo(final String string) {
    QueryContext qc = this;
    while(qc.parent != null) qc = qc.parent;
    qc.info.evalInfo(string);
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
   * Returns the current full-text options. Creates a new instance if called for the first time.
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
    final FElem elem = new FElem(QueryText.QUERY_PLAN);
    elem.add(QueryText.COMPILED, token(compiled));
    elem.add(QueryText.UPDATING, token(updating));
    if(root != null) {
      for(final StaticScope ss : QueryCompiler.usedDecls(root)) ss.plan(elem);
      root.plan(elem);
    } else {
      funcs.plan(elem);
      vars.plan(elem);
    }
    return elem;
  }

  /**
   * Indicates that the query contains updating expressions.
   */
  public void updating() {
    updating = true;
  }

  @Override
  public void close() {
    if(closed) return;
    closed = true;
    if(parent == null) {
      // topmost query: close resources (opened by compile step)
      resources.close();
    } else {
      // otherwise, adopt update reference (may have been initialized by sub query)
      parent.updates = updates;
      parent.popJob();
    }
    // reassign original database options (changed by compile step)
    staticOpts.forEach((key, value) -> context.options.put(key, value));
  }

  @Override
  public String shortInfo() {
    return SAVE;
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
    final Iter iter = iter();
    final ItemList items;
    Item item;

    // check if all results belong to the database of the input context
    final Data data = resources.globalData();
    if(defaultOutput && data != null) {
      final IntList pres = new IntList();
      while((item = next(iter)) != null && item.data() == data && pres.size() < mx) {
        pres.add(((DBNode) item).pre());
      }

      // all results processed: return compact node sequence
      final int ps = pres.size();
      if(item == null || ps == mx) {
        return ps == 0 ? Empty.SEQ : new DBNodes(data, pres.finish()).ftpos(ftPosData);
      }

      // otherwise, add nodes to standard iterator
      items = new ItemList();
      for(int p = 0; p < ps; p++) items.add(new DBNode(data, pres.get(p)));
      items.add(item);
    } else {
      items = new ItemList();
    }

    // use standard iterator
    while((item = next(iter)) != null && items.size() < mx) {
      item.materialize(null);
      items.add(item);
    }
    return items.value();
  }

  // PRIVATE METHODS ====================================================================

  /**
   * Casts a value to the specified type.
   * See {@link #bind(String, Object, String, StaticContext)} for more infos.
   * @param value value to be cast
   * @param type type (may be {@code null})
   * @return cast value
   * @throws QueryException query exception
   */
  private Value cast(final Object value, final String type) throws QueryException {
    final StaticContext sc = root != null ? root.sc : new StaticContext(this);

    // String input
    Object object = value;
    if(object instanceof String) {
      final String string = (String) object;
      final StringList strings = new StringList(1);
      // strings containing multiple items (value \1 ...)
      if(string.indexOf('\1') == -1) {
        strings.add(string);
      } else {
        strings.add(string.split("\1"));
        object = strings.toArray();
      }

      // sub types overriding the global value (value \2 type)
      if(string.indexOf('\2') != -1) {
        final ValueBuilder vb = new ValueBuilder(this);
        for(final String str : strings) {
          final int i = str.indexOf('\2');
          final String val = i == -1 ? str : str.substring(0, i);
          final String tp = i == -1 ? type : str.substring(i + 1);
          vb.add(cast(val, tp));
        }
        return vb.value();
      }
    }

    // no type specified: return original value or convert Java object
    if(type == null || type.isEmpty()) {
      return object instanceof Value ? (Value) object : JavaFunction.toValue(object, this, sc);
    }

    // convert to json
    if(type.equalsIgnoreCase(MainParser.JSON.name())) {
      try {
        final JsonParserOptions jp = new JsonParserOptions();
        jp.set(JsonOptions.FORMAT, JsonFormat.XQUERY);
        return JsonConverter.get(jp).convert(token(object.toString()), null);
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
      tp = nm.eq(AtomType.ITEM.name) ? AtomType.ITEM : NodeType.find(nm);
      if(tp == null) tp = FuncType.find(nm);
    } else {
      tp = ListType.find(nm);
      if(tp == null) tp = AtomType.find(nm, false);
    }
    if(tp == null) throw WHICHTYPE_X.get(null, type);

    // cast XDM values
    if(object instanceof Value) {
      // cast single item
      if(object instanceof Item) return tp.cast((Item) object, this, sc, null);
      // cast sequence
      final Value vl = (Value) object;
      final ValueBuilder vb = new ValueBuilder(this);
      final BasicIter<?> iter = vl.iter();
      for(Item item; (item = iter.next()) != null;) vb.add(tp.cast(item, this, sc, null));
      return vb.value();
    }

    if(object instanceof String[]) {
      // cast string array
      final ValueBuilder vb = new ValueBuilder(this);
      for(final String string : (String[]) object) vb.add(tp.cast(string, this, sc, null));
      return vb.value();
    }

    // cast any other object to XDM
    return tp.cast(object, this, sc, null);
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
   * @param var variable
   * @param val expression to be bound
   * @throws QueryException exception
   */
  public void set(final Var var, final Value val) throws QueryException {
    stack.set(var, val, this);
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
      final Date d = new Date();
      final String ymd = DateTime.format(d, DateTime.DATE);
      final String hms = DateTime.format(d, DateTime.TIME);
      final String zon = DateTime.format(d, DateTime.ZONE);
      final String znm = zon.substring(0, 3), zns = zon.substring(3);
      time = new Tim(token(hms + znm + ':' + zns), null);
      date = new Dat(token(ymd + znm + ':' + zns), null);
      datm = new Dtm(token(ymd + 'T' + hms + znm + ':' + zns), null);
      zone = new DTDur(Strings.toInt(znm), Strings.toInt(zns));
      nano = System.nanoTime();
    }
    return this;
  }

  @Override
  public String toString() {
    return root != null ? QueryInfo.usedDecls(root) : info.query;
  }
}
