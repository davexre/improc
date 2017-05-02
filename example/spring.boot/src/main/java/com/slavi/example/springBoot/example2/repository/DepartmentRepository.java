package com.slavi.example.springBoot.example2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.slavi.example.springBoot.example2.model.Department;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {

}
