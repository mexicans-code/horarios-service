package com.idgs12.horario.horario.dto;

import lombok.Data;

@Data
public class HorarioListDTO {
    private Integer id;
    private String diaSemana;
    private String horaInicio;
    private String horaFin;
    private String aula;
    private Boolean activo;

    // Grupo
    private Integer grupoId;
    private String grupoNombre;

    // Materia ✅
    private Integer materiaId;
    private String materiaNombre;

    private Long profesorId;
    private String profesorNombre;
}
