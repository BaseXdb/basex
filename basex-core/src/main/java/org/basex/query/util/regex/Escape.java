package org.basex.query.util.regex;

import java.util.*;

/**
 * Escape sequence.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class Escape extends RegExp {
  /**  Comparator for int ranges. */
  private static final Comparator<int[]> CMP = Comparator.comparingInt(o -> o[0]);
  /** Character classes. */
  private static final Map<String, CharRange[]> MAP = new HashMap<>();

  /** Initial name characters. */
  private static final String INITIAL;
  /** Everything except initial name characters. */
  private static final String NOT_INITIAL;
  /** Name characters. */
  private static final String CHAR;
  /** Everything except name characters. */
  private static final String NOT_CHAR;
  /** Word characters (anything but punctuation, separators and "other"; XQuery/XSD {@code \w}). */
  public static final String WORD = "[^\\p{P}\\p{Z}\\p{C}]";
  /** Non-word characters (XQuery/XSD {@code \W}). */
  public static final String NOT_WORD = "[\\p{P}\\p{Z}\\p{C}]";
  /** Whitespace. */
  private static final String SPACE = "[ \\t\\n\\r]";
  /** Everything except whitespace. */
  private static final String NOT_SPACE = "[^ \\t\\n\\r]";
  /** Digits. */
  private static final String DIGIT = "\\p{Nd}";
  /** Everything except digits. */
  private static final String NOT_DIGIT = "\\P{Nd}";

  /** Image. */
  private final String img;

  /**
   * Constructor.
   * @param img image string
   */
  private Escape(final String img) {
    this.img = img;
  }

  /**
   * Creates a regular expression from the given escape sequence.
   * @param esc escape sequence
   * @return regular expression
   */
  public static RegExp get(final String esc) {
    if(esc.startsWith("\\p{Is") || esc.startsWith("\\P{Is")) {
      final CharRange[] rng = MAP.get(esc);
      return rng != null ? new CharClass(new CharGroup(rng), null) : null;
    }
    return new Escape(java(esc));
  }

  /**
   * Returns the Java representation of an escape sequence.
   * @param esc escape sequence
   * @return Java representation
   */
  private static String java(final String esc) {
    return switch(esc.charAt(1)) {
      case 'i' -> INITIAL;
      case 'I' -> NOT_INITIAL;
      case 'c' -> CHAR;
      case 'C' -> NOT_CHAR;
      case 's' -> SPACE;
      case 'S' -> NOT_SPACE;
      case 'd' -> DIGIT;
      case 'D' -> NOT_DIGIT;
      case 'w' -> WORD;
      case 'W' -> NOT_WORD;
      default  -> esc;
    };
  }

  /**
   * Gets the character escaped by the given single escape.
   * @param single single-char escape sequence
   * @return escaped char
   */
  public static char getCp(final String single) {
    return switch(single.charAt(1)) {
      case 'n' -> '\n';
      case 'r' -> '\r';
      case 't' -> '\t';
      default  -> single.charAt(1);
    };
  }

  /**
   * Translates the given escape into character ranges if possible.
   * @param esc escape sequence
   * @return array of regular expressions suitable for char groups
   */
  public static RegExp[] inGroup(final String esc) {
    if(esc.startsWith("\\p{Is") || esc.startsWith("\\P{Is")) {
      final CharRange[] rng = MAP.get(esc);
      if(rng != null) return rng;
    }
    return new RegExp[] { new Escape(java(esc)) };
  }

  @Override
  int minLength() {
    return 1;
  }

  @Override
  void toRegEx(final StringBuilder sb) {
    sb.append(img);
  }

  /**
   * Merges codepoint ranges.
   * @param rss ranges
   * @return merged ranges
   */
  private static int[][] merge(final int[][]... rss) {
    final ArrayList<int[]> ranges = new ArrayList<>();
    for(final int[][] rs : rss) Collections.addAll(ranges, rs);
    ranges.sort(CMP);
    for(int i = 0; i < ranges.size(); i++) {
      final int[] rng = ranges.get(i);
      while(i + 1 < ranges.size()) {
        final int[] rng2 = ranges.get(i + 1);
        if(rng2[0] - rng[1] > 1) break;
        rng[1] = rng2[1];
        ranges.remove(i + 1);
      }
    }
    return ranges.toArray(int[][]::new);
  }

  /**
   * Inverts code point ranges.
   * @param rng ranges
   * @return inverted ranges
   */
  private static int[][] invert(final int[][] rng) {
    int start = Character.MIN_CODE_POINT;
    final ArrayList<int[]> ranges = new ArrayList<>();

    for(final int[] in : rng) {
      if(in[0] - 1 >= start) ranges.add(new int[] { start, in[0] - 1 });
      start = in[1] + 1;
    }

    if(start <= Character.MAX_CODE_POINT)
      ranges.add(new int[] { start, Character.MAX_CODE_POINT });

    return ranges.toArray(int[][]::new);
  }

  /**
   * Serializes the given ranges to a character class.
   * @param ranges ranges to serialize
   * @return character class
   */
  private static String serialize(final int[][] ranges) {
    final StringBuilder sb = new StringBuilder().append('[');
    for(final int[] rng : ranges) {
      sb.append(escape(rng[0]));
      if(rng[1] != rng[0]) sb.append('-').append(escape(rng[1]));
    }
    return sb.append(']').toString();
  }

  /**
   * Escapes code points for use in character ranges.
   * @param cp code point
   * @return char representation
   */
  static char[] escape(final int cp) {
    return switch(cp) {
      case '[', ']', '-', '\\', '^', '&' -> new char[] { '\\', (char) cp };
      case '\n' -> new char[] { '\\', 'n' };
      case '\r' -> new char[] { '\\', 'r' };
      case '\t' -> new char[] { '\\', 't' };
      default -> Character.toChars(cp);
    };
  }

  /**
   * Adds a new named character range to the map.
   * @param m map
   * @param n name
   * @param r range
   */
  private static void add(final Map<String, int[][]> m, final String n, final int[] r) {
    final int[][] nw = { r };
    m.compute(n, (k, old) -> old == null ? nw : merge(old, nw));
  }

  static {
    // XML names, NameStartChar/NameChar (XML 1.0.5)
    final int[][] nameStartChar = merge(new int[][] {
      { ':', ':' }, { 'A', 'Z' }, { '_', '_' }, { 'a', 'z' }, { 0xC0, 0xD6 }, { 0xD8, 0xF6 },
      { 0xF8, 0x2FF }, { 0x370, 0x37D }, { 0x37F, 0x1FFF }, { 0x200C, 0x200D }, { 0x2070, 0x218F },
      { 0x2C00, 0x2FEF }, { 0x3001, 0xD7FF }, { 0xF900, 0xFDCF }, { 0xFDF0, 0xFFFD },
      { 0x10000, 0xEFFFF }
    });
    INITIAL = serialize(nameStartChar);
    NOT_INITIAL = serialize(invert(nameStartChar));
    final int[][] nameChar = merge(nameStartChar, new int[][] {
      { '-', '-' }, { '.', '.' }, { '0', '9' }, { 0xB7, 0xB7 }, { 0x300, 0x36F }, { 0x203F, 0x2040 }
    });
    CHAR = serialize(nameChar);
    NOT_CHAR = serialize(invert(nameChar));

    // http://www.w3.org/TR/xsd-unicode-blocknames/#blocks, Blocks.txt (Unicode 18.0)
    final HashMap<String, int[][]> m = new HashMap<>();
    add(m, "BasicLatin", new int[] { 0x0000, 0x007F });
    add(m, "Latin-1Supplement", new int[] { 0x0080, 0x00FF });
    add(m, "LatinExtended-A", new int[] { 0x0100, 0x017F });
    add(m, "LatinExtended-B", new int[] { 0x0180, 0x024F });
    add(m, "IPAExtensions", new int[] { 0x0250, 0x02AF });
    add(m, "SpacingModifierLetters", new int[] { 0x02B0, 0x02FF });
    add(m, "CombiningDiacriticalMarks", new int[] { 0x0300, 0x036F });
    add(m, "Greek", new int[] { 0x0370, 0x03FF });
    add(m, "GreekandCoptic", new int[] { 0x0370, 0x03FF });
    add(m, "Cyrillic", new int[] { 0x0400, 0x04FF });
    add(m, "CyrillicSupplementary", new int[] { 0x0500, 0x052F });
    add(m, "CyrillicSupplement", new int[] { 0x0500, 0x052F });
    add(m, "Armenian", new int[] { 0x0530, 0x058F });
    add(m, "Hebrew", new int[] { 0x0590, 0x05FF });
    add(m, "Arabic", new int[] { 0x0600, 0x06FF });
    add(m, "Syriac", new int[] { 0x0700, 0x074F });
    add(m, "ArabicSupplement", new int[] { 0x0750, 0x077F });
    add(m, "Thaana", new int[] { 0x0780, 0x07BF });
    add(m, "NKo", new int[] { 0x07C0, 0x07FF });
    add(m, "Samaritan", new int[] { 0x0800, 0x083F });
    add(m, "Mandaic", new int[] { 0x0840, 0x085F });
    add(m, "SyriacSupplement", new int[] { 0x0860, 0x086F });
    add(m, "ArabicExtended-B", new int[] { 0x0870, 0x089F });
    add(m, "ArabicExtended-A", new int[] { 0x08A0, 0x08FF });
    add(m, "Devanagari", new int[] { 0x0900, 0x097F });
    add(m, "Bengali", new int[] { 0x0980, 0x09FF });
    add(m, "Gurmukhi", new int[] { 0x0A00, 0x0A7F });
    add(m, "Gujarati", new int[] { 0x0A80, 0x0AFF });
    add(m, "Oriya", new int[] { 0x0B00, 0x0B7F });
    add(m, "Tamil", new int[] { 0x0B80, 0x0BFF });
    add(m, "Telugu", new int[] { 0x0C00, 0x0C7F });
    add(m, "Kannada", new int[] { 0x0C80, 0x0CFF });
    add(m, "Malayalam", new int[] { 0x0D00, 0x0D7F });
    add(m, "Sinhala", new int[] { 0x0D80, 0x0DFF });
    add(m, "Thai", new int[] { 0x0E00, 0x0E7F });
    add(m, "Lao", new int[] { 0x0E80, 0x0EFF });
    add(m, "Tibetan", new int[] { 0x0F00, 0x0FBF });
    add(m, "Tibetan", new int[] { 0x0F00, 0x0FFF });
    add(m, "Myanmar", new int[] { 0x1000, 0x109F });
    add(m, "Georgian", new int[] { 0x10A0, 0x10FF });
    add(m, "HangulJamo", new int[] { 0x1100, 0x11FF });
    add(m, "Ethiopic", new int[] { 0x1200, 0x137F });
    add(m, "EthiopicSupplement", new int[] { 0x1380, 0x139F });
    add(m, "Cherokee", new int[] { 0x13A0, 0x13FF });
    add(m, "UnifiedCanadianAboriginalSyllabics", new int[] { 0x1400, 0x167F });
    add(m, "Ogham", new int[] { 0x1680, 0x169F });
    add(m, "Runic", new int[] { 0x16A0, 0x16FF });
    add(m, "Tagalog", new int[] { 0x1700, 0x171F });
    add(m, "Hanunoo", new int[] { 0x1720, 0x173F });
    add(m, "Buhid", new int[] { 0x1740, 0x175F });
    add(m, "Tagbanwa", new int[] { 0x1760, 0x177F });
    add(m, "Khmer", new int[] { 0x1780, 0x17FF });
    add(m, "Mongolian", new int[] { 0x1800, 0x18AF });
    add(m, "UnifiedCanadianAboriginalSyllabicsExtended", new int[] { 0x18B0, 0x18FF });
    add(m, "Limbu", new int[] { 0x1900, 0x194F });
    add(m, "TaiLe", new int[] { 0x1950, 0x197F });
    add(m, "NewTaiLue", new int[] { 0x1980, 0x19DF });
    add(m, "KhmerSymbols", new int[] { 0x19E0, 0x19FF });
    add(m, "Buginese", new int[] { 0x1A00, 0x1A1F });
    add(m, "TaiTham", new int[] { 0x1A20, 0x1AAF });
    add(m, "CombiningDiacriticalMarksExtended", new int[] { 0x1AB0, 0x1AFF });
    add(m, "Balinese", new int[] { 0x1B00, 0x1B7F });
    add(m, "Sundanese", new int[] { 0x1B80, 0x1BBF });
    add(m, "Batak", new int[] { 0x1BC0, 0x1BFF });
    add(m, "Lepcha", new int[] { 0x1C00, 0x1C4F });
    add(m, "OlChiki", new int[] { 0x1C50, 0x1C7F });
    add(m, "CyrillicExtended-C", new int[] { 0x1C80, 0x1C8F });
    add(m, "GeorgianExtended", new int[] { 0x1C90, 0x1CBF });
    add(m, "SundaneseSupplement", new int[] { 0x1CC0, 0x1CCF });
    add(m, "VedicExtensions", new int[] { 0x1CD0, 0x1CFF });
    add(m, "PhoneticExtensions", new int[] { 0x1D00, 0x1D7F });
    add(m, "PhoneticExtensionsSupplement", new int[] { 0x1D80, 0x1DBF });
    add(m, "CombiningDiacriticalMarksSupplement", new int[] { 0x1DC0, 0x1DFF });
    add(m, "LatinExtendedAdditional", new int[] { 0x1E00, 0x1EFF });
    add(m, "GreekExtended", new int[] { 0x1F00, 0x1FFF });
    add(m, "GeneralPunctuation", new int[] { 0x2000, 0x206F });
    add(m, "SuperscriptsandSubscripts", new int[] { 0x2070, 0x209F });
    add(m, "CurrencySymbols", new int[] { 0x20A0, 0x20CF });
    add(m, "CombiningMarksforSymbols", new int[] { 0x20D0, 0x20FF });
    add(m, "CombiningDiacriticalMarksforSymbols", new int[] { 0x20D0, 0x20FF });
    add(m, "LetterlikeSymbols", new int[] { 0x2100, 0x214F });
    add(m, "NumberForms", new int[] { 0x2150, 0x218F });
    add(m, "Arrows", new int[] { 0x2190, 0x21FF });
    add(m, "MathematicalOperators", new int[] { 0x2200, 0x22FF });
    add(m, "MiscellaneousTechnical", new int[] { 0x2300, 0x23FF });
    add(m, "ControlPictures", new int[] { 0x2400, 0x243F });
    add(m, "OpticalCharacterRecognition", new int[] { 0x2440, 0x245F });
    add(m, "EnclosedAlphanumerics", new int[] { 0x2460, 0x24FF });
    add(m, "BoxDrawing", new int[] { 0x2500, 0x257F });
    add(m, "BlockElements", new int[] { 0x2580, 0x259F });
    add(m, "GeometricShapes", new int[] { 0x25A0, 0x25FF });
    add(m, "MiscellaneousSymbols", new int[] { 0x2600, 0x26FF });
    add(m, "Dingbats", new int[] { 0x2700, 0x27BF });
    add(m, "MiscellaneousMathematicalSymbols-A", new int[] { 0x27C0, 0x27EF });
    add(m, "SupplementalArrows-A", new int[] { 0x27F0, 0x27FF });
    add(m, "BraillePatterns", new int[] { 0x2800, 0x28FF });
    add(m, "SupplementalArrows-B", new int[] { 0x2900, 0x297F });
    add(m, "MiscellaneousMathematicalSymbols-B", new int[] { 0x2980, 0x29FF });
    add(m, "SupplementalMathematicalOperators", new int[] { 0x2A00, 0x2AFF });
    add(m, "MiscellaneousSymbolsandArrows", new int[] { 0x2B00, 0x2BFF });
    add(m, "Glagolitic", new int[] { 0x2C00, 0x2C5F });
    add(m, "LatinExtended-C", new int[] { 0x2C60, 0x2C7F });
    add(m, "Coptic", new int[] { 0x2C80, 0x2CFF });
    add(m, "GeorgianSupplement", new int[] { 0x2D00, 0x2D2F });
    add(m, "Tifinagh", new int[] { 0x2D30, 0x2D7F });
    add(m, "EthiopicExtended", new int[] { 0x2D80, 0x2DDF });
    add(m, "CyrillicExtended-A", new int[] { 0x2DE0, 0x2DFF });
    add(m, "SupplementalPunctuation", new int[] { 0x2E00, 0x2E7F });
    add(m, "CJKRadicalsSupplement", new int[] { 0x2E80, 0x2EFF });
    add(m, "KangxiRadicals", new int[] { 0x2F00, 0x2FDF });
    add(m, "IdeographicDescriptionCharacters", new int[] { 0x2FF0, 0x2FFF });
    add(m, "CJKSymbolsandPunctuation", new int[] { 0x3000, 0x303F });
    add(m, "Hiragana", new int[] { 0x3040, 0x309F });
    add(m, "Katakana", new int[] { 0x30A0, 0x30FF });
    add(m, "Bopomofo", new int[] { 0x3100, 0x312F });
    add(m, "HangulCompatibilityJamo", new int[] { 0x3130, 0x318F });
    add(m, "Kanbun", new int[] { 0x3190, 0x319F });
    add(m, "BopomofoExtended", new int[] { 0x31A0, 0x31BF });
    add(m, "CJKStrokes", new int[] { 0x31C0, 0x31EF });
    add(m, "KatakanaPhoneticExtensions", new int[] { 0x31F0, 0x31FF });
    add(m, "EnclosedCJKLettersandMonths", new int[] { 0x3200, 0x32FF });
    add(m, "CJKCompatibility", new int[] { 0x3300, 0x33FF });
    add(m, "CJKUnifiedIdeographsExtensionA", new int[] { 0x3400, 0x4DB5 });
    add(m, "CJKUnifiedIdeographsExtensionA", new int[] { 0x3400, 0x4DBF });
    add(m, "YijingHexagramSymbols", new int[] { 0x4DC0, 0x4DFF });
    add(m, "CJKUnifiedIdeographs", new int[] { 0x4E00, 0x9FFF });
    add(m, "YiSyllables", new int[] { 0xA000, 0xA48F });
    add(m, "YiRadicals", new int[] { 0xA490, 0xA4CF });
    add(m, "Lisu", new int[] { 0xA4D0, 0xA4FF });
    add(m, "Vai", new int[] { 0xA500, 0xA63F });
    add(m, "CyrillicExtended-B", new int[] { 0xA640, 0xA69F });
    add(m, "Bamum", new int[] { 0xA6A0, 0xA6FF });
    add(m, "ModifierToneLetters", new int[] { 0xA700, 0xA71F });
    add(m, "LatinExtended-D", new int[] { 0xA720, 0xA7FF });
    add(m, "SylotiNagri", new int[] { 0xA800, 0xA82F });
    add(m, "CommonIndicNumberForms", new int[] { 0xA830, 0xA83F });
    add(m, "Phags-pa", new int[] { 0xA840, 0xA87F });
    add(m, "Saurashtra", new int[] { 0xA880, 0xA8DF });
    add(m, "DevanagariExtended", new int[] { 0xA8E0, 0xA8FF });
    add(m, "KayahLi", new int[] { 0xA900, 0xA92F });
    add(m, "Rejang", new int[] { 0xA930, 0xA95F });
    add(m, "HangulJamoExtended-A", new int[] { 0xA960, 0xA97F });
    add(m, "Javanese", new int[] { 0xA980, 0xA9DF });
    add(m, "MyanmarExtended-B", new int[] { 0xA9E0, 0xA9FF });
    add(m, "Cham", new int[] { 0xAA00, 0xAA5F });
    add(m, "MyanmarExtended-A", new int[] { 0xAA60, 0xAA7F });
    add(m, "TaiViet", new int[] { 0xAA80, 0xAADF });
    add(m, "MeeteiMayekExtensions", new int[] { 0xAAE0, 0xAAFF });
    add(m, "EthiopicExtended-A", new int[] { 0xAB00, 0xAB2F });
    add(m, "LatinExtended-E", new int[] { 0xAB30, 0xAB6F });
    add(m, "CherokeeSupplement", new int[] { 0xAB70, 0xABBF });
    add(m, "MeeteiMayek", new int[] { 0xABC0, 0xABFF });
    add(m, "HangulSyllables", new int[] { 0xAC00, 0xD7A3 });
    add(m, "HangulSyllables", new int[] { 0xAC00, 0xD7AF });
    add(m, "HangulJamoExtended-B", new int[] { 0xD7B0, 0xD7FF });
    add(m, "HighSurrogates", new int[] { 0xD800, 0xDB7F });
    add(m, "HighPrivateUseSurrogates", new int[] { 0xDB80, 0xDBFF });
    add(m, "LowSurrogates", new int[] { 0xDC00, 0xDFFF });
    add(m, "PrivateUse", new int[] { 0xE000, 0xF8FF });
    add(m, "PrivateUseArea", new int[] { 0xE000, 0xF8FF });
    add(m, "CJKCompatibilityIdeographs", new int[] { 0xF900, 0xFAFF });
    add(m, "AlphabeticPresentationForms", new int[] { 0xFB00, 0xFB4F });
    add(m, "ArabicPresentationForms-A", new int[] { 0xFB50, 0xFDFF });
    add(m, "VariationSelectors", new int[] { 0xFE00, 0xFE0F });
    add(m, "VerticalForms", new int[] { 0xFE10, 0xFE1F });
    add(m, "CombiningHalfMarks", new int[] { 0xFE20, 0xFE2F });
    add(m, "CJKCompatibilityForms", new int[] { 0xFE30, 0xFE4F });
    add(m, "SmallFormVariants", new int[] { 0xFE50, 0xFE6F });
    add(m, "ArabicPresentationForms-B", new int[] { 0xFE70, 0xFEFE });
    add(m, "ArabicPresentationForms-B", new int[] { 0xFE70, 0xFEFF });
    add(m, "Specials", new int[] { 0xFEFF, 0xFEFF });
    add(m, "HalfwidthandFullwidthForms", new int[] { 0xFF00, 0xFFEF });
    add(m, "Specials", new int[] { 0xFFF0, 0xFFFD });
    add(m, "Specials", new int[] { 0xFFF0, 0xFFFF });
    add(m, "LinearBSyllabary", new int[] { 0x10000, 0x1007F });
    add(m, "LinearBIdeograms", new int[] { 0x10080, 0x100FF });
    add(m, "AegeanNumbers", new int[] { 0x10100, 0x1013F });
    add(m, "AncientGreekNumbers", new int[] { 0x10140, 0x1018F });
    add(m, "AncientSymbols", new int[] { 0x10190, 0x101CF });
    add(m, "PhaistosDisc", new int[] { 0x101D0, 0x101FF });
    add(m, "Lycian", new int[] { 0x10280, 0x1029F });
    add(m, "Carian", new int[] { 0x102A0, 0x102DF });
    add(m, "CopticEpactNumbers", new int[] { 0x102E0, 0x102FF });
    add(m, "OldItalic", new int[] { 0x10300, 0x1032F });
    add(m, "Gothic", new int[] { 0x10330, 0x1034F });
    add(m, "OldPermic", new int[] { 0x10350, 0x1037F });
    add(m, "Ugaritic", new int[] { 0x10380, 0x1039F });
    add(m, "OldPersian", new int[] { 0x103A0, 0x103DF });
    add(m, "Deseret", new int[] { 0x10400, 0x1044F });
    add(m, "Shavian", new int[] { 0x10450, 0x1047F });
    add(m, "Osmanya", new int[] { 0x10480, 0x104AF });
    add(m, "Osage", new int[] { 0x104B0, 0x104FF });
    add(m, "Elbasan", new int[] { 0x10500, 0x1052F });
    add(m, "CaucasianAlbanian", new int[] { 0x10530, 0x1056F });
    add(m, "Vithkuqi", new int[] { 0x10570, 0x105BF });
    add(m, "Todhri", new int[] { 0x105C0, 0x105FF });
    add(m, "LinearA", new int[] { 0x10600, 0x1077F });
    add(m, "LatinExtended-F", new int[] { 0x10780, 0x107BF });
    add(m, "CypriotSyllabary", new int[] { 0x10800, 0x1083F });
    add(m, "ImperialAramaic", new int[] { 0x10840, 0x1085F });
    add(m, "Palmyrene", new int[] { 0x10860, 0x1087F });
    add(m, "Nabataean", new int[] { 0x10880, 0x108AF });
    add(m, "Hatran", new int[] { 0x108E0, 0x108FF });
    add(m, "Phoenician", new int[] { 0x10900, 0x1091F });
    add(m, "Lydian", new int[] { 0x10920, 0x1093F });
    add(m, "Sidetic", new int[] { 0x10940, 0x1095F });
    add(m, "MeroiticHieroglyphs", new int[] { 0x10980, 0x1099F });
    add(m, "MeroiticCursive", new int[] { 0x109A0, 0x109FF });
    add(m, "Kharoshthi", new int[] { 0x10A00, 0x10A5F });
    add(m, "OldSouthArabian", new int[] { 0x10A60, 0x10A7F });
    add(m, "OldNorthArabian", new int[] { 0x10A80, 0x10A9F });
    add(m, "Manichaean", new int[] { 0x10AC0, 0x10AFF });
    add(m, "Avestan", new int[] { 0x10B00, 0x10B3F });
    add(m, "InscriptionalParthian", new int[] { 0x10B40, 0x10B5F });
    add(m, "InscriptionalPahlavi", new int[] { 0x10B60, 0x10B7F });
    add(m, "PsalterPahlavi", new int[] { 0x10B80, 0x10BAF });
    add(m, "OldTurkic", new int[] { 0x10C00, 0x10C4F });
    add(m, "OldHungarian", new int[] { 0x10C80, 0x10CFF });
    add(m, "HanifiRohingya", new int[] { 0x10D00, 0x10D3F });
    add(m, "Garay", new int[] { 0x10D40, 0x10D8F });
    add(m, "RumiNumeralSymbols", new int[] { 0x10E60, 0x10E7F });
    add(m, "Yezidi", new int[] { 0x10E80, 0x10EBF });
    add(m, "ArabicExtended-C", new int[] { 0x10EC0, 0x10EFF });
    add(m, "OldSogdian", new int[] { 0x10F00, 0x10F2F });
    add(m, "Sogdian", new int[] { 0x10F30, 0x10F6F });
    add(m, "OldUyghur", new int[] { 0x10F70, 0x10FAF });
    add(m, "Chorasmian", new int[] { 0x10FB0, 0x10FDF });
    add(m, "Elymaic", new int[] { 0x10FE0, 0x10FFF });
    add(m, "Brahmi", new int[] { 0x11000, 0x1107F });
    add(m, "Kaithi", new int[] { 0x11080, 0x110CF });
    add(m, "SoraSompeng", new int[] { 0x110D0, 0x110FF });
    add(m, "Chakma", new int[] { 0x11100, 0x1114F });
    add(m, "Mahajani", new int[] { 0x11150, 0x1117F });
    add(m, "Sharada", new int[] { 0x11180, 0x111DF });
    add(m, "SinhalaArchaicNumbers", new int[] { 0x111E0, 0x111FF });
    add(m, "Khojki", new int[] { 0x11200, 0x1124F });
    add(m, "Multani", new int[] { 0x11280, 0x112AF });
    add(m, "Khudawadi", new int[] { 0x112B0, 0x112FF });
    add(m, "Grantha", new int[] { 0x11300, 0x1137F });
    add(m, "Tulu-Tigalari", new int[] { 0x11380, 0x113FF });
    add(m, "Newa", new int[] { 0x11400, 0x1147F });
    add(m, "Tirhuta", new int[] { 0x11480, 0x114DF });
    add(m, "Siddham", new int[] { 0x11580, 0x115FF });
    add(m, "Modi", new int[] { 0x11600, 0x1165F });
    add(m, "MongolianSupplement", new int[] { 0x11660, 0x1167F });
    add(m, "Takri", new int[] { 0x11680, 0x116CF });
    add(m, "MyanmarExtended-C", new int[] { 0x116D0, 0x116FF });
    add(m, "Ahom", new int[] { 0x11700, 0x1174F });
    add(m, "Dogra", new int[] { 0x11800, 0x1184F });
    add(m, "WarangCiti", new int[] { 0x118A0, 0x118FF });
    add(m, "DivesAkuru", new int[] { 0x11900, 0x1195F });
    add(m, "Nandinagari", new int[] { 0x119A0, 0x119FF });
    add(m, "ZanabazarSquare", new int[] { 0x11A00, 0x11A4F });
    add(m, "Soyombo", new int[] { 0x11A50, 0x11AAF });
    add(m, "UnifiedCanadianAboriginalSyllabicsExtended-A", new int[] { 0x11AB0, 0x11ABF });
    add(m, "PauCinHau", new int[] { 0x11AC0, 0x11AFF });
    add(m, "DevanagariExtended-A", new int[] { 0x11B00, 0x11B5F });
    add(m, "SharadaSupplement", new int[] { 0x11B60, 0x11B7F });
    add(m, "Sunuwar", new int[] { 0x11BC0, 0x11BFF });
    add(m, "Bhaiksuki", new int[] { 0x11C00, 0x11C6F });
    add(m, "Marchen", new int[] { 0x11C70, 0x11CBF });
    add(m, "MasaramGondi", new int[] { 0x11D00, 0x11D5F });
    add(m, "GunjalaGondi", new int[] { 0x11D60, 0x11DAF });
    add(m, "TolongSiki", new int[] { 0x11DB0, 0x11DEF });
    add(m, "BengaliSupplement", new int[] { 0x11DF0, 0x11DFF });
    add(m, "Makasar", new int[] { 0x11EE0, 0x11EFF });
    add(m, "Kawi", new int[] { 0x11F00, 0x11F5F });
    add(m, "LisuSupplement", new int[] { 0x11FB0, 0x11FBF });
    add(m, "TamilSupplement", new int[] { 0x11FC0, 0x11FFF });
    add(m, "Cuneiform", new int[] { 0x12000, 0x123FF });
    add(m, "CuneiformNumbersandPunctuation", new int[] { 0x12400, 0x1247F });
    add(m, "EarlyDynasticCuneiform", new int[] { 0x12480, 0x1254F });
    add(m, "ArchaicCuneiformNumerals", new int[] { 0x12550, 0x1268F });
    add(m, "Cypro-Minoan", new int[] { 0x12F90, 0x12FFF });
    add(m, "EgyptianHieroglyphs", new int[] { 0x13000, 0x1342F });
    add(m, "EgyptianHieroglyphFormatControls", new int[] { 0x13430, 0x1345F });
    add(m, "EgyptianHieroglyphsExtended-A", new int[] { 0x13460, 0x143FF });
    add(m, "AnatolianHieroglyphs", new int[] { 0x14400, 0x1467F });
    add(m, "GurungKhema", new int[] { 0x16100, 0x1613F });
    add(m, "BamumSupplement", new int[] { 0x16800, 0x16A3F });
    add(m, "Mro", new int[] { 0x16A40, 0x16A6F });
    add(m, "Tangsa", new int[] { 0x16A70, 0x16ACF });
    add(m, "BassaVah", new int[] { 0x16AD0, 0x16AFF });
    add(m, "PahawhHmong", new int[] { 0x16B00, 0x16B8F });
    add(m, "KiratRai", new int[] { 0x16D40, 0x16D7F });
    add(m, "Medefaidrin", new int[] { 0x16E40, 0x16E9F });
    add(m, "BeriaErfe", new int[] { 0x16EA0, 0x16EDF });
    add(m, "Miao", new int[] { 0x16F00, 0x16F9F });
    add(m, "IdeographicSymbolsandPunctuation", new int[] { 0x16FE0, 0x16FFF });
    add(m, "Tangut", new int[] { 0x17000, 0x187FF });
    add(m, "TangutComponents", new int[] { 0x18800, 0x18AFF });
    add(m, "KhitanSmallScript", new int[] { 0x18B00, 0x18CFF });
    add(m, "TangutSupplement", new int[] { 0x18D00, 0x18D7F });
    add(m, "TangutComponentsSupplement", new int[] { 0x18D80, 0x18DFF });
    add(m, "Jurchen", new int[] { 0x18E00, 0x1919F });
    add(m, "JurchenRadicals", new int[] { 0x191A0, 0x191DF });
    add(m, "KanaExtended-B", new int[] { 0x1AFF0, 0x1AFFF });
    add(m, "KanaSupplement", new int[] { 0x1B000, 0x1B0FF });
    add(m, "KanaExtended-A", new int[] { 0x1B100, 0x1B12F });
    add(m, "SmallKanaExtension", new int[] { 0x1B130, 0x1B16F });
    add(m, "Nushu", new int[] { 0x1B170, 0x1B2FF });
    add(m, "Duployan", new int[] { 0x1BC00, 0x1BC9F });
    add(m, "ShorthandFormatControls", new int[] { 0x1BCA0, 0x1BCAF });
    add(m, "SymbolsforLegacyComputingSupplement", new int[] { 0x1CC00, 0x1CEBF });
    add(m, "MiscellaneousSymbolsSupplement", new int[] { 0x1CEC0, 0x1CEFF });
    add(m, "ZnamennyMusicalNotation", new int[] { 0x1CF00, 0x1CFCF });
    add(m, "ByzantineMusicalSymbols", new int[] { 0x1D000, 0x1D0FF });
    add(m, "MusicalSymbols", new int[] { 0x1D100, 0x1D1FF });
    add(m, "AncientGreekMusicalNotation", new int[] { 0x1D200, 0x1D24F });
    add(m, "MusicalSymbolsSupplement", new int[] { 0x1D250, 0x1D28F });
    add(m, "KaktovikNumerals", new int[] { 0x1D2C0, 0x1D2DF });
    add(m, "MayanNumerals", new int[] { 0x1D2E0, 0x1D2FF });
    add(m, "TaiXuanJingSymbols", new int[] { 0x1D300, 0x1D35F });
    add(m, "CountingRodNumerals", new int[] { 0x1D360, 0x1D37F });
    add(m, "MathematicalAlphanumericSymbols", new int[] { 0x1D400, 0x1D7FF });
    add(m, "SuttonSignWriting", new int[] { 0x1D800, 0x1DAAF });
    add(m, "MiscellaneousSymbolsandArrowsExtended", new int[] { 0x1DB00, 0x1DBFF });
    add(m, "LatinExtended-G", new int[] { 0x1DF00, 0x1DFFF });
    add(m, "GlagoliticSupplement", new int[] { 0x1E000, 0x1E02F });
    add(m, "CyrillicExtended-D", new int[] { 0x1E030, 0x1E08F });
    add(m, "NyiakengPuachueHmong", new int[] { 0x1E100, 0x1E14F });
    add(m, "Toto", new int[] { 0x1E290, 0x1E2BF });
    add(m, "Wancho", new int[] { 0x1E2C0, 0x1E2FF });
    add(m, "NagMundari", new int[] { 0x1E4D0, 0x1E4FF });
    add(m, "OlOnal", new int[] { 0x1E5D0, 0x1E5FF });
    add(m, "TaiYo", new int[] { 0x1E6C0, 0x1E6FF });
    add(m, "EthiopicExtended-B", new int[] { 0x1E7E0, 0x1E7FF });
    add(m, "MendeKikakui", new int[] { 0x1E800, 0x1E8DF });
    add(m, "Adlam", new int[] { 0x1E900, 0x1E95F });
    add(m, "IndicSiyaqNumbers", new int[] { 0x1EC70, 0x1ECBF });
    add(m, "OttomanSiyaqNumbers", new int[] { 0x1ED00, 0x1ED4F });
    add(m, "ArabicMathematicalAlphabeticSymbols", new int[] { 0x1EE00, 0x1EEFF });
    add(m, "MahjongTiles", new int[] { 0x1F000, 0x1F02F });
    add(m, "DominoTiles", new int[] { 0x1F030, 0x1F09F });
    add(m, "PlayingCards", new int[] { 0x1F0A0, 0x1F0FF });
    add(m, "EnclosedAlphanumericSupplement", new int[] { 0x1F100, 0x1F1FF });
    add(m, "EnclosedIdeographicSupplement", new int[] { 0x1F200, 0x1F2FF });
    add(m, "MiscellaneousSymbolsAndPictographs", new int[] { 0x1F300, 0x1F5FF });
    add(m, "MiscellaneousSymbolsandPictographs", new int[] { 0x1F300, 0x1F5FF });
    add(m, "Emoticons", new int[] { 0x1F600, 0x1F64F });
    add(m, "OrnamentalDingbats", new int[] { 0x1F650, 0x1F67F });
    add(m, "TransportAndMapSymbols", new int[] { 0x1F680, 0x1F6FF });
    add(m, "TransportandMapSymbols", new int[] { 0x1F680, 0x1F6FF });
    add(m, "AlchemicalSymbols", new int[] { 0x1F700, 0x1F77F });
    add(m, "GeometricShapesExtended", new int[] { 0x1F780, 0x1F7FF });
    add(m, "SupplementalArrows-C", new int[] { 0x1F800, 0x1F8FF });
    add(m, "SupplementalSymbolsandPictographs", new int[] { 0x1F900, 0x1F9FF });
    add(m, "ChessSymbols", new int[] { 0x1FA00, 0x1FA6F });
    add(m, "SymbolsandPictographsExtended-A", new int[] { 0x1FA70, 0x1FAFF });
    add(m, "SymbolsforLegacyComputing", new int[] { 0x1FB00, 0x1FBFF });
    add(m, "CJKUnifiedIdeographsExtensionB", new int[] { 0x20000, 0x2A6D6 });
    add(m, "CJKUnifiedIdeographsExtensionB", new int[] { 0x20000, 0x2A6DF });
    add(m, "CJKUnifiedIdeographsExtensionC", new int[] { 0x2A700, 0x2B73F });
    add(m, "CJKUnifiedIdeographsExtensionD", new int[] { 0x2B740, 0x2B81F });
    add(m, "CJKUnifiedIdeographsExtensionE", new int[] { 0x2B820, 0x2CEAF });
    add(m, "CJKUnifiedIdeographsExtensionF", new int[] { 0x2CEB0, 0x2EBEF });
    add(m, "CJKUnifiedIdeographsExtensionI", new int[] { 0x2EBF0, 0x2EE5F });
    add(m, "CJKCompatibilityIdeographsSupplement", new int[] { 0x2F800, 0x2FA1F });
    add(m, "CJKUnifiedIdeographsExtensionG", new int[] { 0x30000, 0x3134F });
    add(m, "CJKUnifiedIdeographsExtensionH", new int[] { 0x31350, 0x323AF });
    add(m, "CJKUnifiedIdeographsExtensionJ", new int[] { 0x323B0, 0x3347F });
    add(m, "Seal", new int[] { 0x3D000, 0x3FC3F });
    add(m, "Tags", new int[] { 0xE0000, 0xE007F });
    add(m, "VariationSelectorsSupplement", new int[] { 0xE0100, 0xE01EF });
    add(m, "PrivateUse", new int[] { 0xF0000, 0xFFFFD });
    add(m, "SupplementaryPrivateUseArea-A", new int[] { 0xF0000, 0xFFFFF });
    add(m, "PrivateUse", new int[] { 0x100000, 0x10FFFD });
    add(m, "SupplementaryPrivateUseArea-B", new int[] { 0x100000, 0x10FFFF });

    // add entries for all known character classes
    m.forEach((key, vals) -> {
      final int vl = vals.length;
      final CharRange[] rs = new CharRange[vl];
      for(int v = 0; v < vl; v++) rs[v] = new CharRange(vals[v][0], vals[v][1]);
      MAP.put("\\p{Is" + key + '}', rs);

      final int[][] not = invert(vals);
      final int nl = not.length;
      final CharRange[] nrs = new CharRange[nl];
      for(int n = 0; n < nl; n++) nrs[n] = new CharRange(not[n][0], not[n][1]);
      MAP.put("\\P{Is" + key + '}', nrs);
    });
  }
}
