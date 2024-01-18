(:~
 : Close file.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/editor';

import module namespace config = 'dba/config' at '../lib/config.xqm';

(:~
 : Closes a file.
 : @param  $name  name of file
 :)
declare
  %rest:POST
  %rest:path('/dba/editor-close')
  %rest:query-param('name', '{$name}')
function dba:editor-close(
  $name  as xs:string
) as empty-sequence() {
  config:file('')
};
