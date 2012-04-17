package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.data.*;
import org.basex.index.*;
import org.basex.index.IndexToken.IndexType;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * This index class retrieves ranges from a value index.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class StringRangeAccess extends IndexAccess {
  /** Index token. */
  final StringRange sr;

  /**
   * Constructor.
   * @param ii input info
   * @param t index reference
   * @param ic index context
   */
  public StringRangeAccess(final InputInfo ii, final StringRange t,
      final IndexContext ic) {
    super(ic, ii);
    sr = t;
  }

  @Override
  public AxisIter iter(final QueryContext ctx) {
    final Data data = ictx.data;
    final boolean text = sr.type == IndexType.TEXT;
    final byte kind = text ? Data.TEXT : Data.ATTR;
    final int ml = data.meta.maxlen;
    final IndexIterator ii = sr.min.length <= ml && sr.max.length <= ml &&
        (text ? data.meta.textindex : data.meta.attrindex) ? data.iter(sr) : scan();

    return new AxisIter() {
      @Override
      public ANode next() {
        return ii.more() ? new DBNode(data, ii.next(), kind) : null;
      }
    };
  }

  /**
   * Returns scan-based iterator.
   * @return node iterator
   */
  private IndexIterator scan() {
    return new IndexIterator() {
      final boolean text = sr.type == IndexType.TEXT;
      final byte kind = text ? Data.TEXT : Data.ATTR;
      final Data data = ictx.data;
      int pre = -1;

      @Override
      public double score() {
        return -1;
      }
      @Override
      public int next() {
        return pre;
      }
      @Override
      public boolean more() {
        while(++pre < data.meta.size) {
          if(data.kind(pre) != kind) continue;
          final byte[] t = data.text(pre, text);
          final int mn = Token.diff(t, sr.min);
          final int mx = Token.diff(t, sr.max);
          if(mn >= (sr.mni ? 0 : 1) && mx <= (sr.mxi ? 0 : 1)) return true;
        }
        return false;
      }
    };
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, DATA, token(ictx.data.meta.name),
        MIN, sr.min, MAX, sr.max, TYP, token(sr.type.toString()));
  }

  @Override
  public String toString() {
    return (sr.type == IndexType.TEXT ? Function._DB_TEXT_RANGE :
      Function._DB_ATTRIBUTE_RANGE).get(info, Str.get(ictx.data.meta.name),
          Str.get(sr.min), Str.get(sr.max)).toString();
  }
}
