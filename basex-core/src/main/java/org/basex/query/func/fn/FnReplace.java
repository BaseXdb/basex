package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.regex.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnReplace extends RegExFn {
  /** Type of the groups argument of a replacement function. */
  private static final SeqType GROUPS_TYPE = MapType.get(
      ChoiceItemType.get(BasicType.INTEGER, BasicType.STRING), Types.UNTYPED_ATOMIC_O).seqType();
  /** Type of the replacement argument (deprecated, including the old function signature). */
  public static final SeqType REPLACEMENT_TYPE = ChoiceItemType.get(BasicType.STRING,
      FuncType.get(Types.ITEM_ZO, Types.UNTYPED_ATOMIC_O, GROUPS_TYPE),
      FuncType.get(Types.ITEM_ZO, Types.UNTYPED_ATOMIC_O, Types.UNTYPED_ATOMIC_ZM)).
      seqType(Occ.ZERO_OR_ONE);

  @Override
  public Str value(final QueryContext qc) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final byte[] pattern = toToken(arg(1), qc);
    final Item replacement = arg(2).unwrappedItem(qc, info);
    final byte[] flags = toZeroToken(arg(3), qc);

    final boolean func = replacement instanceof FItem;
    final FItem action = func ? toFunction(replacement, 2, qc) : null;
    final byte[] replace = func ? null : replacement.isEmpty() ? EMPTY : toToken(replacement);

    // shortcut for literal replacements
    if(!func) {
      final byte[] search = literal(pattern, flags);
      // with "q" flag, "$" and "\" in the replacement are literal; otherwise they must be absent
      final boolean q = flags.length == 1 && flags[0] == 'q';
      if(search != null && (q || !contains(replace, '\\') && !contains(replace, '$'))) {
        return Str.get(replace(value, search, replace));
      }
    }
    final RegExpr regExpr = regExpr(pattern, flags, qc);
    final String input = string(value);
    final Matcher matcher = regExpr.pattern.matcher(input);

    if(func) {
      // deprecated: groups are passed on as sequence
      final FuncType ft = action.funcType();
      final SeqType[] at = ft != null ? ft.argTypes : null;
      final boolean seq = at != null && at.length > 1 && !GROUPS_TYPE.instanceOf(at[1]);
      final String[] names = seq ? null : regExpr.getGroupNames();
      final HofArgs args = new HofArgs(2);
      final StringBuilder sb = new StringBuilder();
      int pos = 0;
      while(matcher.find()) {
        args.set(0, Atm.get(matcher.group())).set(1, groups(matcher, names, qc));
        final Item item = invoke(action, args, qc).atomItem(qc, info);
        // replacements are appended verbatim, so no escaping is required
        sb.append(input, pos, matcher.start());
        if(!item.isEmpty()) sb.append(string(item.string(info)));
        pos = matcher.end();
      }
      return Str.get(sb.append(input, pos, input.length()).toString());
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
        } else if(replace[r] == '$' && (r == 0 || replace[r - 1] != '\\') &&
            !digit(n) && n != '<') {
          throw REGDOLLAR_X.get(info, replace);
        }
      }

      // convert named group references, remove unused group references
      if(contains(replace, '$')) {
        final List<String> names = Arrays.asList(regExpr.getGroupNames());
        final StringBuilder sb = new StringBuilder();
        final int sl = string.length();
        for(int s = 0; s < sl;) {
          int i = string.indexOf('$', s);
          if(i == -1) {
            sb.append(string, s, sl);
            s = sl;
          } else if(!isEscaped(string, i)) {
            sb.append(string, s, i);
            s = ++i;
            if(string.charAt(s) == '<') {
              // named group reference: $<name> → ${name}
              i = string.indexOf('>', s);
              if(i == -1) throw REGDOLLAR_X.get(info, replace);
              final String name = string.substring(s + 1, i);
              if(!names.contains(name)) throw REGGROUP_X.get(info, name);
              sb.append("${").append(name).append('}');
              s = i + 1;
            } else {
              if(i < sl && Character.isDigit(string.charAt(i))) i++;
              final int n = Integer.parseInt(string.substring(s, i));
              if(n <= matcher.groupCount()) sb.append('$').append(n);
              s = i;
            }
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

  /**
   * Returns the capturing groups of the current match.
   * @param matcher matcher
   * @param names group names, or {@code null} if groups are to be returned as sequence
   * @param qc query context
   * @return groups
   * @throws QueryException query exception
   */
  private static Value groups(final Matcher matcher, final String[] names, final QueryContext qc)
      throws QueryException {
    final int gc = matcher.groupCount();
    if(names == null) {
      final ValueBuilder vb = new ValueBuilder(qc);
      for(int g = 1; g <= gc; g++) {
        final String group = matcher.group(g);
        vb.add(group == null ? Atm.EMPTY : Atm.get(group));
      }
      return vb.value();
    }
    final MapBuilder groups = new MapBuilder();
    for(int g = 1; g <= gc; g++) {
      final String group = matcher.group(g);
      if(group != null) {
        final String name = g <= names.length ? names[g - 1] : null;
        groups.put(name != null ? Str.get(name) : Itr.get(g), Atm.get(group));
      }
    }
    return groups.map();
  }

  /**
   * Checks if the character at the specified position is escaped, i.e., if it is preceded by
   * an odd number of backslashes.
   * @param string string
   * @param pos position of the character
   * @return result of check
   */
  private static boolean isEscaped(final String string, final int pos) {
    int bs = 0;
    for(int p = pos - 1; p >= 0 && string.charAt(p) == '\\'; p--) bs++;
    return (bs & 1) != 0;
  }

  @Override
  public int hofOffsets() {
    return functionOption(2) ? hofOffset(2) : 0;
  }
}
