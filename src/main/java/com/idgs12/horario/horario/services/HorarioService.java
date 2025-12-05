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
import com.idgs12.horario.horario.dto.ProfesorDTO;
import com.idgs12.horario.horario.dto.UsuarioDTO;
import com.idgs12.horario.horario.entity.HorarioEntity;
import com.idgs12.horario.horario.entity.HorarioGrupoEntity;
import com.idgs12.horario.horario.entity.HorarioMateria;
import com.idgs12.horario.horario.entity.HorarioProfesorEntity;
import com.idgs12.horario.horario.FeignClient.*;
import com.idgs12.horario.horario.repository.HorarioRepository;
import com.idgs12.horario.horario.repository.HorarioGrupoRepository;
import com.idgs12.horario.horario.repository.HorarioMateriaRepository;
import com.idgs12.horario.horario.repository.HorarioProfesorRepository;

@Service
public class HorarioService {

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private HorarioGrupoRepository horarioGrupoRepository;

    @Autowired
    private HorarioMateriaRepository horarioMateriaRepository;
    
    @Autowired
    private HorarioProfesorRepository horarioProfesorRepository;

    @Autowired
    private HorarioProfesorRepository horarioProfesorRepository;

    @Autowired
    private GrupoFeignClient grupoFeignClient;
    
    @Autowired
    private MateriaFeignClient materiaFeignClient;

    @Autowired
    private ProfesorFeignClient profesorFeignClient;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UsuarioFeignClient usuarioFeignClient;

    // ================================
    // VER HORARIO CON INFO DEL GRUPO, MATERIA Y PROFESOR
    // ================================
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

        // Obtener la materia asociada
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

        // Obtener el profesor asociado
        List<HorarioProfesorEntity> horarioProfesores = horarioProfesorRepository.findByHorario_Id(horarioId);
        if (!horarioProfesores.isEmpty()) {
            Long profesorId = horarioProfesores.get(0).getProfesorId();
            try {
                ProfesorDTO profesor = profesorFeignClient.getProfesorById(profesorId);
                response.setProfesor(profesor);
            } catch (Exception e) {
                System.err.println("❌ Error al obtener profesor: " + e.getMessage());
            }
        }

        return response;
    }

    // ================================
    // CREAR HORARIO
    // ================================
    @Transactional
    public HorarioEntity crearHorario(HorarioDTO horarioDTO) {
        System.out.println("🆕 Creando nuevo horario...");

        // Crear horario
        HorarioEntity horario = new HorarioEntity();
        horario.setDiaSemana(horarioDTO.getDiaSemana());
        horario.setHoraInicio(horarioDTO.getHoraInicio());
        horario.setHoraFin(horarioDTO.getHoraFin());
        horario.setAula(horarioDTO.getAula());
        horario.setActivo(horarioDTO.getActivo());
        horario = horarioRepository.save(horario);

        System.out.println("✅ Horario base creado con ID: " + horario.getId());

        // Asignar grupo
        if (horarioDTO.getGrupoId() != null) {
            HorarioGrupoEntity hg = new HorarioGrupoEntity();
            hg.setHorario(horario);
            hg.setGrupoId(horarioDTO.getGrupoId());
            horarioGrupoRepository.save(hg);
            System.out.println("✅ Grupo asignado: " + horarioDTO.getGrupoId());
        }

        // Asignar materia
        if (horarioDTO.getMateriaId() != null) {
            HorarioMateria hm = new HorarioMateria();
            hm.setHorario(horario);
            hm.setMateriaId(horarioDTO.getMateriaId());
            horarioMateriaRepository.save(hm);
            System.out.println("✅ Materia asignada: " + horarioDTO.getMateriaId());
        }

        // Asignar profesor
        if (horarioDTO.getProfesorId() != null) {
            HorarioProfesorEntity hp = new HorarioProfesorEntity();
            hp.setHorario(horario);
            hp.setProfesorId(horarioDTO.getProfesorId());
            horarioProfesorRepository.save(hp);
            System.out.println("✅ Profesor asignado: " + horarioDTO.getProfesorId());
        }

        System.out.println("✅ Horario completo creado exitosamente");
        return horario;
    }

    // ================================
    // ACTUALIZAR HORARIO
    // ================================
    @Transactional
    public HorarioEntity actualizarHorario(HorarioDTO horarioDTO) {
        System.out.println("🔄 Actualizando horario ID: " + horarioDTO.getId());

        HorarioEntity horario = entityManager.find(HorarioEntity.class, horarioDTO.getId());

        if (horario == null) {
            throw new RuntimeException("❌ Horario no encontrado");
        }

        // Actualizar datos básicos
        horario.setDiaSemana(horarioDTO.getDiaSemana());
        horario.setHoraInicio(horarioDTO.getHoraInicio());
        horario.setHoraFin(horarioDTO.getHoraFin());
        horario.setAula(horarioDTO.getAula());
        horario.setActivo(horarioDTO.getActivo());

        HorarioEntity horarioActualizado = entityManager.merge(horario);
        System.out.println("✅ Datos básicos actualizados");

        // Actualizar relación con grupo
        horarioGrupoRepository.deleteByHorario_Id(horarioDTO.getId());
        if (horarioDTO.getGrupoId() != null) {
            HorarioGrupoEntity hg = new HorarioGrupoEntity();
            hg.setHorario(horarioActualizado);
            hg.setGrupoId(horarioDTO.getGrupoId());
            horarioGrupoRepository.save(hg);
            System.out.println("✅ Grupo actualizado: " + horarioDTO.getGrupoId());
        }

        // Actualizar relación con materia
        horarioMateriaRepository.deleteByHorario_Id(horarioDTO.getId());
        if (horarioDTO.getMateriaId() != null) {
            HorarioMateria hm = new HorarioMateria();
            hm.setHorario(horarioActualizado);
            hm.setMateriaId(horarioDTO.getMateriaId());
            horarioMateriaRepository.save(hm);
            System.out.println("✅ Materia actualizada: " + horarioDTO.getMateriaId());
        }

        // Actualizar relación con profesor
        horarioProfesorRepository.deleteByHorario_Id(horarioDTO.getId());
        if (horarioDTO.getProfesorId() != null) {
            HorarioProfesorEntity hp = new HorarioProfesorEntity();
            hp.setHorario(horarioActualizado);
            hp.setProfesorId(horarioDTO.getProfesorId());
            horarioProfesorRepository.save(hp);
            System.out.println("✅ Profesor actualizado: " + horarioDTO.getProfesorId());
        }

        System.out.println("✅ Horario actualizado completamente");
        return horarioActualizado;
    }

    // ================================
    // ELIMINAR HORARIO
    // ================================
    @Transactional
    public void eliminarHorario(int horarioId) {
        System.out.println("🗑️ Eliminando horario ID: " + horarioId);

        // Eliminar relaciones
        horarioGrupoRepository.deleteByHorario_Id(horarioId);
        horarioMateriaRepository.deleteByHorario_Id(horarioId);
        horarioProfesorRepository.deleteByHorario_Id(horarioId);
        System.out.println("✅ Relaciones eliminadas");

        // Eliminar horario
        HorarioEntity horario = entityManager.find(HorarioEntity.class, horarioId);
        if (horario != null) {
            entityManager.remove(horario);
            System.out.println("✅ Horario eliminado exitosamente");
        } else {
            System.out.println("⚠️ Horario no encontrado");
        }
    }

    // ================================
    // OBTENER HORARIOS DE UN GRUPO ESPECÍFICO
    // ================================
    public List<HorarioResponseDTO> findByGrupoId(Integer grupoId) {
        System.out.println("🔍 Buscando horarios del grupo ID: " + grupoId);

        List<HorarioGrupoEntity> horarioGrupos = horarioGrupoRepository.findByGrupoId(grupoId);

        List<HorarioResponseDTO> result = horarioGrupos.stream()
                .map(hg -> findByIdWithGrupoYMateria(hg.getHorario().getId()))
                .filter(h -> h != null)
                .collect(Collectors.toList());

        System.out.println("✅ Encontrados " + result.size() + " horarios");
        return result;
    }

    // ================================
    // OBTENER HORARIOS POR MATRÍCULA DE ALUMNO
    // ================================
    public List<HorarioResponseDTO> findByMatricula(String matricula) {
        System.out.println("🔍 Buscando horarios para matrícula: " + matricula);

        try {
            UsuarioDTO usuario = usuarioFeignClient.getUsuarioByMatricula(matricula);

            if (usuario == null) {
                System.err.println("❌ Usuario no encontrado con matrícula: " + matricula);
                return null;
            }

            GrupoDTO grupo = grupoFeignClient.getGrupoByUsuarioId(usuario.getId());

            if (grupo == null) {
                System.err.println("❌ El usuario no está asignado a ningún grupo");
                return null;
            }

            return findByGrupoId(grupo.getId());

        } catch (Exception e) {
            System.err.println("❌ Error al obtener horario por matrícula: " + e.getMessage());
            return null;
        }
    }

    // ================================
    // OBTENER HORARIOS POR ID DE PROFESOR
    // ================================
    public List<HorarioResponseDTO> findByProfesorId(Long profesorId) {
        System.out.println("🔍 Buscando horarios del profesor ID: " + profesorId);

        List<HorarioProfesorEntity> horariosProfesores = horarioProfesorRepository.findByProfesorId(profesorId);

        List<HorarioResponseDTO> result = horariosProfesores.stream()
                .map(hp -> {
                    HorarioEntity horario = hp.getHorario();
                    HorarioResponseDTO dto = new HorarioResponseDTO();

                    dto.setId(horario.getId());
                    dto.setDiaSemana(horario.getDiaSemana());
                    dto.setHoraInicio(horario.getHoraInicio());
                    dto.setHoraFin(horario.getHoraFin());
                    dto.setAula(horario.getAula());
                    dto.setActivo(horario.getActivo());

                    // Obtener grupo
                    var hgs = horarioGrupoRepository.findByHorario_Id(horario.getId());
                    if (!hgs.isEmpty()) {
                        Integer grupoId = hgs.get(0).getGrupoId();
                        try {
                            GrupoDTO grupo = grupoFeignClient.getGrupoById(grupoId);
                            dto.setGrupo(grupo);
                        } catch (Exception e) {
                            System.err.println("❌ Error al obtener grupo: " + e.getMessage());
                        }
                    }

                    // Obtener materia
                    var hms = horarioMateriaRepository.findByHorario_Id(horario.getId());
                    if (!hms.isEmpty()) {
                        Integer materiaId = hms.get(0).getMateriaId();
                        try {
                            MateriaDTO materia = materiaFeignClient.getMateriaById(materiaId);
                            dto.setMateria(materia);
                        } catch (Exception e) {
                            System.err.println("❌ Error al obtener materia: " + e.getMessage());
                        }
                    }

                    // Obtener profesor
                    try {
                        ProfesorDTO profesor = profesorFeignClient.getProfesorById(profesorId);
                        dto.setProfesor(profesor);
                    } catch (Exception e) {
                        System.err.println("❌ Error al obtener profesor: " + e.getMessage());
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        System.out.println("✅ Encontrados " + result.size() + " horarios para el profesor");
        return result;
    }
}