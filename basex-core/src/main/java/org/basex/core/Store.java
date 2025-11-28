package org.basex.core;

import java.io.*;
import java.lang.invoke.*;
import java.util.*;
import java.util.regex.*;

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
 * This class provides a main-memory key/value store.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class Store implements Closeable {
  /** Name of store files. */
  private static final String NAME = Util.className(Store.class).toLowerCase(Locale.ENGLISH);
  /** File pattern. */
  private static final Pattern PATTERN = Pattern.compile(NAME + "-(.*)\\" + IO.BASEXSUFFIX);

  /** Stores. */
  private final HashMap<String, Value> map = new HashMap<>();
  /** Database context. */
  private final Context context;

  /** File name of current store. */
  private String filename = "";
  /** Timestamp of current store (set to {@code -1} if store is dirty). */
  private long timestamp = -1;
  /** Initialization flag. */
  private boolean initialized;

  /**
   * Constructor.
   * @param context database context
   */
  public Store(final Context context) {
    this.context = context;
  }

  /**
   * Returns all keys.
   * @return keys
   */
  public synchronized Value keys() {
    init();
    final TokenList list = new TokenList(map.size());
    for(final String key : map.keySet()) list.add(key);
    return StrSeq.get(list);
  }

  /**
   * Returns a value.
   * @param key key
   * @return value or empty sequence
   */
  public synchronized Value get(final String key) {
    init();
    final Value value = map.get(key);
    return value != null ? value : Empty.VALUE;
  }

  /**
   * Stores a value.
   * @param key key
   * @param value value
   */
  public synchronized void put(final String key, final Value value) {
    init();
    if(value.isEmpty()) {
      map.remove(key);
    } else {
      map.put(key, value);
    }
    timestamp = -1;
  }

  /**
   * Removes a value.
   * @param key key
   */
  public synchronized void remove(final String key) {
    init();
    map.remove(key);
    timestamp = -1;
  }

  /**
   * Clears the map.
   */
  public synchronized void clear() {
    initialized = true;
    map.clear();
    timestamp = -1;
  }

  /**
   * Returns the names of all stores.
   * @return keys
   */
  public synchronized Value list() {
    final TokenList list = new TokenList();
    for(final IOFile file : context.soptions.dbPath().children()) {
      final Matcher m = PATTERN.matcher(file.name());
      if(m.matches()) list.add(m.group(1));
    }
    return StrSeq.get(list);
  }

  /**
   * Reads a store from disk.
   * @param store name (empty for standard store)
   * @param qc query context
   * @throws IOException I/O exception
   * @return success flag (also {@code true} if standard store is requested and does not exist)
   * @throws QueryException query exception
   */
  public synchronized boolean read(final String store, final QueryContext qc)
      throws IOException, QueryException {

    // return false if a requested non-standard store does not exist
    final IOFile file = file(store);
    final boolean standard = standard(store), exists = file.exists();
    if(!(standard || exists)) return false;

    // skip read if store in memory is up to date
    initialized = true;
    if(exists && filename.equals(file.name()) && timestamp == file.timeStamp()) return true;

    // regenerate store in memory
    map.clear();
    if(exists) {
      try(DataInput in = new DataInput(file)) {
        for(int s = in.readNum() - 1; s >= 0; s--) {
          map.put(Token.string(in.readToken()), read(in, qc));
        }
      }
      timestamp = file.timeStamp();
    } else {
      timestamp = -1;
    }
    filename = file.name();
    return true;
  }

  /**
   * Writes the current store to disk.
   * @param store name (empty for standard store)
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public synchronized void write(final String store) throws IOException, QueryException {
    if(!initialized) return;

    init();
    final IOFile file = file(store);
    if(standard(store) && map.isEmpty()) {
      // delete standard store if it is empty
      file.delete();
      timestamp = -1;
    } else {
      // write store to disk
      file.parent().md();
      try(DataOutput out = new DataOutput(file)) {
        out.writeNum(map.size());
        for(final Map.Entry<String, Value> entry : map.entrySet()) {
          out.writeToken(Token.token(entry.getKey()));
          write(out, entry.getValue());
        }
      }
      timestamp = file.timeStamp();
    }
    filename = file.name();
  }

  /**
   * Deletes a store on disk.
   * @param store name (empty for standard store)
   * @return success flag
   */
  public synchronized boolean delete(final String store) {
    final IOFile file = file(store);
    return file.exists() && file.delete();
  }

  @Override
  public synchronized void close() {
    final boolean writestore = context.soptions.get(StaticOptions.WRITESTORE);
    if(writestore && initialized && !filename.contains("-") && timestamp == -1) {
      try {
        write("");
      } catch(final IOException | QueryException ex) {
        Util.stack(ex);
      }
    }
  }

  // PRIVATE FUNCTIONS ============================================================================

  /**
   * Initializes the store.
   */
  private synchronized void init() {
    if(initialized) return;
    try(QueryContext qc = new QueryContext(context)) {
      read("", qc);
    } catch(final IOException | QueryException ex) {
      Util.stack(ex);
    }
  }

  /**
   * Reads a file reference for the specified store.
   * @param store name (empty for standard store)
   * @return file
   */
  private IOFile file(final String store) {
    final TokenBuilder tb = new TokenBuilder().add(NAME);
    if(!standard(store)) tb.add('-').add(store);
    return context.soptions.dbPath(tb.add(IO.BASEXSUFFIX).toString());
  }

  /**
   * Checks if the supplied store refers to the standard store.
   * @param store name (empty for standard store)
   * @return result of check
   */
  private static boolean standard(final String store) {
    return store.isEmpty();
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
   * Reads a store from disk.
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
}
