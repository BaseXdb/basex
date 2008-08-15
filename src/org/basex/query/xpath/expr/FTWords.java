package org.basex.query.xpath.expr;

import static org.basex.util.Token.*;
import org.basex.BaseX;
import org.basex.data.MetaData;
import org.basex.data.Serializer;
import org.basex.query.FTOpt.FTMode;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.internal.FTIndex;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.Item;
import org.basex.util.FTTokenizer;

/**
 * Fulltext primary expression and FTTimes.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTWords extends FTArrayExpr {
  /** Token. */
  final byte[] token;
  /** Occurrences. */
  final long[] occ;
  /** Search mode. */
  final FTMode mode;

  /**
   * Constructor.
   * @param t token
   * @param m search mode
   * @param o occurrences
   */
  public FTWords(final byte[] t, final FTMode m, final long[] o) {
    exprs = new FTArrayExpr[] {};
    token = t;
    mode = m;
    occ = o;
  }

  @Override
  public Item eval(final XPContext ctx) {
    if (ctx.local.nodes[0] != 0) {
      final FTTokenizer tmp = ctx.ftitem;
      ctx.ftitem = new FTTokenizer();
      ctx.ftitem.init(ctx.local.data.text(ctx.local.nodes[0] + 1));
      final boolean r = contains(ctx);
      ctx.ftitem = tmp;
      return Bool.get(r);
    }
    return Bool.get(contains(ctx));
  }
  
  @Override
  public FTArrayExpr compile(final XPContext ctx) {
    return this;
  }
 
  
  /**
   * Evaluates the fulltext match.
   * @param ctx query context
   * @return result of matching
   */
  private boolean contains(final XPContext ctx) {
    int len = 0;
    int o = 0;
    
    switch(mode) {
      case ALL:
        final int o1 = fto.contains(ctx.ftitem, ctx.ftpos.pos, token);
        if(o1 == 0) return false;
        len += token.length * o1;
        o += o1;
        break;
      case ALLWORDS:
        for(final byte[] t2 : split(token, ' ')) {
          final int o2 = fto.contains(ctx.ftitem, ctx.ftpos.pos, t2);
          if(o2 == 0) return false;
          len += t2.length * o2;
          o += o2;
        }
        break;
      case ANY:
        final int o3 = fto.contains(ctx.ftitem, ctx.ftpos.pos, token);
        len += token.length * o3;
        o += o3;
        break;
      case ANYWORD:
        for(final byte[] t4 : split(token, ' ')) {
          final int o4 = fto.contains(ctx.ftitem, ctx.ftpos.pos, t4);
          len += t4.length * o4;
          o += o4;
        }
        break;
      case PHRASE:
        final int o5 = fto.contains(ctx.ftitem, ctx.ftpos.pos, token);
        len += token.length * o5;
        o += o5;
        break;
    }
    return o >= occ[0] && o <= occ[1];
  }

  @Override
  public boolean indexOptions(final MetaData meta) {
    // if the following conditions yield true, the index is accessed:
    // - case sensitivity, diacritics and stemming flag complies with index flag
    // - no stop words are specified
    // - if wildcards are specified, the fulltext index is a trie
    // - no FTTimes option is specified
    // <SG> I (temporarily) readded case sensitivity check for fuzzy index 
    return meta.ftcs == fto.cs && 
      meta.ftdc == fto.dc && meta.ftstem == fto.st &&
      fto.sw == null && (!fto.wc || !meta.ftfuzzy) &&
      occ[0] == 1  && occ[1] == Long.MAX_VALUE;
  }
  
  @Override
  public FTArrayExpr indexEquivalent(final XPContext ctx, final Step curr, 
      final boolean seq) {
    return new FTIndex(token, fto);
  }

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    // not correct; all ft options should be checked
    fto.sb.init(token);
    fto.sb.fz = fto.fz;
    fto.sb.wc = fto.wc;
    fto.sb.st = fto.st;
    fto.sb.cs = fto.cs;
    fto.sb.dc = fto.dc;
    
    int i = 0;
    while(fto.sb.more()) {
      int n = ctx.local.data.nrIDs(fto.sb);
      if(n == 0) return 0;
      i = Math.max(i, n);
    }
    return i;
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