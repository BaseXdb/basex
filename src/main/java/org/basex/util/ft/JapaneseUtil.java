package org.basex.util.ft;

import static org.basex.util.Token.*;

import org.basex.util.TokenBuilder;

/**
 * Japanese utility class.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Toshio HIRAI
 */
final class JapaneseUtil {
  /** Private constructor. */
  private JapaneseUtil() { }

  /**
   * Checks if the specified token only consists of KATAKANA characters.
   * @param s Japanese characters.
   * @return result of check
   */
  static boolean isKatakana(final byte[] s) {
    if(ascii(s)) return false;
    for(int p = 0; p < s.length; p += cl(s, p)) {
      final int c = cp(s, p);
      if(c < 0x30A1 || c > 0x30F6 && c != 0x30FC) return false;
    }
    return true;
  }

  /**
   * Checks if the specified token only consists of HIRAGANA characters.
   * @param s Japanese characters.
   * @return result of check
   */
  static boolean isHiragana(final byte[] s) {
    if(ascii(s)) return false;
    for(int p = 0; p < s.length; p += cl(s, p)) {
      final int c = cp(s, p);
      if(c < 0x3041 || c > 0x3096 && c != 0x30FC) return false;
    }
    return true;
  }

  /**
   * Convert to HANKAKU characters.
   *
   * @param s Japanese text
   * @return result of conversion(->HANKAKU)
   */
  static byte[] toHankaku(final byte[] s) {
    if(ascii(s)) return s;
    final TokenBuilder tb = new TokenBuilder(s.length);
    for(int p = 0; p < s.length; p += cl(s, p)) {
      final int c = cp(s, p);
      if(c >= 0xFF10 && c <= 0xFF19 || c >= 0xFF21 && c <= 0xFF3A
          || c >= 0xFF41 && c <= 0xFF5A) {
        tb.add(c - 0xFEE0);
      } else if(c == 0x3000) { // IDEOGRAPHIC SPACE
        tb.add(0x0020);
      } else if(c == 0xFF01) { // !
        tb.add(0x0021);
      } else if(c == 0x201D) { // "
        tb.add(0x0022);
      } else if(c == 0xFF03) { // #
        tb.add(0x0023);
      } else if(c == 0xFF04) { // $
        tb.add(0x0024);
      } else if(c == 0xFF05) { // %
        tb.add(0x0025);
      } else if(c == 0xFF06) { // &
        tb.add(0x0026);
      } else if(c == 0x2019) { // '
        tb.add(0x0027);
      } else if(c == 0xFF08) { // (
        tb.add(0x0028);
      } else if(c == 0xFF09) { // )
        tb.add(0x0029);
      } else if(c == 0xFF0A) { // *
        tb.add(0x002A);
      } else if(c == 0xFF0B) { // +
        tb.add(0x002B);
      } else if(c == 0xFF0C) { // ,
        tb.add(0x002C);
      } else if(c == 0xFF0D) { // -
        tb.add(0x002D);
      } else if(c == 0xFF0E) { // .
        tb.add(0x002E);
      } else if(c == 0xFF0F) { // /
        tb.add(0x002F);
      } else if(c == 0xFF1A) { // :
        tb.add(0x003A);
      } else if(c == 0xFF1B) { // ;
        tb.add(0x003B);
      } else if(c == 0xFF1C) { // <
        tb.add(0x003C);
      } else if(c == 0xFF1D) { // =
        tb.add(0x003D);
      } else if(c == 0xFF1E) { // >
        tb.add(0x003E);
      } else if(c == 0xFF1F) { // ?
        tb.add(0x003F);
      } else if(c == 0xFF20) { // @
        tb.add(0x0040);
      } else if(c == 0xFF3B) { // [
        tb.add(0x005B);
      } else if(c == 0xFFE5) { // \
        tb.add(0x005C);
      } else if(c == 0xFF3D) { // ]
        tb.add(0x005D);
      } else if(c == 0xFF3E) { // ^
        tb.add(0x005E);
      } else if(c == 0xFF3F) { // _
        tb.add(0x005F);
      } else if(c == 0xFF40) { // `
        tb.add(0x0060);
      } else if(c == 0xFF5B) { // {
        tb.add(0x007B);
      } else if(c == 0xFF5C) { // |
        tb.add(0x007C);
      } else if(c == 0xFF5D) { // }
        tb.add(0x007D);
      } else if(c == 0xFF5E) { // ~
        tb.add(0x007E);
      } else {
        tb.add(c);
      }
    }
    return tb.finish();
  }

  /**
   * Converts characters to HIRAGANA.
   * @param s Japanese text
   * @return result of conversion(->HANKAKU)
   */
  static byte[] toHiragana(final byte[] s) {
    if(ascii(s)) return s;
    final TokenBuilder tb = new TokenBuilder(s.length);
    for(int p = 0; p < s.length; p += cl(s, p)) {
      int c = cp(s, p);
      if(c >= 0x30A1 && c <= 0x30F6) c -= 0x60;
      tb.add(c);
    }
    return tb.finish();
  }

  /**
   * Converts characters to KATAKANA.
   * @param s Japanese text
   * @return result of conversion(->KATAKANA)
   */
  static byte[] toKatakana(final byte[] s) {
    if(ascii(s)) return s;
    final TokenBuilder tb = new TokenBuilder(s.length);
    for(int p = 0; p < s.length; p += cl(s, p)) {
      int c = cp(s, p);
      if(c >= 0x3041 && c <= 0x3096) c += 0x60;
      tb.add(c);
    }
    return tb.finish();
  }
}
