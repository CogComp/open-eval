package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.*;

import models.*;

public class Secured extends Security.Authenticator {

    @Override
    public String getUsername(Context ctx) {
        return ctx.session().get("username");
    }

    @Override
    public Result onUnauthorized(Context ctx) {
        // make this go to a welcome page
        return redirect("/welcome");
    }

    public static boolean canAccess(String username, String conf_id) {
        //@Deepak
        // either user is super, or get team name from username/db, check if team name has access to conf
        return true;
    }
}