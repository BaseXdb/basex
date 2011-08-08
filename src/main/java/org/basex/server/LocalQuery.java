package org.basex.server;

import static org.basex.util.Token.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.io.out.PrintOutput;
import org.basex.query.QueryException;

/**
 * This class defines all methods for iteratively evaluating queries locally.
 * All data is interpreted by the {@link QueryListener}.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class LocalQuery extends Query {
  /** Active query listener. */
  private final QueryListener qp;
  /** Buffer output; {@code null} if an {@link OutputStream} is specified. */
  private final ByteArrayOutputStream buf;
  /** Iterator flag. */
  private boolean ready;
  /** Owning local session. */
  private final LocalSession session;

  /**
   * Constructor. Query output will be returned by each called methods.
   * @param s owning session
   * @param q query string
   * @param ctx database context
   * @throws BaseXException query exception
   */
  LocalQuery(final LocalSession s, final String q, final Context ctx)
      throws BaseXException {
    session = s;
    buf = new ByteArrayOutputStream();
    try {
      qp = new QueryListener(q, PrintOutput.get(buf), ctx);
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }

  /**
   * Constructor. Query output will be written to the provided output stream.
   * All methods will return {@code null}.
   * @param s owning session
   * @param q query string
   * @param ctx database context
   * @param o output stream to write query output
   * @throws BaseXException query exception
   */
  LocalQuery(final LocalSession s, final String q, final Context ctx,
      final OutputStream o) throws BaseXException {
    session = s;
    buf = null;
    try {
      qp = new QueryListener(q, PrintOutput.get(o), ctx);
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public void bind(final String n, final Object v, final String t)
      throws BaseXException {
    try {
      qp.bind(n, v, t);
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public String init() throws BaseXException {
    try {
      qp.init();
    } catch(final Exception ex) {
      throw new BaseXException(ex);
    }
    return output();
  }

  @Override
  public boolean more() throws BaseXException {
    try {
      ready = true;
      return qp.next();
    } catch(final Exception ex) {
      throw new BaseXException(ex);
    }
  }

  @Override
  public String next() throws BaseXException {
    try {
      if(ready) ready = false;
      else qp.next();
    } catch(final Exception ex) {
      throw new BaseXException(ex);
    }
    return output();
  }

  @Override
  public String execute() throws BaseXException {
    try {
      qp.execute();
    } catch(final Exception ex) {
      throw new BaseXException(ex);
    }
    return output();
  }

  @Override
  public String info() throws BaseXException {
    return string(qp.info());
  }

  @Override
  public String close() throws BaseXException {
    session.removeQuery(this);
    return closeListener();
  }

  /**
   * Close the query listener.
   * @return output
   * @throws BaseXException IO exception
   */
  String closeListener() throws BaseXException {
    try {
      qp.close(false);
    } catch(final IOException ex) {
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
