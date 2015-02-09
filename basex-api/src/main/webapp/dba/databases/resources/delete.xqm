(:~
 : Delete resources.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/databases';

import module namespace web = 'dba/web' at '../../modules/web.xqm';

(:~
 : Deletes resources.
 : @param  $names     database
 : @param  $resource  resources
 :)
declare
  %updating
  %rest:GET
  %rest:path("dba/delete")
  %rest:query-param("name",     "{$name}")
  %rest:query-param("resource", "{$resources}")
  %output:method("html")
function _:delete(
  $name       as xs:string,
  $resources  as xs:string*
) {
  web:check(),
  try {
    web:update("$r ! db:delete($n, .)", map { 'n': $name, 'r': $resources }),
    web:redirect("database",
      map { 'name': $name, 'info': 'Deleted resources: ' || count($resources) })
  } catch * {
    web:redirect("database", map { 'name': $name, 'error': $err:description })
  }
};
