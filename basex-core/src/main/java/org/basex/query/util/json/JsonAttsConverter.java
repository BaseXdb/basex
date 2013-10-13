package org.basex.query.util.json;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import org.basex.build.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;

/**
 * This class converts a JSON document to a XML structure. JSON keys will be stored in attributes.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public class JsonAttsConverter extends JsonXMLConverter implements JsonHandler {
  /** Current root element. */
  private FElem elem;

  /**
   * Constructor.
   * @param opts json options
   */
  public JsonAttsConverter(final JsonOptions opts) {
    super(opts);
  }

  @Override
  public ANode convert(final String in) throws QueryIOException {
    JsonParser.parse(in, jopts, this);
    return element();
  }

  @Override
  public void openObject() {
    element().add(T_TYPE, T_OBJECT);
  }

  @Override
  public void openPair(final byte[] key) {
    final FElem e = new FElem(T_PAIR).add(T_NAME, key);
    elem.add(e);
    elem = e;
  }

  @Override
  public void closePair() {
    elem = (FElem) elem.parent();
  }

  @Override
  public void closeObject() {
  }

  @Override
  public void openArray() {
    element().add(T_TYPE, T_ARRAY);
  }

  @Override
  public void openItem() {
    final FElem e = new FElem(T_ITEM);
    elem.add(e);
    elem = e;
  }

  @Override
  public void closeItem() {
    elem = (FElem) elem.parent();
  }

  @Override
  public void closeArray() {
  }

  @Override
  public void openConstr(final byte[] nm) {
    openObject();
    openPair(nm);
    openArray();
  }

  @Override
  public void openArg() {
    openItem();
  }

  @Override
  public void closeArg() {
    closeItem();
  }

  @Override
  public void closeConstr() {
    closeArray();
    closePair();
    closeObject();
  }

  @Override
  public void numberLit(final byte[] value) {
    element().add(T_TYPE, T_NUMBER).add(value);
  }

  @Override
  public void stringLit(final byte[] value) {
    element().add(T_TYPE, T_STRING).add(value);
  }

  @Override
  public void nullLit() {
    element().add(T_TYPE, NULL);
  }

  @Override
  public void booleanLit(final byte[] value) {
    element().add(T_TYPE, T_BOOLEAN).add(value);
  }

  /**
   * Returns the current element.
   * @return element
   */
  private FElem element() {
    if(elem == null) elem = new FElem(T_JSON);
    return elem;
  }
}
