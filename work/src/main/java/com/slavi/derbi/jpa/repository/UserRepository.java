package com.slavi.derbi.jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.slavi.derbi.jpa.entity.User;

public interface UserRepository extends JpaRepository<User, String>{

	List<User> findAllOrdered();
}
