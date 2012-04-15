package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the ModuleImport production.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdModuleImport extends QT3TestSet {

  /**
   *  Module import with empty target namespace. .
   */
  @org.junit.Test
  public void kModuleImport1() {
    final XQuery query = new XQuery(
      "import(::)module \"\"; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0088")
    );
  }

  /**
   *  Module import with empty target namespace, and two location hints. .
   */
  @org.junit.Test
  public void kModuleImport2() {
    final XQuery query = new XQuery(
      "import(::)module \"\" at \"http://example.com/\", \"http://example.com/2\"; 1 eq 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0088")
    );
  }

  /**
   *  ':=' cannot be used to assing namespaces in 'import module'. .
   */
  @org.junit.Test
  public void kModuleImport3() {
    final XQuery query = new XQuery(
      "import module namespace NCName := \"http://example.com/Dummy\"; 1",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Ensure that a start looking like an import, isn't treated as so. .
   */
  @org.junit.Test
  public void k2ModuleImport1() {
    final XQuery query = new XQuery(
      "import ne import",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPDY0002")
    );
  }

  /**
   *  A library module that has a query body. .
   */
  @org.junit.Test
  public void k2ModuleProlog1() {
    final XQuery query = new XQuery(
      "module namespace example = \"http://example.com/\"; \"an expression\"",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0003")
    );
  }

  /**
   *  Test XQST0093 by importing a module with a circular dependency .
   */
  @org.junit.Test
  public void errata8001() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace errata8_1a=\"http://www.w3.org/TestModules/errata8_1a\"; \n" +
      "        errata8_1a:fun()\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/errata8_1a", file("prod/ModuleImport/errata8-module1a.xq"));
    query.addModule("http://www.w3.org/TestModules/errata8_1b", file("prod/ModuleImport/errata8-module1b.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XQST0093")
      ||
        error("XQST0054")
      )
    );
  }

  /**
   *  Test XQST0093 by importing a module with a circular dependency .
   */
  @org.junit.Test
  public void errata8002() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace errata8_2a=\"http://www.w3.org/TestModules/errata8_2a\"; \n" +
      "        errata8_2a:fun()",
      ctx);
    query.addModule("http://www.w3.org/TestModules/errata8_2a", file("prod/ModuleImport/errata8-module2a.xq"));
    query.addModule("http://www.w3.org/TestModules/errata8_2b", file("prod/ModuleImport/errata8-module2b.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0093")
    );
  }

  /**
   *  Test lack of XQST0093 by importing a module without a circular dependency .
   */
  @org.junit.Test
  public void errata8003() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace errata8_3a=\"http://www.w3.org/TestModules/errata8_3a\"; \n" +
      "        errata8_3a:fun()\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/errata8_3a", file("prod/ModuleImport/errata8-module3a.xq"));
    query.addModule("http://www.w3.org/TestModules/errata8_3b", file("prod/ModuleImport/errata8-module3b.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("10")
    );
  }

  /**
   *  leading and trailing whitespace is removed .
   */
  @org.junit.Test
  public void moduleURIs1() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"  http://www.w3.org/TestModules/test \";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test", file("prod/ModuleImport/module-uris1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  resolving dots in uri 4 .
   */
  @org.junit.Test
  public void moduleURIs10() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"http://www.w3.org/..TestModules/test\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/..TestModules/test", file("prod/ModuleImport/module-uris6-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  URI containing unicode characters .
   */
  @org.junit.Test
  public void moduleURIs11() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"http://www.w3.org/TestModules/&#xd0a4;/test\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/키/test", file("prod/ModuleImport/module-uris7-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  URI containing unusual characters .
   */
  @org.junit.Test
  public void moduleURIs12() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"http://www.w3.org/TestModules/&#x3c;&#x3d;&#x3e;&#x40;/test\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/<=>@/test", file("prod/ModuleImport/module-uris8-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  URN as URI .
   */
  @org.junit.Test
  public void moduleURIs13() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"urn:example:animal:ferret:nose\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("urn:example:animal:ferret:nose", file("prod/ModuleImport/module-uris9-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  URI with type param .
   */
  @org.junit.Test
  public void moduleURIs14() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"ftp://www.w3.org/TestModules/test;type=A\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("ftp://www.w3.org/TestModules/test;type=A", file("prod/ModuleImport/module-uris10-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  URI containing query .
   */
  @org.junit.Test
  public void moduleURIs15() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"http://www.w3.org/TestModules/test?hello=world\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test?hello=world", file("prod/ModuleImport/module-uris11-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  URI containing fragment .
   */
  @org.junit.Test
  public void moduleURIs16() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"http://www.w3.org/TestModules/test#world\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test#world", file("prod/ModuleImport/module-uris12-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  URI containing param, query and fragment .
   */
  @org.junit.Test
  public void moduleURIs17() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"ftp://www.w3.org/TestModules/test;type=A?hello=world&amp;q#world\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("ftp://www.w3.org/TestModules/test;type=A?hello=world&q#world", file("prod/ModuleImport/module-uris13-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  URI containing fragment only .
   */
  @org.junit.Test
  public void moduleURIs18() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"#1\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("#1", file("prod/ModuleImport/module-uris14-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  relative URI containing scheme .
   */
  @org.junit.Test
  public void moduleURIs19() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"http:test\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http:test", file("prod/ModuleImport/module-uris15-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  leading and trailing whitespace is removed .
   */
  @org.junit.Test
  public void moduleURIs2() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"&#x20;&#x9;&#xA;&#xD;http://www.w3.org/TestModules/test&#x20;&#x9;&#xA;&#xD;\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test", file("prod/ModuleImport/module-uris1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  URI containing username .
   */
  @org.junit.Test
  public void moduleURIs20() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"ftp://user@www.w3.org/TestModules/test\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("ftp://user@www.w3.org/TestModules/test", file("prod/ModuleImport/module-uris16-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  URI containing IPv6 .
   */
  @org.junit.Test
  public void moduleURIs21() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"http://2001:0db8:85a3:0000:0000:8a2e:0370:7334/TestModules/test\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://2001:0db8:85a3:0000:0000:8a2e:0370:7334/TestModules/test", file("prod/ModuleImport/module-uris17-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  URI containing Port .
   */
  @org.junit.Test
  public void moduleURIs22() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"http://www.w3.org:-7334/TestModules/test\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org:-7334/TestModules/test", file("prod/ModuleImport/module-uris18-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  URI email .
   */
  @org.junit.Test
  public void moduleURIs23() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"mailto:jane.doe@w3c.org\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("mailto:jane.doe@w3c.org", file("prod/ModuleImport/module-uris19-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  URI fax .
   */
  @org.junit.Test
  public void moduleURIs24() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"fax:+1-234-567-890\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("fax:+1-234-567-890", file("prod/ModuleImport/module-uris20-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  URI ldap .
   */
  @org.junit.Test
  public void moduleURIs25() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"ldap://[2001:db8::7]/c=GB?objectClass?one\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("ldap://[2001:db8::7]/c=GB?objectClass?one", file("prod/ModuleImport/module-uris21-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  any other sequence of whitespace characters is replaced by a single space (#x20) character .
   */
  @org.junit.Test
  public void moduleURIs3() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"http://www.w3.org/Test&#x20;&#x20;&#x20;&#x20;Modules/test\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/Test Modules/test", file("prod/ModuleImport/module-uris2-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  do not resolve relative path in the target namespace .
   */
  @org.junit.Test
  public void moduleURIs4() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"http://www.w3.org/TestModules/test/../../TestModules/./test\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test", file("prod/ModuleImport/module-urisi1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0059")
    );
  }

  /**
   *  resolving against base uri .
   */
  @org.junit.Test
  public void moduleURIs5() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare base-uri \"http://www.w3.org/TestModules/test\";\n" +
      "        import module namespace test=\"test\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test", file("prod/ModuleImport/module-uris1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  resolving dots against base uri .
   */
  @org.junit.Test
  public void moduleURIs6() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare base-uri \"http://www.w3.org/TestModules/test/1/2\";\n" +
      "        import module namespace test=\"../../test\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test", file("prod/ModuleImport/module-uris1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  resolving dots in uri 1 .
   */
  @org.junit.Test
  public void moduleURIs7() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"http://www.w3.org/TestModules./test\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules./test", file("prod/ModuleImport/module-uris3-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  resolving dots in uri 2 .
   */
  @org.junit.Test
  public void moduleURIs8() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"http://www.w3.org/.TestModules/test\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/.TestModules/test", file("prod/ModuleImport/module-uris4-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  resolving dots in uri 3 .
   */
  @org.junit.Test
  public void moduleURIs9() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test=\"http://www.w3.org/TestModules../test\";\n" +
      "        <result>{test:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules../test", file("prod/ModuleImport/module-uris5-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  Namespaces from imported modules are not visible. .
   */
  @org.junit.Test
  public void modules1() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/defs\"; \n" +
      "        <foo:anElement>some Content</foo:anElement>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/defs", file("prod/ModuleImport/moduleDefs-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }

  /**
   *  Evaluation of import module feature that applies the lower case function to a value from an imported module. .
   */
  @org.junit.Test
  public void modules10() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        fn:lower-case(test1:ok())\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ok")
    );
  }

  /**
   *  Evaluation of import module feature that applies the string-length function to a value from an imported module. .
   */
  @org.junit.Test
  public void modules11() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        fn:string-length(test1:ok())",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("2")
    );
  }

  /**
   *  Evaluation of import module feature that performs and addition operation to a value from an imported module. .
   */
  @org.junit.Test
  public void modules12() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\";\n" +
      "        $test1:flag + $test1:flag\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("2")
    );
  }

  /**
   *  Evaluation of import module feature that performs a subtraction operation to a value from an imported module. .
   */
  @org.junit.Test
  public void modules13() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        $test1:flag - $test1:flag\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("0")
    );
  }

  /**
   *  Evaluation of import module feature that applies the xs:integer function to a value from an imported module. .
   */
  @org.junit.Test
  public void modules14() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        xs:integer($test1:flag)\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("1")
    );
  }

  /**
   *  Evaluation of importing a library module whose namespace uri is set to "". .
   */
  @org.junit.Test
  public void modules15() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test2=\"http://www.w3.org/TestModules/test2\"; \n" +
      "        \"aaa\"\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test2-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0088")
    );
  }

  /**
   *  Evaluation of module import with "xml" prefix. .
   */
  @org.junit.Test
  public void modules16() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace xml=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        xs:string($xml:flag)\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0070")
    );
  }

  /**
   *  Evaluation of module import with variables bound to a namespace URI different from the target namespace of the module. .
   */
  @org.junit.Test
  public void modules17() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/diffns\"; \n" +
      "        \"abc\"\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/diffns", file("prod/ModuleImport/modulesdiffns-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0048")
    );
  }

  /**
   *  Evaluation of module import with two imports, one of which import the other and uses its function. .
   */
  @org.junit.Test
  public void modules18() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace mod1=\"http://www.w3.org/TestModules/module1\"; \n" +
      "        import module namespace mod2=\"http://www.w3.org/TestModules/module2\"; \n" +
      "        mod1:x(),mod2:y()",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module1", file("prod/ModuleImport/module1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/module2", file("prod/ModuleImport/module2-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "x y x y")
    );
  }

  /**
   *  Same Namespace used in imported and importing modules. .
   */
  @org.junit.Test
  public void modules2() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/defs\"; \n" +
      "        declare namespace foo = \"http://example.org\"; \n" +
      "        <foo:anElement>some Content</foo:anElement>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/defs", file("prod/ModuleImport/moduleDefs-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<foo:anElement xmlns:foo=\"http://example.org\">some Content</foo:anElement>", false)
    );
  }

  /**
   *  Cyclic module imports .
   */
  @org.junit.Test
  public void modules28() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs1 = \"http://www.w3.org/TestModules/defs1\"; \n" +
      "        $defs1:var\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/defs1", file("prod/ModuleImport/modules-recursive1.xq"));
    query.addModule("http://www.w3.org/TestModules/defs2", file("prod/ModuleImport/modules-recursive2.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      (
        error("XQST0093")
      ||
        error("XQST0054")
      )
    );
  }

  /**
   *  Evaluation of module import with "xmlns" prefix. .
   */
  @org.junit.Test
  public void modules29() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace xmlns=\"http://www.w3.org/TestModules/test\"; \n" +
      "        xmlns:ok ()\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0070")
    );
  }

  /**
   *  Evaluates that module importing is not transitive. .
   */
  @org.junit.Test
  public void modules3() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/defs\"; \n" +
      "        let $var := $test1:flag + 1 return $var\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/defs", file("prod/ModuleImport/moduleDefs-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0081")
    );
  }

  /**
   *  Evaluates actual usage of a variable declared in an imported module. .
   */
  @org.junit.Test
  public void modules4() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/defs\"; \n" +
      "        let $var := $defs:var1+ 1 return $var\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/defs", file("prod/ModuleImport/moduleDefs-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("2")
    );
  }

  /**
   *  Module that uses a variable from an imported module and modifies a variable. 
   *         The importing module in turn uses that modified value. .
   */
  @org.junit.Test
  public void modules5() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/defs\"; \n" +
      "        let $var := $defs:var2 + 1 return $var\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/defs", file("prod/ModuleImport/moduleDefs-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("4")
    );
  }

  /**
   *  Evaluation of usage of variable from imported module and usage of variable with same name in importing module,
   *          but with different namespace prefix. .
   */
  @org.junit.Test
  public void modules6() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        declare namespace foo = \"http://example.org\"; \n" +
      "        declare variable $foo:flag := 3; \n" +
      "        let $var := $test1:flag + $foo:flag \n" +
      "        return $var",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertEq("4")
    );
  }

  /**
   *  Evaluation of of an importing module that uses both a variable and a function from an importing module. .
   */
  @org.junit.Test
  public void modules7() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        let $var := fn:concat(xs:string($test1:flag),xs:string(test1:ok())) \n" +
      "        return $var",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "1ok")
    );
  }

  /**
   *  Evaluation of usage of same function name from imported/importing module, but different namespaces. .
   */
  @org.junit.Test
  public void modules8() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        declare namespace foo = \"http://example.org\"; \n" +
      "        declare function foo:ok () { \"ok\" }; \n" +
      "        let $var := fn:concat(test1:ok(),foo:ok()) \n" +
      "        return $var",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "okok")
    );
  }

  /**
   *  Evaluation of import module feature that applies the upper case function to a value from an imported module. .
   */
  @org.junit.Test
  public void modules9() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        fn:upper-case(test1:ok())",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "OK")
    );
  }

  /**
   *  Negative test: wrong namespace .
   */
  @org.junit.Test
  public void modulesBadNs() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test2=\"http://www.w3.org/TestModules/test2\";\n" +
      "        <result>{test2:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0059")
    );
  }

  /**
   *  Importing circular modules .
   */
  @org.junit.Test
  public void modulesCircular() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        <result>{test1:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1c1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test2c1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "ok")
    );
  }

  /**
   *  Colliding function declarations .
   */
  @org.junit.Test
  public void modulesCollideFn001() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        <result>{test1:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1collide2-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0034")
    );
  }

  /**
   *  Colliding function declarations .
   */
  @org.junit.Test
  public void modulesCollideFn002() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        declare function test1:ok () { \"ok\" }; \n" +
      "        <result>{test1:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0034")
    );
  }

  /**
   *  Colliding variable declarations .
   */
  @org.junit.Test
  public void modulesCollideVar001() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        <result>{test1:ok()}</result>",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1collide1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0049")
    );
  }

  /**
   *  Colliding variable declarations .
   */
  @org.junit.Test
  public void modulesCollideVar002() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        declare variable $test1:flag := 1; \n" +
      "        <result>{test1:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0049")
    );
  }

  /**
   *  Module with an empty namespace .
   */
  @org.junit.Test
  public void modulesEmptyns() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1= \"\"; \n" +
      "        <result>ok</result>\n" +
      "      ",
      ctx);
    query.addModule("", file("prod/ModuleImport/emptyns-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0088")
    );
  }

  /**
   * Written By: Carmelo Montanez  Test for importing non-existing library module. .
   */
  @org.junit.Test
  public void modulesNone() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace none=\"http://www.w3.org/TestModules/none\" at \"empty-lib.xq\"; \n" +
      "        \"abc\"\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/none", file("prod/ModuleImport/empty-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0059")
    );
  }

  /**
   *  public functions in imported module are visible. .
   */
  @org.junit.Test
  public void modulesPubPriv1() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "        <a>{defs:g(42)}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>65</a>", false)
    );
  }

  /**
   *  main module private function is visible from main module function. .
   */
  @org.junit.Test
  public void modulesPubPriv10() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %private function local:inc($i as xs:integer) {\n" +
      "            $i + 1\n" +
      "        };\n" +
      "        \n" +
      "        declare function local:test() {\n" +
      "            local:inc(1)\n" +
      "        };\n" +
      "        <a>{local:test()}</a>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>2</a>", false)
    );
  }

  /**
   *  main module public variable is visible from main module function. .
   */
  @org.junit.Test
  public void modulesPubPriv11() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %public variable $i := 1;\n" +
      "        \n" +
      "        declare function local:test() {\n" +
      "            $i + 1\n" +
      "        };\n" +
      "        <a>{local:test()}</a>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>2</a>", false)
    );
  }

  /**
   *  main module private variable is visible from main module function. .
   */
  @org.junit.Test
  public void modulesPubPriv12() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %private variable $i := 1;\n" +
      "        \n" +
      "        declare function local:test() {\n" +
      "           $i + 1\n" +
      "        };\n" +
      "        <a>{local:test()}</a>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>2</a>", false)
    );
  }

  /**
   *  library module public function is visible from main module function. .
   */
  @org.junit.Test
  public void modulesPubPriv13() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "        declare function local:test() {\n" +
      "            defs:g(42)\n" +
      "        };\n" +
      "        <a>{local:test()}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>65</a>", false)
    );
  }

  /**
   *  library module private function is not visible from main module function. .
   */
  @org.junit.Test
  public void modulesPubPriv14() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "        declare function local:test() {\n" +
      "            defs:f()\n" +
      "        };\n" +
      "        <a>{local:test()}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  library module public variable is visible from main module function. .
   */
  @org.junit.Test
  public void modulesPubPriv15() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "        declare function local:test() {\n" +
      "            $defs:one\n" +
      "        };\n" +
      "        <a>{local:test()}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>1</a>", false)
    );
  }

  /**
   *  library module private variable is not visible from main module function. .
   */
  @org.junit.Test
  public void modulesPubPriv16() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "        declare function local:test() {\n" +
      "            $defs:two\n" +
      "        };\n" +
      "        <a>{local:test()}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   *  main module public function is visible from main module variable. .
   */
  @org.junit.Test
  public void modulesPubPriv17() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %public function local:inc($i as xs:integer) {\n" +
      "            $i + 1\n" +
      "        };\n" +
      "        \n" +
      "        declare variable $test := local:inc(1);\n" +
      "\n" +
      "        <a>{$test}</a>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>2</a>", false)
    );
  }

  /**
   *  main module private function is visible from main module variable. .
   */
  @org.junit.Test
  public void modulesPubPriv18() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %private function local:inc($i as xs:integer) {\n" +
      "            $i + 1\n" +
      "        };\n" +
      "        \n" +
      "        declare variable $test := local:inc(1);\n" +
      "\n" +
      "        <a>{$test}</a>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>2</a>", false)
    );
  }

  /**
   *  main module public variable is visible from main module variable. .
   */
  @org.junit.Test
  public void modulesPubPriv19() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %public variable $i := 1;\n" +
      "        \n" +
      "\tdeclare variable $test := $i + 1;\n" +
      "\n" +
      "        <a>{$test}</a>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>2</a>", false)
    );
  }

  /**
   *  private functions in imported module are not visible. .
   */
  @org.junit.Test
  public void modulesPubPriv2() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "        defs:f()\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  main module private variable is visible from main module variable. .
   */
  @org.junit.Test
  public void modulesPubPriv20() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %private variable $i := 1;\n" +
      "        \n" +
      "        declare variable $test := $i + 1;\n" +
      "\n" +
      "        <a>{$test}</a>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>2</a>", false)
    );
  }

  /**
   *  library module public function is visible from main module variable. .
   */
  @org.junit.Test
  public void modulesPubPriv21() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "\n" +
      "        declare variable $test := defs:g(42);\n" +
      "\n" +
      "        <a>{$test}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>65</a>", false)
    );
  }

  /**
   *  library module private function is not visible from main module variable. .
   */
  @org.junit.Test
  public void modulesPubPriv22() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "\n" +
      "        declare variable $test := defs:f();\n" +
      "\n" +
      "        <a>{$test}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  library module public variable is visible from main module variable. .
   */
  @org.junit.Test
  public void modulesPubPriv23() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "\n" +
      "        declare variable $test := $defs:one;\n" +
      "\n" +
      "        <a>{$test}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>1</a>", false)
    );
  }

  /**
   *  library module private variable is not visible from main module variable. .
   */
  @org.junit.Test
  public void modulesPubPriv24() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "\n" +
      "        declare variable $test := $defs:two;\n" +
      "\n" +
      "        <a>{local:test()}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   *  library module public function is visible from library module function. .
   */
  @org.junit.Test
  public void modulesPubPriv25() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "\n" +
      "        <a>{defs:h(42)}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>65</a>", false)
    );
  }

  /**
   *  library module public/private functions/variables are visible from library module variable. .
   */
  @org.junit.Test
  public void modulesPubPriv26() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "\n" +
      "        <a>{$defs:ninety}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>90</a>", false)
    );
  }

  /**
   *  library module public function is visible from function in different library module. .
   */
  @org.junit.Test
  public void modulesPubPriv27() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv3\"; \n" +
      "        <a>{defs:f(42)}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv3", file("prod/ModuleImport/module-pub-priv3.xq"));
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>65</a>", false)
    );
  }

  /**
   *  library module private function is visible from function in different library module. .
   */
  @org.junit.Test
  public void modulesPubPriv28() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv2\"; \n" +
      "        <a>{defs:fails()}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv2", file("prod/ModuleImport/module-pub-priv2.xq"));
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   *  It is an error if a function's annotations contain contain a public and private annotation. .
   */
  @org.junit.Test
  public void modulesPubPriv29() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %private %public function local:foo() { () };\n" +
      "        local:foo()\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0106")
    );
  }

  /**
   *  public variables in imported module are visible. .
   */
  @org.junit.Test
  public void modulesPubPriv3() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "        <a>{defs:g($defs:one)}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>24</a>", false)
    );
  }

  /**
   *  It is an error if a function's annotations contain contain a public and private annotation. .
   */
  @org.junit.Test
  public void modulesPubPriv30() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %private %fn:public function local:foo() { () };\n" +
      "        local:foo()\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0106")
    );
  }

  /**
   *  It is an error if a function's annotations contain contain two public annotations. .
   */
  @org.junit.Test
  public void modulesPubPriv31() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %public %public function local:foo() { () };\n" +
      "        local:foo()\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0106")
    );
  }

  /**
   *  It is an error if a function's annotations contain contain two private annotations. .
   */
  @org.junit.Test
  public void modulesPubPriv32() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %private %private function local:foo() { () };\n" +
      "        local:foo()\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0106")
    );
  }

  /**
   *  It is an error if a variable's annotations contain contain a public and private annotation. .
   */
  @org.junit.Test
  public void modulesPubPriv33() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %private %public variable $foo := ();\n" +
      "        $foo\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0116")
    );
  }

  /**
   *  It is an error if a variable's annotations contain contain a public and private annotation. .
   */
  @org.junit.Test
  public void modulesPubPriv34() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %private %fn:public variable $foo := ();\n" +
      "        $foo\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0116")
    );
  }

  /**
   *  It is an error if a variable's annotations contain two private annotations. .
   */
  @org.junit.Test
  public void modulesPubPriv35() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %private %private variable $foo := ();\n" +
      "        $foo\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0116")
    );
  }

  /**
   *  It is an error if a variable's annotations contain two public annotations. .
   */
  @org.junit.Test
  public void modulesPubPriv36() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %public %public variable $foo := ();\n" +
      "        $foo\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0116")
    );
  }

  /**
   * Invokes a private function item.
   */
  @org.junit.Test
  public void modulesPubPriv37() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "        defs:f#0()\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0017")
    );
  }

  /**
   * Invokes a public function item .
   */
  @org.junit.Test
  public void modulesPubPriv38() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "        <a>{defs:g#1(42)}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>65</a>", false)
    );
  }

  /**
   *  private variables in imported module are not visible. .
   */
  @org.junit.Test
  public void modulesPubPriv4() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace defs=\"http://www.w3.org/TestModules/module-pub-priv\"; \n" +
      "        <a>{defs:g($defs:two)}</a>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/module-pub-priv", file("prod/ModuleImport/module-pub-priv.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XPST0008")
    );
  }

  /**
   *  public functions in main module are visible. .
   */
  @org.junit.Test
  public void modulesPubPriv5() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %public function local:inc($i as xs:integer) {\n" +
      "            $i + 1\n" +
      "        };\n" +
      "        <a>{local:inc(1)}</a>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>2</a>", false)
    );
  }

  /**
   *  private functions in main module are visible. .
   */
  @org.junit.Test
  public void modulesPubPriv6() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %private function local:inc($i as xs:integer) {\n" +
      "            $i + 1\n" +
      "        };\n" +
      "        <a>{local:inc(1)}</a>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>2</a>", false)
    );
  }

  /**
   *  public variable in main module is visible. .
   */
  @org.junit.Test
  public void modulesPubPriv7() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %public variable $i := 1;\n" +
      "        <a>{$i+1}</a>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>2</a>", false)
    );
  }

  /**
   *  private variable in main module is visible. .
   */
  @org.junit.Test
  public void modulesPubPriv8() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %private variable $i := 1;\n" +
      "        <a>{$i+1}</a>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>2</a>", false)
    );
  }

  /**
   *  main module public function is visible from main module function. .
   */
  @org.junit.Test
  public void modulesPubPriv9() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare %public function local:inc($i as xs:integer) {\n" +
      "            $i + 1\n" +
      "        };\n" +
      "        \n" +
      "        declare function local:test() {\n" +
      "            local:inc(1)\n" +
      "        };\n" +
      "        <a>{local:test()}</a>\n" +
      "      ",
      ctx);

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<a>2</a>", false)
    );
  }

  /**
   *  Simple module declaration and import .
   */
  @org.junit.Test
  public void modulesSimple() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        <result>{test1:ok()}</result>",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }

  /**
   *  Importing two modules the disapproved way .
   */
  @org.junit.Test
  public void modulesTwoImport() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        import module namespace test2=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        <result>{test1:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      error("XQST0047")
    );
  }

  /**
   *  Importing two modules, the approved way .
   */
  @org.junit.Test
  public void modulesTwoImportOk() {
    final XQuery query = new XQuery(
      "\n" +
      "        import module namespace test1=\"http://www.w3.org/TestModules/test1\"; \n" +
      "        <result>{test1:ok()}</result>\n" +
      "      ",
      ctx);
    query.addModule("http://www.w3.org/TestModules/test1", file("prod/ModuleImport/test1-lib.xq"));
    query.addModule("http://www.w3.org/TestModules/test2", file("prod/ModuleImport/test1-lib.xq"));

    final QT3Result res = result(query);
    result = res;
    test(
      assertSerialization("<result>ok</result>", false)
    );
  }
}
