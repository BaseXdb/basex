package org.basex.query.func.fn;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.util.function.*;
import java.util.regex.*;

import org.basex.query.*;
import org.basex.query.util.regex.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnAnalyzeString extends RegExFn {
  /** QName. */
  public static final QNm Q_ANALYZE_STRING_RESULT = new QNm("analyze-string-result", FN_URI);
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
  private static final QNm Q_NAME = new QNm("name");
  /** QName. */
  private static final QNm Q_VALUE = new QNm("value");
  /** QName. */
  private static final QNm Q_POSITION = new QNm("position");

  @Override
  public FNode value(final QueryContext qc) throws QueryException {
    final String value = string(toZeroToken(arg(0), qc));
    final byte[] pattern = toToken(arg(1), qc);
    final byte[] flags = toZeroToken(arg(2), qc);

    final RegExpr regExpr = regExpr(pattern, flags, qc);
    final Matcher matcher = regExpr.pattern.matcher(value);
    final IntUnaryOperator positions = positions(value);
    final FBuilder root = FElem.build(Q_ANALYZE_STRING_RESULT).ns();
    int start = 0;
    while(matcher.find()) {
      if(start != matcher.start()) nonmatch(value.substring(start, matcher.start()), root);
      match(matcher, value, root, 0, regExpr, positions);
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
   * @param positions mapping from string indexes to character positions
   * @return next group number and position in string
   */
  private static int[] match(final Matcher matcher, final String string, final FBuilder parent,
      final int group, final RegExpr regExpr, final IntUnaryOperator positions) {

    final FBuilder node = FElem.build(group == 0 ? Q_MATCH : Q_MGROUP);
    if(group > 0) {
      final String name = regExpr.getGroupNames()[group - 1];
      if(name != null) node.attr(Q_NAME, name);
      node.attr(Q_NR, group);
    }

    final int jg = regExpr.group(group), gc = regExpr.groupCount(matcher);
    final int start = matcher.start(jg), end = matcher.end(jg);
    int[] pos = { group + 1, start }; // group and position in string
    while(pos[0] <= gc) {
      final int g = pos[0], j = regExpr.group(g), st = matcher.start(j);
      // skip lookahead groups: they may exceed the match, and they are added at the end
      if(!regExpr.getAssertionFlags()[g - 1]) {
        // stop at groups outside the current group
        if(matcher.end(j) > end || st >= end && regExpr.getParentGroups()[g - 1] != group) break;
        // skip unmatched groups and groups captured in previous iterations
        if(st >= pos[1]) {
          if(pos[1] < st) node.text(string.substring(pos[1], st));
          pos = match(matcher, string, node, g, regExpr, positions);
          continue;
        }
      }
      pos[0]++;
    }
    if(pos[1] < end) {
      node.text(string.substring(pos[1], end));
      pos[1] = end;
    }
    if(group == 0) {
      final boolean[] assertionFlags = regExpr.getAssertionFlags();
      for(int g = 1; g <= assertionFlags.length; g++) {
        final int j = regExpr.group(g), st = matcher.start(j);
        if(assertionFlags[g - 1] && st >= 0) {
          final FBuilder lg = FElem.build(Q_LGROUP);
          final String name = regExpr.getGroupNames()[g - 1];
          if(name != null) lg.attr(Q_NAME, name);
          lg.attr(Q_NR, g);
          lg.attr(Q_VALUE, string.substring(st, matcher.end(j)));
          lg.attr(Q_POSITION, positions.applyAsInt(st));
          node.node(lg);
        }
      }
    }
    parent.node(node);
    return pos;
  }

  /**
   * Processes a non-match.
   * @param text text
   * @param parent root node
   */
  private static void nonmatch(final String text, final FBuilder parent) {
    parent.node(FElem.build(Q_NON_MATCH).text(text));
  }
}
