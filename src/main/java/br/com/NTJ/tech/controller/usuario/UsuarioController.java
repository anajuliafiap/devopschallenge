package br.com.NTJ.tech.controller.usuario;

import br.com.NTJ.tech.dto.usuario.CadastroUsuario;
import br.com.NTJ.tech.dto.usuario.DetalhesUsuario;
import br.com.NTJ.tech.model.usuario.Usuario;
import br.com.NTJ.tech.repository.usuario.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("registrar")
    @Transactional
    public ResponseEntity<DetalhesUsuario> post(@RequestBody @Valid CadastroUsuario dto,
                                                UriComponentsBuilder builder){
        var usuario = new Usuario(dto.username(), passwordEncoder.encode(dto.password()));
        usuarioRepository.save(usuario);
        var url = builder.path("usuario/{id}").buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(url).body(new DetalhesUsuario(usuario));
    }

}
