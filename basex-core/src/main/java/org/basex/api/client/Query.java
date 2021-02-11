package org.basex.api.client;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.Type.ID;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * <p>This class defines methods for evaluating queries.
 * It is implemented by {@link ClientQuery} and {@link LocalQuery}.</p>
 *
 * <p>Results are either returned as string or serialized to the output
 * stream that has been specified via the constructor or via
 * {@link Session#setOutputStream(OutputStream)}.</p>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class Query implements Closeable {
  /** Client output stream. */
  protected OutputStream out;
  /** Cached results. */
  protected TokenList cache;

  /** Cached result types. */
  private ByteList types;
  /** Cache pointer. */
  private int pos;

  /**
   * Binds a value to an external variable.
   * @param name name of variable
   * @param value value to be bound
   * @throws IOException I/O exception
   */
  public final void bind(final String name, final Object value) throws IOException {
    bind(name, value, "");
  }

  /**
   * Binds a value with an optional type to an external variable.
   * @param name name of variable
   * @param value value to be bound
   * @param type value type (may be {@code null})
   * @throws IOException I/O exception
   */
  public abstract void bind(String name, Object value, String type) throws IOException;

  /**
   * Binds a value to the context value.
   * @param value value to be bound
   * @throws IOException I/O exception
   */
  public final void context(final Object value) throws IOException {
    context(value, "");
  }

  /**
   * Binds a value with an optional type to an external variable.
   * @param value value to be bound
   * @param type value type (may be {@code null})
   * @throws IOException I/O exception
   */
  public abstract void context(Object value, String type) throws IOException;

  /**
   * Returns {@code true} if more items are available.
   * @return result of check
   * @throws IOException I/O exception
   */
  public boolean more() throws IOException {
    if(cache == null) cache(false);
    if(pos < cache.size()) return true;
    cache = null;
    types = null;
    return false;
  }

  /**
   * Caches the query result.
   * @param full retrieve full type information
   * @throws IOException I/O exception
   */
  public abstract void cache(boolean full) throws IOException;

  /**
   * Returns the next item of the query as string.
   * @return string or {@code null}
   * @throws IOException I/O exception
   */
  public final String next() throws IOException {
    if(more()) {
      final byte[] item = cache.get(pos);
      cache.set(pos++, null);
      if(out == null) return Token.string(item);
      out.write(item);
    }
    return null;
  }

  /**
   * Returns the XQuery type of the current item (must be called after {@link #next()}.
   * @return item type
   */
  public final Type type() {
    return ID.getType(types.get(pos - 1));
  }

  /**
   * Caches the incoming input.
   * @param input input stream
   * @param full retrieve full type information
   * @throws IOException I/O exception
   */
  void cache(final InputStream input, final boolean full) throws IOException {
    cache = new TokenList();
    types = new ByteList();
    final ByteList bl = new ByteList();
    for(int t; (t = input.read()) > 0;) {
      // skip type information
      if(full) {
        final ID id = ID.get(t);
        if(id != null && id.isExtended()) {
          while(input.read() > 0);
        }
      }
      // read and decode result
      final ServerInput si = new ServerInput(input);
      for(int b; (b = si.read()) != -1;) bl.add(b);
      cache.add(bl.next());
      types.add(t);
    }
    pos = 0;
  }

  /**
   * Returns the complete result of the query.
   * @return item string or {@code null}
   * @throws IOException I/O exception
   */
  public abstract String execute() throws IOException;

  /**
   * Returns the serialization options.
   * @return serialization options
   * @throws IOException I/O exception
   */
  public abstract String options() throws IOException;

  /**
   * Returns {@code true} if the query may perform updates.
   * @return updating flag
   * @throws IOException I/O exception
   */
  public abstract boolean updating() throws IOException;

  /**
   * Returns query info.
   * @return query info
   * @throws IOException I/O exception
   */
  public abstract String info() throws IOException;
}
