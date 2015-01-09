package org.basex.io.serial;

import javax.xml.bind.annotation.*;

/**
 * Class for testing the (un)marshalling and serialization of objects.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Michael Hedenus
 */
@XmlRootElement(name = "domain-object")
@XmlAccessorType(XmlAccessType.FIELD)
class SAXSerializerObject {
  /** Name of the object. */
  @XmlAttribute
  private String name;
  /** Value. */
  private long value;

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
