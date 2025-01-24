package com.espe.micro_cursos.services;

import com.espe.micro_cursos.clients.UsuarioClientRest;
import com.espe.micro_cursos.models.Usuario;
import com.espe.micro_cursos.models.entities.Curso;
import com.espe.micro_cursos.models.entities.CursoUsuario;
import com.espe.micro_cursos.repositories.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CursoServiceImpl implements CursoService {
    @Autowired
    private CursoRepository repository;

    @Autowired
    private UsuarioClientRest clientRest;

    @Override
    public List<Curso> findAll() {
        // Convertir Iterable a List
        List<Curso> cursos = new ArrayList<>();
        repository.findAll().forEach(cursos::add);

        // Completar los usuarios en cada curso
        cursos.forEach(curso -> {
            List<Long> usuarioIds = curso.getCursoUsuarios().stream()
                    .map(cursoUsuario -> cursoUsuario.getUsuarioId())
                    .collect(Collectors.toList());

            // Log para depuración
            System.out.println("Curso ID: " + curso.getId() + " tiene usuarios con IDs: " + usuarioIds);
        });
        return cursos;
    }



    @Override
    public Optional<Curso> findById(Long id) {
        // Obtener el curso como Optional
        Optional<Curso> cursoOptional = repository.findById(id);

        // Si el curso existe, completar los usuarios y devolverlo
        if (cursoOptional.isPresent()) {
            Curso curso = cursoOptional.get();
            try {
                List<Usuario> usuarios = getUsuariosByCursoId(id); // Obtener los usuarios relacionados
                curso.setUsuarios(usuarios);
            } catch (Exception e) {
                curso.setUsuarios(Collections.emptyList());
                System.err.println("Error al obtener usuarios para el curso: " + curso.getId());
            }
            return Optional.of(curso);
        }

        // Si no existe, devolver un Optional vacío
        return Optional.empty();
    }

    @Override
    public Curso save(Curso curso) {
        return repository.save(curso);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Optional<Usuario> addUsuario(Usuario usuario, Long id) {
        Optional<Curso> cursoOptional = repository.findById(id);
        if (cursoOptional.isPresent()) {
            Usuario usuarioTemp = clientRest.findById(usuario.getId());

            Curso curso = cursoOptional.get();
            // Agregar la relación curso-usuario
            CursoUsuario cursoUsuario = new CursoUsuario();
            cursoUsuario.setUsuarioId(usuarioTemp.getId());
            curso.addCursoUsuario(cursoUsuario);
            repository.save(curso);
            return Optional.of(usuarioTemp);
        }
        return Optional.empty();
    }

    @Override
    public Usuario saveUsuario(Usuario usuario) {
        return clientRest.save(usuario);
    }

    @Override
    public Optional<Usuario> removeUsuario(Long usuarioId, Long cursoId) {
        Optional<Curso> cursoOptional = repository.findById(cursoId);
        if (cursoOptional.isPresent()) {
            Curso curso = cursoOptional.get();
            // Remover la relación curso-usuario
            curso.removeCursoUsuarioByUsuarioId(usuarioId);
            repository.save(curso);
            return Optional.of(clientRest.findById(usuarioId));
        }
        return Optional.empty();
    }

    @Override
    public List<Usuario> getUsuariosByCursoId(Long cursoId) {
        Optional<Curso> cursoOptional = repository.findById(cursoId);
        if (cursoOptional.isPresent()) {
            Curso curso = cursoOptional.get();
            // Obtener los IDs de los usuarios relacionados
            List<Long> usuarioIds = curso.getCursoUsuarios().stream()
                    .map(CursoUsuario::getUsuarioId)
                    .collect(Collectors.toList());

            // Si hay IDs, obtener los usuarios completos a través del cliente Feign
            if (!usuarioIds.isEmpty()) {
                return clientRest.findAllByIds(usuarioIds); // Solicitud batch
            }
        }
        return Collections.emptyList();
    }
}
