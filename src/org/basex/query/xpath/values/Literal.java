package org.basex.query.xpath.values;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.query.xpath.XPContext;
import org.basex.util.Levenshtein;
import org.basex.util.Token;

/**
 * XPath Literal.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class Literal extends Item {
  /** Precedence. */
  private static final int PREC = 1;
  /** byte[] value. */
  private final byte[] bytes;
  /** Cached double value. */
  private double dbl = Double.NaN;

  /**
   * Constructor.
   * @param chars Characters to represent
   */
  public Literal(final byte[] chars) {
    bytes = chars;
  }

  @Override
  public Literal eval(final XPContext ctx) {
    return this;
  }

  @Override
  public boolean bool() {
    return bytes.length > 0;
  }

  @Override
  public byte[] str() {
    return bytes;
  }

  @Override
  public double num() {
    if(dbl != dbl) dbl = Token.toDouble(bytes);
    return dbl;
  }

  @Override
  public int prec() {
    return PREC;
  }

  @Override
  public boolean eq(final Item val) {
    return val.prec() > PREC ? val.eq(this) :
      Token.eq(val.str(), str());
  }

  @Override
  public boolean appr(final Item val) {
    return val.prec() > PREC ? val.appr(this) :
      Levenshtein.similar(val.str(), str());
  }

  @Override
  public String toString() {
    return "\"" + Token.string(bytes) + "\"";
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ser.text(bytes);
    ser.closeElement();
  }
}
