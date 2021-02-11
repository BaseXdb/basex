package org.basex.query.func.proc;

import java.util.Map.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ProcPropertyNames extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) {
    final TokenList tl = new TokenList();
    for(final Entry<String, String> entry : Prop.entries()) tl.add(entry.getKey());
    return StrSeq.get(tl.sort());
  }
}
