package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Flag.*;
import static org.basex.query.value.type.SeqType.*;

import java.util.*;
import java.util.function.*;

import org.basex.core.users.*;
import org.basex.query.func.geo.*;
import org.basex.query.func.request.*;
import org.basex.query.func.rest.*;
import org.basex.query.func.session.*;
import org.basex.query.func.sessions.*;
import org.basex.query.func.ws.*;
import org.basex.query.util.*;
import org.basex.query.value.type.*;

/**
 * Definitions of all built-in XQuery functions.
 * New namespace mappings for function prefixes and URIs must be added to the static initializer of
 * the {@link NSGlobal} class.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public enum ApiFunction implements AFunction {

  // Geo Module

  /** XQuery function. */
  _GEO_AREA(GeoArea::new, "area(node)",
      params(ELEMENT_O), DOUBLE_O, GEO_URI),
  /** XQuery function. */
  _GEO_AS_BINARY(GeoAsBinary::new, "as-binary(node)",
      params(ELEMENT_O), BASE64_BINARY_O, GEO_URI),
  /** XQuery function. */
  _GEO_AS_TEXT(GeoAsText::new, "as-text(node)",
      params(ELEMENT_O), STRING_O, GEO_URI),
  /** XQuery function. */
  _GEO_BOUNDARY(GeoBoundary::new, "boundary(node)",
      params(ELEMENT_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_BUFFER(GeoBuffer::new, "buffer(node,distance)",
      params(ELEMENT_O, DOUBLE_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_CENTROID(GeoCentroid::new, "centroid(node)",
      params(ELEMENT_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_CONTAINS(GeoContains::new, "contains(node1,node2)",
      params(ELEMENT_O, ELEMENT_O), BOOLEAN_O, GEO_URI),
  /** XQuery function. */
  _GEO_CONVEX_HULL(GeoConvexHull::new, "convex-hull(node)",
      params(ELEMENT_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_CROSSES(GeoCrosses::new, "crosses(node1,node2)",
      params(ELEMENT_O, ELEMENT_O), BOOLEAN_O, GEO_URI),
  /** XQuery function. */
  _GEO_DIFFERENCE(GeoDifference::new, "difference(node1,node2)",
      params(ELEMENT_O, ELEMENT_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_DIMENSION(GeoDimension::new, "dimension(node)",
      params(ELEMENT_O), INTEGER_O, GEO_URI),
  /** XQuery function. */
  _GEO_DISJOINT(GeoDisjoint::new, "disjoint(node1,node2)",
      params(ELEMENT_O, ELEMENT_O), BOOLEAN_O, GEO_URI),
  /** XQuery function. */
  _GEO_DISTANCE(GeoDistance::new, "distance(node1,node2)",
      params(ELEMENT_O, ELEMENT_O), DOUBLE_O, GEO_URI),
  /** XQuery function. */
  _GEO_END_POINT(GeoEndPoint::new, "end-point(node)",
      params(ELEMENT_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_ENVELOPE(GeoEnvelope::new, "envelope(node)",
      params(ELEMENT_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_EQUALS(GeoEquals::new, "equals(node1,node2)",
      params(ELEMENT_O, ELEMENT_O), BOOLEAN_O, GEO_URI),
  /** XQuery function. */
  _GEO_EXTERIOR_RING(GeoExteriorRing::new, "exterior-ring(node)",
      params(ELEMENT_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_GEOMETRY_N(GeoGeometryN::new, "geometry-n(node,number)",
      params(ELEMENT_O, INTEGER_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_GEOMETRY_TYPE(GeoGeometryType::new, "geometry-type(node)",
      params(ELEMENT_O), QNAME_O, GEO_URI),
  /** XQuery function. */
  _GEO_INTERIOR_RING_N(GeoInteriorRingN::new, "interior-ring-n(node,number)",
      params(ELEMENT_O, INTEGER_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_INTERSECTION(GeoIntersection::new, "intersection(node1,node2)",
      params(ELEMENT_O, ELEMENT_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_INTERSECTS(GeoIntersects::new, "intersects(node1,node2)",
      params(ELEMENT_O, ELEMENT_O), BOOLEAN_O, GEO_URI),
  /** XQuery function. */
  _GEO_IS_CLOSED(GeoIsClosed::new, "is-closed(node)",
      params(ELEMENT_O), BOOLEAN_O, GEO_URI),
  /** XQuery function. */
  _GEO_IS_RING(GeoIsRing::new, "is-ring(node)",
      params(ELEMENT_O), BOOLEAN_O, GEO_URI),
  /** XQuery function. */
  _GEO_IS_SIMPLE(GeoIsSimple::new, "is-simple(node)",
      params(ELEMENT_O), BOOLEAN_O, GEO_URI),
  /** XQuery function. */
  _GEO_LENGTH(GeoLength::new, "length(node)",
      params(ELEMENT_O), DOUBLE_O, GEO_URI),
  /** XQuery function. */
  _GEO_NUM_GEOMETRIES(GeoNumGeometries::new, "num-geometries(node)",
      params(ELEMENT_O), INTEGER_O, GEO_URI),
  /** XQuery function. */
  _GEO_NUM_INTERIOR_RING(GeoNumInteriorRing::new, "num-interior-ring(node)",
      params(ELEMENT_O), INTEGER_O, GEO_URI),
  /** XQuery function. */
  _GEO_NUM_POINTS(GeoNumPoints::new, "num-points(node)",
      params(ELEMENT_O), INTEGER_O, GEO_URI),
  /** XQuery function. */
  _GEO_OVERLAPS(GeoOverlaps::new, "overlaps(node1,node2)",
      params(ELEMENT_O, ELEMENT_O), BOOLEAN_O, GEO_URI),
  /** XQuery function. */
  _GEO_POINT_N(GeoPointN::new, "point-n(node,number)",
      params(ELEMENT_O, INTEGER_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_POINT_ON_SURFACE(GeoPointOnSurface::new, "point-on-surface(node)",
      params(ELEMENT_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_RELATE(GeoRelate::new, "relate(node1,node2,matrix))",
      params(ELEMENT_O, ELEMENT_O, STRING_O), BOOLEAN_O, GEO_URI),
  /** XQuery function. */
  _GEO_SRID(GeoSrid::new, "srid(node)",
      params(ELEMENT_O), ANY_URI_O, GEO_URI),
  /** XQuery function. */
  _GEO_START_POINT(GeoStartPoint::new, "start-point(node)",
      params(ELEMENT_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_SYM_DIFFERENCE(GeoSymDifference::new, "sym-difference(node1,node2)",
      params(ELEMENT_O, ELEMENT_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_TOUCHES(GeoTouches::new, "touches(node1,node2)",
      params(ELEMENT_O, ELEMENT_O), BOOLEAN_O, GEO_URI),
  /** XQuery function. */
  _GEO_UNION(GeoUnion::new, "union(node1,node2)",
      params(ELEMENT_O, ELEMENT_O), ELEMENT_O, GEO_URI),
  /** XQuery function. */
  _GEO_WITHIN(GeoWithin::new, "within(node1,node2)",
      params(ELEMENT_O, ELEMENT_O), BOOLEAN_O, GEO_URI),
  /** XQuery function. */
  _GEO_X(GeoX::new, "x(node)",
      params(ELEMENT_O), DOUBLE_O, GEO_URI),
  /** XQuery function. */
  _GEO_Y(GeoY::new, "y(node)",
      params(ELEMENT_O), DOUBLE_O, GEO_URI),
  /** XQuery function. */
  _GEO_Z(GeoZ::new, "z(node)",
      params(ELEMENT_O), DOUBLE_O, GEO_URI),

  // Request Module

  /** XQuery function. */
  _REQUEST_ADDRESS(RequestAddress::new, "address()",
      params(), STRING_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_ATTRIBUTE(RequestAttribute::new, "attribute(name)",
      params(STRING_O), ITEM_ZM, flag(NDT), REQUEST_URI),
  /** XQuery function. */
  _REQUEST_ATTRIBUTE_NAMES(RequestAttributeNames::new, "attribute-names()",
      params(), STRING_ZM, flag(NDT), REQUEST_URI),
  /** XQuery function. */
  _REQUEST_CONTEXT_PATH(RequestContextPath::new, "context-path()",
      params(), STRING_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_COOKIE(RequestCookie::new, "cookie(name[,default])",
      params(STRING_O, STRING_O), STRING_ZO, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_COOKIE_NAMES(RequestCookieNames::new, "cookie-names()",
      params(), STRING_ZM, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_HEADER(RequestHeader::new, "header(name[,default])",
      params(STRING_O, STRING_O), STRING_ZO, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_HEADER_NAMES(RequestHeaderNames::new, "header-names()",
      params(), STRING_ZM, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_HOSTNAME(RequestHostname::new, "hostname()",
      params(), STRING_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_METHOD(RequestMethod::new, "method()",
      params(), STRING_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_PARAMETER(RequestParameter::new, "parameter(name[,default])",
      params(STRING_O, ITEM_ZM), ITEM_ZM, flag(NDT), REQUEST_URI),
  /** XQuery function. */
  _REQUEST_PARAMETER_NAMES(RequestParameterNames::new, "parameter-names()",
      params(), STRING_ZM, flag(NDT), REQUEST_URI),
  /** XQuery function. */
  _REQUEST_PATH(RequestPath::new, "path()",
      params(), STRING_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_PORT(RequestPort::new, "port()",
      params(), INTEGER_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_QUERY(RequestQuery::new, "query()",
      params(), STRING_ZO, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_REMOTE_ADDRESS(RequestRemoteAddress::new, "remote-address()",
      params(), STRING_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_REMOTE_HOSTNAME(RequestRemoteHostname::new, "remote-hostname()",
      params(), STRING_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_REMOTE_PORT(RequestRemotePort::new, "remote-port()",
      params(), INTEGER_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_SCHEME(RequestScheme::new, "scheme()",
      params(), STRING_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_SET_ATTRIBUTE(RequestSetAttribute::new, "set-attribute(name,value)",
      params(STRING_O, ITEM_ZM), EMPTY_SEQUENCE_Z, flag(NDT), REQUEST_URI, Perm.ADMIN),
  /** XQuery function. */
  _REQUEST_URI(RequestUri::new, "uri()",
      params(), ANY_URI_O, REQUEST_URI),

  // RESTXQ Module

  /** XQuery function. */
  _RESTXQ_BASE_URI(RestBaseUri::new, "base-uri()",
      params(), ANY_URI_O, REST_URI),
  /** XQuery function. */
  _RESTXQ_INIT(RestInit::new, "init([update])",
      params(BOOLEAN_O), EMPTY_SEQUENCE_Z, REST_URI),
  /** XQuery function. */
  _RESTXQ_URI(RestUri::new, "uri()",
      params(), ANY_URI_O, REST_URI),
  /** XQuery function. */
  _RESTXQ_WADL(RestWadl::new, "wadl()",
      params(), ELEMENT_O, REST_URI),

  // Session Module

  /** XQuery function. */
  _SESSION_ACCESSED(SessionAccessed::new, "accessed()",
      params(), DATE_TIME_O, flag(NDT), SESSION_URI),
  /** XQuery function. */
  _SESSION_CLOSE(SessionClose::new, "close()",
      params(), EMPTY_SEQUENCE_Z, flag(NDT), SESSION_URI),
  /** XQuery function. */
  _SESSION_CREATED(SessionCreated::new, "created()",
      params(), DATE_TIME_O, flag(NDT), SESSION_URI),
  /** XQuery function. */
  _SESSION_DELETE(SessionDelete::new, "delete(key)",
      params(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), SESSION_URI),
  /** XQuery function. */
  _SESSION_GET(SessionGet::new, "get(key[,default])",
      params(STRING_O, ITEM_ZM), ITEM_ZM, flag(NDT), SESSION_URI),
  /** XQuery function. */
  _SESSION_ID(SessionId::new, "id()",
      params(), STRING_O, flag(NDT), SESSION_URI),
  /** XQuery function. */
  _SESSION_NAMES(SessionNames::new, "names()",
      params(), STRING_ZM, flag(NDT), SESSION_URI),
  /** XQuery function. */
  _SESSION_SET(SessionSet::new, "set(key,value)",
      params(STRING_O, ITEM_ZM), EMPTY_SEQUENCE_Z, flag(NDT), SESSION_URI),

  // Sessions Module

  /** XQuery function. */
  _SESSIONS_ACCESSED(SessionsAccessed::new, "accessed(id)",
      params(STRING_O), DATE_TIME_O, flag(NDT), SESSIONS_URI, Perm.ADMIN),
  /** XQuery function. */
  _SESSIONS_CLOSE(SessionsClose::new, "close(id,)",
      params(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), SESSIONS_URI, Perm.ADMIN),
  /** XQuery function. */
  _SESSIONS_CREATED(SessionsCreated::new, "created(id)",
      params(STRING_O), DATE_TIME_O, flag(NDT), SESSIONS_URI, Perm.ADMIN),
  /** XQuery function. */
  _SESSIONS_DELETE(SessionsDelete::new, "delete(id,key)",
      params(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), SESSIONS_URI, Perm.ADMIN),
  /** XQuery function. */
  _SESSIONS_GET(SessionsGet::new, "get(id,key[,default])",
      params(STRING_O, STRING_O, ITEM_ZM), ITEM_ZM, flag(NDT), SESSIONS_URI, Perm.ADMIN),
  /** XQuery function. */
  _SESSIONS_IDS(SessionsIds::new, "ids()",
      params(), STRING_ZM, flag(NDT), SESSIONS_URI, Perm.ADMIN),
  /** XQuery function. */
  _SESSIONS_NAMES(SessionsNames::new, "names(id)",
      params(STRING_O), STRING_ZM, flag(NDT), SESSIONS_URI, Perm.ADMIN),
  /** XQuery function. */
  _SESSIONS_SET(SessionsSet::new, "set(id,key,value)",
      params(STRING_O, STRING_O, ITEM_ZM), EMPTY_SEQUENCE_Z, flag(NDT), SESSIONS_URI, Perm.ADMIN),

  // WebSocket Module

  /** XQuery function. */
  _WS_BROADCAST(WsBroadcast::new, "broadcast(message)",
      params(ITEM_O), EMPTY_SEQUENCE_Z, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_CLOSE(WsClose::new, "close(id)",
      params(STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_DELETE(WsDelete::new, "delete(id,key)",
      params(STRING_O, STRING_O), EMPTY_SEQUENCE_Z, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_EMIT(WsEmit::new, "emit(message)",
      params(ITEM_O), EMPTY_SEQUENCE_Z, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_EVAL(WsEval::new, "eval(string[,bindings[,options]])",
      params(STRING_O, MAP_ZO, MAP_ZO), STRING_O, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_GET(WsGet::new, "get(id,key[,default])",
      params(STRING_O, STRING_O, ITEM_ZM), ITEM_ZM, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_ID(WsId::new, "id()",
      params(), STRING_O, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_IDS(WsIds::new, "ids()",
      params(), STRING_ZM, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_PATH(WsPath::new, "path(id)",
      params(STRING_O), STRING_O, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_SEND(WsSend::new, "send(message[,ids])",
      params(ITEM_O, STRING_ZM), EMPTY_SEQUENCE_Z, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_SET(WsSet::new, "set(id,key,value)",
      params(STRING_O, STRING_O, ITEM_ZM), EMPTY_SEQUENCE_Z, flag(NDT), WS_URI);

  /** Function definition. */
  private final FuncDefinition definition;

  /**
   * Constructs a function signature; calls
   * {@link #ApiFunction(Supplier, String, SeqType[], SeqType, EnumSet, byte[])}.
   * @param supplier function implementation constructor
   * @param desc descriptive function string
   * @param params parameter types
   * @param type return type
   * @param uri uri
   */
  ApiFunction(final Supplier<? extends StandardFunc> supplier, final String desc,
      final SeqType[] params, final SeqType type, final byte[] uri) {
    this(supplier, desc, params, type, EnumSet.noneOf(Flag.class), uri);
  }

  /**
   * Constructs a function signature.
   * @param supplier function implementation constructor
   * @param desc descriptive function string, containing the function name and its parameters in
   *   parentheses. Optional parameters are represented in nested square brackets; three dots
   *   indicate that the number of parameters of a function is not limited.
   * @param params parameter types
   * @param seqType return type
   * @param flags static function properties
   * @param uri uri
   */
  ApiFunction(final Supplier<? extends StandardFunc> supplier, final String desc,
      final SeqType[] params, final SeqType seqType, final EnumSet<Flag> flags, final byte[] uri) {
    this(supplier, desc, params, seqType, flags, uri, Perm.NONE);
  }

  /**
   * Constructs a function signature.
   * @param supplier function implementation constructor
   * @param desc descriptive function string, containing the function name and its parameters in
   *   parentheses. Optional parameters are represented in nested square brackets; three dots
   *   indicate that the number of parameters of a function is not limited.
   * @param params parameter types
   * @param seqType return type
   * @param flags static function properties
   * @param perm minimum permission
   * @param uri uri
   */
  ApiFunction(final Supplier<? extends StandardFunc> supplier, final String desc,
      final SeqType[] params, final SeqType seqType, final EnumSet<Flag> flags, final byte[] uri,
      final Perm perm) {
    definition = new FuncDefinition(supplier, desc, params, seqType, flags, uri, perm);
  }

  @Override
  public FuncDefinition definition() {
    return definition;
  }

  /**
   * Returns an array representation of the specified sequence types.
   * @param params parameter types
   * @return array
   */
  private static SeqType[] params(final SeqType... params) {
    return params;
  }

  /**
   * Returns a set representation of the specified compiler flags.
   * @param flags flags
   * @return set
   */
  private static EnumSet<Flag> flag(final Flag... flags) {
    final EnumSet<Flag> set = EnumSet.noneOf(Flag.class);
    Collections.addAll(set, flags);
    return set;
  }

  /**
   * Adds function signatures to the list. Called via reflection during initialization.
   * @param list list of function signatures
   */
  public static void init(final ArrayList<FuncDefinition> list) {
    for(final ApiFunction function : values()) list.add(function.definition);
  }

  @Override
  public final String toString() {
    return definition.toString();
  }
}
