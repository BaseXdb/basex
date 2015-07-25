package org.basex.query.value.array;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.Map;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * An array storing {@link Value}s.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public abstract class Array extends FItem {
  /** Minimum size of a leaf. */
  static final int MIN_LEAF = 8;
  /** Maximum size of a leaf. */
  static final int MAX_LEAF = 2 * MIN_LEAF - 1;
  /** Minimum number of elements in a digit. */
  static final int MIN_DIGIT = MIN_LEAF / 2;
  /** Maximum number of elements in a digit. */
  static final int MAX_DIGIT = MAX_LEAF + MIN_DIGIT;
  /** Maximum size of a small array. */
  static final int MAX_SMALL = 2 * MIN_DIGIT - 1;

  /**
   * Default constructor.
   */
  Array() {
    super(SeqType.ANY_ARRAY, new AnnList());
  }

  /**
   * The empty sequence.
   * Running time: <i>O(1)</i> and no allocation
   * @return (unique) instance of an empty sequence
   */
  public static Array empty() {
    return EmptyArray.INSTANCE;
  }

  /**
   * Creates a singleton array containing the given element.
   * @param elem the contained element
   * @return the singleton array
   */
  public static Array singleton(final Value elem) {
    return new SmallArray(new Value[] { elem });
  }

  /**
   * Creates an array containing the given elements.
   * @param values elements
   * @return the resulting array
   */
  @SafeVarargs
  public static Array from(final Value... values) {
    final ArrayBuilder builder = new ArrayBuilder();
    for(final Value val : values) builder.append(val);
    return builder.freeze();
  }

  /**
   * Prepends an element to the front of this array.
   * Running time: <i>O(1)*</i>
   * @param elem element to prepend
   * @return resulting array
   */
  public abstract Array cons(final Value elem);

  /**
   * Appends an element to the back of this array.
   * Running time: <i>O(1)*</i>
   * @param elem element to append
   * @return resulting array
   */
  public abstract Array snoc(final Value elem);

  /**
   * Gets the element at the given position in this array.
   * Running time: <i>O(log n)</i>
   * @param index index of the element to get
   * @return the corresponding element
   * @throws IndexOutOfBoundsException if the index is smaller that {@code 0}
   *             or {@code >=} the {@link #arraySize()} of this array
   */
  public abstract Value get(long index);

  /**
   * Returns the number of elements in this array.
   * Running time: <i>O(1)</i>
   * @return number of elements
   */
  public abstract long arraySize();

  /**
   * Concatenates this array with another one.
   * Running time: <i>O(log (min { this.arraySize(), other.arraySize() }))</i>
   * @param other array to append to the end of this array
   * @return resulting array
   */
  public abstract Array concat(Array other);

  /**
   * First element of this array, equivalent to {@code array.get(0)}.
   * Running time: <i>O(1)</i>
   * @return the first element
   */
  public abstract Value head();

  /**
   * Last element of this array, equivalent to {@code array.get(array.arraySize() - 1)}.
   * Running time: <i>O(1)</i>
   * @return last element
   */
  public abstract Value last();

  /**
   * Initial segment of this array, i.e. an array containing all elements of this array (in the
   * same order), except for the last one.
   * Running time: <i>O(1)*</i>
   * @return initial segment
   * @throws IllegalStateException if the array is empty
   */
  public abstract Array init();

  /**
   * Tail segment of this array, i.e. an array containing all elements of this array (in the
   * same order), except for the first one.
   * Running time: <i>O(1)*</i>
   * @return tail segment
   * @throws IllegalStateException if the array is empty
   */
  public abstract Array tail();

  /**
   * Extracts a contiguous part of this array.
   * @param pos position of first element
   * @param len number of elements
   * @return the sub-array
   * @throws IndexOutOfBoundsException if {@code pos < 0} or {@code pos + len > this.arraySize()}
   */
  public abstract Array subArray(final long pos, final long len);

  /**
   * Returns an array with the same elements as this one, but their order reversed.
   * Running time: <i>O(n)</i>
   * @return reversed version of this array
   */
  public abstract Array reverseArray();

  @Override
  public final boolean isEmpty() {
    return false;
  }

  /**
   * Checks if this array is empty.
   * Running time: <i>O(1)</i>
   * @return {@code true} if the array is empty, {@code false} otherwise
   */
  public abstract boolean isEmptyArray();

  /**
   * Inserts the given element at the given position into this array.
   * Running time: <i>O(log n)</i>
   * @param pos insertion position, must be between {@code 0} and {@code this.arraySize()}
   * @param val element to insert
   * @return resulting array
   * @throws IndexOutOfBoundsException if {@code pos < 0 || pos > this.arraySize()} holds
   */
  public abstract Array insertBefore(final long pos, final Value val);

  /**
   * Removes the element at the given position in this array.
   * Running time: <i>O(log n)</i>
   * @param pos deletion position, must be between {@code 0} and {@code this.arraySize() - 1}
   * @return resulting array
   * @throws IndexOutOfBoundsException if {@code pos < 0 || pos >= this.arraySize()} holds
   */
  public abstract Array remove(final long pos);

  /**
   * Iterator over the members of this array.
   * @param start starting position
   *   (i.e. the position initially returned by {@link ListIterator#nextIndex()})
   * @return array over the array members
   */
  public abstract ListIterator<Value> iterator(final long start);

  /** Iterable over the elements of this array. */
  private Iterable<Value> iterable;

  /**
   * Iterator over the members of this array.
   * @return array over the array members
   */
  public final Iterable<Value> members() {
    if(iterable == null) {
      iterable = new Iterable<Value>() {
        @Override
        public Iterator<Value> iterator() {
          return Array.this.iterator(0);
        }
      };
    }
    return iterable;
  }

  /**
   * Prepends the given elements to this array.
   * @param vals values, with length at most {@link Array#MAX_SMALL}
   * @return resulting array
   */
  abstract Array consSmall(final Value[] vals);

  /**
   * Returns an array containing the values at the indices {@code from} to {@code to - 1} in
   * the given array. Its length is always {@code to - from}. If {@code from} is smaller than zero,
   * the first {@code -from} entries in the resulting array are {@code null}.
   * If {@code to > arr.length} then the last {@code to - arr.length} entries are {@code null}.
   * If {@code from == 0 && to == arr.length}, the original array is returned.
   * @param arr input array
   * @param from first index, inclusive (may be negative)
   * @param to last index, exclusive (may be greater than {@code arr.length})
   * @return resulting array
   */
  static final Value[] slice(final Value[] arr, final int from, final int to) {
    final Value[] out = new Value[to - from];
    final int in0 = Math.max(0, from), in1 = Math.min(to, arr.length);
    final int out0 = Math.max(-from, 0);
    System.arraycopy(arr, in0, out, out0, in1 - in0);
    return out;
  }

  /**
   * Concatenates the two int arrays.
   * @param as first array
   * @param bs second array
   * @return resulting array
   */
  static final Value[] concat(final Value[] as, final Value[] bs) {
    final int l = as.length, r = bs.length, n = l + r;
    final Value[] out = new Value[n];
    System.arraycopy(as, 0, out, 0, l);
    System.arraycopy(bs, 0, out, l, r);
    return out;
  }

  /**
   * Checks that this array's implementation does not violate any invariants.
   * @throws AssertionError if an invariant was violated
   */
  abstract void checkInvariants();

  @Override
  public Value invValue(final QueryContext qc, final InputInfo ii, final Value... args)
      throws QueryException {
    final Item key = args[0].atomItem(qc, ii);
    if(key == null) throw EMPTYFOUND_X.get(ii, AtomType.ITR);
    return get(key, ii);
  }

  @Override
  public Item invItem(final QueryContext qc, final InputInfo ii, final Value... args)
      throws QueryException {
    return invValue(qc, ii, args).item(qc, ii);
  }

  /**
   * Gets the value from this array.
   * @param key key to look for (must be an integer)
   * @param ii input info
   * @return bound value if found, the empty sequence {@code ()} otherwise
   * @throws QueryException query exception
   */
  public final Value get(final Item key, final InputInfo ii) throws QueryException {
    if(!key.type.instanceOf(AtomType.ITR) && !key.type.isUntyped())
      throw castError(ii, key, AtomType.ITR);

    final long pos = key.itr(ii), size = arraySize();
    if(pos > 0 && pos <= size) return get(pos - 1);
    throw (size == 0 ? ARRAYEMPTY : ARRAYBOUNDS_X_X).get(ii, pos, size);
  }

  @Override
  public int stackFrameSize() {
    return 0;
  }

  @Override
  public int arity() {
    return 1;
  }

  @Override
  public QNm funcName() {
    return null;
  }

  @Override
  public QNm argName(final int pos) {
    return new QNm("pos", "");
  }

  @Override
  public FuncType funcType() {
    return ArrayType.get(SeqType.ITEM_ZM);
  }

  @Override
  public Expr inlineExpr(final Expr[] exprs, final QueryContext qc, final VarScope scp,
      final InputInfo ii) {
    return null;
  }

  @Override
  public Array materialize(final InputInfo ii) throws QueryException {
    final ArrayBuilder builder = new ArrayBuilder();
    for(final Value val : members()) builder.append(val.materialize(ii));
    return builder.freeze();
  }

  @Override
  public Value atomValue(final InputInfo ii) throws QueryException {
    return atm(ii, false);
  }

  @Override
  public Item atomItem(final InputInfo ii) throws QueryException {
    final Value v = atm(ii, true);
    return v.isEmpty() ? null : (Item) v;
  }

  @Override
  public long atomSize() {
    long s = 0;
    for(final Value val : members()) {
      for(final Item it : val) s += it.atomSize();
    }
    return s;
  }

  /**
   * Atomizes the values of the array.
   * @param ii input info
   * @param single only allow single items as result
   * @return result
   * @throws QueryException query exception
   */
  private Value atm(final InputInfo ii, final boolean single) throws QueryException {
    final long s = atomSize(), size = arraySize();
    if(single && s > 1) throw SEQFOUND_X.get(ii, this);
    if(size == 1) return get(0).atomValue(ii);
    final ValueBuilder vb = new ValueBuilder();
    for(final Value val : members()) vb.add(val.atomValue(ii));
    return vb.value();
  }

  /**
   * Returns a string representation of the array.
   * @param ii input info
   * @return string
   * @throws QueryException query exception
   */
  public byte[] serialize(final InputInfo ii) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    string(tb, ii);
    return tb.finish();
  }

  /**
   * Returns a string representation of the array.
   * @param tb token builder
   * @param ii input info
   * @throws QueryException query exception
   */
  public void string(final TokenBuilder tb, final InputInfo ii) throws QueryException {
    tb.add('[');
    int c = 0;
    for(final Value val : members()) {
      if(c++ > 0) tb.add(", ");
      final long vs = val.size();
      if(vs != 1) tb.add('(');
      int cc = 0;
      for(int i = 0; i < vs; i++) {
        if(cc++ > 0) tb.add(", ");
        final Item it = val.itemAt(i);
        if(it instanceof Array) ((Array) it).string(tb, ii);
        else if(it instanceof Map) ((Map) it).string(tb, 0, ii);
        else tb.add(it.toString());
      }
      if(vs != 1) tb.add(')');
    }
    tb.add(']');
  }

  /**
   * Checks if the array has the given type.
   * @param t type
   * @return {@code true} if the type fits, {@code false} otherwise
   */
  public boolean hasType(final ArrayType t) {
    if(!t.retType.eq(SeqType.ITEM_ZM)) {
      for(final Value val : members()) if(!t.retType.instance(val)) return false;
    }
    return true;
  }

  @Override
  public FItem coerceTo(final FuncType ft, final QueryContext qc, final InputInfo ii,
      final boolean opt) throws QueryException {
    if(!(ft instanceof ArrayType) || !hasType((ArrayType) ft)) throw castError(ii, this, ft);
    return this;
  }

  @Override
  public boolean deep(final Item item, final InputInfo ii, final Collation coll)
      throws QueryException {

    if(item instanceof Array) {
      final Array o = (Array) item;
      if(arraySize() != o.arraySize()) return false;
      final Iterator<Value> it1 = iterator(0), it2 = o.iterator(0);
      while(it1.hasNext()) {
        final Value v1 = it1.next(), v2 = it2.next();
        if(v1.size() != v2.size() || !new Compare(ii).collation(coll).equal(v1, v2))
          return false;
      }
      return true;
    }
    return item instanceof FItem && !(item instanceof Map) && super.deep(item, ii, coll);
  }

  @Override
  public String description() {
    return SQUARE1 + DOTS + SQUARE2;
  }

  @Override
  public void plan(final FElem plan) {
    final long size = arraySize();
    final FElem el = planElem(SIZE, size);
    final int max = (int) Math.min(size, 5);
    for(int i = 0; i < max; i++) get(i).plan(el);
    addPlan(plan, el);
  }

  @Override
  public Object[] toJava() throws QueryException {
    final long size = arraySize();
    final Object[] tmp = new Object[(int) size];
    final Iterator<Value> iter = iterator(0);
    for(int i = 0; iter.hasNext(); i++) tmp[i] = iter.next().toJava();
    return tmp;
  }

  @Override
  public String toString() {
    final StringBuilder tb = new StringBuilder().append('[');
    final Iterator<Value> iter = iterator(0);
    for(boolean fst = true; iter.hasNext(); fst = false) {
      if(!fst) tb.append(", ");
      final Value value = iter.next();
      final long vs = value.size();
      if(vs != 1) tb.append('(');
      for(int i = 0; i < vs; i++) {
        if(i != 0) tb.append(", ");
        tb.append(value.itemAt(i));
      }
      if(vs != 1) tb.append(')');
    }
    return tb.append(']').toString();
  }
}
