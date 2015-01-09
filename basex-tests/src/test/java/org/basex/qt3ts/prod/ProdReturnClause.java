package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the ReturnClause production.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdReturnClause extends QT3TestSet {

  /**
   *  Only a return clause is syntactically invalid. .
   */
  @org.junit.Test
  public void k2ReturnExpr1() {
    final XQuery query = new XQuery(
      "return 1",
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
        error("XPDY0002")
      )
    );
  }

  /**
   *  For+Return - use element's text to construct new element .
   */
  @org.junit.Test
  public void returnExpr001() {
    final XQuery query = new XQuery(
      "for $folder in /MyComputer/Drive2//Folder return <newFolder>{ $folder/FolderName/text() }</newFolder>",
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
      assertSerialization("<newFolder>Folder00000000001</newFolder><newFolder>Folder00000000002</newFolder><newFolder>Folder00000000003</newFolder><newFolder>Folder00000000004</newFolder><newFolder>Folder00000000005</newFolder><newFolder>Folder00000000006</newFolder><newFolder>Folder00000000007</newFolder><newFolder>Folder00000000008</newFolder><newFolder>Folder00000000009</newFolder><newFolder>Folder00000000010</newFolder><newFolder>Folder00000000011</newFolder><newFolder>Folder00000000012</newFolder><newFolder>Folder00000000013</newFolder><newFolder>Folder00000000014</newFolder><newFolder>Folder00000000015</newFolder><newFolder>Folder00000000016</newFolder>", false)
    );
  }

  /**
   *  For+Return - use existing attribute to generate new attribute for new element .
   */
  @org.junit.Test
  public void returnExpr002() {
    final XQuery query = new XQuery(
      "for $folder in /MyComputer/Drive2//Folder return <newFolder>{ $folder/@name, $folder/FolderName/text() }</newFolder>",
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
      assertSerialization("<newFolder name=\"Folder00000000001\">Folder00000000001</newFolder><newFolder name=\"Folder00000000002\">Folder00000000002</newFolder><newFolder name=\"Folder00000000003\">Folder00000000003</newFolder><newFolder name=\"Folder00000000004\">Folder00000000004</newFolder><newFolder name=\"Folder00000000005\">Folder00000000005</newFolder><newFolder name=\"Folder00000000006\">Folder00000000006</newFolder><newFolder name=\"Folder00000000007\">Folder00000000007</newFolder><newFolder name=\"Folder00000000008\">Folder00000000008</newFolder><newFolder name=\"Folder00000000009\">Folder00000000009</newFolder><newFolder name=\"Folder00000000010\">Folder00000000010</newFolder><newFolder name=\"Folder00000000011\">Folder00000000011</newFolder><newFolder name=\"Folder00000000012\">Folder00000000012</newFolder><newFolder name=\"Folder00000000013\">Folder00000000013</newFolder><newFolder name=\"Folder00000000014\">Folder00000000014</newFolder><newFolder name=\"Folder00000000015\">Folder00000000015</newFolder><newFolder name=\"Folder00000000016\">Folder00000000016</newFolder>", false)
    );
  }

  /**
   *  For+Return - one For+Return expr contains another one For+Return expr .
   */
  @org.junit.Test
  public void returnExpr003() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $folder in /MyComputer/Drive1/Folder[1] \n" +
      "        return <folder1> {$folder/FolderName} <files> { \n" +
      "                    for $file in ($folder/File)[position() < 6] \n" +
      "                    return <f>{$file/FileName/substring(.,13)}</f> } </files> \n" +
      "               </folder1>\n" +
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
      assertSerialization("<folder1><FolderName>Folder00000000000</FolderName><files><f>000</f><f>001</f><f>002</f><f>003</f><f>004</f></files></folder1>", false)
    );
  }

  /**
   *  For+Where+Return - filters in 'Where' expr and in 'Return' expr using predicate .
   */
  @org.junit.Test
  public void returnExpr004() {
    final XQuery query = new XQuery(
      "for $f in /MyComputer//File where $f/@creation_date=\"08/06/00\" return $f/SecurityObject/Denies/Deny[security/right]/user/string()",
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
      assertSerialization("system\\Toshiko", false)
    );
  }

  /**
   *  FLWOR expression returns selected element nodes .
   */
  @org.junit.Test
  public void returnExpr005() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File return $file/FileName",
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
   *  FLWOR expression returns selected values .
   */
  @org.junit.Test
  public void returnExpr006() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File return data( $file/FileName )",
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
      assertStringValue(false, "File00000000000 File00000000001 File00000000002 File00000000003 File00000000004 File00000000005 File00000000006 File00000000007 File00000000008 File00000000009 File00000000010 File00000000011 File00000000012 File00000000013 File00000000014 File00000000015 File00000000016 File00000000017 File00000000018 File00000000019 File00000000020 File00000000021 File00000000022 File00000000023 File00000000024 File00000000025 File00000000026 File00000000027 File00000000028 File00000000029 File00000000030")
    );
  }

  /**
   *  FLWOR expression return parent of select nodes .
   */
  @org.junit.Test
  public void returnExpr007() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File return $file/parent::Folder/FolderName",
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
      assertSerialization("<FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName><FolderName>Folder00000000000</FolderName>", false)
    );
  }

  /**
   *  FLWOR expression returns constant value, independent of input bindings .
   */
  @org.junit.Test
  public void returnExpr008() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File return 1",
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
      assertStringValue(false, "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1")
    );
  }

  /**
   *  FLWOR expression returns node from document, independent of input bindings .
   */
  @org.junit.Test
  public void returnExpr009() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File return (//FileName)[1]",
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
      assertSerialization("<FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName><FileName>File00000000000</FileName>", false)
    );
  }

  /**
   *  FLWOR expression returns empty sequence literal .
   */
  @org.junit.Test
  public void returnExpr010() {
    final XQuery query = new XQuery(
      "<empty> { for $file in (//Folder)[1]/File return () } </empty>",
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
        assertSerialization("<empty />", false)
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  FLWOR expression returns a constructed sequence .
   */
  @org.junit.Test
  public void returnExpr011() {
    final XQuery query = new XQuery(
      "for $folder in //Folder, $file in $folder/File return ( $folder/FolderName, $file/FileName )",
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
      assertSerialization("<FolderName>Folder00000000000</FolderName><FileName>File00000000000</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000001</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000002</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000003</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000004</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000005</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000006</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000007</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000008</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000009</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000010</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000011</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000012</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000013</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000014</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000015</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000016</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000017</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000018</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000019</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000020</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000021</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000022</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000023</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000024</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000025</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000026</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000027</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000028</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000029</FileName><FolderName>Folder00000000000</FolderName><FileName>File00000000030</FileName><FolderName>Folder00000000001</FolderName><FileName>File00000000031</FileName><FolderName>Folder00000000002</FolderName><FileName>File00000000032</FileName><FolderName>Folder00000000003</FolderName><FileName>File00000000033</FileName><FolderName>Folder00000000004</FolderName><FileName>File00000000034</FileName><FolderName>Folder00000000005</FolderName><FileName>File00000000035</FileName><FolderName>Folder00000000006</FolderName><FileName>File00000000036</FileName><FolderName>Folder00000000007</FolderName><FileName>File00000000037</FileName><FolderName>Folder00000000008</FolderName><FileName>File00000000038</FileName><FolderName>Folder00000000009</FolderName><FileName>File00000000039</FileName><FolderName>Folder00000000010</FolderName><FileName>File00000000040</FileName><FolderName>Folder00000000011</FolderName><FileName>File00000000041</FileName><FolderName>Folder00000000012</FolderName><FileName>File00000000042</FileName><FolderName>Folder00000000013</FolderName><FileName>File00000000043</FileName><FolderName>Folder00000000014</FolderName><FileName>File00000000044</FileName><FolderName>Folder00000000015</FolderName><FileName>File00000000045</FileName><FolderName>Folder00000000016</FolderName><FileName>File00000000046</FileName><FolderName>Folder00000000017</FolderName><FileName>File00000000047</FileName><FolderName>Folder00000000018</FolderName><FileName>File00000000048</FileName><FolderName>Folder00000000019</FolderName><FileName>File00000000049</FileName><FolderName>Folder00000000020</FolderName><FileName>File00000000050</FileName><FolderName>Folder00000000021</FolderName><FileName>File00000000051</FileName><FolderName>Folder00000000022</FolderName><FileName>File00000000052</FileName><FolderName>Folder00000000023</FolderName><FileName>File00000000053</FileName><FolderName>Folder00000000024</FolderName><FileName>File00000000054</FileName><FolderName>Folder00000000025</FolderName><FileName>File00000000055</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000056</FileName><FolderName>Folder00000000027</FolderName><FileName>File00000000057</FileName><FolderName>Folder00000000028</FolderName><FileName>File00000000058</FileName><FolderName>Folder00000000029</FolderName><FileName>File00000000059</FileName><FolderName>Folder00000000030</FolderName><FileName>File00000000060</FileName><FolderName>Folder00000000031</FolderName><FileName>File00000000061</FileName><FolderName>Folder00000000032</FolderName><FileName>File00000000062</FileName><FolderName>Folder00000000033</FolderName><FileName>File00000000063</FileName><FolderName>Folder00000000034</FolderName><FileName>File00000000064</FileName><FolderName>Folder00000000035</FolderName><FileName>File00000000065</FileName><FolderName>Folder00000000036</FolderName><FileName>File00000000066</FileName><FolderName>Folder00000000037</FolderName><FileName>File00000000067</FileName><FolderName>Folder00000000038</FolderName><FileName>File00000000068</FileName><FolderName>Folder00000000039</FolderName><FileName>File00000000069</FileName><FolderName>Folder00000000040</FolderName><FileName>File00000000070</FileName><FolderName>Folder00000000041</FolderName><FileName>File00000000071</FileName><FolderName>Folder00000000042</FolderName><FileName>File00000000072</FileName><FolderName>Folder00000000043</FolderName><FileName>File00000000073</FileName><FolderName>Folder00000000044</FolderName><FileName>File00000000074</FileName><FolderName>Folder00000000045</FolderName><FileName>File00000000075</FileName><FolderName>Folder00000000046</FolderName><FileName>File00000000076</FileName><FolderName>Folder00000000048</FolderName><FileName>File00000000077</FileName><FolderName>Folder00000000048</FolderName><FileName>File00000000078</FileName><FolderName>Folder00000000048</FolderName><FileName>File00000000079</FileName><FolderName>Folder00000000048</FolderName><FileName>File00000000080</FileName><FolderName>Folder00000000048</FolderName><FileName>File00000000081</FileName><FolderName>Folder00000000049</FolderName><FileName>File00000000082</FileName><FolderName>Folder00000000049</FolderName><FileName>File00000000083</FileName><FolderName>Folder00000000049</FolderName><FileName>File00000000084</FileName><FolderName>Folder00000000049</FolderName><FileName>File00000000085</FileName><FolderName>Folder00000000049</FolderName><FileName>File00000000086</FileName><FolderName>Folder00000000050</FolderName><FileName>File00000000087</FileName><FolderName>Folder00000000050</FolderName><FileName>File00000000088</FileName><FolderName>Folder00000000051</FolderName><FileName>File00000000089</FileName><FolderName>Folder00000000051</FolderName><FileName>File00000000090</FileName><FolderName>Folder00000000051</FolderName><FileName>File00000000091</FileName><FolderName>Folder00000000051</FolderName><FileName>File00000000092</FileName><FolderName>Folder00000000051</FolderName><FileName>File00000000093</FileName><FolderName>Folder00000000051</FolderName><FileName>File00000000094</FileName><FolderName>Folder00000000052</FolderName><FileName>File00000000095</FileName><FolderName>Folder00000000052</FolderName><FileName>File00000000096</FileName><FolderName>Folder00000000053</FolderName><FileName>File00000000097</FileName><FolderName>Folder00000000053</FolderName><FileName>File00000000098</FileName><FolderName>Folder00000000053</FolderName><FileName>File00000000099</FileName><FolderName>Folder00000000054</FolderName><FileName>File00000000100</FileName>", false)
    );
  }

  /**
   *  Multiple return statements .
   */
  @org.junit.Test
  public void returnExpr012() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File return $file return $file",
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
   *  Missing 'return' statement in FLWOR expression .
   */
  @org.junit.Test
  public void returnExpr013() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File",
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
   *  FLWOR expression return statement depends on undefined variable .
   */
  @org.junit.Test
  public void returnExpr014() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File return $undefined",
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
   *  Variable bound to value from return statement .
   */
  @org.junit.Test
  public void returnExpr015() {
    final XQuery query = new XQuery(
      "for $file in for $folder in (//Folder)[1] return $folder/File return $file/FileName",
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
   *  Return value of positional variable .
   */
  @org.junit.Test
  public void returnExpr016() {
    final XQuery query = new XQuery(
      "for $file at $offset in (//Folder)[1]/File return <File>{ $file/@name }{ attribute offset{ $offset }}</File>",
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
      assertSerialization("<File name=\"File00000000000\" offset=\"1\" /><File name=\"File00000000001\" offset=\"2\" /><File name=\"File00000000002\" offset=\"3\" /><File name=\"File00000000003\" offset=\"4\" /><File name=\"File00000000004\" offset=\"5\" /><File name=\"File00000000005\" offset=\"6\" /><File name=\"File00000000006\" offset=\"7\" /><File name=\"File00000000007\" offset=\"8\" /><File name=\"File00000000008\" offset=\"9\" /><File name=\"File00000000009\" offset=\"10\" /><File name=\"File00000000010\" offset=\"11\" /><File name=\"File00000000011\" offset=\"12\" /><File name=\"File00000000012\" offset=\"13\" /><File name=\"File00000000013\" offset=\"14\" /><File name=\"File00000000014\" offset=\"15\" /><File name=\"File00000000015\" offset=\"16\" /><File name=\"File00000000016\" offset=\"17\" /><File name=\"File00000000017\" offset=\"18\" /><File name=\"File00000000018\" offset=\"19\" /><File name=\"File00000000019\" offset=\"20\" /><File name=\"File00000000020\" offset=\"21\" /><File name=\"File00000000021\" offset=\"22\" /><File name=\"File00000000022\" offset=\"23\" /><File name=\"File00000000023\" offset=\"24\" /><File name=\"File00000000024\" offset=\"25\" /><File name=\"File00000000025\" offset=\"26\" /><File name=\"File00000000026\" offset=\"27\" /><File name=\"File00000000027\" offset=\"28\" /><File name=\"File00000000028\" offset=\"29\" /><File name=\"File00000000029\" offset=\"30\" /><File name=\"File00000000030\" offset=\"31\" />", false)
    );
  }

  /**
   *  Apply arithmetic operator inside 'return' statement .
   */
  @org.junit.Test
  public void returnExpr017() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File return ($file/Stream/StreamSize)[1] + 1",
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
      assertStringValue(false, "1002.33 1003.33 1004.33 1005.33 1006.33 1007.33 1008.33 1009.33 1010.33 1011.33 1012.33 1013.33 1014.33 1015.33 1016.33 1017.33 1018.33 1019.33 1020.33 1021.33 1022.33 1023.33 1024.33 1025.33 1026.33 1027.33 1028.33 1029.33 1030.33 1031.33 1032.33")
    );
  }

  /**
   *  Apply comparison operator inside 'return' statement .
   */
  @org.junit.Test
  public void returnExpr018() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File return ($file/Stream/StreamSize)[1] > 1004",
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
      assertStringValue(false, "false false false true true true true true true true true true true true true true true true true true true true true true true true true true true true true")
    );
  }

  /**
   *  Multiple 'return' statements .
   */
  @org.junit.Test
  public void returnExpr019() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File return return $file/FileName",
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
   *  'return' expression containing a typed value constructor function .
   */
  @org.junit.Test
  public void returnExpr020() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File return xs:string( data( $file/FileName[1] ))",
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
      assertStringValue(false, "File00000000000 File00000000001 File00000000002 File00000000003 File00000000004 File00000000005 File00000000006 File00000000007 File00000000008 File00000000009 File00000000010 File00000000011 File00000000012 File00000000013 File00000000014 File00000000015 File00000000016 File00000000017 File00000000018 File00000000019 File00000000020 File00000000021 File00000000022 File00000000023 File00000000024 File00000000025 File00000000026 File00000000027 File00000000028 File00000000029 File00000000030")
    );
  }
}
