(:~
 : Resource handling.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/databases';

import module namespace web = 'dba/web' at '../../modules/web.xqm';

(:~ Top category :)
declare variable $_:CAT := 'databases';
(:~ Sub category :)
declare variable $_:SUB := 'database';

(:~
 : Redirects to the specified action.
 : @param  $action    action to perform
 : @param  $name      name of resource
 : @param  $resource  resource
 :)
declare
  %updating
  %rest:POST
  %rest:path("dba/resource")
  %rest:form-param("action",   "{$action}")
  %rest:form-param("name",     "{$name}")
  %rest:form-param("resource", "{$resource}")
function _:resource(
  $action    as xs:string,
  $name      as xs:string,
  $resource  as xs:string*
) {
  web:check(),
  if($action = ('rename', 'replace')) then (
    web:redirect($action, map { 'name': $name, 'resource': $resource })
  ) else (
    (: download :)
    web:redirect($action || '/' || file:name($resource), map { 'name': $name, 'resource': $resource})
  )
};
