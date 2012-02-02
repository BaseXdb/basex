package org.basex.query.expr;

import static org.basex.query.QueryText.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import java.util.Locale;

import org.basex.data.Data;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.IndexIterator;
import org.basex.index.RangeToken;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.util.IndexContext;
import org.basex.util.InputInfo;
import org.basex.util.TokenBuilder;

/**
 * This index class retrieves range values from the index.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class RangeAccess extends Simple {
  /** Index type. */
  final RangeToken ind;
  /** Index context. */
  private final IndexContext ictx;

  /**
   * Constructor.
   * @param ii input info
   * @param t index reference
   * @param ic index context
   */
  RangeAccess(final InputInfo ii, final RangeToken t, final IndexContext ic) {
    super(ii);
    ind = t;
    ictx = ic;
    type = SeqType.NOD_ZM;
  }

  @Override
  public Iter iter(final QueryContext ctx) {
    final Data data = ictx.data;
    final byte kind = ind.type() == IndexType.TEXT ? Data.TEXT : Data.ATTR;

    return new Iter() {
      final IndexIterator it = data.iter(ind);
      @Override
      public Item next() {
        return it.more() ? new DBNode(data, it.next(), kind) : null;
      }
    };
  }

  @Override
  public boolean iterable() {
    return ictx.iterable;
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.emptyElement(this, DATA, token(ictx.data.meta.name),
        MIN, token(ind.min), MAX, token(ind.max),
        TYP, token(ind.ind.toString()));
  }

  @Override
  public String toString() {
    return new TokenBuilder(DB).add(':').
      addExt(ind.type().toString().toLowerCase(Locale.ENGLISH)).add("-range(").
      addExt(ind.min).add(SEP).addExt(ind.max).add(')').toString();
  }
}
