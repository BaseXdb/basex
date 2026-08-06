(:~
 : Optimize databases.
 :
 : @author Christian Grün, BaseX Team, BSD License
 :)
module namespace dba = 'dba/databases';

import module namespace html = 'dba/html' at '../lib/html.xqm';
import module namespace utils = 'dba/utils' at '../lib/utils.xqm';

(:~ Top category :)
declare variable $dba:CAT := 'databases';
(:~ Sub category :)
declare variable $dba:SUB := 'database';

(:~
 : Optimize single database.
 : @param  $name  entered name
 : @param  $all   optimize all
 : @param  $opts  database options
 : @param  $lang  language
 : @param  $do    perform update
 : @return form or redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/db-optimize')
  %rest:form-param('name', '{$name}')
  %rest:form-param('all',  '{$all}')
  %rest:form-param('opts', '{$opts}')
  %rest:form-param('lang', '{$lang}')
  %rest:form-param('do',   '{$do}')
  %output:method('html')
function dba:db-optimize(
  $name  as xs:string,
  $all   as xs:string?,
  $opts  as xs:string*,
  $lang  as xs:string?,
  $do    as xs:string?
) {
  html:update($do, { 'header': ($dba:CAT, $name) }, fn() {
    let $opts := if ($do) then $opts else db:info($name)//*[text() = 'true']/name()
    let $lang := if ($do) then $lang else db:property($name, 'language')
    return <div class='panel'>
      <form method='post' autocomplete='off'>
        <input type='hidden' name='do' value='do'/>
        <input type='hidden' name='name' value='{ $name }'/>
        <h2>{
          html:link('Databases', $dba:CAT), ' » ',
          html:link($name, 'database', { 'name': $name }), ' » ',
          html:button('db-optimize', 'Optimize')
        }</h2>
        {
          html:checkbox('all', 'all', exists($all), 'Full optimization'),
          <h3>{ html:option('textindex', 'Text Index', $opts) }</h3>,
          <h3>{ html:option('attrindex', 'Attribute Index', $opts) }</h3>,
          <h3>{ html:option('tokenindex', 'Token Index', $opts) }</h3>,
          <h3>{ html:option('ftindex', 'Fulltext Index', $opts) }</h3>,
          html:option('stemming', 'Stemming', $opts),
          html:option('casesens', 'Case Sensitivity', $opts),
          html:option('diacritics', 'Diacritics', $opts),
          html:field('Language:', <input type='text' name='lang' value='{ $lang }'/>)
        }
      </form>
    </div>
  }, fn() {
    db:optimize($name, boolean($all), map:merge((
      ('textindex', 'attrindex', 'tokenindex', 'ftindex', 'stemming', 'casesens', 'diacritics')
      ! map:entry(., $opts = .),
      $lang ! map:entry('language', .)
    ))),
    utils:redirect($dba:SUB, { 'name': $name, 'info': 'Database was optimized.' })
  })
};
