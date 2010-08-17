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
   * @param parse parser
   * @param pr properties
   */
  public MemBuilder(final Parser parse, final Prop pr) {
    super(parse, pr);
  }

  /**
   * Builds the main-memory database instance without database name.
   * @param parser parser
   * @param prop properties
   * @return data database instance
   * @throws IOException I/O exception
   */
  public static Data build(final Parser parser, final Prop prop)
      throws IOException {
    return build(parser, prop, parser.file.name());
  }

  /**
   * Builds a main-memory database instance.
   * @param parser parser
   * @param prop properties
   * @param name name of database
   * @return data database instance
   * @throws IOException I/O exception
   */
  public static MemData build(final Parser parser, final Prop prop,
      final String name) throws IOException {
    return new MemBuilder(parser, prop).build(name);
  }

  @Override
  public MemData build(final String name) throws IOException {
    data = new MemData(tags, atts, ns, path, prop);
    meta = data.meta;
    meta.name = name;
    // all contents will be indexed in main-memory mode
    meta.txtindex = true;
    meta.atvindex = true;
    meta.ftxindex = false;
    meta.file = parser.file;

    parse(name);
    data.init();
    return data;
  }

  @Override
  public void close() {
  }

  @Override
  protected void addDoc(final byte[] txt) {
    data.doc(meta.size, 0, txt);
    data.insert(meta.size);
  }

  @Override
  protected void addElem(final int dis, final int n, final int as, final int u,
      final boolean ne) {
    data.elem(meta.size, dis, n, as, as, u, ne);
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
