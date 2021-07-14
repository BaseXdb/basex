package org.basex.query.ann;

import static org.basex.query.QueryText.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Annotation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Ann {
  /** Input info. */
  public final InputInfo info;
  /** Annotation definition ({@code null} if {@link #name} is assigned). */
  public final Annotation definition;
  /** No-standard annotation ({@code null} if {@link #definition} is assigned). */
  private final QNm name;
  /** Value. */
  private final Value value;

  /**
   * Constructor.
   * @param info input info
   * @param definition definition
   * @param value value
   */
  public Ann(final InputInfo info, final Annotation definition, final Value value) {
    this.info = info;
    this.value = value;
    this.definition = definition;
    name = null;
  }

  /**
   * Constructor.
   * @param info input info
   * @param name name
   * @param value value
   */
  public Ann(final InputInfo info, final QNm name, final Value value) {
    this.info = info;
    this.value = value;
    this.name = name;
    definition = null;
  }

  /**
   * Returns the name of the annotation.
   * @return name
   */
  public QNm name() {
    return definition != null ? definition.qname() : name;
  }

  /**
   * Returns the value of the annotation.
   * @return value
   */
  public Value value() {
    return value;
  }

  @Override
  public boolean equals(final Object obj) {
    if(this == obj) return true;
    if(!(obj instanceof Ann)) return false;
    final Ann ann = (Ann) obj;
    return (name != null ? ann.name != null && name.eq(ann.name) : definition == ann.definition) &&
        value.equals(ann.value);
  }

  /**
   * Adds the annotation to a query string.
   * @param qs query string builder
   */
  public void toString(final QueryString qs) {
    qs.concat("%", definition != null ? definition.id() : name.prefixId(XQ_URI));
    if(!value.isEmpty()) {
      final StringList list = new StringList(value.size());
      for(final Item item : value) list.add(item.toString());
      qs.params(list.finish());
    }
  }
}
