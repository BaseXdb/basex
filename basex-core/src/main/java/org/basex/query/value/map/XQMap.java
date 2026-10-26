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
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.hash.ItemSet.*;
import org.basex.query.value.*;
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

  /**
   * Creates a map with a known shape.
   * @param shape shape
   * @param values values, one per field
   * @return map
   */
  public static XQMap get(final ShapeType shape, final Value... values) {
    return values.length == 1 ? get(shape, values[0]) : new XQShapeValueMap(shape, values);
  }

  /**
   * Creates a map with a shape that has a single field.
   * @param shape shape with a single field
   * @param value value of the field
   * @return map
   */
  public static XQMap get(final ShapeType shape, final Value value) {
    if(value instanceof final Itr itr && itr.type == BasicType.INTEGER) {
      return new XQShapeIntMap(shape, itr.itr());
    }
    if(value instanceof final Dbl dbl) return new XQShapeDblMap(shape, dbl.dbl());
    return new XQShapeSingletonMap(shape, value);
  }

  /**
   * Returns a representation of this map that is based on a hash array mapped trie.
   * @return map
   * @throws QueryException query exception
   */
  XQMap trie() throws QueryException {
    final int size = (int) structSize();
    if(size < 2) return size == 0 ? empty() : empty().put(keyAt(0), valueAt(0));

    final Item[] keys = new Item[size];
    final Value[] values = new Value[size];
    final int[] hashes = new int[size], indexes = new int[size];
    MapType mt = null;
    for(int i = 0; i < size; i++) {
      final Item key = keyAt(i);
      final Value value = valueAt(i);
      keys[i] = key;
      values[i] = value;
      hashes[i] = key.hashCode();
      indexes[i] = i;
      // stop refining once the type of this map has been reached
      if(mt != type) {
        final SeqType st = value.seqType();
        mt = mt == null ? MapType.get(key.type, st) : mt.union(key.type, st);
        if(mt.eq(type)) mt = (MapType) type;
      }
    }
    final TrieNode root = TrieNode.build(keys, values, hashes, indexes, new int[size], 0, size, 0);
    return new XQTrieMap(root, new TrieOrder(keys), mt);
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
    final Value value = getOrNull(k);
    if(value != null) return value;
    if(type instanceof final ShapeType sh && sh.strict() && sh.index(k) == 0) {
      throw RECORDFIELD_X_X.get(ii, this, k);
    }
    return Empty.VALUE;
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
  public Value getOrNull(final Item key) throws QueryException {
    final int i = index(key);
    return i != -1 ? valueAt(i) : null;
  }

  /**
   * Returns the position of the specified key.
   * @param key key to look for
   * @return position (starting with 0), or {@code -1} if the key does not exist
   * @throws QueryException query exception
   */
  public int index(final Item key) throws QueryException {
    final long size = structSize();
    for(int i = 0; i < size; i++) {
      if(key.atomicEqual(keyAt(i))) return i;
    }
    return -1;
  }

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
   * Puts a value at the specified index into this map.
   * @param index map index (starting with 0, must be valid)
   * @param value value to insert
   * @return updated map
   * @throws QueryException query exception
   */
  public abstract XQMap putAt(int index, Value value) throws QueryException;

  /**
   * Removes a key from this map.
   * @param key key to remove
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public abstract XQMap remove(Item key) throws QueryException;

  /**
   * Returns this map without a record type annotation.
   * @return map without annotation, or {@code this} if the map is no record
   */
  public XQMap unannotated() {
    return this;
  }

  /**
   * Applies a function on all entries.
   * @param func function to apply on keys and values
   * @throws QueryException query exception
   */
  public final void forEach(final QueryBiConsumer<Item, Value> func) throws QueryException {
    final long size = structSize();
    for(long i = 0; i < size; i++) func.accept(keyAt(i), valueAt(i));
  }

  /**
   * Tests all entries.
   * @param func predicate function
   * @return {@code true} if the check is successful for all entries
   * @throws QueryException query exception
   */
  public final boolean test(final QueryBiPredicate<Item, Value> func) throws QueryException {
    final long size = structSize();
    for(long i = 0; i < size; i++) {
      if(!func.test(keyAt(i), valueAt(i))) return false;
    }
    return true;
  }

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
  public XQMap materialize(final Predicate<Data> test, final boolean funcs, final InputInfo ii,
      final QueryContext qc) throws QueryException {

    if(materialized(test, funcs, ii)) return this;

    final MapBuilder mb = new MapBuilder(structSize());
    forEach((key, value) -> {
      qc.checkStop();
      mb.put((Item) key.materialize(test, funcs, ii, qc), value.materialize(test, funcs, ii, qc));
    });
    return mb.map();
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final boolean funcs, final InputInfo ii)
      throws QueryException {
    return test((key, value) -> key.materialized(test, funcs, ii) &&
        value.materialized(test, funcs, ii));
  }

  /**
   * Checks if all values of this map are materialized.
   * @param test test to check if a node can be adopted unchanged
   * @param funcs accept function items without dependencies on the current query
   * @param ii input info (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  final boolean valuesMaterialized(final Predicate<Data> test, final boolean funcs,
      final InputInfo ii) throws QueryException {
    final long is = structSize();
    for(long i = 0; i < is; i++) {
      if(!valueAt(i).materialized(test, funcs, ii)) return false;
    }
    return true;
  }

  @Override
  public final boolean instanceOf(final Type tp, final boolean coerce) {
    if(type == tp) return true;

    try {
      if(tp instanceof final ShapeType sh) return sh.matches(this, coerce);
      if(SeqType.keptStatic(type, tp, coerce)) return true;

      final Type kt;
      final SeqType vt;
      if(tp instanceof final MapType mt) {
        kt = mt.keyType() == BasicType.ANY_ATOMIC_TYPE ? null : mt.keyType();
        vt = mt.valueType().eq(Types.ITEM_ZM) ? null : mt.valueType();
      } else if(!coerce && tp instanceof final FuncType ft) {
        // without coercion: maps are functions; with coercion, they are wrapped
        if(ft.declType.occ.min != 0 || ft.argTypes.length != 1 ||
            !ft.argTypes[0].instanceOf(Types.ANY_ATOMIC_TYPE_O)) return false;
        kt = null;
        vt = ft.declType.eq(Types.ITEM_ZM) ? null : ft.declType;
      } else {
        return false;
      }
      if(kt == null && vt == null) return true;
      if(!test((key, value) -> (kt == null || kt.seqType().instance(key, coerce)) &&
          (vt == null || vt.instance(value, coerce)))) return false;
      // remember the more specific type to skip subsequent checks (records keep their type)
      if(tp instanceof MapType && !(type instanceof ShapeType) && tp.assignable(coerce)) {
        refineType(tp);
      }
      return true;
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
          return (Item) valueAt(i);
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

    // record(*) is abstract: it is matched, but never constructed, by coercion
    if(rt.any()) throw typeError(this, rt, ii);

    // map with the fields of the record type in the same order: access values by position
    final TokenObjectMap<ShapeField> fields = rt.fields();
    final boolean sameOrder = this instanceof XQShapeMap && rt.sameOrder((ShapeType) type);
    if(!sameOrder) {
      // reject undeclared keys
      for(final Item key : keys()) {
        if(rt.index(key) == 0) throw typeError(this, rt, ii);
      }
    }

    // build record; a field that cannot be coerced is named in the error message
    final int fs = fields.size();
    final Value[] values = new Value[fs];
    for(int f = 0; f < fs; f++) {
      final ShapeField rf = fields.value(f + 1);
      final Value value = sameOrder ? valueAt(f) : getOrNull(rt.key(f + 1));
      if(value != null) {
        // static fields must not be supplied
        if(rf.isStatic()) throw typeError(this, rt, ii);
        values[f] = rt.coerce(f + 1, value, qc, ii, cc);
      } else if(rf.init() == null && rf.seqType().occ.min > 0) {
        throw typeError(this, rt, ii);
      } else {
        values[f] = rf.init(qc);
      }
    }
    return get(rt, values);
  }

  /**
   * Converts this map to the given simple record type.
   * @param sh simple record type
   * @param qc query context
   * @param ii input info (can be {@code null})
   * @param cc compilation context ({@code null} during runtime)
   * @return coerced map
   * @throws QueryException query exception
   */
  public final XQMap coerceTo(final ShapeType sh, final QueryContext qc, final InputInfo ii,
      final CompileContext cc) throws QueryException {

    // entries keep their order; a field that cannot be coerced is named in the error message
    final int fs = sh.fields().size();
    final MapBuilder mb = new MapBuilder(fs);
    forEach((key, value) -> {
      qc.checkStop();
      final int f = sh.index(key);
      if(f == 0) throw typeError(this, sh, ii);
      mb.put(sh.key(f), sh.coerce(f, value, qc, ii, cc));
    });
    if(structSize() < fs && addMissing(sh, mb) != 0) throw typeError(this, sh, ii);
    return mb.map();
  }

  /**
   * Casts this map to the given simple record type (see {@link SeqType#cast}).
   * @param sh simple record type
   * @param error raise error (return {@code null} otherwise)
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return cast map, or {@code null} if casting failed and no error was raised
   * @throws QueryException query exception
   */
  public final XQMap castTo(final ShapeType sh, final boolean error, final QueryContext qc,
      final InputInfo info) throws QueryException {

    // entries keep their order, undeclared entries are discarded
    final TokenObjectMap<ShapeField> fields = sh.fields();
    final MapBuilder mb = new MapBuilder(fields.size());
    if(!test((key, value) -> {
      qc.checkStop();
      final int f = sh.index(key);
      if(f == 0) return true;
      final Value cv = fields.value(f).seqType().convert(value, error, qc, info);
      if(cv == null) return false;
      mb.put(sh.key(f), cv);
      return true;
    })) return null;
    final int f = addMissing(sh, mb);
    if(f == 0) return mb.map();
    if(error) throw INVCONVERT_X_X.get(info, Empty.VALUE, fields.value(f).seqType());
    return null;
  }

  /**
   * Adds empty entries for the fields of a simple record type that are missing in a map.
   * @param sh simple record type
   * @param mb map builder
   * @return {@code 0}, or the index of a missing field that does not permit the empty sequence
   * @throws QueryException query exception
   */
  private static int addMissing(final ShapeType sh, final MapBuilder mb) throws QueryException {
    final TokenObjectMap<ShapeField> fields = sh.fields();
    final int fs = fields.size();
    for(int f = 1; f <= fs; f++) {
      final Str key = sh.key(f);
      if(mb.contains(key)) continue;
      if(!fields.value(f).seqType().instance(Empty.VALUE)) return f;
      mb.put(key, Empty.VALUE);
    }
    return 0;
  }

  /**
   * Casts this map to the given map type (see {@link SeqType#cast}).
   * @param mt map type
   * @param error raise error (return {@code null} otherwise)
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return cast map, or {@code null} if casting failed and no error was raised
   * @throws QueryException query exception
   */
  public final XQMap castTo(final MapType mt, final boolean error, final QueryContext qc,
      final InputInfo info) throws QueryException {
    if(mt == Types.MAP) return this;
    final SeqType kt = mt.keyType().seqType(), vt = mt.valueType();
    final MapBuilder mb = new MapBuilder(structSize());
    return test((key, value) -> {
      qc.checkStop();
      if(!(kt.convert(key, error, qc, info) instanceof final Item k)) return false;
      if(mb.contains(k)) {
        if(error) throw MAPDUPLKEY_X.get(info, k);
        return false;
      }
      final Value cv = vt.convert(value, error, qc, info);
      if(cv == null) return false;
      mb.put(k, cv);
      return true;
    }) ? mb.map() : null;
  }

  /**
   * Casts this map to the given record type (see {@link SeqType#cast}). Undeclared entries are
   * discarded; a missing field yields its default value, or the empty sequence if that is a valid
   * field value.
   * @param rt record type
   * @param error raise error (return {@code null} otherwise)
   * @param qc query context
   * @param info input info (can be {@code null})
   * @return cast record, or {@code null} if casting failed and no error was raised
   * @throws QueryException query exception
   */
  public final XQMap castTo(final RecordType rt, final boolean error, final QueryContext qc,
      final InputInfo info) throws QueryException {
    // record(*) is abstract: only records can be cast to it
    if(rt.any()) return instanceOf(rt, false) ? this : null;
    final TokenObjectMap<ShapeField> fields = rt.fields();
    final int fs = fields.size();
    final Value[] values = new Value[fs];
    for(int f = 0; f < fs; f++) {
      qc.checkStop();
      final ShapeField rf = fields.value(f + 1);
      final SeqType ft = rf.seqType();
      final Value value = getOrNull(rt.key(f + 1));
      final Value cast;
      if(value != null) {
        if(!rf.isStatic()) cast = ft.convert(value, error, qc, info);
        // static fields can only be supplied by records of the target type
        else if(instanceOf(rt, false)) cast = value;
        else if(error) throw typeError(this, rt, info);
        else cast = null;
      } else if(rf.init() != null) {
        cast = rf.init(qc);
      } else if(ft.instance(Empty.VALUE)) {
        cast = Empty.VALUE;
      } else {
        if(error) throw INVCONVERT_X_X.get(info, Empty.VALUE, ft);
        cast = null;
      }
      if(cast == null) return null;
      values[f] = cast;
    }
    return get(rt, values);
  }

  @Override
  public boolean refineType() {
    Type refined = null;
    for(final Entry entry : entries()) {
      final Value value = entry.value();
      final MapType mt = MapType.get(entry.key().type, value.seqType());
      refined = refined == null ? mt : refined.union(mt);
      if(refined.eq(type)) return true;
    }
    if(refined != null) refineType(refined);
    return true;
  }

  @Override
  protected final XQMap rebuild(final QueryContext qc) throws QueryException {
    final MapBuilder mb = new MapBuilder(structSize());
    forEach((key, value) -> mb.put(key, value.shrink(qc)));
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

    // deep = null: ordered comparison at compile time
    final Boolean order = deep == null ? null : deep.options.get(DeepEqualOptions.MAP_ORDER);
    if(deep != null && deep.options.get(DeepEqualOptions.IGNORE_EMPTY_ENTRIES)) {
      final HashItemSet set1 = new HashItemSet(Mode.DEEP, deep.info);
      forEach((k, v) -> { if(v != Empty.VALUE) set1.add(k); });
      final HashItemSet set2 = new HashItemSet(Mode.DEEP, deep.info);
      map.forEach((k, v) -> { if(v != Empty.VALUE) set2.add(k); });
      if(set1.size() != set2.size()) return false;

      final Iterator<Item> k2 = set2.iterator();
      for(final Item k1 : set1) {
        if(!equal(k1, getOrNull(k1), k2.next(), map.getOrNull(k1), order, deep)) return false;
      }
    } else {
      final long size = structSize();
      if(size != map.structSize()) return false;

      for(long i = 0; i < size; i++) {
        final Item k1 = keyAt(i);
        final Value v2 = order == Boolean.FALSE ? map.getOrNull(k1) : map.valueAt(i);
        if(!equal(k1, valueAt(i), map.keyAt(i), v2, order, deep)) return false;
      }
    }
    return true;
  }

  /**
   * Compares two map entries.
   * @param k1 first key
   * @param v1 first value
   * @param k2 second key (ignored for unordered comparisons)
   * @param v2 second value (can be {@code null})
   * @param order ordered comparison (can be {@code null})
   * @param deep comparator (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  private static boolean equal(final Item k1, final Value v1, final Item k2, final Value v2,
      final Boolean order, final DeepEqual deep) throws QueryException {
    return order == Boolean.FALSE ? v2 != null && deep.equal(v1, v2) :
      order == Boolean.TRUE ? k1.atomicEqual(k2) && deep.equal(v1, v2) :
      k1.equals(k2) && v1.equals(v2);
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
