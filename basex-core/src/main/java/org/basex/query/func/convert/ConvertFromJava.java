package org.basex.query.func.convert;

import org.basex.core.MainOptions.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.java.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ConvertFromJava extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Item item : value) {
      if(item instanceof XQJava) {
        vb.add(JavaCall.toValue(item.toJava(), qc, info, WrapOptions.NONE));
      } else {
        vb.add(item);
      }
    }
    return vb.value();
  }
}
