package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnReplace extends RegEx {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(exprs[0], qc);
    final byte[] pattern = toToken(exprs[1], qc), replacement = toZeroToken(exprs[2], qc);
    final boolean noFlags = exprs.length < 4, noAction = exprs.length < 5;
    if(noFlags) {
      // shortcut for simple character replacements
      final int sp = patternChar(pattern), rp = patternChar(replacement);
      if(sp != -1 && rp != -1) return Str.get(replace(value, sp, rp));
    }

    final RegExpr regExpr = regExpr(pattern, noFlags ? null : exprs[3], qc, true);
    final FItem action = noAction ? null : toFunction(exprs[4], 2, qc);

    String replace = string(replacement);
    if(noAction && (regExpr.pattern.flags() & Pattern.LITERAL) == 0) {
      // standard parsing: raise errors for some special cases
      final int rl = replacement.length;
      for(int r = 0; r < rl; ++r) {
        final int n = r + 1 == rl ? 0 : replacement[r + 1];
        if(replacement[r] == '\\') {
          if(n != '\\' && n != '$') throw FUNREPBS_X.get(info, replacement);
          ++r;
        } else if(replacement[r] == '$' && (r == 0 || replacement[r - 1] != '\\') && !digit(n)) {
          throw FUNREPDOL_X.get(info, replacement);
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
      if(noAction) {
        replaced = m.replaceAll(replace);
      } else {
        final StringBuilder sb = new StringBuilder();
        while(m.find()) {
          final int gc = m.groupCount();
          final TokenList tl = new TokenList(gc);
          for(int g = 0; g < gc; g++) tl.add(m.group(g + 1));
          final Item rplc = action.invoke(qc, info, Str.get(m.group()), StrSeq.get(tl)).
              atomItem(qc, ii);
          m.appendReplacement(sb, rplc == Empty.VALUE ? "" : string(toToken(rplc)));
        }
        replaced = m.appendTail(sb).toString();
      }
      return Str.get(replaced);
    } catch(final QueryException ex) {
      throw ex;
    } catch(final Exception ex) {
      if(ex.getMessage().contains("No group")) {
        Util.debug(ex);
        throw REGROUP_X.get(info, pattern);
      }
      throw REGPAT_X.get(info, ex);
    }
  }
}
