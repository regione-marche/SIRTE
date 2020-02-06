------ SEQUENZE------------
-- Create sequence 
create sequence SEQ_FLUSSI_FAR
minvalue 1
maxvalue 9999999999999
start with 1
increment by 1
NOCACHE;

-- Create sequence 
create sequence SEQ_TICKET_ELAB_FAR
minvalue 1
maxvalue 9999999999999
start with 1
increment by 1
NOCACHE;

-- Create sequence 
create sequence SEQ_FLUSSI_FILER
minvalue 1
maxvalue 9999999999999
start with 1
increment by 1
NOCACHE;

-- Create sequence 
create sequence SEQ_TICKET_ELAB_FILER
minvalue 1
maxvalue 9999999999999
start with 1
increment by 1
NOCACHE;

------ NUOVE TABELLE------------
CREATE TABLE flussi_far_db AS SELECT * FROM flussi_siad_db WHERE 1=2;
CREATE TABLE flussi_far_elab AS select * FROM flussi_siad_elab WHERE 1=2;
CREATE TABLE flussi_filer_db AS SELECT * FROM flussi_siad_db WHERE 1=2;
CREATE TABLE flussi_filer_elab AS select * FROM flussi_siad_elab WHERE 1=2;

ALTER TABLE flussi_filer_db DROP COLUMN mese;
ALTER TABLE flussi_filer_db ADD PRIMARY KEY (anno,progr);

------ ALTER TABLE------------

-- RUGVAL
alter table RUGVAL rename column FAR1 to PUNT_SOCIALE;
alter table RUGVAL rename column FAR2 to PUNT_FINANZIARIO;
alter table RUGVAL ADD tratt_spec_SngPeg NUMBER(1);
alter table RUGVAL ADD tratt_spec_Dialisi NUMBER(1);
alter table RUGVAL ADD tratt_spec_AlimParenterale NUMBER(1);
alter table RUGVAL ADD tratt_spec_AltriTrattamenti NUMBER(1);
alter table RUGVAL ADD tratt_spec_Ulcere NUMBER(1);
alter table RUGVAL ADD tratt_spec_Ossigenoterapia NUMBER(1);
alter table RUGVAL ADD tratt_spec_Respiratorie NUMBER(1);
alter table RUGVAL ADD tratt_spec_Tracheostomia NUMBER(1);
alter table RUGVAL ADD punt_comportamentale NUMBER(1);
ALTER TABLE RUGVAL ADD flag_sent NUMBER(1);
ALTER TABLE RUGVAL ADD progr_sent NUMBER(13);
ALTER TABLE RUGVAL ADD data_sent DATE;
ALTER TABLE RUGVAL ADD id_skso NUMBER(13);
ALTER TABLE rugval ADD valutazione NUMBER(1) ;


-- SCL_VALUTAZIONE
ALTER TABLE scl_valutazione ADD TRATT_SPEC_ALTRITRATTAMENTI NUMBER(1);
ALTER TABLE scl_valutazione RENAME COLUMN flag_sended TO flag_sent;
ALTER TABLE scl_valutazione RENAME COLUMN data_sended TO data_sent;
ALTER TABLE scl_valutazione ADD valutazione NUMBER(1);
ALTER TABLE scl_valutazione ADD id_skso NUMBER(13);

-- SC_BISOGNI
ALTER TABLE sc_bisogni ADD TRATT_SPEC_ALTRITRATTAMENTI NUMBER (1);

-- ZK_RSA_RICOVERI
ALTER TABLE zk_rsa_ricoveri add file_r_id number(13);
ALTER TABLE zk_rsa_ricoveri ADD flag_filer_sent number(1);
ALTER TABLE zk_rsa_ricoveri ADD progr_filer_sent number(13);
ALTER TABLE zk_rsa_ricoveri ADD data_filer_sent DATE;
