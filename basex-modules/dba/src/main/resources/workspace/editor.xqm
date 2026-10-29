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
function dba:editor-open(
  $name  as xs:string,
  $dir   as xs:string?
) as xs:string {
  try {
    file:read-text(utils:file-path($dir, $name))
  } catch file:io-error {
    (: the client previews an image; any other error is reported :)
    if (starts-with(web:content-type($name), 'image/')) then (
      web:error(415, $err:description)
    ) else (
      error($err:code, $err:description, $err:value)
    )
  }
};

(:~
 : Returns the raw content of a file, for previewing it.
 : @param  $name  name of file
 : @param  $dir   directory of the file panel
 : @return rest response and binary data
 :)
declare
  %rest:GET
  %rest:path('/dba/file-raw')
  %rest:query-param('name', '{$name}')
  %rest:query-param('dir',  '{$dir}')
function dba:file-raw(
  $name  as xs:string,
  $dir   as xs:string?
) as item()+ {
  utils:inline(file:read-binary(utils:file-path($dir, $name)), web:content-type($name))
};

(:~
 : Saves a file.
 : @param  $name     name of file
 : @param  $dir      directory of the file panel
 : @param  $replace  replace an existing file
 : @param  $content  file content
 : @return empty response
 :)
declare
  %rest:POST('{$content}')
  %rest:path('/dba/editor-save')
  %rest:query-param('name',    '{$name}')
  %rest:query-param('dir',     '{$dir}')
  %rest:query-param('replace', '{$replace}', 'false')
function dba:editor-save(
  $name     as xs:string,
  $dir      as xs:string?,
  $replace  as xs:string,
  $content  as xs:string?
) as xs:string {
  let $path := utils:file-path($dir, $name)
  let $string := string($content)
  return if ($replace != 'true' and file:exists($path)) then (
    (: the client asks before it replaces a file that it did not open :)
    web:error(409, `File “{ $name }” already exists.`)
  ) else (
    (: validate file :)
    if (matches($path, $utils:XQUERY-REGEX, 'i')) { void(utils:query-parse($string, $path)) },
    file:write-text($path, $string),
    ''
  )
};
