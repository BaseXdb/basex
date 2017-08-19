(:~
 : Delete resources.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../../modules/cons.xqm';
import module namespace util = 'dba/util' at '../../modules/util.xqm';

(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Deletes resources.
 : @param  $names     database
 : @param  $resource  resources
 : @return redirection
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/db-delete")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resources}")
  %output:method("html")
function dba:db-delete(
  $name       as xs:string,
  $resources  as xs:string*
) as empty-sequence() {
  cons:check(),
  try {
    $resources ! db:delete($name, .),
    cons:redirect($dba:SUB,
      map { 'name': $name, 'info': util:info($resources, 'resource', 'deleted') }
    )
  } catch * {
    cons:redirect($dba:SUB, map { 'name': $name, 'error': $err:description })
  }
};
