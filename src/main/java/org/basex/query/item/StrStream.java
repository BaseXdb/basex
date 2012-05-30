package org.basex.query.item;

import java.io.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Streamable string item.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class StrStream extends Item {
  /** Input reference. */
  private final IO input;
  /** Encoding (optional). */
  private final String encoding;
  /** Error message. */
  private final Err error;
  /** String representation. */
  private byte[] val;

  /**
   * Constructor.
   * @param io input
   * @param enc encoding (may be null)
   * @param err error message to be thrown
   */
  public StrStream(final IO io, final String enc, final Err err) {
    super(AtomType.STR);
    input = io;
    encoding = enc;
    error = err;
  }

  @Override
  public byte[] string(final InputInfo ii) throws QueryException {
    if(val == null) {
      try {
        val = input(ii).content();
      } catch(final IOException ex) {
        throw error.thrw(ii, input);
      }
    }
    return val;
  }

  @Override
  public boolean bool(final InputInfo ii) throws QueryException {
    return string(ii).length != 0;
  }

  @Override
  public boolean eq(final InputInfo ii, final Item it) throws QueryException {
    return Token.eq(string(ii), it.string(ii));
  }

  @Override
  public int diff(final InputInfo ii, final Item it) throws QueryException {
    return Token.diff(string(ii), it.string(ii));
  }

  @Override
  public String toJava() throws QueryException {
    return Token.string(string(null));
  }

  @Override
  public TextInput input(final InputInfo ii) throws QueryException {
    try {
      return new NewlineInput(input).encoding(encoding);
    } catch(final IOException ex) {
      throw error.thrw(ii, input);
    }
  }

  @Override
  public Item materialize(final InputInfo ii) throws QueryException {
    return Str.get(string(ii));
  }

  @Override
  public String toString() {
    return Util.info(Function._FILE_READ_TEXT.args(input));
  }
}
