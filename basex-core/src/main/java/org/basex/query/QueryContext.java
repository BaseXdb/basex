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
import org.basex.core.jobs.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.func.*;
import org.basex.query.func.java.*;
import org.basex.query.iter.*;
import org.basex.query.scope.*;
import org.basex.query.up.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.ft.*;
import org.basex.query.util.hash.*;
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

/**
 * This class organizes both static and dynamic properties that are specific to a
 * single query.
 *
 * @author BaseX Team 2005-21, BSD License
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
  private final QNmMap<Value> bindings = new QNmMap<>();

  /** Parent query context. */
  public final QueryContext parent;
  /** Query info. */
  public final QueryInfo info;
  /** Database context. */
  public final Context context;

  /** Query resources. */
  public QueryResources resources;
  /** Update container; will be created if the first update is evaluated. */
  public Updates updates;

  /** Global database options (will be reassigned after query execution). */
  final QueryOptions options = new QueryOptions(this);

  /** Query threads. */
  public final QueryThreads threads = new QueryThreads();
  /** Current context value. */
  public QueryFocus focus = new QueryFocus();
  /** Current date/time values. */
  public QueryDateTime dateTime;

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

  /** Available collations. */
  public TokenObjMap<Collation> collations;

  /** User-defined locks. */
  public final LockList locks = new LockList();

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
   * @param info query info (can be {@code null})
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
   * @return main module
   * @throws QueryException query exception
   */
  public AModule parse(final String query, final String uri) throws QueryException {
    return parse(query, QueryProcessor.isLibrary(query), uri);
  }

  /**
   * Parses the specified query.
   * @param query query string
   * @param library library/main module
   * @param uri base URI (may be {@code null})
   * @return main module
   * @throws QueryException query exception
   */
  public AModule parse(final String query, final boolean library, final String uri)
      throws QueryException {
    return library ? parseLibrary(query, uri) : parseMain(query, uri);
  }

  /**
   * Parses the specified query.
   * @param query query string
   * @param uri base URI (may be {@code null})
   * @return main module
   * @throws QueryException query exception
   */
  public MainModule parseMain(final String query, final String uri) throws QueryException {
    return parseMain(query, uri, null);
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
    if(updating && !qp.sc.mixUpdates) updating = root.expr.has(Flag.UPD);
    return root;
  }

  /**
   * Parses the specified module.
   * @param query query string
   * @param uri base URI (may be {@code null})
   * @return name of module
   * @throws QueryException query exception
   */
  public LibraryModule parseLibrary(final String query, final String uri) throws QueryException {
    info.query = query;
    try {
      return new QueryParser(query, uri, this, null).parseLibrary(true);
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
    info.runtime = false;

    final CompileContext cc = new CompileContext(this);
    try {
      // bind external variables of global option (if not assigned yet by other APIs)
      final MainOptions mopts = context.options;
      if(root != null && parent == null) {
        for(final Entry<String, String> entry : mopts.toMap(MainOptions.BINDINGS).entrySet()) {
          final String key = entry.getKey();
          final Atm value = new Atm(entry.getValue());
          if(key.isEmpty()) {
            if(ctxItem == null) context(value, root.sc);
          } else {
            final QNm name = qname(key, root.sc);
            if(!bindings.contains(name)) bind(name, value);
          }
        }
      }

      // set database options
      options.compile();
      // set tail call option after assigning database options
      maxCalls = mopts.get(MainOptions.TAILCALLS);

      // bind external variables
      vars.bindExternal(this, bindings);

      if(ctxItem != null) {
        // evaluate initial expression
        try {
          ctxItem.comp(cc);
          focus.value = ctxItem.value(this);
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
      if(focus.value != null && root.sc != null && root.sc.contextType != null) {
        focus.value = root.sc.contextType.promote(focus.value, null, this, root.sc, null, true);
      }

      try {
        // compile the expression
        if(root != null) QueryCompiler.compile(cc, root);
        // compile static functions
        else funcs.compile(cc);
      } catch(final StackOverflowError ex) {
        Util.debug(ex);
        throw BASEX_OVERFLOW.get(null, ex);
      }
    } finally {
      info.runtime = true;
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
    return updating ? update().iter() : root.iter(this);
  }

  /**
   * Returns the result.
   * @return result iterator
   * @throws QueryException query exception
   */
  public Value value() throws QueryException {
    compile();
    return updating ? update() : root.value(this);
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
    // choose read or write locks
    final Locks l = jc().locks;
    final LockList list = updating ? l.writes : l.reads;

    if(root == null || !root.databases(l, this) || ctxItem != null && !ctxItem.databases(l, this)) {
      // use global locking if referenced databases cannot statically be determined
      list.addGlobal();
    } else {
      // add custom locks
      list.add(locks);
    }
  }

  /**
   * Binds the context value, using the same rules as for
   * {@link #bind(String, Object, String, StaticContext) binding variables}.
   * @param value value to be bound
   * @param type type (may be {@code null})
   * @param sc static context
   * @throws QueryException query exception
   */
  public void context(final Object value, final String type, final StaticContext sc)
      throws QueryException {
    context(cast(value, type), sc);
  }

  /**
   * Binds the context value.
   * @param value value to be bound
   * @param sc static context
   */
  public void context(final Value value, final StaticContext sc) {
    ctxItem = MainModule.get(new VarScope(sc), value, null, null, null);
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
   * @param value value to be bound
   * @param type type (may be {@code null})
   * @param sc static context
   * @throws QueryException query exception
   */
  public void bind(final String name, final Object value, final String type, final StaticContext sc)
      throws QueryException {
    bind(name, cast(value, type), sc);
  }

  /**
   * Binds a value to a global variable.
   * @param name name of variable
   * @param value value to be bound
   * @param sc static context
   * @throws QueryException query exception
   */
  public void bind(final String name, final Value value, final StaticContext sc)
      throws QueryException {
    bind(qname(name, sc), value);
  }

  /**
   * Binds a value to a global variable.
   * @param name name of variable
   * @param value value to be bound
   */
  public void bind(final QNm name, final Value value) {
    bindings.put(name, value);
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
   * @param full include comprehensive information
   * @return query plan
   */
  public FElem plan(final boolean full) {
    // only show root node if functions or variables exist
    final QueryPlan plan = new QueryPlan(compiled, closed, full);
    if(root != null) {
      for(final StaticScope ss : QueryCompiler.usedDecls(root)) ss.plan(plan);
      root.plan(plan);
    } else {
      funcs.plan(plan);
      vars.plan(plan);
    }
    return plan.root();
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
      threads.close();
    } else {
      // otherwise, adopt update reference (may have been initialized by sub query)
      parent.updates = updates;
      parent.popJob();
    }
    options.close();
  }

  @Override
  public String shortInfo() {
    return SAVE;
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
   * @param value expression to be bound
   * @throws QueryException exception
   */
  public void set(final Var var, final Value value) throws QueryException {
    stack.set(var, value, this);
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
  public QueryDateTime dateTime() throws QueryException {
    if(dateTime == null) dateTime = new QueryDateTime();
    return dateTime;
  }

  @Override
  public String toString() {
    return root != null ? QueryInfo.usedDecls(root) : info.query;
  }

  // CLASS METHODS ================================================================================

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

      // all results processed: return compact node sequence, attach full-text positions
      final int ps = pres.size();
      if(item == null || ps == mx) {
        return ps == 0 ? Empty.VALUE : new DBNodes(data, pres.finish()).ftpos(ftPosData);
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
      item.cache(false, null);
      items.add(item);
    }
    return items.value();
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Returns the result of an updating expression.
   * @return result iterator
   * @throws QueryException query exception
   */
  private Value update() throws QueryException {
    try {
      // retrieve result
      final Value value = root.value(this);
      // only perform updates if no parent context exists
      if(updates == null || parent != null) return value;

      // create copies of results that will be modified by an update operation
      final HashSet<Data> datas = updates.prepare(this);
      final StringList dbs = updates.databases();
      final QueryFunction<Item, Item> materialize = item -> {
        final Data data = item.data();
        final boolean copy = data != null &&
            (datas.contains(data) || !data.inMemory() && dbs.contains(data.meta.name));
        final Item it = item.materialize(this, copy);
        if(it == null) throw BASEX_FUNCTION_X.get(null, item);
        return it;
      };

      final ValueBuilder vb = new ValueBuilder(this);
      for(final Item item : value) {
        checkStop();
        vb.add(materialize.apply(item));
      }
      for(final Item item : updates.output(true)) {
        checkStop();
        vb.add(materialize.apply(item));
      }

      // invalidate current node set in context, apply updates
      if(context.data() != null) context.invalidate();
      updates.apply(this);

      return vb.value((Type) null);

    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      throw BASEX_OVERFLOW.get(null);
    }
  }

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
      return object instanceof Value ? (Value) object : JavaCall.toValue(object, this, sc);
    }

    // convert to json
    if(type.equalsIgnoreCase(MainParser.JSON.name())) {
      try {
        final JsonParserOptions jp = new JsonParserOptions();
        jp.set(JsonOptions.FORMAT, JsonFormat.XQUERY);
        return JsonConverter.get(jp).convert(object.toString(), "");
      } catch(final QueryIOException ex) {
        throw ex.getCause();
      }
    }

    // test for empty sequence
    if(type.equals(QueryText.EMPTY_SEQUENCE + "()")) return Empty.VALUE;

    // convert to the specified type
    // [LW] type should be parsed properly
    final QNm nm = new QNm(token(type.replaceAll("\\(.*?\\)$", "")), sc);
    if(!nm.hasURI() && nm.hasPrefix()) throw NOURI_X.get(null, nm.string());

    Type tp;
    if(Strings.endsWith(type, ')')) {
      tp = nm.eq(AtomType.ITEM.qname()) ? AtomType.ITEM : NodeType.find(nm);
      if(tp == null) tp = FuncType.find(nm);
    } else {
      tp = ListType.find(nm);
      if(tp == null) tp = AtomType.find(nm, false);
    }
    if(tp == null) throw WHICHTYPE_X.get(null, type);

    // cast XDM values
    if(object instanceof Value) {
      final Value vl = (Value) object;
      // cast single item
      if(vl.isItem()) return tp.cast((Item) vl, this, sc, null);
      // cast sequence
      final ValueBuilder vb = new ValueBuilder(this);
      for(final Item item : vl) vb.add(tp.cast(item, this, sc, null));
      return vb.value(tp);
    }

    if(object instanceof String[]) {
      // cast string array
      final ValueBuilder vb = new ValueBuilder(this);
      for(final String string : (String[]) object) vb.add(tp.cast(string, this, sc, null));
      return vb.value(tp);
    }

    // cast any other object to XDM
    return tp.cast(object, this, sc, null);
  }

  /**
   * Converts the specified variable name to a QName.
   * @param name name of variable
   * @param sc static context
   * @return QName
   * @throws QueryException query context
   */
  private static QNm qname(final String name, final StaticContext sc) throws QueryException {
    return QNm.resolve(token(Strings.startsWith(name, '$') ? name.substring(1) : name), sc);
  }
}
