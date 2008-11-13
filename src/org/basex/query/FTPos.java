package org.basex.query;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.index.FTTokenizer;
import org.basex.util.Array;
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
   * Checks if the position values are ordered.
   * @return result of check
   */
  private boolean ordered() {
    if(!ordered || size == 1) return true;

    final IntList[] il = sortPositions();
    IntList p = il[0]; // new IntList();
    IntList pp = il[1]; // new IntList();
    int i = 0;
    int lp;
    while (i < p.size && pp.list[i] != 0) i++;
    lp = i;
    i++;
    while (i < p.size) {
      if (pp.list[i] == pp.list[lp] + 1) lp = i;
      else if (pp.list[i] < pp.list[lp]) return false;
      if (pp.list[lp] == size - 1) return true;
      i++;
    }
    
    
    return false;
    /*
    int c = -1;
    int d = -1;
    for(int i = 0; i < size; i++) {
      for(int j = 0; j < pos[i].size; j++) {
        d = pos[i].get(j);
        if(c <= d) break;
      }
      if(c > d) return false;
      c = d;
    }
    return true;
    */
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
    IntList p = il[0]; // new IntList();
    IntList pp = il[1]; // new IntList();
    int p1;
    int p2;

    /*  int[] k = new int[size];
    int min = 0;
    int p1;
    int p2;
    boolean q = false;
    while(true) {
      min = 0;
      q = true;
      for (int j = 0; j < size; j++) {
        if (k[j] > -1) {
          if (k[min] == -1) min = j;
          q = false;
          if (pos[min].get(k[min]) > pos[j].get(k[j])) min = j;
        }  
      }
      
      if(q) break;
      
      p.add(pos[min].get(k[min]));
      pp.add(min);
      k[min]++;
      if (k[min] == pos[min].size) k[min] = -1;
    }
*/
    int[] res = new int[p.size];
    int c = 0;
    int i = 0;
    int lp = 0;
    while (i < pp.size && pp.list[lp] == pp.list[i]) {
      res[c++] = i;
      i++;
    }
    while (i < pp.size) {
      int[] tmp = new int[p.size];
      int tc = 0;
      lp = i;
      while (i < pp.size && pp.list[lp] == pp.list[i]) {
        p1 = calcPosition(p.list[i], dunit);
        boolean o = false;
        for(int z = 0; z < c; z++) {
          p2 = calcPosition(p.list[res[z]], dunit);
          final int d = Math.abs(p1 - p2) - 1;
          if(d >= mn && d <= mx) {
            o = true;
            break;
          }
        }
        if (o) tmp[tc++] = i;
        i++;
      }
      res = tmp;
      c = tc;
    }
    
    return c > 0;

/*
    // ...to be revised...
    int l = -1;
    for(int i = 0; i < size; i++) {
      boolean o = false;
      for(int j = 0; j < pos[i].size; j++) {
        final int p = calcPosition(pos[i].get(j), dunit);
        final int d = Math.abs(p - l) - 1;
        if(i == 0 || (d >= mn && d <= mx)) {
          o = true;
          l = p;
          break;
        }
      }
      if(!o) return false;
    }
    return true; */
  }

  
  /**
   * Checks if the specified window is correct.
   * @param win window value
   * @return result of check
   */
  public boolean window(final long win) {
    if(wunit == null) return true;

    // ...to be revised...
    int l = -1;
    for(int i = 0; i < size; i++) {
      boolean o = false;
      for(int j = 0; j < pos[i].size; j++) {
        final int p = calcPosition(pos[i].list[j], wunit);
        if(i == 0 || (Math.abs(p - l) - 1 < win)) {
          o = true;
          l = p;
          break;
        }
      }
      if(!o) return false;
    }
    return true;
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
   * Get position values.
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
}
