package org.basex.query.xpath.values;

import java.io.IOException;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.util.Levenshtein;

/**
 * Interface for all XPath items (results of expressions).
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 * @author Christian Gruen
 */
public abstract class Item extends Expr implements Result {
  /**
   * Returns the evaluation precedence.
   * @return evaluation precedence
   */
  abstract int prec();

  /**
   * Returns the boolean value.
   * @return boolean value
   */
  public abstract boolean bool();

  /**
   * Returns the literal value.
   * @return literal value
   */
  public abstract byte[] str();

  /**
   * Returns the double value.
   * @return double value
   */
  public abstract double num();

  /**
   * Checks if the value is less than the specified value.
   * @param v value to be compared
   * @return result of comparison.
   */
  protected boolean lt(final Item v) {
    return v instanceof NodeSet ? v.gt(this) : num() < v.num();
  }

  /**
   * Checks if the value is less than or equal to the specified value.
   * @param v value to be compared
   * @return result of comparison.
   */
  protected boolean le(final Item v) {
    return v instanceof NodeSet ? v.ge(this) : num() <= v.num();
  }

  /**
   * Checks if the value is greater than the specified value.
   * @param v value to be compared
   * @return result of comparison.
   */
  protected boolean gt(final Item v) {
    return v instanceof NodeSet ? v.lt(this) : num() > v.num();
  }

  /**
   * Checks if the value is greater than or equal to the specified value.
   * @param v value to be compared
   * @return result of comparison.
   */
  protected boolean ge(final Item v) {
    return v instanceof NodeSet ? v.le(this) : num() >= v.num();
  }

  /**
   * Checks the equality of the value and the specified value.
   * @param v value to be compared
   * @return result of comparison.
   */
  public abstract boolean eq(Item v);

  /**
   * Checks the approximate equality of the value and the specified value.
   * @param v value to be compared
   * @return result of comparison.
   */
  protected abstract boolean appr(Item v);

  /**
   * Checks whether this value approximately contains the word(s) in val.
   * @param v value to be compared
   * @return result of comparison.
   */
  protected boolean apprContains(final Item v) {
    return v instanceof NodeSet ? ((NodeSet) v).apprContainedIn(this) :
      Levenshtein.contains(str(), v.str());
  }

  @Override
  public final Expr compile(final XPContext ctx) {
    return this;
  }

  @Override
  public final boolean usesSize() {
    return false;
  }

  @Override
  public final boolean usesPos() {
    return false;
  }

  @Override
  public abstract String toString();

  /** {@inheritDoc} */
  public int size() {
    return 1;
  }

  /** {@inheritDoc} */
  public final boolean same(final Result v) {
    return v instanceof Item && eq((Item) v);
  }
  
  /** {@inheritDoc} */
  public void serialize(final Serializer ser) throws IOException {
    ser.open(1);
    ser.openResult();
    ser.item(str());
    ser.closeResult();
    ser.close(1);
  }
  
  /** {@inheritDoc} */
  public void serialize(final Serializer ser, final int n) throws IOException {
    ser.item(str());
  }
}
