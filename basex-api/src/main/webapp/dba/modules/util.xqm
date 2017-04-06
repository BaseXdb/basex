(:~
 : Utility functions.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace util = 'dba/util';

import module namespace cons = 'dba/cons' at 'cons.xqm';

(:~
 : Evaluates a query and returns the result.
 : @param  $query    query string
 : @param  $context  initial context value
 : @return serialized result of query
 :)
declare function util:query(
  $query    as xs:string?,
  $context  as item()*
) as xs:string {
  let $limit := $cons:OPTION($cons:K-MAXCHARS)
  let $result := xquery:eval($query, map { '': $context }, util:query-options())
  (: serialize more characters than requested, because limit represents number of bytes :)
  return util:chop(serialize($result, map { 'limit': $limit * 2 + 1, 'method': 'basex' }), $limit)
};

(:~
 : Runs an updating query.
 : @param  $query  query string
 : @return empty sequence
 :)
declare %updating function util:update-query(
  $query  as xs:string?
) {
  xquery:update($query, map { }, util:query-options())
};

(:~
 : Returns the options for evaluating a query.
 : @return options
 :)
declare %private function util:query-options() as map(*) {
  map {
    'timeout'   : $cons:OPTION($cons:K-TIMEOUT),
    'memory'    : $cons:OPTION($cons:K-MEMORY),
    'permission': $cons:OPTION($cons:K-PERMISSION)
  }
};

(:~
 : Returns the index of the first result to generate.
 : @param  $page  current page
 : @param  $sort  sort key
 : @return last result
 :)
declare function util:start(
  $page  as xs:integer,
  $sort  as xs:string
) as xs:integer {
  if($page and not($sort)) then (
    ($page - 1) * $cons:OPTION($cons:K-MAXROWS) + 1
  ) else (
    1
  )
};

(:~
 : Returns the index of the last result to generate.
 : @param  $page  current page
 : @param  $sort  sort key
 : @return last result
 :)
declare function util:end(
  $page  as xs:integer,
  $sort  as xs:string
) as xs:integer {
  if($page and not($sort)) then (
    $page * $cons:OPTION($cons:K-MAXROWS)
  ) else (
    999999999
  )
};

(:~
 : Chops a string result to the maximum number of allowed characters.
 : @param  $string  string
 : @param  $max     maximum number of characters
 : @return string
 :)
declare function util:chop(
  $string  as xs:string,
  $max     as xs:integer
) {
  if(string-length($string) > $max) then (
    substring($string, 1, $max) || '...'
  ) else (
    $string
  )
};
