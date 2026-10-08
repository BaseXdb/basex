package org.basex.query.func.ft;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FtLanguages extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) {
    final RecordType type = Records.LANGUAGE.get();
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final Language ln : Language.all()) {
      vb.add(XQMap.get(type, Str.get(ln.code()), Str.get(ln.toString()),
          Bln.get(Stemmer.supportFor(ln)), Bln.get(Tokenizer.supportFor(ln))));
    }
    return vb.value(this);
  }
}
