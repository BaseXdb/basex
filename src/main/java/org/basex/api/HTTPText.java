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
  /** Configuration: client flag. */
  String DBCLIENT = "org.basex.client";

  /** HTTP String. */
  String HTTP = "HTTP";
  /** Configuration: JAX-RX path. */
  String JAX_RXPATH = "org.basex.jaxrxpath";
  /** Configuration: serializer options. */
  String SERIALIZER = "org.jaxrx.parameter.output";

  /** JAX-RX String. */
  String JAX_RX = "JAX-RX";
}
