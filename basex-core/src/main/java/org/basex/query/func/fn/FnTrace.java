package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnTrace extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc);
    final byte[] label = toTokenOrNull(arg(1), qc);

    if(input.isEmpty() || input instanceof RangeSeq || input instanceof SingletonSeq) {
      trace(token(input.toString()), label, qc);
    } else {
      final Iter iter = input.iter();
      try {
        for(Item item; (item = qc.next(iter)) != null;) {
          trace(item.serialize(SerializerMode.DEBUG.get()).finish(), label, qc);
        }
      } catch(final QueryIOException ex) {
        throw ex.getCause(info);
      }
    }
    return input;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return adoptType(arg(0));
  }

  @Override
  public boolean ddo() {
    return arg(0).ddo();
  }

  /**
   * Dumps the specified info to standard error or the info view of the GUI.
   * @param value traced value
   * @param label additional label to display (can be {@code null})
   * @param qc query context
   */
  public static void trace(final byte[] value, final byte[] label, final QueryContext qc) {
    final TokenBuilder tb = new TokenBuilder();
    if(label != null) tb.add(label);
    final String info = tb.add(value).toString();
    if(qc.jc().tracer().print(info)) qc.evalInfo(info);
  }
}
