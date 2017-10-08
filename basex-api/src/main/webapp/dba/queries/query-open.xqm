(:~
 : Open query.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/queries';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';

(:~
 : Returns the contents of a query file.
 : @param  $name  name of query file
 : @return query string
 :)
declare
  %rest:path("/dba/query-open")
  %rest:query-param("name", "{$name}")
  %output:method("text")
function dba:query-open(
  $name  as xs:string
) as xs:string {
  cons:check(),
  cons:save(map { $cons:K-QUERY: $name }),
  file:read-text(cons:dir() || $name)
};
