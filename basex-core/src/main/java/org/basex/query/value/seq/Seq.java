package org.basex.query.value.seq;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.tree.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Sequence, containing at least two items.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public abstract class Seq extends Value {
  /** Length. */
  protected long size;

  /**
   * Constructor, specifying a type.
   * @param size size
   * @param type exact type, {@code item()*} otherwise
   */
  protected Seq(final long size, final Type type) {
    super(type);
    this.size = size;
  }

  @Override
  public Object toJava() throws QueryException {
    // determine type (static or exact)
    Type tp = type;
    if(tp == AtomType.ITEM) {
      tp = null;
      for(final Item item : this) {
        final Type st = item.type;
        tp = tp == null ? st : tp.union(st);
      }
    }

    // shortcut for strings (avoid intermediate token representation)
    final int sz = (int) size;
    if(tp == AtomType.STRING) {
      final StringList list = new StringList(sz);
      for(final Item item : this) list.add(item.string(null));
      return list.finish();
    }
    // try to create custom Java representation
    final Value value = get(sz, tp, this);
    if(value != null) return value.toJava();

    int a = 0;
    final Object[] array = new Object[sz];
    for(final Item item : this) array[a++] = item.toJava();
    return array;
  }

  @Override
  public final long size() {
    return size;
  }

  @Override
  public final Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw SEQFOUND_X.get(ii, this);
  }

  @Override
  public final Item test(final QueryContext qc, final InputInfo ii) throws QueryException {
    return ebv(qc, ii);
  }

  @Override
  public BasicIter<Item> iter() {
    return new BasicIter<Item>(size) {
      @Override
      public Item get(final long i) {
        return itemAt(i);
      }
      @Override
      public Value iterValue() {
        return Seq.this;
      }
      @Override
      public Value value(final QueryContext qc, final Expr expr) {
        return Seq.this;
      }
    };
  }

  @Override
  public boolean ddo() {
    return false;
  }

  @Override
  public final Value subsequence(final long start, final long length, final QueryContext qc) {
    return length == 0 ? Empty.VALUE :
           length == 1 ? itemAt(start) :
           length == size() ? this :
           subSeq(start, length, qc);
  }

  /**
   * Returns a sub sequence of this value with the given start and length.
   * @param pos position of first item (ge 0)
   * @param length number of items (1 lt length lt size())
   * @param qc query context
   * @return sub sequence
   */
  protected Seq subSeq(final long pos, final long length, final QueryContext qc) {
    qc.checkStop();
    return new SubSeq(this, pos, length);
  }

  /**
   * Inserts a value at the given position into this sequence and returns the resulting sequence.
   * @param pos position at which the value should be inserted, must be between 0 and {@link #size}
   * @param value value to insert
   * @param qc query context
   * @return resulting value
   */
  public final Value insertBefore(final long pos, final Value value, final QueryContext qc) {
    final long n = value.size();
    return n == 0 ? this : n == 1 ? insert(pos, (Item) value, qc) : copyInsert(pos, value, qc);
  }

  /**
   * Inserts an item at the given position into this sequence and returns the resulting sequence.
   * @param pos position at which the item should be inserted, must be between 0 and {@link #size}
   * @param item item to insert
   * @param qc query context
   * @return resulting value
   */
  public abstract Value insert(long pos, Item item, QueryContext qc);

  /**
   * Helper for {@link #insertBefore(long, Value, QueryContext)} that copies all items into a
   * {@link TreeSeq}.
   * @param pos position at which the value should be inserted, must be between 0 and {@link #size}
   * @param value value to insert
   * @param qc query context
   * @return resulting value
   */
  protected Value copyInsert(final long pos, final Value value, final QueryContext qc) {
    final Type tp = type.union(value.type);
    if(pos == size) return new TreeSeqBuilder().add(this, qc).add(value, qc).sequence(tp);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(long i = 0; i < pos; i++) vb.add(itemAt(i));
    vb.add(value);
    for(long i = pos; i < size; i++) vb.add(itemAt(i));
    return vb.value(tp);
  }

  /**
   * Removes the item at the given position in this sequence and returns the resulting sequence.
   * @param pos position of the item to remove, must be between 0 and {@link #size} - 1
   * @param qc query context
   * @return resulting sequence
   */
  public abstract Value remove(long pos, QueryContext qc);

  /**
   * Helper for {@link #remove(long, QueryContext)} that copies all items into a {@link TreeSeq}.
   * @param pos position of the item to remove, must be between 0 and {@link #size} - 1
   * @param qc query context
   * @return resulting sequence
   */
  final Value copyRemove(final long pos, final QueryContext qc) {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(long i = 0; i < pos; i++) vb.add(itemAt(i));
    for(long i = pos + 1; i < size; i++) vb.add(itemAt(i));
    return vb.value(type);
  }

  @Override
  public final void refineType(final Expr expr) {
    final Type t = expr.seqType().type.intersect(type);
    if(t != null) type = t;
  }

  @Override
  public final int hash(final InputInfo ii) throws QueryException {
    // final hash function because equivalent sequences *must* produce the
    // same hash value, otherwise they get lost in hash maps.
    // example: hash(RangeSeq(1 to 3)) == hash(ItrSeq(1, 2, 3))
    //                                 == hash(ItemSeq(Itr(1), Itr(2), Itr(3)))
    int h = 1;
    for(long v = Math.min(size, 5); --v >= 0;) h = 31 * h + itemAt(v).hash(ii);
    return h;
  }

  @Override
  public final SeqType seqType() {
    return SeqType.get(type, Occ.ONE_OR_MORE);
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item item : this) vb.add(item.atomValue(qc, ii));
    return vb.value(AtomType.ANY_ATOMIC_TYPE);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc)
      throws QueryException {

    Value value = null;
    if(type instanceof NodeType && (mode == Simplify.DATA || mode == Simplify.NUMBER ||
      mode == Simplify.STRING)) {
      if(mode == Simplify.STRING) {
        final TokenList list = new TokenList(size);
        for(int i = 0; i < size; i++) list.add(itemAt(i).string(null));
        value = StrSeq.get(list);
      } else {
        final Item[] items = new Item[(int) size];
        for(int i = 0; i < size; i++) items[i] = Atm.get(itemAt(i).string(null));
        value = ItemSeq.get(items, (int) size, AtomType.UNTYPED_ATOMIC);
      }
    }
    return value != null ? cc.simplify(this, value) : super.simplifyFor(mode, cc);
  }

  @Override
  public void cache(final boolean lazy, final InputInfo ii) throws QueryException {
    for(final Item item : this) item.cache(lazy, ii);
  }

  @Override
  public Item ebv(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item head = itemAt(0);
    if(head instanceof ANode) return head;
    throw ebvError(this, ii);
  }

  @Override
  public long atomSize() {
    long sz = 0;
    for(final Item item : this) sz += item.atomSize();
    return sz;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Seq)) return false;
    final Seq s = (Seq) obj;
    if(size != s.size) return false;
    final BasicIter<Item> iter1 = iter(), iter2 = s.iter();
    for(Item item1; (item1 = iter1.next()) != null;) {
      if(!item1.equals(iter2.next())) return false;
    }
    return true;
  }

  @Override
  public String description() {
    return type + " " + SEQUENCE;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    final int max = (int) Math.min(size, 5);
    final ExprList list = new ExprList(max);
    for(long i = 0; i < max; i++) list.add(itemAt(i));
    plan.add(plan.create(this), list.finish());
  }

  @Override
  public final String toErrorString() {
    return build(true).toString();
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(build(false).finish());
  }

  /**
   * Returns a string representation of the sequence.
   * @param error error flag
   * @return token builder
   */
  private TokenBuilder build(final boolean error) {
    final TokenBuilder tb = new TokenBuilder().add('(');
    for(int i = 0; i < size; ++i) {
      if(i > 0) tb.add(SEP);
      tb.add(error ? itemAt(i).toErrorString() : itemAt(i).toString());
      if(tb.size() <= 40 || i + 1 == size) continue;
      // chop output to prevent too long error strings
      tb.add(SEP).add(DOTS);
      break;
    }
    return tb.add(')');
  }

  // STATIC METHODS ===============================================================================

  /**
   * Tries to create a typed sequence with the items of the specified values.
   * @param size size of resulting sequence
   * @param values values
   * @param type type
   * @return value, or {@code null} if sequence could not be created
   * @throws QueryException query exception
   */
  public static Value get(final int size, final Type type, final Value... values)
      throws QueryException {

    if(type instanceof AtomType) {
      switch((AtomType) type) {
        case BOOLEAN:
          return BlnSeq.get(size, values);
        case STRING:
          return StrSeq.get(size, values);
        case BYTE:
          return BytSeq.get(size, values);
        case SHORT:
          return ShrSeq.get(size, values);
        case FLOAT:
          return FltSeq.get(size, values);
        case DOUBLE:
          return DblSeq.get(size, values);
        case DECIMAL:
          return DecSeq.get(size, values);
        case UNSIGNED_LONG:
          return null;
        default:
          if(type.instanceOf(AtomType.INTEGER)) return IntSeq.get(type, size, values);
      }
    }
    return null;
  }

  /**
   * Returns an initial array capacity for the expected result size.
   * Throws an exception if the requested size will take too much memory.
   * @param size expected result size
   * @return capacity
   * @throws QueryException query exception
   */
  public static int initialCapacity(final long size) throws QueryException {
    if(size > Array.MAX_SIZE) throw ARRAY_X_X.get(null, Array.MAX_SIZE, size);
    return Array.initialCapacity(size);
  }
}
