DROP TABLE IF EXISTS people ;

CREATE TABLE people  (
    person_id INTEGER AUTO_INCREMENT NOT NULL PRIMARY KEY,
    first_name VARCHAR(20),
    last_name VARCHAR(20)
);

DROP TABLE IF EXISTS operations;

CREATE TABLE operations  (
    operations_id INTEGER AUTO_INCREMENT NOT NULL PRIMARY KEY,
    date DATE,
    libelle VARCHAR(150),
    montant DECIMAL(20,4),
    montant2 DECIMAL(20,4),
    nomFichier VARCHAR(30)
);
