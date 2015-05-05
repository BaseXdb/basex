(:~
 : Delete resources.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/databases';

import module namespace cons = 'dba/cons' at '../../modules/cons.xqm';
import module namespace util = 'dba/util' at '../../modules/util.xqm';

(:~ Sub category :)
declare variable $_:SUB := 'database';

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
function _:delete(
  $name       as xs:string,
  $resources  as xs:string*
) {
  cons:check(),
  try {
    util:update("$r ! db:delete($n, .)", map { 'n': $name, 'r': $resources }),
    db:output(web:redirect($_:SUB,
      map { 'name': $name, 'info': 'Deleted resources: ' || count($resources) })
    )
  } catch * {
    db:output(
      web:redirect($_:SUB, map { 'name': $name, 'error': $err:description })
    )
  }
};
