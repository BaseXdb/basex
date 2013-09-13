package org.basex.test;

import static org.basex.query.util.Err.*;
import static org.junit.Assert.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.expath.ns.*;
import org.junit.*;

/**
 * This class tests the XQuery Geo functions prefixed with "geo".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Masoumeh Seydi
 */
public final class GeoTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void dimension() {
    runQuery("geo:dimension(" +
            "<gml:Point><gml:coordinates>1,2</gml:coordinates></gml:Point>)", "0");

    runError("geo:dimension(text {'a'})", FUNCMP.qname());
    runError("geo:dimension(<gml:unknown/>)", GeoErrors.qname(1));
    runError("geo:dimension(<gml:Point>" +
            "<gml:coordinates>1 2</gml:coordinates></gml:Point>)",
            GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void geometryType() {
    runQuery("geo:geometryType(<gml:MultiPoint><gml:Point>" +
            "<gml:coordinates>1,1</gml:coordinates></gml:Point>" +
            "<gml:Point><gml:coordinates>1,2</gml:coordinates></gml:Point>" +
            "</gml:MultiPoint>)", "gml:MultiPoint");

    runError("geo:geometryType(text {'srsName'})", FUNCMP.qname());
    runError("geo:geometryType(<gml:unknown/>)", GeoErrors.qname(1));
    runError("geo:geometryType(<gml:Point><gml:coordinates>1 2</gml:coordinates>" +
            "</gml:Point>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void srid() {
    runQuery("geo:srid(<gml:Polygon srsName=" +
            "\"http://www.opengis.net/gml/srs/epsg.xml#4326\">" +
            "<outerboundaryIs><gml:LinearRing><coordinates>" +
            "-150,50 -150,60 -125,60 -125,50 -150,50" +
            "</coordinates></gml:LinearRing></outerboundaryIs></gml:Polygon>)", "0");

    runError("geo:srid(text {'a'})", FUNCMP.qname());
    runError("geo:srid(<gml:unknown/>)", GeoErrors.qname(1));
    runError("geo:srid(<gml:LinearRing><gml:pos>1,1 20,1 50,30 1,1</gml:pos>" +
            "</gml:LinearRing>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void envelope() {
    runQuery("geo:envelope(<gml:LinearRing><gml:coordinates>1,1 20,1 50,30 1,1" +
            "</gml:coordinates></gml:LinearRing>)",
            "<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>1.0,1.0 1.0,30.0 50.0,30.0 50.0,1.0 1.0,1.0" +
            "</gml:coordinates></gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>");

    runError("geo:envelope(text {'a'})", FUNCMP.qname());
    runError("geo:envelope(<gml:unknown/>)", GeoErrors.qname(1));
    runError("geo:envelope(<gml:LinearRing><gml:pos>1,1 20,1 50,30 1,1</gml:pos>" +
            "</gml:LinearRing>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void asText() {
    runQuery("geo:asText(<gml:LineString><gml:coordinates>1,1 55,99 2,1" +
            "</gml:coordinates></gml:LineString>)", "LINESTRING (1 1, 55 99, 2 1)");

    runError("geo:asText(text {'a'})", FUNCMP.qname());
    runError("geo:asText(<gml:unknown/>)", GeoErrors.qname(1));
    runError("geo:asText(<gml:LineString><gml:coordinates>1,1</gml:coordinates>" +
            "</gml:LineString>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void asBinary() {
    runQuery("geo:asBinary(<gml:LineString><gml:coordinates>1,1 55,99 2,1" +
            "</gml:coordinates></gml:LineString>)",
          "AAAAAAIAAAADP/AAAAAAAAA/8AAAAAAAAEBLgAAAAAAAQFjAAAAAAABAAAAAAAAAAD/wAAAAAAAA");

    runError("geo:asBinary(text {'a'})", FUNCMP.qname());
    runError("geo:asBinary(<gml:unknown/>)", GeoErrors.qname(1));
    runError("geo:asBinary(<gml:LinearRing><gml:coordinates>1,1 55,99 2,1" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void isSimple() {
    runQuery("geo:isSimple(<gml:LineString><gml:coordinates>1,1 20,1 10,4 20,-10" +
            "</gml:coordinates></gml:LineString>)", "false");

    runError("geo:isSimple(text {'a'})", FUNCMP.qname());
    runError("geo:isSimple(<gml:unknown/>)", GeoErrors.qname(1));
    runError("geo:isSimple(<gml:LinearRing><gml:coordinates>1,1 55,99 2,1" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void boundary() {
    runQuery("geo:boundary(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>)",
        "<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>11.0,11.0 18.0,11.0 18.0,18.0 11.0,18.0 " +
        "11.0,11.0</gml:coordinates></gml:LineString>");

    runQuery("geo:boundary(" +
        "<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
        "<gml:MultiGeometry xmlns:gml=\"http://www.opengis.net/gml\"/>");
    runError("geo:boundary(text {'a'})", FUNCMP.qname());
    runError("geo:boundary(a)", NOCTX.qname());
    runError("geo:boundary(<gml:geo/>)", GeoErrors.qname(1));
    runError("geo:boundary(<gml:Point><gml:pos>1 2</gml:pos></gml:Point>)",
        GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void equals() {
    runQuery("geo:equals(<gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1" +
            "</gml:coordinates></gml:LinearRing>, " +
            "<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates></gml:LinearRing>" +
            "</gml:outerBoundaryIs></gml:Polygon>)", "false");

    runError("geo:equals(text {'a'}, a)", NOCTX.qname());
    runError("geo:equals(<gml:unknown/>, <gml:LinearRing><gml:coordinates>" +
            "1,1 2,1 5,3 1,1</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(1));
    runError("geo:equals(<gml:LinearRing><gml:coordinates>1,1 55,99 2,1" +
            "</gml:coordinates></gml:LinearRing>," +
            "<gml:LineString><gml:coordinates>1,1 55,99 2,1" +
            "</gml:coordinates></gml:LineString>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void disjoint() {
    runQuery("geo:disjoint(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>, " +
            "<gml:LineString><gml:coordinates>0,0 2,1 3,3</gml:coordinates>" +
            "</gml:LineString>)", "false");

    runError("geo:disjoint(a, text {'a'})", NOCTX.qname());
    runError("geo:disjoint(<gml:Ring/>, " +
            "<gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates>" +
            "</gml:LinearRing>)", GeoErrors.qname(1));
    runError("geo:disjoint(<gml:LineString><gml:coordinates></gml:coordinates>" +
                  "</gml:LineString>)", FUNCARGSG.qname());
  }

  /** Test method. */
  @Test
  public void intersects() {
    runQuery("geo:intersects(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>, <gml:LineString><gml:coordinates>0,0 2,1 3,3" +
            "</gml:coordinates></gml:LineString>)", "true");

    runError("geo:intersects(a, text {'a'})", NOCTX.qname());
    runError("geo:intersects(<gml:Point><gml:coordinates>10,10 12,11</gml:coordinates>" +
            "</gml:Point>, <gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(2));
    runError("geo:intersects(<gml:Point><gml:coordinates>1,1</gml:coordinates>" +
            "</gml:Point>, <gml:Line><gml:coordinates>0,0 2,1 3,3</gml:coordinates>" +
            "</gml:Line>)", GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void touches() {
    runQuery("geo:touches(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
        "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
        "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
        "</gml:MultiLineString>, <gml:LineString><gml:coordinates>0,0 2,1 3,3" +
        "</gml:coordinates></gml:LineString>)", "false");

    runError("geo:touches(a, text {'a'})", NOCTX.qname());
    runError("geo:touches(<gml:Point><gml:coordinates>10,10 12,11</gml:coordinates>" +
            "</gml:Point>, <gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(2));
    runError("geo:touches(<gml:Point><gml:coordinates>1,1</gml:coordinates>" +
            "</gml:Point>, <gml:Line><gml:coordinates>0,0 2,1 3,3" +
            "</gml:coordinates></gml:Line>)", GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void crosses() {
    runQuery("geo:crosses(" +
        "<gml:Point><gml:coordinates>10,11</gml:coordinates></gml:Point>, " +
        "<gml:LineString><gml:coordinates>0,0 2,2</gml:coordinates></gml:LineString>)",
        "false");

    runError("geo:crosses(a, text {'a'})", NOCTX.qname());
    runError("geo:crosses(<gml:Point><gml:coordinates>10,10 12,11" +
            "</gml:coordinates></gml:Point>, " +
            "<gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates>" +
            "</gml:LinearRing>)", GeoErrors.qname(2));
    runError("geo:crosses(<gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>)",
            FUNCARGSG.qname());
  }

  /** Test method. */
  @Test
  public void within() {
    runQuery("geo:within(<gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1" +
            "</gml:coordinates></gml:LinearRing>, " +
            "<gml:LinearRing><gml:coordinates>1,1 20,1 50,30 1,1</gml:coordinates>" +
            "</gml:LinearRing>)", "false");

    runError("geo:within()", FUNCARGSG.qname());
    runError("geo:within(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>, " +
            "<gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(1));
    runError("geo:within(<gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>)",
            FUNCARGSG.qname());
  }

  /** Test method. */
  @Test
  public void contains() {
    runQuery("geo:contains(" +
            "<gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>, " +
            "<gml:Point><gml:coordinates>1.00,1.00</gml:coordinates></gml:Point>)",
            "true");

    runError("geo:contains()", FUNCARGSG.qname());
    runError("geo:contains(" +
            "<gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>, " +
            "<gml:Line><gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates></gml:Line>)",
            GeoErrors.qname(1));
    runError("geo:contains(" +
            "<gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>)",
            FUNCARGSG.qname());
  }

  /** Test method. */
  @Test
  public void overlaps() {
    runQuery("geo:overlaps(" +
            "<gml:LineString><gml:coordinates>1,1 55,99 2,1</gml:coordinates>" +
            "</gml:LineString>, <gml:LineString><gml:coordinates>1,1 55,0" +
            "</gml:coordinates></gml:LineString>)", "false");

    runError("geo:overlaps()", FUNCARGSG.qname());
    runError("geo:overlaps(<gml:LineString><gml:coordinates>1,1 55,99 2,1" +
            "</gml:coordinates></gml:LineString>," +
            "<gml:LineString></gml:LineString>)", GeoErrors.qname(2));
    runError("geo:overlaps(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>)",
            FUNCARGSG.qname());
  }

  /** Test method. */
  @Test
  public void relate() {
    runQuery("geo:relate(" +
            "<gml:Point><gml:coordinates>18,11</gml:coordinates></gml:Point>, " +
            "<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing><gml:coordinates>" +
            "10,10 20,10 30,40 20,40 10,10</gml:coordinates></gml:LinearRing>" +
            "</gml:outerBoundaryIs></gml:Polygon>, \"0********\")", "true");

    runError("geo:relate(<gml:Point><gml:coordinates>18,11</gml:coordinates></gml:Point>,"
          + "<gml:LineString><gml:coordinates>11,10 20,1 20,20</gml:coordinates>" +
            "</gml:LineString>, \"0******\")", FUNCMP.qname());

    runError("geo:relate(" +
            "<gml:Point><gml:coordinates>18,11</gml:coordinates></gml:Point>," +
            "<gml:LineString><gml:coordinates>11,10 20,1 20,20</gml:coordinates>" +
            "</gml:LineString>, \"0*******12*F\")", FUNCMP.qname());

    runError("geo:relate()", FUNCARGSG.qname());
    runError("geo:relate(" +
            "<gml:Line><gml:coordinates>1,1 55,99 2,1</gml:coordinates></gml:Line>," +
            "<gml:LineString></gml:LineString>, \"0********\")", GeoErrors.qname(1));
    runError("geo:relate(" +
            "<gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>," +
            " \"0********\")", FUNCARGSG.qname());
  }

  /** Test method. */
  @Test
  public void distance() {
    runQuery("geo:distance(<gml:LinearRing><gml:coordinates>10,400 20,200 30,100 " +
            "20,100 10,400</gml:coordinates></gml:LinearRing>, " +
            "<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing><gml:coordinates>" +
            "10,10 20,10 30,40 20,40 10,10</gml:coordinates></gml:LinearRing>" +
            "</gml:outerBoundaryIs></gml:Polygon>)", "60");

    runError("geo:distance()", FUNCARGSG.qname());
    runError("geo:distance(" +
            "<gml:LinearRing><gml:coordinates>1,1 55,99 2,1</gml:coordinates>" +
            "</gml:LinearRing>, <gml:LineString></gml:LineString>)", GeoErrors.qname(2));
    runError("geo:distance(" +
            "<gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>)",
            FUNCARGSG.qname());
  }

  /** Test method. */
  @Test
  public void buffer() {
    runQuery("geo:buffer(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>10,10 20,10 30,40 20,40 10,10</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>,xs:double(0))",
        "<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:outerBoundaryIs><gml:LinearRing><gml:coordinates>" +
        "10.0,10.0 20.0,40.0 30.0,40.0 20.0,10.0 10.0,10.0</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>");

    runQuery("geo:buffer(<gml:LineString><gml:coordinates>1,1 5,9 2,1" +
        "</gml:coordinates></gml:LineString>, xs:double(0))",
        "<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:outerBoundaryIs><gml:LinearRing><gml:coordinates/>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>");
    runError("geo:buffer(" +
            "<gml:LinearRing><gml:coordinates>1,1 55,99 2,1</gml:coordinates>" +
            "</gml:LinearRing>, xs:double(1))", GeoErrors.qname(2));
    runError("geo:buffer(<gml:LinearRing><gml:coordinates>1,1 55,99 1,1" +
            "</gml:coordinates></gml:LinearRing>, 1)", FUNCMP.qname());
    runError("geo:buffer(xs:double(1))", FUNCARGSG.qname());
  }

  /** Test method. */
  @Test
  public void convexHull() {
    runQuery("geo:convexHull(<gml:LinearRing><gml:coordinates>1,1 55,99 2,2 1,1" +
            "</gml:coordinates></gml:LinearRing>)",
            "<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:outerBoundaryIs><gml:LinearRing><gml:coordinates>" +
            "1.0,1.0 55.0,99.0 2.0,2.0 1.0,1.0</gml:coordinates></gml:LinearRing>" +
            "</gml:outerBoundaryIs></gml:Polygon>");

    runError("geo:convexHull(<gml:LinearRing><gml:coordinates>1,1 55,99 1,1" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(2));
    runError("geo:convexHull()", FUNCARGSG.qname());
    runError("geo:convexHull(<gml:LinearRing/>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void intersection() {
    runQuery("geo:intersection(<gml:LinearRing><gml:coordinates>1,1 55,99 2,3 1,1" +
            "</gml:coordinates></gml:LinearRing>," +
            "<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing><gml:coordinates>" +
            "10,10 20,10 30,40 10,10</gml:coordinates></gml:LinearRing>" +
            "</gml:outerBoundaryIs></gml:Polygon>)",
            "<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates/></gml:LineString>");

    runQuery("geo:intersection(<gml:LinearRing><gml:coordinates>1,1 55,99 2,3 1,1" +
            "</gml:coordinates></gml:LinearRing>," +
            "<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>");

    runError("geo:intersection(<gml:LinearRing><gml:coordinates></gml:coordinates>" +
            "</gml:LinearRing>)", FUNCARGSG.qname());
    runError("geo:intersection(<gml:Geo><gml:coordinates>2,3</gml:coordinates>" +
            "</gml:Geo>,<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
            GeoErrors.qname(1));
    runError("geo:intersection(<gml:LinearRing/>, <gml:Point/>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void union() {
    runQuery("geo:union(<gml:Point><gml:coordinates>2</gml:coordinates></gml:Point>," +
            "<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
            "<gml:MultiPoint xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:pointMember><gml:Point><gml:coordinates>2.0,0.0" +
            "</gml:coordinates></gml:Point></gml:pointMember><gml:pointMember>" +
            "<gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>" +
            "</gml:pointMember></gml:MultiPoint>");

    runQuery("geo:union(<gml:Point><gml:coordinates>2</gml:coordinates></gml:Point>," +
            "<gml:Point><gml:coordinates>3</gml:coordinates></gml:Point>)",
            "<gml:MultiPoint xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:pointMember><gml:Point>" +
            "<gml:coordinates>2.0,0.0</gml:coordinates></gml:Point></gml:pointMember>" +
            "<gml:pointMember><gml:Point><gml:coordinates>3.0,0.0</gml:coordinates>" +
            "</gml:Point></gml:pointMember></gml:MultiPoint>");

    runError("geo:union(<gml:Point><gml:coordinates></gml:coordinates></gml:Point>," +
            "<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
            GeoErrors.qname(2));
    runError("geo:union(text {'a'}, <gml:Point><gml:coordinates>2,3" +
            "</gml:coordinates></gml:Point>)", FUNCMP.qname());
  }

  /** Test method. */
  @Test
  public void difference() {
    runQuery("geo:difference(" +
        "<gml:Point><gml:coordinates>20,1</gml:coordinates></gml:Point>," +
        "<gml:LinearRing><gml:coordinates>0,0 20,20 20,30 0,20 0,0" +
        "</gml:coordinates></gml:LinearRing>)",
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>20.0,1.0</gml:coordinates></gml:Point>");

    runError("geo:difference(" +
            "<gml:Point><gml:coordinates></gml:coordinates></gml:Point>," +
        "<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
        GeoErrors.qname(2));
    runError("geo:difference(text {'a'}, <gml:Point><gml:coordinates>2,3" +
        "</gml:coordinates></gml:Point>)", FUNCMP.qname());
  }

  /** Test method. */
  @Test
  public void symDifference() {
    runQuery("geo:symDifference(" +
            "<gml:Point><gml:coordinates>20,1</gml:coordinates></gml:Point>," +
            "<gml:LinearRing><gml:coordinates>0,0 20,20 20,30 0,20 0,0" +
            "</gml:coordinates></gml:LinearRing>)",
             "<gml:MultiGeometry xmlns:gml=\"http://www.opengis.net/gml\">" +
             "<gml:geometryMember><gml:Point><gml:coordinates>" +
             "20.0,1.0</gml:coordinates></gml:Point></gml:geometryMember>" +
             "<gml:geometryMember><gml:LineString><gml:coordinates>0.0,0.0 20.0,20.0" +
             " 20.0,30.0 0.0,20.0 0.0,0.0</gml:coordinates></gml:LineString>" +
             "</gml:geometryMember></gml:MultiGeometry>");

    runError("geo:symDifference(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>," +
            "<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
            GeoErrors.qname(1));

    runError("geo:symDifference(text {'a'}, <gml:Point><gml:coordinates>2,3" +
            "</gml:coordinates></gml:Point>)", FUNCMP.qname());
  }

  /** Test method. */
  @Test
  public void geometryN() {
    runQuery("geo:geometryN(" +
            "<gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>, 1)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>2.0,1.0</gml:coordinates></gml:Point>");

    runError("geo:geometryN(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>,1)",
            GeoErrors.qname(1));
    runError("geo:geometryN(" +
            "<gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>, 0)",
            GeoErrors.qname(4));
    runError("geo:geometryN(" +
            "<gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>, 2)",
            GeoErrors.qname(4));
    runError("geo:geometryN(text {'a'}, <gml:Point><gml:coordinates>2,3" +
            "</gml:coordinates></gml:Point>)", FUNCMP.qname());
  }

  /** Test method. */
  @Test
  public void x() {
    runQuery("geo:x(<gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>)", "2");

    runError("geo:x(<gml:MultiPoint><gml:Point><gml:coordinates>1,1" +
            "</gml:coordinates></gml:Point><gml:Point><gml:coordinates>1,2" +
            "</gml:coordinates></gml:Point></gml:MultiPoint>)", GeoErrors.qname(3));

    runError("geo:x(" +
            "<gml:LinearRing><gml:coordinates>0,0 20,0 0,20 0,0</gml:coordinates>" +
            "</gml:LinearRing>)", GeoErrors.qname(3));

    runError("geo:x(<gml:Point><gml:coordinates></gml:coordinates></gml:Point>)",
        GeoErrors.qname(2));
    runError("geo:x(<gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>)",
        GeoErrors.qname(1));
    runError("geo:x(text {'a'})", FUNCMP.qname());
  }

  /** Test method. */
  @Test
  public void y() {
    runQuery("geo:y(<gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>)",
            "1");
    runQuery("geo:y(<gml:Point><gml:coordinates>2</gml:coordinates></gml:Point>)", "0");

    runError("geo:y(<gml:MultiPoint><gml:Point><gml:coordinates>1,1" +
            "</gml:coordinates></gml:Point><gml:Point><gml:coordinates>1,2" +
            "</gml:coordinates></gml:Point></gml:MultiPoint>)", GeoErrors.qname(3));

    runError("geo:y(" +
            "<gml:LinearRing><gml:coordinates>0,0 20,0 0,20 0,0" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(3));
    runError("geo:y(<gml:Point><gml:coordinates></gml:coordinates></gml:Point>)",
        GeoErrors.qname(2));
    runError("geo:y(<gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>)",
        GeoErrors.qname(1));
    runError("geo:y(a)", NOCTX.qname());
  }

  /** Test method. */
  @Test
  public void z() {
    runQuery("geo:z(<gml:Point><gml:coordinates>2,1,3</gml:coordinates></gml:Point>)",
        "3");
    runQuery("geo:z(<gml:Point><gml:coordinates>2</gml:coordinates></gml:Point>)",
        "NaN");

    runError("geo:z(<gml:MultiPoint><gml:Point><gml:coordinates>1,1" +
            "</gml:coordinates></gml:Point><gml:Point><gml:coordinates>1,2" +
        "</gml:coordinates></gml:Point></gml:MultiPoint>)", GeoErrors.qname(3));
    runError("geo:z(" +
        "<gml:LinearRing><gml:coordinates>0,0 20,0 0,20 0,0" +
        "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(3));
    runError("geo:z(<gml:Point><gml:coordinates></gml:coordinates></gml:Point>)",
            GeoErrors.qname(2));
    runError("geo:z(<gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>)",
        GeoErrors.qname(1));
    runError("geo:z(a)", NOCTX.qname());
  }

  /** Test method. */
  @Test
  public void length() {
    runQuery("geo:length(" +
            "<gml:Point><gml:coordinates>2,1,3</gml:coordinates></gml:Point>)", "0");

    runQuery("geo:length(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates></gml:LinearRing>" +
            "</gml:outerBoundaryIs></gml:Polygon>)", "9.07768723046357");
    runQuery("geo:length(<gml:MultiPoint><gml:Point><gml:coordinates>1,1" +
            "</gml:coordinates></gml:Point><gml:Point><gml:coordinates>1,2" +
            "</gml:coordinates></gml:Point></gml:MultiPoint>)", "0");

    runError("geo:length(<gml:LinearRing><gml:coordinates>0,0 0,20 0,0" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(2));
    runError("geo:length(<gml:Point><gml:coordinates></gml:coordinates></gml:Point>)",
        GeoErrors.qname(2));
    runError("geo:length(<gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>)",
        GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void startPoint() {
    runQuery("geo:startPoint(<gml:LinearRing><gml:coordinates>1,1 20,1 20,20 1,1" +
            "</gml:coordinates></gml:LinearRing>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>1.0,1.0</gml:coordinates></gml:Point>");

    runQuery("geo:startPoint(<gml:LineString><gml:coordinates>1,1 20,1 20,20 1,1" +
            "</gml:coordinates></gml:LineString>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>1.0,1.0</gml:coordinates></gml:Point>");

    runError("geo:startPoint(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>)", GeoErrors.qname(3));

    runError("geo:startPoint(" +
            "<gml:LineString><gml:coordinates>1,1</gml:coordinates></gml:LineString>)",
            GeoErrors.qname(2));
    runError("geo:startPoint()", FUNCARGSG.qname());
    runError("geo:startPoint(text {'gml:Point'})", FUNCMP.qname());
    runError("geo:startPoint(<gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>)",
            GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void endPoint() {
    runQuery("geo:endPoint(<gml:LinearRing><gml:coordinates>2,3 20,1 20,20 2,3" +
            "</gml:coordinates></gml:LinearRing>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>");

    runQuery("geo:endPoint(<gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
            "</gml:coordinates></gml:LineString>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>12.0,13.0</gml:coordinates></gml:Point>");

    runError("geo:endPoint(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>)", GeoErrors.qname(3));
    runError("geo:endPoint(" +
            "<gml:LineString><gml:coordinates>1,1</gml:coordinates></gml:LineString>)",
            GeoErrors.qname(2));
    runError("geo:endPoint()", FUNCARGSG.qname());
    runError("geo:endPoint(text {'gml:Point'})", FUNCMP.qname());
    runError("geo:endPoint(<gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>)",
            GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void isClosed() {
    runQuery("geo:isClosed(<gml:LinearRing><gml:coordinates>2,3 20,1 20,20 2,3" +
            "</gml:coordinates></gml:LinearRing>)", "true");
    runQuery("geo:isClosed(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>)", "false");

    runQuery("geo:isClosed(<gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
            "</gml:coordinates></gml:LineString>)", "false");
    runError("geo:isClosed(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates></gml:LinearRing>" +
            "</gml:outerBoundaryIs></gml:Polygon>)", GeoErrors.qname(3));

    runError("geo:isClosed(" +
            "<gml:LineString><gml:coordinates>1,1</gml:coordinates></gml:LineString>)",
            GeoErrors.qname(2));
    runError("geo:isClosed()", FUNCARGSG.qname());
    runError("geo:isClosed(text {'gml:Point'})", FUNCMP.qname());
    runError("geo:isClosed(" +
            "<gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>)",
            GeoErrors.qname(3));
  }

  /** Test method. */
  @Test
  public void isRing() {
    runQuery("geo:isRing(<gml:LinearRing><gml:coordinates>2,3 20,1 20,20 2,3" +
            "</gml:coordinates></gml:LinearRing>)", "true");

    runQuery("geo:isRing(<gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
            "</gml:coordinates></gml:LineString>)", "false");

    runError("geo:isRing(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>)", GeoErrors.qname(3));
    runError("geo:isClosed(" +
            "<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
            GeoErrors.qname(3));
    runError("geo:isRing(" +
            "<gml:LineString><gml:coordinates>1,1</gml:coordinates></gml:LineString>)",
            GeoErrors.qname(2));
    runError("geo:isRing()", FUNCARGSG.qname());
    runError("geo:isRing(text {'gml:Point'})", FUNCMP.qname());
    runError("geo:isRing(<Point><gml:coordinates>2,1</gml:coordinates></Point>)",
            GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void numPoints() {
    runQuery("geo:numPoints(<gml:LinearRing><gml:coordinates>2,3 20,1 20,20 2,3" +
            "</gml:coordinates></gml:LinearRing>)", "4");

    runQuery("geo:numPoints(<gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
            "</gml:coordinates></gml:LineString>)", "4");

    runQuery("geo:numPoints(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>)", "6");

    runError("geo:numPoints(<gml:LineString><gml:coordinates>1,1</gml:coordinates>" +
            "</gml:LineString>)", GeoErrors.qname(2));
    runError("geo:numPoints()", FUNCARGSG.qname());
    runError("geo:numPoints(text {'gml:Point'})", FUNCMP.qname());
    runError("geo:numPoints(<Point><gml:coordinates>2,1</gml:coordinates></Point>)",
            GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void pointN() {
    runQuery("geo:pointN(<gml:LinearRing><gml:coordinates>2,3 20,1 20,20 2,3" +
            "</gml:coordinates></gml:LinearRing>, 1)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>");

    runQuery("geo:pointN(<gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
            "</gml:coordinates></gml:LineString>, 4)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>12.0,13.0</gml:coordinates></gml:Point>");

    runError("geo:pointN(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>, 4)", GeoErrors.qname(3));

    runError("geo:pointN(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>,1)",
            GeoErrors.qname(1));
    runError("geo:pointN(<gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
            "</gml:coordinates></gml:LineString>, 5)", GeoErrors.qname(4));
    runError("geo:pointN(<gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
            "</gml:coordinates></gml:LineString>, 0)", GeoErrors.qname(4));
    runError("geo:pointN(text {'a'},3)", FUNCMP.qname());
  }

  /** Test method. */
  @Test
  public void area() {
    runQuery("geo:area(<gml:MultiPoint><gml:Point><gml:coordinates>1,1" +
            "</gml:coordinates></gml:Point><gml:Point><gml:coordinates>1,2" +
            "</gml:coordinates></gml:Point></gml:MultiPoint>)", "0");

    runQuery("geo:area(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
            "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>)", "49");

    runQuery("geo:area(<gml:LineString><gml:coordinates>" +
            "11,10 20,1 20,20</gml:coordinates></gml:LineString>)", "0");

    runError("geo:area(<gml:LinearRing><gml:coordinates>0,0 0,20 0,0" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(2));
    runError("geo:area()", FUNCARGSG.qname());
    runError("geo:area(<gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>)",
            GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void centroid() {
    runQuery("geo:centroid(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
            "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>14.5,14.5</gml:coordinates></gml:Point>");

    runQuery("geo:centroid(" +
            "<gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>");

    runQuery("geo:centroid(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>1.8468564716806986,1.540569415042095" +
            "</gml:coordinates></gml:Point>");

    runError("geo:centroid(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>)",
            GeoErrors.qname(1));
    runError("geo:centroid()", FUNCARGSG.qname());
    runError("geo:centroid(text {'a'})", FUNCMP.qname());
  }

  /** Test method. */
  @Test
  public void pointOnSurface() {
    runQuery("geo:pointOnSurface(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
            "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>14.5,14.5</gml:coordinates></gml:Point>");

    runQuery("geo:pointOnSurface(" +
            "<gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>");

    runQuery("geo:pointOnSurface(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>3.0,3.0</gml:coordinates></gml:Point>");

    runError("geo:pointOnSurface(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>)",
            GeoErrors.qname(1));

    runError("geo:pointOnSurface()", FUNCARGSG.qname());
    runError("geo:pointOnSurface(text {'a'})", FUNCMP.qname());
  }

  /** Test method. */
  @Test
  public void exteriorRing() {
    runQuery("geo:exteriorRing(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
            "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>)",
            "<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>11.0,11.0 18.0,11.0 18.0,18.0 11.0,18.0 11.0,11.0" +
            "</gml:coordinates></gml:LineString>");

    runError("geo:exteriorRing(" +
            "<gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>)",
            GeoErrors.qname(3));
    runError("geo:exteriorRing(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>)",
            GeoErrors.qname(1));
    runError("geo:exteriorRing()", FUNCARGSG.qname());
    runError("geo:exteriorRing(text {'a'})", FUNCMP.qname());
  }

  /** Test method. */
  @Test
  public void numInteriorRing() {
    runQuery("geo:numInteriorRing(" +
            "<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
            "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>)", "0");

    runError("geo:numInteriorRing(" +
            "<gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>)",
            GeoErrors.qname(3));

    runError("geo:numInteriorRing(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>)",
            GeoErrors.qname(1));

    runError("geo:numInteriorRing()", FUNCARGSG.qname());
    runError("geo:numInteriorRing(text {'a'})", FUNCMP.qname());
  }

  /** Test method. */
  @Test
  public void interiorRingN() {
    runQuery("geo:interiorRingN(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
            "</gml:LinearRing></gml:outerBoundaryIs><gml:innerBoundaryIs>" +
            "<gml:LinearRing><gml:coordinates>2,2 3,2 3,3 2,3 2,2" +
            "</gml:coordinates></gml:LinearRing></gml:innerBoundaryIs>" +
            "<gml:innerBoundaryIs><gml:LinearRing><gml:coordinates>" +
            "10,10 20,10 20,20 10,20 10,10</gml:coordinates></gml:LinearRing>" +
            "</gml:innerBoundaryIs></gml:Polygon>, 1)",
            "<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>2.0,2.0 3.0,2.0 3.0,3.0 2.0,3.0 2.0,2.0" +
            "</gml:coordinates></gml:LineString>");

    runError("geo:interiorRingN(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
            "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>, 1)",
            GeoErrors.qname(4));

    runError("geo:interiorRingN(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
            "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>, 0)",
            GeoErrors.qname(4));

    runError("geo:interiorRingN(" +
            "<gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>, 1)",
            GeoErrors.qname(3));

    runError("geo:interiorRingN(text {'<gml:Polygon/'}, 1)", FUNCMP.qname());
    runError("geo:interiorRingN()", FUNCARGSG.qname());
  }

  /**
   * Query.
   * @param query query
   * @param result result
   */
  private void runQuery(final String query, final String result) {
    query("import module namespace geo='http://expath.org/ns/geo'; " +
          "declare namespace gml='http://www.opengis.net/gml';" + query, result);
  }

  /**
   * Checks if a query yields the specified error code.
   * @param query query string
   * @param error expected error
   */
  static void runError(final String query, final QNm error) {
    final String q = "import module namespace geo='http://expath.org/ns/geo'; " +
        "declare namespace gml='http://www.opengis.net/gml';" + query;

    final QueryProcessor qp = new QueryProcessor(q, context);
    qp.ctx.sc.baseURI(".");
    try {
      final String res = qp.execute().toString().replaceAll("(\\r|\\n) *", "");
      fail("Query did not fail:\n" + query + "\n[E] " +
          error + "...\n[F] " + res);
    } catch(final QueryException ex) {
      if(!ex.qname().eq(error))
        fail("Wrong error code:\n[E] " + error + "\n[F] " + ex.qname());
    } finally {
      qp.close();
    }
  }
}
