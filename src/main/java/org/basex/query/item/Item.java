package org.basex.query.item;

import static org.basex.query.util.Err.*;
import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import java.math.BigDecimal;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.iter.ValueIter;
import org.basex.util.InputInfo;
import org.basex.util.Token;

/**
 * Abstract item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Item extends Value {
  /** Undefined item. */
  public static final int UNDEF = Integer.MIN_VALUE;
  /** Dummy item. */
  public static final Item DUMMY = new Item(Type.ITEM) {
    @Override public byte[] atom() { return Token.EMPTY; }
    @Override public boolean eq(final InputInfo ii, final Item it) {
      return false;
    }
  };
  /** Score value. Will be {@code null} if not assigned. */
  protected Double score;

  /**
   * Constructor.
   * @param t data type
   */
  protected Item(final Type t) {
    super(t);
  }

  @Override
  public ValueIter iter() {
    return new ValueIter() {
      private boolean more;
      @Override
      public Item next() { return (more ^= true) ? Item.this : null; }
      @Override
      public long size() { return 1; }
      @Override
      public Item get(final long i) { return Item.this; }
      @Override
      public boolean reset() { more = false; return true; }
      @Override
      public Value finish() { return Item.this; }
    };
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) {
    return this;
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii) {
    return this;
  }

  @Override
  public Item test(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return bool(ii) ? this : null;
  }

  @Override
  public final boolean item() {
    return true;
  }

  /**
   * Returns an atomized string.
   * @return string representation
   */
  public abstract byte[] atom();

  /**
   * Returns a boolean representation of the value.
   * @param ii input info
   * @return boolean value
   * @throws QueryException query exception
   */
  public boolean bool(final InputInfo ii) throws QueryException {
    throw CONDTYPE.thrw(ii, type, this);
  }

  /**
   * Returns a decimal representation of the value.
   * @param ii input info
   * @return decimal value
   * @throws QueryException query exception
   */
  public BigDecimal dec(final InputInfo ii) throws QueryException {
    return Dec.parse(atom(), ii);
  }

  /**
   * Returns an integer (long) representation of the value.
   * @param ii input info
   * @return long value
   * @throws QueryException query exception
   */
  public long itr(final InputInfo ii) throws QueryException {
    return Itr.parse(atom(), ii);
  }

  /**
   * Returns a float representation of the value.
   * @param ii input info
   * @return float value
   * @throws QueryException query exception
   */
  public float flt(final InputInfo ii) throws QueryException {
    return Flt.parse(atom(), ii);
  }

  /**
   * Returns a double representation of the value.
   * @param ii input info
   * @return double value
   * @throws QueryException query exception
   */
  public double dbl(final InputInfo ii) throws QueryException {
    return Dbl.parse(atom(), ii);
  }

  @Override
  public Object toJava() {
    return Token.string(atom());
  }

  /**
   * Checks if the items can be compared.
   * Items are comparable
   * @param b second item
   * @return result of check
   */
  public final boolean comparable(final Item b) {
    return type == b.type || num() && b.num() || (unt() || str()) &&
      (b.str() || b.unt()) || dur() && b.dur();
  }

  /**
   * Checks the items for equality.
   * @param ii input info
   * @param it item to be compared
   * @return result of check
   * @throws QueryException query exception
   */
  public abstract boolean eq(final InputInfo ii, final Item it)
      throws QueryException;

  /**
   * Checks the items for equivalence.
   * @param ii input info
   * @param it item to be compared
   * @return result of check
   * @throws QueryException query exception
   */
  public final boolean equiv(final InputInfo ii, final Item it)
      throws QueryException {

    // [CG] XQuery: check when/if comparable items may lead to exceptions

    // check if both values are NaN, or if values are equal..
    return (this == Dbl.NAN || this == Flt.NAN) && it.num() &&
        Double.isNaN(it.dbl(ii)) || comparable(it) && eq(ii, it);
  }

  /**
   * Returns the difference between the current and the specified item.
   * @param ii input info
   * @param it item to be compared
   * @return difference
   * @throws QueryException query exception
   */
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    throw (this == it ? TYPECMP : XPTYPECMP).thrw(ii, type, it.type);
  }

  @Override
  public SeqType type() {
    return type.seq();
  }

  @Override
  public final long size() {
    return 1;
  }

  @Override
  public final boolean duplicates() {
    return false;
  }

  /**
   * Returns a score value.
   * @return score value
   */
  public double score() {
    return score == null ? 0 : score;
  }

  /**
   * Sets a new score value.
   * @param s score value
   */
  public final void score(final double s) {
    if(score != null || s != 0) score = s;
  }

  /**
   * Serializes the item.
   * @param ser serializer
   * @throws IOException I/O exception
   */
  public void serialize(final Serializer ser) throws IOException {
    ser.item(atom());
  }

  /**
   * Throws a cast error.
   * @param val cast value
   * @param ii input info
   * @throws QueryException query exception
   */
  protected final void castErr(final Object val, final InputInfo ii)
      throws QueryException {
    FUNCAST.thrw(ii, type, val);
  }

  /**
   * Throws a date format exception.
   * @param i input
   * @param ex example format
   * @param ii input info
   * @throws QueryException query exception
   */
  public void dateErr(final byte[] i, final String ex, final InputInfo ii)
      throws QueryException {
    DATEFORMAT.thrw(ii, type, i, ex);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(ITM, VAL, atom(), TYP, Token.token(name()));
  }

  @Override
  public int hash() {
    return Token.hash(atom());
  }

  @Override
  public String toString() {
    return Token.string(atom());
  }
}
