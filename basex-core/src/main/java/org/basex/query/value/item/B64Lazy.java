package org.basex.query.value.item;

import java.io.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Lazy base64 item ({@code xs:base64Binary}).
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class B64Lazy extends B64 implements Lazy {
  /** File reference. */
  private final IO input;
  /** Error message. */
  private final QueryError error;

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
  public BufferInput input(final InputInfo info) throws QueryException {
    if(isCached()) return super.input(info);
    try {
      return BufferInput.get(input);
    } catch(final IOException ex) {
      throw error.get(info, ex);
    }
  }

  @Override
  public void cache(final InputInfo info) throws QueryException {
    try {
      if(!isCached()) data = input.read();
    } catch(final IOException ex) {
      throw error.get(info, ex);
    }
  }

  @Override
  public boolean isCached() {
    return data != null;
  }

  @Override
  public String toString() {
    return isCached() ? super.toString() : Function._FILE_READ_BINARY.args(input).substring(1);
  }
}
