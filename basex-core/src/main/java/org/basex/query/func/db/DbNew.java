package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
abstract class DbNew extends DbAccess {
  /** Element: parameters. */
  static final QNm Q_OPTIONS = new QNm("options");

  /**
   * Creates a {@link Data} instance for the specified document.
   * @param input input item (node or string)
   * @param path optional path argument
   * @return database instance
   * @throws QueryException query exception
   */
  final NewInput checkInput(final Item input, final byte[] path) throws QueryException {
    final NewInput ni = new NewInput();

    if(input instanceof ANode) {
      if(endsWith(path, '.') || endsWith(path, '/')) throw RESINV_X.get(info, path);

      // ensure that the final name is not empty
      ANode node = (ANode) input;
      byte[] name = path;
      if(name.length == 0) {
        // adopt name from document node
        name = node.baseURI();
        final Data data = node.data();
        // adopt path if node is part of disk database. otherwise, only adopt file name
        final int i = data == null || data.inMemory() ? lastIndexOf(name, '/') : indexOf(name, '/');
        if(i != -1) name = substring(name, i + 1);
        if(name.length == 0) throw RESINV_X.get(info, name);
      }

      // adding a document node
      if(node.type == NodeType.ATT) throw UPDOCTYPE_X.get(info, node);
      ni.node = node;
      ni.path = string(name);
      return ni;
    }

    if(!input.type.isStringOrUntyped()) throw STRNOD_X_X.get(info, input.type, input);

    final QueryInput qi = new QueryInput(string(input.string(info)));
    if(!qi.input.exists()) throw WHICHRES_X.get(info, qi.original);

    // add slash to the target if the addressed file is an archive or directory
    String name = string(path);
    if(name.endsWith(".")) throw RESINV_X.get(info, path);
    if(!name.endsWith("/") && (qi.input.isDir() || qi.input.isArchive())) name += "/";
    String target = "";
    final int s = name.lastIndexOf('/');
    if(s != -1) {
      target = name.substring(0, s);
      name = name.substring(s + 1);
    }

    // set name of document
    if(!name.isEmpty()) qi.input.name(name);
    // get name from io reference
    else if(!(qi.input instanceof IOContent)) name = qi.input.name();

    // ensure that the final name is not empty
    if(name.isEmpty()) throw RESINV_X.get(info, path);

    ni.io = qi.input;
    ni.path = target;
    return ni;
  }
}
