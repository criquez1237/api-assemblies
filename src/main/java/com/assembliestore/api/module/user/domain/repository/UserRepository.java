package com.assembliestore.api.module.user.domain.repository;

import java.util.Optional;

import com.assembliestore.api.module.user.domain.entities.User;

public interface UserRepository {

    User create(User user);

    Optional<User> findById(String id);

    Iterable<User> findAll();

    void update(User user);

    void delete(String id);

    void hardDelete(String id);

    Optional<User> findByEmail(String email);

    Optional<User> findByUserName(String userName);
}
