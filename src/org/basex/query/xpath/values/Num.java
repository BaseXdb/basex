package org.basex.query.xpath.values;

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
public final class Num extends Item {
  /** Static number. */
  public static final Num ZERO = new Num(0);
  /** Static number. */
  public static final Num ONE = new Num(1);
  /** Precedence. */
  private static final int PREC = 3;
  /** Number value. */
  private final double num;

  /**
   * Constructor.
   * @param n number to be represented.
   */
  public Num(final double n) {
    num = n;
  }

  /**
   * Constructor.
   * @param n number to be represented.
   */
  public Num(final long n) {
    num = n;
  }

  /**
   * Constructor.
   * @param n number to be represented.
   */
  public Num(final byte[] n) {
    num = Token.toDouble(n);
  }

  @Override
  public Num eval(final XPContext ctx) {
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
    return v.prec() > PREC ? v.eq(this) :
      Levenshtein.similar(v.str(), str());
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
