package org.basex.build;

import java.io.*;

import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.name.*;
import org.basex.io.*;

/**
 * Dummy parser for parsing XML input.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class EmptyBuilder extends Builder {
  /** Context. */
  private final Context context;

  /**
   * Constructor.
   * @param io input
   * @param ctx database context
   * @throws IOException I/O exception
   */
  public EmptyBuilder(final IO io, final Context ctx) throws IOException {
    super("", new XMLParser(io, ctx.options));
    context = ctx;
  }

  @Override
  public Data build() throws IOException {
    meta = new MetaData(dbname, context);
    tags = new Names(meta);
    atts = new Names(meta);
    try { parse(); } finally { close(); }
    return null;
  }

  @Override
  public void close() throws IOException {
    parser.close();
  }

  @Override
  protected void addDoc(final byte[] value) {
    meta.size++;
  }

  @Override
  protected void addElem(final int dist, final int nm, final int asize, final int uri,
      final boolean ne) {
    meta.size++;
  }

  @Override
  protected void addAttr(final int nm, final byte[] value, final int dist, final int uri) {
    meta.size++;
  }

  @Override
  protected void addText(final byte[] value, final int dist, final byte kind) {
    meta.size++;
  }

  @Override
  protected void setSize(final int pre, final int size) {
  }
}
