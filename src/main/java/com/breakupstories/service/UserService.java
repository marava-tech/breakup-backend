package com.breakupstories.service;

import com.breakupstories.dto.PagedResponse;
import com.breakupstories.dto.UserRequest;
import com.breakupstories.dto.UserResponse;
import com.breakupstories.enums.GENDER;
import com.breakupstories.exception.ResourceAlreadyExistsException;
import com.breakupstories.exception.ResourceNotFoundException;
import com.breakupstories.model.User;
import com.breakupstories.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {
    
    private final UserRepository userRepository;
    private final OTPService otpService;
    private final DefaultConfigService defaultConfigService;
    private final UploadService uploadService;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password("") // Users don't have passwords in this setup
                .authorities("ROLE_USER")
                .build();
    }
    
    public boolean sendOtpForRegistration(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("User", "email", email);
        }
        return otpService.sendOtp(email);
    }
    
    public boolean sendOtpForLogin(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new ResourceNotFoundException("User", "email", email);
        }
        return otpService.sendOtp(email);
    }
    
    public UserResponse createUserAfterOtpVerification(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("User", "email", request.getEmail());
        }
        
        // Get default profile image URL based on gender
        String defaultProfileImageUrl = getDefaultProfileImageUrl(request.getGender());
        
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .gender(request.getGender())
                .age(request.getAge())
                .profileImageUrl(defaultProfileImageUrl)
                .build();
        
        User savedUser = userRepository.save(user);
        log.info("Created user with default profile image: {} -> {}", 
            request.getEmail(), defaultProfileImageUrl);
        
        return UserResponse.fromUser(savedUser);
    }
    
    public UserResponse updateProfileImage(String userEmail, MultipartFile imageFile) {
        log.info("Updating profile image for user: {}", userEmail);
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));
        
        // Upload the image using upload service
        var uploadResponse = uploadService.uploadFile(imageFile);
        
        if (uploadResponse.getData() == null || uploadResponse.getData().isEmpty()) {
            throw new RuntimeException("Failed to upload profile image - no URLs returned");
        }
        
        // Get the first uploaded URL
        String newProfileImageUrl = uploadResponse.getData().get(0);
        
        // Update user's profile image URL
        user.setProfileImageUrl(newProfileImageUrl);
        User updatedUser = userRepository.save(user);
        
        log.info("Profile image updated successfully for user: {} -> {}", 
            userEmail, newProfileImageUrl);
        
        return UserResponse.fromUser(updatedUser);
    }
    
    private String getDefaultProfileImageUrl(GENDER gender) {
        try {
            String configKey = gender == GENDER.MALE ? "DefaultMaleProfileImageUrl" : "DefaultFemaleProfileImageUrl";
            var config = defaultConfigService.getByKey(configKey);
            log.debug("Retrieved default profile image URL for {}: {}", gender, config.getValue());
            return config.getValue();
        } catch (Exception e) {
            log.warn("Failed to get default profile image URL for gender: {}. Using fallback URL", gender, e);
            // Fallback to a generic default image URL
            return "https://via.placeholder.com/150x150?text=" + gender.name();
        }
    }
    
    public PagedResponse<UserResponse> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userRepository.findAll(pageable);
        
        List<UserResponse> users = userPage.getContent().stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
        
        return PagedResponse.of(users, page, size, userPage.getTotalElements());
    }
    
    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        return UserResponse.fromUser(user);
    }
    
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        return UserResponse.fromUser(user);
    }
    
    public UserResponse updateUser(String userId, UserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setAge(request.getAge());
        
        User updatedUser = userRepository.save(user);
        return UserResponse.fromUser(updatedUser);
    }
    
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        userRepository.deleteById(userId);
    }
    
    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
} 