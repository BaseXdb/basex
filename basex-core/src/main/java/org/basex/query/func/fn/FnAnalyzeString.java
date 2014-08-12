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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnAnalyzeString extends RegEx {
  /** Module prefix. */
  private static final String PREFIX = "fn";
  /** QName. */
  private static final QNm Q_ANALYZE = QNm.get(PREFIX, "analyze-string-result", FNURI);
  /** QName. */
  private static final QNm Q_MATCH = QNm.get(PREFIX, "match", FNURI);
  /** QName. */
  private static final QNm Q_NONMATCH = QNm.get(PREFIX, "non-match", FNURI);
  /** QName. */
  private static final QNm Q_MGROUP = QNm.get(PREFIX, "group", FNURI);
  /** Attribute for the analyze-string-result function. */
  private static final String NR = "nr";

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toEmptyToken(exprs[0], qc);
    final Pattern p = pattern(exprs[1], exprs.length == 3 ? exprs[2] : null, qc, true);
    final String str = string(value);
    final Matcher m = p.matcher(str);

    final FElem root = new FElem(Q_ANALYZE).declareNS();
    int s = 0;
    while(m.find()) {
      if(s != m.start()) nonmatch(str.substring(s, m.start()), root);
      match(m, str, root, 0);
      s = m.end();
    }
    if(s != str.length()) nonmatch(str.substring(s), root);
    return root;
  }

  /**
   * Processes a match.
   * @param matcher matcher
   * @param string string
   * @param parent parent
   * @param group group number
   * @return next group number and position in string
   */
  private static int[] match(final Matcher matcher, final String string, final FElem parent,
      final int group) {

    final FElem nd = new FElem(group == 0 ? Q_MATCH : Q_MGROUP);
    if(group > 0) nd.add(NR, token(group));

    final int start = matcher.start(group), end = matcher.end(group), gc = matcher.groupCount();
    int[] pos = { group + 1, start }; // group and position in string
    while(pos[0] <= gc && matcher.end(pos[0]) <= end) {
      final int st = matcher.start(pos[0]);
      if(st >= 0) { // group matched
        if(pos[1] < st) nd.add(string.substring(pos[1], st));
        pos = match(matcher, string, nd, pos[0]);
      } else pos[0]++; // skip it
    }
    if(pos[1] < end) {
      nd.add(string.substring(pos[1], end));
      pos[1] = end;
    }
    parent.add(nd);
    return pos;
  }

  /**
   * Processes a non-match.
   * @param text text
   * @param par root node
   */
  private static void nonmatch(final String text, final FElem par) {
    par.add(new FElem(Q_NONMATCH).add(text));
  }
}
