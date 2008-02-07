package org.basex.query.xquery.expr;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.TokenList;

/**
 * FTOptions expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTOptions extends Single implements Cloneable {
  /** Sensitive flag. */
  public boolean sensitive;
  /** Lowercase flag. */
  public boolean lowercase;
  /** Uppercase flag. */
  public boolean uppercase;
  /** Diacritics flag. */
  public boolean diacritics;
  /** Stemming flag (currently ignored). */
  public boolean stemming;
  /** Thesaurus flag (currently ignored). */
  public boolean thesaurus;
  /** Wildcards flag. */
  public boolean wildcards;
  /** Stopwords flag. */
  public TokenList stopwords;
  /** Language (currently ignored). */
  public byte[] language;

  /**
   * Constructor.
   * @param e expression
   */
  public FTOptions(final Expr e) {
    super(e);
  }

  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    final FTOptions tmp = ctx.ftopt;
    ctx.ftopt = this;
    final Iter it = ctx.iter(expr);
    ctx.ftopt = tmp;
    return it;
  }

  @Override
  public FTOptions clone() {
    try {
      return (FTOptions) super.clone();
    } catch(final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public String toString() {
    return expr.toString();
  }
}
