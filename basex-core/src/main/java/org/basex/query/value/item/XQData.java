package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function item with a known data structure (map, array).
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public abstract class XQData extends FItem {
  /**
   * Constructor.
   * @param type function type
   */
  protected XQData(final Type type) {
    super(type);
  }

  @Override
  public final AnnList annotations() {
    return AnnList.EMPTY;
  }

  @Override
  public final byte[] string(final InputInfo ii) throws QueryException {
    throw FIATOMIZE_X.get(ii, this);
  }

  @Override
  public final int arity() {
    return 1;
  }

  @Override
  public final QNm funcName() {
    return null;
  }

  @Override
  public final Expr inline(final Expr[] exprs, final CompileContext cc) {
    return null;
  }

  @Override
  public final boolean vacuousBody() {
    return false;
  }

  /**
   * Returns the key for accessing a function value.
   * @param key key argument
   * @param qc query context
   * @param ii input info
   * @return key
   * @throws QueryException query exception
   */
  protected final Item key(final Value key, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    final Item item = key.atomItem(qc, ii);
    if(item.isEmpty()) throw EMPTYFOUND.get(ii);
    return item;
  }

  @Override
  public final int stackFrameSize() {
    return 0;
  }

  @Override
  public final FItem coerceTo(final FuncType ft, final QueryContext qc, final CompileContext cc,
      final InputInfo ii) throws QueryException {
    return coerceTo(ft, qc, cc, ii, cc != null ? cc.sc() : null, false);
  }

  /**
   * Returns a string representation of the item.
   * @param indent indent output
   * @param tb token builder
   * @param level current level
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public abstract void string(boolean indent, TokenBuilder tb, int level, InputInfo info)
      throws QueryException;
}
