package org.basex.core;

import static org.basex.core.Text.*;
import static org.basex.query.QueryError.*;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * This class provides access to main-memory key/value stores.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Stores implements Closeable {
  /** Name of store files. */
  private static final String NAME = "store";

  /** Stores. */
  private final HashMap<String, Store> stores = new HashMap<>();
  /** Database context. */
  private final Context context;

  /** WebDAV locks (in-memory only, not persisted). */
  private XQMap locks = XQMap.empty();

  /**
   * Constructor.
   * @param context database context
   */
  public Stores(final Context context) {
    this.context = context;
  }

  /**
   * Returns all keys.
   * @param name name of store
   * @param info input info
   * @param qc query context
   * @return keys
   * @throws QueryException query exception
   */
  public synchronized Value keys(final String name, final InputInfo info, final QueryContext qc)
      throws QueryException {
    final Store store = get(name, false, info, qc);
    return store != null ? store.map.keys() : Empty.VALUE;
  }

  /**
   * Returns a value.
   * @param key key
   * @param name name of store
   * @param info input info
   * @param qc query context
   * @return value or empty sequence
   * @throws QueryException query exception
   */
  public synchronized Value get(final String key, final String name, final InputInfo info,
      final QueryContext qc) throws QueryException {
    final Store store = get(name, false, info, qc);
    return store != null ? store.map.get(Str.get(key)) : Empty.VALUE;
  }

  /**
   * Stores a value.
   * @param key key
   * @param value value
   * @param name name of store
   * @param info input info
   * @param qc query context
   * @throws QueryException query exception
   */
  public synchronized void put(final String key, final Value value, final String name,
      final InputInfo info, final QueryContext qc) throws QueryException {
    final Store store = get(name, true, info, qc);
    final Item ky = Str.get(key);
    store.map = value.isEmpty() ? store.map.remove(ky) : store.map.put(ky, value);
    store.dirty = true;
  }

  /**
   * Removes a value.
   * @param key key
   * @param name name of store
   * @param info input info
   * @param qc query context
   * @throws QueryException query exception
   */
  public synchronized void remove(final String key, final String name, final InputInfo info,
      final QueryContext qc) throws QueryException {
    put(key, Empty.VALUE, name, info, qc);
  }

  /**
   * Returns the WebDAV locks.
   * @return locks
   */
  public synchronized XQMap locks() {
    return locks;
  }

  /**
   * Atomically replaces the WebDAV locks.
   * @param func function that maps the current locks to the new ones
   * @return {@code true} if the locks have changed
   * @throws QueryException query exception
   */
  public synchronized boolean locks(final QueryFunction<XQMap, XQMap> func) throws QueryException {
    final XQMap map = func.apply(locks);
    if(map == locks) return false;
    locks = map;
    return true;
  }

  /**
   * Clears all stores.
   */
  public synchronized void clear() {
    stores.clear();
    for(final String name : listStores()) {
      storeFile(name).delete();
    }
  }

  /**
   * Closes a store.
   * @param name name of store
   * @param info input info
   * @throws QueryException query exception
   */
  public synchronized void close(final String name, final InputInfo info) throws QueryException {
    try {
      writeStore(name, false);
    } catch(final IOException ex) {
      throw STORE_IO_X.get(info, ex);
    }
    stores.remove(name);
  }

  /**
   * Returns the names of all stores.
   * @return keys
   */
  public synchronized Value list() {
    final TreeSet<String> names = listStores();
    for(final Map.Entry<String, Store> entry : stores.entrySet()) {
      if(entry.getValue().map.structSize() == 0) names.remove(entry.getKey());
      else names.add(entry.getKey());
    }
    names.remove("");
    final TokenList list = new TokenList(names.size());
    for(final String name : names) list.add(name);
    return StrSeq.get(list);
  }

  /**
   * Returns information on a store.
   * @param name name of store
   * @param info input info
   * @return number of entries, and size and modification date of the store file
   * @throws QueryException query exception
   */
  public synchronized XQMap info(final String name, final InputInfo info) throws QueryException {
    final Store store = stores.get(name);
    final IOFile file = storeFile(name);
    final boolean persisted = file.exists();

    long entries = 0;
    if(store != null) {
      entries = store.map.structSize();
    } else if(persisted) {
      // the number of entries is the first value of the store file: the values are not read
      try(DataInput in = new DataInput(file)) {
        entries = in.readNum();
      } catch(final IOException ex) {
        throw STORE_IO_X.get(info, ex);
      }
    }
    final MapBuilder mb = new MapBuilder();
    mb.put("entries", Itr.get(entries));
    mb.put("size", Itr.get(persisted ? file.length() : 0));
    mb.put("persisted", Bln.get(persisted));
    if(persisted) mb.put("modified", Dtm.get(file.timeStamp()));
    return mb.map();
  }

  /**
   * Reads a store from disk.
   * @param name name of store
   * @param info input info
   * @param qc query context
   * @throws QueryException query exception
   */
  public synchronized void read(final String name, final InputInfo info, final QueryContext qc)
      throws QueryException {
    if(storeFile(name).exists()) {
      readStore(name, info, qc);
    } else {
      stores.remove(name);
    }
  }

  /**
   * Writes the current store to disk.
   * @param name name of store
   * @param info input info
   * @throws QueryException query exception
   */
  public synchronized void write(final String name, final InputInfo info) throws QueryException {
    try {
      writeStore(name, true);
    } catch(final IOException ex) {
      throw STORE_IO_X.get(info, ex);
    }
  }

  /**
   * Deletes a store.
   * @param name name of store
   */
  public synchronized void delete(final String name) {
    stores.remove(name);
    storeFile(name).delete();
  }

  @Override
  public synchronized void close() {
    if(context.soptions.get(StaticOptions.WRITESTORE)) {
      for(final String name : stores.keySet()) {
        try {
          writeStore(name, false);
        } catch(final IOException | QueryException ex) {
          Util.stack(ex);
        }
      }
    }
  }

  // PRIVATE FUNCTIONS ============================================================================

  /**
   * Returns or creates the specified store.
   * @param name name of store
   * @param create create store if it does not exist
   * @param info input info
   * @param qc query context
   * @return store or {@code null}
   * @throws QueryException query exception
   */
  private Store get(final String name, final boolean create, final InputInfo info,
      final QueryContext qc) throws QueryException {
    if(!stores.containsKey(name)) {
      if(storeFile(name).exists()) {
        readStore(name, info, qc);
      } else if(create) {
        stores.put(name, new Store(XQMap.empty()));
      }
    }
    return stores.get(name);
  }

  /**
   * Reads a store from disk.
   * @param name name of store
   * @param info input info
   * @param qc query context
   * @throws QueryException query exception
   */
  private void readStore(final String name, final InputInfo info, final QueryContext qc)
      throws QueryException {
    final MapBuilder mb = new MapBuilder();
    try(DataInput in = new DataInput(storeFile(name))) {
      for(int s = in.readNum() - 1; s >= 0; s--) {
        mb.put(in.readToken(), read(in, qc));
      }
    } catch(final IOException ex) {
      throw QueryError.STORE_IO_X.get(info, ex);
    }
    stores.put(name, new Store(mb.map()));
  }

  /**
   * Writes the current store to disk.
   * @param name name of store
   * @param enforce always write store (not only dirty ones)
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private void writeStore(final String name, final boolean enforce)
      throws IOException, QueryException {
    final Store entry = stores.get(name);
    final IOFile file = storeFile(name);
    if(entry != null && (entry.dirty || enforce)) {
      final XQMap map = entry.map;
      final long size = map.structSize();
      if(size == 0) {
        file.delete();
      } else {
        try(DataOutput out = new DataOutput(file)) {
          out.writeNum((int) size);
          for(final XQMap.Entry e : map.entries()) {
            out.writeToken(e.key().string(null));
            write(out, e.value());
          }
        }
      }
      entry.dirty = false;
    }
  }

  /**
   * Returns the names of all stores from disk.
   * @return keys
   */
  private synchronized TreeSet<String> listStores() {
    final TreeSet<String> names = new TreeSet<>();
    for(final IOFile file : context.soptions.dbPath().children()) {
      final String name = file.name();
      if(name.matches("store(-.+|)\\" + IO.BASEXSUFFIX)) {
        names.add(name.replaceAll("^store-?|\\" + IO.BASEXSUFFIX, ""));
      }
    }
    return names;
  }

  /**
   * Returns a file reference for the specified store.
   * @param name name of store
   * @return file
   */
  private IOFile storeFile(final String name) {
    final TokenBuilder tb = new TokenBuilder().add(NAME);
    if(!standardStore(name)) tb.add('-').add(name);
    return context.soptions.dbPath(tb.add(IO.BASEXSUFFIX).toString());
  }

  /**
   * Checks if the supplied store refers to the standard store.
   * @param name name of store
   * @return result of check
   */
  private static boolean standardStore(final String name) {
    return name.isEmpty();
  }

  // STATIC FUNCTIONS =============================================================================

  /** Format marker (values without marker were written by BaseX 12 and older). */
  private static final int MARKER = 0x80;
  /** Marker flag for single items, which are stored without size. */
  private static final int SINGLE = 0x20;
  /** Current format version (1-31). */
  private static final int VERSION = 1;
  /** Format ID of sequences with arbitrary items. */
  private static final int SEQUENCE = 0x3f;
  /** Format ID of the prefix that makes BaseX 12 and older reject top-level values. */
  private static final int VERSIONED = 0x3e;

  /**
   * Writes a top-level value to an output stream.
   * @param out data output
   * @param value value to write
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public static void write(final DataOutput out, final Value value)
      throws IOException, QueryException {
    // older versions read a sequence with an unknown format and reject the value
    out.writeNum(BasicType.STRING.index());
    out.writeLong(2);
    out.writeNum(VERSIONED);
    writeNested(out, value);
  }

  /**
   * Writes a value that is nested in a top-level value to an output stream.
   * @param out data output
   * @param value value to write
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public static void writeNested(final DataOutput out, final Value value)
      throws IOException, QueryException {
    final long size = value.size();
    out.write1(MARKER | (size == 1 ? SINGLE : 0) | VERSION);
    out.writeNum(value.seqType().type.index());
    if(size == 1) {
      value.write(out);
    } else {
      out.writeLong(size);
      if(size == 0) return;
      final Format format = Format.get(value);
      if(format == null) {
        out.writeNum(SEQUENCE);
        final boolean same = value.refineType();
        out.writeBool(same);
        for(final Item item : value) {
          if(!same) out.writeNum(item.type.index());
          item.write(out);
        }
      } else {
        out.writeNum(format.ordinal());
        value.write(out);
      }
    }
  }

  /**
   * Reads a value from disk.
   * @param in input stream
   * @param qc query context
   * @return value
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public static Value read(final DataInput in, final QueryContext qc)
      throws IOException, QueryException  {
    qc.checkStop();
    final int b = in.read(), version, index;
    final boolean single;
    if(b < MARKER) {
      // without marker, the first byte belongs to the compressed type index
      version = 0;
      single = false;
      index = in.readNum(b);
    } else {
      version = b & (SINGLE - 1);
      single = (b & SINGLE) != 0;
      if(b >= 0xC0 || version == 0 || version > VERSION) throw new IOException(H_STORE_FORMAT);
      index = in.readNum();
    }
    final Type type = type(index);
    if(single) return item(in, type, version, qc);
    final long size = in.readLong();
    if(size == 0) return Empty.VALUE;
    if(size == 1) return item(in, type, version, qc);
    final int id = in.readNum();
    if(version == 0 && id == VERSIONED) return read(in, qc);
    if(id == SEQUENCE) {
      final ValueBuilder vb = new ValueBuilder(qc, size);
      final boolean same = in.readBool();
      for(long s = 0; s < size; s++) {
        vb.add(item(in, same ? type : type(in.readNum()), version, qc));
      }
      return vb.value(type);
    }
    // format is unknown: the store was written by a newer version
    final Format format = Format.get(id);
    if(format == null) throw new IOException(H_STORE_FORMAT);
    return version == 0 ? legacySequence(in, format, type, qc) : format.reader.read(in, type, qc);
  }

  /**
   * Returns the type with the specified index.
   * @param index index
   * @return type
   * @throws IOException I/O exception
   */
  private static Type type(final int index) throws IOException {
    final Type type = Types.type(index);
    if(type == null) throw new IOException(H_STORE_FORMAT);
    return type;
  }

  /**
   * Reads a single item.
   * @param in input stream
   * @param type type
   * @param version format version
   * @param qc query context
   * @return item
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private static Item item(final DataInput in, final Type type, final int version,
      final QueryContext qc) throws IOException, QueryException {
    return version == 0 ? legacyItem(in, type, qc) : type.read(in, qc);
  }

  /**
   * Reads a single item in the format of version 0 (BaseX 12 and older).
   * @param in input stream
   * @param type type
   * @param qc query context
   * @return item
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private static Item legacyItem(final DataInput in, final Type type, final QueryContext qc)
      throws IOException, QueryException {
    if(type == NodeType.NAMESPACE) return NodeType.NAMESPACE.cast(in.readToken(), qc, null);
    if(type instanceof final BasicType bt) {
      // floating-point numbers, decimals and unsigned longs were stored as strings
      if(bt.oneOf(BasicType.DOUBLE, BasicType.FLOAT, BasicType.DECIMAL, BasicType.UNSIGNED_LONG)) {
        return bt.cast(Str.get(in.readToken()), qc, null);
      }
      // integers were stored without zigzag encoding
      if(bt.instanceOf(BasicType.INTEGER)) return Itr.get(in.readLong(), bt);
    }
    return type.read(in, qc);
  }

  /**
   * Reads a sequence in the format of version 0 (BaseX 12 and older).
   * @param in input stream
   * @param format sequence format
   * @param type type
   * @param qc query context
   * @return value
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private static Value legacySequence(final DataInput in, final Format format, final Type type,
      final QueryContext qc) throws IOException, QueryException {
    return switch(format) {
      case DBL, FLT, DEC, BYT, SHR, INT, LONG -> {
        final int size = in.readNum();
        final ValueBuilder vb = new ValueBuilder(qc, size);
        // bytes and shorts were stored as compressed integers
        final boolean num = format == Format.BYT || format == Format.SHR;
        for(int s = 0; s < size; s++) {
          vb.add(num ? Itr.get(in.readNum(), type) : legacyItem(in, type, qc));
        }
        yield vb.value(type);
      }
      case RANGE -> {
        final long size = in.readLong(), start = in.readLong();
        yield RangeSeq.get(start, size, in.readBool());
      }
      default -> format.reader.read(in, type, qc);
    };
  }

  /**
   * Reader for a sequence format.
   */
  @FunctionalInterface
  private interface Reader {
    /**
     * Creates a value from the input stream.
     * @param in data input
     * @param type type
     * @param qc query context
     * @return value
     * @throws IOException I/O exception
     * @throws QueryException query exception
     */
    Value read(DataInput in, Type type, QueryContext qc) throws IOException, QueryException;
  }

  /**
   * Sequence formats.
   */
  private enum Format {
    // ORDER MUST NOT BE CHANGED. MAXIMUM: 62 ENTRIES
    /** Booleans. */ BLN(BlnSeq.class, (in, type, qc) -> BlnSeq.read(in)),
    /** Bytes. */ BYT(BytSeq.class, (in, type, qc) -> BytSeq.read(in, type)),
    /** Doubles. */ DBL(DblSeq.class, (in, type, qc) -> DblSeq.read(in)),
    /** Decimals. */ DEC(DecSeq.class, (in, type, qc) -> DecSeq.read(in)),
    /** Floats. */ FLT(FltSeq.class, (in, type, qc) -> FltSeq.read(in)),
    /** Integers. */ INT(IntSeq.class, (in, type, qc) -> IntSeq.read(in, type)),
    /** Shorts. */ SHR(ShrSeq.class, (in, type, qc) -> ShrSeq.read(in, type)),
    /** Strings. */ STR(StrSeq.class, (in, type, qc) -> StrSeq.read(in, type, qc)),
    /** Singletons. */ SINGLETON(SingletonSeq.class, (in, type, qc) -> SingletonSeq.read(in, qc)),
    /** Ranges. */ RANGE(RangeSeq.class, (in, type, qc) -> RangeSeq.read(in)),
    /** Longs. */ LONG(LongSeq.class, (in, type, qc) -> LongSeq.read(in, type));

    /** Cached formats. */
    private static final Format[] VALUES = values();

    /** Data class. */
    private final Class<? extends Value> clazz;
    /** Reader. */
    private final Reader reader;

    /**
     * Constructor.
     * @param clazz data class
     * @param reader reader
     */
    Format(final Class<? extends Value> clazz, final Reader reader) {
      this.clazz = clazz;
      this.reader = reader;
    }

    /**
     * Returns the format of the specified value.
     * @param value value
     * @return format or {@code null}
     */
    static Format get(final Value value) {
      for(final Format format : VALUES) {
        if(format.clazz == value.getClass()) return format;
      }
      return null;
    }

    /**
     * Returns the format with the specified ID.
     * @param id ID
     * @return format or {@code null}
     */
    static Format get(final int id) {
      return id >= 0 && id < VALUES.length ? VALUES[id] : null;
    }
  }

  /**
   * Single entry of a store.
   */
  private static final class Store {
    /** Map with data. */
    private XQMap map;
    /** Dirty flag. */
    private boolean dirty;

    /**
     * Constructor.
     * @param map map
     */
    private Store(final XQMap map) {
      this.map = map;
    }
  }
}
