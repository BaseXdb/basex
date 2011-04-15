package org.basex.query.item;

import static org.basex.query.util.Err.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.iter.ItemCache;
import org.basex.query.util.Err;
import org.basex.query.util.map.HashTrie;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;

/**
 * The map item.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public class Map extends Fun {
  /** Wrapped immutable map. */
  private final HashTrie map;

  /**
   * Constructor.
   * @param m map
   */
  public Map(final HashTrie m) {
    super(FunType.get(new SeqType[]{ SeqType.AAT }, SeqType.ITEM_ZM));
    map = m;
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
   * Gets the value from this map.
   * @param key key to look for
   * @param ii input info
   * @return bound value if found, the empty sequence {@code ()} otherwise
   * @throws QueryException query exception
   */
  public Value get(final Item key, final InputInfo ii) throws QueryException {
    if(key == null) XPEMPTY.thrw(ii, desc());

    // NaN can't be stored as key, as it isn't equal to anything
    if(key == Flt.NAN || key == Dbl.NAN) return Empty.SEQ;

    // untyped items are converted to strings
    final Item k = key.type.unt() ? Str.get(key.atom(ii)) : key;

    final Value val = map.get(k, ii);
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
    // NaN can't be stored as key, as it isn't equal to anything
    if(k == Flt.NAN || k == Dbl.NAN) return Bln.FALSE;

    // untyped items are converted to strings
    final Item key = k.type.unt() ? Str.get(k.atom(ii)) : k;

    return Bln.get(map.contains(key, ii));
  }

  @Override
  public String toString() {
    try {
      final TokenBuilder tb = new TokenBuilder("map{");
      final ItemCache keys = map.keys();
      for(Item k; (k = keys.next()) != null;) {
        if(tb.size() > 4) tb.add(", ");
        tb.add(k.toString()).add(":=").add(map.get(k, null).toString());
      }
      return tb.add("}").toString();
    } catch(final QueryException e) {
      throw Util.notexpected(e);
    }
  }

  @Override
  Fun coerceTo(final FunType ft, final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    if(!(ft instanceof MapType)) throw Err.cast(ii, ft, this);
    final MapType mt = (MapType) ft;

    final AtomType k = mt.keyType == AtomType.AAT ? null : mt.keyType;
    final SeqType v = SeqType.ITEM_ZM.eq(mt.ret) ? null : mt.ret;
    final ItemCache keys = map.keys();
    for(long i = keys.size(); i-- != 0;) {
      final Item key = keys.get(i);
      if(k != null && !k.instance(key.type)) Err.type(ii, desc(), k, key);
      if() {
        
      }
      final Value bound = get(key, ii);
      if(!v.instance(bound))
    }
    return null;
  }
}
