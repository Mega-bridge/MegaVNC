-- Table: public.users

-- DROP TABLE IF EXISTS public.users;

CREATE TABLE IF NOT EXISTS public.users
(
    created_at timestamp(6) without time zone,
    id bigint NOT NULL,
    password character varying(255) COLLATE pg_catalog."default",
    username character varying(255) COLLATE pg_catalog."default",
    roles character varying(255)[] COLLATE pg_catalog."default",
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT users_username_key UNIQUE (username)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.users
    OWNER to megavncuser;

-- Table: public.remote_pc

-- DROP TABLE IF EXISTS public.remote_pc;

CREATE TABLE IF NOT EXISTS public.remote_pc
(
    status smallint,
    created_at timestamp(6) without time zone,
    id bigint NOT NULL,
    owner_id bigint,
    repeater_id bigint,
    name character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT remote_pc_pkey PRIMARY KEY (id),
    CONSTRAINT remote_pc_repeater_id_key UNIQUE (repeater_id),
    CONSTRAINT fk4eyifhukd8vdjrjtta8tidlu FOREIGN KEY (owner_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT remote_pc_status_check CHECK (status >= 0 AND status <= 2)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.remote_pc
    OWNER to megavncuser;