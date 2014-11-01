package org.expath.ns;

import static org.basex.query.util.Err.*;
import static org.junit.Assert.*;

import org.basex.AdvancedQueryTest;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.junit.*;

/**
 * This class tests the XQuery Geo functions prefixed with "geo".
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Masoumeh Seydi
 */
public final class GeoTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void dimension() {
    run("geo:dimension(" +
            "<gml:Point><gml:coordinates>1,2</gml:coordinates></gml:Point>)", "0");

    error("geo:dimension(text {'a'})", EXPTYPE_X_X_X.qname());
    error("geo:dimension(<gml:unknown/>)", GeoErrors.qname(1));
    error("geo:dimension(<gml:Point>" +
            "<gml:coordinates>1 2</gml:coordinates></gml:Point>)",
            GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void geometryType() {
    run("geo:geometryType(<gml:MultiPoint><gml:Point>" +
            "<gml:coordinates>1,1</gml:coordinates></gml:Point>" +
            "<gml:Point><gml:coordinates>1,2</gml:coordinates></gml:Point>" +
            "</gml:MultiPoint>)", "gml:MultiPoint");

    error("geo:geometryType(text {'srsName'})", EXPTYPE_X_X_X.qname());
    error("geo:geometryType(<gml:unknown/>)", GeoErrors.qname(1));
    error("geo:geometryType(<gml:Point><gml:coordinates>1 2</gml:coordinates>" +
            "</gml:Point>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void srid() {
    run("geo:srid(<gml:Polygon srsName=" +
            "\"http://www.opengis.net/gml/srs/epsg.xml#4326\">" +
            "<outerboundaryIs><gml:LinearRing><coordinates>" +
            "-150,50 -150,60 -125,60 -125,50 -150,50" +
            "</coordinates></gml:LinearRing></outerboundaryIs></gml:Polygon>)", "0");

    error("geo:srid(text {'a'})", EXPTYPE_X_X_X.qname());
    error("geo:srid(<gml:unknown/>)", GeoErrors.qname(1));
    error("geo:srid(<gml:LinearRing><gml:pos>1,1 20,1 50,30 1,1</gml:pos>" +
            "</gml:LinearRing>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void envelope() {
    run("geo:envelope(<gml:LinearRing><gml:coordinates>1,1 20,1 50,30 1,1" +
            "</gml:coordinates></gml:LinearRing>)",
            "<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>1.0,1.0 1.0,30.0 50.0,30.0 50.0,1.0 1.0,1.0" +
            "</gml:coordinates></gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>");

    error("geo:envelope(text {'a'})", EXPTYPE_X_X_X.qname());
    error("geo:envelope(<gml:unknown/>)", GeoErrors.qname(1));
    error("geo:envelope(<gml:LinearRing><gml:pos>1,1 20,1 50,30 1,1</gml:pos>" +
            "</gml:LinearRing>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void asText() {
    run("geo:asText(<gml:LineString><gml:coordinates>1,1 55,99 2,1" +
            "</gml:coordinates></gml:LineString>)", "LINESTRING (1 1, 55 99, 2 1)");

    error("geo:asText(text {'a'})", EXPTYPE_X_X_X.qname());
    error("geo:asText(<gml:unknown/>)", GeoErrors.qname(1));
    error("geo:asText(<gml:LineString><gml:coordinates>1,1</gml:coordinates>" +
            "</gml:LineString>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void asBinary() {
    run("geo:asBinary(<gml:LineString><gml:coordinates>1,1 55,99 2,1" +
            "</gml:coordinates></gml:LineString>)",
          "AAAAAAIAAAADP/AAAAAAAAA/8AAAAAAAAEBLgAAAAAAAQFjAAAAAAABAAAAAAAAAAD/wAAAAAAAA");

    error("geo:asBinary(text {'a'})", EXPTYPE_X_X_X.qname());
    error("geo:asBinary(<gml:unknown/>)", GeoErrors.qname(1));
    error("geo:asBinary(<gml:LinearRing><gml:coordinates>1,1 55,99 2,1" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void isSimple() {
    run("geo:isSimple(<gml:LineString><gml:coordinates>1,1 20,1 10,4 20,-10" +
            "</gml:coordinates></gml:LineString>)", "false");

    error("geo:isSimple(text {'a'})", EXPTYPE_X_X_X.qname());
    error("geo:isSimple(<gml:unknown/>)", GeoErrors.qname(1));
    error("geo:isSimple(<gml:LinearRing><gml:coordinates>1,1 55,99 2,1" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void boundary() {
    run("geo:boundary(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>)",
        "<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>11.0,11.0 18.0,11.0 18.0,18.0 11.0,18.0 " +
        "11.0,11.0</gml:coordinates></gml:LineString>");

    run("geo:boundary(" +
        "<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
        "<gml:MultiGeometry xmlns:gml=\"http://www.opengis.net/gml\"/>");
    error("geo:boundary(text {'a'})", EXPTYPE_X_X_X.qname());
    error("geo:boundary(a)", NOCTX_X.qname());
    error("geo:boundary(<gml:geo/>)", GeoErrors.qname(1));
    error("geo:boundary(<gml:Point><gml:pos>1 2</gml:pos></gml:Point>)",
        GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void equals() {
    run("geo:equals(<gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1" +
            "</gml:coordinates></gml:LinearRing>, " +
            "<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates></gml:LinearRing>" +
            "</gml:outerBoundaryIs></gml:Polygon>)", "false");

    error("geo:equals(text {'a'}, a)", NOCTX_X.qname());
    error("geo:equals(<gml:unknown/>, <gml:LinearRing><gml:coordinates>" +
            "1,1 2,1 5,3 1,1</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(1));
    error("geo:equals(<gml:LinearRing><gml:coordinates>1,1 55,99 2,1" +
            "</gml:coordinates></gml:LinearRing>," +
            "<gml:LineString><gml:coordinates>1,1 55,99 2,1" +
            "</gml:coordinates></gml:LineString>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void disjoint() {
    run("geo:disjoint(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>, " +
            "<gml:LineString><gml:coordinates>0,0 2,1 3,3</gml:coordinates>" +
            "</gml:LineString>)", "false");

    error("geo:disjoint(a, text {'a'})", NOCTX_X.qname());
    error("geo:disjoint(<gml:Ring/>, " +
            "<gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates>" +
            "</gml:LinearRing>)", GeoErrors.qname(1));
    error("geo:disjoint(<gml:LineString><gml:coordinates></gml:coordinates>" +
                  "</gml:LineString>)", FUNCARGS_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void intersects() {
    run("geo:intersects(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>, <gml:LineString><gml:coordinates>0,0 2,1 3,3" +
            "</gml:coordinates></gml:LineString>)", "true");

    error("geo:intersects(a, text {'a'})", NOCTX_X.qname());
    error("geo:intersects(<gml:Point><gml:coordinates>10,10 12,11</gml:coordinates>" +
            "</gml:Point>, <gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(2));
    error("geo:intersects(<gml:Point><gml:coordinates>1,1</gml:coordinates>" +
            "</gml:Point>, <gml:Line><gml:coordinates>0,0 2,1 3,3</gml:coordinates>" +
            "</gml:Line>)", GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void touches() {
    run("geo:touches(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
        "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
        "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
        "</gml:MultiLineString>, <gml:LineString><gml:coordinates>0,0 2,1 3,3" +
        "</gml:coordinates></gml:LineString>)", "false");

    error("geo:touches(a, text {'a'})", NOCTX_X.qname());
    error("geo:touches(<gml:Point><gml:coordinates>10,10 12,11</gml:coordinates>" +
            "</gml:Point>, <gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(2));
    error("geo:touches(<gml:Point><gml:coordinates>1,1</gml:coordinates>" +
            "</gml:Point>, <gml:Line><gml:coordinates>0,0 2,1 3,3" +
            "</gml:coordinates></gml:Line>)", GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void crosses() {
    run("geo:crosses(" +
        "<gml:Point><gml:coordinates>10,11</gml:coordinates></gml:Point>, " +
        "<gml:LineString><gml:coordinates>0,0 2,2</gml:coordinates></gml:LineString>)",
        "false");

    error("geo:crosses(a, text {'a'})", NOCTX_X.qname());
    error("geo:crosses(<gml:Point><gml:coordinates>10,10 12,11" +
            "</gml:coordinates></gml:Point>, " +
            "<gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates>" +
            "</gml:LinearRing>)", GeoErrors.qname(2));
    error("geo:crosses(<gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>)",
            FUNCARGS_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void within() {
    run("geo:within(<gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1" +
            "</gml:coordinates></gml:LinearRing>, " +
            "<gml:LinearRing><gml:coordinates>1,1 20,1 50,30 1,1</gml:coordinates>" +
            "</gml:LinearRing>)", "false");

    error("geo:within()", FUNCARGS_X_X_X.qname());
    error("geo:within(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>, " +
            "<gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(1));
    error("geo:within(<gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>)",
            FUNCARGS_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void contains() {
    run("geo:contains(" +
            "<gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>, " +
            "<gml:Point><gml:coordinates>1.00,1.00</gml:coordinates></gml:Point>)",
            "true");

    error("geo:contains()", FUNCARGS_X_X_X.qname());
    error("geo:contains(" +
            "<gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>, " +
            "<gml:Line><gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates></gml:Line>)",
            GeoErrors.qname(1));
    error("geo:contains(" +
            "<gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>)",
            FUNCARGS_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void overlaps() {
    run("geo:overlaps(" +
            "<gml:LineString><gml:coordinates>1,1 55,99 2,1</gml:coordinates>" +
            "</gml:LineString>, <gml:LineString><gml:coordinates>1,1 55,0" +
            "</gml:coordinates></gml:LineString>)", "false");

    error("geo:overlaps()", FUNCARGS_X_X_X.qname());
    error("geo:overlaps(<gml:LineString><gml:coordinates>1,1 55,99 2,1" +
            "</gml:coordinates></gml:LineString>," +
            "<gml:LineString></gml:LineString>)", GeoErrors.qname(2));
    error("geo:overlaps(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>)",
            FUNCARGS_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void relate() {
    run("geo:relate(" +
            "<gml:Point><gml:coordinates>18,11</gml:coordinates></gml:Point>, " +
            "<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing><gml:coordinates>" +
            "10,10 20,10 30,40 20,40 10,10</gml:coordinates></gml:LinearRing>" +
            "</gml:outerBoundaryIs></gml:Polygon>, \"0********\")", "true");

    error("geo:relate(<gml:Point><gml:coordinates>18,11</gml:coordinates></gml:Point>,"
          + "<gml:LineString><gml:coordinates>11,10 20,1 20,20</gml:coordinates>" +
            "</gml:LineString>, \"0******\")", EXPTYPE_X_X_X.qname());

    error("geo:relate(" +
            "<gml:Point><gml:coordinates>18,11</gml:coordinates></gml:Point>," +
            "<gml:LineString><gml:coordinates>11,10 20,1 20,20</gml:coordinates>" +
            "</gml:LineString>, \"0*******12*F\")", EXPTYPE_X_X_X.qname());

    error("geo:relate()", FUNCARGS_X_X_X.qname());
    error("geo:relate(" +
            "<gml:Line><gml:coordinates>1,1 55,99 2,1</gml:coordinates></gml:Line>," +
            "<gml:LineString></gml:LineString>, \"0********\")", GeoErrors.qname(1));
    error("geo:relate(" +
            "<gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>," +
            " \"0********\")", FUNCARGS_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void distance() {
    run("geo:distance(<gml:LinearRing><gml:coordinates>10,400 20,200 30,100 " +
            "20,100 10,400</gml:coordinates></gml:LinearRing>, " +
            "<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing><gml:coordinates>" +
            "10,10 20,10 30,40 20,40 10,10</gml:coordinates></gml:LinearRing>" +
            "</gml:outerBoundaryIs></gml:Polygon>)", "60");

    error("geo:distance()", FUNCARGS_X_X_X.qname());
    error("geo:distance(" +
            "<gml:LinearRing><gml:coordinates>1,1 55,99 2,1</gml:coordinates>" +
            "</gml:LinearRing>, <gml:LineString></gml:LineString>)", GeoErrors.qname(2));
    error("geo:distance(" +
            "<gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>)",
            FUNCARGS_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void buffer() {
    run("geo:buffer(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>10,10 20,10 30,40 20,40 10,10</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>,xs:double(0))",
        "<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:outerBoundaryIs><gml:LinearRing><gml:coordinates>" +
        "10.0,10.0 20.0,40.0 30.0,40.0 20.0,10.0 10.0,10.0</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>");

    run("geo:buffer(<gml:LineString><gml:coordinates>1,1 5,9 2,1" +
        "</gml:coordinates></gml:LineString>, xs:double(0))",
        "<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:outerBoundaryIs><gml:LinearRing><gml:coordinates/>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>");
    error("geo:buffer(" +
            "<gml:LinearRing><gml:coordinates>1,1 55,99 2,1</gml:coordinates>" +
            "</gml:LinearRing>, xs:double(1))", GeoErrors.qname(2));
    error("geo:buffer(<gml:LinearRing><gml:coordinates>1,1 55,99 1,1" +
            "</gml:coordinates></gml:LinearRing>, 1)", EXPTYPE_X_X_X.qname());
    error("geo:buffer(xs:double(1))", FUNCARGS_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void convexHull() {
    run("geo:convexHull(<gml:LinearRing><gml:coordinates>1,1 55,99 2,2 1,1" +
            "</gml:coordinates></gml:LinearRing>)",
            "<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:outerBoundaryIs><gml:LinearRing><gml:coordinates>" +
            "1.0,1.0 55.0,99.0 2.0,2.0 1.0,1.0</gml:coordinates></gml:LinearRing>" +
            "</gml:outerBoundaryIs></gml:Polygon>");

    error("geo:convexHull(<gml:LinearRing><gml:coordinates>1,1 55,99 1,1" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(2));
    error("geo:convexHull()", FUNCARGS_X_X_X.qname());
    error("geo:convexHull(<gml:LinearRing/>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void intersection() {
    run("geo:intersection(<gml:LinearRing><gml:coordinates>1,1 55,99 2,3 1,1" +
            "</gml:coordinates></gml:LinearRing>," +
            "<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing><gml:coordinates>" +
            "10,10 20,10 30,40 10,10</gml:coordinates></gml:LinearRing>" +
            "</gml:outerBoundaryIs></gml:Polygon>)",
            "<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates/></gml:LineString>");

    run("geo:intersection(<gml:LinearRing><gml:coordinates>1,1 55,99 2,3 1,1" +
            "</gml:coordinates></gml:LinearRing>," +
            "<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>");

    error("geo:intersection(<gml:LinearRing><gml:coordinates></gml:coordinates>" +
            "</gml:LinearRing>)", FUNCARGS_X_X_X.qname());
    error("geo:intersection(<gml:Geo><gml:coordinates>2,3</gml:coordinates>" +
            "</gml:Geo>,<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
            GeoErrors.qname(1));
    error("geo:intersection(<gml:LinearRing/>, <gml:Point/>)", GeoErrors.qname(2));
  }

  /** Test method. */
  @Test
  public void union() {
    run("geo:union(<gml:Point><gml:coordinates>2</gml:coordinates></gml:Point>," +
            "<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
            "<gml:MultiPoint xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:pointMember><gml:Point><gml:coordinates>2.0,0.0" +
            "</gml:coordinates></gml:Point></gml:pointMember><gml:pointMember>" +
            "<gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>" +
            "</gml:pointMember></gml:MultiPoint>");

    run("geo:union(<gml:Point><gml:coordinates>2</gml:coordinates></gml:Point>," +
            "<gml:Point><gml:coordinates>3</gml:coordinates></gml:Point>)",
            "<gml:MultiPoint xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:pointMember><gml:Point>" +
            "<gml:coordinates>2.0,0.0</gml:coordinates></gml:Point></gml:pointMember>" +
            "<gml:pointMember><gml:Point><gml:coordinates>3.0,0.0</gml:coordinates>" +
            "</gml:Point></gml:pointMember></gml:MultiPoint>");

    error("geo:union(<gml:Point><gml:coordinates></gml:coordinates></gml:Point>," +
            "<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
            GeoErrors.qname(2));
    error("geo:union(text {'a'}, <gml:Point><gml:coordinates>2,3" +
            "</gml:coordinates></gml:Point>)", EXPTYPE_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void difference() {
    run("geo:difference(" +
        "<gml:Point><gml:coordinates>20,1</gml:coordinates></gml:Point>," +
        "<gml:LinearRing><gml:coordinates>0,0 20,20 20,30 0,20 0,0" +
        "</gml:coordinates></gml:LinearRing>)",
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>20.0,1.0</gml:coordinates></gml:Point>");

    error("geo:difference(" +
            "<gml:Point><gml:coordinates></gml:coordinates></gml:Point>," +
        "<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
        GeoErrors.qname(2));
    error("geo:difference(text {'a'}, <gml:Point><gml:coordinates>2,3" +
        "</gml:coordinates></gml:Point>)", EXPTYPE_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void symDifference() {
    run("geo:symDifference(" +
            "<gml:Point><gml:coordinates>20,1</gml:coordinates></gml:Point>," +
            "<gml:LinearRing><gml:coordinates>0,0 20,20 20,30 0,20 0,0" +
            "</gml:coordinates></gml:LinearRing>)",
             "<gml:MultiGeometry xmlns:gml=\"http://www.opengis.net/gml\">" +
             "<gml:geometryMember><gml:Point><gml:coordinates>" +
             "20.0,1.0</gml:coordinates></gml:Point></gml:geometryMember>" +
             "<gml:geometryMember><gml:LineString><gml:coordinates>0.0,0.0 20.0,20.0" +
             " 20.0,30.0 0.0,20.0 0.0,0.0</gml:coordinates></gml:LineString>" +
             "</gml:geometryMember></gml:MultiGeometry>");

    error("geo:symDifference(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>," +
            "<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
            GeoErrors.qname(1));

    error("geo:symDifference(text {'a'}, <gml:Point><gml:coordinates>2,3" +
            "</gml:coordinates></gml:Point>)", EXPTYPE_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void geometryN() {
    run("geo:geometryN(" +
            "<gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>, 1)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>2.0,1.0</gml:coordinates></gml:Point>");

    error("geo:geometryN(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>,1)",
            GeoErrors.qname(1));
    error("geo:geometryN(" +
            "<gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>, 0)",
            GeoErrors.qname(4));
    error("geo:geometryN(" +
            "<gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>, 2)",
            GeoErrors.qname(4));
    error("geo:geometryN(text {'a'}, <gml:Point><gml:coordinates>2,3" +
            "</gml:coordinates></gml:Point>)", EXPTYPE_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void x() {
    run("geo:x(<gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>)", "2");

    error("geo:x(<gml:MultiPoint><gml:Point><gml:coordinates>1,1" +
            "</gml:coordinates></gml:Point><gml:Point><gml:coordinates>1,2" +
            "</gml:coordinates></gml:Point></gml:MultiPoint>)", GeoErrors.qname(3));

    error("geo:x(" +
            "<gml:LinearRing><gml:coordinates>0,0 20,0 0,20 0,0</gml:coordinates>" +
            "</gml:LinearRing>)", GeoErrors.qname(3));

    error("geo:x(<gml:Point><gml:coordinates></gml:coordinates></gml:Point>)",
        GeoErrors.qname(2));
    error("geo:x(<gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>)",
        GeoErrors.qname(1));
    error("geo:x(text {'a'})", EXPTYPE_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void y() {
    run("geo:y(<gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>)",
            "1");
    run("geo:y(<gml:Point><gml:coordinates>2</gml:coordinates></gml:Point>)", "0");

    error("geo:y(<gml:MultiPoint><gml:Point><gml:coordinates>1,1" +
            "</gml:coordinates></gml:Point><gml:Point><gml:coordinates>1,2" +
            "</gml:coordinates></gml:Point></gml:MultiPoint>)", GeoErrors.qname(3));

    error("geo:y(" +
            "<gml:LinearRing><gml:coordinates>0,0 20,0 0,20 0,0" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(3));
    error("geo:y(<gml:Point><gml:coordinates></gml:coordinates></gml:Point>)",
        GeoErrors.qname(2));
    error("geo:y(<gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>)",
        GeoErrors.qname(1));
    error("geo:y(a)", NOCTX_X.qname());
  }

  /** Test method. */
  @Test
  public void z() {
    run("geo:z(<gml:Point><gml:coordinates>2,1,3</gml:coordinates></gml:Point>)",
        "3");
    run("geo:z(<gml:Point><gml:coordinates>2</gml:coordinates></gml:Point>)",
        "NaN");

    error("geo:z(<gml:MultiPoint><gml:Point><gml:coordinates>1,1" +
            "</gml:coordinates></gml:Point><gml:Point><gml:coordinates>1,2" +
        "</gml:coordinates></gml:Point></gml:MultiPoint>)", GeoErrors.qname(3));
    error("geo:z(" +
        "<gml:LinearRing><gml:coordinates>0,0 20,0 0,20 0,0" +
        "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(3));
    error("geo:z(<gml:Point><gml:coordinates></gml:coordinates></gml:Point>)",
            GeoErrors.qname(2));
    error("geo:z(<gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>)",
        GeoErrors.qname(1));
    error("geo:z(a)", NOCTX_X.qname());
  }

  /** Test method. */
  @Test
  public void length() {
    run("geo:length(" +
            "<gml:Point><gml:coordinates>2,1,3</gml:coordinates></gml:Point>)", "0");

    run("geo:length(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates></gml:LinearRing>" +
            "</gml:outerBoundaryIs></gml:Polygon>)", "9.07768723046357");
    run("geo:length(<gml:MultiPoint><gml:Point><gml:coordinates>1,1" +
            "</gml:coordinates></gml:Point><gml:Point><gml:coordinates>1,2" +
            "</gml:coordinates></gml:Point></gml:MultiPoint>)", "0");

    error("geo:length(<gml:LinearRing><gml:coordinates>0,0 0,20 0,0" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(2));
    error("geo:length(<gml:Point><gml:coordinates></gml:coordinates></gml:Point>)",
        GeoErrors.qname(2));
    error("geo:length(<gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>)",
        GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void startPoint() {
    run("geo:startPoint(<gml:LinearRing><gml:coordinates>1,1 20,1 20,20 1,1" +
            "</gml:coordinates></gml:LinearRing>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>1.0,1.0</gml:coordinates></gml:Point>");

    run("geo:startPoint(<gml:LineString><gml:coordinates>1,1 20,1 20,20 1,1" +
            "</gml:coordinates></gml:LineString>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>1.0,1.0</gml:coordinates></gml:Point>");

    error("geo:startPoint(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>)", GeoErrors.qname(3));

    error("geo:startPoint(" +
            "<gml:LineString><gml:coordinates>1,1</gml:coordinates></gml:LineString>)",
            GeoErrors.qname(2));
    error("geo:startPoint()", FUNCARGS_X_X_X.qname());
    error("geo:startPoint(text {'gml:Point'})", EXPTYPE_X_X_X.qname());
    error("geo:startPoint(<gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>)",
            GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void endPoint() {
    run("geo:endPoint(<gml:LinearRing><gml:coordinates>2,3 20,1 20,20 2,3" +
            "</gml:coordinates></gml:LinearRing>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>");

    run("geo:endPoint(<gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
            "</gml:coordinates></gml:LineString>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>12.0,13.0</gml:coordinates></gml:Point>");

    error("geo:endPoint(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>)", GeoErrors.qname(3));
    error("geo:endPoint(" +
            "<gml:LineString><gml:coordinates>1,1</gml:coordinates></gml:LineString>)",
            GeoErrors.qname(2));
    error("geo:endPoint()", FUNCARGS_X_X_X.qname());
    error("geo:endPoint(text {'gml:Point'})", EXPTYPE_X_X_X.qname());
    error("geo:endPoint(<gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>)",
            GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void isClosed() {
    run("geo:isClosed(<gml:LinearRing><gml:coordinates>2,3 20,1 20,20 2,3" +
            "</gml:coordinates></gml:LinearRing>)", "true");
    run("geo:isClosed(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>)", "false");

    run("geo:isClosed(<gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
            "</gml:coordinates></gml:LineString>)", "false");
    error("geo:isClosed(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates></gml:LinearRing>" +
            "</gml:outerBoundaryIs></gml:Polygon>)", GeoErrors.qname(3));

    error("geo:isClosed(" +
            "<gml:LineString><gml:coordinates>1,1</gml:coordinates></gml:LineString>)",
            GeoErrors.qname(2));
    error("geo:isClosed()", FUNCARGS_X_X_X.qname());
    error("geo:isClosed(text {'gml:Point'})", EXPTYPE_X_X_X.qname());
    error("geo:isClosed(" +
            "<gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>)",
            GeoErrors.qname(3));
  }

  /** Test method. */
  @Test
  public void isRing() {
    run("geo:isRing(<gml:LinearRing><gml:coordinates>2,3 20,1 20,20 2,3" +
            "</gml:coordinates></gml:LinearRing>)", "true");

    run("geo:isRing(<gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
            "</gml:coordinates></gml:LineString>)", "false");

    error("geo:isRing(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>)", GeoErrors.qname(3));
    error("geo:isClosed(" +
            "<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>)",
            GeoErrors.qname(3));
    error("geo:isRing(" +
            "<gml:LineString><gml:coordinates>1,1</gml:coordinates></gml:LineString>)",
            GeoErrors.qname(2));
    error("geo:isRing()", FUNCARGS_X_X_X.qname());
    error("geo:isRing(text {'gml:Point'})", EXPTYPE_X_X_X.qname());
    error("geo:isRing(<Point><gml:coordinates>2,1</gml:coordinates></Point>)",
            GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void numPoints() {
    run("geo:numPoints(<gml:LinearRing><gml:coordinates>2,3 20,1 20,20 2,3" +
            "</gml:coordinates></gml:LinearRing>)", "4");

    run("geo:numPoints(<gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
            "</gml:coordinates></gml:LineString>)", "4");

    run("geo:numPoints(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>)", "6");

    error("geo:numPoints(<gml:LineString><gml:coordinates>1,1</gml:coordinates>" +
            "</gml:LineString>)", GeoErrors.qname(2));
    error("geo:numPoints()", FUNCARGS_X_X_X.qname());
    error("geo:numPoints(text {'gml:Point'})", EXPTYPE_X_X_X.qname());
    error("geo:numPoints(<Point><gml:coordinates>2,1</gml:coordinates></Point>)",
            GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void pointN() {
    run("geo:pointN(<gml:LinearRing><gml:coordinates>2,3 20,1 20,20 2,3" +
            "</gml:coordinates></gml:LinearRing>, 1)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>");

    run("geo:pointN(<gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
            "</gml:coordinates></gml:LineString>, 4)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>12.0,13.0</gml:coordinates></gml:Point>");

    error("geo:pointN(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>, 4)", GeoErrors.qname(3));

    error("geo:pointN(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>,1)",
            GeoErrors.qname(1));
    error("geo:pointN(<gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
            "</gml:coordinates></gml:LineString>, 5)", GeoErrors.qname(4));
    error("geo:pointN(<gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
            "</gml:coordinates></gml:LineString>, 0)", GeoErrors.qname(4));
    error("geo:pointN(text {'a'},3)", EXPTYPE_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void area() {
    run("geo:area(<gml:MultiPoint><gml:Point><gml:coordinates>1,1" +
            "</gml:coordinates></gml:Point><gml:Point><gml:coordinates>1,2" +
            "</gml:coordinates></gml:Point></gml:MultiPoint>)", "0");

    run("geo:area(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
            "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>)", "49");

    run("geo:area(<gml:LineString><gml:coordinates>" +
            "11,10 20,1 20,20</gml:coordinates></gml:LineString>)", "0");

    error("geo:area(<gml:LinearRing><gml:coordinates>0,0 0,20 0,0" +
            "</gml:coordinates></gml:LinearRing>)", GeoErrors.qname(2));
    error("geo:area()", FUNCARGS_X_X_X.qname());
    error("geo:area(<gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>)",
            GeoErrors.qname(1));
  }

  /** Test method. */
  @Test
  public void centroid() {
    run("geo:centroid(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
            "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>14.5,14.5</gml:coordinates></gml:Point>");

    run("geo:centroid(" +
            "<gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>");

    run("geo:centroid(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>1.8468564716806986,1.540569415042095" +
            "</gml:coordinates></gml:Point>");

    error("geo:centroid(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>)",
            GeoErrors.qname(1));
    error("geo:centroid()", FUNCARGS_X_X_X.qname());
    error("geo:centroid(text {'a'})", EXPTYPE_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void pointOnSurface() {
    run("geo:pointOnSurface(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
            "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>14.5,14.5</gml:coordinates></gml:Point>");

    run("geo:pointOnSurface(" +
            "<gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>");

    run("geo:pointOnSurface(<gml:MultiLineString><gml:LineString><gml:coordinates>" +
            "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
            "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
            "</gml:MultiLineString>)",
            "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>3.0,3.0</gml:coordinates></gml:Point>");

    error("geo:pointOnSurface(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>)",
            GeoErrors.qname(1));

    error("geo:pointOnSurface()", FUNCARGS_X_X_X.qname());
    error("geo:pointOnSurface(text {'a'})", EXPTYPE_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void exteriorRing() {
    run("geo:exteriorRing(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
            "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>)",
            "<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\">" +
            "<gml:coordinates>11.0,11.0 18.0,11.0 18.0,18.0 11.0,18.0 11.0,11.0" +
            "</gml:coordinates></gml:LineString>");

    error("geo:exteriorRing(" +
            "<gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>)",
            GeoErrors.qname(3));
    error("geo:exteriorRing(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>)",
            GeoErrors.qname(1));
    error("geo:exteriorRing()", FUNCARGS_X_X_X.qname());
    error("geo:exteriorRing(text {'a'})", EXPTYPE_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void numInteriorRing() {
    run("geo:numInteriorRing(" +
            "<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
            "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>)", "0");

    error("geo:numInteriorRing(" +
            "<gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>)",
            GeoErrors.qname(3));

    error("geo:numInteriorRing(" +
            "<gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>)",
            GeoErrors.qname(1));

    error("geo:numInteriorRing()", FUNCARGS_X_X_X.qname());
    error("geo:numInteriorRing(text {'a'})", EXPTYPE_X_X_X.qname());
  }

  /** Test method. */
  @Test
  public void interiorRingN() {
    run("geo:interiorRingN(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
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

    error("geo:interiorRingN(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
            "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>, 1)",
            GeoErrors.qname(4));

    error("geo:interiorRingN(<gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
            "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
            "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>, 0)",
            GeoErrors.qname(4));

    error("geo:interiorRingN(" +
            "<gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>, 1)",
            GeoErrors.qname(3));

    error("geo:interiorRingN(text {'<gml:Polygon/'}, 1)", EXPTYPE_X_X_X.qname());
    error("geo:interiorRingN()", FUNCARGS_X_X_X.qname());
  }

  /**
   * Query.
   * @param query query
   * @param result result
   */
  private static void run(final String query, final String result) {
    query("import module namespace geo='http://expath.org/ns/geo'; " +
          "declare namespace gml='http://www.opengis.net/gml';" + query, result);
  }

  /**
   * Checks if a query yields the specified error code.
   * @param query query string
   * @param error expected error
   */
  private static void error(final String query, final QNm error) {
    final String q = "import module namespace geo='http://expath.org/ns/geo'; " +
        "declare namespace gml='http://www.opengis.net/gml';" + query;

    try(final QueryProcessor qp = new QueryProcessor(q, context)) {
      final String res = qp.execute().toString().replaceAll("(\\r|\\n) *", "");
      fail("Query did not fail:\n" + query + "\n[E] " +
          error + "...\n[F] " + res);
    } catch(final QueryException ex) {
      if(!ex.qname().eq(error))
        fail("Wrong error code:\n[E] " + error + "\n[F] " + ex.qname());
    }
  }
}
