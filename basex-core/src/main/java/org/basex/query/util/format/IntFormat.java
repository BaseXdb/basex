package org.basex.query.util.format;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.query.*;
import org.basex.util.*;

import com.ibm.icu.text.*;

/**
 * Parser for formatting integers.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class IntFormat extends FormatParser {
  /** Whether ICU is available on the class path. */
  private static final boolean IS_ICU_AVAILABLE =
      Reflect.available("com.ibm.icu.text.RuleBasedNumberFormat");
  /** Name or prefix of ICU spell out cardinal RuleSet. */
  private static final String ICU_SPELLOUT_CARDINAL =
      string(Formatter.ICU_SPELLOUT_PREFIX) + "cardinal";
  /** Name or prefix of ICU spell out ordinal RuleSet. */
  private static final String ICU_SPELLOUT_ORDINAL =
      string(Formatter.ICU_SPELLOUT_PREFIX) + "ordinal";

  /** Whether the radix was specified explicitly. */
  private final boolean hasExplicitRadix;

  /**
   * Constructor.
   * @param picture picture
   * @param language language
   * @param info input info (can be {@code null})
   * @throws QueryException query exception
   */
  public IntFormat(final byte[] picture, final byte[] language, final InputInfo info)
      throws QueryException {
    super(info);

    final int sc = lastIndexOf(picture, ';');
    int rc = indexOf(picture, '^');
    if(rc != -1) {
      int xuc = indexOf(picture, 'X', rc + 1);
      int xlc = indexOf(picture, 'x', rc + 1);
      if(sc != -1 && xuc > sc) xuc = -1;
      if(sc != -1 && xlc > sc) xlc = -1;
      if(xuc == -1 && xlc == -1) {
        rc = -1;
      } else {
        radix = toInt(substring(picture, 0, rc));
        if(radix < 2 || radix > 36) {
          rc = -1;
          radix = 10;
        } else if(xuc != -1 && xlc != -1) {
          throw DIFFMAND_X.get(info, picture);
        }
      }
    }
    hasExplicitRadix = rc != -1;

    final byte[] pres = substring(picture, rc + 1, sc == -1 ? picture.length : sc);
    if(pres.length == 0) throw PICEMPTY.get(info, picture);
    finish(presentation(pres, ONE, false));
    if(sc == -1) return;

    // parses the format modifier
    final byte[] mod = substring(picture, sc + 1);

    final TokenParser tp = new TokenParser(mod);
    boolean isIcuRuleSet = false;
    // parse cardinal/ordinal flag
    ordinal = tp.consume('o');
    if(ordinal || tp.consume('c')) {
      final TokenBuilder tb = new TokenBuilder();
      if(tp.consume('(')) {
        while(!tp.consume(')')) {
          if(!tp.more()) throw INVMODIFIER_X.get(info, mod);
          tb.add(tp.next());
        }
        if(tb.isEmpty()) throw INVMODIFIER_X.get(info, mod);
        modifier = tb.finish();
        isIcuRuleSet = startsWith(modifier, Formatter.ICU_SPELLOUT_PREFIX);
        if (!isIcuRuleSet) modifier = Token.delete(modifier, '-');
      }
    }
    useIcu = IS_ICU_AVAILABLE && first == 'w';
    if (useIcu) {
      locale = determineIcuLocale(language);
      if (modifier == null || isIcuRuleSet) {
        modifier = determineIcuRuleSetName(ordinal, modifier, locale);
      }
    }
    else if (isIcuRuleSet) {
      modifier = null;
    }

    // parse alphabetical/traditional flag
    if(!tp.consume('a')) tp.consume('t');
    if(tp.more()) throw INVMODIFIER_X.get(info, mod);
  }

  /**
   * Returns the zero base for the specified code point, or {@code -1}.
   * @param ch character
   */
  @Override
  public int zeroes(final int ch) {
    if(hasExplicitRadix && (ch == 'x' || ch == 'X')) return '0';
    if(radix == 10) return super.zeroes(ch);
    for(int r = 0; r < radix; r++) {
      final int c = DIGITS[r];
      if(ch == c || ch > '9' && ch == uc(c)) return '0';
    }
    return -1;
  }

  /**
   * Checks if a character is a valid digit.
   * @param ch character
   * @param zero zero character
   * @return result of check
   */
  @Override
  public boolean digit(final int ch, final int zero) {
    if(hasExplicitRadix && (ch == 'X' || ch == 'x')) return true;
    if(radix == 10) return super.digit(ch, zero);
    final int num = ch <= '9' ? ch : (ch & 0xDF) - 0x37;
    return ch >= '0' && ch <= '9' || ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' &&
        num < radix;
  }

  /**
   * Determine the locale to be used with ICU number formats.
   * @param language language
   * @return a locale supported by ICU, or the default locale.
   */
  private Locale determineIcuLocale(final byte[] language) {
    final Locale l = Locale.forLanguageTag(string(language));
    Locale[] availableLocales = NumberFormat.getAvailableLocales();
    for (int i = 0; i < availableLocales.length; ++i)
      if (availableLocales[i].getLanguage().equals(l.getLanguage()))
        return availableLocales[i];
    return Locale.forLanguageTag(string(Formatter.EN));
  }

  /**
   * Determine name of rule set for ICU RuleBasedNumberFormat to be used with this format.
   * @param ordinal ordinal flag
   * @param modifier format modifier
   * @param locale locale
   * @return rule set name
   */
  private static byte[] determineIcuRuleSetName(final boolean ordinal,
      final byte[] modifier, final Locale locale) {
    final RuleBasedNumberFormat format =
        new RuleBasedNumberFormat(locale, RuleBasedNumberFormat.SPELLOUT);
    final String m = modifier == null ? "" : string(modifier);
    final String[] ruleSetNames = format.getRuleSetNames();
    String c = null, o = null, cx = null, ox = null;
    for (String n : ruleSetNames) {
      if (n.equals(m)) return token(n);
      if (c == null && n.equals(ICU_SPELLOUT_CARDINAL)) c = n;
      if (cx == null && n.startsWith(ICU_SPELLOUT_CARDINAL)) cx = n;
      if (o == null && n.equals(ICU_SPELLOUT_ORDINAL)) o = n;
      if (ox == null && n.startsWith(ICU_SPELLOUT_ORDINAL)) ox = n;
    }
    if (o == null) o = ox;
    if (c == null) c = cx;
    if (o != null && m.startsWith(ICU_SPELLOUT_ORDINAL)) return token(o);
    if (c != null && m.startsWith(ICU_SPELLOUT_CARDINAL)) return token(c);
    if (o != null && ordinal) return token(o);
    if (c != null && !ordinal) return token(c);
    return token(format.getDefaultRuleSetName());
  }
}
