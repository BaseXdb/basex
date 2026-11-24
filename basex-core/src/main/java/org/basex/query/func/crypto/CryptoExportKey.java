package org.basex.query.func.crypto;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.crypto.CryptoKey.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CryptoExportKey extends StandardFunc {
  /** Type of the format argument. */
  public static final SeqType FORMAT_TYPE = EnumType.get(Format.values()).seqType();

  @Override
  public Item value(final QueryContext qc) throws QueryException {
    final Item key = arg(0).item(qc, info);
    final Format format = toEnum(toAtomItem(arg(1), qc), Format.class);
    final CryptoKey ck = new CryptoKey(info);
    return ck.export(ck.any(key), format);
  }
}
