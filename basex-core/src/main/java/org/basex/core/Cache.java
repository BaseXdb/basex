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
 * This class provides a global key/value cache.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class Cache implements Closeable {
  /** File pattern. */
  private static final Pattern PATTERN = Pattern.compile("cache-(.*)\\" + IO.BASEXSUFFIX);

  /** Cache entries. */
  private final TokenObjMap<Value> map = new TokenObjMap<>();
  /** Database context. */
  private final Context context;

  /** Name of current cache. */
  private String name = "";
  /** Dirty flag. */
  private boolean dirty = true;
  /** Initialization flag. */
  private boolean init;

  /**
   * Constructor.
   * @param context database context
   */
  public Cache(final Context context) {
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
   * Returns the names of all caches.
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
   * Reads a cache from disk.
   * @param cache name (empty for standard cache)
   * @param qc query context
   * @throws IOException I/O exception
   * @return success flag (also {@code true} if standard cache is requested and does not exist)
   * @throws QueryException query exception
   */
  public synchronized boolean read(final String cache, final QueryContext qc)
      throws IOException, QueryException {

    final IOFile file = file(cache);
    final boolean exists = file.exists();
    if(!exists && !standard(cache)) return false;

    name = cache;
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
   * Writes the current cache to disk.
   * @param cache name (empty for standard cache)
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  public synchronized void write(final String cache) throws IOException, QueryException {
    init();
    name = cache;
    dirty = false;

    final IOFile file = file(cache);
    if(standard(cache) && map.isEmpty()) {
      // standard cache: delete standard cache if it is empty
      file.delete();
    } else {
      // write cache to disk
      file.parent().md();
      try(DataOutput out = new DataOutput(file)) {
        out.writeNum(map.size());
        for(final byte[] key : map) {
          out.writeToken(key);
          write(out, map.get(key));
        }
      }
    }
  }

  /**
   * Deletes a cache on disk.
   * @param cache name (empty for standard cache)
   * @return success flag
   */
  public synchronized boolean delete(final String cache) {
    final IOFile file = file(cache);
    return file.exists() && file.delete();
  }

  @Override
  public synchronized void close() {
    // skip write if cache has not been used or cache is not standard cache
    try {
      if(init && name.isEmpty() && dirty) write("");
    } catch(final IOException | QueryException ex) {
      Util.stack(ex);
    }
  }

  // PRIVATE FUNCTIONS ============================================================================

  /**
   * Initializes the cache.
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
   * Reads a file reference for the specified cache.
   * @param cache name (empty for standard cache)
   * @return file
   */
  private IOFile file(final String cache) {
    final TokenBuilder tb = new TokenBuilder();
    tb.add(Util.className(this).toLowerCase(Locale.ENGLISH));
    if(!standard(cache)) tb.add('-').add(cache);
    return context.soptions.dbPath(tb.add(IO.BASEXSUFFIX).toString());
  }

  /**
   * Checks if the supplied cache name refers to the standard cache.
   * @param cache name (empty for standard cache)
   * @return result of check
   */
  private boolean standard(final String cache) {
    return cache.isEmpty();
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
   * Reads a cache from disk.
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
