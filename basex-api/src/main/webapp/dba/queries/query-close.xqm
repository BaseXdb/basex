(:~
 : Save query.
 :
 : @author Christian Grün, BaseX Team 2005-22, BSD License
 :)
module namespace dba = 'dba/queries';

import module namespace config = 'dba/config' at '../lib/config.xqm';

(:~
 : Closes a query file.
 : @param  $name  name of query file
 :)
declare
  %rest:POST
  %rest:path('/dba/query-close')
  %rest:query-param('name', '{$name}')
function dba:query-close(
  $name  as xs:string
) as empty-sequence() {
  config:query('')
};
