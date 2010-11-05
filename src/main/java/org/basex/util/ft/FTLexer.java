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
public final class FTLexer implements Iterator<Span>, Iterable<Span>,
    IndexToken {

  /** Index type. */
  private static final IndexType INDEXTYPE = IndexType.FULLTEXT;
  /** List of available stemmers. */
  private static final LinkedList<SpanProcessor> STEMMERS;
  /** List of available tokenizers. */
  private static final LinkedList<Tokenizer> TOKENIZERS;

  /** Tokenizer. */
  private final Tokenizer tok;
  /** Full-text options. */
  private final FTOpt fto;
  /** database properties. */
  private final Prop prop;
  /** String to be tokenized. */
  private final byte[] text;
  /** Iterator over result tokens. */
  private Iterator<Span> iterator;
  /** The last parsed span. */
  private Span currentSpan;

  /** Load stemmer and tokenizer classes and order them by precedence. */
  static {
    STEMMERS = new LinkedList<SpanProcessor>();
    TOKENIZERS = new LinkedList<Tokenizer>();

    // Built-in stemmers and tokenizers
    STEMMERS.add(new EnglishStemmer());
    TOKENIZERS.add(new WesternTokenizer(null));

    /* SPI / Plug-In processors
     * final ServiceLoader<SpanProcessor> spLoader =
     * ServiceLoader.load(SpanProcessor.class); for(final SpanProcessor sp :
     * spLoader) { switch(sp.type()) { case stemmer: stemmers.add(sp); break;
     * default: break; } }
     */

    try {
      if(SnowballStemmer.available()) {
        STEMMERS.add(new SnowballStemmer(Language.DEFAULT));
      }
      if(WordnetStemmer.available()) {
        STEMMERS.add(new WordnetStemmer(Language.DEFAULT));
      }
    } catch(final QueryException ex) {
      Util.notexpected(ex);
    }

    // sort stemmers and tokenizers by precedence
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
   * @param txt text to analyze
   * @param p database properties
   * @param f full-text options
   */
  public FTLexer(final byte[] txt, final Prop p, final FTOpt f) {
    this(txt, p, f, false);
  }

  /**
   * Constructor. Finds tokenizer and stemmer based on database properties.
   * @param txt text to analyze
   * @param pr database properties
   * @param opt full-text options
   * @param sc include special characters
   */
  public FTLexer(final byte[] txt, final Prop pr, final FTOpt opt,
      final boolean sc) {
    prop = pr;
    fto = opt;
    text = txt;

    // check if language option is provided:
    final byte[] lang;
    final String lstr;
    if(fto != null && fto.ln != null) {
      lang = fto.ln;
    } else if(prop != null && (lstr = pr.get(Prop.FTLANGUAGE)).length() > 0) {
      lang = token(lstr);
    } else {
      lang = Language.DEFAULT.ln;
    }

    // look for matching tokenizer:
    Tokenizer tk = TOKENIZERS.getFirst();
    if(lang != null) {
      for(final Tokenizer t : TOKENIZERS) {
        if(t.supports(lang)) {
          tk = t;
          break;
        }
      }
    }
    tok = tk.get(txt, pr, opt, sc);
    iterator = tok.iterator();

    // check if stemming is required:
    if(opt != null && opt.isSet(ST) && opt.is(ST) && opt.sd == null ||
        opt == null && pr != null && pr.is(Prop.STEMMING)) {

      // look for matching stemmer:
      SpanProcessor sp = STEMMERS.getFirst();
      for(final SpanProcessor stem : STEMMERS) {
        if(stem.supports(lang)) {
          sp = stem;
          break;
        }
      }
      iterator = sp.get(pr, opt).process(iterator);
      // Additional layer for multithreading
      // SpanProcessor queue = new SpanThreadedQueue();
      // iterator = queue.process(iterator);
    } else if(opt != null && opt.isSet(ST) && opt.is(ST) &&
        opt.sd != null) iterator = new DictStemmer(opt.sd).process(iterator);
  }

  /**
   * Copy constructor.
   * @param t Text to tokenize
   * @param copy Instance to copy
   */
  public FTLexer(final byte[] t, final FTLexer copy) {
    this(t, copy.prop, copy.fto, copy.tok.special);
  }

  /**
   * Checks if full text options are provided by the database setup.
   * @param f full text options
   * @return whether full text options are provided
   * @throws QueryException if full text options aren't provided
   */
  public static boolean checkFTOpt(final FTOpt f) throws QueryException {
    // use default language if not provided
    final byte[] lang = f != null && f.ln != null ? f.ln : Language.DEFAULT.ln;

    boolean supp = false;
    // Check tokenizers if language is specified
    for(final Tokenizer t : TOKENIZERS) {
      if(t.supports(lang)) {
        supp = true;
        break;
      }
    }
    // Check stemmers if language is specified (if we use stemming)
    if(supp && f != null && f.isSet(ST) && f.is(ST) &&
        f.sd == null || f == null) {
      supp = false;
      for(final SpanProcessor s : STEMMERS) {
        if(s.supports(lang)) {
          supp = true;
          break;
        }
      }
    }
    if(!supp) Err.FTLAN.thrw(null, lang);
    return supp;
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
   * Returns full text options of FTLexer instance.
   * @return full text options
   */
  public FTOpt ftOpt() {
    return fto;
  }

  /**
   * Get the text currently being parsed.
   * @return byte array representing the text
   */
  public byte[] text() {
    return text;
  }

  /**
   * Gets full-text info for the specified token; needed for visualizations.
   * Does not have to be implemented by all tokenizers.
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
  public int[][] info(final byte[] t) {
    return tok.info(t);
  }

  @Override
  public Iterator<Span> iterator() {
    return new FTLexer(text, prop, fto);
  }

  /**
   * Lists all languages for which tokenizers and stemmers are available.
   * @return supported languages
   */
  public static EnumSet<Language> languages() {
    final EnumSet<Language> ln = EnumSet.noneOf(Language.class);
    for(final Tokenizer t : TOKENIZERS) ln.addAll(t.languages());
    final EnumSet<Language> sln = EnumSet.noneOf(Language.class);
    for(final SpanProcessor stem : STEMMERS) sln.addAll(stem.languages());
    // intersection of languages tokenizers and stemmers support
    ln.retainAll(sln);
    return ln;
  }
}
