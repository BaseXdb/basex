package org.basex.query.value.item;

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
import org.basex.query.value.type.Type.ID;
import org.basex.util.*;

/**
 * Abstract super class for all items.
 *
 * @author BaseX Team 2005-15, BSD License
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
  public final ValueIter iter() {
    return new ValueIter() {
      private boolean req;
      @Override
      public Item next() { if(req) return null; req = true; return Item.this; }
      @Override
      public long size() { return 1; }
      @Override
      public Item get(final long i) { return Item.this; }
      @Override
      public Value value() { return Item.this; }
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
   * Checks if the items can be compared.
   * @param it item to be compared
   * @return result of check
   */
  public final boolean comparable(final Item it) {
    final Type t1 = type, t2 = it.type;
    return t1 == t2 || this instanceof ANum && it instanceof ANum ||
      t1.isStringOrUntyped() && t2.isStringOrUntyped() || this instanceof Dur && it instanceof Dur;
  }

  /**
   * Checks the items for equality.
   * @param it item to be compared
   * @param coll collation
   * @param sc static context
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  public abstract boolean eq(final Item it, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException;

  /**
   * Checks the items for equivalence.
   * @param it item to be compared
   * @param coll collation
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  public final boolean equiv(final Item it, final Collation coll, final InputInfo ii)
      throws QueryException {
    // check if both values are NaN, or if values are equal..
    return (this == Dbl.NAN || this == Flt.NAN) && it instanceof ANum && Double.isNaN(it.dbl(ii)) ||
        comparable(it) && eq(it, coll, null, ii);
  }

  /**
   * Returns the difference between the current and the specified item.
   * @param it item to be compared
   * @param coll query context
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

  // Overridden by B64Stream, StrStream, Jav and Array.
  @Override
  public Item materialize(final InputInfo ii) throws QueryException {
    return this;
  }

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
  // Overridden by FItem and ANode.
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
  public String description() {
    return type.toString();
  }

  @Override
  public void plan(final FElem plan) {
    try {
      addPlan(plan, planElem(VAL, string(null), TYP, type));
    } catch(final QueryException ex) {
      // only function items throw exceptions in atomization, and they should
      // override plan(Serializer) sensibly
      throw Util.notExpected(ex);
    }
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
   * @return type string
   */
  public byte[] xdmInfo() {
    return typeId().bytes();
  }

  /**
   * Returns a type id.
   * @return type string
   */
  public ID typeId() {
    return type.id();
  }
}
