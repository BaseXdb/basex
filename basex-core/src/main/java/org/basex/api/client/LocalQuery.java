package org.basex.api.client;

import java.io.*;

import org.basex.core.*;
import org.basex.io.in.*;
import org.basex.io.out.*;
import org.basex.server.*;

/**
 * This class defines all methods for iteratively evaluating queries locally.
 * All data is interpreted by the {@link ServerQuery}.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class LocalQuery extends Query {
  /** Active query listener. */
  private final ServerQuery ql;

  /**
   * Constructor. Query output will be written to the provided output stream.
   * All methods will return {@code null}.
   * @param query query string
   * @param context database context
   * @param output output stream to write query output
   */
  LocalQuery(final String query, final Context context, final OutputStream output) {
    ql = new ServerQuery(query, context);
    out = output;
  }

  @Override
  public void bind(final String name, final Object value, final String type) throws IOException {
    cache = null;
    ql.bind(name, value, type);
  }

  @Override
  public void context(final Object value, final String type) throws IOException {
    cache = null;
    ql.context(value, type);
  }

  @Override
  protected void cache() throws IOException {
    final ArrayOutput ao = new ArrayOutput();
    ql.execute(true, ao, true, false);
    cache(new ArrayInput(ao.finish()));
  }

  @Override
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
    return ql.parameters();
  }

  @Override
  public boolean updating() throws IOException {
    return ql.updating();
  }

  @Override
  public void close() {
  }
}
