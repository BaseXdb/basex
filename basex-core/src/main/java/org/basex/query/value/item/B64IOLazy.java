package org.basex.query.value.item;

import java.io.*;

import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.query.func.Function;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Lazy base64 item ({@code xs:base64Binary}) backed by an input reference.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class B64IOLazy extends B64Lazy {
  /** File reference. */
  private final IO input;
  /** Error message. */
  private final QueryError error;
  /** Registry for temporary files; if {@code null}, the input is reopened on each access. */
  private final TempFiles temp;

  /**
   * Constructor.
   * @param input input
   * @param error error message to be thrown
   */
  public B64IOLazy(final IO input, final QueryError error) {
    this(input, error, null);
  }

  /**
   * Constructor.
   * @param input input
   * @param error error message to be thrown
   * @param temp registry for temporary files (if {@code null}, input is reopened on each access)
   */
  public B64IOLazy(final IO input, final QueryError error, final TempFiles temp) {
    this.input = input;
    this.error = error;
    this.temp = temp;
  }

  /**
   * Returns the input reference.
   * @return input
   */
  public IO input() {
    return input;
  }

  @Override
  IO source() throws IOException {
    return temp != null ? SpillOutput.read(input, temp) : input;
  }

  @Override
  QueryException exception(final IOException ex, final InputInfo ii) {
    return error.get(ii, ex);
  }

  @Override
  public void toString(final QueryString qs) {
    if(isCached()) super.toString(qs);
    else qs.function(Function._FILE_READ_BINARY, IOUrl.stripUserInfo(input.toString()));
  }
}
