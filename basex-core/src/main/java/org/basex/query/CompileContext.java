package org.basex.query;

import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.scope.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Compilation context.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class CompileContext {
  /** Query context. */
  public final QueryContext qc;
  /** Variable scopes. */
  private final ArrayList<VarScope> scopes = new ArrayList<>();

  /**
   * Constructor.
   * @param qc query context
   */
  public CompileContext(final QueryContext qc) {
    this.qc = qc;
  }

  /**
   * Adds some compilation info.
   * @param string evaluation info
   * @param ext text text extensions
   */
  public void info(final String string, final Object... ext) {
    qc.info.compInfo(string,  ext);
  }

  /**
   * Pushes a new variable scope to the stack.
   * @param scp variable scope
   */
  public void pushScope(final VarScope scp) {
    scopes.add(scp);
  }

  /**
   * Removes a variable scope from the stack.
   * @return the removed element
   */
  public VarScope removeScope() {
    return scopes.remove(scopes.size() - 1);
  }

  /**
   * Prepares the variable scope for being compiled.
   * This method should be run after compiling a scope.
   * @param scope scope
   */
  public void removeScope(final Scope scope) {
    removeScope().cleanUp(scope);
  }

  /**
   * Returns the current variable scope.
   * @return variable scope
   */
  public VarScope vs() {
    return scopes.get(scopes.size() - 1);
  }

  /**
   * Returns the current static context.
   * @return static context
   */
  public StaticContext sc() {
    return vs().sc;
  }

  /**
   * Creates a new copy of the given variable in this scope.
   * @param var variable to copy (can be {@code null})
   * @param vm variable mapping (can be {@code null})
   * @return new variable, or {@code null} if the supplied variable is {@code null}
   */
  public Var copy(final Var var, final IntObjMap<Var> vm) {
    if(var == null) return null;
    final VarScope vs = vs();
    final Var v = vs.add(new Var(var, qc, vs.sc));
    if(vm != null) vm.put(var.id, v);
    return v;
  }

  /**
   * Pre-evaluates the specified expression.
   * @param expr expression
   * @return optimized expression
   * @throws QueryException query exception
   */
  public Expr preEval(final Expr expr) throws QueryException {
    return replaceWith(expr, qc.value(expr));
  }

  /**
   * Adds an optimization info for pre-evaluating the specified expression to an empty sequence.
   * @param result resulting expression
   * @return optimized expression
   */
  public Expr emptySeq(final Expr result) {
    return replaceWith(result, null);
  }

  /**
   * Replaces an EBV expression.
   * @param expr expression
   * @param result resulting expression ({@code null} indicates empty sequence)
   * @return optimized expression
   */
  public Expr replaceEbv(final Expr expr, final Expr result) {
    return replaceWith(expr, result, false);
  }

  /**
   * Replaces an expression with the specified one.
   * @param expr expression
   * @param result resulting expression ({@code null} indicates empty sequence)
   * @return optimized expression
   */
  public Expr replaceWith(final Expr expr, final Expr result) {
    return replaceWith(expr, result, true);
  }

  /**
   * Replaces an expression with the specified one.
   * @param expr expression
   * @param result resulting expression ({@code null} indicates empty sequence)
   * @param refine refine type
   * @return optimized expression
   */
  private Expr replaceWith(final Expr expr, final Expr result, final boolean refine) {
    final Expr res = result == null ? Empty.SEQ : result;
    if(res != expr) {
      final byte[] exprString = QueryError.chop(Token.token(expr.toString()), null);
      final byte[] resString = QueryError.chop(Token.token(res.toString()), null);
      final TokenBuilder tb = new TokenBuilder(res instanceof ParseExpr ? OPTREWRITE : OPTPRE);
      tb.add(' ').add(expr.description()).add(" to ").add(res.description()).add(": ");
      tb.add(exprString);
      if(!Token.eq(exprString, resString)) tb.add(" -> ").add(resString);
      info(tb.toString());

      if(res instanceof ParseExpr) {
        // refine type. required mostly for {@link Filter} rewritings
        if(refine) {
          final ParseExpr re = (ParseExpr) res;
          final SeqType et = expr.seqType(), rt  = re.seqType();
          if(!et.eq(rt) && et.instanceOf(rt)) {
            final SeqType st = et.intersect(rt);
            if(st != null) re.seqType = st;
          }
        }
      } else if(res != Empty.SEQ && refine) {
        // refine type. required because original type might have got lost in new sequence
        if(res instanceof Seq) {
          final Seq seq = (Seq) res;
          final Type et = expr.seqType().type, rt = seq.type;
          if(!et.eq(rt) && et.instanceOf(rt)) {
            final Type t = et.intersect(rt);
            if(t != null) {
              seq.type = t;
              // Indicate that types may not be homogeneous
              seq.homo = false;
            }
          }
        } else if(res instanceof FItem) {
          // refine type of function items (includes maps and arrays)
          final FItem fitem = (FItem) res;
          final SeqType et = expr.seqType(), rt = res.seqType();
          if(!et.eq(rt) && et.instanceOf(rt)) {
            final Type t = et.type.intersect(rt.type);
            if(t != null) fitem.type = t;
          }
        }
      }
    }
    return res;
  }

  /**
   * Creates an error function instance.
   * @param qe exception to be raised
   * @param expr expression
   * @return function
   */
  public StandardFunc error(final QueryException qe, final Expr expr) {
    return FnError.get(qe, expr.seqType(), sc());
  }

  /**
   * Creates and returns an optimized function instance.
   * @param func function
   * @param info input info
   * @param exprs expressions
   * @return function
   * @throws QueryException query exception
   */
  public Expr function(final Function func, final InputInfo info, final Expr... exprs)
      throws QueryException {
    return func.get(sc(), info, exprs).optimize(this);
  }
}
