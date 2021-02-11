package org.basex.util.ft;

import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Simple stemming directory for full-text requests.
 *
 * @author BaseX Team 2005-21, BSD License
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
      for(final byte[] sl : split(fl.read(), '\n')) {
        byte[] val = null;
        for(final byte[] st : distinctTokens(sl)) {
          if(val == null) val = st;
          else put(st, val);
        }
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
    final byte[] sn = get(word);
    return sn != null ? sn : word;
  }
}
