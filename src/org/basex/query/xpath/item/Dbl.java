package org.basex.query.xpath.item;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xpath.XPContext;
import org.basex.util.Levenshtein;
import org.basex.util.Token;

/**
 * XPath value type modeling a number.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Dbl extends Item {
  /** Static number. */
  public static final Dbl ZERO = new Dbl(0);
  /** Static number. */
  public static final Dbl ONE = new Dbl(1);
  /** Precedence. */
  private static final int PREC = 3;
  /** Number value. */
  private final double num;

  /**
   * Constructor.
   * @param n number to be represented.
   */
  public Dbl(final double n) {
    num = n;
  }

  /**
   * Constructor.
   * @param n number to be represented.
   */
  public Dbl(final long n) {
    num = n;
  }

  @Override
  public Dbl eval(final XPContext ctx) {
    return this;
  }

  @Override
  public boolean bool() {
    // if num == num, the value is a valid double value in Java
    return num != 0 && num == num;
  }

  @Override
  public byte[] str() {
    final int n = (int) num;
    return num == n ? Token.token(n) : Token.token(num);
  }

  @Override
  public double num() {
    return num;
  }

  @Override
  public boolean eq(final Item v) {
    return v.prec() > PREC ? v.eq(this) : v.num() == num;
  }

  @Override
  public boolean appr(final Item v) {
    if(v.prec() > PREC) return v.appr(this);
    if(ls == null) ls = new Levenshtein();
    return ls.similar(v.str(), str());
  }

  @Override
  public int prec() {
    return PREC;
  }

  @Override
  public String toString() {
    return Token.string(str());
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ser.text(str());
    ser.closeElement();
  }
}
