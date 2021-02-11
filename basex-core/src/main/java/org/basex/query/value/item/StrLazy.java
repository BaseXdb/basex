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
 * @author BaseX Team 2005-21, BSD License
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
  public byte[] string(final InputInfo ii) throws QueryException {
    cache(ii);
    return value;
  }

  @Override
  public String toJava() throws QueryException {
    return Token.string(string(null));
  }

  @Override
  public BufferInput input(final InputInfo ii) throws QueryException {
    if(cache) cache(ii);
    return isCached() ? super.input(ii) : get(ii);
  }

  @Override
  public void cache(final  boolean lazy, final InputInfo ii) throws QueryException {
    if(lazy) cache = true;
    else cache(ii);
  }

  @Override
  public void cache(final InputInfo ii) throws QueryException {
    try {
      if(!isCached()) value = get(ii).content();
    } catch(final IOException ex) {
      throw error.get(ii, ex);
    }
  }

  /**
   * Returns an input stream for the item.
   * @param ii input info
   * @return stream
   * @throws QueryException query exception
   */
  private BufferInput get(final InputInfo ii) throws QueryException {
    TextInput ti = null;
    try {
      ti = new TextInput(input);
      ti.encoding(encoding).validate(validate);
      return ti;
    } catch(final IOException ex) {
      if(ti != null) try { ti.close(); } catch(final IOException e) { Util.debug(e); }
      throw error.get(ii, ex);
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
  public void plan(final QueryString qs) {
    if(isCached()) {
      super.plan(qs);
    } else {
      qs.function(Function._FILE_READ_TEXT, input);
    }
  }
}
