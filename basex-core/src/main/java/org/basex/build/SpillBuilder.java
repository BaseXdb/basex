package org.basex.build;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.util.*;

/**
 * This class creates a database instance in main memory and moves it to disk if it gets too large.
 * The storage layout is described in the {@link Data} class.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SpillBuilder extends Builder {
  /** Estimated memory consumption of a table entry. */
  private static final int ENTRY = 16;
  /** Estimated memory overhead of a stored token. */
  private static final int TOKEN = 40;
  /** Maximum memory consumption of a single instance. */
  private static final long CAP = 1 << 26;

  /** Static options. */
  private final StaticOptions sopts;
  /** Main-memory data ({@code null} after spilling). */
  private MemData data;
  /** Disk builder ({@code null} before spilling). */
  private DiskBuilder disk;
  /** Estimated memory consumption. */
  private long used;

  /**
   * Constructor.
   * @param name name of the database (used as prefix for the name of a temporary disk instance)
   * @param parser parser
   * @param sopts static options
   */
  public SpillBuilder(final String name, final Parser parser, final StaticOptions sopts) {
    super(name, parser);
    this.sopts = sopts;
  }

  @Override
  public Data build() throws IOException {
    data = new MemData(path, nspaces, parser.options);
    meta = data.meta;
    meta.name = dbName;
    elemNames = data.elemNames;
    attrNames = data.attrNames;
    meta.assign(parser);
    try {
      parse();
      if(disk != null) return disk.finish(elemNames, attrNames, path, nspaces);
    } catch(final Throwable th) {
      if(disk != null) disk.abort();
      throw th;
    }
    // assign data reference after building: the main-memory data may be dropped when spilling
    path.data(data);
    data.lastid = data.nodes() - 1;
    if(meta.updindex) data.idmap.finish(data.lastid);
    data.release(MemoryLimit.hold(data, used));
    return data;
  }

  @Override
  int size() {
    return disk != null ? disk.size() : data.nodes();
  }

  @Override
  protected void addDoc(final byte[] value) throws IOException {
    if(disk != null) {
      disk.addDoc(value);
    } else {
      final int tokens = tokens();
      data.doc(0, value);
      insert(value, tokens);
    }
  }

  @Override
  protected void addElem(final int dist, final int nameId, final int asize, final int uriId,
      final boolean ne) throws IOException {
    if(disk != null) {
      disk.addElem(dist, nameId, asize, uriId, ne);
    } else {
      data.elem(dist, nameId, asize, asize, uriId, ne);
      insert(null, 0);
    }
  }

  @Override
  protected void addAttr(final int nameId, final byte[] value, final int dist, final int uriId)
      throws IOException {
    if(disk != null) {
      disk.addAttr(nameId, value, dist, uriId);
    } else {
      final int tokens = tokens();
      data.attr(dist, nameId, value, uriId);
      insert(value, tokens);
    }
  }

  @Override
  protected void addText(final byte[] value, final int dist, final byte kind) throws IOException {
    if(disk != null) {
      disk.addText(value, dist, kind);
    } else {
      final int tokens = tokens();
      data.text(dist, value, kind);
      insert(value, tokens);
    }
  }

  @Override
  protected void setSize(final int pre, final int size) throws IOException {
    if(disk != null) disk.setSize(pre, size);
    else data.size(pre, Data.ELEM, size);
  }

  /**
   * Returns the number of stored texts and attribute values.
   * @return number of tokens
   */
  private int tokens() {
    return data.values(true).size() + data.values(false).size();
  }

  /**
   * Inserts a table entry, adds its estimated memory consumption, and spills data if necessary.
   * @param value value of the entry (can be {@code null})
   * @param tokens number of stored tokens before the value was added
   * @throws IOException I/O exception
   */
  private void insert(final byte[] value, final int tokens) throws IOException {
    data.insert(data.nodes());
    used += value != null && tokens() != tokens ? ENTRY + TOKEN + value.length : ENTRY;
    // limit size of single instance, consider instances held in main memory
    if(used >= CAP || MemoryLimit.exceeded(used + MemoryLimit.held())) spill();
  }

  /**
   * Writes the main-memory data to a temporary disk instance and continues on disk.
   * @throws IOException I/O exception
   */
  private void spill() throws IOException {
    disk = new DiskBuilder(sopts.createTempDb(dbName), parser, sopts, parser.options);
    final MetaData dmeta = disk.meta;
    dmeta.original = meta.original;
    dmeta.inputsize = meta.inputsize;
    dmeta.time = meta.time;
    dmeta.ndocs = meta.ndocs;
    disk.open();

    // write table entries; sizes of open elements will be overwritten when they are closed
    final int nodes = data.nodes();
    for(int pre = 0; pre < nodes; pre++) {
      final int kind = data.kind(pre);
      if(kind == Data.DOC) {
        disk.addDoc(data.text(pre, true));
      } else if(kind == Data.ELEM) {
        disk.addElem(data.dist(pre, kind), data.nameId(pre),
            Math.min(IO.MAXATTS, data.attSize(pre, kind)), data.uriId(pre, kind), data.nsFlag(pre));
      } else if(kind == Data.ATTR) {
        disk.addAttr(data.nameId(pre), data.text(pre, false),
            Math.min(IO.MAXATTS, data.dist(pre, kind)), data.uriId(pre, kind));
      } else {
        disk.addText(data.text(pre, true), data.dist(pre, kind), (byte) kind);
      }
      if(kind == Data.DOC || kind == Data.ELEM) disk.setSize(pre, data.size(pre, kind));
    }
    meta = dmeta;
    data = null;
  }
}
