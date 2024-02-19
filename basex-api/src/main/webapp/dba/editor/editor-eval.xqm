(:~
 : Evaluate query.
 :
 : @author Christian Grün, BaseX Team 2005-24, BSD License
 :)
module namespace dba = 'dba/editor';

import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~
 : Checks if a query is updating.
 : @param  $query  query string
 : @return result of check
 :)
declare
  %rest:POST('{$query}')
  %rest:path('/dba/parse')
  %output:method('text')
function dba:parse(
  $query  as xs:string?
) as xs:boolean {
  utils:query-parse(string($query), ())/@updating = 'true'
};

(:~
 : Evaluates a query and returns the result.
 : @param  $query  query string
 : @return result of query
 :)
declare
  %rest:POST('{$query}')
  %rest:path('/dba/query')
  %rest:single
  %output:method('text')
function dba:query(
  $query  as xs:string?
) as xs:string {
  utils:query(string($query), ())
};

(:~
 : Runs an updating query.
 : @param  $query  query string
 : @return result of query
 :)
declare
  %updating
  %rest:POST('{$query}')
  %rest:path('/dba/update')
  %rest:single
  %output:method('text')
function dba:update(
  $query  as xs:string?
) as empty-sequence() {
  utils:update(string($query))
};
