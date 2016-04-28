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
import org.basex.query.func.fn.*;
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
 * @author BaseX Team 2005-16, BSD License
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
   * @param type declared return type
   * @param expr function body
   * @param doc current xqdoc cache
   * @param sc static context
   * @param scope variable scope
   * @param info input info
   */
  StaticFunc(final AnnList anns, final QNm name, final Var[] args, final SeqType type,
      final Expr expr, final String doc, final StaticContext sc, final VarScope scope,
      final InputInfo info) {

    super(sc, anns, name, type, scope, doc, info);
    this.args = args;
    this.expr = expr;
    updating = anns.contains(Annotation.UPDATING);
  }

  @Override
  public void compile(final QueryContext qc) {
    if(compiled || expr == null) return;
    compiling = compiled = true;

    final Value cv = qc.value;
    qc.value = null;

    try {
      expr = expr.compile(qc, scope);

      if(type != null) {
        // remove redundant casts
        if((type.type == AtomType.BLN || type.type == AtomType.FLT ||
            type.type == AtomType.DBL || type.type == AtomType.QNM ||
            type.type == AtomType.URI) && type.eq(expr.seqType())) {
          qc.compInfo(OPTTYPE_X, type);
        } else {
          expr = new TypeCheck(sc, info, expr, type, true).optimize(qc, scope);
        }
      }
    } catch(final QueryException qe) {
      expr = FnError.get(qe, expr.seqType(), sc);
    } finally {
      scope.cleanUp(this);
      qc.value = cv;
    }

    // convert all function calls in tail position to proper tail calls
    expr.markTailCalls(qc);

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
    return scope.stackSize();
  }

  @Override
  public AnnList annotations() {
    return anns;
  }

  @Override
  public Item invItem(final QueryContext qc, final InputInfo ii, final Value... arg)
      throws QueryException {

    // reset context and evaluate function
    final Value cv = qc.value;
    qc.value = null;
    try {
      final int al = args.length;
      for(int a = 0; a < al; a++) qc.set(args[a], arg[a]);
      return expr.item(qc, ii);
    } finally {
      qc.value = cv;
    }
  }

  @Override
  public Value invValue(final QueryContext qc, final InputInfo ii, final Value... arg)
      throws QueryException {

    // reset context and evaluate function
    final Value cv = qc.value;
    qc.value = null;
    try {
      final int al = args.length;
      for(int a = 0; a < al; a++) qc.set(args[a], arg[a]);
      return qc.value(expr);
    } finally {
      qc.value = cv;
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
    final boolean u = expr.has(Flag.UPD);
    if(u) expr.checkUp();
    final InputInfo ii = expr instanceof ParseExpr ? ((ParseExpr) expr).info : info;
    if(updating) {
      // updating function
      if(type != null && !type.eq(SeqType.EMP)) throw UUPFUNCTYPE.get(info);
      if(!u && !expr.isVacuous()) throw UPEXPECTF.get(ii);
    } else if(u) {
      // uses updates, but is not declared as such
      throw UPNOT_X.get(ii, description());
    }
  }

  /**
   * Checks if this function returns vacuous results (see {@link Expr#isVacuous()}).
   * @return result of check
   */
  public boolean isVacuous() {
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
  public Expr inlineExpr(final Expr[] exprs, final QueryContext qc, final VarScope scp,
      final InputInfo ii) throws QueryException {

    if(!inline(qc, anns, expr) || has(Flag.CTX) || compiling || selfRecursive()) return null;
    qc.compInfo(OPTINLINE_X, id());

    // create let bindings for all variables
    final LinkedList<Clause> cls = exprs.length == 0 ? null : new LinkedList<Clause>();
    final IntObjMap<Var> vs = new IntObjMap<>();
    final int al = args.length;
    for(int a = 0; a < al; a++) {
      final Var old = args[a], v = scp.addCopy(old, qc);
      vs.put(old.id, v);
      cls.add(new Let(v, exprs[a], false).optimize(qc, scp));
    }

    // copy the function body
    final Expr cpy = expr.copy(qc, scp, vs);
    return cls == null ? cpy : new GFLWOR(info, cls, cpy).optimize(qc, scp);
  }

  /**
   * Checks if inlining conditions are given.
   * @param qc query context
   * @param anns annotations
   * @param expr expression
   * @return result of check
   */
  public static boolean inline(final QueryContext qc, final AnnList anns, final Expr expr) {
    final Ann ann = anns.get(Annotation._BASEX_INLINE);
    final long limit;
    if(ann == null) {
      limit = qc.context.options.get(MainOptions.INLINELIMIT);;
    } else {
      final Item[] args = ann.args();
      limit = args.length > 0 ? ((ANum) args[0]).itr() : Long.MAX_VALUE;
    }
    return expr.isValue() || expr.exprSize() < limit;
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
