package org.basex.query.ft;

import static org.basex.query.QueryText.*;

import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTContent expression.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FTContent extends FTFilter {
  /** Content type. */
  private final FTContents content;

  /**
   * Constructor.
   * @param ii input info
   * @param ex expression
   * @param cont contents type
   */
  public FTContent(final InputInfo ii, final FTExpr ex, final FTContents cont) {
    super(ii, ex);
    content = cont;
  }

  @Override
  protected boolean filter(final QueryContext ctx, final FTMatch mtc, final FTLexer lex) {
    if(content == FTContents.START) {
      for(final FTStringMatch sm : mtc) if(sm.start == 0) return true;
    } else if(content == FTContents.END) {
      final int p = lex.count() - 1;
      for(final FTStringMatch sm : mtc) if(sm.end == p) return true;
    } else {
      final int s = lex.count();
      final boolean[] bl = new boolean[s];
      for(final FTStringMatch sm : mtc) {
        if(sm.gaps) continue;
        for(int p = sm.start; p <= sm.end; ++p) bl[p] = true;
      }
      for(final boolean b : bl) if(!b) return false;
      return true;
    }
    return false;
  }

  @Override
  protected boolean content() {
    return content != FTContents.START;
  }

  @Override
  public FTExpr copy(final QueryContext ctx, final VarScope scp, final IntObjMap<Var> vs) {
    return new FTContent(info, expr[0].copy(ctx, scp, vs), content);
  }

  @Override
  public void plan(final FElem plan) {
    addPlan(plan, planElem(CONTENT, content.toString()), expr);
  }

  @Override
  public String toString() {
    return super.toString() + (
      content == FTContents.START ? AT + ' ' + START :
      content == FTContents.END   ? AT + ' ' + END :
        ENTIRE + ' ' + CONTENT);
  }
}
