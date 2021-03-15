package org.basex.query.expr;

import java.util.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.constr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.func.java.*;
import org.basex.query.iter.*;
import org.basex.query.up.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.index.*;
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
 * @author BaseX Team 2005-21, BSD License
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
    if(st.type.instanceOf(AtomType.ANY_ATOMIC_TYPE)) return iter;
    long size = iter.size();
    if(size != -1 && st.mayBeArray()) size = -1;
    return new AtomIter(iter, qc, ii, size);
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
  public boolean vacuous() {
    return false;
  }

  /**
   * Returns the data reference bound to this expression. This method is currently overwritten
   * by {@link DBNode}, {@link DBNodeSeq}, {@link AxisPath} and {@link VarRef}.
   * @return data reference (can be {@code null})
   */
  public Data data() {
    return null;
  }

  /**
   * Assigns a data reference, if supported.
   * @param dt data reference
   */
  public void data(@SuppressWarnings("unused") final Data dt) {
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
   * Checks if inlining is possible.
   * This function is called by {@link InlineContext#inlineable} and:
   * {@link CNode#inlineable} returns false if the new expression construct new nodes.
   *
   * The following tests might reject inlining if the expression is a context reference:
   * <ul>
   *   <li>{@link Preds#inlineable}</li>
   *   <li>{@link Path#inlineable}</li>
   *   <li>{@link SimpleMap#inlineable}</li>
   *   <li>{@link StaticJavaCall#inlineable}</li>
   *   <li>{@link TransformWith#inlineable}</li>
   * </ul>
   *
   * @param ic inlining context
   * @return result of check
   */
  public abstract boolean inlineable(InlineContext ic);

  /**
   * Checks how often a variable or context reference is used in this expression.
   *
   * This function is called by:
   * <ul>
   *   <li> {@link Closure#optimize}</li>
   *   <li> {@link GFLWOR#inlineForLet}</li>
   *   <li> {@link GFLWOR#optimizePos}</li>
   *   <li> {@link GFLWOR#simplify}</li>
   *   <li> {@link GFLWOR#unusedVars}</li>
   *   <li> {@link SimpleMap#optimize}</li>
   *   <li> {@link TypeswitchGroup#optimize}</li>
   * </ul>
   *
   * @param var variable ({@link Var} reference) or context ({@code null}) to inline
   * @return number of usages, see {@link VarUsage}
   */
  public abstract VarUsage count(Var var);

  /**
   * Inlines an expression into this one, replacing all variable or context references.
   * This function is called by {@link InlineContext#inline(Expr)} (see invocations of this
   * functions for further inlinings).
   *
   * The variable reference is replaced in:
   * <ul>
   *   <li> {@link OrderBy#inline}</li>
   *   <li> {@link VarRef#inline}</li>
   * </ul>
   * The context is replaced in:
   * <ul>
   *   <li> {@link ContextFn#inline}</li>
   *   <li> {@link ContextValue#inline}</li>
   *   <li> {@link Lookup#inline}</li>
   * </ul>
   *
   * @param ic inlining context
   * @return resulting expression if something has changed, {@code null} otherwise
   * @throws QueryException query exception
   */
  public abstract Expr inline(InlineContext ic) throws QueryException;

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
   * This function is called at compile time for expressions whose operands might be simplified.
   * @param mode mode of simplification
   * @param cc compilation context
   * @return simplified or original expression
   * @see Simplify
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // return true if a deterministic expression returns at least one node
    return (mode == Simplify.EBV || mode == Simplify.PREDICATE) &&
      seqType().instanceOf(SeqType.NODE_OM) && !has(Flag.NDT) ? cc.simplify(this, Bln.TRUE) : this;
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
   * Indicates if this expression returns nodes in document order without duplicates.
   * @return result of check
   */
  public boolean ddo() {
    final SeqType st = seqType();
    return st.zeroOrOne() && st.type instanceof NodeType;
  }

  /**
   * Returns the arguments/operands of an expression (function, list, etc).
   * @return arguments or {@code null}
   */
  public Expr[] args() {
    return null;
  }

  /**
   * Returns the specified argument/operand of an expression (function, list, etc).
   * @param i index of argument
   * @return arguments or {@code null}
   */
  public final Expr arg(final int i) {
    return args()[i];
  }

  /**
   * Checks if an expression can be rewritten to an index access.
   * If so, the index expression will be bound to {@link IndexInfo#expr}.
   * This method will be called by the {@link Path} expression.
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
   * Tries to merge two expressions that are part of an EBV test.
   * Called by {@link And}, {@link Or}), {@link Step} and {@link Filter}.
   * Overwritten by {@link CmpG}, {@link CmpIR}, {@link CmpR}, {@link CmpSR},
   * {@link ItrPos}, {@link Pos} and others.
   * @param expr second expression
   * @param or union or intersection
   * @param cc compilation context
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Expr mergeEbv(final Expr expr, final boolean or, final CompileContext cc)
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
   * Refines the expression type.
   * @param expr original expression
   */
  public abstract void refineType(Expr expr);

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
   *   <li>{@link CmpR#mergeEbv(Expr, boolean, CompileContext)} or
   *     {@link CmpSR#mergeEbv(Expr, boolean, CompileContext)},
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
