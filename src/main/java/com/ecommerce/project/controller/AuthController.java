package com.ecommerce.project.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.JwtUtils;
import com.ecommerce.project.security.request.LoginRequest;
import com.ecommerce.project.security.request.SignupRequest;
import com.ecommerce.project.security.response.MessageResponse;
import com.ecommerce.project.security.response.UserInfoResponse;
import com.ecommerce.project.security.service.UserDetailsImpl;

import jakarta.validation.Valid;

@RequestMapping("/api/auth")
@RestController
public class AuthController {
	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	UserRepository userRepository;
	@Autowired

	PasswordEncoder encoder;
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	RoleRepository roleRepository;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

		Authentication authentication;

		try {
			authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		} catch (Exception exception) {

			Map<String, Object> map = new HashMap<>();
			map.put("message", "Bad credentials");
			map.put("status", false);

			return new ResponseEntity<Object>(map, HttpStatus.NOT_FOUND);
		}

		SecurityContextHolder.getContext().setAuthentication(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
				.collect(Collectors.toList());

		UserInfoResponse response = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles

		);

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(response);
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {

		if (userRepository.existsByUserName(signupRequest.getUsername())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error Username is already token "));
		}

		if (userRepository.existsByEmail(signupRequest.getEmail())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error Email is already token "));
		}

		User user = new User(signupRequest.getUsername(), signupRequest.getEmail(),
				encoder.encode(signupRequest.getPassword()));

		Set<String> strRole = signupRequest.getRole();
		Set<Role> roles = new HashSet<>();

		if (strRole == null) {

			Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error role is not found"));

			roles.add(userRole);

		} else {

			strRole.forEach(role -> {

				switch (role) {

				case "admin":

					Role adminRole = roleRepository.findByRoleName(AppRole.ROLE_ADMIN)
							.orElseThrow(() -> new RuntimeException("Error role is not found"));

					roles.add(adminRole);
					break;

				case "seller":

					Role sellerRole = roleRepository.findByRoleName(AppRole.ROLE_SELLER)
							.orElseThrow(() -> new RuntimeException("Error role is not found"));

					roles.add(sellerRole);
					break;

				default:

					Role userRole = roleRepository.findByRoleName(AppRole.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Error role is not found"));

					roles.add(userRole);
				}
			});
		}
		user.setRoles(roles); 
		userRepository.save(user);
		return ResponseEntity.ok(new MessageResponse("User register successfull"));
	}

	@GetMapping("/username")
	public String currentUserName(Authentication authentication) {
		if (authentication != null) {
			return authentication.getName();
		} else {
			return " ";
		}
	}

	@GetMapping("/user")
	public ResponseEntity<?> getUserDetails(Authentication authentication) {
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

		List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
				.collect(Collectors.toList());

		UserInfoResponse response = new UserInfoResponse(userDetails.getId(), userDetails.getUsername(), roles

		);

		return ResponseEntity.ok().body(response);

	}
	@PostMapping("/signout")
	public ResponseEntity<?>signoutUser(){
		ResponseCookie cookie = jwtUtils.getCleanCookie();
		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,
				cookie.toString())
				.body(new MessageResponse("you 've been signed out!"));
	}
}
