package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the WhereClause production.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdWhereClause extends QT3TestSet {

  /**
   *  A for/where combination where the cardinality of the return statement is crucially affected by the binding sequence. .
   */
  @org.junit.Test
  public void kWhereExpr1() {
    final XQuery query = new XQuery(
      "(for $fo in (1, 2, 3) where $fo eq 3 return $fo)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("3")
    );
  }

  /**
   *  A for/where expression combined with fn:boolean and xs:anyURI. .
   */
  @org.junit.Test
  public void kWhereExpr10() {
    final XQuery query = new XQuery(
      "for $i in (1, 2, current-time())[1] where xs:anyURI(\"example.com/\") return true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A for/where expression combined with fn:boolean and xs:anyURI. .
   */
  @org.junit.Test
  public void kWhereExpr11() {
    final XQuery query = new XQuery(
      "empty(for $i in (1, 2, current-time())[1] where xs:anyURI(\"\") return true())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Get the string value of the return statement of a for expression after being filtered by a where clause. .
   */
  @org.junit.Test
  public void kWhereExpr2() {
    final XQuery query = new XQuery(
      "string(exactly-one((for $fo in (1, 2, 3) where $fo eq 3 return $fo)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3")
    );
  }

  /**
   *  A where clause containing a value which EBV cannot be extracted from. .
   */
  @org.junit.Test
  public void kWhereExpr3() {
    final XQuery query = new XQuery(
      "count((for $fo in (1, 2, 3) where xs:time(\"08:08:23Z\") return $fo)) eq 3",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  A for/where expression combined with fn:count(). .
   */
  @org.junit.Test
  public void kWhereExpr4() {
    final XQuery query = new XQuery(
      "for $i in 1 where count(($i, 2, timezone-from-time(current-time()))) return true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A for expression binding to one single value, combined with a positive where clause. .
   */
  @org.junit.Test
  public void kWhereExpr5() {
    final XQuery query = new XQuery(
      "empty(for $i in 1 where false() return $i)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A for expression binding to one single value, combined with a negative where clause. .
   */
  @org.junit.Test
  public void kWhereExpr6() {
    final XQuery query = new XQuery(
      "(for $i in 1 where true() return $i) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A for expression binding to one single value, combined with a positive where clause. .
   */
  @org.junit.Test
  public void kWhereExpr7() {
    final XQuery query = new XQuery(
      "empty(for $i in 1 where (1, current-time())[1] treat as xs:integer eq 0 return $i)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A for expression binding to one single value, combined with a negative where clause. .
   */
  @org.junit.Test
  public void kWhereExpr8() {
    final XQuery query = new XQuery(
      "(for $i in 1 where (1, current-time())[1] treat as xs:integer eq 1 return $i) eq 1",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  A for/where expression combined with fn:boolean. .
   */
  @org.junit.Test
  public void kWhereExpr9() {
    final XQuery query = new XQuery(
      "for $i in (1, 2, current-time())[1] where fn:boolean($i treat as xs:integer) return true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Multiple where clauses is not allowed in XQuery 1.0. .
   */
  @org.junit.Test
  public void k2WhereExpr1() {
    final XQuery query = new XQuery(
      "for $a in 1 where true() where true() return $a",
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
   *  Multiple where clauses are allowed in XQuery 3.0. .
   */
  @org.junit.Test
  public void k2WhereExpr1b() {
    final XQuery query = new XQuery(
      "for $a in 1 where true() where true() return $a",
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
   *  Multiple where clauses is not allowed(#2). .
   */
  @org.junit.Test
  public void k2WhereExpr2() {
    final XQuery query = new XQuery(
      "for $a in 1 where true(), true() return $a",
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
   *  For+Where+Return - test existence of child elements in Where clause .
   */
  @org.junit.Test
  public void whereExpr001() {
    final XQuery query = new XQuery(
      "if ( count( for $f in //* where $f/File return $f/File[1] ) = count( //File[1]) ) then <Same/> else <notSame/>",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<Same />", false)
    );
  }

  /**
   *  For+Where+Return - error, variable in 'Where' Expr hasn't been defined .
   */
  @org.junit.Test
  public void whereExpr002() {
    final XQuery query = new XQuery(
      "for $folder in //Folder where $folde/@name = \"ABC\" return <folder/>",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  For+Where+Return - using same predicate (as FLWOR003) in 'Where' predicate .
   */
  @org.junit.Test
  public void whereExpr003() {
    final XQuery query = new XQuery(
      "for $d in /MyComputer/Drive4 where $d/Folder[@id=\"128\"] return <FolderName>{$d/Folder/@name}</FolderName>",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<FolderName name=\"Folder00000000047\" />", false)
    );
  }

  /**
   *  For+Where+Return - filters in 'Where' expr and in 'In' expr using predicate .
   */
  @org.junit.Test
  public void whereExpr004() {
    final XQuery query = new XQuery(
      "for $f in /MyComputer/Drive1/Folder[@creator=\"Mani\"]/File where $f/@creation_date=\"08/06/00\" return $f",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<File creation_date=\"08/06/00\" creator=\"Leon\" name=\"File00000000000\" type=\"FileType\" idref=\"1\" id=\"2\">\n\t\t\t\t<FileName>File00000000000</FileName>\n\t\t\t\t<description> abides fullest charms aprons batter perspective brags yesterday honor bluest<bold> wills crimeful calm lobby truer miser perseus take</bold> tapers owe ceas rhyme mindful surly suspect fantasticoes spirits legitimation holofernes portraiture trouts suffocating deed strides ithaca<bold> smiles sunshine loud right disdainfully figs searching</bold> curl reins humble nurture hue doom hidden abridgement seein<bold> invisible authentic shapes quae several reaches howled</bold> merry purpos howl burneth scholars untreasur maidenliest dealing</description>\n\t\t\t\t<SecurityObject name=\"so00000000001\">\n\t\t\t\t\t<Denies>\n\t\t\t\t\t\t<Deny>\n\t\t\t\t\t\t\t<security/>\n\t\t\t\t\t\t\t<user>system\\Changyuan</user>\n\t\t\t\t\t\t</Deny>\n\t\t\t\t\t\t<Deny>\n\t\t\t\t\t\t\t<security>\n\t\t\t\t\t\t\t\t<right>Read</right>\n\t\t\t\t\t\t\t</security>\n\t\t\t\t\t\t\t<user>system\\Toshiko</user>\n\t\t\t\t\t\t</Deny>\n\t\t\t\t\t</Denies>\n\t\t\t\t\t<Allows>\n\t\t\t\t\t\t<Allow>\n\t\t\t\t\t\t\t<security>\n\t\t\t\t\t\t\t\t<right>Read</right>\n\t\t\t\t\t\t\t\t<right>Write</right>\n\t\t\t\t\t\t\t</security>\n\t\t\t\t\t\t\t<user>system\\Hyungjeong</user>\n\t\t\t\t\t\t</Allow>\n\t\t\t\t\t</Allows>\n\t\t\t\t</SecurityObject>\n\t\t\t\t<Stream binary=\"false\" streamid=\"1\" content-type=\"text/xml\" size=\"1001.33\" name=\"sample.xml\" type=\"StreamType\">\n\t\t\t\t\t<StreamType>StreamType</StreamType>\n\t\t\t\t\t<StreamSize>1001.33</StreamSize>\n\t\t\t\t\t<StreamID>1</StreamID>\n\t\t\t\t\t<Binary>false</Binary>\n\t\t\t\t\t<Summary>This value is constant</Summary>\n\t\t\t\t</Stream>\n\t\t\t</File>", false)
    );
  }

  /**
   *  For+Where+Return - use always-false-predicate in 'Where' expr .
   */
  @org.junit.Test
  public void whereExpr005() {
    final XQuery query = new XQuery(
      "<empty> { for $folder in /MyComputer/Drive2//Folder where 1 = 2 return <Folder> { $folder/FolderName/text() } </Folder> } </empty>",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<empty/>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  For+Where+Return - In the inner For+Where+Return, uses outer variable in 'Where' expr .
   */
  @org.junit.Test
  public void whereExpr006() {
    final XQuery query = new XQuery(
      "for $folder in /MyComputer/Drive4//Folder return <Folder>{ $folder/@name }{ for $file in /MyComputer/Drive4//File where $file/@idref = $folder/@id return <File>{ $file/@name }</File> }</Folder>",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<Folder name=\"Folder00000000047\"/><Folder name=\"Folder00000000048\"><File name=\"File00000000077\"/><File name=\"File00000000078\"/><File name=\"File00000000079\"/><File name=\"File00000000080\"/><File name=\"File00000000081\"/></Folder><Folder name=\"Folder00000000049\"><File name=\"File00000000082\"/><File name=\"File00000000083\"/><File name=\"File00000000084\"/><File name=\"File00000000085\"/><File name=\"File00000000086\"/></Folder><Folder name=\"Folder00000000050\"><File name=\"File00000000087\"/><File name=\"File00000000088\"/></Folder><Folder name=\"Folder00000000051\"><File name=\"File00000000089\"/><File name=\"File00000000090\"/><File name=\"File00000000091\"/><File name=\"File00000000092\"/><File name=\"File00000000093\"/><File name=\"File00000000094\"/></Folder><Folder name=\"Folder00000000052\"><File name=\"File00000000095\"/><File name=\"File00000000096\"/></Folder><Folder name=\"Folder00000000053\"><File name=\"File00000000097\"/><File name=\"File00000000098\"/><File name=\"File00000000099\"/></Folder><Folder name=\"Folder00000000054\"><File name=\"File00000000100\"/></Folder>", false)
    );
  }

  /**
   *  For+Where+Return - 2 iterations use 'Where' to build relationship .
   */
  @org.junit.Test
  public void whereExpr007() {
    final XQuery query = new XQuery(
      "<fragment-result>{ for $folder in /MyComputer/Drive3/Folder ,$file in /MyComputer/Drive3/Folder/File where $folder/@id = $file/@idref return <Folder> { $folder/@name, $folder/@id } <file>{ $file/@idref, $file/FileName/text() }</file> </Folder> }</fragment-result>",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<fragment-result><Folder name=\"Folder00000000017\" id=\"67\"><file idref=\"67\">File00000000047</file></Folder><Folder name=\"Folder00000000032\" id=\"97\"><file idref=\"97\">File00000000062</file></Folder></fragment-result>", false)
    );
  }

  /**
   *  Test 'where' expression with the empty sequence literal .
   */
  @org.junit.Test
  public void whereExpr008() {
    final XQuery query = new XQuery(
      "<empty> { for $file in (//Folder)[1]/File where () return $file/FileName } </empty>",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<empty/>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Test 'where' expression returning a sequence with one or more nodes .
   */
  @org.junit.Test
  public void whereExpr009() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File where $file/SecurityObject/Denies/Deny/security/right return $file/FileName",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<FileName>File00000000000</FileName><FileName>File00000000002</FileName><FileName>File00000000004</FileName><FileName>File00000000006</FileName><FileName>File00000000007</FileName><FileName>File00000000008</FileName><FileName>File00000000011</FileName><FileName>File00000000014</FileName><FileName>File00000000016</FileName><FileName>File00000000017</FileName><FileName>File00000000018</FileName><FileName>File00000000020</FileName><FileName>File00000000022</FileName><FileName>File00000000023</FileName><FileName>File00000000024</FileName><FileName>File00000000025</FileName><FileName>File00000000027</FileName><FileName>File00000000028</FileName><FileName>File00000000029</FileName><FileName>File00000000030</FileName>", false)
    );
  }

  /**
   *  Test 'where' expression returning a sequence with multiple values .
   */
  @org.junit.Test
  public void whereExpr010() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File where data( $file/SecurityObject//right ) return $file/FileName",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("FORG0006")
    );
  }

  /**
   *  Test 'where' expression returning a sequence with a single boolean value .
   */
  @org.junit.Test
  public void whereExpr013() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File where $file/SecurityObject/Denies/Deny/security/right=\"Change\" return $file/FileName",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<FileName>File00000000002</FileName><FileName>File00000000004</FileName><FileName>File00000000008</FileName><FileName>File00000000014</FileName><FileName>File00000000018</FileName><FileName>File00000000020</FileName><FileName>File00000000022</FileName><FileName>File00000000025</FileName><FileName>File00000000030</FileName>", false)
    );
  }

  /**
   *  Test 'where' expression returning a sequence with a constant boolean value .
   */
  @org.junit.Test
  public void whereExpr014() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File where true() return $file/FileName",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<FileName>File00000000000</FileName><FileName>File00000000001</FileName><FileName>File00000000002</FileName><FileName>File00000000003</FileName><FileName>File00000000004</FileName><FileName>File00000000005</FileName><FileName>File00000000006</FileName><FileName>File00000000007</FileName><FileName>File00000000008</FileName><FileName>File00000000009</FileName><FileName>File00000000010</FileName><FileName>File00000000011</FileName><FileName>File00000000012</FileName><FileName>File00000000013</FileName><FileName>File00000000014</FileName><FileName>File00000000015</FileName><FileName>File00000000016</FileName><FileName>File00000000017</FileName><FileName>File00000000018</FileName><FileName>File00000000019</FileName><FileName>File00000000020</FileName><FileName>File00000000021</FileName><FileName>File00000000022</FileName><FileName>File00000000023</FileName><FileName>File00000000024</FileName><FileName>File00000000025</FileName><FileName>File00000000026</FileName><FileName>File00000000027</FileName><FileName>File00000000028</FileName><FileName>File00000000029</FileName><FileName>File00000000030</FileName>", false)
    );
  }

  /**
   *  Test 'where' expression returning a sequence with a constant boolean value .
   */
  @org.junit.Test
  public void whereExpr015() {
    final XQuery query = new XQuery(
      "<empty> { for $file in (//Folder)[1]/File where false() return $file/FileName } </empty>",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      (
        assertSerialization("<empty/>", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  Test 'where' clause based on a positional variable .
   */
  @org.junit.Test
  public void whereExpr016() {
    final XQuery query = new XQuery(
      "for $file at $offset in (//Folder)[1]/File where $offset mod 2 = 1 return $file/FileName",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<FileName>File00000000000</FileName><FileName>File00000000002</FileName><FileName>File00000000004</FileName><FileName>File00000000006</FileName><FileName>File00000000008</FileName><FileName>File00000000010</FileName><FileName>File00000000012</FileName><FileName>File00000000014</FileName><FileName>File00000000016</FileName><FileName>File00000000018</FileName><FileName>File00000000020</FileName><FileName>File00000000022</FileName><FileName>File00000000024</FileName><FileName>File00000000026</FileName><FileName>File00000000028</FileName><FileName>File00000000030</FileName>", false)
    );
  }

  /**
   *  Attempt to use multiple expressions in a 'where' clause .
   */
  @org.junit.Test
  public void whereExpr017() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File where true(), true() return $file/FileName",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
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
   *  Use of undefined variable in 'where' clause .
   */
  @org.junit.Test
  public void whereExpr018() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File where $undefined return $file/FileName",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XPST0008")
    );
  }

  /**
   *  Typo on 'where' clause .
   */
  @org.junit.Test
  public void whereExpr019() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File where_ true() return $file/FileName",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
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
   *  Multiple 'where' clauses (XQuery 1.0).
   */
  @org.junit.Test
  public void whereExpr020() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File where true() where false() return $file/FileName",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
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
   *  Multiple 'where' clauses (XQuery 3,0).
   */
  @org.junit.Test
  public void whereExpr020a() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File where true() where false() return $file/FileName",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Check that context item is NOT changed when evaluating the where clause.
   */
  @org.junit.Test
  public void whereExpr021() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File where (. instance of element(File)) return $file/FileName",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEmpty()
    );
  }

  /**
   *  Composite where clause depending on multiple variables .
   */
  @org.junit.Test
  public void whereExpr022() {
    final XQuery query = new XQuery(
      "\n" +
      "         for $file in //File \n" +
      "         let $namelen := string-length($file/FileName)\n" +
      "         where ($namelen = 15 and $file//Allow/user = 'system\\Hyungjeong') \n" +
      "         return $file/FileName\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "File00000000000")
    );
  }

  /**
   *  Where clause appearing after a count clause .
   */
  @org.junit.Test
  public void whereExpr023() {
    final XQuery query = new XQuery(
      "\n" +
      "         for $file in //File\n" +
      "         count $c\n" +
      "         where ($file//Allow/user = 'system\\Aladin') \n" +
      "         return $c\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2")
    );
  }

  /**
   *  Multiple where clauses in suboptimal order .
   */
  @org.junit.Test
  public void whereExpr024() {
    final XQuery query = new XQuery(
      "\n" +
      "         for $file in //File \n" +
      "         let $prefix := substring($file/FileName, 1, 4)\n" +
      "         where ($prefix = 'File')\n" +
      "         where ($file//Allow/user = 'system\\Hyungjeong')\n" +
      "         return $file/FileName\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "File00000000000")
    );
  }

  /**
   *  Where clause with no dependencies on FLWOR variables .
   */
  @org.junit.Test
  public void whereExpr025() {
    final XQuery query = new XQuery(
      "\n" +
      "         for $file in //File \n" +
      "         where current-date() gt xs:date('1900-01-01')\n" +
      "         let $prefix := substring($file/FileName, 1, 4)\n" +
      "         where ($prefix = 'File')\n" +
      "         where ($file//Allow/user = 'system\\Hyungjeong')\n" +
      "         return $file/FileName\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "File00000000000")
    );
  }

  /**
   *  Where clause that depends on position() .
   */
  @org.junit.Test
  public void whereExpr026() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $doc := (/)\n" +
      "         return (11 to 15)!\n" +
      "            (for $file in $doc//File\n" +
      "            count $c\n" +
      "            where $c = position()\n" +
      "            return $file/FileName)\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "File00000000000 File00000000001 File00000000002 File00000000003 File00000000004")
    );
  }

  /**
   *  Where clause that depends on last() .
   */
  @org.junit.Test
  public void whereExpr027() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $doc := (/)\n" +
      "         return (11 to 15)!\n" +
      "            (for $file at $c in $doc//File\n" +
      "            where $c = last()\n" +
      "            return string($file/FileName))\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "File00000000004 File00000000004 File00000000004 File00000000004 File00000000004")
    );
  }

  /**
   *  Where clause with let (and no for) .
   */
  @org.junit.Test
  public void whereExpr028() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $x := 1 to 10\n" +
      "         where $x = 1\n" +
      "         return $x\n" +
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
      assertStringValue(false, "1 2 3 4 5 6 7 8 9 10")
    );
  }

  /**
   *  Where clause with let (and no for) .
   */
  @org.junit.Test
  public void whereExpr029() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $x := 1 to 10\n" +
      "         where count($x) = 1\n" +
      "         return $x\n" +
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
      assertEmpty()
    );
  }

  /**
   *  Where clause with "outer for" .
   */
  @org.junit.Test
  public void whereExpr030() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare variable $n external := 10;\n" +
      "         for $x allowing empty in 1 to $n\n" +
      "         where $x lt 5\n" +
      "         return $x\n" +
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
      assertStringValue(false, "1 2 3 4")
    );
  }

  /**
   *  Where clause with "outer for" .
   */
  @org.junit.Test
  public void whereExpr031() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare variable $n external := 0;\n" +
      "         for $x allowing empty in 1 to $n\n" +
      "         where not($x = 5)\n" +
      "         return concat(\"[\", $x, \"]\")\n" +
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
      assertStringValue(false, "[]")
    );
  }

  /**
   *  Where clause with "outer for". A trap for systems that rewrite where clauses as predicates. .
   */
  @org.junit.Test
  public void whereExpr032() {
    final XQuery query = new XQuery(
      "\n" +
      "         for $x allowing empty in 1 to 5\n" +
      "         where $x lt 0\n" +
      "         return empty($x)\n" +
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
      assertEmpty()
    );
  }

  /**
   *  Integer-valued where clause. A trap for systems that rewrite where clauses as predicates. .
   */
  @org.junit.Test
  public void whereExpr033() {
    final XQuery query = new XQuery(
      "\n" +
      "         declare variable $n external := 0;\n" +
      "         for $x in -2 to +2\n" +
      "         where ($n - $x)\n" +
      "         return $x\n" +
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
      assertStringValue(false, "-2 -1 1 2")
    );
  }

  /**
   *  Test producing a group-by-key expression .
   */
  @org.junit.Test
  public void cbclGroupByKey001() {
    final XQuery query = new XQuery(
      "declare function local:get-words() { tokenize(\"she sells sea shells by the sea shore\", \"\\s+\") }; let $words := local:get-words() let $distinct-words := distinct-values($words) for $word in $distinct-words return <word word=\"{$word}\" count=\"{count($words[. = $word])}\" />",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<word word=\"she\" count=\"1\"/><word word=\"sells\" count=\"1\"/><word word=\"sea\" count=\"2\"/><word word=\"shells\" count=\"1\"/><word word=\"by\" count=\"1\"/><word word=\"the\" count=\"1\"/><word word=\"shore\" count=\"1\"/>", false)
    );
  }

  /**
   *  A variant on group-by-key that does not involve a left outer join .
   */
  @org.junit.Test
  public void cbclGroupByKey002() {
    final XQuery query = new XQuery(
      "let $items := for $x in 1 to 100 return $x mod 10, $distinct-items := distinct-values($items) for $dist in $distinct-items, $item in $items where $item = $dist return $item",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 1 1 1 1 1 1 1 1 1 2 2 2 2 2 2 2 2 2 2 3 3 3 3 3 3 3 3 3 3 4 4 4 4 4 4 4 4 4 4 5 5 5 5 5 5 5 5 5 5 6 6 6 6 6 6 6 6 6 6 7 7 7 7 7 7 7 7 7 7 8 8 8 8 8 8 8 8 8 8 9 9 9 9 9 9 9 9 9 9 0 0 0 0 0 0 0 0 0 0")
    );
  }

  /**
   *  A group-by-key that has to be careful about errors .
   */
  @org.junit.Test
  public void cbclGroupByKey003() {
    final XQuery query = new XQuery(
      "let $items := for $x in 1 to 100 return $x mod 10, $distinct-items := distinct-values($items) for $dist in $distinct-items return min( if ($dist = 0) then 0 else for $item in $items where $item = $dist return 1 div $item )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 0.5 0.333333333333333333 0.25 0.2 0.166666666666666667 0.142857142857142857 0.125 0.111111111111111111 0")
    );
  }

  /**
   *  A group by that has to be careful about errors .
   */
  @org.junit.Test
  public void cbclGroupByKey004() {
    final XQuery query = new XQuery(
      "let $items := for $x in 1 to 100 return $x mod 10, $distinct-items := distinct-values($items) for $dist in $distinct-items return min( for $item in $items where $item = $dist return if ($dist = 0) then 0 else 1 div $item )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 0.5 0.333333333333333333 0.25 0.2 0.166666666666666667 0.142857142857142857 0.125 0.111111111111111111 0")
    );
  }

  /**
   *  Test tricky hash-join implicit conversion case. .
   */
  @org.junit.Test
  public void cbclHashJoin005() {
    final XQuery query = new XQuery(
      "declare function local:durationOrFloat($i as xs:integer) { (xs:float(123), xs:float(234), xs:duration(\"P1D\"))[$i] }; for $x in (xs:untypedAtomic(\"123\"), xs:untypedAtomic(\"234\")) for $y in (for $z in (1,2) return local:durationOrFloat($z)) where $x = $y return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "123 234")
    );
  }

  /**
   *  Test a hash join that produces a dynamic type check error .
   */
  @org.junit.Test
  public void cbclHashJoin006() {
    final XQuery query = new XQuery(
      "declare function local:sequence($arg as xs:integer) as item()* { if ($arg mod 2 eq 0) then (1, 3, 5, 7, 9) else (\"a\", \"b\", \"c\") }; for $x in local:sequence(2) for $y in local:sequence(3) where $x = $y return ($x, $y)",
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
   *  Test a hash join that produces a static type check error, but is fine in dynamic mode .
   */
  @org.junit.Test
  public void cbclHashJoin007() {
    final XQuery query = new XQuery(
      "declare function local:sequence($arg as xs:integer) as item()* { if ($arg mod 2 eq 0) then (1, 3, 5, 7, 9) else (\"a\", \"b\", \"c\") }; for $x in local:sequence(2) for $y in local:sequence(2) where $x = $y return ($x, $y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 1 3 3 5 5 7 7 9 9")
    );
  }

  /**
   *  Test a hash join that casts untypedAtomic values to multiple types .
   */
  @org.junit.Test
  public void cbclHashJoin008() {
    final XQuery query = new XQuery(
      "declare function local:sequence($arg as xs:integer) as item()* { if ($arg mod 2 eq 0) then (1, 3, 5, 7, 9, \"cheese\") else (xs:untypedAtomic(\"1\"), xs:untypedAtomic(\"2\")) }; for $x in local:sequence(2) for $y in local:sequence(3) where $x = $y return ($x, $y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 1")
    );
  }

  /**
   *  Test a hash join with type error .
   */
  @org.junit.Test
  public void cbclHashJoin009() {
    final XQuery query = new XQuery(
      "for $x in (1,2,3,\"cheese\") for $y in (1,2,3,\"cheese\") where $x = $y return ($x, $y)",
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
   *  Test hash-join requiring untypedAtomic casting, with a type error. .
   */
  @org.junit.Test
  public void cbclHashJoin010() {
    final XQuery query = new XQuery(
      "for $x in (1,2,3, xs:untypedAtomic(\"1\")) for $y in (1,2,3,\"cheese\") where $x = $y return ($x, $y)",
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
   *  Test a hash join with sequences for keys .
   */
  @org.junit.Test
  public void cbclHashJoin011() {
    final XQuery query = new XQuery(
      "for $x in <t><r><d>1</d><d>1</d><d>1</d></r><r><d>1</d><d>2</d><d>3</d></r><r><d>2</d><d>3</d><d>4</d></r></t>/r, $y in <t><r><d>1</d><d>1</d><d>1</d></r><r><d>2</d><d>4</d><d>6</d></r><r><d>3</d><d>2</d><d>1</d></r></t>/r where $x/d/string(.) = $y/d/string(.) return concat($x, '=', $y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "111=111 111=321 123=111 123=246 123=321 234=246 234=321")
    );
  }

  /**
   *  Tests hash join between anyAtomicType containing only untypedAtomic and string .
   */
  @org.junit.Test
  public void cbclHashJoin012() {
    final XQuery query = new XQuery(
      "declare variable $strings := ('1','2'); declare variable $untypeds := (<untyped>1</untyped>,<untyped>2</untyped>); for $string in $strings for $untyped in $untypeds where $untyped = $string return $string",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 2")
    );
  }

  /**
   *  A query that can be optimized down to a hash join - testing that the implicit cast-operand is emulated correctly .
   */
  @org.junit.Test
  public void cbclHashJoin1() {
    final XQuery query = new XQuery(
      "for $x in (xs:untypedAtomic(\"123\"), xs:untypedAtomic(\"234\")) for $y in (xs:string(\"123\"), xs:float(123), xs:double(123)) where $x = $y return $x",
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
        assertStringValue(false, "123 123 123")
      ||
        error("XPST0004")
      )
    );
  }

  /**
   *  A query that can be optimized down to a hash join - with integer types. .
   */
  @org.junit.Test
  public void cbclHashJoin2() {
    final XQuery query = new XQuery(
      "for $x in (xs:integer(\"123\"), xs:integer(\"234\")) for $y in (xs:integer(\"123\"), xs:integer(\"456\")) where $x = $y return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "123")
    );
  }

  /**
   *  A query that can be optimized down to a hash join - with date types of varying timezones, assumes context timezone Z .
   */
  @org.junit.Test
  public void cbclHashJoin3() {
    final XQuery query = new XQuery(
      "for $x in (xs:date(\"2010-10-10\"), xs:date(\"1997-01-01+12:00\"), xs:date(\"1997-01-02+12:00\")) for $y in (xs:date(\"2010-10-10Z\"), xs:date(\"2010-10-10+01:00\"), xs:date(\"1997-01-01-12:00\")) where $x = $y return $x",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "2010-10-10 1997-01-02+12:00")
    );
  }

  /**
   *  A query that can be optimized down to a hash join - with a mixture of untypedAtomics and doubles. .
   */
  @org.junit.Test
  public void cbclHashJoin4() {
    final XQuery query = new XQuery(
      "for $x in (xs:untypedAtomic(\"2.0\"), xs:untypedAtomic(\"3\"), xs:double(\"3.0\")) for $y in (xs:untypedAtomic(\"2\"), xs:untypedAtomic(\"3\"), xs:double(\"3\")) where $x = $y return $x",
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
        assertStringValue(false, "3 3 3 3")
      ||
        error("XPST0004")
      )
    );
  }

  /**
   *  A left outer join with some tricky error avoidance .
   */
  @org.junit.Test
  public void cbclLeftOuterJoin001() {
    final XQuery query = new XQuery(
      "let $items := for $x in 1 to 100 return $x mod 10, $distinct-items := for $y in 1 to (count($items) idiv 10) return $y mod 10 for $dist in $distinct-items return if ($dist = 0) then 0 else min( for $item in $items where $item = $dist return 1 div $item )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 0.5 0.333333333333333333 0.25 0.2 0.166666666666666667 0.142857142857142857 0.125 0.111111111111111111 0")
    );
  }

  /**
   *  A left outer join that requires tricky predicate hoisting to spot. .
   */
  @org.junit.Test
  public void cbclLeftOuterJoin002() {
    final XQuery query = new XQuery(
      "<e> { for $x in 1 to 10 return <a>{for $y in 1 to 10 where $x > 7 and $y = $x return $y}</a> } </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e><a/><a/><a/><a/><a/><a/><a/><a>8</a><a>9</a><a>10</a></e>", false)
    );
  }

  /**
   *  A left outer join with multiple predicates .
   */
  @org.junit.Test
  public void cbclLeftOuterJoin003() {
    final XQuery query = new XQuery(
      "<e> { for $x in 1 to 10 where $x > 7 return <a>{for $y in 1 to 10 where $y = $x return $y}</a> } </e>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<e><a>8</a><a>9</a><a>10</a></e>", false)
    );
  }

  /**
.
   */
  @org.junit.Test
  public void cbclLeftOuterJoin004() {
    final XQuery query = new XQuery(
      "<tbody> { let $rows := <tables> <table> <row> <entry>Acetazolamide</entry> <entry>Acetazolamide</entry> </row> <row> <entry>Acetazolamide sodium</entry> <entry>Acetazolamide sodium</entry> </row> </table> <table> <row> <entry>Acetylcholine chloride</entry> <entry>Acetylcholine chloride</entry> </row> </table> <table> <row> <entry>Acetylcysteine</entry> <entry>Acetylcysteine</entry> </row> <row> <entry>Acetylcysteine sodium</entry> <entry>Acetylcysteine sodium</entry> </row> </table> </tables>/table/row return for $g in distinct-values($rows/entry[2][string(.)]) order by $g return let $matches := for $row in $rows where $g = string($row/entry[2][string(.)]) return $row/entry[1] return <entry> { $matches/( <link> { node() } </link>, text { if (position() lt last()) then '; ' else () } ) } </entry> } </tbody>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<tbody><entry><link>Acetazolamide</link></entry><entry><link>Acetazolamide sodium</link></entry><entry><link>Acetylcholine chloride</link></entry><entry><link>Acetylcysteine</link></entry><entry><link>Acetylcysteine sodium</link></entry></tbody>", false)
    );
  }

  /**
   *  Test to hit EvaluateOptional on MapConcatSingle .
   */
  @org.junit.Test
  public void cbclMapConcat001() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then true() else () }; declare function local:g($x) { if ($x) then \"true\" else \"false\" }; let $x := local:g(true()) for $y in local:f($x) return ($y, $x)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true true")
    );
  }

  /**
   *  Test to hit Evaluate on MapConcatOptional .
   */
  @org.junit.Test
  public void cbclMapConcat002() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then true() else () }; empty(for $x in local:f(false()), $y in 1 to 10 return ($x, $y))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  Test product with optional input .
   */
  @org.junit.Test
  public void cbclMapConcat003() {
    final XQuery query = new XQuery(
      "declare function local:f($x) { if ($x) then true() else () }; for $x in local:f(true()), $y in 1 to 10 return ($x, $y)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "true 1 true 2 true 3 true 4 true 5 true 6 true 7 true 8 true 9 true 10")
    );
  }

  /**
   *  Test products which return empty sequence .
   */
  @org.junit.Test
  public void cbclProduct001() {
    final XQuery query = new XQuery(
      "declare function local:odds($arg as xs:integer*) as xs:integer* { $arg[. mod 2 eq 1] }; boolean(zero-or-one(for $x in local:odds((2,4,8)),$y in local:odds((2,4,8)) return ($x,$y)))",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  Tests set from optional item returning empty sequence .
   */
  @org.junit.Test
  public void cbclSetFromOptionalItem001() {
    final XQuery query = new XQuery(
      "declare function local:odds($arg as xs:integer?) as xs:integer? { $arg[. mod 2 eq 1] }; boolean(for $x in local:odds(2) where $x < 3 return $x + 1)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  use of where clause involving an "or" expression. Use fn:true() .
   */
  @org.junit.Test
  public void whereClause1() {
    final XQuery query = new XQuery(
      "let $var := (fn:true()) where $var or fn:true() return $var or fn:true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  use of where clause used with quantified expression (some keyword). .
   */
  @org.junit.Test
  public void whereClause10() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $var := 1 \n" +
      "         where some $x in (1, 2) satisfies fn:string($var) = \"1\" \n" +
      "         return fn:string($var )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  use of where clause used with quantified expression (every keyword). .
   */
  @org.junit.Test
  public void whereClause11() {
    final XQuery query = new XQuery(
      "\n" +
      "         let $var := 1 \n" +
      "         where every $x in (1, 1) satisfies fn:string($var) = \"1\" \n" +
      "         return fn:string($var )",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1")
    );
  }

  /**
   *  use of where clause involving an "or" expression. Use fn:false() Use fn:not() .
   */
  @org.junit.Test
  public void whereClause2() {
    final XQuery query = new XQuery(
      "let $var := (fn:false()) where fn:not($var or fn:false()) return $var or fn:false()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  use of where clause involving an "or" ("and" operator) expression. Use fn:true() .
   */
  @org.junit.Test
  public void whereClause3() {
    final XQuery query = new XQuery(
      "let $var := (fn:true()) where $var and fn:true() return $var and fn:true()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }

  /**
   *  use of where clause involving an "or" ("and" operator) expression. Use fn:false() Use fn:not() .
   */
  @org.junit.Test
  public void whereClause4() {
    final XQuery query = new XQuery(
      "let $var := (fn:false()) where fn:not($var and fn:false()) return $var and fn:false()",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(false)
    );
  }

  /**
   *  use of where clause with a typeswitch expression .
   */
  @org.junit.Test
  public void whereClause5() {
    final XQuery query = new XQuery(
      "let $var := 100 where typeswitch($var) case $i as xs:string return fn:false() case $i as xs:integer return fn:true() default return fn:false() return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("100")
    );
  }

  /**
   *  use of where clause used with a string value and fn:string .
   */
  @org.junit.Test
  public void whereClause6() {
    final XQuery query = new XQuery(
      "let $var := \"String\" where fn:string($var) = \"String\" return $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "String")
    );
  }

  /**
   *  use of where clause used with a string-length function. .
   */
  @org.junit.Test
  public void whereClause7() {
    final XQuery query = new XQuery(
      "let $var := \"String\" where fn:string-length($var) = 6 return fn:string-length($var)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertEq("6")
    );
  }

  /**
   *  use of where clause used with a fn:count() function. .
   */
  @org.junit.Test
  public void whereClause8() {
    final XQuery query = new XQuery(
      "let $var := 100 where fn:count(($var)) = 1 return fn:count(($var))",
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
   *  use of where clause used with is comparator. .
   */
  @org.junit.Test
  public void whereClause9() {
    final XQuery query = new XQuery(
      "let $var := <anElement>Some content</anElement> where $var is $var return $var is $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertBoolean(true)
    );
  }
}
