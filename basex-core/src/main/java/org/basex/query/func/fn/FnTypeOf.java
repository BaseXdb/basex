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
    return Str.get(toString(value));
  }

  /**
   * Returns a string representation of the type.
   * @param value value
   * @return string representation
   */
  private String toString(final Value value) {
    if(value.isEmpty()) return value.seqType().toString();

    final TokenSet types = new TokenSet();
    for(final Value item : value) {
      String type = item.type.toString();
      if(item.type instanceof FType) {
        type = type.replaceAll("\\(.*", "(*)").replace("record", "map");
      }
      types.add(type);
    }

    final TokenBuilder tb = new TokenBuilder();
    final boolean seq = types.size() > 1;
    if(seq) tb.add('(');
    for(final byte[] type : types) {
      if(tb.size() > 1) tb.add('|');
      tb.add(type);
    }
    if(seq) tb.add(')');
    if(value.size() > 1) tb.add('+');
    return tb.toString();
  }
}
