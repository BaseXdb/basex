package org.basex.geo;

import static org.basex.util.Token.token;

import java.io.IOException;
import java.math.BigInteger;

import org.basex.BaseXGUI;
import org.basex.build.MemBuilder;
import org.basex.build.xml.XMLParser;
import org.basex.core.BaseXException;
import org.basex.data.MemData;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.query.QueryException;
import org.basex.query.QueryModule;
import org.basex.query.iter.ValueBuilder;
import org.basex.query.util.Err;
import org.basex.query.value.Value;
import org.basex.query.value.item.B64;
import org.basex.query.value.item.Bln;
import org.basex.query.value.item.Dbl;
import org.basex.query.value.item.Int;
import org.basex.query.value.item.QNm;
import org.basex.query.value.item.Str;
import org.basex.query.value.node.ANode;
import org.basex.query.value.node.DBNode;
import org.basex.query.value.seq.Empty;
import org.basex.query.value.type.NodeType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKBWriter;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.io.gml2.GMLReader;
import com.vividsolutions.jts.io.gml2.GMLWriter;

/**
 * This module contains geo spatial functions for the Geo module.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Masoumeh Seydi
 */


public class GeoModule extends QueryModule {

	  /** GML URI. */
	  private static final byte[] GMLURI = token("http://www.opengis.net/gml");

	  /** QName gml:Point. */
	  private static final QNm Q_GML_POINT = new QNm("gml:Point", GMLURI);
	  /** QName gml:MultiPoint. */
	  private static final QNm Q_GML_MULTIPOINT = new QNm("gml:MultiPoint", GMLURI);
	  /** QName gml:LineString. */
	  private static final QNm Q_GML_LINESTRING = new QNm("gml:LineString", GMLURI);
	  /** QName gml:LinearRing. */
	  private static final QNm Q_GML_LINEARRING = new QNm("gml:LinearRing", GMLURI);
	  /** QName gml:MultiLineString. */
	  private static final QNm Q_GML_MULTILINESTRING = new QNm("gml:MultiLineString", GMLURI);
	  /** QName gml:Polygon. */
	  private static final QNm Q_GML_POLYGON = new QNm("gml:Polygon", GMLURI);
	  /** QName gml:MultiPolygon. */
	  private static final QNm Q_GML_MULTIPOLYGON = new QNm("gml:MultiPolygon", GMLURI);
	  
	
	  	  
	  /**
	   * @param args args
	   * @throws BaseXException exception
	   */
	  public static void main(String[] args) throws BaseXException {
		  new BaseXGUI();
	  }
	  
	  /**
	   * Reads an element as a gml node and returns the geometry.
	   * @param element xml node containing gml object(s) 
	   * @return geometry
	   * @throws QueryException exception
	   */
	  public Geometry gmlReader(final ANode element) throws QueryException {

	    Geometry geom = null;
	    try {
	      String input = element.serialize().toString();
	      GMLReader gmlReader = new GMLReader();
	      GeometryFactory geoFactory = new GeometryFactory();
	      geom = gmlReader.read(input, geoFactory);

	    } catch (Throwable e) {
	    	  throw GeoErrors.gmlReaderErr(e);
	    }
	    return geom;
	  }
	  
	  /**
	   * Writes an geometry and returns a string representation of the geometry.
	   * @param geometry geometry 
	   * @return string output written string
	   * @throws QueryException exception
	   */
	  public Value gmlWriter(final Geometry geometry) throws QueryException {

	    String geom;
	    try {
	    	if(geometry.isEmpty()) 
	    		return Empty.SEQ;
	    	GMLWriter gmlWriter = new GMLWriter();
	    	gmlWriter.setPrefix("gml");
	    	geom = gmlWriter.write(geometry);
	    } catch (Throwable e) {
	    	  throw GeoErrors.gmlWriterErr(e);
	    }
	    IO io = new IOContent(geom);
		try {
			//final ANode builder;
			final ValueBuilder builder = new ValueBuilder();
			final MemData md = MemBuilder.build(
					new XMLParser(io, context.context.prop));
			builder.add(new DBNode(md, 0));
			return builder.value();
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	  }
	    
	  /**
	   * Returns the dimension of an item.
	   * @param node xml element containing gml object(s)
	   * @return dimension
	   * @throws QueryException query exception
	   */
	  public Int dimension(final ANode node) throws QueryException {

		 if(node.type != NodeType.ELM) 
			 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
		// Retrieve element name
		  QNm qname = node.qname();

	    // Check QName
	    if(qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING)
	       || qname.eq(Q_GML_POLYGON) || qname.eq(Q_GML_MULTIPOINT)
	       || qname.eq(Q_GML_MULTILINESTRING) || qname.eq(Q_GML_MULTIPOLYGON)
	       || qname.eq(Q_GML_LINEARRING)) {
	    	
	    	Geometry geom = gmlReader(node);
		    return Int.get(geom.getDimension());
	    }
	    throw GeoErrors.unrecognizedGeo(node);
	
	  }
	  
	  /**
	   * Returns the name of the geometry type in the GML namespace, or the empty sequence.
	   * @param node xml element containing gml object(s)
	   * @return geometry type
	   * @throws QueryException query exception
	   */

	  public QNm geometryType(final ANode node) throws QueryException {
		
		  if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
		// Retrieve element name
		  QNm qname = node.qname();

	  // Check QName
	    if(qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
	        || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
	        || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
	    	
	    	Geometry geom = gmlReader(node);      
		    QNm geomType = new QNm("gml:" + geom.getGeometryType());
		    return geomType;
	    }
	    throw GeoErrors.unrecognizedGeo(node);
	  }

	  /**
	   * Returns the name of the geometry type in the GML namespace, or the empty sequence.
	   * @param node xml element containing gml object(s)
	   * @return integer value of CRS of the geometry
	   * @throws QueryException query exception
	   */
	  public Int SRID(final ANode node) throws QueryException {
		
		  if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
		// Retrieve element name
		  QNm qname = node.qname();

	  // Check QName
	    if(qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
	        || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
	        || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
	    	
	    	Geometry geom = gmlReader(node);      
		    return Int.get(geom.getSRID());
	    }
	    throw GeoErrors.unrecognizedGeo(node);
	  }
	  
	  /**
	   * Returns the gml:Envelope of the specified geometry. The envelope is the minimum bounding box of this geometry.
	   * @param node xml element containing gml object(s)
	   * @return envelop element
	   * @throws QueryException query exception
	   */
	  public Value envelope(final ANode node) throws QueryException {
	    
		  if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
		// Retrieve element name
		  QNm qname = node.qname();

	  // Check QName
	    if(qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
	       || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
	       || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
	    	
	    	Geometry geom = gmlReader(node);
	    	Geometry env = geom.getEnvelope();
	    	// Write the Geometry in GML2 format
		    return gmlWriter(env);
	    }
	    throw GeoErrors.unrecognizedGeo(node);
	  }

	  /**
	   * Returns the WKT format of a geometry.
	   * @param node xml element containing gml object(s)
	   * @return Well-Known Text geometry representation
	   * @throws QueryException query exception
	   */
	  public Str asText(final ANode node) throws QueryException {
		  
		  if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
		// Retrieve element name
		  QNm qname = node.qname();

	    // Check QName
		  if(qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
		        || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
		        || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
		    	
		    	Geometry geom = gmlReader(node);
			    WKTWriter wktWriter = new WKTWriter();
			    return Str.get(wktWriter.write(geom));
		    }
		   throw GeoErrors.unrecognizedGeo(node);
	  	}

		 /**
		 * Returns the WKB format of a geometry.
		 * @param node xml element containing gml object(s)
		 * @return Well-Known Binary geometry representation
		 * @throws QueryException query exception
		 */
		public B64 asBinary(final ANode node) throws QueryException {
		  
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
			// Retrieve element name
			QNm qname = node.qname();
		
		  // Check QName
			if (qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
					|| qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
					|| qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
				
				Geometry geom = gmlReader(node);
				WKBWriter wkbWriter = new WKBWriter();
				byte[] bin = wkbWriter.write(geom);
				B64 binary = new B64(bin);
				return binary;
			}
			throw GeoErrors.unrecognizedGeo(node);
		}
	
//		/**
//		 * Returns a boolean value which shows if the specified geometry is empty or not.
//		 * @param node xml element containing gml object(s)
//		 * @return boolean value
//		 * @throws QueryException query exception
//		 */
//		public Bln isEmpty(final ANode node) throws QueryException {
//
//			if(node.type != NodeType.ELM) 
//				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
//			// Retrieve element name
//			QNm qname = node.qname();
//
//			// Check QName
//			if(qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
//			      || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
//			      || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
//				
//				Geometry geom = gmlReader(node);
//				return Bln.get(geom.isEmpty());
//			}
//			throw GeoErrors.unrecognizedGeo(node);
//		}

		/**
		 * Returns a boolean value which shows if the specified geometry is simple or not,
		 * which has no anomalous geometric points, such as self intersection or self tangency.
		 * @param node xml element containing gml object(s)
		 * @return boolean value
		 * @throws QueryException query exception
		 */
		public Bln isSimple(final ANode node) throws QueryException {
			
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
			// Retrieve element name
			QNm qname = node.qname();
			
			// Check QName
			if(qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
				|| qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
				|| qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
		    
				Geometry geom = gmlReader(node);
			    return Bln.get(geom.isSimple());
			}
			throw GeoErrors.unrecognizedGeo(node);
		}

		/**
		 * Returns the boundary of the geometry, in GML. The return value is a sequence of either gml:Point or gml:LinearRing elements.
		 * @param node xml element containing gml object(s)
		 * @return boundary element (geometry)
		 * @throws QueryException query exception
		 */
		public Value boundary(final ANode node) throws QueryException {
			
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
			// Retrieve element name
			QNm qname = node.qname();

			// Check QName
			if(qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
			      || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
			      || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
			    
			    Geometry geom = gmlReader(node);
			    Geometry bound = geom.getBoundary();
			    // Write the Geometry in GML2 format
			    return gmlWriter(bound);
			}
			throw GeoErrors.unrecognizedGeo(node);
		}

		/**
		 * Returns a boolean value that shows if two geometries are equal or not.
		 * @param node1 xml element containing gml object(s)
		 * @param node2 xml element containing gml object(s)
		 * @return boolean value
		 * @throws QueryException query exception
		 */
		public Bln isEqual(final ANode node1, final ANode node2) throws QueryException {
			
			if(node1.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);
			if(node2.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);

			// Retrieve element names
			QNm qname1 = node1.qname();
			QNm qname2 = node2.qname();

			// Check QName
			if (qname1.eq(Q_GML_POINT) || qname1.eq(Q_GML_LINESTRING) || qname1.eq(Q_GML_POLYGON)
				      || qname1.eq(Q_GML_MULTIPOINT) || qname1.eq(Q_GML_MULTILINESTRING)
				      || qname1.eq(Q_GML_MULTIPOLYGON) || qname1.eq(Q_GML_LINEARRING)) {
					  
				  if (qname2.eq(Q_GML_POINT) || qname2.eq(Q_GML_LINESTRING)
					 || qname2.eq(Q_GML_POLYGON) || qname2.eq(Q_GML_MULTIPOINT)
					 || qname2.eq(Q_GML_MULTILINESTRING) || qname2.eq(Q_GML_MULTIPOLYGON)
					 || qname2.eq(Q_GML_LINEARRING)) {
					 
					  Geometry geom1 = gmlReader(node1);
					  Geometry geom2 = gmlReader(node2);
						
					  return Bln.get(geom1.equals(geom2));
				  }
				  throw GeoErrors.unrecognizedGeo(qname2.local());
			}
			throw GeoErrors.unrecognizedGeo(qname1.local());
		}

		/**
		 * Returns a boolean value that shows if this geometry is disjoint to the specified geometry.
		 * @param node1 xml element containing gml object(s)
		 * @param node2 xml element containing gml object(s)
		 * @return boolean value
		 * @throws QueryException query exception
		 */
		public Bln disjoint(final ANode node1, final ANode node2) throws QueryException {
		  
			if(node1.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);
			if(node2.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);

			// Retrieve element names
			QNm qname1 = node1.qname();
			QNm qname2 = node2.qname();

			// Check QName
			if (qname1.eq(Q_GML_POINT) || qname1.eq(Q_GML_LINESTRING) || qname1.eq(Q_GML_POLYGON)
				      || qname1.eq(Q_GML_MULTIPOINT) || qname1.eq(Q_GML_MULTILINESTRING)
				      || qname1.eq(Q_GML_MULTIPOLYGON) || qname1.eq(Q_GML_LINEARRING)) {
					  
				  if (qname2.eq(Q_GML_POINT) || qname2.eq(Q_GML_LINESTRING)
					 || qname2.eq(Q_GML_POLYGON) || qname2.eq(Q_GML_MULTIPOINT)
					 || qname2.eq(Q_GML_MULTILINESTRING) || qname2.eq(Q_GML_MULTIPOLYGON)
					 || qname2.eq(Q_GML_LINEARRING)) {
					 
					  Geometry geom1 = gmlReader(node1);
					  Geometry geom2 = gmlReader(node2);
					 return Bln.get(geom1.disjoint(geom2));
				  }
				  throw GeoErrors.unrecognizedGeo(qname2.local());
			}
			throw GeoErrors.unrecognizedGeo(qname1.local());
		}
		
		/**
		 * Returns a boolean value that shows if this geometry intersects the specified geometry.
		 * @param node1 xml element containing gml object(s)
		 * @param node2 xml element containing gml object(s)
		 * @return boolean value
		 * @throws QueryException query exception
		 */
		public Bln intersects(final ANode node1, final ANode node2) throws QueryException {
			
			if(node1.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);
			if(node2.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);

			// Retrieve element names
			QNm qname1 = node1.qname();
			QNm qname2 = node2.qname();

			// Check QName
			if (qname1.eq(Q_GML_POINT) || qname1.eq(Q_GML_LINESTRING) || qname1.eq(Q_GML_POLYGON)
					|| qname1.eq(Q_GML_MULTIPOINT) || qname1.eq(Q_GML_MULTILINESTRING)
				      || qname1.eq(Q_GML_MULTIPOLYGON) || qname1.eq(Q_GML_LINEARRING)) {
					  
				  if (qname2.eq(Q_GML_POINT) || qname2.eq(Q_GML_LINESTRING)
					 || qname2.eq(Q_GML_POLYGON) || qname2.eq(Q_GML_MULTIPOINT)
					 || qname2.eq(Q_GML_MULTILINESTRING) || qname2.eq(Q_GML_MULTIPOLYGON)
					 || qname2.eq(Q_GML_LINEARRING)) {
					  
					  Geometry geom1 = gmlReader(node1);
					  Geometry geom2 = gmlReader(node2);
					  return Bln.get(geom1.intersects(geom2));
				  }
				  throw GeoErrors.unrecognizedGeo(qname2.local());
			}
		  throw GeoErrors.unrecognizedGeo(qname1.local());
	}

		/**
		 * Returns a boolean value that shows if this geometry touches the specified geometry.
		 * @param node1 xml element containing gml object(s)
		 * @param node2 xml element containing gml object(s)
		 * @return boolean value
		 * @throws QueryException query exception
		 */
		public Bln touches(final ANode node1, final ANode node2) throws QueryException {
			
			if(node1.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);
			if(node2.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);

			// Retrieve element names								
			QNm qname1 = node1.qname();
			QNm qname2 = node2.qname();

			// Check QName
			if (qname1.eq(Q_GML_POINT) || qname1.eq(Q_GML_LINESTRING) || qname1.eq(Q_GML_POLYGON)
				      || qname1.eq(Q_GML_MULTIPOINT) || qname1.eq(Q_GML_MULTILINESTRING)
				      || qname1.eq(Q_GML_MULTIPOLYGON) || qname1.eq(Q_GML_LINEARRING)) {
					  
				  if (qname2.eq(Q_GML_POINT) || qname2.eq(Q_GML_LINESTRING)
					 || qname2.eq(Q_GML_POLYGON) || qname2.eq(Q_GML_MULTIPOINT)
					 || qname2.eq(Q_GML_MULTILINESTRING) || qname2.eq(Q_GML_MULTIPOLYGON)
					 || qname2.eq(Q_GML_LINEARRING)) {
					 
					  Geometry geom1 = gmlReader(node1);
					  Geometry geom2 = gmlReader(node2);
					  return Bln.get(geom1.touches(geom2));
				  }
				  throw GeoErrors.unrecognizedGeo(qname2.local());
			}
			throw GeoErrors.unrecognizedGeo(qname1.local());
		}

		/**
		 * Returns a boolean value that shows if this geometry crosses the specified geometry.
		 * @param node1 xml element containing gml object(s)
		 * @param node2 xml element containing gml object(s)
		 * @return boolean value
		 * @throws QueryException query exception
		 */
		public Bln crosses(final ANode node1, final ANode node2) throws QueryException {
		  
			if(node1.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);
			if(node2.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);

			// Retrieve element names
			QNm qname1 = node1.qname();
			QNm qname2 = node2.qname();

		// Check QName
			if (qname1.eq(Q_GML_POINT) || qname1.eq(Q_GML_LINESTRING) || qname1.eq(Q_GML_POLYGON)
			      || qname1.eq(Q_GML_MULTIPOINT) || qname1.eq(Q_GML_MULTILINESTRING)
			      || qname1.eq(Q_GML_MULTIPOLYGON) || qname1.eq(Q_GML_LINEARRING)) {
				
				  
				if (qname2.eq(Q_GML_POINT) || qname2.eq(Q_GML_LINESTRING)
					 || qname2.eq(Q_GML_POLYGON) || qname2.eq(Q_GML_MULTIPOINT)
					 || qname2.eq(Q_GML_MULTILINESTRING) || qname2.eq(Q_GML_MULTIPOLYGON)
					 || qname2.eq(Q_GML_LINEARRING)) {
					
					  Geometry geom1 = gmlReader(node1);
					  Geometry geom2 = gmlReader(node2);
					  return Bln.get(geom1.crosses(geom2));
				}
				throw GeoErrors.unrecognizedGeo(qname2.local());
			}
			throw GeoErrors.unrecognizedGeo(qname1.local());
		}

		/**
		 * Returns a boolean value that shows if this geometry is within the specified geometry.
		 * @param node1 xml element containing gml object(s)
		 * @param node2 xml element containing gml object(s)
		 * @return boolean value
		 * @throws QueryException query exception
		 */
		public Bln within(final ANode node1, final ANode node2) throws QueryException {
		  
			if(node1.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);
			if(node2.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);

			// Retrieve element names
			  QNm qname1 = node1.qname();
			  QNm qname2 = node2.qname();

			  // 	Check QName
			  if (qname1.eq(Q_GML_POINT) || qname1.eq(Q_GML_LINESTRING) || qname1.eq(Q_GML_POLYGON)
				      || qname1.eq(Q_GML_MULTIPOINT) || qname1.eq(Q_GML_MULTILINESTRING)
				      || qname1.eq(Q_GML_MULTIPOLYGON) || qname1.eq(Q_GML_LINEARRING)) {
					  
				  if (qname2.eq(Q_GML_POINT) || qname2.eq(Q_GML_LINESTRING)
					 || qname2.eq(Q_GML_POLYGON) || qname2.eq(Q_GML_MULTIPOINT)
					 || qname2.eq(Q_GML_MULTILINESTRING) || qname2.eq(Q_GML_MULTIPOLYGON)
					 || qname2.eq(Q_GML_LINEARRING)) {
					  
					  Geometry geom1 = gmlReader(node1);
					  Geometry geom2 = gmlReader(node2);
					 return Bln.get(geom1.within(geom2));
				  }
				  throw GeoErrors.unrecognizedGeo(qname2.local());
			  }
			  throw GeoErrors.unrecognizedGeo(qname1.local());
		}

		/**
		 * Returns a boolean value that shows if this geometry contains the specified geometry.
		 * @param node1 xml element containing gml object(s)
		 * @param node2 xml element containing gml object(s)
		 * @return boolean value
		 * @throws QueryException query exception
		 */
		public Bln contains(final ANode node1, final ANode node2) throws QueryException {
		  
			if(node1.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);
			if(node2.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);

			// Retrieve element names
			  QNm qname1 = node1.qname();
			  QNm qname2 = node2.qname();
	
			// Check QName
			  if (qname1.eq(Q_GML_POINT) || qname1.eq(Q_GML_LINESTRING) || qname1.eq(Q_GML_POLYGON)
				      || qname1.eq(Q_GML_MULTIPOINT) || qname1.eq(Q_GML_MULTILINESTRING)
				      || qname1.eq(Q_GML_MULTIPOLYGON) || qname1.eq(Q_GML_LINEARRING)) {
					  
				  if (qname2.eq(Q_GML_POINT) || qname2.eq(Q_GML_LINESTRING)
					 || qname2.eq(Q_GML_POLYGON) || qname2.eq(Q_GML_MULTIPOINT)
					 || qname2.eq(Q_GML_MULTILINESTRING) || qname2.eq(Q_GML_MULTIPOLYGON)
					 || qname2.eq(Q_GML_LINEARRING)) {

					  Geometry geom1 = gmlReader(node1);
					  Geometry geom2 = gmlReader(node2);
					  return Bln.get(geom1.contains(geom2));
				  }
				  throw GeoErrors.unrecognizedGeo(qname2.local());
			  }
			  throw GeoErrors.unrecognizedGeo(qname1.local());
		  }

		/**
		 * Returns a boolean value that shows if this geometry overlaps the specified geometry.
		 * @param node1 xml element containing gml object(s)
		 * @param node2 xml element containing gml object(s)
		 * @return boolean value
		 * @throws QueryException query exception
		 */
		public Bln overlaps(final ANode node1, final ANode node2) throws QueryException {
		  
			if(node1.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);
			if(node2.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);

			// Retrieve element names
			  QNm qname1 = node1.qname();
			  QNm qname2 = node2.qname();
	
			// Check QName
			  if (qname1.eq(Q_GML_POINT) || qname1.eq(Q_GML_LINESTRING) || qname1.eq(Q_GML_POLYGON)
				      || qname1.eq(Q_GML_MULTIPOINT) || qname1.eq(Q_GML_MULTILINESTRING)
				      || qname1.eq(Q_GML_MULTIPOLYGON) || qname1.eq(Q_GML_LINEARRING)) {
					  
				  if (qname2.eq(Q_GML_POINT) || qname2.eq(Q_GML_LINESTRING)
					 || qname2.eq(Q_GML_POLYGON) || qname2.eq(Q_GML_MULTIPOINT)
					 || qname2.eq(Q_GML_MULTILINESTRING) || qname2.eq(Q_GML_MULTIPOLYGON)
					 || qname2.eq(Q_GML_LINEARRING)) {

					  Geometry geom1 = gmlReader(node1);
					  Geometry geom2 = gmlReader(node2);
					  return Bln.get(geom1.overlaps(geom2));
				  }
				  throw GeoErrors.unrecognizedGeo(qname2.local());
			  }
			  throw GeoErrors.unrecognizedGeo(qname1.local());
		 }

		/**
		 * Returns a boolean value that shows if whether relationships between the boundaries,
		 * interiors and exteriors of two geometries match the pattern specified in intersection-matrix-pattern.
		 * @param node1 xml element containing gml object(s)
		 * @param node2 xml element containing gml object(s)
		 * @param intersectionMatrix intersection matrix for two geometries
		 * @return boolean value
		 * @throws QueryException query exception
		 */
		public Bln relate(final ANode node1, final ANode node2, final Str intersectionMatrix) throws QueryException {
		  
			if(node1.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);
			if(node2.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);

			// Retrieve element names
			  QNm qname1 = node1.qname();
			  QNm qname2 = node2.qname();
	
			// Check QName
			  if (qname1.eq(Q_GML_POINT) || qname1.eq(Q_GML_LINESTRING)
					 || qname1.eq(Q_GML_POLYGON) || qname1.eq(Q_GML_LINEARRING)
					 || qname1.eq(Q_GML_MULTIPOINT) || qname1.eq(Q_GML_MULTILINESTRING)
				     || qname1.eq(Q_GML_MULTIPOLYGON)) {
					  
				  if (qname2.eq(Q_GML_POINT) || qname2.eq(Q_GML_LINESTRING)
						  || qname2.eq(Q_GML_POLYGON)|| qname2.eq(Q_GML_LINEARRING)
						  || qname2.eq(Q_GML_MULTIPOINT) || qname2.eq(Q_GML_MULTILINESTRING) 
				  		  || qname2.eq(Q_GML_MULTIPOLYGON)) {

					  Geometry geom1 = gmlReader(node1);
					  Geometry geom2 = gmlReader(node2);
					  return Bln.get(geom1.relate(geom2, intersectionMatrix.toJava()));
				  }
				  throw GeoErrors.unrecognizedGeo(qname2.local());
			  }
			  throw GeoErrors.unrecognizedGeo(qname1.local());
		}

		/**
		 * Returns the shortest distance in the units of the spatial reference system of geometry,
		 * between the geometries.
		 * The distance is the distance between a point on each of the geometries.
		 * @param node1 xml element containing gml object(s)
		 * @param node2 xml element containing gml object(s)
		 * @return distance double value
		 * @throws QueryException query exception
		 */
		public Dbl distance(final ANode node1, final ANode node2) throws QueryException {
		  
			if(node1.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);
			if(node2.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);

			// Retrieve element names
			  QNm qname1 = node1.qname();
			  QNm qname2 = node2.qname();
	
			// Check QName
			  if (qname1.eq(Q_GML_POINT) || qname1.eq(Q_GML_LINESTRING) || qname1.eq(Q_GML_POLYGON)
				      || qname1.eq(Q_GML_MULTIPOINT) || qname1.eq(Q_GML_MULTILINESTRING)
				      || qname1.eq(Q_GML_MULTIPOLYGON) || qname1.eq(Q_GML_LINEARRING)) {
				  
				  if (qname2.eq(Q_GML_POINT) || qname2.eq(Q_GML_LINESTRING)
						 || qname2.eq(Q_GML_POLYGON) || qname2.eq(Q_GML_MULTIPOINT)
						 || qname2.eq(Q_GML_MULTILINESTRING) || qname2.eq(Q_GML_MULTIPOLYGON)
						 || qname2.eq(Q_GML_LINEARRING)) {
						  
					  Geometry geom1 = gmlReader(node1);
					  Geometry geom2 = gmlReader(node2);
					  return Dbl.get(geom1.distance(geom2));
				  }
				  throw GeoErrors.unrecognizedGeo(qname2.local());
			  }
			  throw GeoErrors.unrecognizedGeo(qname1.local());
		}

		/**
		 * Returns a polygon that represents all Points whose distance from this geometric object is less than or equal to distance.
		 * The returned element must be either gml:Polygon, gml:LineString or gml:Point.
		 * @param node xml element containing gml object(s)
		 * @param distance specific distance from the $geometry (the buffer width)
		 * @return buffer geometry as gml element
		 * @throws QueryException query exception
		 */
		public Value buffer(final ANode node, final Dbl distance) throws QueryException {
		  
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);

			// Retrieve element name
			  QNm qname = node.qname();
	
			  // Check QName
			  if(qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
			      || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
			      || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
				  
				  Geometry geom = gmlReader(node);
				  Geometry buff = geom.buffer(distance.toJava().doubleValue());
				// Write the Geometry in GML2 format
				  return gmlWriter(buff);
			  }
			  throw GeoErrors.unrecognizedGeo(qname.local());
		 }

		/**
		 * Returns the convex hull geometry of a geometry in GML, or the empty sequence.
		 * The returned element must be either gml:Polygon, gml:LineString or gml:Point.
		 * @param node xml element containing gml object(s)
		 * @return convex hull geometry as a gml element
		 * @throws QueryException query exception
		 */
		public Value convexHull(final ANode node) throws QueryException {
		  
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
			// Retrieve element name
			  QNm qname = node.qname();
	
			  // Check QName
			  if(qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
			      || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
			      || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
				  
				  Geometry geom = gmlReader(node);
				  Geometry con = geom.convexHull();
				// Write the Geometry in GML2 format
				  return gmlWriter(con);
			  }
			  throw GeoErrors.unrecognizedGeo(qname.local());
		 }

		/**
		 * Returns a geometric object representing the Point set intersection of two geometries.
		 * @param node1 xml element containing gml object(s)
		 * @param node2 xml element containing gml object(s)
		 * @return intersection geometry as a gml element
		 * @throws QueryException query exception
		 */
		public Value intersection(final ANode node1, final ANode node2) throws QueryException {
		 
			if(node1.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);
			if(node2.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);

			// Retrieve element names
			  QNm qname1 = node1.qname();
			  QNm qname2 = node2.qname();
			  
			// Check QName
			  if (qname1.eq(Q_GML_POINT) || qname1.eq(Q_GML_LINESTRING) || qname1.eq(Q_GML_POLYGON)
				      || qname1.eq(Q_GML_MULTIPOINT) || qname1.eq(Q_GML_MULTILINESTRING)
				      || qname1.eq(Q_GML_MULTIPOLYGON) || qname1.eq(Q_GML_LINEARRING)) {
				  
				  if (qname2.eq(Q_GML_POINT) || qname2.eq(Q_GML_LINESTRING)
						 || qname2.eq(Q_GML_POLYGON) || qname2.eq(Q_GML_MULTIPOINT)
						 || qname2.eq(Q_GML_MULTILINESTRING) || qname2.eq(Q_GML_MULTIPOLYGON)
						 || qname2.eq(Q_GML_LINEARRING)) {
					
					  Geometry geom1 = gmlReader(node1);
					  Geometry geom2 = gmlReader(node2);
					  Geometry inter = geom1.intersection(geom2);
					// Write the Geometry in GML2 format
					  return gmlWriter(inter);				  
				  }
				  throw GeoErrors.unrecognizedGeo(qname2.local());
			  }
			  throw GeoErrors.unrecognizedGeo(qname1.local());
		 }

		/**
		 * Returns a geometric object that represents the Point set union of two geometries.
		 * @param node1 xml element containing gml object(s)
		 * @param node2 xml element containing gml object(s)
		 * @return union geometry as a gml element
		 * @throws QueryException query exception
		 */
		public Value union(final ANode node1, final ANode node2) throws QueryException {
		  
			if(node1.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);
			if(node2.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);

			// Retrieve element names
			  QNm qname1 = node1.qname();
			  QNm qname2 = node2.qname();
	
			// Check QName
			  if (qname1.eq(Q_GML_POINT) || qname1.eq(Q_GML_LINESTRING) || qname1.eq(Q_GML_POLYGON)
				      || qname1.eq(Q_GML_MULTIPOINT) || qname1.eq(Q_GML_MULTILINESTRING)
				      || qname1.eq(Q_GML_MULTIPOLYGON) || qname1.eq(Q_GML_LINEARRING)) {
					  
				  if (qname2.eq(Q_GML_POINT) || qname2.eq(Q_GML_LINESTRING)
						 || qname2.eq(Q_GML_POLYGON) || qname2.eq(Q_GML_MULTIPOINT)
						 || qname2.eq(Q_GML_MULTILINESTRING) || qname2.eq(Q_GML_MULTIPOLYGON)
						 || qname2.eq(Q_GML_LINEARRING)) {
					
					  Geometry geom1 = gmlReader(node1);
					  Geometry geom2 = gmlReader(node2);
					  Geometry un = geom1.union(geom2);
					  // Write the Geometry in GML2 format
					  return gmlWriter(un);
				  }
				  throw GeoErrors.unrecognizedGeo(qname2.local());
			  }
			  throw GeoErrors.unrecognizedGeo(qname1.local());
		}

		/**
		 * Returns a geometric object that represents the Point set difference of two geometries.
		 * @param node1 xml element containing gml object(s)
		 * @param node2 xml element containing gml object(s)
		 * @return difference geometry as a gml element
		 * @throws QueryException query exception
		 */
		public Value difference(final ANode node1, final ANode node2) throws QueryException {
			
			if(node1.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);
			if(node2.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);

			  // Retrieve element names
			  QNm qname1 = node1.qname();
			  QNm qname2 = node2.qname();
	
			// Check QName
			  if (qname1.eq(Q_GML_POINT) || qname1.eq(Q_GML_LINESTRING) || qname1.eq(Q_GML_POLYGON)
				      || qname1.eq(Q_GML_MULTIPOINT) || qname1.eq(Q_GML_MULTILINESTRING)
				      || qname1.eq(Q_GML_MULTIPOLYGON) || qname1.eq(Q_GML_LINEARRING)) {
				  
				  if (qname2.eq(Q_GML_POINT) || qname2.eq(Q_GML_LINESTRING)
						 || qname2.eq(Q_GML_POLYGON) || qname2.eq(Q_GML_MULTIPOINT)
						 || qname2.eq(Q_GML_MULTILINESTRING) || qname2.eq(Q_GML_MULTIPOLYGON)
						 || qname2.eq(Q_GML_LINEARRING)) {
					
					  Geometry geom1 = gmlReader(node1);
					  Geometry geom2 = gmlReader(node2);
					  Geometry diff = geom1.difference(geom2);
					// Write the Geometry in GML2 format
					  return gmlWriter(diff);
				  }
				  throw GeoErrors.unrecognizedGeo(qname2.local());
			  }
			  throw GeoErrors.unrecognizedGeo(qname1.local());
		  }

		/**
		 * Returns a geometric object that represents the Point set symmetric difference of two geometries.
		 * @param node1 xml element containing gml object(s)
		 * @param node2 xml element containing gml object(s)
		 * @return symmetric difference geometry as a gml element
		 * @throws QueryException query exception
		 */
		public Value symDifference(final ANode node1, final ANode node2) throws QueryException {
		  
			if(node1.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);
			if(node2.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node2.type);

			// Retrieve element names
			  QNm qname1 = node1.qname();
			  QNm qname2 = node2.qname();
	
			// Check QName
			  if (qname1.eq(Q_GML_POINT) || qname1.eq(Q_GML_LINESTRING) || qname1.eq(Q_GML_POLYGON)
			      || qname1.eq(Q_GML_MULTIPOINT) || qname1.eq(Q_GML_MULTILINESTRING)
			      || qname1.eq(Q_GML_MULTIPOLYGON) || qname1.eq(Q_GML_LINEARRING)) {
				  
				  if (qname2.eq(Q_GML_POINT) || qname2.eq(Q_GML_LINESTRING)
						 || qname2.eq(Q_GML_POLYGON) || qname2.eq(Q_GML_MULTIPOINT)
						 || qname2.eq(Q_GML_MULTILINESTRING) || qname2.eq(Q_GML_MULTIPOLYGON)
						 || qname2.eq(Q_GML_LINEARRING)) {
					 
					  Geometry geom1 = gmlReader(node1);
					  Geometry geom2 = gmlReader(node2);
					  Geometry diff = geom1.symDifference(geom2);
					// Write the Geometry in GML2 format
					  return gmlWriter(diff);
		   		  }
				  throw GeoErrors.unrecognizedGeo(qname2.local());
			 }
			 throw GeoErrors.unrecognizedGeo(qname1.local());
		} 

		/**
		 * Returns number of geometries in a geometry collection, 
		 * or 1 if the input is not a collection.
		 * @param node xml element containing gml object(s)
		 * @return integer value of number of geometries
		 * @throws QueryException query exception
		 */
		public Int numGeometries(final ANode node) throws QueryException {

			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);

			// Retrieve element name
			  QNm qname = node.qname();
	
			// Check QName
			  if (qname.eq(Q_GML_POINT)  || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
			      || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
			      || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
				  
				  Geometry geom = gmlReader(node);
			      return Int.get(geom.getNumGeometries());
			  }
			  throw GeoErrors.unrecognizedGeo(qname.local());
		 }


		/**
		 * Returns the nth geometry of a geometry collection, 
		 * or the geometry if the input is not a collection.
		 * @param node xml element containing gml object(s)
		 * @param geoNumber integer number as the index of nth geometry
		 * @return geometry as a gml element
		 * @throws QueryException query exception
		 */
		public Value geometryN(final ANode node, Int geoNumber) throws QueryException {

			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);

			// Retrieve element name
			  QNm qname = node.qname();
	
			// Check QName
			  if (qname.eq(Q_GML_POINT)  || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
			      || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
			      || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
				  
				  Geometry geom = gmlReader(node);
			      int temp = geom.getNumGeometries();
			      if (((BigInteger) geoNumber.toJava()).intValue() < 1 || ((BigInteger) geoNumber.toJava()).intValue() > temp)
			    	  throw GeoErrors.outOfRangeIdx(geoNumber); 	  
			      Geometry geo = geom.getGeometryN(((BigInteger) geoNumber.toJava()).intValue() - 1);
			        // Write the Geometry in GML2 format
			      return gmlWriter(geo);
			  }
			  throw GeoErrors.unrecognizedGeo(qname.local());
		 }

		/**
		 * Returns the x-coordinate value for point.
		 * @param node xml element containing gml object(s)
		 * @return x double value
		 * @throws QueryException query exception
		 */
		public Dbl x(final ANode node) throws QueryException {
		  
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
			// Retrieve element name
			  QNm qname = node.qname();
	
			// Check QName
			  if (qname.eq(Q_GML_POINT)) {
				  Geometry geom = gmlReader(node);
				  return Dbl.get(geom.getCoordinate().x);
			  }
			  if (qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_POLYGON) || qname.eq(Q_GML_LINESTRING)
			      || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_MULTILINESTRING)
			      || qname.eq(Q_GML_LINEARRING))
				  throw GeoErrors.pointNeeded(qname.local());
			  throw GeoErrors.unrecognizedGeo(qname.local());
		 }

		/**
		 * Returns the y-coordinate value for point.
		 * @param node xml element containing gml object(s)
		 * @return y double value
		 * @throws QueryException query exception
		 */
		public Dbl y(final ANode node) throws QueryException {
		  
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
			// Retrieve element name
			  QNm qname = node.qname();
	
			// Check QName
			  if (qname.eq(Q_GML_POINT)) {
				  Geometry geom = gmlReader(node);
				  return Dbl.get(geom.getCoordinate().y);
			  }
			  if (qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_POLYGON) || qname.eq(Q_GML_LINESTRING)
			      || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_MULTILINESTRING)
			      || qname.eq(Q_GML_LINEARRING))
				  throw GeoErrors.pointNeeded(qname.local());
			  
			  throw GeoErrors.unrecognizedGeo(qname.local());
		 }

		/**
		 * Returns the z-coordinate value for point.
		 * @param node xml element containing gml object(s)
		 * @return z double value
		 * @throws QueryException query exception
		 */
		public Dbl z(final ANode node) throws QueryException {
		  
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);

			// Retrieve element name
			  QNm qname = node.qname();
	
			// Check QName
			  if (qname.eq(Q_GML_POINT)) {
				  Geometry geom = gmlReader(node);
				  return Dbl.get(geom.getCoordinate().z);
			  }
			  if (qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_POLYGON) || qname.eq(Q_GML_LINESTRING)
			      || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_MULTILINESTRING)
			      || qname.eq(Q_GML_LINEARRING))
				  throw GeoErrors.pointNeeded(qname.local());
			  
			  throw GeoErrors.unrecognizedGeo(qname.local());
		 }
		
		/**
		 * Returns the length of this Geometry. Linear geometries return their length.
		 * Areal geometries return their parameter. Others return 0.0
		 * @param node xml element containing gml object(s)
		 * @return length double value
		 * @throws QueryException query exception
		 */
		public Dbl length(final ANode node) throws QueryException {
		  
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);

			// Retrieve element name
			  QNm qname = node.qname();
	
			// Check QName
			  if(qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
			      || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
			      || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
				  
				  Geometry geom = gmlReader(node);
				  return Dbl.get(geom.getLength());
			  }
			  throw GeoErrors.unrecognizedGeo(qname.local());
		 }
		
		/**
		 * Returns the start Point of a line.
		 * @param node xml element containing gml object(s)
		 * @return start point geometry as a gml element
		 * @throws QueryException query exception
		 */
		public Value startPoint(final ANode node) throws QueryException {
		  
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);

			// Retrieve element name
			  QNm qname = node.qname();
	
			// Check QName
			  if(qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_LINEARRING)) {
				  Geometry geom = gmlReader(node);
				  if (geom instanceof LineString) {
					  Point point = ((LineString) geom).getStartPoint();
					// Write the Geometry in GML2 format
					  return gmlWriter(point);
				  } else if (geom instanceof LinearRing) {
					  Point point = ((LinearRing) geom).getStartPoint();
					// Write the Geometry in GML2 format
					  return gmlWriter(point);
				  }
			  }
			  if (qname.eq(Q_GML_POINT) || qname.eq(Q_GML_MULTIPOINT) 
			      ||qname.eq(Q_GML_POLYGON) || qname.eq(Q_GML_MULTIPOLYGON)
			      || qname.eq(Q_GML_MULTILINESTRING))
				  throw GeoErrors.lineNeeded(qname.local());
			  
			  throw GeoErrors.unrecognizedGeo(qname.local());
		 }

		/**
		 * Returns the end Point of a line.
		 * @param node xml element containing gml object(s)
		 * @return end point geometry as a gml element
		 * @throws QueryException query exception
		 */
		public Value endPoint(final ANode node) throws QueryException {
		 			
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
			// Retrieve element name
			  QNm qname = node.qname();
	
			// Check QName
			  if(qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_LINEARRING)) {
				  Geometry geom = gmlReader(node);
				  if (geom instanceof LineString) {
					  Point point = ((LineString) geom).getEndPoint();
					// Write the Geometry in GML2 format
					  return gmlWriter(point);
				  } else if (geom instanceof LinearRing) {
					  Point point = ((LinearRing) geom).getEndPoint();
					// Write the Geometry in GML2 format
					  return gmlWriter(point);
				  }
			  }
			  if (qname.eq(Q_GML_POINT) || qname.eq(Q_GML_MULTIPOINT) 
				  || qname.eq(Q_GML_POLYGON) || qname.eq(Q_GML_MULTIPOLYGON)
			      || qname.eq(Q_GML_MULTILINESTRING))
				  throw GeoErrors.lineNeeded(qname.local());
			  
			  throw GeoErrors.unrecognizedGeo(qname.local());
		 }

		/**
		 * Checks if the line is closed loop. That is, if the start Point is same with end Point.
		 * @param node xml element containing gml object(s)
		 * @return boolean value
		 * @throws QueryException query exception
		 */
		public Bln isClosed(final ANode node) throws QueryException {
			
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);

			  // Retrieve element name
			  QNm qname = node.qname();
	
			// Check QName
			  if (qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_LINEARRING)
			      || qname.eq(Q_GML_MULTILINESTRING)) {
		
				  Geometry geom = gmlReader(node);
				  if (geom instanceof LineString) 
					  return Bln.get(((LineString) geom).isClosed());
					  
				  if (geom instanceof LinearRing) 
					  return Bln.get(((LinearRing) geom).isClosed());
				  
				  if (geom instanceof MultiLineString) 
					  return Bln.get(((MultiLineString) geom).isClosed());
			   
			  }
			  if (qname.eq(Q_GML_POINT) || qname.eq(Q_GML_MULTIPOINT)
			       || qname.eq(Q_GML_POLYGON) || qname.eq(Q_GML_MULTIPOLYGON))
				  throw GeoErrors.lineNeeded(qname.local());
			  
			  throw GeoErrors.unrecognizedGeo(qname.local());
		}

		/**
		 * Return a boolean value that shows weather the line is a ring or not.
		 * A line is a ring if it is closed and simple.
		 * @param node xml element containing gml object(s)
		 * @return boolean value
		 * @throws QueryException query exception
		 */
		public Bln isRing(final ANode node) throws QueryException {
		  
			
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
			// Retrieve element name
			  QNm qname = node.qname();
	
			// Check QName
			  if (qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_LINEARRING)) {
				  
				  Geometry geom = gmlReader(node);
				  if (geom instanceof LineString)
					  return Bln.get(((LineString) geom).isRing());
					  
				  if (geom instanceof LinearRing)
					  return Bln.get(((LinearRing) geom).isRing());
			  }
	
			  if (qname.eq(Q_GML_POINT) || qname.eq(Q_GML_MULTIPOINT)
			      || qname.eq(Q_GML_MULTILINESTRING) || qname.eq(Q_GML_POLYGON)
			      || qname.eq(Q_GML_MULTIPOLYGON))
				  throw GeoErrors.lineNeeded(qname.local());
			  
			  throw GeoErrors.unrecognizedGeo(qname.local());
		}

		/**
		 * Returns the number of points in a geometry.
		 * @param node xml element containing gml object(s)
		 * @return number of points int value
		 * @throws QueryException query exception
		 */
		public Int numPoints(final ANode node) throws QueryException {

			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);

			// Retrieve element name
			  QNm qname = node.qname();
	
			// Check QName
			  if (qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_LINEARRING)
			      || qname.eq(Q_GML_MULTILINESTRING) || qname.eq(Q_GML_POINT)
			      || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_POLYGON)
			      || qname.eq(Q_GML_MULTIPOLYGON)) {
				  
				  Geometry geom = gmlReader(node);
				  return Int.get(geom.getNumPoints());
			  }
//			  if (qname.eq(Q_GML_POINT) || qname.eq(Q_GML_MULTIPOINT)
//			      || qname.eq(Q_GML_POLYGON) || qname.eq(Q_GML_MULTIPOLYGON))
//				  throw GeoErrors.lineNeeded(qname.local());
			  
			  throw GeoErrors.unrecognizedGeo(qname.local());
		}

		/**
		 * Returns the nth point of a line.
		 * @param node xml element containing gml object(s)
		 * @param pointNumber index of i-th point
		 * @return n-th point as a gml element
		 * @throws QueryException query exception
		 */
		public Value pointN(final ANode node, final Int pointNumber) throws QueryException {

			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);

			// Retrieve element name
			  QNm qname = node.qname();
	
			  // Check QName
			  if (qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_LINEARRING)) {
				  
				  Geometry geom = gmlReader(node);
				  int temp = geom.getNumPoints();
			      if (((BigInteger) pointNumber.toJava()).intValue() < 1 || ((BigInteger) pointNumber.toJava()).intValue() > temp)
			    	  throw GeoErrors.outOfRangeIdx(pointNumber); 
			      if (geom instanceof LineString) {
						  Point point = ((LineString) geom).getPointN(((BigInteger) pointNumber.toJava()).intValue() - 1);
						// Write the Geometry in GML2 format
						 return gmlWriter(point);
					  }
					  if (geom instanceof LinearRing) {
						  Point point = ((LinearRing) geom).getPointN(((BigInteger) pointNumber.toJava()).intValue() - 1);
						// Write the Geometry in GML2 format
						  return gmlWriter(point);  
			      }
			  }
			  if (qname.eq(Q_GML_POINT) || qname.eq(Q_GML_MULTIPOINT)
				  || qname.eq(Q_GML_MULTILINESTRING)
			      || qname.eq(Q_GML_POLYGON) || qname.eq(Q_GML_MULTIPOLYGON))
				  throw GeoErrors.lineNeeded(qname.local());
			  
			  throw GeoErrors.unrecognizedGeo(qname.local());
		}

		/**
		 * Returns the area of a Geometry. Areal Geometries have a non-zero area.
		 * Returns zero for Point and Lines.
		 * @param node xml element containing gml object(s)
		 * @return geometry area as a double vaue
		 * @throws QueryException query exception
		 */
		public Dbl area(final ANode node) throws QueryException {
		  
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);

			// Retrieve element name
			QNm qname = node.qname();
	
			// Check QName
			if(qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
			    || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
			    || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
				
				Geometry geom = gmlReader(node);
				return Dbl.get(geom.getArea());
			}
			throw GeoErrors.unrecognizedGeo(qname.local());
		 }

		/**
		 * Returns the mathematical centroid of the geometry as a gml:Point.
		 * The point is not guaranteed to be on the surface.
		 * @param node xml element containing gml object(s)
		 * @return centroid geometry as a gml element
		 * @throws QueryException query exception
		 */
		public Value centroid(final ANode node) throws QueryException {
		  
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);

			// Retrieve element name
			QNm qname = node.qname();
	
			// Check QName
			if(qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
			    || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
			    || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
				 
				Geometry geom = gmlReader(node);
				Point center = geom.getCentroid();
				// Write the Geometry in GML2 format
				return gmlWriter(center);
			}
			throw GeoErrors.unrecognizedGeo(qname.local());
		 }

		/**
		 * Returns a gml:Point that is interior of this geometry.
		 * If it cannot be inside the geometry, then it will be on the boundary.
		 * @param node xml element containing gml object(s)
		 * @return a point as a gml element
		 * @throws QueryException query exception
		 */
		public Value pointOnSurface(final ANode node) throws QueryException {
		  
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);

			// Retrieve element name
			QNm qname = node.qname();
	
			// Check QName
			if(qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING) || qname.eq(Q_GML_POLYGON)
			    || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
			    || qname.eq(Q_GML_MULTIPOLYGON) || qname.eq(Q_GML_LINEARRING)) {
				  
			  Geometry geom = gmlReader(node);
			  Point point = geom.getInteriorPoint();
			// Write the Geometry in GML2 format
			  return gmlWriter(point);
			}
			  
			throw GeoErrors.unrecognizedGeo(qname.local());
		}
		/**
		 * Returns the outer ring of a polygon, in GML.
		 * @param node xml element containing gml object(s)
		 * @return exterior ring geometry (LineString) as a gml element
		 * @throws QueryException query exception
		 */
		public Value exteriorRing(final ANode node) throws QueryException {
		  
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
			// Retrieve element name
			  QNm qname = node.qname();
	
			  if(qname.eq(Q_GML_POLYGON)) {
				  
				  Geometry geom = gmlReader(node);
				  LineString ring = ((Polygon) geom).getExteriorRing();
				// Write the Geometry in GML2 format
				  return gmlWriter(ring);
			  }
			  if (qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING)
			      || qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
			      || qname.eq(Q_GML_LINEARRING) || qname.eq(Q_GML_MULTIPOLYGON))
				  throw GeoErrors.polygonNeeded(qname.local());
			  
			  throw GeoErrors.unrecognizedGeo(node.qname().local());
		}

		/**
		 * Returns the number of interior rings in a polygon.
		 * @param node xml element containing gml object(s)
		 * @return integer number of interior rings
		 * @throws QueryException query exception
		 */
		public Int numInteriorRing(final ANode node) throws QueryException {
			
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);

		  // Retrieve element name
			QNm qname = node.qname();

			if(qname.eq(Q_GML_POLYGON)) {
				Geometry geom = gmlReader(node);
				return Int.get(((Polygon) geom).getNumInteriorRing());
			}
			if (qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING)
					|| qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
					|| qname.eq(Q_GML_LINEARRING) || qname.eq(Q_GML_MULTIPOLYGON))
				throw GeoErrors.polygonNeeded(qname.local());
		 
			throw GeoErrors.unrecognizedGeo(qname.local());
		}

		/**
		 * Returns the nth geometry of a geometry collection.
		 * @param node xml element containing gml object(s)
		 * @param ringNumber index of i-th interior ring
		 * @return n-th interior ring geometry (LineString) as a gml element
		 * @throws QueryException query exception
		 */
		public Value interiorRingN(final ANode node, Int ringNumber) throws QueryException {
			
			if(node.type != NodeType.ELM) 
				 Err.FUNCMP.thrw(null, this, NodeType.ELM, node.type);
			// Retrieve element name
			QNm qname = node.qname();

			// Check QName
			if(qname.eq(Q_GML_POLYGON)) {
				Geometry geom = gmlReader(node);
				int temp = ((Polygon) geom).getNumInteriorRing();
			    if (((BigInteger) ringNumber.toJava()).intValue() < 1 || ((BigInteger) ringNumber.toJava()).intValue() > temp)
			    	throw GeoErrors.outOfRangeIdx(ringNumber); 	  
			    LineString ring = ((Polygon) geom).getInteriorRingN(((BigInteger) ringNumber.toJava()).intValue() - 1);
			    	// Write the Geometry in GML2 format
					return gmlWriter(ring);
		  	}
			if (qname.eq(Q_GML_POINT) || qname.eq(Q_GML_LINESTRING)
					|| qname.eq(Q_GML_MULTIPOINT) || qname.eq(Q_GML_MULTILINESTRING)
					|| qname.eq(Q_GML_LINEARRING) || qname.eq(Q_GML_MULTIPOLYGON))
				throw GeoErrors.polygonNeeded(qname.local());
		
			throw GeoErrors.unrecognizedGeo(qname.local());
		}
	}