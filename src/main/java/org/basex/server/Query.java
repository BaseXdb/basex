package org.basex.server;

import java.io.*;

import org.basex.io.in.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * <p>This class defines methods for executing queries.
 * It is implemented by {@link ClientQuery}.</p>
 * <p>Results are either returned as string or serialized to the output
 * stream that has been specified via the constructor or via
 * {@link Session#setOutputStream(OutputStream)}.</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Query {
  /** Client output stream. */
  protected OutputStream out;
  /** Cached results. */
  protected TokenList cache;
  /** Cached result types. */
  protected ByteList types;
  /** Cache pointer. */
  protected int pos;

  /**
   * Binds a value to an external variable.
   * @param n name of variable
   * @param v value to be bound
   * @throws IOException I/O exception
   */
  public final void bind(final String n, final Object v) throws IOException {
    bind(n, v, "");
  }

  /**
   * Binds a value with an optional type to an external variable.
   * @param n name of variable
   * @param v value to be bound
   * @param t data type (may be {@code null})
   * @throws IOException I/O exception
   */
  public abstract void bind(final String n, final Object v, final String t)
      throws IOException;

  /**
   * Binds a value to the context item.
   * @param v value to be bound
   * @throws IOException I/O exception
   */
  public final void context(final Object v) throws IOException {
    context(v, "");
  }

  /**
   * Binds a value with an optional type to an external variable.
   * @param v value to be bound
   * @param t data type (may be {@code null})
   * @throws IOException I/O exception
   */
  public abstract void context(final Object v, final String t) throws IOException;

  /**
   * Returns {@code true} if more items are available.
   * @return result of check
   * @throws IOException I/O exception
   */
  public boolean more() throws IOException {
    if(cache == null) cache();
    if(pos < cache.size()) return true;
    cache = null;
    types = null;
    return false;
  }

  /**
   * Caches the query result.
   * @throws IOException I/O exception
   */
  protected abstract void cache() throws IOException;

  /**
   * Returns the next item of the query.
   * @return item string or {@code null}.
   * @throws IOException I/O exception
   */
  public final String next() throws IOException {
    if(!more()) return null;
    final byte[] item = cache.get(pos);
    cache.set(pos++, null);
    if(out == null) return Token.string(item);
    out.write(item);
    return null;
  }

  /**
   * Returns the current XQuery type (must be called after {@link #next()}.
   * @return item type
   */
  public final Type type() {
    final byte id = types.get(pos - 1);
    for(final AtomType t : AtomType.values()) if(t.id() == id) return t;
    for(final NodeType t : NodeType.values()) if(t.id() == id) return t;
    return null;
  }

  /**
   * Caches the incoming input.
   * @param is input stream
   * @throws IOException I/O exception
   */
  @SuppressWarnings("resource")
  protected void cache(final InputStream is) throws IOException {
    cache = new TokenList();
    types = new ByteList();
    final ByteList bl = new ByteList();
    for(int t; (t = is.read()) > 0;) {
      final DecodingInput di = new DecodingInput(is);
      for(int b; (b = di.read()) != -1;) bl.add(b);
      cache.add(bl.toArray());
      types.add(t);
      bl.reset();
    }
  }

  /**
   * Returns the complete result of the query.
   * @return item string or {@code null}.
   * @throws IOException I/O exception
   */
  public abstract String execute() throws IOException;

  /**
   * Returns the serialization options.
   * @return serialization options.
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

  /**
   * Closes the query.
   * @throws IOException I/O exception
   */
  public abstract void close() throws IOException;
}
