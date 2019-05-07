CREATE database IF NOT EXISTS moviedb;
USE moviedb;

#DROP TABLE IF EXISTS sales;
#DROP TABLE IF EXISTS customers;
#DROP TABLE IF EXISTS ratings;
#DROP TABLE IF EXISTS creditcards;
#DROP TABLE IF EXISTS genres_in_movies;
#DROP TABLE IF EXISTS genres;
#DROP TABLE IF EXISTS stars_in_movies;
#DROP TABLE IF EXISTS stars;
#DROP TABLE IF EXISTS movies;

CREATE TABLE IF NOT EXISTS movies(
	id varchar(10) NOT NULL,
    title varchar(100) NOT NULL,
    year integer NOT NULL,
    director varchar(100) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS stars(
	id varchar(10) NOT NULL,
    name varchar(100) NOT NULL,
    birthYear integer,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS stars_in_movies(
	starId varchar(10) NOT NULL,
    movieId varchar(32) NOT NULL,
    FOREIGN KEY (starId) REFERENCES stars (id) ON DELETE CASCADE,
    FOREIGN KEY (movieId) REFERENCES movies (id)  ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS genres(
	id integer NOT NULL auto_increment,
    name varchar(32) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS genres_in_movies(
	genreId integer NOT NULL,
    movieId varchar(10) NOT NULL,
    FOREIGN KEY (genreId) REFERENCES genres(id) ON DELETE CASCADE,
    FOREIGN KEY (movieId) REFERENCES movies(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS creditcards(
	id varchar(20) NOT NULL,
    firstName varchar(50) NOT NULL,
    lastName varchar(50) NOT NULL,
    expiration date NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS ratings(
	movieId varchar(10) NOT NULL,
    rating float NOT NULL,
    numVotes integer NOT NULL,
    FOREIGN KEY (movieId) REFERENCES movies (id)
);

CREATE TABLE IF NOT EXISTS customers(
	id integer NOT NULL AUTO_INCREMENT,
    firstName varchar(50) NOT NULL,
    lastName varchar(50) NOT NULL,
    ccId varchar(20) NOT NULL,
    address varchar(200),
    email varchar(50) NOT NULL,
    password varchar(50),
    PRIMARY KEY (id),
    FOREIGN KEY (ccId) REFERENCES creditcards (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sales(
	id integer NOT NULL AUTO_INCREMENT,
    customerId integer NOT NULL,
    movieId varchar(10) NOT NULL,
    saleDate date NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (customerId) REFERENCES customers (id) ON DELETE CASCADE,
    FOREIGN KEY (movieId) REFERENCES movies (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS employees(
	email varchar(50) primary key,
	password varchar(20) not null,
	fullname varchar(100)
);

INSERT INTO employees VALUES('classta@email.edu', 'classta', 'TA CS122B');