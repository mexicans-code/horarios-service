package com.idgs12.horario.horario.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.idgs12.horario.horario.dto.UsuarioDTO;

@FeignClient(name = "usuarios", url = "https://usuarios-repository-production.up.railway.app")
public interface UsuarioFeignClient {

    @GetMapping("/usuarios/matricula/{matricula}")
    UsuarioDTO getUsuarioByMatricula(@PathVariable String matricula);
}