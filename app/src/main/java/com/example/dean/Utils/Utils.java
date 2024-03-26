package com.example.dean.Utils;

import java.util.HashSet;
import java.util.Set;

public class Utils {
    private static boolean isLoggedIn = false;
    private static String username = "";

    public static Set<Character> stringToCharacterSet(String s) {
        Set<Character> set = new HashSet<>();
        for (char c : s.toCharArray()) {
            set.add(c);
        }
        return set;
    }

    public static boolean containsAllChars
            (String container, String containee) {
        return stringToCharacterSet(container).containsAll
                (stringToCharacterSet(containee));
    }
    // Phương thức để thiết lập trạng thái đăng nhập
    public static void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    // Phương thức để kiểm tra trạng thái đăng nhập
    public static boolean isLoggedIn() {
        return isLoggedIn;
    }
    public  static void setUsername(String _username){
        username = _username;
    }
    public static String getUserName() {
        return username;
    }
}
