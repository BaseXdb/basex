(:~
 : Web functions.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace web = 'dba/web';

import module namespace Request = 'http://exquery.org/ns/request';
import module namespace G = 'dba/global' at 'global.xqm';

(:~
 : <p>Creates a URL representation for the specified URI path
 : and query parameters. All parameters will be URI-encoded
 : and appended to the link. Map entries with empty string values
 : will be ignored.</p>
 : <p>An example:
 :   <code>{ 'a':'foo', 'b':'bar' } -> ?a=foo&amp;b=bar</code>.
 : </p>
 : @param  $link    the target page
 : @param  $params  the parameters to add
 : @return url
 :)
declare function web:create-url(
  $link    as xs:string,
  $params  as map(*)
) as xs:string? {
  $link || string-join(
    for $k in map:keys($params) ! string()
    for $v in distinct-values($params($k))
    count $c
    return (
      if($c = 1) then '?' else '&amp;',
      encode-for-uri($k) || '=' || encode-for-uri(xs:string($v))
    )
  )
};

(:~
 : Returns the mime-type for the specified file.
 : @param  $name  file name
 : @return mime type
 :)
declare function web:mime-type(
  $name  as xs:string
) as xs:string {
  Q{java:org.basex.io.MimeTypes}get($name)
};

(:~
 : Creates a RESTXQ (HTTP) redirect header for the specified page.
 : @param  $page  page to forward to
 :)
declare %updating function web:redirect(
  $page  as xs:string
) {
  web:redirect($page, map { })
};

(:~
 : Creates a RESTXQ (HTTP) redirect header for the specified page and parameters.
 : @param  $page    page to forward to
 : @param  $params  map with query parameters
 :)
declare %updating function web:redirect(
  $page    as xs:string,
  $params  as map(*)
) {
  db:output(
    web:redirect-ro($page, $params)
  )
};

(:~
 : Creates a RESTXQ (HTTP) redirect header for the specified page.
 : @param  $page    page to forward to
 : @return redirect header
 :)
declare function web:redirect-ro(
  $page    as xs:string
) as element(rest:redirect) {
  web:redirect-ro($page, map {})
};

(:~
 : Creates a RESTXQ (HTTP) redirect header for the specified page and parameters.
 : @param  $page    page to forward to
 : @param  $params  map with query parameters
 : @return redirect header
 :)
declare function web:redirect-ro(
  $page    as xs:string,
  $params  as map(*)
) as element(rest:redirect) {
  element rest:redirect { web:create-url($page, $params) }
};

(:~
 : Checks if the current client is logged in. If not, raises an error.
 :)
declare function web:check(
) as empty-sequence() {
  if($G:SESSION) then () else error($G:LOGIN-ERROR, 'Please log in again.')
};

(:~
 : Evaluates a query and returns the result.
 : @param  $query  query
 : @return result of query
 :)
declare function web:query(
  $query  as xs:string?
) as xs:string {
  web:query($query, '', map {})
};

(:~
 : Evaluates a query and returns the result.
 : @param  $query  query
 : @param  $map    bindings
 : @param  $vars   variable bindings
 : @return result of query
 :)
declare function web:query(
  $query  as xs:string?,
  $map    as xs:string,
  $vars   as map(*)
) as xs:string {
  let $limit := $G:MAX-CHARS
  let $query := if($query) then $query else '()'
  let $q := "xquery:eval($query, map {" || $map || "}, " || web:query-options() || ")"
  let $s := "serialize(" || $q || ", map{ 'limit': $limit*2, 'method': 'adaptive' })"
  return web:eval(
    $s ||
    "! (if(string-length(.) > $limit) then substring(., 1, $limit) || '...' else .)",
    map:merge((map { 'query': $query, 'limit': $limit }, $vars))
  )
};

(:~
 : Runs an updating query.
 : @param  $query  query
 : @return empty sequence
 :)
declare %updating function web:update-query(
  $query  as xs:string?
) {
  let $q := "xquery:update($query, map { }, " || web:query-options() || ")"
  return web:update($q, map { 'query': if($query) then $query else '()' })
};

(:~
 : Returns the options for evaluating a query.
 : @return options
 :)
declare %private function web:query-options() {
  "map { 'timeout':" || $G:TIMEOUT ||
       ",'memory':" || $G:MEMORY ||
       ",'permission':'" || $G:PERMISSION || "' }"
};

(:~
 : Evaluates the specified query locally or on a remote server and returns the results.
 : @param  $query  query to be executed
 : @return result
 :)
declare function web:eval(
  $query  as xs:string
) as item()* {
  web:eval($query, map {})
};

(:~
 : Evaluates the specified query locally or remotely and returns the resulting items.
 : @param  $query  query to be executed
 : @param  $vars   variables
 : @return result
 :)
declare function web:eval(
  $query  as xs:string,
  $vars   as map(*)
) as item()* {
  let $query := string-join((
    map:keys($vars) ! ('declare variable $' || . || ' external;'), $query
  ))
  return if($G:SESSION/host) then (
    web:remote-query($query, $vars)
  ) else (
    xquery:eval($query, $vars)
  )
};

(:~
 : Evaluates the specified updating query locally or remotely.
 : @param  $query  query to be executed
 : @param  $vars   variables
 :)
declare %updating function web:update(
  $query  as xs:string,
  $vars   as map(*)
) {
  let $query := string-join((
    map:keys($vars) ! ('declare variable $' || . || ' external;'), $query
  ))
  return if($G:SESSION/host) then (
    prof:void(web:remote-query($query, $vars))
  ) else (
    xquery:update($query, $vars)
  )
};

(:~
 : Evaluates the specified query on a remote server and returns the results.
 : @param  $query  query to be executed
 : @param  $vars   variables
 : @return result
 :)
declare %private function web:remote-query(
  $query  as xs:string,
  $vars   as map(*)
) as item()* {
  let $id := client:connect($G:SESSION/host, $G:SESSION/port, $G:SESSION/name, $G:SESSION/pass)
  return (
    client:query($id, $query, $vars),
    client:close($id)
  )
};
