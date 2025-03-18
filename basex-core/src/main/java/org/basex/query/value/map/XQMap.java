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
import org.basex.query.iter.*;
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
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public abstract class XQMap extends XQStruct {
  /** The empty map. */
  private static final XQMap EMPTY = new XQTrieMap(TrieEmpty.VALUE, null, SeqType.MAP);

  /**
   * Constructor.
   * @param type map type
   */
  XQMap(final Type type) {
    super(type);
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
   */
  public static XQMap singleton(final Item key, final Value value) {
    return new XQSingletonMap(key, value);
  }

  /**
   * Creates a map with 'key' and 'value' entries.
   * @param key key
   * @param value value
   * @return map
   * @throws QueryException query exception
   */
  public static XQMap pair(final Item key, final Value value) throws QueryException {
    return new MapBuilder(2).put(Str.KEY, key).put(Str.VALUE, value).map();
  }

  @Override
  public final void write(final DataOutput out) throws IOException, QueryException {
    out.writeNum((int) structSize());
    for(final Item key : keys()) {
      Store.write(out, key);
      Store.write(out, get(key));
    }
  }

  @Override
  public final void refineType(final Expr expr) {
    if(structSize() != 0) super.refineType(expr);
  }

  @Override
  public final void cache(final boolean lazy, final InputInfo ii) throws QueryException {
    forEach((key, value) -> {
      key.cache(lazy, ii);
      value.cache(lazy, ii);
    });
  }

  @Override
  public final Value invokeInternal(final QueryContext qc, final InputInfo ii, final Value[] args)
      throws QueryException {
    return get(key(args[0], qc, ii));
  }

  /**
   * Gets a value from this map.
   * @param key key to look for
   * @return bound value if found, empty sequence otherwise
   * @throws QueryException query exception
   */
  public final Value get(final Item key) throws QueryException {
    return getInternal(key, true);
  }

  /**
   * Gets the internal map value.
   * @param key key to look for
   * @param empty if {@code true}, return empty sequence if key is not found
   * @return value or {@code null}
   * @throws QueryException query exception
   */
  public final Value getInternal(final Item key, final boolean empty) throws QueryException {
    final Value value = getInternal(key);
    return value != null ? value : empty ? Empty.VALUE : null;
  }

  /**
   * Gets the internal map value.
   * @param key key to look for
   * @return value or {@code null}
   * @throws QueryException query exception
   */
  abstract Value getInternal(Item key) throws QueryException;

  /**
   * Puts the given value into this map and replaces existing keys.
   * @param key key to insert
   * @param value value to insert
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public abstract XQMap put(Item key, Value value) throws QueryException;

  /**
   * Removed a key from this map.
   * @param key key to remove
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public abstract XQMap remove(Item key) throws QueryException;

  /**
   * Applies a function on all entries.
   * @param func function to apply on keys and values
   * @throws QueryException query exception
   */
  public abstract void forEach(QueryBiConsumer<Item, Value> func) throws QueryException;

  /**
   * Tests all entries.
   * @param func predicate function
   * @return {@code true} if all the check is successful for all entries
   * @throws QueryException query exception
   */
  public abstract boolean test(QueryBiPredicate<Item, Value> func) throws QueryException;

  /**
   * Checks if the given key exists in the map.
   * @param key key to look for
   * @return result of check
   * @throws QueryException query exception
   */
  public final boolean contains(final Item key) throws QueryException {
    return getInternal(key) != null;
  }

  @Override
  public final Value atomValue(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw FIATOMIZE_X.get(ii, this);
  }

  @Override
  public final Item atomItem(final QueryContext qc, final InputInfo ii) throws QueryException {
    throw FIATOMIZE_X.get(ii, this);
  }

  @Override
  public final XQMap materialize(final Predicate<Data> test, final InputInfo ii,
      final QueryContext qc) throws QueryException {

    if(materialized(test, ii)) return this;

    final MapBuilder mb = new MapBuilder(structSize());
    forEach((key, value) -> {
      qc.checkStop();
      mb.put(key, value.materialize(test, ii, qc));
    });
    return mb.map();
  }

  @Override
  public final boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    return funcType().declType.type.instanceOf(AtomType.ANY_ATOMIC_TYPE) ||
        test((key, value) -> value.materialized(test, ii));
  }

  @Override
  public final boolean instanceOf(final Type tp) {
    if(tp instanceof RecordType) return ((RecordType) tp).instance(this);
    if(type.instanceOf(tp)) return true;

    final Type kt;
    final SeqType vt;
    if(tp instanceof MapType) {
      final MapType mt = (MapType) tp;
      kt = mt.keyType() == AtomType.ANY_ATOMIC_TYPE ? null : mt.keyType();
      vt = mt.valueType().eq(SeqType.ITEM_ZM) ? null : mt.valueType();
    } else if(tp instanceof FuncType) {
      final FuncType ft = (FuncType) tp;
      if(ft.declType.occ.min != 0 || ft.argTypes.length != 1 ||
          !ft.argTypes[0].instanceOf(SeqType.ANY_ATOMIC_TYPE_O)) return false;
      kt = null;
      vt = ft.declType.eq(SeqType.ITEM_ZM) ? null : ft.declType;
    } else {
      return false;
    }
    try {
      return kt == null && vt == null || test((key, value) ->
        (kt == null || key.type.instanceOf(kt)) && (vt == null || vt.instance(value)));
    } catch(final QueryException ex) {
      throw Util.notExpected(ex);
    }
  }

  /**
   * Returns a key iterator.
   * @return iterator
   */
  public abstract BasicIter<Item> keys();

  @Override
  public final Value items(final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    forEach((key, value) -> vb.add(value));
    return vb.value(((MapType) type).valueType().type);
  }

  /**
   * Converts this map to the given map type.
   * @param mt map type
   * @param qc query context
   * @param cc compilation context ({@code null} during runtime)
   * @param ii input info (can be {@code null})
   * @return coerced map
   * @throws QueryException query exception
   */
  public final XQMap coerceTo(final MapType mt, final QueryContext qc, final CompileContext cc,
      final InputInfo ii) throws QueryException {

    final SeqType kt = mt.keyType().seqType(), vt = mt.valueType();
    final MapBuilder mb = new MapBuilder(structSize());
    forEach((key, value) -> {
      qc.checkStop();
      final Item k = (Item) kt.coerce(key, null, qc, cc, ii);
      if(mb.contains(k)) throw typeError(this, mt.seqType(), ii);
      mb.put(k, vt.coerce(value, null, qc, cc, ii));
    });
    return mb.map();
  }

  /**
   * Converts this map to the given record type.
   * @param rt record type
   * @param qc query context
   * @param cc compilation context ({@code null} during runtime)
   * @param ii input info (can be {@code null})
   * @return coerced map
   * @throws QueryException query exception
   */
  public final XQMap coerceTo(final RecordType rt, final QueryContext qc, final CompileContext cc,
      final InputInfo ii) throws QueryException {

    for(final byte[] key : rt) {
      if(!rt.field(key).isOptional() && !contains(Str.get(key))) {
        throw typeError(this, rt.seqType(), ii);
      }
    }
    final MapBuilder mb = new MapBuilder(structSize());
    forEach((key, value) -> {
      qc.checkStop();
      final RecordField rf = key.instanceOf(AtomType.STRING) ? rt.field(key.string(null)) : null;
      final Value v;
      if(rf != null) {
        v = rf.seqType().coerce(value, null, qc, cc, ii);
      } else if(rt.isExtensible()) {
        v = value;
      } else {
        throw typeError(this, rt.seqType(), ii);
      }
      mb.put(key, v);
    });
    // assign record type to speed up future type checks
    final XQMap map = mb.map();
    if(map != EMPTY) map.type = rt;
    return map;
  }

  @Override
  public final HashMap<Object, Object> toJava() throws QueryException {
    final HashMap<Object, Object> map = new HashMap<>((int) structSize());
    forEach((key, value) -> map.put(key.toJava(), value.toJava()));
    return map;
  }

  @Override
  public final boolean deepEqual(final Item item, final DeepEqual deep) throws QueryException {
    if(this == item) return true;
    if(!(item instanceof XQMap)) return false;
    final XQMap map = (XQMap) item;
    if(structSize() != map.structSize()) return false;
    // compilation: ordered comparison
    if(deep == null) return deepEqualOrdered(map);
    // deep equality: always compare ordered first (faster)
    return deepEqualOrdered(map, deep) ||
      !deep.options.get(DeepEqualOptions.MAP_ORDER) && deepEqualUnordered(map, deep);
  }

  /**
   * Compares two maps for equality, considering order, at compile time.
   * @param map map to be compared
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean deepEqualOrdered(final XQMap map) throws QueryException {
    final BasicIter<Item> keys2 = map.keys();
    for(final Item key : keys()) {
      final Item k = keys2.next();
      if(!(key.equals(k) && getInternal(key).equals(map.getInternal(k)))) return false;
    }
    return true;
  }

  /**
   * Compares two maps for equality, considering order.
   * @param map map to be compared
   * @param deep comparator
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean deepEqualOrdered(final XQMap map, final DeepEqual deep)throws QueryException {
    final BasicIter<Item> keys2 = map.keys();
    for(final Item key : keys()) {
      final Item k = keys2.next();
      if(!(key.atomicEqual(k) && deep.equal(getInternal(key), map.getInternal(k)))) return false;
    }
    return true;
  }

  /**
   * Compares two maps for equality, ignoring order.
   * @param map map to be compared
   * @param deep comparator
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean deepEqualUnordered(final XQMap map, final DeepEqual deep) throws QueryException {
    for(final Item key : keys()) {
      final Value v = map.getInternal(key);
      if(v == null || !deep.equal(getInternal(key), v)) return false;
    }
    return true;
  }

  @Override
  public final void string(final boolean indent, final TokenBuilder tb, final int level,
      final InputInfo ii) throws QueryException {

    tb.add("{");
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
  public final String description() {
    return MAP;
  }

  @Override
  public final void toXml(final QueryPlan plan) {
    try {
      final long size = structSize();
      final Value keys = keys().value(null, null);
      final ExprList list = new ExprList();
      final long max = Math.min(size, 5);
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
  public final void toString(final QueryString qs) {
    final TokenBuilder tb = new TokenBuilder();
    try {
      forEach((key, value) -> {
        if(tb.moreInfo()) tb.add(key).add(MAPASG).add(value).add(SEP);
      });
    } catch(final QueryException ex) {
      Util.notExpected(ex);
    }
    qs.braced("{ ", tb.toString().replaceAll(", $", ""), " }");
  }
}
