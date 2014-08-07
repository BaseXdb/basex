package org.basex.query.value.map;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * The map item.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class Map extends FItem {
  /** The empty map. */
  public static final Map EMPTY = new Map(TrieNode.EMPTY);
  /** Number of bits per level, maximum is 5 because {@code 1 << 5 == 32}. */
  static final int BITS = 5;

  /** Wrapped immutable map. */
  private final TrieNode root;
  /** Key sequence. */
  private Value keys;

  /**
   * Constructor.
   * @param root map
   */
  private Map(final TrieNode root) {
    super(SeqType.ANY_MAP, new Ann());
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
    return get(args[0].item(qc, ii), ii).item(qc, ii);
  }

  @Override
  public Value invValue(final QueryContext qc, final InputInfo ii, final Value... args)
      throws QueryException {
    return get(args[0].item(qc, ii), ii);
  }

  /**
   * Checks the key item.
   * @param it item
   * @param ii input info
   * @return possibly atomized item if non {@code NaN}, {@code null} otherwise
   * @throws QueryException query exception
   */
  private static Item key(final Item it, final InputInfo ii) throws QueryException {
    // no empty sequence allowed
    if(it == null) throw SEQEMPTY.get(ii);

    // function items can't be keys
    if(it instanceof FItem) throw FIATOM.get(ii, it.type);

    // nodes are converted to untyped atomics
    return it instanceof ANode ? new Atm(it.string(ii)) : it;
  }

  /**
   * Deletes a key from this map.
   * @param key key to delete
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public Map delete(final Item key, final InputInfo ii) throws QueryException {
    final Item k = key(key, ii);
    if(k == null) return this;

    final TrieNode del = root.delete(k.hash(ii), k, 0, ii);
    return del == root ? this : del != null ? new Map(del) : EMPTY;
  }

  /**
   * Gets the value from this map.
   * @param key key to look for
   * @param ii input info
   * @return bound value if found, the empty sequence {@code ()} otherwise
   * @throws QueryException query exception
   */
  public Value get(final Item key, final InputInfo ii) throws QueryException {
    final Item k = key(key, ii);
    if(k == null) return Empty.SEQ;

    final Value val = root.get(k.hash(ii), k, 0, ii);
    return val == null ? Empty.SEQ : val;
  }

  /**
   * Checks if the given key exists in the map.
   * @param k key to look for
   * @param ii input info
   * @return {@code true()}, if the key exists, {@code false()} otherwise
   * @throws QueryException query exception
   */
  public boolean contains(final Item k, final InputInfo ii) throws QueryException {
    final Item key = key(k, ii);
    return key != null && root.contains(key.hash(ii), key, 0, ii);
  }

  /**
   * Adds all bindings from the given map into {@code this}.
   * @param other map to add
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public Map addAll(final Map other, final InputInfo ii) throws QueryException {
    if(other == EMPTY) return this;
    final TrieNode upd = root.addAll(other.root, 0, ii);
    return upd == other.root ? other : new Map(upd);
  }

  /**
   * Checks if the map has the given type.
   * @param t type
   * @return {@code true} if the type fits, {@code false} otherwise
   */
  public boolean hasType(final MapType t) {
    return root.hasType(t.keyType == AtomType.AAT ? null : t.keyType,
        t.retType.eq(SeqType.ITEM_ZM) ? null : t.retType);
  }

  @Override
  public Map coerceTo(final FuncType ft, final QueryContext qc, final InputInfo ii,
      final boolean opt) throws QueryException {
    if(!(ft instanceof MapType) || !hasType((MapType) ft)) throw castError(ii, this, ft);
    return this;
  }

  /**
   * Inserts the given value into this map.
   * @param k key to insert
   * @param v value to insert
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public Map insert(final Item k, final Value v, final InputInfo ii) throws QueryException {
    final Item key = key(k, ii);
    if(key == null) return this;
    final TrieNode ins = root.insert(key.hash(ii), key, v, 0, ii);
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
    if(keys == null) {
      final ValueBuilder res = new ValueBuilder(root.size);
      root.keys(res);
      keys = res.value();
    }
    return keys;
  }

  /**
   * Checks if the this map is deep-equal to the given one.
   * @param ii input info
   * @param o other map
   * @param coll collation
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean deep(final InputInfo ii, final Map o, final Collation coll) throws QueryException {
    return root.deep(ii, o.root, coll);
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
    return BRACE1 + DOTS + BRACE2;
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
  private void string(final TokenBuilder tb, final int level, final InputInfo ii)
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
