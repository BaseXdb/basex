package org.basex.query.func.fn;

import java.util.stream.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnAvailableEnvironmentVariables extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) {
    final TokenList tl = new TokenList();
    for(final Object o : System.getenv().keySet().stream().sorted().collect(Collectors.toList())) {
      tl.add(o.toString());
    }
    return StrSeq.get(tl);
  }
}
