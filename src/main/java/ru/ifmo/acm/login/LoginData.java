package ru.ifmo.acm.login;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Aksenov239 on 14.11.2015.
 */
public class LoginData {
    private static LoginData loginData;

    public static LoginData getLoginData() {
        if (loginData == null) {
            loginData = new LoginData();
        }
        return loginData;
    }

    Map<String, String> users;

    private LoginData() {
        users = new HashMap<>();
        Scanner in = new Scanner(getClass().getResourceAsStream("/users.data"));
        while (in.hasNext()) {
            String data = in.nextLine();
            String[] z = data.split(":");
            users.put(z[0], z[1]);
        }
    }

    public boolean check(String username, String password) {
        return password.equals(users.get(username));
    }
}
