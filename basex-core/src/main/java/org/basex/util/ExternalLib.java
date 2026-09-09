package org.basex.util;

/**
 * External libraries that enable optional features if they are found in the classpath.
 * BaseX runs on a plain JDK, and this enum is the single place that answers if a library is
 * available and which of its classes is missing. Its entries are listed by the {@code INFO}
 * command and by {@code db:system}.
 *
 * Conventions for adding a library:
 * <ul>
 * <li> Register the artifact, not a capability: one constant per library, with the class that
 * proves its presence. Version and edition probes (Saxon EE/PE/HE, Xerces 1.0/1.1) stay in the
 * code that needs them.</li>
 * <li> Access the library via {@link Reflect} if only a few members are called; otherwise import
 * it in a separate class that is loaded only after {@link #available} has returned {@code true}.
 * </li>
 * <li> Never resolve a library in a static field of a class on the startup path.</li>
 * <li> Raise {@code BASEX_CLASSPATH_X_X} if the user asked for the feature by name; stay silent
 * if the library only improves an existing feature.</li>
 * </ul>
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum ExternalLib {
  /** Aircompressor: Zstandard decompression. */
  AIRCOMPRESSOR("Aircompressor", "io.airlift.compress.zstd.ZstdInputStream"),
  /** chardet: charset detection heuristics of the Validator.nu HTML parser. */
  CHARDET("chardet", "org.mozilla.intl.chardet.nsICharsetDetectionObserver"),
  /** ICU4J: Unicode collations, graphemes, language-specific formatting. */
  ICU("ICU4J", "com.ibm.icu.text.BreakIterator"),
  /** Igo: Japanese tokenizer. */
  IGO("Igo", "net.reduls.igo.Tagger"),
  /** Jing: RELAX NG validation. */
  JING("Jing", "com.thaiopensource.validate.ValidationDriver"),
  /** JLine: interactive console. */
  JLINE("JLine", "jline.console.ConsoleReader"),
  /** Lucene stemmers: full-text stemming. */
  LUCENE("Lucene Stemmers", "org.apache.lucene.analysis.de.GermanStemmer"),
  /** Markup Blitz: Invisible XML parsing. */
  MARKUP_BLITZ("Markup Blitz", "de.bottlecaps.markup.Blitz", "de.bottlecaps.markup.BlitzException",
      "de.bottlecaps.markup.BlitzParseException", "de.bottlecaps.markup.blitz.ResultHandler"),
  /** Saxon: XSLT 3.0 transformations via the s9api interface. */
  SAXON("Saxon", "net.sf.saxon.s9api.Xslt30Transformer"),
  /** SLF4J: logging facade. */
  SLF4J("SLF4J", "org.slf4j.LoggerFactory"),
  /** Snowball: full-text stemming. */
  SNOWBALL("Snowball", "org.tartarus.snowball.ext.GermanStemmer"),
  /** TagSoup: HTML parsing. */
  TAGSOUP("TagSoup", "org.ccil.cowan.tagsoup.Parser"),
  /** Validator.nu: HTML parsing. */
  VALIDATOR_NU("Validator.nu", "nu.validator.htmlparser.sax.HtmlParser",
      "nu.validator.htmlparser.sax.XmlSerializer",
      "nu.validator.htmlparser.common.XmlViolationPolicy",
      "nu.validator.htmlparser.common.Heuristics"),
  /** WordNet: full-text stemming. */
  WORDNET("WordNet", "edu.mit.jwi.Dictionary"),
  /** Xerces: XSD 1.0 and 1.1 validation. */
  XERCES("Xerces", "org.apache.xerces.jaxp.validation.XMLSchemaFactory"),
  /** XML Resolver: catalog resolution. */
  XML_RESOLVER("XML Resolver", "org.xmlresolver.Resolver");

  /** Library name. */
  private final String name;
  /** Required classes; the first one is the main class. */
  private final String[] classes;
  /** Availability of the library ({@code null} if it has not been checked yet). */
  private volatile Boolean available;

  /**
   * Constructor.
   * @param name library name
   * @param classes required classes
   */
  ExternalLib(final String name, final String... classes) {
    this.name = name;
    this.classes = classes;
  }

  /**
   * Checks if all required classes are found in the classpath.
   * @return result of check
   */
  public boolean available() {
    Boolean avl = available;
    if(avl == null) {
      avl = missing() == null;
      available = avl;
    }
    return avl;
  }

  /**
   * Returns the first required class that is not found in the classpath.
   * @return class name, or {@code null} if the library is available
   */
  public String missing() {
    for(final String clazz : classes) {
      if(Reflect.find(clazz) == null) return clazz;
    }
    return null;
  }

  /**
   * Returns the main class of the library.
   * @return class name
   */
  public String clazz() {
    return classes[0];
  }

  @Override
  public String toString() {
    return name;
  }
}
