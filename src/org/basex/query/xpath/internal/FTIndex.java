package org.basex.query.xpath.internal;

import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.index.FTNode;
import org.basex.index.FTTokenizer;
import org.basex.index.IndexArrayIterator;
import org.basex.index.IndexIterator;
import org.basex.query.FTOpt;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.FTArrayExpr;
import org.basex.query.xpath.expr.FTPositionFilter;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Bool;
import org.basex.util.IntArrayList;

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
  /** Collection for the index results. */
  private IndexArrayIterator iat = null;
  
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
  public FTArrayExpr indexEquivalent(final XPContext ctx, final Step curr, 
      final boolean seq) {
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
  public Bool eval(final XPContext ctx) {
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
   
    ft.lp = ctx.ftpos.lp;
    
    // check if all terms return a result at all... if not, skip node retrieval
    // (has still to be checked, maybe ft options cause troubles here)
    // ideal solution for phrases would be to process small results first
    // and end up with the largest ones.. but this seems tiresome
    int sum = 0;
    int n = 0;
    iat = new IndexArrayIterator();
    
    while(ft.more()) {
      n = data.nrIDs(ft);
      if(n == 0) return Bool.FALSE;
      ctx.checkStop();
      sum += n;
    }
    
    ft.init();
    int w = 0;
    IndexIterator it;
    while(ft.more()) {
      it = data.ids(ft);
      if (it == IndexIterator.EMPTY) return Bool.FALSE;
      if (w == 0) {
        iat = (IndexArrayIterator) it;  
      } else {
        iat = phrase(iat, (IndexArrayIterator) it, w);
      }
      w++;
    }
    
    if (iat.size() > 0) {
      if (ctx.ftpos.st) iat.setToken(new FTTokenizer[]{ft});
      iat.setTokenNum(++ctx.ftcount);
      return Bool.TRUE;
    }
    
    return Bool.FALSE;
  }

  @Override
  public boolean more() {
    return iat.more();
  }
  
  @Override
  public FTNode next(final XPContext ctx) {
    return iat.nextFTNode();
  }

  /**
   * Joins the specified iterators and returns its result.
   * @param i1 IndexArrayIterator1
   * @param i2 IndexArrayIterator2
   * @param w distance between the phrases
   * @return IndexArrayIterator as result
   */
  private IndexArrayIterator phrase(final IndexArrayIterator i1, 
      final IndexArrayIterator i2, final int w) {
    IntArrayList ial = new IntArrayList();
    i1.more();
    i2.more();
    while(true) {
      int d = i1.next() - i2.next();
      if(d == 0) {
        FTNode n1 = i1.nextFTNode();
        FTNode n2 = i2.nextFTNode();
        if (n1.merge(n2, w)) {
        //if (n1.phrase(n2, w)) {
          ial.add(n1.getFTNode());
        }
      }
      if (d <= 0 && !i1.more()) break;
       
      if (d >= 0 && !i2.more()) break;
      

  //    if((d <= 0 && !i1.more()) | (d >= 0 && !i1.more())) break;
    }
    return new IndexArrayIterator(ial.list, ial.size, false);
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
