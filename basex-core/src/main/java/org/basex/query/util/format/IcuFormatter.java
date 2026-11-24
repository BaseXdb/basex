package org.basex.query.util.format;

import static org.basex.query.util.format.FormatParser.NumeralType.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.util.format.FormatParser.*;
import org.basex.util.hash.*;

import com.ibm.icu.text.*;
import com.ibm.icu.util.*;

/**
 * Language formatter using ICU4J. Can be instantiated via {@link IcuFormatter#get}.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class IcuFormatter extends Formatter {
  /** Prefix of RuleSet names that are supported by ICU's SPELLOUT format. */
  private static final String ICU_SPELLOUT_PREFIX = "%spellout-";
  /** Prefix of RuleSet names, as token. */
  private static final byte[] SPELLOUT = token(ICU_SPELLOUT_PREFIX);
  /** Name or prefix of ICU spell out ordinal rule set. */
  private static final String ICU_SPELLOUT_ORDINAL = ICU_SPELLOUT_PREFIX + "ordinal";
  /** Name or prefix of ICU spell out cardinal rule set. */
  private static final String ICU_SPELLOUT_CARDINAL = ICU_SPELLOUT_PREFIX + "cardinal";
  /** Preferred rule set variants, in descending order. */
  private static final String[] VARIANTS = { "", "-neuter", "-masculine" };

  /** Maximum number of cached language tags per thread. */
  private static final int MAX_CACHED = 100;
  /** IcuFormatter instances. */
  private static final ThreadLocal<TokenObjectMap<IcuFormatter>> MAP =
      ThreadLocal.withInitial(TokenObjectMap::new);

  /** ICU rule based spell out format. */
  private final RuleBasedNumberFormat spelloutFormat;
  /** ICU rule based ordinal format. */
  private final RuleBasedNumberFormat ordinalFormat;
  /** Supported ICU rule set names. */
  private final Set<String> ruleSetNames;
  /** Rule set cache: rule set name by format modifier for each NumeralType. */
  private final EnumMap<NumeralType, TokenObjectMap<String>> ruleSets =
      new EnumMap<>(NumeralType.class);
  /** Internal formatter used for weekdays and ordinal words (can be {@code null}). */
  private final Formatter internal;
  /** Indicates if the spell out format is English. */
  private final boolean english;

  /**
   * Constructor.
   * @param s spellout format
   * @param o ordinal format
   * @param d date format symbols
   * @param internal internal formatter used for weekdays and ordinal words (can be {@code null})
   */
  private IcuFormatter(final RuleBasedNumberFormat s, final RuleBasedNumberFormat o,
      final DateFormatSymbols d, final Formatter internal) {
    super(tokens(d.getMonths()), days(d, internal), tokens(d.getAmPmStrings()),
        tokens(d.getEras()));
    spelloutFormat = s;
    ordinalFormat = o;
    ruleSetNames = new LinkedHashSet<>(Arrays.asList(spelloutFormat.getRuleSetNames()));
    for(final NumeralType numType : NumeralType.values()) {
      ruleSets.put(numType, new TokenObjectMap<>());
    }
    this.internal = internal;
    english = s.getLocale(ULocale.ACTUAL_LOCALE).getLanguage().equals(string(EN));
  }

  /**
   * Returns the names of the weekdays.
   * @param d date format symbols
   * @param internal internal formatter with abbreviated names (can be {@code null})
   * @return names of weekdays (Monday-Sunday), each ordered by length
   */
  private static byte[][][] days(final DateFormatSymbols d, final Formatter internal) {
    if(internal != null) return internal.days;
    // weekdays of ICU start with an empty entry and Sunday
    final byte[][] weekdays = tokens(d.getWeekdays());
    final byte[][][] days = new byte[7][][];
    for(int n = 0; n < 7; n++) days[n] = new byte[][] { weekdays[n < 6 ? n + 2 : 1] };
    return days;
  }

  /**
   * Returns a cached formatter instance for the specified language.
   * @param languageTag language tag
   * @return formatter instance
   */
  public static IcuFormatter get(final byte[] languageTag) {
    final IcuFormatter form = cached(languageTag);
    return form != null ? form : cached(EN);
  }

  /**
   * Returns a cached formatter for the specified language, or creates it.
   * @param languageTag language tag
   * @return formatter instance, or {@code null} if the language is not supported
   */
  private static IcuFormatter cached(final byte[] languageTag) {
    final TokenObjectMap<IcuFormatter> map = MAP.get();
    if(map.contains(languageTag)) return map.get(languageTag);
    // the number of distinct tags is unbounded (private use, extensions): limit the cache size
    if(map.size() >= MAX_CACHED) map.clear();
    // unsupported languages are cached as well, in order to avoid recalculation
    final IcuFormatter form = forLanguage(languageTag);
    map.put(languageTag, form);
    return form;
  }

  /**
   * Constructs a formatter for the specified language, if available.
   * @param languageTag language tag
   * @return formatter instance, or {@code null} if not available
   */
  private static IcuFormatter forLanguage(final byte[] languageTag) {
    final String tag = string(languageTag);
    if(tag.isBlank()) return null;
    final ULocale l = ULocale.forLanguageTag(tag);
    final String lang = l.getLanguage();
    if(lang.isBlank()) return null;
    final RuleBasedNumberFormat s = new RuleBasedNumberFormat(l, RuleBasedNumberFormat.SPELLOUT);
    final RuleBasedNumberFormat o = new RuleBasedNumberFormat(l, RuleBasedNumberFormat.ORDINAL);
    // names of the Gregorian calendar (default calendars of some locales differ: fa, th)
    final DateFormatSymbols d = new DateFormatSymbols(l.setKeywordValue("calendar", "gregorian"));
    return s.getLocale(ULocale.ACTUAL_LOCALE).getLanguage().equals(lang) &&
           o.getLocale(ULocale.ACTUAL_LOCALE).getLanguage().equals(lang) &&
           d.getLocale(ULocale.ACTUAL_LOCALE).getLanguage().equals(lang)
        ? new IcuFormatter(s, o, d, getInternal(languageTag))
        : null;
  }

  /**
   * Checks whether a formatter is available for the specified language.
   * @param languageTag language tag
   * @return true if the language is supported
   */
  public static boolean available(final byte[] languageTag) {
    return cached(languageTag) != null;
  }

  @Override
  protected byte[] word(final long n, final NumeralType numType, final byte[] modifier) {
    // ICU spells some ordinals incorrectly ("zwei millionenste", "cent-et-unième")
    if(numType == ORDINAL && !explicit(modifier) && internal != null && internal.ordinalWords()) {
      return internal.word(n, numType, modifier);
    }
    // format number using appropriate rule set
    final String formatted = spelloutFormat.format(n, ruleSet(numType, modifier));
    // rule sets format numbers outside their range with digits
    for(int f = 0; f < formatted.length(); f++) {
      if(Character.isDigit(formatted.charAt(f))) return null;
    }
    // remove soft hyphen; clone token, as single ASCII characters are cached
    final byte[] result = token(formatted.replace("\u00ad", "")).clone();
    // English: hyphenated words are capitalized as well (other words: see formatInt)
    if(english) {
      for(int i = 1; i < result.length; i++) {
        if(result[i - 1] == '-') result[i] = (byte) uc(result[i]);
      }
    }
    return result;
  }

  @Override
  protected byte[] suffix(final long n, final NumeralType numType) {
    if(numType != ORDINAL) return EMPTY;
    final String f = ordinalFormat.format(n);
    // the suffix follows the last digit (which may be non-ASCII, e.g. with -u-nu-arab)
    int offset = f.length();
    for(int cp; offset > 0 && !Character.isDigit(cp = f.codePointBefore(offset));) {
      offset -= Character.charCount(cp);
    }
    return offset == 0 ? EMPTY : token(f.substring(offset));
  }

  /**
   * Determines name of rule set for ICU RuleBasedNumberFormat to be used with this format for some
   * given numeral type and format modifier.
   * @param numType numeral type
   * @param modifier format modifier (can be {@code null})
   * @return rule set name
   */
  private String ruleSet(final NumeralType numType, final byte[] modifier) {
    final TokenObjectMap<String> map = ruleSets.get(numType);
    final byte[] key = modifier == null ? EMPTY : modifier;
    String ruleSet = map.get(key);
    if(ruleSet == null) {
      if(explicit(modifier)) {
        ruleSet = ruleSet(string(modifier));
      } else if(modifier != null) {
        // search for result with desired ending by trying all rule sets of the numeral type
        final String suffix = string(delete(modifier, '-'));
        final String prefix = prefix(numType);
        for(final String r : ruleSetNames) {
          if(r.startsWith(prefix) && spelloutFormat.format(1, r).endsWith(suffix)) {
            ruleSet = r;
            break;
          }
        }
      }
      if(ruleSet == null) ruleSet = ruleSet(numType);
      if(ruleSet == null) ruleSet = spelloutFormat.getDefaultRuleSetName();
      map.put(key, ruleSet);
    }
    return ruleSet;
  }

  /**
   * Determines rule set to be used for a given rule set name, by matching that name with supported
   * rule set names.
   * @param name proposed ruleset name
   * @return supported rule set name, or {@code null} if none available
   */
  private String ruleSet(final String name) {
    // try exact match first
    if(ruleSetNames.contains(name)) return name;
    String ruleSet = null;
    for(final String r : ruleSetNames) {
      if(name.startsWith(r)) {
        // use closest available more generic rule set,
        // e.g. %spellout-cardinal-feminine in Bosnian
        //  for %spellout-cardinal-feminine-financial
        if(ruleSet == null || r.length() > ruleSet.length()) ruleSet = r;
      }
    }
    if(ruleSet == null) {
      // use ordinal or cardinal spelling, if name indicates it
      // e.g. %spellout-ordinal-feminine in Spanish
      //  for %spellout-ordinal-neuter
      if(name.startsWith(ICU_SPELLOUT_ORDINAL)) ruleSet = ruleSet(ORDINAL);
      else if(name.startsWith(ICU_SPELLOUT_CARDINAL)) ruleSet = ruleSet(CARDINAL);
    }
    return ruleSet;
  }

  /**
   * Determines rule set to be used for a given numeral type.
   * @param numType numeral type
   * @return rule set name, or {@code null} if not available
   */
  private String ruleSet(final NumeralType numType) {
    if(numType == NUMBERING) return null;
    final String prefix = prefix(numType);
    for(final String variant : VARIANTS) {
      final String name = prefix + variant;
      if(ruleSetNames.contains(name)) return name;
    }
    for(final String r : ruleSetNames) if(r.startsWith(prefix)) return r;
    return null;
  }

  /**
   * Checks if the format modifier is the name of an ICU rule set.
   * @param modifier format modifier (can be {@code null})
   * @return result of check
   */
  private static boolean explicit(final byte[] modifier) {
    return modifier != null && startsWith(modifier, SPELLOUT);
  }

  /**
   * Returns the rule set prefix for an ordinal or cardinal numeral type.
   * @param numType numeral type
   * @return prefix
   */
  private static String prefix(final NumeralType numType) {
    return numType == ORDINAL ? ICU_SPELLOUT_ORDINAL : ICU_SPELLOUT_CARDINAL;
  }

  /**
   * Returns decimal-format properties for the given language.
   * @param languageTag language tag
   * @return properties, or {@code null} if the language is not supported
   */
  static DecFormatOptions decFormat(final String languageTag) {
    final ULocale locale = ULocale.forLanguageTag(languageTag);
    final DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(locale);
    if(dfs.getLocale(ULocale.ACTUAL_LOCALE).toLanguageTag().equals(languageTag)) {
      final DecFormatOptions dfo = new DecFormatOptions();
      dfo.putMarker(DecFormatOptions.DECIMAL_SEPARATOR, dfs.getDecimalSeparatorString(), '.');
      dfo.put(DecFormatOptions.DIGIT, String.valueOf(dfs.getDigit()));
      dfo.putMarker(DecFormatOptions.GROUPING_SEPARATOR, dfs.getGroupingSeparatorString(), ',');
      dfo.putMarker(DecFormatOptions.EXPONENT_SEPARATOR, dfs.getExponentSeparator(), 'e');
      dfo.put(DecFormatOptions.INFINITY, dfs.getInfinity());
      dfo.put(DecFormatOptions.MINUS_SIGN, dfs.getMinusSignString());
      dfo.put(DecFormatOptions.NAN, dfs.getNaN());
      dfo.put(DecFormatOptions.PATTERN_SEPARATOR, String.valueOf(dfs.getPatternSeparator()));
      dfo.putMarker(DecFormatOptions.PERCENT, dfs.getPercentString(), '%');
      dfo.putMarker(DecFormatOptions.PER_MILLE, dfs.getPerMillString(), '‰');
      dfo.put(DecFormatOptions.ZERO_DIGIT, String.valueOf(dfs.getZeroDigit()));
      return dfo;
    }
    return null;
  }
}