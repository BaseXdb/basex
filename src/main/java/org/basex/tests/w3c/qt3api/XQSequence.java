package org.basex.tests.w3c.qt3api;

import java.util.Iterator;
import org.basex.query.item.Seq;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.util.Util;

/**
 * Wrapper for representing XQuery values.
 */
final class XQSequence extends XQValue {
  /** Wrapped sequence. */
  final Seq sequence;

  /**
   * Constructor.
   * @param seq sequence
   */
  XQSequence(final Seq seq) {
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
  public Iterator<XQItem> iterator() {
    return new Iterator<XQItem>() {
      private int c;

      @Override
      public boolean hasNext() {
        return c < sequence.size();
      }

      @Override
      public XQItem next() {
        return XQItem.get(sequence.itemAt(c++));
      }

      @Override
      public void remove() {
        Util.notexpected();
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
    for(final XQItem it : this) {
      if(sb.length() != 1) sb.append(',');
      sb.append(it.toString());
    }
    return sb.append(")").toString();
  }
}
