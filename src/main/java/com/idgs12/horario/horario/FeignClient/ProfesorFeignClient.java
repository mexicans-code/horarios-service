package com.idgs12.horario.horario.FeignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.idgs12.horario.horario.dto.ProfesorDTO;

@FeignClient(name = "PROFESORES", url = "https://profesores-repository-production.up.railway.app")
public interface ProfesorFeignClient {

    @GetMapping("/api/profesores/{id}")
    ProfesorDTO getProfesorById(@PathVariable("id") Long id);
}