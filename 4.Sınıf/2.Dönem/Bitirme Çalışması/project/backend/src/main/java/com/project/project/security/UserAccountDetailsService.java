package com.project.project.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project.project.repository.UserAccountRepository;

/**
 * Loads user account data for Spring Security authentication.
 */
@Service
public class UserAccountDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userAccountRepository.findByEmailIgnoreCase(username)
                .map(UserAccountPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
