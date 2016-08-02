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
import org.basex.query.expr.gflwor.GFLWOR.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Inline function.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Leo Woerteler
 */
public final class Closure extends Single implements Scope, XQFunctionExpr {
  /** Function name. */
  private final QNm name;
  /** Arguments. */
  private final Var[] args;
  /** Declared type, {@code null} if not specified. */
  private SeqType type;
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
   * @param type declared return type (can be {@code null})
   * @param args arguments
   * @param expr function body
   * @param anns annotations
   * @param global bindings for non-local variables
   * @param vs scope
   */
  public Closure(final InputInfo info, final SeqType type, final Var[] args, final Expr expr,
      final AnnList anns, final Map<Var, Expr> global, final VarScope vs) {
    this(info, null, type, args, expr, anns, global, vs);
  }

  /**
   * Package-private constructor allowing a name.
   * @param info input info
   * @param name name of the function
   * @param type declared return type (can be {@code null})
   * @param args argument variables
   * @param expr function expression
   * @param anns annotations
   * @param global bindings for non-local variables
   * @param vs variable scope
   */
  Closure(final InputInfo info, final QNm name, final SeqType type, final Var[] args,
      final Expr expr, final AnnList anns, final Map<Var, Expr> global, final VarScope vs) {
    super(info, expr);
    this.name = name;
    this.args = args;
    this.type = type;
    this.anns = anns;
    this.global = global == null ? Collections.<Var, Expr>emptyMap() : global;
    this.vs = vs;
  }

  @Override
  public int arity() {
    return args.length;
  }

  @Override
  public QNm funcName() {
    return name;
  }

  @Override
  public QNm argName(final int pos) {
    return args[pos].name;
  }

  @Override
  public FuncType funcType() {
    return FuncType.get(anns, type, args);
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
    for(final Entry<Var, Expr> e : global.entrySet()) {
      final Expr bound = e.getValue().compile(cc);
      e.setValue(bound);
      e.getKey().refineType(bound.seqType(), cc);
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
    final SeqType r = expr.seqType(), rt = type == null || r.instanceOf(type) ? r : type;
    seqType = FuncType.get(anns, rt, args).seqType();
    size = 1;

    cc.pushScope(vs);
    try {
      // inline all values in the closure
      final Iterator<Entry<Var, Expr>> cls = global.entrySet().iterator();
      Map<Var, Expr> add = null;
      final int limit = cc.qc.context.options.get(MainOptions.INLINELIMIT);
      while(cls.hasNext()) {
        final Entry<Var, Expr> e = cls.next();
        final Var v = e.getKey();
        final Expr c = e.getValue();
        if(c instanceof Value) {
          // values are always inlined into the closure
          final Expr inlined = expr.inline(v, v.checkType((Value) c, cc.qc, true), cc);
          if(inlined != null) expr = inlined;
          cls.remove();
        } else if(c instanceof Closure) {
          // nested closures are inlined if their size and number of closed-over variables is small
          final Closure cl = (Closure) c;
          if(!cl.has(Flag.NDT) && !cl.has(Flag.UPD) && cl.global.size() < 5
              && expr.count(v) != VarUsage.MORE_THAN_ONCE && cl.exprSize() < limit) {
            cc.info(OPTINLINE_X, e);
            for(final Entry<Var, Expr> e2 : cl.global.entrySet()) {
              final Var v2 = cc.copy(e2.getKey(), null);
              if(add == null) add = new HashMap<>();
              add.put(v2, e2.getValue());
              e2.setValue(new VarRef(cl.info, v2));
            }

            final Expr inlined = expr.inline(v, cl, cc);
            if(inlined != null) expr = inlined;
            cls.remove();
          }
        }
      }

      // add all newly added bindings
      if(add != null) global.putAll(add);
    } catch(final QueryException qe) {
      expr = cc.error(qe, expr);
    } finally {
      cc.removeScope(this);
    }

    // only evaluate if the closure is empty, so we don't lose variables
    return global.isEmpty() ? preEval(cc) : this;
  }

  @Override
  public VarUsage count(final Var var) {
    VarUsage all = VarUsage.NEVER;
    for(final Entry<Var, Expr> e : global.entrySet()) {
      if((all = all.plus(e.getValue().count(var))) == VarUsage.MORE_THAN_ONCE) break;
    }
    return all;
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    boolean change = false;
    for(final Entry<Var, Expr> entry : global.entrySet()) {
      final Expr e = entry.getValue().inline(var, ex, cc);
      if(e != null) {
        change = true;
        entry.setValue(e);
      }
    }

    return change ? optimize(cc) : null;
  }

  @Override
  public Expr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    cc.pushScope(new VarScope(vs.sc));
    try {
      vs.copy(cc, vm);
      final HashMap<Var, Expr> nl = new HashMap<>();
      for(final Entry<Var, Expr> e : global.entrySet()) {
        nl.put(vm.get(e.getKey().id), e.getValue().copy(cc, vm));
      }
      final Var[] vars = args.clone();
      final int vl = vars.length;
      for(int v = 0; v < vl; v++) vars[v] = vm.get(vars[v].id);

      final Expr e = expr.copy(cc, vm);
      e.markTailCalls(null);
      return copyType(new Closure(info, name, type, vars, e, anns, nl, cc.vs()));
    } finally {
      cc.removeScope();
    }
  }

  @Override
  public Expr inlineExpr(final Expr[] exprs, final CompileContext cc, final InputInfo ii)
      throws QueryException {

    if(expr.has(Flag.CTX)) return null;

    cc.info(OPTINLINE_X, this);
    // create let bindings for all variables
    final LinkedList<Clause> cls =
        exprs.length == 0 && global.isEmpty() ? null : new LinkedList<Clause>();
    final IntObjMap<Var> vm = new IntObjMap<>();
    final int al = args.length;
    for(int a = 0; a < al; a++) {
      cls.add(new Let(cc.copy(args[a], vm), exprs[a], false).optimize(cc));
    }
    for(final Entry<Var, Expr> e : global.entrySet()) {
      cls.add(new Let(cc.copy(e.getKey(), vm), e.getValue(), false).optimize(cc));
    }

    // copy the function body
    final Expr cpy = expr.copy(cc, vm), rt = type == null ? cpy :
      new TypeCheck(vs.sc, ii, cpy, type, true).optimize(cc);

    return cls == null ? rt : new GFLWOR(ii, cls, rt).optimize(cc);
  }

  @Override
  public FuncItem item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Type tp = seqType().type;
    if(!(tp instanceof FuncType)) throw Util.notExpected("Closure was not compiled: %", this);
    final FuncType ft = (FuncType) tp;

    final Expr body;
    if(global.isEmpty()) {
      body = expr;
    } else {
      // collect closure
      final LinkedList<Clause> cls = new LinkedList<>();
      for(final Entry<Var, Expr> e : global.entrySet())
        cls.add(new Let(e.getKey(), e.getValue().value(qc), false));
      body = new GFLWOR(info, cls, expr);
    }

    final SeqType argType = body.seqType();
    final Expr checked;
    if(type == null || argType.instanceOf(type)) {
      // return type is already correct
      checked = body;
    } else if(body instanceof FuncItem && type.type instanceof FuncType) {
      // function item coercion
      if(!type.occ.check(1)) throw QueryError.typeError(body, type, null, info);
      final FuncItem fi = (FuncItem) body;
      checked = fi.coerceTo((FuncType) type.type, qc, info, true);
    } else if(body.isValue()) {
      // we can type check immediately
      final Value val = (Value) body;
      checked = type.instance(val) ? val : type.promote(val, null, qc, vs.sc, info, false);
    } else {
      // check at each call
      if(argType.type.instanceOf(type.type) && !body.has(Flag.NDT) && !body.has(Flag.UPD)) {
        // reject impossible arities
        final Occ occ = argType.occ.intersect(type.occ);
        if(occ == null) throw QueryError.typeError(body, type, null, info);
      }
      checked = new TypeCheck(vs.sc, info, body, type, true);
    }

    return new FuncItem(vs.sc, anns, name, args, ft, checked, vs.stackSize());
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return item(qc, info);
  }

  @Override
  public ValueIter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public boolean has(final Flag flag) {
    // handle recursive calls: set dummy value, eventually replace it with final value
    Boolean b = map.get(flag);
    if(b == null) {
      map.put(flag, false);
      // function itself does not perform any updates
      b = flag != Flag.UPD && expr.has(flag);
      map.put(flag, b);
    }
    return b;
  }

  @Override
  public boolean removable(final Var var) {
    for(final Entry<Var, Expr> e : global.entrySet())
      if(!e.getValue().removable(var)) return false;
    return true;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem();
    for(final Entry<Var, Expr> e : global.entrySet()) {
      e.getKey().plan(el);
      e.getValue().plan(el);
    }
    addPlan(plan, el, expr);
    final int al = args.length;
    for(int a = 0; a < al; a++) el.add(planAttr(ARG + a, args[a].name.string()));
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    for(final Entry<Var, Expr> v : global.entrySet())
      if(!(v.getValue().accept(visitor) && visitor.declared(v.getKey()))) return false;
    for(final Var v : args) if(!visitor.declared(v)) return false;
    return expr.accept(visitor);
  }

  @Override
  public void checkUp() throws QueryException {
    checkUpdating();
    if(updating) {
      expr.checkUp();
      if(type != null && !type.eq(SeqType.EMP)) throw UUPFUNCTYPE.get(info);
    }
  }

  @Override
  public boolean isVacuous() {
    return type != null && type.eq(SeqType.EMP) && !has(Flag.UPD);
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    for(final Entry<Var, Expr> e : global.entrySet())
      if(!e.getValue().accept(visitor)) return false;
    return visitor.inlineFunc(this);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Entry<Var, Expr> e : global.entrySet())
      sz += e.getValue().exprSize();
    return sz + expr.exprSize();
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
    final int al = args.length;
    for(int a = 0; a < al; a++) args[a].type = ft.argTypes[a];
    if(ft.type != null && !ft.type.eq(SeqType.ITEM_ZM)) type = ft.type;
  }

  /**
   * Assigns the updating flag.
   */
  private void checkUpdating() {
    // derive updating flag from function body
    updating = expr.has(Flag.UPD);
    if(!updating) anns.delete(Annotation.UPDATING);
    else if(!anns.contains(Annotation.UPDATING)) anns.add(new Ann(info, Annotation.UPDATING));
  }

  /**
   * Creates a function literal for a function that was not yet encountered while parsing.
   * @param name function name
   * @param arity function arity
   * @param qc query context
   * @param sc static context
   * @param info input info
   * @return function literal
   * @throws QueryException query exception
   */
  public static Closure unknownLit(final QNm name, final int arity, final QueryContext qc,
      final StaticContext sc, final InputInfo info) throws QueryException {

    final VarScope scp = new VarScope(sc);
    final Var[] arg = new Var[arity];
    final Expr[] refs = new Expr[arity];
    for(int a = 0; a < arity; a++) {
      arg[a] = scp.addNew(new QNm(ARG + (a + 1), ""), null, true, qc, info);
      refs[a] = new VarRef(info, arg[a]);
    }
    final TypedFunc call = qc.funcs.getFuncRef(name, refs, sc, info);
    return new Closure(info, name, SeqType.ITEM_ZM, arg, call.fun, new AnnList(), null, scp);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(!global.isEmpty()) {
      sb.append("((: inline-closure :) ");
      for(final Entry<Var, Expr> e : global.entrySet()) {
        sb.append("let ").append(e.getKey()).append(" := ").append(e.getValue()).append(' ');
      }
      sb.append(RETURN).append(' ');
    }
    sb.append(FUNCTION).append(PAREN1);
    final int al = args.length;
    for(int a = 0; a < al; a++) {
      if(a > 0) sb.append(", ");
      sb.append(args[a]);
    }
    sb.append(PAREN2).append(' ');
    if(type != null) sb.append("as ").append(type).append(' ');
    sb.append("{ ").append(expr).append(" }");
    if(!global.isEmpty()) sb.append(')');
    return sb.toString();
  }
}
