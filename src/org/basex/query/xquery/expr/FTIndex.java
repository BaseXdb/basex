package org.basex.query.xquery.expr;

import java.io.IOException;
import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.index.FTNode;
import org.basex.index.FTTokenizer;
import org.basex.index.IndexArrayIterator;
import org.basex.index.IndexIterator;
import org.basex.query.FTPos;
import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.DNode;
import org.basex.query.xquery.item.FTIndexItem;
import org.basex.query.xquery.item.Type;
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
public final class FTIndex extends Single {
  /** Token. */
  final byte[] token;
  /** Collection for the index results. */
  public IndexArrayIterator iat = null;
  /** Flag for loading text from disk. */
  public boolean lt; 
  /** Number of token in the query. */
  private int tn;
  /** Data reference, used for index access. */
  public Data data;
  
  /**
   * Constructor.
   * @param tok index token
   * @param loadtext flag for loading text
   * @param toknum number of token in the query
   */
  public FTIndex(final byte[] tok, final boolean loadtext, final int toknum) {
    super(null);
    token = tok;
    lt = loadtext;
    tn = toknum;
  }

  @Override
  public Expr comp(final XQContext ctx) {
    return this;
    //return super.comp(ctx);
  }
  
  @Override
  public Iter iter(final XQContext ctx) throws XQException {
    // index access
    final boolean e = eval(ctx);
    if (!e) return Iter.EMPTY;
    //else return new FTNodeIter(iat, data, ctx, lt); 
    else return new FTNodeIter() {
      @Override
      public FTIndexItem next() {
        if (!iat.more()) {
          ctx.item = null;
          return null;
        }
        
        FTNode ftn = iat.nextFTNode();
        if (ftn.size > 0) {
          DNode dn = new DNode(data, ftn.getPre(), null, Type.TXT);
          ctx.item = dn;
          ftn.convertPos();
          ctx.ftpos = new FTPos();
          ctx.ftpos.setPos(ftn.convertPos(), ftn.p.list[0]);
          
          if (ftn.getToken() != null) {
            if (lt) ctx.ftpos.ft.init(dn.data.text(ftn.getPre()));
            ctx.ftpos.term = ctx.ftitem.getTokenList();
          }
        }
        return new FTIndexItem(iat.nextFTNode());
        
      }
    };
  }

  
  /**
   * Evalutates the index access.
   * 
   * @param ctx XQContext
   * @return boolean if anything was found
   * @throws XQException XQException
   */
  private boolean eval(final XQContext ctx) throws XQException {
    final DNode dn = (DNode) ctx.coll(null).next();
    data = dn.data;
    
    final FTTokenizer ft = new FTTokenizer();
    ft.init(token);
    ft.st = ctx.ftitem.st;
    ft.dc = ctx.ftitem.dc;
    ft.lc = ctx.ftitem.lc;
    ft.uc = ctx.ftitem.uc;
    ft.cs = ctx.ftitem.cs;
    ft.wc = ctx.ftitem.wc;
    ft.fz = ctx.ftitem.fz;
   
    //ft.lp = ctx.ftpos.lp;
    
    // check if all terms return a result at all... if not, skip node retrieval
    // (has still to be checked, maybe ft options cause troubles here)
    // ideal solution for phrases would be to process small results first
    // and end up with the largest ones.. but this seems tiresome
    while(ft.more()) {
      if(dn.data.nrIDs(ft) == 0) return false;
      ctx.checkStop();
    }
    
    ft.init();
    int w = 0;
    while(ft.more()) {
      final IndexIterator it = dn.data.ids(ft);
      if(it == IndexIterator.EMPTY) return false;
      if(w == 0) {
        iat = (IndexArrayIterator) it;  
      } else {
        iat = phrase(iat, (IndexArrayIterator) it, w);
        if (iat.size() == 0) return false;
      }
      w++;
    }
    
    if (iat.size() > 0) {
      iat.setTokenNum(tn);
      iat.setToken(new FTTokenizer[]{ft});
      //iat.setTokenNum(++ctx.ftcount);
      return true;
    }
    
    return false;
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
        //if (n1.phrase(n2, w)) {
          ial.add(n1.getFTNode());
        }
      }
      if(d <= 0 && !i1.more() || d >= 0 && !i2.more()) break;
    }
    return new IndexArrayIterator(ial.list, ial.size, false);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    ser.text(token);
    ser.closeElement();
  }

  @Override
  public String toString() {
    return BaseX.info("%(\"%\")", name(), token);
  }
}
