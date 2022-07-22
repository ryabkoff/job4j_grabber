CREATE TABLE IF NOT EXISTS post(
    id serial primary key,
    name varchar(250),
    text text,
    link text,
    created timestamp 
);