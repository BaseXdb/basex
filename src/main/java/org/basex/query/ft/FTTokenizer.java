package org.basex.query.ft;

import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;
import org.basex.core.Prop;
import org.basex.query.QueryException;
import org.basex.util.Levenshtein;
import org.basex.util.ft.FTBitapSearch;
import org.basex.util.ft.FTBitapSearch.TokenComparator;
import org.basex.util.ft.FTIterator;
import org.basex.util.ft.FTLexer;
import org.basex.util.ft.FTOpt;
import org.basex.util.hash.TokenObjMap;
import org.basex.util.list.TokenList;

/**
 * This class performs the full-text tokenization.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class FTTokenizer {
  /** Wildcard object cache. */
  final TokenObjMap<FTWildcard> wcCache = new TokenObjMap<FTWildcard>();
  /** Levenshtein reference. */
  final Levenshtein ls = new Levenshtein();
  /** Calling expression. */
  final FTWords words;
  /** Full-text options. */
  final FTOpt opt;
  /** Levenshtein error. */
  final int lserr;

  /** Token comparator. */
  private final TokenComparator cmp;
  /** Cache. */
  private final TokenObjMap<FTTokens> cache = new TokenObjMap<FTTokens>();

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

        FTWildcard ftw = null;
        if(opt.is(WC)) {
          ftw = wcCache.get(qu);
          if(ftw == null) {
            ftw = new FTWildcard(qu, words.input);
            wcCache.add(qu, ftw);
          }
        }

        return
          // skip stop words, i. e. if the current query token is a stop word,
          // it is always equal to the corresponding input token:
          opt.sw != null && opt.sw.id(qu) != 0 ||
          // fuzzy search:
          (opt.is(FZ) ? ls.similar(in, qu, lserr) :
          // wild-card search:
          ftw != null ? ftw.match(in) :
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
   * @param query cached query tokens
   * @param input input text
   * @return number of occurrences
   * @throws QueryException query exception
   */
  int contains(final FTTokens query, final FTLexer input)
      throws QueryException {

    input.init();
    final FTBitapSearch bs = new FTBitapSearch(input, query, cmp);
    int c = 0;
    while(bs.hasNext()) {
      final int pos = bs.next();
      words.add(pos, pos + query.length() - 1);
      ++c;
    }

    words.matches.sTokenNum++;
    words.first = false;
    return c;
  }
}
