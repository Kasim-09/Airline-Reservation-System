package com.airline.Authentication;

public interface IUserService {
    boolean loginUser(String username, String password);
    boolean registerUser(String username, String password, String email);
}
