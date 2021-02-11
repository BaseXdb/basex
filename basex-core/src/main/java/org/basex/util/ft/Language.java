package org.basex.util.ft;

import java.util.*;

import org.basex.core.*;

/**
 * This class contains language tokens which are valid for the xml:lang
 * attribute.
 * As specified by W3C, the values of the attribute are language identifiers as
 * defined by IETF BCP 47, Tags for the Identification of Languages.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 * @author Jens Erat
 *
 * @see <a href="http://www.w3.org/TR/REC-xml/#sec-lang-tag"
 *      >http://www.w3.org/TR/REC-xml/#sec-lang-tag</a>
 * @see <a href="http://tools.ietf.org/html/bcp47"
 *      >http://tools.ietf.org/html/bcp47</a>
 * @see <a href="http://www.iana.org/assignments/language-subtag-registry"
 *      >http://www.iana.org/assignments/language-subtag-registry</a>
 */
public final class Language implements Comparable<Language> {
  /** Available languages, indexed by language code. */
  static final HashMap<String, Language> ALL = new HashMap<>();
  /** Available languages, indexed by their display. */
  private static final HashMap<String, Language> DISP = new HashMap<>();
  /** Locale. */
  private final Locale locale;

  static {
    for(final Locale l : Locale.getAvailableLocales()) {
      ALL.put(l.getLanguage(), new Language(l));
      DISP.put(l.getDisplayLanguage(Locale.ENGLISH), new Language(l));
    }
  }

  /**
   * Private Constructor.
   * @param locale locale
   */
  private Language(final Locale locale) {
    this.locale = locale;
  }

  /**
   * Returns an instance for the specified language code or {@code null}.
   * @param lang name or code of language
   * @return language code
   */
  public static Language get(final String lang) {
    final int i = lang.indexOf('-');
    final String l = i == -1 ? lang : lang.substring(0, i);
    final Language ln = ALL.get(l.toLowerCase(Locale.ENGLISH));
    return ln == null ? DISP.get(lang) : ln;
  }

  /**
   * Returns an instance for the current language option, or English as default language.
   * @param opts database options
   * @return language code
   */
  public static Language get(final MainOptions opts) {
    final Language ln = get(opts.get(MainOptions.LANGUAGE));
    return ln == null ? get("en") : ln;
  }

  /**
   * Returns the user language as default language, or English if the language cannot be assigned.
   * @return default language
   */
  public static Language def() {
    final Language ln = DISP.get(MainOptions.LANGUAGE.value());
    return ln == null ? get("en") : ln;
  }

  /**
   * Returns the language code (ISO 639).
   * @return code
   */
  public String code() {
    return locale.getLanguage();
  }

  @Override
  public boolean equals(final Object obj) {
    return this == obj || obj instanceof Language && code().equals(((Language) obj).code());
  }

  @Override
  public int hashCode() {
    return code().hashCode();
  }

  @Override
  public int compareTo(final Language o) {
    return code().compareTo(o.code());
  }

  @Override
  public String toString() {
    return locale.getDisplayLanguage(Locale.ENGLISH);
  }
}
