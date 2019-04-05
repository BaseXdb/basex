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
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public abstract class Expr extends ExprInfo {
  /**
   * Checks if the updating semantics are satisfied.
   * This function is only called if any updating expression was found in the query.
   * @throws QueryException query exception
   */
  public abstract void checkUp() throws QueryException;

  /**
   * Compiles and optimizes the expression, assigns types and cardinalities.
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
   * The implementation of this method is optional.
   * @param qc query context
   * @return iterator
   * @throws QueryException query exception
   */
  public abstract Iter iter(QueryContext qc) throws QueryException;

  /**
   * Evaluates the expression and returns the resulting value.
   * If this method is not implemented, {@link #item(QueryContext, InputInfo)} must be implemented
   * instead.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  public abstract Value value(QueryContext qc) throws QueryException;

  /**
   * Evaluates the expression and returns the resulting item,
   * or {@link Empty#VALUE} if the expression yields an empty sequence.
   * If this method is not implemented, {@link #value(QueryContext)} must be implemented instead.
   * @param qc query context
   * @param ii input info (only required by {@link Seq} instances, which have no input info)
   * @return item or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  public abstract Item item(QueryContext qc, InputInfo ii) throws QueryException;

  /**
   * Evaluates the expression and returns an iterator on the resulting, atomized items.
   * @param qc query context
   * @param ii input info
   * @return iterator
   * @throws QueryException query exception
   */
  public Iter atomIter(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = iter(qc);
    final SeqType st = seqType();
    return st.type.instanceOf(AtomType.AAT) ? iter :
      new AtomIter(iter, qc, ii, st.mayBeArray() ? -1 : iter.size());
  }

  /**
   * Evaluates the expression and returns the resulting, atomized item,
   * or {@link Empty#VALUE} if the expression yields an empty sequence.
   * @param qc query context
   * @param ii input info (only required by {@link Seq} instances, which have no input info)
   * @return item or {@link Empty#VALUE}
   * @throws QueryException query exception
   */
  public Item atomItem(final QueryContext qc, final InputInfo ii) throws QueryException {
    return atomValue(qc, ii).item(qc, ii);
  }

  /**
   * Evaluates the expression and returns the atomized items.
   * @param qc query context
   * @param ii input info (only required by {@link Seq} instances, which have no input info)
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
   * Performs a predicate test and returns the item if the test was successful.
   * The returned item is required for full-text scoring.
   * @param qc query context
   * @param ii input info (required for {@link Seq} instances, which have no input info)
   * @return item or {@code null}
   * @throws QueryException query exception
   */
  public abstract Item test(QueryContext qc, InputInfo ii) throws QueryException;

  /**
   * Tests if this is a vacuous expression (empty sequence or error function).
   * This check is needed for updating queries.
   * @return result of check
   */
  public boolean isVacuous() {
    return false;
  }

  /**
   * Returns the data reference bound to this expression. This method is currently overwritten
   * by {@link DBNode}, {@link DBNodeSeq}, {@link Path} and {@link VarRef}.
   * @return data reference (can be {@code null})
   */
  public Data data() {
    return null;
  }

  /**
   * Returns the result size, or {@code -1} if the size is unknown.
   * @return result of check
   */
  public abstract long size();

  /**
   * Indicates if an expression has one of the specified compiler properties. This method must only
   * be called at compile time. It is invoked to test properties of sub-expressions.
   * It returns {@code true} if at least flag matches an expression.
   * @param flags flags to be checked
   * @return result of check
   */
  public abstract boolean has(Flag... flags);

  /**
   * Checks if the given variable is used by this expression.
   * @param var variable to be checked
   * @return {@code true} if the variable is used
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
   * Checks if the specified variable is inlineable.
   * This function is called by:
   * <ul>
   *   <li> {@link For#toPredicate}</li>
   *   <li> {@link GFLWOR#inlineLets}</li>
   * </ul>
   * The following tests might return false:
   * <ul>
   *   <li>{@link Preds#inlineable} if one of the variables is used within a predicate.</li>
   *   <li>{@link Path#inlineable} if the variable occurs within the path.</li>
   *   <li>{@link SimpleMap#inlineable} if the variable occurs in a right-hand expression.</li>
   * </ul>
   * @param var variable to be inlined
   * @return result of check
   */
  public abstract boolean inlineable(Var var);

  /**
   * Checks how often a variable is used in this expression.
   * This function is called by:
   * <ul>
   *   <li> {@link GFLWOR#simplify}, {@link GFLWOR#inlineLets}, {@link GFLWOR#optimizePos}
   *     and {@link GFLWOR#unusedVars}</li>
   *   <li> {@link SwitchGroup#countCases}</li>
   * </ul>
   * @param var variable to look for
   * @return how often the variable is used, see {@link VarUsage}
   */
  public abstract VarUsage count(Var var);

  /**
   * Inlines an expression into this one, replacing all references to the given variable.
   * This function is called by:
   * <ul>
   *   <li> {@link Catch#inline(QueryException, CompileContext)}</li>
   *   <li> {@link Closure#optimize}</li>
   *   <li> {@link For#toPredicate}</li>
   *   <li> {@link GFLWOR#inlineLets}</li>
   *   <li> {@link TypeswitchGroup#inline}</li>
   * </ul>
   * The variable reference is replaced in:
   * <ul>
   *   <li> {@link VarRef#inline}</li>
   *   <li> {@link OrderBy#inline}</li>
   * </ul>
   * @param var variable to replace
   * @param ex expression to inline
   * @param cc compilation context
   * @return resulting expression if something changed, {@code null} otherwise
   * @throws QueryException query exception
   */
  public abstract Expr inline(Var var, Expr ex, CompileContext cc) throws QueryException;

  /**
   * Inlines the given expression into all elements of the given array.
   * @param var variable to replace
   * @param expr expression to inline
   * @param array array
   * @param cc compilation context
   * @return {@code true} if the array has changed, {@code false} otherwise
   * @throws QueryException query exception
   */
  protected static boolean inlineAll(final Var var, final Expr expr, final Expr[] array,
      final CompileContext cc) throws QueryException {

    boolean changed = false;
    final int al = array.length;
    for(int a = 0; a < al; a++) {
      final Expr ex = array[a].inline(var, expr, cc);
      if(ex != null) {
        array[a] = ex;
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Copies an expression. Used for inlining functions, or for copying static queries.
   * It is utilized by {@link VarRef#inline}, {@link FuncItem#inline},
   * {@link Closure#inline} and {@link StaticFunc#inline}.
   * @param cc compilation context
   * @param vm mapping from old variable IDs to new variable copies.
   *           Required by {@link Closure#copy} and {@link VarRef#copy}
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
    return st.type instanceof NodeType && st.oneOrMore() && !has(Flag.NDT) ?
      cc.replaceEbv(this, Bln.TRUE) : this;
  }

  /**
   * Returns the static type of the resulting value.
   * @return result of check
   */
  public abstract SeqType seqType();

  /**
   * Returns the function type of this expression.
   * @return function type, or {@code null} if expression yields no functions
   */
  public FuncType funcType() {
    final Type type = seqType().type;
    return type instanceof FuncType ? (FuncType) type : null;
  }

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
   * Checks if this expression is deterministic, performs no updates, does not access the context
   * value and position, and calls no higher-order function.
   * @return result of check
   */
  public final boolean isSimple() {
    return !has(Flag.CTX, Flag.NDT, Flag.HOF, Flag.POS);
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
   * Tries to merge two expressions.
   * @param expr second expression
   * @param union union or intersection
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Expr merge(final Expr expr, final boolean union, final CompileContext cc)
      throws QueryException {
    return null;
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
  public abstract boolean accept(ASTVisitor visitor);

  /**
   * Visit all given expressions with the given visitor.
   * @param visitor visitor
   * @param exprs expressions to visit
   * @return success flag
   */
  protected static boolean visitAll(final ASTVisitor visitor, final Expr...exprs) {
    for(final Expr expr : exprs) {
      if(!expr.accept(visitor)) return false;
    }
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
  /**
   * {@inheritDoc}
   * <div>
   * This function is e.g. called by:
   * <ul>
   *   <li>{@link If#optimize(CompileContext)}, {@link Switch#optimize(CompileContext)},
   *     {@link Typeswitch#optimize(CompileContext)}, in order to discard identical expressions.
   *   </li>
   *   <li>{@link CmpR#merge(Expr, boolean, CompileContext)} or
   *     {@link CmpSR#merge(Expr, boolean, CompileContext)},
   *     in order to merge expressions with identical input.
   *   </li>
   *   <li>{@link CmpG#optimize(CompileContext)} or {@link CmpV#optimize(CompileContext)},
   *     in order to pre-evaluate equality tests.
   *   </li>
   *   <li>{@link CmpG#optimize(CompileContext)} or
   *     {@link Pos#get(Expr, CmpV.OpV, InputInfo, CompileContext)},
   *     in order to compare the start and end value.
   *   </li>
   *   <li>{@link PathCache}, in order to find identical root values at runtime.
   *   </li>
   * </ul>
   * </div>
   */
  @Override
  public abstract boolean equals(Object obj);
}
