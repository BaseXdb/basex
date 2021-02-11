package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for function items.
 * This class is inherited by {@link XQMap}, {@link Array}, and {@link FuncItem}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public abstract class FItem extends Item implements XQFunction {
  /** Annotations. */
  final AnnList anns;

  /**
   * Constructor.
   * @param type type
   * @param anns this function item's annotations
   */
  protected FItem(final FuncType type, final AnnList anns) {
    super(type);
    this.anns = anns;
  }

  @Override
  public final AnnList annotations() {
    return anns;
  }

  @Override
  public final byte[] string(final InputInfo ii) throws QueryException {
    throw FIATOM_X.get(ii, type);
  }

  @Override
  public final boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    throw FIATOM_X.get(ii, type);
  }

  @Override
  public final boolean sameKey(final Item item, final InputInfo ii) {
    return false;
  }

  @Override
  public void refineType(final Expr expr) {
    final Type t = type.intersect(expr.seqType().type);
    if(t != null) type = t;
  }

  @Override
  public final FuncType funcType() {
    return (FuncType) type;
  }

  /**
   * Coerces this function item to the given function type.
   * @param ft function type
   * @param qc query context
   * @param ii input info
   * @param optimize optimize resulting item
   * @return coerced item
   * @throws QueryException query exception
   */
  public abstract FItem coerceTo(FuncType ft, QueryContext qc, InputInfo ii, boolean optimize)
      throws QueryException;

  /**
   * Performs a deep comparison of two items.
   * @param item item to be compared
   * @param coll collation (can be {@code null})
   * @param ii input info
   * @return result of check
   * @throws QueryException query exception
   */
  public abstract boolean deep(Item item, Collation coll, InputInfo ii) throws QueryException;
}
