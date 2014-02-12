package org.basex.io.serial;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class for testing the (un)marshalling and serialization of objects.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Michael Hedenus
 */
@XmlRootElement(name = "domain-object")
@XmlAccessorType(XmlAccessType.FIELD)
public class SAXSerializerObject {
  /** Name of the object. */
  @XmlAttribute
  String name;
  /** Value. */
  long value;

  /**
   * Empty constructor.
   */
  public SAXSerializerObject() {
  }

  /**
   * Empty constructor, specifying initial values.
   * @param nm name
   * @param val value
   */
  public SAXSerializerObject(final String nm, final long val) {
    name = nm;
    value = val;
  }

  /**
   * Returns the value.
   * @return value
   */
  public long getValue() {
    return value;
  }
}
