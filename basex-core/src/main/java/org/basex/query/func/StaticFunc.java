package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.Expr.Flag;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.gflwor.GFLWOR.Clause;
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
 * A static user-defined function.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class StaticFunc extends StaticDecl implements XQFunction {
  /** Arguments. */
  public final Var[] args;
  /** Updating flag. */
  final boolean updating;

  /** Map with requested function properties. */
  private final EnumMap<Flag, Boolean> map = new EnumMap<>(Flag.class);
  /** Flag that is turned on during compilation and prevents premature inlining. */
  private boolean compiling;

  /**
   * Function constructor.
   * @param anns annotations
   * @param name function name
   * @param args arguments
   * @param type declared return type (can be {@code null})
   * @param expr function body (can be {@code null})
   * @param doc xqdoc string
   * @param vs variable scope
   * @param info input info
   */
  StaticFunc(final AnnList anns, final QNm name, final Var[] args, final SeqType type,
      final Expr expr, final String doc, final VarScope vs, final InputInfo info) {
    super(anns, name, type, vs, doc, info);
    this.args = args;
    this.expr = expr;
    updating = anns.contains(Annotation.UPDATING);
  }

  @Override
  public void comp(final CompileContext cc) {
    if(compiled || expr == null) return;
    compiling = compiled = true;

    final QueryFocus focus = cc.qc.focus;
    final Value cv = focus.value;
    focus.value = null;

    cc.pushScope(vs);
    try {
      expr = expr.compile(cc);

      if(type != null) {
        // remove redundant casts
        if((type.type == AtomType.BLN || type.type == AtomType.FLT ||
            type.type == AtomType.DBL || type.type == AtomType.QNM ||
            type.type == AtomType.URI) && type.eq(expr.seqType())) {
          cc.info(OPTTYPE_X, type);
        } else {
          expr = new TypeCheck(sc, info, expr, type, true).optimize(cc);
        }
      }
    } catch(final QueryException qe) {
      expr = cc.error(qe, expr);
    } finally {
      cc.removeScope(this);
      focus.value = cv;
    }

    // convert all function calls in tail position to proper tail calls
    expr.markTailCalls(cc);

    compiling = false;
  }

  @Override
  public void plan(final FElem plan) {
    final FElem el = planElem(NAM, name.string());
    addPlan(plan, el, expr);
    final int al = args.length;
    for(int a = 0; a < al; ++a) el.add(planAttr(ARG + a, args[a].name.string()));
  }

  /**
   * Checks if this function calls itself recursively.
   * @return result of check
   */
  private boolean selfRecursive() {
    return !expr.accept(new ASTVisitor() {
      @Override
      public boolean staticFuncCall(final StaticFuncCall call) {
        return call.func != StaticFunc.this;
      }

      @Override
      public boolean inlineFunc(final Scope sub) {
        return sub.visit(this);
      }
    });
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
  public int stackFrameSize() {
    return vs.stackSize();
  }

  @Override
  public AnnList annotations() {
    return anns;
  }

  @Override
  public Item invItem(final QueryContext qc, final InputInfo ii, final Value... arg)
      throws QueryException {

    // reset context and evaluate function
    final QueryFocus qf = qc.focus;
    final Value cv = qf.value;
    qf.value = null;
    try {
      final int al = args.length;
      for(int a = 0; a < al; a++) qc.set(args[a], arg[a]);
      return expr.item(qc, ii);
    } finally {
      qf.value = cv;
    }
  }

  @Override
  public Value invValue(final QueryContext qc, final InputInfo ii, final Value... arg)
      throws QueryException {

    // reset context and evaluate function
    final QueryFocus qf = qc.focus;
    final Value cv = qf.value;
    qf.value = null;
    try {
      final int al = args.length;
      for(int a = 0; a < al; a++) qc.set(args[a], arg[a]);
      return qc.value(expr);
    } finally {
      qf.value = cv;
    }
  }

  @Override
  public Value invokeValue(final QueryContext qc, final InputInfo ii, final Value... arg)
      throws QueryException {
    return FuncCall.value(this, arg, qc, ii);
  }

  @Override
  public Item invokeItem(final QueryContext qc, final InputInfo ii, final Value... arg)
      throws QueryException {
    return FuncCall.item(this, arg, qc, ii);
  }

  /**
   * Checks if the updating semantics are satisfied.
   * @throws QueryException query exception
   */
  void checkUp() throws QueryException {
    final boolean upd = expr.has(Flag.UPD);
    if(upd) expr.checkUp();
    final InputInfo ii = expr instanceof ParseExpr ? ((ParseExpr) expr).info : info;
    if(updating) {
      // updating function
      if(type != null && !type.eq(SeqType.EMP)) throw UUPFUNCTYPE.get(info);
      if(!upd && !expr.isVacuous()) throw UPEXPECTF.get(ii);
    } else if(upd) {
      // uses updates, but is not declared as such
      throw UPNOT_X.get(ii, description());
    }
  }

  @Override
  public boolean isVacuousBody() {
    return type != null && type.eq(SeqType.EMP) && !has(Flag.UPD);
  }

  /**
   * Indicates if an expression has the specified compiler property.
   * @param flag feature
   * @return result of check
   * @see Expr#has(Flag)
   */
  boolean has(final Flag flag) {
    // function itself does not perform any updates
    return flag != Flag.UPD && check(flag);
  }

  /**
   * Checks if the function body is updating.
   * @return result of check
   * @see Expr#has(Flag)
   */
  boolean updating() {
    // MIXUPDATES: recursive check; otherwise, rely on flag (GH-1281)
    return sc.mixUpdates ? check(Flag.UPD) : updating;
  }

  /**
   * Checks if the function body is updating.
   * @param flag feature
   * @return result of check
   */
  private boolean check(final Flag flag) {
    // handle recursive calls: set dummy value, eventually replace it with final value
    if(!map.containsKey(flag)) {
      map.put(flag, false);
      map.put(flag, expr.has(flag));
    }
    return map.get(flag);
  }



  @Override
  public boolean visit(final ASTVisitor visitor) {
    for(final Var v : args) if(!visitor.declared(v)) return false;
    return expr == null || expr.accept(visitor);
  }

  @Override
  public byte[] id() {
    return StaticFuncs.sig(name, args.length);
  }

  @Override
  public Expr inlineExpr(final Expr[] exprs, final CompileContext cc, final InputInfo ii)
      throws QueryException {

    if(!inline(cc, anns, expr) || has(Flag.CTX) || compiling || selfRecursive()) return null;
    cc.info(OPTINLINE_X, id());

    // create let bindings for all variables
    final LinkedList<Clause> cls = exprs.length == 0 ? null : new LinkedList<>();
    final IntObjMap<Var> vars = new IntObjMap<>();
    final int al = args.length;
    for(int a = 0; a < al; a++) {
      cls.add(new Let(cc.copy(args[a], vars), exprs[a], false).optimize(cc));
    }

    // copy the function body
    final Expr cpy = expr.copy(cc, vars);
    return cls == null ? cpy : new GFLWOR(info, cls, cpy).optimize(cc);
  }

  /**
   * Checks if inlining conditions are given.
   * @param cc compilation context
   * @param anns annotations
   * @param expr expression
   * @return result of check
   */
  public static boolean inline(final CompileContext cc, final AnnList anns, final Expr expr) {
    final Ann ann = anns.get(Annotation._BASEX_INLINE);
    final long limit;
    if(ann == null) {
      limit = cc.qc.context.options.get(MainOptions.INLINELIMIT);
    } else {
      final Item[] args = ann.args();
      limit = args.length > 0 ? ((ANum) args[0]).itr() : Long.MAX_VALUE;
    }
    return expr.isValue() || expr.exprSize() < limit;
  }

  @Override
  public String description() {
    return "Function declaration";
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(DECLARE).add(' ').addExt(anns);
    tb.add(FUNCTION).add(' ').add(name.string());
    tb.add(PAREN1).addSep(args, SEP).add(PAREN2);
    if(type != null) tb.add(' ' + AS + ' ' + type);
    if(expr != null) tb.add(" { ").addExt(expr).add(" }; ");
    else tb.add(" external; ");
    return tb.toString();
  }
}
