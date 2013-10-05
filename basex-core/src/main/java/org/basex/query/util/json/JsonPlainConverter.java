package org.basex.query.util.json;

import static org.basex.data.DataText.*;
import static org.basex.util.Token.*;

import org.basex.build.*;
import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * <p>This class converts a JSON document to a plain XML structure.</p>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 * @author Leo Woerteler
 */
public class JsonPlainConverter extends JsonXMLConverter implements JsonHandler {
  /** Current root element. */
  private FElem elem;

  /**
   * Constructor.
   * @param opts json options
   * @param ii input info
   */
  public JsonPlainConverter(final JsonOptions opts, final InputInfo ii) {
    super(opts, ii);
  }

  @Override
  public ANode convert(final String in) throws QueryException {
    JsonParser.parse(in, jopts, this, info);
    return elem;
  }

  @Override
  public void openObject() {
    if(elem == null) elem = new FElem(T_JSON);
    elem.add(T_TYPE, T_OBJECT);
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
    if(elem == null) elem = new FElem(T_JSON);
    elem.add(T_TYPE, T_ARRAY);
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
    elem.add(T_TYPE, T_NUMBER).add(value);
  }

  @Override
  public void stringLit(final byte[] value) {
    elem.add(T_TYPE, T_STRING).add(value);
  }

  @Override
  public void nullLit() {
    elem.add(T_TYPE, NULL);
  }

  @Override
  public void booleanLit(final byte[] value) {
    elem.add(T_TYPE, T_BOOLEAN).add(value);
  }
}
