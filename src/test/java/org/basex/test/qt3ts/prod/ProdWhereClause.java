package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the WhereClause production.
 *
 * @author BaseX Team 2005-12, BSD License
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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
    query.context(node(file("prod/ForClause/fsx.xml")));

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertStringValue(false, "-2 -1 1 2")
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
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

    final QT3Result res = result(query);
    result = res;
    test(
      assertBoolean(true)
    );
  }
}
