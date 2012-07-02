package org.basex.query.expr;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.flwor.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.path.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Abstract class for representing XQuery expressions.
 * Expression are divided into {@link ParseExpr} and {@link Value} classes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Expr extends ExprInfo {
  /** Flags that influence query compilation. */
  public enum Use {
    /** Creates new fragments. Example: node constructor. */ CNS,
    /** Depends on context. Example: context node. */        CTX,
    /** Non-deterministic. Example: random(). */             NDT,
    /** Context position. Example: position(). */            POS,
    /** Performs updates. Example: insert expression. */     UPD,
    /** References a variable. Example: {@link VarRef}. */   VAR,
    /** Based on XQuery 3.0. Example: group by statement. */ X30,
  }

  /**
   * Checks if all updating expressions are correctly placed.
   * This function is only called if any updating expression was found in the query.
   * @throws QueryException query exception
   */
  public abstract void checkUp() throws QueryException;

  /**
   * Compiles and optimizes the expression, assigns data types and
   * cardinalities.
   * @param ctx query context
   * @return optimized expression
   * @throws QueryException query exception
   */
  public abstract Expr compile(final QueryContext ctx) throws QueryException;

  /**
   * Evaluates the expression and returns an iterator on the resulting items.
   * If this method is not overwritten, {@link #item} must be implemented
   * by an expression, as it may be called by this method.
   * @param ctx query context
   * @return resulting item
   * @throws QueryException query exception
   */
  public abstract Iter iter(final QueryContext ctx) throws QueryException;

  /**
   * Evaluates the expression and returns the resulting item or
   * a {@code null} reference, if the expression yields an empty sequence.
   * If this method is not overwritten, {@link #iter} must be implemented
   * by an expression, as it may be called by this method.
   * @param ctx query context
   * @param ii input info
   * @return iterator
   * @throws QueryException query exception
   */
  public abstract Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  /**
   * Evaluates the expression and returns the resulting value.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  public abstract Value value(final QueryContext ctx) throws QueryException;

  /**
   * Checks if the iterator can be dissolved into an effective boolean value.
   * If not, returns an error. If yes, returns the first value - which can be
   * also be e.g. an integer, which is later evaluated as numeric predicate.
   * @param ctx query context
   * @param ii input info
   * @return item
   * @throws QueryException query exception
   */
  public abstract Item ebv(final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  /**
   * Performs a predicate test and returns the item if test was successful.
   * @param ctx query context
   * @param ii input info
   * @return item
   * @throws QueryException query exception
   */
  public abstract Item test(final QueryContext ctx, final InputInfo ii)
      throws QueryException;

  /**
   * Copies an expression. Returns {@code null} if expression cannot be copied.
   * Will be useful for inlining functions, or for copying static queries.
   * @return copied expression
   */
  public Expr copy() {
    return null;
  }

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
   * Tests if this is an item.
   * @return result of check
   */
  public boolean isItem() {
    return false;
  }

  /**
   * Returns the sequence size or 1.
   * @return result of check
   */
  public abstract long size();

  /**
   * Indicates if an expression uses the specified type or operation. This method is
   * called by numerous {@link #compile} methods to test the properties of
   * sub-expressions. It will return {@code true} as soon as at least one test is
   * successful.
   * @param u type/operation to be found
   * @return result of check
   */
  public abstract boolean uses(final Use u);

  /**
   * Counts how often the specified variable is used by an expression.
   * This method is called by:
   * <ul>
   * <li> {@link GFLWOR#compile} to rewrite where clauses as predicates and
   *  remove statically bound or unused clauses</li>
   * <li> {@link GFLWOR#compHoist} to hoist independent variables</li>
   * </ul>
   * @param v variable to be checked
   * @return number of occurrences
   */
  public abstract int count(final Var v);

  /**
   * Checks if the specified variable is replaceable by a context item.
   * The following tests might return false:
   * <ul>
   * <li>{@link Preds#removable}, if one of the variables is used within
   * a predicate.</li>
   * <li>{@link MixedPath#removable}, if the variable occurs within
   * the path.</li>
   * <li>{@link Group#removable}, as the group by expression depends on
   * variable references.</li>
   * </ul>
   * This method is called by {@link GFLWOR#compile} to rewrite where clauses
   * into predicates.
   * @param v variable to be replaced
   * @return result of check
   */
  public abstract boolean removable(final Var v);

  /**
   * Substitutes all {@link VarRef} expressions for the given variable
   * by a {@link Context} reference. This method is called by
   * {@link GFLWOR#compile} to rewrite where clauses as predicates.
   * @param v variable to be replaced
   * @return new expression
   */
  public abstract Expr remove(final Var v);

  /**
   * Adds the names of the databases that will be touched by the query.
   * The {@code abstract} modifier can be replaced as soon as the function is
   * implemented by all expressions.
   * @param db set
   * @return {@code false} if databases cannot be statically determined
   */
  public abstract boolean databases(final StringList db);

  /**
   * <p>This method is overwritten by {@link CmpG}, {@link CmpV} and {@link FNSimple}.
   * It is called at compile time by expressions that perform
   * effective boolean value tests (e.g. {@link If} or {@link Preds}).
   * If the arguments of the called expression return a boolean anyway,
   * the expression will be simplified.</p>
   * <p>Example in {@link CmpV}:
   * {@code if($x eq true())} is rewritten to {@code if($x)}, if {@code $x}
   * is known to return a single boolean.</p>
   * @param ctx query context
   * @return optimized expression
   */
  @SuppressWarnings("unused")
  public Expr compEbv(final QueryContext ctx) {
    return this;
  }

  /**
   * Returns the static type of the evaluated value. For simplicity,
   * some types have been merged to super types. As an example, many numeric types
   * are treated as integers.
   * @return result of check
   */
  public abstract SeqType type();

  /**
   * Returns true if the expression is iterable, i.e., if it will not contain
   * any duplicate nodes and if all nodes will be are sorted.
   * The return value of this method is only relevant for node sequences.
   * It is e.g. called by {@link AxisPath}.
   * @return result of check
   */
  public boolean iterable() {
    return type().zeroOrOne();
  }

  /**
   * Checks if an expression can be rewritten to an index access.
   * If this method is implemented, {@link #indexEquivalent} must be
   * implemented as well.
   * @param ic index context
   * @return true if an index can be used
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    return false;
  }

  /**
   * Returns an equivalent expression which accesses an index structure.
   * Will be called if {@link #indexAccessible} is returns true for an
   * expression.
   * @param ic index context
   * @return equivalent index-expression
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Expr indexEquivalent(final IndexContext ic) throws QueryException {
    return null;
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
   * @param f function definition
   * @return result of check
   */
  @SuppressWarnings("unused")
  public boolean isFunction(final Function f) {
    return false;
  }

  /**
   * Optionally adds a text node to an expression for potential index rewriting.
   * @param ctx query context
   * @return expression
   */
  @SuppressWarnings("unused")
  public Expr addText(final QueryContext ctx) {
    return this;
  }

  /**
   * Checks if this expression has free variables.
   * @param ctx query context on the level of this expression
   * @return {@code true} if there are variables which are used but not declared
   *         in this expression, {@code false} otherwise
   */
  public boolean hasFreeVars(final QueryContext ctx) {
    final VarStack global = ctx.vars.globals();
    for(int i = global.size; --i >= 0;) {
      if(count(global.vars[i]) > 0) return true;
    }
    final VarStack vars = ctx.vars.locals();
    for(int i = vars.size; --i >= 0;) {
      if(count(vars.vars[i]) > 0) return true;
    }
    return false;
  }

  /**
   * Finds and marks tail calls, enabling TCO.
   * @return the expression, with tail calls marked
   */
  public Expr markTailCalls() {
    return this;
  }
}
