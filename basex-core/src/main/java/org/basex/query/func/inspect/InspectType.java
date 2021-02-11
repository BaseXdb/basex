package org.basex.query.func.inspect;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class InspectType extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Value value = exprs[0].value(qc);

    // combine types of all items to get more specific type
    SeqType st = null;
    for(final Item item : value) {
      final SeqType st2 = item.seqType();
      st = st == null ? st2 : st.union(st2);
    }
    if(st == null) st = SeqType.EMPTY_SEQUENCE_Z;
    st = st.with(value.seqType().occ);

    // compare with original type, which may be more specific (in particular for node types)
    final SeqType et = exprs[0].seqType();
    if(et.instanceOf(st)) st = et;

    return Str.get(st.toString());
  }
}
