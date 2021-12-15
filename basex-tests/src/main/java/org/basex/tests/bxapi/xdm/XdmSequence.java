package org.basex.tests.bxapi.xdm;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Wrapper for representing XQuery values.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class XdmSequence extends XdmValue {
  /** Wrapped sequence. */
  final Seq seq;

  /**
   * Constructor.
   * @param seq sequence
   */
  XdmSequence(final Seq seq) {
    this.seq = seq;
  }

  @Override
  public SeqType getType() {
    return seq.seqType();
  }

  @Override
  public int size() {
    return (int) seq.size();
  }

  @Override
  public Iterator<XdmItem> iterator() {
    return new Iterator<XdmItem>() {
      private final int ss = size();
      private int s;

      @Override
      public boolean hasNext() {
        return s < ss;
      }

      @Override
      public XdmItem next() {
        return XdmItem.get(seq.itemAt(s++));
      }

      @Override
      public void remove() {
        throw Util.notExpected();
      }
    };
  }

  @Override
  public Value internal() {
    return seq;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("(");
    for(final XdmItem item : this) {
      if(sb.length() != 1) sb.append(',');
      sb.append(item);
    }
    return sb.append(')').toString();
  }
}
