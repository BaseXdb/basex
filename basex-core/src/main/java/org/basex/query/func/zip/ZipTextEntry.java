package org.basex.query.func.zip;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.in.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Functions on zip files.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ZipTextEntry extends ZipBinaryEntry {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String encoding = exprs.length < 3 ? null : string(toToken(exprs[2], qc));
    final byte[] entry = entry(qc);
    final boolean validate = qc.context.options.get(MainOptions.CHECKSTRINGS);

    try(NewlineInput ni = new NewlineInput(entry)) {
      return Str.get(ni.encoding(encoding).validate(validate).content());
    } catch(final IOException ex) {
      throw ZIP_FAIL_X.get(info, ex);
    }
  }
}
