package org.basex.index.ft;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.random.*;

/**
 * This class provides temporary access to sorted list data.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Sebastian Gath
 */
final class FTList {
  /** Empty integer array. */
  private static final int[] NOINTS = {};

  /** Storing pre and pos values for each token. */
  private final DataAccess dat;
  /** Structure file. */
  private final IOFile files;
  /** Data file. */
  private final IOFile filed;
  /** Wasted flag. */
  private boolean wasted;

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

  /** Indexed tokens. */
  private final DataAccess str;

  /** Current data size. */
  int size;
  /** Next token. */
  byte[] tok;
  /** Next pre values. */
  int[] prv;
  /** Next pos values. */
  int[] pov;

  /**
   * Constructor, initializing the index structure.
   * @param data data
   * @param prefix prefix
   * @throws IOException I/O exception
   */
  FTList(final Data data, final int prefix) throws IOException {
    files = data.meta.dbfile(DATAFTX + prefix + 'y');
    filed = data.meta.dbfile(DATAFTX + prefix + 'z');
    str = new DataAccess(files);
    dat = new DataAccess(filed);
    tp = new int[data.meta.maxlen + 3];
    final int tl = tp.length;
    for(int t = 0; t < tl; t++) tp[t] = -1;
    sizes = data.meta.dbfile(DATAFTX + prefix + 'x');
    try(final DataAccess li = new DataAccess(sizes)) {
      int is = li.readNum();
      while(--is >= 0) {
        final int p = li.readNum();
        tp[p] = li.read4();
      }
      tp[tl - 1] = (int) str.length();
    }
    next();
  }

  /**
   * Checks if more tokens are found.
   */
  void next() {
    if(wasted) return;

    tok = token();
    if(tok.length == 0) {
      wasted = true;
      prv = NOINTS;
      pov = NOINTS;
      close();
    } else {
      prv = new int[size];
      pov = new int[size];
      for(int j = 0; j < size; ++j) {
        prv[j] = dat.readNum();
        pov[j] = dat.readNum();
      }
    }
  }

  /**
   * Closes and deletes the input files.
   */
  private void close() {
    str.close();
    dat.close();
    files.delete();
    filed.delete();
    sizes.delete();
  }

  /**
   * Returns next token.
   * @return byte[] token
   */
  private byte[] token() {
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
