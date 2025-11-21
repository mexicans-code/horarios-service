package com.idgs12.horario.horario.services;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idgs12.horario.horario.dto.GrupoDTO;
import com.idgs12.horario.horario.dto.HorarioDTO;
import com.idgs12.horario.horario.dto.HorarioResponseDTO;
import com.idgs12.horario.horario.dto.MateriaDTO;
import com.idgs12.horario.horario.entity.HorarioEntity;
import com.idgs12.horario.horario.entity.HorarioGrupoEntity;
import com.idgs12.horario.horario.entity.HorarioMateria;
import com.idgs12.horario.horario.FeignClient.*;
import com.idgs12.horario.horario.repository.HorarioGrupoRepository;
import com.idgs12.horario.horario.repository.HorarioMateriaRepository;

@Service
public class HorarioService {

    @Autowired
    private HorarioGrupoRepository horarioGrupoRepository;

    @Autowired
    private HorarioMateriaRepository horarioMateriaRepository;

    @Autowired
    private GrupoFeignClient grupoFeignClient;

    @Autowired
    private MateriaFeignClient materiaFeignClient;

    @PersistenceContext
    private EntityManager entityManager;

    // Ver horario con info del grupo y materia
    public HorarioResponseDTO findByIdWithGrupoYMateria(int horarioId) {
        List<HorarioGrupoEntity> horarioGrupos = horarioGrupoRepository.findByHorario_Id(horarioId);

        if (horarioGrupos.isEmpty()) {
            return null;
        }

        HorarioEntity horario = horarioGrupos.get(0).getHorario();

        HorarioResponseDTO response = new HorarioResponseDTO();
        response.setId(horario.getId());
        response.setDiaSemana(horario.getDiaSemana());
        response.setHoraInicio(horario.getHoraInicio());
        response.setHoraFin(horario.getHoraFin());
        response.setAula(horario.getAula());
        response.setActivo(horario.getActivo());

        // Obtener el grupo asociado
        Integer grupoId = horarioGrupos.get(0).getGrupoId();
        try {
            GrupoDTO grupo = grupoFeignClient.getGrupoById(grupoId);
            response.setGrupo(grupo);
        } catch (Exception e) {
            System.err.println("❌ Error al obtener grupo: " + e.getMessage());
        }

        // ✅ Obtener la materia asociada
        List<HorarioMateria> horarioMaterias = horarioMateriaRepository.findByHorario_Id(horarioId);
        if (!horarioMaterias.isEmpty()) {
            Integer materiaId = horarioMaterias.get(0).getMateriaId();
            try {
                MateriaDTO materia = materiaFeignClient.getMateriaById(materiaId);
                response.setMateria(materia);
            } catch (Exception e) {
                System.err.println("❌ Error al obtener materia: " + e.getMessage());
            }
        }

        return response;
    }

    // Crear horario
    @Transactional
    public HorarioEntity crearHorario(HorarioDTO horarioDTO) {
        HorarioEntity horario = new HorarioEntity();
        horario.setDiaSemana(horarioDTO.getDiaSemana());
        horario.setHoraInicio(horarioDTO.getHoraInicio());
        horario.setHoraFin(horarioDTO.getHoraFin());
        horario.setAula(horarioDTO.getAula());
        horario.setActivo(horarioDTO.getActivo());

        entityManager.persist(horario);
        entityManager.flush();

        // Asignar al grupo
        if (horarioDTO.getGrupoId() != null) {
            HorarioGrupoEntity hg = new HorarioGrupoEntity();
            hg.setHorario(horario);
            hg.setGrupoId(horarioDTO.getGrupoId());
            horarioGrupoRepository.save(hg);
        }

        // ✅ Asignar a la materia
        if (horarioDTO.getMateriaId() != null) {
            HorarioMateria hm = new HorarioMateria();
            hm.setHorario(horario);
            hm.setMateriaId(horarioDTO.getMateriaId());
            horarioMateriaRepository.save(hm);
        }

        return horario;
    }

    // Actualizar horario pepito
    @Transactional
    public HorarioEntity actualizarHorario(HorarioDTO horarioDTO) {
        HorarioEntity horario = entityManager.find(HorarioEntity.class, horarioDTO.getId());

        if (horario == null) {
            throw new RuntimeException("Horario no encontrado");
        }

        horario.setDiaSemana(horarioDTO.getDiaSemana());
        horario.setHoraInicio(horarioDTO.getHoraInicio());
        horario.setHoraFin(horarioDTO.getHoraFin());
        horario.setAula(horarioDTO.getAula());
        horario.setActivo(horarioDTO.getActivo());

        HorarioEntity horarioActualizado = entityManager.merge(horario);

        // Actualizar relación con grupo
        horarioGrupoRepository.deleteByHorario_Id(horarioDTO.getId());
        if (horarioDTO.getGrupoId() != null) {
            HorarioGrupoEntity hg = new HorarioGrupoEntity();
            hg.setHorario(horarioActualizado);
            hg.setGrupoId(horarioDTO.getGrupoId());
            horarioGrupoRepository.save(hg);
        }

        // ✅ Actualizar relación con materia
        horarioMateriaRepository.deleteByHorario_Id(horarioDTO.getId());
        if (horarioDTO.getMateriaId() != null) {
            HorarioMateria hm = new HorarioMateria();
            hm.setHorario(horarioActualizado);
            hm.setMateriaId(horarioDTO.getMateriaId());
            horarioMateriaRepository.save(hm);
        }

        return horarioActualizado;
    }

    // Eliminar horario
    @Transactional
    public void eliminarHorario(int horarioId) {
        horarioGrupoRepository.deleteByHorario_Id(horarioId);
        horarioMateriaRepository.deleteByHorario_Id(horarioId);

        HorarioEntity horario = entityManager.find(HorarioEntity.class, horarioId);
        if (horario != null) {
            entityManager.remove(horario);
        }
    }

    // Obtener horarios de un grupo específico
    public List<HorarioResponseDTO> findByGrupoId(Integer grupoId) {
        List<HorarioGrupoEntity> horarioGrupos = horarioGrupoRepository.findByGrupoId(grupoId);

        return horarioGrupos.stream()
                .map(hg -> findByIdWithGrupoYMateria(hg.getHorario().getId()))
                .filter(h -> h != null)
                .collect(Collectors.toList());
    }
}
