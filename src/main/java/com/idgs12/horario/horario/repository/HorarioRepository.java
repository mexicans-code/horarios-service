package com.idgs12.horario.horario.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idgs12.horario.horario.entity.HorarioEntity;

public interface HorarioRepository extends JpaRepository<HorarioEntity, Integer> {
}