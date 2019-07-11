package org.basex.query.func;

import static org.basex.query.QueryText.*;
import static org.basex.query.util.Flag.*;
import static org.basex.query.value.type.SeqType.*;

import java.util.*;

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
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public enum ApiFunction implements AFunction {

  // Geo Module

  /** XQuery function. */
  _GEO_AREA(GeoArea.class, "area(node)", arg(ELM_O), DBL_O, GEO_URI),
  /** XQuery function. */
  _GEO_AS_BINARY(GeoAsBinary.class, "as-binary(node)", arg(ELM_O), B64_O, GEO_URI),
  /** XQuery function. */
  _GEO_AS_TEXT(GeoAsText.class, "as-text(node)", arg(ELM_O), STR_O, GEO_URI),
  /** XQuery function. */
  _GEO_BOUNDARY(GeoBoundary.class, "boundary(node)", arg(ELM_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_BUFFER(GeoBuffer.class, "buffer(node,distance)", arg(ELM_O, DBL_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_CENTROID(GeoCentroid.class, "centroid(node)", arg(ELM_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_CONTAINS(GeoContains.class, "contains(node1,node2)", arg(ELM_O, ELM_O), BLN_O, GEO_URI),
  /** XQuery function. */
  _GEO_CONVEX_HULL(GeoConvexHull.class, "convex-hull(node)", arg(ELM_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_CROSSES(GeoCrosses.class, "crosses(node1,node2)", arg(ELM_O, ELM_O), BLN_O, GEO_URI),
  /** XQuery function. */
  _GEO_DIFFERENCE(GeoDifference.class, "difference(node1,node2)",
      arg(ELM_O, ELM_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_DIMENSION(GeoDimension.class, "dimension(node)", arg(ELM_O), ITR_O, GEO_URI),
  /** XQuery function. */
  _GEO_DISJOINT(GeoDisjoint.class, "disjoint(node1,node2)", arg(ELM_O, ELM_O), BLN_O, GEO_URI),
  /** XQuery function. */
  _GEO_DISTANCE(GeoDistance.class, "distance(node1,node2)", arg(ELM_O, ELM_O), DBL_O, GEO_URI),
  /** XQuery function. */
  _GEO_END_POINT(GeoEndPoint.class, "end-point(node)", arg(ELM_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_ENVELOPE(GeoEnvelope.class, "envelope(node)", arg(ELM_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_EQUALS(GeoEquals.class, "equals(node1,node2)", arg(ELM_O, ELM_O), BLN_O, GEO_URI),
  /** XQuery function. */
  _GEO_EXTERIOR_RING(GeoExteriorRing.class, "exterior-ring(node)", arg(ELM_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_GEOMETRY_N(GeoGeometryN.class, "geometry-n(node,number)", arg(ELM_O, ITR_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_GEOMETRY_TYPE(GeoGeometryType.class, "geometry-type(node)", arg(ELM_O), QNM_O, GEO_URI),
  /** XQuery function. */
  _GEO_INTERIOR_RING_N(GeoInteriorRingN.class, "interior-ring-n(node,number)",
      arg(ELM_O, ITR_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_INTERSECTION(GeoIntersection.class, "intersection(node1,node2)",
      arg(ELM_O, ELM_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_INTERSECTS(GeoIntersects.class, "intersects(node1,node2)",
      arg(ELM_O, ELM_O), BLN_O, GEO_URI),
  /** XQuery function. */
  _GEO_IS_CLOSED(GeoIsClosed.class, "is-closed(node)", arg(ELM_O), BLN_O, GEO_URI),
  /** XQuery function. */
  _GEO_IS_RING(GeoIsRing.class, "is-ring(node)", arg(ELM_O), BLN_O, GEO_URI),
  /** XQuery function. */
  _GEO_IS_SIMPLE(GeoIsSimple.class, "is-simple(node)", arg(ELM_O), BLN_O, GEO_URI),
  /** XQuery function. */
  _GEO_LENGTH(GeoLength.class, "length(node)", arg(ELM_O), DBL_O, GEO_URI),
  /** XQuery function. */
  _GEO_NUM_GEOMETRIES(GeoNumGeometries.class, "num-geometries(node)", arg(ELM_O), ITR_O, GEO_URI),
  /** XQuery function. */
  _GEO_NUM_INTERIOR_RING(GeoNumInteriorRing.class, "num-interior-ring(node)",
      arg(ELM_O), ITR_O, GEO_URI),
  /** XQuery function. */
  _GEO_NUM_POINTS(GeoNumPoints.class, "num-points(node)", arg(ELM_O), ITR_O, GEO_URI),
  /** XQuery function. */
  _GEO_OVERLAPS(GeoOverlaps.class, "overlaps(node1,node2)", arg(ELM_O, ELM_O), BLN_O, GEO_URI),
  /** XQuery function. */
  _GEO_POINT_N(GeoPointN.class, "point-n(node,number)", arg(ELM_O, ITR_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_POINT_ON_SURFACE(GeoPointOnSurface.class, "point-on-surface(node)",
      arg(ELM_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_RELATE(GeoRelate.class, "relate(node1,node2,matrix))",
      arg(ELM_O, ELM_O, STR_O), BLN_O, GEO_URI),
  /** XQuery function. */
  _GEO_SRID(GeoSrid.class, "srid(node)", arg(ELM_O), URI_O, GEO_URI),
  /** XQuery function. */
  _GEO_START_POINT(GeoStartPoint.class, "start-point(node)", arg(ELM_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_SYM_DIFFERENCE(GeoSymDifference.class, "sym-difference(node1,node2)",
      arg(ELM_O, ELM_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_TOUCHES(GeoTouches.class, "touches(node1,node2)", arg(ELM_O, ELM_O), BLN_O, GEO_URI),
  /** XQuery function. */
  _GEO_UNION(GeoUnion.class, "union(node1,node2)", arg(ELM_O, ELM_O), ELM_O, GEO_URI),
  /** XQuery function. */
  _GEO_WITHIN(GeoWithin.class, "within(node1,node2)", arg(ELM_O, ELM_O), BLN_O, GEO_URI),
  /** XQuery function. */
  _GEO_X(GeoX.class, "x(node)", arg(ELM_O), DBL_O, GEO_URI),
  /** XQuery function. */
  _GEO_Y(GeoY.class, "y(node)", arg(ELM_O), DBL_O, GEO_URI),
  /** XQuery function. */
  _GEO_Z(GeoZ.class, "z(node)", arg(ELM_O), DBL_O, GEO_URI),

  // Request Module

  /** XQuery function. */
  _REQUEST_ADDRESS(RequestAddress.class, "address()", arg(), STR_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_ATTRIBUTE(RequestAttribute.class, "attribute(name)",
      arg(STR_O), STR_O, flag(NDT), REQUEST_URI),
  /** XQuery function. */
  _REQUEST_ATTRIBUTE_NAMES(RequestAttributeNames.class, "attribute-names()",
      arg(), STR_ZM, flag(NDT), REQUEST_URI),
  /** XQuery function. */
  _REQUEST_CONTEXT_PATH(RequestContextPath.class, "context-path()", arg(), STR_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_COOKIE(RequestCookie.class, "cookie(name[,default])",
      arg(STR_O, STR_O), STR_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_COOKIE_NAMES(RequestCookieNames.class, "cookie-names()", arg(), STR_ZM, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_HEADER(RequestHeader.class, "header(name[,default])",
      arg(STR_O, STR_O), STR_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_HEADER_NAMES(RequestHeaderNames.class, "header-names()", arg(), STR_ZM, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_HOSTNAME(RequestHostname.class, "hostname()", arg(), STR_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_METHOD(RequestMethod.class, "method()", arg(), STR_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_PARAMETER(RequestParameter.class, "parameter(name[,default])",
      arg(STR_O, ITEM_ZM), ITEM_ZM, flag(NDT), REQUEST_URI),
  /** XQuery function. */
  _REQUEST_PARAMETER_NAMES(RequestParameterNames.class, "parameter-names()",
      arg(), STR_ZM, flag(NDT), REQUEST_URI),
  /** XQuery function. */
  _REQUEST_PATH(RequestPath.class, "path()", arg(), STR_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_PORT(RequestPort.class, "port()", arg(), ITR_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_QUERY(RequestQuery.class, "query()", arg(), STR_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_REMOTE_ADDRESS(RequestRemoteAddress.class,
      "remote-address()", arg(), STR_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_REMOTE_HOSTNAME(RequestRemoteHostname.class, "remote-hostname()",
      arg(), STR_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_REMOTE_PORT(RequestRemotePort.class, "remote-port()", arg(), ITR_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_SCHEME(RequestScheme.class, "scheme()", arg(), STR_O, REQUEST_URI),
  /** XQuery function. */
  _REQUEST_SET_ATTRIBUTE(RequestSetAttribute.class, "set-attribute(name,value)",
      arg(STR_O, ITEM_ZM), EMP, flag(NDT), REQUEST_URI),
  /** XQuery function. */
  _REQUEST_URI(RequestUri.class, "uri()", arg(), URI_O, REQUEST_URI),

  // RESTXQ Module

  /** XQuery function. */
  _RESTXQ_BASE_URI(RestBaseUri.class, "base-uri()", arg(), URI_O, REST_URI),
  /** XQuery function. */
  _RESTXQ_INIT(RestInit.class, "init()", arg(), EMP, REST_URI),
  /** XQuery function. */
  _RESTXQ_URI(RestUri.class, "uri()", arg(), URI_O, REST_URI),
  /** XQuery function. */
  _RESTXQ_WADL(RestWadl.class, "wadl()", arg(), ELM_O, REST_URI),

  // Session Module

  /** XQuery function. */
  _SESSION_ACCESSED(SessionAccessed.class, "accessed()", arg(), DTM_O, flag(NDT), SESSION_URI),
  /** XQuery function. */
  _SESSION_CLOSE(SessionClose.class, "close()", arg(), EMP, flag(NDT), SESSION_URI),
  /** XQuery function. */
  _SESSION_CREATED(SessionCreated.class, "created()", arg(), DTM_O, flag(NDT), SESSION_URI),
  /** XQuery function. */
  _SESSION_DELETE(SessionDelete.class, "delete(key)", arg(STR_O), EMP, flag(NDT), SESSION_URI),
  /** XQuery function. */
  _SESSION_GET(SessionGet.class, "get(key[,default])",
      arg(STR_O, ITEM_ZM), ITEM_ZM, flag(NDT), SESSION_URI),
  /** XQuery function. */
  _SESSION_ID(SessionId.class, "id()", arg(), STR_O, flag(NDT), SESSION_URI),
  /** XQuery function. */
  _SESSION_NAMES(SessionNames.class, "names()", arg(), STR_ZM, flag(NDT), SESSION_URI),
  /** XQuery function. */
  _SESSION_SET(SessionSet.class, "set(key,value)",
      arg(STR_O, ITEM_ZM), EMP, flag(NDT), SESSION_URI),

  // Sessions Module

  /** XQuery function. */
  _SESSIONS_ACCESSED(SessionsAccessed.class, "accessed(id)",
      arg(STR_O), DTM_O, flag(NDT), SESSIONS_URI),
  /** XQuery function. */
  _SESSIONS_CLOSE(SessionsClose.class, "close(id,)", arg(STR_O), EMP, flag(NDT), SESSIONS_URI),
  /** XQuery function. */
  _SESSIONS_CREATED(SessionsCreated.class, "created(id)",
      arg(STR_O), DTM_O, flag(NDT), SESSIONS_URI),
  /** XQuery function. */
  _SESSIONS_DELETE(SessionsDelete.class, "delete(id,key)",
      arg(STR_O, STR_O), EMP, flag(NDT), SESSIONS_URI),
  /** XQuery function. */
  _SESSIONS_GET(SessionsGet.class, "get(id,key[,default])",
      arg(STR_O, STR_O, ITEM_ZM), ITEM_ZM, flag(NDT), SESSIONS_URI),
  /** XQuery function. */
  _SESSIONS_IDS(SessionsIds.class, "ids()", arg(), STR_ZM, flag(NDT), SESSIONS_URI),
  /** XQuery function. */
  _SESSIONS_NAMES(SessionsNames.class, "names(id)", arg(STR_O), STR_ZM, flag(NDT), SESSIONS_URI),
  /** XQuery function. */
  _SESSIONS_SET(SessionsSet.class, "set(id,key,value)",
      arg(STR_O, STR_O, ITEM_ZM), EMP, flag(NDT), SESSIONS_URI),

  // WebSocket Module

  /** XQuery function. */
  _WS_BROADCAST(WsBroadcast.class, "broadcast(message)", arg(ITEM_O), EMP, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_CLOSE(WsClose.class, "close(id)", arg(STR_O), EMP, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_DELETE(WsDelete.class, "delete(id,key)", arg(STR_O, STR_O), EMP, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_EMIT(WsEmit.class, "emit(message)", arg(ITEM_O), EMP, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_EVAL(WsEval.class, "eval(string[,bindings[,options]])",
      arg(STR_O, MAP_ZO, MAP_ZO), STR_O, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_GET(WsGet.class, "get(id,key[,default])",
      arg(STR_O, STR_O, ITEM_ZM), ITEM_ZM, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_ID(WsId.class, "id()", arg(), STR_O, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_IDS(WsIds.class, "ids()", arg(), STR_ZM, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_PATH(WsPath.class, "path(id)", arg(STR_O), STR_O, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_SEND(WsSend.class, "send(message[,ids])", arg(ITEM_O, STR_ZM), EMP, flag(NDT), WS_URI),
  /** XQuery function. */
  _WS_SET(WsSet.class, "set(id,key,value)", arg(STR_O, STR_O, ITEM_ZM), EMP, flag(NDT), WS_URI);

  /** Function definition. */
  private final FuncDefinition definition;

  /**
   * Constructs a function signature; calls
   * {@link #ApiFunction(Class, String, SeqType[], SeqType, EnumSet)}.
   * @param func reference to the class containing the function implementation
   * @param desc descriptive function string
   * @param args types of the function arguments
   * @param seqType return type
   */
  ApiFunction(final Class<? extends StandardFunc> func, final String desc, final SeqType[] args,
      final SeqType seqType) {
    this(func, desc, args, seqType, EnumSet.noneOf(Flag.class));
  }

  /**
   * Constructs a function signature; calls
   * {@link #ApiFunction(Class, String, SeqType[], SeqType, EnumSet)}.
   * @param func reference to the class containing the function implementation
   * @param desc descriptive function string
   * @param args types of the function arguments
   * @param type return type
   * @param uri uri
   */
  ApiFunction(final Class<? extends StandardFunc> func, final String desc, final SeqType[] args,
      final SeqType type, final byte[] uri) {
    this(func, desc, args, type, EnumSet.noneOf(Flag.class), uri);
  }

  /**
   * Constructs a function signature; calls
   * {@link #ApiFunction(Class, String, SeqType[], SeqType, EnumSet, byte[])}.
   * @param func reference to the class containing the function implementation
   * @param desc descriptive function string
   * @param args types of the function arguments
   * @param seqType return type
   * @param flag static function properties
   */
  ApiFunction(final Class<? extends StandardFunc> func, final String desc, final SeqType[] args,
      final SeqType seqType, final EnumSet<Flag> flag) {
    this(func, desc, args, seqType, flag, FN_URI);
  }

  /**
   * Constructs a function signature.
   * @param func reference to the class containing the function implementation
   * @param desc descriptive function string, containing the function name and its
   *             arguments in parentheses. Optional arguments are represented in nested
   *             square brackets; three dots indicate that the number of arguments of a
   *             function is not limited
   * @param params parameter types
   * @param seqType return type
   * @param flags static function properties
   * @param uri uri
   */
  ApiFunction(final Class<? extends StandardFunc> func, final String desc, final SeqType[] params,
      final SeqType seqType, final EnumSet<Flag> flags, final byte[] uri) {
    definition = new FuncDefinition(this, func, desc, params, seqType, flags, uri);
  }

  @Override
  public FuncDefinition definition() {
    return definition;
  }

  /**
   * Returns an array representation of the specified sequence types.
   * @param arg arguments
   * @return array
   */
  private static SeqType[] arg(final SeqType... arg) {
    return arg;
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
    for(final ApiFunction func : values()) list.add(func.definition);
  }

  @Override
  public final String toString() {
    return definition.toString();
  }
}
