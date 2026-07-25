-- Corrige hash da senha do admin demo (admin123)
-- O hash anterior na V1 não correspondia à senha documentada

UPDATE usuarios
SET senha = '$2a$10$GsRYf01tp66kwGoDYz/UB..k7oe2p9ILwQPAfD/u22mGpqacSWhu6',
    tentativas_login = 0,
    bloqueado_ate = NULL
WHERE email = 'admin@jurisflow.com.br';
