package org.basex.api;

/**
 * This class assembles texts which are used in the HTTP classes.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public interface HTTPText {
  /** Configuration: database user. */
  String DBUSER = "org.basex.user";
  /** Configuration: database user password. */
  String DBPASS = "org.basex.password";
  /** Configuration: database server port. */
  String DBPORT = "org.basex.serverport";
  /** Configuration: local flag. */
  String LOCAL = "org.basex.jaxrx.local";
  /** Configuration: serializer options. */
  String SERIALIZER = "org.jaxrx.parameter.output";

  /** HTTP String. */
  String HTTP = "HTTP";
  /** Configuration: JAX-RX path. */
  String JAXRXPATH = "org.basex.jaxrxpath";
  /** JAX-RX String. */
  String JAXRX = "JAX-RX";
}
