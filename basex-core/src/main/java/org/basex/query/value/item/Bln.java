package org.basex.query.value.item;

import java.math.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Boolean item ({@code xs:boolean}).
 *
 * @author BaseX Team 2005-18, BSD License
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
   * @param score score value
   */
  private Bln(final double score) {
    this(true);
    this.score = score;
  }

  /**
   * Constructor, adding a full-text score.
   * @param value boolean value
   * @param score score value
   * @return item
   */
  public static Bln get(final boolean value, final double score) {
    return value && score != 0 ? new Bln(score) : get(value);
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
  public byte[] string(final InputInfo info) {
    return Token.token(value);
  }

  @Override
  public boolean bool(final InputInfo info) {
    return value;
  }

  @Override
  public long itr(final InputInfo info) {
    return value ? 1 : 0;
  }

  @Override
  public float flt(final InputInfo info) {
    return value ? 1 : 0;
  }

  @Override
  public double dbl(final InputInfo info) {
    return value ? 1 : 0;
  }

  @Override
  public BigDecimal dec(final InputInfo info) {
    return value ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  @Override
  public boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo info) throws QueryException {
    return value == (item.type == type ? item.bool(info) : parse(item, info));
  }

  @Override
  public int diff(final Item item, final Collation coll, final InputInfo info)
      throws QueryException {
    final boolean n = item.type == type ? item.bool(info) : parse(item, info);
    return value ? n ? 0 : 1 : n ? -1 : 0;
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
  public String toString() {
    return Strings.concat(value ? Token.TRUE : Token.FALSE, "()");
  }

  /**
   * Converts the specified item to a boolean.
   * @param item item to be converted
   * @param info input info
   * @return resulting boolean value
   * @throws QueryException query exception
   */
  public static boolean parse(final Item item, final InputInfo info) throws QueryException {
    final Boolean b = parse(item.string(info));
    if(b != null) return b;
    throw AtomType.BLN.castError(item, info);
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
