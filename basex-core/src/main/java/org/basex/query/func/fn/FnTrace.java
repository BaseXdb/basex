package org.basex.query.func.fn;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnTrace extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc);
    final String label = toStringOrNull(arg(1), qc);

    if(input.isEmpty() || input instanceof RangeSeq || input instanceof SingletonSeq) {
      qc.trace(label, () -> input.toString());
    } else {
      final Iter iter = input.iter();
      for(Item item; (item = qc.next(iter)) != null;) {
        final Item it = item;
        qc.trace(label, () -> {
          try {
            return it.serialize(SerializerMode.DEBUG.get()).toString();
          } catch(final QueryIOException ex) {
            return ex.getMessage(); // unexpected
          }
        });
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
}
