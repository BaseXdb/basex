package org.basex.query.func.stream;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Streaming functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNStream extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _STREAM_IS_STREAMABLE: return isStreamable(qc);
      default:                    return super.item(qc, ii);
    }
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case _STREAM_MATERIALIZE: return materialize(qc).iter();
      default:                  return super.iter(qc);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case _STREAM_MATERIALIZE: return materialize(qc);
      default:                  return super.value(qc);
    }
  }

  /**
   * Performs the materialize function.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Value materialize(final QueryContext qc) throws QueryException {
    return qc.value(exprs[0]).materialize(info);
  }

  /**
   * Performs the is-streamable function.
   * @param qc query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Item isStreamable(final QueryContext qc) throws QueryException {
    final Item it = toItem(exprs[0], qc);
    return Bln.get(it instanceof StrStream || it instanceof B64Stream);
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    if(func == Function._STREAM_MATERIALIZE) seqType = exprs[0].seqType();
    return this;
  }
}
