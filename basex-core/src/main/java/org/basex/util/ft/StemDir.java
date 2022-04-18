package org.basex.util.ft;

import java.io.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple stemming directory for full-text requests.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class StemDir extends TokenMap {
  /**
   * Reads a stop words file.
   * @param fl file reference
   * @return success flag
   */
  public boolean read(final IO fl) {
    try {
      for(final String line : fl.string().split("\\n")) {
        final String[] terms = line.split("\\s+");
        final int tl = terms.length;
        for(int t = 1; t < tl; t++) put(terms[t], terms[0]);
      }
      return true;
    } catch(final IOException ex) {
      Util.debug(ex);
      return false;
    }
  }

  /**
   * Returns a stemmed word or the word itself.
   * @param word word to be stemmed
   * @return resulting token
   */
  byte[] stem(final byte[] word) {
    final byte[] token = get(word);
    return token != null ? token : word;
  }
}
