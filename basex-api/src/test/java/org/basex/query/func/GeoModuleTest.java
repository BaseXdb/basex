package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.ApiFunction.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.io.out.*;
import org.basex.query.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the XQuery Geo functions prefixed with "geo".
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Masoumeh Seydi
 */
public final class GeoModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void dimension() {
    final ApiFunction func = _GEO_DIMENSION;

    run(func.args(" <gml:Point><gml:coordinates>1,2</gml:coordinates></gml:Point>"), 0);
    error(func.args(" text {'a'}"), INVTYPE_X_X_X);
    error(func.args(" <gml:unknown/>"), GEO_WHICH);
    error(func.args(" <gml:Point><gml:coordinates>1 2</gml:coordinates></gml:Point>"), GEO_READ);
  }

  /** Test method. */
  @Test public void geometryType() {
    final ApiFunction func = _GEO_GEOMETRY_TYPE;

    run(func.args(" <gml:MultiPoint><gml:Point>" +
        "<gml:coordinates>1,1</gml:coordinates></gml:Point>" +
        "<gml:Point><gml:coordinates>1,2</gml:coordinates></gml:Point>" +
        "</gml:MultiPoint>"), "gml:MultiPoint");
    error(func.args(" text {'srsName'}"), INVTYPE_X_X_X);
    error(func.args(" <gml:unknown/>"), GEO_WHICH);
    error(func.args(" <gml:Point><gml:coordinates>1 2</gml:coordinates></gml:Point>"),
        GEO_READ);
  }

  /** Test method. */
  @Test public void srid() {
    final ApiFunction func = _GEO_SRID;

    run(func.args(" <gml:Polygon srsName=" +
        "\"http://www.opengis.net/gml/srs/epsg.xml#4326\">" +
        "<outerboundaryIs><gml:LinearRing><coordinates>" +
        "-150,50 -150,60 -125,60 -125,50 -150,50" +
        "</coordinates></gml:LinearRing></outerboundaryIs></gml:Polygon>"), 0);

    error(func.args(" text {'a'}"), INVTYPE_X_X_X);
    error(func.args(" <gml:unknown/>"), GEO_WHICH);
    error(func.args(" <gml:LinearRing><gml:pos>1,1 20,1 50,30 1,1</gml:pos></gml:LinearRing>"),
        GEO_READ);
  }

  /** Test method. */
  @Test public void envelope() {
    final ApiFunction func = _GEO_ENVELOPE;

    run(func.args(" <gml:LinearRing><gml:coordinates>1,1 20,1 50,30 1,1" +
        "</gml:coordinates></gml:LinearRing>"),
        "<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>1.0,1.0 1.0,30.0 50.0,30.0 50.0,1.0 1.0,1.0" +
        "</gml:coordinates></gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>");

    error(func.args(" text {'a'}"), INVTYPE_X_X_X);
    error(func.args(" <gml:unknown/>"), GEO_WHICH);
    error(func.args(" <gml:LinearRing><gml:pos>1,1 20,1 50,30 1,1</gml:pos></gml:LinearRing>"),
        GEO_READ);
  }

  /** Test method. */
  @Test public void asText() {
    final ApiFunction func = _GEO_AS_TEXT;

    run(func.args(" <gml:LineString><gml:coordinates>1,1 55,99 2,1" +
        "</gml:coordinates></gml:LineString>"), "LINESTRING (1 1, 55 99, 2 1)");

    error(func.args(" text {'a'}"), INVTYPE_X_X_X);
    error(func.args(" <gml:unknown/>"), GEO_WHICH);
    error(func.args(" <gml:LineString><gml:coordinates>1,1</gml:coordinates></gml:LineString>"),
        GEO_READ);
  }

  /** Test method. */
  @Test public void asBinary() {
    final ApiFunction func = _GEO_AS_BINARY;

    run("string(" + func.args(" <gml:LineString><gml:coordinates>1,1 55,99 2,1" +
        "</gml:coordinates></gml:LineString>") + ')',
        "AAAAAAIAAAADP/AAAAAAAAA/8AAAAAAAAEBLgAAAAAAAQFjAAAAAAABAAAAAAAAAAD/wAAAAAAAA");

    error(func.args(" text {'a'}"), INVTYPE_X_X_X);
    error(func.args(" <gml:unknown/>"), GEO_WHICH);
    error(func.args(" <gml:LinearRing><gml:coordinates>1,1 55,99 2,1" +
        "</gml:coordinates></gml:LinearRing>"), GEO_READ);
  }

  /** Test method. */
  @Test public void isSimple() {
    final ApiFunction func = _GEO_IS_SIMPLE;

    run(func.args(" <gml:LineString><gml:coordinates>1,1 20,1 10,4 20,-10" +
        "</gml:coordinates></gml:LineString>"), false);

    error(func.args(" text {'a'}"), INVTYPE_X_X_X);
    error(func.args(" <gml:unknown/>"), GEO_WHICH);
    error(func.args(" <gml:LinearRing><gml:coordinates>1,1 55,99 2,1" +
        "</gml:coordinates></gml:LinearRing>"), GEO_READ);
  }

  /** Test method. */
  @Test public void boundary() {
    final ApiFunction func = _GEO_BOUNDARY;

    run(func.args(" <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>"),
        "<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>11.0,11.0 18.0,11.0 18.0,18.0 11.0,18.0 11.0,11.0</gml:coordinates>" +
        "</gml:LineString>");
    run(func.args(" <gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>"),
        "<gml:MultiGeometry xmlns:gml=\"http://www.opengis.net/gml\"/>");

    error(func.args(" text {'a'}"), INVTYPE_X_X_X);
    error(func.args(" a"), NOCTX_X);
    error(func.args(" <gml:geo/>"), GEO_WHICH);
    error(func.args(" <gml:Point><gml:pos>1 2</gml:pos></gml:Point>"), GEO_READ);
  }

  /** Test method. */
  @Test public void equals() {
    final ApiFunction func = _GEO_EQUALS;

    run(func.args(" <gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1" +
        "</gml:coordinates></gml:LinearRing>",
        " <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates></gml:LinearRing>" +
        "</gml:outerBoundaryIs></gml:Polygon>"), false);

    error(func.args(" text { 'a' }", " a"), INVTYPE_X_X_X);
    error(func.args(" <gml:unknown/>", " <gml:LinearRing><gml:coordinates>" +
        "1,1 2,1 5,3 1,1</gml:coordinates></gml:LinearRing>"), GEO_WHICH);
    error(func.args(" <gml:LinearRing><gml:coordinates>1,1 55,99 2,1" +
        "</gml:coordinates></gml:LinearRing>",
        " <gml:LineString><gml:coordinates>1,1 55,99 2,1" +
        "</gml:coordinates></gml:LineString>"), GEO_READ);
  }

  /** Test method. */
  @Test public void disjoint() {
    final ApiFunction func = _GEO_DISJOINT;

    run(func.args(" <gml:MultiLineString><gml:LineString><gml:coordinates>" +
        "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
        "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
        "</gml:MultiLineString>",
        " <gml:LineString><gml:coordinates>0,0 2,1 3,3</gml:coordinates>" +
        "</gml:LineString>"), false);

    error(func.args(" a, text {'a'}"), NOCTX_X);
    error(func.args(" <gml:Ring/>",
        " <gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates>" +
        "</gml:LinearRing>"), GEO_WHICH);
    error(func.args(" <gml:LineString><gml:coordinates></gml:coordinates>" +
        "</gml:LineString>"), FUNCARITY_X_X_X);
  }

  /** Test method. */
  @Test public void intersects() {
    final ApiFunction func = _GEO_INTERSECTS;

    run(func.args(" <gml:MultiLineString><gml:LineString><gml:coordinates>" +
        "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
        "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString></gml:MultiLineString>",
        " <gml:LineString><gml:coordinates>0,0 2,1 3,3</gml:coordinates></gml:LineString>"), true);

    error(func.args(" a, text {'a'}"), NOCTX_X);
    error(func.args(" <gml:Point><gml:coordinates>10,10 12,11</gml:coordinates></gml:Point>",
        " <gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1" +
        "</gml:coordinates></gml:LinearRing>"), GEO_READ);
    error(func.args(" <gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>",
        " <gml:Line><gml:coordinates>0,0 2,1 3,3</gml:coordinates></gml:Line>"),
        GEO_WHICH);
  }

  /** Test method. */
  @Test public void touches() {
    final ApiFunction func = _GEO_TOUCHES;

    run(func.args(" <gml:MultiLineString><gml:LineString><gml:coordinates>" +
        "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
        "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString></gml:MultiLineString>",
        " <gml:LineString><gml:coordinates>0,0 2,1 3,3" +
        "</gml:coordinates></gml:LineString>"), false);

    error(func.args(" a, text {'a'}"), NOCTX_X);
    error(func.args(" <gml:Point><gml:coordinates>10,10 12,11</gml:coordinates></gml:Point>",
        " <gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1" +
        "</gml:coordinates></gml:LinearRing>"), GEO_READ);
    error(func.args(" <gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>",
        " <gml:Line><gml:coordinates>0,0 2,1 3,3</gml:coordinates></gml:Line>"),
        GEO_WHICH);
  }

  /** Test method. */
  @Test public void crosses() {
    final ApiFunction func = _GEO_CROSSES;

    run(func.args(" <gml:Point><gml:coordinates>10,11</gml:coordinates></gml:Point>",
        " <gml:LineString><gml:coordinates>0,0 2,2</gml:coordinates></gml:LineString>"), false);

    error(func.args(" a, text {'a'}"), NOCTX_X);
    error(func.args(" <gml:Point><gml:coordinates>10,10 12,11</gml:coordinates></gml:Point>",
        " <gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates></gml:LinearRing>"),
        GEO_READ);
    error(func.args(" <gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>"),
        FUNCARITY_X_X_X);
  }

  /** Test method. */
  @Test public void within() {
    final ApiFunction func = _GEO_WITHIN;

    run(func.args(" <gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1" +
        "</gml:coordinates></gml:LinearRing>",
        " <gml:LinearRing><gml:coordinates>1,1 20,1 50,30 1,1</gml:coordinates></gml:LinearRing>"),
        false);

    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" <gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>",
        " <gml:LinearRing><gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates></gml:LinearRing>"),
        GEO_WHICH);
    error(func.args(" <gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>"),
        FUNCARITY_X_X_X);
  }

  /** Test method. */
  @Test public void contains() {
    final ApiFunction func = _GEO_CONTAINS;

    run(func.args(" <gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>",
        " <gml:Point><gml:coordinates>1.00,1.00</gml:coordinates></gml:Point>"), true);

    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" <gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>",
        " <gml:Line><gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates></gml:Line>"),
        GEO_WHICH);
    error(func.args(" <gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>"),
        FUNCARITY_X_X_X);
  }

  /** Test method. */
  @Test public void overlaps() {
    final ApiFunction func = _GEO_OVERLAPS;

    run(func.args(" <gml:LineString><gml:coordinates>1,1 55,99 2,1</gml:coordinates>" +
        "</gml:LineString>",
        " <gml:LineString><gml:coordinates>1,1 55,0</gml:coordinates></gml:LineString>"), false);

    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" <gml:LineString><gml:coordinates>1,1 55,99 2,1" +
        "</gml:coordinates></gml:LineString>",
        " <gml:LineString></gml:LineString>"), GEO_READ);
    error(func.args(" <gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>"),
        FUNCARITY_X_X_X);
  }

  /** Test method. */
  @Test public void relate() {
    final ApiFunction func = _GEO_RELATE;

    run(func.args(" <gml:Point><gml:coordinates>18,11</gml:coordinates></gml:Point>",
        " <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing><gml:coordinates>" +
        "10,10 20,10 30,40 20,40 10,10</gml:coordinates></gml:LinearRing>" +
        "</gml:outerBoundaryIs></gml:Polygon>", "0********"), true);

    error(func.args(" <gml:Point><gml:coordinates>18,11</gml:coordinates></gml:Point>",
        " <gml:LineString><gml:coordinates>11,10 20,1 20,20</gml:coordinates>" +
        "</gml:LineString>", "0******"), GEO_ARG);
    error(func.args(" <gml:Point><gml:coordinates>18,11</gml:coordinates></gml:Point>",
        " <gml:LineString><gml:coordinates>11,10 20,1 20,20</gml:coordinates>" +
        "</gml:LineString>", "0*******12*F"), GEO_ARG);
    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" <gml:Line><gml:coordinates>1,1 55,99 2,1</gml:coordinates></gml:Line>",
        " <gml:LineString></gml:LineString>", "0********"), GEO_WHICH);
    error(func.args(" <gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>",
        "0********"), FUNCARITY_X_X_X);
  }

  /** Test method. */
  @Test public void distance() {
    final ApiFunction func = _GEO_DISTANCE;

    run(func.args(" <gml:LinearRing><gml:coordinates>10,400 20,200 30,100 " +
        "20,100 10,400</gml:coordinates></gml:LinearRing>",
        " <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing><gml:coordinates>" +
        "10,10 20,10 30,40 20,40 10,10</gml:coordinates></gml:LinearRing>" +
        "</gml:outerBoundaryIs></gml:Polygon>"), "60");

    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" <gml:LinearRing><gml:coordinates>1,1 55,99 2,1</gml:coordinates>" +
        "</gml:LinearRing>", " <gml:LineString/>"), GEO_READ);
    error(func.args(" <gml:Point><gml:coordinates>1,1</gml:coordinates></gml:Point>"),
        FUNCARITY_X_X_X);
  }

  /** Test method. */
  @Test public void buffer() {
    final ApiFunction func = _GEO_BUFFER;

    run(func.args(" <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>10,10 20,10 30,40 20,40 10,10</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>", " xs:double(0)"),
        "<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:outerBoundaryIs><gml:LinearRing><gml:coordinates>" +
        "10.0,10.0 20.0,40.0 30.0,40.0 20.0,10.0 10.0,10.0</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>");
    run(func.args(" <gml:LineString><gml:coordinates>1,1 5,9 2,1" +
        "</gml:coordinates></gml:LineString>", " xs:double(0)"),
        "<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:outerBoundaryIs><gml:LinearRing><gml:coordinates/>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>");

    error(func.args(" <gml:LinearRing><gml:coordinates>1,1 55,99 2,1</gml:coordinates>" +
        "</gml:LinearRing>",
        " xs:double(1)"), GEO_READ);
    error(func.args(" xs:double(1)"), FUNCARITY_X_X_X);
  }

  /** Test method. */
  @Test public void convexHull() {
    final ApiFunction func = _GEO_CONVEX_HULL;

    run(func.args(" <gml:LinearRing><gml:coordinates>1,1 55,99 2,2 1,1" +
        "</gml:coordinates></gml:LinearRing>"),
        "<gml:Polygon xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:outerBoundaryIs><gml:LinearRing><gml:coordinates>" +
        "1.0,1.0 55.0,99.0 2.0,2.0 1.0,1.0</gml:coordinates></gml:LinearRing>" +
        "</gml:outerBoundaryIs></gml:Polygon>");

    error(func.args(" <gml:LinearRing><gml:coordinates>1,1 55,99 1,1" +
        "</gml:coordinates></gml:LinearRing>"), GEO_READ);
    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" <gml:LinearRing/>"), GEO_READ);
  }

  /** Test method. */
  @Test public void intersection() {
    final ApiFunction func = _GEO_INTERSECTION;

    run(func.args(" <gml:LinearRing><gml:coordinates>1,1 55,99 2,3 1,1" +
        "</gml:coordinates></gml:LinearRing>",
        " <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing><gml:coordinates>" +
        "10,10 20,10 30,40 10,10</gml:coordinates></gml:LinearRing>" +
        "</gml:outerBoundaryIs></gml:Polygon>"),
        "<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates/></gml:LineString>");
    run(func.args(" <gml:LinearRing><gml:coordinates>1,1 55,99 2,3 1,1" +
        "</gml:coordinates></gml:LinearRing>",
        " <gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>"),
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>");

    error(func.args(" <gml:LinearRing><gml:coordinates></gml:coordinates>" +
        "</gml:LinearRing>"), FUNCARITY_X_X_X);
    error(func.args(" <gml:Geo><gml:coordinates>2,3</gml:coordinates>" +
        "</gml:Geo>,<gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>"),
        GEO_WHICH);
    error(func.args(" <gml:LinearRing/>", " <gml:Point/>"), GEO_READ);
  }

  /** Test method. */
  @Test public void union() {
    final ApiFunction func = _GEO_UNION;

    run(func.args(" <gml:Point><gml:coordinates>2</gml:coordinates></gml:Point>",
        " <gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>"),
        "<gml:MultiPoint xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:pointMember><gml:Point><gml:coordinates>2.0,0.0" +
        "</gml:coordinates></gml:Point></gml:pointMember><gml:pointMember>" +
        "<gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>" +
        "</gml:pointMember></gml:MultiPoint>");

    run(func.args(" <gml:Point><gml:coordinates>2</gml:coordinates></gml:Point>",
        " <gml:Point><gml:coordinates>3</gml:coordinates></gml:Point>"),
        "<gml:MultiPoint xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:pointMember><gml:Point>" +
        "<gml:coordinates>2.0,0.0</gml:coordinates></gml:Point></gml:pointMember>" +
        "<gml:pointMember><gml:Point><gml:coordinates>3.0,0.0</gml:coordinates>" +
        "</gml:Point></gml:pointMember></gml:MultiPoint>");

    error(func.args(" <gml:Point><gml:coordinates></gml:coordinates></gml:Point>",
        " <gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>"),
        GEO_READ);
    error(func.args(" text { 'a' }", " <gml:Point><gml:coordinates>2,3" +
        "</gml:coordinates></gml:Point>"), INVTYPE_X_X_X);
  }

  /** Test method. */
  @Test public void difference() {
    final ApiFunction func = _GEO_DIFFERENCE;

    run(func.args(" <gml:Point><gml:coordinates>20,1</gml:coordinates></gml:Point>",
        " <gml:LinearRing><gml:coordinates>0,0 20,20 20,30 0,20 0,0" +
        "</gml:coordinates></gml:LinearRing>"),
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>20.0,1.0</gml:coordinates></gml:Point>");

    error(func.args(" <gml:Point><gml:coordinates></gml:coordinates></gml:Point>",
        " <gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>"),
        GEO_READ);
    error(func.args(" text { 'a' }", " <gml:Point><gml:coordinates>2,3" +
        "</gml:coordinates></gml:Point>"), INVTYPE_X_X_X);
  }

  /** Test method. */
  @Test public void symDifference() {
    final ApiFunction func = _GEO_SYM_DIFFERENCE;

    run(func.args(" <gml:Point><gml:coordinates>20,1</gml:coordinates></gml:Point>",
        " <gml:LinearRing><gml:coordinates>0,0 20,20 20,30 0,20 0,0" +
        "</gml:coordinates></gml:LinearRing>"),
        "<gml:MultiGeometry xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:geometryMember><gml:Point><gml:coordinates>" +
        "20.0,1.0</gml:coordinates></gml:Point></gml:geometryMember>" +
        "<gml:geometryMember><gml:LineString><gml:coordinates>0.0,0.0 20.0,20.0" +
        " 20.0,30.0 0.0,20.0 0.0,0.0</gml:coordinates></gml:LineString>" +
        "</gml:geometryMember></gml:MultiGeometry>");

    error(func.args(" <gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>",
        " <gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>"),
        GEO_WHICH);
    error(func.args(" text {'a'}", "<gml:Point><gml:coordinates>2,3" +
        "</gml:coordinates></gml:Point>"), INVTYPE_X_X_X);
  }

  /** Test method. */
  @Test public void geometryN() {
    final ApiFunction func = _GEO_GEOMETRY_N;

    run(func.args(" <gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>", 1),
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>2.0,1.0</gml:coordinates></gml:Point>");

    error(func.args(" <gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>,1"),
        GEO_WHICH);
    error(func.args(" <gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>, 0"),
        GEO_RANGE);
    error(func.args(" <gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>, 2"),
        GEO_RANGE);
    error(func.args(" text { 'a' }", " <gml:Point><gml:coordinates>2,3" +
        "</gml:coordinates></gml:Point>"), INVTYPE_X_X_X);
  }

  /** Test method. */
  @Test public void x() {
    final ApiFunction func = _GEO_X;

    run(func.args(" <gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>"), 2);

    error(func.args(" <gml:MultiPoint><gml:Point><gml:coordinates>1,1" +
        "</gml:coordinates></gml:Point><gml:Point><gml:coordinates>1,2" +
        "</gml:coordinates></gml:Point></gml:MultiPoint>"), GEO_TYPE);
    error(func.args(" <gml:LinearRing><gml:coordinates>0,0 20,0 0,20 0,0</gml:coordinates>" +
        "</gml:LinearRing>"), GEO_TYPE);
    error(func.args(" <gml:Point><gml:coordinates></gml:coordinates></gml:Point>"),
        GEO_READ);
    error(func.args(" <gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>"),
        GEO_WHICH);
    error(func.args(" text {'a'}"), INVTYPE_X_X_X);
  }

  /** Test method. */
  @Test public void y() {
    final ApiFunction func = _GEO_Y;

    run(func.args(" <gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>"), 1);
    run(func.args(" <gml:Point><gml:coordinates>2</gml:coordinates></gml:Point>"), 0);

    error(func.args(" <gml:MultiPoint><gml:Point><gml:coordinates>1,1" +
        "</gml:coordinates></gml:Point><gml:Point><gml:coordinates>1,2" +
        "</gml:coordinates></gml:Point></gml:MultiPoint>"), GEO_TYPE);
    error(func.args(" <gml:LinearRing><gml:coordinates>0,0 20,0 0,20 0,0" +
        "</gml:coordinates></gml:LinearRing>"), GEO_TYPE);
    error(func.args(" <gml:Point><gml:coordinates></gml:coordinates></gml:Point>"),
        GEO_READ);
    error(func.args(" <gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>"),
        GEO_WHICH);
    error(func.args(" a"), NOCTX_X);
  }

  /** Test method. */
  @Test public void z() {
    final ApiFunction func = _GEO_Z;

    run(func.args(" <gml:Point><gml:coordinates>2,1,3</gml:coordinates></gml:Point>"), 3);
    run(func.args(" <gml:Point><gml:coordinates>2</gml:coordinates></gml:Point>"), "NaN");

    error(func.args(" <gml:MultiPoint><gml:Point><gml:coordinates>1,1" +
        "</gml:coordinates></gml:Point><gml:Point><gml:coordinates>1,2" +
        "</gml:coordinates></gml:Point></gml:MultiPoint>"), GEO_TYPE);
    error(func.args(" <gml:LinearRing><gml:coordinates>0,0 20,0 0,20 0,0" +
        "</gml:coordinates></gml:LinearRing>"), GEO_TYPE);
    error(func.args(" <gml:Point><gml:coordinates></gml:coordinates></gml:Point>"),
        GEO_READ);
    error(func.args(" <gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>"),
        GEO_WHICH);
    error(func.args(" a"), NOCTX_X);
  }

  /** Test method. */
  @Test public void length() {
    final ApiFunction func = _GEO_LENGTH;

    run(func.args(" <gml:Point><gml:coordinates>2,1,3</gml:coordinates></gml:Point>"), 0);
    run(func.args(" <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates></gml:LinearRing>" +
        "</gml:outerBoundaryIs></gml:Polygon>"), "9.07768723046357");
    run(func.args(" <gml:MultiPoint><gml:Point><gml:coordinates>1,1" +
        "</gml:coordinates></gml:Point><gml:Point><gml:coordinates>1,2" +
        "</gml:coordinates></gml:Point></gml:MultiPoint>"), 0);

    error(func.args(" <gml:LinearRing><gml:coordinates>0,0 0,20 0,0" +
        "</gml:coordinates></gml:LinearRing>"), GEO_READ);
    error(func.args(" <gml:Point><gml:coordinates></gml:coordinates></gml:Point>"),
        GEO_READ);
    error(func.args(" <gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>"),
        GEO_WHICH);
  }

  /** Test method. */
  @Test public void startPoint() {
    final ApiFunction func = _GEO_START_POINT;

    run(func.args(" <gml:LinearRing><gml:coordinates>1,1 20,1 20,20 1,1" +
        "</gml:coordinates></gml:LinearRing>"),
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>1.0,1.0</gml:coordinates></gml:Point>");
    run(func.args(" <gml:LineString><gml:coordinates>1,1 20,1 20,20 1,1" +
        "</gml:coordinates></gml:LineString>"),
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>1.0,1.0</gml:coordinates></gml:Point>");

    error(func.args(" <gml:MultiLineString><gml:LineString><gml:coordinates>" +
        "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
        "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
        "</gml:MultiLineString>"), GEO_TYPE);
    error(func.args(" <gml:LineString><gml:coordinates>1,1</gml:coordinates></gml:LineString>"),
        GEO_READ);
    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" text {'gml:Point'}"), INVTYPE_X_X_X);
    error(func.args(" <gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>"),
        GEO_WHICH);
  }

  /** Test method. */
  @Test public void endPoint() {
    final ApiFunction func = _GEO_END_POINT;

    run(func.args(" <gml:LinearRing><gml:coordinates>2,3 20,1 20,20 2,3" +
        "</gml:coordinates></gml:LinearRing>"),
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>");
    run(func.args(" <gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
        "</gml:coordinates></gml:LineString>"),
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>12.0,13.0</gml:coordinates></gml:Point>");

    error(func.args(" <gml:MultiLineString><gml:LineString><gml:coordinates>" +
        "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
        "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
        "</gml:MultiLineString>"), GEO_TYPE);
    error(func.args(" <gml:LineString><gml:coordinates>1,1</gml:coordinates></gml:LineString>"),
        GEO_READ);
    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" text {'gml:Point'}"), INVTYPE_X_X_X);
    error(func.args(" <gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>"),
        GEO_WHICH);
  }

  /** Test method. */
  @Test public void isClosed() {
    final ApiFunction func = _GEO_IS_CLOSED;

    run(func.args(" <gml:LinearRing><gml:coordinates>2,3 20,1 20,20 2,3" +
        "</gml:coordinates></gml:LinearRing>"), true);
    run(func.args(" <gml:MultiLineString><gml:LineString><gml:coordinates>" +
        "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
        "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
        "</gml:MultiLineString>"), false);
    run(func.args(" <gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
        "</gml:coordinates></gml:LineString>"), false);

    error(func.args(" <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>1,1 2,1 5,3 1,1</gml:coordinates></gml:LinearRing>" +
        "</gml:outerBoundaryIs></gml:Polygon>"), GEO_TYPE);
    error(func.args(" <gml:LineString><gml:coordinates>1,1</gml:coordinates></gml:LineString>"),
        GEO_READ);
    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" text {'gml:Point'}"), INVTYPE_X_X_X);
    error(func.args(" <gml:Point><gml:coordinates>2,1</gml:coordinates></gml:Point>"),
        GEO_TYPE);
  }

  /** Test method. */
  @Test public void isRing() {
    final ApiFunction func = _GEO_IS_RING;

    run(func.args(" <gml:LinearRing><gml:coordinates>2,3 20,1 20,20 2,3" +
        "</gml:coordinates></gml:LinearRing>"), true);
    run(func.args(" <gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
        "</gml:coordinates></gml:LineString>"), false);

    error(func.args(" <gml:MultiLineString><gml:LineString><gml:coordinates>" +
        "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
        "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
        "</gml:MultiLineString>"), GEO_TYPE);
    error(func.args(" <gml:Point><gml:coordinates>2,3</gml:coordinates></gml:Point>"),
        GEO_TYPE);
    error(func.args(" <gml:LineString><gml:coordinates>1,1</gml:coordinates></gml:LineString>"),
        GEO_READ);
    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" text {'gml:Point'}"), INVTYPE_X_X_X);
    error(func.args(" <Point><gml:coordinates>2,1</gml:coordinates></Point>"), GEO_WHICH);
  }

  /** Test method. */
  @Test public void numPoints() {
    final ApiFunction func = _GEO_NUM_POINTS;

    run(func.args(" <gml:LinearRing><gml:coordinates>2,3 20,1 20,20 2,3" +
        "</gml:coordinates></gml:LinearRing>"), 4);
    run(func.args(" <gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
        "</gml:coordinates></gml:LineString>"), 4);
    run(func.args(" <gml:MultiLineString><gml:LineString><gml:coordinates>" +
        "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
        "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
        "</gml:MultiLineString>"), 6);

    error(func.args(" <gml:LineString><gml:coordinates>1,1</gml:coordinates>" +
        "</gml:LineString>"), GEO_READ);
    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" text {'gml:Point'}"), INVTYPE_X_X_X);
    error(func.args(" <Point><gml:coordinates>2,1</gml:coordinates></Point>"),
        GEO_WHICH);
  }

  /** Test method. */
  @Test public void pointN() {
    final ApiFunction func = _GEO_POINT_N;

    run(func.args(" <gml:LinearRing><gml:coordinates>2,3 20,1 20,20 2,3" +
        "</gml:coordinates></gml:LinearRing>, 1"),
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>");
    run(func.args(" <gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
        "</gml:coordinates></gml:LineString>, 4"),
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>12.0,13.0</gml:coordinates></gml:Point>");

    error(func.args(" <gml:MultiLineString><gml:LineString><gml:coordinates>" +
        "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
        "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
        "</gml:MultiLineString>, 4"), GEO_TYPE);
    error(func.args(" <gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>,1"),
        GEO_WHICH);
    error(func.args(" <gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
        "</gml:coordinates></gml:LineString>, 5"), GEO_RANGE);
    error(func.args(" <gml:LineString><gml:coordinates>11,10 20,1 20,20 12,13" +
        "</gml:coordinates></gml:LineString>, 0"), GEO_RANGE);
    error(func.args(" text {'a'},3"), INVTYPE_X_X_X);
  }

  /** Test method. */
  @Test public void area() {
    final ApiFunction func = _GEO_AREA;

    run(func.args(" <gml:MultiPoint><gml:Point><gml:coordinates>1,1" +
        "</gml:coordinates></gml:Point><gml:Point><gml:coordinates>1,2" +
        "</gml:coordinates></gml:Point></gml:MultiPoint>"), 0);
    run(func.args(" <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>"), "49");
    run(func.args(" <gml:LineString><gml:coordinates>" +
        "11,10 20,1 20,20</gml:coordinates></gml:LineString>"), 0);

    error(func.args(" <gml:LinearRing><gml:coordinates>0,0 0,20 0,0" +
        "</gml:coordinates></gml:LinearRing>"), GEO_READ);
    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" <gml:geo><gml:coordinates>2,1</gml:coordinates></gml:geo>"),
        GEO_WHICH);
  }

  /** Test method. */
  @Test public void centroid() {
    final ApiFunction func = _GEO_CENTROID;

    run(func.args(" <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>"),
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>14.5,14.5</gml:coordinates></gml:Point>");
    run(func.args(" <gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>"),
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>");
    run(func.args(" <gml:MultiLineString><gml:LineString><gml:coordinates>" +
        "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
        "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
        "</gml:MultiLineString>"),
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>1.8468564716806986,1.540569415042095" +
        "</gml:coordinates></gml:Point>");

    error(func.args(" <gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>"),
        GEO_WHICH);
    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" text {'a'}"), INVTYPE_X_X_X);
  }

  /** Test method. */
  @Test public void pointOnSurface() {
    final ApiFunction func = _GEO_POINT_ON_SURFACE;

    run(func.args(" <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>"),
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>14.5,14.5</gml:coordinates></gml:Point>");
    run(func.args(" <gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>"),
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>");
    run(func.args(" <gml:MultiLineString><gml:LineString><gml:coordinates>" +
        "1,1 0,0 2,1</gml:coordinates></gml:LineString><gml:LineString>" +
        "<gml:coordinates>2,1 3,3 4,4</gml:coordinates></gml:LineString>" +
        "</gml:MultiLineString>"),
        "<gml:Point xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>3.0,3.0</gml:coordinates></gml:Point>");

    error(func.args(" <gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>"),
        GEO_WHICH);
    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" text {'a'}"), INVTYPE_X_X_X);
  }

  /** Test method. */
  @Test public void exteriorRing() {
    final ApiFunction func = _GEO_EXTERIOR_RING;

    run(func.args(" <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>"),
        "<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>11.0,11.0 18.0,11.0 18.0,18.0 11.0,18.0 11.0,11.0" +
        "</gml:coordinates></gml:LineString>");

    error(func.args(" <gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>"),
        GEO_TYPE);
    error(func.args(" <gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>"),
        GEO_WHICH);
    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" text {'a'}"), INVTYPE_X_X_X);
  }

  /** Test method. */
  @Test public void numInteriorRing() {
    final ApiFunction func = _GEO_NUM_INTERIOR_RING;

    run(func.args(" <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>"), 0);

    error(func.args(" <gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>"),
        GEO_TYPE);
    error(func.args(" <gml:unknown><gml:coordinates>1,1</gml:coordinates></gml:unknown>"),
        GEO_WHICH);
    error(func.args(" "), FUNCARITY_X_X_X);
    error(func.args(" text {'a'}"), INVTYPE_X_X_X);
  }

  /** Test method. */
  @Test public void interiorRingN() {
    final ApiFunction func = _GEO_INTERIOR_RING_N;

    run(func.args(" <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs><gml:innerBoundaryIs>" +
        "<gml:LinearRing><gml:coordinates>2,2 3,2 3,3 2,3 2,2" +
        "</gml:coordinates></gml:LinearRing></gml:innerBoundaryIs>" +
        "<gml:innerBoundaryIs><gml:LinearRing><gml:coordinates>" +
        "10,10 20,10 20,20 10,20 10,10</gml:coordinates></gml:LinearRing>" +
        "</gml:innerBoundaryIs></gml:Polygon>, 1"),
        "<gml:LineString xmlns:gml=\"http://www.opengis.net/gml\">" +
        "<gml:coordinates>2.0,2.0 3.0,2.0 3.0,3.0 2.0,3.0 2.0,2.0" +
        "</gml:coordinates></gml:LineString>");

    error(func.args(" <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>, 1"),
        GEO_RANGE);
    error(func.args(" <gml:Polygon><gml:outerBoundaryIs><gml:LinearRing>" +
        "<gml:coordinates>11,11 18,11 18,18 11,18 11,11</gml:coordinates>" +
        "</gml:LinearRing></gml:outerBoundaryIs></gml:Polygon>, 0"),
        GEO_RANGE);
    error(func.args(" <gml:Point><gml:coordinates>2.0,3.0</gml:coordinates></gml:Point>, 1"),
        GEO_TYPE);
    error(func.args(" text {'<gml:Polygon/'}, 1"), INVTYPE_X_X_X);
    error(func.args(" "), FUNCARITY_X_X_X);
  }

  /**
   * Query.
   * @param query query
   * @param result result
   */
  private static void run(final String query, final Object result) {
    final String qu = "declare namespace gml='http://www.opengis.net/gml';" + query;
    assertEquals(result.toString(), query(qu).replaceAll(Prop.NL + "\\s*", ""));
  }

  /**
   * Checks if a query yields the specified error code.
   * @param query query string
   * @param qe query error
   */
  private static void error(final String query, final QueryError qe) {
    final String qu = "declare namespace gml='http://www.opengis.net/gml';" + query;
    try(QueryProcessor qp = new QueryProcessor(qu, context)) {
      final ArrayOutput ao = qp.value().serialize();
      fail("Query did not fail:\n" + query + "\n[E] " + qe.qname() + "...\n[F] " + ao);
    } catch(final QueryException ex) {
      final QueryError qerr = ex.error();
      if(qe != qerr) {
        Util.stack(ex);
        final StringBuilder sb = new StringBuilder("Wrong error code:\n[E] ");
        fail(sb.append(qe.name()).append("\n[F] ").append(qerr.name()).toString());
      }
    } catch(final Exception ex) {
      Util.stack(ex);
      fail(ex.toString());
    }
  }
}
