package org.basex.query;

import static org.basex.util.Token.*;

import org.basex.index.FTTokenizer;
import org.basex.util.IntList;
import org.basex.util.Levenshtein;
import org.basex.util.Set;

/**
 * This class contains all ftcontains options. It can be used
 * by different query implementations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class FTOpt {
  /** Words mode. */
  public enum FTMode {
    /** All option. */       ALL,
    /** All words option. */ ALLWORDS,
    /** Any option. */       ANY,
    /** Any words option. */ ANYWORD,
    /** Phrase search. */    PHRASE
  }

  /** Sensitive flag. */
  public boolean cs;
  /** Lowercase flag. */
  public boolean lc;
  /** Uppercase flag. */
  public boolean uc;
  /** Diacritics flag. */
  public boolean dc;
  /** Stemming flag. */
  public boolean st;
  /** Wildcards flag. */
  public boolean wc;
  /** Fuzzy flag. */
  public boolean fz;
  /** Stopwords. */
  public Set sw;
  /** Thesaurus flag. */
  public boolean ts;
  /** Language. */
  public byte[] ln;
  
  /** Fulltext tokenizer. */
  public final FTTokenizer sb = new FTTokenizer();

  /**
   * Checks if the first token contains the second fulltext term.
   * @param tk ft tokenizer
   * @param pos ft position filter
   * @param sub second token
   * @return number of occurrences
   */
  public int contains(final FTTokenizer tk, final FTPos pos, final byte[] sub) {
    if(sub.length == 0) return 0;

    tk.st = st;
    tk.dc = dc;
    tk.cs = cs;
    tk.init();

    sb.init(sub);
    sb.st = tk.st;
    sb.dc = tk.dc;
    sb.cs = tk.cs;
    sb.uc = uc;
    sb.lc = lc;
    sb.wc = wc;
    sb.fz = fz;

    IntList il = null;
    while(tk.more()) {
      final int tp = tk.p;
      byte[] t = tk.get();
      boolean f = true;
      boolean c = false;
      sb.init();
      final int tpos = tk.pos;
      while(f && sb.more()) {
        final byte[] s = sb.get();
        if(c) {
          tk.more();
          t = tk.get();
        } else {
          c = true;
        }

        if(sw != null) {
          final boolean s1 = sw.id(s) != 0;
          final boolean s2 = sw.id(t) != 0;
          f = !(s1 ^ s2);
          if(s1 || s2) continue;
        }
        f = sb.fz ? Levenshtein.similar(t, s) : sb.wc ?
            string(t).matches(string(s)) : eq(t, s);
      }

      if(f) {
        if(il == null) il = new IntList();
        il.add(tpos);
      }
      tk.p = tp;
    }
    
    if(il != null) pos.add(sub, il);
    return il == null ? 0 : il.size;
  }
}
