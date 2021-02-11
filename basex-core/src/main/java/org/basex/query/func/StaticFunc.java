package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.core.locks.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * A static user-defined function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class StaticFunc extends StaticDecl implements XQFunction {
  /** Formal parameters. */
  public final Var[] params;
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
   * @param params formal parameters
   * @param type declared return type (can be {@code null})
   * @param expr function body (can be {@code null})
   * @param doc xqdoc string
   * @param vs variable scope
   * @param info input info
   */
  StaticFunc(final AnnList anns, final QNm name, final Var[] params, final SeqType type,
      final Expr expr, final String doc, final VarScope vs, final InputInfo info) {
    super(anns, name, type, vs, doc, info);
    this.params = params;
    this.expr = expr;
    updating = anns.contains(Annotation.UPDATING);
  }

  @Override
  public void comp(final CompileContext cc) {
    if(compiled || expr == null) return;
    compiling = compiled = true;

    // compile function body, handle return type
    cc.pushFocus(null);
    cc.pushScope(vs);
    try {
      expr = expr.compile(cc);
      if(declType != null) expr = new TypeCheck(sc, info, expr, declType, true).optimize(cc);
    } catch(final QueryException qe) {
      expr = cc.error(qe, expr);
    } finally {
      cc.removeScope(this);
      cc.removeFocus();
    }

    // convert all function calls in tail position to proper tail calls
    expr.markTailCalls(cc);
    compiling = false;
  }

  /**
   * Optimize the static function.
   * @param cc compilation context
   */
  public void optimize(final CompileContext cc) {
    // check function calls, drop superfluous type checks
    final SeqType[] seqTypes = cc.qc.funcs.seqTypes(this);
    if(seqTypes != null) {
      final int pl = arity();
      for(int p = 0; p < pl; p++) {
        if(seqTypes[p].instanceOf(params[p].seqType())) {
          cc.info(OPTTYPE_X, params[p]);
          params[p].declType = null;
        }
      }
    }
    declType = null;
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
      public boolean inlineFunc(final Scope scope) {
        return scope.visit(this);
      }
    });
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
    return FuncType.get(anns, declType, params);
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

    // reset context and evaluate function
    final QueryFocus qf = qc.focus;
    final Value cv = qf.value;
    qf.value = null;
    try {
      final int pl = params.length;
      for(int p = 0; p < pl; p++) qc.set(params[p], args[p]);
      return expr.value(qc);
    } finally {
      qf.value = cv;
    }
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
      if(declType != null && !declType.zero()) throw UUPFUNCTYPE.get(info);
      if(!upd && !expr.vacuous()) throw UPEXPECTF.get(ii);
    } else if(upd) {
      // uses updates, but is not declared as such
      throw UPNOT_X.get(ii, description());
    }
  }

  @Override
  public boolean vacuousBody() {
    return declType != null && declType.zero() && !has(Flag.UPD);
  }

  /**
   * Indicates if an expression has one of the specified compiler properties.
   * @param flags flags
   * @return result of check
   * @see Expr#has(Flag...)
   */
  boolean has(final Flag... flags) {
    // function itself does not perform any updates
    final Flag[] flgs = Flag.UPD.remove(flags);
    return flgs.length != 0 && check(flgs);
  }

  /**
   * Checks if the function body is updating.
   * @return result of check
   * @see Expr#has(Flag...)
   */
  boolean updating() {
    // MIXUPDATES: recursive check; otherwise, rely on flag (GH-1281)
    return sc.mixUpdates ? check(Flag.UPD) : updating;
  }

  /**
   * Checks if the function body has one of the specified compiler properties.
   * @param flags flags
   * @return result of check
   */
  private boolean check(final Flag... flags) {
    // handle recursive calls: check which flags have already been assigned
    final ArrayList<Flag> flgs = new ArrayList<>();
    for(final Flag flag : flags) {
      if(!map.containsKey(flag)) {
        map.put(flag, false);
        flgs.add(flag);
      }
    }
    // cache flags for remaining, new properties
    for(final Flag flag : flgs) map.put(flag, expr.has(flag));
    // evaluate result
    for(final Flag flag : flags) {
      if(map.get(flag)) return true;
    }
    return false;
  }

  @Override
  public boolean visit(final ASTVisitor visitor) {
    for(final Ann ann : anns) {
      if(ann.sig == Annotation._BASEX_LOCK) {
        for(final Item arg : ann.args()) {
          for(final String lock : Locking.queryLocks(((Str) arg).string())) {
            visitor.lock(lock, false);
          }
        }
      }
    }

    for(final Var var : params) {
      if(!visitor.declared(var)) return false;
    }
    return expr == null || expr.accept(visitor);
  }

  @Override
  public byte[] id() {
    return StaticFuncs.signature(name, params.length);
  }

  @Override
  public Expr inline(final Expr[] exprs, final CompileContext cc) throws QueryException {
    if(!inline(cc, anns, expr) || has(Flag.CTX) || compiling || selfRecursive()) return null;
    cc.info(OPTINLINE_X, (Supplier<?>) this::id);

    // create let bindings for all variables
    final LinkedList<Clause> clauses = new LinkedList<>();
    final IntObjMap<Var> vm = new IntObjMap<>();
    final int pl = params.length;
    for(int p = 0; p < pl; p++) {
      clauses.add(new Let(cc.copy(params[p], vm), exprs[p]).optimize(cc));
    }

    // create the return clause
    final Expr rtrn = expr.copy(cc, vm).optimize(cc);
    return clauses.isEmpty() ? rtrn : new GFLWOR(info, clauses, rtrn).optimize(cc);
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
    return expr.exprSize() < limit;
  }

  @Override
  public String description() {
    return "function declaration";
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, NAME, name.string()), params, expr);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(DECLARE).token(anns).token(FUNCTION).token(name.prefixId()).params(params);
    if(declType != null) qs.token(AS).token(declType);
    if(expr != null) qs.brace(expr);
    else qs.token(EXTERNAL);
    qs.token(';');
  }
}
