package org.basex.query.value.map;

import static org.basex.query.QueryError.*;
import static org.basex.query.QueryText.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * The map item.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class Map extends FItem {
  /** The empty map. */
  public static final Map EMPTY = new Map(TrieNode.EMPTY);
  /** Number of bits per level, maximum is 5 because {@code 1 << 5 == 32}. */
  static final int BITS = 5;

  /** Wrapped immutable map. */
  private final TrieNode root;

  /**
   * Constructor.
   * @param root map
   */
  private Map(final TrieNode root) {
    super(SeqType.ANY_MAP, new AnnList());
    this.root = root;
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
  public QNm paramName(final int pos) {
    return new QNm("key", "");
  }

  @Override
  public FuncType funcType() {
    return MapType.get(AtomType.AAT, SeqType.ITEM_ZM);
  }

  @Override
  public void materialize(final InputInfo info) throws QueryException {
    root.materialize(info);
  }

  @Override
  public int stackFrameSize() {
    return 0;
  }

  @Override
  public Item invItem(final QueryContext qc, final InputInfo info, final Value... args)
      throws QueryException {
    return invValue(qc, info, args).item(qc, info);
  }

  @Override
  public Value invValue(final QueryContext qc, final InputInfo info, final Value... args)
      throws QueryException {
    final Item key = args[0].atomItem(qc, info);
    if(key == null) throw EMPTYFOUND.get(info);
    return get(key, info);
  }

  /**
   * Deletes a key from this map.
   * @param key key to delete (must not be {@code null})
   * @param info input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public Map delete(final Item key, final InputInfo info) throws QueryException {
    final TrieNode del = root.delete(key.hash(info), key, 0, info);
    return del == root ? this : del == null ? EMPTY : new Map(del);
  }

  /**
   * Gets the value from this map.
   * @param key key to look for (must not be {@code null})
   * @param info input info
   * @return bound value if found, the empty sequence {@code ()} otherwise
   * @throws QueryException query exception
   */
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
  public Map addAll(final Map map, final MergeDuplicates merge, final InputInfo info,
      final QueryContext qc) throws QueryException {

    if(map == EMPTY) return this;
    final TrieNode upd = root.addAll(map.root, 0, merge, info, qc);
    return upd == map.root ? map : new Map(upd);
  }

  @Override
  public boolean instanceOf(final Type tp) {
    return tp == AtomType.ITEM || tp instanceof FuncType && instanceOf((FuncType) tp, false);
  }

  @Override
  public Map coerceTo(final FuncType ft, final QueryContext qc, final InputInfo info,
      final boolean opt) throws QueryException {

    if(instanceOf(ft, true)) return this;
    throw typeError(this, ft, info);
  }

  /**
   * Checks if this is an instance of the specified type.
   * @param ft type
   * @param coerce coerce value
   * @return result of check
   */
  private boolean instanceOf(final FuncType ft, final boolean coerce) {
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
  public Map put(final Item key, final Value value, final InputInfo info) throws QueryException {
    final TrieNode ins = root.put(key.hash(info), key, value, 0, info);
    return ins == root ? this : new Map(ins);
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

    if(item instanceof Map) return root.deep(info, ((Map) item).root, coll);
    return item instanceof FItem && !(item instanceof Array) && super.deep(item, info, coll);
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

  /**
   * Returns a string representation of the map.
   * @param indent indent output
   * @param tb token builder
   * @param level current level
   * @param info input info
   * @throws QueryException query exception
   */
  public void string(final boolean indent, final TokenBuilder tb, final int level,
      final InputInfo info) throws QueryException {

    tb.add("map{");
    int c = 0;
    for(final Item key : keys()) {
      if(c++ > 0) tb.add(',');
      if(indent) {
        tb.add('\n');
        indent(tb, level + 1);
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
        if(item instanceof Map) ((Map) item).string(indent, tb, level + 1, info);
        else if(item instanceof Array) ((Array) item).string(indent, tb, level, info);
        else tb.add(item.toString());
      }
      if(par) tb.add(')');
    }
    if(indent) {
      tb.add('\n');
      indent(tb, level);
    }
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
  public Expr inlineExpr(final Expr[] exprs, final CompileContext cc, final InputInfo info) {
    return null;
  }

  @Override
  public boolean isVacuousBody() {
    return false;
  }

  @Override
  public boolean equals(final Object obj) {
    // [CG] could be enhanced
    return this == obj;
  }

  @Override
  public String toString() {
    return MAP + " { " + root.append(new StringBuilder()).toString().replaceAll(", $", "") + " }";
  }
}
