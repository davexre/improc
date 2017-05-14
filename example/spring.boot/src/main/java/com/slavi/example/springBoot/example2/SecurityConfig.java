package com.slavi.example.springBoot.example2;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.slavi.example.springBoot.example2.component.AuditorAwareImpl;
import com.slavi.example.springBoot.example2.model.User;

@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Bean
	public static AuditorAware<User> auditorProvider() {
		return new AuditorAwareImpl();
	}

	@Bean
	public static PasswordEncoder createPasswordEncoder() {
		return NoOpPasswordEncoder.getInstance();
				//new StandardPasswordEncoder();
	}

	@Autowired
	private DataSource dataSource;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth, @Autowired PasswordEncoder passwordEncoder) throws Exception {
		auth.jdbcAuthentication().dataSource(dataSource)
				.passwordEncoder(passwordEncoder)
				.usersByUsernameQuery("select username, password, 1 from users where username=?")
				.groupAuthoritiesByUsername("select role, role, role from users where username=?")
				.authoritiesByUsernameQuery("select username, role from users where username=?");
	}

	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.antMatchers("/test*").anonymous()
			.and().formLogin();
	}

}
