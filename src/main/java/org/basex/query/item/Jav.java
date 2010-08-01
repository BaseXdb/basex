package org.basex.query.item;

import java.math.BigDecimal;
import org.basex.query.QueryException;
import org.basex.query.expr.ParseExpr;
import org.basex.util.Token;

/**
 * String item.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class Jav extends Item {
  /** Java object. */
  public final Object val;

  /**
   * Constructor.
   * @param v value
   */
  public Jav(final Object v) {
    super(Type.JAVA);
    val = v;
  }

  @Override
  public byte[] atom() {
    return Token.token(val.toString());
  }

  @Override
  public boolean bool() {
    return atom().length != 0;
  }

  @Override
  public long itr() throws QueryException {
    return Itr.parse(atom());
  }

  @Override
  public float flt() throws QueryException {
    return Flt.parse(atom());
  }

  @Override
  public double dbl() throws QueryException {
    return Dbl.parse(atom());
  }

  @Override
  public BigDecimal dec() throws QueryException {
    return Dec.parse(atom());
  }

  @Override
  public boolean eq(final Item it) {
    return Token.eq(atom(), it.atom());
  }

  @Override
  public int diff(final ParseExpr e, final Item it) {
    return Token.diff(atom(), it.atom());
  }

  @Override
  public Object toJava() {
    return val;
  }

  @Override
  public String toString() {
    return "\"" + val + "\"";
  }
}
