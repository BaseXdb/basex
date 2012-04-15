package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the SchemaImport production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdSchemaImport extends QT3TestSet {

  /**
.
   */
  @org.junit.Test
  public void qischema266() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare construction preserve; \n" +
      "        declare function local:foo1($e as element()) {\n" +
      "            data($e) instance of xs:untypedAtomic\n" +
      "        }; \n" +
      "        declare function local:foo2($e as element(*,xs:decimal)) {\n" +
      "            data($e) instance of xs:untypedAtomic\n" +
      "        }; \n" +
      "        declare function local:foo3($e as element()) {\n" +
      "            (data($e) + 1) instance of xs:double\n" +
      "        }; \n" +
      "        declare function local:foo4($e as element(*,xs:decimal)) {\n" +
      "            (data($e) + 1) instance of xs:double\n" +
      "        }; \n" +
      "        declare function local:foo5($e as element(*,xs:decimal)) {\n" +
      "            ($e + 1) instance of xs:double\n" +
      "        }; \n" +
      "        declare function local:foo6($e as element(*,xs:decimal)) {\n" +
      "            (data($e) + data($e)) instance of xs:double\n" +
      "        }; \n" +
      "        3\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   *  Evaluation of a prolog with a schema import that should be hard to find .
   */
  @org.junit.Test
  public void schemaImport1() {
    final XQuery query = new XQuery(
      "import schema namespace aSpace=\"http://www.youcannotfindthisschemaorg/schemas\" at \"http://www.youcannotfindithere/noschemas\";\n" +
      "             \"abc\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0059")
    );
  }

  /**
   *  Evaluation of a prolog with two schema import that specifies the same namespace. .
   */
  @org.junit.Test
  public void schemaImport2() {
    final XQuery query = new XQuery(
      "import schema namespace ns1 = \"http://www.w3.org/XQueryTestOrderBy\";\n" +
      "             import schema namespace ns2 = \"http://www.w3.org/XQueryTestOrderBy\";\n" +
      "             \"abc\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0058")
    );
  }

  /**
   *  Evaluation of a prolog with two schema import that specifies no target namespace and specifies a prefix. .
   */
  @org.junit.Test
  public void schemaImport3() {
    final XQuery query = new XQuery(
      "import schema namespace ns1 = \"\";\n" +
      "             \"abc\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0057")
    );
  }
}
