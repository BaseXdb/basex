package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnReplace extends RegEx {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final byte[] pattern = toToken(arg(1), qc);
    final byte[] replacement = toZeroToken(arg(2), qc);
    final Expr flags = defined(3) ? arg(3) : null;
    final FItem action = defined(4) ? toFunction(arg(4), 2, qc) : null;

    // shortcut for simple character replacements
    if(flags == null) {
      final int sp = patternChar(pattern), rp = patternChar(replacement);
      if(sp != -1 && rp != -1) return Str.get(replace(value, sp, rp));
    }

    final RegExpr regExpr = regExpr(pattern, flags, qc, true);
    String replace = string(replacement);
    if(action == null && (regExpr.pattern.flags() & Pattern.LITERAL) == 0) {
      // standard parsing: raise errors for some special cases
      final int rl = replacement.length;
      for(int r = 0; r < rl; ++r) {
        final int n = r + 1 == rl ? 0 : replacement[r + 1];
        if(replacement[r] == '\\') {
          if(n != '\\' && n != '$') throw REGBACKSLASH_X.get(info, replacement);
          ++r;
        } else if(replacement[r] == '$' && (r == 0 || replacement[r - 1] != '\\') && !digit(n)) {
          throw REGDOLLAR_X.get(info, replacement);
        }
      }

      // remove unused group references
      if(Strings.contains(replace, '$')) {
        final StringBuilder sb = new StringBuilder();
        final int sl = replace.length();
        for(int s = 0; s < sl;) {
          int i = replace.indexOf('$', s);
          if(i == -1) {
            sb.append(replace, s, sl);
            s = sl;
          } else if(i == 0 || replace.charAt(i - 1) != '\\') {
            sb.append(replace, s, i);
            s = ++i;
            if(i < sl && Character.isDigit(replace.charAt(i))) i++;
            final int n = Integer.parseInt(replace.substring(s, i));
            if(n <= regExpr.groups) sb.append('$').append(n);
            s = i;
          } else {
            sb.append(replace, s, i + 1);
            s = i + 1;
          }
        }
        replace = sb.toString();
      }
    } else {
      // literal parsing: add backslashes
      replace = replace.replace("\\", "\\\\").replace("$", "\\$");
    }

    try {
      final Matcher m = regExpr.pattern.matcher(string(value));
      final String replaced;
      if(action == null) {
        replaced = m.replaceAll(replace);
      } else {
        final StringBuilder sb = new StringBuilder();
        while(m.find()) {
          final int gc = m.groupCount();
          final ValueBuilder groups = new ValueBuilder(qc);
          for(int g = 0; g < gc; g++) groups.add(Atm.get(m.group(g + 1)));
          final Item rplc = action.invoke(qc, info, Atm.get(m.group()), groups.value()).
              atomItem(qc, ii);
          m.appendReplacement(sb, rplc.isEmpty() ? "" : string(rplc.string(info)));
        }
        replaced = m.appendTail(sb).toString();
      }
      return Str.get(replaced);
    } catch(final QueryException ex) {
      throw ex;
    } catch(final Exception ex) {
      if(ex.getMessage().contains("No group")) {
        Util.debug(ex);
        throw REGEMPTY_X.get(info, pattern);
      }
      throw REGINVALID_X.get(info, ex);
    }
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.HOF.in(flags) && defined(4) || super.has(flags);
  }
}
