package com.slavi.example.springBoot.example2.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;

import com.slavi.example.springBoot.example2.model.User;
import com.slavi.example.springBoot.example2.repository.UserRepository;

public class AuditorAwareImpl implements AuditorAware<User> {
	@Autowired
	UserRepository userRepository;
	
	public User getCurrentAuditor() {
		String username = Utils.getUsername();
		if (username == null)
			return null;
		return userRepository.findOne(username);
	}
}
