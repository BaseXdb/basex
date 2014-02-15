package org.basex.tests.bxapi.xdm;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Wrapper for representing XQuery values.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
final class XdmSequence extends XdmValue {
  /** Wrapped sequence. */
  final Seq sequence;

  /**
   * Constructor.
   * @param seq sequence
   */
  XdmSequence(final Seq seq) {
    sequence = seq;
  }

  @Override
  public SeqType getType() {
    return sequence.type();
  }

  @Override
  public int size() {
    return (int) sequence.size();
  }

  @Override
  public Iterator<XdmItem> iterator() {
    return new Iterator<XdmItem>() {
      private int c;

      @Override
      public boolean hasNext() {
        return c < sequence.size();
      }

      @Override
      public XdmItem next() {
        return XdmItem.get(sequence.itemAt(c++));
      }

      @Override
      public void remove() {
        throw Util.notExpected();
      }
    };
  }

  @Override
  public Value internal() {
    return sequence;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("(");
    for(final XdmItem it : this) {
      if(sb.length() != 1) sb.append(',');
      sb.append(it.toString());
    }
    return sb.append(')').toString();
  }
}
