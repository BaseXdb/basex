package org.basex.query.value.array;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Array item.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class Array extends FItem {
  /** Empty array. */
  private static final Array EMPTY = new Array(new Value[0], 0, 0);
  /** Members of the array. */
  private final Value[] members;
  /** Starting index. */
  private final int start;
  /** Length. */
  private final int size;

  /**
   * Constructor.
   * @param members array members
   * @param start starting index
   * @param size number of members
   */
  private Array(final Value[] members, final int start, final int size) {
    super(SeqType.ANY_ARRAY, new Ann());
    this.members = members;
    this.start = start;
    this.size = size;
  }

  /**
   * Returns an instance of this class.
   * @param members values
   * @return instance
   */
  public static Array get(final Value... members) {
    final int s = members.length;
    return s == 0 ? EMPTY : new Array(members, 0, s);
  }

  /**
   * Factory method for subarrays.
   * @param array input array
   * @param start start index (starting from 0}
   * @param size number of members
   * @return resulting array
   */
  public static Array get(final Array array, final int start, final int size) {
    return size == 0 ? EMPTY : start == 0 && size == array.size ? array :
      new Array(array.members, array.start + start, size);
  }

  @Override
  public Value invValue(final QueryContext qc, final InputInfo ii, final Value... args)
      throws QueryException {
    return get(args[0].item(qc, ii), ii);
  }

  @Override
  public Item invItem(final QueryContext qc, final InputInfo ii, final Value... args)
      throws QueryException {
    return invValue(qc, ii, args).item(qc, ii);
  }

  /**
   * Gets the value from this array.
   * @param key key to look for (must be integer)
   * @param ii input info
   * @return bound value if found, the empty sequence {@code ()} otherwise
   * @throws QueryException query exception
   */
  public Value get(final Item key, final InputInfo ii) throws QueryException {
    if(key == null) throw EMPTYFOUND_X.get(ii, AtomType.ITR);
    if(!key.type.instanceOf(AtomType.ITR) && !key.type.isUntyped())
      throw castError(ii, key, AtomType.ITR);

    final long i = key.itr(ii);
    if(i > 0 && i <= size) return get((int) i - 1);
    throw ARRAYPOS_X.get(ii, i);
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

  /**
   * Returns a member iterator.
   * @return iterator
   */
  public ArrayIterator<Value> members() {
    return new ArrayIterator<>(members, 0, size);
  }

  /**
   * Returns the member at the specified index.
   * @param index index
   * @return value
   */
  public Value get(final int index) {
    return members[start + index];
  }

  /**
   * Number of members contained in this array.
   * @return size
   */
  public int arraySize() {
    return size;
  }

  @Override
  public Item materialize(final InputInfo ii) throws QueryException {
    final ValueList vl = new ValueList(size);
    for(int a = 0; a < size; a++) vl.add(get(a).materialize(ii));
    return vl.array();
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
    for(int a = 0; a < size; a++) {
      final Value v = get(a);
      final long vs = v.size();
      for(int i = 0; i < vs; i++) s += v.itemAt(i).atomSize();
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
    final long s = atomSize();
    if(single && s > 1) throw SEQFOUND_X.get(ii, this);
    if(size == 1) return get(0).atomValue(ii);
    final ValueBuilder vb = new ValueBuilder((int) s);
    for(int a = 0; a < size; a++) vb.add(get(a).atomValue(ii));
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
    for(int a = 0; a < size; a++) {
      if(c++ > 0) tb.add(", ");
      final Value v = get(a);
      final long vs = v.size();
      if(vs != 1) tb.add('(');
      int cc = 0;
      for(int i = 0; i < vs; i++) {
        if(cc++ > 0) tb.add(", ");
        final Item it = v.itemAt(i);
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
      for(int a = 0; a < size; a++) if(!t.retType.instance(get(a))) return false;
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
      if(size != o.size) return false;
      for(int a = 0; a < size; a++) {
        final Value v1 = get(a), v2 = o.get(a);
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
    final FElem el = planElem(SIZE, size);
    final int max = Math.min(size, 5);
    for(int i = 0; i < max; i++) get(i).plan(el);
    addPlan(plan, el);
  }

  @Override
  public Object toJava() throws QueryException {
    final Object[] tmp = new Object[size];
    for(int a = 0; a < size; a++) tmp[a] = get(a).toJava();
    return tmp;
  }

  @Override
  public String toString() {
    final StringBuilder tb = new StringBuilder().append('[');
    for(int a = 0; a < size; a++) {
      if(a != 0) tb.append(", ");
      final Value value = get(a);
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
