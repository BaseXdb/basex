package org.basex.query.func.geo;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.build.*;
import org.basex.build.xml.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.type.*;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.gml2.*;

/**
 * Session function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class GeoFn extends StandardFunc {
  /** GML URI. */
  static final byte[] URI = token("http://www.opengis.net/gml");
  /** Prefix: "gml". */
  static final byte[] GML = token("gml");

  /** Line string. */
  static final byte[] LINE = token("line");
  /** Point string. */
  static final byte[] POINT = token("point");
  /** Polygon string. */
  static final byte[] POLYGON = token("polygon");

  /** QName gml:Point. */
  static final QNm Q_GML_POINT = new QNm(GML, "Point", URI);
  /** QName gml:MultiPoint. */
  static final QNm Q_GML_MULTIPOINT = new QNm(GML, "MultiPoint", URI);
  /** QName gml:LineString. */
  static final QNm Q_GML_LINESTRING = new QNm(GML, "LineString", URI);
  /** QName gml:LinearRing. */
  static final QNm Q_GML_LINEARRING = new QNm(GML, "LinearRing", URI);
  /** QName gml:Polygon. */
  static final QNm Q_GML_POLYGON = new QNm(GML, "Polygon", URI);
  /** QName gml:MultiPolygon. */
  static final QNm Q_GML_MULTIPOLYGON = new QNm(GML, "MultiPolygon", URI);
  /** QName gml:MultiLineString. */
  static final QNm Q_GML_MULTILINESTRING = new QNm(GML, "MultiLineString", URI);

  /** Array containing all QNames. */
  private static final QNm[] QNAMES = {
    Q_GML_POINT, Q_GML_LINESTRING, Q_GML_POLYGON, Q_GML_MULTIPOINT,
    Q_GML_MULTILINESTRING, Q_GML_MULTIPOLYGON, Q_GML_LINEARRING
  };

  /**
   * Reads an element as a GML node. Returns a geometry element
   * or throws an exception if the element is of the wrong type.
   * @param i index of argument
   * @param qc query context
   * @return geometry
   * @throws QueryException query exception
   */
  final Geometry checkGeo(final int i, final QueryContext qc) throws QueryException {
    return checkGeo(toElem(exprs[i], qc));
  }

  /**
   * Reads an element as a GML node. Returns a geometry element or raises an error if the element
   * does not match one of the specified types.
   * @param i index of argument
   * @param qc query context
   * @param type expected type
   * @param names allowed geometry types
   * @return geometry
   * @throws QueryException query exception
   */
  final Geometry geo(final int i, final QueryContext qc, final byte[] type, final QNm... names)
      throws QueryException {

    final ANode node = toElem(exprs[i], qc);
    final Geometry geo = geo(node, names);
    if(geo == null) {
      checkGeo(node);
      throw GEO_TYPE.get(info, type, node.qname().local());
    }
    return geo;
  }

  /**
   * Writes an geometry and returns a new element.
   * @param qc query context
   * @param geometry geometry
   * @return DBNode database node
   * @throws QueryException exception
   */
  final ANode toElement(final Geometry geometry, final QueryContext qc) throws QueryException {
    final String geo;
    try {
      // write geometry and add namespace declaration
      geo = new GMLWriter().write(geometry).replaceAll(
          "^<gml:(.*?)>", "<gml:$1 xmlns:gml='" + string(URI) + "'>");
    } catch(final Exception ex) {
      throw GEO_WRITE.get(info, ex);
    }

    try {
      final XMLParser parser = new XMLParser(new IOContent(geo), qc.context.options);
      return new DBNode(MemBuilder.build(parser)).childIter().next();
    } catch(final IOException ex) {
      throw IOERR_X.get(null, ex);
    }
  }

  /**
   * Reads an element as a GML node. Returns a geometry element
   * or throws an exception if the element is of the wrong type.
   * @param node xml node containing GML object(s)
   * @return geometry
   * @throws QueryException query exception
   */
  private Geometry checkGeo(final ANode node) throws QueryException {
    final Geometry geo = geo(node, QNAMES);
    if(geo == null) throw GEO_WHICH.get(info, node.qname().local());
    return geo;
  }

  /**
   * Reads an element as a GML node. Returns a geometry element or {@code null}.
   * @param node xml node containing GML object(s)
   * @param names allowed geometry types
   * @return geometry or {@code null}
   * @throws QueryException query exception
   */
  private Geometry geo(final ANode node, final QNm... names) throws QueryException {
    if(node.type != NodeType.ELEMENT) throw typeError(node, NodeType.ELEMENT, null);

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
        throw GEO_READ.get(info, ex);
      }
    }
    return null;
  }
}
