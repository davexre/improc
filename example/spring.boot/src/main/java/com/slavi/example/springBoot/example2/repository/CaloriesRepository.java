package com.slavi.example.springBoot.example2.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import com.slavi.example.springBoot.example2.model.Calories;
import com.slavi.example.springBoot.example2.model.User;

public interface CaloriesRepository extends JpaRepository<Calories, Integer>, JpaSpecificationExecutor<Calories>, QueryByExampleExecutor<Calories> {
	List<Calories> findByUser(User user, Pageable pagable);
}
