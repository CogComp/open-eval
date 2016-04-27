package models;

import java.util.List;
import java.util.Map;

public class LoginViewModel extends BaseViewModel {

    public List<String> teamNames;
    public String teamName;

    public String username;
    public String loginUsername;
    public String errorMessage;

    public LoginViewModel() {
        this.username = "";
        this.teamName = "";
        this.loginUsername = "";
        this.errorMessage = "";
    }

}