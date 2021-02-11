package org.basex.query.value.map;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.collation.*;
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
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class XQMap extends XQData {
  /** The empty map. */
  public static final XQMap EMPTY = new XQMap(TrieNode.EMPTY);
  /** Number of bits per level, maximum is 5 because {@code 1 << 5 == 32}. */
  static final int BITS = 5;

  /** Wrapped immutable map. */
  private final TrieNode root;

  /**
   * Constructor.
   * @param root map
   */
  private XQMap(final TrieNode root) {
    super(SeqType.MAP);
    this.root = root;
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
   * @param key key to delete (must not be {@code null})
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public XQMap delete(final Item key, final InputInfo ii) throws QueryException {
    final TrieNode del = root.delete(key.hash(ii), key, 0, ii);
    return del == root ? this : del == null ? EMPTY : new XQMap(del);
  }

  @Override
  public Value get(final Item key, final InputInfo ii) throws QueryException {
    final Value value = root.get(key.hash(ii), key, 0, ii);
    return value == null ? Empty.VALUE : value;
  }

  /**
   * Checks if the given key exists in the map.
   * @param key key to look for (must not be {@code null})
   * @param ii input info
   * @return {@code true()} if the key exists, {@code false()} otherwise
   * @throws QueryException query exception
   */
  public boolean contains(final Item key, final InputInfo ii) throws QueryException {
    return root.contains(key.hash(ii), key, 0, ii);
  }

  /**
   * Adds all bindings from the given map into {@code this}.
   * @param map map to add
   * @param merge merge duplicate keys
   * @param qc query context
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public XQMap addAll(final XQMap map, final MergeDuplicates merge, final QueryContext qc,
      final InputInfo ii) throws QueryException {

    if(map == EMPTY) return this;
    final TrieNode upd = root.addAll(map.root, 0, merge, qc, ii);
    return upd == map.root ? map : new XQMap(upd);
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw FIATOM_X.get(ii, type);
  }

  @Override
  public Item atomItem(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw FIATOM_X.get(ii, type);
  }

  @Override
  public Item materialize(final QueryContext qc, final boolean copy) {
    return root.materialized() ? this : null;
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
   * @param key key to insert (must not be {@code null})
   * @param value value to insert
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public XQMap put(final Item key, final Value value, final InputInfo ii) throws QueryException {
    final TrieNode ins = root.put(key.hash(ii), key, value, 0, ii);
    return ins == root ? this : new XQMap(ins);
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
    return items.value();
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
   * @param qc query context
   * @param ii input info
   * @return resulting value
   * @throws QueryException query exception
   */
  public Value forEach(final FItem func, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    root.forEach(vb, func, qc, ii);
    return vb.value();
  }

  @Override
  public boolean deep(final Item item, final Collation coll, final InputInfo ii)
      throws QueryException {

    if(item instanceof FuncItem) throw FICMP_X.get(ii, type);
    if(item instanceof XQMap) return root.deep(((XQMap) item).root, coll, ii);
    return false;
  }

  @Override
  public HashMap<Object, Object> toJava() throws QueryException {
    final HashMap<Object, Object> map = new HashMap<>();
    for(final Item key : keys()) map.put(key.toJava(), get(key, null).toJava());
    return map;
  }

  @Override
  public int hash(final InputInfo ii) throws QueryException {
    return root.hash(ii);
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
      final Value value = get(key, ii);
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
  public void plan(final QueryPlan plan) {
    try {
      final int size = mapSize();
      final Value keys = keys();
      final ExprList list = new ExprList();
      final int max = Math.min(size, 5);
      for(long i = 0; i < max; i++) {
        final Item key = keys.itemAt(i);
        list.add(key).add(get(key, null));
      }
      plan.add(plan.create(this, ENTRIES, size));
    } catch(final QueryException ex) {
      throw Util.notExpected(ex);
    }
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(MAP).brace(root.append(new StringBuilder()).toString().replaceAll(", $", ""));
  }
}
