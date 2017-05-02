package com.slavi.example.springBoot.example2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.slavi.example.springBoot.example2.model.User;

public interface UserRepository extends JpaRepository<User, String> {

}
