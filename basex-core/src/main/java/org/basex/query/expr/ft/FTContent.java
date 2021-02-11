package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.ft.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTContent expression.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FTContent extends FTFilter {
  /** Content type. */
  private final FTContents content;

  /**
   * Constructor.
   * @param info input info
   * @param expr expression
   * @param content contents type
   */
  public FTContent(final InputInfo info, final FTExpr expr, final FTContents content) {
    super(info, expr);
    this.content = content;
  }

  @Override
  protected boolean filter(final QueryContext qc, final FTMatch match, final FTLexer lexer) {
    if(content == FTContents.START) {
      for(final FTStringMatch sm : match) {
        if(sm.start == 0) return true;
      }
    } else if(content == FTContents.END) {
      final int p = lexer.count() - 1;
      for(final FTStringMatch sm : match) {
        if(sm.end == p) return true;
      }
    } else {
      final int s = lexer.count();
      final boolean[] bl = new boolean[s];
      for(final FTStringMatch sm : match) {
        if(sm.gaps) continue;
        for(int p = sm.start; p <= sm.end; ++p) bl[p] = true;
      }
      for(final boolean b : bl) {
        if(!b) return false;
      }
      return true;
    }
    return false;
  }

  @Override
  protected boolean content() {
    return content != FTContents.START;
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new FTContent(info, exprs[0].copy(cc, vm), content));
  }

  @Override
  public void plan(final QueryPlan plan) {
    plan.add(plan.create(this, CONTENT, content), exprs);
  }

  @Override
  public void plan(final QueryString qs) {
    qs.token(exprs[0]);
    if(content == FTContents.START) qs.token(AT).token(START);
    if(content == FTContents.END) qs.token(AT).token(END);
    qs.token(ENTIRE).token(CONTENT);
  }
}
