CREATE TABLE IF NOT EXISTS post(
    id serial,
    name varchar(250),
    link text,
    text text,
    created timestamp,
    PRIMARY KEY(id, link)
);