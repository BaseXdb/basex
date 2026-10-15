package org.basex.query.func.db;

import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.query.*;
import org.basex.query.up.*;
import org.basex.query.up.primitives.*;
import org.basex.query.up.primitives.db.*;
import org.basex.query.up.primitives.node.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DbPut extends DbNew {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    final Item input = toNodeOrAtomItem(arg(1), false, qc);
    final String path = toDbPath(arg(2), qc);
    final XQMap options = toEmptyMap(arg(3), qc);

    if(input instanceof Bin) {
      // store binary resource
      putFile(data, path, ResourceType.BINARY, new DBPutBinary(data, input, path, info), options,
          qc);
    } else {
      // directories and archives replace all documents at and below the path
      final NewInput ni = toNewInput(input, path, qc);
      final boolean tree = ni.io != null && (ni.io.isDir() || ni.io.isArchive());
      final IntList docs = tree ? data.resources.docs(path) : data.resources.docsAt(path);
      if(put(docs, tree ? 0 : data.resources.files(path), options)) {
        // store XML documents: replace existing document or add new ones
        final Updates updates = qc.updates();
        final XQMap opts = options.remove(REPLACE);
        int d = 0;
        final Update update = docs.isEmpty() ?
          new DBAdd(data, opts, true, qc, info, ni) :
          new ReplaceDoc(docs.get(d++), data, ni, opts, qc, info);
        updates.add(update, qc);
        if(!tree) deleteFiles(data, path, null, qc);
        // delete spare documents
        final int ds = docs.size();
        for(; d < ds; d++) updates.add(new DeleteNode(docs.get(d), data, info), qc);
      }
    }
    return Empty.VALUE;
  }
}
