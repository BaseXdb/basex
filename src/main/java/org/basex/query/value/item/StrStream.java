package org.basex.query.value.item;

import java.io.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Streamable string item ({@code xs:string}).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class StrStream extends AStr {
  /** Input reference. */
  private final IO input;
  /** Encoding (optional). */
  private final String encoding;
  /** Error message. */
  private final Err error;

  /**
   * Constructor.
   * @param io input
   * @param enc encoding (may be null)
   * @param err error message to be thrown
   */
  public StrStream(final IO io, final String enc, final Err err) {
    input = io;
    encoding = enc;
    error = err;
  }

  @Override
  public byte[] string(final InputInfo ii) throws QueryException {
    try {
      return input(ii).content();
    } catch(final IOException ex) {
      throw error.thrw(ii, input);
    }
  }

  @Override
  public String toJava() throws QueryException {
    return Token.string(string(null));
  }

  @Override
  public TextInput input(final InputInfo ii) throws QueryException {
    TextInput nli = null;
    try {
      nli = new TextInput(input);
      nli.encoding(encoding).valid(true);
      return nli;
    } catch(final IOException ex) {
      if(nli != null) try { nli.close(); } catch(final IOException ignored) { }
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
