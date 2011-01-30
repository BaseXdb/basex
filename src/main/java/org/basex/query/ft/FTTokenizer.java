package org.basex.query.ft;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;
import org.basex.core.Prop;
import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.Levenshtein;
import org.basex.util.TokenList;
import org.basex.util.TokenObjMap;
import org.basex.util.ft.FTBitapSearch;
import org.basex.util.ft.FTBitapSearch.TokenComparator;
import org.basex.util.ft.FTIterator;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTOpt;

/**
 * This class performs the full-text tokenization.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Christian Gruen
 */
public final class FTTokenizer {
  /** Token comparator. */
  private final TokenComparator cmp;
  /** Cache. */
  final TokenObjMap<FTTokens> cache = new TokenObjMap<FTTokens>();
  /** Levenshtein reference. */
  final Levenshtein ls = new Levenshtein();
  /** Database properties. */
  final FTWords words;
  /** Full-text options. */
  final FTOpt opt;
  /** Levenshtein error. */
  final int lserr;

  /**
   * Constructor.
   * @param w full-text words
   * @param o full-text options
   * @param pr database properties
   */
  public FTTokenizer(final FTWords w, final FTOpt o, final Prop pr) {
    words = w;
    opt = o;
    lserr = pr.num(Prop.LSERROR);

    cmp = new TokenComparator() {
      @Override
      public boolean equal(final byte[] in, final byte[] qu)
        throws QueryException {

        // [DP] QueryException is only thrown by wc, during parsing of the wild-
        // card expression; it might be more efficient, if an automaton is built
        // before doing the actual search; thus the wild-card expression will
        // not be parsed by each comparison and will also eliminate the need of
        // throwing an exception :)
        return
          // skip stop words, i. e. if the current query token is a stop word,
          // it is always equal to the corresponding input token:
          opt.sw != null && opt.sw.id(qu) != 0 ||
          // fuzzy search:
          (opt.is(FZ) ? ls.similar(in, qu, lserr) :
          // wildcard search:
          opt.is(WC) ? wc(words.input, in, qu, 0, 0) :
          // simple search:
          eq(in, qu));
      }
    };
  }

  /**
   * Returns a new lexer, adopting the tokenizer options.
   * @param lex input lexer
   * @return lexer
   */
  FTLexer copy(final FTLexer lex) {
    // assign options to text:
    final FTOpt to = lex.ftOpt();
    to.set(ST, opt.is(ST));
    to.set(DC, opt.is(DC));
    to.set(CS, opt.is(CS));
    to.ln = opt.ln;
    to.th = opt.th;
    to.sd = opt.sd;
    return new FTLexer(to).init(lex.text());
  }
  
  /**
   * Returns cached query tokens.
   * @param query query token
   * @return number of occurrences
   * @throws QueryException query exception
   */
  FTTokens cache(final byte[] query) throws QueryException {
    FTTokens tokens = cache.get(query);
    if(tokens == null) {
      tokens = new FTTokens();
      cache.add(query, tokens);

      // cache query tokens:
      final FTIterator quLex = new FTLexer(opt).init(query);
      final TokenList quList = new TokenList(1);
      while(quLex.hasNext()) quList.add(quLex.nextToken());
      tokens.add(quList);

      // if thesaurus is required, add the terms which extend the query:
      if(opt.th != null) {
        for(final byte[] ext : opt.th.find(words.input, query)) {
          // parse each extension term to a set of tokens:
          final TokenList tl = new TokenList(1);
          // [DP] should we apply the same FT options (e.g. stemming, etc.)
          // to the thesaurus tokens?
          quLex.init(ext);
          while(quLex.hasNext()) tl.add(quLex.nextToken());
          // add each thesaurus term as an additional query term:
          tokens.add(tl);
        }
      }
    }
    return tokens;
  }

  /**
   * Checks if the first token contains the second full-text term.
   * @param tokens cached query tokens
   * @param lex input text
   * @return number of occurrences
   * @throws QueryException query exception
   */
  int contains(final FTTokens tokens, final FTLexer lex)
      throws QueryException {

    lex.init();
    final FTBitapSearch bs = new FTBitapSearch(lex, tokens, cmp);
    int c = 0;
    while(bs.hasNext()) {
      final int pos = bs.next();
      words.add(pos, pos + tokens.tokens() - 1);
      ++c;
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
