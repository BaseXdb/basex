package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.Serializer;
import org.basex.query.ExprInfo;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.util.Err;
import org.basex.util.Levenshtein;
import org.basex.util.Tokenizer;

/**
 * This class contains all ftcontains options.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTOpt extends ExprInfo {
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

  /** Stemming dictionary. */
  public StemDir sd;
  /** Stopwords. */
  public StopWords sw;
  /** Thesaurus. */
  public ThesQuery th;
  /** Language. */
  public byte[] ln;

  /** Flag values. */
  private final boolean[] flag = new boolean[FZ + 1];
  /** States which flags are assigned. */
  private final boolean[] set = new boolean[flag.length];

  /** Levenshtein reference. */
  private Levenshtein ls;
  /** Full-text tokenizer. */
  private final Tokenizer qu;
  /** Levenshtein error. */
  private final int lserr;

  /**
   * Constructor.
   * @param pr database properties
   */
  public FTOpt(final Prop pr) {
    qu = new Tokenizer(pr);
    lserr = pr.num(Prop.LSERROR);
  }

  /**
   * Compiles the full-text options, inheriting the options of the argument.
   * @param opt parent full-text options
   */
  public void init(final FTOpt opt) {
    for(int i = 0; i < flag.length; i++) {
      if(!set[i]) {
        set[i] = opt.set[i];
        flag[i] = opt.flag[i];
      }
    }
    if(sw == null) sw = opt.sw;
    if(sd == null) sd = opt.sd;
    if(ln == null) ln = opt.ln;
    if(th == null) th = opt.th;
    else if(opt.th != null) th.merge(opt.th);
  }

  /**
   * Compiles the full-text options.
   * @param ctx query context
   */
  public void comp(final QueryContext ctx) {
    if(sw != null) sw.comp(ctx);
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
   * Checks if the first token contains the second full-text term.
   * Sequential variant.
   * @param q query token
   * @param tk input tokenizer
   * @param words words reference
   * @return number of occurrences
   * @throws QueryException query exception
   */
  int contains(final byte[] q, final Tokenizer tk, final FTWords words)
      throws QueryException {

    // assign options to text
    tk.st = is(ST);
    tk.dc = is(DC);
    tk.cs = is(CS);
    tk.sd = sd;
    tk.init();

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

    if(qu.fz && ls == null) ls = new Levenshtein();

    int c = 0;
    while(tk.more()) {
      final int tp = tk.p;
      final int tpos = tk.pos;
      byte[] t = tk.get();
      boolean f = false;
      boolean m = false;
      qu.init();
      while(qu.more()) {
        if(m) {
          tk.more();
          t = tk.get();
        } else {
          m = true;
        }
        final byte[] s = qu.get();
        if(sw != null && sw.id(s) != 0) continue;

        f = qu.fz ? ls.similar(t, s, lserr) : qu.wc ? wc(t, s, 0, 0) : eq(t, s);
        if(!f) break;
      }

      if(!f && th != null) {
        final byte[] tmp = qu.text;
        for(final byte[] txt : th.find(qu)) {
          qu.init(txt);
          qu.more();
          f |= eq(qu.get(), t);
          if(f) break;
        }
        qu.text = tmp;
      }

      if(f) {
        c++;
        if(words.add(tpos, tpos + qu.pos - 1)) break;
      }
      tk.p = tp;
      tk.pos = tpos;
    }

    words.all.sTokenNum++;
    words.first = false;
    return c;
  }

  /**
   * Performs a wildcard search.
   * @param t text token
   * @param q query token
   * @param tp input position
   * @param qp query position
   * @return result of check, or -1 for a negative match
   * @throws QueryException query exception
   */
  private boolean wc(final byte[] t, final byte[] q, final int tp, final int qp)
      throws QueryException {

    int ql = qp;
    int tl = tp;
    while(ql < q.length) {
      // parse wildcards
      if(q[ql] == '.') {
        byte c = ++ql < q.length ? q[ql] : 0;
        // minimum/maximum number of occurrence
        int n = 0;
        int m = Integer.MAX_VALUE;
        if(c == '?') { // .?
          ++ql;
          m = 1;
        } else if(c == '*') { // .*
          ++ql;
        } else if(c == '+') { // .+
          ++ql;
          n = 1;
        } else if(c == '{') { // .{m,n}
          m = 0;
          while(true) {
            c = ++ql < q.length ? q[ql] : 0;
            if(c >= '0' && c <= '9') n = (n << 3) + (n << 1) + c - '0';
            else if(c == ',') break;
            else Err.or(FTREG, q);
          }
          while(true) {
            c = ++ql < q.length ? q[ql] : 0;
            if(c >= '0' && c <= '9') m = (m << 3) + (m << 1) + c - '0';
            else if(c == '}') break;
            else Err.or(FTREG, q);
          }
          ++ql;
        } else { // .
          m = 1;
          n = 1;
        }
        // recursively evaluates wildcards (non-greedy)
        while(!wc(t, q, tl + n, ql)) if(tl + ++n > t.length) return false;
        if(n > m) return false;
        tl += n;
      } else {
        if(q[ql] == '\\' && ++ql == q.length) Err.or(FTREG, q);
        if(tl >= t.length || t[tl++] != q[ql++]) return false;
      }
    }
    return tl == t.length;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    if(is(ST)) ser.attribute(token(QueryTokens.STEMMING)  , TRUE);
    if(is(WC)) ser.attribute(token(QueryTokens.WILDCARDS) , TRUE);
    if(is(FZ)) ser.attribute(token(QueryTokens.FUZZY)     , TRUE);
    if(is(DC)) ser.attribute(token(QueryTokens.DIACRITICS), TRUE);
    if(is(UC)) ser.attribute(token(QueryTokens.UPPERCASE) , TRUE);
    if(is(LC)) ser.attribute(token(QueryTokens.LOWERCASE) , TRUE);
  }

  @Override
  public String toString() {
    final StringBuilder s = new StringBuilder();
    if(is(ST) || sd != null)
      s.append(" " + QueryTokens.USING + " " + QueryTokens.STEMMING);
    if(is(WC))
      s.append(" " + QueryTokens.USING + " " + QueryTokens.WILDCARDS);
    if(is(FZ))
      s.append(" " + QueryTokens.USING + " " + QueryTokens.FUZZY);
    if(is(DC))
      s.append(" " + QueryTokens.USING + " " + QueryTokens.DIACRITICS + " " +
          QueryTokens.SENSITIVE);
    if(th != null)
      s.append(" " + QueryTokens.USING + " " + QueryTokens.THESAURUS);
    if(is(UC))
      s.append(" " + QueryTokens.USING + " " + QueryTokens.UPPERCASE);
    if(is(LC))
      s.append(" " + QueryTokens.USING + " " + QueryTokens.LOWERCASE);
    return s.toString();
  }
}
