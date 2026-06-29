package org.basex.query.value.map;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.core.jobs.*;
import org.basex.data.*;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.hash.ItemSet.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * The map item.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public abstract class XQMap extends XQStruct {
  /**
   * Single map entry. Instances of this record are returned by {@link #entries()}.
   * @param key key
   * @param value value
   */
  public record Entry(Item key, Value value) { }

  /**
   * Constructor.
   * @param type map type
   */
  XQMap(final Type type) {
    super(type);
  }

  /**
   * The empty map.
   * @return (unique) instance of an empty map
   */
  public static XQMap empty() {
    return XQTrieMap.EMPTY;
  }

  /**
   * Creates a map with a single entry.
   * @param key key
   * @param value value
   * @return map
   */
  public static XQMap get(final Item key, final Value value) {
    return new XQSingletonMap(key, value);
  }

  @Override
  public final void write(final DataOutput out) throws IOException, QueryException {
    out.writeNum((int) structSize());
    for(final Entry entry : entries()) {
      Stores.write(out, entry.key());
      Stores.write(out, entry.value());
    }
  }

  @Override
  public final void refineType(final Expr expr) {
    if(this != empty()) super.refineType(expr);
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
    final Item k = key(args[0], qc, ii);
    if(type instanceof final RecordType rt && rt.strict() &&
        (!k.type.isStringOrUntyped() || !rt.fields().contains(k.string(null)))) {
      throw RECORDFIELD_X_X.get(ii, this, k);
    }
    return get(k);
  }

  /**
   * Gets a value from this map.
   * @param key key to look for
   * @return value if found, empty sequence otherwise
   * @throws QueryException query exception
   */
  public final Value get(final Item key) throws QueryException {
    final Value value = getOrNull(key);
    return value != null ? value : Empty.VALUE;
  }

  /**
   * Gets a value from this map.
   * @param key key to look for
   * @return value if found, {@code null} otherwise
   * @throws QueryException query exception
   */
  public abstract Value getOrNull(Item key) throws QueryException;

  /**
   * Gets a value from this map.
   * @param key atomic key to look for
   * @return value if found, {@code null} otherwise
   */
  public final Value value(final Item key) {
    try {
      return getOrNull(key);
    } catch(final QueryException ex) {
      // the exception is only caused by atomizing the key
      throw Util.notExpected(ex);
    }
  }

  /**
   * Returns an iterable instance for all entries of this map.
   * @return iterable
   */
  public final Iterable<Entry> entries() {
    return () -> new Iterator<>() {
      private long i;

      @Override
      public boolean hasNext() {
        return i < structSize();
      }

      @Override
      public Entry next() {
        final long c = i++;
        return new Entry(keyAt(c), valueAt(c));
      }
    };
  }

  /**
   * Puts a value with the specified key into this map.
   * @param key key to insert
   * @param value value to insert
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public abstract XQMap put(Item key, Value value) throws QueryException;

  /**
   * Puts a value with the specified key into this map.
   * @param index map index (starting with 0, must be valid)
   * @param value value to insert
   * @return updated map
   * @throws QueryException query exception
   */
  public abstract XQMap putAt(int index, Value value) throws QueryException;

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
    return getOrNull(key) != null;
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
    return funcType().declType.type.instanceOf(BasicType.ANY_ATOMIC_TYPE) ||
        test((key, value) -> value.materialized(test, ii));
  }

  @Override
  public final boolean instanceOf(final Type tp, final boolean coerce) {
    if(type == tp) return true;
    if(coerce && tp instanceof FuncType) return false;

    try {
      if(tp instanceof final RecordType rt) {
        // record(*)
        if(rt == Types.RECORD) {
          for(final Item key : keys()) {
            if(!key.type.instanceOf(BasicType.STRING)) return false;
          }
          return true;
        }
        // coercion: skip only if this is already a sealed record of the required type
        if(coerce) {
          return type.instanceOf(rt) && type instanceof final RecordType st && st.sealed();
        }
        // structural check: every declared field is present with a matching value, no extra keys
        final TokenObjectMap<RecordField> fields = rt.fields();
        final int fs = fields.size();
        for(int f = 1; f <= fs; f++) {
          final Value value = getOrNull(Str.get(fields.key(f)));
          if(value == null || !fields.value(f).seqType().instance(value)) return false;
        }
        for(final Item key : keys()) {
          if(!key.type.instanceOf(BasicType.STRING) || !fields.contains(key.string(null)))
            return false;
        }
        return true;
      }
      if(type.instanceOf(tp)) return true;

      final Type kt;
      final SeqType vt;
      if(tp instanceof final MapType mt) {
        kt = mt.keyType() == BasicType.ANY_ATOMIC_TYPE ? null : mt.keyType();
        vt = mt.valueType().eq(Types.ITEM_ZM) ? null : mt.valueType();
      } else if(tp instanceof final FuncType ft) {
        if(ft.declType.occ.min != 0 || ft.argTypes.length != 1 ||
            !ft.argTypes[0].instanceOf(Types.ANY_ATOMIC_TYPE_O)) return false;
        kt = null;
        vt = ft.declType.eq(Types.ITEM_ZM) ? null : ft.declType;
      } else {
        return false;
      }
      return kt == null && vt == null || test((key, value) ->
        (kt == null || key.type.instanceOf(kt)) && (vt == null || vt.instance(value)));
    } catch(final QueryException ex) {
      throw Util.notExpected(ex);
    }
  }

  /**
   * Returns all keys of this map.
   * @return keys
   */
  public abstract Value keys();

  @Override
  public final Iter itemsIter() {
    final long size = structSize();
    if(size == 0) return Empty.ITER;
    if(size == 1) return valueAt(0).iter();

    // single-item values?
    if(((MapType) type).valueType().one()) {
      return new BasicIter<>(size) {
        @Override
        public Item get(final long i) {
          return (Item) valueAt((int) i);
        }
      };
    }

    final Iterator<Entry> entries = entries().iterator();
    return new Iter() {
      Iter iter = Empty.ITER;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item item = iter.next();
          if(item != null) return item;
          if(!entries.hasNext()) return null;
          iter = entries.next().value().iter();
        }
      }
    };
  }

  /**
   * Converts this map to the given map type.
   * @param mt map type
   * @param qc query context
   * @param ii input info (can be {@code null})
   * @param cc compilation context ({@code null} during runtime)
   * @return coerced map
   * @throws QueryException query exception
   */
  public final XQMap coerceTo(final MapType mt, final QueryContext qc, final InputInfo ii,
      final CompileContext cc) throws QueryException {

    final SeqType kt = mt.keyType().seqType(), vt = mt.valueType();
    final MapBuilder mb = new MapBuilder(structSize());
    forEach((key, value) -> {
      qc.checkStop();
      final Item k = (Item) kt.coerce(key, qc, ii, null, cc);
      if(mb.contains(k)) throw typeError(this, mt, ii);
      mb.put(k, vt.coerce(value, qc, ii, null, cc));
    });
    return mb.map();
  }

  /**
   * Converts this map to the given record type.
   * @param rt record type
   * @param qc query context
   * @param ii input info (can be {@code null})
   * @param cc compilation context ({@code null} during runtime)
   * @return coerced map
   * @throws QueryException query exception
   */
  public final XQMap coerceTo(final RecordType rt, final QueryContext qc, final InputInfo ii,
      final CompileContext cc) throws QueryException {

    final TokenObjectMap<RecordField> fields = rt.fields();
    // reject undeclared keys
    for(final Item key : keys()) {
      if(!key.type.isStringOrUntyped() || !fields.contains(key.string(null))) {
        throw typeError(this, rt, ii);
      }
    }

    // build record
    final int fs = fields.size();
    if(fs == 0) return empty();
    final Value[] values = new Value[fs];
    for(int f = 0; f < fs; f++) {
      final Value value = getOrNull(Str.get(fields.key(f + 1)));
      values[f] = fields.value(f + 1).seqType().coerce(value != null ? value : Empty.VALUE,
          qc, ii, null, cc);
    }
    return new XQRecordMap(rt, values);
  }

  @Override
  public boolean refineType() throws QueryException {
    Type refined = null;
    for(final Entry entry : entries()) {
      final Value value = entry.value();
      final MapType mt = MapType.get(entry.key().type, value.seqType());
      refined = refined == null ? mt : refined.union(mt);
      if(refined.eq(type)) return true;
    }
    type = refined;
    return true;
  }

  @Override
  protected final XQMap rebuild(final Job job) throws QueryException {
    final MapBuilder mb = new MapBuilder(structSize());
    forEach((key, value) -> mb.put(key, value.shrink(job)));
    return mb.map(this);
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
    if(!(item instanceof final XQMap map)) return false;

    // choose keys to compare
    Iterable<Item> keys1 = null, keys2 = null;
    if(deep != null && deep.options.get(DeepEqualOptions.IGNORE_EMPTY_ENTRIES)) {
      final HashItemSet set1 = new HashItemSet(Mode.DEEP, deep.info);
      forEach((k, v) -> { if(v != Empty.VALUE) set1.add(k); });
      final HashItemSet set2 = new HashItemSet(Mode.DEEP, deep.info);
      map.forEach((k, v) -> { if(v != Empty.VALUE) set2.add(k); });
      if(set1.size() == set2.size()) {
        keys1 = set1;
        keys2 = set2;
      }
    } else if(structSize() == map.structSize()) {
      keys1 = keys();
      keys2 = map.keys();
    }
    if(keys1 == null) return false;

    // deep = null: ordered comparison at compile time
    final Boolean order = deep == null ? null : deep.options.get(DeepEqualOptions.MAP_ORDER);
    final Iterator<Item> k2 = keys2.iterator();
    for(final Item k1 : keys1) {
      final Value v1 = getOrNull(k1), v2 = map.getOrNull(k1);
      if(order == Boolean.FALSE) {
        if(v2 == null || !deep.equal(v1, v2)) return false;
      } else if(order == Boolean.TRUE) {
        if(!(k2.hasNext() && k1.atomicEqual(k2.next()) && deep.equal(v1, v2))) return false;
      } else {
        if(!(k2.hasNext() && k1.equals(k2.next()) && v1.equals(v2))) return false;
      }
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
    for(final Entry entry : entries()) {
      if(c++ > 0) tb.add(',');
      if(indent) {
        tb.add('\n');
        addWS.accept(level + 1);
      }
      tb.add(entry.key()).add(':');
      if(indent) tb.add(' ');
      final Value value = entry.value();
      final boolean par = value.size() != 1;
      if(par) tb.add('(');
      int cc = 0;
      for(final Item item : value) {
        if(cc++ > 0) {
          tb.add(',');
          if(indent) tb.add(' ');
        }
        if(item instanceof final XQMap map) map.string(indent, tb, level + 1, ii);
        else if(item instanceof final XQArray array) array.string(indent, tb, level, ii);
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
    plan.add(plan.create(this, ENTRIES, structSize()));
  }

  @Override
  public final void toString(final QueryString qs) {
    if(structSize() == 0) {
      qs.token("{}");
    } else {
      final TokenBuilder tb = new TokenBuilder();
      for(final Entry entry : entries()) {
        if(!tb.moreInfo()) break;
        final Value value = entry.value();
        tb.add(entry.key).add(MAPASG).add(qs.error() ? value.toErrorString() : value).add(SEP);
      }
      qs.braced("{ ", tb.toString().replaceAll(", $", ""), " }");
    }
  }
}
