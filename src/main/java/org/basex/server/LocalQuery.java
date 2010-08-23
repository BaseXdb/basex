package org.basex.server;

import java.io.IOException;
import java.io.OutputStream;
import org.basex.core.BaseXException;
import org.basex.io.ArrayOutput;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;

/**
 * This class defines all methods for iteratively evaluating queries with the
 * local architecture.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class LocalQuery extends Query {
  /** Query processor. */
  private final QueryProcessor qp;
  /** Query iterator. */
  private Iter iter;
  /** Next item. */
  private Item item;
  
  /**
   * Standard constructor.
   * @param proc query processor
   */
  public LocalQuery(final QueryProcessor proc) {
    qp = proc;
  }

  @Override
  public boolean more() throws BaseXException {
    try {
      if(iter == null) iter = qp.iter();
      item = iter.next();
      return item != null;
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public void next(final OutputStream out) throws BaseXException {
    try {
      item.serialize(qp.getSerializer(out));
    } catch(final Exception ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public String next() throws BaseXException {
    final ArrayOutput out = new ArrayOutput();
    next(out);
    return out.toString();
  }

  @Override
  public void close() throws BaseXException {
    try {
      qp.close();
    } catch(final IOException ex) {
      throw new BaseXException(ex);
    }
  }
}
