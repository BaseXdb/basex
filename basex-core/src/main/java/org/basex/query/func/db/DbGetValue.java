package org.basex.query.func.db;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class DbGetValue extends DbAccess {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return value(ResourceType.VALUE, qc);
  }

  /**
   * Returns a single resource or a map with all resources of the specified type.
   * @param type resource type
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  final Value value(final ResourceType type, final QueryContext qc) throws QueryException {
    final Data data = toData(qc);
    if(data.inMemory()) throw DB_MAINMEM_X.get(info, data.meta.name);

    try {
      if(exprs.length > 1) {
        final String path = toDbPath(1, qc);
        final IOFile bin = data.meta.file(path, type);
        if(!bin.exists() || bin.isDir()) throw WHICHRES_X.get(info, path);
        return resource(bin, qc);
      }

      final MapBuilder mb = new MapBuilder(info);
      final IOFile bin = data.meta.dir(type);
      for(final String path : data.resources.paths("", type)) {
        mb.put(path, resource(type.filePath(bin, path), qc));
      }
      return mb.map();
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }

  /**
   * Returns a single resource.
   * @param path path to resource
   * @param qc query context
   * @return resource
   * @throws IOException I/O exception
   * @throws QueryException query exception
   */
  Value resource(final IOFile path, final QueryContext qc) throws IOException, QueryException {
    try(DataInput in = new DataInput(path)) {
      return Store.read(in, qc);
    }
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    if(exprs.length == 1) {
      exprType.assign(MapType.get(AtomType.STRING, SeqType.ITEM_ZM).seqType());
    }
    return this;
  }
}
