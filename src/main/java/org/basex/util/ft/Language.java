package org.basex.util.ft;

import static org.basex.util.Token.*;
import java.util.EnumSet;

/**
 * This class contains language tokens which are valid for the xml:lang
 * attribute.
 * <p>
 * As specified by W3C the values of the attribute are language identifiers as
 * defined by IETF BCP 47, Tags for the Identification of Languages.
 *
 * @see <a href="http://www.w3.org/TR/REC-xml/#sec-lang-tag"
 *      >http://www.w3.org/TR/REC-xml/#sec-lang-tag</a>
 * @see <a href="http://tools.ietf.org/html/bcp47"
 *      >http://tools.ietf.org/html/bcp47</a>
 * @see <a href="http://www.iana.org/assignments/language-subtag-registry"
 *      >http://www.iana.org/assignments/language-subtag-registry</a>
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Dimitar Popov
 * @author Jens Erat
 */
public enum Language {
  /** English. */    EN("en", "English"),
  /** Arabic. */     AR("ar", "Arabic"),
  /** Brazilian. */  BR("br", "Brazilian"),
  /** Bulgarian. */  BG("bg", "Bulgarian"),
  /** Chinese. */    ZH("zh", "Chinese", false),
  /** Czech. */      CS("cs", "Czech"),
  /** Danish. */     DA("da", "Danish"),
  /** Dutch. */      NL("nl", "Dutch"),
  /** Finnish. */    FI("fi", "Finnish"),
  /** French. */     FR("fr", "French"),
  /** German. */     DE("de", "German"),
  /** Greek. */      EL("el", "Greek"),
  /** Hungarian. */  HU("hu", "Hungarian"),
  /** Italian. */    IT("it", "Italian"),
  /** Japanese. */   JA("ja", "Japanese", false),
  /** Korean. */     KO("ko", "Korean", false),
  /** Norwegian. */  NO("no", "Norwegian"),
  /** Persian. */    FA("pa", "Persian"),
  /** Portuguese. */ PT("pt", "Portuguese"),
  /** Romanian. */   RO("ro", "Romanian"),
  /** Russian. */    RU("ru", "Russian"),
  /** Spanish. */    ES("es", "Spanish"),
  /** Swedish. */    SV("sv", "Swedish"),
  /** Thai. */       TH("th", "Thai", false),
  /** Turkish. */    TR("tr", "Turkish");

  /** Default language. */
  static final Language DEFAULT = EN;
  /** Language code. */
  final byte[] ln;

  /** Whether language is whitespace-tokenizable (e. g., Chinese is not). */
  private final boolean wsTokenizable;
  /** Language name. */
  private final String name;

  /**
   * Constructor.
   * @param l language code
   * @param n name of language
   */
  private Language(final String l, final String n) {
    this(l, n, true);
  }

  /**
   * Constructor.
   * @param l language code
   * @param n name of language
   * @param ws is language whitespace-tokenizable?
   */
  private Language(final String l, final String n, final boolean ws) {
    ln = token(l);
    name = n;
    wsTokenizable = ws;
  }

  /**
   * Returns all languages which are whitespace-tokenizable.
   * @return all whitespace-tokenizable languages
   */
  public static EnumSet<Language> wsTokenizable() {
    final EnumSet<Language> lns = EnumSet.noneOf(Language.class);
    for(final Language lt : values()) {
      if(lt.wsTokenizable) lns.add(lt);
    }
    return lns;
  }

  /**
   * Returns the code for the specified language, or {@code null}.
   * @param lang name of language
   * @return language code
   */
  public static Language forName(final String lang) {
    for(final Language lt : values()) if(lt.name.equals(lang)) return lt;
    return null;
  }

  /**
   * Get the enumeration value of language represented as token.
   * @param code language code represented as byte array representation
   * @return enum value
   */
  public static Language valueOf(final byte[] code) {
    return Language.valueOf(string(uc(code)));
  }
  
  @Override
  public String toString() {
    return name;
  }
}
