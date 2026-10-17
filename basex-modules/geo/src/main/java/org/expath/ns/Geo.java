package org.expath.ns;

import java.util.function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.geo.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;

/**
 * Java bindings for the Geo XQuery module (namespace {@code http://expath.org/ns/geo}).
 *
 * <p>This class delegates all function calls to BaseX Geo function implementations.</p>
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public class Geo extends QueryModule {
  /**
   * Implementation of {@code geo:area($geometry)}.
   *
   * @param geometry geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item area(final GNode geometry) throws QueryException {
    return call(GeoArea::new, geometry);
  }

  /**
   * Implementation of {@code geo:as-binary($geometry)}.
   *
   * @param geometry geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item asBinary(final GNode geometry) throws QueryException {
    return call(GeoAsBinary::new, geometry);
  }

  /**
   * Implementation of {@code geo:as-text($geometry)}.
   *
   * @param geometry geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item asText(final GNode geometry) throws QueryException {
    return call(GeoAsText::new, geometry);
  }

  /**
   * Implementation of {@code geo:boundary($geometry)}.
   *
   * @param geometry geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item boundary(final GNode geometry) throws QueryException {
    return call(GeoBoundary::new, geometry);
  }

  /**
   * Implementation of {@code geo:buffer($geometry, $distance)}.
   *
   * @param geometry geometry element
   * @param distance buffer distance
   * @return function result
   * @throws QueryException query exception
   */
  public Item buffer(final GNode geometry, final Dbl distance) throws QueryException {
    return call(GeoBuffer::new, geometry, distance);
  }

  /**
   * Implementation of {@code geo:centroid($geometry)}.
   *
   * @param geometry geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item centroid(final GNode geometry) throws QueryException {
    return call(GeoCentroid::new, geometry);
  }

  /**
   * Implementation of {@code geo:contains($geometry1, $geometry2)}.
   *
   * @param geometry1 first geometry element
   * @param geometry2 second geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item contains(final GNode geometry1, final GNode geometry2) throws QueryException {
    return call(GeoContains::new, geometry1, geometry2);
  }

  /**
   * Implementation of {@code geo:convex-hull($geometry)}.
   *
   * @param geometry geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item convexHull(final GNode geometry) throws QueryException {
    return call(GeoConvexHull::new, geometry);
  }

  /**
   * Implementation of {@code geo:crosses($geometry1, $geometry2)}.
   *
   * @param geometry1 first geometry element
   * @param geometry2 second geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item crosses(final GNode geometry1, final GNode geometry2) throws QueryException {
    return call(GeoCrosses::new, geometry1, geometry2);
  }

  /**
   * Implementation of {@code geo:difference($geometry1, $geometry2)}.
   *
   * @param geometry1 first geometry element
   * @param geometry2 second geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item difference(final GNode geometry1, final GNode geometry2) throws QueryException {
    return call(GeoDifference::new, geometry1, geometry2);
  }

  /**
   * Implementation of {@code geo:dimension($geometry)}.
   *
   * @param geometry geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item dimension(final GNode geometry) throws QueryException {
    return call(GeoDimension::new, geometry);
  }

  /**
   * Implementation of {@code geo:disjoint($geometry1, $geometry2)}.
   *
   * @param geometry1 first geometry element
   * @param geometry2 second geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item disjoint(final GNode geometry1, final GNode geometry2) throws QueryException {
    return call(GeoDisjoint::new, geometry1, geometry2);
  }

  /**
   * Implementation of {@code geo:distance($geometry1, $geometry2)}.
   *
   * @param geometry1 first geometry element
   * @param geometry2 second geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item distance(final GNode geometry1, final GNode geometry2) throws QueryException {
    return call(GeoDistance::new, geometry1, geometry2);
  }

  /**
   * Implementation of {@code geo:end-point($line)}.
   *
   * @param line line geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item endPoint(final GNode line) throws QueryException {
    return call(GeoEndPoint::new, line);
  }

  /**
   * Implementation of {@code geo:envelope($geometry)}.
   *
   * @param geometry geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item envelope(final GNode geometry) throws QueryException {
    return call(GeoEnvelope::new, geometry);
  }

  /**
   * Implementation of {@code geo:equals($geometry1, $geometry2)}.
   *
   * @param geometry1 first geometry element
   * @param geometry2 second geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item equals(final GNode geometry1, final GNode geometry2) throws QueryException {
    return call(GeoEquals::new, geometry1, geometry2);
  }

  /**
   * Implementation of {@code geo:exterior-ring($polygon)}.
   *
   * @param polygon polygon geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item exteriorRing(final GNode polygon) throws QueryException {
    return call(GeoExteriorRing::new, polygon);
  }

  /**
   * Implementation of {@code geo:geometry-n($geometry, $n)}.
   *
   * @param geometry geometry element
   * @param n 1-based geometry index
   * @return function result
   * @throws QueryException query exception
   */
  public Item geometryN(final GNode geometry, final Itr n) throws QueryException {
    return call(GeoGeometryN::new, geometry, n);
  }

  /**
   * Implementation of {@code geo:geometry-type($geometry)}.
   *
   * @param geometry geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item geometryType(final GNode geometry) throws QueryException {
    return call(GeoGeometryType::new, geometry);
  }

  /**
   * Implementation of {@code geo:interior-ring-n($polygon, $n)}.
   *
   * @param polygon polygon geometry element
   * @param n 1-based ring index
   * @return function result
   * @throws QueryException query exception
   */
  public Item interiorRingN(final GNode polygon, final Itr n) throws QueryException {
    return call(GeoInteriorRingN::new, polygon, n);
  }

  /**
   * Implementation of {@code geo:intersection($geometry1, $geometry2)}.
   *
   * @param geometry1 first geometry element
   * @param geometry2 second geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item intersection(final GNode geometry1, final GNode geometry2)
      throws QueryException {
    return call(GeoIntersection::new, geometry1, geometry2);
  }

  /**
   * Implementation of {@code geo:intersects($geometry1, $geometry2)}.
   *
   * @param geometry1 first geometry element
   * @param geometry2 second geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item intersects(final GNode geometry1, final GNode geometry2) throws QueryException {
    return call(GeoIntersects::new, geometry1, geometry2);
  }

  /**
   * Implementation of {@code geo:is-closed($line)}.
   *
   * @param line line geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item isClosed(final GNode line) throws QueryException {
    return call(GeoIsClosed::new, line);
  }

  /**
   * Implementation of {@code geo:is-ring($line)}.
   *
   * @param line line geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item isRing(final GNode line) throws QueryException {
    return call(GeoIsRing::new, line);
  }

  /**
   * Implementation of {@code geo:is-simple($geometry)}.
   *
   * @param geometry geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item isSimple(final GNode geometry) throws QueryException {
    return call(GeoIsSimple::new, geometry);
  }

  /**
   * Implementation of {@code geo:length($geometry)}.
   *
   * @param geometry geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item length(final GNode geometry) throws QueryException {
    return call(GeoLength::new, geometry);
  }

  /**
   * Implementation of {@code geo:num-geometries($geometry)}.
   *
   * @param geometry geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item numGeometries(final GNode geometry) throws QueryException {
    return call(GeoNumGeometries::new, geometry);
  }

  /**
   * Implementation of {@code geo:num-interior-ring($polygon)}.
   *
   * @param polygon polygon geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item numInteriorRing(final GNode polygon) throws QueryException {
    return call(GeoNumInteriorRing::new, polygon);
  }

  /**
   * Implementation of {@code geo:num-points($geometry)}.
   *
   * @param geometry geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item numPoints(final GNode geometry) throws QueryException {
    return call(GeoNumPoints::new, geometry);
  }

  /**
   * Implementation of {@code geo:overlaps($geometry1, $geometry2)}.
   *
   * @param geometry1 first geometry element
   * @param geometry2 second geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item overlaps(final GNode geometry1, final GNode geometry2) throws QueryException {
    return call(GeoOverlaps::new, geometry1, geometry2);
  }

  /**
   * Implementation of {@code geo:point-n($line, $n)}.
   *
   * @param line line geometry element
   * @param n 1-based point index
   * @return function result
   * @throws QueryException query exception
   */
  public Item pointN(final GNode line, final Itr n) throws QueryException {
    return call(GeoPointN::new, line, n);
  }

  /**
   * Implementation of {@code geo:point-on-surface($geometry)}.
   *
   * @param geometry geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item pointOnSurface(final GNode geometry) throws QueryException {
    return call(GeoPointOnSurface::new, geometry);
  }

  /**
   * Implementation of {@code geo:relate($geometry1, $geometry2, $intersectionMatrix)}.
   *
   * @param geometry1 first geometry element
   * @param geometry2 second geometry element
   * @param intersectionMatrix DE-9IM pattern
   * @return function result
   * @throws QueryException query exception
   */
  public Item relate(final GNode geometry1, final GNode geometry2,
      final Str intersectionMatrix) throws QueryException {
    return call(GeoRelate::new, geometry1, geometry2, intersectionMatrix);
  }

  /**
   * Implementation of {@code geo:srid($geometry)}.
   *
   * @param geometry geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item srid(final GNode geometry) throws QueryException {
    return call(GeoSrid::new, geometry);
  }

  /**
   * Implementation of {@code geo:start-point($line)}.
   *
   * @param line line geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item startPoint(final GNode line) throws QueryException {
    return call(GeoStartPoint::new, line);
  }

  /**
   * Implementation of {@code geo:sym-difference($geometry1, $geometry2)}.
   *
   * @param geometry1 first geometry element
   * @param geometry2 second geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item symDifference(final GNode geometry1, final GNode geometry2)
      throws QueryException {
    return call(GeoSymDifference::new, geometry1, geometry2);
  }

  /**
   * Implementation of {@code geo:touches($geometry1, $geometry2)}.
   *
   * @param geometry1 first geometry element
   * @param geometry2 second geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item touches(final GNode geometry1, final GNode geometry2) throws QueryException {
    return call(GeoTouches::new, geometry1, geometry2);
  }

  /**
   * Implementation of {@code geo:union($geometry1, $geometry2)}.
   *
   * @param geometry1 first geometry element
   * @param geometry2 second geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item union(final GNode geometry1, final GNode geometry2) throws QueryException {
    return call(GeoUnion::new, geometry1, geometry2);
  }

  /**
   * Implementation of {@code geo:within($geometry1, $geometry2)}.
   *
   * @param geometry1 first geometry element
   * @param geometry2 second geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item within(final GNode geometry1, final GNode geometry2) throws QueryException {
    return call(GeoWithin::new, geometry1, geometry2);
  }

  /**
   * Implementation of {@code geo:x($point)}.
   *
   * @param point point geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item x(final GNode point) throws QueryException {
    return call(GeoX::new, point);
  }

  /**
   * Implementation of {@code geo:y($point)}.
   *
   * @param point point geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item y(final GNode point) throws QueryException {
    return call(GeoY::new, point);
  }

  /**
   * Implementation of {@code geo:z($point)}.
   *
   * @param point point geometry element
   * @return function result
   * @throws QueryException query exception
   */
  public Item z(final GNode point) throws QueryException {
    return call(GeoZ::new, point);
  }

  /**
   * Calls a Geo function.
   *
   * @param newFn function constructor
   * @param exprs function arguments
   * @return function result
   * @throws QueryException query exception
   */
  private Item call(final Supplier<? extends GeoFn> newFn, final Expr... exprs)
      throws QueryException {
    final GeoFn fn = newFn.get();
    fn.exprs = exprs;
    return fn.item(queryContext, null);
  }
}
