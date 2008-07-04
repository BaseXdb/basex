package org.basex.query.xpath.internal;

import org.basex.BaseX;
import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.FTOption;
import org.basex.query.xpath.expr.FTPositionFilter;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Array;
import org.basex.util.FTTokenizer;
import org.basex.util.Token;

/**
 * This expression retrieves the ids of indexed fulltext terms.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 * @author Christian Gruen
 */
public final class FTIndex extends InternalExpr {
  /** Token to be found in the index. */
  private final byte[] token;
  /** FullText options. */
  private FTOption option;
  /** FullText options. */
  FTPositionFilter ftpos;

  /** Ids as result of index request with tok. */
  private int[][] ids;
  /** Flag for simple ftcontains queries. **/
  private boolean simple = true;
  /** Flag for tokens containing only one single word,
   * like 'usability' (not 'usability testing'). **/
  private boolean single = false;
  /** Number of errors allowed - used for fuzzy search. **/
  private int ne = -1;

  /**
   * Constructor.
   * @param tok index token
   * @param opt FTOption for index token
   * @param sim flag for simple ftcontains queries
   * @param sing flag for single word queries
   */
  public FTIndex(final byte[] tok, final FTOption opt,
      final boolean sim, final boolean sing) {
    token = tok;
    option = opt;
    simple = sim;
    single = sing;
  }

  /**
   * Constructor.
   * @param tok index token
   * @param opt FTOption for index token
   * @param ftp FTPositionFilter postion filter used for FTContent
   * @param sim flag for simple ftcontains queries
   * @param sing flag for single word queries
   */
  public FTIndex(final byte[] tok, final FTOption opt,
      final FTPositionFilter ftp, final boolean sim, final boolean sing) {
    token = tok;
    option = opt;
    ftpos = ftp;
    simple = sim;
    single = sing;
  }

  /**
   * Constructor used for fuzzy search.
   * @param tok index token
   * @param numErrors int number of errors allowed
   */
  public FTIndex(final byte[] tok, final int numErrors) {
    token = tok;
    ne = numErrors;
  }


  /**
   * Setter for FTPostion Filter - used for fTContent.
   * @param ftp FTPostionFilter
   */
  public void setFTPosFilter(final FTPositionFilter ftp) {
    ftpos = ftp;
  }

  /**
   * Setter for single - used for fTContent.
   * @param sing boolean flag for single word queries
   */
  public void setSingle(final boolean sing) {
    single = sing;
  }

  /**
   * Get token for index access.
   * @return token
   */
  public byte[] getToken() {
    return token;
  }

  @Override
  public Expr indexEquivalent(final XPContext ctx, final Step curr) {
    return this;
  }

  /**
   * Get FTOptions for index access.
   * @return ftoptions
   */
  public FTOption getFTOption() {
    return option;
  }



  @Override
  public NodeSet eval(final XPContext ctx) {
    final Data data = ctx.local.data;

    if (ne > 0) {
      ids = data.fuzzyIDs(token, ne);
      if (ids == null) return new NodeSet(ctx);
      return new NodeSet(Array.extractIDsFromData(ids), ctx, ids);
    }

    final FTTokenizer ftt = new FTTokenizer();
    ftt.init(token);
    ftt.sens = option.ftCasesen;
    ftt.wc = option.ftWild;
    ftt.lc = option.ftlc | !ftt.sens;
    ftt.uc = option.ftuc;

    int[][] d = null;
    while(ftt.more()) {
      final byte[] b = ftt.next();
      int[][] dd = null;
      final int pos = Token.indexOf(b, '.');
      if(ftt.wc && pos > -1) {
        dd = data.wildcardIDs(b, pos);
      } else {
        dd = data.ftIDs(b, ftt.sens);
      }
      d = d == null ? dd : phrase(d, dd);
      if(d == null || d.length == 0) break;
    }
    return new NodeSet(Array.extractIDsFromData(d), ctx);
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
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this, Token.token("simple"), Token.token(simple),
        Token.token("single"), Token.token(single));
    ser.item(token);
    ser.closeElement(this);
  }

  @Override
  public int indexSizes(final XPContext ctx, final Step curr, final int min) {
    return 1;
  }

  @Override
  public String toString() {
    return BaseX.info("%(\"%\")", name(), token);
  }
}
