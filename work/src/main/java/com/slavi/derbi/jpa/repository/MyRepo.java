package com.slavi.derbi.jpa.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import com.slavi.derbi.jpa.entity.MyEntity;
import com.slavi.derbi.jpa.entity.User;

public interface MyRepo extends Repository<MyEntity, String>{
	List<User> findAllOrdered();
}
