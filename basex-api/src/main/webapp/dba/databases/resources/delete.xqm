(:~
 : Delete resources.
 :
 : @author Christian Grün, BaseX Team, 2014-16
 :)
module namespace dba = 'dba/databases';

import module namespace cons = 'dba/cons' at '../../modules/cons.xqm';

(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Deletes resources.
 : @param  $names     database
 : @param  $resource  resources
 :)
declare
  %updating
  %rest:GET
  %rest:path("/dba/delete")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resources}")
  %output:method("html")
function dba:delete(
  $name       as xs:string,
  $resources  as xs:string*
) {
  cons:check(),
  try {
    $resources ! db:delete($name, .),
    cons:redirect($dba:SUB,
      map { 'name': $name, 'info': 'Deleted resources: ' || count($resources) }
    )
  } catch * {
    cons:redirect($dba:SUB, map { 'name': $name, 'error': $err:description })
  }
};
