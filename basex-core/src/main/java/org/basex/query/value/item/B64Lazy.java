package org.basex.query.value.item;

import java.io.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Lazy base64 item ({@code xs:base64Binary}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class B64Lazy extends B64 implements Lazy {
  /** Error message. */
  private final QueryError error;
  /** Caching flag. */
  private boolean cache;

  /**
   * Constructor.
   * @param error error message to be thrown
   */
  B64Lazy(final QueryError error) {
    this.error = error;
  }

  /**
   * Opens a stream on the uncached value.
   * @return buffered input
   * @throws IOException I/O exception
   */
  abstract BufferInput open() throws IOException;

  @Override
  public final byte[] binary(final InputInfo info) throws QueryException {
    cache(info);
    return data;
  }

  @Override
  public final BufferInput input(final InputInfo ii) throws QueryException {
    if(cache) cache(ii);
    if(isCached()) return super.input(ii);
    try {
      return open();
    } catch(final IOException ex) {
      throw error.get(ii, ex);
    }
  }

  @Override
  public final void cache(final boolean lazy, final InputInfo ii) throws QueryException {
    if(lazy) cache = true;
    else cache(ii);
  }

  @Override
  public final void cache(final InputInfo ii) throws QueryException {
    if(isCached()) return;
    try(BufferInput bi = open()) {
      data = bi.content();
    } catch(final IOException ex) {
      throw error.get(ii, ex);
    }
  }

  @Override
  public final boolean isCached() {
    return data != null;
  }

  @Override
  public final Item materialize(final Predicate<Data> test, final InputInfo ii,
      final QueryContext qc) throws QueryException {
    cache(ii);
    return this;
  }

  @Override
  public final boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    cache(ii);
    return true;
  }
}
