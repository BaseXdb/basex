package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnTokenize extends RegEx {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] val = toEmptyToken(exprs[0], qc);
    if(exprs.length < 2) return StrSeq.get(split(normalize(val), ' '));

    final Pattern p = pattern(exprs[1], exprs.length == 3 ? exprs[2] : null, qc, true);
    if(p.matcher("").matches()) throw REGROUP.get(info);

    final TokenList tl = new TokenList();
    final String str = string(val);
    if(!str.isEmpty()) {
      final Matcher m = p.matcher(str);
      int s = 0;
      while(m.find()) {
        tl.add(str.substring(s, m.start()));
        s = m.end();
      }
      tl.add(str.substring(s, str.length()));
    }
    return StrSeq.get(tl);
  }
}
