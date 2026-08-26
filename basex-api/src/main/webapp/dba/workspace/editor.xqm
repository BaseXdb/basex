(:~
 : Open and save the file of the editor.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/editor';

import module namespace utils = 'dba/lib/utils' at '../lib/utils.xqm';

(:~
 : Returns the content of a file.
 : @param  $name  name of file
 : @param  $dir   directory of the file panel
 : @return content
 :)
declare
  %rest:path('/dba/editor-open')
  %rest:query-param('name', '{$name}')
  %rest:query-param('dir',  '{$dir}')
  %output:method('text')
function dba:editor-open(
  $name  as xs:string,
  $dir   as xs:string?
) as xs:string {
  file:read-text(utils:file-path($dir, $name))
};

(:~
 : Saves a file.
 : @param  $name     name of file
 : @param  $dir      directory of the file panel
 : @param  $content  file content
 : @return empty response
 :)
declare
  %rest:POST('{$content}')
  %rest:path('/dba/editor-save')
  %rest:query-param('name', '{$name}')
  %rest:query-param('dir',  '{$dir}')
  %output:method('text')
function dba:editor-save(
  $name     as xs:string,
  $dir      as xs:string?,
  $content  as xs:string?
) as xs:string {
  let $path := utils:file-path($dir, $name)
  let $string := string($content)
  return (
    (: validate file :)
    if (matches($path, $utils:XQUERY-REGEX, 'i')) { void(utils:query-parse($string, $path)) },
    file:write-text($path, $string),
    ''
  )
};
