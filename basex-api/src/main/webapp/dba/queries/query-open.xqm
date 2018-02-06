(:~
 : Open query.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/queries';

import module namespace session = 'dba/session' at '../modules/session.xqm';

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
  session:set($session:QUERY, $name),
  file:read-text(session:directory() || $name)
};
