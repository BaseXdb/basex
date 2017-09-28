(:~
 : Resource handling.
 :
 : @author Christian Grün, BaseX Team, 2014-17
 :)
module namespace dba = 'dba/databases';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Redirects to the specified action.
 : @param  $action    action to perform
 : @param  $name      name of resource
 : @param  $resource  resource
 : @return redirection
 :)
declare
  %rest:POST
  %rest:path("/dba/resource")
  %rest:form-param("action",   "{$action}")
  %rest:form-param("name",     "{$name}")
  %rest:form-param("resource", "{$resource}")
function dba:resource-redirect(
  $action    as xs:string,
  $name      as xs:string,
  $resource  as xs:string*
) as element(rest:response) {
  web:redirect($action, map { 'name': $name, 'resource': $resource })
};
