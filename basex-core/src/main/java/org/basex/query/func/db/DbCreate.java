package org.basex.query.func.db;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class DbCreate extends DbNew {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final String name = string(toToken(exprs[0], qc));
    if(!Databases.validName(name)) throw BXDB_NAME_X.get(info, name);

    final TokenList paths = new TokenList();
    if(exprs.length > 2) {
      final Iter ir = qc.iter(exprs[2]);
      for(Item it; (it = ir.next()) != null;) {
        final String path = string(toToken(it));
        final String norm = MetaData.normPath(path);
        if(norm == null) throw RESINV_X.get(info, path);
        paths.add(norm);
      }
    }

    final int ps = paths.size();
    final List<NewInput> inputs = new ArrayList<>(ps);
    if(exprs.length > 1) {
      final Value val = qc.value(exprs[1]);
      // number of specified inputs and paths must be identical
      final long is = val.size();
      if(ps != 0 && is != ps) throw BXDB_CREATEARGS_X_X.get(info, is, ps);

      for(int i = 0; i < is; i++) {
        final byte[] path = i < ps ? paths.get(i) : Token.EMPTY;
        inputs.add(checkInput(val.itemAt(i), path));
      }
    }

    final Options opts = toOptions(3, Q_OPTIONS, new Options(), qc);
    qc.resources.updates().add(new DBCreate(name, inputs, opts, qc, info), qc);
    return null;
  }
}
