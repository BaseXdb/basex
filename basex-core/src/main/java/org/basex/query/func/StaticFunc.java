package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.util.parse.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * A static user-defined function.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class StaticFunc extends StaticDecl implements XQFunction {
  /** Parameters. */
  public final Var[] params;
  /** Default expressions (entries can be {@code null} references). */
  final Expr[] defaults;
  /** Minimum number of arguments. */
  final int min;
  /** Updating flag. */
  final boolean updating;
  /** Memoization flag. */
  private final boolean memo;

  /** Indicates if the query focus is accessed or modified. */
  private boolean simple;
  /** Indicates if the function body enforces the declared return type. */
  private boolean checked;

  /** Prepared default expressions (entries can be {@code null}). */
  private final Prepared[] dflts;
  /** Cached properties of the default expressions (entries can be {@code null}). */
  private final FlagCache[] dprops;

  /**
   * Default expression with a variable scope of its own.
   * @param expr expression
   * @param scope variable scope
   */
  private record Prepared(Expr expr, VarScope scope) { }

  /**
   * Function constructor.
   * @param name function name
   * @param params parameters
   * @param expr function body (can be {@code null})
   * @param anns annotations
   * @param vs variable scope
   * @param info input info (can be {@code null})
   * @param doc xqdoc string
   */
  StaticFunc(final QNm name, final Params params, final Expr expr, final AnnList anns,
      final VarScope vs, final InputInfo info, final String doc) {
    super(name, params.seqType(), anns, vs, info, doc);

    this.params = params.vars();
    defaults = params.defaults();
    this.expr = expr;
    updating = anns.contains(Annotation.UPDATING);
    memo = anns.contains(Annotation._BASEX_MEMO);

    final int dl = defaults.length;
    int mn = dl;
    dflts = new Prepared[dl];
    dprops = new FlagCache[dl];
    for(int d = 0; d < dl; d++) {
      if(defaults[d] != null) {
        final int i = d;
        dprops[d] = new FlagCache(flag -> dflt(i).has(flag));
        mn--;
      }
    }
    min = mn;
  }

  /**
   * Returns a default expression, compiled or prepared if available.
   * @param index parameter index
   * @return default expression
   */
  private Expr dflt(final int index) {
    final Prepared prepared = dflts[index];
    return prepared != null ? prepared.expr : defaults[index];
  }

  /**
   * Prepares a default expression for evaluation: assigns a copy with a variable scope of its own.
   * @param index parameter index
   * @param cc compilation context
   * @return {@code true} if the expression was prepared by this call
   */
  private synchronized boolean prepareDefault(final int index, final CompileContext cc) {
    if(dflts[index] != null) return false;
    final VarScope scope = new VarScope();
    cc.pushScope(scope);
    try {
      dflts[index] = new Prepared(defaults[index].copy(cc, new IntObjectMap<>()), scope);
    } finally {
      cc.removeScope();
    }
    return true;
  }

  /**
   * Compiles a default expression without knowing the focus of the caller.
   * @param index parameter index
   * @param cc compilation context
   * @return compiled expression
   * @throws QueryException query exception
   */
  Expr compileDefault(final int index, final CompileContext cc) throws QueryException {
    // recursive references: the default expression is compiled at the outermost call
    if(prepareDefault(index, cc)) {
      final Prepared prepared = dflts[index];
      Expr ex = prepared.expr;
      cc.pushFocus(null, false);
      cc.pushScope(prepared.scope);
      try {
        ex = ex.compile(cc);
      } finally {
        cc.removeScope();
        cc.removeFocus();
      }
      dflts[index] = new Prepared(ex, prepared.scope);
      dprops[index].clear();
    }
    return dflts[index].expr;
  }

  /**
   * Evaluates a default expression with the focus of the caller.
   * @param index parameter index
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  Value defaultValue(final int index, final QueryContext qc) throws QueryException {
    // defaults assigned at runtime are not compiled
    Prepared prepared = dflts[index];
    if(prepared == null) {
      prepareDefault(index, new CompileContext(qc, true));
      prepared = dflts[index];
    }
    final Value current = qc.current;
    qc.current = null;
    final int fp = prepared.scope.enter(qc);
    try {
      return prepared.expr.value(qc);
    } finally {
      prepared.scope.exit(fp, qc);
      qc.current = current;
    }
  }

  /**
   * Indicates if a default expression has one of the specified compiler properties.
   * @param index parameter index
   * @param flags flags
   * @return result of check
   */
  boolean defaultHas(final int index, final Flag... flags) {
    return dprops[index].has(flags);
  }

  @Override
  public Expr compile(final CompileContext cc) {
    if(!compiled && expr != null) {
      compiled = true;
      simple = !expr.has(Flag.CTX);

      // dynamic compilation: refine parameter types to arguments types of function call
      final SeqType[] callTypes = cc.dynamic ? cc.qc.functions.seqTypes(this, cc) : null;
      final int pl = params.length;
      if(callTypes != null) {
        boolean refined = false;
        for(int p = 0; p < pl; p++) {
          final Var param = params[p];
          final SeqType cst = callTypes[p], pst = param.seqType();
          if(!cst.eq(pst) && cst.refines(pst)) {
            param.declType = cst;
            refined = true;
          }
        }
        if(refined) cc.info(OPTREFINED_X, funcLabel());
      }

      // compile function body, handle return type
      cc.pushFocus(null, false);
      cc.pushScope(vs, anns);
      try {
        cc.enter(this, () -> {
          expr = expr.compile(cc);
          if(declType != null && !checked) expr = new TypeCheck(info, expr, declType).optimize(cc);
          return null;
        });
      } catch(final QueryException ex) {
        expr = FnError.get(ex);
      } finally {
        cc.removeScope(this, anns);
        cc.removeFocus();
      }
      checked = true;
      // convert all function calls in tail position to proper tail calls
      expr.markTailCalls(cc);

      // dynamic compilation: remove redundant type declarations
      if(callTypes != null) {
        for(int p = 0; p < pl; p++) {
          final Var param = params[p];
          if(callTypes[p].instanceOf(param.seqType(), true)) param.declType = null;
        }
      }
    }
    return null;
  }

  @Override
  public SeqType seqType() {
    return checked ? expr.seqType() : super.seqType();
  }

  /**
   * Returns the minimum arity.
   * @return minimum arity
   */
  public int minArity() {
    return min;
  }

  /**
   * Returns the maximum arity.
   */
  @Override
  public int arity() {
    return params.length;
  }

  @Override
  public QNm funcName() {
    return name;
  }

  @Override
  public QNm paramName(final int pos) {
    return params[pos].name;
  }

  @Override
  public String paramString() {
    return XQFunctionExpr.paramString(this, min);
  }

  @Override
  public FuncType funcType() {
    // refined return type: the body type (via seqType), consistent with call-result typing
    return FuncType.get(anns, declType, params).withRefinedType(seqType());
  }

  @Override
  public int stackFrameSize() {
    return vs.stackSize();
  }

  @Override
  public AnnList annotations() {
    return anns;
  }

  @Override
  public Value invokeInternal(final QueryContext qc, final InputInfo ii, final Value[] args)
      throws QueryException {
    if(!memo) return qc.invoke(params, args, true, expr, simple, null);

    // coerce arguments before computing the key
    final int pl = params.length;
    final Value[] values = new Value[pl];
    for(int p = 0; p < pl; p++) values[p] = params[p].checkType(args[p], qc, null);
    final MemoKey key = new MemoKey(this, values);
    Value value = qc.memo.get(key);
    if(value == null) {
      value = qc.invoke(params, values, false, expr, simple, null);
      // skip placeholders of deferred tail calls
      if(qc.tcFunc == null) qc.memo.put(key, value);
    }
    return value;
  }

  /**
   * Checks if the function can be memoized.
   * @throws QueryException query exception
   */
  void checkMemo() throws QueryException {
    final Ann ann = anns.get(Annotation._BASEX_MEMO);
    if(ann == null || expr == null) return;
    // constructed nodes must not be returned, as cached nodes would have the same identity
    final boolean atomic = declType != null &&
        (declType.zero() || declType.type.instanceOf(BasicType.ANY_ATOMIC_TYPE));
    final String reason = has(Flag.NDT) ? "Function is nondeterministic" :
      !atomic && has(Flag.CNS) ? "Function constructs nodes, atomic return type expected" : null;
    if(reason != null) throw BASEX_ANN2_X_X.get(ann.info, ann, reason);
  }

  /**
   * Checks if the updating semantics are satisfied.
   * @throws QueryException query exception
   */
  void checkUp() throws QueryException {
    // skip already compiled functions
    if(compiled) return;

    final boolean exprUpdating = expr.has(Flag.UPD);
    if(exprUpdating) expr.checkUp();
    final InputInfo ii = expr.info(info);
    if(updating) {
      // updating function
      if(!(exprUpdating || expr.vacuous())) throw UPEXPECTF.get(ii);
      if(declType != null && !declType.zero()) throw UUPFUNCTYPE.get(ii);
    } else if(exprUpdating) {
      // uses updates, but is not declared as such
      throw UPNOT_X.get(ii, description());
    }
  }

  @Override
  public boolean vacuousBody() {
    return declType != null && declType.zero();
  }

  /**
   * Indicates if an expression has one of the specified compiler properties.
   * @param flags flags
   * @return result of check
   * @see Expr#has(Flag...)
   */
  boolean has(final Flag... flags) {
    // function itself does not perform any updates
    final Flag[] flgs = Flag.remove(flags, Flag.UPD);
    return flgs.length != 0 && check(flgs);
  }

  /**
   * Checks if the function body is updating.
   * @return result of check
   * @see Expr#has(Flag...)
   */
  public boolean updating() {
    return updating;
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    visitor.queryLock(() -> {
      final ArrayList<String> list = new ArrayList<>(1);
      for(final Ann ann : anns) {
        if(ann.definition == Annotation._BASEX_LOCK) {
          for(final Item arg : ann.value()) {
            Collections.addAll(list, Locking.queryLocks(((Str) arg).string()));
          }
        }
      }
      return list;
    });

    for(int d = 0; d < defaults.length; d++) {
      if(defaults[d] != null && !dflt(d).accept(visitor)) return false;
    }
    return visitor.declared(params) && visitor.declared(declType) &&
        (expr == null || expr.accept(visitor));
  }

  /**
   * Only called by {@link StaticFuncCall#compile(CompileContext)}.
   * {@inheritDoc}
   */
  @Override
  public Expr inline(final Expr[] exprs, final CompileContext cc) throws QueryException {
    if(memo || !cc.inlineable(anns, expr) || has(Flag.CTX)) return null;
    cc.info(OPTINLINE_X, (Supplier<?>) this::funcLabel);
    return cc.inline(params, exprs, null, expr, null, info);
  }

  @Override
  public String description() {
    return "function declaration";
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name.string()), params, expr);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(DECLARE).token(anns).token(FUNCTION).token(name.prefixId()).params(params);
    if(declType != null && !checked) qs.token(AS).token(declType);
    if(expr != null) qs.brace(expr);
    else qs.token(EXTERNAL);
    qs.token(';');
  }
}
