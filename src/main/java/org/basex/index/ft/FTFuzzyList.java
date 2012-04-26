package org.basex.index.ft;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.random.*;

/**
 * This class provides access to a sorted token list.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Sebastian Gath
 */
final class FTFuzzyList extends FTList {
  /** Size file. */
  private final IOFile sizes;
  /** Token positions. */
  private final int[] tp;
  /** Pointer on current token length. */
  private int ctl;
  /** Pointer on next token length. */
  private int ntl;
  /** Number of written bytes for tokens. */
  private int ptok;

  /**
   * Constructor, initializing the index structure.
   * @param d data reference
   * @param cf current file
   * @throws IOException I/O Exception
   */
  FTFuzzyList(final Data d, final int cf) throws IOException {
    super(d, cf, 'y', 'z');
    tp = new int[d.meta.maxlen + 3];
    for(int i = 0; i < tp.length; ++i) tp[i] = -1;
    sizes = d.meta.dbfile(DATAFTX + cf + 'x');
    final DataAccess li = new DataAccess(sizes);
    int is = li.readNum();
    while(--is >= 0) {
      final int p = li.readNum();
      tp[p] = li.read4();
    }
    tp[tp.length - 1] = (int) str.length();
    li.close();
    next();
  }

  @Override
  void close() {
    super.close();
    sizes.delete();
  }

  @Override
  protected byte[] token() {
    if(tp[tp.length - 1] == ptok) return EMPTY;
    if(tp[ntl] == ptok || ntl == 0) {
      ++ctl;
      while(tp[ctl] == -1) ++ctl;
      ntl = ctl + 1;
      while(tp[ntl] == -1) ++ntl;
    }
    if(ctl == tp.length) return EMPTY;

    final byte[] t = str.readBytes(ptok, ctl);
    // skip pointer
    size = str.read4(str.cursor() + 5);
    // position will always fit in an integer...
    ptok = (int) str.cursor();
    return t;
  }
}
