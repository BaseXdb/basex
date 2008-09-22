package org.basex.query.xquery.item;

import java.math.BigDecimal;
import org.basex.query.xquery.XQException;
import org.basex.util.Token;

/**
 * String item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Jav extends Item {
  /** Java object. */
  public Object val;

  /**
   * Constructor.
   * @param v value
   */
  public Jav(final Object v) {
    super(Type.JAVA);
    val = v;
  }

  @Override
  public byte[] str() {
    return Token.token(val.toString());
  }

  @Override
  public boolean bool() {
    return str().length != 0;
  }

  @Override
  public long itr() throws XQException {
    return Itr.parse(str());
  }

  @Override
  public float flt() throws XQException {
    return Flt.parse(str());
  }

  @Override
  public double dbl() throws XQException {
    return Dbl.parse(str());
  }

  @Override
  public BigDecimal dec() throws XQException {
    return Dec.parse(str());
  }

  @Override
  @SuppressWarnings("unused")
  public boolean eq(final Item it) throws XQException {
    return Token.eq(str(), it.str());
  }

  @Override
  @SuppressWarnings("unused")
  public int diff(final Item it) throws XQException {
    return Token.diff(str(), it.str());
  }

  @Override
  public Object java() {
    return val;
  }

  @Override
  public String toString() {
    return "\"" + val + "\"";
  }
}
