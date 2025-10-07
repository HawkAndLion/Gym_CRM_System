INSERT INTO users (first_name, last_name, username, password, is_active) VALUES ('John', 'Brown', 'John.Brown', 'qwertyuiop', true);
INSERT INTO users (first_name, last_name, username, password, is_active) VALUES ('Amanda', 'Smith', 'Amanda.Smith', 'qwertyuiop', true);
INSERT INTO users (first_name, last_name, username, password, is_active) VALUES ('Lindsey', 'Adams', 'Lindsey.Adams', 'qwertyuiop', true);
INSERT INTO users (first_name, last_name, username, password, is_active) VALUES ('Jacob', 'Franklin', 'Jacob.Franklin', 'qwertyuiop', true);
INSERT INTO users (first_name, last_name, username, password, is_active) VALUES ('Jessica', 'Parker', 'Jessica.Parker', 'qwertyuiop', true);
INSERT INTO users (first_name, last_name, username, password, is_active) VALUES ('Nick', 'Carter', 'Nick.Carter', 'qwertyuiop', true);

INSERT INTO trainers (user_id, specialization, is_active) VALUES (1, 'Weight Loss Specialist', true);
INSERT INTO trainers (user_id, specialization, is_active) VALUES (2, 'Strength and Conditioning Coach', true);
INSERT INTO trainers (user_id, specialization, is_active) VALUES (3, 'Yoga Instructor', true);

INSERT INTO trainees (user_id, address, date_of_birth, is_active) VALUES (4, 'Almaty, Seifullin Str. 153', '1998-04-15', 'true');
INSERT INTO trainees (user_id, address, date_of_birth, is_active) VALUES (5, 'Astana, Makataev Str. 208', '1995-11-20', 'true');
INSERT INTO trainees (user_id, address, date_of_birth, is_active) VALUES (6, 'Shymkent, Kunaev Str. 88', '2000-02-01', 'true');

INSERT INTO training_types (name) VALUES ('Power Lifting');
INSERT INTO training_types (name) VALUES ('Body Shape');
INSERT INTO training_types (name) VALUES ('Cardio');

INSERT INTO trainee_trainers (trainee_id, trainer_id) VALUES (1, 2);
INSERT INTO trainee_trainers (trainee_id, trainer_id) VALUES (2, 1);
INSERT INTO trainee_trainers (trainee_id, trainer_id) VALUES (2, 3);
INSERT INTO trainee_trainers (trainee_id, trainer_id) VALUES (3, 1);
INSERT INTO trainee_trainers (trainee_id, trainer_id) VALUES (3, 2);
INSERT INTO trainee_trainers (trainee_id, trainer_id) VALUES (3, 3);

INSERT INTO trainings (trainee_id, trainer_id, name, training_type_id, training_date, duration) VALUES (1, 2, 'Workout', 1, '2025-10-01', 1.5);
INSERT INTO trainings (trainee_id, trainer_id, name, training_type_id, training_date, duration) VALUES (2, 3, 'Fitness', 2, '2025-10-02', 2.0);
INSERT INTO trainings (trainee_id, trainer_id, name, training_type_id, training_date, duration) VALUES (3, 1, 'Cardio', 3, '2025-10-03', 1.0);