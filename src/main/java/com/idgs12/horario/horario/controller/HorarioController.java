package com.idgs12.horario.horario.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.idgs12.horario.horario.dto.GrupoDTO;
import com.idgs12.horario.horario.dto.HorarioDTO;
import com.idgs12.horario.horario.dto.HorarioListDTO;
import com.idgs12.horario.horario.dto.HorarioResponseDTO;
import com.idgs12.horario.horario.dto.MateriaDTO;
import com.idgs12.horario.horario.dto.ProfesorDTO;
import com.idgs12.horario.horario.entity.HorarioEntity;
import com.idgs12.horario.horario.FeignClient.GrupoFeignClient;
import com.idgs12.horario.horario.FeignClient.MateriaFeignClient;
import com.idgs12.horario.horario.FeignClient.ProfesorFeignClient;
import com.idgs12.horario.horario.repository.HorarioGrupoRepository;
import com.idgs12.horario.horario.repository.HorarioMateriaRepository;
import com.idgs12.horario.horario.repository.HorarioProfesorRepository;
import com.idgs12.horario.horario.services.HorarioService;

@RestController
@RequestMapping("/horarios")
@CrossOrigin(origins = "*")
public class HorarioController {

    @Autowired
    private HorarioService horarioService;

    @Autowired
    private HorarioGrupoRepository horarioGrupoRepository;

    @Autowired
    private HorarioMateriaRepository horarioMateriaRepository;
    
    @Autowired
    private HorarioProfesorRepository horarioProfesorRepository;

    @Autowired
    private GrupoFeignClient grupoFeignClient;

    @Autowired
    private MateriaFeignClient materiaFeignClient;

    @Autowired
    private ProfesorFeignClient profesorFeignClient;

    // ================================
    // GET ALL HORARIOS
    // ================================
    @GetMapping("/all")
    public List<HorarioListDTO> getAllHorarios() {
        return horarioGrupoRepository.findAll()
                .stream()
                .map(hg -> hg.getHorario())
                .distinct()
                .map(horario -> {
                    HorarioListDTO dto = new HorarioListDTO();
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
                        dto.setGrupoId(grupoId);

                        try {
                            GrupoDTO grupo = grupoFeignClient.getGrupoById(grupoId);
                            dto.setGrupoNombre(grupo.getNombre());
                        } catch (Exception e) {
                            dto.setGrupoNombre("N/A");
                        }
                    }

                    // Obtener materia
                    var hms = horarioMateriaRepository.findByHorario_Id(horario.getId());
                    if (!hms.isEmpty()) {
                        Integer materiaId = hms.get(0).getMateriaId();
                        dto.setMateriaId(materiaId);

                        try {
                            MateriaDTO materia = materiaFeignClient.getMateriaById(materiaId);
                            dto.setMateriaNombre(materia.getNombre());
                        } catch (Exception e) {
                            dto.setMateriaNombre("N/A");
                        }
                    }

                    // Obtener profesor
                    var hps = horarioProfesorRepository.findByHorario_Id(horario.getId());
                    if (!hps.isEmpty()) {
                        Long profesorId = hps.get(0).getProfesorId();
                        dto.setProfesorId(profesorId);

                        try {
                            ProfesorDTO profesor = profesorFeignClient.getProfesorById(profesorId);
                            dto.setProfesorNombre(profesor.getNombreCompleto());
                        } catch (Exception e) {
                            dto.setProfesorNombre("N/A");
                        }
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ================================
    // GET HORARIO BY ID
    // ================================
    @GetMapping("/{id}")
    public ResponseEntity<HorarioResponseDTO> getHorarioConGrupoYMateria(@PathVariable int id) {
        HorarioResponseDTO horario = horarioService.findByIdWithGrupoYMateria(id);
        if (horario != null) {
            return ResponseEntity.ok(horario);
        }
        return ResponseEntity.notFound().build();
    }

    // ================================
    // CREATE HORARIO
    // ================================
    @PostMapping
    public ResponseEntity<HorarioEntity> crearHorario(@RequestBody HorarioDTO horarioDTO) {
        HorarioEntity nuevoHorario = horarioService.crearHorario(horarioDTO);
        return ResponseEntity.ok(nuevoHorario);
    }

    // ================================
    // UPDATE HORARIO
    // ================================
    @PutMapping("/{id}")
    public ResponseEntity<HorarioEntity> actualizarHorario(@PathVariable int id, @RequestBody HorarioDTO horarioDTO) {
        horarioDTO.setId(id);
        HorarioEntity horarioActualizado = horarioService.actualizarHorario(horarioDTO);
        return ResponseEntity.ok(horarioActualizado);
    }

    // ================================
    // DELETE HORARIO
    // ================================
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarHorario(@PathVariable int id) {
        try {
            horarioService.eliminarHorario(id);
            return ResponseEntity.ok("Horario eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ================================
    // GET HORARIOS BY GRUPO ID
    // ================================
    @GetMapping("/grupo/{grupoId}")
    public List<HorarioResponseDTO> getHorariosPorGrupo(@PathVariable Integer grupoId) {
        return horarioService.findByGrupoId(grupoId);
    }

    // ================================
    // GET HORARIOS BY MATRICULA
    // ================================
    @GetMapping("/matricula/{matricula}")
    public ResponseEntity<List<HorarioResponseDTO>> getHorarioByMatricula(@PathVariable String matricula) {
        List<HorarioResponseDTO> horarios = horarioService.findByMatricula(matricula);

        if (horarios == null || horarios.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(horarios);
    }

    // ================================
    // GET HORARIOS BY PROFESOR ID
    // ================================
    @GetMapping("/profesor/{profesorId}")
    public ResponseEntity<List<HorarioResponseDTO>> getHorariosPorProfesor(@PathVariable Long profesorId) {
        List<HorarioResponseDTO> horarios = horarioService.findByProfesorId(profesorId);

        if (horarios == null || horarios.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(horarios);
    }
}