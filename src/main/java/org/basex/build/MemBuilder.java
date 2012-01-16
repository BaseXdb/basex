package org.basex.build;

import java.io.IOException;
import org.basex.core.Prop;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.data.MetaData;
import org.basex.io.IO;

/**
 * This class creates a database instance in main memory.
 * The storage layout is described in the {@link Data} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class MemBuilder extends Builder {
  /** Data reference. */
  private MemData data;

  /**
   * Constructor.
   * @param nm name of database
   * @param parse parser
   * @param pr properties
   */
  public MemBuilder(final String nm, final Parser parse, final Prop pr) {
    super(nm, parse, pr);
  }

  /**
   * Builds the main memory database instance without database name.
   * @param parser parser
   * @param prop properties
   * @return data database instance
   * @throws IOException I/O exception
   */
  public static MemData build(final Parser parser, final Prop prop)
      throws IOException {
    return build(parser.src.name(), parser, prop);
  }

  /**
   * Builds a main memory database instance.
   * @param name name of database
   * @param parser parser
   * @param prop properties
   * @return data database instance
   * @throws IOException I/O exception
   */
  public static MemData build(final String name, final Parser parser,
      final Prop prop) throws IOException {
    return new MemBuilder(name, parser, prop).build();
  }

  @Override
  public MemData build() throws IOException {
    data = new MemData(null, null, path, ns, prop);

    final MetaData md = data.meta;
    md.name = name;
    // all contents will be indexed in main memory mode
    md.createtext = true;
    md.createattr = true;
    md.textindex = true;
    md.attrindex = true;
    md.pathindex = md.createpath;
    final IO file = parser.src;
    md.original = file != null ? file.path() : "";
    md.filesize = file != null ? file.length() : 0;
    md.time = file != null ? file.date() : System.currentTimeMillis();

    parse(md, data.tagindex, data.atnindex);
    data.init();
    path.finish(data);
    return data;
  }

  @Override
  public void close() throws IOException {
    parser.close();
  }

  @Override
  protected void addDoc(final byte[] value) {
    data.doc(meta.size, 0, value);
    data.insert(meta.size);
  }

  @Override
  protected void addElem(final int dist, final int nm, final int asize,
      final int uri, final boolean ne) {
    data.elem(dist, nm, asize, asize, uri, ne);
    data.insert(meta.size);
  }

  @Override
  protected void addAttr(final int nm, final byte[] value, final int dist,
      final int uri) {
    data.attr(meta.size, dist, nm, value, uri, false);
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
