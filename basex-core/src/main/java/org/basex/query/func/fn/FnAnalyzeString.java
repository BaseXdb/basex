package org.basex.query.func.fn;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnAnalyzeString extends RegExFn {
  /** QName. */
  private static final QNm Q_ANALYZE_STRING_RESULT = new QNm("analyze-string-result", FN_URI);
  /** QName. */
  private static final QNm Q_MATCH = new QNm("match", FN_URI);
  /** QName. */
  private static final QNm Q_NON_MATCH = new QNm("non-match", FN_URI);
  /** QName. */
  private static final QNm Q_MGROUP = new QNm("group", FN_URI);
  /** QName. */
  private static final QNm Q_LGROUP = new QNm("lookahead-group", FN_URI);
  /** QName. */
  private static final QNm Q_NR = new QNm("nr");
  /** QName. */
  private static final QNm Q_VALUE = new QNm("value");
  /** QName. */
  private static final QNm Q_POSITION = new QNm("position");

  @Override
  public FNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String value = string(toZeroToken(arg(0), qc));
    final byte[] pattern = toToken(arg(1), qc);
    final byte[] flags = toZeroToken(arg(2), qc);

    final RegExpr regExpr = regExpr(pattern, flags);
    final Matcher matcher = regExpr.pattern.matcher(value);
    final FBuilder root = FElem.build(Q_ANALYZE_STRING_RESULT).declareNS();
    int start = 0;
    while(matcher.find()) {
      if(start != matcher.start()) nonmatch(value.substring(start, matcher.start()), root);
      match(matcher, value, root, 0, regExpr);
      start = matcher.end();
    }
    if(start != value.length()) nonmatch(value.substring(start), root);
    return root.finish();
  }

  /**
   * Processes a match.
   * @param matcher matcher
   * @param string string
   * @param parent parent
   * @param group group number
   * @param regExpr regExpr
   * @return next group number and position in string
   */
  private static int[] match(final Matcher matcher, final String string, final FBuilder parent,
      final int group, final RegExpr regExpr) {

    final FBuilder node = FElem.build(group == 0 ? Q_MATCH : Q_MGROUP);
    if(group > 0) node.add(Q_NR, group);

    final int start = matcher.start(group), end = matcher.end(group), gc = matcher.groupCount();
    int[] pos = { group + 1, start }; // group and position in string
    while(pos[0] <= gc && matcher.end(pos[0]) <= end
        && (matcher.start(pos[0]) < end || regExpr.getParentGroups()[pos[0] - 1] == group)) {
      final int st = matcher.start(pos[0]);
      if(st >= 0 && !regExpr.getAssertionFlags()[pos[0] - 1]) { // group matched
        if(pos[1] < st) node.add(string.substring(pos[1], st));
        pos = match(matcher, string, node, pos[0], regExpr);
      } else pos[0]++; // skip it
    }
    if(pos[1] < end) {
      node.add(string.substring(pos[1], end));
      pos[1] = end;
    }
    if(group == 0) {
      final boolean[] assertionFlags = regExpr.getAssertionFlags();
      for(int g = 1; g <= assertionFlags.length; ++g) {
        if(assertionFlags[g - 1] && matcher.start(g) >= 0) {
          final FBuilder lg = FElem.build(Q_LGROUP);
          lg.add(Q_NR, g);
          lg.add(Q_VALUE, string.substring(matcher.start(g), matcher.end(g)));
          lg.add(Q_POSITION, matcher.start(g) + 1);
          node.add(lg);
        }
      }
    }
    parent.add(node);
    return pos;
  }

  /**
   * Processes a non-match.
   * @param text text
   * @param parent root node
   */
  private static void nonmatch(final String text, final FBuilder parent) {
    parent.add(FElem.build(Q_NON_MATCH).add(text));
  }
}
