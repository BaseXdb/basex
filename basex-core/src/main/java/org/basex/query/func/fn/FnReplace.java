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
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnReplace extends RegEx {
  /** Slash pattern. */
  private static final Pattern SLASH = Pattern.compile("\\$");
  /** Slash pattern. */
  private static final Pattern BSLASH = Pattern.compile("\\\\");

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toEmptyToken(exprs[0], qc);
    final byte[] rep = toToken(exprs[2], qc);
    final int rl = rep.length;
    for(int i = 0; i < rl; ++i) {
      if(rep[i] == '\\') {
        if(i + 1 == rl || rep[i + 1] != '\\' && rep[i + 1] != '$')
          throw FUNREPBS_X.get(info, rep);
        ++i;
      }
      if(rep[i] == '$' && (i == 0 || rep[i - 1] != '\\') &&
        (i + 1 == rl || !digit(rep[i + 1]))) throw FUNREPDOL_X.get(info, rep);
    }

    final Pattern p = pattern(exprs[1], exprs.length == 4 ? exprs[3] : null, qc, true);
    String r = string(rep);
    if((p.flags() & Pattern.LITERAL) != 0) {
      r = SLASH.matcher(BSLASH.matcher(r).replaceAll("\\\\\\\\")).replaceAll("\\\\\\$");
    }

    try {
      return Str.get(p.matcher(string(value)).replaceAll(r));
    } catch(final Exception ex) {
      if(ex.getMessage().contains("No group")) throw REGROUP.get(info);
      throw REGPAT_X.get(info, ex);
    }
  }
}
