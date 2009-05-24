package org.basex.query.item;

import static org.basex.query.QueryText.*;
import java.math.BigDecimal;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.Token;

/**
 * Boolean item.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Bln extends Item {
  /** Static boolean item (use with care). */
  public static final Bln TRUE = new Bln(true, 0);
  /** Static boolean item (use with care). */
  public static final Bln FALSE = new Bln(false, 0);
  /** Data. */
  private final boolean val;
  
  /**
   * Constructor, adding a full-text score.
   * @param b boolean value
   * @param s score value
   */
  private Bln(final boolean b, final double s) {
    super(Type.BLN);
    val = b;
    score = s;
  }

  /**
   * Constructor, adding a full-text score.
   * @param s score value
   * @return item
   */
  public static Bln get(final double s) {
    return s == 0 ? Bln.FALSE : new Bln(true, s);
  }

  /**
   * Returns a static item instance.
   * @param b boolean value
   * @return item
   */
  public static Bln get(final boolean b) {
    return b ? TRUE : FALSE;
  }

  @Override
  public byte[] str() {
    return Token.token(val);
  }

  @Override
  public boolean bool() {
    return val;
  }

  @Override
  public long itr() {
    return val ? 1 : 0;
  }

  @Override
  public float flt() {
    return val ? 1 : 0;
  }

  @Override
  public double dbl() {
    return val ? 1 : 0;
  }

  @Override
  public BigDecimal dec() {
    return val ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  @Override
  public boolean eq(final Item it) throws QueryException {
    return val == (it.type == type ? it.bool() : check(it.str()));
  }

  @Override
  public int diff(final Item it) throws QueryException {
    final boolean n = it.type == type ? it.bool() : check(it.str());
    return val ? !n ? 1 : 0 : n ? -1 : 0;
  }

  @Override
  public Boolean java() {
    return val;
  }

  /**
   * Checks if the specified value is a correct boolean string.
   * @param str string to be checked
   * @return result of check
   * @throws QueryException evaluation exception
   */
  public static boolean check(final byte[] str) throws QueryException {
    final byte[] s = Token.trim(str);
    if(Token.eq(s, Token.TRUE) || Token.eq(s, Token.ONE)) return true;
    if(Token.eq(s, Token.FALSE) || Token.eq(s, Token.ZERO)) return false;
    Err.or(CASTBOOL, str);
    return false;
  }

  @Override
  public String toString() {
    return val ? "true()" : "false()";
  }
}
