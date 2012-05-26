package org.basex.query.util.json;

import static org.basex.data.DataText.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.item.*;
import org.basex.query.util.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * <p>This class converts a <a href="http://jsonml.org">JsonML</a>
 * document to XML.
 * The specified JSON input is first transformed into a tree representation
 * and then converted to an XML document.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class JsonMLConverter extends XMLConverter {
  /** Cached names. */
  private final TokenObjMap<QNm> qnames = new TokenObjMap<QNm>();

  /**
   * Constructor.
   * @param ii input info
   */
  public JsonMLConverter(final InputInfo ii) {
    super(ii);
  }

  @Override
  public ANode parse(final byte[] in) throws QueryException {
    // create and return XML fragment
    return create(new JSONParser(in, info).parse());
  }

  /**
   * Converts the JSON tree to XML.
   * @param value node to be converted
   * @return root node
   * @throws QueryException query exception
   */
  private FElem create(final JValue value) throws QueryException {
    return elem((JArray) check(value, T_ARRAY, "element constructor"));
  }

  /**
   * Converts an element node.
   * @param value node to be converted
   * @return root node
   * @throws QueryException query exception
   */
  private FElem elem(final JArray value) throws QueryException {
    FElem elem = null;
    boolean txt = false;
    for(int s = 0; s < value.size(); s++) {
      final JValue val = value.value(s);
      if(s == 0) {
        final JString str = (JString) check(val, T_STRING, "element name");
        elem = new FElem(qname(str.value));
      } else if(s == 1 && val instanceof JObject) {
        attr(elem, (JObject) val);
      } else if(val instanceof JArray) {
        elem.add(elem((JArray) val));
        txt = false;
      } else if(val instanceof JString) {
        if(txt) error("No subsequent texts allowed");
        txt = true;
        elem.add(((JString) val).value);
      } else {
        error("No % allowed at this stage", val.type());
      }
    }
    if(elem == null) error("No element name specified in array");
    return elem;
  }

  /**
   * Converts attributes.
   * @param elem root node
   * @param attr attributes
   * @throws QueryException query exception
   */
  private void attr(final FElem elem, final JObject attr) throws QueryException {
    for(int s = 0; s < attr.size(); s++) {
      final JString v = (JString) check(attr.value(s), T_STRING, "attribute value");
      elem.add(new FAttr(qname(attr.name(s)), v.value));
    }
  }

  /**
   * Returns a cached {@link QNm} instance for the specified name.
   * @param name name
   * @return cached QName
   * @throws QueryException query exception
   */
  private QNm qname(final byte[] name) throws QueryException {
    // retrieve name from cache, or create new instance
    QNm qname = qnames.get(name);
    if(qname == null) {
      if(!XMLToken.isNCName(name)) error("Invalid name: \"%\"", name);
      qname = new QNm(name);
      qnames.add(name, qname);
    }
    return qname;
  }

  /**
   * Checks the type of the specified value.
   * @param val value to be checked
   * @param type expected type
   * @param ext error extension
   * @return checked value
   * @throws QueryException query exception
   */
  private JValue check(final JValue val, final byte[] type, final String ext)
      throws QueryException {
    if(!eq(val.type(), type))
      error("% expected for %, % found", type, ext, val.type());
    return val;
  }

  /**
   * Raises an error with the specified message.
   * @param msg error message
   * @param ext error details
   * @throws QueryException query exception
   */
  private void error(final String msg, final Object... ext) throws QueryException {
    throw BXJS_PARSEML.thrw(info, Util.inf(msg, ext));
  }
}
