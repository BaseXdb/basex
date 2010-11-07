package org.basex.query.ft;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;
import java.util.ArrayList;
import java.util.Comparator;
import org.basex.core.Prop;
import org.basex.query.QueryException;
import org.basex.util.BitapSearch;
import org.basex.util.InputInfo;
import org.basex.util.Levenshtein;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTOpt;
import org.basex.util.ft.Span;

/**
 * This class performs the full-text tokenization.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class FTTokenizer {
  /** Levenshtein reference. */
  final Levenshtein ls = new Levenshtein();
  /** Database properties. */
  final FTWords words;
  /** Database properties. */
  final Prop prop;
  /** Levenshtein error. */
  final int lserr;

  /**
   * Constructor.
   * @param pr database properties
   * @param w full-text words
   */
  public FTTokenizer(final Prop pr, final FTWords w) {
    prop = pr;
    lserr = pr.num(Prop.LSERROR);
    words = w;
  }

  /**
   * Checks if the first token contains the second full-text term.
   * @param q query token
   * @param lex input text
   * @param fto full-text options
   * @return number of occurrences
   * @throws QueryException query exception
   */
  int contains(final byte[] q, final FTLexer lex, final FTOpt fto)
      throws QueryException {

    // cache query tokens:
    final FTLexer quLex = new FTLexer(q, prop, fto);
    final ArrayList<Span> quSpan = new ArrayList<Span>();
    while(quLex.hasNext()) quSpan.add(quLex.next());
    final Span[] quTokens = quSpan.toArray(new Span[quSpan.size()]);

    // assign options to text:
    final FTOpt to = lex.ftOpt();
    to.set(ST, fto.is(ST));
    to.set(DC, fto.is(DC));
    to.set(CS, fto.is(CS));
    to.ln = fto.ln;
    to.th = fto.th;
    to.sd = fto.sd;

    // create the comparator:
    final Comparator<Span> cmp = new Comparator<Span>() {
      /** Query term extension with thesaurus terms. */
      private byte[][] thes;

      @Override
      public int compare(final Span o1, final Span o2) {
        final byte[] in = o1.text;
        final byte[] qu = o2.text;

        // skip stop words, i. e. if the current query token is a stop word,
        // it is always equal to the corresponding input token:
        if(fto.sw != null && fto.sw.id(qu) != 0) return 0;

        // [DP][JE] ugly way to send the QueryException to the caller by
        // wrapping it in a RuntimeException:
        try {
          // choose fuzzy, wildcard or default search
          if(fto.is(FZ) ? ls.similar(in, qu, lserr) :
            fto.is(WC) ? wc(words.input, in, qu, 0, 0) : eq(in, qu)) return 0;

          if(fto.th != null) {
            // if a thesaurus is provided, check if the current input token is
            // the same as one of the extension tokens of the query tokens:
            if(thes == null) thes = fto.th.find(words.input, q);

            for(final byte[] txt : thes) {
              quLex.init(txt);
              if(quLex.hasNext() && eq(quLex.nextToken(), in)) return 0;
            }
          }
        } catch(final QueryException e) {
          throw new RuntimeException(e);
        }
        return 1;
      }
    };

    final FTLexer ftl = new FTLexer(lex.text(), prop, to);
    final BitapSearch<Span> bs = new BitapSearch<Span>(ftl, quTokens, cmp);
    int c = 0;
    try {
      while(bs.hasNext()) {
        final int pos = bs.next();
        ++c;
        // if add returns true (i. e. fast evaluation mode), break the loop:
        if(words.add(pos, pos + quTokens.length - 1)) break;
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
}
