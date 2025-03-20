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
  %output:html-version('5')
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
    return <tr>
      <td>
        <form method='post' autocomplete='off'>
          <input type='hidden' name='do' value='do'/>
          <input type='hidden' name='name' value='{ $name }'/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:link($name, 'database', { 'name': $name }), ' » ',
            html:button('db-optimize', 'Optimize')
          }</h2>
          <table>
            <tr>
              <td colspan='2'>
                { html:checkbox('all', 'all', exists($all), 'Full optimization') }
                <h3>{ html:option('textindex', 'Text Index', $opts) }</h3>
                <h3>{ html:option('attrindex', 'Attribute Index', $opts) }</h3>
                <h3>{ html:option('tokenindex', 'Token Index', $opts) }</h3>
                <h3>{ html:option('ftindex', 'Fulltext Index', $opts) }</h3>
              </td>
            </tr>
            <tr>
              <td colspan='2'>{
                html:option('stemming', 'Stemming', $opts),
                html:option('casesens', 'Case Sensitivity', $opts),
                html:option('diacritics', 'Diacritics', $opts)
              }</td>
            </tr>
            <tr>
              <td>Language:</td>
              <td>
                <input type='text' name='lang' value='{ $lang }'/>
                <div class='small'/>
              </td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  }, fn() {
    db:optimize($name, boolean($all), map:merge((
      ('textindex', 'attrindex', 'tokenindex', 'ftindex', 'stemming', 'casesens', 'diacritics')
      ! map:entry(., $opts = .),
      $lang ! map:entry('language', .)
    ))),
    utils:redirect($dba:SUB, { 'name': $name, 'info': 'Database was optimized.' })
  })
};

(:~
 : Optimizes databases with the given settings.
 : @param  $names  names of databases
 : @return redirection
 :)
declare
  %updating
  %rest:path('/dba/dbs-optimize')
  %rest:form-param('name', '{$names}')
function dba:dbs-optimize(
  $names  as xs:string*
) {
  try {
    $names ! db:optimize(.),
    utils:redirect($dba:CAT, { 'info': utils:info($names, 'database', 'optimized') })
  } catch * {
    utils:redirect($dba:CAT, { 'error': $err:description })
  }
};
