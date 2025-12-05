package com.idgs12.horario.horario.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.idgs12.horario.horario.dto.GrupoDTO;

@FeignClient(name = "grupos", url = "https://grupos-repository-production.up.railway.app")
public interface GrupoFeignClient {

    @GetMapping("/grupos/{id}")
    GrupoDTO getGrupoById(@PathVariable("id") Integer id);
    
    @GetMapping("/grupos/usuario/{usuarioId}")
    GrupoDTO getGrupoByUsuarioId(@PathVariable Long usuarioId);
}
