package org.basex.query.func.convert;

import static org.basex.query.QueryError.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.java.*;
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
    return toValue(exprs[0].value(qc), qc);
  }

  /**
   * Converts the specified value to an XQuery value.
   * @param value value to convert
   * @param qc query context
   * @return XQuery value
   * @throws QueryException query exception
   */
  private Value toValue(final Value value, final QueryContext qc) throws QueryException {
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item item : value) {
      if(item instanceof Jav) {
        final Object object = item.toJava();
        if(object instanceof Iterable) {
          for(final Object obj : (Iterable<?>) object) vb.add(toValue(obj, qc));
        } else if(object instanceof Iterator) {
          final Iterator<?> ir = (Iterator<?>) object;
          while(ir.hasNext()) vb.add(toValue(ir.next(), qc));
        } else if(object instanceof Map) {
          XQMap map = XQMap.EMPTY;
          for(final Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
            final Item key = toValue(entry.getKey(), qc).item(qc, info);
            final Value val = toValue(entry.getValue(), qc);
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

  /**
   * Converts the specified Java object to an XQuery value.
   * @param object object to convert
   * @param qc query context
   * @return converted value
   * @throws QueryException query exception
   */
  private Value toValue(final Object object, final QueryContext qc) throws QueryException {
    return toValue(JavaCall.toValue(object, qc, sc, info), qc);
  }
}

