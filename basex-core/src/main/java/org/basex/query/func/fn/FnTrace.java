package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class FnTrace extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter ir = exprs[0].iter(qc);
      final byte[] label = exprs.length > 1 ? toToken(exprs[1], qc) : null;
      boolean empty = true;
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it != null) {
          trace(it, label, info, qc);
          empty = false;
        } else if(empty) {
          trace(null, label, info, qc);
        }
        return it;
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    seqType = exprs[0].seqType();
    return this;
  }

  /**
   * Dumps the specified item.
   * @param it item to be dumped (may be {@code null})
   * @param label label
   * @param info input info
   * @param qc query context
   * @throws QueryException query exception
   */
  public static void trace(final Item it, final byte[] label, final InputInfo info,
      final QueryContext qc) throws QueryException {
    try {
      trace(it == null ? token(SeqType.EMP.toString()) :
        it.serialize(SerializerMode.DEBUG.get()).finish(), label, qc);
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    }
  }

  /**
   * Dumps the specified info to standard error or the info view of the GUI.
   * @param value traced value
   * @param label additional label to display (may be {@code null})
   * @param qc query context
   */
  public static void trace(final byte[] value, final byte[] label, final QueryContext qc) {
    final TokenBuilder tb = new TokenBuilder();
    if(label != null) tb.add(label);
    final String info = tb.add(value).toString();

    // if GUI is used or client is calling, cache trace info
    final InfoListener il = qc.job().listener;
    if(il != null || qc.context.listener != null) {
      qc.evalInfo(info);
      if(il != null) il.info(info);
    } else {
      Util.errln(info);
    }
  }
}
