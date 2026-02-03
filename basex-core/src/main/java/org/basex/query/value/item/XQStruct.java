package org.basex.query.value.item;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function item with a known data structure (map, array).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class XQStruct extends FItem {
  /**
   * Constructor.
   * @param type function type
   */
  protected XQStruct(final Type type) {
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
  public final String funcIdentity() {
    final long size = structSize();
    return description() + size + (size > 0 ? "-" + hashCode() : "");
  }

  @Override
  public final Expr inline(final Expr[] exprs, final CompileContext cc) {
    return null;
  }

  @Override
  public final boolean vacuousBody() {
    return false;
  }

  @Override
  public final boolean updating() {
    return false;
  }

  @Override
  public abstract long structSize();

  /**
   * Returns all items (sequence-concatenated values) of this structure.
   * @param qc query context
   * @return items
   * @throws QueryException query exception
   */
  public Value items(final QueryContext qc) throws QueryException {
    return itemsIter().value(qc, null);
  }

  /**
   * Returns an iterator for all items (sequence-concatenated values) of this structure.
   * @return iterator
   */
  public abstract Iter itemsIter();

  /**
   * Gets the key at the given position.
   * @param index index position (starting with 0, must be valid)
   * @return key
   */
  public abstract Item keyAt(long index);

  /**
   * Returns the value at the given position.
   * @param index index position (starting with 0, must be valid)
   * @return value
   */
  public abstract Value valueAt(long index);

  /**
   * Returns the key for accessing a function value.
   * @param key key argument
   * @param qc query context
   * @param ii input info
   * @return key
   * @throws QueryException query exception
   */
  protected static Item key(final Value key, final QueryContext qc, final InputInfo ii)
      throws QueryException {
    final Item item = key.atomItem(qc, ii);
    if(item == Empty.VALUE) throw typeError(item, BasicType.ITEM, ii);
    return item;
  }

  @Override
  public final QNm paramName(final int pos) {
    return new QNm("key", "");
  }

  @Override
  public void refineType(final Expr expr) {
    final Type tp = type.intersect(expr.seqType().type);
    if(tp != null) type = tp;
  }

  @Override
  public final int stackFrameSize() {
    return 0;
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
