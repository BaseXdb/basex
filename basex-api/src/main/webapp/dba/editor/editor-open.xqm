(:~
 : Open file.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/editor';

import module namespace config = 'dba/config' at '../lib/config.xqm';

(:~
 : Returns the content of a file.
 : @param  $name  name of file
 : @return content
 :)
declare
  %rest:path('/dba/editor-open')
  %rest:query-param('name', '{$name}')
  %output:method('text')
function dba:editor-open(
  $name  as xs:string
) as xs:string {
  let $path := config:editor-dir() || $name
  return (
    file:read-text($path),
    config:set-edited-file($path)
  )
};
