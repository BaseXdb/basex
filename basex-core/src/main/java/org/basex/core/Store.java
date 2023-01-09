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
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * This class provides a main-memory key/value store.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Store implements Closeable {
  /** Name of store files. */
  private static final String NAME = Util.className(Store.class).toLowerCase(Locale.ENGLISH);
  /** File pattern. */
  private static final Pattern PATTERN = Pattern.compile(NAME + "-(.*)\\" + IO.BASEXSUFFIX);

  /** Store entries. */
  private final TokenObjMap<Value> map = new TokenObjMap<>();
  /** Database context. */
  private final Context context;

  /** Name of current store. */
  private String name = "";
  /** Dirty flag. */
  private boolean dirty = true;
  /** Initialization flag. */
  private boolean init;

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
    for(final byte[] key : map) list.add(key);
    return StrSeq.get(list);
  }

  /**
   * Returns a value.
   * @param key key
   * @return value or empty sequence
   */
  public synchronized Value get(final byte[] key) {
    init();
    final Value value = map.get(key);
    return value != null ? value : Empty.VALUE;
  }

  /**
   * Stores a value.
   * @param key key
   * @param value value
   */
  public synchronized void put(final byte[] key, final Value value) {
    init();
    dirty = true;
    if(value == Empty.VALUE) map.remove(key);
    else map.put(key, value);
  }

  /**
   * Removes a value.
   * @param key key
   */
  public synchronized void remove(final byte[] key) {
    init();
    dirty = true;
    map.remove(key);
  }

  /**
   * Clears the map.
   */
  public synchronized void clear() {
    init = true;
    dirty = true;
    map.clear();
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

    final IOFile file = file(store);
    final boolean exists = file.exists();
    if(!exists && !standard(store)) return false;

    name = store;
    init = true;
    dirty = false;
    map.clear();
    if(exists) {
      try(DataInput in = new DataInput(file)) {
        for(int s = in.readNum() - 1; s >= 0; s--) {
          map.put(in.readToken(), read(in, qc));
        }
      }
    }
    return true;
  }

  /**
   * Writes the current store to disk.
   * @param store name (empty for standard store)
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public synchronized void write(final String store) throws IOException, QueryException {
    init();
    name = store;
    dirty = false;

    final IOFile file = file(store);
    if(standard(store) && map.isEmpty()) {
      // delete standard store if it is empty
      file.delete();
    } else {
      // write store to disk
      file.parent().md();
      try(DataOutput out = new DataOutput(file)) {
        int size = 0;
        for(final Iterator<byte[]> iter = map.iterator(); iter.hasNext(); iter.next()) size++;
        out.writeNum(size);
        for(final byte[] key : map) {
          out.writeToken(key);
          write(out, map.get(key));
        }
      }
    }
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
    // skip write if store has not been used or is not standard store
    try {
      if(init && name.isEmpty() && dirty) write("");
    } catch(final IOException | QueryException ex) {
      Util.stack(ex);
    }
  }

  // PRIVATE FUNCTIONS ============================================================================

  /**
   * Initializes the store.
   */
  private synchronized void init() {
    if(init) return;
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
    } catch(NoSuchMethodException | IllegalAccessException ex) {
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
    out.writeNum(value.seqType().type.id().asByte());
    final long size = value.size();
    out.writeLong(size);
    if(size == 1) {
      value.write(out);
    } else if(size > 1) {
      final Integer classId = CLASS_IDS.get(value.getClass());
      if(classId == null) {
        out.writeNum(SEQUENCE);
        final boolean same = value.sameType();
        out.writeBool(same);
        for(final Item item : value) {
          if(!same) out.writeNum(item.type.id().asByte());
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
   * @return value or {@code null} if data could not be read
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public static synchronized Value read(final DataInput in, final QueryContext qc)
      throws IOException, QueryException  {
    qc.checkStop();
    final int id = in.readNum();
    final Type type = Type.ID.getType(id);
    final long size = in.readLong();
    if(size == 0) return Empty.VALUE;
    if(size == 1) return type.read(in, qc);
    final int classId = in.readNum();
    if(classId == SEQUENCE) {
      final ValueBuilder vb = new ValueBuilder(qc);
      final boolean same = in.readBool();
      for(long s = 0; s < size; s++) {
        final Type tp = same ? type : Type.ID.getType(in.readNum());
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
