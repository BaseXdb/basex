package org.basex.query.expr;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * Pragma.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class Pragma extends ExprInfo {
  /** QName. */
  private final QNm qName;
  /** PragmaContents. */
  private final byte[] pContent;

  /**
   * Constructor.
   * @param qn QName
   * @param content pragma contents
   */
  public Pragma(final QNm qn, final byte[] content) {
    qName = qn;
    pContent = content;
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(VAL, pContent), qName);
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(PRAGMA + ' ' + qName + ' ');
    if(pContent.length != 0) tb.add(pContent).add(' ');
    return tb.add(PRAGMA2).toString();
  }
}
