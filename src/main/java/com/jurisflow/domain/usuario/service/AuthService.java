package com.jurisflow.domain.usuario.service;

import com.jurisflow.domain.usuario.dto.LoginRequestDTO;
import com.jurisflow.domain.usuario.dto.LoginResponseDTO;
import com.jurisflow.domain.usuario.dto.UsuarioCreateDTO;
import com.jurisflow.domain.usuario.dto.UsuarioDTO;
import com.jurisflow.domain.usuario.entity.Usuario;
import com.jurisflow.domain.usuario.mapper.UsuarioMapper;
import com.jurisflow.domain.usuario.repository.UsuarioRepository;
import com.jurisflow.domain.escritorio.entity.Escritorio;
import com.jurisflow.domain.escritorio.repository.EscritorioRepository;
import com.jurisflow.exception.BusinessException;
import com.jurisflow.exception.ResourceNotFoundException;
import com.jurisflow.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service para autenticação e registro de usuários.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final EscritorioRepository escritorioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Realiza o login do usuário.
     */
    @Transactional
    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getSenha()
                )
            );

            Usuario usuario = (Usuario) authentication.getPrincipal();
            
            // Atualiza último login
            usuario.setUltimoLogin(LocalDateTime.now());
            usuario.setTentativasLogin(0);
            usuarioRepository.save(usuario);

            String accessToken = jwtTokenProvider.generateToken(usuario);
            String refreshToken = jwtTokenProvider.generateRefreshToken(usuario);

            log.info("Usuário {} logado com sucesso", usuario.getEmail());

            return LoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration)
                    .usuario(usuarioMapper.toDTO(usuario))
                    .build();

        } catch (BadCredentialsException e) {
            handleFailedLogin(request.getEmail());
            throw e;
        }
    }

    /**
     * Registra um novo usuário e escritório (onboarding).
     */
    @Transactional
    public LoginResponseDTO registrar(UsuarioCreateDTO usuarioDTO, String nomeEscritorio) {
        // Verifica se email já existe
        if (usuarioRepository.existsByEmail(usuarioDTO.getEmail())) {
            throw new BusinessException("Email já cadastrado no sistema");
        }

        // Cria o escritório
        Escritorio escritorio = Escritorio.builder()
                .nome(nomeEscritorio)
                .email(usuarioDTO.getEmail())
                .plano(Escritorio.PlanoEscritorio.TRIAL)
                .build();
        escritorio = escritorioRepository.save(escritorio);

        // Cria o usuário administrador
        Usuario usuario = usuarioMapper.toEntity(usuarioDTO);
        usuario.setSenha(passwordEncoder.encode(usuarioDTO.getSenha()));
        usuario.setEscritorio(escritorio);
        usuario.setRole(Usuario.Role.ADMIN);
        usuario = usuarioRepository.save(usuario);

        log.info("Novo escritório {} e usuário {} registrados", nomeEscritorio, usuario.getEmail());

        // Gera tokens e retorna
        String accessToken = jwtTokenProvider.generateToken(usuario);
        String refreshToken = jwtTokenProvider.generateRefreshToken(usuario);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .usuario(usuarioMapper.toDTO(usuario))
                .build();
    }

    /**
     * Atualiza o token de acesso usando o refresh token.
     */
    @Transactional(readOnly = true)
    public LoginResponseDTO refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("Refresh token inválido ou expirado");
        }

        String email = jwtTokenProvider.extractUsername(refreshToken);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "email", email));

        String newAccessToken = jwtTokenProvider.generateToken(usuario);

        return LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .usuario(usuarioMapper.toDTO(usuario))
                .build();
    }

    /**
     * Trata falha de login (incrementa tentativas).
     */
    private void handleFailedLogin(String email) {
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            int tentativas = usuario.getTentativasLogin() + 1;
            usuario.setTentativasLogin(tentativas);
            
            // Bloqueia após 5 tentativas por 15 minutos
            if (tentativas >= 5) {
                usuario.setBloqueadoAte(LocalDateTime.now().plusMinutes(15));
                log.warn("Usuário {} bloqueado por múltiplas tentativas de login", email);
            }
            
            usuarioRepository.save(usuario);
        });
    }
}


