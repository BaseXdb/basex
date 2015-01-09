package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the DirElemContent.namespace production.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdDirElemContentNamespace extends QT3TestSet {

  /**
   *  Rename inscope namespace .
   */
  @org.junit.Test
  public void constrInscope1() {
    final XQuery query = new XQuery(
      "<new xmlns:foo=\"http://www.example.com\">{//@*:attr1}</new>",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent.namespace/inscope.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new xmlns:foo=\"http://www.example.com\" xmlns:XXX=\"http://www.example.com/parent1\" XXX:attr1=\"attr1\"/>", true)
    );
  }

  /**
   *  copy element node with namespace .
   */
  @org.junit.Test
  public void constrInscope10() {
    final XQuery query = new XQuery(
      "<new xmlns=\"http://www.example.com\">{//*:child2}</new>",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent.namespace/inscope.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new xmlns=\"http://www.example.com\"><child2 xmlns=\"\" xmlns:foo=\"http://www.example.com/parent2\" attr=\"child\"/></new>", false)
    );
  }

  /**
   *  copy element node with namespace .
   */
  @org.junit.Test
  public void constrInscope11() {
    final XQuery query = new XQuery(
      "for $x in <parent1 xmlns:foo=\"http://www.example.com/parent1\" foo:attr1=\"attr1\"><child1 attr=\"child\"/></parent1> return <new>{$x//*:child1}</new>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new><child1 xmlns:foo=\"http://www.example.com/parent1\" attr=\"child\"/></new>", false)
    );
  }

  /**
   *  copy element node with namespace .
   */
  @org.junit.Test
  public void constrInscope12() {
    final XQuery query = new XQuery(
      "for $x in <parent2 xmlns:foo=\"http://www.example.com/parent2\" foo:attr2=\"attr2\"><child2 attr=\"child\"/></parent2> return <new xmlns=\"http://www.example.com\">{$x//*:child2}</new>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new xmlns=\"http://www.example.com\"><child2 xmlns=\"\" xmlns:foo=\"http://www.example.com/parent2\" attr=\"child\"/></new>", false)
    );
  }

  /**
   *  declared element namespace .
   */
  @org.junit.Test
  public void constrInscope13() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://example.com\"; <new/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new/>", false)
    );
  }

  /**
   *  declared element namespace .
   */
  @org.junit.Test
  public void constrInscope14() {
    final XQuery query = new XQuery(
      "declare namespace foo = \"http://example.com\"; <foo:new/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<foo:new xmlns:foo=\"http://example.com\"/>", false)
    );
  }

  /**
   *  element with prefix xml .
   */
  @org.junit.Test
  public void constrInscope15() {
    final XQuery query = new XQuery(
      "<xml:new/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<xml:new/>", false)
    );
  }

  /**
   *  attribute with prefix xml .
   */
  @org.junit.Test
  public void constrInscope16() {
    final XQuery query = new XQuery(
      "<new xml:attr=\"foo\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new xml:attr=\"foo\"/>", false)
    );
  }

  /**
   *  copy element with same prefix .
   */
  @org.junit.Test
  public void constrInscope17() {
    final XQuery query = new XQuery(
      "<new xmlns:foo=\"http://www.example.com\">{//*:child1}</new>",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent.namespace/inscope.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new xmlns:foo=\"http://www.example.com\"><child1 xmlns:foo=\"http://www.example.com/parent1\" attr=\"child\"/></new>", false)
    );
  }

  /**
   *  copy element with same prefix .
   */
  @org.junit.Test
  public void constrInscope18() {
    final XQuery query = new XQuery(
      "<new xmlns:foo=\"http://www.example.com/parent1\">{//*:child1}</new>",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent.namespace/inscope.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new xmlns:foo=\"http://www.example.com/parent1\"><child1 attr=\"child\"/></new>", false)
    );
  }

  /**
   *  copy element with different default namespace .
   */
  @org.junit.Test
  public void constrInscope19() {
    final XQuery query = new XQuery(
      "<new xmlns=\"http://www.example.com\">{//*:child4}</new>",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent.namespace/inscope.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new xmlns=\"http://www.example.com\"><child4 xmlns=\"http://www.example.com/parent4\"/></new>", false)
    );
  }

  /**
   *  Rename inscope namespace .
   */
  @org.junit.Test
  public void constrInscope2() {
    final XQuery query = new XQuery(
      "<new>{//@*:attr1, //@*:attr2}</new>",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent.namespace/inscope.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new xmlns:foo=\"http://www.example.com/parent1\" xmlns:XXX=\"http://www.example.com/parent2\" foo:attr1=\"attr1\" XXX:attr2=\"attr2\"/>", true)
    );
  }

  /**
   *  copy element with same default namespace .
   */
  @org.junit.Test
  public void constrInscope20() {
    final XQuery query = new XQuery(
      "<new xmlns=\"http://www.example.com/parent4\">{//*:child4}</new>",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent.namespace/inscope.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new xmlns=\"http://www.example.com/parent4\"><child4/></new>", false)
    );
  }

  /**
   *  namespace not declared: see bug 17040 (cezar.andrei@gmail.com).
   */
  @org.junit.Test
  public void constrInscope21() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace cat ='mycat'; \n" +
      "        <a t='cat:miau'>42</a>\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a t='cat:miau'>42</a>", false)
    );
  }

  /**
   *  namespace not declared: see bug 17040 (cezar.andrei@gmail.com).
   */
  @org.junit.Test
  public void constrInscope22() {
    final XQuery query = new XQuery(
      "\n" +
      "        <a xsi:type='xs:integer'>42</a>\n" +
      "      ",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type='xs:integer'>42</a>", false)
    );
  }

  /**
   *  Rename inscope namespace .
   */
  @org.junit.Test
  public void constrInscope3() {
    final XQuery query = new XQuery(
      "for $x in <parent1 xmlns:foo=\"http://www.example.com/parent1\" foo:attr1=\"attr1\"/> return <new xmlns:foo=\"http://www.example.com\">{$x//@*:attr1}</new>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new xmlns:foo=\"http://www.example.com\" xmlns:XXX=\"http://www.example.com/parent1\" XXX:attr1=\"attr1\"/>", true)
    );
  }

  /**
   *  Rename inscope namespace .
   */
  @org.junit.Test
  public void constrInscope4() {
    final XQuery query = new XQuery(
      "for $x in <inscope> <parent1 xmlns:foo=\"http://www.example.com/parent1\" foo:attr1=\"attr1\"/> <parent2 xmlns:foo=\"http://www.example.com/parent2\" foo:attr2=\"attr2\"/></inscope> return <new>{$x//@*:attr1, $x//@*:attr2}</new>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new xmlns:foo=\"http://www.example.com/parent1\" xmlns:XXX=\"http://www.example.com/parent2\" foo:attr1=\"attr1\" XXX:attr2=\"attr2\"/>", true)
    );
  }

  /**
   *  copy element node with namespace .
   */
  @org.junit.Test
  public void constrInscope5() {
    final XQuery query = new XQuery(
      "<new>{//*:child3}</new>",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent.namespace/inscope.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new><foo:child3 xmlns:foo=\"http://www.example.com/parent3\"/></new>", false)
    );
  }

  /**
   *  copy element node with namespace .
   */
  @org.junit.Test
  public void constrInscope6() {
    final XQuery query = new XQuery(
      "<new>{//*:child4}</new>",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent.namespace/inscope.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new><child4 xmlns=\"http://www.example.com/parent4\"/></new>", false)
    );
  }

  /**
   *  copy element node with namespace .
   */
  @org.junit.Test
  public void constrInscope7() {
    final XQuery query = new XQuery(
      "for $x in <parent3 xmlns:foo=\"http://www.example.com/parent3\"><foo:child3/></parent3> return <new>{$x//*:child3}</new>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new><foo:child3 xmlns:foo=\"http://www.example.com/parent3\"/></new>", false)
    );
  }

  /**
   *  copy element node with namespace .
   */
  @org.junit.Test
  public void constrInscope8() {
    final XQuery query = new XQuery(
      "for $x in <parent4 xmlns=\"http://www.example.com/parent4\"><child4/></parent4> return <new>{$x//*:child4}</new>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new><child4 xmlns=\"http://www.example.com/parent4\"/></new>", false)
    );
  }

  /**
   *  copy element node with namespace .
   */
  @org.junit.Test
  public void constrInscope9() {
    final XQuery query = new XQuery(
      "<new>{//*:child1}</new>",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent.namespace/inscope.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<new><child1 xmlns:foo=\"http://www.example.com/parent1\" attr=\"child\"/></new>", false)
    );
  }

  /**
   *  Namespace Declarations - Use undeclared namespace prefix .
   */
  @org.junit.Test
  public void constrNamespace1() {
    final XQuery query = new XQuery(
      "<foo:elem/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0081")
    );
  }

  /**
   *  Namespace Declarations - overriding prolog namespace prefix .
   */
  @org.junit.Test
  public void constrNamespace10() {
    final XQuery query = new XQuery(
      "declare namespace foo=\"http://www.example.com/prolog\"; <foo:elem xmlns:foo=\"http://www.example.com/element\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<foo:elem xmlns:foo=\"http://www.example.com/element\"/>", false)
    );
  }

  /**
   *  Namespace Declarations - select child of element overriding parent namespace prefix .
   */
  @org.junit.Test
  public void constrNamespace11() {
    final XQuery query = new XQuery(
      "(<elem xmlns:foo=\"http://www.example.com/parent\"><foo:child xmlns:foo=\"http://www.example.com/child\"><foo:grand-child/></foo:child></elem>)//*:grand-child",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<foo:grand-child xmlns:foo=\"http://www.example.com/child\"/>", false)
    );
  }

  /**
   *  Namespace Declarations - select child of element overriding prolog namespace prefix .
   */
  @org.junit.Test
  public void constrNamespace12() {
    final XQuery query = new XQuery(
      "declare namespace foo=\"http://www.example.com/prolog\"; (<elem xmlns:foo=\"http://www.example.com/parent\"><foo:child/></elem>)//*:child",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<foo:child xmlns:foo=\"http://www.example.com/parent\"/>", false)
    );
  }

  /**
   *  Namespace Declarations - undeclare namespace prefix .
   */
  @org.junit.Test
  public void constrNamespace13() {
    final XQuery query = new XQuery(
      "<foo:elem xmlns:foo=\"http://www.example.com/parent\"><child xmlns:foo=\"\"/></foo:elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<foo:elem xmlns:foo=\"http://www.example.com/parent\"><child/></foo:elem>", false)
      ||
        error("XQST0085")
      )
    );
  }

  /**
   *  Namespace Declarations - use undeclared parent prefix in child .
   */
  @org.junit.Test
  public void constrNamespace14() {
    final XQuery query = new XQuery(
      "<foo:elem xmlns:foo=\"http://www.example.com/parent\"><child xmlns:foo=\"\"><foo:grand-child/></child></foo:elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XPST0081")
      ||
        error("XQST0085")
      )
    );
  }

  /**
   *  Namespace Declarations - use undeclared prolog prefix in child .
   */
  @org.junit.Test
  public void constrNamespace15() {
    final XQuery query = new XQuery(
      "declare namespace foo=\"http://www.example.com/prolog\"; <elem xmlns:foo=\"\"><foo:child/></elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XPST0081")
      ||
        error("XQST0085")
      )
    );
  }

  /**
   *  Namespace Declarations - select child of element overriding namespace prefix .
   */
  @org.junit.Test
  public void constrNamespace16() {
    final XQuery query = new XQuery(
      "(<foo:elem xmlns:foo=\"http://www.example.com/parent\"><child xmlns:foo=\"\"><grand-child/></child></foo:elem>)//grand-child",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<grand-child/>", false)
      ||
        error("XQST0085")
      )
    );
  }

  /**
   *  Namespace Declarations - child inherits default namespace .
   */
  @org.junit.Test
  public void constrNamespace17() {
    final XQuery query = new XQuery(
      "(<elem xmlns=\"http://www.example.com\"><child/></elem>)/*:child",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<child xmlns=\"http://www.example.com\"/>", false)
    );
  }

  /**
   *  Namespace Declarations - override parents default namespace .
   */
  @org.junit.Test
  public void constrNamespace18() {
    final XQuery query = new XQuery(
      "<elem xmlns=\"http://www.example.com/parent\"><child xmlns=\"http://www.example.com/child\"/></elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns=\"http://www.example.com/parent\"><child xmlns=\"http://www.example.com/child\"/></elem>", false)
    );
  }

  /**
   *  Namespace Declarations - override prologs default namespace .
   */
  @org.junit.Test
  public void constrNamespace19() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/prolog\"; <elem xmlns=\"http://www.example.com/element\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns=\"http://www.example.com/element\"/>", false)
    );
  }

  /**
   *  Namespace Declarations - declare namespace with prefix .
   */
  @org.junit.Test
  public void constrNamespace2() {
    final XQuery query = new XQuery(
      "<elem xmlns:foo=\"http://www.example.com\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns:foo=\"http://www.example.com\"/>", false)
    );
  }

  /**
   *  Namespace Declarations - child inherits overridden parents default namespace .
   */
  @org.junit.Test
  public void constrNamespace20() {
    final XQuery query = new XQuery(
      "(<elem xmlns=\"http://www.example.com/parent\"><child xmlns=\"http://www.example.com/child\"><grand-child/></child></elem>)//*:grand-child",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<grand-child xmlns=\"http://www.example.com/child\"/>", false)
    );
  }

  /**
   *  Namespace Declarations - child inherits overridden prologs default namespace .
   */
  @org.junit.Test
  public void constrNamespace21() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/prolog\"; (<elem xmlns=\"http://www.example.com/element\"><child/></elem>)/*:child",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<child xmlns=\"http://www.example.com/element\"/>", false)
    );
  }

  /**
   *  Namespace Declarations - undeclare default namespace .
   */
  @org.junit.Test
  public void constrNamespace22() {
    final XQuery query = new XQuery(
      "<elem xmlns=\"http://www.example.com/parent\"><child xmlns=\"\"/></elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns=\"http://www.example.com/parent\"><child xmlns=\"\"/></elem>", false)
    );
  }

  /**
   *  Namespace Declarations - child inherits undeclared default namespace .
   */
  @org.junit.Test
  public void constrNamespace23() {
    final XQuery query = new XQuery(
      "(<elem xmlns=\"http://www.example.com/parent\"><child xmlns=\"\"><grand-child/></child></elem>)//*:grand-child",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<grand-child/>", false)
    );
  }

  /**
   *  Namespace Declarations - namespace URI is not string literal .
   */
  @org.junit.Test
  public void constrNamespace24() {
    final XQuery query = new XQuery(
      "<elem xmlns=\"{'http://www.example.com'}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  Namespace Declarations - namespace URI is not string literal .
   */
  @org.junit.Test
  public void constrNamespace25() {
    final XQuery query = new XQuery(
      "<elem xmlns=\"http://www.example.com{'/namespace'}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  Namespace Declarations - test namespace added to statically known namespaces: element content .
   */
  @org.junit.Test
  public void constrNamespace26() {
    final XQuery query = new XQuery(
      "<elem xmlns:cm=\"http://www.example.com\">{count(//cm:b)}</elem>",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent.namespace/MixNS.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns:cm=\"http://www.example.com\">1</elem>", false)
    );
  }

  /**
   *  Namespace Declarations - test namespace added to statically known namespaces: attribute content .
   */
  @org.junit.Test
  public void constrNamespace27() {
    final XQuery query = new XQuery(
      "<elem xmlns:cm=\"http://www.example.com\" attr=\"{count(//cm:b)}\"/>",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent.namespace/MixNS.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns:cm=\"http://www.example.com\" attr=\"1\"/>", false)
    );
  }

  /**
   *  Namespace Declarations - test namespace removed from statically known namespaces after end of element .
   */
  @org.junit.Test
  public void constrNamespace29() {
    final XQuery query = new XQuery(
      "<parent><child xmlns:cm=\"http://www.example.com\"/><child>{count(//cm:b)}</child></parent>",
      ctx);
    try {
      query.context(node(file("prod/DirElemContent.namespace/MixNS.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0081")
    );
  }

  /**
   *  Namespace Declarations - declare default namespace .
   */
  @org.junit.Test
  public void constrNamespace3() {
    final XQuery query = new XQuery(
      "<elem xmlns=\"http://www.example.com\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns=\"http://www.example.com\"/>", false)
    );
  }

  /**
   *  Namespace Declarations - duplicate namespace prefix .
   */
  @org.junit.Test
  public void constrNamespace4() {
    final XQuery query = new XQuery(
      "<elem xmlns:foo=\"http://www.example.com\" xmlns:foo=\"http://www.example.com\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0071")
    );
  }

  /**
   *  Namespace Declarations - duplicate default namespace .
   */
  @org.junit.Test
  public void constrNamespace5() {
    final XQuery query = new XQuery(
      "<elem xmlns=\"http://www.example.com\" xmlns=\"http://www.example.com\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0071")
    );
  }

  /**
   *  Namespace Declarations - local part of declaration is xml .
   */
  @org.junit.Test
  public void constrNamespace6() {
    final XQuery query = new XQuery(
      "<elem xmlns:xml=\"http://www.example.com\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0070")
    );
  }

  /**
   *  Namespace Declarations - local part of declaration is xmlns .
   */
  @org.junit.Test
  public void constrNamespace7() {
    final XQuery query = new XQuery(
      "<elem xmlns:xmlns=\"http://www.example.com\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0070")
    );
  }

  /**
   *  Namespace Declarations - child inherits namespace prefix .
   */
  @org.junit.Test
  public void constrNamespace8() {
    final XQuery query = new XQuery(
      "(<elem xmlns:foo=\"http://www.example.com\"><foo:child/></elem>)/*:child",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<foo:child xmlns:foo=\"http://www.example.com\"/>", false)
    );
  }

  /**
   *  Namespace Declarations - overriding parents namespace prefix .
   */
  @org.junit.Test
  public void constrNamespace9() {
    final XQuery query = new XQuery(
      "<elem xmlns:foo=\"http://www.example.com/parent\"><foo:child xmlns:foo=\"http://www.example.com/child\"/></elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<elem xmlns:foo=\"http://www.example.com/parent\"><foo:child xmlns:foo=\"http://www.example.com/child\"/></elem>", false)
    );
  }

  /**
   *  Use the ""-escape mechanism. Modified by MHK to avoid generating output that won't canonicalize - bug 6868 .
   */
  @org.junit.Test
  public void directConElemNamespace3() {
    final XQuery query = new XQuery(
      "namespace-uri(<p:e xmlns:p=\"http://ns.example.com/ns?val=\"\"\"\"\"\"asd\"/>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "http://ns.example.com/ns?val=\"\"\"asd")
      ||
        error("XQST0046")
      ||
        error("XQST0022")
      )
    );
  }

  /**
   *  Use the ""-escape mechanism. Modified by MHK to avoid generating output that won't canonicalize - bug 6868 .
   */
  @org.junit.Test
  public void directConElemNamespace4() {
    final XQuery query = new XQuery(
      "namespace-uri(<p:e xmlns:p=\"http://ns.example.com/ns?val=\"\"asd\"/>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "http://ns.example.com/ns?val=\"asd")
      ||
        error("XQST0046")
      ||
        error("XQST0022")
      )
    );
  }

  /**
   *  Use the ''-escape mechanism. Modified by MHK to avoid generating output that won't canonicalize - bug 6868 .
   */
  @org.junit.Test
  public void directConElemNamespace5() {
    final XQuery query = new XQuery(
      "namespace-uri(<p:e xmlns:p='http://ns.example.com/ns?val=''''''asd'/>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://ns.example.com/ns?val='''asd")
    );
  }

  /**
   *  Use the ''-escape mechanism. Modified by MHK to avoid generating output that won't canonicalize - bug 6868 .
   */
  @org.junit.Test
  public void directConElemNamespace6() {
    final XQuery query = new XQuery(
      "namespace-uri(<e xmlns='http://ns.example.com/ns?val=''asd'/>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "http://ns.example.com/ns?val='asd")
    );
  }

  /**
   *  Ensure the in-scope prefixes are correct for all top-level elements. .
   */
  @org.junit.Test
  public void k2ConInScopeNamespace1() {
    final XQuery query = new XQuery(
      "let $i := document{<e1/>, <e2/>, <e3/>, <e4/>} return (in-scope-prefixes($i/e1), in-scope-prefixes($i/e2), in-scope-prefixes($i/e3), in-scope-prefixes($i/e4))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "xml xml xml xml")
    );
  }

  /**
   *  A namespace declaration inside a direct element constructor is not in-scope for the next operand of the comma operator. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace1() {
    final XQuery query = new XQuery(
      "<name xmlns:ns=\"http://example.com/NS\"/>, ns:nametest",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0081")
    );
  }

  /**
   *  Default namespace declarations does not affect the default function namespace(#4). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace10() {
    final XQuery query = new XQuery(
      "declare default function namespace \"http://example.com\"; <e xmlns:p=\"http://www.w3.org/2001/XMLSchema\" a=\"{p:count()}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  A name test whose namespace is declared with a default namespace attribute. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace11() {
    final XQuery query = new XQuery(
      "declare default function namespace \"http://example.com\"; <e xmlns=\"http://www.w3.org/2001/XMLSchema\" a=\"{nametest}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  A name test whose namespace is declared with a default namespace attribute(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace12() {
    final XQuery query = new XQuery(
      "declare default function namespace \"http://example.com\"; <e a=\"{nametest}\" xmlns=\"http://www.w3.org/2001/XMLSchema\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  A name test whose namespace is declared with a prefixed namespace attribute. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace13() {
    final XQuery query = new XQuery(
      "declare default function namespace \"http://example.com\"; <e xmlns:p=\"http://www.w3.org/2001/XMLSchema\" p:p=\"{p:nametest}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  A name test whose namespace is declared with a prefixed namespace attribute(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace14() {
    final XQuery query = new XQuery(
      "declare default function namespace \"http://example.com\"; <e p:p=\"{p:nametest}\" xmlns:p=\"http://www.w3.org/2001/XMLSchema\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPDY0002")
    );
  }

  /**
   *  A namespace must be a literal. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace15() {
    final XQuery query = new XQuery(
      "<e xmlns=\"content{{ {'1'}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  A namespace must be a literal(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace16() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"content{{ {'1'}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  A namespace must be a literal, for which bracket-escapes are invalid. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace17() {
    final XQuery query = new XQuery(
      "<e xmlns=\"content{()}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  A namespace must be a literal, for which bracket-escapes are invalid(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace18() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"content{()}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  Ensure that the correct default element namespace is used, with 'instance of'. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace19() {
    final XQuery query = new XQuery(
      "<e a=\"{1 instance of integer}\" xmlns=\"http://www.w3.org/2001/XMLSchema\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns=\"http://www.w3.org/2001/XMLSchema\" a=\"true\"/>", false)
    );
  }

  /**
   *  Element constructors aren't well-formed despite the namespace declarations. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace2() {
    final XQuery query = new XQuery(
      "<a:elem xmlns:a=\"http://example.com/NS\" xmlns:b=\"http://example.com/NS\"></b:elem>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0118")
    );
  }

  /**
   *  Ensure that the correct default element namespace is used, with 'treat as'. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace20() {
    final XQuery query = new XQuery(
      "<e a=\"{1 treat as integer}\" xmlns=\"http://www.w3.org/2001/XMLSchema\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns=\"http://www.w3.org/2001/XMLSchema\" a=\"1\"/>", false)
    );
  }

  /**
   *  Ensure that the correct default element namespace is used, with 'cast as'. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace21() {
    final XQuery query = new XQuery(
      "<e a=\"{1 cast as string}\" xmlns=\"http://www.w3.org/2001/XMLSchema\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns=\"http://www.w3.org/2001/XMLSchema\" a=\"1\"/>", false)
    );
  }

  /**
   *  Ensure that the correct default element namespace is used, with 'cast as'. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace22() {
    final XQuery query = new XQuery(
      "<e a=\"{1 castable as string}\" xmlns=\"http://www.w3.org/2001/XMLSchema\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns=\"http://www.w3.org/2001/XMLSchema\" a=\"true\"/>", false)
    );
  }

  /**
   *  Ensure that the correct default element namespace is used, with an element name test. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace23() {
    final XQuery query = new XQuery(
      "<e a=\"{<e><b>data</b></e>/b}\" xmlns=\"http://www.example.com/\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns=\"http://www.example.com/\" a=\"data\"/>", false)
    );
  }

  /**
   *  Ensure that the correct default element namespace is used, with an attribute name test. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace24() {
    final XQuery query = new XQuery(
      "<e a=\"{<e b=\"data\"/>/@b}\" xmlns=\"http://www.example.com/\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns=\"http://www.example.com/\" a=\"data\"/>", false)
    );
  }

  /**
   *  Test that an empty namespace declaration is handled correctly. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace25() {
    final XQuery query = new XQuery(
      "declare namespace b = \"http://www.example.com/\"; empty(<e xmlns=\"http://www.example.com/\"><d xmlns=\"\"><b/></d></e>/b:d/b:b)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(true)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Ensure that a declaration is only in-scope for the element constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace26() {
    final XQuery query = new XQuery(
      "<e xmlns:d=\"http://www.example.com/\"/>, d:d",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0081")
    );
  }

  /**
   *  Ensure that when one namespace declaration goes out of scope, that the one above doesn't dissapear. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace27() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"http://www.w3.org/2001/XMLSchema\"> <b xmlns:p=\"http://www.w3.org/2001/XMLSchema\"/> {p:integer(1)} </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns:p=\"http://www.w3.org/2001/XMLSchema\"><b/>1</e>", false)
    );
  }

  /**
   *  Ensure that when one namespace declaration goes out of scope, that the one above persists. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace28() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"http://www.w3.org/2005/xpath-functions\"> <b xmlns:p=\"http://www.w3.org/2001/XMLSchema\"/> {fn:count(0)} </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns:p=\"http://www.w3.org/2005/xpath-functions\"><b xmlns:p=\"http://www.w3.org/2001/XMLSchema\"/>1</e>", false)
    );
  }

  /**
   *  Ensure that the correct namespace is used, with 'instance of'. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace29() {
    final XQuery query = new XQuery(
      "<e a=\"{1 instance of p:integer}\" xmlns:p=\"http://www.w3.org/2001/XMLSchema\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns:p=\"http://www.w3.org/2001/XMLSchema\" a=\"true\"/>", false)
    );
  }

  /**
   *  Element constructors aren't well-formed despite the namespace declarations. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace3() {
    final XQuery query = new XQuery(
      "<a:elem xmlns:a=\"http://example.com/NS\" xmlns:b=\"http://example.com/NS\"></b:elem> declare default element namespace \"http://example.com/NS\";",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0118")
    );
  }

  /**
   *  Ensure that the correct namespace is used, with 'treat as'. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace30() {
    final XQuery query = new XQuery(
      "<e a=\"{1 treat as p:integer}\" xmlns:p=\"http://www.w3.org/2001/XMLSchema\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns:p=\"http://www.w3.org/2001/XMLSchema\" a=\"1\"/>", false)
    );
  }

  /**
   *  Ensure that the correct namespace is used, with 'cast as'. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace31() {
    final XQuery query = new XQuery(
      "<e a=\"{1 cast as p:string}\" xmlns:p=\"http://www.w3.org/2001/XMLSchema\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns:p=\"http://www.w3.org/2001/XMLSchema\" a=\"1\"/>", false)
    );
  }

  /**
   *  Ensure that the correct namespace is used, with 'cast as'. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace32() {
    final XQuery query = new XQuery(
      "<e a=\"{1 castable as p:string}\" xmlns:p=\"http://www.w3.org/2001/XMLSchema\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns:p=\"http://www.w3.org/2001/XMLSchema\" a=\"true\"/>", false)
    );
  }

  /**
   *  Use a namespace binding in range variables that appears after them in query order. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace33() {
    final XQuery query = new XQuery(
      "<a attr=\"{let $p:name := 3 return $p:name}\" xmlns:p=\"http://www.example.com/\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a xmlns:p=\"http://www.example.com/\" attr=\"3\"/>", false)
    );
  }

  /**
   *  Use an enclosed expression inside an namespace declaration. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace34() {
    final XQuery query = new XQuery(
      "<e xmlns=\"http://{exa}mple.com/\"/>/@xmlns",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  Use an enclosed expression inside an namespace declaration(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace35() {
    final XQuery query = new XQuery(
      "<e xmlns=\"{1}\"/>/@xmlns",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  Use an enclosed expression inside an namespace declaration(#3). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace36() {
    final XQuery query = new XQuery(
      "<e xmlns=\"{xs:anyURI(\"http://www.example.com/\")}\"/>/@xmlns",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  Use an enclosed expression inside an namespace declaration(#3). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace37() {
    final XQuery query = new XQuery(
      "<e xmlns=\"{xs:untypedAtomic(\"http://www.example.com/\")}\"/>/@xmlns",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  Use an inproperly enclosed expression inside an namespace declaration. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace38() {
    final XQuery query = new XQuery(
      "<e xmlns=\"/www.example.com/}\"/>/@xmlns",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Combine an empty default namespace, with a full namespace. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace39() {
    final XQuery query = new XQuery(
      "declare namespace p = \"http://example.com/\"; <p:e xmlns=\"\"/>, count(in-scope-prefixes(<p:e xmlns=\"\"/>))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<p:e xmlns:p=\"http://example.com/\"/>2", false)
    );
  }

  /**
   *  A namespace declaration binding to a a one-letter NCName. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace4() {
    final XQuery query = new XQuery(
      "declare namespace p = \"http://example.com/QuiteWeirdNamespace\"; empty(p:e[1])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertBoolean(true)
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   *  Ensure the namespaces in-scope are outputted. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace40() {
    final XQuery query = new XQuery(
      "declare namespace p = \"http://www.example.com/A\"; <e xmlns=\"http://www.example.com/A\" xmlns:A=\"http://www.example.com/C\"> <b xmlns:B=\"http://www.example.com/C\"/> </e>/p:b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<b xmlns:B=\"http://www.example.com/C\" xmlns:A=\"http://www.example.com/C\" xmlns=\"http://www.example.com/A\"/>", false)
    );
  }

  /**
   *  Ensure all namespaces in-scope, through a couple of elements, are found by in-scope-prefixes(). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace41() {
    final XQuery query = new XQuery(
      "declare namespace p = \"http://www.example.com/A\"; \"START\", for $i in in-scope-prefixes(<e xmlns=\"http://www.example.com/A\" xmlns:A=\"http://www.example.com/C\"> <b xmlns:B=\"http://www.example.com/C\" /> </e>/p:b) order by $i return $i, \"END\"",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "START  A B xml END")
    );
  }

  /**
   *  A direct element constructor with a redundant, prefixed namespace declaration. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace42() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"http://www.example.com/\"> <e xmlns:p=\"http://www.example.com/\"/> </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns:p=\"http://www.example.com/\"><e/></e>", false)
    );
  }

  /**
   *  A direct element constructor with a redundant namespace declaration. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace43() {
    final XQuery query = new XQuery(
      "<e xmlns=\"http://www.example.com/\"> <e xmlns=\"http://www.example.com/\"/> </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns=\"http://www.example.com/\"><e/></e>", false)
    );
  }

  /**
   *  Use an inproperly enclosed expression inside an namespace declaration(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace44() {
    final XQuery query = new XQuery(
      "<e xmlns=\"/www.example.com/{\"/>/@xmlns",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XPST0003")
      ||
        error("XQST0022")
      )
    );
  }

  /**
   *  Ensure the namespace declaration is output for a copied attribute. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace45() {
    final XQuery query = new XQuery(
      "let $i := <e xmlns:p=\"http://example.com\" p:anAttribute=\"attrValue\"/> return <a>{$i/@*}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a xmlns:p=\"http://example.com\" p:anAttribute=\"attrValue\"/>", false)
    );
  }

  /**
   *  Ensure no declaration is output for the XML namespace. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace46() {
    final XQuery query = new XQuery(
      "let $i := <e xml:space=\"preserve\"/> return <a>{$i/@*}</a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a xml:space=\"preserve\"/>", false)
    );
  }

  /**
   *  Use an empty-string namespace declaration. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace47() {
    final XQuery query = new XQuery(
      "<e xmlns=\"\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e/>", false)
    );
  }

  /**
   *  Declare a prefix that isn't used. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace48() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"http://example.com\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns:p=\"http://example.com\"/>", false)
    );
  }

  /**
   *  Use a 'declare default element namespace' in addition to binding to the empty prefix. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace49() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://www.example.com/A\"; <anElement xmlns=\"http://www.example.com/B\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<anElement xmlns=\"http://www.example.com/B\"/>", false)
    );
  }

  /**
   *  Verify the 'xml' prefix is in scope in a direct constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace5() {
    final XQuery query = new XQuery(
      "count(in-scope-prefixes(<e/>)[. eq \"xml\"])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Ensure undeclarations affect path expressions correctly. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace50() {
    final XQuery query = new XQuery(
      "<e xmlns=\"http://www.example.com/A\" xmlns:A=\"http://www.example.com/C\"> <b xmlns:B=\"http://www.example.com/C\" xmlns=\"\"/> </e>/b",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<b xmlns:B=\"http://www.example.com/C\" xmlns:A=\"http://www.example.com/C\"/>", false)
      ||
        assertSerialization("<b xmlns:A=\"http://www.example.com/C\" xmlns:B=\"http://www.example.com/C\"/>", false)
      )
    );
  }

  /**
   *  Use an empty-string namespace declaration(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace51() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://example.com\"; <e xmlns=\"\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e/>", false)
    );
  }

  /**
   *  Use an empty-string namespace declaration(#3). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace52() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://example.com/\"; <a> <e xmlns=\"\"/> </a>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<a xmlns=\"http://example.com/\"><e xmlns=\"\"/></a>", false)
    );
  }

  /**
   *  Ensure a namespace undeclaration aren't counted as a namespace binding. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace53() {
    final XQuery query = new XQuery(
      "count(in-scope-prefixes(<a xmlns=\"http://example.com/\"> <e xmlns=\"\"/> </a>/e))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  Ensure an undeclaration is treated properly. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace54() {
    final XQuery query = new XQuery(
      "count(<e xmlns=\"http://example.com/\"><a xmlns=\"\"/></e>/namespace-uri(exactly-one(*)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  A combination of computed and direct constructors with namespace undeclarations. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace55() {
    final XQuery query = new XQuery(
      "<e xmlns=\"http://example.com/\"> <b xmlns=\"\"> { attribute {QName(\"http://example.com/2\", \"p:attr\")} {()} } </b> </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e xmlns=\"http://example.com/\"><b xmlns=\"\" xmlns:p=\"http://example.com/2\" p:attr=\"\"/></e>", false)
    );
  }

  /**
   *  Check that an attribute name tests properly interact with namespace declarations. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace56() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://example.com/\"; <r xmlns:p=\"http://example.com/\"> { <e p:att=\"\"/>/(@att, attribute::att) } </r>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<r xmlns=\"http://example.com/\" xmlns:p=\"http://example.com/\"/>", false)
    );
  }

  /**
   *  Attempt an attribute node copy that never will happen. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace57() {
    final XQuery query = new XQuery(
      "declare default element namespace \"http://example.com/\"; declare namespace p = \"http://example.com/\"; <r> { <e p:att=\"\"/>/(@att) } </r>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<r xmlns=\"http://example.com/\"/>", false)
    );
  }

  /**
   *  Use the pre-declared prefixes in element constructors. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace58() {
    final XQuery query = new XQuery(
      "<r> <xs:element/> <local:element/> <fn:element/> <xml:element/> </r>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<r><xs:element xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/><local:element xmlns:local=\"http://www.w3.org/2005/xquery-local-functions\"/><fn:element xmlns:fn=\"http://www.w3.org/2005/xpath-functions\"/><xml:element/></r>", false)
    );
  }

  /**
   *  Use the {}-escape mechanism. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace59() {
    final XQuery query = new XQuery(
      "namespace-uri(<p:e xmlns:p=\"http://example.com/{{{{{{}}}}}}asd\"/>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "http://example.com/{{{}}}asd")
      ||
        error("XQST0046")
      ||
        error("XQST0022")
      )
    );
  }

  /**
   *  Verify the 'xml' prefix is in scope in a computed constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace6() {
    final XQuery query = new XQuery(
      "count(in-scope-prefixes(element e{})[. eq \"xml\"])",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("1")
    );
  }

  /**
   *  An non-matched { inside a namespace constructor. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace61() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"{\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XPST0003")
      ||
        error("XQST0022")
      )
    );
  }

  /**
   *  An non-matched } inside a namespace constructor(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace62() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  An non-matched { inside a namespace constructor(#3). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace63() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"content{\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XPST0003")
      ||
        error("XQST0022")
      )
    );
  }

  /**
   *  An non-matched } inside a namespace constructor(#4). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace64() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"content}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Extract an escaped namespace with fn:namespace-for-prefix(). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace65() {
    final XQuery query = new XQuery(
      "namespace-uri-for-prefix(\"p\", <e xmlns:p=\"http://example.com/{{}}{{{{}}}}\"/>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "http://example.com/{}{{}}")
      ||
        error("XQST0046")
      ||
        error("XQST0022")
      )
    );
  }

  /**
   *  An non-matched { inside a namespace constructor with content afterwards. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace67() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"{content\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XPST0003")
      ||
        error("XQST0022")
      )
    );
  }

  /**
   *  An non-matched } inside a namespace constructor with content afterwards(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace68() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"}content\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  An non-matched { inside a namespace constructor with content afterwards(#3). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace69() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"content{content\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        error("XPST0003")
      ||
        error("XPST0022")
      )
    );
  }

  /**
   *  Default namespace declarations does not affect the default function namespace. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace7() {
    final XQuery query = new XQuery(
      "declare default function namespace \"http://example.com\"; <e a=\"{count()}\" xmlns=\"http://www.w3.org/2001/XMLSchema\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  An non-matched } inside a namespace constructor with content afterwards(#4). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace70() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"content}content\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0003")
    );
  }

  /**
   *  Enclosed expressions cannot be used. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace71() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"{\"http://example.com/\"}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  Enclosed expressions cannot be used(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace72() {
    final XQuery query = new XQuery(
      "<e xmlns=\"{\"http://example.com/\"}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  Enclosed expressions containing the empty sequence cannot be used. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace73() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"{()}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  Enclosed expressions containing the empty sequence cannot be used(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace74() {
    final XQuery query = new XQuery(
      "<e xmlns=\"{()}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0022")
    );
  }

  /**
   *  Serialize a namespace that has braces. Changed by MHK to avoid problems canonicalizing an invalid namespace URI .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace75() {
    final XQuery query = new XQuery(
      "namespace-uri(<e xmlns=\"http://example.com/{{1}}\"/>)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertStringValue(false, "http://example.com/{1}")
      ||
        error("XQST0046")
      ||
        error("XQST0022")
      )
    );
  }

  /**
   *  Serialize a prefixed namespace that has braces. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace76() {
    final XQuery query = new XQuery(
      "<e xmlns:p=\"http://example.com/{{1}}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<e xmlns:p=\"http://example.com/{1}\"/>", false)
      ||
        error("XQST0046")
      ||
        error("XQST0022")
      )
    );
  }

  /**
   *  Pull out a namespace prefix that shadows another. .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace77() {
    final XQuery query = new XQuery(
      "declare namespace t = \"http://example.com/2\"; <p:a xmlns:p=\"http://example.com/\"> <p:e xmlns:p=\"http://example.com/2\"/> </p:a>//t:e",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<p:e xmlns:p=\"http://example.com/2\"/>", false)
    );
  }

  /**
   *  Ensure that namespaces are expanded in the correct places(positive test). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace78() {
    final XQuery query = new XQuery(
      "let $e := document{(<X1:L xmlns:X1=\"http://example.com/URL1\">1</X1:L>, <X2:L xmlns:X2=\"http://example.com/URL2\">2</X2:L>)} return <outer xmlns:P=\"http://example.com/URL1\"> { let $outer as element(P:L) := $e/element(P:L) return <inner xmlns:P=\"http://example.com/URL2\"> { let $inner as element(P:L) := $e/element(P:L) return ($outer, $inner) } </inner> } </outer>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<outer xmlns:P=\"http://example.com/URL1\"><inner xmlns:P=\"http://example.com/URL2\"><X1:L xmlns:X1=\"http://example.com/URL1\">1</X1:L><X2:L xmlns:X2=\"http://example.com/URL2\">2</X2:L></inner></outer>", false)
    );
  }

  /**
   *  Ensure that namespaces are expanded in the correct places(negative test). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace79() {
    final XQuery query = new XQuery(
      "let $e := document{(<X1:L xmlns:X1=\"http://ns.example.com/URL1\">1</X1:L>, <X2:L xmlns:X2=\"http://ns.example.com/URL2\">2</X2:L>)} return <outer xmlns:P=\"http://ns.example.com/URL1\"> { let $outer as element(P:L) := $e/element(P:L) return <inner xmlns:P=\"http://ns.example.com/URL2\"> { let $inner as element(P:L) := $outer return $inner } </inner> } </outer>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPTY0004")
    );
  }

  /**
   *  Default namespace declarations does not affect the default function namespace(#2). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace8() {
    final XQuery query = new XQuery(
      "declare default function namespace \"http://example.com\"; <e a=\"{p:count()}\" xmlns:p=\"http://www.w3.org/2001/XMLSchema\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }

  /**
   *  Default namespace declarations does not affect the default function namespace(#3). .
   */
  @org.junit.Test
  public void k2DirectConElemNamespace9() {
    final XQuery query = new XQuery(
      "declare default function namespace \"http://example.com\"; <e xmlns=\"http://www.w3.org/2001/XMLSchema\" a=\"{count()}\"/>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0017")
    );
  }
}
