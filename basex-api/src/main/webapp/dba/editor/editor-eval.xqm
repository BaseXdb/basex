(:~
 : Evaluate query.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/editor';

import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~
 : Evaluates a query and returns the result.
 : @param  $query  query string
 : @return result of query
 :)
declare
  %rest:POST('{$query}')
  %rest:path('/dba/editor-eval')
  %rest:single
  %output:method('text')
function dba:editor-eval(
  $query  as xs:string?
) as xs:string {
  utils:query($query, ())
};

(:~
 : Runs an updating query.
 : @param  $query  query string
 : @return result of query
 :)
declare
  %updating
  %rest:POST('{$query}')
  %rest:path('/dba/editor-update')
  %rest:single
  %output:method('text')
function dba:editor-update(
  $query  as xs:string?
) as empty-sequence() {
  utils:update-query($query)
};
