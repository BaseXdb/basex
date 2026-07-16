package org.basex.query.value.item;

import java.io.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.Function;

/**
 * Lazy base64 item ({@code xs:base64Binary}) backed by an input reference.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class B64IOLazy extends B64Lazy {
  /** File reference. */
  private final IO input;

  /**
   * Constructor.
   * @param input input
   * @param error error message to be thrown
   */
  public B64IOLazy(final IO input, final QueryError error) {
    super(error);
    this.input = input;
  }

  /**
   * Returns the input reference.
   * @return input
   */
  public IO input() {
    return input;
  }

  @Override
  BufferInput open() throws IOException {
    return BufferInput.get(input);
  }

  @Override
  public void toString(final QueryString qs) {
    if(isCached()) super.toString(qs);
    else qs.function(Function._FILE_READ_BINARY, input);
  }
}
