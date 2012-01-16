package org.basex.query.util;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.basex.query.QueryException;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * Regular expression class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class RegEx {
  /** Classes pattern. */
  private static final Pattern CLASSES =
    Pattern.compile(".*?\\[([a-zA-Z])-([a-zA-Z]).*");
  /** Excluded classes pattern. */
  private static final Pattern EXCLUDE =
    Pattern.compile(".*?\\[(.*?)-\\[(.*?)\\].*");
  /** Input info. */
  private final InputInfo input;
  /** Input pattern. */
  private String pattern;

  /**
   * Constructor.
   * @param pat input pattern
   * @param ii input info
   */
  public RegEx(final String pat, final InputInfo ii) {
    pattern = pat;
    input = ii;
  }

  /**
   * Returns a regular expression pattern.
   * @param mod modifier item
   * @param ext XQuery 3.0 syntax
   * @return modified pattern
   * @throws QueryException query exception
   */
  public Pattern pattern(final byte[] mod,
      final boolean ext) throws QueryException {

    // process modifiers
    int m = Pattern.UNIX_LINES;
    if(mod != null) {
      for(final byte b : mod) {
        if(b == 'i') m |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        else if(b == 'm') m |= Pattern.MULTILINE;
        else if(b == 's') m |= Pattern.DOTALL;
        else if(b == 'q' && ext) m |= Pattern.LITERAL;
        else if(b == 'x') {
          boolean cc = false;
          final StringBuilder sb = new StringBuilder();
          for(int s = 0; s < pattern.length(); s++) {
            final char c = pattern.charAt(s);
            if(cc || !ws(c)) sb.append(c);
            if(c == '[') cc = true;
            else if(c == ']') cc = false;
          }
          pattern = sb.toString();
        } else {
          REGMOD.thrw(input, (char) b);
        }
      }
    }

    // check escaped characters
    final StringBuilder bl = new StringBuilder();
    for(int i = 0; i < pattern.length(); ++i) {
      char b = pattern.charAt(i);
      if(b != '\\') {
        bl.append(b);
        continue;
      }
      // backslash
      final char c = next(++i);
      // character class
      if(c != 'p' && c != 'P') {
        if("0123456789cCdDniIrsStwW|.-^$?*+{}()[]\\".indexOf(c) == -1)
          REGESC.thrw(input, c);
        bl.append(b);
        bl.append(c);
      } else {
        b = next(++i);
        if(b != '{') REGCC.thrw(input, b);
        final StringBuilder tmp = new StringBuilder();
        while(true) {
          b = next(++i);
          if(b == 0) REGCC.thrw(input, b);
          if(b == '}') {
            bl.append(replace(tmp.toString(), c));
            break;
          }
          tmp.append(b);
        }
      }
    }
    pattern = bl.toString();

    try {
      if((m & Pattern.LITERAL) == 0 && pattern.indexOf('[') != -1 &&
          pattern.indexOf('-') != -1) {
        // replace classes by single characters to support Unicode matches
        while(true) {
          final Matcher mt = CLASSES.matcher(pattern);
          if(!mt.matches()) break;
          final char c1 = mt.group(1).charAt(0);
          final char c2 = mt.group(2).charAt(0);
          final TokenBuilder tb2 = new TokenBuilder("[");
          for(char c = c1; c <= c2; ++c) tb2.add(c);
          pattern = pattern.replaceAll("\\[" + c1 + "-" + c2, tb2.toString());
        }

        // remove excluded characters in classes
        String old = "";
        for(Matcher mt; (mt = EXCLUDE.matcher(pattern)).matches() &&
            !old.equals(pattern);) {
          old = pattern;
          final String in = mt.group(1);
          final String ex = mt.group(2);
          String out = in;
          for(int e = 0; e < ex.length(); ++e) {
            out = out.replaceAll(ex.substring(e, e + 1), "");
          }
          pattern = pattern.replaceAll("\\[" + in + "-\\[.*?\\]", "[" + out);
        }
      }
      return Pattern.compile(pattern, m);
    } catch(final Exception ex) {
      throw REGINV.thrw(input, pattern);
    }
  }

  /**
   * Returns the next character of the specified pattern, or {@code 0}.
   * @param pos position
   * @return next character
   */
  private char next(final int pos) {
    return pos == pattern.length() ? 0 : pattern.charAt(pos);
  }

  /**
   * Replaces a character class with the contained characters.
   * @param cls class
   * @param incl flag for including/excluding characters ({@code p/P})
   * @return next character
   */
  private static String replace(final String cls, final char incl) {
    final int[] v = cls.startsWith("Is") ? MAP.get(cls.substring(2)) : null;
    if(v == null) return "\\" + incl + "{" + cls + "}";
    final TokenBuilder tb = new TokenBuilder().add('[');
    if(incl == 'P') tb.add("!");
    for(int i = 0; i < v.length;) {
      tb.add(v[i++]);
      tb.add('-');
      tb.add(v[i++]);
    }
    return tb.add(']').toString();
  }

  /** Character class map. */
  private static final HashMap<String, int[]> MAP =
      new HashMap<String, int[]>();

  /** Character classes. */
  private static final Object[] CLS = {
    "AegeanNumbers", new int[] { 0x10100, 0x1013F },
    "AlphabeticPresentationForms", new int[] { 0xFB00, 0xFB4F },
    "AncientGreekMusicalNotation", new int[] { 0x1D200, 0x1D24F },
    "AncientGreekNumbers", new int[] { 0x10140, 0x1018F },
    "AncientSymbols", new int[] { 0x10190, 0x101CF },
    "Arabic", new int[] { 0x0600, 0x06FF },
    "ArabicPresentationForms-A", new int[] { 0xFB50, 0xFDFF },
    "ArabicPresentationForms-B", new int[] { 0xFE70, 0xFEFF },
    "ArabicSupplement", new int[] { 0x0750, 0x077F },
    "Armenian", new int[] { 0x0530, 0x058F },
    "Arrows", new int[] { 0x2190, 0x21FF },
    "Avestan", new int[] { 0x10B00, 0x10B3F },
    "Balinese", new int[] { 0x1B00, 0x1B7F },
    "Bamum", new int[] { 0xA6A0, 0xA6FF },
    "BasicLatin", new int[] { 0x0000, 0x007F },
    "Bengali", new int[] { 0x0980, 0x09FF },
    "BlockElements", new int[] { 0x2580, 0x259F },
    "Bopomofo", new int[] { 0x3100, 0x312F },
    "BopomofoExtended", new int[] { 0x31A0, 0x31BF },
    "BoxDrawing", new int[] { 0x2500, 0x257F },
    "BraillePatterns", new int[] { 0x2800, 0x28FF },
    "Buginese", new int[] { 0x1A00, 0x1A1F },
    "Buhid", new int[] { 0x1740, 0x175F },
    "ByzantineMusicalSymbols", new int[] { 0x1D000, 0x1D0FF },
    "Carian", new int[] { 0x102A0, 0x102DF },
    "Cham", new int[] { 0xAA00, 0xAA5F },
    "Cherokee", new int[] { 0x13A0, 0x13FF },
    "CJKCompatibility", new int[] { 0x3300, 0x33FF },
    "CJKCompatibilityForms", new int[] { 0xFE30, 0xFE4F },
    "CJKCompatibilityIdeographs", new int[] { 0xF900, 0xFAFF },
    "CJKCompatibilityIdeographsSupplement", new int[] { 0x2F800, 0x2FA1F },
    "CJKRadicalsSupplement", new int[] { 0x2E80, 0x2EFF },
    "CJKStrokes", new int[] { 0x31C0, 0x31EF },
    "CJKSymbolsandPunctuation", new int[] { 0x3000, 0x303F },
    "CJKUnifiedIdeographs", new int[] { 0x4E00, 0x9FFF },
    "CJKUnifiedIdeographsExtensionA", new int[] { 0x3400, 0x4DBF },
    "CJKUnifiedIdeographsExtensionB", new int[] { 0x20000, 0x2A6DF },
    "CJKUnifiedIdeographsExtensionC", new int[] { 0x2A700, 0x2B73F },
    "CombiningDiacriticalMarks", new int[] { 0x0300, 0x036F },
    "CombiningDiacriticalMarksforSymbols", new int[] { 0x20D0, 0x20FF },
    "CombiningDiacriticalMarksSupplement", new int[] { 0x1DC0, 0x1DFF },
    "CombiningHalfMarks", new int[] { 0xFE20, 0xFE2F },
    "CombiningMarksforSymbols", new int[] { 0x20D0, 0x20FF },
    "CommonIndicNumberForms", new int[] { 0xA830, 0xA83F },
    "ControlPictures", new int[] { 0x2400, 0x243F },
    "Coptic", new int[] { 0x2C80, 0x2CFF },
    "CountingRodNumerals", new int[] { 0x1D360, 0x1D37F },
    "Cuneiform", new int[] { 0x12000, 0x123FF },
    "CuneiformNumbersandPunctuation", new int[] { 0x12400, 0x1247F },
    "CurrencySymbols", new int[] { 0x20A0, 0x20CF },
    "CypriotSyllabary", new int[] { 0x10800, 0x1083F },
    "Cyrillic", new int[] { 0x0400, 0x04FF },
    "CyrillicExtended-A", new int[] { 0x2DE0, 0x2DFF },
    "CyrillicExtended-B", new int[] { 0xA640, 0xA69F },
    "CyrillicSupplement", new int[] { 0x0500, 0x052F },
    "Deseret", new int[] { 0x10400, 0x1044F },
    "Devanagari", new int[] { 0x0900, 0x097F },
    "DevanagariExtended", new int[] { 0xA8E0, 0xA8FF },
    "Dingbats", new int[] { 0x2700, 0x27BF },
    "DominoTiles", new int[] { 0x1F030, 0x1F09F },
    "EgyptianHieroglyphs", new int[] { 0x13000, 0x1342F },
    "EnclosedAlphanumerics", new int[] { 0x2460, 0x24FF },
    "EnclosedAlphanumericSupplement", new int[] { 0x1F100, 0x1F1FF },
    "EnclosedCJKLettersandMonths", new int[] { 0x3200, 0x32FF },
    "EnclosedIdeographicSupplement", new int[] { 0x1F200, 0x1F2FF },
    "Ethiopic", new int[] { 0x1200, 0x137F },
    "EthiopicExtended", new int[] { 0x2D80, 0x2DDF },
    "EthiopicSupplement", new int[] { 0x1380, 0x139F },
    "GeneralPunctuation", new int[] { 0x2000, 0x206F },
    "GeometricShapes", new int[] { 0x25A0, 0x25FF },
    "Georgian", new int[] { 0x10A0, 0x10FF },
    "GeorgianSupplement", new int[] { 0x2D00, 0x2D2F },
    "Glagolitic", new int[] { 0x2C00, 0x2C5F },
    "Gothic", new int[] { 0x10330, 0x1034F },
    "Greek", new int[] { 0x0370, 0x03FF },
    "GreekandCoptic", new int[] { 0x0370, 0x03FF },
    "GreekExtended", new int[] { 0x1F00, 0x1FFF },
    "Gujarati", new int[] { 0x0A80, 0x0AFF },
    "Gurmukhi", new int[] { 0x0A00, 0x0A7F },
    "HalfwidthandFullwidthForms", new int[] { 0xFF00, 0xFFEF },
    "HangulCompatibilityJamo", new int[] { 0x3130, 0x318F },
    "HangulJamo", new int[] { 0x1100, 0x11FF },
    "HangulJamoExtended-A", new int[] { 0xA960, 0xA97F },
    "HangulJamoExtended-B", new int[] { 0xD7B0, 0xD7FF },
    "HangulSyllables", new int[] { 0xAC00, 0xD7AF },
    "Hanunoo", new int[] { 0x1720, 0x173F },
    "Hebrew", new int[] { 0x0590, 0x05FF },
    "HighPrivateUseSurrogates", new int[] { 0xDB80, 0xDBFF },
    "HighSurrogates", new int[] { 0xD800, 0xDB7F },
    "Hiragana", new int[] { 0x3040, 0x309F },
    "IdeographicDescriptionCharacters", new int[] { 0x2FF0, 0x2FFF },
    "ImperialAramaic", new int[] { 0x10840, 0x1085F },
    "InscriptionalPahlavi", new int[] { 0x10B60, 0x10B7F },
    "InscriptionalParthian", new int[] { 0x10B40, 0x10B5F },
    "IPAExtensions", new int[] { 0x0250, 0x02AF },
    "Javanese", new int[] { 0xA980, 0xA9DF },
    "Kaithi", new int[] { 0x11080, 0x110CF },
    "Kanbun", new int[] { 0x3190, 0x319F },
    "KangxiRadicals", new int[] { 0x2F00, 0x2FDF },
    "Kannada", new int[] { 0x0C80, 0x0CFF },
    "Katakana", new int[] { 0x30A0, 0x30FF },
    "KatakanaPhoneticExtensions", new int[] { 0x31F0, 0x31FF },
    "KayahLi", new int[] { 0xA900, 0xA92F },
    "Kharoshthi", new int[] { 0x10A00, 0x10A5F },
    "Khmer", new int[] { 0x1780, 0x17FF },
    "KhmerSymbols", new int[] { 0x19E0, 0x19FF },
    "Lao", new int[] { 0x0E80, 0x0EFF },
    "Latin-1Supplement", new int[] { 0x0080, 0x00FF },
    "LatinExtended-A", new int[] { 0x0100, 0x017F },
    "LatinExtendedAdditional", new int[] { 0x1E00, 0x1EFF },
    "LatinExtended-B", new int[] { 0x0180, 0x024F },
    "LatinExtended-C", new int[] { 0x2C60, 0x2C7F },
    "LatinExtended-D", new int[] { 0xA720, 0xA7FF },
    "Lepcha", new int[] { 0x1C00, 0x1C4F },
    "LetterlikeSymbols", new int[] { 0x2100, 0x214F },
    "Limbu", new int[] { 0x1900, 0x194F },
    "LinearBIdeograms", new int[] { 0x10080, 0x100FF },
    "LinearBSyllabary", new int[] { 0x10000, 0x1007F },
    "Lisu", new int[] { 0xA4D0, 0xA4FF },
    "LowSurrogates", new int[] { 0xDC00, 0xDFFF },
    "Lycian", new int[] { 0x10280, 0x1029F },
    "Lydian", new int[] { 0x10920, 0x1093F },
    "MahjongTiles", new int[] { 0x1F000, 0x1F02F },
    "Malayalam", new int[] { 0x0D00, 0x0D7F },
    "MathematicalAlphanumericSymbols", new int[] { 0x1D400, 0x1D7FF },
    "MathematicalOperators", new int[] { 0x2200, 0x22FF },
    "MeeteiMayek", new int[] { 0xABC0, 0xABFF },
    "MiscellaneousMathematicalSymbols-A", new int[] { 0x27C0, 0x27EF },
    "MiscellaneousMathematicalSymbols-B", new int[] { 0x2980, 0x29FF },
    "MiscellaneousSymbols", new int[] { 0x2600, 0x26FF },
    "MiscellaneousSymbolsandArrows", new int[] { 0x2B00, 0x2BFF },
    "MiscellaneousTechnical", new int[] { 0x2300, 0x23FF },
    "ModifierToneLetters", new int[] { 0xA700, 0xA71F },
    "Mongolian", new int[] { 0x1800, 0x18AF },
    "MusicalSymbols", new int[] { 0x1D100, 0x1D1FF },
    "Myanmar", new int[] { 0x1000, 0x109F },
    "MyanmarExtended-A", new int[] { 0xAA60, 0xAA7F },
    "NewTaiLue", new int[] { 0x1980, 0x19DF },
    "NKo", new int[] { 0x07C0, 0x07FF },
    "NumberForms", new int[] { 0x2150, 0x218F },
    "Ogham", new int[] { 0x1680, 0x169F },
    "OlChiki", new int[] { 0x1C50, 0x1C7F },
    "OldItalic", new int[] { 0x10300, 0x1032F },
    "OldPersian", new int[] { 0x103A0, 0x103DF },
    "OldSouthArabian", new int[] { 0x10A60, 0x10A7F },
    "OldTurkic", new int[] { 0x10C00, 0x10C4F },
    "OpticalCharacterRecognition", new int[] { 0x2440, 0x245F },
    "Oriya", new int[] { 0x0B00, 0x0B7F },
    "Osmanya", new int[] { 0x10480, 0x104AF },
    "Phags-pa", new int[] { 0xA840, 0xA87F },
    "PhaistosDisc", new int[] { 0x101D0, 0x101FF },
    "Phoenician", new int[] { 0x10900, 0x1091F },
    "PhoneticExtensions", new int[] { 0x1D00, 0x1D7F },
    "PhoneticExtensionsSupplement", new int[] { 0x1D80, 0x1DBF },
    "PrivateUse", new int[] { 0xE000, 0xF8FF },
    "PrivateUseArea", new int[] { 0xE000, 0xF8FF },
    "Rejang", new int[] { 0xA930, 0xA95F },
    "RumiNumeralSymbols", new int[] { 0x10E60, 0x10E7F },
    "Runic", new int[] { 0x16A0, 0x16FF },
    "Samaritan", new int[] { 0x0800, 0x083F },
    "Saurashtra", new int[] { 0xA880, 0xA8DF },
    "Shavian", new int[] { 0x10450, 0x1047F },
    "Sinhala", new int[] { 0x0D80, 0x0DFF },
    "SmallFormVariants", new int[] { 0xFE50, 0xFE6F },
    "SpacingModifierLetters", new int[] { 0x02B0, 0x02FF },
    "Specials", new int[] { 0xFFF0, 0xFFFF },
    "Sundanese", new int[] { 0x1B80, 0x1BBF },
    "SuperscriptsandSubscripts", new int[] { 0x2070, 0x209F },
    "SupplementalArrows-A", new int[] { 0x27F0, 0x27FF },
    "SupplementalArrows-B", new int[] { 0x2900, 0x297F },
    "SupplementalMathematicalOperators", new int[] { 0x2A00, 0x2AFF },
    "SupplementalPunctuation", new int[] { 0x2E00, 0x2E7F },
    "SupplementaryPrivateUseArea-A", new int[] { 0xF0000, 0xFFFFF },
    "SupplementaryPrivateUseArea-B", new int[] { 0x100000, 0x10FFFF },
    "SylotiNagri", new int[] { 0xA800, 0xA82F },
    "Syriac", new int[] { 0x0700, 0x074F },
    "Tagalog", new int[] { 0x1700, 0x171F },
    "Tagbanwa", new int[] { 0x1760, 0x177F },
    "Tags", new int[] { 0xE0000, 0xE007F },
    "TaiLe", new int[] { 0x1950, 0x197F },
    "TaiTham", new int[] { 0x1A20, 0x1AAF },
    "TaiViet", new int[] { 0xAA80, 0xAADF },
    "TaiXuanJingSymbols", new int[] { 0x1D300, 0x1D35F },
    "Tamil", new int[] { 0x0B80, 0x0BFF },
    "Telugu", new int[] { 0x0C00, 0x0C7F },
    "Thaana", new int[] { 0x0780, 0x07BF },
    "Thai", new int[] { 0x0E00, 0x0E7F },
    "Tibetan", new int[] { 0x0F00, 0x0FFF },
    "Tifinagh", new int[] { 0x2D30, 0x2D7F },
    "Ugaritic", new int[] { 0x10380, 0x1039F },
    "UnifiedCanadianAboriginalSyllabics", new int[] { 0x1400, 0x167F },
    "UnifiedCanadianAboriginalSyllabicsExtended", new int[] { 0x18B0, 0x18FF },
    "Vai", new int[] { 0xA500, 0xA63F },
    "VariationSelectors", new int[] { 0xFE00, 0xFE0F },
    "VariationSelectorsSupplement", new int[] { 0xE0100, 0xE01EF },
    "VedicExtensions", new int[] { 0x1CD0, 0x1CFF },
    "VerticalForms", new int[] { 0xFE10, 0xFE1F },
    "YijingHexagramSymbols", new int[] { 0x4DC0, 0x4DFF },
    "YiRadicals", new int[] { 0xA490, 0xA4CF },
    "YiSyllables", new int[] { 0xA000, 0xA48F },
  };

  static {
    for(int s = 0; s < CLS.length; s += 2) {
      MAP.put((String) CLS[s], (int[]) CLS[s + 1]);
    }
  }
}
