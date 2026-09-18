package org.basex.index.ft;

import java.io.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.io.out.DataOutput;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Writes the files of a full-text index segment (see {@link FTIndex} for the format).
 * Tokens must be written in ascending order of their length and their bytes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class FTSegmentWriter implements SegmentWriter {
  /** Token length index. */
  private final DataOutput outX;
  /** Tokens and references. */
  private final DataOutput outY;
  /** ID/POS references. */
  private final DataOutput outZ;
  /** Token lengths and offsets of the first tokens with these lengths. */
  private final IntList lengths = new IntList();
  /** Length of the last written token. */
  private int length;

  /**
   * Constructor.
   * @param data data reference
   * @param prefix file prefix of the segment
   * @throws IOException I/O exception
   */
  FTSegmentWriter(final Data data, final String prefix) throws IOException {
    final DataOutput[] outs = new DataOutput[3];
    try {
      for(int o = 0; o < 3; o++) {
        outs[o] = new DataOutput(data.meta.dbFile(prefix + FTIndex.FILES.charAt(o)));
      }
    } catch(final IOException ex) {
      for(final DataOutput out : outs) {
        if(out != null) out.close();
      }
      throw ex;
    }
    outX = outs[0];
    outY = outs[1];
    outZ = outs[2];
  }

  /**
   * Writes a token with compressed references.
   * @param token token
   * @param refs compressed ID or PRE values and positions (see {@link Num}): id1 pos1 id2 pos2 ...
   * @throws IOException I/O exception
   */
  void write(final byte[] token, final byte[] refs) throws IOException {
    final int rs = Num.size(refs);
    int count = 0;
    for(int r = 4; r < rs; count++) {
      r += Num.length(refs, r);
      r += Num.length(refs, r);
    }
    entry(token, count);
    outZ.write(refs, 4, rs - 4);
  }

  @Override
  public void write(final byte[] token, final IntList ids, final IntList poss)
      throws IOException {
    final int count = ids.size();
    entry(token, count);
    for(int c = 0; c < count; c++) {
      outZ.writeNum(ids.get(c));
      outZ.writeNum(poss.get(c));
    }
  }

  /**
   * Writes a token entry.
   * @param token token
   * @param count number of references
   * @throws IOException I/O exception
   */
  private void entry(final byte[] token, final int count) throws IOException {
    final int tl = token.length;
    if(length < tl) {
      length = tl;
      lengths.add(tl);
      lengths.add((int) outY.size());
    }
    outY.writeBytes(token);
    outY.write5(outZ.size());
    outY.write4(count);
  }

  @Override
  public void close() throws IOException {
    try {
      final int ls = lengths.size();
      outX.writeNum(ls / 2);
      for(int l = 0; l < ls; l += 2) {
        outX.writeNum(lengths.get(l));
        outX.write4(lengths.get(l + 1));
      }
    } finally {
      outX.close();
      outY.close();
      outZ.close();
    }
  }
}
