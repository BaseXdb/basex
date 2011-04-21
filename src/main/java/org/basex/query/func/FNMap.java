package org.basex.query.func;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.MapType;
import org.basex.query.item.Value;
import org.basex.query.item.map.Map;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;

/**
 * Functions on maps.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public final class FNMap extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNMap(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case MAPGET:  return get(ctx).iter();
      case MAPKEYS: return getMap(ctx).keys().iter();
      default:      return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(def) {
      case MAPGET:  return get(ctx);
      case MAPKEYS: return getMap(ctx).keys();
      default:      return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case MAPNEW:   return newMap(ctx, ii);
      case MAPENTRY: return entry(ctx, ii);
      case MAPCONT:  return contains(ctx, ii);
      case MAPSIZE:  return getMap(ctx).mapSize();
      case MAPREM:   return remove(ctx, ii);
      case MAPCOLL:  return getMap(ctx).collation();
      default:       return super.item(ctx, ii);
    }
  }

  /**
   * Removes a key from a map.
   * @param ctx query context
   * @param ii input info
   * @return new map with key removed from it
   * @throws QueryException query exception
   */
  private Map remove(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return getMap(ctx).delete(expr[1].item(ctx, ii), ii);
  }

  /**
   * Creates a new map containing the given key-value pair.
   * @param ctx query context
   * @param ii input info
   * @return the singleton map
   * @throws QueryException query exception
   */
  private Map entry(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return Map.entry(expr[0].item(ctx, ii), expr[1].value(ctx), ii);
  }

  /**
   * Creates a new map from a list of old ones.
   * @param ctx query context
   * @param ii input info
   * @return new map
   * @throws QueryException query exception
   */
  private Map newMap(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    if(expr.length == 0) return Map.EMPTY;
    if(expr.length == 2) checkColl(expr[1], ctx);

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
    return getMap(ctx).get(expr[1].item(ctx, input), input);
  }

  /**
   * Checks if the given key is contained in the given map.
   * @param ctx query context
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  private Bln contains(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    return getMap(ctx).contains(expr[1].item(ctx, ii), ii);
  }

  /**
   * Assures that the given (non-{@code null}) item is a map.
   * @param it item to check
   * @return the map
   * @throws QueryException if the item is not a map
   */
  private Map checkMap(final Item it) throws QueryException {
    if(it instanceof Map) return (Map) it;
    throw Err.type(this, MapType.ANY_MAP, it);
  }

  /**
   * Gets the map at the given argument position.
   * @param ctx query context
   * @return map
   * @throws QueryException query exception
   */
  private Map getMap(final QueryContext ctx) throws QueryException {
    return checkMap(checkItem(expr[0], ctx));
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 || super.uses(u);
  }
}
