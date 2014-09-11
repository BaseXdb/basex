package org.expath.ns;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.build.xml.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.io.gml2.*;

/**
 * This module contains geo spatial functions for the Geo module.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Masoumeh Seydi
 */
public final class Geo extends QueryModule {
  /** GML URI. */
  private static final byte[] URI = token("http://www.opengis.net/gml");
  /** Prefix: "gml". */
  private static final byte[] GML = token("gml");

  /** QName gml:Point. */
  private static final QNm Q_GML_POINT = QNm.get(GML, "Point", URI);
  /** QName gml:MultiPoint. */
  private static final QNm Q_GML_MULTIPOINT = QNm.get(GML, "MultiPoint", URI);
  /** QName gml:LineString. */
  private static final QNm Q_GML_LINESTRING = QNm.get(GML, "LineString", URI);
  /** QName gml:LinearRing. */
  private static final QNm Q_GML_LINEARRING = QNm.get(GML, "LinearRing", URI);
  /** QName gml:Polygon. */
  private static final QNm Q_GML_POLYGON = QNm.get(GML, "Polygon", URI);
  /** QName gml:MultiPolygon. */
  private static final QNm Q_GML_MULTIPOLYGON = QNm.get(GML, "MultiPolygon", URI);
  /** QName gml:MultiLineString. */
  private static final QNm Q_GML_MULTILINESTRING = QNm.get(GML, "MultiLineString", URI);

  /** Array containing all QNames. */
  private static final QNm[] QNAMES = {
    Q_GML_POINT, Q_GML_LINESTRING, Q_GML_POLYGON, Q_GML_MULTIPOINT,
    Q_GML_MULTILINESTRING, Q_GML_MULTIPOLYGON, Q_GML_LINEARRING
  };

  /**
   * Returns the dimension of an item.
   * @param node xml element containing gml object(s)
   * @return dimension
   * @throws QueryException query exception
   */
  @Deterministic
  public Int dimension(final ANode node) throws QueryException {
    return Int.get(checkGeo(node).getDimension());
  }

  /**
   * Returns the name of the geometry type in the GML namespace, or the empty sequence.
   * @param node xml element containing gml object(s)
   * @return geometry type
   * @throws QueryException query exception
   */
  @Deterministic
  public QNm geometryType(final ANode node) throws QueryException {
    return QNm.get(GML, checkGeo(node).getGeometryType(), URI);
  }

  /**
   * Returns the name of the geometry type in the GML namespace, or the empty sequence.
   * @param node xml element containing gml object(s)
   * @return integer value of CRS of the geometry
   * @throws QueryException query exception
   */
  @Deterministic
  public Uri srid(final ANode node) throws QueryException {
    return Uri.uri(token(checkGeo(node).getSRID()));
  }

  /**
   * Returns the gml:Envelope of the specified geometry.
   * The envelope is the minimum bounding box of this geometry.
   * @param node xml element containing gml object(s)
   * @return envelop element
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode envelope(final ANode node) throws QueryException {
    return gmlWriter(checkGeo(node).getEnvelope());
  }

  /**
   * Returns the WKT format of a geometry.
   * @param node xml element containing gml object(s)
   * @return Well-Known Text geometry representation
   * @throws QueryException query exception
   */
  @Deterministic
  public Str asText(final ANode node) throws QueryException {
    return Str.get(new WKTWriter().write(checkGeo(node)));
  }

  /**
   * Returns the WKB format of a geometry.
   * @param node xml element containing gml object(s)
   * @return Well-Known Binary geometry representation
   * @throws QueryException query exception
   */
  @Deterministic
  public B64 asBinary(final ANode node) throws QueryException {
    return new B64(new WKBWriter().write(checkGeo(node)));
  }

  /**
   * Returns a boolean value which shows if the specified geometry is empty or not.
   * @param node xml element containing gml object(s)
   * @return boolean value
   * @throws QueryException query exception
   */
  @Deterministic
  public Bln isEmpty(final ANode node) throws QueryException {
    return Bln.get(node != null && checkGeo(node) != null);
  }

  /**
   * Returns a boolean value which shows if the specified geometry is simple or not,
   * which has no anomalous geometric points, such as self intersection or self tangency.
   * @param node xml element containing gml object(s)
   * @return boolean value
   * @throws QueryException query exception
   */
  @Deterministic
  public Bln isSimple(final ANode node) throws QueryException {
    return Bln.get(checkGeo(node).isSimple());
  }

  /**
   * Returns the boundary of the geometry, in GML.
   * The return value is a sequence of either gml:Point or gml:LinearRing elements.
   * @param node xml element containing gml object(s)
   * @return boundary element (geometry)
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode boundary(final ANode node) throws QueryException {
    return gmlWriter(checkGeo(node).getBoundary());
  }

  /**
   * Returns a boolean value that shows if two geometries are equal or not.
   * @param node1 xml element containing gml object(s)
   * @param node2 xml element containing gml object(s)
   * @return boolean value
   * @throws QueryException query exception
   */
  @Deterministic
  public Bln equals(final ANode node1, final ANode node2) throws QueryException {
    final Geometry geo1 = checkGeo(node1);
    final Geometry geo2 = checkGeo(node2);
    return Bln.get(geo1.equals(geo2));
  }

  /**
   * Returns a boolean value that shows if this geometry is disjoint to another geometry.
   * @param node1 xml element containing gml object(s)
   * @param node2 xml element containing gml object(s)
   * @return boolean value
   * @throws QueryException query exception
   */
  @Deterministic
  public Bln disjoint(final ANode node1, final ANode node2) throws QueryException {
    final Geometry geo1 = checkGeo(node1);
    final Geometry geo2 = checkGeo(node2);
    return Bln.get(geo1.disjoint(geo2));
  }

  /**
   * Returns a boolean value that shows if this geometry intersects another geometry.
   * @param node1 xml element containing gml object(s)
   * @param node2 xml element containing gml object(s)
   * @return boolean value
   * @throws QueryException query exception
   */
  @Deterministic
  public Bln intersects(final ANode node1, final ANode node2) throws QueryException {
    final Geometry geo1 = checkGeo(node1);
    final Geometry geo2 = checkGeo(node2);
    return Bln.get(geo1.intersects(geo2));
  }

  /**
   * Returns a boolean value that shows if this geometry touches the specified geometry.
   * @param node1 xml element containing gml object(s)
   * @param node2 xml element containing gml object(s)
   * @return boolean value
   * @throws QueryException query exception
   */
  @Deterministic
  public Bln touches(final ANode node1, final ANode node2) throws QueryException {
    final Geometry geo1 = checkGeo(node1);
    final Geometry geo2 = checkGeo(node2);
    return Bln.get(geo1.touches(geo2));
  }

  /**
   * Returns a boolean value that shows if this geometry crosses the specified geometry.
   * @param node1 xml element containing gml object(s)
   * @param node2 xml element containing gml object(s)
   * @return boolean value
   * @throws QueryException query exception
   */
  @Deterministic
  public Bln crosses(final ANode node1, final ANode node2) throws QueryException {
    final Geometry geo1 = checkGeo(node1);
    final Geometry geo2 = checkGeo(node2);
    return Bln.get(geo1.crosses(geo2));
  }

  /**
   * Returns a boolean value that shows if this geometry is within the specified geometry.
   * @param node1 xml element containing gml object(s)
   * @param node2 xml element containing gml object(s)
   * @return boolean value
   * @throws QueryException query exception
   */
  @Deterministic
  public Bln within(final ANode node1, final ANode node2) throws QueryException {
    final Geometry geo1 = checkGeo(node1);
    final Geometry geo2 = checkGeo(node2);
    return Bln.get(geo1.within(geo2));
  }

  /**
   * Returns a boolean value that shows if this geometry contains the specified geometry.
   * @param node1 xml element containing gml object(s)
   * @param node2 xml element containing gml object(s)
   * @return boolean value
   * @throws QueryException query exception
   */
  @Deterministic
  public Bln contains(final ANode node1, final ANode node2) throws QueryException {
    final Geometry geo1 = checkGeo(node1);
    final Geometry geo2 = checkGeo(node2);
    return Bln.get(geo1.contains(geo2));
  }

  /**
   * Returns a boolean value that shows if this geometry overlaps the specified geometry.
   * @param node1 xml element containing gml object(s)
   * @param node2 xml element containing gml object(s)
   * @return boolean value
   * @throws QueryException query exception
   */
  @Deterministic
  public Bln overlaps(final ANode node1, final ANode node2) throws QueryException {
    final Geometry geo1 = checkGeo(node1);
    final Geometry geo2 = checkGeo(node2);
    return Bln.get(geo1.overlaps(geo2));
  }

  /**
   * Returns a boolean value that shows if whether relationships between the boundaries,
   * interiors and exteriors of two geometries match
   * the pattern specified in intersection-matrix-pattern.
   * @param node1 xml element containing gml object(s)
   * @param node2 xml element containing gml object(s)
   * @param intersectionMatrix intersection matrix for two geometries
   * @return boolean value
   * @throws QueryException query exception
   */
  @Deterministic
  public Bln relate(final ANode node1, final ANode node2, final Str intersectionMatrix)
      throws QueryException {
    final Geometry geo1 = checkGeo(node1);
    final Geometry geo2 = checkGeo(node2);
    return Bln.get(geo1.relate(geo2, intersectionMatrix.toJava()));
  }

  /**
   * Returns the shortest distance in the units of the spatial reference system
   * of geometry, between the geometries.
   * The distance is the distance between a point on each of the geometries.
   * @param node1 xml element containing gml object(s)
   * @param node2 xml element containing gml object(s)
   * @return distance double value
   * @throws QueryException query exception
   */
  @Deterministic
  public Dbl distance(final ANode node1, final ANode node2) throws QueryException {
    final Geometry geo1 = checkGeo(node1);
    final Geometry geo2 = checkGeo(node2);
    return Dbl.get(geo1.distance(geo2));
  }

  /**
   * Returns a polygon that represents all Points whose distance from this
   * geometric object is less than or equal to distance.
   * The returned element must be either gml:Polygon, gml:LineString or gml:Point.
   * @param node xml element containing gml object(s)
   * @param distance specific distance from the $geometry (the buffer width)
   * @return buffer geometry as gml element
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode buffer(final ANode node, final Dbl distance) throws QueryException {
    return gmlWriter(checkGeo(node).buffer(distance.dbl()));
  }

  /**
   * Returns the convex hull geometry of a geometry in GML, or the empty sequence.
   * The returned element must be either gml:Polygon, gml:LineString or gml:Point.
   * @param node xml element containing gml object(s)
   * @return convex hull geometry as a gml element
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode convexHull(final ANode node) throws QueryException {
    return gmlWriter(checkGeo(node).convexHull());
  }

  /**
   * Returns a geometric object representing the Point set intersection of two geometries.
   * @param node1 xml element containing gml object(s)
   * @param node2 xml element containing gml object(s)
   * @return intersection geometry as a gml element
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode intersection(final ANode node1, final ANode node2) throws QueryException {
    final Geometry geo1 = checkGeo(node1);
    final Geometry geo2 = checkGeo(node2);
    return gmlWriter(geo1.intersection(geo2));
  }

  /**
   * Returns a geometric object that represents the Point set union of two geometries.
   * @param node1 xml element containing gml object(s)
   * @param node2 xml element containing gml object(s)
   * @return union geometry as a gml element
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode union(final ANode node1, final ANode node2) throws QueryException {
    final Geometry geo1 = checkGeo(node1);
    final Geometry geo2 = checkGeo(node2);
    return gmlWriter(geo1.union(geo2));
  }

  /**
   * Returns a geometric object that represents the
   * Point set difference of two geometries.
   * @param node1 xml element containing gml object(s)
   * @param node2 xml element containing gml object(s)
   * @return difference geometry as a gml element
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode difference(final ANode node1, final ANode node2) throws QueryException {
    final Geometry geo1 = checkGeo(node1);
    final Geometry geo2 = checkGeo(node2);
    return gmlWriter(geo1.difference(geo2));
  }

  /**
   * Returns a geometric object that represents the
   * Point set symmetric difference of two geometries.
   * @param node1 xml element containing gml object(s)
   * @param node2 xml element containing gml object(s)
   * @return symmetric difference geometry as a gml element
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode symDifference(final ANode node1, final ANode node2) throws QueryException {
    final Geometry geo1 = checkGeo(node1);
    final Geometry geo2 = checkGeo(node2);
    return gmlWriter(geo1.symDifference(geo2));
  }

  /**
   * Returns number of geometries in a geometry collection,
   * or 1 if the input is not a collection.
   * @param node xml element containing gml object(s)
   * @return integer value of number of geometries
   * @throws QueryException query exception
   */
  @Deterministic
  public Int numGeometries(final ANode node) throws QueryException {
    return Int.get(checkGeo(node).getNumGeometries());
  }

  /**
   * Returns the nth geometry of a geometry collection,
   * or the geometry if the input is not a collection.
   * @param node xml element containing gml object(s)
   * @param number integer number as the index of nth geometry
   * @return geometry as a gml element
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode geometryN(final ANode node, final Int number) throws QueryException {
    final Geometry geo = checkGeo(node);
    final long n = number.itr();
    if(n < 1 || n > geo.getNumGeometries()) throw GeoErrors.outOfRangeIdx(number);
    return gmlWriter(geo.getGeometryN((int) n - 1));
  }

  /**
   * Returns the x-coordinate value for point.
   * @param node xml element containing gml object(s)
   * @return x double value
   * @throws QueryException query exception
   */
  @Deterministic
  public Dbl x(final ANode node) throws QueryException {
    final Geometry geo = geo(node, Q_GML_POINT);
    if(geo == null && checkGeo(node) != null)
      throw GeoErrors.geoType(node.qname().local(), "Point");

    return Dbl.get(geo.getCoordinate().x);
  }

  /**
   * Returns the y-coordinate value for point.
   * @param node xml element containing gml object(s)
   * @return y double value
   * @throws QueryException query exception
   */
  @Deterministic
  public Dbl y(final ANode node) throws QueryException {
    final Geometry geo = geo(node, Q_GML_POINT);
    if(geo == null && checkGeo(node) != null)
      throw GeoErrors.geoType(node.qname().local(), "Point");

    return Dbl.get(geo.getCoordinate().y);
  }

  /**
   * Returns the z-coordinate value for point.
   * @param node xml element containing gml object(s)
   * @return z double value
   * @throws QueryException query exception
   */
  @Deterministic
  public Dbl z(final ANode node) throws QueryException {
    final Geometry geo = geo(node, Q_GML_POINT);
    if(geo == null && checkGeo(node) != null)
      throw GeoErrors.geoType(node.qname().local(), "Line");

    return Dbl.get(geo.getCoordinate().z);
  }

  /**
   * Returns the length of this Geometry. Linear geometries return their length.
   * Areal geometries return their parameter. Others return 0.0
   * @param node xml element containing gml object(s)
   * @return length double value
   * @throws QueryException query exception
   */
  @Deterministic
  public Dbl length(final ANode node) throws QueryException {
    return Dbl.get(checkGeo(node).getLength());
  }

  /**
   * Returns the start point of a line.
   * @param node xml element containing gml object(s)
   * @return start point geometry as a gml element
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode startPoint(final ANode node) throws QueryException {
    final Geometry geo = geo(node, Q_GML_LINEARRING, Q_GML_LINESTRING);
    if(geo == null && checkGeo(node) != null)
      throw GeoErrors.geoType(node.qname().local(), "Line");

    return gmlWriter(((LineString) geo).getStartPoint());
  }

  /**
   * Returns the end point of a line.
   * @param node xml element containing gml object(s)
   * @return end point geometry as a gml element
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode endPoint(final ANode node) throws QueryException {
    final Geometry geo = geo(node, Q_GML_LINEARRING, Q_GML_LINESTRING);
    if(geo == null && checkGeo(node) != null)
      throw GeoErrors.geoType(node.qname().local(), "Line");

    return gmlWriter(((LineString) geo).getEndPoint());
  }

  /**
   * Checks if the line is closed loop.
   * That is, if the start Point is same with end Point.
   * @param node xml element containing gml object(s)
   * @return boolean value
   * @throws QueryException query exception
   */
  @Deterministic
  public Bln isClosed(final ANode node) throws QueryException {
    final Geometry geo = geo(node, Q_GML_LINEARRING, Q_GML_LINESTRING, Q_GML_MULTILINESTRING);
    if(geo == null && checkGeo(node) != null)
      throw GeoErrors.geoType(node.qname().local(), "Line");

    return Bln.get(geo instanceof LineString ? ((LineString) geo).isClosed() :
      ((MultiLineString) geo).isClosed());
  }

  /**
   * Return a boolean value that shows weather the line is a ring or not.
   * A line is a ring if it is closed and simple.
   * @param node xml element containing gml object(s)
   * @return boolean value
   * @throws QueryException query exception
   */
  @Deterministic
  public Bln isRing(final ANode node) throws QueryException {
    final Geometry geo = geo(node, Q_GML_LINEARRING, Q_GML_LINESTRING);
    if(geo == null && checkGeo(node) != null)
      throw GeoErrors.geoType(node.qname().local(), "Line");

    return Bln.get(((LineString) geo).isRing());
  }

  /**
   * Returns the number of points in a geometry.
   * @param node xml element containing gml object(s)
   * @return number of points int value
   * @throws QueryException query exception
   */
  @Deterministic
  public Int numPoints(final ANode node) throws QueryException {
    return Int.get(checkGeo(node).getNumPoints());
  }

  /**
   * Returns the nth point of a line.
   * @param node xml element containing gml object(s)
   * @param number index of i-th point
   * @return n-th point as a gml element
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode pointN(final ANode node, final Int number) throws QueryException {
    final Geometry geo = geo(node, Q_GML_LINEARRING, Q_GML_LINESTRING);
    if(geo == null && checkGeo(node) != null)
      throw GeoErrors.geoType(node.qname().local(), "Line");

    final int max = geo.getNumPoints();
    final long n = number.itr();
    if(n < 1 || n > max) throw GeoErrors.outOfRangeIdx(number);

    return gmlWriter(((LineString) geo).getPointN((int) n - 1));
  }

  /**
   * Returns the area of a Geometry. Areal Geometries have a non-zero area.
   * Returns zero for Point and Lines.
   * @param node xml element containing gml object(s)
   * @return geometry area as a double vaue
   * @throws QueryException query exception
   */
  @Deterministic
  public Dbl area(final ANode node) throws QueryException {
    return Dbl.get(checkGeo(node).getArea());
  }

  /**
   * Returns the mathematical centroid of the geometry as a gml:Point.
   * The point is not guaranteed to be on the surface.
   * @param node xml element containing gml object(s)
   * @return centroid geometry as a gml element
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode centroid(final ANode node) throws QueryException {
    return gmlWriter(checkGeo(node).getCentroid());
  }

  /**
   * Returns a gml:Point that is interior of this geometry.
   * If it cannot be inside the geometry, then it will be on the boundary.
   * @param node xml element containing gml object(s)
   * @return a point as a gml element
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode pointOnSurface(final ANode node) throws QueryException {
    return gmlWriter(checkGeo(node).getInteriorPoint());
  }

  /**
   * Returns the outer ring of a polygon, in GML.
   * @param node xml element containing gml object(s)
   * @return exterior ring geometry (LineString) as a gml element
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode exteriorRing(final ANode node) throws QueryException {
    final Geometry geo = geo(node, Q_GML_POLYGON);
    if(geo == null && checkGeo(node) != null)
      throw GeoErrors.geoType(node.qname().local(), "Polygon");

    return gmlWriter(((Polygon) geo).getExteriorRing());
  }

  /**
   * Returns the number of interior rings in a polygon.
   * @param node xml element containing gml object(s)
   * @return integer number of interior rings
   * @throws QueryException query exception
   */
  @Deterministic
  public Int numInteriorRing(final ANode node) throws QueryException {
    final Geometry geo = geo(node, Q_GML_POLYGON);
    if(geo == null && checkGeo(node) != null)
      throw GeoErrors.geoType(node.qname().local(), "Polygon");

    return Int.get(((Polygon) geo).getNumInteriorRing());
  }

  /**
   * Returns the nth geometry of a geometry collection.
   * @param node xml element containing gml object(s)
   * @param number index of i-th interior ring
   * @return n-th interior ring geometry (LineString) as a gml element
   * @throws QueryException query exception
   */
  @Deterministic
  public ANode interiorRingN(final ANode node, final Int number) throws QueryException {
    final Geometry geo = geo(node, Q_GML_POLYGON);
    if(geo == null && checkGeo(node) != null)
      throw GeoErrors.geoType(node.qname().local(), "Polygon");

    final long n = number.itr();
    final int max = ((Polygon) geo).getNumInteriorRing();
    if(n < 1 || n > max) throw GeoErrors.outOfRangeIdx(number);
    return gmlWriter(((Polygon) geo).getInteriorRingN((int) n - 1));
  }

  // PRIVATE METHODS (hidden from user of module) ========================================

  /**
   * Reads an element as a gml node. Returns a geometry element
   * or throws an exception if the element is of the wrong type.
   * @param node xml node containing gml object(s)
   * @return geometry
   * @throws QueryException query exception
   */
  private static Geometry checkGeo(final ANode node) throws QueryException {
    final Geometry geo = geo(node, QNAMES);
    if(geo == null) throw GeoErrors.unrecognizedGeo(node.qname().local());
    return geo;
  }

  /**
   * Reads an element as a gml node. Returns a geometry element
   * or {@code null} if the element does not match one of the specified types.
   * @param node xml node containing gml object(s)
   * @param names allowed geometry types
   * @return geometry, or {@code null}
   * @throws QueryException query exception
   */
  private static Geometry geo(final ANode node, final QNm... names) throws QueryException {
    if(node.type != NodeType.ELM) throw EXPTYPE_X_X_X.get(null, NodeType.ELM, node.type, node);

    final QNm qname = node.qname();
    for(final QNm geo : names) {
      if(!qname.eq(geo)) continue;
      // type found... create reader and geometry element
      try {
        final String input = node.serialize().toString();
        final GMLReader gmlReader = new GMLReader();
        final GeometryFactory geoFactory = new GeometryFactory();
        return gmlReader.read(input, geoFactory);
      } catch(final Throwable ex) {
        throw GeoErrors.gmlReaderErr(ex);
      }
    }
    return null;
  }

  /**
   * Writes an geometry and returns a string representation of the geometry.
   * @param geometry geometry
   * @return DBNode database node
   * @throws QueryException exception
   */
  private DBNode gmlWriter(final Geometry geometry) throws QueryException {
    final String geo;
    try {
      // write geometry and add namespace declaration
      geo = new GMLWriter().write(geometry).replaceAll(
          "^<gml:(.*?)>", "<gml:$1 xmlns:gml='" + string(URI) + "'>");
    } catch(final Exception ex) {
      throw GeoErrors.gmlWriterErr(ex);
    }

    try {
      final IO io = new IOContent(geo);
      return new DBNode(MemBuilder.build(new XMLParser(io, queryContext.context.options)));
    } catch(final IOException ex) {
      throw IOERR_X.get(null, ex);
    }
  }
}