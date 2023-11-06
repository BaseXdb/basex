package org.basex.io.parse.json;

import static org.basex.io.parse.json.JsonConstants.*;
import static org.basex.util.Token.*;

import org.basex.build.json.*;
import org.basex.query.*;
import org.basex.query.value.node.*;

/**
 * <p>This class converts a JSON document to XML.</p>
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class JsonBasicConverter extends JsonXmlConverter {
  /** Escape characters. */
  private final boolean escape;

  /**
   * Constructor.
   * @param opts json options
   * @throws QueryIOException query I/O exception
   */
  JsonBasicConverter(final JsonParserOptions opts) throws QueryIOException {
    super(opts);
    escape = jopts.get(JsonParserOptions.ESCAPE);
  }

  @Override
  void openObject() {
    openOuter(MAP);
  }

  @Override
  void closeObject() {
    closeOuter();
  }

  @Override
  void openPair(final byte[] key, final boolean add) {
    addValues.add(add);
    if(add) name = shared.token(key);
  }

  @Override
  void closePair(final boolean add) {
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
  void openItem() { }

  @Override
  void closeItem() { }

  @Override
  void addValue(final byte[] type, final byte[] value) {
    if(addValues.peek()) {
      final byte[] val = value != null ? shared.token(value) : null;
      final FBuilder elem = element(type).add(val);
      if(escape && value != null && contains(val, '\\')) elem.add(Q_ESCAPED, TRUE);
      if(curr != null) curr.add(elem);
      else curr = elem;
    }
  }

  /**
   * Opens an outer entry.
   * @param type JSON type
   */
  private void openOuter(final byte[] type) {
    curr = element(type);
    if(stack.isEmpty()) curr.declareNS();
    stack.push(curr);
  }

  /**
   * Closes an outer entry.
   */
  private void closeOuter() {
    curr = stack.pop();
    if(!stack.isEmpty()) curr = stack.peek().add(curr);
  }

  /**
   * Creates a new element with the given type.
   * @param type JSON type
   * @return new element
   */
  private FBuilder element(final byte[] type) {
    final FBuilder elem = FElem.build(shared.qName(type, QueryText.FN_URI));
    if(name != null) {
      elem.add(Q_KEY, name);
      if(escape && contains(name, '\\')) elem.add(Q_ESCAPED_KEY, TRUE);
      name = null;
    }
    return elem;
  }
}
