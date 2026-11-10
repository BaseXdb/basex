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
    final Matcher matcher = regExpr.matcher(value, qc);
    final IntUnaryOperator positions = positions(value);
    final FBuilder root = FElem.build(Q_ANALYZE_STRING_RESULT).ns();
    final boolean[] assertionFlags = regExpr.getAssertionFlags();
    int start = 0;
    while(matcher.find()) {
      if(start != matcher.start()) nonmatch(value.substring(start, matcher.start()), root);
      final boolean[] done = assertionFlags.clone();
      match(matcher, value, root, 0, done, regExpr, positions);
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
   * @param done processed groups (lookahead groups are marked in advance)
   * @param regExpr regExpr
   * @param positions mapping from string indexes to character positions
   */
  private static void match(final Matcher matcher, final String string, final FBuilder parent,
      final int group, final boolean[] done, final RegExpr regExpr,
      final IntUnaryOperator positions) {

    final FBuilder node = FElem.build(group == 0 ? Q_MATCH : Q_MGROUP);
    if(group > 0) {
      final String name = regExpr.getGroupNames()[group - 1];
      if(name != null) node.attr(Q_NAME, name);
      node.attr(Q_NR, group);
    }

    final int jg = regExpr.group(group), end = matcher.end(jg);
    int pos = matcher.start(jg);
    for(int g; (g = child(matcher, group, pos, end, done, regExpr)) != 0;) {
      final int j = regExpr.group(g), st = matcher.start(j);
      if(pos < st) node.text(string.substring(pos, st));
      done[g - 1] = true;
      match(matcher, string, node, g, done, regExpr, positions);
      pos = matcher.end(j);
    }
    if(pos < end) node.text(string.substring(pos, end));
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
  }

  /**
   * Returns the next captured group that will be nested in the specified group.
   * @param matcher matcher
   * @param group group number
   * @param pos current position in string
   * @param end end of group in string
   * @param done processed groups
   * @param regExpr regExpr
   * @return group number, or {@code 0} if no group is left
   */
  private static int child(final Matcher matcher, final int group, final int pos, final int end,
      final boolean[] done, final RegExpr regExpr) {
    final int[] parents = regExpr.getParentGroups();
    int child = 0, cst = 0, cend = 0;
    for(int g = 1; g <= done.length; g++) {
      if(done[g - 1]) continue;
      final int j = regExpr.group(g), st = matcher.start(j), en = matcher.end(j);
      if(st < pos || en > end) continue;
      int p = parents[g - 1];
      while(p > group) p = parents[p - 1];
      if(p != group) continue;
      if(child == 0 || st < cst || st == cst && en > cend) {
        child = g;
        cst = st;
        cend = en;
      }
    }
    return child;
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
