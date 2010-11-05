package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTOptions.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import org.basex.core.Prop;
import org.basex.data.ExprInfo;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.DBNode;
import org.basex.util.BitapSearch;
import org.basex.util.InputInfo;
import org.basex.util.Levenshtein;
import org.basex.util.TokenList;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.Span;
import org.basex.util.ft.StopWords;

/**
 * This class contains all ftcontains options.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FTOpt extends ExprInfo {
  /** Stemming dictionary. */
  public StemDir sd;
  /** Stopwords. */
  public StopWords sw;
  /** Thesaurus. */
  public ThesQuery th;
  /** Language. */
  public byte[] ln;
  /** Tokenizer ID. */
  public byte[] tokId;
  /** Stemmer ID. */
  public byte[] stemID;

  /** Flag values. */
  private final boolean[] flag = new boolean[FZ + 1];
  /** States which flags are assigned. */
  private final boolean[] set = new boolean[flag.length];

  /** Cached tokens. */
  private final TokenList query = new TokenList();
  /** Database properties. */
  private final Prop prop;
  /** Levenshtein reference. */
  private Levenshtein ls;
  /** Levenshtein error. */
  private final int lserr;

  /**
   * Constructor.
   * @param pr database properties
   */
  public FTOpt(final Prop pr) {
    prop = pr;
    lserr = pr.num(Prop.LSERROR);
  }

  /**
   * Compiles the full-text options, inheriting the options of the argument.
   * @param opt parent full-text options
   */
  public void init(final FTOpt opt) {
    for(int i = 0; i < flag.length; ++i) {
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
  void comp(final QueryContext ctx) {
    if(sw != null && ctx.value instanceof DBNode)
      sw.comp(((DBNode) ctx.value).data);
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
   * Tests if the specified flag has been set.
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
   * Checks if the first token contains the second full-text term. Sequential
   * variant.
   * @param q query token
   * @param tk input tokenizer
   * @param words words reference
   * @return number of occurrences
   * @throws QueryException query exception
   */
  int contains(final byte[] q, final FTLexer tk, final FTWords words)
      throws QueryException {

    // assign options to query:
    final FTLexer quLexer = new FTLexer(q, prop, this);
    if(quLexer.getFTOpt().is(FZ) && ls == null) ls = new Levenshtein();

    // cache query tokens:
    query.reset();
    final Iterator<Span> it = quLexer.iterator();
    final ArrayList<Span> qSpanList = new ArrayList<Span>();
    while(it.hasNext()) {
      final Span s = it.next();
      query.add(s.txt);
      qSpanList.add(s);
    }
    final Span[] qTokenSpans = qSpanList.toArray(new Span[qSpanList.size()]);

    // assign options to text:
    final FTOpt to = tk.getFTOpt();
    to.set(ST, is(ST));
    to.set(DC, is(DC));
    to.set(CS, is(CS));
    to.ln = ln;
    to.th = th;
    to.sd = sd;

    final Iterator<Span> inputIter =
      new FTLexer(tk.getText(), prop, to).iterator();

    // create the comparator:
    final Levenshtein lvs = ls;
    final int lvserr = lserr;
    final Comparator<Span> cmp = new Comparator<Span>() {
      /** Query term extension with thesaurus terms. */
      private byte[][] queryExtension;

      @Override
      public int compare(final Span o1, final Span o2) {
        final byte[] inputTkn = o1.txt;
        final byte[] queryTkn = o2.txt;

        // skip stop words, i. e. if the current query token is a stop word,
        // it is always equal to the corresponding input token:
        if(sw != null && sw.id(queryTkn) != 0) return 0;

        // [DP][JE] ugly way to send the QueryException to the caller by
        // wrapping it in a RuntimeException:
        try {
          if(quLexer.getFTOpt().is(FZ) ? // perform fuzzy search?
                lvs.similar(inputTkn, queryTkn, lvserr) :
             quLexer.getFTOpt().is(WC) ? // perform wildcard search?
                wc(words.input, inputTkn, queryTkn, 0, 0) :
             /* else */
                eq(inputTkn, queryTkn)) return 0;

          else if(th != null) {

            // if a thesaurus is provided, check if the current input token is
            // the same as one of the extension tokens of the query tokens:

            if (queryExtension == null)
              queryExtension = th.find(words.input, quLexer.getText());

            for(final byte[] txt : queryExtension) {
              final FTLexer thWordLexer = new FTLexer(txt, quLexer);
              if(thWordLexer.hasNext() && eq(thWordLexer.next().txt, inputTkn))
                return 0;
            }
          }
        } catch(final QueryException e) {
          throw new RuntimeException(e);
        }
        return 1;
      }
    };

    int c = 0;
    final BitapSearch<Span> search = new BitapSearch<Span>(inputIter,
        qTokenSpans, cmp);

    try {
      while(search.hasNext()) {
        final int pos = search.next();
        ++c;

        // if add returns true (i. e. fast evaluation mode), break the loop:
        if(words.add(pos, pos + qTokenSpans.length - 1)) break;
      }
    } catch(final RuntimeException e) {
      throw (QueryException) e.getCause();
    }

    words.all.sTokenNum++;
    words.first = false;
    return c;
  }

  /**
   * Performs a wildcard search.
   * @param ii input info
   * @param t text token
   * @param q query token
   * @param tp input position
   * @param qp query position
   * @return result of check, or -1 for a negative match
   * @throws QueryException query exception
   */
  static boolean wc(final InputInfo ii, final byte[] t, final byte[] q,
      final int tp, final int qp) throws QueryException {

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
            else FTREG.thrw(ii, q);
          }
          while(true) {
            c = ++ql < q.length ? q[ql] : 0;
            if(c >= '0' && c <= '9') m = (m << 3) + (m << 1) + c - '0';
            else if(c == '}') break;
            else FTREG.thrw(ii, q);
          }
          ++ql;
        } else { // .
          m = 1;
          n = 1;
        }
        // recursively evaluates wildcards (non-greedy)
        while(!wc(ii, t, q, tl + n, ql))
          if(tl + ++n > t.length) return false;
        if(n > m) return false;
        tl += n;
      } else {
        if(q[ql] == '\\' && ++ql == q.length) FTREG.thrw(ii, q);
        if(tl >= t.length || t[tl++] != q[ql++]) return false;
      }
    }
    return tl == t.length;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    if(is(WC)) ser.attribute(token(WILDCARDS), TRUE);
    if(is(FZ)) ser.attribute(token(FUZZY), TRUE);
    if(is(UC)) ser.attribute(token(UPPERCASE), TRUE);
    if(is(LC)) ser.attribute(token(LOWERCASE), TRUE);
    if(is(DC)) ser.attribute(token(DIACRITICS), TRUE);
    if(is(ST)) ser.attribute(token(STEMMING), TRUE);
    if(ln != null) ser.attribute(token(LANGUAGE), ln);
    if(th != null) ser.attribute(token(THESAURUS), TRUE);
  }

  @Override
  public String toString() {
    final StringBuilder s = new StringBuilder();
    if(is(WC)) s.append(' ' + USING + ' ' + WILDCARDS);
    if(is(FZ)) s.append(' ' + USING + ' ' + FUZZY);
    if(is(UC)) s.append(' ' + USING + ' ' + UPPERCASE);
    if(is(LC)) s.append(' ' + USING + ' ' + LOWERCASE);
    if(is(DC)) s.append(' ' + USING + ' ' + DIACRITICS + ' ' + SENSITIVE);
    if(is(ST) || sd != null) s.append(' ' + USING + ' ' + STEMMING);
    if(ln != null) s.append(' ' + USING + ' ' + LANGUAGE + " '" + string(ln)
        + '\'');
    if(th != null) s.append(' ' + USING + ' ' + THESAURUS);
    return s.toString();
  }
}
