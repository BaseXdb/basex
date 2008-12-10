package org.basex.query.xquery.expr;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.index.FTNode;

import org.basex.index.FTTokenizer;
import org.basex.index.IndexArrayIterator;
import org.basex.index.IndexIterator;
import org.basex.query.FTOpt.FTMode;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.item.DNode;
import org.basex.query.xquery.item.FTNodeItem;
import org.basex.query.xquery.iter.FTNodeIter;
import org.basex.query.xquery.iter.Iter;
import org.basex.util.IntArrayList;

/**
 * This expression retrieves the ids of indexed fulltext terms.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
public final class FTIndex extends FTExpr {
  /** Fulltext token. */
  public byte[] tok;
  /** Minimum and maximum occurrences. */
  final Expr[] occ;
  /** Search mode. */
  final FTMode mode;
  /** Collection for the index results. */
  public IndexArrayIterator iat = null;
  /** Data reference. */
  public Data data;
  
  /**
   * Constructor.
   * @param t token
   * @param m search mode
   * @param o occurrences
   */
  public FTIndex(final byte[] t, final FTMode m, final Expr[] o) {
    tok = t;
    mode = m;
    occ = o;
  }


  /**
   * Evaluate index access.
   * 
   * @param ctx current context
   * @return boolean
   */
  public boolean eval(final XQContext ctx) {
    data = ((DNode) ctx.item).data;

    final FTTokenizer ft = new FTTokenizer();
    ft.init(tok);
    ft.lp = true;
    
    // check if all terms return a result at all... if not, skip node retrieval
    // (has still to be checked, maybe ft options cause troubles here)
    // ideal solution for phrases would be to process small results first
    // and end up with the largest ones.. but this seems tiresome
    while(ft.more()) {
      if(data.nrIDs(ft) == 0) return false;
      ctx.checkStop();
    }
    
    ft.init();
    int w = 0;
    while(ft.more()) {
      final IndexIterator it = data.ids(ft);
      if(it.size() == 0) return false;

      if(w == 0) {
        iat = (IndexArrayIterator) it;  
      } else {
        iat = phrase(iat, (IndexArrayIterator) it, w);
        if (iat.size() == 0) return false;
      }
      w++;
    }
    
    if (iat.size() > 0) {
      iat.setToken(new FTTokenizer[]{ft});
      iat.setTokenNum(++ctx.ftcount);
      return true;
    }
    
    return false;
  }

  /*
  @Override
  public Iter iter(final XQContext ctx) {
    return new FTNodeIter(){
      @Override
      public FTNodeItem next() { 
        if (iat == null && !eval(ctx)) return new FTNodeItem();
        return iat.more() ? new FTNodeItem(iat.nextFTNode(), data) : 
          new FTNodeItem(new FTNode(), data); 
      }
    };
  }
*/
  @Override
  public Iter iter(final XQContext ctx) {
    return new FTNodeIter(){
      @Override
      public FTNodeItem next() { 
        if (iat == null && !evalIter(ctx)) return new FTNodeItem();
        return iat.more() ? new FTNodeItem(
            iat.nextFTNode(), data) : 
          new FTNodeItem(new FTNode(), data); 
      }
    };
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
    final IntArrayList ial = new IntArrayList();
    i1.more();
    i2.more();
    while(true) {
      final int d = i1.next() - i2.next();
      if(d == 0) {
        final FTNode n1 = i1.nextFTNode();
        final FTNode n2 = i2.nextFTNode();
        if (n1.merge(n2, w)) {
          ial.add(n1.getFTNode());
        }
      }
      if(d <= 0 && !i1.more() || d >= 0 && !i2.more()) break;
    }
    return new IndexArrayIterator(ial.list, ial.size, false);
  }

 
  /**
   * Evaluate index access.
   * 
   * @param ctx current context
   * @return boolean
   */
  public boolean evalIter(final XQContext ctx) {
      data = ((DNode) ctx.item).data;
      final FTTokenizer ft = new FTTokenizer();
      ft.init(tok);
      ft.lp = true;
      while(ft.more()) {
        if(data.nrIDs(ft) == 0) return false;
        ctx.checkStop();
      }
      ft.init();
      int w = 0;
      while(ft.more()) {
        final IndexIterator it = data.ids(ft);
        if(w == 0) {
          iat = (IndexArrayIterator) it;  
        } else {
          iat = IndexArrayIterator.and(iat, (IndexArrayIterator) it, w);
        }
        w++;
      }
      
      if (iat != null && iat.size() > 0) {
        iat.setToken(new FTTokenizer[]{ft});
        iat.setTokenNum(++ctx.ftcount);
        return true;
      }
      return false;
  }

  
  
  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ser.text(tok);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return BaseX.info("%(\"%\")", name(), tok);
  }
}
