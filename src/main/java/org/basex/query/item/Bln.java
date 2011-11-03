package org.basex.query.item;

import static org.basex.query.util.Err.*;
import java.math.BigDecimal;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.TokenBuilder;

/**
 * Boolean item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Bln extends Item {
  /** Static boolean item without scoring. */
  public static final Bln TRUE = new Bln(true);
  /** Static boolean item without scoring. */
  public static final Bln FALSE = new Bln(false);
  /** Data. */
  private final boolean val;

  /**
   * Constructor, adding a full-text score.
   * @param b boolean value
   */
  private Bln(final boolean b) {
    super(AtomType.BLN);
    val = b;
  }

  /**
   * Constructor, adding a full-text score.
   * @param b boolean value
   * @param s score value
   */
  private Bln(final boolean b, final double s) {
    this(b);
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
  public byte[] atom(final InputInfo ii) {
    return Token.token(val);
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return val;
  }

  @Override
  public long itr(final InputInfo ii) {
    return val ? 1 : 0;
  }

  @Override
  public float flt(final InputInfo ii) {
    return val ? 1 : 0;
  }

  @Override
  public double dbl(final InputInfo ii) {
    return val ? 1 : 0;
  }

  @Override
  public BigDecimal dec(final InputInfo ii) {
    return val ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    return val == (it.type == type ? it.bool(ii) : parse(it.atom(ii), ii));
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    final boolean n = it.type == type ? it.bool(ii) : parse(it.atom(ii), ii);
    return val ? !n ? 1 : 0 : n ? -1 : 0;
  }

  @Override
  public Boolean toJava() {
    return val;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Bln && val == ((Bln) cmp).val;
  }

  /**
   * Converts the specified string to a boolean.
   * @param str string to be checked
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  public static boolean parse(final byte[] str, final InputInfo ii)
      throws QueryException {

    final byte[] s = Token.trim(str);
    if(Token.eq(s, Token.TRUE) || Token.eq(s, Token.ONE)) return true;
    if(Token.eq(s, Token.FALSE) || Token.eq(s, Token.ZERO)) return false;
    throw FUNCAST.thrw(ii, AtomType.BLN, str);
  }

  @Override
  public String toString() {
    return new TokenBuilder(val ? Token.TRUE : Token.FALSE).
        add("()").toString();
  }
}
