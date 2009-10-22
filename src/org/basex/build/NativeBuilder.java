package org.basex.build;

import static org.basex.core.Text.*;
import java.io.IOException;

import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.data.NativeData;

/**
 * This class creates a memory based database instance and passes the events
 * to a native application.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 * @author Alexander Holupirek
 */
public final class NativeBuilder extends Builder {
  /** Data reference. */
  private NativeData data;

  /**
   * Constructor.
   * @param p parser
   */
  public NativeBuilder(final Parser p) {
    super(p);
  }

  @Override
  public void init(final String db) {
    // always index values in main memory mode
    meta = new MetaData(db, parser.prop);
    meta.txtindex = true;
    meta.atvindex = true;
    meta.ftxindex = false;
    meta.file = parser.io;
    data = new NativeData(64, tags, atts, ns, path, parser.prop);
    data.meta = meta;
  }

  /**
   * Uses the specified data instance (and its indexes) for the build process.
   * @param mdata data instance
   */
  public void init(final NativeData mdata) {
    data = mdata;
  }

  @Override
  protected NativeData finish() throws IOException {
    data.init();
    return data;
  }

  @Override
  public void close() { }

  @Override
  public void addDoc(final byte[] tok) {
    data.addDoc(tok, 0);
  }

  @Override
  public void addElem(final int tok, final int s, final int dis,
      final int a, final boolean n) throws IOException {
    data.addElem(tok, s, dis, a, a, n);
    if(data.meta.size < 0) throw new IOException(LIMITRANGE);
  }

  @Override
  public void addAttr(final int n, final int s, final byte[] v,
      final int d) {
    data.addAtt(n, s, v, d);
  }

  @Override
  public void addText(final byte[] tok, final int par, final byte kind) {
    data.addText(tok, par, kind);
  }

  @Override
  public void setSize(final int pre, final int val) {
    data.size(pre, Data.ELEM, val);
  }

  @Override
  public void setAttValue(final int pre, final byte[] val) {
    data.attValue(pre, val);
  }
}
