package org.basex.query.xpath.internal;

import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.query.FTOpt;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.FTArrayExpr;
import org.basex.query.xpath.expr.FTPositionFilter;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Array;
import org.basex.util.FTTokenizer;

/**
 * This expression retrieves the ids of indexed fulltext terms.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
public final class FTIndex extends FTArrayExpr {
  /** Token. */
  final byte[] token;
  /** FullText options. */
  private FTOpt option;

  /**
   * Constructor.
   * @param tok index token
   * @param opt FTOption for index token
   */
  public FTIndex(final byte[] tok, final FTOpt opt) {
    exprs = new FTArrayExpr[] {};
    token = tok;
    option = opt;
  }

  /**
   * Setter for FTPostion Filter - used for fTContent.
   * @param ftp FTPostionFilter
   */
  public void setFTPosFilter(final FTPositionFilter ftp) {
    ftpos = ftp;
  }

  @Override
  public FTArrayExpr indexEquivalent(final XPContext ctx, final Step curr) {
    return this;
  }

  /**
   * Get FTOptions for index access.
   * @return ft options
   */
  public FTOpt getFTOption() {
    return option;
  }

  @Override
  public NodeSet eval(final XPContext ctx) {
    final Data data = ctx.local.data;

    final FTTokenizer ft = new FTTokenizer();
    ft.init(token);
    ft.stem = option.st;
    ft.dc = option.dc;
    ft.lc = option.lc;
    ft.uc = option.uc;
    ft.cs = option.cs;
    ft.wc = option.wc;
    ft.fz = option.fz;
    
    // check if all terms return a result at all... if not, skip node retrieval
    // (has still to be checked, maybe ft options cause troubles here)
    // ideal solution for phrases would be to process small results first
    // and end up with the largest ones.. but this seems tiresome
    while(ft.more()) {
      if(data.nrFTIDs(ft.next(), ft) == 0) return new NodeSet(ctx);
    }
    
    int[][] d = null;
    ft.init();
    while(ft.more()) {
      final byte[] b = ft.next();
      int[][] dd = data.ftIDs(b, ft);
      ctx.checkStop();
      
      d = d == null ? dd : phrase(d, dd);
      if(d == null || d.length == 0) break;
    }
    return new NodeSet(Array.extractIDsFromData(d), ctx, d);
  }

  /**
   * Joins the specified arrays, returning only phrase hits.
   * @param a first array
   * @param b second array
   * @return resulting array
   */
  private int[][] phrase(final int[][] a, final int[][] b) {
    if(b == null) return null;

    final int[][] il = new int[2][0];
    for(int ai = 0, bi = 0; ai < a[0].length && bi < b[0].length;) {
      int d = a[0][ai] - b[0][bi];
      if(d == 0) {
        d = a[1][ai] - b[1][bi] + 1;
        if(d == 0) {
          final int i = il[0].length;
          final int[] t0 = new int[i + 1], t1 = new int[i + 1];
          System.arraycopy(il[0], 0, t0, 0, i);
          System.arraycopy(il[1], 0, t1, 0, i);
          t0[i] = b[0][bi];
          t1[i] = b[1][bi];
          il[0] = t0;
          il[1] = t1;
        }
      }
      if(d <= 0) ai++;
      if(d >= 0) bi++;
    }
    return il;
  }

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    return 1;
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this);
    ser.text(token);
    ser.closeElement(this);
  }

  @Override
  public String toString() {
    return BaseX.info("%(\"%\")", name(), token);
  }
}
