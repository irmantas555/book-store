# Book store project 
This is Java backend REST project for storing Books, Antique Books and Science journals  
Project uses in memory H2 database, some data stored by default(16 books to all 3 repositories) 
Server starting on http://localhost:8080, H2 database console endpoint http://localhost:8081, H2 console JDBC URL: jdbc:h2:mem:testdb  
## Api endpoints

Books:  
/books  

Antique books:  
/books/antique  

Science journals:  
/science/journals  

Following api endpoints is exposed for each fo book types (e.g /science/journals + /12 as id):  
GET /                       -all entities (eg /books/ or /books/antique/)  
GET /{id}                   -entity with id  
GET /barcode/{value}        -entity with barcode value   
GET /barcode/match/{value}  -all entities with a barcode matching value  
DELETE /{id}                -delete entity with id  
POST /                      -add new entity  
PUT /{id}                   -update entity with id  
PUT /barcode/{barcodeValue}/{field}/{fieldValue} - update entity with barcodeValue set field new value fieldValue   
(fields available: name, author, barcode, quantity, price for all book types , additionally scienceIndex for journals or releaseYear for antique books)  

There is possibility to calculate total price for all types of books for a barcode partial match:  
GET /totals/price/matching/barcode/{value}  
