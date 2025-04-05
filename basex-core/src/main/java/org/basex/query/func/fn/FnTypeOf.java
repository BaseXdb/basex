package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnTypeOf extends ContextFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Value value = arg(0).value(qc);

    final TokenSet types = new TokenSet();
    for(final Value item : value) {
      String type = item.type.toString();
      if(item.type instanceof FType) {
        type = type.replaceAll("\\(.*", "(*)").replace("record", "map");
      }
      types.add(type);
    }
    final int ts = types.size();

    final TokenBuilder tb = new TokenBuilder();
    if(ts == 0) {
      tb.add(value.seqType().toString());
    } else {
      if(ts > 1) tb.add('(');
      for(final byte[] type : types) {
        if(tb.size() > 1) tb.add('|');
        tb.add(type);
      }
      if(ts > 1) tb.add(')');
      if(value.size() > 1) tb.add('+');
    }
    return Str.get(tb.finish());
  }
}
