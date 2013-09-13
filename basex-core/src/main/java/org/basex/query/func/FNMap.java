package org.basex.query.func;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.util.*;

/**
 * Functions on maps.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class FNMap extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNMap(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _MAP_GET:  return get(ctx).iter();
      case _MAP_KEYS: return map(ctx).keys().iter();
      default:        return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _MAP_GET:  return get(ctx);
      case _MAP_KEYS: return map(ctx).keys();
      default:        return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _MAP_NEW:       return newMap(ctx, ii);
      case _MAP_ENTRY:     return entry(ctx, ii);
      case _MAP_CONTAINS:  return contains(ctx, ii);
      case _MAP_SIZE:      return map(ctx).mapSize();
      case _MAP_REMOVE:    return remove(ctx, ii);
      case _MAP_COLLATION: return map(ctx).collation();
      default:             return super.item(ctx, ii);
    }
  }

  /**
   * Removes a key from a map.
   * @param ctx query context
   * @param ii input info
   * @return new map with key removed from it
   * @throws QueryException query exception
   */
  private Map remove(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return map(ctx).delete(expr[1].item(ctx, ii), ii);
  }

  /**
   * Creates a new map containing the given key-value pair.
   * @param ctx query context
   * @param ii input info
   * @return the singleton map
   * @throws QueryException query exception
   */
  private Map entry(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return Map.EMPTY.insert(expr[0].item(ctx, ii), ctx.value(expr[1]), ii);
  }

  /**
   * Creates a new map from a list of old ones.
   * @param ctx query context
   * @param ii input info
   * @return new map
   * @throws QueryException query exception
   */
  private Map newMap(final QueryContext ctx, final InputInfo ii) throws QueryException {
    if(expr.length == 0) return Map.EMPTY;
    // collations are ignored here as they may disappear in a future version
    checkColl(expr.length == 2 ? expr[1] : null, ctx);

    Map map = Map.EMPTY;
    final Iter maps = expr[0].iter(ctx);
    for(Item m; (m = maps.next()) != null;)
      map = map.addAll(checkMap(m), ii);
    return map;
  }

  /**
   * Looks up the given key in the given map.
   * @param ctx query context
   * @return bound value or empty sequence if none exists
   * @throws QueryException query exception
   */
  private Value get(final QueryContext ctx) throws QueryException {
    return map(ctx).get(expr[1].item(ctx, info), info);
  }

  /**
   * Checks if the given key is contained in the given map.
   * @param ctx query context
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  private Bln contains(final QueryContext ctx, final InputInfo ii) throws QueryException {
    return map(ctx).contains(expr[1].item(ctx, ii), ii);
  }

  /**
   * Gets the map at the first argument position.
   * @param ctx query context
   * @return map
   * @throws QueryException query exception
   */
  private Map map(final QueryContext ctx) throws QueryException {
    return checkMap(checkItem(expr[0], ctx));
  }
}
