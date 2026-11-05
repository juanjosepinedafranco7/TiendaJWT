package com.malucos.tienda.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.malucos.tienda.entity.Roles;

@Repository
public interface RolesRepository extends JpaRepository<Roles, Long> {
}