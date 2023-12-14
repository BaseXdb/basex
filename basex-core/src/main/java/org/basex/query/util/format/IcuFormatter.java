package org.basex.query.util.format;

import static org.basex.query.util.format.FormatParser.NumeralType.*;
import static org.basex.util.Token.*;

import java.util.*;
import java.util.stream.*;

import org.basex.query.util.format.FormatParser.*;
import org.basex.util.*;
import org.basex.util.hash.*;

import com.ibm.icu.text.*;
import com.ibm.icu.util.*;

/**
 * Language formatter using ICU4J. Can be instantiated via {@link IcuFormatter#get}.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Gunther Rademacher
 */
public final class IcuFormatter extends Formatter {
  /** Whether ICU is available on the class path. */
  private static final boolean AVAILABLE =
      Reflect.available("com.ibm.icu.text.RuleBasedNumberFormat");
  /** Prefix of RuleSet names that are supported by ICU's SPELLOUT format. */
  private static final byte[] ICU_SPELLOUT_PREFIX = token("%spellout-");
  /** Name or prefix of ICU spell out ordinal rule set. */
  private static final String ICU_SPELLOUT_ORDINAL = string(ICU_SPELLOUT_PREFIX) + "ordinal";
  /** Name of ICU spell out ordinal neuter rule set. */
  private static final String ICU_SPELLOUT_ORDINAL_NEUTER = ICU_SPELLOUT_ORDINAL + "-neuter";
  /** Name or prefix of ICU spell out cardinal rule set. */
  private static final String ICU_SPELLOUT_CARDINAL = string(ICU_SPELLOUT_PREFIX) + "cardinal";
  /** Name of ICU spell out cardinal neuter rule set. */
  private static final String ICU_SPELLOUT_CARDINAL_NEUTER = ICU_SPELLOUT_CARDINAL + "-neuter";

  /** IcuFormatter instances. */
  private static final ThreadLocal<TokenObjMap<IcuFormatter>> MAP = new ThreadLocal<>() {
    protected TokenObjMap<IcuFormatter> initialValue() {
      final TokenObjMap<IcuFormatter> map = new TokenObjMap<>();
//    initialize hash map with English formatter as default
      map.put(EN, forLanguage(EN));
      return map;
    }
  };

  /** ICU rule based spell out format. */
  private final RuleBasedNumberFormat spelloutFormat;
  /** ICU rule based spell out format. */
  private final RuleBasedNumberFormat ordinalFormat;
  /** Supported ICU rule set names. */
  private final Set<String> ruleSetNames;
  /** Rule set cache: rule set name by format modifier for each NumeralType. */
  @SuppressWarnings("rawtypes")
  private final TokenObjMap[] ruleSetByModifierForNumType =
      {new TokenObjMap<>(), new TokenObjMap<>(), new TokenObjMap<>()};
  /** Month names. */
  private final byte[][] months;
  /** Weekdays. */
  private final byte[][] days;
  /** AM/PM Markers. */
  private final byte[][] ampm;
  /** Eras: BC, AD. */
  private final byte[][] eras;
  /** Internal formatter used for specific abbreviations of weekdays (can be {@code null}). */
  private final Formatter internal;

  /**
   * Constructor.
   * @param s spellout format
   * @param o ordinal format
   * @param d date format symbols
   * @param internal formatter used for specific abbeviations of weekdays (can be {@code null})
   */
  private IcuFormatter(final RuleBasedNumberFormat s, final RuleBasedNumberFormat o,
      final DateFormatSymbols d, final Formatter internal) {
    spelloutFormat = s;
    ordinalFormat = o;
    ruleSetNames = new LinkedHashSet<>(Arrays.asList(spelloutFormat.getRuleSetNames()));
    this.internal = internal;
    final String[] weekdays = d.getWeekdays();
    days = Stream.concat(Arrays.stream(weekdays).skip(2), Stream.of(weekdays[1])).
        map(n -> token(n)).toArray(byte[][]::new);
    months = Arrays.stream(d.getMonths()).map(n -> token(n)).toArray(byte[][]::new);
    ampm = Arrays.stream(d.getAmPmStrings()).map(n -> token(n)).toArray(byte[][]::new);
    eras = Arrays.stream(d.getEras()).map(n -> token(n)).toArray(byte[][]::new);
  }

  /**
   * Returns a cached formatter instance for the specified language.
   * @param language language
   * @return formatter instance
   */
  public static IcuFormatter get(final byte[] language) {
    final TokenObjMap<IcuFormatter> map = MAP.get();
    final IcuFormatter form;
    if(map.contains(language)) {
      form = map.get(language);
    } else {
      form = forLanguage(language);
      // put null values as well, in order to avoid recalculation
      map.put(language, form);
    }
    return form != null ? form : get(EN);
  }

  /**
   * Constructs a formatter for the specified language, if available.
   * @param language language
   * @return formatter instance, or {@code null} if not available
   */
  private static IcuFormatter forLanguage(final byte[] language) {
    final String lang = string(language);
    if(lang.isBlank()) return null;
    final Locale l = Locale.forLanguageTag(lang);
    final RuleBasedNumberFormat s = new RuleBasedNumberFormat(l, RuleBasedNumberFormat.SPELLOUT);
    final RuleBasedNumberFormat o = new RuleBasedNumberFormat(l, RuleBasedNumberFormat.ORDINAL);
    final DateFormatSymbols d = new DateFormatSymbols(l);
    return s.getLocale(ULocale.ACTUAL_LOCALE).getLanguage().equals(lang) &&
           o.getLocale(ULocale.ACTUAL_LOCALE).getLanguage().equals(lang) &&
           d.getLocale(ULocale.ACTUAL_LOCALE).getLanguage().equals(lang)
        ? new IcuFormatter(s, o, d, getInternal(language))
        : null;
  }

  /**
   * Checks whether a formatter is available for the specified language.
   * @param language language
   * @return true if the language is supported
   */
  public static boolean available(final byte[] language) {
    // instantiate a formatter, if available
    get(language);
    // check map using get rather than contains, as map contains null values
    return MAP.get().get(language) != null;
  }

  /**
   * Checks if the ICU4J library is available.
   * @return result of check
   */
  public static boolean available() {
    return AVAILABLE;
  }

  @Override
  protected byte[] word(final long n, final NumeralType numType, final byte[] modifier) {
    // format number using appropriate rule set
    final String formatted = spelloutFormat.format(n, ruleSet(numType, modifier));
    // remove soft hyphen
    final byte[] result = token(formatted.replace("\u00ad", ""));
    // establish title case
    if (!eq(token(spelloutFormat.getLocale(ULocale.ACTUAL_LOCALE).getLanguage()), EN)) {
      result[0] = (byte) uc(result[0]);
    } else {
      for (int i = 0; i < result.length; ++i) {
        if (i == 0 || result[i - 1] == ' ' || result[i - 1] == '-') {
          result[i] = (byte) uc(result[i]);
        }
      }
    }
    return result;
  }

  @Override
  protected byte[] suffix(final long n, final NumeralType numType) {
    if (numType != ORDINAL) return EMPTY;
    final byte[] f = token(ordinalFormat.format(n));
    int offset = f.length;
    for (int i = offset - 1; i >= 0; --i) {
      if(f[i] >= '0' && f[i] <= '9') {
        offset = i + 1;
        break;
      }
    }
    return substring(f, offset);
  }

  @Override
  protected byte[] month(final int n, final int min, final int max) {
    return format(months[n], min, max);
  }

  @Override
  protected byte[] day(final int n, final int min, final int max) {
    if(internal != null) return internal.day(n, min, max);
    return format(days[n], min, max);
  }

  @Override
  protected byte[] ampm(final boolean am) {
    return ampm[am ? 0 : 1];
  }

  @Override
  protected byte[] calendar() {
    return eras[1];
  }

  @Override
  protected byte[] era(final long year) {
    return eras[year <= 0 ? 0 : 1];
  }

  /**
   * Determine name of rule set for ICU RuleBasedNumberFormat to be used with this format for some
   * given numeral type and format modifier.
   * @param numType numeral type
   * @param modifier format modifier
   * @return rule set name
   */
  private String ruleSet(final NumeralType numType, final byte[] modifier) {
    @SuppressWarnings("unchecked")
    final TokenObjMap<String> map = ruleSetByModifierForNumType[numType.ordinal()];
    final byte[] key = modifier == null ? EMPTY : modifier;
    String ruleSet = map.get(key);
    if (ruleSet == null) {
      if (modifier != null) {
        if (startsWith(modifier, ICU_SPELLOUT_PREFIX)) {
          ruleSet = ruleSet(string(modifier));
        } else {
          // search for result with desired ending by trying all rule sets
          final String suffix = string(delete(modifier, '-'));
          for (final String r : ruleSetNames) {
            if (spelloutFormat.format(1, r).endsWith(suffix)) {
              ruleSet = r;
              break;
            }
          }
        }
      }
      if (ruleSet == null) ruleSet = ruleSet(numType);
      if (ruleSet == null) ruleSet = spelloutFormat.getDefaultRuleSetName();
      map.put(key, ruleSet);
    }
    return ruleSet;
  }

  /**
   * Determine rule set to be used for a given rule set name, by matching that name with supported
   * rule set names.
   * @param name proposed ruleset name
   * @return a supported rule set name, or {@code null} if none available
   */
  private String ruleSet(final String name) {
    // try exact match first
    if(ruleSetNames.contains(name)) return name;
    String ruleSet = null;
    // by  returning null here, test case format-integer-077 could be made to succeed
    for(String r : ruleSetNames) {
      if (name.startsWith(r)) {
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
   * Determine rule set to be used for a given numeral type.
   * @param numType numeral type
   * @return rule set name, or {@code null} if not available
   */
  private String ruleSet(final NumeralType numType) {
    switch(numType) {
      case ORDINAL:
        if(ruleSetNames.contains(ICU_SPELLOUT_ORDINAL)) return ICU_SPELLOUT_ORDINAL;
        if(ruleSetNames.contains(ICU_SPELLOUT_ORDINAL_NEUTER)) return ICU_SPELLOUT_ORDINAL_NEUTER;
        for (String r : ruleSetNames) if(r.startsWith(ICU_SPELLOUT_ORDINAL)) return r;
        break;
      case CARDINAL:
        if(ruleSetNames.contains(ICU_SPELLOUT_CARDINAL)) return ICU_SPELLOUT_CARDINAL;
        if(ruleSetNames.contains(ICU_SPELLOUT_CARDINAL_NEUTER)) return ICU_SPELLOUT_CARDINAL_NEUTER;
        for (String r : ruleSetNames) if(r.startsWith(ICU_SPELLOUT_CARDINAL)) return r;
        break;
      default:
        break;
    }
    return null;
  }
}