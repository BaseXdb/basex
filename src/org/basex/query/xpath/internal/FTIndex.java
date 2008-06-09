package org.basex.query.xpath.internal;

import org.basex.data.Data;
import org.basex.data.Serializer;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.FTOption;
import org.basex.query.xpath.expr.FTPositionFilter;
import org.basex.query.xpath.locpath.Step;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Array;
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
  private FTPositionFilter ftpos;
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
    
    if(single) {
      ids = data.ftIDs(token, option);
      if (ids == null) return new NodeSet(ctx);
      /*if(simple) {
        // simple ftcontains don't need pos-values out of the index
        return new NodeSet(Array.extractIDsFromData(ids), ctx);
      } else {*/
        return new NodeSet(Array.extractIDsFromData(ids), ctx, ids);
      //}
    } else {
      evalNonSingle(data);
      if (ids == null) return new NodeSet(ctx);
      /*if(simple) {
        // simple ftcontains don't need pos-values out of the index
        return new NodeSet(Array.extractIDsFromData(ids), ctx);
      } else {*/
        return new NodeSet(Array.extractIDsFromData(ids), ctx, ids);
      //no}
    }
  }

  @Override
  public String toString() {
    return Token.string(name()) + "(" + Token.string(token) + ")";
  }

  /**
   * Evaluation of non single word queries.
   * @param data data reference
   */
  private void evalNonSingle(final Data data) {
    byte[] tok;
    int i = Token.indexOf(token, ' ');
    if (i == -1) i = token.length; 
    tok = new byte[i];
    System.arraycopy(token, 0, tok, 0, i);
    int[][] tmp = data.ftIDs(tok, option);

    if(tmp == null) return;

    int lastpre = -1;
    byte[] db = null;
    int count = 0;
    int[][] res = new int[2][tmp[0].length];
    int j; // runvariable for token
    int k; // runvaribale for db

    tok = token;

    if(option.ftCase == FTOption.CASE.UPPERCASE) {
      tok = Token.uc(tok);
    } else if(option.ftCase == FTOption.CASE.INSENSITIVE ||
        option.ftCase == FTOption.CASE.LOWERCASE) {
      tok = Token.lc(tok);
      
    }
    
    for(i = 0; i < tmp[0].length; i++) {
      j = 0;
      k = 0;
      if(lastpre != tmp[0][i]) {
        lastpre = tmp[0][i];
        db = data.text(lastpre);
      }
      
          // "usability" not in "usability testing"
      // "mexico" not in "new mexico"
      // "mexico" not in "new mexico city"
      if(db.length >= tmp[1][i] + tok.length) {
        //if(option.ftCase == FTOption.CASE.INSENSITIVE) {
        if(option.ftCase == FTOption.CASE.INSENSITIVE) db = Token.lc(db);
        while(j < tok.length && tmp[1][i] + k < db.length) { 
            if(db[tmp[1][i] + k] == tok[j]) {
              j++;
              k++;
            } else if(!Token.ftChar(db[tmp[1][i] + k])) {
              while(db.length > tmp[1][i] + k 
                  && !Token.ftChar(db[tmp[1][i] + k])) k++;
            } else {
              break;
            }
          //}       
        }

        if (j == tok.length) {
          if (ftpos != null && ftpos.ftContent != null) {
            if ((ftpos.ftContent == FTPositionFilter.CONTENT.ATSTART 
                && tmp[1][i] == 0) 
                || (ftpos.ftContent == FTPositionFilter.CONTENT.ATEND 
                    && tmp[1][i] + k == db.length)
                || (ftpos.ftContent ==  FTPositionFilter.CONTENT.ENTIRECONTENT
                    && tmp[1][i] == 0 && db.length == k)) {              
              res[0][count] = tmp[0][i];
              res[1][count++] = tmp[1][i];

            }
          } else {
            res[0][count] = tmp[0][i];
            res[1][count++] = tmp[1][i];
          }
        } 
      }
    }
    
    if (count == 0) {
      ids = null;
      return;
    }
    ids = new int[2][];
    ids[0] = Array.finish(res[0], count);
    ids[1] = Array.finish(res[1], count);
  }

  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this, Token.token("simple"), Token.token(simple),
        Token.token("single"), Token.token(single));
    ser.item(token);
    ser.closeElement(this);
  }

  @Override
  public int indexSizes(final XPContext ctx, final Step curr,
      final int min) {
    return 1;
  }
}
