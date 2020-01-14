package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Abstract array expression.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public abstract class Arr extends ParseExpr {
  /** Expressions. */
  public Expr[] exprs;

  /**
   * Constructor.
   * @param info input info
   * @param seqType sequence type
   * @param exprs expressions
   */
  protected Arr(final InputInfo info, final SeqType seqType, final Expr... exprs) {
    super(info, seqType);
    this.exprs = exprs;
  }

  @Override
  public void checkUp() throws QueryException {
    checkNoneUp(exprs);
  }

  @Override
  public Expr compile(final CompileContext cc) throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e < el; e++) exprs[e] = exprs[e].compile(cc);
    return optimize(cc);
  }

  @Override
  public boolean has(final Flag... flags) {
    for(final Expr expr : exprs) {
      if(expr.has(flags)) return true;
    }
    return false;
  }

  @Override
  public boolean inlineable(final Var var) {
    for(final Expr expr : exprs) {
      if(!expr.inlineable(var)) return false;
    }
    return true;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.sum(var, exprs);
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    return inlineAll(var, ex, exprs, cc) ? optimize(cc) : null;
  }

  /**
   * Inlines an expression into this one, replacing all variable or context references.
   * @param var variable to replace
   * @param ex expression to inline
   * @param cc compilation context
   * @param context function for context inlining
   * @return resulting expression if something changed, {@code null} otherwise
   * @throws QueryException query exception
   */
  public Expr inline(final Var var, final Expr ex, final CompileContext cc,
      final QueryFunction<Void, Expr> context) throws QueryException {

    // no arguments are inlined: return null or apply context inlining function
    if(!inlineAll(var, ex, exprs, cc))
      return var != null ? null : context.apply(null);

    // optimize new expression. skip further optimizations if variable was inlined
    final Expr opt = optimize(cc);
    if(var != null) return opt;

    // inline again if context was inlined
    final Expr inlined = opt.inline(var, ex, cc);
    return inlined != null ? inlined : opt;
  }

  /**
   * Creates a deep copy of the given array.
   * @param <T> element type
   * @param cc compilation context
   * @param vm variable mapping
   * @param arr array to copy
   * @return deep copy of the array
   */
  @SuppressWarnings("unchecked")
  public static <T extends Expr> T[] copyAll(final CompileContext cc, final IntObjMap<Var> vm,
      final T[] arr) {

    final T[] copy = arr.clone();
    final int cl = copy.length;
    for(int c = 0; c < cl; c++) copy[c] = (T) copy[c].copy(cc, vm);
    return copy;
  }

  /**
   * Returns true if all arguments are values (possibly of small size).
   * @param limit check if size of any value exceeds {@link CompileContext#MAX_PREEVAL}
   * @return result of check
   */
  protected final boolean allAreValues(final boolean limit) {
    for(final Expr expr : exprs) {
      if(!(expr instanceof Value) || (limit && expr.size() > CompileContext.MAX_PREEVAL))
        return false;
    }
    return true;
  }

  /**
   * Returns the first expression that yields an empty sequence. If all expressions return non-empty
   * results, the original expression is returned.
   * @return empty or original expression
   */
  final Expr emptyExpr() {
    // pre-evaluate if one value is empty (e.g.: () = local:expensive() )
    for(final Expr expr : exprs) {
      if(expr.seqType().zero()) return expr;
    }
    return this;
  }

  /**
   * Tries to merge consecutive expressions.
   * @param pos allow positional expressions
   * @param union merge as union expression
   * @param cc compilation context
   * @throws QueryException query exception
   */
  public void merge(final boolean pos, final boolean union, final CompileContext cc)
      throws QueryException {
    // 'a'[. = 'a' or . = 'b']  ->  'a'[. = ('a', 'b')]
    final ExprList list = new ExprList().add(exprs);
    for(int l = 0; l < list.size(); l++) {
      for(int m = l + 1; m < list.size(); m++) {
        final Expr expr = list.get(l);
        if(pos || !expr.has(Flag.POS)) {
          final Expr merged = expr.merge(list.get(m), union, cc);
          if(merged != null) {
            cc.info(OPTSIMPLE_X_X, (Supplier<?>) this::description, this);
            list.set(l, merged);
            list.remove(m--);
          }
        }
      }
    }
    exprs = list.finish();
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, exprs);
  }

  @Override
  public int exprSize() {
    int size = 1;
    for(final Expr expr : exprs) size += expr.exprSize();
    return size;
  }

  /**
   * {@inheritDoc}
   * Must be overwritten by implementing class.
   */
  @Override
  public boolean equals(final Object obj) {
    return obj instanceof Arr && Array.equals(exprs, ((Arr) obj).exprs);
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this), exprs);
  }

  /**
   * Prints array entries with the specified separator.
   * @param sep separator
   * @return string representation
   */
  protected String toString(final String sep) {
    return new TokenBuilder().add(PAREN1).addSep(exprs, sep).add(PAREN2).toString();
  }
}
