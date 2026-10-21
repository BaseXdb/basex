package org.basex.util;

import java.util.function.*;

/**
 * External libraries that enable optional features if they are found in the classpath.
 * BaseX runs on a plain JDK, and this enum is the single place that answers if a library is
 * available and which requirement is missing. Its entries are listed by the {@code INFO}
 * command and by {@code db:system}.
 *
 * Conventions for adding a library:
 * <ul>
 * <li> Register the artifact, not a capability: one constant per library, with the class that
 * proves its presence. Add a compatibility check if class presence alone cannot guarantee the
 * required API.</li>
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
  /** Aircompressor: Zstandard compression. */
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
  MARKUP_BLITZ("Markup Blitz", ExternalLib::markupBlitz, "de.bottlecaps.markup.Blitz",
      "de.bottlecaps.markup.BlitzException", "de.bottlecaps.markup.BlitzParseException",
      "de.bottlecaps.markup.blitz.ResultHandler", "de.bottlecaps.markup.blitz.Version"),
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
  XML_RESOLVER("XML Resolver", "org.xmlresolver.Resolver"),
  /** XZ for Java: XZ compression. */
  XZ("XZ for Java", "org.tukaani.xz.XZInputStream");

  /** Library name. */
  private final String name;
  /** Required classes; the first one is the main class. */
  private final String[] classes;
  /** Additional compatibility check (can be {@code null}). */
  private final Supplier<String> checker;
  /** Availability of the library ({@code null} if it has not been checked yet). */
  private volatile Boolean available;
  /** Missing requirement (can be {@code null}). */
  private String missing;

  /**
   * Constructor.
   * @param name library name
   * @param classes required classes
   */
  ExternalLib(final String name, final String... classes) {
    this(name, null, classes);
  }

  /**
   * Constructor.
   * @param name library name
   * @param checker additional compatibility check; returns a missing requirement, or {@code null}
   * @param classes required classes
   */
  ExternalLib(final String name, final Supplier<String> checker, final String... classes) {
    this.name = name;
    this.checker = checker;
    this.classes = classes;
  }

  /**
   * Checks if all requirements are satisfied.
   * @return result of check
   */
  public boolean available() {
    Boolean avl = available;
    if(avl == null) {
      missing = check();
      avl = missing == null;
      available = avl;
    }
    return avl;
  }

  /**
   * Returns the first missing requirement.
   * @return missing requirement, or {@code null} if the library is available
   */
  public String missing() {
    available();
    return missing;
  }

  /**
   * Returns the first missing requirement.
   * @return missing requirement, or {@code null} if the library is available
   */
  private String check() {
    for(final String clazz : classes) {
      if(Reflect.find(clazz) == null) return clazz;
    }
    return checker != null ? checker.get() : null;
  }

  /**
   * Checks the Markup Blitz version.
   * @return missing requirement, or {@code null} if the version is supported
   */
  private static String markupBlitz() {
    final String clazz = "de.bottlecaps.markup.blitz.Version";
    try {
      final Object value = Reflect.find(clazz).getField("VALUE").get(null);
      if(value instanceof final String version) {
        // ignore qualifiers such as "-SNAPSHOT"
        final Version ver = new Version(version.replaceAll("[^0-9.].*", ""));
        if(ver.compareTo(new Version("1.13")) >= 0 && ver.compareTo(new Version("2")) < 0) {
          return null;
        }
      }
    } catch(final Throwable ex) {
      Util.debug(ex);
    }
    return clazz + " (1.13 <= version < 2)";
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
