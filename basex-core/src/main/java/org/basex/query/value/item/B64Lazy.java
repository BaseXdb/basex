package org.basex.query.value.item;

import java.io.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.Function;
import org.basex.util.*;

/**
 * Lazy base64 item ({@code xs:base64Binary}).
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class B64Lazy extends B64 implements Lazy {
  /** File reference. */
  private final IO input;
  /** Error message. */
  private final QueryError error;
  /** Caching flag. */
  private boolean cache;

  /**
   * Constructor.
   * @param input input
   * @param error error message to be thrown
   */
  public B64Lazy(final IO input, final QueryError error) {
    this.input = input;
    this.error = error;
  }

  @Override
  public byte[] binary(final InputInfo info) throws QueryException {
    cache(info);
    return data;
  }

  @Override
  public BufferInput input(final InputInfo ii) throws QueryException {
    if(cache) cache(ii);
    if(isCached()) return super.input(ii);
    try {
      return BufferInput.get(input);
    } catch(final IOException ex) {
      Util.debug(ex);
      throw error.get(ii,  input);
    }
  }

  @Override
  public void cache(final  boolean lazy, final InputInfo ii) throws QueryException {
    if(lazy) cache = true;
    else cache(ii);
  }

  @Override
  public void cache(final InputInfo ii) throws QueryException {
    try {
      if(!isCached()) data = input.read();
    } catch(final IOException ex) {
      throw error.get(ii, ex);
    }
  }

  @Override
  public boolean isCached() {
    return data != null;
  }

  @Override
  public Item materialize(final Predicate<Data> test, final InputInfo ii, final QueryContext qc)
      throws QueryException {
    cache(ii);
    return this;
  }

  @Override
  public boolean materialized(final Predicate<Data> test, final InputInfo ii)
      throws QueryException {
    cache(ii);
    return true;
  }

  @Override
  public void toString(final QueryString qs) {
    if(isCached()) {
      super.toString(qs);
    } else {
      qs.function(Function._FILE_READ_BINARY, input);
    }
  }
}
