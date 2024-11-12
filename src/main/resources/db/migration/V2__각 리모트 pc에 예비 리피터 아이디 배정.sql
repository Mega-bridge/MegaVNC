ALTER TABLE remote_pc
    ADD secondary_repeater_id BIGINT;

ALTER TABLE remote_pc
    ADD CONSTRAINT uc_remote_pc_secondaryrepeaterid UNIQUE (secondary_repeater_id);

