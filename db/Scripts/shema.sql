CREATE TABLE IF NOT EXISTS post(
    id serial primary key,
    name varchar(250),
    link text,
    text text,
    created timestamp,
    UNIQUE(link)
);