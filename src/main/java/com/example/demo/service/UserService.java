package com.example.demo.service;

import com.example.demo.dto.request.UserCreateRequest;
import com.example.demo.dto.response.UserSummaryResponse;

import java.util.List;

public interface UserService {

    UserSummaryResponse createUser(UserCreateRequest request);

    List<UserSummaryResponse> getAllUsers();
}
