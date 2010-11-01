package org.basex.util.ft;

import static org.basex.util.Token.*;
import static org.basex.util.ft.FTOptions.*;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import org.basex.core.Prop;
import org.basex.index.IndexToken;
import org.basex.query.QueryException;
import org.basex.query.ft.FTOpt;
import org.basex.query.util.Err;
import org.basex.util.Util;

/**
 * Performs full-text lexing on token. Calls tokenizers, stemmers matching to
 * full-text options to achieve this.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Jens Erat
 */
public class FTLexer implements Iterator<Span>, Iterable<Span>, IndexToken {
  /** Index type. */
  private static final IndexType INDEXTYPE = IndexType.FULLTEXT;
  /** List of available stemmers. */
  private static final LinkedList<SpanProcessor> STEMMERS;
  /** List of available tokenizers. */
  private static final LinkedList<Tokenizer> TOKENIZERS;

  /** Tokenizer. */
  private final Tokenizer tokenizer;
  /** Full-text options. */
  private final FTOpt fto;
  /** database properties. */
  private final Prop pr;
  /** String to be tokenized. */
  private final byte[] txt;
  /** Iterator over result tokens. */
  private Iterator<Span> iterator;
  /** The last parsed span. */
  private Span currentSpan;

  /** Load stemmer and tokenizer classes and order them by precedence. */
  static {
    STEMMERS = new LinkedList<SpanProcessor>();
    TOKENIZERS = new LinkedList<Tokenizer>();

    // Built-in stemmers and tokenizers
    STEMMERS.add(new BaseXStemmer());
    TOKENIZERS.add(new BaseXTokenizer(null));

    /* SPI / Plug-In processors
     * final ServiceLoader<SpanProcessor> spLoader =
     * ServiceLoader.load(SpanProcessor.class); for(final SpanProcessor sp :
     * spLoader) { switch(sp.getType()) { case stemmer: stemmers.add(sp); break;
     * default: break; } }
     */

    if(SnowballStemmer.isAvailable()) {
      try {
        STEMMERS.add(new SnowballStemmer(LanguageTokens.DEFAULT));
      } catch(final QueryException e) {
        ;
      }
    }

    if(WordnetStemmer.isAvailable()) {
      try {
        STEMMERS.add(new WordnetStemmer(LanguageTokens.DEFAULT));
      } catch(final QueryException e) {
        ;
      }
    }

    // sort stemmers, tokenizers by precedence
    Collections.sort(STEMMERS);
    Collections.sort(TOKENIZERS);
  }

  /**
   * Constructor.
   * @param t text to analyze
   */
  public FTLexer(final byte[] t) {
    this(t, null, null);
  }

  /**
   * Constructor.
   * @param t text to analyze
   * @param p database properties
   */
  public FTLexer(final byte[] t, final Prop p) {
    this(t, p, null);
  }

  /**
   * Constructor. Finds tokenizer and stemmer based on database properties.
   * @param t text to analyze
   * @param p database properties
   * @param f full-text options
   */
  public FTLexer(final byte[] t, final Prop p, final FTOpt f) {
    this(t, p, f, false);
  }

  /**
   * Constructor. Finds tokenizer and stemmer based on database properties.
   * @param t text to analyze
   * @param p database properties
   * @param f full-text options
   * @param sc include special characters
   */
  public FTLexer(final byte[] t, final Prop p, final FTOpt f,
      final boolean sc) {
    pr = p;
    fto = f;
    txt = t;

    // check if language option is provided:
    final byte[] lang;
    final String lstr;
    if(fto != null && fto.ln != null) {
      lang = fto.ln;
    } else if(pr != null && (lstr = p.get(Prop.FTLANGUAGE)).length() > 0) {
      lang = token(lstr);
    } else {
      lang = LanguageTokens.DEFAULT.ln;
    }

    // look for matching tokenizer:
    Tokenizer tk = TOKENIZERS.getFirst();
    if(lang != null) {
      for(final Tokenizer tok : TOKENIZERS) {
        if(tok.isLanguageSupported(lang)) {
          tk = tok;
          break;
        }
      }
    }
    tokenizer = tk.newInstance(t, p, f, sc);
    iterator = tokenizer.iterator();

    // check if stemming is required:
    if(f != null && f.isSet(ST) && f.is(ST) && f.sd == null ||
        f == null && p != null && p.is(Prop.STEMMING)) {

      // look for matching stemmer:
      SpanProcessor sp = STEMMERS.getFirst();
      for(final SpanProcessor stem : STEMMERS) {
        if(stem.isLanguageSupported(lang)) {
          sp = stem;
          break;
        }
      }
      iterator = sp.newInstance(p, f).process(iterator);
      // Additional layer for multithreading
      // SpanProcessor queue = new SpanThreadedQueue();
      // iterator = queue.process(iterator);
    } else if(f != null && f.isSet(ST) && f.is(ST) &&
        f.sd != null) iterator = new DictionaryStemmer(f.sd).process(iterator);
  }

  /**
   * Copy constructor.
   * @param t Text to tokenize
   * @param copy Instance to copy
   */
  public FTLexer(final byte[] t, final FTLexer copy) {
    this(t, copy.pr, copy.fto, copy.tokenizer.specialChars);
  }

  /**
   * Checks if full text options are provided by the database setup.
   * @param f full text options
   * @return whether full text options are provided
   * @throws QueryException if full text options aren't provided
   */
  public static boolean checkFTOpt(final FTOpt f) throws QueryException {
    // use default language if not provided
    final byte[] language = f != null && f.ln != null ? f.ln :
      LanguageTokens.DEFAULT.ln;

    boolean supported = false;
    // Check tokenizers if language is specified
    for(final Tokenizer tok : TOKENIZERS) {
      if(tok.isLanguageSupported(language)) {
        supported = true;
        break;
      }
    }
    // Check stemmers if language is specified (if we use stemming)
    if(supported && f != null && f.isSet(ST) && f.is(ST) &&
        f.sd == null || f == null) {
      supported = false;
      for(final SpanProcessor s : STEMMERS) {
        if(s.isLanguageSupported(language)) {
          supported = true;
          break;
        }
      }
    }
    if(!supported) throw new QueryException(null, Err.FTLAN, language);
    return supported;
  }

  /**
   * Returns total number of tokens.
   * @return token count
   */
  public int count() {
    // calculate all tokens and count them. caching them would be more efficient
    // if they would be used after calling count() - not done currently. On the
    // other hand, caching would add some memory overhead.
    final Iterator<Span> it = iterator();
    int count = 0;
    while(it.hasNext()) {
      it.next();
      count++;
    }
    return count;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public Span next() {
    currentSpan = iterator.next();
    return currentSpan;
  }

  @Override
  public void remove() {
    Util.notimplemented();
  }

  @Override
  public IndexType type() {
    return INDEXTYPE;
  }

  /**
   * Returns the current token. Inherited from IndexToken. Use next() if not
   * using this interface.
   * @return Current token.
   */
  @Override
  public byte[] get() {
    return currentSpan.txt;
  }

  /**
   * Is paragraph? Must not be implemented by all tokenizers. Returns false if
   * not implemented.
   * @return boolean
   */
  public boolean isParagraph() {
    return tokenizer.isParagraph();
  }

  /**
   * Calculates a position value, dependent on the specified unit. Must not be
   * implemented by all tokenizers. Returns 0 if not implemented.
   * @param w word position
   * @param u unit
   * @return new position
   */
  public int pos(final int w, final FTUnit u) {
    return tokenizer.pos(w, u);
  }

  /**
   * Returns full text options of FTLexer instance.
   * @return full text options
   */
  public FTOpt getFTOpt() {
    return fto;
  }

  /**
   * Get the text currently being parsed.
   * @return byte array representing the text
   */
  public byte[] getText() {
    return txt;
  }

  /**
   * Gets full-text info for the specified token; needed for visualizations.
   * Must not be implemented by all tokenizers.
   * <ul>
   * <li/>int[0]: length of each token
   * <li/>int[1]: sentence info, length of each sentence
   * <li/>int[2]: paragraph info, length of each parap.get(Prop.FTLANGUAGE))
   * graph
   * <li/>int[3]: each token as int[]
   * <li/>int[4]: punctuation marks of each sentence
   * </ul>
   * @param t text to be parsed
   * @return int arrays or empty array if not implemented
   */
  public int[][] getInfo(final byte[] t) {
    return tokenizer.getInfo(t);
  }

  @Override
  public Iterator<Span> iterator() {
    return new FTLexer(txt, pr, fto);
  }

  /**
   * Lists all languages for which tokenizers and stemmers are available.
   * @return supported languages
   */
  public EnumSet<LanguageTokens> supportedLanguages() {
    final EnumSet<LanguageTokens> tokLN = EnumSet.noneOf(LanguageTokens.class);
    for(final Tokenizer tok : TOKENIZERS) {
      tokLN.addAll(tok.supportedLanguages());
    }
    final EnumSet<LanguageTokens> stemLN = EnumSet.noneOf(LanguageTokens.class);
    for(final SpanProcessor stem : STEMMERS) {
      stemLN.addAll(stem.supportedLanguages());
    }
    // intersection of languages tokenizers and stemmers support
    tokLN.retainAll(stemLN);
    return tokLN;
  }

  /** Units. */
  public enum FTUnit {
    /** Word unit. */
    WORD,
    /** Sentence unit. */
    SENTENCE,
    /** Paragraph unit. */
    PARAGRAPH;

    /**
     * Returns a string representation.
     * @return string representation
     */
    @Override
    public String toString() {
      return name().toLowerCase();
    }
  }
}
