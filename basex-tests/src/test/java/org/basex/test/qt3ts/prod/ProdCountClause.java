package org.basex.test.qt3ts.prod;

import org.basex.tests.bxapi.XQuery;
import org.basex.test.qt3ts.QT3TestSet;

/**
 * Tests for the CountClause production in XQuery 3.0.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@SuppressWarnings("all")
public class ProdCountClause extends QT3TestSet {

  /**
   * simple count clause.
   */
  @org.junit.Test
  public void count001() {
    final XQuery query = new XQuery(
      "\n" +
      "        <out>{ \n" +
      "\t\t  for $x in 1 to 10 \n" +
      "\t\t  count $j\n" +
      "\t\t  return \n" +
      "\t\t    <item><x>{$x}</x><j>{$j}</j></item> \n" +
      "\t\t}</out>\n" +
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
      assertSerialization("<out><item><x>1</x><j>1</j></item><item><x>2</x><j>2</j></item><item><x>3</x><j>3</j></item><item><x>4</x><j>4</j></item><item><x>5</x><j>5</j></item><item><x>6</x><j>6</j></item><item><x>7</x><j>7</j></item><item><x>8</x><j>8</j></item><item><x>9</x><j>9</j></item><item><x>10</x><j>10</j></item></out>", false)
    );
  }

  /**
   * count clause .
   */
  @org.junit.Test
  public void count002() {
    final XQuery query = new XQuery(
      "\n" +
      "        <out>{ \n" +
      "\t\t  for $x at $j in 1 to 10 \n" +
      "\t\t  return \n" +
      "\t\t    <item><x>{$x}</x><j>{$j}</j></item> \n" +
      "\t\t}</out>\n" +
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
      assertSerialization("<out><item><x>1</x><j>1</j></item><item><x>2</x><j>2</j></item><item><x>3</x><j>3</j></item><item><x>4</x><j>4</j></item><item><x>5</x><j>5</j></item><item><x>6</x><j>6</j></item><item><x>7</x><j>7</j></item><item><x>8</x><j>8</j></item><item><x>9</x><j>9</j></item><item><x>10</x><j>10</j></item></out>", false)
    );
  }

  /**
   * count clauses in a nested for loop.
   */
  @org.junit.Test
  public void count003() {
    final XQuery query = new XQuery(
      "\n" +
      "        <out>{ \n" +
      "\t\t  for $x in 1 to 4\n" +
      "\t\t  count $ix\n" +
      "\t\t  for $y in $x to 3\n" +
      "\t\t  count $iy \n" +
      "\t\t  return \n" +
      "\t\t    <item><x>{$x}</x><ix>{$ix}</ix><y>{$y}</y><iy>{$iy}</iy></item> \n" +
      "\t\t}</out>\n" +
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
      assertSerialization("<out><item><x>1</x><ix>1</ix><y>1</y><iy>1</iy></item><item><x>1</x><ix>1</ix><y>2</y><iy>2</iy></item><item><x>1</x><ix>1</ix><y>3</y><iy>3</iy></item><item><x>2</x><ix>2</ix><y>2</y><iy>4</iy></item><item><x>2</x><ix>2</ix><y>3</y><iy>5</iy></item><item><x>3</x><ix>3</ix><y>3</y><iy>6</iy></item></out>", false)
    );
  }

  /**
   * count clauses in a nested for loop with allowing empty.
   */
  @org.junit.Test
  public void count004() {
    final XQuery query = new XQuery(
      "\n" +
      "        <out>{ \n" +
      "\t\t  for $x in 1 to 4\n" +
      "\t\t  count $ix\n" +
      "\t\t  for $y allowing empty in $x to 3\n" +
      "\t\t  count $iy \n" +
      "\t\t  return \n" +
      "\t\t    <item><x>{$x}</x><ix>{$ix}</ix><y>{$y}</y><iy>{$iy}</iy></item> \n" +
      "\t\t}</out>\n" +
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
      assertSerialization("<out><item><x>1</x><ix>1</ix><y>1</y><iy>1</iy></item><item><x>1</x><ix>1</ix><y>2</y><iy>2</iy></item><item><x>1</x><ix>1</ix><y>3</y><iy>3</iy></item><item><x>2</x><ix>2</ix><y>2</y><iy>4</iy></item><item><x>2</x><ix>2</ix><y>3</y><iy>5</iy></item><item><x>3</x><ix>3</ix><y>3</y><iy>6</iy></item><item><x>4</x><ix>4</ix><y/><iy>7</iy></item></out>", false)
    );
  }

  /**
   * Use of a count clause in a filter.
   */
  @org.junit.Test
  public void count005() {
    final XQuery query = new XQuery(
      "\n" +
      "        <out>{ \n" +
      "\t\t  for $x in 1 to 5\n" +
      "\t\t  for $y in 1 to 5\n" +
      "\t\t  count $index\n" +
      "\t\t  where $index mod 3 = 0\n" +
      "\t\t  return \n" +
      "\t\t    <item><x>{$x}</x><y>{$y}</y><index>{$index}</index></item> \n" +
      "\t\t}</out>\n" +
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
      assertSerialization("<out><item><x>1</x><y>3</y><index>3</index></item><item><x>2</x><y>1</y><index>6</index></item><item><x>2</x><y>4</y><index>9</index></item><item><x>3</x><y>2</y><index>12</index></item><item><x>3</x><y>5</y><index>15</index></item><item><x>4</x><y>3</y><index>18</index></item><item><x>5</x><y>1</y><index>21</index></item><item><x>5</x><y>4</y><index>24</index></item></out>", false)
    );
  }

  /**
   * Use of a count clause in a filter.
   */
  @org.junit.Test
  public void count006() {
    final XQuery query = new XQuery(
      "\n" +
      "        <out>{ \n" +
      "\t\t  for $x in 1 to 5\n" +
      "\t\t  for $y in 1 to 5\n" +
      "\t\t  count $index\n" +
      "\t\t  where $index mod 3 = 0\n" +
      "\t\t  count $index2\n" +
      "\t\t  return \n" +
      "\t\t    <item><x>{$x}</x><y>{$y}</y><index2>{$index2}</index2></item> \n" +
      "\t\t}</out>\n" +
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
      assertSerialization("<out><item><x>1</x><y>3</y><index2>1</index2></item><item><x>2</x><y>1</y><index2>2</index2></item><item><x>2</x><y>4</y><index2>3</index2></item><item><x>3</x><y>2</y><index2>4</index2></item><item><x>3</x><y>5</y><index2>5</index2></item><item><x>4</x><y>3</y><index2>6</index2></item><item><x>5</x><y>1</y><index2>7</index2></item><item><x>5</x><y>4</y><index2>8</index2></item></out>", false)
    );
  }

  /**
   * Re-assigning a count variable name.
   */
  @org.junit.Test
  public void count007() {
    final XQuery query = new XQuery(
      "\n" +
      "        <out>{ \n" +
      "\t\t  for $x in 1 to 5\n" +
      "\t\t  for $y in 1 to 5\n" +
      "\t\t  count $index\n" +
      "\t\t  where $index mod 3 = 0\n" +
      "\t\t  count $index\n" +
      "\t\t  return \n" +
      "\t\t    <item><x>{$x}</x><y>{$y}</y><index>{$index}</index></item> \n" +
      "\t\t}</out>\n" +
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
      assertSerialization("<out><item><x>1</x><y>3</y><index>1</index></item><item><x>2</x><y>1</y><index>2</index></item><item><x>2</x><y>4</y><index>3</index></item><item><x>3</x><y>2</y><index>4</index></item><item><x>3</x><y>5</y><index>5</index></item><item><x>4</x><y>3</y><index>6</index></item><item><x>5</x><y>1</y><index>7</index></item><item><x>5</x><y>4</y><index>8</index></item></out>", false)
    );
  }

  /**
   * Re-assigning a count variable name.
   */
  @org.junit.Test
  public void count008() {
    final XQuery query = new XQuery(
      "\n" +
      "        <out>{ \n" +
      "\t\t  for $x in 1 to 2\n" +
      "\t\t  for $y in 1 to 3\n" +
      "\t\t  let $index := $y\n" +
      "\t\t  count $index\n" +
      "\t\t  return \n" +
      "\t\t    <item><x>{$x}</x><y>{$y}</y><index>{$index}</index></item> \n" +
      "\t\t}</out>\n" +
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
      assertSerialization("<out><item><x>1</x><y>1</y><index>1</index></item><item><x>1</x><y>2</y><index>2</index></item><item><x>1</x><y>3</y><index>3</index></item><item><x>2</x><y>1</y><index>4</index></item><item><x>2</x><y>2</y><index>5</index></item><item><x>2</x><y>3</y><index>6</index></item></out>", false)
    );
  }

  /**
   * Using a count clause with order-by.
   */
  @org.junit.Test
  public void count009() {
    final XQuery query = new XQuery(
      "\n" +
      "        <out>{ \n" +
      "\t\t  for $x in 1 to 4\n" +
      "\t\t  for $y in 1 to 4\n" +
      "\t\t  count $index\n" +
      "\t\t  let $remainder := $index mod 3\n" +
      "\t\t  order by $remainder, $index\n" +
      "\t\t  count $index2\n" +
      "\t\t  return \n" +
      "\t\t    <item><x>{$x}</x><y>{$y}</y><remainder>{$remainder}</remainder><index2>{$index2}</index2></item> \n" +
      "\t\t}</out>\n" +
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
      assertSerialization("<out><item><x>1</x><y>3</y><remainder>0</remainder><index2>1</index2></item><item><x>2</x><y>2</y><remainder>0</remainder><index2>2</index2></item><item><x>3</x><y>1</y><remainder>0</remainder><index2>3</index2></item><item><x>3</x><y>4</y><remainder>0</remainder><index2>4</index2></item><item><x>4</x><y>3</y><remainder>0</remainder><index2>5</index2></item><item><x>1</x><y>1</y><remainder>1</remainder><index2>6</index2></item><item><x>1</x><y>4</y><remainder>1</remainder><index2>7</index2></item><item><x>2</x><y>3</y><remainder>1</remainder><index2>8</index2></item><item><x>3</x><y>2</y><remainder>1</remainder><index2>9</index2></item><item><x>4</x><y>1</y><remainder>1</remainder><index2>10</index2></item><item><x>4</x><y>4</y><remainder>1</remainder><index2>11</index2></item><item><x>1</x><y>2</y><remainder>2</remainder><index2>12</index2></item><item><x>2</x><y>1</y><remainder>2</remainder><index2>13</index2></item><item><x>2</x><y>4</y><remainder>2</remainder><index2>14</index2></item><item><x>3</x><y>3</y><remainder>2</remainder><index2>15</index2></item><item><x>4</x><y>2</y><remainder>2</remainder><index2>16</index2></item></out>", false)
    );
  }

  /**
   * Count is reset on each execution of a FLWOR expression.
   */
  @org.junit.Test
  public void count010() {
    final XQuery query = new XQuery(
      "\n" +
      "\t\t  for $x in 1 to 4 return\n" +
      "\t\t    for $y in 1 to 4\n" +
      "\t\t    count $index\n" +
      "\t\t    return $index\n" +
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
      assertStringValue(false, "1 2 3 4 1 2 3 4 1 2 3 4 1 2 3 4")
    );
  }
}
