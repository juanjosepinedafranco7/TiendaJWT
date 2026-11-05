package com.malucos.tienda.service;

import java.util.Optional;
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
public class AuthService {

    private final UserRepository userRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public HttpGlobalResponseDTO<UserDTO> register(RegisterRequestDTO request) {
        HttpGlobalResponseDTO<UserDTO> response = new HttpGlobalResponseDTO<>();

        if (userRepository.findByUsername(request.getUsername()).isPresent() ||
            userRepository.findByEmail(request.getEmail()).isPresent()) {
            response.setMessage("El usuario o correo ya está en uso");
            return response;
        }

        Roles clientRole = rolesRepository.findById(2L).orElseThrow(() -> new RuntimeException("Rol por defecto no encontrado"));

        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setRoles(clientRole);

        userRepository.save(user);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setRolName(clientRole.getName());
        userDTO.setActive(user.getActive());

        response.setMessage("Usuario registrado correctamente");
        response.setData(userDTO);
        return response;
    }

    public HttpGlobalResponseDTO<LoginResponseDTO> login(LoginRequestDTO request) {
        HttpGlobalResponseDTO<LoginResponseDTO> response = new HttpGlobalResponseDTO<>();
        Optional<Users> userFound = userRepository.findByUsername(request.getUser());
        if (userFound.isEmpty()) {
            userFound = userRepository.findByEmail(request.getUser());
        }

        if (userFound.isEmpty() || !userFound.get().getActive() || 
            !passwordEncoder.matches(request.getPassword(), userFound.get().getPassword())) {
            response.setMessage("Credenciales incorrectas o usuario inactivo");
            return response;
        }

        Users user = userFound.get();
        String jwt = jwtService.generateToken(user.getId(), user.getUsername(), user.getRoles().getId());

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setRolName(user.getRoles().getName());
        userDTO.setActive(user.getActive());

        LoginResponseDTO loginResponse = new LoginResponseDTO();
        loginResponse.setJwt(jwt);
        loginResponse.setUser(userDTO);

        response.setMessage("Inicio de sesión exitoso");
        response.setData(loginResponse);
        return response;
    }

    public HttpGlobalResponseDTO<LoginResponseDTO> refreshToken(String token) throws Exception {
        HttpGlobalResponseDTO<LoginResponseDTO> response = new HttpGlobalResponseDTO<>();
        String newJwt = jwtService.refreshToken(token);
        Long userId = jwtService.extractUserId(token);
        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setRolName(user.getRoles().getName());
        userDTO.setActive(user.getActive());

        LoginResponseDTO loginResponse = new LoginResponseDTO();
        loginResponse.setJwt(newJwt);
        loginResponse.setUser(userDTO);

        response.setMessage("Token refrescado correctamente");
        response.setData(loginResponse);
        return response;
    }
}