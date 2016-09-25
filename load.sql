SET SERVEROUTPUT ON

DROP TRIGGER tr_emp_num;
DROP TRIGGER trg_anim;
DROP TRIGGER trg_billing;

DROP TABLE prescribed_in cascade constraints;
DROP TABLE medicine cascade constraints;
DROP TABLE prescription cascade constraints;
DROP TABLE consultation cascade constraints;
DROP TABLE animal cascade constraints;
DROP TABLE owner cascade constraints;
DROP TABLE employees cascade constraints;
DROP TABLE clinic cascade constraints;
DROP TABLE city_pc cascade constraints;

CREATE TABLE city_pc
(city   VARCHAR2(30) NOT NULL,
postcode  VARCHAR2(4) NOT NULL PRIMARY KEY);

CREATE TABLE clinic
(name           VARCHAR2(30)  NOT NULL  PRIMARY KEY,
st_num          VARCHAR2(5)   NOT NULL,
st_name         VARCHAR2(30)  NOT NULL,
suburb          VARCHAR2(30)  NOT NULL,
postcode        VARCHAR2(4)   NOT NULL,
phone_num       VARCHAR2(10)  NOT NULL,
fax_num         VARCHAR2(20),
no_of_employees INT NOT NULL,
mgr_ird         VARCHAR(10)   NOT NULL,
start_date      DATE          NOT NULL);

CREATE TABLE employees
(ird            VARCHAR2(10)   NOT NULL PRIMARY KEY,
f_name          VARCHAR2(30)  NOT NULL,
m_init          CHAR,
l_name          VARCHAR2(30)  NOT NULL,
st_num          VARCHAR2(5)   NOT NULL,
st_name         VARCHAR2(30)  NOT NULL,
suburb          VARCHAR2(30)  NOT NULL,
postcode        VARCHAR2(4)   NOT NULL,
position        VARCHAR(13) CHECK(position IN ('vet','vet nurse','administrator')) NOT NULL,
vet_ird         VARCHAR2(10),
salary          FLOAT         NOT NULL,
clinic_name VARCHAR2(30)      NOT NULL,
CONSTRAINT FK_asn_vet_ird       FOREIGN KEY (vet_ird)   REFERENCES employees(ird) ENABLE,
CONSTRAINT FK_clinic_name FOREIGN KEY (clinic_name) REFERENCES clinic(name) ENABLE);

CREATE TABLE owner
(owner_id       VARCHAR(10)  NOT NULL PRIMARY KEY,
f_name          VARCHAR2(30) NOT NULL,
m_init          VARCHAR2(3),
l_name          VARCHAR2(30) NOT NULL,
date_of_birth   DATE         NOT NULL,
st_num          VARCHAR2(5)  NOT NULL,
st_name         VARCHAR2(30) NOT NULL,
suburb          VARCHAR2(30) NOT NULL,
postcode         VARCHAR2(4) NOT NULL,
account_balance FLOAT,
phone_num       VARCHAR2(10));

CREATE TABLE animal
(animal_id VARCHAR2(20) PRIMARY KEY, --changed from 30 to 20
name    VARCHAR2(30),
sex     CHAR NOT NULL,
species VARCHAR2(30),
age     INT,
owner_id   VARCHAR(10) NOT NULL,
CONSTRAINT FK_owner_id FOREIGN KEY (owner_id) REFERENCES owner(owner_id) ENABLE);

--Combined treatment_slot, treatment_date into one atrribute
CREATE TABLE consultation
(treatment_slot   DATE NOT NULL,
vet_ird         VARCHAR2(10) NOT NULL,
animal_id       VARCHAR(20) NOT NULL, --changed from 30 to 20
owner_id        VARCHAR(20),  --added, since we might have strays I have it not as NOT NULL
description     VARCHAR(256),
billing         FLOAT,  --added
CONSTRAINT FK_animal_id FOREIGN KEY (animal_id) REFERENCES animal(animal_id) ENABLE,
CONSTRAINT FK_vet_ird   FOREIGN KEY (vet_ird)   REFERENCES employees(ird) ENABLE,
CONSTRAINT FK_payee     FOREIGN KEY (owner_id) REFERENCES owner(owner_id) ENABLE,
CONSTRAINT PK_consultation PRIMARY KEY (treatment_slot, vet_ird) ENABLE);


CREATE TABLE prescription
(prescription_num   INT NOT NULL PRIMARY KEY,
instructions            VARCHAR(256) NOT NULL,
treatment_slot          DATE NOT NULL,
vet_ird                 VARCHAR(10) NOT NULL,
CONSTRAINT FK_vet_slot FOREIGN KEY (vet_ird,treatment_slot) REFERENCES consultation(vet_ird,treatment_slot) ENABLE);

CREATE TABLE medicine
(name   VARCHAR(128) NOT NULL,
dosage VARCHAR2(30) NOT NULL,
stock   INT NOT NULL,
CONSTRAINT PK_medicine PRIMARY KEY (name, dosage) ENABLE);

CREATE TABLE prescribed_in
(quantity   INT NOT NULL,
p_num   INT NOT NULL,
name   VARCHAR2(128) NOT NULL,
dosage  VARCHAR2(30) NOT NULL,
CONSTRAINT FK_p_num FOREIGN KEY (p_num) REFERENCES prescription(prescription_num) ENABLE,
CONSTRAINT FK_medicine FOREIGN KEY (name,dosage) REFERENCES medicine(name,dosage) ENABLE,
CONSTRAINT PK_prescribed_in PRIMARY KEY (p_num,name,dosage) ENABLE);

/* TRIGGERS */

CREATE OR REPLACE TRIGGER tr_emp_num
       AFTER INSERT OR DELETE OR UPDATE ON employees
             BEGIN
                --Dunedin
                UPDATE clinic SET no_of_employees
                = (SELECT count(*) FROM employees
                WHERE clinic_name='Pet Clinic Dunedin')
                WHERE name='Pet Clinic Dunedin';

                --Christchurch
                UPDATE clinic SET no_of_employees
                = (SELECT count(*) FROM employees
                WHERE clinic_name='Pet Clinic Christchurch')
                WHERE name='Pet Clinic Christchurch';

                --Auckland
                UPDATE clinic SET no_of_employees
                = (SELECT count(*) FROM employees
                WHERE clinic_name='Pet Clinic Auckland')
                WHERE name='Pet Clinic Auckland';


              END;
/
CREATE OR REPLACE TRIGGER trg_anim
BEFORE INSERT OR UPDATE OF animal_id ON animal
FOR EACH ROW
WHEN (NEW.animal_id IS NULL)
DECLARE n_seed VARCHAR2(100);
BEGIN
n_seed := TO_CHAR(SYSTIMESTAMP,'YYYYDDMMHH24MISSFFFF');
DBMS_RANDOM.seed(val=>n_seed);
IF :NEW.animal_id IS NULL THEN
n_seed := DBMS_RANDOM.value(0,99);
n_seed := substr(n_seed,4,5);
:New.animal_id := n_seed;
END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_billing
AFTER INSERT OR UPDATE OF billing ON consultation
FOR EACH ROW
DECLARE diff FLOAT;
BEGIN
IF INSERTING THEN
UPDATE owner
SET owner.account_balance = owner.account_balance + :NEW.billing WHERE owner.owner_id = :NEW.owner_id;
ELSIF UPDATING THEN
diff := :NEW.billing - :OLD.billing;
UPDATE owner
SET owner.account_balance = owner.account_balance + diff WHERE owner.owner_id = :NEW.owner_id;
END IF;
END;
/


/* CLINICS */

INSERT INTO clinic VALUES
('Pet Clinic Dunedin', '121', 'Albany Street', 'Dunedin North', '9016', '034764344', '034764345', 4, '73-378-229', TO_DATE('21-11-2012', 'dd-mm-yyyy'));
INSERT INTO clinic VALUES
('Pet Clinic Auckland', '65', 'Leek Street', 'Newmarket', '1023', '095232257', '095232258', 4, '45-678-765', TO_DATE('15-08-2014', 'dd-mm-yyyy'));
INSERT INTO clinic VALUES
('Pet Clinic Christchurch', '212', 'Heaton Street', 'Strowan', '8052', '033356786', '033356787', 4, '46-382-759', TO_DATE('13-12-2013', 'dd-mm-yyyy'));


/* Employees for Pet Clinic Dunedin
2 vets, 2 nurses, 1 Administrator
Administrator is manager */

INSERT INTO employees VALUES
('32-325-334', 'Jane', 'F', 'Thompson', '22', 'Delta St', 'Belleknowes', '9011', 'vet', NULL, 68000, 'Pet Clinic Dunedin');

INSERT INTO employees VALUES
('52-385-949', 'Jeff', 'R', 'Trotter', '3', 'Alva St.', 'Dunedin Central', '9016', 'vet', NULL, 68000, 'Pet Clinic Dunedin');

INSERT INTO employees VALUES
('78-675-678', 'David', 'S', 'Adams', '23', 'David St.', 'Caversham', '9012', 'vet nurse', '32-325-334', 48000, 'Pet Clinic Dunedin');

INSERT INTO employees VALUES
('33-345-699', 'Eva', 'P', 'Lobb', '86', 'Drivers Rd.', 'Maori Hill', '9010', 'vet nurse', '52-385-949', 48000, 'Pet Clinic Dunedin');

INSERT INTO employees VALUES
('73-378-229', 'Rebecca', 'G', 'Johnson', '21', 'Prestwick St.', 'Maori Hill', '9010', 'administrator', NULL, 52000, 'Pet Clinic Dunedin');

/* Employees for Pet Clinic Auckland
2 vets, 2 nurses, 1 Administrator
Administrator is manager */

INSERT INTO employees VALUES
('39-655-456', 'Kerry', 'R', 'Daniels', '89', 'Gambia Pl', 'Onehunga', '1061', 'vet', NULL, 68000, 'Pet Clinic Auckland');

INSERT INTO employees VALUES
('88-855-987', 'John', 'S', 'Logan', '12', 'Trinity Pl', 'Albany', '0632', 'vet', NULL, 68000, 'Pet Clinic Auckland');

INSERT INTO employees VALUES
('76-876-458', 'Christine', 'J', 'Andrews', '23', 'Sandspit Rd', 'Shelly Park', '2014', 'vet nurse', '39-655-456', 48000, 'Pet Clinic Auckland');

INSERT INTO employees VALUES
('67-545-679', 'Graham', 'P', 'Fraser', '15', 'Crummer Rd', 'Grey Lynn', '1021', 'vet nurse', '88-855-987', 48000, 'Pet Clinic Auckland');

INSERT INTO employees VALUES
('45-678-765', 'Jason', 'G', 'Samuels', '10', 'Long Dr', 'St Heliers', '1071', 'administrator', NULL, 52000, 'Pet Clinic Auckland');


/* Employees for Pet Clinic Christchurch
2 vets, 2 nurses, 1 Administrator
Administrator is manager */

INSERT INTO employees VALUES
('34-654-993', 'Grace', 'L', 'Smith', '45', 'Southamton St', 'Sydenham', '8023', 'vet', NULL, 68000, 'Pet Clinic Christchurch');

INSERT INTO employees VALUES
('23-456-369', 'Timothy', 'S', 'Gresham', '18', 'Peterborough St', 'Christchurch Central', '8013', 'vet', NULL, 68000, 'Pet Clinic Christchurch');

INSERT INTO employees VALUES
('45-289-381', 'Linda', 'D', 'Roberts', '16', 'Memorial Ave', 'Burnside', '8053' ,'vet nurse', '34-654-993', 48000, 'Pet Clinic Christchurch');

INSERT INTO employees VALUES
('89-672-361', 'Josh', 'W', 'Black', '20', 'Steadman Rd', 'Broomfield', '8042', 'vet nurse', '23-456-369', 48000, 'Pet Clinic Christchurch');

INSERT INTO employees VALUES
('46-382-759', 'Samantha', 'R', 'Preston', '13', 'Rocking Horse Rd', 'Southshore', '8062', 'administrator', NULL, 52000, 'Pet Clinic Christchurch');

ALTER TABLE clinic ADD CONSTRAINT FK_mgr_ird FOREIGN KEY (mgr_ird) REFERENCES employees(ird) ENABLE;


/* Owners */

INSERT INTO owner VALUES
('48-389-639', 'Jenny', 'P', 'Allman', TO_DATE('28-02-1977', 'DD-MM-YYYY'), '21', 'Chambers St', 'North East Valley', '9010', 688.96, '0278294443');
INSERT INTO owner VALUES
('58-002-629', 'Devin', 'S', 'Helminiak', TO_DATE('18-07-1963', 'DD-MM-YYYY'), '34', 'Peter St', 'Caversham', '9012', 1710.34, '0229306722');
INSERT INTO owner VALUES
('98-572-119', 'Emily', NULL, 'McPherson', TO_DATE('18-12-1990', 'DD-MM-YYYY'), '16', 'Farley St', 'Kaikorai', '9010', NULL, '0253790374');
INSERT INTO owner VALUES
('21-940-007', 'Ziqi', 'N', 'Wang', TO_DATE('01-10-1944', 'DD-MM-YYYY'), '56', 'Ardmore Rd', 'Ponsonby', '1011', 2610.55, '0229903649');
INSERT INTO owner VALUES
('72-938-036', 'Nicholas', 'P', 'Bramley', TO_DATE('30-06-1982', 'DD-MM-YYYY'), '111', 'St Andrews Rd', 'Epsom', '1023', 1139.08, '0208206629');
INSERT INTO owner VALUES
('01-629-729', 'Salli', 'M', 'Remnick', TO_DATE('06-11-1939', 'DD-MM-YYYY'), '18', 'Sunnylaw Pl', 'Glen Eden', '0602', 820.62, '092274018');
INSERT INTO owner VALUES
('83-029-728', 'Nicholas', 'N', 'Bylica', TO_DATE('31-03-1989', 'DD-MM-YYYY'), '9', 'St James Ave', 'Papanui', '8053', 904.77, '0286289361');
INSERT INTO owner VALUES
('30-819-444', 'Andrea', NULL, 'Renopoulos', TO_DATE('11-05-1977', 'DD-MM-YYYY'), '3', 'Colesbury St', 'Bishopdale', '8053', 2710.78, '0295267937');
INSERT INTO owner VALUES
('17-552-027', 'Anja', 'E', 'Guarino', TO_DATE('22-09-1994', 'DD-MM-YYYY'), '20B', 'Walpole St', 'Waltham', '8023', NULL, '0250179022');

/* Animals */

INSERT INTO animal VALUES
('1234567890', 'Mrs. Fluffington III','F','Cat',5,'48-389-639');
INSERT INTO animal VALUES
('543210987654321','Herbert','O','Axolotl',2,'58-002-629');
INSERT INTO animal VALUES
('543216354654321','Murderface','M','Ferret',1,'58-002-629');
INSERT INTO animal VALUES
('1918172625','A.Kitler','M','Cat',8,'98-572-119');
INSERT INTO animal VALUES
('543210987650082','Fidopheles','M','Dog',5,'21-940-007');
INSERT INTO animal VALUES
('1568344801','Dot','F','Dog',4,'72-938-036');
INSERT INTO animal VALUES
('345110987654321','Polly','N','Parrot',2,'01-629-729');
INSERT INTO animal VALUES
('188974465632001','Nietzche','F','Cat',12,'01-629-729');
INSERT INTO animal VALUES
('180309840759874','Lovelace','M','Cat',12,'83-029-728');
INSERT INTO animal VALUES
('0098175648','B.Mouseolini','M','Mouse',2,'30-819-444');
INSERT INTO animal VALUES
('54825821654321','Raticate','F','Rat',3,'30-819-444');
INSERT INTO animal VALUES
('543444987659998','Greta','F','Turtle',4,'17-552-027');

/* Medicine */
INSERT INTO medicine VALUES
('Cat Wormer', '5kg', 50);
INSERT INTO medicine VALUES
('Dog Wormer', '10kg', 100);
INSERT INTO medicine VALUES
('Dog Wormer', '10-20kg', 100);
INSERT INTO medicine VALUES
('Morphine', '20mg', 150);
INSERT INTO medicine VALUES
('Natural Pet Skin and Itch Dog Care', '4floz', 20);
INSERT INTO medicine VALUES
('Pet Pectillin for Dogs Cats and Birds', '4oz', 20);
INSERT INTO medicine VALUES
('Nature Zone SNZ59211 Turtle Eye Drops', '2oz', 10);

DELETE medicine
WHERE rowid NOT IN (
        SELECT min(rowid)
        FROM medicine
        GROUP BY name, dosage, stock);

/*Consultations*/

INSERT INTO consultation VALUES
(TO_DATE('1520, 06/05/16', 'hh24mi dd/mm/yy'), '34-654-993',  '1234567890','48-389-639', 'cat had injured paw',380);
INSERT INTO consultation VALUES
(TO_DATE('1210, 07/05/16', 'hh24mi dd/mm/yy'), '23-456-369',  '543210987654321','58-002-629', 'near drowning',200);
INSERT INTO consultation VALUES
(TO_DATE('1300, 02/05/16', 'hh24mi dd/mm/yy'),'34-654-993',  '543216354654321','58-002-629', 'fell out of tree',120);
INSERT INTO consultation VALUES
(TO_DATE('1300, 02/05/16', 'hh24mi dd/mm/yy'),'45-289-381',  '1918172625','98-572-119', 'dental work',350);
INSERT INTO consultation VALUES
(TO_DATE('0900, 03/06/16', 'hh24mi dd/mm/yy'), '89-672-361',  '543210987650082','21-940-007', 'dog had conjunctivitis',120);
INSERT INTO consultation VALUES
(TO_DATE('0840, 26/06/16', 'hh24mi dd/mm/yy'),'46-382-759',  '1568344801','72-938-036', 'dog had thorn in foot',60);
INSERT INTO consultation VALUES
(TO_DATE('1450, 21/06/16', 'hh24mi dd/mm/yy'),'39-655-456',  '345110987654321','01-629-729', 'broken wing',300);
INSERT INTO consultation VALUES
(TO_DATE('1440, 18/06/16', 'hh24mi dd/mm/yy'), '88-855-987',  '188974465632001','01-629-729', 'kidney failure',10000);
INSERT INTO consultation VALUES
(TO_DATE('1200, 06/07/16', 'hh24mi dd/mm/yy'),'32-325-334',  '180309840759874','83-029-728', 'hit by car, bruising',500);
INSERT INTO consultation VALUES
(TO_DATE('1300, 15/07/16', 'hh24mi dd/mm/yy'),'52-385-949',  '0098175648','30-819-444', 'routine checkup',60);
INSERT INTO consultation VALUES
(TO_DATE('1135, 17/07/16', 'hh24mi dd/mm/yy'),'78-675-678',  '54825821654321','30-819-444', 'broken squeaker',200);
INSERT INTO consultation VALUES
(TO_DATE('1625, 05/08/16', 'hh24mi dd/mm/yy'),'45-678-765',  '543444987659998','17-552-027', 'shell needed cleaning',100);

DELETE consultation
WHERE rowid NOT IN (
        SELECT min(rowid)
        FROM consultation
        GROUP BY treatment_slot, vet_ird, animal_id, description);

/*Prescription*/
INSERT INTO prescription VALUES
(55456, 'Take twice daily', TO_DATE('1520, 06/05/16', 'hh24mi dd/mm/yy'), '34-654-993');
INSERT INTO prescription VALUES
(78913, 'Take once every two days', TO_DATE('1210, 07/05/16', 'hh24mi dd/mm/yy'), '23-456-369');
INSERT INTO prescription VALUES
(31267, 'Take Four times daily', TO_DATE('1300, 02/05/16', 'hh24mi dd/mm/yy') ,'34-654-993');
INSERT INTO prescription VALUES
(55342,  'Apply once orally', TO_DATE('1300, 02/05/16', 'hh24mi dd/mm/yy'), '45-289-381');
INSERT INTO prescription VALUES
(62242, 'Take once daily', TO_DATE('0900, 03/06/16', 'hh24mi dd/mm/yy'), '89-672-361');
INSERT INTO prescription VALUES
(46423, 'Take three times daily', TO_DATE('0840, 26/06/16', 'hh24mi dd/mm/yy'), '46-382-759');
INSERT INTO prescription VALUES
(71435, 'Take twice daily', TO_DATE('1450, 21/06/16', 'hh24mi dd/mm/yy'), '39-655-456');
INSERT INTO prescription VALUES
(77443, 'Apply daily', TO_DATE('1440, 18/06/16', 'hh24mi dd/mm/yy'), '88-855-987');
INSERT INTO prescription VALUES
(53422, 'Take once daily with food', TO_DATE('1200, 06/07/16', 'hh24mi dd/mm/yy'), '32-325-334');
INSERT INTO prescription VALUES
(46554, 'Take twice daily', TO_DATE('1300, 15/07/16', 'hh24mi dd/mm/yy'), '52-385-949');
INSERT INTO prescription VALUES
(48242, 'Take once daily', TO_DATE('1135, 17/07/16', 'hh24mi dd/mm/yy'), '78-675-678');
INSERT INTO prescription VALUES
(51214, 'Take three times daily', TO_DATE('1625, 05/08/16', 'hh24mi dd/mm/yy'), '45-678-765');

DELETE prescription
WHERE rowid NOT IN (
        SELECT min(rowid)
        FROM prescription
        GROUP BY prescription_num, instructions, treatment_slot, vet_ird);

/*Prescribed In*/
INSERT INTO prescribed_in VALUES
(20, 55456, 'Pet Pectillin for Dogs Cats and Birds', '4oz');
INSERT INTO prescribed_in VALUES
(8, 78913, 'Morphine', '20mg');
INSERT INTO prescribed_in VALUES
(7, 31267, 'Morphine', '20mg');
INSERT INTO prescribed_in VALUES
(7, 55342, 'Morphine', '20mg');
INSERT INTO prescribed_in VALUES
(14, 62242, 'Nature Zone SNZ59211 Turtle Eye Drops', '2oz');
INSERT INTO prescribed_in VALUES
(14, 46423, 'Pet Pectillin for Dogs Cats and Birds', '4oz');
INSERT INTO prescribed_in VALUES
(7, 71435, 'Morphine', '20mg');
INSERT INTO prescribed_in VALUES
(8, 77443, 'Natural Pet Skin and Itch Dog Care', '4floz');
INSERT INTO prescribed_in VALUES
(7, 53422, 'Morphine', '20mg');
INSERT INTO prescribed_in VALUES
(15, 46554, 'Morphine', '20mg');
INSERT INTO prescribed_in VALUES
(9, 48242, 'Morphine', '20mg');
INSERT INTO prescribed_in VALUES
(14, 51214, 'Nature Zone SNZ59211 Turtle Eye Drops', '2oz');

DELETE prescribed_in
WHERE rowid NOT IN (
        SELECT min(rowid)
        FROM prescribed_in
        GROUP BY quantity, p_num, name, dosage);

/*City Postcode*/

INSERT INTO city_pc (city,postcode) VALUES
('Dunedin', '9016');
INSERT INTO city_pc VALUES
('Dunedin', '9012');
INSERT INTO city_pc VALUES
('Auckland', '1052');
INSERT INTO city_pc VALUES
('Christchurch', '8052');
INSERT INTO city_pc VALUES
('Dunedin', '9011');
INSERT INTO city_pc VALUES
('Dunedin', '9010');
INSERT INTO city_pc VALUES
('Auckland', '1061');
INSERT INTO city_pc VALUES
('Auckland', '0632');
INSERT INTO city_pc VALUES
('Auckland', '2014');
INSERT INTO city_pc VALUES
('Auckland', '1021');
INSERT INTO city_pc VALUES
('Auckland', '1071');
INSERT INTO city_pc VALUES
('Christchurch', '8011');
INSERT INTO city_pc VALUES
('Christchurch', '8013');
INSERT INTO city_pc VALUES
('Christchurch', '8042');
INSERT INTO city_pc VALUES
('Christchurch', '8062');
INSERT INTO city_pc VALUES
('Auckland', '1011');
INSERT INTO city_pc VALUES
('Auckland', '1023');
INSERT INTO city_pc VALUES
('Auckland', '0602');
INSERT INTO city_pc VALUES
('Christchurch', '8051');
INSERT INTO city_pc VALUES
('Christchurch', '8023');
INSERT INTO city_pc VALUES
('Christchurch', '8053');
