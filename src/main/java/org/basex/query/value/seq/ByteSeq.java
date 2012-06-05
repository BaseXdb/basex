package org.basex.query.value.seq;

import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.util.*;

/**
 * Sequence, containing at least two bytes.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ByteSeq extends Seq {
  /** Byte array. */
  private final byte[] bytes;
  /** Sequence type. */
  private SeqType seq = SeqType.get(AtomType.BYT, Occ.ONE_MORE);

  /**
   * Constructor.
   * @param b bytes
   */
  public ByteSeq(final byte[] b) {
    super(b.length);
    bytes = b;
  }

  @Override
  public Item ebv(final QueryContext ctx, final InputInfo ii) throws QueryException {
    throw CONDTYPE.thrw(ii, this);
  }

  @Override
  public SeqType type() {
    return seq;
  }

  @Override
  public boolean iterable() {
    return false;
  }

  @Override
  public boolean sameAs(final Expr cmp) {
    return cmp instanceof ByteSeq && Token.eq(bytes, ((ByteSeq) cmp).bytes);
  }

  @Override
  public int writeTo(final Item[] arr, final int start) {
    for(int i = 0; i < size; i++) arr[start + i] = new Int(bytes[i], AtomType.BYT);
    return (int) size;
  }

  @Override
  public Item itemAt(final long pos) {
    return new Int(bytes[(int) pos], AtomType.BYT);
  }

  @Override
  public boolean homogenous() {
    return true;
  }

  /**
   * Returns the internal array.
   * @return content
   */
  public byte[] bytes() {
    return bytes;
  }
}
