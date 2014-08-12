package org.basex.qt3ts.prod;

import org.basex.tests.bxapi.*;
import org.basex.tests.qt3ts.*;

/**
 * Tests for the ForClause production (or the ForExpr in XPath).
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdForClause extends QT3TestSet {

  /**
   *  For+Return - to iterate a constant sequence .
   */
  @org.junit.Test
  public void forExpr001() {
    final XQuery query = new XQuery(
      "for $a in (\"test\", \"test1\", \"test2\") return $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "test test1 test2")
    );
  }

  /**
   *  For+Return - error, undefined variable in 'IN' Expr .
   */
  @org.junit.Test
  public void forExpr002() {
    final XQuery query = new XQuery(
      "for $folder in $folder/File return <file name=\"{$folder/File/FileName}\"/>",
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
   *  For+Return - use predicate in 'IN' Expr .
   */
  @org.junit.Test
  public void forExpr003() {
    final XQuery query = new XQuery(
      "for $f in /MyComputer/Drive4/Folder[@id=\"128\"] return <FolderName>{$f/@name}</FolderName>",
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
      assertSerialization("<FolderName name=\"Folder00000000047\"/>", false)
    );
  }

  /**
   *  For+Return - use more than one predicates in 'IN' Expr .
   */
  @org.junit.Test
  public void forExpr004() {
    final XQuery query = new XQuery(
      "for $f in /MyComputer/Drive1/Folder[@creator=\"Mani\"]/File[@creation_date=\"08/06/00\"] return <file>{$f/../@creator}{$f/@creation_date}</file>",
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
      assertSerialization("<file creator=\"Mani\" creation_date=\"08/06/00\" />", false)
    );
  }

  /**
   *  For+Return - use sequence(security/right) as the predicate .
   */
  @org.junit.Test
  public void forExpr005() {
    final XQuery query = new XQuery(
      "for $f in /MyComputer//File[@creation_date=\"08/06/00\"]/SecurityObject/Denies/Deny[security/right] return $f/../../@name/string()",
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
      assertStringValue(false, "so00000000001")
    );
  }

  /**
   *  For+Return - 2 For will generate two iterations .
   */
  @org.junit.Test
  public void forExpr006() {
    final XQuery query = new XQuery(
      "<fragment-result>{ for $folder in /MyComputer/Drive3/Folder ,$file in /MyComputer/Drive3/Folder/File return <Folder>{ $folder/@name, $folder/@id }<file>{ $file/@idref }{ data($file/@name) }</file> </Folder> }</fragment-result>",
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
      assertSerialization("<fragment-result><Folder name=\"Folder00000000017\" id=\"67\"><file idref=\"67\">File00000000047</file></Folder><Folder name=\"Folder00000000017\" id=\"67\"><file idref=\"97\">File00000000062</file></Folder><Folder name=\"Folder00000000032\" id=\"97\"><file idref=\"67\">File00000000047</file></Folder><Folder name=\"Folder00000000032\" id=\"97\"><file idref=\"97\">File00000000062</file></Folder></fragment-result>", false)
    );
  }

  /**
   *  For+Return - use special character in variable name .
   */
  @org.junit.Test
  public void forExpr007() {
    final XQuery query = new XQuery(
      "for $AaBbCc.-_Dd in /MyComputer/Drive1/Folder return $AaBbCc.-_Dd/FolderName",
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
      assertSerialization("<FolderName>Folder00000000000</FolderName>", false)
    );
  }

  /**
   *  For+Where+Return - use special characters in variable name .
   */
  @org.junit.Test
  public void forExpr008() {
    final XQuery query = new XQuery(
      "for $AaBbCc.-_Dd in /MyComputer/Drive1/Folder where $AaBbCc.-_Dd/@creator = \"Mani\" return $AaBbCc.-_Dd/FolderName",
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
      assertSerialization("<FolderName>Folder00000000000</FolderName>", false)
    );
  }

  /**
   *  For+Return - error, use variable in it's own declaration .
   */
  @org.junit.Test
  public void forExpr009() {
    final XQuery query = new XQuery(
      "for $a in $a/* return $a",
      ctx);
    try {
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
   *  FLWOR expression with multiple, interdependent variables .
   */
  @org.junit.Test
  public void forExpr010() {
    final XQuery query = new XQuery(
      "for $folder in //Folder for $file in $folder//File return <File folder=\"{$folder/FolderName}\">{string($file/FileName[1])}</File>",
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
      assertSerialization("<File folder=\"Folder00000000000\">File00000000000</File><File folder=\"Folder00000000000\">File00000000001</File><File folder=\"Folder00000000000\">File00000000002</File><File folder=\"Folder00000000000\">File00000000003</File><File folder=\"Folder00000000000\">File00000000004</File><File folder=\"Folder00000000000\">File00000000005</File><File folder=\"Folder00000000000\">File00000000006</File><File folder=\"Folder00000000000\">File00000000007</File><File folder=\"Folder00000000000\">File00000000008</File><File folder=\"Folder00000000000\">File00000000009</File><File folder=\"Folder00000000000\">File00000000010</File><File folder=\"Folder00000000000\">File00000000011</File><File folder=\"Folder00000000000\">File00000000012</File><File folder=\"Folder00000000000\">File00000000013</File><File folder=\"Folder00000000000\">File00000000014</File><File folder=\"Folder00000000000\">File00000000015</File><File folder=\"Folder00000000000\">File00000000016</File><File folder=\"Folder00000000000\">File00000000017</File><File folder=\"Folder00000000000\">File00000000018</File><File folder=\"Folder00000000000\">File00000000019</File><File folder=\"Folder00000000000\">File00000000020</File><File folder=\"Folder00000000000\">File00000000021</File><File folder=\"Folder00000000000\">File00000000022</File><File folder=\"Folder00000000000\">File00000000023</File><File folder=\"Folder00000000000\">File00000000024</File><File folder=\"Folder00000000000\">File00000000025</File><File folder=\"Folder00000000000\">File00000000026</File><File folder=\"Folder00000000000\">File00000000027</File><File folder=\"Folder00000000000\">File00000000028</File><File folder=\"Folder00000000000\">File00000000029</File><File folder=\"Folder00000000000\">File00000000030</File><File folder=\"Folder00000000001\">File00000000031</File><File folder=\"Folder00000000001\">File00000000032</File><File folder=\"Folder00000000001\">File00000000033</File><File folder=\"Folder00000000001\">File00000000034</File><File folder=\"Folder00000000001\">File00000000035</File><File folder=\"Folder00000000001\">File00000000036</File><File folder=\"Folder00000000001\">File00000000037</File><File folder=\"Folder00000000001\">File00000000038</File><File folder=\"Folder00000000001\">File00000000039</File><File folder=\"Folder00000000001\">File00000000040</File><File folder=\"Folder00000000001\">File00000000041</File><File folder=\"Folder00000000001\">File00000000042</File><File folder=\"Folder00000000001\">File00000000043</File><File folder=\"Folder00000000001\">File00000000044</File><File folder=\"Folder00000000001\">File00000000045</File><File folder=\"Folder00000000001\">File00000000046</File><File folder=\"Folder00000000002\">File00000000032</File><File folder=\"Folder00000000002\">File00000000033</File><File folder=\"Folder00000000002\">File00000000034</File><File folder=\"Folder00000000002\">File00000000035</File><File folder=\"Folder00000000002\">File00000000036</File><File folder=\"Folder00000000002\">File00000000037</File><File folder=\"Folder00000000002\">File00000000038</File><File folder=\"Folder00000000002\">File00000000039</File><File folder=\"Folder00000000002\">File00000000040</File><File folder=\"Folder00000000002\">File00000000041</File><File folder=\"Folder00000000002\">File00000000042</File><File folder=\"Folder00000000002\">File00000000043</File><File folder=\"Folder00000000002\">File00000000044</File><File folder=\"Folder00000000002\">File00000000045</File><File folder=\"Folder00000000002\">File00000000046</File><File folder=\"Folder00000000003\">File00000000033</File><File folder=\"Folder00000000003\">File00000000034</File><File folder=\"Folder00000000003\">File00000000035</File><File folder=\"Folder00000000003\">File00000000036</File><File folder=\"Folder00000000003\">File00000000037</File><File folder=\"Folder00000000003\">File00000000038</File><File folder=\"Folder00000000003\">File00000000039</File><File folder=\"Folder00000000003\">File00000000040</File><File folder=\"Folder00000000003\">File00000000041</File><File folder=\"Folder00000000003\">File00000000042</File><File folder=\"Folder00000000003\">File00000000043</File><File folder=\"Folder00000000003\">File00000000044</File><File folder=\"Folder00000000003\">File00000000045</File><File folder=\"Folder00000000003\">File00000000046</File><File folder=\"Folder00000000004\">File00000000034</File><File folder=\"Folder00000000004\">File00000000035</File><File folder=\"Folder00000000004\">File00000000036</File><File folder=\"Folder00000000004\">File00000000037</File><File folder=\"Folder00000000004\">File00000000038</File><File folder=\"Folder00000000004\">File00000000039</File><File folder=\"Folder00000000004\">File00000000040</File><File folder=\"Folder00000000004\">File00000000041</File><File folder=\"Folder00000000004\">File00000000042</File><File folder=\"Folder00000000004\">File00000000043</File><File folder=\"Folder00000000004\">File00000000044</File><File folder=\"Folder00000000004\">File00000000045</File><File folder=\"Folder00000000004\">File00000000046</File><File folder=\"Folder00000000005\">File00000000035</File><File folder=\"Folder00000000005\">File00000000036</File><File folder=\"Folder00000000005\">File00000000037</File><File folder=\"Folder00000000005\">File00000000038</File><File folder=\"Folder00000000005\">File00000000039</File><File folder=\"Folder00000000005\">File00000000040</File><File folder=\"Folder00000000005\">File00000000041</File><File folder=\"Folder00000000005\">File00000000042</File><File folder=\"Folder00000000005\">File00000000043</File><File folder=\"Folder00000000005\">File00000000044</File><File folder=\"Folder00000000005\">File00000000045</File><File folder=\"Folder00000000005\">File00000000046</File><File folder=\"Folder00000000006\">File00000000036</File><File folder=\"Folder00000000006\">File00000000037</File><File folder=\"Folder00000000006\">File00000000038</File><File folder=\"Folder00000000006\">File00000000039</File><File folder=\"Folder00000000006\">File00000000040</File><File folder=\"Folder00000000006\">File00000000041</File><File folder=\"Folder00000000006\">File00000000042</File><File folder=\"Folder00000000006\">File00000000043</File><File folder=\"Folder00000000006\">File00000000044</File><File folder=\"Folder00000000006\">File00000000045</File><File folder=\"Folder00000000006\">File00000000046</File><File folder=\"Folder00000000007\">File00000000037</File><File folder=\"Folder00000000007\">File00000000038</File><File folder=\"Folder00000000007\">File00000000039</File><File folder=\"Folder00000000007\">File00000000040</File><File folder=\"Folder00000000007\">File00000000041</File><File folder=\"Folder00000000007\">File00000000042</File><File folder=\"Folder00000000007\">File00000000043</File><File folder=\"Folder00000000007\">File00000000044</File><File folder=\"Folder00000000007\">File00000000045</File><File folder=\"Folder00000000007\">File00000000046</File><File folder=\"Folder00000000008\">File00000000038</File><File folder=\"Folder00000000008\">File00000000039</File><File folder=\"Folder00000000008\">File00000000040</File><File folder=\"Folder00000000008\">File00000000041</File><File folder=\"Folder00000000008\">File00000000042</File><File folder=\"Folder00000000008\">File00000000043</File><File folder=\"Folder00000000008\">File00000000044</File><File folder=\"Folder00000000008\">File00000000045</File><File folder=\"Folder00000000008\">File00000000046</File><File folder=\"Folder00000000009\">File00000000039</File><File folder=\"Folder00000000009\">File00000000040</File><File folder=\"Folder00000000009\">File00000000041</File><File folder=\"Folder00000000009\">File00000000042</File><File folder=\"Folder00000000009\">File00000000043</File><File folder=\"Folder00000000009\">File00000000044</File><File folder=\"Folder00000000009\">File00000000045</File><File folder=\"Folder00000000009\">File00000000046</File><File folder=\"Folder00000000010\">File00000000040</File><File folder=\"Folder00000000010\">File00000000041</File><File folder=\"Folder00000000010\">File00000000042</File><File folder=\"Folder00000000010\">File00000000043</File><File folder=\"Folder00000000010\">File00000000044</File><File folder=\"Folder00000000010\">File00000000045</File><File folder=\"Folder00000000010\">File00000000046</File><File folder=\"Folder00000000011\">File00000000041</File><File folder=\"Folder00000000011\">File00000000042</File><File folder=\"Folder00000000011\">File00000000043</File><File folder=\"Folder00000000011\">File00000000044</File><File folder=\"Folder00000000011\">File00000000045</File><File folder=\"Folder00000000011\">File00000000046</File><File folder=\"Folder00000000012\">File00000000042</File><File folder=\"Folder00000000012\">File00000000043</File><File folder=\"Folder00000000012\">File00000000044</File><File folder=\"Folder00000000012\">File00000000045</File><File folder=\"Folder00000000012\">File00000000046</File><File folder=\"Folder00000000013\">File00000000043</File><File folder=\"Folder00000000013\">File00000000044</File><File folder=\"Folder00000000013\">File00000000045</File><File folder=\"Folder00000000013\">File00000000046</File><File folder=\"Folder00000000014\">File00000000044</File><File folder=\"Folder00000000014\">File00000000045</File><File folder=\"Folder00000000014\">File00000000046</File><File folder=\"Folder00000000015\">File00000000045</File><File folder=\"Folder00000000015\">File00000000046</File><File folder=\"Folder00000000016\">File00000000046</File><File folder=\"Folder00000000017\">File00000000047</File><File folder=\"Folder00000000017\">File00000000048</File><File folder=\"Folder00000000017\">File00000000049</File><File folder=\"Folder00000000017\">File00000000050</File><File folder=\"Folder00000000017\">File00000000051</File><File folder=\"Folder00000000017\">File00000000052</File><File folder=\"Folder00000000017\">File00000000053</File><File folder=\"Folder00000000017\">File00000000054</File><File folder=\"Folder00000000017\">File00000000055</File><File folder=\"Folder00000000017\">File00000000056</File><File folder=\"Folder00000000017\">File00000000057</File><File folder=\"Folder00000000017\">File00000000058</File><File folder=\"Folder00000000017\">File00000000059</File><File folder=\"Folder00000000017\">File00000000060</File><File folder=\"Folder00000000017\">File00000000061</File><File folder=\"Folder00000000018\">File00000000048</File><File folder=\"Folder00000000018\">File00000000049</File><File folder=\"Folder00000000018\">File00000000050</File><File folder=\"Folder00000000018\">File00000000051</File><File folder=\"Folder00000000018\">File00000000052</File><File folder=\"Folder00000000018\">File00000000053</File><File folder=\"Folder00000000018\">File00000000054</File><File folder=\"Folder00000000019\">File00000000049</File><File folder=\"Folder00000000019\">File00000000050</File><File folder=\"Folder00000000019\">File00000000051</File><File folder=\"Folder00000000020\">File00000000050</File><File folder=\"Folder00000000021\">File00000000051</File><File folder=\"Folder00000000022\">File00000000052</File><File folder=\"Folder00000000022\">File00000000053</File><File folder=\"Folder00000000022\">File00000000054</File><File folder=\"Folder00000000023\">File00000000053</File><File folder=\"Folder00000000024\">File00000000054</File><File folder=\"Folder00000000025\">File00000000055</File><File folder=\"Folder00000000025\">File00000000056</File><File folder=\"Folder00000000025\">File00000000057</File><File folder=\"Folder00000000025\">File00000000058</File><File folder=\"Folder00000000025\">File00000000059</File><File folder=\"Folder00000000025\">File00000000060</File><File folder=\"Folder00000000025\">File00000000061</File><File folder=\"Folder00000000026\">File00000000056</File><File folder=\"Folder00000000026\">File00000000057</File><File folder=\"Folder00000000026\">File00000000058</File><File folder=\"Folder00000000027\">File00000000057</File><File folder=\"Folder00000000028\">File00000000058</File><File folder=\"Folder00000000029\">File00000000059</File><File folder=\"Folder00000000029\">File00000000060</File><File folder=\"Folder00000000029\">File00000000061</File><File folder=\"Folder00000000030\">File00000000060</File><File folder=\"Folder00000000031\">File00000000061</File><File folder=\"Folder00000000032\">File00000000062</File><File folder=\"Folder00000000032\">File00000000063</File><File folder=\"Folder00000000032\">File00000000064</File><File folder=\"Folder00000000032\">File00000000065</File><File folder=\"Folder00000000032\">File00000000066</File><File folder=\"Folder00000000032\">File00000000067</File><File folder=\"Folder00000000032\">File00000000068</File><File folder=\"Folder00000000032\">File00000000069</File><File folder=\"Folder00000000032\">File00000000070</File><File folder=\"Folder00000000032\">File00000000071</File><File folder=\"Folder00000000032\">File00000000072</File><File folder=\"Folder00000000032\">File00000000073</File><File folder=\"Folder00000000032\">File00000000074</File><File folder=\"Folder00000000032\">File00000000075</File><File folder=\"Folder00000000032\">File00000000076</File><File folder=\"Folder00000000033\">File00000000063</File><File folder=\"Folder00000000033\">File00000000064</File><File folder=\"Folder00000000033\">File00000000065</File><File folder=\"Folder00000000033\">File00000000066</File><File folder=\"Folder00000000033\">File00000000067</File><File folder=\"Folder00000000033\">File00000000068</File><File folder=\"Folder00000000033\">File00000000069</File><File folder=\"Folder00000000034\">File00000000064</File><File folder=\"Folder00000000034\">File00000000065</File><File folder=\"Folder00000000034\">File00000000066</File><File folder=\"Folder00000000035\">File00000000065</File><File folder=\"Folder00000000036\">File00000000066</File><File folder=\"Folder00000000037\">File00000000067</File><File folder=\"Folder00000000037\">File00000000068</File><File folder=\"Folder00000000037\">File00000000069</File><File folder=\"Folder00000000038\">File00000000068</File><File folder=\"Folder00000000039\">File00000000069</File><File folder=\"Folder00000000040\">File00000000070</File><File folder=\"Folder00000000040\">File00000000071</File><File folder=\"Folder00000000040\">File00000000072</File><File folder=\"Folder00000000040\">File00000000073</File><File folder=\"Folder00000000040\">File00000000074</File><File folder=\"Folder00000000040\">File00000000075</File><File folder=\"Folder00000000040\">File00000000076</File><File folder=\"Folder00000000041\">File00000000071</File><File folder=\"Folder00000000041\">File00000000072</File><File folder=\"Folder00000000041\">File00000000073</File><File folder=\"Folder00000000042\">File00000000072</File><File folder=\"Folder00000000043\">File00000000073</File><File folder=\"Folder00000000044\">File00000000074</File><File folder=\"Folder00000000044\">File00000000075</File><File folder=\"Folder00000000044\">File00000000076</File><File folder=\"Folder00000000045\">File00000000075</File><File folder=\"Folder00000000046\">File00000000076</File><File folder=\"Folder00000000047\">File00000000077</File><File folder=\"Folder00000000047\">File00000000078</File><File folder=\"Folder00000000047\">File00000000079</File><File folder=\"Folder00000000047\">File00000000080</File><File folder=\"Folder00000000047\">File00000000081</File><File folder=\"Folder00000000047\">File00000000082</File><File folder=\"Folder00000000047\">File00000000083</File><File folder=\"Folder00000000047\">File00000000084</File><File folder=\"Folder00000000047\">File00000000085</File><File folder=\"Folder00000000047\">File00000000086</File><File folder=\"Folder00000000047\">File00000000087</File><File folder=\"Folder00000000047\">File00000000088</File><File folder=\"Folder00000000047\">File00000000089</File><File folder=\"Folder00000000047\">File00000000090</File><File folder=\"Folder00000000047\">File00000000091</File><File folder=\"Folder00000000047\">File00000000092</File><File folder=\"Folder00000000047\">File00000000093</File><File folder=\"Folder00000000047\">File00000000094</File><File folder=\"Folder00000000047\">File00000000095</File><File folder=\"Folder00000000047\">File00000000096</File><File folder=\"Folder00000000047\">File00000000097</File><File folder=\"Folder00000000047\">File00000000098</File><File folder=\"Folder00000000047\">File00000000099</File><File folder=\"Folder00000000047\">File00000000100</File><File folder=\"Folder00000000048\">File00000000077</File><File folder=\"Folder00000000048\">File00000000078</File><File folder=\"Folder00000000048\">File00000000079</File><File folder=\"Folder00000000048\">File00000000080</File><File folder=\"Folder00000000048\">File00000000081</File><File folder=\"Folder00000000049\">File00000000082</File><File folder=\"Folder00000000049\">File00000000083</File><File folder=\"Folder00000000049\">File00000000084</File><File folder=\"Folder00000000049\">File00000000085</File><File folder=\"Folder00000000049\">File00000000086</File><File folder=\"Folder00000000050\">File00000000087</File><File folder=\"Folder00000000050\">File00000000088</File><File folder=\"Folder00000000051\">File00000000089</File><File folder=\"Folder00000000051\">File00000000090</File><File folder=\"Folder00000000051\">File00000000091</File><File folder=\"Folder00000000051\">File00000000092</File><File folder=\"Folder00000000051\">File00000000093</File><File folder=\"Folder00000000051\">File00000000094</File><File folder=\"Folder00000000052\">File00000000095</File><File folder=\"Folder00000000052\">File00000000096</File><File folder=\"Folder00000000053\">File00000000097</File><File folder=\"Folder00000000053\">File00000000098</File><File folder=\"Folder00000000053\">File00000000099</File><File folder=\"Folder00000000054\">File00000000100</File>", false)
    );
  }

  /**
   *  FLWOR expression with multiple, interdependent variables .
   */
  @org.junit.Test
  public void forExpr011() {
    final XQuery query = new XQuery(
      "for $folder in //Folder, $file in $folder//File return <File folder=\"{$folder/FolderName}\">{string($file/FileName[1])}</File>",
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
      assertSerialization("<File folder=\"Folder00000000000\">File00000000000</File><File folder=\"Folder00000000000\">File00000000001</File><File folder=\"Folder00000000000\">File00000000002</File><File folder=\"Folder00000000000\">File00000000003</File><File folder=\"Folder00000000000\">File00000000004</File><File folder=\"Folder00000000000\">File00000000005</File><File folder=\"Folder00000000000\">File00000000006</File><File folder=\"Folder00000000000\">File00000000007</File><File folder=\"Folder00000000000\">File00000000008</File><File folder=\"Folder00000000000\">File00000000009</File><File folder=\"Folder00000000000\">File00000000010</File><File folder=\"Folder00000000000\">File00000000011</File><File folder=\"Folder00000000000\">File00000000012</File><File folder=\"Folder00000000000\">File00000000013</File><File folder=\"Folder00000000000\">File00000000014</File><File folder=\"Folder00000000000\">File00000000015</File><File folder=\"Folder00000000000\">File00000000016</File><File folder=\"Folder00000000000\">File00000000017</File><File folder=\"Folder00000000000\">File00000000018</File><File folder=\"Folder00000000000\">File00000000019</File><File folder=\"Folder00000000000\">File00000000020</File><File folder=\"Folder00000000000\">File00000000021</File><File folder=\"Folder00000000000\">File00000000022</File><File folder=\"Folder00000000000\">File00000000023</File><File folder=\"Folder00000000000\">File00000000024</File><File folder=\"Folder00000000000\">File00000000025</File><File folder=\"Folder00000000000\">File00000000026</File><File folder=\"Folder00000000000\">File00000000027</File><File folder=\"Folder00000000000\">File00000000028</File><File folder=\"Folder00000000000\">File00000000029</File><File folder=\"Folder00000000000\">File00000000030</File><File folder=\"Folder00000000001\">File00000000031</File><File folder=\"Folder00000000001\">File00000000032</File><File folder=\"Folder00000000001\">File00000000033</File><File folder=\"Folder00000000001\">File00000000034</File><File folder=\"Folder00000000001\">File00000000035</File><File folder=\"Folder00000000001\">File00000000036</File><File folder=\"Folder00000000001\">File00000000037</File><File folder=\"Folder00000000001\">File00000000038</File><File folder=\"Folder00000000001\">File00000000039</File><File folder=\"Folder00000000001\">File00000000040</File><File folder=\"Folder00000000001\">File00000000041</File><File folder=\"Folder00000000001\">File00000000042</File><File folder=\"Folder00000000001\">File00000000043</File><File folder=\"Folder00000000001\">File00000000044</File><File folder=\"Folder00000000001\">File00000000045</File><File folder=\"Folder00000000001\">File00000000046</File><File folder=\"Folder00000000002\">File00000000032</File><File folder=\"Folder00000000002\">File00000000033</File><File folder=\"Folder00000000002\">File00000000034</File><File folder=\"Folder00000000002\">File00000000035</File><File folder=\"Folder00000000002\">File00000000036</File><File folder=\"Folder00000000002\">File00000000037</File><File folder=\"Folder00000000002\">File00000000038</File><File folder=\"Folder00000000002\">File00000000039</File><File folder=\"Folder00000000002\">File00000000040</File><File folder=\"Folder00000000002\">File00000000041</File><File folder=\"Folder00000000002\">File00000000042</File><File folder=\"Folder00000000002\">File00000000043</File><File folder=\"Folder00000000002\">File00000000044</File><File folder=\"Folder00000000002\">File00000000045</File><File folder=\"Folder00000000002\">File00000000046</File><File folder=\"Folder00000000003\">File00000000033</File><File folder=\"Folder00000000003\">File00000000034</File><File folder=\"Folder00000000003\">File00000000035</File><File folder=\"Folder00000000003\">File00000000036</File><File folder=\"Folder00000000003\">File00000000037</File><File folder=\"Folder00000000003\">File00000000038</File><File folder=\"Folder00000000003\">File00000000039</File><File folder=\"Folder00000000003\">File00000000040</File><File folder=\"Folder00000000003\">File00000000041</File><File folder=\"Folder00000000003\">File00000000042</File><File folder=\"Folder00000000003\">File00000000043</File><File folder=\"Folder00000000003\">File00000000044</File><File folder=\"Folder00000000003\">File00000000045</File><File folder=\"Folder00000000003\">File00000000046</File><File folder=\"Folder00000000004\">File00000000034</File><File folder=\"Folder00000000004\">File00000000035</File><File folder=\"Folder00000000004\">File00000000036</File><File folder=\"Folder00000000004\">File00000000037</File><File folder=\"Folder00000000004\">File00000000038</File><File folder=\"Folder00000000004\">File00000000039</File><File folder=\"Folder00000000004\">File00000000040</File><File folder=\"Folder00000000004\">File00000000041</File><File folder=\"Folder00000000004\">File00000000042</File><File folder=\"Folder00000000004\">File00000000043</File><File folder=\"Folder00000000004\">File00000000044</File><File folder=\"Folder00000000004\">File00000000045</File><File folder=\"Folder00000000004\">File00000000046</File><File folder=\"Folder00000000005\">File00000000035</File><File folder=\"Folder00000000005\">File00000000036</File><File folder=\"Folder00000000005\">File00000000037</File><File folder=\"Folder00000000005\">File00000000038</File><File folder=\"Folder00000000005\">File00000000039</File><File folder=\"Folder00000000005\">File00000000040</File><File folder=\"Folder00000000005\">File00000000041</File><File folder=\"Folder00000000005\">File00000000042</File><File folder=\"Folder00000000005\">File00000000043</File><File folder=\"Folder00000000005\">File00000000044</File><File folder=\"Folder00000000005\">File00000000045</File><File folder=\"Folder00000000005\">File00000000046</File><File folder=\"Folder00000000006\">File00000000036</File><File folder=\"Folder00000000006\">File00000000037</File><File folder=\"Folder00000000006\">File00000000038</File><File folder=\"Folder00000000006\">File00000000039</File><File folder=\"Folder00000000006\">File00000000040</File><File folder=\"Folder00000000006\">File00000000041</File><File folder=\"Folder00000000006\">File00000000042</File><File folder=\"Folder00000000006\">File00000000043</File><File folder=\"Folder00000000006\">File00000000044</File><File folder=\"Folder00000000006\">File00000000045</File><File folder=\"Folder00000000006\">File00000000046</File><File folder=\"Folder00000000007\">File00000000037</File><File folder=\"Folder00000000007\">File00000000038</File><File folder=\"Folder00000000007\">File00000000039</File><File folder=\"Folder00000000007\">File00000000040</File><File folder=\"Folder00000000007\">File00000000041</File><File folder=\"Folder00000000007\">File00000000042</File><File folder=\"Folder00000000007\">File00000000043</File><File folder=\"Folder00000000007\">File00000000044</File><File folder=\"Folder00000000007\">File00000000045</File><File folder=\"Folder00000000007\">File00000000046</File><File folder=\"Folder00000000008\">File00000000038</File><File folder=\"Folder00000000008\">File00000000039</File><File folder=\"Folder00000000008\">File00000000040</File><File folder=\"Folder00000000008\">File00000000041</File><File folder=\"Folder00000000008\">File00000000042</File><File folder=\"Folder00000000008\">File00000000043</File><File folder=\"Folder00000000008\">File00000000044</File><File folder=\"Folder00000000008\">File00000000045</File><File folder=\"Folder00000000008\">File00000000046</File><File folder=\"Folder00000000009\">File00000000039</File><File folder=\"Folder00000000009\">File00000000040</File><File folder=\"Folder00000000009\">File00000000041</File><File folder=\"Folder00000000009\">File00000000042</File><File folder=\"Folder00000000009\">File00000000043</File><File folder=\"Folder00000000009\">File00000000044</File><File folder=\"Folder00000000009\">File00000000045</File><File folder=\"Folder00000000009\">File00000000046</File><File folder=\"Folder00000000010\">File00000000040</File><File folder=\"Folder00000000010\">File00000000041</File><File folder=\"Folder00000000010\">File00000000042</File><File folder=\"Folder00000000010\">File00000000043</File><File folder=\"Folder00000000010\">File00000000044</File><File folder=\"Folder00000000010\">File00000000045</File><File folder=\"Folder00000000010\">File00000000046</File><File folder=\"Folder00000000011\">File00000000041</File><File folder=\"Folder00000000011\">File00000000042</File><File folder=\"Folder00000000011\">File00000000043</File><File folder=\"Folder00000000011\">File00000000044</File><File folder=\"Folder00000000011\">File00000000045</File><File folder=\"Folder00000000011\">File00000000046</File><File folder=\"Folder00000000012\">File00000000042</File><File folder=\"Folder00000000012\">File00000000043</File><File folder=\"Folder00000000012\">File00000000044</File><File folder=\"Folder00000000012\">File00000000045</File><File folder=\"Folder00000000012\">File00000000046</File><File folder=\"Folder00000000013\">File00000000043</File><File folder=\"Folder00000000013\">File00000000044</File><File folder=\"Folder00000000013\">File00000000045</File><File folder=\"Folder00000000013\">File00000000046</File><File folder=\"Folder00000000014\">File00000000044</File><File folder=\"Folder00000000014\">File00000000045</File><File folder=\"Folder00000000014\">File00000000046</File><File folder=\"Folder00000000015\">File00000000045</File><File folder=\"Folder00000000015\">File00000000046</File><File folder=\"Folder00000000016\">File00000000046</File><File folder=\"Folder00000000017\">File00000000047</File><File folder=\"Folder00000000017\">File00000000048</File><File folder=\"Folder00000000017\">File00000000049</File><File folder=\"Folder00000000017\">File00000000050</File><File folder=\"Folder00000000017\">File00000000051</File><File folder=\"Folder00000000017\">File00000000052</File><File folder=\"Folder00000000017\">File00000000053</File><File folder=\"Folder00000000017\">File00000000054</File><File folder=\"Folder00000000017\">File00000000055</File><File folder=\"Folder00000000017\">File00000000056</File><File folder=\"Folder00000000017\">File00000000057</File><File folder=\"Folder00000000017\">File00000000058</File><File folder=\"Folder00000000017\">File00000000059</File><File folder=\"Folder00000000017\">File00000000060</File><File folder=\"Folder00000000017\">File00000000061</File><File folder=\"Folder00000000018\">File00000000048</File><File folder=\"Folder00000000018\">File00000000049</File><File folder=\"Folder00000000018\">File00000000050</File><File folder=\"Folder00000000018\">File00000000051</File><File folder=\"Folder00000000018\">File00000000052</File><File folder=\"Folder00000000018\">File00000000053</File><File folder=\"Folder00000000018\">File00000000054</File><File folder=\"Folder00000000019\">File00000000049</File><File folder=\"Folder00000000019\">File00000000050</File><File folder=\"Folder00000000019\">File00000000051</File><File folder=\"Folder00000000020\">File00000000050</File><File folder=\"Folder00000000021\">File00000000051</File><File folder=\"Folder00000000022\">File00000000052</File><File folder=\"Folder00000000022\">File00000000053</File><File folder=\"Folder00000000022\">File00000000054</File><File folder=\"Folder00000000023\">File00000000053</File><File folder=\"Folder00000000024\">File00000000054</File><File folder=\"Folder00000000025\">File00000000055</File><File folder=\"Folder00000000025\">File00000000056</File><File folder=\"Folder00000000025\">File00000000057</File><File folder=\"Folder00000000025\">File00000000058</File><File folder=\"Folder00000000025\">File00000000059</File><File folder=\"Folder00000000025\">File00000000060</File><File folder=\"Folder00000000025\">File00000000061</File><File folder=\"Folder00000000026\">File00000000056</File><File folder=\"Folder00000000026\">File00000000057</File><File folder=\"Folder00000000026\">File00000000058</File><File folder=\"Folder00000000027\">File00000000057</File><File folder=\"Folder00000000028\">File00000000058</File><File folder=\"Folder00000000029\">File00000000059</File><File folder=\"Folder00000000029\">File00000000060</File><File folder=\"Folder00000000029\">File00000000061</File><File folder=\"Folder00000000030\">File00000000060</File><File folder=\"Folder00000000031\">File00000000061</File><File folder=\"Folder00000000032\">File00000000062</File><File folder=\"Folder00000000032\">File00000000063</File><File folder=\"Folder00000000032\">File00000000064</File><File folder=\"Folder00000000032\">File00000000065</File><File folder=\"Folder00000000032\">File00000000066</File><File folder=\"Folder00000000032\">File00000000067</File><File folder=\"Folder00000000032\">File00000000068</File><File folder=\"Folder00000000032\">File00000000069</File><File folder=\"Folder00000000032\">File00000000070</File><File folder=\"Folder00000000032\">File00000000071</File><File folder=\"Folder00000000032\">File00000000072</File><File folder=\"Folder00000000032\">File00000000073</File><File folder=\"Folder00000000032\">File00000000074</File><File folder=\"Folder00000000032\">File00000000075</File><File folder=\"Folder00000000032\">File00000000076</File><File folder=\"Folder00000000033\">File00000000063</File><File folder=\"Folder00000000033\">File00000000064</File><File folder=\"Folder00000000033\">File00000000065</File><File folder=\"Folder00000000033\">File00000000066</File><File folder=\"Folder00000000033\">File00000000067</File><File folder=\"Folder00000000033\">File00000000068</File><File folder=\"Folder00000000033\">File00000000069</File><File folder=\"Folder00000000034\">File00000000064</File><File folder=\"Folder00000000034\">File00000000065</File><File folder=\"Folder00000000034\">File00000000066</File><File folder=\"Folder00000000035\">File00000000065</File><File folder=\"Folder00000000036\">File00000000066</File><File folder=\"Folder00000000037\">File00000000067</File><File folder=\"Folder00000000037\">File00000000068</File><File folder=\"Folder00000000037\">File00000000069</File><File folder=\"Folder00000000038\">File00000000068</File><File folder=\"Folder00000000039\">File00000000069</File><File folder=\"Folder00000000040\">File00000000070</File><File folder=\"Folder00000000040\">File00000000071</File><File folder=\"Folder00000000040\">File00000000072</File><File folder=\"Folder00000000040\">File00000000073</File><File folder=\"Folder00000000040\">File00000000074</File><File folder=\"Folder00000000040\">File00000000075</File><File folder=\"Folder00000000040\">File00000000076</File><File folder=\"Folder00000000041\">File00000000071</File><File folder=\"Folder00000000041\">File00000000072</File><File folder=\"Folder00000000041\">File00000000073</File><File folder=\"Folder00000000042\">File00000000072</File><File folder=\"Folder00000000043\">File00000000073</File><File folder=\"Folder00000000044\">File00000000074</File><File folder=\"Folder00000000044\">File00000000075</File><File folder=\"Folder00000000044\">File00000000076</File><File folder=\"Folder00000000045\">File00000000075</File><File folder=\"Folder00000000046\">File00000000076</File><File folder=\"Folder00000000047\">File00000000077</File><File folder=\"Folder00000000047\">File00000000078</File><File folder=\"Folder00000000047\">File00000000079</File><File folder=\"Folder00000000047\">File00000000080</File><File folder=\"Folder00000000047\">File00000000081</File><File folder=\"Folder00000000047\">File00000000082</File><File folder=\"Folder00000000047\">File00000000083</File><File folder=\"Folder00000000047\">File00000000084</File><File folder=\"Folder00000000047\">File00000000085</File><File folder=\"Folder00000000047\">File00000000086</File><File folder=\"Folder00000000047\">File00000000087</File><File folder=\"Folder00000000047\">File00000000088</File><File folder=\"Folder00000000047\">File00000000089</File><File folder=\"Folder00000000047\">File00000000090</File><File folder=\"Folder00000000047\">File00000000091</File><File folder=\"Folder00000000047\">File00000000092</File><File folder=\"Folder00000000047\">File00000000093</File><File folder=\"Folder00000000047\">File00000000094</File><File folder=\"Folder00000000047\">File00000000095</File><File folder=\"Folder00000000047\">File00000000096</File><File folder=\"Folder00000000047\">File00000000097</File><File folder=\"Folder00000000047\">File00000000098</File><File folder=\"Folder00000000047\">File00000000099</File><File folder=\"Folder00000000047\">File00000000100</File><File folder=\"Folder00000000048\">File00000000077</File><File folder=\"Folder00000000048\">File00000000078</File><File folder=\"Folder00000000048\">File00000000079</File><File folder=\"Folder00000000048\">File00000000080</File><File folder=\"Folder00000000048\">File00000000081</File><File folder=\"Folder00000000049\">File00000000082</File><File folder=\"Folder00000000049\">File00000000083</File><File folder=\"Folder00000000049\">File00000000084</File><File folder=\"Folder00000000049\">File00000000085</File><File folder=\"Folder00000000049\">File00000000086</File><File folder=\"Folder00000000050\">File00000000087</File><File folder=\"Folder00000000050\">File00000000088</File><File folder=\"Folder00000000051\">File00000000089</File><File folder=\"Folder00000000051\">File00000000090</File><File folder=\"Folder00000000051\">File00000000091</File><File folder=\"Folder00000000051\">File00000000092</File><File folder=\"Folder00000000051\">File00000000093</File><File folder=\"Folder00000000051\">File00000000094</File><File folder=\"Folder00000000052\">File00000000095</File><File folder=\"Folder00000000052\">File00000000096</File><File folder=\"Folder00000000053\">File00000000097</File><File folder=\"Folder00000000053\">File00000000098</File><File folder=\"Folder00000000053\">File00000000099</File><File folder=\"Folder00000000054\">File00000000100</File>", false)
    );
  }

  /**
   *  Nested FLWOR expressions .
   */
  @org.junit.Test
  public void forExpr012() {
    final XQuery query = new XQuery(
      "for $fileName in for $file in //Folder/File return $file/FileName return string( $fileName )",
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
      assertStringValue(false, "File00000000000 File00000000001 File00000000002 File00000000003 File00000000004 File00000000005 File00000000006 File00000000007 File00000000008 File00000000009 File00000000010 File00000000011 File00000000012 File00000000013 File00000000014 File00000000015 File00000000016 File00000000017 File00000000018 File00000000019 File00000000020 File00000000021 File00000000022 File00000000023 File00000000024 File00000000025 File00000000026 File00000000027 File00000000028 File00000000029 File00000000030 File00000000031 File00000000032 File00000000033 File00000000034 File00000000035 File00000000036 File00000000037 File00000000038 File00000000039 File00000000040 File00000000041 File00000000042 File00000000043 File00000000044 File00000000045 File00000000046 File00000000047 File00000000048 File00000000049 File00000000050 File00000000051 File00000000052 File00000000053 File00000000054 File00000000055 File00000000056 File00000000057 File00000000058 File00000000059 File00000000060 File00000000061 File00000000062 File00000000063 File00000000064 File00000000065 File00000000066 File00000000067 File00000000068 File00000000069 File00000000070 File00000000071 File00000000072 File00000000073 File00000000074 File00000000075 File00000000076 File00000000077 File00000000078 File00000000079 File00000000080 File00000000081 File00000000082 File00000000083 File00000000084 File00000000085 File00000000086 File00000000087 File00000000088 File00000000089 File00000000090 File00000000091 File00000000092 File00000000093 File00000000094 File00000000095 File00000000096 File00000000097 File00000000098 File00000000099 File00000000100")
    );
  }

  /**
   *  Multiple variables based off the same input context .
   */
  @org.junit.Test
  public void forExpr013() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $fileName in //File/FileName, \n" +
      "            $folderName in //Folder[contains(description, 'fren')]/FolderName \n" +
      "        return ($folderName, $fileName)",
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
      assertSerialization("<FolderName>Folder00000000026</FolderName><FileName>File00000000000</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000001</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000002</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000003</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000004</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000005</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000006</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000007</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000008</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000009</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000010</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000011</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000012</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000013</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000014</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000015</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000016</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000017</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000018</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000019</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000020</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000021</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000022</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000023</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000024</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000025</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000026</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000027</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000028</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000029</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000030</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000031</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000032</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000033</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000034</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000035</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000036</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000037</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000038</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000039</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000040</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000041</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000042</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000043</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000044</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000045</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000046</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000047</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000048</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000049</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000050</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000051</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000052</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000053</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000054</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000055</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000056</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000057</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000058</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000059</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000060</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000061</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000062</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000063</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000064</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000065</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000066</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000067</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000068</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000069</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000070</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000071</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000072</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000073</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000074</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000075</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000076</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000077</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000078</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000079</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000080</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000081</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000082</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000083</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000084</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000085</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000086</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000087</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000088</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000089</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000090</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000091</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000092</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000093</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000094</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000095</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000096</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000097</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000098</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000099</FileName><FolderName>Folder00000000026</FolderName><FileName>File00000000100</FileName>", false)
    );
  }

  /**
   *  Incorrect syntax for nested loop. Multiple return statements at the same level .
   */
  @org.junit.Test
  public void forExpr014() {
    final XQuery query = new XQuery(
      "for $fileName in //File/FileName for $folderName in //Folder/FolderName return $folderName return $fileName",
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
   *  Return expression contains nested for expression .
   */
  @org.junit.Test
  public void forExpr015() {
    final XQuery query = new XQuery(
      "for $folder in //Folder return for $file in $folder/File return string( $file/FileName[1] )",
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
      assertStringValue(false, "File00000000000 File00000000001 File00000000002 File00000000003 File00000000004 File00000000005 File00000000006 File00000000007 File00000000008 File00000000009 File00000000010 File00000000011 File00000000012 File00000000013 File00000000014 File00000000015 File00000000016 File00000000017 File00000000018 File00000000019 File00000000020 File00000000021 File00000000022 File00000000023 File00000000024 File00000000025 File00000000026 File00000000027 File00000000028 File00000000029 File00000000030 File00000000031 File00000000032 File00000000033 File00000000034 File00000000035 File00000000036 File00000000037 File00000000038 File00000000039 File00000000040 File00000000041 File00000000042 File00000000043 File00000000044 File00000000045 File00000000046 File00000000047 File00000000048 File00000000049 File00000000050 File00000000051 File00000000052 File00000000053 File00000000054 File00000000055 File00000000056 File00000000057 File00000000058 File00000000059 File00000000060 File00000000061 File00000000062 File00000000063 File00000000064 File00000000065 File00000000066 File00000000067 File00000000068 File00000000069 File00000000070 File00000000071 File00000000072 File00000000073 File00000000074 File00000000075 File00000000076 File00000000077 File00000000078 File00000000079 File00000000080 File00000000081 File00000000082 File00000000083 File00000000084 File00000000085 File00000000086 File00000000087 File00000000088 File00000000089 File00000000090 File00000000091 File00000000092 File00000000093 File00000000094 File00000000095 File00000000096 File00000000097 File00000000098 File00000000099 File00000000100")
    );
  }

  /**
   *  Interate over nodes in document and constant sequence. Return value based on both .
   */
  @org.junit.Test
  public void forExpr016() {
    final XQuery query = new XQuery(
      "for $folder in //Folder, $index in (1, 2, 3) return $folder/File[$index]/FileName",
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
      assertSerialization("<FileName>File00000000000</FileName><FileName>File00000000001</FileName><FileName>File00000000002</FileName><FileName>File00000000031</FileName><FileName>File00000000032</FileName><FileName>File00000000033</FileName><FileName>File00000000034</FileName><FileName>File00000000035</FileName><FileName>File00000000036</FileName><FileName>File00000000037</FileName><FileName>File00000000038</FileName><FileName>File00000000039</FileName><FileName>File00000000040</FileName><FileName>File00000000041</FileName><FileName>File00000000042</FileName><FileName>File00000000043</FileName><FileName>File00000000044</FileName><FileName>File00000000045</FileName><FileName>File00000000046</FileName><FileName>File00000000047</FileName><FileName>File00000000048</FileName><FileName>File00000000049</FileName><FileName>File00000000050</FileName><FileName>File00000000051</FileName><FileName>File00000000052</FileName><FileName>File00000000053</FileName><FileName>File00000000054</FileName><FileName>File00000000055</FileName><FileName>File00000000056</FileName><FileName>File00000000057</FileName><FileName>File00000000058</FileName><FileName>File00000000059</FileName><FileName>File00000000060</FileName><FileName>File00000000061</FileName><FileName>File00000000062</FileName><FileName>File00000000063</FileName><FileName>File00000000064</FileName><FileName>File00000000065</FileName><FileName>File00000000066</FileName><FileName>File00000000067</FileName><FileName>File00000000068</FileName><FileName>File00000000069</FileName><FileName>File00000000070</FileName><FileName>File00000000071</FileName><FileName>File00000000072</FileName><FileName>File00000000073</FileName><FileName>File00000000074</FileName><FileName>File00000000075</FileName><FileName>File00000000076</FileName><FileName>File00000000077</FileName><FileName>File00000000078</FileName><FileName>File00000000079</FileName><FileName>File00000000082</FileName><FileName>File00000000083</FileName><FileName>File00000000084</FileName><FileName>File00000000087</FileName><FileName>File00000000088</FileName><FileName>File00000000089</FileName><FileName>File00000000090</FileName><FileName>File00000000091</FileName><FileName>File00000000095</FileName><FileName>File00000000096</FileName><FileName>File00000000097</FileName><FileName>File00000000098</FileName><FileName>File00000000099</FileName><FileName>File00000000100</FileName>", false)
    );
  }

  /**
   *  Multiple return statements from single for expression .
   */
  @org.junit.Test
  public void forExpr017() {
    final XQuery query = new XQuery(
      "for $folder in //Folder return $folder return $folder",
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
   *  Multiple in statements for single variable binding .
   */
  @org.junit.Test
  public void forExpr018() {
    final XQuery query = new XQuery(
      "for $folder in in .//Folder return $folder",
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
   *  Multiple for statements for single variable binding .
   */
  @org.junit.Test
  public void forExpr019() {
    final XQuery query = new XQuery(
      "for for $folder in //Folder return $folder",
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
   *  Multiple variable bindings followed by a trailing , .
   */
  @org.junit.Test
  public void forExpr020() {
    final XQuery query = new XQuery(
      "for $folder in //Folder, $file in $folder/File, return $file/FileName",
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
   *  Test order of iteration between two variables in the same 'for' statement .
   */
  @org.junit.Test
  public void forExpr021() {
    final XQuery query = new XQuery(
      "for $i in (1, 2), $j in (3, 4) return ($i, $j)",
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
      assertStringValue(false, "1 3 1 4 2 3 2 4")
    );
  }

  /**
   *  FLWOR expressions with positional variable .
   */
  @org.junit.Test
  public void forExpr022() {
    final XQuery query = new XQuery(
      "for $file at $pos in (//Folder)[1]/File return (string($file/FileName[1]), $pos)",
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
      assertStringValue(false, "File00000000000 1 File00000000001 2 File00000000002 3 File00000000003 4 File00000000004 5 File00000000005 6 File00000000006 7 File00000000007 8 File00000000008 9 File00000000009 10 File00000000010 11 File00000000011 12 File00000000012 13 File00000000013 14 File00000000014 15 File00000000015 16 File00000000016 17 File00000000017 18 File00000000018 19 File00000000019 20 File00000000020 21 File00000000021 22 File00000000022 23 File00000000023 24 File00000000024 25 File00000000025 26 File00000000026 27 File00000000027 28 File00000000028 29 File00000000029 30 File00000000030 31")
    );
  }

  /**
   *  Use positional variable in binding for new variable .
   */
  @org.junit.Test
  public void forExpr023() {
    final XQuery query = new XQuery(
      "for $file at $pos in (//Folder)[1]/File, $pos2 in $pos+1 return (string($file/FileName[1]), $pos, $pos2)",
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
      assertStringValue(false, "File00000000000 1 2 File00000000001 2 3 File00000000002 3 4 File00000000003 4 5 File00000000004 5 6 File00000000005 6 7 File00000000006 7 8 File00000000007 8 9 File00000000008 9 10 File00000000009 10 11 File00000000010 11 12 File00000000011 12 13 File00000000012 13 14 File00000000013 14 15 File00000000014 15 16 File00000000015 16 17 File00000000016 17 18 File00000000017 18 19 File00000000018 19 20 File00000000019 20 21 File00000000020 21 22 File00000000021 22 23 File00000000022 23 24 File00000000023 24 25 File00000000024 25 26 File00000000025 26 27 File00000000026 27 28 File00000000027 28 29 File00000000028 29 30 File00000000029 30 31 File00000000030 31 32")
    );
  }

  /**
   *  Reuse existing variable name for positional variable .
   */
  @org.junit.Test
  public void forExpr024() {
    final XQuery query = new XQuery(
      "for $file at $file in (//Folder)[1]/File return (string($file/FileName[1]), $file)",
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
      error("XQST0089")
    );
  }

  /**
   *  Redefine existing bound variable. Second binding overrides first .
   */
  @org.junit.Test
  public void forExpr025() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File, $file in (//Folder)[2]/File return $file/FileName",
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
      assertSerialization("<FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName><FileName>File00000000031</FileName>", false)
    );
  }

  /**
   *  Missing 'in' .
   */
  @org.junit.Test
  public void forExpr026() {
    final XQuery query = new XQuery(
      "for $file (//Folder)[1]/File return $file/FileName",
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
   *  Wrong order for positional variable .
   */
  @org.junit.Test
  public void forExpr027() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File at $pos return (string($file/FileName[1]), $pos)",
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
   *  FLWOR expression iterating over constructed XML .
   */
  @org.junit.Test
  public void forExpr028() {
    final XQuery query = new XQuery(
      "for $num in ( <one/>, <two/>, <three/> ) return $num",
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
      assertSerialization("<one/><two/><three/>", false)
    );
  }

  /**
   *  Embedded FLOWR expression that binds same variable on boths expressions (two for clauses) .
   */
  @org.junit.Test
  public void forExpr029() {
    final XQuery query = new XQuery(
      "for $var in (1,2) for $var in (2,2) return $var * $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4 4 4 4")
    );
  }

  /**
   *  Embedded FLOWR expression that binds same variable on boths expressions (use commas) .
   */
  @org.junit.Test
  public void forExpr030() {
    final XQuery query = new XQuery(
      "for $var in (1,2), $var in (2,2) return $var * $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "4 4 4 4")
    );
  }

  /**
   *  FLOWR expression that binds same variable on both ends of "at" .
   */
  @org.junit.Test
  public void forExpr031() {
    final XQuery query = new XQuery(
      "for $var at $var in (1, 2) return $var * $var",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      error("XQST0089")
    );
  }

  /**
   *  FLWOR expressions with type declaration (element type) .
   */
  @org.junit.Test
  public void forExprType001() {
    final XQuery query = new XQuery(
      "for $file as element(File,xs:untyped) in (//Folder)[1]/File return $file/FileName",
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
   *  FLWOR expressions with type declaration (attribute type) .
   */
  @org.junit.Test
  public void forExprType002() {
    final XQuery query = new XQuery(
      "for $fileName as attribute(name,xs:untypedAtomic) in (//Folder)[1]/File/@name return data($fileName)",
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
   *  FLWOR expressions with type declaration (PI type) .
   */
  @org.junit.Test
  public void forExprType003() {
    final XQuery query = new XQuery(
      "for $PI as processing-instruction() in //processing-instruction() return $PI",
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
      assertSerialization("<?FileSystem scale='0'?><?FileSystem maxDepth='64'?>", false)
    );
  }

  /**
   *  FLWOR expressions with type declaration (comment type) .
   */
  @org.junit.Test
  public void forExprType004() {
    final XQuery query = new XQuery(
      "for $comment as comment() in //comment() return $comment",
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
      assertSerialization("<!-- This is an official fsx file -->", false)
    );
  }

  /**
   *  FLWOR expressions with type declaration (simple type) .
   */
  @org.junit.Test
  public void forExprType005() {
    final XQuery query = new XQuery(
      "for $int as xs:int in (xs:int(1), xs:int(2)) return $int",
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
      assertStringValue(false, "1 2")
    );
  }

  /**
   *  Wrong type in type declaration .
   */
  @org.junit.Test
  public void forExprType006() {
    final XQuery query = new XQuery(
      "for $int as xs:string in (xs:int(1), xs:int(2)) return $int",
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
      error("XPTY0004")
    );
  }

  /**
   *  Wrong order for type declaration .
   */
  @org.junit.Test
  public void forExprType007() {
    final XQuery query = new XQuery(
      "for $file in (//Folder)[1]/File as element(Folder,xs:untypedAny) return $file/FileName",
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
   *  Wrong order for positional and typing parts of FLWOR expression .
   */
  @org.junit.Test
  public void forExprType008() {
    final XQuery query = new XQuery(
      "for $file at $pos as element(Folder,xs:untyped) in (//Folder)[1]/File return (string($file/FileName[1]), $pos)",
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
   *  Missing type declaration on type expression .
   */
  @org.junit.Test
  public void forExprType012() {
    final XQuery query = new XQuery(
      "for $file as in (//Folder)[1]/File return $file",
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
   *  Type declaration is a super type .
   */
  @org.junit.Test
  public void forExprType015() {
    final XQuery query = new XQuery(
      "for $num as xs:decimal in (xs:integer(1), xs:integer(2), xs:integer(3)) return $num",
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
      assertStringValue(false, "1 2 3")
    );
  }

  /**
   *  Type declaration is a super type of the union type bound to the variable .
   */
  @org.junit.Test
  public void forExprType016() {
    final XQuery query = new XQuery(
      "for $num as xs:decimal in (xs:integer(1), xs:decimal(2), xs:integer(3)) return $num",
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
      assertStringValue(false, "1 2 3")
    );
  }

  /**
   *  Bound sequence is a union type containing a single instance that does not match the type declaration .
   */
  @org.junit.Test
  public void forExprType017() {
    final XQuery query = new XQuery(
      "for $num as xs:double in (xs:int(\"1\"), xs:integer(\"2\"), xs:string(\"3\")) return $num",
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
      error("XPTY0004")
    );
  }

  /**
   *  Match a 'node' type .
   */
  @org.junit.Test
  public void forExprType018() {
    final XQuery query = new XQuery(
      "for $file as node() in (//Folder)[1]/File return $file/FileName",
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
   *  Match a text node type .
   */
  @org.junit.Test
  public void forExprType019() {
    final XQuery query = new XQuery(
      "for $fileName as text() in (//Folder)[1]/File/FileName/text() return string( $fileName )",
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
   *  Match a 'item' type .
   */
  @org.junit.Test
  public void forExprType020() {
    final XQuery query = new XQuery(
      "for $fileName as item() in data( (//Folder)[1]/File/FileName ) return $fileName",
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
   *  Match a 'document' type .
   */
  @org.junit.Test
  public void forExprType021() {
    final XQuery query = new XQuery(
      "for $doc as document-node() in (/) return count( $doc )",
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
      assertEq("1")
    );
  }

  /**
   *  FLWOR expressions with type declaration. Value subtype promotion: Numeric based types -> integer .
   */
  @org.junit.Test
  public void forExprType028() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $test as xs:integer in ( xs:byte(\"1\"), xs:long(\"10000\"), xs:negativeInteger(\"-10\"), \n" +
      "                        xs:short(\"100\"), xs:positiveInteger(\"100\"), xs:nonNegativeInteger(\"100\") ) \n" +
      "        return $test\n" +
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
      assertStringValue(false, "1 10000 -10 100 100 100")
    );
  }

  /**
   *  FLWOR expressions with type declaration. Value subtype promotion: Numeric based types -> decimal .
   */
  @org.junit.Test
  public void forExprType029() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $test as xs:decimal in ( xs:integer(\"100\"), xs:short(\"1\"), xs:int(\"10000\"), \n" +
      "                xs:nonPositiveInteger(\"-10\"), xs:unsignedShort(\"100\"), xs:positiveInteger(\"100\"), xs:nonNegativeInteger(\"100\") ) \n" +
      "        return $test\n" +
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
      assertStringValue(false, "100 1 10000 -10 100 100 100")
    );
  }

  /**
   *  FLWOR expressions with type declaration. Value subtype promotion: String based types -> normalizedString .
   */
  @org.junit.Test
  public void forExprType030() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $test as xs:normalizedString in ( xs:language(\"en-US\"), xs:NCName(\"foobar\"), \n" +
      "            xs:NMTOKEN(\"token1\"), xs:ENTITY(\"entity1\") ) \n" +
      "        return $test\n" +
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
      assertStringValue(false, "en-US foobar token1 entity1")
    );
  }

  /**
   *  FLWOR expressions with type declaration. Value subtype promotion: Time based types -> anyAtomicType .
   */
  @org.junit.Test
  public void forExprType031() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $test as xs:anyAtomicType in ( xs:duration(\"P0Y1347M0D\"), xs:dateTime(\"1999-05-31T13:20:00-05:00\"), \n" +
      "                    xs:time(\"13:00:00-05:00\"), xs:date(\"1999-05-10Z\"), xs:gYearMonth(\"2002-03\"), xs:gYear(\"1999\"), \n" +
      "                    xs:gMonthDay(\"--05-10\"), xs:gDay(\"---10\"), xs:gMonth(\"--05\") ) \n" +
      "        return $test\n" +
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
      assertStringValue(false, "P112Y3M 1999-05-31T13:20:00-05:00 13:00:00-05:00 1999-05-10Z 2002-03 1999 --05-10 ---10 --05")
    );
  }

  /**
   *  FLWOR expressions with type declaration. Value based subtype promotion: 2nd level derived types .
   */
  @org.junit.Test
  public void forExprType032() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $test as xs:anyAtomicType in ( xs:boolean(\"true\"), xs:base64Binary(\"abcd\"), xs:hexBinary(\"1234\"),\n" +
      "                     xs:float(\"5.7\"), xs:double(\"7.5\"), xs:anyURI(\"http://foo\") ) \n" +
      "        return $test\n" +
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
      assertStringValue(false, "true abcd 1234 5.7 7.5 http://foo")
    );
  }

  /**
   *  FLWOR expressions with type declaration. Value based subtype promotion: Types from all levels .
   */
  @org.junit.Test
  public void forExprType033() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $test as xs:anyAtomicType in ( xs:unsignedByte(\"10\"), xs:short(\"20\"), xs:positiveInteger(\"30\"), \n" +
      "                xs:nonPositiveInteger(\"-40\"), xs:decimal(\"5.5\"), xs:ENTITY(\"entity1\"), xs:NCName(\"foobar\"), \n" +
      "                xs:language(\"en-US\"), xs:string(\"foobar\"), xs:hexBinary(\"ffff\"), xs:gYear(\"1999\") ) \n" +
      "        return $test\n" +
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
      assertStringValue(false, "10 20 30 -40 5.5 entity1 foobar en-US foobar FFFF 1999")
    );
  }

  /**
   *  FLWOR expressions with type declaration. Value based subtype promotion: numeric literal values -> double .
   */
  @org.junit.Test
  public void forExprType034() {
    final XQuery query = new XQuery(
      "for $test as xs:decimal in ( 1, 0.3 ) return $test",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 0.3")
    );
  }

  /**
   *  FLWOR expressions with type declaration. Value based subtype promotion: string and numeric literal values -> anyAtomicType .
   */
  @org.junit.Test
  public void forExprType035() {
    final XQuery query = new XQuery(
      "for $test as xs:anyAtomicType in ( 1, \"string\", 1e2, 0.3 ) return $test",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 string 100 0.3")
    );
  }

  /**
   *  FLWOR expressions with type declaration. Type declaration matching item() values .
   */
  @org.junit.Test
  public void forExprType054() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as item() in data( (//fs:Folder)[1]/fs:File/fs:FileName ) return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "File00000000000 File00000000001 File00000000002")
    );
  }

  /**
   *  FLWOR expressions with type declaration. Type declaration matching node() values .
   */
  @org.junit.Test
  public void forExprType055() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as node() in (//fs:Folder)[1]/fs:File return $test/fs:FileName\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<fs:FileName xmlns:fs=\"http://www.example.com/filesystem\">File00000000000</fs:FileName><fs:FileName xmlns:fs=\"http://www.example.com/filesystem\">File00000000001</fs:FileName><fs:FileName xmlns:fs=\"http://www.example.com/filesystem\">File00000000002</fs:FileName>", false)
    );
  }

  /**
   *  FLWOR expressions with type declaration. Type declaration matching a document node .
   */
  @org.junit.Test
  public void forExprType056() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as document-node() in (/) return ($test//fs:Folder)[1]/fs:FolderName\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<fs:FolderName xmlns:fs=\"http://www.example.com/filesystem\">Folder00000000000</fs:FolderName>", false)
    );
  }

  /**
   *  FLWOR expressions with type declaration. Type declaration matcing text nodes .
   */
  @org.junit.Test
  public void forExprType057() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as text() in (//fs:Folder)[1]/fs:File/fs:FileName/text() return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("File00000000000File00000000001File00000000002", false)
    );
  }

  /**
   *  FLWOR expressions with type declaration. Type declaration matcing XML comment nodes .
   */
  @org.junit.Test
  public void forExprType058() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as comment() in (//fs:Folder)[1]/comment() return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<!-- This is a comment for File00000000000 --><!-- This is a comment for File00000000001 --><!-- This is a comment for File00000000002 -->", false)
    );
  }

  /**
   *  FLWOR expressions with type declaration. Type declaration matcing PI nodes .
   */
  @org.junit.Test
  public void forExprType059() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as processing-instruction() in (//fs:Folder)[1]//processing-instruction() return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<?filesystem compressionUtil='foozip.exe'?>", false)
    );
  }

  /**
   *  FLWOR expressions with type declaration. Type declaration matcing named PI nodes .
   */
  @org.junit.Test
  public void forExprType060() {
    final XQuery query = new XQuery(
      "declare namespace fs=\"http://www.example.com/filesystem\"; for $test as processing-instruction() in (//fs:Folder)[1]//processing-instruction() return $test",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<?filesystem compressionUtil='foozip.exe'?>", false)
    );
  }

  /**
   *  FLWOR expressions with type declaration. Type declaration matcing PI nodes - no matching PI found .
   */
  @org.junit.Test
  public void forExprType061() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as processing-instruction(\"unknown\") in (//fs:Folder)[1]//processing-instruction() return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
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
   *  FLWOR expressions with type declaration. Attempt type declaration of item() on a set of nodes .
   */
  @org.junit.Test
  public void forExprType062() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as item() in (//fs:Folder)[1]/fs:File/fs:FileName return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<fs:FileName xmlns:fs=\"http://www.example.com/filesystem\">File00000000000</fs:FileName><fs:FileName xmlns:fs=\"http://www.example.com/filesystem\">File00000000001</fs:FileName><fs:FileName xmlns:fs=\"http://www.example.com/filesystem\">File00000000002</fs:FileName>", false)
    );
  }

  /**
   *  FLWOR expressions with type declaration. Attempt type declaration of node() on a set of items .
   */
  @org.junit.Test
  public void forExprType063() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as node() in data( (//fs:Folder)[1]/fs:File/fs:FileName ) return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
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
   *  FLWOR expressions with type declaration. Attempt type declaration of text() on a set of element nodes .
   */
  @org.junit.Test
  public void forExprType064() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as text() in (//fs:Folder)[1]/fs:File/fs:FileName return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
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
   *  FLWOR expressions with type declaration. Attempt type declaration of text() on a set of values .
   */
  @org.junit.Test
  public void forExprType065() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as text() in data( (//fs:Folder)[1]/fs:File/fs:FileName ) return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
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
   *  FLWOR expressions with type declaration. Attempt type declaration of comment() on a set of element nodes .
   */
  @org.junit.Test
  public void forExprType066() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as comment() in (//fs:Folder)[1]/fs:File/fs:FileName return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
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
   *  FLWOR expressions with type declaration. Attempt type declaration of comment() on a set of values .
   */
  @org.junit.Test
  public void forExprType067() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as comment() in data( (//fs:Folder)[1]/fs:File/fs:FileName ) return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
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
   *  FLWOR expressions with type declaration. Attempt type declaration of processing-instruction() on a set of element nodes .
   */
  @org.junit.Test
  public void forExprType068() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as processing-instruction() in (//fs:Folder)[1]/fs:File/fs:FileName return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
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
   *  FLWOR expressions with type declaration. Attempt type declaration of processing-instruction() on a set of values .
   */
  @org.junit.Test
  public void forExprType069() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as processing-instruction() in data( (//fs:Folder)[1]/fs:File/fs:FileName ) return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
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
   *  FLWOR expressions with type declaration. Attempt type declaration of processing-instruction() with name specifier, on a set of element nodes .
   */
  @org.junit.Test
  public void forExprType070() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as processing-instruction(\"foo\") in (//fs:Folder)[1]/fs:File/fs:FileName return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
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
   *  FLWOR expressions with type declaration. Attempt type declaration of processing-instruction() with name specifier, on a set of values .
   */
  @org.junit.Test
  public void forExprType071() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as processing-instruction(\"foo\") in data( (//fs:Folder)[1]/fs:File/fs:FileName ) return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
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
   *  FLWOR expressions with type declaration. Attempt type declaration of document-node() on a set of element nodes .
   */
  @org.junit.Test
  public void forExprType072() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as document-node() in (//fs:Folder)[1]/fs:File/fs:FileName return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
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
   *  FLWOR expressions with type declaration. Attempt type declaration of document-node() on a set of values .
   */
  @org.junit.Test
  public void forExprType073() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare namespace fs=\"http://www.example.com/filesystem\"; \n" +
      "        for $test as document-node() in data( (//fs:Folder)[1]/fs:File/fs:FileName ) return $test\n" +
      "      ",
      ctx);
    try {
      query.context(node(file("prod/ForClause/fsx_NS.xml")));
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
   *  Syntactically invalid for-expression. .
   */
  @org.junit.Test
  public void kForExprWithout1() {
    final XQuery query = new XQuery(
      "for $foo in 1, $bar in 2, $moo in 3, return 4",
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
   *  A test whose essence is: `deep-equal((for $var in (1, 2, 3) return $var), (1, 2, 3))`. .
   */
  @org.junit.Test
  public void kForExprWithout10() {
    final XQuery query = new XQuery(
      "deep-equal((for $var in (1, 2, 3) return $var), (1, 2, 3))",
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
   *  A test whose essence is: `deep-equal((2, 2), (for $foo in (1, 1) return 1 + $foo))`. .
   */
  @org.junit.Test
  public void kForExprWithout11() {
    final XQuery query = new XQuery(
      "deep-equal((2, 2), (for $foo in (1, 1) return 1 + $foo))",
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
   *  A test whose essence is: `4 eq (for $foo in 1, $bar in 2, $moo in 3 return 4)`. .
   */
  @org.junit.Test
  public void kForExprWithout12() {
    final XQuery query = new XQuery(
      "4 eq (for $foo in 1, $bar in 2, $moo in 3 return 4)",
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
   *  A test whose essence is: `3 eq (for $foo in 1 return for $bar in 2 return $bar + $foo)`. .
   */
  @org.junit.Test
  public void kForExprWithout13() {
    final XQuery query = new XQuery(
      "3 eq (for $foo in 1 return for $bar in 2 return $bar + $foo)",
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
   *  A test whose essence is: `3 eq (for $foo in 1 return for $bar in 2 return $bar + $foo)`. .
   */
  @org.junit.Test
  public void kForExprWithout14() {
    final XQuery query = new XQuery(
      "3 eq (for $foo in 1 return for $bar in 2 return $bar + $foo)",
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
   *  A test whose essence is: `3 eq (for $foo in 1, $foo in 3 return $foo)`. .
   */
  @org.junit.Test
  public void kForExprWithout15() {
    final XQuery query = new XQuery(
      "3 eq (for $foo in 1, $foo in 3 return $foo)",
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
   *  A test whose essence is: `8 eq (for $foo in 1, $foo in 3, $moo in 5 return $moo + $foo)`. .
   */
  @org.junit.Test
  public void kForExprWithout16() {
    final XQuery query = new XQuery(
      "8 eq (for $foo in 1, $foo in 3, $moo in 5 return $moo + $foo)",
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
   *  A test whose essence is: `6 eq (for $foo in 1, $foo in 3 return $foo + $foo)`. .
   */
  @org.junit.Test
  public void kForExprWithout17() {
    final XQuery query = new XQuery(
      "6 eq (for $foo in 1, $foo in 3 return $foo + $foo)",
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
   *  A for variable binding to the empty sequence. .
   */
  @org.junit.Test
  public void kForExprWithout18() {
    final XQuery query = new XQuery(
      "empty(for $var in () return current-time())",
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
   *  A for variable binding to the empty sequence. .
   */
  @org.junit.Test
  public void kForExprWithout19() {
    final XQuery query = new XQuery(
      "empty(for $var in () return 1)",
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
   *  Syntactically invalid for-expression. .
   */
  @org.junit.Test
  public void kForExprWithout2() {
    final XQuery query = new XQuery(
      "for in 1 return 4",
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
   *  A for variable binding to the empty sequence, combined with value comparison. .
   */
  @org.junit.Test
  public void kForExprWithout20() {
    final XQuery query = new XQuery(
      "empty((for $var in () return current-time()))",
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
   *  A for variable binding to the empty sequence, combined with value comparison. .
   */
  @org.junit.Test
  public void kForExprWithout21() {
    final XQuery query = new XQuery(
      "empty((for $var in () return 1) eq 1)",
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
   *  A for variable binding to the empty sequence, combined with value comparison. .
   */
  @org.junit.Test
  public void kForExprWithout22() {
    final XQuery query = new XQuery(
      "empty(for $var in subsequence((current-time(), 1), 4) return 1)",
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
   *  An for-return expression which only is the binding variable. .
   */
  @org.junit.Test
  public void kForExprWithout23() {
    final XQuery query = new XQuery(
      "deep-equal(remove(for $var in (current-time(), 2, 3, 4) return $var, 1), (2, 3, 4))",
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
   *  A test whose essence is: `(for $fn:name in (1, 1) return $fn:name) instance of xs:integer+`. .
   */
  @org.junit.Test
  public void kForExprWithout24() {
    final XQuery query = new XQuery(
      "(for $fn:name in (1, 1) return $fn:name) instance of xs:integer+",
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
   *  A test whose essence is: `(for $xs:name in (1, 1) return $xs:name) instance of xs:integer+`. .
   */
  @org.junit.Test
  public void kForExprWithout25() {
    final XQuery query = new XQuery(
      "(for $xs:name in (1, 1) return $xs:name) instance of xs:integer+",
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout26() {
    final XQuery query = new XQuery(
      "$NOTEXIST",
      ctx);
    try {
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout27() {
    final XQuery query = new XQuery(
      "$PREFIXNOTEXIST:NOTEXIST",
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout28() {
    final XQuery query = new XQuery(
      "$xs:NOTEXIST",
      ctx);
    try {
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout29() {
    final XQuery query = new XQuery(
      "1 + $NOTEXIST",
      ctx);
    try {
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
   *  Syntactically invalid for-expression. .
   */
  @org.junit.Test
  public void kForExprWithout3() {
    final XQuery query = new XQuery(
      "for return 4",
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout30() {
    final XQuery query = new XQuery(
      "1 + $prefix:NOTEXIST",
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout31() {
    final XQuery query = new XQuery(
      "1 + $xs:NOTEXIST",
      ctx);
    try {
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout32() {
    final XQuery query = new XQuery(
      "$NOTEXIST + 1",
      ctx);
    try {
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout33() {
    final XQuery query = new XQuery(
      "$PREFIXNOTEXIST:NOTEXIST + 1",
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout34() {
    final XQuery query = new XQuery(
      "$xs:NOTEXIST + 1",
      ctx);
    try {
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout35() {
    final XQuery query = new XQuery(
      "for $foo in 1 return $NOTEXIST",
      ctx);
    try {
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout36() {
    final XQuery query = new XQuery(
      "for $foo in (1, 2, $foo) return 1",
      ctx);
    try {
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout37() {
    final XQuery query = new XQuery(
      "for $foo in (1, $foo, 3) return 1",
      ctx);
    try {
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout38() {
    final XQuery query = new XQuery(
      "for $foo in ($foo, 2, 3) return 1",
      ctx);
    try {
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout39() {
    final XQuery query = new XQuery(
      "for $foo in $foo return 1",
      ctx);
    try {
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
   *  Syntactically invalid for-expression. .
   */
  @org.junit.Test
  public void kForExprWithout4() {
    final XQuery query = new XQuery(
      "for $a in 1 satisfies 4",
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout40() {
    final XQuery query = new XQuery(
      "for $foo in 1 return $bar + (for $bar in 2 return $bar)",
      ctx);
    try {
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout41() {
    final XQuery query = new XQuery(
      "for $a in (1, 2, 3), $b in (1, 2, 3, $b) return $a, $b",
      ctx);
    try {
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout42() {
    final XQuery query = new XQuery(
      "for $a in (1, 2), $b in (1, 2), $c in (1, 2) return 1, $a",
      ctx);
    try {
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout43() {
    final XQuery query = new XQuery(
      "for $a in (1, 2) return 1, $a",
      ctx);
    try {
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout44() {
    final XQuery query = new XQuery(
      "for $a in (1, 2), $b in (1, 2), $c in (1, 2) return 1, $b",
      ctx);
    try {
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
   *  Variable which is not in scope. .
   */
  @org.junit.Test
  public void kForExprWithout45() {
    final XQuery query = new XQuery(
      "for $a in (1, 2), $b in (1, 2), $c in (1, 2) return 1, $c",
      ctx);
    try {
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
   *  Type check: $foo is of type xs:string, which cannot be added to xs:integer 1. .
   */
  @org.junit.Test
  public void kForExprWithout46() {
    final XQuery query = new XQuery(
      "for $foo in \"foo\" return 1 + $foo",
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
   *  A variable's for expression causes type error in a value comparison. .
   */
  @org.junit.Test
  public void kForExprWithout47() {
    final XQuery query = new XQuery(
      "for $foo in (\"a string\", \"another one\") return 1 + subsequence($foo, 1, 1)",
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
   *  Parser test: variable names in a for expression that only exist of one character are valid. .
   */
  @org.junit.Test
  public void kForExprWithout48() {
    final XQuery query = new XQuery(
      "1 eq (for $a in 1 return $a)",
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
   *  For-expression involving a simple return statement that in some implementations trigger optimization paths. .
   */
  @org.junit.Test
  public void kForExprWithout49() {
    final XQuery query = new XQuery(
      "deep-equal((for $fo in trace((1, 2, 3), \"msg\") return $fo), (1, 2, 3))",
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
   *  A test whose essence is: `deep-equal(1 to 5, (for $i in (1,2,3,4,5) return $i))`. .
   */
  @org.junit.Test
  public void kForExprWithout5() {
    final XQuery query = new XQuery(
      "deep-equal(1 to 5, (for $i in (1,2,3,4,5) return $i))",
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
   *  Ensure scanners/parsers accept small QNames in for-expressions. .
   */
  @org.junit.Test
  public void kForExprWithout50() {
    final XQuery query = new XQuery(
      "1 eq (for $a in 1 return $a)",
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
   *  Ensure scanners/parsers accept small QNames in for-expressions(#2). .
   */
  @org.junit.Test
  public void kForExprWithout51() {
    final XQuery query = new XQuery(
      "1 eq (for $xs:a in 1 return $xs:a)",
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
   *  A binding in a for-expression shadows global variables. .
   */
  @org.junit.Test
  public void kForExprWithout52() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare variable $i := false(); \n" +
      "        declare variable $t := false(); \n" +
      "        deep-equal( for $i in (true(), true()), $t in (true(), true()) \n" +
      "                    return ($i, $t), (true(), true(), true(), true(), true(), true(), true(), true()))",
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
   *  Ensure the correct variable is used in an for-expression whose return sequence is only a variable reference. .
   */
  @org.junit.Test
  public void kForExprWithout53() {
    final XQuery query = new XQuery(
      "declare variable $my := 3; (for $i in 1 return $my) eq 3",
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
   *  Ensure the correct variable is used in an for-expression whose return sequence is only a variable reference(#2). .
   */
  @org.junit.Test
  public void kForExprWithout54() {
    final XQuery query = new XQuery(
      "declare variable $i := 3; (for $i in 1 return $i) eq 1",
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
   *  Apply fn:count() to a for-expression. .
   */
  @org.junit.Test
  public void kForExprWithout55() {
    final XQuery query = new XQuery(
      "count(for $i in (1, 2, current-time()) return ($i, $i)) eq 6",
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
   *  Apply fn:count() to a for-expression(#2). .
   */
  @org.junit.Test
  public void kForExprWithout56() {
    final XQuery query = new XQuery(
      "count(for $i in (1, 2, timezone-from-time(current-time())) return ($i, $i)) eq 6 or count(for $i in (1, 2, timezone-from-time(current-time())) return ($i, $i)) eq 4",
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
   *  A test whose essence is: `3 eq (for $foo in 1 return 3)`. .
   */
  @org.junit.Test
  public void kForExprWithout6() {
    final XQuery query = new XQuery(
      "3 eq (for $foo in 1 return 3)",
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
   *  A test whose essence is: `1 eq (for $foo in 1 return $foo)`. .
   */
  @org.junit.Test
  public void kForExprWithout7() {
    final XQuery query = new XQuery(
      "1 eq (for $foo in 1 return $foo)",
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
   *  A test whose essence is: `2 eq (for $foo in 1 return $foo + 1)`. .
   */
  @org.junit.Test
  public void kForExprWithout8() {
    final XQuery query = new XQuery(
      "2 eq (for $foo in 1 return $foo + 1)",
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
   *  A test whose essence is: `3 eq (for $foo in 1 return for $foo in 3 return $foo)`. .
   */
  @org.junit.Test
  public void kForExprWithout9() {
    final XQuery query = new XQuery(
      "3 eq (for $foo in 1 return for $foo in 3 return $foo)",
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
   *  For declarations does not cause type promotions to apply. .
   */
  @org.junit.Test
  public void k2ForExprWith1() {
    final XQuery query = new XQuery(
      "for $i as xs:string in xs:untypedAtomic(\"input\") return $i",
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
   *  Keywords are case sensitive. .
   */
  @org.junit.Test
  public void k2ForExprWithout1() {
    final XQuery query = new XQuery(
      "FOR $i IN (1, 2, 3)",
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
   *  Make use of many syntactical combinations. .
   */
  @org.junit.Test
  public void k2ForExprWithout10() {
    final XQuery query = new XQuery(
      "\n" +
      "        for $a in (3, 2, 1) \n" +
      "        for $b in (6, 5, 4) \n" +
      "        let $c := $a + $b \n" +
      "        let $d := $a - $b \n" +
      "        let $e := () stable order by $a ascending, $b descending, $d empty greatest, $e empty least, $c descending empty greatest, $d ascending empty greatest, $e descending empty least, $b ascending empty least \n" +
      "        return $a",
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
        assertStringValue(false, "1 1 1 2 2 2 3 3 3")
      ||
        error("XPST0005")
      )
    );
  }

  /**
   *  The return expression does not depend on the for iteration. .
   */
  @org.junit.Test
  public void k2ForExprWithout11() {
    final XQuery query = new XQuery(
      "for $a in (3, 2, 1, 1) stable order by $a return 5",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "5 5 5 5")
    );
  }

  /**
   *  The order by expression does not depend on the for iteration. .
   */
  @org.junit.Test
  public void k2ForExprWithout12() {
    final XQuery query = new XQuery(
      "for $a in (3, 2, 1, 1) stable order by 1 return $a",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "3 2 1 1")
    );
  }

  /**
   *  Use the focus from within a for-expression. .
   */
  @org.junit.Test
  public void k2ForExprWithout13() {
    final XQuery query = new XQuery(
      "<e/>/(for $i in self::node() return $i)",
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
   *  Use the focus from within a for tail-expression. .
   */
  @org.junit.Test
  public void k2ForExprWithout14() {
    final XQuery query = new XQuery(
      "<e/>/(for $i in 1, $b in self::node() return $b)",
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
   *  Use a name test named 'node' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout15() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/element return $n }; 1",
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
   *  Use a name test named 'document' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout16() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/document return $n }; 1",
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
   *  Use a name test named 'document' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout17() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/document-node return $n }; 1",
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
   *  Use a name test named 'attribute' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout18() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/attribute return $n }; 1",
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
   *  Use a name test named 'comment' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout19() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/comment return $n }; 1",
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
   *  A for-expression doesn't create a focus. .
   */
  @org.junit.Test
  public void k2ForExprWithout2() {
    final XQuery query = new XQuery(
      "\n" +
      "        declare function local:myFunc() { for $i in (1, 2, 3) return position() }; \n" +
      "        local:myFunc()",
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
   *  Use a name test named 'processing-instruction' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout20() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/processing-instruction return $n }; 1",
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
   *  Use a name test named 'processing-instruction' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout21() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/processing-instruction return $n }; 1",
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
   *  Use a name test named 'text' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout22() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/text return $n }; 1",
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
   *  Use a name test named 'typeswitch' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout23() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/typeswitch return $n }; 1",
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
   *  Use a name test named 'if' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout24() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/if return $n }; 1",
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
   *  Use a name test named 'for' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout25() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/for return $n }; 1",
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
   *  Use a name test named 'let' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout26() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/let return $n }; 1",
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
   *  Use a name test named 'declare' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout27() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/declare return $n }; 1",
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
   *  Use a name test named 'some' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout28() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/some return $n }; 1",
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
   *  Use a name test named 'child' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout29() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/child return $n }; 1",
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
   *  A for-expression doesn't create a focus(#2). .
   */
  @org.junit.Test
  public void k2ForExprWithout3() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { for $i in (1, 2, 3) return position() }; 1",
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
        assertEq("1")
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   *  Use a name test named 'ordered' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout30() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/ordered return $n }; 1",
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
   *  Use a name test named 'unordered' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout31() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/unordered return $n }; 1",
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
   *  Use a name test named 'schema-attribute' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout32() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/schema-attribute return $n }; 1",
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
   *  Use a name test named 'schema-attribute' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout33() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/schema-element return $n }; 1",
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
   *  Use a name test named 'item' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout34() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/item return $n }; 1",
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
   *  Use a name test named 'following-sibling' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout35() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/following-sibling return $n }; 1",
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
   *  Use a name test named 'validate' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout36() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/validate return $n }; 1",
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
   *  Use a name test named 'instance' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout37() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/instance return $n }; 1",
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
   *  Use a name test named 'castable' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout38() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/castable return $n }; 1",
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
   *  Use a name test named 'import' inside a for loop, inside a user function. .
   */
  @org.junit.Test
  public void k2ForExprWithout39() {
    final XQuery query = new XQuery(
      "declare function local:func($arg as element()* ) as element()* { for $n in $arg/import return $n }; 1",
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
   *  A for-expression with a positional variable doesn't create a focus. .
   */
  @org.junit.Test
  public void k2ForExprWithout4() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { for $i at $p in (1, 2, 3) return position() }; local:myFunc()",
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
   *  Use a name test named 'node' inside a for loop. .
   */
  @org.junit.Test
  public void k2ForExprWithout40() {
    final XQuery query = new XQuery(
      "for $n in node return 1",
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
   *  Use a name test named 'document' inside a for loop. .
   */
  @org.junit.Test
  public void k2ForExprWithout41() {
    final XQuery query = new XQuery(
      "for $n in document return 1",
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
   *  Use a computed attribute constructor with name return, inside a for loop. .
   */
  @org.junit.Test
  public void k2ForExprWithout42() {
    final XQuery query = new XQuery(
      "for $n in attribute return {()} return 1",
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
   *  Use a computed element constructor with name return, inside a for loop. .
   */
  @org.junit.Test
  public void k2ForExprWithout43() {
    final XQuery query = new XQuery(
      "for $n in element return {()} return 1",
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
   *  Use a computed processing-instruction constructor with name return, inside a for loop. .
   */
  @org.junit.Test
  public void k2ForExprWithout44() {
    final XQuery query = new XQuery(
      "for $n in processing-instruction return {()} return 1",
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
   *  Use name test 'attribute' constructor with name return, inside a for loop. .
   */
  @org.junit.Test
  public void k2ForExprWithout45() {
    final XQuery query = new XQuery(
      "for $n in attribute return 1",
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
        assertEq("1")
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   *  A for-expression with a positional variable doesn't create a focus(#2). .
   */
  @org.junit.Test
  public void k2ForExprWithout5() {
    final XQuery query = new XQuery(
      "declare function local:myFunc() { for $i at $p in (1, 2, 3) return position() }; 1",
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
        assertEq("1")
      ||
        error("XPDY0002")
      )
    );
  }

  /**
   *  A query that performs up-transformation of dates. .
   */
  @org.junit.Test
  public void k2ForExprWithout6() {
    final XQuery query = new XQuery(
      "for $ti in <ti><rangeDate><initDate>17880505</initDate><terminatingDate>19550505</terminatingDate></rangeDate></ti> return <result> { fn:min(if ($ti/singleDate) then let $tmp:=$ti/singleDate/compute return xs:date(fn:concat(fn:substring($tmp, 1, 4), \"-\", fn:substring($tmp, 5, 2), \"-\", fn:substring($tmp, 7, 2))) else let $tmp:=$ti/rangeDate/initDate return xs:date(fn:concat(fn:substring($tmp, 1, 4), \"-\", fn:substring($tmp, 5, 2), \"-\", fn:substring($tmp, 7, 2)))) } </result>",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<result>1788-05-05</result>", false)
    );
  }

  /**
   *  Ensure use of the undefined focus gets flagged. .
   */
  @org.junit.Test
  public void k2ForExprWithout7() {
    final XQuery query = new XQuery(
      "for $d in person return $d",
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
   *  Wrap a path expression. .
   */
  @org.junit.Test
  public void k2ForExprWithout8() {
    final XQuery query = new XQuery(
      "(for $d in <people> <person id=\"person0\"> <name>First</name> </person> <person> <name>Second</name> </person> </people>/person return if (some $id in 1 satisfies typeswitch ($d/@id) case $n as node() return $d/@id = \"person0\" default $d return ()) then $d/name else ())",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertSerialization("<name>First</name>", false)
    );
  }

  /**
   *  Ensure positional variables and bindings handles nesting. .
   */
  @org.junit.Test
  public void k2ForExprWithout9() {
    final XQuery query = new XQuery(
      "for $a at $pos1 in (1, 2, 3) return for $b at $pos2 in (4, 5, 6) return for $c at $pos3 in (7, 8, 9) return ($a, $pos1, $b, $pos2, $c, $pos2)",
      ctx);
    try {
      result = new QT3Result(query.value());
    } catch(final Throwable trw) {
      result = new QT3Result(trw);
    } finally {
      query.close();
    }
    test(
      assertStringValue(false, "1 1 4 1 7 1 1 1 4 1 8 1 1 1 4 1 9 1 1 1 5 2 7 2 1 1 5 2 8 2 1 1 5 2 9 2 1 1 6 3 7 3 1 1 6 3 8 3 1 1 6 3 9 3 2 2 4 1 7 1 2 2 4 1 8 1 2 2 4 1 9 1 2 2 5 2 7 2 2 2 5 2 8 2 2 2 5 2 9 2 2 2 6 3 7 3 2 2 6 3 8 3 2 2 6 3 9 3 3 3 4 1 7 1 3 3 4 1 8 1 3 3 4 1 9 1 3 3 5 2 7 2 3 3 5 2 8 2 3 3 5 2 9 2 3 3 6 3 7 3 3 3 6 3 8 3 3 3 6 3 9 3")
    );
  }
}
