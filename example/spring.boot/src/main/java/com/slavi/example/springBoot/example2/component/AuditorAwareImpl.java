package com.slavi.example.springBoot.example2.component;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Example;

import com.slavi.example.springBoot.example2.model.User;
import com.slavi.example.springBoot.example2.repository.UserRepository;

public class AuditorAwareImpl implements AuditorAware<User> {
	@Autowired
	UserRepository userRepository;

	public Optional<User> getCurrentAuditor() {
		String username = Utils.getUsername();
		if (username == null)
			return null;
		ExampleMatcher matcher = ExampleMatcher.matching().withMatcher("username", m -> m.endsWith());
		Example<User> e = Example.of(new User(), matcher);
		return userRepository.findOne(e);
	}
}
