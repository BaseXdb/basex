(:~
 : Create new database.
 :
 : @author Christian Grün, BaseX GmbH, 2014-15
 :)
module namespace _ = 'dba/databases';

import module namespace cons = 'dba/cons' at '../modules/cons.xqm';
import module namespace html = 'dba/html' at '../modules/html.xqm';
import module namespace tmpl = 'dba/tmpl' at '../modules/tmpl.xqm';
import module namespace util = 'dba/util' at '../modules/util.xqm';

(:~ Top category :)
declare variable $_:CAT := 'databases';
(:~ Sub category :)
declare variable $_:SUB := 'database';

(:~
 : Form for creating a new database.
 : @param  $name   entered name
 : @param  $opts   chosen database options
 : @param  $lang   entered language
 : @param  $error  error string
 : @return page
 :)
declare
  %rest:GET
  %rest:path("/dba/create-db")
  %rest:query-param("name",  "{$name}")
  %rest:query-param("opts",  "{$opts}", "textindex", "attrindex")
  %rest:query-param("lang",  "{$lang}", "en")
  %rest:query-param("error", "{$error}")
  %output:method("html")
function _:create(
  $name   as xs:string?,
  $opts   as xs:string*,
  $lang   as xs:string?,
  $error  as xs:string?
) as element(html) {
  cons:check(),
  tmpl:wrap(map { 'top': $_:CAT, 'error': $error },
    <tr>
      <td>
        <form action="create-db" method="post" autocomplete="off">
          <h2>
            <a href="{ $_:CAT }">Databases</a> »
            { html:button('create', 'Create') }
          </h2>
          <!-- dummy value; prevents reset of options when nothing is selected -->
          <input type="hidden" name="opts" value="x"/>
          <table>
            <tr>
              <td>Name:</td>
              <td>
                <input type="text" name="name" value="{ $name }" id="name"/>
                { html:focus('name') }
                <div class='small'/>
              </td>
            </tr>
            <tr>
              <td colspan="2">
                <h3>{ "textindex" ! html:checkbox("opts", ., $opts = ., 'Text Index') }</h3>
                <h3>{ "attrindex" ! html:checkbox("opts", ., $opts = ., 'Attribute Index') }</h3>
                { "updindex" ! html:checkbox("opts", ., $opts = ., 'Incremental Indexing') }<br/>
                <div class='small'/>
                <h3>{ "ftindex" ! html:checkbox("opts", ., $opts = ., 'Fulltext Index') }</h3>
              </td>
            </tr>
            <tr>
              <td colspan="2">
                { "stemming" ! html:checkbox("opts", ., $opts = ., 'Stemming') }<br/>
                { "casesens" ! html:checkbox("opts", ., $opts = ., 'Case Sensitivity') }<br/>
                { "diacritics" ! html:checkbox("opts", ., $opts = ., 'Diacritics') }<br/>
              </td>
            </tr>
            <tr>
              <td>Language:</td>
              <td><input type="text" name="language" value="{ $lang }"/></td>
            </tr>
          </table>
        </form>
      </td>
    </tr>
  )
};

(:~
 : Creates a database.
 : @param  $name  database
 : @param  $opts  database options
 : @param  $lang  language
 :)
declare
  %updating
  %rest:POST
  %rest:path("/dba/create-db")
  %rest:query-param("name", "{$name}")
  %rest:query-param("opts", "{$opts}")
  %rest:query-param("lang", "{$lang}")
function _:create(
  $name  as xs:string,
  $opts  as xs:string*,
  $lang  as xs:string?
) {
  cons:check(),
  try {
    util:update("if(db:exists($name)) then (
      error((), 'Database already exists: ' || $name || '.')
    ) else (
      db:create($name, (), (), map:merge((
      (('textindex','attrindex','ftindex','stemming','casesens','diacritics','updindex') !
         map:entry(., $opts = .)), $lang ! map:entry('language', .))
      ))
    )", map { 'name': $name, 'lang': $lang, 'opts': $opts }),
    db:output(web:redirect($_:SUB, map {
      'info': 'Created Database: ' || $name,
      'name': $name
    }))
  } catch * {
    db:output(web:redirect("create-db", map {
      'error': $err:description,
      'name': $name,
      'opts': $opts,
      'lang': $lang
    }))
  }
};
