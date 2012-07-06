package org.basex.server;

import java.io.*;

import org.basex.core.*;
import org.basex.io.in.*;
import org.basex.io.out.*;

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
  protected LocalQuery(final String q, final Context ctx, final OutputStream o) {
    ql = new QueryListener(q, ctx);
    out = o;
  }

  @Override
  public void bind(final String n, final Object v, final String t) throws IOException {
    ql.bind(n, v, t);
  }

  @Override
  public void context(final Object v, final String t) throws IOException {
    ql.context(v, t);
  }

  @Override
  protected void cache() throws IOException {
    final ArrayOutput ao = new ArrayOutput();
    ql.execute(true, ao, true, false);
    cache(new ArrayInput(ao.toArray()));
  }

  @Override
  @SuppressWarnings("resource")
  public String execute() throws IOException {
    final OutputStream os = out == null ? new ArrayOutput() : out;
    ql.execute(false, os, false, false);
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
  public boolean updating() throws IOException {
    return ql.updating();
  }

  @Override
  public void close() {
  }
}
