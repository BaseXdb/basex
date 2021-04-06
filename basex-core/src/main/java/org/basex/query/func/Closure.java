package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.Map.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Inline function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class Closure extends Single implements Scope, XQFunctionExpr {
  /** Function name, {@code null} if not specified. */
  private final QNm name;
  /** Formal parameters. */
  private final Var[] params;
  /** Value type, {@code null} if not specified. */
  private SeqType declType;
  /** Annotations. */
  private AnnList anns;
  /** Updating flag. */
  private boolean updating;

  /** Map with requested function properties. */
  private final EnumMap<Flag, Boolean> map = new EnumMap<>(Flag.class);
  /** Compilation flag. */
  private boolean compiled;

  /** Local variables in the scope of this function. */
  private final VarScope vs;
  /** Non-local variable bindings. */
  private final Map<Var, Expr> global;

  /**
   * Constructor.
   * @param info input info
   * @param declType declared type (can be {@code null})
   * @param params formal parameters
   * @param expr function body
   * @param anns annotations
   * @param global bindings for non-local variables
   * @param vs scope
   */
  public Closure(final InputInfo info, final SeqType declType, final Var[] params, final Expr expr,
      final AnnList anns, final Map<Var, Expr> global, final VarScope vs) {
    this(info, null, declType, params, expr, anns, global, vs);
  }

  /**
   * Package-private constructor allowing a name.
   * @param info input info
   * @param name name of the function (can be {@code null})
   * @param declType declared type (can be {@code null})
   * @param params formal parameters
   * @param expr function expression
   * @param anns annotations
   * @param global bindings for non-local variables (can be {@code null})
   * @param vs variable scope
   */
  Closure(final InputInfo info, final QNm name, final SeqType declType, final Var[] params,
      final Expr expr, final AnnList anns, final Map<Var, Expr> global, final VarScope vs) {
    super(info, expr, SeqType.FUNCTION_O);
    this.name = name;
    this.params = params;
    this.declType = declType == null || declType.eq(SeqType.ITEM_ZM) ? null : declType;
    this.anns = anns;
    this.global = global == null ? Collections.emptyMap() : global;
    this.vs = vs;
  }

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
  public FuncType funcType() {
    final FuncType type = super.funcType();
    return type != null ? type : FuncType.get(anns, declType, params);
  }

  @Override
  public AnnList annotations() {
    return anns;
  }

  @Override
  public void comp(final CompileContext cc) throws QueryException {
    compile(cc);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    if(compiled) return this;
    compiled = true;

    checkUpdating();

    // compile closure
    for(final Entry<Var, Expr> entry : global.entrySet()) {
      final Expr bound = entry.getValue().compile(cc);
      entry.setValue(bound);
      entry.getKey().refineType(bound.seqType(), cc);
    }

    cc.pushScope(vs);
    try {
      expr = expr.compile(cc);
    } catch(final QueryException qe) {
      expr = cc.error(qe, expr);
    } finally {
      cc.removeScope(this);
    }

    // convert all function calls in tail position to proper tail calls
    expr.markTailCalls(cc);

    return optimize(cc);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    cc.pushScope(vs);
    try {
      // inline all values in the closure
      final Iterator<Entry<Var, Expr>> iter = global.entrySet().iterator();
      Map<Var, Expr> add = null;
      final int limit = cc.qc.context.options.get(MainOptions.INLINELIMIT);
      while(iter.hasNext()) {
        final Entry<Var, Expr> entry = iter.next();
        final Var var = entry.getKey();
        final Expr ex = entry.getValue();

        Expr inline = null;
        if(ex instanceof Value) {
          // values are always inlined into the closure
          inline = var.checkType((Value) ex, cc.qc, true);
        } else if(ex instanceof Closure) {
          // nested closures are inlined if their size and number of closed-over variables is small
          final Closure cl = (Closure) ex;
          if(!cl.has(Flag.NDT) && cl.global.size() < 5
              && expr.count(var) != VarUsage.MORE_THAN_ONCE && cl.exprSize() < limit) {
            cc.info(OPTINLINE_X, entry);
            for(final Entry<Var, Expr> expr2 : cl.global.entrySet()) {
              final Var var2 = cc.copy(expr2.getKey(), null);
              if(add == null) add = new HashMap<>();
              add.put(var2, expr2.getValue());
              expr2.setValue(new VarRef(cl.info, var2).optimize(cc));
            }
            inline = cl;
          }
        }
        if(inline != null) {
          expr = new InlineContext(var, inline, cc).inline(expr);
          iter.remove();
        }
      }
      // add all newly added bindings
      if(add != null) global.putAll(add);
    } catch(final QueryException qe) {
      expr = cc.error(qe, expr);
    } finally {
      cc.removeScope(this);
    }

    final SeqType st = expr.seqType();
    final SeqType dt = declType == null || st.instanceOf(declType) ? st : declType;
    exprType.assign(FuncType.get(anns, dt, params));

    // only evaluate if:
    // - the closure is empty, so we don't lose variables
    // - the result size does not exceed a specific limit
    return global.isEmpty() && expr.size() <= CompileContext.MAX_PREEVAL ?
      cc.preEval(this) : this;
  }

  @Override
  public VarUsage count(final Var var) {
    VarUsage all = VarUsage.NEVER;
    for(final Expr ex : global.values()) {
      if((all = all.plus(ex.count(var))) == VarUsage.MORE_THAN_ONCE) break;
    }
    return all;
  }

  @Override
  public Expr inline(final InlineContext ic) throws QueryException {
    boolean changed = false;
    for(final Entry<Var, Expr> entry : global.entrySet()) {
      final Expr inlined = entry.getValue().inline(ic);
      if(inlined != null) {
        changed = true;
        entry.setValue(inlined);
      }
    }
    if(!changed) return null;

    // invalidate cached flags, optimize closure
    map.clear();
    return optimize(ic.cc);
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    final VarScope innerScope = new VarScope(vs.sc);

    final HashMap<Var, Expr> outer = new HashMap<>();
    global.forEach((key, value) -> outer.put(key, value.copy(cc, vm)));

    cc.pushScope(innerScope);
    try {
      final IntObjMap<Var> innerVars = new IntObjMap<>();
      vs.copy(cc, innerVars);

      final HashMap<Var, Expr> nl = new HashMap<>();
      outer.forEach((key, value) -> nl.put(innerVars.get(key.id), value));

      final Var[] prms = params.clone();
      final int pl = prms.length;
      for(int p = 0; p < pl; p++) prms[p] = innerVars.get(prms[p].id);

      final Expr ex = expr.copy(cc, innerVars);
      ex.markTailCalls(null);
      return copyType(new Closure(info, name, declType, prms, ex, anns, nl, cc.vs()));
    } finally {
      cc.removeScope();
    }
  }

  @Override
  public Expr inline(final Expr[] exprs, final CompileContext cc) throws QueryException {
    if(!StaticFunc.inline(cc, anns, expr) || expr.has(Flag.CTX)) return null;

    cc.info(OPTINLINE_X, this);

    // create let bindings for all variables
    final LinkedList<Clause> clauses = new LinkedList<>();
    final IntObjMap<Var> vm = new IntObjMap<>();
    final int pl = params.length;
    for(int p = 0; p < pl; p++) {
      clauses.add(new Let(cc.copy(params[p], vm), exprs[p]).optimize(cc));
    }
    for(final Entry<Var, Expr> entry : global.entrySet()) {
      clauses.add(new Let(cc.copy(entry.getKey(), vm), entry.getValue()).optimize(cc));
    }

    // create the return clause
    final Expr body = expr.copy(cc, vm).optimize(cc);
    final Expr rtrn = declType == null ? body :
      new TypeCheck(vs.sc, info, body, declType, true).optimize(cc);
    return clauses.isEmpty() ? rtrn : new GFLWOR(info, clauses, rtrn).optimize(cc);
  }

  @Override
  public FuncItem item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr body;
    if(global.isEmpty()) {
      body = expr;
    } else {
      // collect closure
      final LinkedList<Clause> clauses = new LinkedList<>();
      for(final Entry<Var, Expr> entry : global.entrySet()) {
        clauses.add(new Let(entry.getKey(), entry.getValue().value(qc)));
      }
      body = new GFLWOR(info, clauses, expr);
    }

    final SeqType argType = body.seqType();
    final Expr checked;
    if(declType == null || argType.instanceOf(declType)) {
      // return type is already correct
      checked = body;
    } else if(body instanceof FuncItem && declType.type instanceof FuncType) {
      // function item coercion
      if(!declType.occ.check(1)) throw typeError(body, declType, null, info, true);
      checked = ((FuncItem) body).coerceTo((FuncType) declType.type, qc, info, true);
    } else if(body instanceof Value) {
      // we can type check immediately
      final Value value = (Value) body;
      checked = declType.instance(value) ? value :
        declType.promote(value, null, qc, vs.sc, info, false);
    } else {
      // check at each call: reject impossible arities
      if(argType.type.instanceOf(declType.type) && argType.occ.intersect(declType.occ) == null &&
        !body.has(Flag.NDT)) throw typeError(body, declType, null, info, true);

      checked = new TypeCheck(vs.sc, info, body, declType, true);
    }

    final FuncType type = (FuncType) seqType().type;
    return new FuncItem(vs.sc, anns, name, params, type, checked, vs.stackSize(), info);
  }

  @Override
  public boolean has(final Flag... flags) {
    // closure does not perform any updates
    if(Flag.UPD.in(flags)) return false;

    // handle recursive calls: check which flags are already or currently assigned
    final ArrayList<Flag> flgs = new ArrayList<>();
    for(final Flag flag : flags) {
      if(!map.containsKey(flag)) {
        map.put(flag, Boolean.FALSE);
        flgs.add(flag);
      }
    }
    // request missing properties
    for(final Flag flag : flgs) {
      boolean f = false;
      for(final Expr ex : global.values()) f = f || ex.has(flag);
      map.put(flag, f || expr.has(flag));
    }

    // evaluate result
    for(final Flag flag : flags) {
      if(map.get(flag)) return true;
    }
    return false;
  }

  @Override
  public boolean inlineable(final InlineContext ic) {
    for(final Expr ex : global.values()) {
      if(!ex.inlineable(ic)) return false;
    }
    return true;
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    for(final Entry<Var, Expr> entry : global.entrySet()) {
      if(!(entry.getValue().accept(visitor) && visitor.declared(entry.getKey()))) return false;
    }
    for(final Var var : params) {
      if(!visitor.declared(var)) return false;
    }
    return expr.accept(visitor);
  }

  @Override
  public void checkUp() throws QueryException {
    checkUpdating();
    if(updating) {
      expr.checkUp();
      if(declType != null && !declType.zero()) throw UUPFUNCTYPE.get(info);
    }
  }

  @Override
  public boolean vacuous() {
    return declType != null && declType.zero() && !has(Flag.UPD);
  }

  @Override
  public boolean vacuousBody() {
    return vacuous();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    for(final Expr ex : global.values()) {
      if(!ex.accept(visitor)) return false;
    }
    return visitor.inlineFunc(this);
  }

  @Override
  public int exprSize() {
    int size = 1;
    for(final Expr ex : global.values()) size += ex.exprSize();
    return size + expr.exprSize();
  }

  @Override
  public boolean compiled() {
    return compiled;
  }

  /**
   * Returns an iterator over the non-local bindings of this closure.
   * @return the iterator
   */
  public Iterator<Entry<Var, Expr>> globalBindings() {
    return global.entrySet().iterator();
  }

  /**
   * Fixes the function type of this closure after it was generated for a function literal during
   * parsing.
   * @param ft function type
   */
  void adoptSignature(final FuncType ft) {
    anns = ft.anns;
    final int pl = params.length;
    for(int p = 0; p < pl; p++) params[p].declType = ft.argTypes[p];
    final SeqType dt = ft.declType;
    if(!dt.eq(SeqType.ITEM_ZM)) declType = dt;
  }

  /**
   * Assigns the updating flag.
   * @throws QueryException query exception
   */
  private void checkUpdating() throws QueryException {
    // derive updating flag from function body
    updating = expr.has(Flag.UPD);
    final boolean updAnn = anns.contains(Annotation.UPDATING);
    if(updating != updAnn) {
      if(!updAnn) anns.add(new Ann(info, Annotation.UPDATING));
      else if(!expr.vacuous()) throw UPEXPECTF.get(info);
    }
  }

  /**
   * Creates a function literal for a function that was not yet encountered while parsing.
   * @param name function name
   * @param arity function arity
   * @param qc query context
   * @param sc static context
   * @param ii input info
   * @return function literal
   * @throws QueryException query exception
   */
  public static Closure undeclaredLiteral(final QNm name, final int arity, final QueryContext qc,
      final StaticContext sc, final InputInfo ii) throws QueryException {

    final VarScope vs = new VarScope(sc);
    final Var[] params = new Var[arity];
    final Expr[] args = new Expr[arity];
    for(int a = 0; a < arity; a++) {
      params[a] = vs.addNew(new QNm(ARG + (a + 1), ""), null, true, qc, ii);
      args[a] = new VarRef(ii, params[a]);
    }
    final TypedFunc tf = qc.funcs.undeclaredFuncCall(name, args, sc, ii);
    return new Closure(ii, name, null, params, tf.func, new AnnList(), null, vs);
  }

  @Override
  public boolean equals(final Object obj) {
    // [CG] could be enhanced
    return this == obj;
  }

  @Override
  public void plan(final QueryPlan plan) {
    final ArrayList<Object> list = new ArrayList<>();
    global.forEach((key, value) -> {
      list.add(key);
      list.add(value);
    });

    final FElem elem = plan.create(this);
    final int pl = params.length;
    for(int p = 0; p < pl; p++) plan.addAttribute(elem, ARG + p, params[p].name.string());
    plan.add(elem, list.toArray());
  }

  @Override
  public void plan(final QueryString qs) {
    final boolean inlined = !global.isEmpty();
    if(inlined) {
      qs.token("((: inline-closure :)");
      global.forEach((k, v) -> qs.token(LET).token(k).token(ASSIGN).token(v));
      qs.token(RETURN);
    }
    qs.token(FUNCTION).params(params);
    qs.token(AS).token(declType != null ? declType : SeqType.ITEM_ZM).brace(expr);
    if(inlined) qs.token(')');
  }
}
