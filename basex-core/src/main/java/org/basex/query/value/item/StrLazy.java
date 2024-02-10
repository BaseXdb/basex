package org.basex.query.value.item;

import java.io.*;
import java.util.*;
import java.util.function.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.func.Function;
import org.basex.util.*;

/**
 * Lazy string item ({@code xs:string}).
 *
 * @author BaseX Team 2005-24, BSD License
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
  public TextInput stringInput(final InputInfo ii) throws IOException, QueryException {
    if(cache) cache(ii);
    return isCached() ? super.stringInput(ii) : get(ii);
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
   * @param info input info (can be {@code null})
   * @return stream
   * @throws QueryException query exception
   */
  private TextInput get(final InputInfo info) throws QueryException {
    NewlineInput nli = null;
    try {
      nli = new NewlineInput(input);
      nli.encoding(encoding).validate(validate);
      return nli;
    } catch(final IOException ex) {
      if(nli != null) try { nli.close(); } catch(final IOException e) { Util.debug(e); }
      throw error.get(info, ex);
    }
  }

  @Override
  public boolean isCached() {
    return value != null;
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
  public void toString(final QueryString qs) {
    if(isCached()) {
      super.toString(qs);
    } else {
      qs.function(Function._FILE_READ_TEXT, input);
    }
  }
}
