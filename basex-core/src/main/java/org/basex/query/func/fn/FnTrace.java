package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnTrace extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter ir = exprs[0].iter(qc);
      final byte[] label = toToken(exprs[1], qc);
      boolean empty = true;
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it != null) {
          dump(it, label, info, qc);
          empty = false;
        } else if(empty) {
          dump(null, label, info, qc);
        }
        return it;
      }
    };
  }

  /**
   * Dumps the specified item.
   * @param it item to be dumped (may be {@code null})
   * @param label label
   * @param info input info
   * @param qc query context
   * @throws QueryException query exception
   */
  public static void dump(final Item it, final byte[] label, final InputInfo info,
      final QueryContext qc) throws QueryException {
    try {
      final byte[] value;
      if(it == null) {
        value = token(SeqType.EMP.toString());
      } else if(it instanceof FuncItem) {
        value = token((((FuncItem) it).expr).toString());
      } else if(it instanceof FItem || it.type == NodeType.ATT || it.type == NodeType.NSP) {
        value = token(it.toString());
      } else {
        value = it.serialize(SerializerOptions.get(false)).finish();
      }
      dump(value, label, qc);
    } catch(final QueryIOException ex) {
      throw ex.getCause(info);
    }
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    seqType = exprs[0].seqType();
    return this;
  }

  /**
   * Dumps the specified info to standard error or the info view of the GUI.
   * @param value traced value
   * @param label additional label to display (can be {@code null})
   * @param qc query context
   */
  public static void dump(final byte[] value, final byte[] label, final QueryContext qc) {
    final TokenBuilder tb = new TokenBuilder();
    if(label != null) tb.add(label);
    tb.add(value);
    final String info = tb.toString();

    // if GUI is used or client is calling, cache trace info
    if(qc.listen != null || qc.context.listener != null) {
      qc.evalInfo(info);
      if(qc.listen != null) qc.listen.info(info);
    } else {
      Util.errln(info);
    }
  }
}
