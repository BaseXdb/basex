package org.basex.build;

import java.io.*;

import org.basex.data.*;
import org.basex.io.*;

/**
 * This class creates a database instance in main memory.
 * The storage layout is described in the {@link Data} class.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class MemBuilder extends Builder {
  /** Data reference. */
  private MemData data;

  /**
   * Constructor.
   * @param name name of database
   * @param parse parser
   */
  public MemBuilder(final String name, final Parser parse) {
    super(name, parse);
  }

  /**
   * Builds a main memory database instance.
   * @param input input
   * @return data database instance
   * @throws IOException I/O exception
   */
  public static MemData build(final IO input) throws IOException {
    return build(Parser.xmlParser(input));
  }

  /**
   * Builds a main memory database instance.
   * @param parser parser
   * @return data database instance
   * @throws IOException I/O exception
   */
  public static MemData build(final Parser parser) throws IOException {
    return build(parser.source.dbname(), parser);
  }

  /**
   * Builds a main memory database instance with the specified name.
   * @param name name of database
   * @param parser parser
   * @return data database instance
   * @throws IOException I/O exception
   */
  public static MemData build(final String name, final Parser parser) throws IOException {
    return new MemBuilder(name, parser).build();
  }

  @Override
  public MemData build() throws IOException {
    dataClip();
    return data;
  }

  @Override
  public DataClip dataClip() throws IOException {
    init();
    data.meta.assign(parser);
    try {
      parse();
    } catch(final IOException ex) {
      try { close(); } catch(final IOException ignored) { }
      throw ex;
    }
    close();
    data.finish();
    return new DataClip(data);
  }

  /**
   * Initializes the builder.
   */
  public void init() {
    data = new MemData(path, nspaces, parser.options);

    meta = data.meta;
    meta.name = dbname;
    // values are always indexed in main memory mode
    meta.createtext = true;
    meta.createattr = true;
    meta.textindex = true;
    meta.attrindex = true;
    elemNames = data.elemNames;
    attrNames = data.attrNames;
    path.data(data);
  }

  /**
   * Returns the data reference.
   * @return data reference
   */
  public Data data() {
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
  protected void addElem(final int dist, final int nameId, final int asize, final int uriId,
      final boolean ne) {
    data.elem(dist, nameId, asize, asize, uriId, ne);
    data.insert(meta.size);
  }

  @Override
  protected void addAttr(final int nameId, final byte[] value, final int dist, final int uriId) {
    data.attr(meta.size, dist, nameId, value, uriId);
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
