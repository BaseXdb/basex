package org.basex.query.xpath.item;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xpath.XPContext;
import org.basex.util.Token;

/**
 * XPath Value representing a boolean.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Bln extends Item {
  /** Precedence. */
  private static final int PREC = 4;
  /** Static true value. */
  public static final Bln TRUE = new Bln(true);
  /** Static false value. */
  public static final Bln FALSE = new Bln(false);
  /** boolean value. */
  private final boolean value;
  
  /**
   * Constructor.
   * @param val boolean to be represented
   */
  private Bln(final boolean val) {
    value = val;
  }

  /**
   * Returns an instance of this class.
   * @param val boolean value
   * @return class instance
   */
  public static Bln get(final boolean val) {
    return val ? TRUE : FALSE;
  }

  @Override
  public Bln eval(final XPContext ctx) {
    return this;
  }

  @Override
  public boolean bool() {
    return value;
  }

  @Override
  public double num() {
    return value ? 1 : 0;
  }

  @Override
  public byte[] str() {
    return value ? Token.TRUE : Token.FALSE;
  }

  @Override
  public int prec() {
    return PREC;
  }

  @Override
  public boolean eq(final Item v) {
    return v.prec() > PREC ? v.eq(this) : v.bool() == value;
  }

  @Override
  public boolean appr(final Item v) {
    return eq(v);
  }

  @Override
  public boolean apprContains(final Item v) {
    return eq(v);
  }

  @Override
  public String toString() {
    return Token.string(str());
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ser.text(Token.token(value));
    ser.closeElement();
  }
}
