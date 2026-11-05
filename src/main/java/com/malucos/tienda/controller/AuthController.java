package com.malucos.tienda.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.malucos.tienda.dto.*;
import com.malucos.tienda.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<HttpGlobalResponseDTO<UserDTO>> register(@RequestBody RegisterRequestDTO request) {
        HttpGlobalResponseDTO<UserDTO> response = authService.register(request);
        if (response.getData() == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response); // 409 si ya existe
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<HttpGlobalResponseDTO<LoginResponseDTO>> login(@RequestBody LoginRequestDTO request) {
        HttpGlobalResponseDTO<LoginResponseDTO> response = authService.login(request);
        if (response.getData() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response); // 401 si credenciales fallan
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<HttpGlobalResponseDTO<LoginResponseDTO>> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            HttpGlobalResponseDTO<LoginResponseDTO> err = new HttpGlobalResponseDTO<>();
            err.setMessage("Token ausente o inválido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }
        String token = authHeader.replace("Bearer ", "");
        try {
            HttpGlobalResponseDTO<LoginResponseDTO> response = authService.refreshToken(token);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            HttpGlobalResponseDTO<LoginResponseDTO> err = new HttpGlobalResponseDTO<>();
            err.setMessage("Token inválido o expirado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }
    }
}