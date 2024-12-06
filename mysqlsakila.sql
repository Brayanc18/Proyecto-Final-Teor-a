SELECT language_id FROM language WHERE name = 'English';

DESCRIBE film;

ALTER TABLE film ADD COLUMN status ENUM('active', 'inactive') DEFAULT 'active';


CREATE TABLE language (
    language_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE film (
    film_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    release_year INT,
    language_id INT,
    status ENUM('active', 'inactive') DEFAULT 'active',
    FOREIGN KEY (language_id) REFERENCES language(language_id)
);

-- Insertar algunos idiomas
INSERT INTO language (name) VALUES ('English'), ('Spanish'), ('French'), ('German');

-- Insertar algunas películas
INSERT INTO film (title, description, release_year, language_id) 
VALUES ('Inception', 'A mind-bending thriller', 2010, 1),
       ('El Laberinto del Fauno', 'A dark fantasy tale', 2006, 2);


SELECT * FROM film;
