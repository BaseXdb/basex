package org.basex.server;

import java.io.IOException;
import java.io.OutputStream;

import org.basex.core.Context;
import org.basex.io.out.ArrayOutput;
import org.basex.io.out.PrintOutput;

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
  private final ArrayOutput buf;
  /** Iterator flag. */
  private boolean more;

  /**
   * Constructor. Query output will be returned by each called methods.
   * @param q query string
   * @param ctx database context
   */
  LocalQuery(final String q, final Context ctx) {
    buf = new ArrayOutput();
    qp = new QueryListener(q, PrintOutput.get(buf), ctx);
  }

  /**
   * Constructor. Query output will be written to the provided output stream.
   * All methods will return {@code null}.
   * @param q query string
   * @param ctx database context
   * @param o output stream to write query output
   */
  LocalQuery(final String q, final Context ctx, final OutputStream o) {
    buf = null;
    qp = new QueryListener(q, PrintOutput.get(o), ctx);
  }

  @Override
  public void bind(final String n, final Object v, final String t)
      throws IOException {
    qp.bind(n, v, t);
  }

  @Override
  public boolean more() throws IOException {
    more = true;
    return qp.next();
  }

  @Override
  public String next() throws IOException {
    if(more) more = false;
    else qp.next();
    return output();
  }

  @Override
  public String execute() throws IOException {
    qp.execute();
    return output();
  }

  @Override
  public String info() throws IOException {
    return qp.info();
  }

  @Override
  public String options() throws IOException {
    return qp.options();
  }

  @Override
  public void close() throws IOException {
    qp.close();
  }

  /**
   * Query output.
   * @return {@code null} if query output is directly sent to an output stream
   */
  private String output() {
    // if another output stream is specified, the query process has already
    // written the data, so there is nothing to output
    if(buf == null) return null;
    final String result = buf.toString();
    buf.reset();
    return result;
  }
}
