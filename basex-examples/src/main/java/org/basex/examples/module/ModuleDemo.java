package org.basex.examples.module;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * This is a simple XQuery demo module that demonstrates how XQuery items can be
 * processed from Java. It is derived from the {@link QueryModule} class.
 *
 * If the class is in the classpath of the executed BaseX instance, it can be addressed
 * as follows:
 *
 * <pre>
 * import module namespace demo = 'http://basex.org/examples/module/module-demo';
 * demo:name(demo:create()),
 * ...
 * </pre>
 */
public class ModuleDemo extends QueryModule {
  /**
   * Creates a new example node.
   * @return node
   */
  public ANode create() {
    FDoc doc = new FDoc("http://www.example.com");
    FElem elem = new FElem("root").add("attr", "value");
    doc.add(elem);
    return doc;
  }

  /**
   * Returns the QName of a node.
   * @param node input node
   * @return qname
   */
  public QNm name(final ANode node) {
    return node.qname();
  }

  /**
   * Creates a new node sequence.
   * @return resulting value
   */
  public Value sequence() {
    FElem elem1 = new FElem("root1");
    FElem elem2 = new FElem("root2");
    ValueBuilder vb = new ValueBuilder(queryContext);
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
    ValueBuilder vb = new ValueBuilder(queryContext);
    for(final Item item : value) {
      if(item instanceof AStr) {
        vb.add(item);
      } else if(item instanceof ANode) {
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
    ValueBuilder vb = new ValueBuilder(queryContext);
    for(final Item item : value) {
      if(item instanceof DBNode) {
        final DBNode node = (DBNode) item;
        int id = node.data().id(node.pre());
        vb.add(Int.get(id));
      }
    }
    return vb.value();
  }
}
