package org.basex.util.ft;

import static org.basex.util.Token.*;

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
  /** English. */    EN("English"),
  /** Arabic. */     AR("Arabic"),
  /** Brazilian. */  BR("Brazilian"),
  /** Bulgarian. */  BG("Bulgarian"),
  /** Chinese. */    ZH("Chinese", false),
  /** Czech. */      CS("Czech"),
  /** Danish. */     DA("Danish"),
  /** Dutch. */      NL("Dutch"),
  /** Finnish. */    FI("Finnish"),
  /** French. */     FR("French"),
  /** German. */     DE("German"),
  /** Greek. */      EL("Greek"),
  /** Hungarian. */  HU("Hungarian"),
  /** Italian. */    IT("Italian"),
  /** Japanese. */   JA("Japanese", false),
  /** Korean. */     KO("Korean", false),
  /** Norwegian. */  NO("Norwegian"),
  /** Persian. */    FA("Persian"),
  /** Portuguese. */ PT("Portuguese"),
  /** Romanian. */   RO("Romanian"),
  /** Russian. */    RU("Russian"),
  /** Spanish. */    ES("Spanish"),
  /** Swedish. */    SV("Swedish"),
  /** Thai. */       TH("Thai", false),
  /** Turkish. */    TR("Turkish");

  /** Default language. */
  public static final Language DEFAULT = EN;

  /** Whether language uses white-spaces (e. g., Chinese does not). */
  final boolean ws;
  /** Full name. */
  private final String full;

  /**
   * Constructor.
   * @param n name of language
   */
  private Language(final String n) {
    this(n, true);
  }

  /**
   * Constructor.
   * @param f full name of language
   * @param w is language whitespace-tokenizable?
   */
  private Language(final String f, final boolean w) {
    full = f;
    ws = w;
  }

  /**
   * Returns the enumeration value of the specified language, or {@code null}.
   * @param lang name or code of language
   * @return language code
   */
  public static Language get(final String lang) {
    for(final Language lt : values()) {
      if(lang.equalsIgnoreCase(lt.full) ||
         lang.equalsIgnoreCase(lt.name())) return lt;
    }
    return null;
  }

  /**
   * Returns the enumeration value of the specified language, or {@code null}.
   * @param lang name or code of language
   * @return enum value
   */
  public static Language get(final byte[] lang) {
    return Language.get(string(uc(lang)));
  }

  @Override
  public String toString() {
    return full;
  }
}
