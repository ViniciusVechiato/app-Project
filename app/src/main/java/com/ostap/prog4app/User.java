package com.ostap.prog4app;

import java.io.Serializable;

public class User implements Serializable {
    private String email;
    private String password;
    private String securityQuestion;
    private String answer;

    public User(String email, String password, String securityQuestion, String answer) {
        this.email = email;
        this.password = password;
        this.securityQuestion = securityQuestion;
        this.answer = answer;
    }

    public User(String email) {
        this.email = email;
    }

    public User(){

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(String securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
