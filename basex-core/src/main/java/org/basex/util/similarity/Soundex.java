package org.basex.util.similarity;

import static org.basex.util.Token.*;

import org.basex.util.*;
import org.basex.util.list.*;

/**
 * <p>Basic Soundex algorithm, developed by Robert C. Russell and Margaret King Odell.</p>
 *
 * <p>The implementation has been inspired by the Apache Commons Codec algorithms
 * (http://commons.apache.org/proper/commons-codec/).</p>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Soundex {
  /** Private constructor, preventing instantiation. */
  private Soundex() { }

  /** Mapping for 26 ASCII letters (0: no encoding). */
  private static final int[] MAPPING =
      new TokenParser(token("01230120022455012623010202")).toArray();

  /**
   * Computes the Soundex value for the specified codepoints.
   * @param cps codepoint array
   * @return Soundex value
   */
  public static int[] encode(final int[] cps) {
    // normalize input to ascii characters (ignore all others)
    final IntList tmp = new IntList(cps.length);
    for(final int cp : cps) {
      final int c = uc(cp);
      if(c >= 'A' && c <= 'Z') tmp.add(c);
    }

    final int[] out = { '0', '0', '0', '0' }, in = tmp.finish();
    final int is = in.length;
    if(is > 0) {
      out[0] = in[0];
      for(int op = 1, ip = 0, lastCode = map(in, ip++, MAPPING); ip < is && op < 4;) {
        final int code = map(in, ip++, MAPPING);
        if(code != 0) {
          if(code != '0' && code != lastCode) out[op++] = code;
          lastCode = code;
        }
      }
    }
    return out;
  }

  /**
   * Maps a codepoint to a Soundex code.
   * @param cps codepoint array
   * @param index array index
   * @param mapping Soundex mapping
   * @return code
   */
  private static int map(final int[] cps, final int index, final int[] mapping) {
    final int c = mapping[cps[index] - 'A'];
    if(index > 1 && c != '0') {
      final int pc = cps[index - 1];
      if(pc == 'H' || pc == 'W') {
        final int ppc = cps[index - 2];
        if(ppc == 'H' || ppc == 'W' || mapping[ppc - 'A'] == c) return 0;
      }
    }
    return c;
  }
}
