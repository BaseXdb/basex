package org.basex.util;

import static org.basex.util.FTToken.*;
import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.text.*;

import org.junit.jupiter.api.*;

/**
 * Full-text token tests.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FTTokenTest {
  /** Characters that denote more than a single letter are expanded. */
  @Test public void expand() {
    assertEquals("ss", fold("ß"));
    assertEquals("SS", fold("ẞ"));
    assertEquals("AEae", fold("Ææ"));
    assertEquals("THth", fold("Þþ"));
    assertEquals("ffi", fold("ﬃ"));
  }

  /** All other characters are reduced to their base letter. */
  @Test public void reduce() {
    assertEquals("oldd", fold("øłđð"));
    assertEquals("Yy", fold("Ȳȳ"));
    assertEquals("aou", fold("ǎǒǔ"));
  }

  /** Folding must not depend on the normalization form of the input. */
  @Test public void normalizationForm() {
    for(int cp = 0xC0; cp <= 0xFFFF; cp++) {
      // Hangul syllables decompose into jamo, CJK compatibility ideographs into unified ones
      if(cp >= 0xAC00 && cp <= 0xD7A3 || cp >= 0xF900 && cp <= 0xFAFF) continue;
      if(!Character.isLetter(cp)) continue;

      final String string = String.valueOf((char) cp);
      final String nfd = Normalizer.normalize(string, Normalizer.Form.NFD);
      if(!nfd.equals(string)) {
        assertEquals(fold(string), fold(nfd), "U+" + Integer.toHexString(cp).toUpperCase());
      }
    }
  }

  /**
   * Removes diacritics from a string.
   * @param string string
   * @return folded string
   */
  private static String fold(final String string) {
    return string(noDiacritics(token(string)));
  }
}
