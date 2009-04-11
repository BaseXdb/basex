package org.basex.query.ft;

import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.index.FTTokenizer;
import org.basex.io.IO;
import org.basex.query.ExprInfo;
import org.basex.query.QueryTokens;
import org.basex.util.IntList;
import org.basex.util.Levenshtein;
import org.basex.util.Map;
import org.basex.util.Set;

/**
 * This class contains all ftcontains options. It can be used
 * by different query implementations.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
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

  /** Stemming dictionary. */
  public Map<byte[]> sd;
  /** Stopwords. */
  public Set sw;
  /** Language. */
  public byte[] ln;

  /** Fulltext tokenizer. */
  public final FTTokenizer qu = new FTTokenizer();

  /**
   * Compiles the fulltext options, inheriting the parent options.
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
    if(sd == null) sd = opt.sd;
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
   * Returns if the specified flag has been set.
   * @param f flag index
   * @return true if flag has been set
   */
  public boolean isSet(final int f) {
    return set[f];
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
   * @param q query token
   * @return number of occurrences
   */
  int contains(final FTTokenizer tk, final FTPos pos, final byte[] q) {
    if(q.length == 0) return 0;

    // assign options to text
    tk.st = is(ST);
    tk.dc = is(DC);
    tk.cs = is(CS);
    tk.sd = sd;
    tk.init();

    if(is(FZ) && ls == null) ls = new Levenshtein();
    // assign options to query
    qu.init(q);
    qu.st = tk.st;
    qu.dc = tk.dc;
    qu.cs = tk.cs;
    qu.sd = tk.sd;
    // the following options only apply to the query terms..
    qu.uc = is(UC);
    qu.lc = is(LC);
    qu.wc = is(WC);
    qu.fz = is(FZ);

    IntList il = null;
    while(tk.more()) {
      final int tp = tk.p;
      final int tpos = tk.pos;
      byte[] t = tk.get();
      boolean f = true;
      boolean c = false;
      qu.init();
      while(f && qu.more()) {
        if(c) {
          tk.more();
          t = tk.get();
        } else {
          c = true;
        }

        final byte[] s = qu.get();
        if(sw != null && sw.id(s) != 0) continue;

        f = qu.fz ? ls.similar(t, s) : qu.wc ?
            string(t).matches(string(s)) : eq(t, s);
      }

      if(f) {
        if(il == null) il = new IntList();
        // each word position has to be saved for phrases
        for(int i = 0; i < qu.pos; i++) il.add(tpos + i);
      }
      tk.p = tp;
    }

    if(il == null) return 0;
    pos.add(q, il);
    return il.size;
  }

  /**
   * Processes stopwords from the specified file.
   * @param fl file
   * @param e except flag
   * @return success flag
   */
  public boolean stopwords(final IO fl, final boolean e) {
    if(sw == null) sw = new Set();
    try {
      for(final byte[] sl : split(norm(fl.content()), ' ')) {
        if(e) sw.delete(sl);
        else if(sw.id(sl) == 0) sw.add(sl);
      }
      return true;
    } catch(final IOException ex) {
      return false;
    }
  }

  /**
   * Processes a stemming dictionary.
   * @param fl file
   * @return success flag
   */
  public boolean stemming(final IO fl) {
    if(sd == null) sd = new Map<byte[]>();
    try {
      for(final byte[] sl : split(fl.content(), '\n')) {
        byte[] val = null;
        for(final byte[] st : split(norm(sl), ' ')) {
          if(val == null) val = st;
          else sd.add(st, val);
        }
        sd.add(sl);
      }
      return true;
    } catch(final IOException ex) {
      return false;
    }
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
