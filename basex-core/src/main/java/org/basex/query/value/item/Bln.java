package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Boolean item ({@code xs:boolean}).
 *
 * @author BaseX Team 2005-16, BSD License
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
   * @param value boolean value
   * @param score score value
   * @return item
   */
  public static Bln get(final boolean value, final double score) {
    return value && score != 0 ? new Bln(true, score) : get(value);
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
  public boolean eq(final Item it, final Collation coll, final StaticContext sc, final InputInfo ii)
      throws QueryException {
    return value == (it.type == type ? it.bool(ii) : parse(it.string(ii), ii));
  }

  @Override
  public int diff(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
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

  @Override
  public String toString() {
    return new TokenBuilder(value ? Token.TRUE : Token.FALSE).add("()").toString();
  }

  /**
   * Converts the specified string to a boolean.
   * @param value string to be checked
   * @param ii input info
   * @return resulting boolean value, or {@code null}
   * @throws QueryException query exception
   */
  public static boolean parse(final byte[] value, final InputInfo ii) throws QueryException {
    final Boolean b = parse(value);
    if(b != null) return b;
    throw funCastError(ii, AtomType.BLN, value);
  }

  /**
   * Converts the specified string to a boolean.
   * @param value string to be checked
   * @return resulting boolean value, or {@code null}
   */
  public static Boolean parse(final byte[] value) {
    final byte[] v = Token.trim(value);
    if(Token.eq(v, Token.TRUE) || Token.eq(v, Token.ONE)) return Boolean.TRUE;
    if(Token.eq(v, Token.FALSE) || Token.eq(v, Token.ZERO)) return Boolean.FALSE;
    return null;
  }
}
