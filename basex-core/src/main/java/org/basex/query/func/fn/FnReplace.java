package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnReplace extends RegEx {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final byte[] pattern = toToken(arg(1), qc);
    final Item replacement = arg(2).atomItem(qc, info);
    final byte[] flags = toZeroToken(arg(3), qc);
    final FItem action = toFunctionOrNull(arg(4), 2, qc);
    if(!replacement.isEmpty() && action != null) throw REGACTION_X.get(info, this);

    // shortcut for simple character replacements
    final byte[] replace = replacement.isEmpty() ? EMPTY : toToken(replacement);
    if(flags.length == 0) {
      final int sp = patternChar(pattern), rp = replace != null ? patternChar(replace) : -1;
      if(sp != -1 && rp != -1) return Str.get(replace(value, sp, rp));
    }
    final RegExpr regExpr = regExpr(pattern, flags, true);
    final Matcher matcher = regExpr.pattern.matcher(string(value));

    if(action != null) {
      final StringBuilder sb = new StringBuilder();
      while(matcher.find()) {
        final Atm group = Atm.get(matcher.group());
        final ValueBuilder groups = new ValueBuilder(qc);
        final int gc = matcher.groupCount();
        for(int g = 0; g < gc; g++) groups.add(Atm.get(matcher.group(g + 1)));
        final Item item = action.invoke(qc, info, group, groups.value()).atomItem(qc, info);
        matcher.appendReplacement(sb, item.isEmpty() ? "" :
          string(item.string(info)).replace("\\", "\\\\").replace("$", "\\$"));
      }
      return Str.get(matcher.appendTail(sb).toString());
    }

    String string = string(replace);
    if((regExpr.pattern.flags() & Pattern.LITERAL) != 0) {
      // literal parsing: add backslashes
      string = string.replace("\\", "\\\\").replace("$", "\\$");
    } else {
      // standard parsing: raise errors for some special cases
      final int rl = replace.length;
      for(int r = 0; r < rl; ++r) {
        final int n = r + 1 == rl ? 0 : replace[r + 1];
        if(replace[r] == '\\') {
          if(n != '\\' && n != '$') throw REGBACKSLASH_X.get(info, replace);
          ++r;
        } else if(replace[r] == '$' && (r == 0 || replace[r - 1] != '\\') && !digit(n)) {
          throw REGDOLLAR_X.get(info, replace);
        }
      }

      // remove unused group references
      if(contains(replace, '$')) {
        final StringBuilder sb = new StringBuilder();
        final int sl = string.length();
        for(int s = 0; s < sl;) {
          int i = string.indexOf('$', s);
          if(i == -1) {
            sb.append(string, s, sl);
            s = sl;
          } else if(i == 0 || string.charAt(i - 1) != '\\') {
            sb.append(string, s, i);
            s = ++i;
            if(i < sl && Character.isDigit(string.charAt(i))) i++;
            final int n = Integer.parseInt(string.substring(s, i));
            if(n <= matcher.groupCount()) sb.append('$').append(n);
            s = i;
          } else {
            sb.append(string, s, i + 1);
            s = i + 1;
          }
        }
        string = sb.toString();
      }
    }
    return Str.get(matcher.replaceAll(string));
  }

  @Override
  public int hofIndex() {
    return 4;
  }
}
