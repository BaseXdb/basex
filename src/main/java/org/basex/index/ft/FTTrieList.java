package org.basex.index.ft;

import java.io.IOException;
import org.basex.data.Data;

/**
 * This class provides access to a sorted token list.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Sebastian Gath
 */
final class FTTrieList extends FTList {
  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param cf current file
   * @throws IOException I/O Exception
   */
  FTTrieList(final Data d, final int cf) throws IOException {
    super(d, cf, 'a', 'b');
    next();
  }

  @Override
  protected byte[] token() {
    final byte[] t = str.readToken();
    if(t.length != 0) size = str.read4();
    return t;
  }
}
