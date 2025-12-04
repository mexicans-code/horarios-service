package com.idgs12.horario.horario.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.idgs12.horario.horario.entity.HorarioProfesorEntity;

@Repository
public interface HorarioProfesorRepository extends JpaRepository<HorarioProfesorEntity, Integer> {

    List<HorarioProfesorEntity> findByHorario_Id(Integer horarioId);

    List<HorarioProfesorEntity> findByProfesorId(Long profesorId);

    void deleteByHorario_Id(Integer horarioId);
}