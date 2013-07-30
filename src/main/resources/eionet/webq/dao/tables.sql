CREATE TABLE IF NOT EXISTS USER_XML(
id identity primary key,
session_id varchar2(255),
fileName varchar2(255),
xml_schema varchar2(255),
xml blob,
file_size_in_bytes bigint,
created datetime default current_timestamp,
updated datetime default current_timestamp);