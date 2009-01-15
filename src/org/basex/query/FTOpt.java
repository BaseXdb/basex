package org.basex.query;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.MetaData;
import org.basex.data.Serializer;
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
public final class FTOpt extends ExprInfo {
  /** Levenshtein reference. */
  Levenshtein ls;

  /** Words mode. */
  public enum FTMode {
    /** All option. */       ALL,
    /** All words option. */ ALLWORDS,
    /** Any option. */       ANY,
    /** Any words option. */ ANYWORD,
    /** Phrase search. */    PHRASE
  }
  /** Sensitive flag. */
  public static final int CS = 0;
  /** Lowercase flag. */
  public static final int LC = 1;
  /** Uppercase flag. */
  public static final int UC = 2;
  /** Diacritics flag. */
  public static final int DC = 3;
  /** Stemming flag. */
  public static final int ST = 4;
  /** Wildcards flag. */
  public static final int WC = 5;
  /** Fuzzy flag. */
  public static final int FZ = 6;
  /** Thesaurus flag. */
  public static final int TS = 7;

  /** Flag values. */
  private final boolean[] flag = new boolean[TS + 1];
  /** States which flags are assigned. */
  private final boolean[] set = new boolean[flag.length];

  /** Stopwords. */
  public Set sw;
  /** Language. */
  public byte[] ln;

  /** Fulltext tokenizer. */
  public final FTTokenizer sb = new FTTokenizer();

  /**
   * Compiles the fulltext options, inheriting the parent.options.
   * @param opt parent fulltext options
   */
  public void compile(final FTOpt opt) {
    for(int i = 0; i < flag.length; i++) {
      if(!set[i]) {
        set[i] = opt.set[i];
        flag[i] = opt.flag[i];
      }
    }
    if(sw == null) sw = opt.sw;
    if(ln == null) ln = opt.ln;
  }

  /**
   * Sets the specified flag.
   * @param f flag to be set
   * @param v value
   */
  public void set(final int f, final boolean v) {
    flag[f] = v;
    set[f] = true;
  }

  /**
   * Returns the specified flag.
   * @param f flag index
   * @return flag
   */
  public boolean is(final int f) {
    return flag[f];
  }

  /**
   * Checks if the first token contains the second fulltext term.
   * @param tk ft tokenizer
   * @param pos ft position filter
   * @param sub query token
   * @return number of occurrences
   */
  public int contains(final FTTokenizer tk, final FTPos pos, final byte[] sub) {
    if(sub.length == 0) return 0;

    tk.st = is(ST);
    tk.dc = is(DC);
    tk.cs = is(CS);
    tk.init();

    if(is(FZ) && ls == null) ls = new Levenshtein();
    sb.init(sub);
    sb.st = tk.st;
    sb.dc = tk.dc;
    sb.cs = tk.cs;
    sb.uc = is(UC);
    sb.lc = is(LC);
    sb.wc = is(WC);
    sb.fz = is(FZ);

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
        f = sb.fz ? ls.similar(t, s) : sb.wc ?
            string(t).matches(string(s)) : eq(t, s);
      }

      if(f) {
        if(il == null) il = new IntList();
        // each word position has to be saved for phrases
        for(int i = 0; i < sb.pos; i++) il.add(tpos + i);
      }
      tk.p = tp;
    }

    if(il != null) pos.add(sub, il);
    return il == null ? 0 : il.size;
  }

  /**
   * Merges two FTOpts.
   * @param ftopt1 FTOpt to merge
   */
  public void merge(final FTOpt ftopt1) {
    for (int i = 0; i < set.length; i++) {
      if(!flag[i]) {
        flag[i] = ftopt1.flag[i];
        set[i] = ftopt1.set[i];
      } else {
        set[i] |= ftopt1.set[i];
      }
    }
  }

  /**
   * Checks if an index can be used for query evaluation.
   * @param meta meta data reference
   * @return result of check
   */
  public boolean indexAccessible(final MetaData meta) {
    /* if the following conditions are valid, the method returns true:
     - case sensitivity, diacritics and stemming flags comply with index
     - no stop words are specified
     - if wildcards are specified, the fulltext index is a trie */
    return meta.ftcs == is(FTOpt.CS) && meta.ftdc == is(FTOpt.DC) &&
      meta.ftst == is(FTOpt.ST) && sw == null && (!is(FTOpt.WC) || !meta.ftfz);
  }
  
  @Override
  public void plan(final Serializer ser) throws IOException {
    if(is(ST)) ser.attribute(token(QueryTokens.STEMMING), TRUE);
    if(is(WC)) ser.attribute(token(QueryTokens.WILDCARDS), TRUE);
    if(is(FZ)) ser.attribute(token(QueryTokens.FUZZY), TRUE);
    if(is(DC)) ser.attribute(token(QueryTokens.DIACRITICS), TRUE);
    if(is(UC)) ser.attribute(token(QueryTokens.UPPERCASE), TRUE);
    if(is(LC)) ser.attribute(token(QueryTokens.LOWERCASE), TRUE);
  }

  @Override
  public String toString() {
    final StringBuilder s = new StringBuilder();
    if(is(ST))
      s.append(" " + QueryTokens.WITH + " " + QueryTokens.STEMMING);
    if(is(WC))
      s.append(" " + QueryTokens.WITH + " " + QueryTokens.WILDCARDS);
    if(is(FZ))
      s.append(" " + QueryTokens.WITH + " " + QueryTokens.FUZZY);
    if(is(DC))
      s.append(" " + QueryTokens.DIACRITICS + " " + QueryTokens.SENSITIVE);
    if(is(UC))
      s.append(" " + QueryTokens.UPPERCASE);
    if(is(LC))
      s.append(" " + QueryTokens.LOWERCASE);
    return s.toString();
  }
}
