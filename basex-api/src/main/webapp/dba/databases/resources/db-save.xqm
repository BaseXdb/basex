(:~
 : Save resource.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/databases';

(:~
 : Saves the edited content of an XML resource.
 : @param  $name      database
 : @param  $resource  resource
 : @param  $content   new content
 : @return empty output
 :)
declare
  %updating
  %rest:POST('{$content}')
  %rest:path('/dba/db-save')
  %rest:query-param('name',     '{$name}')
  %rest:query-param('resource', '{$resource}')
  %rest:single
  %output:method('text')
function dba:db-save(
  $name      as xs:string,
  $resource  as xs:string,
  $content   as xs:string?
) {
  db:put($name, parse-xml($content), $resource),
  update:output('')
};
