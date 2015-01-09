package org.basex.query.value.map;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * The map item.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
public final class Map extends FItem {
  /** The empty map. */
  public static final Map EMPTY = new Map(TrieNode.EMPTY, 0);
  /** Number of bits per level, maximum is 5 because {@code 1 << 5 == 32}. */
  static final int BITS = 5;

  /** Wrapped immutable map. */
  private final TrieNode root;
  /** Key sequence. */
  private Value keys;
  /** Date/time entries (negative: without timezone). */
  private final int dt;

  /**
   * Constructor.
   * @param root map
   * @param dt number of date/time entries (negative: without timezone)
   */
  private Map(final TrieNode root, final int dt) {
    super(SeqType.ANY_MAP, new Ann());
    this.root = root;
    this.dt = dt;
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
    return new QNm("key", "");
  }

  @Override
  public FuncType funcType() {
    return MapType.get(AtomType.AAT, SeqType.ITEM_ZM);
  }

  @Override
  public int stackFrameSize() {
    return 0;
  }

  @Override
  public Item invItem(final QueryContext qc, final InputInfo ii, final Value... args)
      throws QueryException {
    return invValue(qc, ii, args).item(qc, ii);
  }

  @Override
  public Value invValue(final QueryContext qc, final InputInfo ii, final Value... args)
      throws QueryException {
    final Item key = args[0].atomItem(qc, ii);
    if(key == null) throw EMPTYFOUND.get(ii);
    return get(key, ii);
  }

  /**
   * Deletes a key from this map.
   * @param key key to delete (must not be {@code null})
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public Map delete(final Item key, final InputInfo ii) throws QueryException {
    final TrieNode del = root.delete(key.hash(ii), key, 0, ii);
    if(del == root) return this;
    if(del == null) return EMPTY;
    // update date counter
    int t = dt;
    if(key instanceof ADate) {
      final boolean tz = ((ADate) key).zon() != Short.MAX_VALUE;
      t += tz ? -1 : +1;
    }
    return new Map(del, t);
  }

  /**
   * Gets the value from this map.
   * @param key key to look for (must not be {@code null})
   * @param ii input info
   * @return bound value if found, the empty sequence {@code ()} otherwise
   * @throws QueryException query exception
   */
  public Value get(final Item key, final InputInfo ii) throws QueryException {
    final Value v = root.get(key.hash(ii), key, 0, ii);
    return v == null ? Empty.SEQ : v;
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
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public Map addAll(final Map map, final InputInfo ii) throws QueryException {
    if(map == EMPTY) return this;
    final TrieNode upd = root.addAll(map.root, 0, ii);
    if(upd == map.root) return map;
    if(map.dt != 0 && dt != 0 && (map.dt > 0 ? dt < 0 : dt > 0)) throw MAPTZ.get(ii);
    return new Map(upd, map.dt + dt);
  }

  /**
   * Checks if the map has the given type.
   * @param mt type
   * @return {@code true} if the type fits, {@code false} otherwise
   */
  public boolean hasType(final MapType mt) {
    return root.hasType(mt.keyType == AtomType.AAT ? null : mt.keyType,
        mt.retType.eq(SeqType.ITEM_ZM) ? null : mt.retType);
  }

  @Override
  public Map coerceTo(final FuncType ft, final QueryContext qc, final InputInfo ii,
      final boolean opt) throws QueryException {
    if(!(ft instanceof MapType) || !hasType((MapType) ft)) throw castError(ii, this, ft);
    return this;
  }

  /**
   * Puts the given value into this map and replaces existing keys.
   * @param key key to insert (must not be {@code null})
   * @param value value to insert
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public Map put(final Item key, final Value value, final InputInfo ii) throws QueryException {
    final TrieNode ins = root.put(key.hash(ii), key, value, 0, ii);
    // update date counter
    int t = dt;
    if(key instanceof ADate) {
      final boolean tz = ((ADate) key).zon() != Short.MAX_VALUE;
      if(tz ? t < 0 : t > 0) throw MAPTZ.get(ii);
      t += tz ? 1 : -1;
    }
    return ins == root ? this : new Map(ins, t);
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
    if(keys == null) {
      final ValueBuilder res = new ValueBuilder(root.size);
      root.keys(res);
      keys = res.value();
    }
    return keys;
  }

  /**
   * All values defined in this map.
   * @return list of keys
   */
  public ValueBuilder values() {
    final ValueBuilder res = new ValueBuilder(root.size);
    root.values(res);
    return res;
  }

  /**
   * Applies a function on all entries.
   * @param func function to apply on keys and values
   * @param qc query context
   * @param ii input info
   * @return resulting value
   * @throws QueryException query exception
   */
  public ValueBuilder apply(final FItem func, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    final ValueBuilder vb = new ValueBuilder(root.size);
    root.apply(vb, func, qc, ii);
    return vb;
  }

  @Override
  public boolean deep(final Item item, final InputInfo ii, final Collation coll)
      throws QueryException {

    if(item instanceof Map) return root.deep(ii, ((Map) item).root, coll);
    return item instanceof FItem && !(item instanceof Array) && super.deep(item, ii, coll);
  }

  /**
   * Returns a string representation of the map.
   * @param ii input info
   * @return string
   * @throws QueryException query exception
   */
  public byte[] serialize(final InputInfo ii) throws QueryException {
    final TokenBuilder tb = new TokenBuilder();
    string(tb, 0, ii);
    return tb.finish();
  }

  @Override
  public HashMap<Object, Object> toJava() throws QueryException {
    final HashMap<Object, Object> map = new HashMap<>();
    final ValueIter vi = keys().iter();
    for(Item k; (k = vi.next()) != null;) {
      map.put(k.toJava(), get(k, null).toJava());
    }
    return map;
  }

  @Override
  public int hash(final InputInfo ii) throws QueryException {
    return root.hash(ii);
  }

  @Override
  public String description() {
    return CURLY1 + DOTS + CURLY2;
  }

  @Override
  public void plan(final FElem plan) {
    final int s = mapSize();
    final FElem el = planElem(SIZE, s);
    final Value ks = keys();
    try {
      final int max = Math.min(s, 5);
      for(long i = 0; i < max; i++) {
        final Item key = ks.itemAt(i);
        final Value val = get(key, null);
        key.plan(el);
        val.plan(el);
      }
    } catch(final QueryException ex) {
      throw Util.notExpected(ex);
    }
    addPlan(plan, el);
  }

  /**
   * Returns a string representation of the map.
   * @param tb token builder
   * @param level current level
   * @param ii input info
   * @throws QueryException query exception
   */
  public void string(final TokenBuilder tb, final int level, final InputInfo ii)
      throws QueryException {

    tb.add("{");
    int c = 0;
    for(final Item key : keys()) {
      if(c++ > 0) tb.add(',');
      tb.add('\n');
      indent(tb, level + 1);
      tb.add(key.toString());
      tb.add(": ");
      final Value v = get(key, ii);
      if(v.size() != 1) tb.add('(');
      int cc = 0;
      for(final Item it : v) {
        if(cc++ > 0) tb.add(", ");
        if(it instanceof Map) ((Map) it).string(tb, level + 1, ii);
        else if(it instanceof Array) ((Array) it).string(tb, ii);
        else tb.add(it.toString());
      }
      if(v.size() != 1) tb.add(')');
    }
    tb.add('\n');
    indent(tb, level);
    tb.add('}');
  }

  /**
   * Adds some indentation.
   * @param tb token builder
   * @param level level
   */
  private static void indent(final TokenBuilder tb, final int level) {
    for(int l = 0; l < level; l++) tb.add("  ");
  }

  @Override
  public Expr inlineExpr(final Expr[] exprs, final QueryContext qc, final VarScope scp,
      final InputInfo ii) {
    return null;
  }

  @Override
  public String toString() {
    final StringBuilder sb = root.toString(new StringBuilder(MAPSTR).append(" { "));
    // remove superfluous comma
    if(root.size > 0) sb.deleteCharAt(sb.length() - 2);
    return sb.append('}').toString();
  }
}
