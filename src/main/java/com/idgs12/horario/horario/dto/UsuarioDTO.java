package com.idgs12.horario.horario.dto;

import lombok.Data;

@Data
public class UsuarioDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String matricula;
    private String email;
}