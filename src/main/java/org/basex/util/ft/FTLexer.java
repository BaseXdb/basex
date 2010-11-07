package org.basex.util.ft;

import java.util.EnumSet;
import org.basex.core.Prop;
import org.basex.data.XMLSerializer;
import org.basex.index.FTBuilder;
import org.basex.index.IndexToken;
import org.basex.query.ft.FTFilter;

/**
 * Performs full-text lexing on token. Calls tokenizers, stemmers matching to
 * full-text options to achieve this.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Jens Erat
 */
public final class FTLexer extends FTIterator implements IndexToken {
  /** Tokenizer. */
  private final Tokenizer tok;
  /** Full-text options. */
  private final FTOpt fto;
  /** Text to be tokenized. */
  private byte[] text;

  /** Iterator over result tokens. */
  private FTIterator iter;
  /** The last parsed span. */
  private Span curr;
  /** The last parsed text. */
  private byte[] ctxt;

  /**
   * Constructor. Called by the {@link FTBuilder}.
   * @param p database properties
   */
  public FTLexer(final Prop p) {
    this(null, p, null, false);
  }

  /**
   * Constructor. Called by the {@link XMLSerializer} and {@link FTFilter}.
   * @param t text to analyze
   */
  public FTLexer(final byte[] t) {
    this(t, null, null, false);
  }

  /**
   * Constructor. Called by the map visualization.
   * @param t text to analyze
   * @param p database properties
   */
  public FTLexer(final byte[] t, final Prop p) {
    this(t, p, null, false);
  }

  /**
   * Default constructor.
   * @param txt text to analyze
   * @param p database properties
   * @param f full-text options
   */
  public FTLexer(final byte[] txt, final Prop p, final FTOpt f) {
    this(txt, p, f, false);
  }

  /**
   * Constructor. Called by the map visualization.
   * @param txt text to analyze
   * @param pr database properties
   * @param opt full-text options
   * @param sc include special characters
   */
  public FTLexer(final byte[] txt, final Prop pr, final FTOpt opt,
      final boolean sc) {

    fto = opt;
    text = txt;

    // check if language option is provided:
    Language lang = opt != null ? opt.ln : pr != null ?
        Language.get(pr.get(Prop.LANGUAGE)) : null;
    if(lang == null) lang = Language.DEFAULT;

    // use default tokenizer if specific tokenizer is not available.
    Tokenizer tk = Tokenizer.IMPL.getFirst();
    if(lang != null) {
      for(final Tokenizer t : Tokenizer.IMPL) {
        if(t.supports(lang)) {
          tk = t;
          break;
        }
      }
    }
    tok = tk.get(txt, pr, opt, sc);
    iter = tok.iter();

    // check if stemming is required:
    if(opt != null ? opt.is(FTFlag.ST) : pr != null && pr.is(Prop.STEMMING)) {
      if(opt == null || opt.sd == null) {
        // use default stemmer if specific stemmer is not available.
        Stemmer st = Stemmer.IMPL.getFirst();
        for(final Stemmer stem : Stemmer.IMPL) {
          if(stem.supports(lang)) {
            st = stem;
            break;
          }
        }
        iter = st.get(lang).iter(iter);
      } else {
        iter = new DictStemmer(opt.sd).iter(iter);
      }
    }
  }

  /**
   * Returns total number of tokens.
   * @return token count
   */
  public int count() {
    init(text);
    int c = 0;
    while(hasNext()) {
      nextToken();
      c++;
    }
    return c;
  }

  @Override
  public void init(final byte[] txt) {
    text = txt;
    iter.init(txt);
  }

  @Override
  public boolean hasNext() {
    return iter.hasNext();
  }

  @Override
  public Span next() {
    curr = iter.next();
    return curr;
  }

  @Override
  public byte[] nextToken() {
    ctxt = iter.nextToken();
    return ctxt;
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
   * @return int arrays or empty array if not implemented
   */
  public int[][] info() {
    return tok.info();
  }

  /**
   * Lists all languages for which tokenizers and stemmers are available.
   * @return supported languages
   */
  public static EnumSet<Language> languages() {
    final EnumSet<Language> ln = EnumSet.noneOf(Language.class);
    for(final Tokenizer t : Tokenizer.IMPL) ln.addAll(t.languages());
    final EnumSet<Language> sln = EnumSet.noneOf(Language.class);
    for(final Stemmer stem : Stemmer.IMPL) sln.addAll(stem.languages());
    // intersection of languages tokenizers and stemmers support
    ln.retainAll(sln);
    return ln;
  }
}
