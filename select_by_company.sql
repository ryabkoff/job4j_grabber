CREATE TABLE company(id integer NOT NULL,
    name character varying,
    CONSTRAINT company_pkey PRIMARY KEY (id));

CREATE TABLE person(id integer NOT NULL,
    name character varying,
    company_id integer references company(id),
    CONSTRAINT person_pkey PRIMARY KEY (id));

INSERT INTO company(id, name) VALUES(1, 'Гугл');
INSERT INTO company(id, name) VALUES(2, 'Яндекс');
INSERT INTO company(id, name) VALUES(5, 'Мэйл.ру');

INSERT INTO person(id, name, company_id) VALUES(1, 'Иванов И.И.', 1);
INSERT INTO person(id, name, company_id) VALUES(2, 'Петров П.П.', 2);
INSERT INTO person(id, name, company_id) VALUES(3, 'Сидоров С.С.', 5);
INSERT INTO person(id, name, company_id) VALUES(4, 'Кузнецов К.К.', 5);
INSERT INTO person(id, name, company_id) VALUES(5, 'Медведев М.М.', 1);

SELECT person.name as name,
        company.name as company
FROM person LEFT JOIN company 
    ON person.company_id = company.id
WHERE company.id <> 5;

SELECT COUNT(id) as max_person 
FROM person 
GROUP BY company_id 
ORDER BY max_person DESC 
LIMIT 1;

SELECT * FROM (SELECT company.name AS company_name, COUNT(person.id) AS count_person 
                    FROM company LEFT JOIN person 
                        ON company.id = person.company_id 
                    GROUP BY company.id) AS counter
                    WHERE count_person = (SELECT COUNT(id) as max_count 
                                            FROM person 
                                            GROUP BY company_id 
                                            ORDER BY max_count DESC 
                                            LIMIT 1);