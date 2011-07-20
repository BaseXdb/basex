package org.basex.build;

import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.io.IO;

/**
 * This class creates a database instance in main memory.
 * The storage layout is described in the {@link Data} class.
 *
 * @author BaseX Team 2005-11, BSD License
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
   * Builds the main memory database instance without database name.
   * @param parser parser
   * @param prop properties
   * @return data database instance
   * @throws IOException I/O exception
   */
  public static Data build(final Parser parser, final Prop prop)
      throws IOException {
    return build(parser, prop, parser.src.name());
  }

  /**
   * Builds a main memory database instance.
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
    // all contents will be indexed in main memory mode
    meta.textindex = true;
    meta.attrindex = true;
    meta.ftindex = false;
    final IO file = parser.src;
    meta.original = file != null ? file.path() : "";
    meta.filesize = file != null ? file.length() : 0;
    meta.time = file != null ? file.date() : System.currentTimeMillis();

    parse(name);
    data.init();
    return data;
  }

  @Override
  public void close() {
  }

  @Override
  protected void addDoc(final byte[] value) {
    data.doc(meta.size, 0, value);
    data.insert(meta.size);
  }

  @Override
  protected void addElem(final int dist, final int name, final int asize,
      final int uri, final boolean ne) {
    data.elem(dist, name, asize, asize, uri, ne);
    data.insert(meta.size);
  }

  @Override
  protected void addAttr(final int name, final byte[] value, final int dist,
      final int uri) {
    data.attr(meta.size, dist, name, value, uri, false);
    data.insert(meta.size);
  }

  @Override
  protected void addText(final byte[] value, final int dist, final byte kind) {
    data.text(meta.size, dist, value, kind);
    data.insert(meta.size);
  }

  @Override
  protected void setSize(final int pre, final int size) {
    data.size(pre, Data.ELEM, size);
  }
}
