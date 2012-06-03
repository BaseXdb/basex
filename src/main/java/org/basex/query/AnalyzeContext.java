package org.basex.query;

/**
 * This class references data used for statically analyzing a query.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class AnalyzeContext {
  /** Main query context (hidden). */
  private final QueryContext qc;
  /** Static context of an expression. */
  public final StaticContext sc;
  /** Updating flag. */
  public boolean updating;

  /**
   * Constructor.
   * @param ctx query context
   */
  public AnalyzeContext(final QueryContext ctx) {
    qc = ctx;
    sc = ctx.sc;
    updating = ctx.updating;
  }

  /**
   * Adds some compilation info.
   * @param string evaluation info
   * @param ext text text extensions
   */
  public void compInfo(final String string, final Object... ext) {
    qc.compInfo(string, ext);
  }
}
