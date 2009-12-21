package org.basex.index;

import org.basex.util.IntList;
import org.basex.util.Num;
import org.basex.util.TokenList;

/**
 * This class indexes all the XML Tokens in a balanced binary tree.
 * Additional all pre and pos values are stored.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
*/

public class ValueFTTree extends ValueTree{
  /** Factor for resize. */
  final protected double factor = 1.25;
  /** File ids. */
//  TokenList cfs = new TokenList(factor);
  /** Compressed pre values. */
  TokenList poss = new TokenList(factor);
  /** Tree structure [left, right, parent]. */
  IntList numpre = new IntList(factor);
  /** Current pointer on token in the index. */
  private int lcn;
  /** Current pointer on ft data in the index. */
  private int pft;
  /** Currenct file id. */
  int ccf; 
  
  
  /**
   * Check if specified token was already indexed; if yes, its pre
   * value is added to the existing values. otherwise, create new index entry.
   * @param tok token to be indexed
   * @param pre pre value for the token
   * @param pos pos value of the token
   * @param cf current file id
   */
  void index(final byte[] tok, final int pre, final int pos, final int cf) {
    final int os = tokens.size();
    final int n = index(tok, pre, cf == 0);
//    boolean cadd = true;
    if (os == tokens.size()) {
//      int i = map.size() > 0 ? map.containsAtPos(n) : n;
      int i = cf > 0 ? map.containsAtPos(n) : n;
      /*final byte[] c = cfs.get(n);
      if (c[c.length - 1] < cf) {
        final byte[] t = new byte[c.length + 1];
        System.arraycopy(c, 0, t, 0, c.length);
        t[c.length] = cf;
        cfs.set(t, n);        
        cadd = false;
      }
      */
      if (poss.size() > i && poss.get(i) != null) {
        poss.set(Num.add(poss.get(i), pos), i);
        numpre.set(numpre.get(i) + 1, i);
        return;
      }

    } 

    poss.add(Num.newNum(pos));
    numpre.add(1);
//    if (cadd) cfs.add(new byte[]{cf});    
  }
  
  /** 
   * Init iterator.
   */
  public void initIter() {
    cn = root;
    if(cn != -1) while(l(cn) != -1) cn = l(cn);
  }
  
  /**
   * Init Tree for new full-text values.
   */
  public void initTree() {
    poss = new TokenList(factor);    
    pres = new TokenList(factor);
    numpre = new IntList(factor);
    map = new IntList(factor);
  }
  
  /**
   * Checks for more tokens.
   * @param currcf
   * @return boolean more
   */
  public boolean more(final int currcf) {
    ccf = currcf;
    while (more()) {
            lcn = cn;
            pft = lcn;
            if (currcf > 0)
              pft = map.containsAtPos(lcn);
            if (pft > -1) return true;
//      for (byte b : cfs.get(lcn)) if (b == currcf) return true;      
      next();
    } 
    return false;
  }
  
  /**
   * Returns next token.
   * @return byte[] next token
   */
  public byte[] nextTok(){
//    final int tmp = lcn;
//    lcn = map.size() > 0 ? map.containsAtPos(lcn) : lcn;
//    return tokens.get(tmp);
    return tokens.get(lcn);
  }
  
  /**
   * Returns the next pre values.
   * @return byte[] compressed pre values
   */
  public byte[] nextPres() {
//    final byte[] pp = pres.get(lcn);
    final byte[] pp = pres.get(pft);
    //pres.set(null, lcn);
    return pp;    
  }

  /**
   * Returns the next pos values.
   * @return byte[] compressed pos values
   */
  public byte[] nextPos() {
//    final byte[] pp = poss.get(lcn);
    final byte[] pp = poss.get(pft);
    //poss.set(null, lcn);
    return pp;  
  }

  /**
   * Returns the next number of pre values.
   * @return number of pre values
   */
  public int nextNumPre() {
    return numpre.get(pft);
//    return numpre.get(lcn);
  }
}
