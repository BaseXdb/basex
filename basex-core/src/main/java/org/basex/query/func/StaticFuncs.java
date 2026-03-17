package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.func.java.*;
import org.basex.query.util.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.util.parse.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;
import org.basex.util.similarity.*;

/**
 * Container for user-defined functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StaticFuncs extends ExprInfo implements Iterable<StaticFunc> {
  /** Functions grouped by declaring module, then by QName, with lists of overloads. */
  private final TokenObjectMap<QNmMap<ArrayList<StaticFunc>>> funcsByModule =
      new TokenObjectMap<>();
  /** Unresolved function references. */
  private final ArrayList<FuncRef> unresolvedRefs = new ArrayList<>();
  /** Function calls by function. */
  private final Map<StaticFunc, ArrayList<StaticFuncCall>> callsMap = new IdentityHashMap<>();

  /**
   * Declares a new user-defined function.
   * @param sc static context
   * @param name function name
   * @param params parameters with variables and optional default values
   * @param expr function body (can be {@code null})
   * @param anns annotations
   * @param doc xqdoc string
   * @param vs variable scope
   * @param info input info (can be {@code null})
   * @return static function reference
   * @throws QueryException query exception
   */
  public StaticFunc declare(final StaticContext sc, final QNm name, final Params params,
      final Expr expr, final AnnList anns, final String doc, final VarScope vs,
      final InputInfo info) throws QueryException {

    final byte[] modUri = Token.eq(name.uri(), FN_URI) ? FN_URI : QNm.uri(sc.module);
    final StaticFunc sf = new StaticFunc(name, params, expr, anns, vs, info, doc);
    if(get(sc, name, sf.min, sf.arity()) != null) throw DUPLFUNC_X.get(info, name);
    funcsByModule.computeIfAbsent(modUri, QNmMap::new).computeIfAbsent(name, ArrayList::new).
        add(sf);
    return sf;
  }

  /**
   * Creates a new unresolved function reference.
   * @param resolve function to resolve the reference
   * @return unresolved function reference
   */
  public Expr newRef(final QuerySupplier<Expr> resolve) {
    final FuncRef fr = new FuncRef(resolve);
    unresolvedRefs.add(fr);
    return fr;
  }

  /**
   * Assigns a function to a static function call.
   * @param call name function name
   * @param qc query context
   * @throws QueryException query exception
   */
  void setFunc(final StaticFuncCall call, final QueryContext qc) throws QueryException {
    final InputInfo info = call.info();
    final QNm name = call.name;
    final int arity = call.arity();
    final StaticFunc func = get(info.sc(), name, arity);
    if(func != null) {
      if(func.expr == null) throw FUNCNOIMPL_X.get(func.info, func.name.prefixString());
      call.setFunc(func);
      if(func.updating) qc.updating();
      // update map for direct lookups of function calls
      callsMap.computeIfAbsent(func, k -> new ArrayList<>(1)).add(call);
    } else {
      final JavaCall java = JavaCall.get(name, call.exprs, qc, info);
      if(java == null) throw unknownFunctionError(name, arity, info);
      call.setExternal(java);
      if(java.updating) qc.updating();
    }
  }

  /**
   * Resolves all function calls.
   * @throws QueryException query exception
   */
  public void resolve() throws QueryException {
    for(final FuncRef fr : unresolvedRefs) fr.resolve();
    unresolvedRefs.clear();
  }

  /**
   * Checks if the updating semantics are satisfied.
   * @throws QueryException query exception
   */
  public void checkUp() throws QueryException {
    for(final StaticFunc func : this) func.checkUp();
  }

  /**
   * Compiles all functions.
   * @param cc compilation context
   */
  public void compileAll(final CompileContext cc) {
    for(final StaticFunc func : this) func.compile(cc);
  }

  /**
   * Returns the function with the given name and arity.
   * @param sc static context
   * @param qname function name
   * @param arity function arity
   * @return function if found, {@code null} otherwise
   */
  public StaticFunc get(final StaticContext sc, final QNm qname, final int arity) {
    return get(sc, qname, arity, arity);
  }

  /**
   * Returns a visible function with the given name and arity range.
   * @param sc static context
   * @param qname function name
   * @param min minimum function arity
   * @param max maximum function arity
   * @return function if found, {@code null} otherwise
   */
  private StaticFunc get(final StaticContext sc, final QNm qname, final int min, final int max) {
    final byte[] funcUri = qname.uri();
    final byte[] modUri = Token.eq(funcUri, FN_URI) ? FN_URI : QNm.uri(sc.module);
    StaticFunc func = get(modUri, qname, min, max);
    if(func == null && sc.imports.contains(funcUri)) {
      func = get(funcUri, qname, min, max);
      if(func != null && func.anns.contains(Annotation.PRIVATE)) func = null;
    }
    return func;
  }

  /**
   * Returns a function with the given name, and arity in the given range, from the specified
   * module.
   * @param modUri module URI
   * @param qname function name
   * @param min minimum arity
   * @param max maximum arity
   * @return function if found, {@code null} otherwise
   */
  private StaticFunc get(final byte[] modUri, final QNm qname, final int min, final int max) {
    final QNmMap<ArrayList<StaticFunc>> funcsByName = funcsByModule.get(modUri);
    if(funcsByName != null) {
      final ArrayList<StaticFunc> funcs = funcsByName.get(qname);
      if(funcs != null) {
        for(final StaticFunc func : funcs) if(min <= func.arity() && max >= func.min) return func;
      }
    }
    return null;
  }

  /**
   * Returns the unions of the sequences types for function calls of the specified function.
   * @param func function
   * @return sequence types, or {@code null} if function is not referenced
   */
  SeqType[] seqTypes(final StaticFunc func) {
    final ArrayList<StaticFuncCall> calls = callsMap.get(func);
    final int sl = func.arity();
    if(calls == null || calls.isEmpty() || sl == 0) return null;

    final SeqType[] seqTypes = new SeqType[sl];
    for(final StaticFuncCall call : calls) {
      for(int s = 0; s < sl; s++) {
        final SeqType st = call.arg(s).seqType(), stOld = seqTypes[s];
        seqTypes[s] = stOld == null ? st : stOld.union(st);
      }
    }
    return seqTypes;
  }

  /**
   * Creates an exception for an unknown function.
   * @param name function name
   * @param arity function arity
   * @param info input info
   * @return query exception
   */
  QueryException unknownFunctionError(final QNm name, final int arity,
      final InputInfo info) {

    final byte[] funcUri = name.uri();
    final StaticFunc sf = get(funcUri, name, arity, arity);
    if(sf != null) return sf.anns.contains(Annotation.PRIVATE) ? FUNCPRIVATE_X.get(info, name)
                                                               : INVISIBLEFUNC_X.get(info, name);
    final ArrayList<byte[]> modules = new ArrayList<>(2);
    if(Token.eq(funcUri, FN_URI)) {
      modules.add(FN_URI);
    } else {
      modules.add(QNm.uri(info.sc().module));
      if(info.sc().imports.contains(funcUri)) modules.add(funcUri);
    }
    final IntList arities = new IntList();
    for(final byte[] module : modules) {
      final QNmMap<ArrayList<StaticFunc>> funcsByName = funcsByModule.get(module);
      if(funcsByName != null) {
        final ArrayList<StaticFunc> funcs = funcsByName.get(name);
        if(funcs != null) {
          for(final StaticFunc func : funcs) {
            for(int a = func.min; a <= func.arity(); ++a) arities.add(a);
          }
        }
      }
    }
    return arities.isEmpty()
        ? similarError(name, info)
        : Functions.wrongArity(arity, arities, false, info, name.prefixString());
  }

  /**
   * Throws an exception if the name of a function is similar to the specified function name.
   * @param qname function name
   * @param info input info (can be {@code null})
   * @return exception
   */
  QueryException similarError(final QNm qname, final InputInfo info) {
    // check local functions
    final QNmSet names = new QNmSet();
    if(info != null) {
      for(final StaticFunc func : this) {
        if(func.expr != null && (!func.anns.contains(Annotation.PRIVATE)
            || Token.eq(QNm.uri(info.sc().module), QNm.uri(func.sc.module)))) {
          names.add(func.name);
        }
      }
    }
    final QNm similar = (QNm) Levenshtein.similar(qname.local(), names.keys(),
        o -> ((QNm) o).local());

    // return error for local or global function
    return WHICHFUNC_X.get(info, similar != null ?
      similar(qname.prefixString(), similar.prefixString()) :
      Functions.similar(qname));
  }

  @Override
  public Iterator<StaticFunc> iterator() {
    return new Iterator<>() {
      final Iterator<QNmMap<ArrayList<StaticFunc>>> modules = funcsByModule.values().iterator();
      Iterator<ArrayList<StaticFunc>> names = Collections.emptyIterator();
      Iterator<StaticFunc> funcs = Collections.emptyIterator();

      @Override public boolean hasNext() {
        while(!funcs.hasNext()) {
          if(names.hasNext()) funcs = names.next().iterator();
          else if(modules.hasNext()) names = modules.next().values().iterator();
          else return false;
        }
        return true;
      }

      @Override public StaticFunc next() {
        if(!hasNext()) throw new NoSuchElementException();
        return funcs.next();
      }
    };
  }

  @Override
  public void toXml(final QueryPlan plan) {
    if(funcsByModule.isEmpty()) return;
    final ArrayList<StaticFunc> list = new ArrayList<>();
    forEach(list::add);
    plan.add(plan.create(this), list.toArray(StaticFunc[]::new));
  }

  @Override
  public void toString(final QueryString qs) {
    for(final StaticFunc func : this) if(func.compiled()) qs.token(func).token(Text.NL);
  }

  /**
   * A reference to an initially unresolved function call or named function item, to be resolved
   * after parsing, when all user-defined function declarations have been processed.
   */
  private static final class FuncRef extends Single {
    /** Function to resolve this reference. */
    private final QuerySupplier<Expr> resolve;

    /**
     * Constructor.
     * @param resolve function to resolve the reference
     */
    FuncRef(final QuerySupplier<Expr> resolve) {
      super(null, null, Types.ITEM_ZM);
      this.resolve = resolve;
    }

    /**
     * Resolves the function reference.
     * @throws QueryException query exception
     */
    void resolve() throws QueryException {
      expr = resolve.get();
    }

    @Override
    public void checkUp() throws QueryException {
      expr.checkUp();
    }

    @Override
    public boolean vacuous() {
      return expr.vacuous();
    }

    @Override
    public Expr compile(final CompileContext cc) throws QueryException {
      return expr.compile(cc);
    }

    @Override
    public boolean accept(final ASTVisitor visitor) {
      return expr != null && super.accept(visitor);
    }

    @Override
    public Expr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
      throw Util.notExpected();
    }

    @Override
    public void toString(final QueryString qs) {
      expr.toString(qs);
    }
  }
}
