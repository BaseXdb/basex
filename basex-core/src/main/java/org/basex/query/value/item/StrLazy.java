package org.basex.query.value.item;

import java.io.*;
import java.util.*;

import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Lazy string item ({@code xs:string}).
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class StrLazy extends AStr implements Lazy {
  /** Input reference. */
  private final IO input;
  /** Encoding (can be {@code null}). */
  private final String encoding;
  /** Error message. */
  private final QueryError error;
  /** Validation flag. */
  private final boolean validate;
  /** Caching flag. */
  private boolean cache;

  /**
   * Constructor.
   * @param input input
   * @param encoding encoding (can be {@code null})
   * @param error error message to be thrown
   * @param validate validate flag
   */
  public StrLazy(final IO input, final String encoding, final QueryError error,
      final boolean validate) {
    this.input = input;
    this.encoding = encoding;
    this.error = error;
    this.validate = validate;
  }

  @Override
  public byte[] string(final InputInfo info) throws QueryException {
    cache(info);
    return value;
  }

  @Override
  public String toJava() throws QueryException {
    return Token.string(string(null));
  }

  @Override
  public BufferInput input(final InputInfo info) throws QueryException {
    if(cache) cache(info);
    return isCached() ? super.input(info) : get(info);
  }

  @Override
  public void cache(final InputInfo info, final  boolean lazy) throws QueryException {
    if(lazy) cache = true;
    else cache(info);
  }

  @Override
  public void cache(final InputInfo info) throws QueryException {
    try {
      if(!isCached()) value = get(info).content();
    } catch(final IOException ex) {
      throw error.get(info, ex);
    }
  }

  /**
   * Returns an input stream for the item.
   * @param info input info
   * @return stream
   * @throws QueryException query exception
   */
  private BufferInput get(final InputInfo info) throws QueryException {
    TextInput ti = null;
    try {
      ti = new TextInput(input);
      ti.encoding(encoding).validate(validate);
      return ti;
    } catch(final IOException ex) {
      if(ti != null) try { ti.close(); } catch(final IOException ignore) { }
      throw error.get(info, ex);
    }
  }

  @Override
  public boolean isCached() {
    return value != null;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(obj instanceof StrLazy) {
      final StrLazy s = (StrLazy) obj;
      if(input.eq(s.input) && Objects.equals(encoding, s.encoding) && error == s.error &&
          validate == s.validate) return true;
    }
    // items may be different, but result may be equal...
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return isCached() ? toString(value) : Function._FILE_READ_TEXT.args(input).substring(1);
  }
}
