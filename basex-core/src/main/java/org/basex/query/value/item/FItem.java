package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Abstract super class for function items.
 * This class is inherited by {@link Map}, {@link Array}, and {@link FuncItem}.
 *
 * @author BaseX Team 2005-18, BSD License
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
  public final Value invokeValue(final QueryContext qc, final InputInfo info, final Value... args)
      throws QueryException {
    return FuncCall.value(this, args, qc, info);
  }

  @Override
  public final Item invokeItem(final QueryContext qc, final InputInfo info, final Value... args)
      throws QueryException {
    return FuncCall.item(this, args, qc, info);
  }

  /**
   * Coerces this function item to the given function type.
   * @param ft function type
   * @param qc query context
   * @param info input info
   * @param opt if the result should be optimized
   * @return coerced item
   * @throws QueryException query exception
   */
  public abstract FItem coerceTo(FuncType ft, QueryContext qc, InputInfo info, boolean opt)
      throws QueryException;

  @Override
  public final byte[] string(final InputInfo info) throws QueryException {
    throw FIATOM_X.get(info, type);
  }

  @Override
  public final boolean eq(final Item item, final Collation coll, final StaticContext sc,
      final InputInfo info) throws QueryException {
    throw FIATOM_X.get(info, type);
  }

  @Override
  public boolean sameKey(final Item item, final InputInfo info) {
    return false;
  }

  @Override
  public Value atomValue(final QueryContext qc, final InputInfo info) throws QueryException {
    throw FIATOM_X.get(info, type);
  }

  @Override
  public Item atomItem(final QueryContext qc, final InputInfo info) throws QueryException {
    throw FIATOM_X.get(info, type);
  }

  /**
   * Performs a deep comparison of two items.
   * @param item item to be compared
   * @param info input info
   * @param coll collation (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public boolean deep(final Item item, final InputInfo info, final Collation coll)
      throws QueryException {
    throw FICMP_X.get(info, type);
  }

  @Override
  public abstract void plan(FElem root);
}
