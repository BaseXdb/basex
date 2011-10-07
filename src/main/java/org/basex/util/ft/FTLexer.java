package org.basex.util.ft;

import java.util.Map.Entry;
import java.util.TreeMap;

import org.basex.index.IndexToken;
import org.basex.io.serial.XMLSerializer;
import org.basex.query.ft.FTFilter;
import org.basex.util.Token;
import org.basex.util.list.StringList;

/**
 * Performs full-text lexing on token. Calls tokenizers, stemmers matching to
 * full-text options to achieve this.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Jens Erat
 */
public final class FTLexer extends FTIterator implements IndexToken {
  /** Tokenizer. */
  private final Tokenizer tok;
  /** Full-text options. */
  private final FTOpt fto;
  /** Text to be tokenized. */
  private byte[] text = Token.EMPTY;

  /** Iterator over result tokens. */
  private FTIterator iter;
  /** The last parsed span. */
  private FTSpan curr;
  /** The last parsed text. */
  private byte[] ctxt;

  /**
   * Constructor, using the default full-text options. Called by the
   * {@link XMLSerializer}, {@link FTFilter}, and the map visualizations.
   */
  public FTLexer() {
    this(null);
  }

  /**
   * Default constructor.
   * @param opt full-text options
   */
  public FTLexer(final FTOpt opt) {
    fto = opt;

    // check if language option is provided:
    Language lang = opt != null ? opt.ln : null;
    if(lang == null) lang = Language.def();

    // use default tokenizer if specific tokenizer is not available.
    Tokenizer tk = Tokenizer.IMPL.getFirst();
    for(final Tokenizer t : Tokenizer.IMPL) {
      if(t.supports(lang)) {
        tk = t;
        break;
      }
    }
    tok = tk.get(opt);
    iter = tok;

    // wrap original iterator
    if(opt != null && opt.is(FTFlag.ST)) {
      if(opt.sd == null) {
        // use default stemmer if specific stemmer is not available.
        Stemmer st = Stemmer.IMPL.getFirst();
        for(final Stemmer stem : Stemmer.IMPL) {
          if(stem.supports(lang)) {
            st = stem;
            break;
          }
        }
        iter = st.get(lang, iter);
      } else {
        iter = new DictionaryStemmer(opt.sd, iter);
      }
    }
  }

  /**
   * Sets the special character flag.
   * Returns not only tokens, but also delimiters.
   * @return self reference
   */
  public FTLexer sc() {
    tok.special = true;
    return this;
  }

  /**
   * Initializes the iterator.
   */
  public void init() {
    init(text);
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
   * Returns the full-text options. Can be {@code null}.
   * @return full-text options
   */
  public FTOpt ftOpt() {
    return fto;
  }

  /**
   * Returns the text to be processed.
   * @return text
   */
  public byte[] text() {
    return text;
  }

  /**
   * Is paragraph? Does not have to be implemented by all tokenizers.
   * Returns false if not implemented.
   * @return boolean
   */
  public boolean paragraph() {
    return tok.paragraph();
  }

  /**
   * Calculates a position value, dependent on the specified unit. Does not have
   * to be implemented by all tokenizers. Returns 0 if not implemented.
   * @param w word position
   * @param u unit
   * @return new position
   */
  public int pos(final int w, final FTUnit u) {
    return tok.pos(w, u);
  }

  /**
   * Gets full-text info for the specified token; needed for visualizations.
   * See {@link Tokenizer#info} for more info.
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
    final TreeMap<Language, Stemmer> langs = new TreeMap<Language, Stemmer>();
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
      sl.add(l.getKey() + " (" + l.getValue() + ")");
    }
    sl.sort(true, true);
    return sl;
  }
}
