(:~
 : Save file.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/editor';

import module namespace config = 'dba/config' at '../lib/config.xqm';

(:~
 : Saves a file and returns the list of stored files.
 : @param  $name     name of file
 : @param  $content  file content
 : @return names of stored files
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
  let $path := config:directory() || $name
  return (
    try {
      void(xquery:parse(string($content), map {
        'plan': false(), 'pass': true(), 'base-uri': $path
      }))
    } catch * {
      error($err:code, 'File was not stored: ' || $err:description, $err:value)
    },
    file:write-text($path, $content),
    config:file($name),
    string-join(config:files(), '/')
  )
};
