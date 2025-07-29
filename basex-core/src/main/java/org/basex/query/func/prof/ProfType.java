package org.basex.query.func.prof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ProfType extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Expr expr = arg(0);
    final String label = toStringOrNull(arg(1), qc);

    final Value value = expr.value(qc);
    qc.trace(label, () -> {
      final StringBuilder sb = new StringBuilder().append(info(expr));
      if(expr != value) sb.append(" -> ").append(info(value));
      return sb.toString();
    });
    return value;
  }

  /**
   * Returns an info string for the specified expression.
   * @param expr expression
   * @return info string
   */
  private static String info(final Expr expr) {
    return new TypeInfo(expr.getClass(), expr.seqType().toString(),
        expr instanceof final XQStruct struct ? struct.structSize() : expr.size()).toString();
  }

  /**
   * Type information.
   * @param clazz class
   * @param type type
   * @param size size of expression or structure
   */
  public record TypeInfo(Class<?> clazz, String type, long size) {
    @Override
    public String toString() {
      String info = Util.className(clazz) + " (" + type;
      if(XQStruct.class.isAssignableFrom(clazz)) {
        info += ", " + size + ' ' + (size == 1 ? "entry" : "entries");
      } else {
        if(size > 1) info += ", " + size + " items";
      }
      return info + ')';
    }
  }
}
