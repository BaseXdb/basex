package org.basex.index;

import static org.basex.data.DataText.*;
import static org.basex.Text.*;
import java.io.IOException;
import org.basex.BaseX;
import org.basex.core.Prop;
import org.basex.io.DataAccess;
import org.basex.io.PrintOutput;
import org.basex.util.Array;
import org.basex.util.IntList;
import org.basex.util.Num;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.query.xpath.expr.FTOption;

/**
 * This class provides access to words of attribute values and text contents
 * stored on disk.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class Words implements Index {
  /** Index file. */
  private DataAccess idxb;
  /** Index file. */
  private DataAccess idxn;
  /** Index file. */
  private DataAccess idxt;
  /** Index file. */
  private DataAccess idxi;
  /** Index file. */
  private DataAccess idxl;
  /** Index file. */
  private DataAccess idxc;
  /** Number of tokens in the index. */
  private int size = 1;
  /** Temporary token reference. */
  private byte[] tt;
  /** Temporary token start. */
  private int ts;
  /** Temporary token end. */
  private int te;
  /** Temporary token length. */
  private int tl;
  
  /**
   * Constructor, specifying a filename.
   * @param db name of the database
   * @throws IOException IO Exception
   */
  public Words(final String db) throws IOException {
    final String file = DATAWRD;
    idxb = new DataAccess(db, file + 'b');
    idxc = new DataAccess(db, file + 'c');
    idxi = new DataAccess(db, file + 'i');
    idxl = new DataAccess(db, file + 'l');
    idxn = new DataAccess(db, file + 'n');
    idxt = new DataAccess(db, file + 't');
    size = idxl.read4(0);
  }

  /**
   * Returns information on the index structure.
   * @param out output stream
   * @throws IOException in case of write errors
   */
  public void info(final PrintOutput out) throws IOException {
    out.println(WORDINDEX);
    out.println(DISKHASH);
    out.println(HASHBUCKETS + size);
    final long l = idxb.length() + idxc.length() + idxi.length() +
      idxl.length() + idxn.length() + idxt.length();
    out.println(SIZEDISK + Performance.formatSize(l, true) + Prop.NL);
  }
    
  /**
   * Returns all ids from trie stored for token with respect to ftO.
   * @param tok token to be found
   * @param ftO FTOption for token
   * @return number of ids
   */
   public int[] ids(final byte[] tok, final FTOption ftO) {
    if(ftO != null) BaseX.debug("Words: No fulltext option support.");
    return ids(tok);
  }
 
   /**
    * Returns all ids from trie stored for token with respect to ftO.
    * @param tok token to be found
    * @param ftO FTOption for token
    * @return number of ids
    */
    public int[][] idPos(final byte[] tok, final FTOption ftO) {
     BaseX.debug("Words: No fulltext position support.");
     return null;
    }
   
    /**
     * Returns all ids that are in the range of tok0 and tok1.
     * @param tok0 token defining range start
     * @param itok0 token included in rangebounderies
     * @param tok1 token defining range end
     * @param itok1 token included in rangebounderies
     * @return number of ids
     */
     public int[][]  idPosRange(final byte[] tok0, final boolean itok0,
         final byte[] tok1, final boolean itok1) {
      BaseX.debug("Words: No fulltext range query support.");
      return null;
     }
   
  /**
   * Returns all ids from index stored for token.
   * @param tok token to be found
   * @return number of ids
   */
  public int[] ids(final byte[] tok) {
    byte[][] tmpids = new byte[1][];
    int s = 0;
    // parse all input tokens
    init(tok);
    while(parse()) {
      final int id = get();
      // return empty id list if no ids are found.
      if(id == 0) return Array.NOINTS;
      if(s == tmpids.length) tmpids = Array.extend(tmpids);
      tmpids[s++] = idxl.readToken(idxi.read4(id));
    }
    if(s == 0) return Array.NOINTS;

    final Num[] nums = new Num[s];
    for(int n = 0; n < s; n++) nums[n] = new Num(tmpids[n], true);
    
    // phrase search.. find all words
    final IntList hits = new IntList();
    // loop through ids of first word
    while(nums[0].more()) {
      int id1 = nums[0].id();
      int pos1 = nums[0].pos();
      nums[0].next();
      int p = 1;
      boolean more = false;
      boolean found = true;
      // parse others words
      while(p < nums.length && nums[p].more()) {
        found = false;
        final int id2 = nums[p].id();
        // id of next words is smaller; get next id
        if(id1 > id2) { nums[p].next(); continue; }
        // id of next words is bigger; skip other ids
        if(id1 < id2) { more = true; break; }
        // check word position
        final int pos2 = nums[p].pos();
        // position of next word is smaller; get next id
        if(pos1 + 1 > pos2) { nums[p].next(); continue; }
        // position of next word is bigger; skip other positions
        if(pos1 + 1 < pos2) { more = true; break; }
        // word is adjacent; continue with next word
        id1 = id2;
        pos1 = pos2;
        more = true;
        // all words have been parsed
        found = ++p == nums.length;
      }
      if(found) hits.add(id1);
      else if(!more) break;
    }
    return hits.finish();
  }

  /**
   * Returns the decompressed ids for the specified token.
   * @param tok token to be found
   * @return ids
   */
  public int nrIDs(final byte[] tok) {
    init(tok);
    // no tokens found - return 0
    if(!parse()) return 0;
    // more than one tokens found - parse all tokens
    if(te < tl) return ids(tok).length;
    // return approximate number of results for one token
    final int id = get();
    return id > 0 ? idxl.readNum(idxi.read4(id)) / 5 : 0;
  }
  
  /**
   * Close the index.
   * @throws IOException in case of write errors
   */
  public synchronized void close() throws IOException {
    idxb.close();
    idxc.close();
    idxi.close();
    idxl.close();
    idxn.close();
    idxt.close();
  }

  /**
   * Calculates a hash code for the specified token.
   * @param tok specified token
   * @param s token start
   * @param l token length
   * @return hash code
   */
  static int hash(final byte[] tok, final int s, final int l) {
    int h = 0;
    for(int i = 0; i < l; i++) h = (h << 5) - h + Token.ftNorm(tok[s + i]);
    return h;
  }
  
  /**
   * Finds the specified token and returns its unique id.
   * @return token id or -1 if token was not found
   */
  private int get() {
    final int p = hash(tt, ts, te - ts) & size - 1;
    for(int tid = idxb.read4(p); tid != 0; tid = idxn.read4(tid)) {
      final byte[] tok = idxc.readToken(idxt.read4(tid));
      if(equal(tok)) return tid;
    }
    return 0;
  }

  /** 
   * Compares two character arrays for equality.
   * @param tok token to be compared
   * @return true if the arrays are equal
   */
  private boolean equal(final byte[] tok) {
    final int cl = tok.length;
    int c = 0;
    if(cl != te - ts) return false;
    for(int i = ts; i < te; i++) {
      if(tok[c++] != Token.ftNorm(tt[i])) return false;
    }
    return true;
  }


  /**
   * Initializes the start and end positions for parsing the input token.
   * @param tok token to be parsed
   */
  private void init(final byte[] tok) {
    tl = tok.length;
    tt = tok;
    te = -1;
  }
  
  /**
   * Parses the input byte array and calculates start and end positions
   * for single words. False is returned as soon as all tokens are parsed.
   * @return true if more tokens exist
   */
  private boolean parse() {
    ts = -1;
    while(++te <= tl) {
      if(ts == -1) {
        if(te != tl && Token.ftChar(tt[te])) ts = te;
      } else if(te == tl || !Token.ftChar(tt[te])) {
        return true;
      }
    }
    tt = null;
    return false;
  }
}
