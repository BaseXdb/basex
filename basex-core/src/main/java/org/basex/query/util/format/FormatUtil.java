package org.basex.query.util.format;

import static org.basex.util.Token.*;

/**
 * This class assembles methods and variables that are used by more than one
 * formatter class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class FormatUtil {
  /** Zero digits, Unicode Category Nd. */
  private static final int[] ZEROES = {
    0x30, 0x660, 0x6F0, 0x7C0, 0x966, 0x9E6, 0xA66, 0xAE6, 0xB66, 0xBE6, 0xC66, 0xCE6,
    0xD66, 0xE50, 0xED0, 0xF20, 0x1040, 0x1090, 0x17E0, 0x1810, 0x1946, 0x19D0, 0x1A80,
    0x1A90, 0x1B50, 0x1BB0, 0x1C40, 0x1C50, 0xA620, 0xA8D0, 0xA900, 0xA9D0, 0xAA50,
    0xABF0, 0xFF10, 0x104A0, 0x11066, 0x1D7CE, 0x1D7D8, 0x1D7E2, 0x1D7EC, 0x1D7F6
  };
  /** Kanji digits. */
  static final int[] KANJI = {
    0x3007, 0x4e00, 0x4e8c, 0x4e09, 0x56db, 0x4e94, 0x516d, 0x4e03, 0x516b,
    0x4e5d, 0x5341, 0x767E, 0x5343, 0x4e07, 0x5104, 0x5146 };

  /** Roman numbers (1-10). */
  static final byte[][] ROMANI =
    tokens("", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX");
  /** Roman numbers (10-100). */
  static final byte[][] ROMANX =
    tokens("", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC");
  /** Roman numbers (100-1000). */
  static final byte[][] ROMANC =
    tokens("", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM");
  /** Roman numbers (1000-3000). */
  static final byte[][] ROMANM = tokens("", "M", "MM", "MMM");

  /** Alphabet sequences. */
  private static final String[] SEQS = {
    // Latin numbering
    "abcdefghijklmnopqrstuvwxyz",
    // Latin numbering (upper case)
    "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
    // Greek numbering
    "\u03b1\u03b2\u03b3\u03b4\u03b5\u03b6\u03b7\u03b8\u03b9\u03ba" +
    "\u03bb\u03bc\u03bd\u03be\u03bf\u03c0\u03c1\u03c2\u03c3\u03c4" +
    "\u03c5\u03c6\u03c7\u03c8\u03c9",
    // Greek numbering (upper case)
    "\u0391\u0392\u0393\u0394\u0395\u0396\u0397\u0398\u0399\u039a" +
    "\u039b\u039c\u039d\u039e\u039f\u03a0\u03a1\u03a2\u03a3\u03a4" +
    "\u03a5\u03a6\u03a7\u03a8\u03a9",
    // Hebrew numbering
    "\u05d0\u05d1\u05d2\u05d3\u05d4\u05d5\u05d6\u05d7\u05d8\u05d9\u05db" +
    "\u05dc\u05de\u05e0\u05e1\u05e2\u05e4\u05e6\u05e7\u05e8\u05e9\u05ea",
    // Cyrillic numbering, based on Dmitry Kirsanov's sequence in Saxon.
    "\u0430\u0431\u0432\u0433\u0434\u0435\u0436\u0437\u0438\u043a" +
    "\u043b\u043c\u043d\u043e\u043f\u0440\u0441\u0441\u0443\u0444" +
    "\u0445\u0446\u0447\u0448\u0449\u044b\u044d\u044e\u044f",
    // Cyrillic numbering (upper case).
    "\u0410\u0411\u0412\u0413\u0414\u0415\u0416\u0417\u0418\u041a" +
    "\u041b\u041c\u041d\u041e\u041f\u0420\u0421\u0421\u0423\u0424" +
    "\u0425\u0426\u0427\u0428\u0429\u042b\u042d\u042e\u042f",
    // Hiragana A numbering, based on Murakami Shinyu's sequences in Saxon.
    "\u3042\u3044\u3046\u3048\u304a\u304b\u304d\u304f\u3051\u3053" +
    "\u3055\u3057\u3059\u305b\u305d\u305f\u3061\u3064\u3066\u3068" +
    "\u306a\u306b\u306c\u306d\u306e\u306f\u3072\u3075\u3078\u307b" +
    "\u307e\u307f\u3080\u3081\u3082\u3084\u3086\u3088\u3089\u308a" +
    "\u308b\u308c\u308d\u308f\u3092\u3093",
    // Katakana A numbering.
    "\u30a2\u30a4\u30a6\u30a8\u30aa\u30ab\u30ad\u30af\u30b1\u30b3" +
    "\u30b5\u30b7\u30b9\u30bb\u30bd\u30bf\u30c1\u30c4\u30c6\u30c8" +
    "\u30ca\u30cb\u30cc\u30cd\u30ce\u30cf\u30d2\u30d5\u30d8\u30db" +
    "\u30de\u30df\u30e0\u30e1\u30e2\u30e4\u30e6\u30e8\u30e9\u30ea" +
    "\u30eb\u30ec\u30ed\u30ef\u30f2\u30f3",
    // Hiragana I numbering.
    "\u3044\u308d\u306f\u306b\u307b\u3078\u3068\u3061\u308a\u306c" +
    "\u308b\u3092\u308f\u304b\u3088\u305f\u308c\u305d\u3064\u306d" +
    "\u306a\u3089\u3080\u3046\u3090\u306e\u304a\u304f\u3084\u307e" +
    "\u3051\u3075\u3053\u3048\u3066\u3042\u3055\u304d\u3086\u3081" +
    "\u307f\u3057\u3091\u3072\u3082\u305b\u3059",
    // Katakana I numbering.
    "\u30a4\u30ed\u30cf\u30cb\u30db\u30d8\u30c8\u30c1\u30ea\u30cc" +
    "\u30eb\u30f2\u30ef\u30ab\u30e8\u30bf\u30ec\u30bd\u30c4\u30cd" +
    "\u30ca\u30e9\u30e0\u30a6\u30f0\u30ce\u30aa\u30af\u30e4\u30de" +
    "\u30b1\u30d5\u30b3\u30a8\u30c6\u30a2\u30b5\u30ad\u30e6\u30e1" +
    "\u30df\u30b7\u30f1\u30d2\u30e2\u30bb\u30b9"
  };

  /**
   * Returns a character sequence the first character of which equals the
   * specified character.
   * @param ch character to be checked
   * @return character sequence or {@code null}
   */
  static String sequence(final int ch) {
    for(final String seq : SEQS) {
      if(ch == seq.charAt(0)) return seq;
    }
    return null;
  }

  /** Cases. */
  protected enum Case {
    /** Lower case. */ LOWER,
    /** Upper case. */ UPPER,
    /** Standard.   */ STANDARD
  }

  /**
   * Returns the zero base for the specified code point, or {@code -1}.
   * @param ch character
   * @return zero base
   */
  static int zeroes(final int ch) {
    for(final int zero : ZEROES) {
      if(ch >= zero && ch <= zero + 9) return zero;
    }
    return -1;
  }

  /**
   * Returns the character at the specified position, or {@code 0} if the
   * specified position is outside the string range.
   * @param in input
   * @param pos position
   * @return character
   */
  static int ch(final byte[] in, final int pos) {
    return pos >= 0 && pos < in.length ? cp(in, pos) : 0;
  }
}
