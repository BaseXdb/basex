package org.basex.query;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.index.FTTokenizer;
import org.basex.util.Array;
import org.basex.util.BoolList;
import org.basex.util.IntList;
import org.basex.util.TokenList;

/**
 * This class contains all ftcontains positions filters. It can be used
 * by different query implementations. After calling the {@link #valid}
 * method, {@link #distance} and {@link #window} must be called with
 * query specific arguments.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTPos extends ExprInfo {
  /** Units. */
  public enum FTUnit {
    /** Word unit. */      WORDS,
    /** Sentence unit. */  SENTENCES,
    /** Paragraph unit. */ PARAGRAPHS
  }

  /** Ordered flag. */
  public boolean ordered;
  /** Start flag. */
  public boolean start;
  /** End flag. */
  public boolean end;
  /** Entire content flag. */
  public boolean content;
  /** Window unit. */
  public FTUnit wunit;
  /** Distance unit. */
  public FTUnit dunit;
  /** Same flag. */
  public boolean same;
  /** Different flag. */
  public boolean different;
  /** Same/different unit. */
  public FTUnit sdunit;
  /** Term list. */
  public TokenList term = new TokenList();
  /** Input token. */
  public FTTokenizer ft;

  /** Position list. */
  private IntList[] pos = new IntList[0];
  /** Number of position lists. */
  private int size;

  /**
   * Initializes the select operator. Has to be called before any FTWords
   * are performed.
   * @param tok tokenizer for source term
   */
  public void init(final FTTokenizer tok) {
    size = 0;
    term.reset();
    ft = tok;
  }

  /**
   * Adds the specified fulltext term and position list. This method is
   * called every time a test in {@link FTOpt#contains} was successful.
   * @param t term to be added
   * @param il positions to be added
   */
  public void add(final byte[] t, final IntList il) {
    if(size == pos.length) pos = Array.resize(pos, size, size + 1);
    pos[size++] = il;
    term.add(t);
  }

  /**
   * Sets the position values and the number of tokens.
   * @param il IntList[] with position values
   * @param ilsize int number of tokens in query
   */
  public void setPos(final IntList[] il, final int ilsize) {
    pos = il;
    size = ilsize;
  }
  
  /**
   * Performs common position tests. As {@link #distance} and {@link #window}
   * have variable arguments, they have to be called on their own.
   * @return result of check
   */
  public boolean valid() {
    return ordered() && content() && same() && different();
  }

  /**
   * Checks if the standard options are used.
   * @return result of check
  public boolean standard() {
    return !ordered && !start && !end && !content && wunit == null &&
      dunit == null;
  }
   */

  /**
   * Checks if the position values are ordered.
   * @return result of check
   */
  private boolean ordered() {
    if(!ordered || size == 1) return true;

    final IntList[] il = sortPositions();
    IntList p = il[0]; 
    IntList pp = il[1]; 
    int i = 0;
    int lp;
    while (i < p.size && pp.list[i] != 0) i++;
    lp = i;
    i++;
    while (i < p.size) {
      if (pp.list[i] < pp.list[lp] || pp.list[i] == pp.list[lp] + 1) lp = i;
      if (pp.list[lp] == size - 1) return true;
      i++;
    }
    return false;
  }

  /**
   * Checks if the start and end conditions are fulfilled.
   * @return result of check
   */
  private boolean content() {
    // ...to be revised...
    int l = 0;
    if(start || content) {
      for(int i = 0; i < size; i++) {
        boolean o = false;
        final int ts = pos[i].size;
        for(int j = 0; j < (ordered ? Math.min(1, ts) : ts); j++) {
          if(pos[i].list[j] == l) {
            l += new FTTokenizer(term.list[i]).count();
            o = true;
          }
          if(ordered && !content) break;
        }
        if(!o) return false;
        if(o) break;
      }
    }
    if(content && l != ft.count()) return false;

    if(end) {
      final int c = ft.count();
      for(int i = 0; i < size; i++) {
        l += new FTTokenizer(term.list[i]).count();
      }
      for(int i = 0; i < size; i++) {
        boolean o = false;
        final int ts = pos[i].size;
        for(int j = ordered ? Math.max(0, ts - 1) : 0; j < ts; j++) {
          if(l + pos[i].list[j] == c) {
            o = true;
            break;
          }
          if(ordered) break;
        }
        if(!o) return false;
        if(o) break;
      }
    }
    return true;
  }

  /**
   * Sorts the position values in numeric order.
   * IntList[0] = position values sorted
   * IntList[1] = pointer to the position values.
   * 
   * Each pos value has a pointer, showing which token
   * from the query cloud be found at that pos.
   * 
   * @return IntList[] position values and pointer
   */
  private IntList[] sortPositions() {
    IntList p = new IntList();
    IntList pp = new IntList();
    for (int i = 0; i < pos.length; i++) 
      if (pos[i].size == 0) return new IntList[]{new IntList(), new IntList()};
    
    int[] k = new int[size];
    int min = 0;
    boolean q = false;
    while(true) {
      min = 0;
      q = true;
      for (int j = 0; j < size; j++) {
        if (k[j] > -1) {
          if (k[min] == -1) min = j;
          q = false;
          if (pos[min].list[k[min]] > pos[j].list[k[j]]) min = j;
        }  
      }
      
      if(q) break;
      
      p.add(pos[min].list[k[min]]);
      pp.add(min);
      k[min]++;
      if (k[min] == pos[min].size) k[min] = -1;
    }
    return new IntList[]{p, pp};
  }
  
  /**
   * Checks if the position values are ordered.
   * @param mn minimum distance
   * @param mx maximum distance
   * @return result of check
   */
  public boolean distance(final long mn, final long mx) {
    if(dunit == null) return true;
    final IntList[] il = sortPositions();
    IntList p = il[0]; 
    IntList pp = il[1]; 
    
    
    boolean b = false;
    for (int z = 0; z < pp.size; z++) {
      BoolList bl = new BoolList(size);
      if (checkDist(z, p, pp, mn, mx, bl, true)) {
        b = true;
        break;
      }
    }
    return b;
  }

  /**
   * Checks if each token is reached by the ftdistance query.
   * @param x current posvalue
   * @param p pos list
   * @param pp pointer list
   * @param mn minimum number
   * @param mx maximum number
   * @param bl BollList for each token
   * @param dist flag for ftdistance
   * @return boolean result
   * 
   */
  private boolean checkDist(final int x, final IntList p,  final IntList pp, 
      final long mn, final long mx, final BoolList bl, final boolean dist) {
    if(bl.all(true)) return true;
    boolean f = false;
    int i = x + 1;
    
    final int p1 = calcPosition(p.list[x], dist ? dunit : wunit);
    while (i < p.size) {
      final int p2 = calcPosition(p.list[i], dist ? dunit : wunit);
      if (dist) {
        // ftdistance
        final int d = Math.abs(p1 - p2) - 1;
        if (d >= mn && d <= mx && !bl.list[pp.list[i]]) {
          bl.list[pp.list[x]] = true;
          bl.list[pp.list[i]] = true;
          f |= checkDist(i, p, pp, mn, mx, bl, dist);         
        }
      } else {
        // ftwindow
        final int d = Math.abs(p1 - p2);
        if (mn + d <= mx && !bl.list[pp.list[i]]) {
          bl.list[pp.list[x]] = true;
          bl.list[pp.list[i]] = true;
          f |= checkDist(i, p, pp, mn + d, mx, bl, dist);         
        }
      }
      i++;
    }
    return f;
  }

  
  /**
   * Checks if the specified window is correct.
   * @param win window value
   * @return result of check
   */
  public boolean window(final long win) {
    if(wunit == null) return true;

    final IntList[] il = sortPositions();
    IntList p = il[0]; // new IntList();
    IntList pp = il[1]; // new IntList();
    
    
    boolean b = false;
    for (int z = 0; z < pp.size; z++) {
      BoolList bl = new BoolList(size);
      if (checkDist(z, p, pp, 1, win, bl, false)) {
        b = true;
        break;
      }
    }
    return b;
  }

  /**
   * Checks if all words are found in the same unit.
   * @return result of check
   */
  private boolean same() {
    if(!same) return true;

    final IntList il = pos[0];
    int p = -1, q = 0;
    for(int i = 0; i < il.size && p != q; i++) {
      p = calcPosition(il.list[i], sdunit);
      q = p;
      for(int j = 1; j < size && p == q; j++) {
        for(int k = 0; k < pos[j].size; k++) {
          q = calcPosition(pos[j].list[k], sdunit);
          if(p == q) break;
        }
      }
    }
    return p == q;
  }

  /**
   * Checks if all words are found in different units.
   * @return result of check
   */
  private boolean different() {
    if(!different) return true;

    int l = -1;
    for(int i = 0; i < size; i++) {
      boolean o = false;
      for(int j = 0; j < pos[i].size; j++) {
        final int p = calcPosition(pos[i].list[j], sdunit);
        if(i != 0 && p != l) {
          o = true;
          break;
        }
        l = p;
      }
      if(i != 0 && !o) return false;
    }
    return true;
  }

  /**
   * Calculates a position value, dependent on the specified unit.
   * @param p word position
   * @param u unit
   * @return new position
   */
  private int calcPosition(final int p, final FTUnit u) {
    if(u == FTUnit.WORDS) return p;
    final FTTokenizer iter = ft;
    iter.init();
    while(iter.more() && iter.pos != p);
    return u == FTUnit.SENTENCES ? iter.sent : iter.para;
  }
  
  /**
   * Gets position values.
   * @return IntList pos values
   */
  public IntList[] getPos() {
    return pos;
  }

  /**
   * Evaluates the mild not expression.
   * @return boolean result
   */
  public boolean mildNot() {
    for(int i = 1; i < pos.length; i++) {
      for(int j = 0; j < pos[i].size; j++) {
        if(pos[0].contains(pos[i].list[j])) return false;
      }
    }
    return true;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    if(ordered) ser.attribute(token(QueryTokens.ORDERED), TRUE);
    if(start) ser.attribute(token(QueryTokens.START), TRUE);
    if(end) ser.attribute(token(QueryTokens.END), TRUE);
    if(content) ser.attribute(token(QueryTokens.CONTENT), TRUE);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(ordered) sb.append(" " + QueryTokens.ORDERED);
    if(start) sb.append(" " + QueryTokens.AT + " " + QueryTokens.START);
    if(end) sb.append(" " + QueryTokens.AT + " " + QueryTokens.END);
    if(content) sb.append(" " + QueryTokens.ENTIRE + " " + QueryTokens.CONTENT);
    return sb.toString();
  }
  
  /*
   * Test FTDistance.
  private static void testWindow() {
    System.out.println("Test FTWindow: ");
    boolean e = false;
    FTPos ftpos = new FTPos();
    ftpos.size = 2;
    ftpos.wunit = FTPos.FTUnit.WORDS;
    ftpos.pos = new IntList[2];
    ftpos.pos[0] = new IntList(new int[]{3, 10});
    ftpos.pos[1] = new IntList(new int[]{7, 11});
    if (ftpos.window(0)) {
      System.out.println("Q1: ERROR");
      e = true;
    }
    if (ftpos.window(1)) {
      System.out.println("Q2: ERROR");
      e = true;
    }
    if (!ftpos.window(2)) {
      System.out.println("Q3: ERROR");
      e = true;
    }

    ftpos.size = 4;
    ftpos.pos = new IntList[4];
    ftpos.pos[0] = new IntList(new int[]{1});
    ftpos.pos[1] = new IntList(new int[]{5, 6, 7});
    ftpos.pos[2] = new IntList(new int[]{8, 9, 10, 11});
    ftpos.pos[3] = new IntList(new int[]{12, 13, 14, 15, 16});

    if (ftpos.window(6)) {
      System.out.println("Q4: ERROR");
      e = true;
    }

    if (ftpos.window(11)) {
      System.out.println("Q5: ERROR");
      e = true;
    }

    if (!ftpos.window(12)) {
      System.out.println("Q6: ERROR");
      e = true;
    }

    
    ftpos.size = 4;
    ftpos.pos = new IntList[4];
    ftpos.pos[0] = new IntList(new int[]{1, 2, 3, 4});
    ftpos.pos[1] = new IntList(new int[]{5, 6, 7});
    ftpos.pos[2] = new IntList(new int[]{8, 9, 10, 11});
    ftpos.pos[3] = new IntList(new int[]{12, 13, 14, 15, 16});

    if (ftpos.window(8)) {
      System.out.println("Q7: ERROR");
      e = true;
    }

    if (!ftpos.window(9)) {
      System.out.println("Q8: ERROR");
      e = true;
    }
    if (!e) System.out.println("\t no errors");
  }
  
  /**
   * Test FTDistance.
  private static void testDistance() {
    System.out.println("Test FTOrdered: ");
    boolean e = false;
    FTPos ftpos = new FTPos();
    ftpos.size = 2;
    ftpos.dunit = FTPos.FTUnit.WORDS;
    ftpos.pos = new IntList[2];
    ftpos.pos[0] = new IntList(new int[]{3, 10});
    ftpos.pos[1] = new IntList(new int[]{7, 11});
    if (!ftpos.distance(0, 0)) {
      System.out.println("Q1: ERROR");
      e = true;
    }
    
    
    ftpos.size = 3;
    ftpos.pos = new IntList[3];
    ftpos.pos[0] = new IntList(new int[]{1, 10, 21});
    ftpos.pos[1] = new IntList(new int[]{5, 13, 14, 15});
    ftpos.pos[2] = new IntList(new int[]{6, 16, 17, 18, 19, 20, 22, 23});
    ftpos.dunit = FTPos.FTUnit.WORDS;
    if (!ftpos.distance(4, 4)) {
      System.out.println("Q2: ERROR");
      e = true;
    }

    if (ftpos.distance(7, 7)) {
      System.out.println("Q3: ERROR");
      e = true;
    }
    
    
    ftpos.size = 3;
    ftpos.pos = new IntList[3];
    ftpos.pos[0] = new IntList(new int[]{1, 10, 21});
    ftpos.pos[1] = new IntList(new int[]{5, 15, 23, 25, 26, 27, 28});
    ftpos.pos[2] = new IntList(new int[]{6, 16, 29, 30, 31, 32, 33});
    ftpos.dunit = FTPos.FTUnit.WORDS;
    if (!ftpos.distance(4, 4)) {
      System.out.println("Q4: ERROR");
      e = true;
    }

    if (ftpos.distance(7, 7)) {
      System.out.println("Q5: ERROR");
      e = true;
    }


    ftpos.size = 4;
    ftpos.pos = new IntList[4];
    ftpos.pos[0] = new IntList(new int[]{1});
    ftpos.pos[1] = new IntList(new int[]{3, 4, 5, 6, 7});
    ftpos.pos[2] = new IntList(new int[]{8, 9, 10, 11});
    ftpos.pos[3] = new IntList(new int[]{12, 13, 14, 15, 16});
    ftpos.dunit = FTPos.FTUnit.WORDS;
    if (!ftpos.distance(2, 4)) {
      System.out.println("Q6: ERROR");
      e = true;
    }

    if (ftpos.distance(1, 2)) {
      System.out.println("Q7: ERROR");
      e = true;
    }

    
    if (!e) System.out.println("\t no errors");
  }
  
  /**
   * Test FTContent.
  private static void testContent() {
    System.out.println("Test FTContent: ");
    boolean e = false;
    FTPos ftpos = new FTPos();
    ftpos.size = 1;
    ftpos.term = new TokenList();
    ftpos.ordered = false;
    ftpos.content = true;
    ftpos.ft = new FTTokenizer();
    for (byte c = 'a'; c < 'e'; c++) 
      ftpos.term.add(new byte[]{c});
    for (byte c = 'a'; c < 'e'; c++) 
      ftpos.term.add(new byte[]{c});
    ftpos.ft.init(new byte[]{'a', 'b', 'c', 'd', 
        'a', 'b', 'c', 'd'});
    ftpos.pos = new IntList[1];
    ftpos.pos[0] = new IntList(new int[]{0});
    if (!ftpos.content()) {
      System.out.println("Q1: ERROR");
      e = true;
    }
    
    ftpos.size = 4;
    ftpos.pos = new IntList[4];
    ftpos.pos[0] = new IntList(new int[]{0, 4});
    ftpos.pos[1] = new IntList(new int[]{1, 5});
    ftpos.pos[2] = new IntList(new int[]{2, 6});
    ftpos.pos[3] = new IntList(new int[]{3, 7});
    ftpos.ft.init(new byte[]{'a', 'b', 'c', 'd', 
        'a', 'b', 'c', 'd'});
    if (!ftpos.content()) {
      System.out.println("Q2: ERROR");
      e = true;
    }
    
    ftpos.size = 1;
    ftpos.ft.init(new byte[]{'a', ' ', 'b', ' ', 'c', ' ', 'd'});
    ftpos.content = false;
    ftpos.start = true;
    if (!ftpos.content()) {
      System.out.println("Q3: ERROR");
      e = true;
    }
    
    ftpos.size = 4;
    ftpos.pos = new IntList[4];
    ftpos.pos[0] = new IntList(new int[]{0, 4});
    ftpos.pos[1] = new IntList(new int[]{1, 5});
    ftpos.pos[2] = new IntList(new int[]{2, 6});
    ftpos.pos[3] = new IntList(new int[]{3, 7});
    ftpos.ft.init(new byte[]{'a', ' ', 'b', ' ', 'c', ' ', 'd'});
    if (!ftpos.content()) {
      System.out.println("Q4: ERROR");
      e = true;
    }

    ftpos.size = 1;
    ftpos.pos = new IntList[1];
    ftpos.pos[0] = new IntList(new int[]{0, 3, 4, 7});
    ftpos.ft.init(new byte[]{'a', ' ', 'b', ' ', 'c', ' ', 'd'});
    ftpos.start = false;
    ftpos.end = true;
    if (!ftpos.content()) {
      System.out.println("Q5: ERROR");
      e = true;
    }
    
    ftpos.size = 4;
    ftpos.pos = new IntList[4];
    ftpos.pos[0] = new IntList(new int[]{0, 4});
    ftpos.pos[1] = new IntList(new int[]{1, 5});
    ftpos.pos[2] = new IntList(new int[]{2, 6});
    ftpos.pos[3] = new IntList(new int[]{3, 7});
    ftpos.ft.init(new byte[]{'a', ' ', 'b', ' ', 'c', ' ', 'd'});
    if (!ftpos.content()) {
      System.out.println("Q6: ERROR");
      e = true;
    }
    
    if (!e) System.out.println("\t no errors");
  }
  
  /**
   * Test FTOrdered.
  private static void testOrdered() {
    System.out.println("Test FTOrdered: ");
    boolean e = false;
    FTPos ftpos = new FTPos();
    ftpos.ordered = true;
    ftpos.size = 1;
    IntList tok1 = new IntList(new int[]{6, 12, 16, 35});
    ftpos.pos = new IntList[]{tok1};
    if (!ftpos.ordered()) {
      System.out.println("Q1: ERROR");
      e = true;
    }
    
    ftpos.size = 3;
    IntList tok2 = new IntList(new int[]{4, 21, 36, 40});
    IntList tok3 = new IntList(new int[]{13, 14, 31, 37});
    ftpos.pos = new IntList[]{tok1, tok2, tok3};
    if (!ftpos.ordered()) {
      System.out.println("Q2: ERROR");
      e = true;
    }

    tok2 = new IntList(new int[]{4, 21, 36, 40});
    tok3 = new IntList(new int[]{13, 14, 15, 37});
    ftpos.pos = new IntList[]{tok1, tok2, tok3};
    if (!ftpos.ordered()) {
      System.out.println("Q3: ERROR");
      e = true;
    }

    tok2 = new IntList(new int[]{4, 5, 36, 40});
    tok3 = new IntList(new int[]{13, 14, 17, 43});
    ftpos.pos = new IntList[]{tok1, tok2, tok3};
    if (!ftpos.ordered()) {
      System.out.println("Q4: ERROR");
      e = true;
    }

    tok2 = new IntList(new int[]{4, 5, 36});
    tok3 = new IntList(new int[]{13, 14, 17, 34});
    ftpos.pos = new IntList[]{tok1, tok2, tok3};
    if (ftpos.ordered()) {
      System.out.println("Q5: ERROR");
      e = true;
    }
    if (!e) System.out.println("\t no errors");
  }

  /**
   * Main test method.
   * @param args String[] 
  public static void main(final String[] args) {
    testOrdered();
    testContent();
    testDistance();
    testWindow();
  }
   */
}
