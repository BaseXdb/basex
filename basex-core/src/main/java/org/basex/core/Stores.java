package org.basex.core;

import static org.basex.query.QueryError.*;

import java.io.*;
import java.lang.invoke.*;
import java.util.*;

import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.io.out.DataOutput;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
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
    if(store != null) {
      final TokenList list = new TokenList(store.map.size());
      for(final String key : store.map.keySet()) list.add(key);
      return StrSeq.get(list);
    }
    return Empty.VALUE;
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
    if(store != null) {
      final Value value = store.map.get(key);
      if(value != null) return value;
    }
    return Empty.VALUE;
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
    if(value.isEmpty()) {
      store.map.remove(key);
    } else {
      store.map.put(key, value);
    }
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
   * Clears all stores.
   */
  public synchronized void clear() {
    stores.clear();
    for(final String name : listStores()) {
      storeFile(name).delete();
    }
  }

  /**
   * Resets the stores.
   * @param info input info
   * @throws QueryException query exception
   */
  public synchronized void reset(final InputInfo info) throws QueryException {
    for(final String name : stores.keySet()) {
      try {
        writeStore(name, false);
      } catch(final IOException | QueryException ex) {
        throw STORE_IO_X.get(info, ex);
      }
    }
    stores.clear();
  }

  /**
   * Returns the names of all stores.
   * @return keys
   */
  public synchronized Value list() {
    final TreeSet<String> names = listStores();
    for(final Map.Entry<String, Store> entry : stores.entrySet()) {
      if(entry.getValue().map.isEmpty()) names.remove(entry.getKey());
      else names.add(entry.getKey());
    }
    names.remove("");
    final TokenList list = new TokenList(names.size());
    for(final String name : names) list.add(name);
    return StrSeq.get(list);
  }

  /**
   * Reads a store from disk.
   * @param name name of store
   * @param qc query context
   * @param info input info
   * @throws QueryException query exception
   */
  public synchronized void read(final String name, final InputInfo info, final QueryContext qc)
      throws QueryException {
    if(storeFile(name).exists()) {
      readStore(name, info, qc);
    } else if(standardStore(name)) {
      stores.remove("");
    }
  }

  /**
   * Writes the current store to disk.
   * @param name name of store.
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
   * @param info input info
   * @param qc query context
   * @throws QueryException query exception
   */
  public synchronized void delete(final String name, final InputInfo info,
      final QueryContext qc) throws QueryException {
    if(standardStore(name)) {
      get(name, false, info, qc).map.clear();
    } else {
      stores.remove(name);
    }
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
        stores.put(name, new Store());
      }
    }
    return stores.get(name);
  }

  /**
   * Reads a store from disk.
   * @param name name of store
   * @param qc query context
   * @param info input info
   * @throws QueryException query exception
   */
  private void readStore(final String name, final InputInfo info, final QueryContext qc)
      throws QueryException {
    final HashMap<String, Value> map = new HashMap<>();
    try(DataInput in = new DataInput(storeFile(name))) {
      for(int s = in.readNum() - 1; s >= 0; s--) {
        map.put(Token.string(in.readToken()), read(in, qc));
      }
    } catch(final IOException ex) {
      throw QueryError.STORE_IO_X.get(info, ex);
    }
    stores.put(name, new Store(map));
  }

  /**
   * Writes the current store to disk.
   * @param name name of store
   * @param enforce always write dirty store (not only dirty ones)
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  private void writeStore(final String name, final boolean enforce)
      throws IOException, QueryException {
    final Store entry = stores.get(name);
    final IOFile file = storeFile(name);
    if(entry != null && (entry.dirty || enforce)) {
      final HashMap<String, Value> map = entry.map;
      if(map.isEmpty()) {
        file.delete();
      } else {
        file.parent().md();
        try(DataOutput out = new DataOutput(file)) {
          out.writeNum(map.size());
          for(final Map.Entry<String, Value> e : map.entrySet()) {
            out.writeToken(Token.token(e.getKey()));
            write(out, e.getValue());
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

  /** Sequence flag. */
  private static final int SEQUENCE = 0x3f;
  /** Class methods. */
  private static final MethodHandle[] METHODS;
  /** IDs of data classes. */
  private static final Map<Class<?>, Integer> CLASS_IDS = new HashMap<>();

  static {
    try {
      // ORDER MUST NOT BE CHANGED. MAXIMUM: 63 ENTRIES
      final Class<?>[] classes = {
        BlnSeq.class, BytSeq.class, DblSeq.class, DecSeq.class, FltSeq.class, IntSeq.class,
        ShrSeq.class, StrSeq.class, SingletonSeq.class, RangeSeq.class
      };
      METHODS = new MethodHandle[classes.length];

      final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
      final MethodType mt = MethodType.methodType(Value.class, DataInput.class, Type.class,
          QueryContext.class);
      for(int c = classes.length - 1; c >= 0; c--) {
        CLASS_IDS.put(classes[c], c);
        METHODS[c] = lookup.findStatic(classes[c], "read", mt);
      }
    } catch(final NoSuchMethodException | IllegalAccessException ex) {
      Util.stack(ex);
      throw Util.notExpected(ex);
    }
  }

  /**
   * Writes the specified value to an output stream.
   * @param out data output
   * @param value value to write
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public static synchronized void write(final DataOutput out, final Value value)
      throws IOException, QueryException {
    out.writeNum(value.seqType().type.index());
    final long size = value.size();
    out.writeLong(size);
    if(size == 1) {
      value.write(out);
    } else if(size > 1) {
      final Integer classId = CLASS_IDS.get(value.getClass());
      if(classId == null) {
        out.writeNum(SEQUENCE);
        final boolean same = value.refineType();
        out.writeBool(same);
        for(final Item item : value) {
          if(!same) out.writeNum(item.type.index());
          item.write(out);
        }
      } else {
        out.writeNum(classId);
        value.write(out);
      }
    }
  }

  /**
   * Reads a value from disk.
   * @param in input stream
   * @param qc query context
   * @return value, or {@code null} if data could not be read
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public static synchronized Value read(final DataInput in, final QueryContext qc)
      throws IOException, QueryException  {
    qc.checkStop();
    final int id = in.readNum();
    final Type type = Types.type(id);
    final long size = in.readLong();
    if(size == 0) return Empty.VALUE;
    if(size == 1) return type.read(in, qc);
    final int classId = in.readNum();
    if(classId == SEQUENCE) {
      final ValueBuilder vb = new ValueBuilder(qc, size);
      final boolean same = in.readBool();
      for(long s = 0; s < size; s++) {
        final Type tp = same ? type : Types.type(in.readNum());
        vb.add(tp.read(in, qc));
      }
      return vb.value(type);
    }
    try {
      return (Value) METHODS[classId].invoke(in, type, qc);
    } catch(final Throwable th) {
      throw new IOException(th);
    }
  }

  /**
   * Single entry of a store.
   */
  private static final class Store {
    /** Map with data. */
    private final HashMap<String, Value> map;
    /** Dirty flag. */
    private boolean dirty;

    /**
     * Default constructor.
     */
    private Store() {
      this(new HashMap<>());
    }

    /**
     * Constructor.
     * @param map map
     */
    private Store(final HashMap<String, Value> map) {
      this.map = map;
    }
  }
}
