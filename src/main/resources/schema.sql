CREATE TABLE books (id bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY, 
                          name varchar(40) DEFAULT '',
                          author varchar(40) NOT NULL DEFAULT '',
                          barcode bigint(20) NOT NULL DEFAULT 0, 
                          qty int(11) DEFAULT 0, 
                          price decimal(15,2) NOT NULL DEFAULT 0.00);