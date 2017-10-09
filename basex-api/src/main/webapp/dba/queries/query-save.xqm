(:~
 : Save query.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/queries';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~
 : Saves a query file and returns the list of stored queries.
 : @param  $name   name of query file
 : @param  $query  query string
 : @return names of stored queries
 :)
declare
  %rest:POST("{$query}")
  %rest:path("/dba/query-save")
  %rest:query-param("name", "{$name}")
  %output:method("text")
function dba:query-save(
  $name   as xs:string,
  $query  as xs:string
) as xs:string {
  cons:check(),
  cons:save(map { $cons:K-QUERY: $name }),
  file:write-text(cons:current-dir() || $name, $query),
  string-join(cons:query-files(), '/')
};
