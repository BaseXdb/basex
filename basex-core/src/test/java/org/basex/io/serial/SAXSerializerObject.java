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
public class SAXSerializerObject {
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
   * @param name name
   * @param value value
   */
  SAXSerializerObject(final String name, final long value) {
    this.name = name;
    this.value = value;
  }

  /**
   * Returns the value.
   * @return value
   */
  public long getValue() {
    return value;
  }
}
