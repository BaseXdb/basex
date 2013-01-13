package org.basex.query.value.item;

import java.io.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Streamable base64 item ({@code xs:base64Binary}).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class B64Stream extends B64 {
  /** File reference. */
  private final IO input;
  /** Error message. */
  private final Err error;

  /**
   * Constructor.
   * @param in input
   * @param err error message to be thrown
   */
  public B64Stream(final IO in, final Err err) {
    input = in;
    error = err;
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    return it instanceof B64Stream ? input.eq(((B64Stream) it).input) : super.eq(ii, it);
  }

  @Override
  public byte[] binary(final InputInfo ii) throws QueryException {
    if(data == null) {
      try {
        data = input.read();
      } catch(final IOException ex) {
        error.thrw(ii, ex);
      }
    }
    return data;
  }

  @Override
  public BufferInput input(final InputInfo ii) throws QueryException {
    try {
      return new BufferInput(input);
    } catch(final IOException ex) {
      throw error.thrw(ii, ex);
    }
  }

  @Override
  public Item materialize(final InputInfo ii) throws QueryException {
    try {
      return new B64(input(ii).content());
    } catch(final IOException ex) {
      throw error.thrw(ii, ex);
    }
  }

  @Override
  public String toString() {
    return Util.info(Function._FILE_READ_BINARY.args(input));
  }
}
