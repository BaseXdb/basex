package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class FnReplace extends RegEx {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toEmptyToken(exprs[0], qc);
    final Pattern pat = pattern(exprs[1], exprs.length == 4 ? exprs[3] : null, qc, true);
    final byte[] rep = toToken(exprs[2], qc);
    String replace = string(rep);
    if((pat.flags() & Pattern.LITERAL) != 0) {
      // literal parsing: add backslashes
      replace = replace.replace("\\", "\\\\").replace("$", "\\$");
    } else {
      // standard parsing: raise errors for some special cases
      final int rl = rep.length;
      for(int r = 0; r < rl; ++r) {
        final int n = r + 1 == rl ? 0 : rep[r + 1];
        if(rep[r] == '\\') {
          if(n != '\\' && n != '$') throw FUNREPBS_X.get(info, rep);
          ++r;
        } else if(rep[r] == '$' && (r == 0 || rep[r - 1] != '\\') && !digit(n)) {
          throw FUNREPDOL_X.get(info, rep);
        }
      }
    }

    try {
      return Str.get(pat.matcher(string(value)).replaceAll(replace));
    } catch(final Exception ex) {
      if(ex.getMessage().contains("No group")) throw REGROUP.get(info);
      throw REGPAT_X.get(info, ex);
    }
  }
}
