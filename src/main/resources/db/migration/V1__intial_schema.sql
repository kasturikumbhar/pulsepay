Create table system_metadata(
    id bigint generated always as identity primary key,
    componenet_name varchar(100) not null,
    created_at timestamp not null default current_timestamp);

Insert into system_metadata(componenet_name) values('pulsepay-core');