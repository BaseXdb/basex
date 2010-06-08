package org.basex.build;

import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MemData;

/**
 * This class creates a memory based database instance.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class MemBuilder extends Builder {
  /** Data reference. */
  private MemData data;

  /**
   * Constructor.
   * @param p parser
   */
  public MemBuilder(final Parser p) {
    super(p, new Prop(false));
  }

  /**
   * Constructor.
   * @param p parser
   * @param pr properties
   */
  public MemBuilder(final Parser p, final Prop pr) {
    super(p, pr);
  }

  /**
   * Builds the main-memory database instance without database name.
   * @return data database instance
   * @throws IOException I/O exception
   */
  public Data build() throws IOException {
    return build("");
  }

  @Override
  public MemData build(final String db) throws IOException {
    data = new MemData(tags, atts, ns, path, prop);
    meta = data.meta;
    meta.name = db;
    // all contents will be indexed in main-memory mode
    meta.txtindex = true;
    meta.atvindex = true;
    meta.ftxindex = false;
    meta.file = parser.file;

    parse(db);
    data.init();
    return data;
  }

  @Override
  public void close() { }

  @Override
  protected void addDoc(final byte[] txt) {
    data.doc(meta.size, 0, txt);
    data.insert(meta.size);
  }

  @Override
  protected void addElem(final int dis, final int n, final int as, final int u,
      final boolean ne) {
    data.elem(dis, n, as, as, u, ne);
    data.insert(meta.size);
  }

  @Override
  protected void addAttr(final int n, final byte[] v, final int d,
      final int u) {
    data.attr(meta.size, d, n, v, u, false);
    data.insert(meta.size);
  }

  @Override
  protected void addText(final byte[] tok, final int dis, final byte kind) {
    data.text(meta.size, dis, tok, kind);
    data.insert(meta.size);
  }

  @Override
  protected void setSize(final int pre, final int val) {
    data.size(pre, Data.ELEM, val);
  }
}
