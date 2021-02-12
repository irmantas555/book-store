DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS antique_books;
DROP TABLE IF EXISTS science_journals;
CREATE TABLE books (id bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
                          name varchar(40) DEFAULT '',
                          author varchar(40) NOT NULL DEFAULT '',
                          barcode varchar(13) NOT NULL,
                          qty int(11) DEFAULT 0, 
                          price decimal(15,2) NOT NULL DEFAULT 0.00);
CREATE TABLE science_journals (id bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        name varchar(40) DEFAULT '',
                        author varchar(40) NOT NULL DEFAULT '',
                        barcode varchar(13) NOT NULL,
                        qty int(11) DEFAULT 0,
                        price decimal(15,2) NOT NULL DEFAULT 0.00,
                        science_index int(11) DEFAULT 0);
CREATE TABLE antique_books (id bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        name varchar(40) DEFAULT '',
                        author varchar(40) NOT NULL DEFAULT '',
                        barcode varchar(13) NOT NULL,
                        qty int(11) DEFAULT 0,
                        price decimal(15,2) NOT NULL DEFAULT 0.00,
                        release_year int(11) DEFAULT 0);
CREATE INDEX barcode ON books(barcode);
CREATE INDEX barcode_a ON antique_books(barcode);
CREATE INDEX barcode_s ON science_journals(barcode);