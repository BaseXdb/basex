package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.geo.GeoError.*;

import org.basex.query.func.geo.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the XQuery Geo functions prefixed with "geo" on GML 3 geometries.
 *
 * @author BaseX Team, BSD License
 * @author Masoumeh Seydi
 */
public abstract class Gml3Test extends GmlTest {
  /**
   * Sets the flag for removing gml:id attributes before each test.
   */
  @BeforeEach
  public void beforeEach() {
    GeoFn.removeGmlId = true;
  }

  /**
   * Resets the flag for removing gml:id attributes after each test.
   */
  @AfterEach
  public void afterEach() {
    GeoFn.removeGmlId = false;
  }

  /** Test method. */
  @Test public void dimension() {
    final String func = "dimension";

    run(args(func, " <gml:Point><gml:pos>1 2</gml:pos></gml:Point>"), 0);

    error(args(func, " text { 'a' }"), INVTYPE_X);
    error(args(func, " <gml:unknown/>"), GEO_READ);
    error(args(func, " <gml:Point><gml:pos>1,2</gml:pos></gml:Point>"), GEO_READ);
  }

  /** Test method. */
  @Test public void geometryType() {
    final String func = "geometry-type";

    run(args(func, " <gml:MultiPoint srsName='EPSG:4326'>"
        + "<gml:pointMember><gml:Point><gml:pos>1 1</gml:pos></gml:Point></gml:pointMember>"
        + "<gml:pointMember><gml:Point><gml:pos>1 2</gml:pos></gml:Point></gml:pointMember>"
        + "</gml:MultiPoint>"),
        "#Q{" + gmlUri() + "}MultiPoint");

    error(args(func, " text { 'srsName' }"), INVTYPE_X);
    error(args(func, " <gml:unknown/>"), GEO_READ);
    error(args(func, " <gml:Point><gml:pos>1,2</gml:pos></gml:Point>"), GEO_READ);
  }

  /** Test method. */
  @Test public void srid() {
    final String func = "srid";

    run(args(func, " <gml:Polygon><gml:exterior><gml:LinearRing>"
        + "<gml:posList>-150 50 -150 60 -125 60 -125 50 -150 50</gml:posList>"
        + "</gml:LinearRing></gml:exterior></gml:Polygon>"),
        0);

    error(args(func, " text { 'a' }"), INVTYPE_X);
    error(args(func, " <gml:unknown/>"), GEO_READ);
    error(args(func, " <gml:LinearRing><gml:pos>1 1 20 1 50 30 1 1</gml:pos></gml:LinearRing>"),
        GEO_READ);
  }

  /** Test method. */
  @Test public void envelope() {
    final String func = "envelope";

    run(args(func, " <gml:LinearRing><gml:posList>1 1 20 1 50 30 1 1</gml:posList>"
        + "</gml:LinearRing>"),
        "<gml:Polygon xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:exterior>"
        + "<gml:LinearRing><gml:posList>1 1 1 30 50 30 50 1 1 1</gml:posList></gml:LinearRing>"
        + "</gml:exterior></gml:Polygon>");

    error(args(func, " text { 'a' }"), INVTYPE_X);
    error(args(func, " <gml:unknown/>"), GEO_READ);
    error(args(func, " <gml:LinearRing><gml:pos>1 1 20 1 50 30 1 1</gml:pos></gml:LinearRing>"),
        GEO_READ);
  }

  /** Test method. */
  @Test public void asText() {
    final String func = "as-text";

    run(args(func, " <gml:LineString><gml:posList>1 1 55 99 2 1</gml:posList></gml:LineString>"),
        "LINESTRING (1 1, 55 99, 2 1)");

    error(args(func, " text { 'a' }"), INVTYPE_X);
    error(args(func, " <gml:unknown/>"), GEO_READ);
    error(args(func, " <gml:LineString><gml:posList>1 1</gml:posList></gml:LineString>"), GEO_READ);
  }

  /** Test method. */
  @Test public void asBinary() {
    final String func = "as-binary";

    run("string(" + args(func, " <gml:LineString><gml:posList>1 1 55 99 2 1</gml:posList>"
        + "</gml:LineString>") + ')',
        "AAAAAAIAAAADP/AAAAAAAAA/8AAAAAAAAEBLgAAAAAAAQFjAAAAAAABAAAAAAAAAAD/wAAAAAAAA");

    error(args(func, " text { 'a' }"), INVTYPE_X);
    error(args(func, " <gml:unknown/>"), GEO_READ);
    error(args(func, " <gml:LinearRing><gml:posList>1 1 2 1</gml:posList></gml:LinearRing>"),
        GEO_READ);
  }

  /** Test method. */
  @Test public void isSimple() {
    final String func = "is-simple";

    run(args(func, " <gml:LineString><gml:posList>1 1 20 1 10 4 20 -10</gml:posList>"
        + "</gml:LineString>"), false);

    error(args(func, " text { 'a' }"), INVTYPE_X);
    error(args(func, " <gml:unknown/>"), GEO_READ);
    error(args(func, " <gml:LinearRing><gml:posList>1 1 2 1</gml:posList></gml:LinearRing>"),
        GEO_READ);
  }

  /** Test method. */
  @Test public void boundary() {
    final String func = "boundary";

    run(args(func, " <gml:Polygon><gml:exterior><gml:LinearRing>"
        + "<gml:posList>11 11 18 11 18 18 11 18 11 11</gml:posList></gml:LinearRing></gml:exterior>"
        + "</gml:Polygon>"),
        "<gml:LinearRing xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:posList>"
        + "11 11 18 11 18 18 11 18 11 11</gml:posList></gml:LinearRing>");
    run(args(func, " <gml:Point><gml:pos>2 3</gml:pos></gml:Point>"), "");

    error(args(func, " text { 'a' }"), INVTYPE_X);
    error(args(func, " a"), NOCTX_X);
    error(args(func, " <gml:geo/>"), GEO_READ);
  }

  /** Test method. */
  @Test public void equals() {
    final String func = "equals";

    run(args(func, " <gml:LinearRing><gml:posList>1 1 2 1 5 3 1 1"
        + "</gml:posList></gml:LinearRing>",
        " <gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>1 1 2 1 5 3 1 1</gml:posList>"
        + "</gml:LinearRing></gml:exterior></gml:Polygon>"), false);

    error(args(func, " text { 'a' }", " a"), INVTYPE_X);
    error(args(func, " <gml:unknown/>", " <gml:LinearRing><gml:posList>"
        + "1 1 2 1 5 3 1 1</gml:posList></gml:LinearRing>"), GEO_READ);
    error(args(func, " <gml:LinearRing><gml:posList>1 1 55 99 2 1</gml:posList></gml:LinearRing>",
        " <gml:LineString><gml:posList>1 1 55 99 2 1</gml:posList></gml:LineString>"), GEO_READ);
  }

  /** Test method. */
  @Test public void disjoint() {
    final String func = "disjoint";

    run(args(func, " <gml:MultiCurve srsName='EPSG::4326'><gml:curveMember><gml:LineString>"
        + "<gml:posList>1 1 0 0 2 1</gml:posList></gml:LineString></gml:curveMember>"
        + "<gml:curveMember><gml:LineString><gml:posList>2 1 3 3 4 4</gml:posList>"
        + "</gml:LineString></gml:curveMember></gml:MultiCurve>",
        " <gml:LineString><gml:posList>0 0 2 1 3 3</gml:posList></gml:LineString>"), false);

    error(args(func, " a, text { 'a' }"), NOCTX_X);
    error(args(func, " <gml:Ring/>",
        " <gml:LinearRing><gml:posList>1 1 2 1 5 3 1 1</gml:posList></gml:LinearRing>"),
        GEO_READ);
    error(args(func, " <gml:LineString><gml:posList></gml:posList></gml:LineString>"),
        INVNARGS_X_X);
  }

  /** Test method. */
  @Test public void intersects() {
    final String func = "intersects";

    run(args(func, " <gml:MultiCurve srsName='EPSG::4326'><gml:curveMember><gml:LineString>"
        + "<gml:posList>1 1 0 0 2 1</gml:posList></gml:LineString></gml:curveMember>"
        + "<gml:curveMember><gml:LineString><gml:posList>2 1 3 3 4 4</gml:posList></gml:LineString>"
        + "</gml:curveMember></gml:MultiCurve>",
        " <gml:LineString><gml:posList>0 0 2 1 3 3</gml:posList></gml:LineString>"), true);

    error(args(func, " a, text { 'a' }"), NOCTX_X);
    error(args(func, " <gml:Point><gml:pos>10</gml:pos></gml:Point>",
        " <gml:LinearRing><gml:posList>1 1 2 1 5 3 1 1</gml:posList></gml:LinearRing>"),
        GEO_READ);
    error(args(func, " <gml:Point><gml:pos>1 1</gml:pos></gml:Point>",
        " <gml:Line><gml:posList>0 0 2 1 3 3</gml:posList></gml:Line>"),
        GEO_READ);
  }

  /** Test method. */
  @Test public void touches() {
    final String func = "touches";

    run(args(func, " <gml:MultiCurve srsName='EPSG::4326'><gml:curveMember><gml:LineString>"
        + "<gml:posList>1 1 0 0 2 1</gml:posList></gml:LineString></gml:curveMember>"
        + "<gml:curveMember><gml:LineString><gml:posList>2 1 3 3 4 4</gml:posList></gml:LineString>"
        + "</gml:curveMember></gml:MultiCurve>",
        " <gml:LineString><gml:posList>0 0 2 1 3 3</gml:posList></gml:LineString>"), false);

    error(args(func, " a, text { 'a' }"), NOCTX_X);
    error(args(func, " <gml:Point><gml:pos>10</gml:pos></gml:Point>",
        " <gml:LinearRing><gml:posList>1 1 2 1 5 3 1 1</gml:posList></gml:LinearRing>"), GEO_READ);
    error(args(func, " <gml:Point><gml:pos>1 1</gml:pos></gml:Point>",
        " <gml:Line><gml:posList>0 0 2 1 3 3</gml:posList></gml:Line>"), GEO_READ);
  }

  /** Test method. */
  @Test public void crosses() {
    final String func = "crosses";

    run(args(func, " <gml:Point><gml:pos>10 11</gml:pos></gml:Point>",
        " <gml:LineString><gml:posList>0 0 2 2</gml:posList></gml:LineString>"), false);

    error(args(func, " a, text { 'a' }"), NOCTX_X);
    error(args(func, " <gml:Point><gml:pos>10</gml:pos></gml:Point>",
        " <gml:LinearRing><gml:posList>1 1 2 1 5 3 1 1</gml:posList></gml:LinearRing>"), GEO_READ);
    error(args(func, " <gml:Point><gml:pos>1 1</gml:pos></gml:Point>"), INVNARGS_X_X);
  }

  /** Test method. */
  @Test public void within() {
    final String func = "within";

    run(args(func, " <gml:LinearRing><gml:posList>1 1 2 1 5 3 1 1</gml:posList></gml:LinearRing>",
        " <gml:LinearRing><gml:posList>1 1 20 1 50 30 1 1</gml:posList></gml:LinearRing>"),
        false);

    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " <gml:unknown><gml:pos>1 1</gml:pos></gml:unknown>",
        " <gml:LinearRing><gml:posList>1 1 2 1 5 3 1 1</gml:posList></gml:LinearRing>"), GEO_READ);
    error(args(func, " <gml:Point><gml:pos>1 1</gml:pos></gml:Point>"), INVNARGS_X_X);
  }

  /** Test method. */
  @Test public void contains() {
    final String func = "contains";

    run(args(func, " <gml:Point><gml:pos>1 1</gml:pos></gml:Point>",
        " <gml:Point><gml:pos>1 1</gml:pos></gml:Point>"), true);

    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " <gml:Point><gml:pos>1 1</gml:pos></gml:Point>",
        " <gml:Line><gml:posList>1 1 2 1 5 3 1 1</gml:posList></gml:Line>"), GEO_READ);
    error(args(func, " <gml:Point><gml:pos>1 1</gml:pos></gml:Point>"), INVNARGS_X_X);
  }

  /** Test method. */
  @Test public void overlaps() {
    final String func = "overlaps";

    run(args(func, " <gml:LineString><gml:posList>1 1 55 99 2 1</gml:posList></gml:LineString>",
        " <gml:LineString><gml:posList>1 1 55 0</gml:posList></gml:LineString>"), false);

    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " <gml:LineString><gml:posList>1 1 55 99 2 1</gml:posList></gml:LineString>",
        " <gml:LineString></gml:LineString>"), GEO_READ);
    error(args(func, " <gml:unknown><gml:pos>1 1</gml:pos></gml:unknown>"), INVNARGS_X_X);
  }

  /** Test method. */
  @Test public void relate() {
    final String func = "relate";

    run(args(func, " <gml:Point><gml:pos>18 11</gml:pos></gml:Point>",
        " <gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>10 10 20 10 30 40 20 40 10 10"
        + "</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon>", "0********"), true);

    error(args(func, " <gml:Point><gml:pos>18 11</gml:pos></gml:Point>",
        " <gml:LineString><gml:posList>11 10 20 1 20 20</gml:posList></gml:LineString>",
        "0******"), GEO_ARG);
    error(args(func, " <gml:Point><gml:pos>18 11</gml:pos></gml:Point>",
        " <gml:LineString><gml:posList>11 10 20 1 20 20</gml:posList></gml:LineString>",
        "0*******12*F"), GEO_ARG);
    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " <gml:Line><gml:posList>1 1 55 99 2 1</gml:posList></gml:Line>",
        " <gml:LineString></gml:LineString>", "0********"), GEO_READ);
    error(args(func, " <gml:Point><gml:pos>1,1</gml:pos></gml:Point>",
        "0********"), INVNARGS_X_X);
  }

  /** Test method. */
  @Test public void distance() {
    final String func = "distance";

    run(args(func, " <gml:LinearRing><gml:posList>10 400 20 200 30 100 20 100 10 400</gml:posList>"
        + "</gml:LinearRing>",
        " <gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>10 10 20 10 30 40 20 40 10 10"
        + "</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon>"), "60");

    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " <gml:LinearRing><gml:posList>1 1 55 99 2 1</gml:posList></gml:LinearRing>",
        " <gml:LineString/>"), GEO_READ);
    error(args(func, " <gml:Point><gml:pos>1 1</gml:pos></gml:Point>"), INVNARGS_X_X);
  }

  /** Test method. */
  @Test public void buffer() {
    final String func = "buffer";

    run(args(func, " <gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>"
        + "10 10 20 10 30 40 20 40 10 10</gml:posList></gml:LinearRing></gml:exterior>"
        + "</gml:Polygon>", " xs:double(0)"),
        "<gml:Polygon xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:exterior>"
        + "<gml:LinearRing><gml:posList>10 10 20 40 30 40 20 10 10 10</gml:posList>"
        + "</gml:LinearRing></gml:exterior></gml:Polygon>");
    run(args(func, " <gml:LineString><gml:posList>1 1 5 9 2 1</gml:posList></gml:LineString>",
        " xs:double(0)"), "");

    error(args(func, " <gml:LinearRing><gml:posList>1 1 55 99 2 1</gml:posList></gml:LinearRing>",
        " xs:double(1)"), GEO_READ);
    error(args(func, " xs:double(1)"), INVNARGS_X_X);
  }

  /** Test method. */
  @Test public void convexHull() {
    final String func = "convex-hull";

    run(args(func, " <gml:LinearRing><gml:posList>1 1 55 99 2 2 1 1</gml:posList></gml:LinearRing>"
        ),
        "<gml:Polygon xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:exterior>"
        + "<gml:LinearRing><gml:posList>1 1 55 99 2 2 1 1</gml:posList></gml:LinearRing>"
        + "</gml:exterior></gml:Polygon>");

    error(args(func, " <gml:LinearRing><gml:posList>1 1 1 1</gml:posList></gml:LinearRing>"),
        GEO_READ);
    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " <gml:LinearRing/>"), GEO_READ);
  }

  /** Test method. */
  @Test public void intersection() {
    final String func = "intersection";

    run(args(func, " <gml:LinearRing><gml:posList>1 1 55 99 2 3 1 1</gml:posList></gml:LinearRing>",
        " <gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>10 10 20 10 30 40 10 10"
        + "</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon>"), "");
    run(args(func, " <gml:LinearRing><gml:posList>1 1 55 99 2 3 1 1</gml:posList></gml:LinearRing>",
        " <gml:Point><gml:pos>2 3</gml:pos></gml:Point>"),
        "<gml:Point xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:pos>2 3"
        + "</gml:pos></gml:Point>");

    error(args(func, " <gml:LinearRing><gml:posList></gml:posList></gml:LinearRing>"),
        INVNARGS_X_X);
    error(args(func, " <gml:Geo><gml:pos>2 3</gml:pos></gml:Geo>,<gml:Point><gml:pos>2 3</gml:pos>"
        + "</gml:Point>"),
        GEO_READ);
    error(args(func, " <gml:LinearRing/>", " <gml:Point/>"), GEO_READ);
  }

  /** Test method. */
  @Test public void union() {
    final String func = "union";

    run(args(func, " <gml:Point><gml:pos>2 0</gml:pos></gml:Point>",
        " <gml:Point><gml:pos>2 3</gml:pos></gml:Point>"),
        "<gml:MultiPoint xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\">"
        + "<gml:pointMember><gml:Point><gml:pos>2 0</gml:pos></gml:Point></gml:pointMember>"
        + "<gml:pointMember><gml:Point><gml:pos>2 3</gml:pos></gml:Point></gml:pointMember>"
        + "</gml:MultiPoint>");

    run(args(func, " <gml:Point><gml:pos>2 0</gml:pos></gml:Point>",
        " <gml:Point><gml:pos>3 0</gml:pos></gml:Point>"),
        "<gml:MultiPoint xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\">"
        + "<gml:pointMember><gml:Point><gml:pos>2 0</gml:pos></gml:Point></gml:pointMember>"
        + "<gml:pointMember><gml:Point><gml:pos>3 0</gml:pos></gml:Point></gml:pointMember>"
        + "</gml:MultiPoint>");

    error(args(func, " <gml:Point><gml:pos></gml:pos></gml:Point>",
        " <gml:Point><gml:pos>2 3</gml:pos></gml:Point>"),
        GEO_READ);
    error(args(func, " text { 'a' }", " <gml:Point><gml:pos>2 3</gml:pos></gml:Point>"),
        INVTYPE_X);
  }

  /** Test method. */
  @Test public void difference() {
    final String func = "difference";

    run(args(func, " <gml:Point><gml:pos>20 1</gml:pos></gml:Point>",
        " <gml:LinearRing><gml:posList>0 0 20 20 20 30 0 20 0 0</gml:posList></gml:LinearRing>"),
        "<gml:Point xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:pos>20 1"
        + "</gml:pos></gml:Point>");

    error(args(func, " <gml:Point><gml:pos></gml:pos></gml:Point>",
        " <gml:Point><gml:pos>2 3</gml:pos></gml:Point>"), GEO_READ);
    error(args(func, " text { 'a' }", " <gml:Point><gml:pos>2 3</gml:pos></gml:Point>"),
        INVTYPE_X);
  }

  /** Test method. */
  @Test public void symDifference() {
    final String func = "sym-difference";

    run(args(func, " <gml:Point><gml:pos>20 1</gml:pos></gml:Point>",
        " <gml:LinearRing><gml:posList>0 0 20 20 20 30 0 20 0 0</gml:posList></gml:LinearRing>"),
        "<gml:MultiGeometry xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\">"
        + "<gml:geometryMember><gml:Point><gml:pos>20 1</gml:pos></gml:Point>"
        + "</gml:geometryMember><gml:geometryMember><gml:LineString><gml:posList>"
        + "0 0 20 20 20 30 0 20 0 0</gml:posList></gml:LineString></gml:geometryMember>"
        + "</gml:MultiGeometry>");

    error(args(func, " <gml:unknown><gml:pos>1 1</gml:pos></gml:unknown>",
        " <gml:Point><gml:pos>2 3</gml:pos></gml:Point>"), GEO_READ);
    error(args(func, " text { 'a' }", "<gml:Point><gml:pos>2 3</gml:pos></gml:Point>"),
        INVTYPE_X);
  }

  /** Test method. */
  @Test public void geometryN() {
    final String func = "geometry-n";

    run(args(func, " <gml:Point><gml:pos>2 1</gml:pos></gml:Point>", 1),
        "<gml:Point xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:pos>2 1"
        + "</gml:pos></gml:Point>");

    error(args(func, " <gml:unknown><gml:pos>1 1</gml:pos></gml:unknown>,1"), GEO_READ);
    error(args(func, " <gml:Point><gml:pos>2 1</gml:pos></gml:Point>, 0"), GEO_RANGE);
    error(args(func, " <gml:Point><gml:pos>2 1</gml:pos></gml:Point>, 2"), GEO_RANGE);
    error(args(func, " text { 'a' }", " <gml:Point><gml:pos>2 3</gml:pos></gml:Point>"),
        INVTYPE_X);
  }

  /** Test method. */
  @Test public void x() {
    final String func = "x";

    run(args(func, " <gml:Point><gml:pos>2 1</gml:pos></gml:Point>"), 2);
    run(args(func, " <gml:MultiPoint srsName='EPSG:4326'>"
        + "<gml:pointMember><gml:Point><gml:pos>1 1</gml:pos></gml:Point></gml:pointMember>"
        + "<gml:pointMember><gml:Point><gml:pos>1 2</gml:pos></gml:Point></gml:pointMember>"
        + "</gml:MultiPoint>"), 1);
    run(args(func, " <gml:LinearRing><gml:posList>0 0 20 0 0 20 0 0</gml:posList>"
        + "</gml:LinearRing>"), 0);

    error(args(func, " <gml:Point><gml:pos></gml:pos></gml:Point>"), GEO_READ);
    error(args(func, " <gml:geo><gml:pos>2 1</gml:pos></gml:geo>"), GEO_READ);
    error(args(func, " text { 'a' }"), INVTYPE_X);
  }

  /** Test method. */
  @Test public void y() {
    final String func = "y";

    run(args(func, " <gml:Point><gml:pos>2 1</gml:pos></gml:Point>"), 1);
    run(args(func, " <gml:Point><gml:pos>2 0</gml:pos></gml:Point>"), 0);
    run(args(func, " <gml:MultiPoint srsName='EPSG:4326'>"
        + "<gml:pointMember><gml:Point><gml:pos>1 1</gml:pos></gml:Point></gml:pointMember>"
        + "<gml:pointMember><gml:Point><gml:pos>1 2</gml:pos></gml:Point></gml:pointMember>"
        + "</gml:MultiPoint>"), 1);
    run(args(func, " <gml:LinearRing><gml:posList>0 0 20 0 0 20 0 0</gml:posList>"
        + "</gml:LinearRing>"), 0);

    error(args(func, " <gml:Point><gml:pos></gml:pos></gml:Point>"), GEO_READ);
    error(args(func, " <gml:geo><gml:pos>2 1</gml:pos></gml:geo>"), GEO_READ);
    error(args(func, " a"), NOCTX_X);
  }

  /** Test method. */
  @Test public void z() {
    final String func = "z";

    run(args(func, " <gml:Point><gml:pos>2 1 3</gml:pos></gml:Point>"), 3);
    run(args(func, " <gml:Point><gml:pos>2 0</gml:pos></gml:Point>"), "NaN");
    run(args(func, " <gml:MultiPoint srsName='EPSG:4326'>"
        + "<gml:pointMember><gml:Point><gml:pos>1 1</gml:pos></gml:Point></gml:pointMember>"
        + "<gml:pointMember><gml:Point><gml:pos>1 2</gml:pos></gml:Point></gml:pointMember>"
        + "</gml:MultiPoint>"), "NaN");
    run(args(func, " <gml:LinearRing><gml:posList>0 0 20 0 0 20 0 0</gml:posList>"
        + "</gml:LinearRing>"), "NaN");

    error(args(func, " <gml:Point><gml:pos></gml:pos></gml:Point>"), GEO_READ);
    error(args(func, " <gml:geo><gml:pos>2 1</gml:pos></gml:geo>"), GEO_READ);
    error(args(func, " a"), NOCTX_X);
  }

  /** Test method. */
  @Test public void length() {
    final String func = "length";

    run(args(func, " <gml:Point><gml:pos>2 1 3</gml:pos></gml:Point>"), 0);
    run(args(func, " <gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>1 1 2 1 5 3 1 1"
        + "</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon>"), "9.07768723046357");
    run(args(func, " <gml:MultiPoint srsName='EPSG:4326'><gml:pointMember><gml:Point><gml:pos>1 1"
        + "</gml:pos></gml:Point></gml:pointMember><gml:pointMember><gml:Point><gml:pos>1 2"
        + "</gml:pos></gml:Point></gml:pointMember></gml:MultiPoint>"), 0);

    error(args(func, " <gml:LinearRing><gml:posList>0 0 0 0</gml:posList></gml:LinearRing>"),
        GEO_READ);
    error(args(func, " <gml:Point><gml:pos></gml:pos></gml:Point>"),
        GEO_READ);
    error(args(func, " <gml:geo><gml:pos>2 1</gml:pos></gml:geo>"),
        GEO_READ);
  }

  /** Test method. */
  @Test public void startPoint() {
    final String func = "start-point";

    run(args(func, " <gml:LinearRing><gml:posList>1 1 20 1 20 20 1 1</gml:posList>"
        + "</gml:LinearRing>"),
        "<gml:Point xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:pos>1 1"
        + "</gml:pos></gml:Point>");
    run(args(func, " <gml:LineString><gml:posList>1 1 20 1 20 20 1 1</gml:posList>"
        + "</gml:LineString>"),
        "<gml:Point xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:pos>1 1"
        + "</gml:pos></gml:Point>");

    error(args(func, " <gml:MultiCurve srsName='EPSG::4326'><gml:curveMember><gml:LineString>"
        + "<gml:posList>1 1 0 0 1 2</gml:posList></gml:LineString></gml:curveMember>"
        + "<gml:curveMember><gml:LineString><gml:posList>1 2 3 3 4 4</gml:posList></gml:LineString>"
        + "</gml:curveMember></gml:MultiCurve>"),
        GEO_TYPE);
    error(args(func, " <gml:LineString><gml:posList>1 1</gml:posList></gml:LineString>"), GEO_READ);
    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " text { 'gml:Point' }"), INVTYPE_X);
    error(args(func, " <gml:geo><gml:pos>2 1</gml:pos></gml:geo>"), GEO_READ);
  }

  /** Test method. */
  @Test public void endPoint() {
    final String func = "end-point";

    run(args(func, " <gml:LinearRing><gml:posList>2 3 20 1 20 20 2 3</gml:posList>"
        + "</gml:LinearRing>"),
        "<gml:Point xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:pos>2 3"
        + "</gml:pos></gml:Point>");
    run(args(func, " <gml:LineString><gml:posList>11 10 20 1 20 20 12 13</gml:posList>"
        + "</gml:LineString>"),
        "<gml:Point xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:pos>12 13"
        + "</gml:pos></gml:Point>");

    error(args(func, " <gml:MultiCurve srsName='EPSG::4326'><gml:curveMember><gml:LineString>"
        + "<gml:posList>1 1 0 0 1 2</gml:posList></gml:LineString></gml:curveMember>"
        + "<gml:curveMember><gml:LineString><gml:posList>1 2 3 3 4 4</gml:posList></gml:LineString>"
        + "</gml:curveMember></gml:MultiCurve>"),
        GEO_TYPE);
    error(args(func, " <gml:LineString><gml:posList>1 1</gml:posList></gml:LineString>"), GEO_READ);
    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " text { 'gml:Point' }"), INVTYPE_X);
    error(args(func, " <gml:geo><gml:pos>2 1</gml:pos></gml:geo>"),
        GEO_READ);
  }

  /** Test method. */
  @Test public void isClosed() {
    final String func = "is-closed";

    run(args(func, " <gml:LinearRing><gml:posList>2 3 20 1 20 20 2 3</gml:posList>"
        + "</gml:LinearRing>"), true);
    run(args(func, " <gml:MultiCurve srsName='EPSG::4326'><gml:curveMember><gml:LineString>"
        + "<gml:posList>1 1 0 0 2 1</gml:posList></gml:LineString></gml:curveMember>"
        + "<gml:curveMember><gml:LineString><gml:posList>2 1 3 3 4 4</gml:posList></gml:LineString>"
        + "</gml:curveMember></gml:MultiCurve>"), false);
    run(args(func, " <gml:LineString><gml:posList>11 10 20 1 20 20 12 13</gml:posList>"
        + "</gml:LineString>"), false);

    error(args(func, " <gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>1 1 2 1 5 3 1 1"
        + "</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon>"), GEO_TYPE);
    error(args(func, " <gml:LineString><gml:posList>1 1</gml:posList></gml:LineString>"), GEO_READ);
    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " text { 'gml:Point' }"), INVTYPE_X);
    error(args(func, " <gml:Point><gml:pos>2 1</gml:pos></gml:Point>"), GEO_TYPE);
  }

  /** Test method. */
  @Test public void isRing() {
    final String func = "is-ring";

    run(args(func, " <gml:LinearRing><gml:posList>2 3 20 1 20 20 2 3</gml:posList>"
        + "</gml:LinearRing>"), true);
    run(args(func, " <gml:LineString><gml:posList>11 10 20 1 20 20 12 13</gml:posList>"
        + "</gml:LineString>"), false);

    error(args(func, " <gml:MultiCurve srsName='EPSG::4326'><gml:curveMember><gml:LineString>"
        + "<gml:posList>1 1 0 0 1 2</gml:posList></gml:LineString></gml:curveMember>"
        + "<gml:curveMember><gml:LineString><gml:posList>1 2 3 3 4 4</gml:posList></gml:LineString>"
        + "</gml:curveMember></gml:MultiCurve>"), GEO_TYPE);
    error(args(func, " <gml:Point><gml:pos>2 3</gml:pos></gml:Point>"), GEO_TYPE);
    error(args(func, " <gml:LineString><gml:posList>1 1</gml:posList></gml:LineString>"), GEO_READ);
    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " text { 'gml:Point' }"), INVTYPE_X);
    error(args(func, " <Point><gml:posList>2 1</gml:posList></Point>"), GEO_READ);
  }

  /** Test method. */
  @Test public void numPoints() {
    final String func = "num-points";

    run(args(func, " <gml:LinearRing><gml:posList>2 3 20 1 20 20 2 3</gml:posList>"
        + "</gml:LinearRing>"), 4);
    run(args(func, " <gml:LineString><gml:posList>11 10 20 1 20 20 12 13</gml:posList>"
        + "</gml:LineString>"), 4);
    run(args(func, " <gml:MultiCurve srsName='EPSG::4326'><gml:curveMember><gml:LineString>"
        + "<gml:posList>1 1 0 0 2 1</gml:posList></gml:LineString></gml:curveMember>"
        + "<gml:curveMember><gml:LineString><gml:posList>2 1 3 3 4 4</gml:posList></gml:LineString>"
        + "</gml:curveMember></gml:MultiCurve>"), 6);

    error(args(func, " <gml:LineString><gml:posList>1 1</gml:posList></gml:LineString>"), GEO_READ);
    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " text { 'gml:Point' }"), INVTYPE_X);
    error(args(func, " <Point>2 1</Point>"), GEO_READ);
  }

  /** Test method. */
  @Test public void pointN() {
    final String func = "point-n";

    run(args(func, " <gml:LinearRing><gml:posList>2 3 20 1 20 20 2 3</gml:posList>"
        + "</gml:LinearRing>, 1"),
        "<gml:Point xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:pos>2 3"
        + "</gml:pos></gml:Point>");
    run(args(func, " <gml:LineString><gml:posList>11 10 20 1 20 20 12 13</gml:posList>"
        + "</gml:LineString>, 4"),
        "<gml:Point xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:pos>12 13"
        + "</gml:pos></gml:Point>");

    error(args(func, " <gml:MultiCurve srsName='EPSG::4326'><gml:curveMember><gml:LineString>"
        + "<gml:posList>1 1 0 0 2 1</gml:posList><gml:curveMember></gml:curveMember>"
        + "</gml:LineString><gml:LineString><gml:posList>2 1 3 3 4 4</gml:posList></gml:LineString>"
        + "</gml:curveMember></gml:MultiCurve>, 4"), GEO_TYPE);
    error(args(func, " <gml:unknown><gml:pos>1 1</gml:pos></gml:unknown>,1"), GEO_READ);
    error(args(func, " <gml:LineString><gml:posList>11 10 20 1 20 20 12 13</gml:posList>"
        + "</gml:LineString>, 5"), GEO_RANGE);
    error(args(func, " <gml:LineString><gml:posList>11 10 20 1 20 20 12 13</gml:posList>"
        + "</gml:LineString>, 0"), GEO_RANGE);
    error(args(func, " text { 'a' },3"), INVTYPE_X);
  }

  /** Test method. */
  @Test public void area() {
    final String func = "area";

    run(args(func, " <gml:MultiPoint srsName='EPSG:4326'><gml:pointMember><gml:Point><gml:pos>1 1"
        + "</gml:pos></gml:Point></gml:pointMember><gml:pointMember><gml:Point><gml:pos>1 2"
        + "</gml:pos></gml:Point></gml:pointMember></gml:MultiPoint>"), 0);
    run(args(func, " <gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>"
        + "11 11 18 11 18 18 11 18 11 11</gml:posList></gml:LinearRing></gml:exterior>"
        + "</gml:Polygon>"), "49");
    run(args(func, " <gml:LineString><gml:posList>11 10 20 1 20 20</gml:posList></gml:LineString>"),
        0);

    error(args(func, " <gml:LinearRing><gml:posList>0 0 0 0</gml:posList></gml:LinearRing>"),
        GEO_READ);
    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " <gml:geo><gml:pos>2 1</gml:pos></gml:geo>"), GEO_READ);
  }

  /** Test method. */
  @Test public void centroid() {
    final String func = "centroid";

    run(args(func, " <gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>"
        + "11 11 18 11 18 18 11 18 11 11</gml:posList></gml:LinearRing></gml:exterior>"
        + "</gml:Polygon>"),
        "<gml:Point xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:pos>14.5 14.5"
        + "</gml:pos></gml:Point>");
    run(args(func, " <gml:Point><gml:pos>2 3</gml:pos></gml:Point>"),
        "<gml:Point xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:pos>2 3"
        + "</gml:pos></gml:Point>");
    run(args(func, " <gml:MultiCurve srsName='EPSG::4326'><gml:curveMember><gml:LineString>"
        + "<gml:posList>1 1 0 0 2 1</gml:posList></gml:LineString></gml:curveMember>"
        + "<gml:curveMember><gml:LineString><gml:posList>2 1 3 3 4 4</gml:posList></gml:LineString>"
        + "</gml:curveMember></gml:MultiCurve>"),
        "<gml:Point xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:pos>"
        + "1.8468564716806983 1.540569415042095</gml:pos></gml:Point>");

    error(args(func, " <gml:unknown><gml:pos>1 1</gml:pos></gml:unknown>"), GEO_READ);
    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " text { 'a' }"), INVTYPE_X);
  }

  /** Test method. */
  @Test public void pointOnSurface() {
    final String func = "point-on-surface";

    run(args(func, " <gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>"
        + "11 11 18 11 18 18 11 18 11 11</gml:posList></gml:LinearRing></gml:exterior>"
        + "</gml:Polygon>"),
        "<gml:Point xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:pos>14.5 14.5"
        + "</gml:pos></gml:Point>");
    run(args(func, " <gml:Point><gml:pos>2 3</gml:pos></gml:Point>"),
        "<gml:Point xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:pos>2 3"
        + "</gml:pos></gml:Point>");
    run(args(func, " <gml:MultiCurve srsName='EPSG::4326'><gml:curveMember><gml:LineString>"
        + "<gml:posList>1 1 0 0 2 1</gml:posList></gml:LineString></gml:curveMember>"
        + "<gml:curveMember><gml:LineString><gml:posList>2 1 3 3 4 4</gml:posList></gml:LineString>"
        + "</gml:curveMember></gml:MultiCurve>"),
        "<gml:Point xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:pos>3 3"
        + "</gml:pos></gml:Point>");

    error(args(func, " <gml:unknown><gml:pos>1 1</gml:pos></gml:unknown>"), GEO_READ);
    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " text { 'a' }"), INVTYPE_X);
  }

  /** Test method. */
  @Test public void exteriorRing() {
    final String func = "exterior-ring";

    run(args(func, " <gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>"
        + "11 11 18 11 18 18 11 18 11 11</gml:posList></gml:LinearRing></gml:exterior>"
        + "</gml:Polygon>"),
        "<gml:LinearRing xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:posList>"
        + "11 11 18 11 18 18 11 18 11 11</gml:posList></gml:LinearRing>");

    error(args(func, " <gml:Point><gml:pos>2.0 3.0</gml:pos></gml:Point>"), GEO_TYPE);
    error(args(func, " <gml:unknown><gml:pos>1 1</gml:pos></gml:unknown>"), GEO_READ);
    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " text { 'a' }"), INVTYPE_X);
  }

  /** Test method. */
  @Test public void numInteriorRing() {
    final String func = "num-interior-ring";

    run(args(func, " <gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>"
        + "11 11 18 11 18 18 11 18 11 11</gml:posList></gml:LinearRing></gml:exterior>"
        + "</gml:Polygon>"), 0);

    error(args(func, " <gml:Point><gml:pos>2.0 3.0</gml:pos></gml:Point>"), GEO_TYPE);
    error(args(func, " <gml:unknown><gml:pos>1 1</gml:pos></gml:unknown>"), GEO_READ);
    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " text { 'a' }"), INVTYPE_X);
  }

  /** Test method. */
  @Test public void interiorRingN() {
    final String func = "interior-ring-n";

    run(args(func, " <gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>"
        + "11 11 18 11 18 18 11 18 11 11</gml:posList></gml:LinearRing></gml:exterior>"
        + "<gml:interior><gml:LinearRing><gml:posList>2 2 3 2 3 3 2 3 2 2</gml:posList>"
        + "</gml:LinearRing></gml:interior><gml:interior><gml:LinearRing><gml:posList>"
        + "10 10 20 10 20 20 10 20 10 10</gml:posList></gml:LinearRing></gml:interior>"
        + "</gml:Polygon>, 1"),
        "<gml:LinearRing xmlns:gml=\"" + gmlUri() + "\" srsDimension=\"2\"><gml:posList>"
        + "2 2 3 2 3 3 2 3 2 2</gml:posList></gml:LinearRing>");

    error(args(func, " <gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>"
        + "11 11 18 11 18 18 11 18 11 11</gml:posList></gml:LinearRing></gml:exterior>"
        + "</gml:Polygon>, 1"),
        GEO_RANGE);
    error(args(func, " <gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>"
        + "11 11 18 11 18 18 11 18 11 11</gml:posList></gml:LinearRing></gml:exterior>"
        + "</gml:Polygon>, 0"),
        GEO_RANGE);
    error(args(func, " <gml:Point><gml:pos>2.0 3.0</gml:pos></gml:Point>, 1"), GEO_TYPE);
    error(args(func, " text { '<gml:Polygon/' }, 1"), INVTYPE_X);
    error(args(func, " "), INVNARGS_X_X);
  }

  /** Test method. */
  @Test public void numGeometries() {
    final String func = "num-geometries";

    run(args(func, " <gml:MultiCurve><gml:curveMember><gml:LineString><gml:posList>1 1 0 0 2 1"
        + "</gml:posList></gml:LineString></gml:curveMember><gml:curveMember><gml:LineString>"
        + "<gml:posList>2 1 3 3 4 4</gml:posList></gml:LineString></gml:curveMember>"
        + "</gml:MultiCurve>"), 2);

    error(args(func, " <gml:unknown/>"), GEO_READ);
    error(args(func, " "), INVNARGS_X_X);
    error(args(func, " text { 'a' }"), INVTYPE_X);
  }

  /** Test method. */
  @Test public void srsName() {
    run("let $polygon :=\r"
        + "  <gml:Polygon srsName='urn:ogc:def:crs:EPSG::28992'>\r"
        + "    <gml:exterior>\r"
        + "      <gml:LinearRing>\r"
        + "        <gml:posList srsDimension='2'>138395.204 455996.240 138388.284 456025.713 138388"
        + ".051 456025.656 138394.974 455996.172 138395.204 455996.240</gml:posList>\r"
        + "      </gml:LinearRing>\r"
        + "    </gml:exterior>\r"
        + "  </gml:Polygon>\r"
        + "return geo:area($polygon)",
        7.256866500061102);
  }
}
