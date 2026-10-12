package org.basex.query.expr.ft;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.util.ft.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.hash.*;

/**
 * FTScope expression.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FTScope extends FTFilter {
  /** Same/different flag. */
  private final boolean same;

  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param expr expression
   * @param same same flag
   * @param unit unit
   */
  public FTScope(final InputInfo info, final FTExpr expr, final boolean same, final FTUnit unit) {
    super(info, expr, unit);
    this.same = same;
  }

  @Override
  protected boolean filter(final QueryContext qc, final FTMatch match, final FTLexer lexer) {
    return select(match, combo -> same ? same(combo, lexer) : different(combo, lexer),
        (ex, combo) -> keep(ex, combo, lexer));
  }

  /**
   * Checks if all string matches of the combination occur in the same unit.
   * @param combo include string matches
   * @param lexer tokenizer
   * @return result of check
   */
  private boolean same(final FTMatch combo, final FTLexer lexer) {
    int s = -1;
    for(final FTStringMatch sm : combo) {
      final int p = pos(sm.start, lexer);
      if(s == -1) s = p;
      else if(s != p) return false;
      if(sm.start != sm.end && s != pos(sm.end, lexer)) return false;
    }
    return true;
  }

  /**
   * Checks if all pairs of string matches of the combination occur in different units.
   * @param combo include string matches
   * @param lexer tokenizer
   * @return result of check
   */
  private boolean different(final FTMatch combo, final FTLexer lexer) {
    final int cs = combo.size();
    if(cs < 2) return false;
    for(int c1 = 0; c1 < cs; c1++) {
      for(int c2 = c1 + 1; c2 < cs; c2++) {
        if(!different(combo.get(c1), combo.get(c2), lexer)) return false;
      }
    }
    return true;
  }

  /**
   * Checks if two string matches do not occur in the same single unit.
   * @param sm1 first string match
   * @param sm2 second string match
   * @param lexer tokenizer
   * @return result of check
   */
  private boolean different(final FTStringMatch sm1, final FTStringMatch sm2,
      final FTLexer lexer) {
    final int s1 = pos(sm1.start, lexer), s2 = pos(sm2.start, lexer);
    return s1 != s2 || s1 != pos(sm1.end, lexer) || s2 != pos(sm2.end, lexer);
  }

  /**
   * Checks if an exclude is to be kept for the includes of a combination.
   * @param ex exclude
   * @param combo include string matches
   * @param lexer tokenizer
   * @return result of check
   */
  private boolean keep(final FTStringMatch ex, final FTMatch combo, final FTLexer lexer) {
    if(same && pos(ex.start, lexer) != pos(ex.end, lexer)) return false;
    for(final FTStringMatch sm : combo) {
      if(different(sm, ex, lexer) == same) return false;
    }
    return true;
  }

  @Override
  public FTExpr copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new FTScope(info, exprs[0].copy(cc, vm), same, unit));
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof final FTScope fts && same == fts.same && super.equals(obj);
  }

  @Override
  public void toXml(final QueryPlan plan) {
    plan.add(plan.create(this, same ? SAME : DIFFERENT, unit), exprs);
  }

  @Override
  public void toString(final QueryString qs) {
    qs.token(exprs[0]).token(same ? SAME : DIFFERENT).token(unit);
  }
}
