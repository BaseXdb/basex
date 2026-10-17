package org.basex.query.func.geo;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.geo.GeoError.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.nio.charset.*;

import javax.xml.namespace.*;

import org.basex.build.*;
import org.basex.build.xml.*;
import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.eclipse.emf.common.util.*;
import org.geotools.api.feature.simple.*;
import org.geotools.feature.*;
import org.geotools.feature.simple.*;
import org.geotools.xsd.*;
import org.geotools.xsd.Parser;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.io.gml2.*;

import net.opengis.wfs.*;

/**
 * Session function.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class GeoFn extends StandardFunc {
  /** The Geo module namespace URI. */
  public static final byte[] GEO_URI = token("http://expath.org/ns/geo");
  /** GML 2 URI. */
  public static final byte[] GML_URI = token("http://www.opengis.net/gml");
  /** GML 3.2 URI. */
  public static final byte[] GML32_URI = token("http://www.opengis.net/gml/3.2");
  /** Prefix: "gml". */
  static final byte[] GML = token("gml");

  /** Line string. */
  static final byte[] LINE = token("line");
  /** Point string. */
  static final byte[] POINT = token("point");
  /** Polygon string. */
  static final byte[] POLYGON = token("polygon");

  /** GML 3 namespace fragment. */
  private static final byte[] GML3_NAMESPACE_FRAGMENT = Token.token("gml/3");
  /** GML 3 element name "pos". */
  private static final byte[] GML3_POS = Token.token("pos");
  /** GML 3 element name "posList". */
  private static final byte[] GML3_POS_LIST = Token.token("posList");
  /** GML 3 element name "exterior". */
  private static final byte[] GML3_EXTERIOR = Token.token("exterior");
  /** GML 3 element name "interior". */
  private static final byte[] GML3_INTERIOR = Token.token("interior");
  /** GML 3 element name "coordinates". */
  private static final byte[] GML2_COORDINATES = Token.token("coordinates");
  /** GML 3 element name "outerBoundaryIs". */
  private static final byte[] GML2_OUTER_BOUNDARY_IS = Token.token("outerBoundaryIs");
  /** GML 3 element name "innerBoundaryIs". */
  private static final byte[] GML2_INNER_BOUNDARY_IS = Token.token("innerBoundaryIs");

  /** Simple feature builder for GML 3.2 serialization. */
  private static final SimpleFeatureBuilder FEATURE_BUILDER;

  /** Whether to remove gml:id attributes from serialized geometries (used in tests only). */
  public static boolean removeGmlId;

  static {
    final SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
    typeBuilder.setName("feature");
    typeBuilder.setNamespaceURI(string(GEO_URI));
    typeBuilder.add("geometry", Geometry.class);
    final SimpleFeatureType featureType = typeBuilder.buildFeatureType();
    FEATURE_BUILDER = new SimpleFeatureBuilder(featureType);
  }

  /** GML 3 encoder. */
  private static final Encoder GML3_ENCODER;
  /** GML 3 feature collection name. */
  private static final QName GML3_FEATURE_COLLECTION_NAME = org.geotools.wfs.WFS.FeatureCollection;
  static {
    final Configuration gml3Configuration = new org.geotools.wfs.v1_1.WFSConfiguration();
    gml3Configuration.getProperties().add(org.geotools.gml3.GMLConfiguration.OPTIMIZED_ENCODING);
    increasePrecision(gml3Configuration);
    GML3_ENCODER = new Encoder(gml3Configuration);
    GML3_ENCODER.getNamespaces().declarePrefix("geo", string(GEO_URI));
  }

  /** GML 3.2 encoder. */
  private static final Encoder GML32_ENCODER;
  /** GML 3.2 feature collection name. */
  private static final QName GML32_FEATURE_COLLECTION_NAME =
      org.geotools.wfs.v2_0.WFS.FeatureCollection;
  static {
    final Configuration gml32configuration = new org.geotools.wfs.v2_0.WFSConfiguration();
    gml32configuration.getProperties().add(
        org.geotools.gml3.v3_2.GMLConfiguration.OPTIMIZED_ENCODING);
    increasePrecision(gml32configuration);
    GML32_ENCODER = new Encoder(gml32configuration);
    GML32_ENCODER.getNamespaces().declarePrefix("geo", string(GEO_URI));
  }

  /**
   * Increases the number of decimals for GML geometries in the given configuration.
   * @param configuration GML configuration
   */
  private static void increasePrecision(final Configuration configuration) {
    if (configuration instanceof org.geotools.gml3.GMLConfiguration cfg) {
      cfg.setNumDecimals(16);
    }
    else if (configuration instanceof org.geotools.gml3.v3_2.GMLConfiguration cfg) {
      cfg.setNumDecimals(16);
    }
    for (Configuration dependency : configuration.getDependencies()) {
      increasePrecision(dependency);
    }
  }

  /**
   * Reads an element as a GML node. Returns a geometry element
   * or throws an exception if the element is of the wrong type.
   *
   * @param i index of argument
   * @param qc query context
   * @return geometry
   * @throws QueryException query exception
   */
  final Geometry toGeometry(final int i, final QueryContext qc) throws QueryException {
    return toGeometry(toElem(exprs[i], qc));
  }

  /**
   * Reads an element as a GML node. Returns a geometry element or raises an error if the element
   * does not match one of the specified types.
   *
   * @param i index of argument
   * @param qc query context
   * @param type expected type
   * @return geometry
   * @throws QueryException query exception
   */
  final Geometry toGeometry(final int i, final QueryContext qc, final byte[] type)
      throws QueryException {
    return toGeometry(toElem(exprs[i], qc), type);
  }

  /**
   * Reads an element as a GML node. Returns a geometry element or raises an error if the element
   * does not match one of the specified types.
   *
   * @param node xml node containing GML object(s)
   * @param type expected type
   * @return geometry
   * @throws QueryException query exception
   */
  final Geometry toGeometry(final GNode node, final byte[] type) throws QueryException {
    final Geometry geo = toGeo(node);
    if (geo == null || geo.isEmpty()) {
      throw GEO_TYPE.get(info, type, node.qname().local());
    }
    return geo;
  }

  /**
   * Reads an element as a GML node. Returns a geometry element
   * or throws an exception if the element is of the wrong type.
   *
   * @param node xml node containing GML object(s)
   * @return geometry
   * @throws QueryException query exception
   */
  public Geometry toGeometry(final GNode node) throws QueryException {
    final Geometry geo = toGeo(node);
    if (geo == null || geo.isEmpty()) {
      throw GEO_WHICH.get(info, node.qname());
    }
    return geo;
  }

  /**
   * Reads an element as a GML node. Returns a geometry element or {@code null}.
   *
   * @param node xml node containing GML object(s)
   * @return geometry or {@code null}
   * @throws QueryException query exception
   */
  private Geometry toGeo(final GNode node) throws QueryException {
    if (node.type != NodeType.ELEMENT) {
      throw typeError(node, NodeType.ELEMENT, null);
    }
    try {
      final Configuration configuration;
      final String input = node.serialize().toString();
      switch(GmlDialect.of(node)) {
        case GML2:
          final GMLReader gmlReader = new GMLReader();
          final GeometryFactory geoFactory = new GeometryFactory();
          return gmlReader.read(input, geoFactory);
        case GML3:
          configuration = new org.geotools.gml3.GMLConfiguration(true);
          break;
        case GML32:
          configuration = new org.geotools.gml3.v3_2.GMLConfiguration(true);
          break;
        default:
          throw Util.notExpected();
      }
      final Parser parser = new Parser(configuration);
      parser.setStrict(false);
      parser.setValidating(false);
      return (Geometry) parser.parse(new StringReader(input));
    } catch (final Throwable ex) {
      throw GEO_READ.get(info, ex);
    }
  }

  /**
   * Writes a geometry and returns a new element in the same namespace as the first argument of the
   * Geo function that was invoked.
   *
   * @param geometry geometry
   * @param node the first function argument
   * @return DBNode database node
   * @throws QueryException exception
   */
  final Value toElement(final Geometry geometry, final GNode node) throws QueryException {
    if (geometry.isEmpty()) {
      return Empty.VALUE;
    }
    final String geo;
    switch(GmlDialect.of(node)) {
      case GML2:
        try {
          // Use JTS for serialization to GML2
          geo = new GMLWriter().write(geometry).
              replaceAll(">\\s*\\n\\s*", ">").
              replaceAll("\\s*\\n\\s*<", "<");
        } catch (final Exception ex) {
          throw GEO_WRITE.get(info, ex);
        }
        break;
      case GML3:
        // Use GeoTools for serialization to GML 3
        geo = encode(geometry, GML3_ENCODER, GML3_FEATURE_COLLECTION_NAME);
        break;
      case GML32:
        // Use GeoTools for serialization to GML 3.2
        geo = encode(geometry, GML32_ENCODER, GML32_FEATURE_COLLECTION_NAME);
        break;
      default:
        throw Util.notExpected();
    }
    try {
      // add namespace declaration
      final String gml = geo.replaceFirst(
          "^<gml:(.*?)(/?>)", "<gml:$1 xmlns:gml='" + string(node.qname().uri()) + "'$2");
      final MainOptions mopts = new MainOptions();
      mopts.set(MainOptions.STRIPWS, true);
      final XMLParser parser = new XMLParser(new IOContent(gml), mopts);
      return new DBNode(MemBuilder.build(parser)).childIter().next();
    } catch (final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }

  /**
   * Encodes the given geometry using the specified encoder.
   *
   * @param geometry geometry
   * @param encoder encoder
   * @param featureCollectionName feature collection name
   * @return GML string
   * @throws QueryException query exception
   */
  private String encode(final Geometry geometry, final Encoder encoder,
      final QName featureCollectionName) throws QueryException {

    final DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();
    featureCollection.add(FEATURE_BUILDER.buildFeature(null, new Object[]{geometry}));
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();

    // The code below could do the encoding, but it limits precision to 7 digits.
    //
    // final org.geotools.wfs.GML encoder =
    //     new org.geotools.wfs.GML(org.geotools.wfs.GML.Version.WFS1_1);
    // encoder.setNamespace("geo", GEO_NAMESPACE_URI);
    // encoder.setBaseURL(new URL(GEO_NAMESPACE_URI));
    // encoder.encode(baos, featureCollection);

    final FeatureCollectionType featureCollectionType =
        WfsFactory.eINSTANCE.createFeatureCollectionType();
    @SuppressWarnings("unchecked")
    final EList<DefaultFeatureCollection> feature = featureCollectionType.getFeature();
    feature.add(featureCollection);
    try {
      encoder.encode(featureCollectionType, featureCollectionName, baos);
    } catch (final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
    // extract GML from feature collection
    final String geo = baos.toString(StandardCharsets.UTF_8).
        replaceAll("(?s)^.*<geo:geometry>\\s*|\\s*</geo:geometry>\\s*</geo:feature>\\s*"
        + "</(gml:featureMembers|wfs:member)>\\s*</wfs:FeatureCollection>$", "").
        replaceAll("\\s+xmlns:gml=\"[^\"]*\"", "");
    return removeGmlId ? geo.replaceAll("\\s+gml:id=\"[^\"]*\"", "") : geo;
  }

  /** GML dialects. */
  private enum GmlDialect {
    /** GML 2, use JTS GML reader and writer in order to maintain upward compatibility. */
    GML2,
    /** GML 3, use GeoTools with GML 3 configuration for parsing and serialization. */
    GML3,
    /** GML 3.2, use GeoTools with GML 3.2 configuration for parsing and serialization. */
    GML32;

    /**
     * Determines the GML dialect of the given node.
     * @param node input node
     * @return whether to use GeoTools
     */
    static GmlDialect of(final GNode node) {
      final byte[] uri = node.qname().uri();
      if(Token.contains(uri, GML3_NAMESPACE_FRAGMENT)) return GML32;
      final BasicNodeIter iter = node.descendantIter(false);
      for(GNode n; (n = iter.next()) != null;) {
        if(n.type == NodeType.ELEMENT) {
          final QNm qnm = n.qname();
          if(Token.startsWith(qnm.uri(), GML_URI)) {
            final byte[] local = qnm.local();
            if(Token.eq(local, GML3_EXTERIOR)) return GML3;
            if(Token.eq(local, GML3_INTERIOR)) return GML3;
            if(Token.eq(local, GML2_OUTER_BOUNDARY_IS)) return GML2;
            if(Token.eq(local, GML2_INNER_BOUNDARY_IS)) return GML2;

            if(Token.eq(local, GML3_POS)) return GML3;
            if(Token.eq(local, GML3_POS_LIST)) return GML3;
            if(Token.eq(local, GML2_COORDINATES)) return GML2;
          }
        }
      }
      return GML3;
    }
  }
}
