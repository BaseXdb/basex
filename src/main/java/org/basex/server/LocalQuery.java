package org.basex.server;

import java.io.IOException;
import java.io.OutputStream;

import org.basex.core.Context;
import org.basex.io.in.ArrayInput;
import org.basex.io.out.ArrayOutput;

/**
 * This class defines all methods for iteratively evaluating queries locally.
 * All data is interpreted by the {@link QueryListener}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class LocalQuery extends Query {
  /** Active query listener. */
  private final QueryListener ql;

  /**
   * Constructor. Query output will be written to the provided output stream.
   * All methods will return {@code null}.
   * @param q query string
   * @param ctx database context
   * @param o output stream to write query output
   */
  LocalQuery(final String q, final Context ctx, final OutputStream o) {
    ql = new QueryListener(q, ctx);
    out = o;
  }

  @Override
  public void bind(final String n, final Object v, final String t)
      throws IOException {
    ql.bind(n, v, t);
  }

  @Override
  protected void cache() throws IOException {
    final ArrayOutput ao = new ArrayOutput();
    ql.execute(true, ao, true);
    cache(new ArrayInput(ao.toArray()));
  }

  @Override
  public String execute() throws IOException {
    final OutputStream os = out == null ? new ArrayOutput() : out;
    ql.execute(false, os, false);
    return out == null ? os.toString() : null;
  }

  @Override
  public String info() throws IOException {
    return ql.info();
  }

  @Override
  public String options() throws IOException {
    return ql.options();
  }

  @Override
  public void close() {
  }
}
