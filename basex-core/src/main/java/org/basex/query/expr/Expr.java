package org.basex.query.expr;

import java.util.*;

import org.basex.core.*;
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
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public abstract class Expr extends ExprInfo {
  /** Flags that influence query compilation. */
  public enum Flag {
    /** Node creation. No relocation of expressions that would change number of node constructions
     * Example: node constructor. */
    CNS,
    /** Context dependency. Checked to prevent relocations of expressions to different context.
     * Example: context item ({@code .}). */
    CTX,
    /** Non-deterministic code. Cannot be relocated, pre-evaluated or optimized away.
     * Examples: random:double(), file:write(). */
    NDT,
    /** Positional access. Prevents simple iterative evaluation.
     * Examples: position(), last(). */
    POS,
    /** Performs updates. Checked to detect if an expression is updating or not, or if code
     * can be optimized away when using {@link MainOptions#MIXUPDATES}. Example: delete node. */
    UPD,
    /** Function invocation. Used to suppress pre-evaluation of built-in functions with
     * functions arguments. Example: fold-left. */
    HOF,
  }

  /**
   * Checks if the updating semantics are satisfied.
   * This function is only called if any updating expression was found in the query.
   * @throws QueryException query exception
   */
  public abstract void checkUp() throws QueryException;

  /**
   * Compiles and optimizes the expression, assigns types and cardinalities.
   * This method will be initially called by {@link QueryContext#compile()}.
   * @param cc compilation context
   * @return optimized expression
   * @throws QueryException query exception
   */
  public abstract Expr compile(CompileContext cc) throws QueryException;

  /**
   * Optimizes an already compiled expression without recompiling its sub-expressions.
   * @param cc compilation context
   * @return optimized expression
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Expr optimize(final CompileContext cc) throws QueryException {
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
  public abstract Iter iter(QueryContext qc) throws QueryException;

  /**
   * Evaluates the expression and returns the resulting item,
   * or a {@code null} reference if the expression yields an empty sequence.
   * If this method is not overwritten, {@link #iter(QueryContext)} must be implemented by an
   * expression, as it may be called by this method.
   * @param qc query context
   * @param ii input info (required for {@link Seq} instances, which have no input info)
   * @return item or {@code null}
   * @throws QueryException query exception
   */
  public abstract Item item(QueryContext qc, InputInfo ii) throws QueryException;

  /**
   * Evaluates the expression and returns the resulting value.
   * The implementation of this method is optional.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  public abstract Value value(final QueryContext qc) throws QueryException;

  /**
   * Evaluates the expression and returns an iterator on the resulting, atomized items.
   * @param qc query context
   * @param ii input info (required for {@link Seq} instances, which have no input info)
   * @return iterator
   * @throws QueryException query exception
   */
  public final Iter atomIter(final QueryContext qc, final InputInfo ii) throws QueryException {
    return new AtomIter(iter(qc), qc, ii, seqType().mayBeArray());
  }

  /**
   * Evaluates the expression and returns the resulting, atomized item,
   * or a {@code null} reference if the expression yields an empty sequence.
   * @param qc query context
   * @param ii input info (required for {@link Seq} instances, which have no input info)
   * @return item or {@code null}
   * @throws QueryException query exception
   */
  public abstract Item atomItem(QueryContext qc, InputInfo ii) throws QueryException;

  /**
   * Evaluates the expression and returns the atomized items.
   * @param qc query context
   * @param ii input info (required for {@link Seq} instances, which have no input info)
   * @return atomized item
   * @throws QueryException query exception
   */
  public abstract Value atomValue(QueryContext qc, InputInfo ii) throws QueryException;

  /**
   * <p>Checks if the effective boolean value can be computed for this expression:</p>
   * <ul>
   *   <li> If it yields an empty sequence, {@link Bln#FALSE} will be returned.
   *   <li> If it yields a single item, this item will be returned.
   *   <li> If it yields nodes, the first node will be returned.
   *   <li> Otherwise, an error will be raised.
   * </ul>
   * <p>A single numeric item may later be evaluated as positional predicate.</p>
   * @param qc query context
   * @param ii input info (required for {@link Seq} instances, which have no input info)
   * @return item
   * @throws QueryException query exception
   */
  public abstract Item ebv(QueryContext qc, InputInfo ii) throws QueryException;

  /**
   * Performs a predicate test and returns the item the if test was successful.
   * @param qc query context
   * @param ii input info (required for {@link Seq} instances, which have no input info)
   * @return item
   * @throws QueryException query exception
   */
  public abstract Item test(QueryContext qc, InputInfo ii) throws QueryException;

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
   * Returns the data reference bound to this expression. This method is currently overwritten
   * by {@link DBNode}, {@link DBNodeSeq}, {@link Path} and {@link VarRef}.
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
   * @param flag flag to be checked
   * @return result of check
   */
  public abstract boolean has(Flag flag);

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
   *   <li>{@link Preds#removable} if one of the variables is used within a predicate.</li>
   *   <li>{@link Path#removable} if the variable occurs within the path.</li>
   * </ul>
   * This method is called by {@link For#toPredicate(CompileContext, Expr)}.
   * @param var variable to be replaced
   * @return result of check
   */
  public abstract boolean removable(Var var);

  /**
   * Checks how often a variable is used in this expression.
   * This function is e.g. called by {@link SwitchCase#countCases} or (indirectly)
   * {@link GFLWOR#inlineLets}.
   * @param var variable to look for
   * @return how often the variable is used, see {@link VarUsage}
   */
  public abstract VarUsage count(Var var);

  /**
   * Inlines an expression into this one, replacing all references to the given variable.
   * This function is e.g. called by {@link GFLWOR#inlineLets} and {@link For#toPredicate},
   * and the variable reference is replaced in {@link VarRef#inline}.
   * @param var variable to replace
   * @param ex expression to inline
   * @param cc compilation context
   * @return resulting expression if something changed, {@code null} otherwise
   * @throws QueryException query exception
   */
  public abstract Expr inline(Var var, Expr ex, CompileContext cc) throws QueryException;

  /**
   * Inlines the given expression into all elements of the given array.
   * @param arr array
   * @param var variable to replace
   * @param ex expression to inline
   * @param cc compilation context
   * @return {@code true} if the array has changed, {@code false} otherwise
   * @throws QueryException query exception
   */
  protected static boolean inlineAll(final Expr[] arr, final Var var, final Expr ex,
      final CompileContext cc) throws QueryException {

    boolean change = false;
    final int al = arr.length;
    for(int a = 0; a < al; a++) {
      final Expr e = arr[a].inline(var, ex, cc);
      if(e != null) {
        arr[a] = e;
        change = true;
      }
    }
    return change;
  }

  /**
   * Copies an expression. Used for inlining functions, or for copying static queries.
   * @param cc compilation context
   * @param vm mapping from old variable IDs to new variable copies
   * @return copied expression
   */
  public abstract Expr copy(CompileContext cc, IntObjMap<Var> vm);

  /**
   * <p>This method is e.g. overwritten by expressions like {@link CmpG}, {@link CmpV},
   * {@link FnBoolean}, {@link FnExists}, {@link Path} or {@link Filter}.
   * It is called at compile time by expressions that perform
   * effective boolean value tests (e.g. {@link If} or {@link Preds}).
   * If the arguments of the called expression return a boolean anyway,
   * the expression will be simplified.</p>
   * <p>Example in {@link CmpV}:
   * <code>if($x eq true())</code> is rewritten to <code>if($x)</code> if {@code $x}
   * is known to return a single boolean.</p>
   * @param cc compilation context
   * @return optimized expression
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Expr optimizeEbv(final CompileContext cc) throws QueryException {
    // return true if a deterministic expression returns at least one node
    final SeqType st = seqType();
    if(st.type instanceof NodeType && st.oneOrMore() && !has(Flag.UPD) && !has(Flag.NDT)) {
      cc.info(QueryText.OPTREWRITE_X, this);
      return Bln.TRUE;
    }
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
   * Compares the current and specified expression for equality. {@code false} may be returned,
   * even if the expressions are equal.
   * @param cmp expression to be compared (can be {@code null})
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
   * @param cc compilation context, {@code null} if the changes should not be reported
   */
  @SuppressWarnings("unused")
  public void markTailCalls(final CompileContext cc) { }

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
    for(final Expr expr : exprs) if(!expr.accept(visitor)) return false;
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
   * @param cc compilation context
   * @return the resulting expression if successful, {@code null} otherwise
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  protected Expr typeCheck(final TypeCheck tc, final CompileContext cc) throws QueryException {
    return null;
  }
}
