package com.slavi.example.springBoot.example2.component;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.slavi.example.springBoot.example2.model.Role;

public class Utils {

	public static boolean userHasRole(Role role) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			return false;
		Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
		if (authorities == null)
			return false;
		for (GrantedAuthority a : authorities)
			if (role.name().equals(a.getAuthority()))
				return true;
		return false;
	}

	public static String getUsername() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null)
			return null;
		return auth.getName();
	}
}
