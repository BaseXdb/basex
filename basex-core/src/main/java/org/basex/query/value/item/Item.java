package org.basex.query.value.item;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.io.*;
import java.math.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.io.in.*;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.Type.*;
import org.basex.util.*;

/**
 * Abstract super class for all items.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public abstract class Item extends Value {
  /** Undefined item. */
  public static final int UNDEF = Integer.MIN_VALUE;

  /**
   * Constructor.
   * @param type item type
   */
  protected Item(final Type type) {
    super(type);
  }

  @Override
  public void write(final DataOutput out) throws IOException, QueryException {
    out.writeToken(string(null));
  }

  @Override
  public BasicIter<Item> iter() {
    return new BasicIter<>(1) {
      @Override
      public Item get(final long i) {
        return Item.this;
      }
      @Override
      public boolean valueIter() {
        return true;
      }
      @Override
      public Item value(final QueryContext qc, final Expr expr) {
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
  public final boolean sameType() {
    return true;
  }

  @Override
  public final Item reverse(final QueryContext qc) {
    return this;
  }

  @Override
  public Item ebv(final QueryContext qc, final InputInfo ii) {
    return this;
  }

  @Override
  public Item test(final QueryContext qc, final InputInfo ii) throws QueryException {
    return bool(ii) ? this : null;
  }

  /**
   * Returns a string representation of the value.
   * @param ii input info (can be {@code null})
   * @return string value
   * @throws QueryException if the item cannot be atomized (caused by function or streaming items)
   */
  public abstract byte[] string(InputInfo ii) throws QueryException;

  /**
   * Returns a boolean representation of the value.
   * @param ii input info (can be {@code null})
   * @return boolean value
   * @throws QueryException query exception
   */
  public boolean bool(final InputInfo ii) throws QueryException {
    throw ebvError(this, ii);
  }

  /**
   * Returns a decimal representation of the value.
   * @param ii input info (can be {@code null})
   * @return decimal value
   * @throws QueryException query exception
   */
  public BigDecimal dec(final InputInfo ii) throws QueryException {
    return Dec.parse(string(ii), ii);
  }

  /**
   * Returns an integer (long) representation of the value.
   * @param ii input info (can be {@code null})
   * @return long value
   * @throws QueryException query exception
   */
  public long itr(final InputInfo ii) throws QueryException {
    return Int.parse(string(ii), ii);
  }

  /**
   * Returns a float representation of the value.
   * @param ii input info (can be {@code null})
   * @return float value
   * @throws QueryException query exception
   */
  public float flt(final InputInfo ii) throws QueryException {
    return Flt.parse(string(ii), ii);
  }

  /**
   * Returns a double representation of the value.
   * @param ii input info (can be {@code null})
   * @return double value
   * @throws QueryException query exception
   */
  public double dbl(final InputInfo ii) throws QueryException {
    return Dbl.parse(string(ii), ii);
  }

  /**
   * Checks if this item is instance of the specified type.
   * Overwritten by {@link XQMap} and {@link XQArray}.
   * @param tp type
   * @return result of check
   */
  public boolean instanceOf(final Type tp) {
    return type.instanceOf(tp);
  }

  /**
   * Checks if the items can be compared.
   * @param item item to be compared
   * @return result of check
   */
  public boolean comparable(final Item item) {
    return type == item.type;
  }

  /**
   * Compares the items for equality.
   * @param item item to be compared
   * @param coll collation (can be {@code null})
   * @param sc static context; required for comparing items of type xs:QName (can be {@code null})
   * @param ii input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public abstract boolean eq(Item item, Collation coll, StaticContext sc, InputInfo ii)
      throws QueryException;

  /**
   * Compares the items for equivalence. As item is equivalent to another if:
   * <ul>
   *   <li>both numeric values are NaN, or</li>
   *   <li>if the items have comparable types and are equal</li>
   * </ul>
   * @param item item to be compared
   * @param coll collation (can be {@code null})
   * @param ii input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public final boolean equiv(final Item item, final Collation coll, final InputInfo ii)
      throws QueryException {
    return (this == Dbl.NAN || this == Flt.NAN) && (item == Dbl.NAN || item == Flt.NAN) ||
        comparable(item) && eq(item, coll, null, ii);
  }

  /**
   * Compares atomic items for equality.
   * @param item item to be compared
   * @param ii input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean atomicEq(final Item item, final InputInfo ii) throws QueryException {
    return comparable(item) && eq(item, null, null, ii);
  }

  /**
   * Returns the difference between the current and the specified item.
   * This function is overwritten by the corresponding implementations.
   * @param item item to be compared
   * @param coll collation (can be {@code null})
   * @param ii input info (can be {@code null})
   * @return difference
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public int diff(final Item item, final Collation coll, final InputInfo ii) throws QueryException {
    throw diffError(this, item, ii);
  }

  /**
   * Returns an input stream.
   * @param ii input info (can be {@code null})
   * @return input stream
   * @throws QueryException query exception
   */
  public BufferInput input(final InputInfo ii) throws QueryException {
    return new ArrayInput(string(ii));
  }

  /**
   * Returns a text input stream for the string representation of the item.
   * @param ii input info (can be {@code null})
   * @return input stream
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public TextInput stringInput(final InputInfo ii) throws IOException, QueryException {
    return new TextInput(string(ii));
  }

  @Override
  public Value subsequence(final long start, final long length, final QueryContext qc) {
    return length == 1 ? this : Empty.VALUE;
  }

  /**
   * {@inheritDoc}
   * Overwritten by {@link Lazy}, {@link XQMap} and {@link XQArray}.
   */
  @Override
  public void cache(final boolean lazy, final InputInfo ii) throws QueryException { }

  /**
   * {@inheritDoc}
   * Overwritten by {@link XQArray}, {@link FuncItem} and {@link ANode}.
   */
  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    return this;
  }

  /**
   * {@inheritDoc}
   * Overwritten by {@link XQArray}, {@link FuncItem} and {@link ANode}.
   */
  @Override
  public Item atomItem(final QueryContext qc, final InputInfo ii) throws QueryException {
    return this;
  }

  @Override
  public Item materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc)
      throws QueryException {
    return this;
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    return true;
  }

  @Override
  public SeqType seqType() {
    return type.seqType();
  }

  @Override
  public long size() {
    return 1;
  }

  @Override
  public void refineType(final Expr expr) {
  }

  @Override
  public final boolean ddo() {
    return true;
  }

  /**
   * Returns a score value. Overwritten by {@link FTNode}.
   * @return score value
   */
  public double score() {
    return 0;
  }

  @Override
  public int hash(final InputInfo ii) throws QueryException {
    return Token.hash(string(ii));
  }

  /**
   * Returns data model info.
   * Overwritten by {@link QNm}, {@link DBNode}, {@link FTxt} and {@link FDoc}.
   * @return type string
   */
  public byte[] xdmInfo() {
    return new byte[] { typeId().asByte() };
  }

  /**
   * Returns a type id.
   * Overwritten by {@link DBNode} and {@link FDoc}.
   * @return type string
   */
  public ID typeId() {
    return type.id();
  }

  @Override
  public String description() {
    return type == AtomType.ITEM ? ITEM : type + " " + ITEM;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    try {
      plan.add(plan.create(this), QueryString.toValue(string(null)));
    } catch(final QueryException ex) {
      // only function items throw exceptions in atomization, and they should override sensibly
      throw Util.notExpected(ex);
    }
  }
}
