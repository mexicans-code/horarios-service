package com.idgs12.horario.horario.dto;

import lombok.Data;

@Data
public class ProfesorDTO {
    private Long id;
    private String numeroEmpleado;
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private Boolean activo;
}