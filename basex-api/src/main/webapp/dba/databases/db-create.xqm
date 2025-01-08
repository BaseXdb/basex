(:~
 : Create new database.
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
 : Create new database.
 : @param  $name  entered name
 : @param  $opts  chosen database options
 : @param  $lang  entered language
 : @param  $do    perform update
 : @return form or redirection
 :)
declare
  %updating
  %rest:POST
  %rest:path('/dba/db-create')
  %rest:form-param('name', '{$name}')
  %rest:form-param('opts', '{$opts}')
  %rest:form-param('lang', '{$lang}')
  %rest:form-param('do',   '{$do}')
  %output:method('html')
  %output:html-version('5')
function dba:db-create(
  $name  as xs:string?,
  $opts  as xs:string*,
  $lang  as xs:string?,
  $do    as xs:string?
) {
  html:update($do, { 'header': $dba:CAT }, fn() {
    let $opts := if ($do) then $opts else ('textindex', 'attrindex')
    let $lang := if ($do) then $lang else 'en'
    return <tr>
      <td>
        <form method='post' autocomplete='off'>
          <input type='hidden' name='do' value='do'/>
          <h2>{
            html:link('Databases', $dba:CAT), ' » ',
            html:button('db-create', 'Create')
          }</h2>
          <table>
            <tr>
              <td>Name:</td>
              <td>
                <input type='text' name='name' value='{ $name }' autofocus=''/>
                <div class='small'/>
              </td>
            </tr>
            <tr>
              <td colspan='2'>{
                <h3>{ html:option('textindex', 'Text Index', $opts) }</h3>,
                <h3>{ html:option('attrindex', 'Attribute Index', $opts) }</h3>,
                <h3>{ html:option('tokenindex', 'Token Index', $opts) }</h3>,
                html:option('updindex', 'Incremental Indexing', $opts),
                <div class='small'/>,
                <h3>{ html:option('ftindex', 'Fulltext Indexing', $opts) }</h3>
              }</td>
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
    if (db:exists($name)) {
      error((), 'Database already exists.')
    } else {
      db:create($name, (), (), map:merge((
        for $option in ('textindex', 'attrindex', 'tokenindex', 'ftindex',
          'stemming', 'casesens', 'diacritics', 'updindex')
        return map:entry($option, $opts = $option),
        $lang ! map:entry('language', .)))
      ),
      utils:redirect($dba:SUB, { 'name': $name, 'info': `Database "{ $name }" was created.` })
    }
  })
};
