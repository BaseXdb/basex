(:~
 : Save file.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/editor';

import module namespace config = 'dba/config' at '../lib/config.xqm';
import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~
 : Saves a file and returns the names of the editable files.
 : @param  $name     name of file
 : @param  $content  file content
 : @return editable files
 :)
declare
  %rest:POST('{$content}')
  %rest:path('/dba/editor-save')
  %rest:query-param('name', '{$name}')
  %output:method('text')
function dba:editor-save(
  $name     as xs:string,
  $content  as xs:string?
) as xs:string {
  let $path := config:editor-dir() || $name
  let $string := string($content)
  return (
    (: validate file :)
    if (matches($path, '\.xq(m|l|y|u|uery)?$')) { void(utils:query-parse($string, $path)) },
    file:write-text($path, $string),
    config:set-edited-file($path),
    string-join(config:editor-files(), '/')
  )
};
