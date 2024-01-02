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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
abstract class DbNew extends DbAccess {
  /**
   * Creates a container for the specified input.
   * @param input input item (node or string)
   * @param path path argument (optional, can be empty)
   * @return input container
   * @throws QueryException query exception
   */
  final NewInput toNewInput(final Item input, final String path) throws QueryException {
    final NewInput ni = new NewInput();

    if(input instanceof ANode) {
      if(Strings.endsWith(path, '/')) throw RESINV_X.get(info, path);

      // ensure that the final name is not empty
      final ANode node = (ANode) input;
      String name = path;
      if(name.isEmpty()) {
        // adopt name from document node
        name = string(node.baseURI());
        final Data data = node.data();
        // adopt path if node is part of disk database. otherwise, only adopt file name
        final int i = data == null || data.inMemory() ? name.lastIndexOf('/') : name.indexOf('/');
        if(i != -1) name = name.substring(i + 1);
        if(name.isEmpty()) throw RESINV_X.get(info, name);
      }

      if(node.type == NodeType.ATTRIBUTE) throw UPDOCTYPE_X.get(info, node);
      ni.node = node;
      ni.path = name;
      return ni;
    }

    if(!input.type.isStringOrUntyped()) throw STRNOD_X_X.get(info, input.type, input);

    final String string = string(input.string(info));
    final IO io = IO.get(string);
    if(!io.exists()) throw WHICHRES_X.get(info, string);

    // add slash to the target if the addressed file is an archive or directory
    String name = path;
    if(!Strings.endsWith(name, '/') && (io.isDir() || io.isArchive())) name += "/";
    String target = "";
    final int s = name.lastIndexOf('/');
    if(s != -1) {
      target = name.substring(0, s);
      name = name.substring(s + 1);
    }

    // set name of document
    if(!name.isEmpty()) io.name(name);
    // get name from io reference
    else if(!(io instanceof IOContent)) name = io.name();

    // ensure that the final name is not empty
    if(name.isEmpty()) throw RESINV_X.get(info, path);

    ni.io = io;
    ni.path = target;
    return ni;
  }
}
