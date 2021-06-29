package org.basex.query.func.convert;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.java.JavaCall.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ConvertFromJava extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return fromJava(exprs[0].value(qc), qc);
  }

  /**
   * Converts the specified value to an XQuery value.
   * @param value value to convert
   * @param qc query context
   * @return converted value
   * @throws QueryException query exception
   */
  private Value fromJava(final Value value, final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item item : value) {
      if(item instanceof Jav) {
        final Object object = item.toJava();
        if(object instanceof Iterable) {
          for(final Object obj : (Iterable<?>) object) {
            vb.add(fromJava(toValue(obj, qc, sc, info), qc));
          }
        } else if(object instanceof Iterator) {
          final Iterator<?> ir = (Iterator<?>) object;
          while(ir.hasNext()) {
            vb.add(fromJava(toValue(ir.next(), qc, sc, info), qc));
          }
        } else if(object instanceof Map) {
          XQMap map = XQMap.EMPTY;
          for(final Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
            final Item key = fromJava(toValue(entry.getKey(), qc, sc, info), qc).item(qc, info);
            final Value val = fromJava(toValue(entry.getValue(), qc, sc, info), qc);
            map = map.put(key, val, info);
          }
          vb.add(map);
        } else {
          throw CONVERT_JAVA_X.get(info, item);
        }
      } else {
        vb.add(item);
      }
    }
    return vb.value();
  }
}
