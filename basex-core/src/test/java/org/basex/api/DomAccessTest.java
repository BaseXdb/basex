package org.basex.api;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.*;
import java.util.*;

import org.basex.api.dom.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.w3c.dom.*;

/**
 * Checks that the DOM implementation can be accessed via reflection.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DomAccessTest {
  /** Checks all node kinds and auxiliary DOM objects. */
  @Test public void access() {
    final FNode root = FDoc.build().node(FElem.build(new QNm("e")).
      attr(new QNm("a"), "v").text("t").node(new FComm(Token.token("c"))).
      node(new FPI(new QNm("p"), Token.token("i")))).finish();
    final BXDoc doc = (BXDoc) BXNode.get(root);
    final Element elem = doc.getDocumentElement();
    final NodeList children = elem.getChildNodes();

    check(doc);
    check(elem);
    check(elem.getAttributeNode("a"));
    for(int c = 0; c < children.getLength(); c++) check(children.item(c));
    check(children);
    check(elem.getAttributes());
    check(doc.createCDATASection("d"));
    check(doc.createDocumentFragment());
    check(doc.getImplementation());
  }

  /** Checks boundary cases of navigation methods. */
  @Test public void navigation() {
    final FNode root = FDoc.build().node(FElem.build(new QNm("e")).text("t")).finish();
    final BXDoc doc = (BXDoc) BXNode.get(root);
    final Element elem = doc.getDocumentElement();
    final Node text = elem.getFirstChild();

    assertFalse(doc.hasAttributes());
    assertFalse(text.hasAttributes());
    assertNull(elem.getChildNodes().item(-1));
    assertNull(elem.getChildNodes().item(1));
    assertEquals(Node.TEXT_NODE, elem.getLastChild().getNodeType());
    assertEquals("e", elem.getNodeName());
    assertEquals("#text", text.getNodeName());
    assertTrue(doc.isSameNode(text.getOwnerDocument()));
  }

  /**
   * Checks that the class and all DOM interface methods of an object are public.
   * @param object object to check
   */
  private static void check(final Object object) {
    final Class<?> clazz = object.getClass();
    assertTrue(Modifier.isPublic(clazz.getModifiers()), clazz + " is not public");
    for(final Class<?> iface : interfaces(clazz)) {
      for(final Method im : iface.getMethods()) {
        try {
          final Method method = clazz.getMethod(im.getName(), im.getParameterTypes());
          assertTrue(method.canAccess(object), method + " is not accessible");
        } catch(final NoSuchMethodException ex) {
          fail(ex);
        }
      }
    }
  }

  /**
   * Returns the DOM interfaces implemented by a class and its superclasses.
   * @param clazz class
   * @return interfaces
   */
  private static List<Class<?>> interfaces(final Class<?> clazz) {
    final List<Class<?>> list = new ArrayList<>();
    for(Class<?> c = clazz; c != null; c = c.getSuperclass()) {
      for(final Class<?> iface : c.getInterfaces()) {
        if(iface.getPackageName().equals("org.w3c.dom")) list.add(iface);
      }
    }
    return list;
  }
}
