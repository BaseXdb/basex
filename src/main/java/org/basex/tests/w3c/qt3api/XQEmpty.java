package org.basex.tests.w3c.qt3api;

import java.util.Iterator;

import org.basex.query.item.Empty;
import org.basex.query.item.SeqType;
import org.basex.query.item.Value;
import org.basex.util.Util;

/**
 * Wrapper for representing an empty sequence.
 */
public final class XQEmpty extends XQValue {
  /** Empty sequence. */
  public static final XQEmpty EMPTY = new XQEmpty();

  /**
   * Private Constructor.
   */
  private XQEmpty() { }

  @Override
  public SeqType getType() {
    return SeqType.EMP;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Iterator<XQItem> iterator() {
    return new Iterator<XQItem>() {
      @Override
      public boolean hasNext() {
        return false;
      }

      @Override
      public XQItem next() {
        return null;
      }

      @Override
      public void remove() {
        Util.notexpected();
      }
    };
  }

  @Override
  public Value internal() {
    return Empty.SEQ;
  }

  @Override
  public String toString() {
    return "()";
  }
}
