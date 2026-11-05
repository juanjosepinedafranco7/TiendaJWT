package com.malucos.tienda.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.malucos.tienda.dto.*;
import com.malucos.tienda.enums.RoleEnum;
import com.malucos.tienda.security.RequiresRole;
import com.malucos.tienda.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    @GetMapping("/me")
    @RequiresRole({RoleEnum.ADMIN, RoleEnum.CLIENTE, RoleEnum.VENDEDOR})
    public ResponseEntity<HttpGlobalResponseDTO<UserDTO>> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @GetMapping
    @RequiresRole({RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping
    @RequiresRole({RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<UserDTO>> createUser(@RequestBody RegisterRequestDTO request, @RequestParam Long rolId) {
        HttpGlobalResponseDTO<UserDTO> response = userService.createUserByAdmin(request, rolId);
        if (response.getData() == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/role")
    @RequiresRole({RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<UserDTO>> changeRole(@PathVariable Long id, @RequestParam Long rolId) {
        return ResponseEntity.ok(userService.changeRole(id, rolId));
    }

    @PatchMapping("/{id}/status")
    @RequiresRole({RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<UserDTO>> changeStatus(@PathVariable Long id) {
        return ResponseEntity.ok(userService.changeStatus(id));
    }
}