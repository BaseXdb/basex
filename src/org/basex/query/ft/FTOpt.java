package org.basex.query.ft;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.ft.StemDir;
import org.basex.ft.StopWords;
import org.basex.ft.ThesQuery;
import org.basex.ft.Tokenizer;
import org.basex.ft.Levenshtein;
import org.basex.query.ExprInfo;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.QueryTokens;
import org.basex.query.expr.Expr;
import org.basex.query.util.Err;
import org.basex.util.IntList;

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

  /** Flag values. */
  private final boolean[] flag = new boolean[FZ + 1];
  /** States which flags are assigned. */
  private final boolean[] set = new boolean[flag.length];

  /** Weight. */
  public Expr weight;
  /** Stemming dictionary. */
  public StemDir sd;
  /** Stopwords. */
  public StopWords sw;
  /** Thesaurus. */
  public ThesQuery th;
  /** Language. */
  public byte[] ln;

  /** Full-text tokenizer. */
  public final Tokenizer qu = new Tokenizer();

  /**
   * Compiles the full-text options, inheriting the options of the argument.
   * @param ctx query context
   * @param opt parent full-text options
   * @throws QueryException xquery exception
   */
  public void compile(final QueryContext ctx, final FTOpt opt)
      throws QueryException {

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

    if(weight != null) weight = weight.comp(ctx);
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
   * @param ctx query context
   * @param q query token
   * @return number of occurrences
   * @throws QueryException query exception
   */
  int contains(final QueryContext ctx, final byte[] q) throws QueryException {
    if(q.length == 0) return 0;

    // assign options to text
    final Tokenizer tk = ctx.fttoken;
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

        f = qu.fz ? ls.similar(t, s) : qu.wc ? wc(t, s, 0, 0) : eq(t, s);
      }

      if(!f && th != null) {
        final byte[] tmp = qu.text;
        for(final byte[] txt : th.find(qu)) {
          qu.init(txt);
          qu.more();
          f |= eq(qu.get(), t);
          qu.more();
          if(f) break;
        }
        qu.text = tmp;
      }
      
      if(f) {
        if(il == null) il = new IntList();
        // each word position has to be saved for phrases
        for(int i = 0; i < qu.pos; i++) il.add(tpos + i);
      }
      tk.p = tp;
    }

    if(il == null) return 0;
    ctx.ftselect.add(q, il);
    return il.size;
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
      s.append(" " + QueryTokens.WITH + " " + QueryTokens.STEMMING);
    if(is(WC))
      s.append(" " + QueryTokens.WITH + " " + QueryTokens.WILDCARDS);
    if(is(FZ))
      s.append(" " + QueryTokens.WITH + " " + QueryTokens.FUZZY);
    if(is(DC))
      s.append(" " + QueryTokens.DIACRITICS + " " + QueryTokens.SENSITIVE);
    if(th != null)
      s.append(" " + QueryTokens.WITH + " " + QueryTokens.THESAURUS);
    if(is(UC))
      s.append(" " + QueryTokens.UPPERCASE);
    if(is(LC))
      s.append(" " + QueryTokens.LOWERCASE);
    return s.toString();
  }
}
