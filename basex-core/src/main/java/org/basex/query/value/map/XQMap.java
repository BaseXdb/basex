package org.basex.query.value.map;

import static org.basex.query.QueryText.*;

import java.util.*;
import java.util.function.*;

import org.basex.query.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * The map item.
 *
 * @author BaseX Team 2005-18, BSD License
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
    super(SeqType.ANY_MAP);
    this.root = root;
  }

  @Override
  public QNm paramName(final int pos) {
    return new QNm("key", "");
  }

  @Override
  public FuncType funcType() {
    return MapType.get(AtomType.AAT, SeqType.ITEM_ZM);
  }

  @Override
  public void cache(final InputInfo info, final boolean lazy) throws QueryException {
    root.cache(info, lazy);
  }

  /**
   * Deletes a key from this map.
   * @param key key to delete (must not be {@code null})
   * @param info input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public XQMap delete(final Item key, final InputInfo info) throws QueryException {
    final TrieNode del = root.delete(key.hash(info), key, 0, info);
    return del == root ? this : del == null ? EMPTY : new XQMap(del);
  }

  @Override
  public Value get(final Item key, final InputInfo info) throws QueryException {
    final Value value = root.get(key.hash(info), key, 0, info);
    return value == null ? Empty.SEQ : value;
  }

  /**
   * Checks if the given key exists in the map.
   * @param key key to look for (must not be {@code null})
   * @param info input info
   * @return {@code true()} if the key exists, {@code false()} otherwise
   * @throws QueryException query exception
   */
  public boolean contains(final Item key, final InputInfo info) throws QueryException {
    return root.contains(key.hash(info), key, 0, info);
  }

  /**
   * Adds all bindings from the given map into {@code this}.
   * @param map map to add
   * @param info input info
   * @param merge merge duplicate keys
   * @param qc query context
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public XQMap addAll(final XQMap map, final MergeDuplicates merge, final InputInfo info,
      final QueryContext qc) throws QueryException {

    if(map == EMPTY) return this;
    final TrieNode upd = root.addAll(map.root, 0, merge, info, qc);
    return upd == map.root ? map : new XQMap(upd);
  }

  @Override
  public Item materialize(final QueryContext qc, final boolean copy) {
    return root.materialized() ? this : null;
  }

  @Override
  protected boolean instanceOf(final FuncType ft, final boolean coerce) {
    if(ft instanceof ArrayType) return false;
    if(type.instanceOf(ft)) return true;

    final SeqType[] at = ft.argTypes;
    if(at != null && (at.length != 1 || !at[0].one())) return false;

    SeqType ret = ft.declType;
    if(ft instanceof MapType) {
      AtomType arg = ((MapType) ft).keyType();
      if(arg == AtomType.AAT) arg = null;
      if(ret.eq(SeqType.ITEM_ZM)) ret = null;
      // map { ... } instance of function(...) as item() -> false (result may be empty sequence)
      return (arg == null && ret == null) || root.instanceOf(arg, ret);
    }
    // allow coercion
    return coerce || ret.eq(SeqType.ITEM_ZM);
  }

  /**
   * Puts the given value into this map and replaces existing keys.
   * @param key key to insert (must not be {@code null})
   * @param value value to insert
   * @param info input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public XQMap put(final Item key, final Value value, final InputInfo info) throws QueryException {
    final TrieNode ins = root.put(key.hash(info), key, value, 0, info);
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
   * @param info input info
   * @return resulting value
   * @throws QueryException query exception
   */
  public Value forEach(final FItem func, final QueryContext qc, final InputInfo info)
      throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    root.forEach(vb, func, qc, info);
    return vb.value();
  }

  @Override
  public boolean deep(final Item item, final InputInfo info, final Collation coll)
      throws QueryException {

    if(item instanceof XQMap) return root.deep(info, ((XQMap) item).root, coll);
    return item instanceof FItem && !(item instanceof XQArray) && super.deep(item, info, coll);
  }

  @Override
  public HashMap<Object, Object> toJava() throws QueryException {
    final HashMap<Object, Object> map = new HashMap<>();
    for(final Item key : keys()) map.put(key.toJava(), get(key, null).toJava());
    return map;
  }

  @Override
  public int hash(final InputInfo info) throws QueryException {
    return root.hash(info);
  }

  @Override
  public String description() {
    return MAP;
  }

  @Override
  public void plan(final FElem plan) {
    final int size = mapSize();
    final FElem elem = planElem(ENTRIES, size, TYPE, seqType());
    final Value keys = keys();
    try {
      final int max = Math.min(size, 5);
      for(long i = 0; i < max; i++) {
        final Item key = keys.itemAt(i);
        final Value value = get(key, null);
        key.plan(elem);
        value.plan(elem);
      }
    } catch(final QueryException ex) {
      throw Util.notExpected(ex);
    }
    addPlan(plan, elem);
  }

  @Override
  public void string(final boolean indent, final TokenBuilder tb, final int level,
      final InputInfo info) throws QueryException {

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
      tb.add(key.toString()).add(':');
      if(indent) tb.add(' ');
      final Value value = get(key, info);
      final boolean par = value.size() != 1;
      if(par) tb.add('(');
      int cc = 0;
      for(final Item item : value) {
        if(cc++ > 0) {
          tb.add(',');
          if(indent) tb.add(' ');
        }
        if(item instanceof XQMap) ((XQMap) item).string(indent, tb, level + 1, info);
        else if(item instanceof XQArray) ((XQArray) item).string(indent, tb, level, info);
        else tb.add(item.toString());
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
  public String toString() {
    final TokenBuilder tb = new TokenBuilder().add(MAP).add(" { ");
    if(mapSize() > 0) {
      tb.add(root.append(new StringBuilder()).toString().replaceAll(", $", "")).add(" ");
    }
    return tb.add("}").toString();
  }
}
