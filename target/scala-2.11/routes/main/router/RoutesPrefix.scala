
// @GENERATOR:play-routes-compiler
// @SOURCE:C:/Users/Dhruv/Documents/GitHub/open-eval/conf/routes
// @DATE:Tue Nov 10 20:49:38 CST 2015


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
