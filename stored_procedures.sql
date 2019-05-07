use moviedb;

Delimiter //

DROP FUNCTION IF EXISTS ExtractNumber;
CREATE FUNCTION ExtractNumber(in_string VARCHAR(50)) 
#from https://stackoverflow.com/questions/37268248/how-to-get-only-digits-from-string-in-mysql
RETURNS INT
NO SQL
BEGIN
    DECLARE ctrNumber VARCHAR(50);
    DECLARE finNumber VARCHAR(50) DEFAULT '';
    DECLARE sChar VARCHAR(1);
    DECLARE inti INTEGER DEFAULT 1;

    IF LENGTH(in_string) > 0 THEN
        WHILE(inti <= LENGTH(in_string)) DO
            SET sChar = SUBSTRING(in_string, inti, 1);
            SET ctrNumber = FIND_IN_SET(sChar, '0,1,2,3,4,5,6,7,8,9'); 
            IF ctrNumber > 0 THEN
                SET finNumber = CONCAT(finNumber, sChar);
            END IF;
            SET inti = inti + 1;
        END WHILE;
        RETURN CAST(finNumber AS UNSIGNED);
    ELSE
        RETURN 0;
    END IF;    
END//

DROP FUNCTION IF EXISTS ExtractCharacter;
CREATE FUNCTION ExtractCharacter(in_string VARCHAR(50)) 
#from https://stackoverflow.com/questions/37268248/how-to-get-only-digits-from-string-in-mysql
RETURNS VARCHAR(50)
NO SQL
BEGIN
    DECLARE ctrNumber VARCHAR(50);
    DECLARE finNumber VARCHAR(50) DEFAULT '';
    DECLARE sChar VARCHAR(1);
    DECLARE inti INTEGER DEFAULT 1;

    IF LENGTH(in_string) > 0 THEN
        WHILE(inti <= LENGTH(in_string)) DO
            SET sChar = SUBSTRING(in_string, inti, 1);
            SET ctrNumber = FIND_IN_SET(sChar, "a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z, , "); 
            IF ctrNumber > 0 THEN
                SET finNumber = CONCAT(finNumber, sChar);
            END IF;
            SET inti = inti + 1;
        END WHILE;
        RETURN finNumber;
    ELSE
        RETURN 0;
    END IF;    
END//


DROP PROCEDURE IF EXISTS add_star;
CREATE PROCEDURE add_star(IN star_name VARCHAR(100), IN star_birthYear INTEGER)
BEGIN
	DECLARE maxid VARCHAR(10);    
    DECLARE prefix VARCHAR(10);
    DECLARE num INTEGER;
    
    SELECT MAX(id) into maxid from stars;
    
    SET num = ExtractNumber(maxid);
    SET prefix = ExtractCharacter(maxid);
    
    SET maxid = CONCAT(prefix, CAST(num + 1 AS CHAR(10))); #Cast new id number into string and concat with "nm"
    
	IF star_birthYear IS NULL THEN
		INSERT IGNORE INTO stars (id, name, birthyear)
        VALUES(maxid, star_name, NULL);
    ELSE
		INSERT IGNORE INTO stars (id, name, birthyear)
        VALUES(maxid, star_name, star_birthyear);
	END IF;
END//


#CALL add_star("hello boy", null);

#select * from stars where name = "hello boy";

#DELETE FROM stars where name = "hello boy" LIMIT 1;

DROP PROCEDURE IF EXISTS add_genre;
CREATE PROCEDURE add_genre(IN genre_name VARCHAR(32))
BEGIN
	DECLARE maxid INTEGER;   #max id in table
    SELECT MAX(id) into maxid from genres;
	SET maxid = maxid + 1;
    
    INSERT IGNORE INTO genres (id, name)
    VALUES(maxid, genre_name);
END//

#CALL add_genre("hello");
#SELECT * FROM genres where name = "hello";
#DELETE FROM genres where name = "hello" limit 1;

DROP PROCEDURE IF EXISTS link_genre_to_movie;
CREATE PROCEDURE link_genre_to_movie(IN genre_id INTEGER, IN movie_id VARCHAR(10), OUT linked_genre_to_movie INTEGER)
BEGIN
	DECLARE counter INTEGER;
    SELECT COUNT(*) INTO counter FROM genres_in_movies WHERE genreId = genre_id AND movieId = movie_id;
    
    IF counter = 0 THEN
		INSERT IGNORE INTO genres_in_movies (genreId, movieId)
        VALUES(genre_id, movie_id);
        SET linked_genre_to_movie = 1;
	ELSE
        SET linked_genre_to_movie = 0;
    END IF;
END//

#CALL link_movie_to_genre(17, 'tt0094859');
#SELECT * FROM genres_in_movies WHERE genreId = 17 AND movieId = 'tt0094859';
#DELETE FROM genres_in_movies WHERE genreId = 17 AND movieId = 'tt0094859' LIMIT 1;

DROP PROCEDURE IF EXISTS link_star_to_movie;
CREATE PROCEDURE link_star_to_movie(IN star_id VARCHAR(10), in movie_id VARCHAR(10), OUT linked_star_to_movie INTEGER)
BEGIN
	DECLARE counter INTEGER;
    SELECT COUNT(*) into counter FROM stars_in_movies WHERE starId = star_id AND movieId = movie_id;
	
	IF counter = 0 THEN
		INSERT IGNORE INTO stars_in_movies (starId, movieId)
        VALUES(star_id, movie_id);
        SET linked_star_to_movie = 1;
	Else
		SET linked_star_to_movie = 0;
    END IF;
END//

#CALL link_star_to_movie('nm0000001', 'tt0094859');
#SELECT * FROM stars_in_movies WHERE starId = 'nm0000001' AND movieId = 'tt0094859';
#DELETE FROM stars_in_movies WHERE starId = 'nm0000001' AND movieId = 'tt0094859' LIMIT 1;

DROP PROCEDURE IF EXISTS add_rating_to_movie;
CREATE PROCEDURE add_rating_to_movie(IN movie_id varchar(10), IN movie_rating FLOAT, IN movie_num_votes INTEGER)
BEGIN
	INSERT IGNORE INTO ratings (movieId, rating, numVotes)
    VALUES(movie_id, movie_rating, movie_num_votes);
END//

DROP PROCEDURE IF EXISTS add_movie;
CREATE PROCEDURE add_movie(IN movie_title VARCHAR(100), IN movie_year INTEGER, IN movie_director VARCHAR(100), IN star_name VARCHAR(100), IN genre VARCHAR(32),
							OUT created_movie INTEGER, OUT linked_star_to_movie INTEGER, OUT linked_genre_to_movie INTEGER)
BEGIN
	DECLARE counter INTEGER;
    
    DECLARE num INTEGER;
    DECLARE prefix VARCHAR(10);
    
    DECLARE movieid VARCHAR(10);#new movie id for movie
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INTEGER;
    
    DECLARE star_counter INTEGER;
    DECLARE genre_counter INTEGER;
    
    SELECT COUNT(*) into counter from movies WHERE title = movie_title AND year = movie_year AND director = movie_director;
    
    IF counter = 0 THEN
		SELECT MAX(id) into movieid from movies;
        SET num = ExtractNumber(movieid);
		SET prefix = ExtractCharacter(movieid);
    
		SET movieid = CONCAT(prefix, CAST(num + 1 AS CHAR(10))); #Cast new id number into string and concat with "nm"
        
        INSERT IGNORE INTO movies (id, title, year, director)
        VALUES(movieid, movie_title, movie_year, movie_director);
        
        CALL add_rating_to_movie(movieid, 0.0, 0);
        
        SET created_movie = 1;
	ELSE
		SELECT id into movieid FROM movies WHERE title = movie_title AND year = movie_year AND director = movie_director;
        SET created_movie = 0;
    END IF;
    
    IF star_name IS NOT NULL THEN
		SELECT COUNT(*) into star_counter from stars where name = star_name;
		IF star_counter = 0 THEN
			CALL add_star(star_name, null);
			SELECT MAX(id) into star_id FROM stars WHERE name = star_name LIMIT 1;
			CALL link_star_to_movie(star_id, movieid, linked_star_to_movie);
		ELSE
			SELECT MAX(id) into star_id FROM stars WHERE name = star_name LIMIT 1;
			CALL link_star_to_movie(star_id, movieid, linked_star_to_movie);
		END IF;
	END IF;
	
    IF genre IS NOT NULL THEN
		SELECT COUNT(*) into genre_counter FROM genres where name = genre;
		IF genre_counter = 0 THEN
			CALL add_genre(genre);
			SELECT id into genre_id FROM genres WHERE name = genre LIMIT 1;
			CALL link_genre_to_movie(genre_id, movieid, linked_genre_to_movie);
		ELSE
			SELECT id into genre_id FROM genres WHERE name = genre LIMIT 1;
			CALL link_genre_to_movie(genre_id, movieid, linked_genre_to_movie);
		END IF;
    END IF;
END//

#CALL add_movie("TEST MOVIE1000", 2019, "Justin Huynh", "Justin Huynh", "Memology", @created);
#CALL add_movie("TEST MOVIE1000", 2019, "Justin Huynh", "Justin Huynh2", "Memology", @created);
#CALL add_movie("TEST MOVIE1000", 2019, "JHuynh", "Justin Huynh", "Memology", @created);
#CALL add_movie("TEST MOVIE1000", 2019, "JHuynhing", "Justin Huynh", "nerdology", @created);
#CALL add_movie("TEST MOVIE1000", 2019, "Justin test", "Justin Huynh", "Memology", @created);

#SELECT m.title, m.year, m.director, g.name as genre, s.name as star_name, gm.genreId, gm.movieId, sm.starId FROM movies m INNER JOIN genres_in_movies gm INNER JOIN genres g INNER JOIN stars_in_movies sm INNER JOIN stars s
#ON m.id = gm.movieId AND gm.genreId = g.id AND m.id = sm.movieId AND sm.starId = s.id
#WHERE m.title = "TEST MOVIE1000" AND m.year = 2019


DROP PROCEDURE IF EXISTS add_movie_no_out_params;
CREATE PROCEDURE add_movie_no_out_params(IN movie_id VARCHAR(10), IN movie_title VARCHAR(100), IN movie_year INTEGER, IN movie_director VARCHAR(100), IN genre VARCHAR(32))
BEGIN
	DECLARE counter INTEGER;
    
    DECLARE movieid VARCHAR(10);#new movie id for movie
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INTEGER;
    
    DECLARE star_counter INTEGER;
    DECLARE genre_counter INTEGER;
    
    DECLARE linked_star_to_movie INTEGER;
    DECLARE linked_genre_to_movie INTEGER;
    
    
	SET movieid = movie_id;
    SELECT COUNT(*) into counter from movies WHERE title = movie_title AND year = movie_year AND director = movie_director AND id = movieid;
    
    IF counter = 0 THEN
        INSERT IGNORE INTO movies (id, title, year, director)
        VALUES(movieid, movie_title, movie_year, movie_director);
        CALL add_rating_to_movie(movieid, 0.0, 0);
    END IF;
	
    IF genre IS NOT NULL THEN
		SELECT COUNT(*) into genre_counter FROM genres where name = genre;
		IF genre_counter = 0 THEN
			CALL add_genre(genre);
			SELECT id into genre_id FROM genres WHERE name = genre LIMIT 1;
			CALL link_genre_to_movie(genre_id, movieid, linked_genre_to_movie);
		ELSE
			SELECT id into genre_id FROM genres WHERE name = genre LIMIT 1;
			CALL link_genre_to_movie(genre_id, movieid, linked_genre_to_movie);
		END IF;
    END IF;
END//

#CALL add_movie_no_out_params("123456", "TEST MOVIE1000", 2019, "Justin test", "JDawg", null);

DROP PROCEDURE IF EXISTS link_star_to_movie_no_out_params;
CREATE PROCEDURE link_star_to_movie_no_out_params(IN star_name VARCHAR(100), in movie_id VARCHAR(10))
BEGIN
    DECLARE star_id VARCHAR(10);
    
	CALL add_star(star_name, null);
    SELECT MAX(id) into star_id FROM stars WHERE name = star_name;
	
	INSERT IGNORE INTO stars_in_movies (starId, movieId)
	VALUES(star_id, movie_id);
END//

#CALL link_star_to_movie_no_out_params("JDude", "123456");