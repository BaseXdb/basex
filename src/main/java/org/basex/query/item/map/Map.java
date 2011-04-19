package org.basex.query.item.map;

import static org.basex.query.util.Err.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.item.AtomType;
import org.basex.query.item.Bln;
import org.basex.query.item.Dbl;
import org.basex.query.item.Empty;
import org.basex.query.item.Flt;
import org.basex.query.item.FItem;
import org.basex.query.item.FunType;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.MapType;
import org.basex.query.item.QNm;
import org.basex.query.item.SeqType;
import org.basex.query.item.Str;
import org.basex.query.item.Value;
import org.basex.query.iter.ItemCache;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * The map item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public class Map extends FItem {
  /** Number of bits per level, maximum is 5 because {@code 1 << 5 == 32}. */
  static final int BITS = 5;

  /** Wrapped immutable map. */
  private final TrieNode root;
  /** the empty map. */
  public static final Map EMPTY = new Map(TrieNode.EMPTY);

  /** Key sequence. */
  private Value keys;

  /** Size. */
  private Itr size;

  /**
   * Constructor.
   * @param m map
   */
  public Map(final TrieNode m) {
    super(MapType.ANY_MAP);
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
    if(it == null) XPEMPTY.thrw(ii, desc());

    // NaN can't be stored as key, as it isn't equal to anything
    if(it == Flt.NAN || it == Dbl.NAN) return null;

    // untyped items are converted to strings
   return it.type.unt() ? Str.get(it.atom(ii)) : it;
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

  /**
   * Verifies the data structure.
   * @return result of sanity checks
   */
  public boolean verify() {
    return root.verify();
  }

  @Override
  public String toString() {
    try {
      final TokenBuilder tb = new TokenBuilder("map { ");
      final Value ks = keys();
      for(long i = 0, len = ks.size(); i < len; i++) {
        final Item k = ks.itemAt(i);
        if(tb.size() > 6) tb.add(", ");
        tb.add(k.toString()).add(":=").add(get(k, null).toString());
      }
      return tb.add(" }").toString();
    } catch(final QueryException e) {
      throw Util.notexpected(e);
    }
  }

  @Override
  public Map coerceTo(final FunType ft, final QueryContext ctx,
      final InputInfo ii) throws QueryException {
    if(!(ft instanceof MapType) || !hasType((MapType) ft))
      throw Err.cast(ii, ft, this);

    return this;
  }

  /**
   * Creates a new singleton map, containing one binding.
   * @param key key to store
   * @param val value bound to the key
   * @param ii input info
   * @return singleton map
   * @throws QueryException query exception
   */
  public static Map singleton(final Item key, final Value val,
      final InputInfo ii) throws QueryException {
    return EMPTY.insert(key, val, ii);
  }

  /**
   * Number of values contained in this map.
   * @return size
   */
  public Itr mapSize() {
    if(size == null) size = Itr.get(root.size);
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
      keys = res.finish();
    }
    return keys;
  }

  /**
   * Collation of this map.
   * @return collation
   */
  public Str collation() {
    return Str.get(QueryTokens.URLCOLL);
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    return it instanceof Map && root.eq(ii, ((Map) it).root);
  }

  @Override
  public int hash(final InputInfo ii) throws QueryException {
    return root.hash(ii);
  }
}
