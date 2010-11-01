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
public enum LanguageTokens {
  /** Arabic. */     AR("ar"),
  /** Bulgarian. */  BG("bg"),
  /** Brazilian. */  BR("br"),
  /** Czech. */      CS("cs"),
  /** Danish. */     DA("da"),
  /** German. */     DE("de"),
  /** Greek. */      EL("el"),
  /** English. */    EN("en"),
  /** Spanish. */    ES("es"),
  /** Persian. */    FA("pa"),
  /** Finnish. */    FI("fi"),
  /** French. */     FR("fr"),
  /** Hungarian. */  HU("hu"),
  /** Italian. */    IT("it"),
  /** Japanese. */   JA("ja", false),
  /** Korean. */     KO("ko", false),
  /** Dutch. */      NL("nl"),
  /** Norwegian. */  NO("no"),
  /** Portuguese. */ PT("pt"),
  /** Romanian. */   RO("ro"),
  /** Russian. */    RU("ru"),
  /** Swedish. */    SV("sv"),
  /** Turkish. */    TR("tr"),
  /** Thai. */       TH("th", false),
  /** Chinese. */    ZH("zh", false);

  /** Default language. */
  static final LanguageTokens DEFAULT = EN;
  /** Language code. */
  final byte[] ln;
  /** Whether language is whitespace-tokenizable (e. g., Chinese is not). */
  private final boolean wsTokenizable;

  /**
   * Returns all languages which are whitespace-tokenizable.
   * @return all whitespace-tokenizable languages
   */
  static EnumSet<LanguageTokens> wsTokenizable() {
    final EnumSet<LanguageTokens> lns = EnumSet.noneOf(LanguageTokens.class);
    for(final LanguageTokens lt : values()) {
      if(lt.wsTokenizable) lns.add(lt);
    }
    return lns;
  }

  /**
   * Get the enumeration value of language represented as token.
   * @param code language code represented as byte array representation
   * @return enum value
   */
  static LanguageTokens valueOf(final byte[] code) {
    return LanguageTokens.valueOf(string(uc(code)));
  }

  /**
   * Constructor.
   * @param l language code
   */
  private LanguageTokens(final String l) {
    this(l, true);
  }

  /**
   * Constructor.
   * @param l language code
   * @param ws is language whitespace-tokenizable?
   */
  private LanguageTokens(final String l, final boolean ws) {
    ln = token(l);
    wsTokenizable = ws;
  }
}
