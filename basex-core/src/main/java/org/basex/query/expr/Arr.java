package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.Function;
import org.basex.query.func.fn.*;
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
 * @author BaseX Team 2005-20, BSD License
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
  public Expr inline(final ExprInfo ei, final Expr ex, final CompileContext cc)
      throws QueryException {
    return inlineAll(ei, ex, exprs, cc) ? optimize(cc) : null;
  }

  /**
   * Inlines an expression into this one, replacing all variable or context references.
   * @param ei {@link Var}, {@link Path} or context ({@code null}) to inline
   * @param ex expression to inline
   * @param cc compilation context
   * @param context function for context inlining
   * @return resulting expression if something changed, {@code null} otherwise
   * @throws QueryException query exception
   */
  public Expr inline(final ExprInfo ei, final Expr ex, final CompileContext cc,
      final QuerySupplier<Expr> context) throws QueryException {

    // no arguments are inlined: return null or apply context inlining function
    if(!inlineAll(ei, ex, exprs, cc)) {
      return ei != null ? null : context.get();
    }

    // optimize new expression. skip further optimizations if variable was inlined
    final Expr opt = optimize(cc);
    if(ei != null) return opt;

    // inline again if context was inlined
    final Expr inlined = opt.inline(ei, ex, cc);
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
   * @param limit check if result size of any expression exceeds {@link CompileContext#MAX_PREEVAL}
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
   * Simplifies all expressions for requests of the specified type.
   * @param mode mode of simplification
   * @param cc compilation context
   * @return {@code true} if at least one expression has changed
   * @throws QueryException query exception
   */
  protected boolean simplifyAll(final Simplify mode, final CompileContext cc)
      throws QueryException {

    boolean changed = false;
    final int el = exprs.length;
    for(int e = 0; e < el; e++) {
      final Expr expr = exprs[e].simplifyFor(mode, cc);
      if(expr != exprs[e]) {
        exprs[e] = expr;
        changed = true;
      }
    }
    return changed;
  }

  /**
   * Flattens nested expressions.
   * @param cc compilation context
   */
  protected void flatten(final CompileContext cc) {
    // flatten nested expressions
    final ExprList list = new ExprList(exprs.length);
    final Class<? extends Arr> clazz = getClass();
    for(final Expr expr : exprs) {
      if(clazz.isInstance(expr)) {
        list.add(((Arr) expr).exprs);
        cc.info(OPTFLAT_X_X, expr, (Supplier<?>) this::description);
      } else {
        list.add(expr);
      }
    }
    exprs = list.finish();
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
   * Tries to merge consecutive EBV tests.
   * @param or union or intersection
   * @param positional consider positional tests
   * @param cc compilation context
   * @return {@code true} if evaluation can be skipped
   * @throws QueryException query exception
   */
  boolean optimizeEbv(final boolean or, final boolean positional, final CompileContext cc)
      throws QueryException {

    final ExprList list = new ExprList(exprs.length);
    boolean pos = false;
    for(final Expr expr : exprs) {
      // pre-evaluate values
      if(expr instanceof Value) {
        // skip evaluation: true() or $bool  ->  true()
        if(expr.ebv(cc.qc, info).bool(info) ^ !or) return true;
        // ignore result: true() and $bool  ->  $bool
        cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
      } else if(!pos && list.contains(expr) && !expr.has(Flag.NDT)) {
        // ignore duplicates: A[$node and $node]  ->  A[$node]
        cc.info(OPTREMOVE_X_X, expr, (Supplier<?>) this::description);
      } else {
        list.add(expr);
        // preserve entries after positional predicates
        if(positional && !pos) pos = mayBePositional(expr);
      }
    }
    exprs = list.next();

    if(!(positional && has(Flag.POS))) {
      final Class<? extends Arr> clazz = or ? And.class : Or.class;
      final QueryBiFunction<Boolean, Expr[], Expr> func = (invert, args) ->
        (invert ? !or : or) ? new Or(info, args) : new And(info, args);
      final Expr tmp = rewrite(clazz, func, cc);
      if(tmp != null) {
        exprs = new Expr[] { tmp };
        cc.info(OPTREWRITE_X_X, (Supplier<?>) this::description, this);
      }
    }

    list.add(exprs);
    for(int l = 0; l < list.size(); l++) {
      for(int m = l + 1; m < list.size(); m++) {
        final Expr expr1 = list.get(l), expr2 = list.get(m);
        if(!(positional && expr1.has(Flag.POS))) {
          // A or not(A)  ->  true()
          // A[B][not(B)]  ->  ()
          if(Function.NOT.is(expr2) && ((Arr) expr2).exprs[0].equals(expr1) ||
             Function.NOT.is(expr1) && ((Arr) expr1).exprs[0].equals(expr2)) {
            return true;
          }
          // 'a'[. = 'a' or . = 'b']  ->  'a'[. = ('a', 'b')]
          // $v[. != 'a'][. != 'b']  ->  $v[not(. = ('a', 'b')]
          final Expr merged = expr1.mergeEbv(expr2, or, cc);
          if(merged != null) {
            cc.info(OPTSIMPLE_X_X, (Supplier<?>) this::description, this);
            list.set(l, merged);
            list.remove(m--);
          }
        }
      }
    }
    exprs = list.next();

    // not($a) and not($b)  ->  not($a or $b)
    final org.basex.query.func.Function not = org.basex.query.func.Function.NOT;
    final Checks<Expr> fnNot = ex -> not.is(ex) && !(positional && ex.has(Flag.POS));
    if(exprs.length > 1 && fnNot.all(exprs)) {
      final ExprList tmp = new ExprList(exprs.length);
      for(final Expr expr : exprs) tmp.add(((FnNot) expr).exprs[0]);
      final Expr expr = or ? new And(info, tmp.finish()) : new Or(info, tmp.finish());
      list.add(cc.function(not, info, expr.optimize(cc)));
    } else {
      list.add(exprs);
    }

    exprs = list.finish();
    return false;
  }

  /**
   * Rewrites logical and set expressions.
   * @param inverse inverse operator
   * @param newExpr function for creating a new expression
   * @param cc compilation context
   * @return optimized expression or null
   * @throws QueryException query exception
   */
  Expr rewrite(final Class<? extends Arr> inverse,
      final QueryBiFunction<Boolean, Expr[], Expr> newExpr, final CompileContext cc)
      throws QueryException {

    // skip if only one operand is left, or if children have no operands that can be optimized
    if(exprs.length < 2 || !((Checks<Expr>) expr -> inverse.isInstance(expr)).any(exprs))
      return null;

    // check if expressions have common operands
    final java.util.function.Function<Expr, ExprList> entries = ex ->
      new ExprList().add(inverse.isInstance(ex) ? ((Arr) ex).exprs : new Expr[] { ex });
    final int el = exprs.length;
    final ExprList lefts = new ExprList().add(entries.apply(exprs[0]));
    for(int e = 1; e < el && !lefts.isEmpty(); ++e) {
      final ExprList curr = entries.apply(exprs[e]);
      for(int c = lefts.size() - 1; c >= 0; c--) {
        if(!curr.contains(lefts.get(c))) lefts.remove(c);
      }
    }
    if(lefts.isEmpty()) return null;

    // common operands found: recombine expressions
    final QueryBiFunction<Boolean, Expr[], Expr> f = (invert, args) ->
      args.length == 1 ? args[0] : newExpr.apply(invert, args).optimize(cc);

    final Expr left = f.apply(true, lefts.toArray());
    final ExprList rights = new ExprList(exprs.length);
    for(final Expr expr : exprs) {
      final ExprList curr = entries.apply(expr).removeAll(lefts);
      if(curr.isEmpty()) {
        // no additional tests: return common tests
        // A intersect (A union B)  ->  A
        // (A and B) or (A and B and C)  ->  A
        return left.ddo() ? left : cc.function(Function._UTIL_DDO, info, left);
      } else if(curr.size() == 1) {
        // single additional test: add this test
        // (A and B) or (A and C)  ->  A and (B or C)
        rights.add(curr.get(0));
      } else {
        // multiple additional tests: simplify expression
        // (A and B) or (A and C and D)  ->  A and (B or (C and D))
        rights.add(f.apply(true, curr.finish()));
      }
    }
    return f.apply(true, new Expr[] { left, f.apply(false, rights.finish()) });
  }

  /**
   * Checks if the specified expression may be positional.
   * @param expr expression
   * @return result of check
   */
  protected static boolean mayBePositional(final Expr expr) {
    return expr.seqType().mayBeNumber() || expr.has(Flag.POS);
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
   * Returns a string representation of this expression.
   * Separates the array entries with the specified separator.
   * @param separator separator (operator, delimiter)
   * @return string representation
   */
  protected String toString(final String separator) {
    return new TokenBuilder().add(PAREN1).addSeparated(exprs, separator).add(PAREN2).toString();
  }
}
