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
 * @author BaseX Team 2005-12, BSD License
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
    super("", new XMLParser(io, ctx.prop));
    context = ctx;
  }

  @Override
  public Data build() throws IOException {
    meta = new MetaData(name, context);
    tags = new Names(meta);
    atts = new Names(meta);
    parse();
    close();
    return null;
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  protected void addDoc(final byte[] value) throws IOException {
  }

  @Override
  protected void addElem(final int dist, final int nm, final int asize,
      final int uri, final boolean ne) throws IOException {
  }

  @Override
  protected void addAttr(final int nm, final byte[] value, final int dist, final int uri)
      throws IOException {
  }

  @Override
  protected void addText(final byte[] value, final int dist, final byte kind)
      throws IOException {
  }

  @Override
  protected void setSize(final int pre, final int size) throws IOException {
  }
}
