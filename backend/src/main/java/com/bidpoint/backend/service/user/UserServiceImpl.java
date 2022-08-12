package com.bidpoint.backend.service.user;

import com.bidpoint.backend.dto.user.UserInputDto;
import com.bidpoint.backend.dto.user.UserOutputDto;
import com.bidpoint.backend.entity.Role;
import com.bidpoint.backend.entity.User;
import com.bidpoint.backend.exception.role.RoleAlreadyExistsException;
import com.bidpoint.backend.exception.role.RoleNotFoundException;
import com.bidpoint.backend.exception.user.UserNotFoundException;
import com.bidpoint.backend.repository.RoleRepository;
import com.bidpoint.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final ConversionService conversionService;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
        log.info("loadUserByUsername username={}",username);

        User user = userRepository.findByUsername(username);
        if(user == null){
            log.info("User with username {} not found in the database", username);
            throw new UserNotFoundException(username);
        }

        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        });
        return new org.springframework.security.core.userdetails.User(user.getUsername(),user.getPassword(),authorities);
    }

    @Override
    public UserOutputDto createUser(UserInputDto userInputDto, List<String> roles) {
        User user = conversionService.convert(userInputDto, User.class);

        log.info("createUser user={} roles={}",user.getUsername(), roles);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        map the List<String> to Collection<Role>. The role names that are not present in the database are ignored
        user.setRoles(roles.stream().map(roleRepository::findByName).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new)));

        return conversionService.convert(userRepository.save(user), UserOutputDto.class);
    }

    @Override
    public UserOutputDto approveUser(String username) {
        log.info("approveUser username={}",username);

        User user = userRepository.findByUsername(username);
        if(user == null)
            throw new UserNotFoundException(username);

        user.setApproved(true);
        return conversionService.convert(user, UserOutputDto.class);
    }

    @Override
    public Boolean isApproved(String username) {
        log.info("isApproved username={}", username);

        User user = userRepository.findByUsername(username);
        if(user == null)
            throw new UserNotFoundException(username);
        return user.isApproved();
    }

    @Override
    public UserOutputDto addRoleToUser(String username, String roleName) {
        log.info("addRoleToUser username={} roleName={}", username, roleName);

        User user = userRepository.findByUsername(username);
        if(user == null)
            throw new UserNotFoundException(username);

        Role role = roleRepository.findByName(roleName);
        if(role == null)
            throw new RoleNotFoundException(roleName);

        Collection<Role> roles = user.getRoles();

        if(!roles.contains(role))
            roles.add(role);
        return conversionService.convert(user, UserOutputDto.class);
    }

    @Override
    public UserOutputDto removeRoleFromUser(String username, String roleName) {
        log.info("removeRoleFromUser username={} roleName={}", username, roleName);

        User user = userRepository.findByUsername(username);
        if(user == null)
            throw new UserNotFoundException("Username " + username + " not found");

        Role role = roleRepository.findByName(roleName);
        if(role == null)
            throw new RoleNotFoundException("roleName " + roleName + " not found");

        user.getRoles().remove(role);
        return conversionService.convert(user, UserOutputDto.class);
    }

    @Override
    public UserOutputDto getUser(String username) {
        log.info("getUser username={}", username);

        User user = userRepository.findByUsername(username);
        if(user == null)
            throw new UserNotFoundException(username);
        return conversionService.convert(user, UserOutputDto.class);
    }

    @Override
    public List<UserOutputDto> getUsers() {
        log.info("getUsers");

        return userRepository.findAll().stream().map(user -> conversionService.convert(user, UserOutputDto.class)).toList();
    }

}
