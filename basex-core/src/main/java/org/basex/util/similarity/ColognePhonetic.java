package org.basex.util.similarity;

import static org.basex.util.Token.*;

import org.basex.util.list.*;

/**
 * <p>Cologne Phonetic algorithm. Based on Hans Joachim Postel publication: "Die Kölner Phonetik.
 * Ein Verfahren zur Identifizierung von Personennamen auf der Grundlage der Gestaltanalyse".
 * More details: {@code http://de.wikipedia.org/wiki/K%C3%B6lner_Phonetik}.</p>
 *
 * <p>The implementation has been inspired by the Apache Commons Codec algorithms
 * (http://commons.apache.org/proper/commons-codec/).</p>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ColognePhonetic {
  /** Private constructor, preventing instantiation. */
  private ColognePhonetic() { }

  /** Letter array. */
  private static final byte[] AHKLOQRUX = token("ahkloqrux");
  /** Letter array. */
  private static final byte[] AEIJOUY = token("aeijouy");
  /** Letter array. */
  private static final byte[] AHOUKQX = token("ahoukqx");
  /** Letter array. */
  private static final byte[] WFPV = token("wfpv");
  /** Letter array. */
  private static final byte[] CKQ = token("ckq");
  /** Letter array. */
  private static final byte[] GKQ = token("gkq");
  /** Letter array. */
  private static final byte[] SCZ = token("scz");
  /** Letter array. */
  private static final byte[] TDX = token("tdx");

  /**
   * Encodes a codepoint array.
   * @param cps input codepoints
   * @return encoded codepoints
   */
  public static int[] encode(final int[] cps) {
    // normalize input
    final IntList tmp = new IntList(cps.length);
    for(final int cp : cps) {
      int c = lc(cp);
      if(c == '\u00E4') c = 'a';
      else if(c == '\u00F6') c = 'o';
      else if(c == '\u00FC') c = 'u';
      else if(c == '\u00DF') c = 's';
      tmp.add(c);
    }

    final IntList out = new IntList();
    final int[] in = tmp.finish();
    final int il = in.length;

    int lastCp = '-', lastCode = '/';
    for(int ip = il; ip > 0;) {
      final int cp = in[il - ip--];
      final int nextCp = ip > 0 ? in[il - ip] : '-';

      final int code;
      if(contains(AEIJOUY, cp)) {
        code = '0';
      } else if(cp == 'h' || cp < 'a' || cp > 'z') {
        if(lastCode == '/') continue;
        code = '-';
      } else if(cp == 'b' || cp == 'p' && nextCp != 'h') {
        code = '1';
      } else if((cp == 'd' || cp == 't') && !contains(SCZ, nextCp)) {
        code = '2';
      } else if(contains(WFPV, cp)) {
        code = '3';
      } else if(contains(GKQ, cp)) {
        code = '4';
      } else if(cp == 'x' && !contains(CKQ, lastCp)) {
        code = '4';
        in[il - ++ip] = 's';
      } else if(cp == 's' || cp == 'z') {
        code = '8';
      } else if(cp == 'c') {
        if(lastCode == '/') {
          code = contains(AHKLOQRUX, nextCp) ? '4' : '8';
        } else {
          code = lastCp == 's' || lastCp == 'z' || !contains(AHOUKQX, nextCp) ? '8' : '4';
        }
      } else if(contains(TDX, cp)) {
        code = '8';
      } else if(cp == 'r') {
        code = '7';
      } else if(cp == 'l') {
        code = '5';
      } else if(cp == 'm' || cp == 'n') {
        code = '6';
      } else {
        code = cp;
      }

      if(code != '-' && (lastCode != code && (code != '0' || lastCode == '/') || code > '8')) {
        out.add(code);
      }

      lastCp = cp;
      lastCode = code;
    }

    return out.finish();
  }
}
