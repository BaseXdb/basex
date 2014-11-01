package org.basex.query.expr;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Abstract class for representing XQuery expressions.
 * Expression are divided into {@link ParseExpr} and {@link Value} classes.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public abstract class Expr extends ExprInfo {
  /** Flags that influence query compilation. */
  public enum Flag {
    /** Creates new fragments. Example: node constructor. */ CNS,
    /** Depends on context. Example: context node. */        CTX,
    /** Non-deterministic. Example: random:double(). */      NDT,
    /** Focus-dependent. Example: position(). */             FCS,
    /** Performs updates. Example: insert expression. */     UPD,
    /** Invokes user-supplied functions. Example: fold. */   HOF,
  }

  /**
   * Checks if all updating expressions are correctly placed.
   * This function is only called if any updating expression was found in the query.
   * @throws QueryException query exception
   */
  public abstract void checkUp() throws QueryException;

  /**
   * Compiles and optimizes the expression, assigns types and cardinalities.
   * This method will be initially called by {@link QueryContext#compile()}.
   * @param qc query context
   * @param scp variable scope
   * @return optimized expression
   * @throws QueryException query exception
   */
  public abstract Expr compile(QueryContext qc, VarScope scp) throws QueryException;

  /**
   * Optimizes an already compiled expression without recompiling its sub-expressions.
   * @param qc query context
   * @param scp variable scope
   * @return optimized expression
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Expr optimize(final QueryContext qc, final VarScope scp) throws QueryException {
    return this;
  }

  /**
   * Evaluates the expression and returns an iterator on the resulting items.
   * If this method is not overwritten, {@link #item(QueryContext, InputInfo)} must be implemented
   * by an expression, as it may be called by this method.
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  public abstract Iter iter(final QueryContext qc) throws QueryException;

  /**
   * Evaluates the expression and returns the resulting item,
   * or a {@code null} reference if the expression yields an empty sequence.
   * If this method is not overwritten, {@link #iter(QueryContext)} must be implemented by an
   * expression, as it may be called by this method.
   * @param qc query context
   * @param ii input info
   * @return iterator or {@code null}
   * @throws QueryException query exception
   */
  public abstract Item item(final QueryContext qc, final InputInfo ii) throws QueryException;

  /**
   * Evaluates the expression and returns the resulting value.
   * The implementation of this method is optional.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  public abstract Value value(final QueryContext qc) throws QueryException;

  /**
   * Evaluates the expression and returns zero or one atomized item, or an error.
   * @param qc query context
   * @param ii input info
   * @return iterator
   * @throws QueryException query exception
   */
  public final Iter atomIter(final QueryContext qc, final InputInfo ii) throws QueryException {
    return new AtomIter(iter(qc), qc, ii, seqType().mayBeArray());
  }

  /**
   * Evaluates the expression and returns zero or one atomized item, or an error.
   * @param qc query context
   * @param ii input info
   * @return iterator
   * @throws QueryException query exception
   */
  public abstract Item atomItem(final QueryContext qc, final InputInfo ii) throws QueryException;

  /**
   * Evaluates the expression and returns the atomized items.
   * @param qc query context
   * @param ii input info
   * @return atomized item
   * @throws QueryException query exception
   */
  public abstract Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException;

  /**
   * Checks if the iterator can be dissolved into an effective boolean value.
   * If not, returns an error. If yes, returns the first value - which can be
   * also be e.g. an integer, which is later evaluated as numeric predicate.
   * @param qc query context
   * @param ii input info
   * @return item
   * @throws QueryException query exception
   */
  public abstract Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException;

  /**
   * Performs a predicate test and returns the item if test was successful.
   * @param qc query context
   * @param ii input info
   * @return item
   * @throws QueryException query exception
   */
  public abstract Item test(final QueryContext qc, final InputInfo ii) throws QueryException;

  /**
   * Tests if this is an empty sequence. This function is only overwritten
   * by the {@link Empty} class, which represents the empty sequence.
   * @return result of check
   */
  public boolean isEmpty() {
    return false;
  }

  /**
   * Tests if this is a vacuous expression (empty sequence or error function).
   * This check is needed for updating queries.
   * @return result of check
   */
  public boolean isVacuous() {
    return false;
  }

  /**
   * Tests if this is a value.
   * @return result of check
   */
  public boolean isValue() {
    return false;
  }

  /**
   * Returns the data reference bound to this expression. This method is overwritten
   * by the values {@link DBNode} and {@link DBNodeSeq} and some more expressions.
   * @return data reference
   */
  public Data data() {
    return null;
  }

  /**
   * Returns the sequence size or 1.
   * @return result of check
   */
  public abstract long size();

  /**
   * Indicates if an expression has the specified compiler property. This method must only be
   * called at compile time. It is invoked to test properties of sub-expressions.
   * It returns {@code true} if at least one test is successful.
   * @param flag flag to be found
   * @return result of check
   */
  public abstract boolean has(final Flag flag);

  /**
   * Checks if the given variable is used by this expression.
   * @param var variable to be checked
   * @return {@code true} if the variable is used, {@code false} otherwise
   */
  public final boolean uses(final Var var) {
    // return true iff the the search was aborted, i.e. the variable is used
    return !accept(new ASTVisitor() {
      @Override
      public boolean used(final VarRef ref) {
        // abort when the variable is used
        return !ref.var.is(var);
      }
    });
  }

  /**
   * Checks if the specified variable is replaceable by a context value.
   * The following tests might return false:
   * <ul>
   * <li>{@link Preds#removable} if one of the variables is used within a predicate.</li>
   * <li>{@link MixedPath#removable} if the variable occurs within the path.</li>
   * </ul>
   * This method is called by {@link GFLWOR#compile(QueryContext, VarScope)} to rewrite where
   * clauses into predicates.
   * @param var variable to be replaced
   * @return result of check
   */
  public abstract boolean removable(final Var var);

  /**
   * Checks how often a variable is used in this expression.
   * This function is e.g. called by {@link SwitchCase#countCases} or (indirectly)
   * {@link GFLWOR#inlineLets}.
   * @param var variable to look for
   * @return how often the variable is used, see {@link VarUsage}
   */
  public abstract VarUsage count(final Var var);

  /**
   * Inlines an expression into this one, replacing all references to the given variable.
   * This function is e.g. called by {@link GFLWOR#inlineLets} and {@link For#toPredicate},
   * and the variable reference is replaced in {@link VarRef#inline}.
   * @param qc query context for reoptimization
   * @param scp variable scope for reoptimization
   * @param var variable to replace
   * @param ex expression to inline
   * @return resulting expression if something changed, {@code null} otherwise
   * @throws QueryException query exception
   */
  public abstract Expr inline(final QueryContext qc, final VarScope scp, final Var var,
      final Expr ex) throws QueryException;

  /**
   * Inlines the given expression into all elements of the given array.
   * @param qc query context
   * @param scp variable scope
   * @param arr array
   * @param var variable to replace
   * @param ex expression to inline
   * @return {@code true} if the array has changed, {@code false} otherwise
   * @throws QueryException query exception
   */
  protected static boolean inlineAll(final QueryContext qc, final VarScope scp, final Expr[] arr,
      final Var var, final Expr ex) throws QueryException {

    boolean change = false;
    final int al = arr.length;
    for(int a = 0; a < al; a++) {
      final Expr e = arr[a].inline(qc, scp, var, ex);
      if(e != null) {
        arr[a] = e;
        change = true;
      }
    }
    return change;
  }

  /**
   * Copies an expression.
   * Will be useful for inlining functions, or for copying static queries.
   * @param qc query context
   * @param scp variable scope for creating new variables
   * @param vs mapping from old variable IDs to new variable copies
   * @return copied expression
   */
  public abstract Expr copy(QueryContext qc, VarScope scp, IntObjMap<Var> vs);

  /**
   * <p>This method is e.g. overwritten by expressions like {@link CmpG}, {@link CmpV},
   * {@link FnBoolean}, {@link FnExists}, {@link Path} or {@link Filter}.
   * It is called at compile time by expressions that perform
   * effective boolean value tests (e.g. {@link If} or {@link Preds}).
   * If the arguments of the called expression return a boolean anyway,
   * the expression will be simplified.</p>
   * <p>Example in {@link CmpV}:
   * <code>if($x eq true())</code> is rewritten to <code>if($x)</code> if <code>$x</code>
   * is known to return a single boolean.</p>
   * @param qc query context
   * @param scp variable scope
   * @return optimized expression
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Expr optimizeEbv(final QueryContext qc, final VarScope scp) throws QueryException {
    return this;
  }

  /**
   * Returns the static type of the resulting value.
   * @return result of check
   */
  public abstract SeqType seqType();

  /**
   * Indicates if the items returned by this expression are iterable, i.e., if returned nodes are
   * in document order and contain no duplicates. This will also be guaranteed if zero or one
   * item is returned.
   * It is e.g. called by {@link AxisPath}.
   * @return result of check
   */
  public boolean iterable() {
    return seqType().zeroOrOne();
  }

  /**
   * Checks if an expression can be rewritten to an index access.
   * If so, the index expression will be bound to {@link IndexInfo#expr}.
   * This method will be called by {@link Path#index}.
   * @param ii index info
   * @return true if an index can be used
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public boolean indexAccessible(final IndexInfo ii) throws QueryException {
    return false;
  }

  /**
   * Compares the current and specified expression for equality.
   * @param cmp expression to be compared
   * @return result of check
   */
  public boolean sameAs(final Expr cmp) {
    return this == cmp;
  }

  /**
   * Checks if this expression is a certain function.
   * @param func function definition
   * @return result of check
   */
  @SuppressWarnings("unused")
  public boolean isFunction(final Function func) {
    return false;
  }

  /**
   * Checks if this expression has free variables.
   * @return {@code true} if there are variables which are used but not declared in this expression,
   *         {@code false} otherwise
   */
  protected boolean hasFreeVars() {
    final BitSet declared = new BitSet();
    return !accept(new ASTVisitor() {
      @Override
      public boolean declared(final Var var) {
        declared.set(var.id);
        return true;
      }

      @Override
      public boolean used(final VarRef ref) {
        return declared.get(ref.var.id);
      }
    });
  }

  /**
   * Finds and marks tail calls, enabling TCO.
   * @param qc query context, {@code null} if the changes should not be reported
   */
  @SuppressWarnings("unused")
  public void markTailCalls(final QueryContext qc) { }

  /**
   * Traverses this expression, notifying the visitor of declared and used variables,
   * and checking the tree for other recursive properties.
   * @param visitor visitor
   * @return if the walk should be continued
   */
  public abstract boolean accept(final ASTVisitor visitor);

  /**
   * Visit all given expressions with the given visitor.
   * @param visitor visitor
   * @param exprs expressions to visit
   * @return success flag
   */
  protected static boolean visitAll(final ASTVisitor visitor, final Expr...exprs) {
    for(final Expr e : exprs) if(!e.accept(visitor)) return false;
    return true;
  }

  /**
   * Counts the number of expressions in this expression's sub-tree.
   * This method is e.g. called by {@link StaticFunc#inline} to check if an expression
   * is small enough to be inlined.
   * @return number of expressions
   */
  public abstract int exprSize();

  /**
   * Tries to push the given type check inside this expression.
   * @param tc type check to push into the expression
   * @param qc query context
   * @param scp variable scope
   * @return the resulting expression if successful, {@code null} otherwise
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  protected Expr typeCheck(final TypeCheck tc, final QueryContext qc, final VarScope scp)
      throws QueryException {
    return null;
  }
}
