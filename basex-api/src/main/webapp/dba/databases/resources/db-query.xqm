(:~
 : Query resources.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../../modules/cons.xqm';
import module namespace util = 'dba/util' at '../../modules/util.xqm';

(:~
 : Runs a query on a document and returns the result as string.
 : @param  $name      name of database
 : @param  $resource  resource
 : @param  $query     query
 : @return result string
 :)
declare
  %rest:POST("{$query}")
  %rest:path("/dba/db-query")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resource}")
  %rest:single
  %output:method("text")
function dba:db-query(
  $name      as xs:string,
  $resource  as xs:string,
  $query     as xs:string
) as xs:string {
  cons:check(),
  util:query(if($query) then $query else '.', db:open($name, $resource))
};
