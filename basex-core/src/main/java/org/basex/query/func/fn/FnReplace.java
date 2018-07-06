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
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class FnReplace extends RegEx {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value1 = toEmptyToken(exprs[0], qc);
    final Pattern pattern = pattern(exprs[1], exprs.length == 4 ? exprs[3] : null, qc, true);
    final byte[] value2 = toToken(exprs[2], qc);
    String replace = string(value2);
    if((pattern.flags() & Pattern.LITERAL) == 0) {
      // standard parsing: raise errors for some special cases
      final int rl = value2.length;
      for (int r = 0; r < rl; ++r) {
        final int n = r + 1 == rl ? 0 : value2[r + 1];
        if(value2[r] == '\\') {
          if(n != '\\' && n != '$') throw FUNREPBS_X.get(info, value2);
          ++r;
        } else if(value2[r] == '$' && (r == 0 || value2[r - 1] != '\\') && !digit(n)) {
          throw FUNREPDOL_X.get(info, value2);
        }
      }
    } else {
      // literal parsing: add backslashes
      replace = replace.replace("\\", "\\\\").replace("$", "\\$");
    }

    try {
      return Str.get(pattern.matcher(string(value1)).replaceAll(replace));
    } catch(final Exception ex) {
      if(ex.getMessage().contains("No group")) throw REGROUP.get(info);
      throw REGPAT_X.get(info, ex);
    }
  }
}
