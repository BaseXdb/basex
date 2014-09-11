package org.basex.query.func.zip;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions on zip files.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class ZipTextEntry extends ZipBinaryEntry {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String enc = exprs.length < 3 ? null : string(toToken(exprs[2], qc));
    final IO io = new IOContent(entry(qc));
    final boolean val = qc.context.options.get(MainOptions.CHECKSTRINGS);
    try {
      return Str.get(new NewlineInput(io).encoding(enc).validate(val).content());
    } catch(final IOException ex) {
      throw ZIP_FAIL_X.get(info, ex);
    }
  }
}
