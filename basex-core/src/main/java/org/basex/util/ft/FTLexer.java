package org.basex.util.ft;

import static org.basex.util.ft.FTFlag.*;

import java.util.*;

import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.expr.ft.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Performs full-text lexing on token. Calls tokenizers, stemmers matching to full-text options
 * to achieve this.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Jens Erat
 */
public final class FTLexer extends FTIterator implements IndexSearch {
  /** Tokenizer. */
  private final Tokenizer tokens;
  /** Full-text options. */
  private final FTOpt ftOpt;

  /** Text to be tokenized. */
  private byte[] text = Token.EMPTY;
  /** Levenshtein error. */
  private int errors = -1;

  /** Iterator over result tokens. */
  private FTIterator iter;
  /** The last parsed span. */
  private FTSpan curr;
  /** The last parsed text. */
  private byte[] ctxt;

  /**
   * Constructor, using the default full-text options. Called by the serializer,
   * {@link FTFilter}, and the map visualizations.
   */
  public FTLexer() {
    this(null);
  }

  /**
   * Default constructor.
   * @param ftOpt full-text options (can be {@code null})
   */
  public FTLexer(final FTOpt ftOpt) {
    this.ftOpt = ftOpt;

    // check if language option is provided:
    Language lang = ftOpt != null ? ftOpt.ln : null;
    if(lang == null) lang = Language.def();

    // use default tokenizer if specific tokenizer is not available.
    Tokenizer tkns = Tokenizer.IMPL.get(0);
    for(final Tokenizer tknzr : Tokenizer.IMPL) {
      if(tknzr.supports(lang)) {
        tkns = tknzr;
        break;
      }
    }
    tokens = tkns.get(ftOpt);
    iter = tokens;

    // wrap original iterator
    if(ftOpt != null) {
      errors = ftOpt.errors;

      if(ftOpt.is(ST)) {
        if(ftOpt.sd == null) {
          // use default stemmer if specific stemmer is not available.
          Stemmer st = Stemmer.IMPL.get(0);
          for(final Stemmer stem : Stemmer.IMPL) {
            if(stem.supports(lang)) {
              st = stem;
              break;
            }
          }
          iter = st.get(lang, iter);
        } else {
          iter = new DictionaryStemmer(ftOpt.sd, iter);
        }
      }
    }
  }

  /**
   * If called, the original tokens will be returned (including non-fulltext tokens).
   * @return self reference
   */
  public FTLexer original() {
    tokens.original = true;
    return all();
  }

  /**
   * If called, all tokens will be returned (including non-fulltext tokens).
   * @return self reference
   */
  public FTLexer all() {
    tokens.all = true;
    return this;
  }

  /**
   * Initializes the iterator.
   * @return self reference
   */
  public FTLexer init() {
    init(text);
    return this;
  }

  /**
   * Sets the Levenshtein error if it hasn't been assigned yet.
   * @param err error
   * @return self reference
   */
  public FTLexer errors(final int err) {
    if(errors == -1) errors = err;
    return this;
  }

  /**
   * Returns the Levenshtein error for the specified token.
   * @param token token
   * @return error
   */
  public int errors(final byte[] token) {
    return errors > 0 ? errors : token.length >> 2;
  }

  @Override
  public FTLexer init(final byte[] txt) {
    text = txt;
    iter.init(txt);
    return this;
  }

  @Override
  public boolean hasNext() {
    return iter.hasNext();
  }

  @Override
  public FTSpan next() {
    curr = iter.next();
    return curr;
  }

  @Override
  public byte[] nextToken() {
    ctxt = iter.nextToken();
    return ctxt;
  }

  /**
   * Returns total number of tokens.
   * @return token count
   */
  public int count() {
    init();
    int c = 0;
    while(hasNext()) {
      nextToken();
      c++;
    }
    return c;
  }

  @Override
  public IndexType type() {
    return IndexType.FULLTEXT;
  }

  /**
   * Returns the original token. Inherited from {@link IndexSearch};
   * use {@link #next} or {@link #nextToken} if not using this interface.
   * @return current token
   */
  @Override
  public byte[] token() {
    return ctxt != null ? ctxt : curr.text;
  }

  /**
   * Returns the full-text options.
   * @return full-text options (may be {@code null})
   */
  public FTOpt ftOpt() {
    return ftOpt;
  }

  /**
   * Returns if the current token starts a new paragraph. Needed for visualizations.
   * Does not have to be implemented by all tokenizers.
   * Returns false if not implemented.
   * @return boolean
   */
  public boolean paragraph() {
    return tokens.paragraph();
  }

  /**
   * Calculates a position value, dependent on the specified unit.
   * Does not have to be implemented by all tokenizers.
   * @param word word position
   * @param unit unit
   * @return new position ({@code 0} if not implemented)
   */
  public int pos(final int word, final FTUnit unit) {
    return tokens.pos(word, unit);
  }

  /**
   * Gets full-text info for the specified token.
   * Needed for visualizations; see {@link Tokenizer#info()} for more info.
   * @return int arrays or empty array if not implemented
   */
  public int[][] info() {
    return tokens.info();
  }

  /**
   * Returns a new lexer, adopting the tokenizer options.
   * @param opt full-text options
   * @return lexer
   */
  public FTLexer copy(final FTOpt opt) {
    // assign options to text:
    final FTOpt to = ftOpt;
    to.set(ST, opt.is(ST));
    to.set(DC, opt.is(DC));
    to.ln = opt.ln;
    to.th = opt.th;
    to.sd = opt.sd;
    // only change case in insensitive mode
    to.cs = opt.cs != null && opt.cs != FTCase.INSENSITIVE ? FTCase.SENSITIVE :
      FTCase.INSENSITIVE;
    return new FTLexer(to).init(text);
  }

  /**
   * Lists all languages for which tokenizers and stemmers are available.
   * @return supported languages
   */
  public static StringList languages() {
    final TreeMap<Language, Stemmer> langs = new TreeMap<>();
    for(final Stemmer stem : Stemmer.IMPL) {
      for(final Language l : stem.languages()) {
        if(langs.containsKey(l)) continue;
        for(final Tokenizer tknzr : Tokenizer.IMPL) {
          if(tknzr.languages().contains(l)) langs.put(l, stem);
        }
      }
    }
    final StringList sl = new StringList();
    langs.forEach((key, value) -> sl.add(key + " (" + value + ')'));
    return sl.sort();
  }
}
