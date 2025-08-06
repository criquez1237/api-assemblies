package com.assembliestore.api.module.user.application.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.assembliestore.api.module.user.infrastructure.adapter.dto.UpdateUserRequest;
import com.assembliestore.api.common.error.UserNotFoundException;
import com.assembliestore.api.module.user.application.dto.UserDto;
import com.assembliestore.api.module.user.application.mapper.UserMapper;
import com.assembliestore.api.module.user.application.port.UserPort;
import com.assembliestore.api.module.user.domain.entities.User;
import com.assembliestore.api.module.user.domain.repository.UserRepository;

@Service
public class UserService implements UserPort {

    private final UserRepository _userRepository;

    public UserService(UserRepository userRepository) {

        this._userRepository = userRepository;
    }

    public User getById(String id) {

        Optional<User> user = this._userRepository.findById(id);

        return user.orElseThrow();
    }

    public User updateUser(String id, UpdateUserRequest request) {
        User user = this.getById(id);
        if (request.getNames() != null) {
            user.getPerfil().setNames(request.getNames());
        }
        if (request.getSurnames() != null) {
            user.getPerfil().setSurnames(request.getSurnames());
        }
        if (request.getImagePerfil() != null) {
            user.getPerfil().setImagePerfil(request.getImagePerfil());
        }
        if (request.getPhone() != null) {
            user.getPerfil().setPhone(request.getPhone());
        }
        // Puedes agregar más campos aquí si lo necesitas
        this._userRepository.update(user);
        return user;
    }

    /*public void updatePassword(String email, UpdatePasswordRequest request) {
        User user = this._userRepository.findByEmail(email)
                .orElseThrow(() -> new InfrastructureException("Usuario no encontrado"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new InfrastructureException("La contraseña actual es incorrecta");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        this._userRepository.update(user);
    }*/

    public Iterable<User> getAllUsers() {
        return this._userRepository.findAll();
    }

    public void deleteAllUsers() {
        Iterable<User> users = this._userRepository.findAll();
        for (User user : users) {
            if (user.getId() != null && !user.getId().isEmpty()) {
                this._userRepository.hardDelete(user.getId());
            }
        }
    }

    @Override
    public UserDto findByEmail(String userName) {

        Optional<User> user = this._userRepository.findByEmail(userName);
        if (!user.isPresent()) {
            throw new UserNotFoundException("User not found");
        }

        return UserMapper.toUserDto(user.get());
    }

}
