#---!Ups
CREATE TABLE users(
  id BIGSERIAL PRIMARY KEY,
  ks VARCHAR(255),
  email VARCHAR(255) NOT NULL,
  role VARCHAR(255) NOT NULL,
  name VARCHAR(255),
  company VARCHAR(255)
);

CREATE TABLE user_logins(
  user_id BIGINT NOT NULL REFERENCES users ON UPDATE RESTRICT ON DELETE CASCADE,
  login_provider_id VARCHAR(255) NOT NULL,
  login_provider_key VARCHAR(255) NOT NULL,
  CONSTRAINT user_logins_pkey PRIMARY KEY (login_provider_id, login_provider_key), -- single login for specific key per provider
  CONSTRAINT user_logins_provider_id_idx UNIQUE (user_id, login_provider_id) -- single login per user with specific provider
);


CREATE TABLE passwords(
  provider_id VARCHAR(255) NOT NULL,
  provider_key VARCHAR(255) NOT NULL,
  hasher VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  salt VARCHAR(255),
  CONSTRAINT passwords_pkey PRIMARY KEY (provider_id, provider_key)
);


CREATE TABLE bearer_tokens(
  id VARCHAR(256) PRIMARY KEY,
  provider_id VARCHAR(255) NOT NULL,
  provider_key VARCHAR(255) NOT NULL,
  last_used timestamptz NOT NULL,
  expiration timestamptz NOT NULL,
  idle_timeout BIGINT
);

CREATE TABLE permissions(
  user_id BIGINT NOT NULL REFERENCES users ON UPDATE RESTRICT ON DELETE CASCADE,
  resource_id BIGINT NOT NULL,
  resource_type VARCHAR(32) NOT NULL,
  CONSTRAINT accounts_pkey PRIMARY KEY (user_id, resource_type)
);

CREATE TABLE jwt_tokens(
  id VARCHAR(256) PRIMARY KEY,
  provider_id VARCHAR(255) NOT NULL,
  provider_key VARCHAR(255) NOT NULL,
  last_used timestamptz NOT NULL,
  expiration timestamptz NOT NULL,
  idle_timeout BIGINT,
  custom_claims JSON
);

INSERT INTO users (ks, email, role, name, company) VALUES ('basic-auth', 'admin@smowtion.net', 'admin', 'Admin', 'AppodealX');
INSERT INTO passwords (provider_id, provider_key, hasher, password) VALUES ('credentials', 'admin@smowtion.net', 'bcrypt-sha256', '$2a$10$.p.4AIB89SpW9rheuI2druKESUe6TGvvJ2flQyo2yLeFdjS4QXDfi');
INSERT INTO user_logins (user_id, login_provider_id, login_provider_key) VALUES (1, 'credentials', 'admin@smowtion.net');

#---!Downs
DROP TABLE jwt_tokens;
DROP TABLE permissions;
DROP TABLE bearer_tokens;
DROP TABLE passwords;
DROP TABLE user_logins;
DROP TABLE users;