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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnAnalyzeString extends RegEx {
  /** QName. */
  private static final QNm Q_ANALYZE = new QNm("analyze-string-result", FN_URI);
  /** QName. */
  private static final QNm Q_MATCH = new QNm("match", FN_URI);
  /** QName. */
  private static final QNm Q_NONMATCH = new QNm("non-match", FN_URI);
  /** QName. */
  private static final QNm Q_MGROUP = new QNm("group", FN_URI);
  /** QName. */
  private static final QNm Q_NR = new QNm("nr");

  @Override
  public FNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String value = string(toZeroToken(arg(0), qc));
    final byte[] pattern = toToken(arg(1), qc);
    final byte[] flags = toZeroToken(arg(2), qc);

    final RegExpr regExpr = regExpr(pattern, flags, true);
    final Matcher matcher = regExpr.pattern.matcher(value);
    final FBuilder root = FElem.build(Q_ANALYZE).declareNS();
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
      if(st >= 0) { // group matched
        if(pos[1] < st) node.add(string.substring(pos[1], st));
        pos = match(matcher, string, node, pos[0], regExpr);
      } else pos[0]++; // skip it
    }
    if(pos[1] < end) {
      node.add(string.substring(pos[1], end));
      pos[1] = end;
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
    parent.add(FElem.build(Q_NONMATCH).add(text));
  }
}
