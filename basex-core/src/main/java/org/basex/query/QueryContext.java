package org.basex.query;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.jobs.*;
import org.basex.core.locks.*;
import org.basex.core.users.*;
import org.basex.data.*;
import org.basex.io.parse.json.*;
import org.basex.io.serial.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
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
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

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
  /** Values of static variables (shared with parent context). */
  public final GlobalValues globals;
  /** Functions. */
  public final StaticFuncs functions = new StaticFuncs();
  /** Static and dynamic namespaces. */
  public final NSDynContext ns;
  /** Query resources. */
  public final QueryResources resources;
  /** Parent query context (can be {@code null}). */
  public final QueryContext parent;
  /** Database context. */
  public final Context context;
  /** Query info. */
  public final QueryInfo info;

  /** Query-specific database options. */
  final QueryOptions options = new QueryOptions(this);

  /** Expression caches. */
  public final ExprCaches exprCaches = new ExprCaches();
  /** Shared data references. */
  public final SharedData shared;
  /** Caches of the root query (shared with parent context). */
  public final GlobalCaches globalCaches;
  /** User-defined locks. */
  public final LockList locks = new LockList();
  /** Current query focus. */
  public QueryFocus focus = new QueryFocus();
  /** Date/time values (can be {@code null}). */
  private QueryDateTime dateTime;

  /** Update container (can be {@code null}; will be created for the first update). */
  public Updates updates;
  /** User. */
  public User user;
  /** Indicates if parsed code may access static resources without ADMIN permission. */
  public boolean parseResources;
  /** Maximum permission of the parsed code. */
  public Perm maxPerm;
  /** Indicates if the values of the {@link MainOptions#BINDINGS} option will be bound. */
  public boolean globalBindings;

  /** Full-text state. */
  public final FTState ft;

  /** Available collations. */
  public TokenObjectMap<Collation> collations;
  /** Profiling results. */
  public final QueryProfiler profiler = new QueryProfiler(this);

  /** Counter for variable IDs. */
  public int varIDs;

  /** Parsed and pre-declared modules. */
  public final ModuleRegistry modules;

  /** Main module (root expression, can be {@code null}). */
  public MainModule main;
  /** Context value type (can be {@code null}). */
  public SeqType contextType;
  /** Context scope (can be {@code null}). */
  public ContextScope contextValue;
  /** Indicates if context scope exists and is final. */
  public boolean finalContext;

  /** External variables and context to be bound at compile time. */
  private final QueryBindings bindings = new QueryBindings(this);

  /** Serialization options (can be {@code null}). */
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
   * @param nsContext inherited namespace context (can be {@code null})
   */
  public QueryContext(final QueryContext parent, final NSDynContext nsContext) {
    this(parent.context, parent, nsContext, parent.info);
    parent.pushJob(this);
    updates = parent.updates;
  }

  /**
   * Constructor.
   * @param context database context
   */
  public QueryContext(final Context context) {
    this(context, null, null, null);
  }

  /**
   * Constructor.
   * @param context database context
   * @param parent parent context (can be {@code null})
   * @param nsContext inherited namespace context (can be {@code null})
   * @param info query info (can be {@code null})
   */
  public QueryContext(final Context context, final QueryContext parent,
      final NSDynContext nsContext, final QueryInfo info) {
    this.context = context;
    this.parent = parent;
    this.info = info != null ? info : new QueryInfo(context);
    ns = new NSDynContext(nsContext);
    resources = parent != null ? parent.resources : new QueryResources(this);
    ft = new FTState(parent != null ? parent.ft : null);
    shared = parent != null ? parent.shared : new SharedData();
    globalCaches = parent != null ? parent.globalCaches : new GlobalCaches();
    modules = new ModuleRegistry(parent != null ? parent.modules : null);
    globals = parent != null ? parent.globals : new GlobalValues();
    user = parent != null ? parent.user : context.user();
    maxPerm = parent != null ? parent.maxPerm : Perm.ADMIN;
    if(parent != null) options.state(parent.options.state());
  }

  /**
   * Parses the specified query.
   * @param query query string
   * @param uri base URI (can be {@code null})
   * @return module
   * @throws QueryException query exception
   */
  public AModule parse(final String query, final String uri) throws QueryException {
    return parse(query, uri, null);
  }

  /**
   * Parses the specified query.
   * @param query query string
   * @param uri base URI (can be {@code null})
   * @param sc static context (can be {@code null})
   * @return module
   * @throws QueryException query exception
   */
  public AModule parse(final String query, final String uri, final StaticContext sc)
      throws QueryException {
    return QueryParser.isLibrary(query) ? parseLibrary(query, uri, sc) : parseMain(query, uri, sc);
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
      if(parent == null) info.query = query;
      main = new QueryParser(query, uri, this, sc).parseMain();
      return main;
    });
  }

  /**
   * Parses the specified module.
   * @param query query string
   * @param uri base URI (can be {@code null})
   * @param sc static context (can be {@code null})
   * @return library module
   * @throws QueryException query exception
   */
  public LibraryModule parseLibrary(final String query, final String uri, final StaticContext sc)
      throws QueryException {
    return run(info.parsing, () -> {
      if(parent == null) info.query = query;
      return new QueryParser(query, uri, this, sc).parseLibrary(true);
    });
  }

  /**
   * Creates a function call for the specified function and assigns it as root expression.
   * @param func user-defined function
   * @param args arguments
   * @throws QueryException query exception
   */
  public void assign(final StaticFunc func, final Expr... args) throws QueryException {
    for(final StaticFunc sf : functions) {
      if(func.info.equals(sf.info)) {
        if(!sf.anns.contains(Annotation._BASEX_INLINE)) {
          // explicit lock: disable inlining. otherwise, enforce inlining for large function bodies
          final Value value = sf.anns.contains(Annotation._BASEX_LOCK) ? Itr.ZERO : Empty.VALUE;
          sf.anns = sf.anns.attach(new Ann(sf.info, Annotation._BASEX_INLINE, value));
        }
        // create and assign function call
        final StaticFuncCall call = new StaticFuncCall(sf.name, args, null, sf.info);
        call.setFunc(sf);
        main = new MainModule(call, new VarScope(), sf.sc);
        updating = sf.updating();
        return;
      }
    }
    throw BASEX_WHICH_X.get(null, func);
  }

  /**
   * Assigns a dynamic call of a function item as main module.
   * @param function function item
   * @param args function arguments
   */
  public void assign(final FItem function, final Value[] args) {
    // the function carries the static context of the query it was created in
    final InputInfo ii = function instanceof final FuncItem fi ? fi.info() : null;
    final StaticContext sctx = ii != null ? ii.sc() : new StaticContext(this);
    updating = function.annotations().contains(Annotation.UPDATING);
    main = new MainModule(new DynFuncCall(ii, updating, false, function, args), new VarScope(),
        sctx);
  }

  /**
   * Compiles the expression. Performs static optimizations.
   * @throws QueryException query exception
   */
  public void compile() throws QueryException {
    if(compiled) return;
    compiled = true;

    run(info.compiling, () -> {
      options.compile();

      bindings.assign();

      compile(false);
      return null;
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
      // bind opened database if it is locked by the query
      final StaticContext sc = main.sc;
      if(parent == null && bindings.context() == null) {
        final DBNodes nodes = context.current();
        if(nodes != null && QueryLocking.locked(this, nodes.data())) {
          bind(null, resources.compile(nodes), null, sc);
        }
      }
      final Value value = bindings.context();
      if(value != null) {
        contextValue = new ContextScope(value, sc.contextType, new VarScope(), sc, null, null);
      }
      if(contextValue != null) finalContext = true;

      compile(true);
      return null;
    });
  }

  /**
   * Compiles the expression.
   * @param dynamic dynamic compilation
   * @throws QueryException query exception
   */
  public void compile(final boolean dynamic) throws QueryException {
    checkStop();

    info.runtime = false;
    try {
      final CompileContext cc = new CompileContext(this, dynamic);
      if(contextValue != null && dynamic) {
        try {
          contextValue.compile(cc);
          focus.value = contextValue.value(this);
          for(final LibraryModule lib : modules.libraries()) {
            if(lib.sc.contextType != null && !lib.sc.contextType.instance(focus.value)) {
              throw VALUETYPES_X_X.get(contextValue.info, lib.sc.contextType, contextType);
            }
          }
        } catch(final QueryException ex) {
          throw ex.error() == NOCTX_X ? CIRCCTX.get(ex.info()).cause(ex) : ex;
        }
      }
      if(main != null) {
        QueryCompiler.compile(cc);
      } else {
        // required for XQueryParse
        functions.compileAll(cc);
        // required for fn:load-xquery-module
        vars.compileAll(cc);
      }
      // initializing expressions of record fields may be evaluated by coercions
      vars.compileFields(cc);
    } catch(final StackOverflowError | ArraySizeException ex) {
      throw limitError(ex, null);
    } finally {
      info.runtime = true;
    }
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
   * @return result
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
    QueryLocking.add(this, jc().locks);
  }

  /**
   * Binds a value to a global variable or the context value.
   * The arguments will be ignored if a value has already been assigned.
   * The specified type is interpreted as follows:
   * <ul>
   *   <li> If {@code "json"} is specified, the value is converted according to the rules
   *        specified in {@link JsonW3Converter}.</li>
   *   <li> Otherwise, the value is cast to the specified XDM type.</li>
   * </ul>
   * @param name name of variable; context value if empty string or {@code null}
   * @param value value to be bound (object or XQuery {@link Value})
   * @param type type (can be {@code null})
   * @param sc static context
   * @throws QueryException query exception
   */
  public void bind(final String name, final Object value, final String type, final StaticContext sc)
      throws QueryException {
    bindings.bind(name, value, type, sc);
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
   * Returns the maximum permission of the code at the specified location.
   * @param ii input info (can be {@code null})
   * @return permission
   */
  public Perm perm(final InputInfo ii) {
    // code without static context: fall back to the permission of the query
    return ii == null || ii.sc() == null ? maxPerm : ii.sc().maxPerm;
  }

  /**
   * Indicates if the code at the specified location may access external resources.
   * @param ii input info (can be {@code null})
   * @return result of check
   */
  public boolean trusted(final InputInfo ii) {
    return perm(ii).has(Perm.ADMIN);
  }

  /**
   * Indicates if the current user and the code at the specified location may access external
   * resources when parsing XML.
   * @param ii input info (can be {@code null})
   * @return result of check
   */
  public boolean allowExternal(final InputInfo ii) {
    return has(Perm.ADMIN, null, ii);
  }

  /**
   * Checks if the current user and the code at the specified location have the given permission.
   * @param perm permission
   * @param db database pattern (can be {@code null})
   * @param ii input info (can be {@code null})
   * @return result of check
   */
  public boolean has(final Perm perm, final String db, final InputInfo ii) {
    return perm(ii).has(perm) && user.has(perm, db);
  }

  /**
   * Raises an error if the current user or the code at the specified location lacks the given
   * permission.
   * @param perm permission
   * @param db database pattern (can be {@code null})
   * @param subject subject of the check (for error messages)
   * @param ii input info (can be {@code null})
   * @throws QueryException query exception
   */
  public void checkPerm(final Perm perm, final String db, final Object subject,
      final InputInfo ii) throws QueryException {
    if(!user.has(perm, db)) throw BASEX_PERMISSION_X_X.get(ii, perm, subject);
    if(!perm(ii).has(perm)) {
      // untrusted code: no access to external resources
      throw perm == Perm.ADMIN ? EXTERNALRESOURCE_X.get(ii, subject) :
        BASEX_PERMISSION_X_X.get(ii, perm, subject);
    }
  }

  /**
   * Sends a serialized value to the tracer.
   * @param label label (can be {@code null})
   * @param input value to be traced
   */
  public void trace(final String label, final Value input) {
    if(input.isEmpty() || input instanceof RangeSeq || input instanceof SingletonSeq) {
      trace(label, input::toErrorString);
    } else {
      for(final Item item : input) {
        final Supplier<String> message = item instanceof FItem ? item::toErrorString : () -> {
          try {
            return item.serialize(SerializerMode.API.get()).toString();
          } catch(final QueryIOException ex) {
            return ex.getMessage(); // unexpected
          }
        };
        trace(label, message);
      }
    }
  }

  /**
   * Sends the specified message to the tracer.
   * @param label label (can be {@code null})
   * @param message message function
   */
  public void trace(final String label, final Supplier<String> message) {
    // discard output of users with insufficient permissions
    if(!user.has(Perm.ADMIN)) return;
    if(parent != null) {
      parent.trace(label, message);
    } else {
      final QueryTracer qc = jc().tracer();
      if(qc.moreTraces(++traces)) {
        String lbl = label != null ? label.replaceAll("\\s+", " ").trim() : "";
        if(!lbl.isEmpty()) {
          if(!lbl.matches("^.*\\p{P}$")) lbl += ':';
          lbl += ' ';
        }
        final String string = lbl + message.get();
        qc.printTrace(string);
        if(qc.cacheTrace()) evalInfo(string);
      }
    }
  }

  /**
   * Returns query-specific or default serialization parameters.
   * @return serialization parameters
   */
  public SerializerOptions parameters() {
    if(sopts == null) {
      sopts = new SerializerOptions(options().get(MainOptions.SERIALIZER));
      defaultOutput = main != null;
    }
    return sopts;
  }

  /**
   * Returns the effective main options, which must not be modified.
   * @return main options
   */
  public MainOptions options() {
    return options.get();
  }

  /**
   * Returns the state of the query-specific options.
   * @return state
   */
  public QueryOptions.State optionsState() {
    return options.state();
  }

  /**
   * Assigns the state of the query-specific options.
   * @param st state
   */
  public void optionsState(final QueryOptions.State st) {
    options.state(st);
  }

  /**
   * Returns new main options with the query-local and default options.
   * @return main options
   */
  public MainOptions dbOptions() {
    return options.dbOptions();
  }

  /**
   * Assigns a pragma value to the effective and query-local options.
   * @param option option
   * @param value value
   * @param ii input info (can be {@code null})
   * @return previous state
   * @throws QueryException query exception
   */
  public QueryOptions.State assign(final Option<?> option, final String value,
      final InputInfo ii) throws QueryException {
    return options.assign(option, value, ii);
  }

  /**
   * Creates and returns an XML query plan (expression tree) for this query.
   * @param full include comprehensive information
   * @return query plan
   */
  public FNode toXml(final boolean full) {
    // only show root node if functions or variables exist
    final QueryPlan plan = new QueryPlan(compiled, updating, full);
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
      exprCaches.clear();
      globalCaches.close();
    } else {
      // otherwise, adopt update reference (may have been initialized by sub query)
      parent.updates = updates;
      parent.popJob(this);
    }

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
   * @throws QueryException query exception
   */
  public void set(final Var var, final Value value) throws QueryException {
    stack.set(var, var.checkType(value, this, null));
  }

  /**
   * Binds the arguments of a function call to the function parameters and evaluates the body.
   * @param params function parameters
   * @param args arguments
   * @param coerce coerce arguments to the parameter types
   * @param body function body
   * @param simple indicates if the query focus is neither accessed nor modified
   * @param qf captured query focus (can be {@code null})
   * @return result
   * @throws QueryException query exception
   */
  public Value invoke(final Var[] params, final Value[] args, final boolean coerce,
      final Expr body, final boolean simple, final QueryFocus qf) throws QueryException {

    final int pl = params.length;
    for(int p = 0; p < pl; p++) {
      if(coerce) set(params[p], args[p]);
      else stack.set(params[p], args[p]);
    }

    // use shortcut if focus is not accessed
    if(simple) return body.value(this);

    // assign captured focus, evaluate function body, restore focus
    final QueryFocus qfocus = focus;
    focus = qf != null ? qf : new QueryFocus();
    try {
      return body.value(this);
    } finally {
      focus = qfocus;
    }
  }

  /**
   * Returns a new query focus with the global context value.
   * @param sc static context of the calling expression (can be {@code null})
   * @return query focus
   */
  public QueryFocus globalFocus(final StaticContext sc) {
    final QueryFocus qf = new QueryFocus();
    // code of a loaded module refers to the prolog of that module
    qf.value = sc != null && sc.global != null ? sc.global.value :
      finalContext ? contextValue.value : null;
    return qf;
  }

  /**
   * Initializes the static date and time context of a query if not done yet.
   * @return date and time context
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
    if(data != null) ft.posData = new FTPosData(data);
    try {
      run(info.evaluating, () -> {
        // evaluates the query
        final Iter iter = updating ? update().iter() : main.iter(this);
        Item item;

        // collect PRE values if a database is opened globally
        if(defaultOutput && data != null) {
          while((item = next(iter)) != null && item.data() == data && pres.size() < max) {
            pres.add(((DBNode) item).pre());
          }
          // skip if all results have been collected
          if(item == null || pres.size() >= max) return null;

          // otherwise, add nodes to standard iterator
          for(final int pre : pres.finish()) items.add(new DBNode(data, pre));
          items.add(item);
        }

        // use standard iterator
        while((item = next(iter)) != null && items.size() < max) {
          item.cache(false, null);
          items.add(item);
        }
        return null;
      });
    } catch(final QueryException | JobException ex) {
      cmd.exception = ex;
    }
    // collect results
    cmd.result = !items.isEmpty() ? items.value() :
      !pres.isEmpty() ? new DBNodes(data, pres.finish()).ftpos(ft.posData) : Empty.VALUE;
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Returns the result of an updating expression.
   * @return result
   * @throws QueryException query exception
   */
  private Value update() throws QueryException {
    // retrieve result
    final Value value = main.value(this);
    // only perform updates if no parent context exists
    if(updates == null || parent != null) return value;

    final ValueBuilder vb = new ValueBuilder(this);
    try {
      // create copies of results that will be modified by an update operation
      final HashSet<Data> datas = updates.prepare(this);
      final StringList dbs = updates.databases();

      final QueryConsumer<Value> materialize = val -> vb.add(val.materialize(d -> d != null &&
          (datas.contains(d) || !d.inMemory() && dbs.contains(d.meta.name)), null, this));
      materialize.accept(value);
      materialize.accept(updates.output(true, this));

      // invalidate current node set in context, apply updates
      if(context.data() != null) context.invalidate();
      updates.apply(this);
    } finally {
      // drop remaining temporary database instances
      updates.discard();
    }
    return vb.value(value);
  }

  /**
   * Runs code and measures its runtime.
   * @param runtime value storing the runtime
   * @param code code to run
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
    } catch(final StackOverflowError | ArraySizeException ex) {
      throw limitError(ex, null);
    } finally {
      runtime.addAndGet(perf.nanoRuntime());
    }
  }

  /**
   * Checks whether the given string contains a valid XQuery version number.
   * @param version version string
   * @return true, if string contains a valid XQuery version number
   */
  public static boolean isSupported(final String version) {
    return Strings.eq(version, "1.0", "1.1", "3.0", "3.1", "4.0");
  }
}
