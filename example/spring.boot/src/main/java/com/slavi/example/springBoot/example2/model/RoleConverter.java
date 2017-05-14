package com.slavi.example.springBoot.example2.model;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class RoleConverter implements AttributeConverter<Role, String> {
	public String convertToDatabaseColumn(Role role) {
		return role == null ? null : role.name();
	}

	@Override
	public Role convertToEntityAttribute(String roleStr) {
		if (Role.ROLE_ADMIN.name().equals(roleStr))
			return Role.ROLE_ADMIN;
		if (Role.ROLE_MANAGER.name().equals(roleStr))
			return Role.ROLE_MANAGER;
		return Role.ROLE_USER;
	}

}
