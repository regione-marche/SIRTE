UPDATE CONF SET CONF_TXT = 'Vers. 17.01' WHERE CONF_KPROC = 'SINS' AND CONF_KEY = 'VERSIONE';

-- Inserimento dell'operatore palliativista SPAL2 nella tabella OPERATORI
SET DEFINE OFF;
Insert into OPERATORI (JDBINTERF_VERSION,JDBINTERF_LASTCNG,CODICE,COGNOME,NOME,INDIRIZZO,CITTA,CAP,TELEFONO1,TELEFONO2,TIPO,COD_QUALIF,COD_ZONA,COD_PRESIDIO,COD_SERVIZIO,COD_MAGAZZINO,COD_FIGURA_PROF,COD_FISCALE,DATA_INIZIO,DATA_FINE,DIPEND_CONV,DISTRETTO_SM,PRESIDIO_SM,UNITA_FUNZ,COD_SUBZONA,SETTORE,COD_OSPEDALE,COD_REPARTO,AREADIS,MAGPROT,PRESIDIO_PAS,AREA_ASS,COD_CENTROSERV,COMUNE_RES,COD_MEDICO,FLG_ABIL_RICOVERO) values ('1',to_date('24-APR-2012','DD-MON-yyyy'),'SPAL2','MEDXXX PALLIATIV Zona2','TESXXXXXX',null,null,null,null,null,'52','M03','2','100600','AI',null,null,'XXXXXX',null,null,'D',null,null,null,null,'1',null,null,null,null,null,null,null,null,null,null);

-- Inserimento della colonna COD_OPERATORE nella tabella SC_STAS
ALTER TABLE SC_STAS ADD (COD_OPERATORE CHAR(10 BYTE));

-- Inserimento della colonna COD_OPERATORE nella tabella SKMPAL_HOSPICE
ALTER TABLE SKMPAL_HOSPICE ADD (COD_OPERATORE CHAR(10 BYTE));

-- Inserimento di diverse colonne nella tabella SC_PRESIDI_SAN per il contatto palliativista (dove la tabella è già presente e va solo modificata)
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_SONDINO CHAR(1 BYTE));
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_SONDINO_DATA DATE);
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_DIGIUNOSTOMIA CHAR(1 BYTE));
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_DIGIUNOSTOMIA_DATA DATE);
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_GASTROSTOMIA CHAR(1 BYTE));
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_GASTROSTOMIA_DATA DATE);
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_COLOSTOMIA CHAR(1 BYTE));
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_COLOSTOMIA_DATA DATE);
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_URETERO CHAR(1 BYTE));
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_URETERO_DATA DATE);
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_CAT_SPINALE CHAR(1 BYTE));
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_CAT_SPINALE_DATA DATE);
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_CAT_PERIF CHAR(1 BYTE));
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_CAT_PERIF_DATA DATE);
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_AGO CHAR(1 BYTE));
ALTER TABLE SC_PRESIDI_SAN ADD (SKP_AGO_DATA DATE);


-- Inserimento in TBL_TAB_VOCI della combobox relativa al campo CAT_CVC della tabella SC_PRESIDI_SAN
REM INSERTING into TBL_TAB_VOCI
SET DEFINE OFF;
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','#','Tipo di catetere venoso centrale (cvc)',null,null,null,null,null,'DE');
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','G','Altro',null,null,null,null,null,'DE');
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','F','Catetere venoso centrale (CVC) non valvolato',null,null,null,null,null,'DE');
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','E','Catetere venoso centrale (CVC) valvolato',null,null,null,null,null,'DE');
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','D','Esterno tunnelizzato',null,null,null,null,null,'DE');
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','C','Groschon',null,null,null,null,null,'DE');
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','B','PORT',null,null,null,null,null,'DE');
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','A','PICC',null,null,null,null,null,'DE');
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','F','Catetere venoso centrale (CVC) non valvolato',null,null,null,null,null,'IT');
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','E','Catetere venoso centrale (CVC) valvolato',null,null,null,null,null,'IT');
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','D','Esterno tunnelizzato',null,null,null,null,null,'IT');
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','C','Groschon',null,null,null,null,null,'IT');
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','B','PORT',null,null,null,null,null,'IT');
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','G','Altro',null,null,null,null,null,'IT');
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','#','Tipo di catetere venoso centrale (cvc)',null,null,null,null,null,'IT');
Insert into TBL_TAB_VOCI (TAB_COD,TAB_VAL,TAB_DESCRIZIONE,TAB_FISS,TAB_NUMERO,TAB_CODREG,JDBINTERF_LASTCNG,JDBINTERF_VERSION,ASTER_LANG) values ('CATVENTI','A','PICC',null,null,null,null,null,'IT');


REM INSERTING into ISAS_PROCPROF
SET DEFINE OFF;
Insert into ISAS_PROCPROF (ISAS_PID,ISAS_PROFID,ISAS_KFUNC,ISAS_KSERV,JDBINTERF_VERSION,JDBINTERF_LASTCNG) values ('18','250','SCPRESAN','CONS','1',null);
Insert into ISAS_PROCPROF (ISAS_PID,ISAS_PROFID,ISAS_KFUNC,ISAS_KSERV,JDBINTERF_VERSION,JDBINTERF_LASTCNG) values ('18','250','SCPRESAN','MODI','1',null);
Insert into ISAS_PROCPROF (ISAS_PID,ISAS_PROFID,ISAS_KFUNC,ISAS_KSERV,JDBINTERF_VERSION,JDBINTERF_LASTCNG) values ('18','250','SCPRESAN','CANC','1',null);
Insert into ISAS_PROCPROF (ISAS_PID,ISAS_PROFID,ISAS_KFUNC,ISAS_KSERV,JDBINTERF_VERSION,JDBINTERF_LASTCNG) values ('18','250','SCPRESAN','INSE','1',null);

Insert into ISAS_PROCPROF (ISAS_PID,ISAS_PROFID,ISAS_KFUNC,ISAS_KSERV,JDBINTERF_VERSION,JDBINTERF_LASTCNG) values ('18','100','SCPRESAN','CONS','1',null);
Insert into ISAS_PROCPROF (ISAS_PID,ISAS_PROFID,ISAS_KFUNC,ISAS_KSERV,JDBINTERF_VERSION,JDBINTERF_LASTCNG) values ('18','100','SCPRESAN','MODI','1',null);
Insert into ISAS_PROCPROF (ISAS_PID,ISAS_PROFID,ISAS_KFUNC,ISAS_KSERV,JDBINTERF_VERSION,JDBINTERF_LASTCNG) values ('18','100','SCPRESAN','CANC','1',null);
Insert into ISAS_PROCPROF (ISAS_PID,ISAS_PROFID,ISAS_KFUNC,ISAS_KSERV,JDBINTERF_VERSION,JDBINTERF_LASTCNG) values ('18','100','SCPRESAN','INSE','1',null);

-- inserimento schede di valutazione
REM INSERTING into TAB_SCHEDE
SET DEFINE OFF;
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('82','Scheda Broncopneumologia','sc_bronco',null,'bronco_data','bronco_nome','BroncopneumologiaFormCtrl','N','S',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('81','Scheda dolore','sc_dolore','(null)','dolore_data','dolore_nome','DoloreFormCtrl','N','S',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('80','Malnutrition Universal Screening Tools (MUST)','sc_must','must_punteggio','must_data','must_nome','MustFormCtrl','S','S',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('79','Glasgow Coma Scale','sc_coma','coma_punteggio','coma_data','coma_nome','GlasgowFormCtrl','S','S',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('78','Scheda sedazione','sc_sedaz',null,'sedaz_data','sedaz_nome','SedazioneFormCtrl','N','S',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('77','Support Team Assessment Schedule (STAS)','sc_stas',null,'stas_data','stas_nome','STASFormCtrl','N','S',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('76','Hospice','skmpal_hospice','hosp_totale','hosp_data','hosp_nome','HospiceFormCtrl','S','S',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('75','Therapy Impact Questionnaire (T.I.Q.)','sc_tiq','tiq_totale','tiq_data','tiq_nome','TIQFormCtrl','S','S',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('74','PaP Score','sc_pap','pap_score_totale','pap_data','pap_nome','PaPScoreFormCtrl','S','S',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('73','Intensità percepita del dolore (N.R.S.)','sc_nrs',null,'data','nrs_nome','NRSFormCtrl','N','S',null,null);

Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('73','52',null,null);
Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('74','52',null,null);
Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('75','52',null,null);
Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('76','52',null,null);
Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('77','52',null,null);
Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('78','52',null,null);
Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('79','52',null,null);
Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('80','52',null,null);
Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('81','52',null,null);
Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values ('82','52',null,null);

   
-- nuova tabella skmpal_relcli_new per la relazione clinica sul modello del diario.
CREATE TABLE SKMPAL_RELCLI_NEW 
   (	"N_CARTELLA" NUMBER(13,0) NOT NULL ENABLE, 
	"N_CONTATTO" NUMBER(13,0) NOT NULL ENABLE, 
	"TIPO_OPERATORE" CHAR(2 BYTE) NOT NULL ENABLE, 
	"PROGR_INSE" NUMBER(13,0) NOT NULL ENABLE, 
	"PROGR_MODI" NUMBER(13,0) NOT NULL ENABLE, 
	"OP_INSE" CHAR(10 BYTE), 
	"DATA_INSE" DATE, 
	"ORA_INSE" CHAR(5 BYTE), 
	"OP_MODI" CHAR(10 BYTE), 
	"DATA_MODI" DATE, 
	"ORA_MODI" CHAR(5 BYTE), 
	"DATA_RELCLI" DATE, 
	"OGGETTO" VARCHAR2(1000 BYTE), 
	"TESTO" CLOB, 
	"INFO_PRIVATA" CHAR(1 BYTE), 
	"ID_SKSO" NUMBER(13,0), 
	"JISAS_LASTUID" NUMBER(13,0), 
	 PRIMARY KEY ("N_CARTELLA", "N_CONTATTO", "TIPO_OPERATORE", "PROGR_INSE", "PROGR_MODI")
	 );
	 
	 
-- nuova tabella nucleo_familiare_generico
CREATE TABLE NUCLEO_FAMILIARE_GENERICO 
   (	"TIPO_APPLICAZIONE" NUMBER(13,0) NOT NULL ENABLE, 
	"TIPO_MODULO" NUMBER(13,0) NOT NULL ENABLE, 
	"PROGRESSIVO_MODULO" NUMBER(13,0) NOT NULL ENABLE, 
	"N_CARTELLA" NUMBER(13,0) NOT NULL ENABLE, 
	"N_CONTATTO" NUMBER(13,0) NOT NULL ENABLE, 
	"PROGRESSIVO" NUMBER(10,0) NOT NULL ENABLE, 
	"N_CARTELLA_PAR" NUMBER(13,0), 
	"COGNOME" VARCHAR2(40 CHAR), 
	"NOME" VARCHAR2(40 CHAR), 
	"COD_USL" VARCHAR2(32 CHAR), 
	"GRADO_PARENTELA" VARCHAR2(8 CHAR), 
	"GRADO_PARENTELA_ALTRO" VARCHAR2(30 CHAR), 
	"N_CONTATTO_PAR" NUMBER(13,0), 
	"DATA_NS" DATE, 
	"SESSO" CHAR(1 BYTE), 
	"PROFESSIONE" CHAR(1 BYTE), 
	"GEST_TERAPIA" CHAR(1 BYTE), 
	"NUMTEL1" VARCHAR2(101 BYTE), 
	 PRIMARY KEY ("TIPO_APPLICAZIONE", "TIPO_MODULO", "PROGRESSIVO_MODULO", "N_CARTELLA", "N_CONTATTO", "PROGRESSIVO"));
	 
	 
-- nuova tabella 
CREATE TABLE SKMPAL_TERAPIE_NEW 
   ("N_CARTELLA" NUMBER(13,0), 
	"N_CONTATTO" NUMBER(13,0), 
	"ID_TERAPIA" NUMBER(13,0), 
	"COD_OPERATORE" CHAR(10 BYTE), 
	"MECODI" CHAR(16 BYTE), 
	"SF_CODICE" CHAR(10 BYTE), 
	"DATA_INIZIO" DATE, 
	"DATA_FINE" DATE, 
	"FREQUENZA_GG" NUMBER(13,0), 
	"SOMMINISTRAZIONE" NUMBER(13,0), 
	"POSOLOGIA" VARCHAR2(1000 BYTE), 
	"MODALITA" VARCHAR2(2000 BYTE), 
	"NOTE" VARCHAR2(2000 BYTE), 
	"ORARI" VARCHAR2(1000 BYTE), 
	"FREQUENZA" CHAR(4 BYTE), 
	 PRIMARY KEY ("N_CARTELLA", "N_CONTATTO", "ID_TERAPIA"));

SET DEFINE ON;  
DEFINE n_max_schede = "(SELECT (MAX(id_scheda)+1) FROM tab_schede)";

-- Script aggiornato per l'inserimento della scheda S.Va.M.A. in tab_schede e tab_schede_cont. Eseguire solo se la scheda suddetta non era stata già inserita nella tabella.
Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'DOW',null,null);
-- Inserimento scheda S.Va.M.A. in TAB_SCHEDE
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'S.Va.M.A.','svama_soc','uod_srs_totale','soc_data_valutaz','NOME','ValutazioneSocialeFormCtrl','S','S',null,null);

-- Script aggiornato per l'inserimento della scheda Pfeiffer in tab_schede e tab_schede_cont. Eseguire solo se la scheda suddetta non era stata già inserita nella tabella.
Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'DOW',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'Test di Pfeiffer','sc_pfeiffer','pfeiffer_punt','pfeiffer_data','pfeiffer_nome','PfeifferFormCtrl','S','S',null,null);


-- flussi siad su DB
ALTER TABLE FLUSSI_SIAD_ELAB ADD ZIP_FILE        BLOB;
ALTER TABLE FLUSSI_SIAD_ELAB ADD TIPO_ESTRAZIONE NUMBER(1);
ALTER TABLE FLUSSI_SIAD_ELAB ADD time_elab NUMBER(13);
ALTER TABLE flussi_siad_elab DROP COLUMN filename;



comment on column FLUSSI_SIAD_ELAB.ESITO
  is '0 = elaborazione completata con successo, 1 = errore durante l''elaborazione';
comment on column FLUSSI_SIAD_ELAB.TIPO_ESTRAZIONE
  is '0=ESTRAZIONE EFFETTIVA, 1=ESTRAZIONE DI PROVA';
	
	
create table FLUSSI_SIAD_DB AS SELECT mese, 
anno, 
progr, 
data_estrazione, 
cod_operatore, 
progr ticket_elab, 
zona, 
convalida, 
distretto
FROM flussi_siad
WHERE tipo = 1;


COMMIT;

alter table FLUSSI_SIAD_DB
  add primary key (MESE, ANNO, PROGR);
	
	

