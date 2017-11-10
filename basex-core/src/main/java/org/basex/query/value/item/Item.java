package org.basex.query.value.item;

import static org.basex.data.DataText.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.math.*;

import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.Type.*;
import org.basex.util.*;

/**
 * Abstract super class for all items.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class Item extends Value {
  /** Undefined item. */
  public static final int UNDEF = Integer.MIN_VALUE;
  /** Score value. {@code null} reference takes less memory on 32bit than a double. */
  protected Double score;

  /**
   * Constructor.
   * @param type item type
   */
  protected Item(final Type type) {
    super(type);
  }

  @Override
  public final BasicIter<Item> iter() {
    return new BasicIter<Item>(1) {
      @Override
      public Item get(final long i) {
        return Item.this;
      }
      @Override
      public Value value(final QueryContext qc) {
        return Item.this;
      }
    };
  }

  @Override
  public final Item item(final QueryContext qc, final InputInfo ii) {
    return this;
  }

  @Override
  public final Item itemAt(final long pos) {
    return this;
  }

  @Override
  public final Item reverse() {
    return this;
  }

  @Override
  public final Item ebv(final QueryContext qc, final InputInfo ii) {
    return this;
  }

  @Override
  public Item test(final QueryContext qc, final InputInfo ii) throws QueryException {
    return bool(ii) ? this : null;
  }

  /**
   * Returns a string representation of the value.
   * @param ii input info, use {@code null} if none is available
   * @return string value
   * @throws QueryException if the item cannot be atomized (caused by function or streaming items)
   */
  public abstract byte[] string(InputInfo ii) throws QueryException;

  /**
   * Returns a boolean representation of the value.
   * @param ii input info
   * @return boolean value
   * @throws QueryException query exception
   */
  public boolean bool(final InputInfo ii) throws QueryException {
    throw EBV_X_X.get(ii, type, this);
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
   * Checks if this item is instance of the specified type.
   * @param tp type
   * @return result of check
   */
  public boolean instanceOf(final Type tp) {
    return type.instanceOf(tp);
  }

  /**
   * Checks if the items can be compared.
   * @param it item to be compared
   * @return result of check
   */
  public final boolean comparable(final Item it) {
    final Type t1 = type, t2 = it.type;
    return t1 == t2
        || t1.isStringOrUntyped() && t2.isStringOrUntyped()
        || this instanceof ANum && it instanceof ANum
        || this instanceof Dur && it instanceof Dur;
  }

  /**
   * Compares the items for equality.
   * @param it item to be compared
   * @param coll collation (can be {@code null})
   * @param sc static context; required for comparing items of type xs:QName
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  public abstract boolean eq(Item it, Collation coll, StaticContext sc, InputInfo ii)
      throws QueryException;

  /**
   * Compares the items for equivalence. As item is equivalent to another if:
   * <ul>
   *   <li>both numeric values are NaN, or</li>
   *   <li>if the items have comparable types and are equal</li>
   * </ul>
   * @param it item to be compared
   * @param coll collation (can be {@code null})
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  public final boolean equiv(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    return (this == Dbl.NAN || this == Flt.NAN) && (it == Dbl.NAN || it == Flt.NAN) ||
        comparable(it) && eq(it, coll, null, ii);
  }

  /**
   * Compares the items for equality.
   * @param it item to be compared
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean sameKey(final Item it, final InputInfo ii) throws QueryException {
    return comparable(it) && eq(it, null, null, ii);
  }

  /**
   * Returns the difference between the current and the specified item.
   * @param it item to be compared
   * @param coll collation (can be {@code null})
   * @param ii input info
   * @return difference
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public int diff(final Item it, final Collation coll, final InputInfo ii) throws QueryException {
    throw (type == it.type ? CMPTYPE_X : CMPTYPES_X_X).get(ii, type, it.type);
  }

  /**
   * Returns an input stream.
   * @param ii input info
   * @return input stream
   * @throws QueryException query exception
   */
  public BufferInput input(final InputInfo ii) throws QueryException {
    return new ArrayInput(string(ii));
  }

  @Override
  public Value subSeq(final long start, final long len) {
    return len == 0 ? Empty.SEQ : this;
  }

  // Overridden by B64Stream, StrStream, Map and Array.
  @Override
  public void materialize(final InputInfo ii) throws QueryException { }

  // Overridden by Array.
  @Override
  public Value atomValue(final InputInfo ii) throws QueryException {
    return atomItem(ii);
  }

  @Override
  public final Item atomItem(final QueryContext qc, final InputInfo ii) throws QueryException {
    return atomItem(ii);
  }

  /**
   * Evaluates the expression and returns the atomized items.
   * @param ii input info
   * @return materialized item
   * @throws QueryException query exception
   */
  // Overridden by Array, FItem and ANode.
  @SuppressWarnings("unused")
  public Item atomItem(final InputInfo ii) throws QueryException {
    return this;
  }

  @Override
  public long atomSize() {
    return 1;
  }

  @Override
  public final SeqType seqType() {
    return type.seqType();
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
    final Double s = score;
    return s == null ? 0 : s;
  }

  /**
   * Sets a new score value (do not assign if value is 0).
   * @param s score value
   */
  public final void score(final double s) {
    if(s != 0) score = s;
  }

  @Override
  public int hash(final InputInfo ii) throws QueryException {
    return Token.hash(string(ii));
  }

  @Override
  public final int writeTo(final Item[] arr, final int index) {
    arr[index] = this;
    return 1;
  }

  @Override
  public final boolean homogeneous() {
    return true;
  }

  /**
   * Returns data model info.
   * Overwritten by xs:QName, attribute() and document-node().
   * @return type string
   */
  public byte[] xdmInfo() {
    return new byte[] { typeId().asByte() };
  }

  /**
   * Returns a type id.
   * Overwritten by document-node() to check if document has an element as child.
   * @return type string
   */
  public ID typeId() {
    return type.id();
  }

  @Override
  public void plan(final FElem plan) {
    try {
      addPlan(plan, planElem(TYPE, type), string(string(null), false, true));
    } catch(final QueryException ex) {
      // only function items throw exceptions in atomization, and they should
      // override plan(Serializer) sensibly
      throw Util.notExpected(ex);
    }
  }

  @Override
  public String description() {
    return type.toString();
  }

  /**
   * Returns a string representation of the specified value.
   * @param value value
   * @return string
   */
  public static String toString(final byte[] value) {
    return toString(value, true, true);
  }

  /**
   * Returns a string representation of the specified value.
   * @param value value
   * @param quotes wrap with quotes
   * @param limit limit output
   * @return string
   */
  public static String toString(final byte[] value, final boolean quotes, final boolean limit) {
    return Token.string(string(value, quotes, limit));
  }

  /**
   * Returns a string representation of the specified value.
   * @param value value
   * @param quotes wrap with quotes
   * @param limit limit output
   * @return string
   */
  public static byte[] string(final byte[] value, final boolean quotes, final boolean limit) {
    final TokenBuilder tb = new TokenBuilder();
    if(quotes) tb.add('"');
    for(final byte v : value) {
      if(limit && tb.size() > 255) {
        tb.add(DOTS);
        break;
      }
      if(v == '&') tb.add(E_AMP);
      else if(v == '\r') tb.add(E_CR);
      else if(v == '\n') tb.add(E_NL);
      else if(v == '"' && quotes) tb.add('"').add('"');
      else tb.addByte(v);
    }
    if(quotes) tb.add('"');
    return tb.finish();
  }
}
