package org.basex.examples.query;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * This is a simple XQuery demo module written in Java.
 * It is derived from the abstract {@link QueryModule} class.
 */
public class QueryModuleExamples extends QueryModule {
  /**
   * Returns the QName of a node.
   * @param node input node
   * @return qname
   */
  public QNm name(final ANode node) {
    return node.qname();
  }

  /**
   * Creates a new example node.
   * @return node
   */
  public ANode create() {
    FDoc doc = new FDoc(Token.token("http://www.example.com"));
    FElem elem = new FElem(new QNm("root"));
    elem.add(new FAttr(new QNm("attr"), Token.token("value")));
    doc.add(elem);
    return doc;
  }

  /**
   * Creates a new node sequence.
   * @return resulting value
   */
  public Value sequence() {
    FElem elem1 = new FElem(new QNm("root1"));
    FElem elem2 = new FElem(new QNm("root2"));
    ValueBuilder vb = new ValueBuilder();
    vb.add(elem1);
    vb.add(elem2);
    return vb.value();
  }

  /**
   * Creates a sequence with parts of the input.
   * @param value value
   * @return resulting value
   */
  public Value value(final Value value) {
    ValueBuilder vb = new ValueBuilder();
    for(final Item item : value) {
      if(item.type == AtomType.STR) {
        vb.add(item);
      } else if(item.type.isNode()) {
        ANode node = (ANode) item;
        vb.add(node.qname());
      }
    }
    return vb.value();
  }

  /**
   * Returns all id values of the specified database nodes.
   * @param value value
   * @return resulting value
   */
  public Value dbnodes(final Value value) {
    ValueBuilder vb = new ValueBuilder();
    for(final Item item : value) {
      if(item instanceof DBNode) {
        DBNode node = (DBNode) item;
        int id = node.data.id(node.pre);
        vb.add(Int.get(id));
      }
    }
    return vb.value();
  }
}
