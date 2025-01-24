package com.espe.micro_cursos.clients;

import com.espe.micro_cursos.models.Usuario;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "micro-usuarios", url = "http://localhost:8003/api/usuarios")
public interface UsuarioClientRest {

    // Obtener un usuario por su ID
    @GetMapping("/{id}")
    Usuario findById(@PathVariable Long id);

    // Guardar un nuevo usuario
    @PostMapping
    Usuario save(@RequestBody Usuario usuario);

    // Obtener múltiples usuarios por sus IDs
    @PostMapping("/batch")
    List<Usuario> findAllByIds(@RequestBody List<Long> ids);
}
