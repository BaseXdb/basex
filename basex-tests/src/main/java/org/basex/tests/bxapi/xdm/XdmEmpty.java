package org.basex.tests.bxapi.xdm;

import java.util.*;

import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Wrapper for representing an empty sequence.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class XdmEmpty extends XdmValue {
  /** Empty sequence. */
  public static final XdmEmpty EMPTY = new XdmEmpty();

  /**
   * Private Constructor.
   */
  private XdmEmpty() { }

  @Override
  public SeqType getType() {
    return SeqType.EMP;
  }

  @Override
  public int size() {
    return 0;
  }

  @Override
  public Iterator<XdmItem> iterator() {
    return new Iterator<XdmItem>() {
      @Override
      public boolean hasNext() {
        return false;
      }

      @Override
      public XdmItem next() {
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
