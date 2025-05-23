package org.basex.tests.bxapi.xdm;

import javax.xml.namespace.*;

import org.basex.query.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.tests.bxapi.*;
import org.basex.util.*;

/**
 * Wrapper for representing XQuery values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class XdmValue implements Iterable<XdmItem> {
  /**
   * Returns a new XQuery value.
   * @param value value
   * @return result
   */
  public static XdmValue get(final Value value) {
    final long size = value.size();
    return size == 0 ? XdmEmpty.EMPTY : size == 1 ? XdmItem.get((Item) value) :
      new XdmSequence((Seq) value);
  }

  /**
   * Returns the base URI of a node.
   * @return node name
   */
  public String getBaseURI() {
    throw new XQueryException(new QueryException("Item must be a node: " + this));
  }

  /**
   * Returns the name of a node.
   * @return node name
   */
  public QName getName() {
    throw new XQueryException(new QueryException("Item must be a node: " + this));
  }

  /**
   * Returns the boolean value.
   * @return node name
   */
  public boolean getBoolean() {
    throw new XQueryException(new QueryException(
        "Value has no boolean representation: " + this));
  }

  /**
   * Returns the integer value.
   * @return node name
   */
  public final long getInteger() {
    try {
      return Long.parseLong(getString());
    } catch(final NumberFormatException ex) {
      Util.debug(ex);
      throw new XQueryException(new QueryException(
          "Value has no integer representation: " + this));
    }
  }

  /**
   * Returns the string value.
   * @return node name
   */
  public String getString() {
    throw new XQueryException(new QueryException(
        "Value has no string representation: " + this));
  }

  /**
   * Returns the number of items stored in the value.
   * @return number of items
   */
  public abstract int size();

  /**
   * Checks if the two values are deep-equal, according to XQuery.
   * @param value second value
   * @param ordered ordered results
   * @return result of check
   * @throws XQueryException exception
   */
  public boolean deepEqual(final XdmValue value, final boolean ordered) {
    try {
      final DeepEqualOptions options = new DeepEqualOptions();
      options.set(DeepEqualOptions.ORDERED, ordered);
      return new DeepEqual(null, null, null, options).equal(internal(), value.internal());
    } catch(final QueryException ex) {
      throw new XQueryException(ex);
    }
  }

  /**
   * Returns the value type.
   * @return value type
   */
  public abstract SeqType getType();

  /**
   * Returns the internal value representation.
   * Should be made invisible to other packages.
   * @return value
   */
  public abstract Value internal();

  @Override
  public abstract String toString();
}
