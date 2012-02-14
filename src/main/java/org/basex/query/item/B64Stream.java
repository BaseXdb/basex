package org.basex.query.item;

import java.io.IOException;
import java.io.InputStream;

import org.basex.io.IO;
import org.basex.query.QueryException;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Streamable base64 item.
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
  public boolean eq(final InputInfo ii, final Item it)
      throws QueryException {
    return type == it.type ? input.eq(((B64Stream) it).input) :
      super.eq(ii, it);
  }

  @Override
  protected byte[] val(final InputInfo ii) throws QueryException {
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
  public InputStream input(final InputInfo ii) throws QueryException {
    try {
      return input.inputStream();
    } catch(final IOException ex) {
      throw error.thrw(ii, ex);
    }
  }

  @Override
  public String toString() {
    return Util.info("\"%\"", input.name());
  }
}
