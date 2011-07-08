package org.basex.server;

import static org.basex.util.Token.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.io.PrintOutput;
import org.basex.query.QueryException;

/**
 * This class defines all methods for iteratively evaluating queries locally.
 * All data is interpreted by the {@link QueryProcess}.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class LocalQuery extends Query {
  /** Active query process. */
  private final QueryProcess qp;
  /** Buffer output; {@code null} if an {@link OutputStream} is specified. */
  private final ByteArrayOutputStream buf;
  /** Iterator flag. */
  private boolean ready;

  /**
   * Constructor. Query output will be returned by each called methods.
   * @param q query string
   * @param ctx database context
   * @throws BaseXException query exception
   */
  public LocalQuery(final String q, final Context ctx) throws BaseXException {
    buf = new ByteArrayOutputStream();
    try {
      qp = new QueryProcess(q, PrintOutput.get(buf), ctx);
    } catch(QueryException ex) {
      throw new BaseXException(ex);
    }
  }

  /**
   * Constructor. Query output will be written to the provided output stream.
   * All methods will return {@code null}.
   * @param q query string
   * @param ctx database context
   * @param o output stream to write query output
   * @throws BaseXException query exception
   */
  public LocalQuery(final String q, final Context ctx, final OutputStream o)
      throws BaseXException {
    buf = null;
    try {
      qp = new QueryProcess(q, PrintOutput.get(o), ctx);
    } catch(QueryException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public void bind(final String n, final String v, final String t)
      throws BaseXException {
    try {
      qp.bind(n, v, t);
    } catch(QueryException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public String init() throws BaseXException {
    try {
      qp.init();
    } catch(Exception ex) {
      throw new BaseXException(ex);
    }
    return output();
  }

  @Override
  public boolean more() throws BaseXException {
    try {
      ready = true;
      return qp.next();
    } catch(Exception ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public String next() throws BaseXException {
    try {
      if(ready) ready = false;
      else qp.next();
    } catch(Exception ex) {
      throw new BaseXException(ex);
    }
    return output();
  }

  @Override
  public String execute() throws BaseXException {
    try {
      qp.execute();
    } catch(Exception ex) {
      throw new BaseXException(ex);
    }
    return output();
  }

  @Override
  public String info() throws BaseXException {
    try {
      qp.info();
    } catch(IOException ex) {
      throw new BaseXException(ex);
    }
    return output();
  }

  @Override
  public String close() throws BaseXException {
    try {
      qp.close(false);
    } catch(IOException ex) {
      throw new BaseXException(ex);
    }
    return output();
  }

  /**
   * Query output.
   * @return {@code null} if query output is directly sent to an output stream
   */
  private String output() {
    // if another output stream is specified, the query process has already
    // written the data, so there is nothing to output
    if(buf == null) return null;
    final String result = string(buf.toByteArray());
    buf.reset();
    return result;
  }
}
