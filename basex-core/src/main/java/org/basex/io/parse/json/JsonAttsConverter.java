package org.basex.io.parse.json;

import static org.basex.io.parse.json.JsonConstants.*;

import org.basex.build.json.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * This class converts a JSON document to an XML structure. JSON keys will be stored in attributes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public final class JsonAttsConverter extends JsonXmlConverter {
  /**
   * Constructor.
   * @param opts JSON options
   * @throws QueryException query exception
   */
  JsonAttsConverter(final JsonParserOptions opts) throws QueryException {
    super(opts);
  }

  @Override
  protected void openObject() {
    openOuter(OBJECT);
  }

  @Override
  protected void closeObject() {
    closeOuter();
  }

  @Override
  protected void openPair(final byte[] key, final boolean add) {
    addValues.add(add);
    if(add) {
      openInner(Q_PAIR);
      name = shared.token(key);
      curr.add(Q_NAME, name);
    }
  }

  @Override
  protected void closePair(final boolean add) {
    if(add) {
      closeInner();
      name = null;
    }
    addValues.pop();
  }

  @Override
  protected void openArray() {
    openOuter(ARRAY);
  }

  @Override
  protected void closeArray() {
    closeOuter();
  }

  @Override
  protected void openItem() {
    openInner(Q_ITEM);
  }

  @Override
  protected void closeItem() {
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
  private void openInner(final QNm type) {
    curr = FElem.build(type);
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
    if(curr == null) curr = FElem.build(Q_JSON);
    processType(curr, type);
    return curr;
  }
}
