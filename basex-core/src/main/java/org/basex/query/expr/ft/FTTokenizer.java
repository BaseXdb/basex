package org.basex.query.expr.ft;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import static org.basex.util.ft.FTFlag.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.util.*;
import org.basex.util.ft.*;
import org.basex.util.ft.FTBitapSearch.TokenComparator;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class performs the full-text tokenization.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class FTTokenizer {
  /** Full-text options. */
  final FTOpt opt;

  /** Wildcard object cache. */
  private final TokenObjMap<FTWildcard> wcCache = new TokenObjMap<>();
  /** Token cache. */
  private final TokenObjMap<FTTokens> cache = new TokenObjMap<>();
  /** Token comparator. */
  private final TokenComparator cmp;
  /** Levenshtein reference. */
  private final Levenshtein ls;
  /** Calling expression. */
  private final FTWords words;

  /**
   * Constructor.
   * @param w full-text words
   * @param qc query context
   */
  FTTokenizer(final FTWords w, final QueryContext qc) {
    this(w, qc.ftOpt(), new Levenshtein(qc.context.options.get(MainOptions.LSERROR)));
  }

  /**
   * Constructor.
   * @param w full-text words
   * @param o full-text options
   * @param l Levenshtein distance calculation
   */
  private FTTokenizer(final FTWords w, final FTOpt o, final Levenshtein l) {
    words = w;
    opt = o;
    ls = l;

    cmp = new TokenComparator() {
      @Override
      public boolean equal(final byte[] in, final byte[] qu) throws QueryException {
        FTWildcard ftw = null;
        if(opt.is(WC)) {
          ftw = wcCache.get(qu);
          if(ftw == null) {
            ftw = new FTWildcard(qu);
            if(!ftw.parse()) throw FTWILDCARD_X.get(words.info, qu);
            wcCache.put(qu, ftw);
          }
        }

        return
          // skip stop words, i. e. if the current query token is a stop word,
          // it is always equal to the corresponding input token:
          opt.sw != null && opt.sw.contains(qu) ||
          // fuzzy search:
          (opt.is(FZ) ? ls.similar(in, qu) :
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
  FTLexer lexer(final FTLexer lex) {
    // assign options to text:
    final FTOpt to = lex.ftOpt();
    to.set(ST, opt.is(ST));
    to.set(DC, opt.is(DC));
    to.ln = opt.ln;
    to.th = opt.th;
    to.sd = opt.sd;
    // only change case in insensitive mode
    to.cs = opt.cs != null && opt.cs != FTCase.INSENSITIVE ? FTCase.SENSITIVE :
      FTCase.INSENSITIVE;
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
      cache.put(query, tokens);

      // cache query tokens:
      final FTIterator quLex = new FTLexer(opt).init(query);
      final TokenList quList = new TokenList(1);
      while(quLex.hasNext()) quList.add(quLex.nextToken());
      tokens.add(quList);

      // if thesaurus is required, add the terms which extend the query:
      if(opt.th != null) {
        for(final byte[] ext : opt.th.find(words.info, query)) {
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
  int contains(final FTTokens query, final FTLexer input) throws QueryException {
    input.init();
    final FTBitapSearch bs = new FTBitapSearch(input, query, cmp);
    int c = 0;
    while(bs.hasNext()) {
      final int pos = bs.next();
      words.add(pos, pos + query.length() - 1);
      ++c;
    }

    words.matches.pos++;
    words.first = false;
    return c;
  }

  /**
   * Copies this FTTokenizer.
   * @param ftw calling expression
   * @return copy
   */
  FTTokenizer copy(final FTWords ftw) {
    return new FTTokenizer(ftw, opt, ls);
  }
}
