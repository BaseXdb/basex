package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonOptions.*;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.core.cmd.*;
import org.basex.core.jobs.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
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
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class QueryContext extends Job implements Closeable {
  /** The evaluation stack. */
  public final QueryStack stack = new QueryStack();
  /** Static variables. */
  public final Variables vars = new Variables();
  /** Functions. */
  public final StaticFuncs functions = new StaticFuncs();
  /** Query resources. */
  public final QueryResources resources;
  /** Parent query context. */
  public final QueryContext parent;
  /** Database context. */
  public final Context context;
  /** Query info. */
  public final QueryInfo info;

  /** Global database options (will be reassigned after query execution). */
  final QueryOptions options = new QueryOptions(this);

  /** Query threads. */
  public final QueryThreads threads = new QueryThreads();
  /** Shared data references. */
  public final SharedData shared;
  /** User-defined locks. */
  public final LockList locks = new LockList();
  /** Current query focus. */
  public QueryFocus focus = new QueryFocus();
  /** Date/time values. */
  private QueryDateTime dateTime;

  /** Update container; will be created if the first update is evaluated. */
  public Updates updates;
  /** User. */
  public User user;

  /** Cached full-text position data. */
  public FTPosData ftPosData;
  /** Current full-text lexer. */
  public FTLexer ftLexer;
  /** Current full-text options. */
  private FTOpt ftOpt;
  /** Full-text token positions (needed for highlighting full-text results). */
  public int ftPos;
  /** Scoring flag. */
  public boolean scoring;

  /** Available collations. */
  public TokenObjectMap<Collation> collations;
  /** Profiling results. */
  public final QueryProfiler profiler = new QueryProfiler(this);
  /** Perform tail-call optimizations. */
  public boolean tco;

  /** Function for the next tail call. */
  public XQFunction tcFunc;
  /** Arguments for the next tail call. */
  public Value[] tcArgs;
  /** Counter for variable IDs. */
  public int varIDs;

  /** Parsed modules, containing the file path and module URI. */
  public final TokenObjectMap<byte[]> modParsed = new TokenObjectMap<>();
  /** Parsed modules, containing the file path and library module. */
  public final TokenObjectMap<LibraryModule> libs = new TokenObjectMap<>();
  /** Pre-declared modules, containing module URI and their file paths (required for test APIs). */
  public final TokenObjectMap<byte[]> modDeclared = new TokenObjectMap<>();
  /** Stack of module files that are currently parsed. */
  public final TokenList modStack = new TokenList();

  /** Main module (root expression). */
  public MainModule main;
  /** Context value type. */
  public SeqType contextType;
  /** Context scope. */
  public ContextScope contextValue;
  /** Indicates if context scope exists and is final. */
  public boolean finalContext;

  /** External variables and context to be bound at compile time. */
  private final QNmMap<Value> bindings = new QNmMap<>();

  /** Serialization options. */
  private SerializerOptions sopts;
  /** Indicates if the default serialization parameters are used. */
  private boolean defaultOutput;

  /** Indicates if the query has been compiled. */
  private boolean compiled;
  /** Indicates if the query has been optimized. */
  private boolean optimized;
  /** Indicates if the query context has been closed. */
  private boolean closed;
  /** Trace counter. */
  private int traces;

  /**
   * Constructor.
   * @param parent parent context
   */
  public QueryContext(final QueryContext parent) {
    this(parent.context, parent, parent.info);
    parent.pushJob(this);
    updates = parent.updates;
  }

  /**
   * Constructor.
   * @param context database context
   */
  public QueryContext(final Context context) {
    this(context, null, null);
  }

  /**
   * Constructor.
   * @param context database context
   * @param parent parent context (can be {@code null})
   * @param info query info (can be {@code null})
   */
  public QueryContext(final Context context, final QueryContext parent, final QueryInfo info) {
    this.context = context;
    this.parent = parent;
    this.info = info != null ? info : new QueryInfo(context);
    resources = parent != null ? parent.resources : new QueryResources(this);
    ftPosData = parent != null ? parent.ftPosData : null;
    shared = parent != null ? parent.shared : new SharedData();
    user = context.user();
  }

  /**
   * Parses the specified query.
   * @param query query string
   * @param uri base URI (can be {@code null})
   * @return module
   * @throws QueryException query exception
   */
  public AModule parse(final String query, final String uri) throws QueryException {
    return QueryParser.isLibrary(query) ? parseLibrary(query, uri) : parseMain(query, uri);
  }

  /**
   * Collects suggestions for completing a path expression.
   * @param query query string
   * @param data data reference
   * @return list of suggestions, followed by valid input string
   */
  public StringList suggest(final String query, final Data data) {
    final QuerySuggest qs = new QuerySuggest(query, this, data);
    try {
      qs.parseMain();
      return qs.complete(qs.mark);
    } catch(final QueryException ex) {
      return qs.complete(ex.column() - 1);
    }
  }

  /**
   * Parses the specified query.
   * @param query query string
   * @param uri base URI (can be {@code null})
   * @return main module
   * @throws QueryException query exception
   */
  public MainModule parseMain(final String query, final String uri) throws QueryException {
    return parseMain(query, uri, null);
  }

  /**
   * Parses the specified query and assigns the root expression.
   * @param query query string
   * @param uri base URI (can be {@code null})
   * @param sc static context (can be {@code null})
   * @return main module
   * @throws QueryException query exception
   */
  public MainModule parseMain(final String query, final String uri, final StaticContext sc)
      throws QueryException {

    return run(info.parsing, () -> {
      info.query = query;
      main = new QueryParser(query, uri, this, sc).parseMain();
      return main;
    });
  }

  /**
   * Parses the specified module.
   * @param query query string
   * @param uri base URI (can be {@code null})
   * @return library module
   * @throws QueryException query exception
   */
  public LibraryModule parseLibrary(final String query, final String uri) throws QueryException {
    return run(info.parsing, () -> {
      info.query = query;
      return new QueryParser(query, uri, this, null).parseLibrary(true);
    });
  }

  /**
   * Creates a function call for the specified function and assigns it as root expression.
   * @param func user-defined function
   * @param args arguments
   * @throws QueryException query exception
   */
  public void assign(final StaticFunc func, final Expr... args) throws QueryException {
    for(final StaticFunc sf : functions.funcs()) {
      if(func.info.equals(sf.info)) {
        // disable inlining of called function to ensure explicit locks are considered
        if(!sf.anns.contains(Annotation._BASEX_INLINE)) {
          sf.anns = sf.anns.attach(new Ann(sf.info, Annotation._BASEX_INLINE, Itr.ZERO));
        }
        // create and assign function call
        final StaticFuncCall call = new StaticFuncCall(sf.name, args, null, sf.info, true);
        call.setFunc(sf);
        main = new MainModule(call, new VarScope(), sf.sc);
        updating = sf.updating();
        return;
      }
    }
    throw BASEX_WHICH_X.get(null, func);
  }

  /**
   * Compiles the expression. Performs static optimizations.
   * @throws QueryException query exception
   */
  public void compile() throws QueryException {
    if(compiled) return;
    compiled = true;

    run(info.compiling, () -> {
      // assign tail call option after compiling options
      options.compile();
      tco = context.options.get(MainOptions.TAILCALLS) >= 0;

      // bind external variables
      if(parent == null) {
        final Map<String, String> map = context.options.toMap(MainOptions.BINDINGS);
        for(final Entry<String, String> entry : map.entrySet()) {
          bind(entry.getKey(), Atm.get(entry.getValue()), null, main.sc);
        }
      }
      vars.bindExternal(this, bindings, true);

      return compile(false);
    });
  }

  /**
   * Optimizes the expression. Performs dynamic optimizations.
   * @throws QueryException query exception
   */
  public void optimize() throws QueryException {
    compile();
    if(optimized) return;
    optimized = true;

    run(info.optimizing, () -> {
      // bind context
      final StaticContext sc = main.sc;
      if(parent == null && !bindings.contains(QNm.EMPTY)) {
        final DBNodes nodes = context.current();
        if(nodes != null) bind(null, resources.compile(nodes), null, sc);
      }
      final Value value = bindings.get(QNm.EMPTY);
      if(value != null) {
        contextValue = new ContextScope(value, sc.contextType, new VarScope(), sc, null, null);
      }
      if(contextValue != null) finalContext = true;

      return compile(true);
    });
  }

  /**
   * Compiles the expression.
   * @param dynamic dynamic compilation
   * @return {@code null}
   * @throws QueryException query exception
   */
  public Void compile(final boolean dynamic) throws QueryException {
    checkStop();

    info.runtime = false;
    try {
      final CompileContext cc = new CompileContext(this, dynamic);
      if(contextValue != null && dynamic) {
        try {
          contextValue.compile(cc);
          focus.value = contextValue.value(this);
        } catch(final QueryException ex) {
          Util.debug(ex);
          throw ex.error() == NOCTX_X ? CIRCCTX.get(ex.info()) : ex;
        }
      }
      if(main != null) {
        QueryCompiler.compile(cc);
      } else {
        // required for XQueryParse
        functions.compileAll(cc);
      }
    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      throw BASEX_OVERFLOW.get(null, ex);
    } finally {
      info.runtime = true;
    }
    return null;
  }

  /**
   * Returns a result iterator.
   * @return result iterator
   * @throws QueryException query exception
   */
  public Iter iter() throws QueryException {
    optimize();
    return run(info.evaluating, () -> updating ? update().iter() : main.iter(this));
  }

  /**
   * Returns the result.
   * @return result iterator
   * @throws QueryException query exception
   */
  public Value value() throws QueryException {
    optimize();
    return run(info.evaluating, () -> updating ? update() : main.value(this));
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
   * Returns the updates container.
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

    // locks in main module (can be null if parsing failed)
    boolean local = main == null || main.databases(new LockVisitor(list, contextValue == null));
    // locks in context expression
    if(local && contextValue != null) {
      // check if scope may still be overwritten by dynamic context
      if(!finalContext) list.add(Locking.CONTEXT);
      local = contextValue.databases(new LockVisitor(list, true));
    }
    if(local) {
      list.add(locks);
    } else {
      // global locking, referenced databases cannot be determined statically
      list.addGlobal();
    }
  }

  /**
   * Binds a value to a global variable or the context value.
   * The arguments will be ignored if a value has already been assigned.
   * The specified type is interpreted as follows:
   * <ul>
   *   <li> If {@code "json"} is specified, the value is converted according to the rules
   *        specified in {@link JsonW3Converter}.</li>
   *   <li> Otherwise, the type is cast to the specified XDM type.</li>
   * </ul>
   * @param name name of variable; context value if empty string or {@code null}
   * @param value value to be bound (object or XQuery {@link Value})
   * @param type type (can be {@code null})
   * @param sc static context
   * @throws QueryException query exception
   */
  public void bind(final String name, final Object value, final String type, final StaticContext sc)
      throws QueryException {
    final QNm qnm = name == null || name.isEmpty() ? QNm.EMPTY : qname(name, sc);
    if(!bindings.contains(qnm)) bindings.put(qnm, cast(value, type));
  }

  /**
   * Adds some evaluation info.
   * @param string evaluation info
   */
  public void evalInfo(final String string) {
    if(parent != null) {
      parent.evalInfo(string);
    } else {
      info.evalInfo(string);
    }
  }

  /**
   * Sends the specified message to the tracer.
   * @param label label (can be {@code null})
   * @param message message function
   */
  public void trace(final String label, final Supplier<String> message) {
    if(parent != null) {
      parent.trace(label, message);
    } else {
      final QueryTracer qc = jc().tracer();
      if(qc.moreTraces(++traces)) {
        final String lbl = label != null ? label.replaceAll("\\s+", " ") : "";
        final String msg = (lbl.isEmpty() || lbl.matches("^.*\\p{P} ?$") ? lbl : lbl + ": ") +
            message.get();
        if(qc.printTrace(msg)) evalInfo(msg);
      }
    }
  }

  /**
   * Returns query-specific or default serialization parameters.
   * @return serialization parameters
   */
  public SerializerOptions parameters() {
    if(sopts == null) {
      sopts = new SerializerOptions(context.options.get(MainOptions.SERIALIZER));
      defaultOutput = main != null;
    }
    return sopts;
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
  public FNode toXml(final boolean full) {
    // only show root node if functions or variables exist
    final QueryPlan plan = new QueryPlan(compiled, closed, full);
    if(main != null) {
      for(final StaticDecl ss : main.references()) ss.toXml(plan);
      main.toXml(plan);
    } else {
      functions.toXml(plan);
      vars.toXml(plan);
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
    profiler.finish();
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

    final Performance perf = jc().performance;
    if(perf != null) info.serializing.addAndGet(perf.nanoRuntime());
  }

  @Override
  public String shortInfo() {
    return SAVE + DOTS;
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
    return main != null ? main.toString() : info.query;
  }

  // CLASS METHODS ================================================================================

  /**
   * Caches the result of the specified query.
   * This function is called by the GUI and must only be called after optimizing the query.
   * @param cmd query command
   * @param max maximum number of items to cache
   */
  void cache(final AQuery cmd, final int max) {
    final ItemList items = new ItemList();
    final IntList pres = new IntList();
    final Data data = resources.globalData();
    if(data != null) ftPosData = new FTPosData(data);
    try {
      run(info.evaluating, () -> {
        // evaluates the query
        final Iter iter = updating ? update().iter() : main.iter(this);
        Item item;

        do {
          // collect PRE values if a database is opened globally
          if(defaultOutput && data != null) {
            while((item = next(iter)) != null && item.data() == data && pres.size() < max) {
              pres.add(((DBNode) item).pre());
            }
            // skip if all results have been collected
            if(item == null || pres.size() >= max) break;

            // otherwise, add nodes to standard iterator
            for(final int pre : pres.finish()) items.add(new DBNode(data, pre));
            items.add(item);
          }

          // use standard iterator
          while((item = next(iter)) != null && items.size() < max) {
            item.cache(false, null);
            items.add(item);
          }
        } while(false);
        return null;
      });
    } catch(final QueryException | JobException ex) {
      cmd.exception = ex;
    }
    // collect results
    cmd.result = !items.isEmpty() ? items.value() :
      !pres.isEmpty() ? new DBNodes(data, pres.finish()).ftpos(ftPosData) : Empty.VALUE;
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
      final Value value = main.value(this);
      // only perform updates if no parent context exists
      if(updates == null || parent != null) return value;

      // create copies of results that will be modified by an update operation
      final HashSet<Data> datas = updates.prepare(this);
      final StringList dbs = updates.databases();

      final ValueBuilder vb = new ValueBuilder(this);
      final QueryConsumer<Value> materialize = val -> vb.add(val.materialize(d -> d != null &&
          (datas.contains(d) || !d.inMemory() && dbs.contains(d.meta.name)), null, this));
      materialize.accept(value);
      materialize.accept(updates.output(true, this));

      // invalidate current node set in context, apply updates
      if(context.data() != null) context.invalidate();
      updates.apply(this);

      return vb.value(value);
    } catch(final StackOverflowError ex) {
      Util.debug(ex);
      throw BASEX_OVERFLOW.get(null);
    }
  }

  /**
   * Casts a value to the specified type.
   * @param value value to be cast
   * @param type type (can be {@code null})
   * @return cast value
   * @throws QueryException query exception
   */
  private Value cast(final Object value, final String type) throws QueryException {
    Object object = value;
    if(object instanceof final String string) {
      final StringList strings = new StringList(1);
      // strings containing multiple items (value \1 ...)
      if(string.indexOf('\1') == -1) {
        strings.add(string);
      } else {
        object = strings.add(string.split("\1")).toArray();
      }

      // subtypes overriding the global value (value \2 type)
      if(string.indexOf('\2') != -1) {
        final ValueBuilder vb = new ValueBuilder(this, strings.size());
        for(final String str : strings) {
          final int i = str.indexOf('\2');
          final String val = i == -1 ? str : str.substring(0, i);
          final String tp = i == -1 ? type : str.substring(i + 1);
          vb.add(cast(val, tp));
        }
        return vb.value();
      }
    }

    // no type specified: return original value or convert as Java object
    if(type == null || type.isEmpty()) {
      return object instanceof final Value val ? val : JavaCall.toValue(object, this, null);
    }

    // convert JSON input
    if(type.equalsIgnoreCase(MainParser.JSON.name())) {
      final JsonParserOptions jp = new JsonParserOptions();
      jp.set(JsonOptions.FORMAT, JsonFormat.W3);
      try {
        final TextInput ti = new TextInput(new IOContent(object.toString()));
        return JsonConverter.get(jp).convert(ti, "", null, this);
      } catch(final IOException ex) {
        throw new QueryException(ex);
      }
    }

    // parse target type
    final StaticContext sc = main != null ? main.sc : new StaticContext(this);
    final SeqType st = new QueryParser(type, null, this, sc).parseSeqType();
    if(st.eq(SeqType.EMPTY_SEQUENCE_Z)) return Empty.VALUE;
    final Type tp = st.type;

    // cast XQuery values
    if(object instanceof final Value val) {
      // cast single item
      if(val.size() == 1) return tp.cast((Item) val, this, null);
      // cast sequence
      final ValueBuilder vb = new ValueBuilder(this, val.size());
      for(final Item item : val) vb.add(tp.cast(item, this, null));
      return vb.value(tp);
    }

    // cast sequences
    if(object instanceof final Object[] array) {
      final ValueBuilder vb = new ValueBuilder(this, array.length);
      for(final Object val : array) vb.add(tp.cast(val, this, null));
      return vb.value(tp);
    }

    // cast any other object to XDM
    return tp.cast(object, this, null);
  }

  /**
   * Runs code and measures its runtime.
   * @param code code to run
   * @param runtime value storing the runtime
   * @param <T> type of result value
   * @return result
   * @throws QueryException query exception
   */
  private <T> T run(final AtomicLong runtime, final QuerySupplier<T> code) throws QueryException {
    Performance perf = jc().performance;
    if(perf == null) {
      perf = new Performance();
    } else {
      perf.nanoRuntime();
    }
    try {
      return code.get();
    } finally {
      runtime.addAndGet(perf.nanoRuntime());
    }
  }

  /**
   * Converts the specified variable name to a QName.
   * @param name name of variable
   * @param sc static context
   * @return QName
   * @throws QueryException query context
   */
  private static QNm qname(final String name, final StaticContext sc) throws QueryException {
    return QNm.parse(token(Strings.startsWith(name, '$') ? name.substring(1) : name), sc);
  }

  /**
   * Checks whether the given string contains a valid XQuery version number.
   * @param version version string
   * @return true, if string contains a valid XQuery version number.
   */
  public static boolean isSupported(final String version) {
    return Strings.eq(version, "1.0", "1.1", "3.0", "3.1", "4.0");
  }
}
