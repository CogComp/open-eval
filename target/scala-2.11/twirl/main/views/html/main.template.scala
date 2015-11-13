
package views.html

import play.twirl.api._
import play.twirl.api.TemplateMagic._


     object main_Scope0 {
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import java.lang._
import java.util._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import play.core.j.PlayMagicForJava._
import play.mvc._
import play.data._
import play.api.data.Field
import play.mvc.Http.Context.Implicit._

class main extends BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with play.twirl.api.Template2[String,Html,play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/(title: String)(content: Html):play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.32*/("""


"""),format.raw/*4.1*/("""<!DOCTYPE html>

<html lang="en">
    <head>
        <title>"""),_display_(/*8.17*/title),format.raw/*8.22*/("""</title>
        <link rel="stylesheet" media="screen" href=""""),_display_(/*9.54*/routes/*9.60*/.Assets.versioned("stylesheets/main.css")),format.raw/*9.101*/("""">
        <link rel="shortcut icon" type="image/png" href=""""),_display_(/*10.59*/routes/*10.65*/.Assets.versioned("images/favicon.png")),format.raw/*10.104*/("""">
        <script src=""""),_display_(/*11.23*/routes/*11.29*/.Assets.versioned("javascripts/hello.js")),format.raw/*11.70*/("""" type="text/javascript"></script>
        <link rel="stylesheet" media="screen" href=""""),_display_(/*12.54*/routes/*12.60*/.WebJarAssets.at(WebJarAssets.locate("bootstrap.min.css"))),format.raw/*12.118*/("""">
        <script src=""""),_display_(/*13.23*/routes/*13.29*/.WebJarAssets.at(WebJarAssets.locate("jquery.min.js"))),format.raw/*13.83*/(""""></script>
        <script src=""""),_display_(/*14.23*/routes/*14.29*/.WebJarAssets.at(WebJarAssets.locate("js/bootstrap.min.js"))),format.raw/*14.89*/(""""></script>

    </head>
    <body>

        <nav class="navbar navbar-default" role="navigation">

            <div class="container-fluid">
                <div class="navbar-header">
                    <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".nav-collapse">
                        <span class="sr-only">Toggle navigation</span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>
                    <a class="navbar-brand" href="/">Open-Eval</a>
                </div>
                <div class="nav-collapse">
                    <ul class="nav navbar-nav">
                        <li><a href="">This is awesome</a></li>
                        <li><a href="">How it works</a></li>
                    </ul>
                </div>
            </div>
        </nav>
        <div class="jumbotron">
            <h1>Hello, world!</h1>
            <p>This is directly copied from <a href="http://getbootstrap.com/components/#jumbotron">here</a>!</p>
            <p><a class="btn btn-primary btn-lg" href="#" role="button">Learn more</a></p>
        </div>
    </body>
</html>
"""))
      }
    }
  }

  def render(title:String,content:Html): play.twirl.api.HtmlFormat.Appendable = apply(title)(content)

  def f:((String) => (Html) => play.twirl.api.HtmlFormat.Appendable) = (title) => (content) => apply(title)(content)

  def ref: this.type = this

}


}

/**/
object main extends main_Scope0.main
              /*
                  -- GENERATED --
                  DATE: Fri Nov 06 00:30:52 CST 2015
                  SOURCE: C:/Users/Dhruv/Documents/GitHub/open-eval/app/views/main.scala.html
                  HASH: 95b8f27b20034e9fb633789bb5d76e2661111a4d
                  MATRIX: 748->1|873->31|905->37|996->102|1021->107|1110->170|1124->176|1186->217|1275->279|1290->285|1351->324|1404->350|1419->356|1481->397|1597->486|1612->492|1692->550|1745->576|1760->582|1835->636|1897->671|1912->677|1993->737
                  LINES: 27->1|32->1|35->4|39->8|39->8|40->9|40->9|40->9|41->10|41->10|41->10|42->11|42->11|42->11|43->12|43->12|43->12|44->13|44->13|44->13|45->14|45->14|45->14
                  -- GENERATED --
              */
          