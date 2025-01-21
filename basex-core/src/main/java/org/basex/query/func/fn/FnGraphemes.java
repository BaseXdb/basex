package org.basex.query.func.fn;

import java.text.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnGraphemes extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String value = toStringOrNull(arg(0), qc);
    if(value == null) return Empty.VALUE;

    final TokenList list = new TokenList();
    if(Prop.ICU) {
      Icu.split(list, value);
    } else {
      final BreakIterator bi = BreakIterator.getCharacterInstance();
      bi.setText(value);
      for(int s = bi.first(), e = bi.next(); e != BreakIterator.DONE; s = e, e = bi.next()) {
        list.add(value.substring(s, e));
      }
    }
    return StrSeq.get(list.finish());
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optFirst();
  }
}
