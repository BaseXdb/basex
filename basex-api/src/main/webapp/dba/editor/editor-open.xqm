(:~
 : Open file.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/editor';

import module namespace config = 'dba/config' at '../lib/config.xqm';

(:~
 : Returns the string content of a file.
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
  file:read-text(config:directory() || $name),
  config:file($name)
};
