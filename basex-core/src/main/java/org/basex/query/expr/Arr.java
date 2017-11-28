package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Abstract array expression.
 *
 * @author BaseX Team 2005-17, BSD License
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
    for(final Expr expr : exprs) if(expr.has(flags)) return true;
    return false;
  }

  @Override
  public boolean removable(final Var var) {
    for(final Expr expr : exprs) if(!expr.removable(var)) return false;
    return true;
  }

  @Override
  public VarUsage count(final Var var) {
    return VarUsage.sum(var, exprs);
  }

  @Override
  public Expr inline(final Var var, final Expr ex, final CompileContext cc) throws QueryException {
    return inlineAll(exprs, var, ex, cc) ? optimize(cc) : null;
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
   * Returns true if at least one argument returns the empty sequence.
   * @return result of check
   */
  protected final boolean oneIsEmpty() {
    for(final Expr expr : exprs) {
      if(expr == Empty.SEQ) return true;
    }
    return false;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    return visitAll(visitor, exprs);
  }

  @Override
  public int exprSize() {
    int sz = 1;
    for(final Expr expr : exprs) sz += expr.exprSize();
    return sz;
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
  public void plan(final FElem plan) {
    addPlan(plan, planElem(), exprs);
  }

  /**
   * Prints array entries with the specified separator.
   * @param sep separator
   * @return string representation
   */
  protected String toString(final String sep) {
    return new TokenBuilder(PAREN1).addSep(exprs, sep).add(PAREN2).toString();
  }
}
