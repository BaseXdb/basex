package org.basex.util;

import java.text.*;
import java.text.Normalizer.Form;
import java.util.*;

/**
 * Unicode normalization forms.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public enum NormalizationForm {
  /** NFC.              */ NFC(Form.NFC),
  /** NFD.              */ NFD(Form.NFD),
  /** NFKC.             */ NFKC(Form.NFKC),
  /** NFKD.             */ NFKD(Form.NFKD),
  /** Fully normalized. */ FULLY_NORMALIZED(Form.NFC);

  /** Combining acute accent (combining class 230). */
  private static final char ACUTE = 0x301;
  /** Combining tilde overlay (combining class 1). */
  private static final char OVERLAY = 0x334;
  /** Second characters of canonical decompositions (lazily initialized). */
  private static BitSet seconds;

  /** Java normalization form. */
  private final Form form;

  /**
   * Constructor.
   * @param form Java normalization form
   */
  NormalizationForm(final Form form) {
    this.form = form;
  }

  /**
   * Normalizes a token.
   * @param token token
   * @return normalized token (identical to the input if nothing was changed)
   */
  public byte[] normalize(final byte[] token) {
    final byte[] tkn = this == FULLY_NORMALIZED && token.length > 0 &&
      composing(Token.cp(token, 0)) ? Token.concat(Token.cpToken(' '), token) : token;
    return Token.normalize(tkn, form);
  }

  /**
   * Checks if the specified character is a composing character.
   * @param cp code point
   * @return result of check
   */
  private static boolean composing(final int cp) {
    // non-zero combining class: character is reordered relative to marks of class 230 or 1
    final String nfd = Normalizer.normalize(Character.toString(cp), Form.NFD);
    final String ch = Character.toString(nfd.codePointAt(0));
    return !Normalizer.normalize(ACUTE + ch, Form.NFD).equals(ACUTE + ch) ||
      !Normalizer.normalize(ch + OVERLAY, Form.NFD).equals(ch + OVERLAY) || seconds().get(cp);
  }

  /**
   * Returns the second characters of the canonical decompositions of all primary composites.
   * @return characters
   */
  private static synchronized BitSet seconds() {
    if(seconds == null) {
      final BitSet set = new BitSet();
      for(int cp = 0; cp <= Character.MAX_CODE_POINT; cp++) {
        final String ch = Character.toString(cp), nfd = Normalizer.normalize(ch, Form.NFD);
        if(nfd.codePointCount(0, nfd.length()) > 1 &&
            Normalizer.normalize(nfd, Form.NFC).equals(ch)) {
          set.set(nfd.codePointBefore(nfd.length()));
        }
      }
      seconds = set;
    }
    return seconds;
  }

  @Override
  public String toString() {
    return name().replace('_', '-');
  }
}
