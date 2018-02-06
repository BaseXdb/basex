(:~
 : Save query.
 :
 : @author Christian Grün, BaseX Team, 2014-18
 :)
module namespace dba = 'dba/queries';

import module namespace session = 'dba/session' at '../modules/session.xqm';

(:~
 : Closes a query file.
 : @param  $name   name of query file
 :)
declare
  %rest:POST
  %rest:path("/dba/query-close")
  %rest:query-param("name", "{$name}")
function dba:query-save(
  $name   as xs:string
) as empty-sequence() {
  session:set($session:QUERY, '')
};
