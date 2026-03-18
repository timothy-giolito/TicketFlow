BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS biglietto (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        fila INTEGER NOT NULL,
        colonna INTEGER NOT NULL,
        prezzo_finale REAL NOT NULL,
        tipo_biglietto TEXT,
        codice_qr TEXT,
        data_emissione TIMESTAMP,
        id_spettacolo INTEGER,
        id_utente INTEGER,
        FOREIGN KEY (id_spettacolo) REFERENCES spettacolo(id),
        FOREIGN KEY (id_utente) REFERENCES utente(id)
    );
CREATE TABLE IF NOT EXISTS cinema (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        nome TEXT NOT NULL,
        indirizzo TEXT NOT NULL,
        citta TEXT NOT NULL
    );
CREATE TABLE IF NOT EXISTS film (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        titolo TEXT NOT NULL,
        descrizione TEXT,
        genere TEXT,
        durata INTEGER NOT NULL,
        url_locandina TEXT
    );
CREATE TABLE IF NOT EXISTS sala (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        nome TEXT NOT NULL,
        righe INTEGER NOT NULL,
        colonne INTEGER NOT NULL,
        capienza INTEGER,
        tipo_sala TEXT,
        id_cinema INTEGER,
        FOREIGN KEY (id_cinema) REFERENCES cinema(id) ON DELETE CASCADE
    );
CREATE TABLE IF NOT EXISTS spettacolo (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        data_ora TIMESTAMP NOT NULL,
        prezzo_base REAL NOT NULL,
        id_film INTEGER,
        id_sala INTEGER,
        url_locandina TEXT,
        FOREIGN KEY (id_film) REFERENCES film(id) ON DELETE CASCADE,
        FOREIGN KEY (id_sala) REFERENCES sala(id) ON DELETE CASCADE
    );
CREATE TABLE IF NOT EXISTS utente (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        nome TEXT NOT NULL,
        cognome TEXT NOT NULL,
        email TEXT NOT NULL UNIQUE,
        password TEXT NOT NULL,
        ruolo TEXT NOT NULL,
        eta INTEGER,
        indirizzo TEXT,
        numero_carta TEXT,
        scadenza_carta TEXT,
        cvv TEXT,
        id_cinema INTEGER
    );
INSERT INTO "biglietto" ("id","fila","colonna","prezzo_finale","tipo_biglietto","codice_qr","data_emissione","id_spettacolo","id_utente") VALUES (1,6,8,7.6,NULL,'TKT-1767968162654-6-8',1767968162654,1,1),
 (2,10,11,7.6,NULL,'TKT-1768835484718-10-11',1768835484716,2,1),
 (3,10,12,7.6,NULL,'TKT-1768836308441-10-12',1768836308441,2,1),
 (4,8,7,7.6,NULL,'TKT-1768836659852-8-7',1768836659852,2,1),
 (5,5,9,7.6,NULL,'TKT-1768836668024-5-9',1768836668024,2,1),
 (6,5,10,7.6,NULL,'TKT-1768836670061-5-10',1768836670061,2,1),
 (7,6,11,7.6,NULL,'TKT-1768836672067-6-11',1768836672067,2,1),
 (8,6,10,7.6,NULL,'TKT-1768836674073-6-10',1768836674073,2,1),
 (9,6,9,7.6,NULL,'TKT-1768836676080-6-9',1768836676080,2,1),
 (10,5,11,7.6,NULL,'TKT-1768836678086-5-11',1768836678086,2,1),
 (11,9,8,7.6,NULL,'TKT-1768836984968-9-8',1768836984968,1,1);
INSERT INTO "cinema" ("id","nome","indirizzo","citta") VALUES (1,'Test','Via Test','Default City');
INSERT INTO "film" ("id","titolo","descrizione","genere","durata","url_locandina") VALUES (1,'Test','','Test',120,NULL);
INSERT INTO "sala" ("id","nome","righe","colonne","capienza","tipo_sala","id_cinema") VALUES (1,'Sala 1',10,15,150,'Standard',1);
INSERT INTO "spettacolo" ("id","data_ora","prezzo_base","id_film","id_sala","url_locandina") VALUES (1,1767988800000,9.5,1,1,'file:/C:/Users/deads/Documents/ticketflow/TicketFlow/src/main/resources/images/placeholder_poster.png'),
 (2,1737316800000,9.5,1,1,NULL);
INSERT INTO "utente" ("id","nome","cognome","email","password","ruolo","eta","indirizzo","numero_carta","scadenza_carta","cvv","id_cinema") VALUES (1,'Prova','Test1','test1@mail.com','5bbfd6051320f90b618576d166a7208fb07c850cc0b6e0aef2924388cbedfacd','CLIENTE',18,NULL,'4948119469991629','03/30','243',NULL),
 (2,'Prova','Test2','test2@mail.com','8df59b138cd5cbed38d583ddbd9bd686399c9ef809f983001580d1c8630e78de','MANAGER',18,NULL,NULL,NULL,NULL,1),
 (3,'Prova','Test3','test3@mail.com','1c8806173096a59fc7f54253c93bcc9e0da4487f3f88ae9407c59cf570c61790','SUPER_ADMIN',18,NULL,NULL,NULL,NULL,NULL),
 (4,'test','4','test4@mail.com','af6afb77b098a4536dd3eaefc5d810be8e27089f32cc1b0379ddd067a0e4bb3c','MANAGER',25,NULL,NULL,NULL,NULL,1),
 (5,'Prova','Utente','test5@mail.com','af6afb77b098a4536dd3eaefc5d810be8e27089f32cc1b0379ddd067a0e4bb3c','MANAGER',18,NULL,NULL,NULL,NULL,1);
COMMIT;
