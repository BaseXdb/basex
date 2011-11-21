package org.basex.query.item;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import org.basex.io.in.ArrayInput;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.iter.ValueIter;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;

/**
 * Abstract item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class Item extends Value {
  /** Undefined item. */
  public static final int UNDEF = Integer.MIN_VALUE;
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
  public final ValueIter iter() {
    return new ItemIter(this);
  }

  @Override
  public final Item item(final QueryContext ctx, final InputInfo ii) {
    return this;
  }

  @Override
  public final Item itemAt(final long pos) {
    return this;
  }

  @Override
  public final Item ebv(final QueryContext ctx, final InputInfo ii) {
    return this;
  }

  @Override
  public final Item test(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return bool(ii) ? this : null;
  }

  @Override
  public final boolean item() {
    return true;
  }

  /**
   * Returns a string representation of the value.
   * @param ii input info, use {@code null} if none is available
   * @return string value
   * @throws QueryException if the item can't be atomized
   */
  public abstract byte[] string(final InputInfo ii) throws QueryException;

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
    return Dec.parse(string(ii), ii);
  }

  /**
   * Returns an integer (long) representation of the value.
   * @param ii input info
   * @return long value
   * @throws QueryException query exception
   */
  public long itr(final InputInfo ii) throws QueryException {
    return Int.parse(string(ii), ii);
  }

  /**
   * Returns a float representation of the value.
   * @param ii input info
   * @return float value
   * @throws QueryException query exception
   */
  public float flt(final InputInfo ii) throws QueryException {
    return Flt.parse(string(ii), ii);
  }

  /**
   * Returns a double representation of the value.
   * @param ii input info
   * @return double value
   * @throws QueryException query exception
   */
  public double dbl(final InputInfo ii) throws QueryException {
    return Dbl.parse(string(ii), ii);
  }

  /**
   * Checks if the items can be compared.
   * Items are comparable
   * @param b second item
   * @return result of check
   */
  public final boolean comparable(final Item b) {
    return type == b.type || isNumber() && b.isNumber() ||
        (isUntyped() || isString()) && (b.isString() || b.isUntyped()) ||
        isDuration() && b.isDuration() || isFunction();
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

    // check if both values are NaN, or if values are equal..
    return (this == Dbl.NAN || this == Flt.NAN) && it.isNumber() &&
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

  /**
   * Returns an input stream.
   * @return content
   * @throws IOException I/O exception
   */
  public InputStream input() throws IOException {
    try {
      return new ArrayInput(string(null));
    } catch(final QueryException ex) {
      throw new IOException(ex.getMessage(), ex);
    }
  }

  @Override
  public final SeqType type() {
    return type.seq();
  }

  @Override
  public final long size() {
    return 1;
  }

  @Override
  public final boolean iterable() {
    return true;
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
    // this method is overwritten by some data types
    ser.item(this);
  }

  /**
   * Throws a cast error.
   * @param val cast value
   * @param ii input info
   * @return never
   * @throws QueryException query exception
   */
  protected final QueryException castErr(final Object val, final InputInfo ii)
      throws QueryException {
    return FUNCAST.thrw(ii, type, val);
  }

  /**
   * Throws a date format exception.
   * @param i input
   * @param ex example format
   * @param ii input info
   * @return never
   * @throws QueryException query exception
   */
  public final QueryException dateErr(final byte[] i, final String ex,
      final InputInfo ii) throws QueryException {
    throw DATEFORMAT.thrw(ii, type, i, ex);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    try {
      ser.emptyElement(ITM, VAL, string(null), TYP, Token.token(name()));
    } catch(final QueryException ex) {
      // only function items throw exceptions in atomization, and they should
      // override plan(Serializer) sensibly
      Util.notexpected(ex);
    }
  }

  @Override
  public int hash(final InputInfo ii) throws QueryException {
    return Token.hash(string(ii));
  }

  @Override
  public final int writeTo(final Item[] arr, final int start) {
    arr[start] = this;
    return 1;
  }

  @Override
  public final boolean homogenous() {
    return true;
  }

  /**
   * Item iterator.
   * @author BaseX Team 2005-11, BSD License
   * @author Christian Gruen
   */
  private static final class ItemIter extends ValueIter {
    /** Item. */
    private final Item item;
    /** Requested flag. */
    private boolean req;
    /**
     * Constructor.
     * @param it item
     */
    ItemIter(final Item it) { item = it; }
    @Override
    public Item next() { if(req) return null; req = true; return item; }
    @Override
    public long size() { return 1; }
    @Override
    public Item get(final long i) { return item; }
    @Override
    public boolean reset() { req = false; return true; }
    @Override
    public Value value() { return item; }
  }
}
