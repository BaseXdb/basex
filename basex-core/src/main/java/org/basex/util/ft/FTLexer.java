package org.basex.util.ft;

import java.util.Map.Entry;
import java.util.*;

import org.basex.index.*;
import org.basex.index.query.*;
import org.basex.query.expr.ft.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Performs full-text lexing on token. Calls tokenizers, stemmers matching to
 * full-text options to achieve this.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Jens Erat
 */
public final class FTLexer extends FTIterator implements IndexToken {
  /** Tokenizer. */
  private final Tokenizer tok;
  /** Full-text options. */
  private final FTOpt ftopt;
  /** Text to be tokenized. */
  private byte[] text = Token.EMPTY;

  /** Levenshtein error. */
  private int lserror;

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
   * @param ftopt full-text options
   */
  public FTLexer(final FTOpt ftopt) {
    this.ftopt = ftopt;

    // check if language option is provided:
    Language lang = ftopt != null ? ftopt.ln : null;
    if(lang == null) lang = Language.def();

    // use default tokenizer if specific tokenizer is not available.
    Tokenizer tk = Tokenizer.IMPL.get(0);
    for(final Tokenizer t : Tokenizer.IMPL) {
      if(t.supports(lang)) {
        tk = t;
        break;
      }
    }
    tok = tk.get(ftopt);
    iter = tok;

    // wrap original iterator
    if(ftopt != null && ftopt.is(FTFlag.ST)) {
      if(ftopt.sd == null) {
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
        iter = new DictionaryStemmer(ftopt.sd, iter);
      }
    }
  }

  /**
   * If called, the original tokens will be returned (including non-fulltext tokens).
   * @return self reference
   */
  public FTLexer original() {
    tok.original = true;
    return all();
  }

  /**
   * If called, all tokens will be returned (including non-fulltext tokens).
   * @return self reference
   */
  public FTLexer all() {
    tok.all = true;
    return this;
  }

  /**
   * Initializes the iterator.
   */
  public void init() {
    init(text);
  }

  /**
   * Sets the Levenshtein error.
   * @param ls error
   */
  public void lserror(final int ls) {
    lserror = ls;
  }

  /**
   * Returns the Levenshtein error for the specified token.
   * @param token token
   * @return error
   */
  public int lserror(final byte[] token) {
    return lserror == 0 ? token.length >> 2 : lserror;
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
   * Returns the original token. Inherited from {@link IndexToken};
   * use {@link #next} or {@link #nextToken} if not using this interface.
   * @return current token.
   */
  @Override
  public byte[] get() {
    return ctxt != null ? ctxt : curr.text;
  }

  /**
   * Returns the full-text options.
   * @return full-text options (may be {@code null})
   */
  public FTOpt ftOpt() {
    return ftopt;
  }

  /**
   * Returns the text to be processed.
   * @return text
   */
  public byte[] text() {
    return text;
  }

  /**
   * Returns if the current token starts a new paragraph. Needed for visualizations.
   * Does not have to be implemented by all tokenizers.
   * Returns false if not implemented.
   * @return boolean
   */
  public boolean paragraph() {
    return tok.paragraph();
  }

  /**
   * Calculates a position value, dependent on the specified unit. Does not have
   * to be implemented by all tokenizers. Returns 0 if not implemented.
   * @param word word position
   * @param unit unit
   * @return new position
   */
  public int pos(final int word, final FTUnit unit) {
    return tok.pos(word, unit);
  }

  /**
   * Gets full-text info for the specified token.
   * Needed for visualizations; see {@link Tokenizer#info()} for more info.
   * @return int arrays or empty array if not implemented
   */
  public int[][] info() {
    return tok.info();
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
        for(final Tokenizer t : Tokenizer.IMPL) {
          if(t.languages().contains(l)) langs.put(l, stem);
        }
      }
    }
    final StringList sl = new StringList();
    for(final Entry<Language, Stemmer> l : langs.entrySet()) {
      sl.add(l.getKey() + " (" + l.getValue() + ')');
    }
    return sl.sort();
  }
}
