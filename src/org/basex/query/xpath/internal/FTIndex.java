package org.basex.query.xpath.internal;

import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.index.IndexIterator;
import org.basex.query.FTOpt;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.FTArrayExpr;
import org.basex.query.xpath.expr.FTPositionFilter;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.FTTokenizer;
import org.basex.util.IntList;

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
    ft.st = option.st;
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
      if(data.nrIDs(ft) == 0) return new NodeSet(ctx);
      ctx.checkStop();
    }
    
    IntList pre = null, pos = null;
    ft.init();
    int w = 0;
    while(ft.more()) {
      final IndexIterator it = data.ids(ft);
      final IntList pre2 = new IntList();
      final IntList pos2 = new IntList();
      while(it.more()) {
        ctx.checkStop();
        pre2.add(it.next());
        pos2.add(it.next());
      }
      if(pre == null) {
        pre = pre2;
        pos = pos2;
      } else {
        phrase(pre, pos, pre2, pos2, ++w);
      }
      if(pre.size == 0) break;
    }

    final int[][] d = { pre.finish(), pos.finish() };
    pre.distinct();
    return new NodeSet(pre.finish(), ctx, d);

    /*
    IntList pre = null;
    IntArrayList pos = null;
    ft.init();
    int w = 0;
    while(ft.more()) {
      final IndexIterator it = data.ids(ft);
      final IntList pre2 = new IntList();
      final IntArrayList pos2 = new IntArrayList();
      while(it.more()) {
        ctx.checkStop();
        int p = it.next();
        pre2.add(p);
        int s = it.next();
        final int[] tmp = new int[s];
        for(int i = 0; i < s; i++) tmp[i] = it.next();
        pos2.add(tmp);
      }
      if(pre == null) {
        pre = pre2;
        pos = pos2;
      } else {
        phrase(pre, pos, pre2, pos2, ++w);
      }
      if(pre.size == 0) break;
    }
    return new NodeSet(pre.finish(), ctx);
    */
  }

  /**
   * Joins the specified integer lists, reducing the entries in the first list.
   * @param pre first pre values
   * @param pos first pre values
   * @param pre2 first pre values
   * @param pos2 second pos values
   * @param w distance to first word
   */
  private void phrase(final IntList pre, final IntList pos,
      final IntList pre2, final IntList pos2, final int w) {

    int s = 0;
    for(int ai = 0, bi = 0; ai < pre.size && bi < pre2.size;) {
      int d = pre.get(ai) - pre2.get(bi);
      if(d == 0) {
        d = pos.get(ai) - pos2.get(bi) + w;
        if(d == 0) {
          pre.set(pre.get(ai), s);
          pos.set(pos.get(ai), s++);
        }
      }
      if(d <= 0) ai++;
      if(d >= 0) bi++;
    }
    pre.size = s;
    pos.size = s;
  }

  /*
  private void phrase(final IntList pre, final IntArrayList pos,
      final IntList pre2, final IntArrayList pos2, final int w) {

    int c = 0;
    for(int ai = 0, bi = 0; ai < pre.size && bi < pre2.size;) {
      int d = pre.get(ai) - pre2.get(bi);
      if(d == 0) {
        final int[] ps1 = pos.list[ai];
        final int[] ps2 = pos2.list[bi];
        int cc = 0;
        for(int aj = 0, bj = 0; aj < ps1.length && bj < ps2.length;) {
          int dd = ps1[aj] - ps2[bj] + w;
          if(dd == 0) ps1[cc++] = ps1[aj];
          if(dd <= 0) aj++;
          if(dd >= 0) bj++;
        }
        if(cc != 0) {
          pre.set(pre.get(ai), c);
          pos.list[c++] = ps1;
        }
      }
      if(d <= 0) ai++;
      if(d >= 0) bi++;
    }
    pre.size = c;
    pos.size = c;
  }
  */

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
