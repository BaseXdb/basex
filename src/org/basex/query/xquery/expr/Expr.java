package org.basex.query.xquery.expr;

import static org.basex.query.xquery.XQText.*;
import org.basex.query.ExprInfo;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Seq;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.util.Err;

/**
 * Abstract Expression.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Expr extends ExprInfo {
  /** Undefined value. */
  protected static final int UNDEF = Integer.MIN_VALUE;
  /** Used types, evaluated by the compiler. */
  public enum Using {
    /** Context position. */ POS,
    /** Variable.         */ VAR
  };
  /** Return types, evaluated by the compiler. */
  public enum Return {
    /** Numeric value.    */ NUM,
  };

  /**
   * Optimizes and compiles the expression.
   * @param ctx query context
   * @return optimized Expression
   * @throws XQException evaluation exception
   */
  public abstract Expr comp(final XQContext ctx) throws XQException;

  /**
   * Evaluates the expression and returns an iterator on the resulting items.
   * @param ctx query context
   * @return resulting item
   * @throws XQException evaluation exception
   */
  public abstract Iter iter(final XQContext ctx) throws XQException;

  /**
   * Checks if this is an item.
   * @return result of check
   */
  public boolean i() {
    return this instanceof Item;
  }

  /**
   * Returns if this is an empty sequence.
   * @return result of check
   */
  public final boolean e() {
    return this == Seq.EMPTY;
  }

  /**
   * Indicates if an expression uses the specified type.
   * Called by the compiler to check if expressions can be reformulated.
   * true is returned by default.
   * @param use using flag
   * @return result of check
   */
  @SuppressWarnings("unused")
  public boolean uses(final Using use) {
    return true;
  }

  /**
   * Indicates if an expression returns the specified type.
   * Called by the compiler to check if expressions can be reformulated.
   * null is returned by default.
   * @return result of check
   */
  public Type returned() {
    return null;
  }

  /**
   * Returns an iterator for an item expression.
   * Note that the input must be an {@link Item} instance.
   * @param expr expression
   * @return iterator
   */
  protected final Iter iter(final Expr expr) {
    return ((Item) expr).iter();
  }

  /**
   * Checks if the specified iterator is a number.
   * Returns a token representation or an exception.
   * @param iter iterator to be checked
   * @return item
   * @throws XQException evaluation exception
   */
  protected final double checkDbl(final Iter iter) throws XQException {
    final Item it = iter.atomic(this, true);
    if(it == null) Err.or(XPEMPTYNUM, info());
    if(!it.u() && !it.n()) Err.num(info(), it);
    return it.dbl();
  }

  /**
   * Checks if the specified item is a number.
   * Returns a token representation or an exception.
   * @param it item to be checked
   * @return item
   * @throws XQException evaluation exception
   */
  protected final long checkItr(final Item it) throws XQException {
    if(!it.u() && !it.type.instance(Type.ITR)) Err.type(info(), Type.ITR, it);
    return it.itr();
  }

  /**
   * Checks if the specified iterator is a number.
   * Returns a token representation or an exception.
   * @param iter iterator to be checked
   * @return item
   * @throws XQException evaluation exception
   */
  protected final long checkItr(final Iter iter) throws XQException {
    final Item it = iter.atomic(this, true);
    if(it == null) Err.or(XPEMPTYPE, info(), Type.ITR);
    return checkItr(it);
  }

  /**
   * Throws an exception if the context item is not set.
   * @param ctx xquery context
   * @return item if everything is ok
   * @throws XQException evaluation exception
   */
  protected final Iter checkCtx(final XQContext ctx) throws XQException {
    final Item it = ctx.item;
    if(it == null) Err.or(XPNOCTX, this);
    return it.iter();
  }
}
