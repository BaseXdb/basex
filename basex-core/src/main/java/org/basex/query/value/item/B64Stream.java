package org.basex.query.value.item;

import java.io.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Streamable base64 item ({@code xs:base64Binary}).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class B64Stream extends B64 {
  /** File reference. */
  private final IO input;
  /** Error message. */
  private final QueryError error;

  /**
   * Constructor.
   * @param input input
   * @param error error message to be thrown
   */
  public B64Stream(final IO input, final QueryError error) {
    this.input = input;
    this.error = error;
  }

  @Override
  public byte[] binary(final InputInfo ii) throws QueryException {
    try {
      return input.read();
    } catch(final IOException ex) {
      throw error.get(ii, ex);
    }
  }

  @Override
  public BufferInput input(final InputInfo ii) throws QueryException {
    try {
      return new BufferInput(input);
    } catch(final IOException ex) {
      throw error.get(ii, ex);
    }
  }

  @Override
  public Item materialize(final InputInfo ii) throws QueryException {
    return new B64(binary(ii));
  }

  @Override
  public String toString() {
    return Util.info(Function._FILE_READ_BINARY.args(input));
  }
}
