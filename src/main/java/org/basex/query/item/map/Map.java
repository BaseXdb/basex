package org.basex.query.item.map;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import java.io.IOException;
import java.util.HashMap;

import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.AtomType;
import org.basex.query.item.Bln;
import org.basex.query.item.Dbl;
import org.basex.query.item.Empty;
import org.basex.query.item.FItem;
import org.basex.query.item.Flt;
import org.basex.query.item.FuncType;
import org.basex.query.item.Item;
import org.basex.query.item.Int;
import org.basex.query.item.MapType;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Str;
import org.basex.query.item.Value;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.ValueIter;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.hash.TokenObjMap;

/**
 * The map item.
 *
 * @author BaseX Team 2005-11, BSD License
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
  /** Size. */
  private Int size;

  /**
   * Constructor.
   * @param m map
   */
  private  Map(final TrieNode m) {
    super(SeqType.ANY_MAP);
    root = m;
  }

  @Override
  public int arity() {
    return 1;
  }

  @Override
  public QNm fName() {
    return null;
  }

  @Override
  public Value invValue(final QueryContext ctx, final InputInfo ii,
      final Value... args) throws QueryException {
    return get(args[0].item(ctx, ii), ii);
  }

  /**
   * Checks the key item.
   * @param it item
   * @param ii input info
   * @return possibly atomized item if non {@code NaN}, {@code null} otherwise
   * @throws QueryException query exception
   */
  private Item key(final Item it, final InputInfo ii) throws QueryException {
    // no empty sequence allowed
    if(it == null) throw XPEMPTY.thrw(ii, desc());

    // function items can't be keys
    if(it instanceof FItem) throw FNATM.thrw(ii, it.desc());

    // NaN can't be stored as key, as it isn't equal to anything
    if(it == Flt.NAN || it == Dbl.NAN) return null;

    // untyped items are converted to strings
   return it.isUntyped() ? Str.get(it.string(ii)) : it;
  }

  /**
   * Deletes a key from this map.
   * @param key key to delete
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public Map delete(final Item key, final InputInfo ii)
      throws QueryException {
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
  public Bln contains(final Item k, final InputInfo ii) throws QueryException {
    final Item key = key(k, ii);
    return Bln.get(key != null && root.contains(key.hash(ii), key, 0, ii));
  }

  /**
   * Adds all bindings from the given map into {@code this}.
   * @param other map to add
   * @param ii input info
   * @return updated map if changed, {@code this} otherwise
   * @throws QueryException query exception
   */
  public Map addAll(final Map other, final InputInfo ii)
      throws QueryException {
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
        t.ret.eq(SeqType.ITEM_ZM) ? null : t.ret);
  }

  @Override
  public Map coerceTo(final FuncType ft, final QueryContext ctx,
      final InputInfo ii) throws QueryException {
    if(!(ft instanceof MapType) || !hasType((MapType) ft))
      throw Err.cast(ii, ft, this);

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
  public Map insert(final Item k, final Value v, final InputInfo ii)
      throws QueryException {
    final Item key = key(k, ii);
    if(key == null) return this;

    final TrieNode ins = root.insert(key.hash(ii), key, v, 0, ii);
    return ins == root ? this : new Map(ins);
  }

  /**
   * Number of values contained in this map.
   * @return size
   */
  public Int mapSize() {
    if(size == null) size = Int.get(root.size);
    return size;
  }

  /**
   * All keys defined in this map.
   * @return list of keys
   */
  public Value keys() {
    if(keys == null) {
      final ItemCache res = new ItemCache(root.size);
      root.keys(res);
      keys = res.value();
    }
    return keys;
  }

  /**
   * Collation of this map.
   * @return collation
   */
  public Str collation() {
    return Str.get(URLCOLL);
  }

  /**
   * Checks if the this map is deep-equal to the given one.
   * @param ii input info
   * @param o other map
   * @return result of check
   * @throws QueryException query exception
   */
  public boolean deep(final InputInfo ii, final Map o) throws QueryException {
    return root.deep(ii, o.root);
  }

  /**
   * Converts the map to a map with tokens as keys and java objects as values.
   * @param ii input info
   * @return token map
   * @throws QueryException query exception
   */
  public TokenObjMap<Object> tokenJavaMap(final InputInfo ii)
      throws QueryException {

    final TokenObjMap<Object> tm = new TokenObjMap<Object>();
    final ValueIter vi = keys().iter();
    for(Item k; (k = vi.next()) != null;) {
      if(!k.isString()) FUNCMP.thrw(ii, desc(), AtomType.STR, k.type);
      tm.add(k.string(null), get(k, ii).toJava());
    }
    return tm;
  }

  @Override
  public HashMap<Object, Object> toJava() throws QueryException {
    final HashMap<Object, Object> map = new HashMap<Object, Object>();
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
  public boolean isMap() {
    return true;
  }

  @Override
  public String desc() {
    return MAPSTR + BRACE1 + DOTS + BRACE2;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    final long s = mapSize().itr(null);
    ser.openElement(MAP, SIZE, Token.token(s));
    final Value ks = keys();
    try {
      for(long i = 0, max = Math.min(s, 5); i < max; i++) {
        final Item key = ks.itemAt(i);
        final Value val = get(key, null);
        ser.openElement(ENTRY, KEY, key.string(null));
        val.plan(ser);
        ser.closeElement();
      }
    } catch(final QueryException ex) {
      Util.notexpected(ex);
    }
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = root.toString(new StringBuilder("map{ "));
    // remove superfluous comma
    if(root.size > 0) sb.deleteCharAt(sb.length() - 2);
    return sb.append("}").toString();
  }
}
