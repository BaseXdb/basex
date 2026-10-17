(: --- DELTA 6: module header — add GML version preservation + supported inputs summary --- :)

(:~
 : BaseX Geo Module
 :
 : This module contains functions that may be applied to geometry data
 : conforming to the Open Geospatial Consortium (OGC) Simple Feature (SF) data
 : model. It is based on the
 : <a href="http://expath.org/spec/geo">EXPath Geo Module</a>
 : and uses the JTS and GeoTools libraries.
 :
 : The module supports geometries encoded as GML 2 and GML 3.2. Functions that
 : return GML elements preserve the input version: if the input geometry is 
 : GML 2, results are returned in GML 2; if the input geometry is GML 3.2,
 : results are returned in GML 3.2.
 :
 : Supported geometry types:
 : - GML 2: Point, LineString, LinearRing, Polygon, MultiPoint, MultiLineString,
 :   MultiPolygon (MultiGeometry is not supported as input; some operations may
 :   still return MultiGeometry).
 : - GML 3.2: Point, LineString, LinearRing, Polygon, MultiPoint, MultiCurve,
 :   MultiSurface, MultiGeometry.
 :
 : @author BaseX Team, BSD License
 :)
 
 module namespace geo = "http://expath.org/ns/geo";

(: Import Java class :)
import module namespace java = "java:org.expath.ns.Geo";

(:~
 : Returns the area of the supplied geometry <code>$geometry</code>.
 : For points and lines, <code>0</code> is returned.
 :
 : @param $geometry geometry element (GML)
 : @return area as xs:double
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:area($geometry as element(*)) as xs:double {
  java:area($geometry)
};

(:~
 : Returns the WKB (Well-known Binary) representation of the supplied geometry 
 : <code>$geometry</code>.
 :
 : @param $geometry geometry element (GML)
 : @return WKB as xs:base64Binary
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:as-binary($geometry as element(*)) as xs:base64Binary {
  java:as-binary($geometry)
};

(:~
 : Returns the WKT (Well-known Text) representation of the supplied geometry <code>$geometry</code>.
 :
 : @param $geometry geometry element (GML)
 : @return WKT as xs:string
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:as-text($geometry as element(*)) as xs:string {
  java:as-text($geometry)
};

(:~
 : Returns the boundary of the supplied geometry <code>$geometry</code>, in GML.
 : The result is a sequence of either <code>gml:Point</code> or <code>gml:LinearRing</code> elements
 : as a GeometryCollection object. For a Point or MultiPoint, the boundary is empty, and the empty
 : sequence is returned.
 :
 : @param $geometry geometry element (GML)
 : @return boundary element or empty sequence
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0005 The output object cannot be written as an element by the underlying geometry
 : writer.
 :)
declare function geo:boundary($geometry as element(*)) as element(*)? {
  java:boundary($geometry)
};

(:~
 : Returns a polygonal geometry representing the buffer of width <code>$distance</code> around
 : the supplied geometry <code>$geometry</code>, in the spatial reference system of the geometry.
 : The buffer of a geometry is the Minkowski sum (or difference) of the geometry with a disc of
 : radius <code>abs($distance)</code>. The buffer is constructed using 8 segments per quadrant to
 : approximate curves.
 :
 : @param $geometry geometry element (GML)
 : @param $distance buffer distance
 : @return buffered geometry element, or empty sequence
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0005 The output object cannot be written as an element by the underlying geometry
 : writer.
 :)
declare function geo:buffer($geometry as element(*), $distance as xs:double) as element(*)? {
  java:buffer($geometry, $distance)
};

(:~
 : Returns the mathematical centroid of the supplied geometry <code>$geometry</code>, as a
 : <code>gml:Point</code>.
 : Based on the definition, this point is not always located on the surface of the geometry.
 :
 : @param $geometry geometry element (GML)
 : @return centroid point element
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0005 The output object cannot be written as an element by the underlying geometry
 : writer.
 :)
declare function geo:centroid($geometry as element(*)) as element(*) {
  java:centroid($geometry)
};

(:~
 : Returns whether <code>$geometry1</code> spatially contains <code>$geometry2</code>.
 : Returns <code>true</code> if <code>geo:within($geometry2, $geometry1)</code> is
 : <code>true</code>.
 :
 : @param $geometry1 first geometry
 : @param $geometry2 second geometry
 : @return true if geometry1 contains geometry2
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:contains($geometry1 as element(*), $geometry2 as element(*)) as xs:boolean {
  java:contains($geometry1, $geometry2)
};

(:~
 : Returns the convex hull geometry of the supplied geometry <code>$geometry</code>, in GML.
 : The function returns the object of the smallest possible dimension.
 :
 : @param $geometry geometry element (GML)
 : @return convex hull geometry element
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0005 The output object cannot be written as an element by the underlying geometry
 : writer.
 :)
declare function geo:convex-hull($geometry as element(*)) as element(*)? {
  java:convex-hull($geometry)
};

(:~
 : Returns whether <code>$geometry1</code> spatially crosses <code>$geometry2</code>,
 : i.e., the geometries have some but not all interior points in common.
 :
 : @param $geometry1 first geometry
 : @param $geometry2 second geometry
 : @return true if geometry1 crosses geometry2
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:crosses($geometry1 as element(*), $geometry2 as element(*)) as xs:boolean {
  java:crosses($geometry1, $geometry2)
};

(:~
 : Returns the difference of <code>$geometry1</code> and <code>$geometry2</code>, in GML,
 : or the empty sequence if the difference is empty.
 :
 : @param $geometry1 first geometry
 : @param $geometry2 second geometry
 : @return difference geometry or empty sequence
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0005 The output object cannot be written as an element by the underlying geometry
 : writer.
 :)
declare function geo:difference($geometry1 as element(*), $geometry2 as element(*)) as element(*)? {
  java:difference($geometry1, $geometry2)
};

(:~
 : Returns the topological dimension of the supplied geometry <code>$geometry</code>.
 :
 : @param $geometry geometry element (GML)
 : @return dimension as xs:integer
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:dimension($geometry as element(*)) as xs:integer {
  java:dimension($geometry)
};

(:~
 : Returns whether <code>$geometry1</code> is spatially disjoint from <code>$geometry2</code>.
 :
 : @param $geometry1 first geometry
 : @param $geometry2 second geometry
 : @return true if disjoint
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:disjoint($geometry1 as element(*), $geometry2 as element(*)) as xs:boolean {
  java:disjoint($geometry1, $geometry2)
};

(:~
 : Returns the shortest distance between <code>$geometry1</code> and <code>$geometry2</code>,
 : in the units of the spatial reference system of <code>$geometry1</code>.
 :
 : @param $geometry1 first geometry
 : @param $geometry2 second geometry
 : @return distance as xs:double
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:distance($geometry1 as element(*), $geometry2 as element(*)) as xs:double {
  java:distance($geometry1, $geometry2)
};

(:~
 : Returns the ending point of the supplied line <code>$line</code>.
 : The input must be a single line geometry (LineString or LinearRing).
 :
 : @param $line line geometry element
 : @return end point element
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0003 The supplied element must be a line; other geometries are not accepted.
 :)
declare function geo:end-point($line as element(*)) as element(*) {
  java:end-point($line)
};

(:~
 : Returns the <code>gml:Envelope</code> of the supplied geometry <code>$geometry</code>.
 :
 : @param $geometry geometry element (GML)
 : @return envelope element
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0005 The output object cannot be written as an element by the underlying geometry
 : writer.
 :)
declare function geo:envelope($geometry as element(*)) as element(*) {
  java:envelope($geometry)
};

(:~
 : Returns whether <code>$geometry1</code> is spatially equal to <code>$geometry2</code>.
 :
 : @param $geometry1 first geometry
 : @param $geometry2 second geometry
 : @return true if equal
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:equals($geometry1 as element(*), $geometry2 as element(*)) as xs:boolean {
  java:equals($geometry1, $geometry2)
};

(:~
 : Returns the outer ring of the supplied polygon <code>$polygon</code> as a
 : <code>gml:LineString</code>.
 :
 : @param $polygon polygon geometry element
 : @return exterior ring element
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0003 The supplied element must be a polygon; other geometries are not accepted.
 : @error GEO0005 The output object cannot be written as an element by the underlying geometry
 : writer.
 :)
declare function geo:exterior-ring($polygon as element(*)) as element(*) {
  java:exterior-ring($polygon)
};

(:~
 : Returns the Nth geometry in the geometry collection <code>$geometry</code>, in GML.
 : For geometries that are not collections, the geometry itself is returned if 
 : <code>$n</code> is <code>1</code>.
 :
 : @param $geometry geometry (collection or single geometry)
 : @param $n 1-based geometry index
 : @return geometry element
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0004 The supplied geometry index is out of range.
 : @error GEO0005 The output object cannot be written as an element by the underlying geometry
 : writer.
 :)
declare function geo:geometry-n($geometry as element(*), $n as xs:integer) as element(*)? {
  java:geometry-n($geometry, $n)
};

(:~
 : Returns the name of the geometry type of the supplied geometry <code>$geometry</code>.
 :
 : @param $geometry geometry element (GML)
 : @return geometry type as xs:QName
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:geometry-type($geometry as element(*)) as xs:QName {
  java:geometry-type($geometry)
};

(:~
 : Returns the Nth interior ring of the supplied polygon <code>$polygon</code> as a
 : <code>gml:LineString</code>.
 :
 : @param $polygon polygon geometry element
 : @param $n 1-based ring index
 : @return interior ring element
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0003 The supplied element must be a polygon; other geometries are not accepted.
 : @error GEO0004 The supplied ring index is out of range.
 : @error GEO0005 The output object cannot be written as an element by the underlying geometry
 : writer.
 :)
declare function geo:interior-ring-n($polygon as element(*), $n as xs:integer) as element(*) {
  java:interior-ring-n($polygon, $n)
};

(:~
 : Returns the intersection geometry of <code>$geometry1</code> and <code>$geometry2</code>, in GML,
 : or the empty sequence if there is no intersection.
 :
 : @param $geometry1 first geometry
 : @param $geometry2 second geometry
 : @return intersection geometry or empty sequence
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0005 The output object cannot be written as an element by the underlying geometry
 : writer.
 :)
declare function geo:intersection($geometry1 as element(*), $geometry2 as element(*))
  as element(*)? {
  java:intersection($geometry1, $geometry2)
};

(:~
 : Returns whether <code>$geometry1</code> intersects <code>$geometry2</code>.
 : This is <code>true</code> if the two geometries are not disjoint.
 :
 : @param $geometry1 first geometry
 : @param $geometry2 second geometry
 : @return true if intersects
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:intersects($geometry1 as element(*), $geometry2 as element(*)) as xs:boolean {
  java:intersects($geometry1, $geometry2)
};

(:~
 : Returns whether the supplied line <code>$line</code> forms a closed loop
 : (i.e., the start point and end point are identical). The input must be a line geometry
 : (LineString or LinearRing) or a MultiLineString.
 :
 : @param $line line geometry element
 : @return true if closed
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0003 The supplied element must be a line; other geometries are not accepted.
 :)
declare function geo:is-closed($line as element(*)) as xs:boolean {
  java:is-closed($line)
};

(:~
 : Returns whether the supplied line <code>$line</code> is a ring (i.e., a single closed loop).
 :
 : @param $line line geometry element
 : @return true if ring
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0003 The supplied element must be a line; other geometries are not accepted.
 :)
declare function geo:is-ring($line as element(*)) as xs:boolean {
  java:is-ring($line)
};

(:~
 : Returns whether the supplied geometry <code>$geometry</code> is simple and has no anomalous 
 : geometric points (i.e., it does not self-intersect and does not pass through the same point
 : more than once; rings are allowed).
 :
 : @param $geometry geometry element (GML)
 : @return true if simple
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:is-simple($geometry as element(*)) as xs:boolean {
  java:is-simple($geometry)
};

(:~
 : Returns the length of the supplied geometry <code>$geometry</code>. If the geometry is a point, 
 : <code>0</code> is returned.
 :
 : @param $geometry geometry element (GML)
 : @return length as xs:double
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:length($geometry as element(*)) as xs:double {
  java:length($geometry)
};

(:~
 : Returns the number of geometries in the geometry collection
 : <code>$geometry</code>, in GML. For geometries that are not collections, 
 : <code>1</code> is returned.
 :
 : This implementation is broader than the EXPath specification: it accepts all
 : geometry types, whereas the specification limits the input to collection
 : types (MultiPoint, MultiPolygon, ...).
 :
 : @param $geometry geometry (collection or single geometry)
 : @return number of geometries
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:num-geometries($geometry as element(*)) as xs:integer {
  java:num-geometries($geometry)
};

(:~
 : Returns the number of interior rings of the supplied polygon <code>$polygon</code>.
 :
 : @param $polygon polygon geometry element
 : @return number of interior rings
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0003 The supplied element must be a polygon; other geometries are not accepted.
 :)
declare function geo:num-interior-ring($polygon as element(*)) as xs:integer {
  java:num-interior-ring($polygon)
};

(:~
 : Returns the number of points in the supplied geometry <code>$geometry</code>.
 : It can be used not only for lines, but also for other geometry types such as
 : MultiPolygon. For a Point geometry, <code>1</code> is returned.
 :
 : This differs from the EXPath Geo specification, which limits the input
 : geometry type to lines.
 :
 : @param $geometry geometry element (GML)
 : @return number of points
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:num-points($geometry as element(*)) as xs:integer {
  java:num-points($geometry)
};

(:~
 : Returns whether <code>$geometry1</code> spatially overlaps <code>$geometry2</code>.
 :
 : @param $geometry1 first geometry
 : @param $geometry2 second geometry
 : @return true if overlaps
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:overlaps($geometry1 as element(*), $geometry2 as element(*)) as xs:boolean {
  java:overlaps($geometry1, $geometry2)
};

(:~
 : Returns the Nth point in the supplied line <code>$line</code>.
 :
 : @param $line line geometry element
 : @param $n 1-based point index
 : @return point element
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0003 The supplied element must be a line; other geometries are not accepted.
 : @error GEO0004 The supplied point index is out of range.
 : @error GEO0005 The output object cannot be written as an element by the underlying geometry
 : writer.
 :)
declare function geo:point-n($line as element(*), $n as xs:integer) as element(*) {
  java:point-n($line, $n)
};

(:~
 : Returns an interior point on the supplied geometry <code>$geometry</code>, as a 
 : <code>gml:Point</code>. An interior point is guaranteed to lie in the interior of the Geometry,
 : if it possible to calculate such a point exactly. Otherwise, the point may lie on the boundary
 : of the geometry.
 :
 : @param $geometry geometry element (GML)
 : @return point-on-surface element
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0005 The output object cannot be written as an element by the underlying geometry
 : writer.
 :)
declare function geo:point-on-surface($geometry as element(*)) as element(*) {
  java:point-on-surface($geometry)
};

(:~
 : Returns whether the relationships between the boundaries, interiors, and exteriors of
 : <code>$geometry1</code> and <code>$geometry2</code> match <code>$intersectionMatrix</code>.
 : The matrix must have a length of 9 characters.
 :
 : @param $geometry1 first geometry
 : @param $geometry2 second geometry
 : @param $intersectionMatrix DE-9IM pattern (length 9)
 : @return true if pattern matches
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0006 The supplied intersection matrix is invalid.
 :)
declare function geo:relate($geometry1 as element(*), $geometry2 as element(*),
  $intersectionMatrix as xs:string) as xs:boolean {
  java:relate($geometry1, $geometry2, $intersectionMatrix)
};

(:~
 : Returns the ID of the Spatial Reference System used by the supplied geometry
 : <code>$geometry</code>. The SRID is represented as an integer.
 :
 : This differs from the EXPath Geo Module specification, which returns a URI.
 :
 : @param $geometry geometry element (GML)
 : @return SRID as xs:integer
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:srid($geometry as element(*)) as xs:integer {
  java:srid($geometry)
};

(:~
 : Returns the starting point of the supplied line <code>$line</code>.
 : The input must be a single line geometry (LineString or LinearRing).
 :
 : @param $line line geometry element
 : @return start point element
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0003 The supplied element must be a line; other geometries are not accepted.
 :)
declare function geo:start-point($line as element(*)) as element(*) {
  java:start-point($line)
};

(:~
 : Returns the symmetric difference of <code>$geometry1</code> and <code>$geometry2</code>, in GML,
 : or the empty sequence if the difference is empty.
 :
 : @param $geometry1 first geometry
 : @param $geometry2 second geometry
 : @return symmetric difference geometry or empty sequence
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0005 The output object cannot be written as an element by the underlying geometry
 : writer.
 :)
declare function geo:sym-difference($geometry1 as element(*), $geometry2 as element(*))
  as element(*)? {
  java:sym-difference($geometry1, $geometry2)
};

(:~
 : Returns whether <code>$geometry1</code> spatially touches <code>$geometry2</code>.
 :
 : @param $geometry1 first geometry
 : @param $geometry2 second geometry
 : @return true if touches
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:touches($geometry1 as element(*), $geometry2 as element(*)) as xs:boolean {
  java:touches($geometry1, $geometry2)
};

(:~
 : Returns the union geometry of <code>$geometry1</code> and <code>$geometry2</code>, in GML.
 :
 : @param $geometry1 first geometry
 : @param $geometry2 second geometry
 : @return union geometry
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 : @error GEO0005 The output object cannot be written as an element by the underlying geometry
 : writer.
 :)
declare function geo:union($geometry1 as element(*), $geometry2 as element(*)) as element(*)? {
  java:union($geometry1, $geometry2)
};

(:~
 : Returns whether <code>$geometry1</code> is spatially within <code>$geometry2</code>.
 :
 : @param $geometry1 first geometry
 : @param $geometry2 second geometry
 : @return true if within
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:within($geometry1 as element(*), $geometry2 as element(*)) as xs:boolean {
  java:within($geometry1, $geometry2)
};

(:~
 : Returns the x coordinate of the supplied point <code>$point</code>.
 :
 : @param $point point geometry element
 : @return x coordinate
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:x($point as element(*)) as xs:double {
  java:x($point)
};

(:~
 : Returns the y coordinate of the supplied point <code>$point</code>.
 : If the point does not have a y coordinate, <code>NaN</code> is returned.
 :
 : @param $point point geometry element
 : @return y coordinate
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:y($point as element(*)) as xs:double {
  java:y($point)
};

(:~
 : Returns the z coordinate of the supplied point <code>$point</code>.
 : If the point does not have a z coordinate, <code>NaN</code> is returned.
 :
 : This differs from the EXPath Geo Module specification, which returns an empty
 : sequence.
 :
 : @param $point point geometry element
 : @return z coordinate
 : @error GEO0001 The supplied element is not recognized as a valid geometry.
 : @error GEO0002 The supplied geometry cannot be read by the underlying geometry reader.
 :)
declare function geo:z($point as element(*)) as xs:double {
  java:z($point)
};
