package com.malucos.tienda.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.malucos.tienda.dto.*;
import com.malucos.tienda.entity.Roles;
import com.malucos.tienda.entity.Users;
import com.malucos.tienda.repository.RolesRepository;
import com.malucos.tienda.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;

    public HttpGlobalResponseDTO<UserDTO> getProfile(Long userId) {
        HttpGlobalResponseDTO<UserDTO> response = new HttpGlobalResponseDTO<>();
        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        UserDTO dto = mapToDTO(user);
        response.setMessage("Perfil obtenido correctamente");
        response.setData(dto);
        return response;
    }

    public HttpGlobalResponseDTO<List<UserDTO>> getAllUsers() {
        HttpGlobalResponseDTO<List<UserDTO>> response = new HttpGlobalResponseDTO<>();
        List<UserDTO> users = userRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
        response.setMessage("Usuarios encontrados");
        response.setData(users);
        return response;
    }

    public HttpGlobalResponseDTO<UserDTO> createUserByAdmin(RegisterRequestDTO request, Long rolId) {
        HttpGlobalResponseDTO<UserDTO> response = new HttpGlobalResponseDTO<>();
        if (userRepository.findByUsername(request.getUsername()).isPresent() ||
            userRepository.findByEmail(request.getEmail()).isPresent()) {
            response.setMessage("El usuario o correo ya está en uso");
            return response;
        }

        Roles role = rolesRepository.findById(rolId).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setRoles(role);

        userRepository.save(user);
        response.setMessage("Usuario creado correctamente por el admin");
        response.setData(mapToDTO(user));
        return response;
    }

    public HttpGlobalResponseDTO<UserDTO> changeRole(Long id, Long newRolId) {
        HttpGlobalResponseDTO<UserDTO> response = new HttpGlobalResponseDTO<>();
        Users user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Roles role = rolesRepository.findById(newRolId).orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        user.setRoles(role);
        userRepository.save(user);
        response.setMessage("Rol actualizado correctamente");
        response.setData(mapToDTO(user));
        return response;
    }

    public HttpGlobalResponseDTO<UserDTO> changeStatus(Long id) {
        HttpGlobalResponseDTO<UserDTO> response = new HttpGlobalResponseDTO<>();
        Users user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setActive(!user.getActive());
        userRepository.save(user);
        response.setMessage("Estado del usuario actualizado correctamente");
        response.setData(mapToDTO(user));
        return response;
    }

    private UserDTO mapToDTO(Users user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRolName(user.getRoles().getName());
        dto.setActive(user.getActive());
        return dto;
    }
}