package org.basex.query.value.seq;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * A sequence that defines a sub-range of another sequence.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class SubSeq extends Seq {
  /** Underlying sequence. */
  private final Seq sub;
  /** Starting index in {@link #sub}. */
  private final long start;

  /**
   * Constructor.
   * @param sub underlying sequence
   * @param start starting index
   * @param length length of the subsequence
   */
  SubSeq(final Seq sub, final long start, final long length) {
    super(length, sub.type);
    this.sub = sub;
    this.start = start;
  }

  @Override
  public Item itemAt(final long pos) {
    return sub.itemAt(start + pos);
  }

  @Override
  protected Seq subSeq(final long pos, final long length, final QueryContext qc) {
    return new SubSeq(sub, start + pos, length);
  }
}
