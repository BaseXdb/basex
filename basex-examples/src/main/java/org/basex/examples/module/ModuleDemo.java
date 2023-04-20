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
  public FNode create() {
    FBuilder doc = FDoc.build("http://www.example.com");
    FBuilder elem = FElem.build("root").add("attr", "value");
    return doc.add(elem).finish();
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
    FBuilder elem1 = FElem.build("root1");
    FBuilder elem2 = FElem.build("root2");
    ValueBuilder vb = new ValueBuilder(queryContext);
    vb.add(elem1.finish());
    vb.add(elem2.finish());
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
