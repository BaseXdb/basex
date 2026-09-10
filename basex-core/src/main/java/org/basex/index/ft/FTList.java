package org.basex.index.ft;

import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import org.basex.data.*;
import org.basex.io.random.*;

/**
 * This class provides temporary access to sorted list data.
 *
 * @author BaseX Team, BSD License
 * @author Sebastian Gath
 */
final class FTList {
  /** Empty integer array. */
  private static final int[] NOINTS = {};

  /** Storing PRE and POS values for each token. */
  private final DataAccess dat;
  /** Wasted flag. */
  private boolean wasted;

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
  byte[] token;
  /** Next PRE values. */
  int[] prv;
  /** Next pos values. */
  int[] pov;

  /**
   * Constructor, initializing the index structure.
   * @param data data
   * @param prefix file prefix of the index structure
   * @throws IOException I/O exception
   */
  FTList(final Data data, final String prefix) throws IOException {
    str = new DataAccess(data.meta.dbFile(prefix + 'y'));
    dat = new DataAccess(data.meta.dbFile(prefix + 'z'));
    tp = new int[data.meta.maxlen + 3];
    final int tl = tp.length;
    Arrays.fill(tp, 0, tl, -1);
    try(DataAccess li = new DataAccess(data.meta.dbFile(prefix + 'x'))) {
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

    token = token();
    if(token.length == 0) {
      wasted = true;
      prv = NOINTS;
      pov = NOINTS;
      close();
    } else {
      prv = new int[size];
      pov = new int[size];
      for(int j = 0; j < size; j++) {
        prv[j] = dat.readNum();
        pov[j] = dat.readNum();
      }
    }
  }

  /**
   * Closes the input files.
   */
  private void close() {
    str.close();
    dat.close();
  }

  /**
   * Returns next token.
   * @return byte[] token
   */
  private byte[] token() {
    if(tp[tp.length - 1] == ptok) return EMPTY;
    if(tp[ntl] == ptok || ntl == 0) {
      do ++ctl; while(tp[ctl] == -1);
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
