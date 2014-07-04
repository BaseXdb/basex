package org.basex.query.value.item;

import static org.basex.query.util.Err.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Boolean item ({@code xs:boolean}).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Bln extends Item {
  /** Static boolean item without scoring. */
  public static final Bln TRUE = new Bln(true);
  /** Static boolean item without scoring. */
  public static final Bln FALSE = new Bln(false);
  /** Data. */
  private final boolean value;

  /**
   * Constructor, adding a full-text score.
   * @param value boolean value
   */
  private Bln(final boolean value) {
    super(AtomType.BLN);
    this.value = value;
  }

  /**
   * Constructor, adding a full-text score.
   * @param value boolean value
   * @param score score value
   */
  private Bln(final boolean value, final double score) {
    this(value);
    this.score = score;
  }

  /**
   * Constructor, adding a full-text score.
   * @param score score value
   * @return item
   */
  public static Bln get(final double score) {
    return score == 0 ? FALSE : new Bln(true, score);
  }

  /**
   * Returns a static item instance.
   * @param value boolean value
   * @return item
   */
  public static Bln get(final boolean value) {
    return value ? TRUE : FALSE;
  }

  @Override
  public byte[] string(final InputInfo ii) {
    return Token.token(value);
  }

  /**
   * Returns the string value.
   * @return string value
   */
  public byte[] string() {
    return Token.token(value);
  }

  @Override
  public boolean bool(final InputInfo ii) {
    return value;
  }

  @Override
  public long itr(final InputInfo ii) {
    return value ? 1 : 0;
  }

  @Override
  public float flt(final InputInfo ii) {
    return value ? 1 : 0;
  }

  @Override
  public double dbl(final InputInfo ii) {
    return value ? 1 : 0;
  }

  @Override
  public BigDecimal dec(final InputInfo ii) {
    return value ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  @Override
  public boolean eq(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return value == (it.type == type ? it.bool(ii) : parse(it.string(ii), ii));
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    final boolean n = it.type == type ? it.bool(ii) : parse(it.string(ii), ii);
    return value ? n ? 0 : 1 : n ? -1 : 0;
  }

  @Override
  public Boolean toJava() {
    return value;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof Bln && value == ((Bln) cmp).value;
  }

  /**
   * Converts the specified string to a boolean.
   * @param value string to be checked
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  public static boolean parse(final byte[] value, final InputInfo ii) throws QueryException {
    final byte[] s = Token.trim(value);
    if(Token.eq(s, Token.TRUE) || Token.eq(s, Token.ONE)) return true;
    if(Token.eq(s, Token.FALSE) || Token.eq(s, Token.ZERO)) return false;
    throw FUNCAST.get(ii, AtomType.BLN, chop(value));
  }

  @Override
  public String toString() {
    return new TokenBuilder(value ? Token.TRUE : Token.FALSE).add("()").toString();
  }
}
