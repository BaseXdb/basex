package org.basex.io.parse.json;

import static org.basex.io.parse.json.JsonConstants.*;

import org.basex.build.json.*;
import org.basex.query.*;
import org.basex.query.value.node.*;

/**
 * This class converts a JSON document to an XML structure. JSON keys will be stored in attributes.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class JsonAttsConverter extends JsonXmlConverter {
  /**
   * Constructor.
   * @param opts json options
   * @throws QueryIOException query I/O exception
   */
  JsonAttsConverter(final JsonParserOptions opts) throws QueryIOException {
    super(opts);
  }

  @Override
  void openObject() {
    openOuter(OBJECT);
  }

  @Override
  void closeObject() {
    closeOuter();
  }

  @Override
  void openPair(final byte[] key, final boolean add) throws QueryIOException {
    addValues.add(add);
    if(add) {
      openInner(PAIR);
      curr.add(NAME, shared.token(key));
      name = key;
    }
  }

  @Override
  void closePair(final boolean add) {
    if(add) {
      closeInner();
      name = null;
    }
    addValues.pop();
  }

  @Override
  void openArray() {
    openOuter(ARRAY);
  }

  @Override
  void closeArray() {
    closeOuter();
  }

  @Override
  void openItem() {
    openInner(ITEM);
  }

  @Override
  void closeItem() {
    closeInner();
  }

  @Override
  void addValue(final byte[] type, final byte[] value) {
    if(addValues.peek()) element(type).add(value != null ? shared.token(value) : null);
  }

  /**
   * Opens an outer entry.
   * @param type JSON type
   */
  private void openOuter(final byte[] type) {
    stack.push(element(type));
  }

  /**
   * Closes an outer entry.
   */
  private void closeOuter() {
    stack.pop();
  }

  /**
   * Opens an inner entry.
   * @param type JSON type
   */
  private void openInner(final byte[] type) {
    curr = FElem.build(shared.qnm(type, null));
    stack.push(curr);
  }

  /**
   * Closes an inner entry.
   */
  private void closeInner() {
    curr = stack.pop();
    if(!stack.isEmpty()) curr = stack.peek().add(curr);
  }

  /**
   * Creates a new element with the given type.
   * @param type JSON type
   * @return new element
   */
  private FBuilder element(final byte[] type) {
    if(curr == null) curr = FElem.build(shared.qnm(JSON, null));
    processType(curr, type);
    return curr;
  }
}
