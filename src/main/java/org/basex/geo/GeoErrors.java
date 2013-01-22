package org.basex.geo;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * This module contains static error functions for the Geo module.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Masoumeh Seydi
 */

public final class GeoErrors {
    /** Error namespace. */
    private static final byte[] NS = QueryText.BXERRORS;
    /** Namespace and error code prefix. */
    private static final String PREFIX = "bxerr:BXSE";

    /** Private constructor, preventing instantiation. */
    private GeoErrors() { }

    /**
     * GEO0001: Unrecognized geo object.
     * @param element Geometry object
     * @return query exception
     */
    static QueryException unrecognizedGeo(final Object element) {
      return thrw(1, "Unrecognized Geo type:%", element);
    }

    /**
     * GEO0002: gml reader error massage (JTS).
     * @param e error
     * @return query exception
     */
    static QueryException gmlReaderErr(final Object e) {
      return thrw(2, "%", e);
    }

    /**
     * GEO0003: Input geometry should be a Polygon.
     * @param element Geometry object
     * @return query exception
     */
    static QueryException polygonNeeded(final Object element) {
      return thrw(3, "% is not an appropiate geometry for this function. " +
             "The input geometry should be a Polygon.", element);
    }

    /**
     * GEO0004: Input geometry should be a Line.
     * @param element Geometry object
     * @return query exception
     */
    static QueryException lineNeeded(final Object element) {
      return thrw(4, "% is not an appropiate geometry for this function. " +
            "The input geometry should be a Line.", element);
    }


    /**
     * GEO0005: Input geometry should be a Point.
     * @param element Geometry object
     * @return query exception
     */
     static QueryException pointNeeded(final Object element) {
       return thrw(5, "% is not an appropiate geometry for this function. " +
              "The input geometry should be a Point.", element);
     }

     /**
      * GEO0006: Out of range index.
      * @param geoNumber index
      * @return query exception
      */
     static QueryException outOfRangeIdx(final Int geoNumber) {
       return thrw(6, "Out of range input index: %", geoNumber);
     }

    /**
     * GEO0007: A single Geometry needed.
     * @param element Geometry object
     * @return query exception
     */
     static QueryException singleGeo(final Object element) {
       return thrw(7, "% is not an appropiate geometry for this function. " +
              "The input geometry should be a single geometry," +
              " not a geometry collection.", element);
     }

    /**
     * GEO0008: gml writer error massage (JTS).
     * @param e error
     * @return query exception
     */
     static QueryException gmlWriterErr(final Object e) {
       return thrw(8, "%", e);
     }

     /**
      * Query Exception.
      * @param code code
      * @param msg message
      * @param ext extension
      * @return query exception
      */
     private static QueryException thrw(final int code, final String msg,
         final Object... ext) {

       final QNm name = new QNm(String.format("%s:GEO%04d", PREFIX, code), NS);
       return new QueryException(null, name, msg, ext);
     }

}
