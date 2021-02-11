package org.basex.query.value.item;

import java.math.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Boolean item ({@code xs:boolean}).
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Bln extends Item {
  /** Static boolean item without scoring. */
  public static final Bln TRUE = new Bln(true);
  /** Static boolean item without scoring. */
  public static final Bln FALSE = new Bln(false);
  /** Data. */
  private final boolean value;
  /** Score value. */
  private double score;

  /**
   * Constructor, adding a full-text score.
   * @param value boolean value
   */
  private Bln(final boolean value) {
    super(AtomType.BOOLEAN);
    this.value = value;
  }

  /**
   * Constructor, adding a full-text score.
   * @param score score value
   */
  private Bln(final double score) {
    this(true);
    this.score = score;
  }

  /**
   * Constructor, adding a full-text score.
   * @param score score value
   * @return item
   */
  public static Bln get(final double score) {
    return score != 0 ? new Bln(score) : Bln.FALSE;
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
  public boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    return value == (item.type == type ? item.bool(ii) : parse(item, ii));
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo ii) throws QueryException {
    final boolean n = item.type == type ? item.bool(ii) : parse(item, ii);
    return value ? n ? 0 : 1 : n ? -1 : 0;
  }

  @Override
  public double score() {
    return score;
  }

  @Override
  public Boolean toJava() {
    return value;
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Bln && value == ((Bln) obj).value;
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(value ? Token.TRUE : Token.FALSE).paren("");
  }

  // STATIC METHODS ===============================================================================

  /**
   * Converts the specified item to a boolean.
   * @param item item to be converted
   * @param ii input info
   * @return resulting boolean value
   * @throws QueryException query exception
   */
  public static boolean parse(final Item item, final InputInfo ii) throws QueryException {
    final Boolean b = parse(item.string(ii));
    if(b != null) return b;
    throw AtomType.BOOLEAN.castError(item, ii);
  }

  /**
   * Converts the specified string to a boolean.
   * @param value string to be converted
   * @return boolean value or {@code null}
   */
  public static Boolean parse(final byte[] value) {
    final byte[] v = Token.trim(value);
    if(Token.eq(v, Token.TRUE) || Token.eq(v, Token.ONE)) return Boolean.TRUE;
    if(Token.eq(v, Token.FALSE) || Token.eq(v, Token.ZERO)) return Boolean.FALSE;
    return null;
  }
}
