package org.basex.query.func.db;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.data.*;
import org.basex.index.resource.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.up.primitives.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
abstract class DbNew extends DbAccessFn {
  /**
   * Creates a container for the specified XML input. Used by functions that only accept
   * documents/nodes or IO references parsed as XML (e.g. db:add, db:put).
   * @param input input item (node or string)
   * @param path path argument (optional, can be empty)
   * @return input container
   * @throws QueryException query exception
   */
  final NewInput toNewInput(final Item input, final String path) throws QueryException {
    return toNewInput(input, path, ResourceType.XML);
  }

  /**
   * Creates a container for the specified input, routing it to the requested resource type.
   * If {@code type} is {@code null}, the resource type is inferred from the item type:
   * nodes and string-typed items become XML, binary atomics become BINARY, everything else
   * becomes VALUE.
   * @param input input item
   * @param path path argument (optional for XML, required for BINARY and VALUE)
   * @param type explicit target resource type, or {@code null} for type dispatch
   * @return input container
   * @throws QueryException query exception
   */
  final NewInput toNewInput(final Item input, final String path, final ResourceType type)
      throws QueryException {
    final NewInput ni = new NewInput();
    ni.type = type != null ? type : input.type.isStringOrUntyped() ? ResourceType.XML :
      input instanceof Bin ? ResourceType.BINARY : ResourceType.VALUE;
    switch(ni.type) {
      case XML    -> fillXmlInput(ni, input, path);
      case BINARY -> fillBinaryInput(ni, input, path);
      case VALUE  -> fillValueInput(ni, input, path);
    }
    return ni;
  }

  /**
   * Populates a NewInput for an XML resource.
   * @param ni new input container
   * @param input input item (node or string-typed IO reference)
   * @param path path argument (optional, can be empty)
   * @throws QueryException query exception
   */
  private void fillXmlInput(final NewInput ni, final Item input, final String path)
      throws QueryException {
    if(input instanceof final XNode node) {
      if(Strings.endsWith(path, '/')) throw DB_PATH_X.get(info, path);

      // ensure that the final name is not empty
      String name = path;
      if(name.isEmpty()) {
        // adopt name from document node
        name = string(node.baseURI());
        final Data data = node.data();
        // adopt path if node is part of disk database. otherwise, only adopt file name
        final int i = data == null || data.inMemory() ? name.lastIndexOf('/') : name.indexOf('/');
        if(i != -1) name = name.substring(i + 1);
        if(name.isEmpty()) throw DB_PATH_X.get(info, name);
      }

      if(node.kind() == Kind.ATTRIBUTE) throw UPDOCTYPE_X.get(info, node);
      ni.node = node;
      ni.path = name;
      return;
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
    if(name.isEmpty()) throw DB_PATH_X.get(info, path);

    ni.io = io;
    ni.path = target;
  }

  /**
   * Populates a NewInput for a binary resource.
   * @param ni new input container
   * @param input input item (binary literal or string-typed IO reference)
   * @param path target path (must not be empty)
   * @throws QueryException query exception
   */
  private void fillBinaryInput(final NewInput ni, final Item input, final String path)
      throws QueryException {
    if(path.isEmpty() || Strings.endsWith(path, '/')) throw DB_PATH_X.get(info, path);
    ni.path = path;
    final Object source = toBinarySource(input);
    if(source instanceof Bin) ni.value = (Bin) source;
    else ni.io = (IO) source;
  }

  /**
   * Populates a NewInput for a value resource (db:put-value semantics).
   * @param ni new input container
   * @param input input item (any XDM item, stored verbatim)
   * @param path target path (must not be empty)
   * @throws QueryException query exception
   */
  private void fillValueInput(final NewInput ni, final Item input, final String path)
      throws QueryException {
    if(path.isEmpty() || Strings.endsWith(path, '/')) throw DB_PATH_X.get(info, path);
    ni.path = path;
    ni.value = input;
  }

  /**
   * Builds the input array from the {@code inputs} and {@code paths} arguments
   * (positional args 1 and 2). Each path entry can be either a string or a single-entry map
   * mapping the path to an explicit resource type. If paths are supplied, their count must
   * match the input count.
   * @param qc query context
   * @return new inputs
   * @throws QueryException query exception
   */
  final NewInput[] toInputs(final QueryContext qc) throws QueryException {
    final List<PathSpec> paths = new ArrayList<>();
    final Iter iter = arg(2).iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) paths.add(toPathSpec(item));

    final Value value = arg(1).value(qc);
    final long is = value.size();
    final int ps = paths.size();
    if(ps != 0 && is != ps) throw DB_ARGS_X_X.get(info, is, ps);

    final NewInput[] inputs = new NewInput[(int) is];
    for(int i = 0; i < is; i++) {
      qc.checkStop();
      final PathSpec spec = i < ps ? paths.get(i) : null;
      final String path = spec != null ? spec.path() : "";
      final ResourceType type = spec != null ? spec.type() : null;
      inputs[i] = toNewInput(value.itemAt(i), path, type);
    }
    return inputs;
  }

  /**
   * Parses a single path argument: either a string (path only, resource type via dispatch)
   * or a single-entry map mapping the path to an explicit resource type.
   * @param item path item
   * @return path specification
   * @throws QueryException query exception
   */
  private PathSpec toPathSpec(final Item item) throws QueryException {
    if(item instanceof final XQMap map) {
      if(map.structSize() != 1) throw DB_PATH_X.get(info, map);
      final XQMap.Entry entry = map.entries().iterator().next();
      final Value value = entry.value();
      if(!(value instanceof final Item type)) throw DB_PATH_X.get(info, map);
      return new PathSpec(toDbPath(toString(entry.key())), toEnum(type, ResourceType.class));
    }
    return new PathSpec(toDbPath(toString(item)), null);
  }

  /**
   * Target path with optional explicit resource type ({@code null} for type dispatch).
   * @param path target path
   * @param type explicit resource type, or {@code null}
   */
  private record PathSpec(String path, ResourceType type) { }

  /**
   * Checks if a PUT operation should be performed.
   * @param docs existing documents
   * @param data data reference
   * @param path target path
   * @param options options
   * @return result of check
   */
  final boolean put(final IntList docs, final Data data, final String path,
      final HashMap<String, String> options) {
    final String pr = options.get(MainOptions.REPLACE.name().toLowerCase(Locale.ENGLISH));
    if(pr == null || Strings.toBoolean(pr)) return true;

    boolean add = docs.isEmpty();
    for(final ResourceType type : Resources.BINARIES) {
      final IOFile bin = data.meta.file(path, type);
      add &= bin == null || !bin.exists();
    }
    return add;
  }
}
