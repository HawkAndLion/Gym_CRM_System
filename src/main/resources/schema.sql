DROP TABLE IF EXISTS trainee_trainers;
DROP TABLE IF EXISTS trainings;
DROP TABLE IF EXISTS training_types;
DROP TABLE IF EXISTS trainees;
DROP TABLE IF EXISTS trainers;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE trainers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    specialization VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_trainer_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE trainees (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    address VARCHAR(255),
    date_of_birth DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_trainee_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE training_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE trainings (
    id BIGSERIAL PRIMARY KEY,
    trainee_id BIGINT NOT NULL,
    trainer_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    training_type_id BIGINT NOT NULL,
    training_date DATE NOT NULL,
    duration DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_training_trainee FOREIGN KEY (trainee_id) REFERENCES trainees(id) ON DELETE CASCADE,
    CONSTRAINT fk_training_trainer FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON DELETE CASCADE,
    CONSTRAINT fk_training_type FOREIGN KEY (training_type_id) REFERENCES training_types(id) ON DELETE CASCADE
);

CREATE TABLE trainee_trainers (
    trainee_id BIGINT NOT NULL,
    trainer_id BIGINT NOT NULL,
    PRIMARY KEY (trainee_id, trainer_id),
    CONSTRAINT fk_tt_trainee FOREIGN KEY (trainee_id) REFERENCES trainees(id) ON DELETE CASCADE,
    CONSTRAINT fk_tt_trainer FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON DELETE CASCADE
);
