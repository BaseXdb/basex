package org.basex.index;

import org.basex.util.IntList;

/**
 * This class indexes all the XML Tokens in a balanced binary tree.
 * The iterator returns all compressed pre values in a sorted manner.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
*/

public class ValueFTTrees {
  /** Saves the token length for each ValueFTTree. */
  final IntList sizes = new IntList(1.25);
  /** For each token length is a ValueFTTree created. */
  ValueFTTree[] trees = new ValueFTTree[sizes.maxSize()];
  
  /**
   * Check if specified token was already indexed; if yes, its pre
   * value is added to the existing values. otherwise, create new index entry.
   * @param tok token to be indexed
   * @param pre pre value for the token
   * @param pos pos value of the token
   * @param cf current file id
   */
  void index(final byte[] tok, final int pre, final int pos, final int cf) {
    int i = sizes.containsAtPos(tok.length);
    if (i == -1) {        
      sizes.add(tok.length);
      if (sizes.size() > trees.length) {
        ValueFTTree[] tmp = new ValueFTTree[sizes.maxSize()];
        System.arraycopy(trees, 0, tmp, 0, trees.length);
        trees = tmp;             
      }      
      i = sizes.size() - 1;
      trees[i] = new ValueFTTree();
    }      
    
    trees[i].index(tok, pre, pos, cf);
  }

  /**
   * Return next token length.
   * @return id of ValueFTTree
   */
  private int getNextMin() {
    int min = -1;
    for (int j = 0; j < sizes.size(); j++) {
      final int n = sizes.get(j);
      if (n > 0 && (min == -1 || sizes.get(min) > n))
        min = j;
    }
    return min;
  }
  
  /**
   * Init all ValueFTTrees, for iterative traversal.
   */
  public void init() {
    for (int j = 0; j < sizes.size(); j++) 
      trees[j].init();    
  }
  
//  /** Old memory consumption. */
//  private long oldMem;
//
//  /**
//   * Shows the current memory consumption/difference.
//   * @param desc description
//   * @param diff difference flag
//   */
//  private void diffMem(final String desc, final boolean diff) {
//    Performance.gc(4);
//    final Runtime rt = Runtime.getRuntime();
//    final long mem = rt.totalMemory() - rt.freeMemory();
//    System.out.println("- " + desc + ": " +
//      Performance.format(diff ? oldMem - mem : mem));
//    oldMem = mem;
//  }
  
  /**
   * Init all ValueFTTrees and remove full-text data.
   */
  public void initTrees() {
    for (int j = 0; j < sizes.size(); j++) {
      trees[j].initTree();        
      sizes.set(-sizes.get(j), j);
  }
  
//    diffMem("all", false);    

//    for (int j = 0; j < sizes.size(); j++) trees[j].poss = null;      
//    diffMem("pos: ", true);
//
//    for (int j = 0; j < sizes.size(); j++) trees[j].pres = null;
//    diffMem("pres: ", true);
//    for (int j = 0; j < sizes.size(); j++) trees[j].numpre = null;
//    diffMem("numpre: ", true);
//    
//    for (int j = 0; j < sizes.size(); j++) trees[j].map = null;
//    diffMem("map: ", true);
//    for (int j = 0; j < sizes.size(); j++) trees[j].tree = null;
//    diffMem("tree: ", true);
//    for (int j = 0; j < sizes.size(); j++) trees[j].mod = null;
//    diffMem("mod: ", true);
//    int c = 0;
//    for (int j = 0; j < sizes.size(); j++) c += trees[j].tokens.list.length;
//    System.out.println("length:" + c);
//    for (int j = 0; j < sizes.size(); j++) trees[j].tokens = null;
//    diffMem("tokens: ", true); 
//    trees = null;
//    diffMem("trees: ", true);
  }
  
//  public void info() {
//    int sum = 0; 
//    for (int i = 0; i < sizes.size(); i++) {
//      System.out.println("length: " + sizes.get(i) + " count:" 
//      trees[i].tokens.size());
//      sum += trees[i].tokens.size();
//    }
//    System.out.println("sum:" + sum);
//  }
  
  
  /** Pointer on current ValueFTTree. */
  int nsize = -1;
  /**
   * Checks for more tokens.
   * @param cf current file
   * @return boolean more
   */
  public boolean more(final int cf) {
    if (nsize == -1) {
      nsize = getNextMin();
      if (nsize == -1) return false;
    }
    if (trees[nsize].more(cf)) return true;

    sizes.set(-sizes.get(nsize), nsize);
    nsize = -1;
    return more(cf);        
  }
  
  /**
   * Returns next token.
   * @return byte[] next token
   */
  public byte[] nextTok() {
    return trees[nsize].nextTok();
  }
  
  /**
   * Returns next pointer.
   * @return int next pointer.
   */
  public int nextPoi() {
    return trees[nsize].next();
  }
  
  /**
   * Returns the next pre values.
   * @return byte[] compressed pre values
   */
  public byte[] nextPres() {
    return trees[nsize].nextPres();    
  }
  
  /**
   * Returns next pos values.
   * @return compressed pos values
   */
  public byte[] nextPos() {
    return trees[nsize].nextPos();
  }

  /**
   * Returns next number of pre values.
   * @return int number of pre values
   */
  public int nextNumPre() {
    return trees[nsize].nextNumPre();
  }
}
