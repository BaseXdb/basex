package org.basex.io.parse.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.build.json.*;
import org.basex.build.json.JsonParserOptions.JsonDuplicates;
import org.basex.query.*;
import org.basex.query.value.node.*;
import org.basex.util.list.*;

/**
 * <p>This class converts a JSON document to XML.</p>
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class JsonBasicConverter extends JsonXmlConverter {
  /** Add pairs. */
  private final BoolList addPairs = new BoolList();
  /** Escape characters. */
  private final boolean escape;
  /** Name of next element. */
  private byte[] name;

  /**
   * Constructor.
   * @param opts json options
   * @throws QueryIOException query I/O exception
   */
  JsonBasicConverter(final JsonParserOptions opts) throws QueryIOException {
    super(opts);
    escape = jopts.get(JsonParserOptions.ESCAPE);
    addPairs.add(true);
    final JsonDuplicates dupl = jopts.get(JsonParserOptions.DUPLICATES);
    if(dupl == JsonDuplicates.USE_LAST) throw new QueryIOException(
        JSON_OPTIONS_X.get(null, JsonParserOptions.DUPLICATES.name(), dupl));
  }

  @Override
  void openObject() {
    open(MAP);
  }

  @Override
  void openPair(final byte[] key, final boolean add) {
    name = key;
    addPairs.add(add() && add);
  }

  @Override
  void closePair(final boolean add) {
    addPairs.pop();
  }

  @Override
  void closeObject() {
    close();
  }

  @Override
  void openArray() {
    open(ARRAY);
  }

  @Override
  void openItem() { }

  @Override
  void closeItem() { }

  @Override
  void closeArray() {
    close();
  }

  @Override
  public void numberLit(final byte[] value) {
    if(add()) addElem(NUMBER).add(value);
  }

  @Override
  public void stringLit(final byte[] value) {
    if(add()) {
      final FElem elem = addElem(STRING).add(value);
      if(escape && contains(value, '\\')) elem.add(ESCAPED, TRUE);
    }
  }

  @Override
  public void nullLit() {
    if(add()) addElem(NULL);
  }

  @Override
  public void booleanLit(final byte[] value) {
    if(add()) addElem(BOOLEAN).add(value);
  }

  /**
   * Adds a new element with the given type.
   * @param type JSON type
   * @return new element
   */
  private FElem addElem(final byte[] type) {
    final FElem elem = new FElem(type, QueryText.FN_URI).declareNS();

    if(name != null) {
      elem.add(KEY, name);
      if(escape && contains(name, '\\')) elem.add(ESCAPED_KEY, TRUE);
      name = null;
    }

    if(curr != null) curr.add(elem);
    else curr = elem;
    return elem;
  }

  /**
   * Opens an entry.
   * @param type JSON type
   */
  private void open(final byte[] type) {
    if(add()) curr = addElem(type);
  }

  /**
   * Closes an entry.
   */
  private void close() {
    if(add()) {
      final FElem par = (FElem) curr.parent();
      if(par != null) curr = par;
    }
  }

  /**
   * Indicates if an entry should be added.
   * @return result of check
   */
  private boolean add() {
    return addPairs.peek();
  }
}
