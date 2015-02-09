package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
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
 * This class is inherited by either {@link Map}, {@link Array}, or {@link FuncItem}.
 *
 * @author BaseX Team 2005-15, BSD License
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
  public final Value invokeValue(final QueryContext qc, final InputInfo ii, final Value... args)
      throws QueryException {
    return FuncCall.value(this, args, qc, ii);
  }

  @Override
  public final Item invokeItem(final QueryContext qc, final InputInfo ii, final Value... args)
      throws QueryException {
    return FuncCall.item(this, args, qc, ii);
  }

  /**
   * Coerces this function item to the given function type.
   * @param ft function type
   * @param qc query context
   * @param ii input info
   * @param opt if the result should be optimized
   * @return coerced item
   * @throws QueryException query exception
   */
  public abstract FItem coerceTo(final FuncType ft, final QueryContext qc, final InputInfo ii,
      boolean opt) throws QueryException;

  @Override
  public final byte[] string(final InputInfo ii) throws QueryException {
    throw FIATOM_X.get(ii, type);
  }

  @Override
  public final boolean eq(final Item it, final Collation coll, final StaticContext sc,
      final InputInfo ii) throws QueryException {
    throw FIEQ_X.get(ii, type);
  }

  @Override
  public boolean has(final Flag flag) {
    return flag == Flag.UPD && annotations().contains(Annotation.UPDATING) || super.has(flag);
  }

  @Override
  public Item atomItem(final InputInfo ii) throws QueryException {
    throw FIATOM_X.get(ii, type);
  }

  /**
   * Performs a deep comparison of two items.
   * @param item item to be compared
   * @param ii input info
   * @param coll collation
   * @return result of check
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public boolean deep(final Item item, final InputInfo ii, final Collation coll)
      throws QueryException {
    throw FICMP_X.get(ii, type);
  }

  @Override
  public abstract void plan(final FElem root);
}
