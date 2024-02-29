package org.basex.query.value.map;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * The map item.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Leo Woerteler
 */
public final class XQMap extends XQData {
  /** The empty map. */
  private static final XQMap EMPTY = new XQMap(TrieNode.EMPTY, SeqType.MAP);
  /** Number of bits per level, maximum is 5 because {@code 1 << 5 == 32}. */
  static final int BITS = 5;

  /** Wrapped immutable map. */
  private final TrieNode root;

  /**
   * Constructor.
   * @param root map
   * @param type function type
   */
  private XQMap(final TrieNode root, final Type type) {
    super(type);
    this.root = root;
  }

  /**
   * The empty map.
   * Running time: <i>O(1)</i> and no allocation
   * @return (unique) instance of an empty map
   */
  public static XQMap empty() {
    return EMPTY;
  }

  /**
   * Creates a map with a single entry.
   * @param key key
   * @param value value
   * @return map
   * @throws QueryException query exception
   */
  public static XQMap singleton(final Item key, final Value value) throws QueryException {
    return new XQMap(new TrieLeaf(key.hash(), key, value),
        MapType.get((AtomType) key.type, value.seqType()));
  }

  @Override
  public void write(final DataOutput out) throws IOException, QueryException {
    out.writeNum(mapSize());
    for(final Item key : keys()) {
      Store.write(out, key);
      Store.write(out, get(key));
    }
  }

  @Override
  public QNm paramName(final int pos) {
    return new QNm("key", "");
  }

  @Override
  public void refineType(final Expr expr) {
    if(root.size != 0) super.refineType(expr);
  }

  @Override
  public void cache(final boolean lazy, final InputInfo ii) throws QueryException {
    root.cache(lazy, ii);
  }

  /**
   * Deletes a key from this map.
   * @param key key to delete
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public XQMap delete(final Item key) throws QueryException {
    final TrieNode del = root.delete(key.hash(), key, 0);
    return del == root ? this : del == null ? EMPTY : new XQMap(del, type);
  }

  @Override
  public Value invokeInternal(final QueryContext qc, final InputInfo ii, final Value[] args)
      throws QueryException {
    return get(key(args[0], qc, ii));
  }

  /**
   * Gets a value from this map.
   * @param key key to look for
   * @return bound value if found, empty sequence otherwise
   * @throws QueryException query exception
   */
  public Value get(final Item key) throws QueryException {
    return getInternal(key, true);
  }

  /**
   * Gets the internal map value.
   * @param key key to look for
   * @param empty if {@code true}, return empty sequence if key is not found
   * @return value or {@code null}
   * @throws QueryException query exception
   */
  public Value getInternal(final Item key, final boolean empty) throws QueryException {
    final Value value = root.get(key.hash(), key, 0);
    return value != null ? value : empty ? Empty.VALUE : null;
  }

  /**
   * Checks if the given key exists in the map.
   * @param key key to look for
   * @return {@code true()} if the key exists, {@code false()} otherwise
   * @throws QueryException query exception
   */
  public boolean contains(final Item key) throws QueryException {
    return root.contains(key.hash(), key, 0);
  }

  /**
   * Adds all bindings from the given map into {@code this}.
   * @param map map to add
   * @param merge merge duplicate keys
   * @param qc query context
   * @param ii input info (can be {@code null})
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public XQMap addAll(final XQMap map, final MergeDuplicates merge, final QueryContext qc,
      final InputInfo ii) throws QueryException {

    if(this == EMPTY) return map;
    if(map == EMPTY) return this;
    final TrieNode upd = root.addAll(map.root, 0, merge, qc, ii);
    if(upd == map.root) return map;

    final Type tp;
    if(merge == MergeDuplicates.COMBINE) {
      final MapType mt = (MapType) map.type;
      final SeqType mst = mt.declType;
      tp = union(mt.keyType(), mst.zero() ? mst : mst.with(Occ.ONE_OR_MORE));
    } else {
      tp = type.union(map.type);
    }
    return new XQMap(upd, tp);
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw FIATOMIZE_X.get(ii, this);
  }

  @Override
  public Item atomItem(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw FIATOMIZE_X.get(ii, this);
  }

  @Override
  public Item materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc)
      throws QueryException {

    if(materialized(test, ii)) return this;

    final MapBuilder mb = new MapBuilder();
    for(final Item key : keys()) {
      qc.checkStop();
      mb.put(key, get(key).materialize(test, ii, qc));
    }
    return mb.map();
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    return funcType().declType.type.instanceOf(AtomType.ANY_ATOMIC_TYPE) ||
        root.materialized(test, ii);
  }

  @Override
  public boolean instanceOf(final Type tp) {
    if(type.instanceOf(tp)) return true;
    if(!(tp instanceof FuncType) || tp instanceof ArrayType) return false;

    final FuncType ft = (FuncType) tp;
    if(ft.argTypes.length != 1 || !ft.argTypes[0].instanceOf(SeqType.ANY_ATOMIC_TYPE_O))
      return false;

    AtomType kt = null;
    if(ft instanceof MapType) {
      kt = ((MapType) ft).keyType();
      if(kt == AtomType.ANY_ATOMIC_TYPE) kt = null;
    }

    SeqType dt = ft.declType;
    if(dt.eq(SeqType.ITEM_ZM)) dt = null;
    return kt == null && dt == null || root.instanceOf(kt, dt);
  }

  /**
   * Puts the given value into this map and replaces existing keys.
   * @param key key to insert
   * @param value value to insert
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public XQMap put(final Item key, final Value value) throws QueryException {
    if(this == EMPTY) return singleton(key, value);
    final TrieNode ins = root.put(key.hash(), key, value, 0);
    return ins == root ? this : new XQMap(ins, union(key.type, value.seqType()));
  }

  /**
   * Creates a new map type.
   * @param kt key type
   * @param vt value type
   * @return union type
   */
  private Type union(final Type kt, final SeqType vt) {
    final MapType mt = (MapType) type;
    final Type mkt = mt.keyType();
    final SeqType mst = mt.declType;
    return mkt == kt && mst.eq(vt) ? type : MapType.get((AtomType) mkt.union(kt), mst.union(vt));
  }

  /**
   * Number of values contained in this map.
   * @return size
   */
  public int mapSize() {
    return root.size;
  }

  /**
   * All keys defined in this map.
   * @return list of keys
   */
  public Value keys() {
    final ItemList items = new ItemList(root.size);
    root.keys(items);
    return items.value(((MapType) type).keyType());
  }

  /**
   * Adds all values defined in this map to the specified value builder.
   * @param vb value builder
   */
  public void values(final ValueBuilder vb) {
    root.values(vb);
  }

  /**
   * Applies a function on all entries.
   * @param func function to apply on keys and values
   * @throws QueryException query exception
   */
  public void apply(final QueryBiConsumer<Item, Value> func) throws QueryException {
    root.apply(func);
  }

  @Override
  public boolean deepEqual(final Item item, final DeepEqual deep) throws QueryException {
    return this == item || item instanceof XQMap && root.equal(((XQMap) item).root, deep);
  }

  @Override
  public HashMap<Object, Object> toJava() throws QueryException {
    final HashMap<Object, Object> map = new HashMap<>(root.size);
    apply((key, value) -> map.put(key.toJava(), value.toJava()));
    return map;
  }

  @Override
  public void string(final boolean indent, final TokenBuilder tb, final int level,
      final InputInfo ii) throws QueryException {

    tb.add("map{");
    int c = 0;
    final IntConsumer addWS = lvl -> {
      for(int l = 0; l < lvl; l++) tb.add("  ");
    };
    for(final Item key : keys()) {
      if(c++ > 0) tb.add(',');
      if(indent) {
        tb.add('\n');
        addWS.accept(level + 1);
      }
      tb.add(key).add(':');
      if(indent) tb.add(' ');
      final Value value = get(key);
      final boolean par = value.size() != 1;
      if(par) tb.add('(');
      int cc = 0;
      for(final Item item : value) {
        if(cc++ > 0) {
          tb.add(',');
          if(indent) tb.add(' ');
        }
        if(item instanceof XQMap) ((XQMap) item).string(indent, tb, level + 1, ii);
        else if(item instanceof XQArray) ((XQArray) item).string(indent, tb, level, ii);
        else tb.add(item);
      }
      if(par) tb.add(')');
    }
    if(indent) {
      tb.add('\n');
      addWS.accept(level);
    }
    tb.add('}');
  }

  @Override
  public String description() {
    return MAP;
  }

  @Override
  public void toXml(final QueryPlan plan) {
    try {
      final int size = mapSize();
      final Value keys = keys();
      final ExprList list = new ExprList();
      final int max = Math.min(size, 5);
      for(long i = 0; i < max; i++) {
        final Item key = keys.itemAt(i);
        list.add(key).add(get(key));
      }
      plan.add(plan.create(this, ENTRIES, size));
    } catch(final QueryException ex) {
      throw Util.notExpected(ex);
    }
  }

  @Override
  public void toString(final QueryString qs) {
    final TokenBuilder tb = new TokenBuilder();
    root.add(tb);
    qs.token(MAP).brace(tb.toString().replaceAll(", $", ""));
  }
}
