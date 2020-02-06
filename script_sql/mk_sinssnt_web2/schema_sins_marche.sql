set define on;
define user_schema=&enter_user_schema_for_sins;
   
--############################### TABELLE PROBABILMENTE MANCANTI #########################################################
--RESGOM --Mariarita--
--RESPARG --Mariarita--
--CLONABILI --Mariarita--
--CO_ECONOMICI --Mariarita--
--TABBIAS --Mariarita--

     
--############################### CAMPI PROBABILMENTE MANCANTI #########################################################
--nella tabella operatori manca il campo email --Mariarita--


--############################### MODIFICHE SU TABELLE ESISTENTI #######################################################
--Per le Marche i dati della tessera sanitaria devono essere anche su FASSI
ALTER TABLE &user_schema..FASSI ADD (TEAM_NUMERO              CHAR(20));
ALTER TABLE &user_schema..FASSI ADD (TEAM_SCADENZA            DATE);
ALTER TABLE &user_schema..FASSI ADD (TEAM_PIN                 CHAR(20));
ALTER TABLE &user_schema..FASSI ADD (TEAM_NAZIONE             CHAR(2));
ALTER TABLE &user_schema..FASSI ADD (TEAM_ISTITUZIONE_NUMERO  CHAR(10));
ALTER TABLE &user_schema..FASSI ADD (TEAM_ISTITUZIONE_DESCRI  CHAR(21));

ALTER TABLE &user_schema..FASSI DROP COLUMN MEDICO_CF;

--Per le Marche il codice del medico(disponibile anche su FASSI) deve essere di 16 (usano il CF)
ALTER TABLE &user_schema..MEDICI                   MODIFY (MECODI                   CHAR(16));
ALTER TABLE &user_schema..OPERATORI                MODIFY (COD_MEDICO               CHAR(16));
ALTER TABLE &user_schema..FASSI_AGG                MODIFY (COD_MEDICO               CHAR(16));
ALTER TABLE &user_schema..FASSI                    MODIFY (COD_MEDICO               CHAR(16));
ALTER TABLE &user_schema..ANAGRA_C                 MODIFY (COD_MED                  CHAR(16));
ALTER TABLE &user_schema..SKMMG_ADI                MODIFY (SKADI_MMGPLS             CHAR(16));
ALTER TABLE &user_schema..SKMMG_ADP                MODIFY (SKADP_MMGPLS             CHAR(16));
ALTER TABLE &user_schema..SKMMG_ADR                MODIFY (SKADR_MMGPLS             CHAR(16));
ALTER TABLE &user_schema..ASS_ANAGRAFICA           MODIFY (MECODI                   CHAR(16));
ALTER TABLE &user_schema..RM_SKSO_MMG              MODIFY (COD_MED                  CHAR(16));
ALTER TABLE &user_schema..SINS_MARCHERM_SKSO_MMG   MODIFY (COD_MED                  CHAR(16));
ALTER TABLE &user_schema..RM_RICH_MMG              MODIFY (COD_MED                  CHAR(16));
ALTER TABLE &user_schema..INTMMG                   MODIFY (INT_MEDICO               CHAR(16));
ALTER TABLE &user_schema..INTMMG                   MODIFY (INT_MEDICO_TIT           CHAR(16));
ALTER TABLE &user_schema..INTEXP                   MODIFY (EXP_MEDICO_TIT           CHAR(16));
ALTER TABLE &user_schema..INTEXP                   MODIFY (EXP_MEDICO               CHAR(16));
ALTER TABLE &user_schema..INPMMG                   MODIFY (COD_MEDICO               CHAR(16));
ALTER TABLE &user_schema..F_TPIANI                 MODIFY (MED_FISC                 CHAR(16));
ALTER TABLE &user_schema..F_TPIANI1                MODIFY (MED_FISC                 CHAR(16));
ALTER TABLE &user_schema..CF_TPIANI                MODIFY (MED_FISC                 CHAR(16));
ALTER TABLE &user_schema..BASE_MEDICI              MODIFY (MECODI                   CHAR(16));
ALTER TABLE &user_schema..SKMEDPAL                 MODIFY (SKM_MMG                  CHAR(16));
ALTER TABLE &user_schema..SKFIS                    MODIFY (MECODI                   CHAR(16));
ALTER TABLE &user_schema..SKIPRESTAZ               MODIFY (SKP_MEDICO               CHAR(16));
ALTER TABLE &user_schema..P_MEDPRE                 MODIFY (MEDREP                   CHAR(16));
ALTER TABLE &user_schema..P_MEDPRE                 MODIFY (MECODI                   CHAR(16));
ALTER TABLE &user_schema..PUAUVM                   MODIFY (PR_MMG                   CHAR(16));
ALTER TABLE &user_schema..PUAUVMD                  MODIFY (PR_MMG                   CHAR(16));
ALTER TABLE &user_schema..PUAUVM_COMMISSIONE       MODIFY (PR_OPERATORE             CHAR(16));
ALTER TABLE &user_schema..RI_TAB_MEDICI            MODIFY (MEDREP                   CHAR(16));
ALTER TABLE &user_schema..RI_TAB_MEDICI            MODIFY (MECODI                   CHAR(16));
ALTER TABLE &user_schema..RI_SCHEDA_SPR            MODIFY (SPR_MEDICO_SPEC          CHAR(16));
ALTER TABLE &user_schema..RI_SCHEDA_SPR            MODIFY (SPR_COD_MEDICO           CHAR(16));
ALTER TABLE &user_schema..RI_SCHEDA_PAS            MODIFY (PAS_COD_MEDICO           CHAR(16));


--########################################## CONF ######################################################################
insert into &user_schema..CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 values
   ('SINS', 'COHESION_ABIL', 'SI', SYSDATE, 'se SI abilita l''autenticazione via Cohesion', 1, SYSDATE);

insert into &user_schema..CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 values
   ('SINS', 'COHESION_AUTH', 'http://127.0.0.1:8085/CohesionServlet/Authentication', SYSDATE, 'Url della servlet di autenticazione Cohesion', 1, SYSDATE);

insert into &user_schema..CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 values
   ('SINS', 'COHESION_OUT', 'http://127.0.0.1:8085/CohesionServlet/Logout', SYSDATE, 'Url della servlet di logout da Cohesion', 1, SYSDATE);

insert into &user_schema..CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 values
   ('SINS', 'NUM_GG_ALERT1', '5', SYSDATE, 'Lista attivita: gg dopo i quali mostro alert di livello 1', 1, SYSDATE);

insert into &user_schema..CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 values
   ('SINS', 'NUM_GG_ALERT2', '10', SYSDATE, 'Lista attivita: gg dopo i quali mostro alert di livello 2', 1, SYSDATE);
   
      
/* richiesta mmg */      
CREATE TABLE &user_schema..RM_RICH_MMG( 
  N_CARTELLA            NUMBER(13)              NOT NULL,
  ID_RICH               NUMBER(13)              NOT NULL,
  ID_SCHEDA_SO          NUMBER(13),
  STATO                 CHAR(1 BYTE),
  RICHIEDENTE           CHAR(2 BYTE),
  COD_MED               CHAR(6 BYTE),
  TIPOCURA              VARCHAR2(8 BYTE),
  PR_MMG_DATA_RICHIESTA DATE,
  DATA_PROTOCOLLO       DATE,
  DATA_PRESA_CARICO     DATE,
  DATA_INIZIO           DATE,
  DATA_FINE             DATE,
  SKI_ANAMNESI_1        VARCHAR2(2000 BYTE),
  SKI_ANAMNESI_2        VARCHAR2(2000 BYTE),
  FASE_TERMINALE        CHAR(1 BYTE),
  NEURO_DEGEN           CHAR(1 BYTE),
  CRONICHE_AVANZATE     CHAR(1 BYTE),
  BISOGNI_SANITARI      VARCHAR2(2000 BYTE),
  AUTOSUFFICIENZA       CHAR(2 BYTE),
  FLAG_AUTOSUFF         CHAR(1 BYTE),
  FLAG_DEAMB            CHAR(1 BYTE),
  DEAMB_TIPO            CHAR(1 BYTE),
  FLAG_TRASPORTO        CHAR(1 BYTE),
  FLAG_NON_AUTO         CHAR(1 BYTE),
  FLAG_PIANO_ALTO       CHAR(1 BYTE),
  FLAG_TRAPORTO_ALTRO   CHAR(1 BYTE),
  TRASPORTO_ALTRO       VARCHAR2(300 BYTE),
  FLAG_GRAVI_PAT        CHAR(1 BYTE),
  GRAVI_PATOLOGIE       VARCHAR2(2000 BYTE),
  STATO_CIVILE          CHAR(1 BYTE),
  NUM_FAM               NUMBER(2),
  BADANTE               CHAR(1 BYTE),
  CONVIVENTI            CHAR(1 BYTE),
  SITFAM                CHAR(1 BYTE),
  SITFAM_IDONEA_TIPO    CHAR(1 BYTE),
  SITFAM_PARZ_TIPO      CHAR(1 BYTE),
  SITFAM_NON_IDO_TIPO   CHAR(1 BYTE),
  FLAG_IND_CONOSCITIVA  CHAR(1 BYTE),
  FLAG_ASS_DOM          CHAR(1 BYTE),
  FLAG_ASS_AB           CHAR(1 BYTE),
  FLAG_ASS_SOC          CHAR(1 BYTE),
  FLAG_ASS_ECO          CHAR(1 BYTE),
  COD_DISTRETTO         CHAR(6 BYTE),
  COD_ZONA              CHAR(1 BYTE),
  MEDICO_ALTRO_DESC     VARCHAR2(200 BYTE),
  NUM_PROTOCOLLO        CHAR(10 BYTE),
  NOTE                  CHAR(2000 BYTE),  
  primary key(n_cartella, id_rich)
);      
     
/* scheda segreteria organizzativa */
 CREATE TABLE &user_schema..RM_SKSO(
  N_CARTELLA              NUMBER(13)            NOT NULL,
  ID_SKSO                 NUMBER(13)            NOT NULL,
  PR_DATA_PUAC            DATE,
  DATA_PRESA_CARICO_SKSO  DATE,
  PR_PROTOC_DOMANDA       VARCHAR2(20 BYTE),
  PR_DATA_PROTOC          DATE,
  PR_NOTE                 VARCHAR2(2000 BYTE),
  PR_DATA_VERBALE_UVM     DATE,
  PR_NUM_VERBALE          NUMBER(13),
  PR_VALUTAZIONE          VARCHAR2(2000 BYTE),
  PR_OBIETTIVO            VARCHAR2(2000 BYTE),
  PR_PIANO_TERAPEUTICO    VARCHAR2(2000 BYTE),
  PR_REVISIONE            CHAR(2 BYTE),
  PR_DATA_REVISIONE       DATE,
  PR_DATA_CHIUSURA        DATE,
  PR_MOTIVO_CHIUSURA      CHAR(8 BYTE),
  PR_NOTE_CHIUSURA        VARCHAR2(2000 BYTE),
  TIPO_UTE                CHAR(2 BYTE),
  CAREGIVER_NOME 		  CHAR(20 BYTE),
  CAREGIVER_COGNOME 	  CHAR(20 BYTE),
  CAREGIVER_TELEFONO 	  CHAR(20 BYTE),
  CAREGIVER_GRADO_PARENTELA CHAR(8 BYTE),
  COD_CASE_MANAGER 			CHAR(10 BYTE),
  DT_PRESA_CARICO_LIVELLO 	DATE,
  PRESA_CARICO_LIVELLO    CHAR(8 BYTE),
  COD_ZONA_VERBALE	      CHAR(1 BYTE),
  COD_DISTRETTO_VERBALE   CHAR(6 BYTE),
  CASE_MANAGER_MMG 		  CHAR(1 byte),
  PV_TP_OPERATORE 		  CHAR(8 BYTE), // TIPO OPERATORE PRIMA VISITA 
  PV_COD_OPERATORE 		  CHAR(10 BYTE), // CODICE OPERATORE PRIMA VISITA 
  PV_DT_VISITA 			  DATE, // data prima visita 
  JISAS_UID               NUMBER(13),
  JISAS_GID               NUMBER(13),
  JISAS_MASK              CHAR(9 BYTE),
  JDBINTERF_VERSION       NUMBER(13),
  JDBINTERF_LASTCNG       DATE,
  PRIMARY KEY  (N_CARTELLA, ID_SKSO)
);

/* contiene le informazioni della scheda mmg */
CREATE TABLE &user_schema..RM_SKSO_MMG
(
  N_CARTELLA             NUMBER(13)             NOT NULL,
  ID_SKSO                NUMBER(13)             NOT NULL,
  ID_RICH                NUMBER(13),
  STATO                  CHAR(1 BYTE),
  RICHIEDENTE            CHAR(2 BYTE),
  COD_MED                CHAR(6 BYTE),
  TIPOCURA               VARCHAR2(8 BYTE),
  DATA_RICHIESTA         DATE,
  NUM_PROTOCOLLO         CHAR(10),
  --DATA_PROTOCOLLO        DATE,
  --DATA_PRESA_CARICO      DATE,
  DATA_INIZIO            DATE,
  DATA_FINE              DATE,
  SKI_ANAMNESI_1         VARCHAR2(2000 BYTE),
  SKI_ANAMNESI_2         VARCHAR2(2000 BYTE),
  FASE_TERMINALE         CHAR(1 BYTE),
  NEURO_DEGEN            CHAR(1 BYTE),
  CRONICHE_AVANZATE      CHAR(1 BYTE),
  BISOGNI_SANITARI       VARCHAR2(2000 BYTE),
  AUTOSUFFICIENZA        CHAR(2 BYTE),
  FLAG_AUTOSUFF          CHAR(1 BYTE),
  FLAG_DEAMB             CHAR(1 BYTE),
  DEAMB_TIPO             CHAR(1 BYTE),
  FLAG_TRASPORTO         CHAR(1 BYTE),
  FLAG_NON_AUTO          CHAR(1 BYTE),
  FLAG_PIANO_ALTO        CHAR(1 BYTE),
  FLAG_TRAPORTO_ALTRO    CHAR(1 BYTE),
  TRASPORTO_ALTRO        VARCHAR2(300 BYTE),
  FLAG_GRAVI_PAT         CHAR(1 BYTE),
  GRAVI_PATOLOGIE        VARCHAR2(2000 BYTE),
  STATO_CIVILE           CHAR(1 BYTE),
  NUM_FAM                NUMBER(2),
  BADANTE                CHAR(1 BYTE),
  CONVIVENTI             CHAR(1 BYTE),
  SITFAM                 CHAR(1 BYTE),
  SITFAM_IDONEA_TIPO     CHAR(1 BYTE),
  SITFAM_PARZ_TIPO       CHAR(1 BYTE),
  SITFAM_NON_IDO_TIPO    CHAR(1 BYTE),
  NOTE                   VARCHAR2(2000 BYTE),
  FLAG_IND_CONOSCITIVA   CHAR(1 BYTE),
  FLAG_ASS_DOM           CHAR(1 BYTE),
  FLAG_ASS_AB            CHAR(1 BYTE),
  FLAG_ASS_SOC           CHAR(1 BYTE),
  FLAG_ASS_ECO           CHAR(1 BYTE),
  COD_DISTRETTO          CHAR(6 BYTE),
  COD_ZONA               CHAR(1 BYTE),
  MEDICO_ALTRO_DESC      VARCHAR2(200 BYTE),
  PR_MOTIVO              VARCHAR2(8 BYTE),
  TIPO_UTE               CHAR(2 BYTE),
  PR_MMG_DATA_RICHIESTA  DATE,
  ACCESSI_MMG 			 NUMBER(2),
  PRIMARY KEY (N_CARTELLA, ID_SKSO)
);       


CREATE TABLE &user_schema..RM_SKSO_OP_COINVOLTI(
  N_CARTELLA            NUMBER(13)              NOT NULL,
  ID_SKSO               NUMBER(13)              NOT NULL,
  TIPO_OPERATORE        CHAR(8 BYTE),
  COD_ZONA              CHAR(1 BYTE),
  COD_DISTRETTO         CHAR(6 BYTE),
  DT_PRESA_CARICO       DATE,
  DT_CHIUSURA           DATE,
  DT_INIZIO_PIANO       DATE,
  DT_FINE_PIANO         DATE,
  NUM_ACCES_SET         NUMBER(13),
  COD_OPERATORE         CHAR(10 BYTE),
  COD_PRESIDIO          CHAR(6 BYTE),
  primary key(n_cartella, id_skso, tipo_operatore)
);

/* contiene le proroghe delle schede */
CREATE TABLE &user_schema..RM_SKSO_PROROGHE(
  N_CARTELLA            NUMBER(13)              NOT NULL,
  ID_SKSO               NUMBER(13)              NOT NULL,
  id_proroga            NUMBER(13),
  DT_PROROGA_INIZIO     DATE,
  DT_PROROGA_FINE       DATE,
  NOTE_PROROGHE         VARCHAR(2000),
  primary key(n_cartella, id_skso, id_proroga)
);

/* contiene le sospensioni delle schede */
CREATE TABLE &user_schema..RM_SKSO_SOSPENSIONI(
  N_CARTELLA             NUMBER(13)              NOT NULL,
  ID_SKSO                NUMBER(13)              NOT NULL,
  id_sospensione         number(13),
  MOTIVO                 CHAR(9 BYTE),
  NOTE_SOSPENSIONE       VARCHAR(2000),
  DT_SOSPENSIONE_INIZIO  DATE,
  DT_SOSPENSIONE_FINE    DATE,
  primary key(n_cartella, id_skso, id_sospensione)
);

/* contiene le sospensioni delle schede */
CREATE TABLE &user_schema..RM_PUAUVM_COMMISSIONE
(
  N_CARTELLA            NUMBER(13)              NOT NULL,
  ID_SKSO               NUMBER(13)              NOT NULL, 
  PR_PRESENZA           NUMBER(13)              NOT NULL,
  PR_TIPO               CHAR(2 BYTE),
  PR_OPERATORE          CHAR(10 BYTE),
  PR_APPROFONDIMENTO    CHAR(1 BYTE),
  PR_PARTECIPA          CHAR(1 BYTE),
  PR_RESPONSABILE       CHAR(1 BYTE),
  JDBINTERF_LASTCNG     DATE,
  JDBINTERF_VERSION     NUMBER(13),
  PR_OPERATORE_COGNOME  VARCHAR2(30 BYTE),
  PR_OPERATORE_NOME     VARCHAR2(30 BYTE),
  PR_QUAL               CHAR(6 BYTE),
  PRIMARY KEY(N_CARTELLA, ID_SKSO, PR_PRESENZA)
)

CREATE TABLE &user_schema..RUGIII_HC(
  N_CARTELLA          NUMBER(13) NOT NULL,
  DATA                DATE NOT NULL,
  COD_OPERATORE       CHAR(10 BYTE),
  RUG_A12            CHAR(2 BYTE),
  RUG_A13_A          CHAR(1 BYTE),
  RUG_A13_B          CHAR(1 BYTE),
  RUG_A13_C          CHAR(1 BYTE),
  RUG_A14             CHAR(1 BYTE),  
  RUG_PUNT            NUMBER(13),
  NOME                VARCHAR2(200 BYTE),
  DATA_TEST           DATE,
  TEMPO_T             NUMBER(13),
  JDBINTERF_LASTCNG   DATE,
  JDBINTERF_VERSION   NUMBER(13), 
  OPERATORE_MOD       CHAR(10 BYTE),
  MOD_DATA            DATE,
  primary key(N_CARTELLA, DATA)
);
CREATE TABLE &user_schema..SKFPG(
  N_CARTELLA            NUMBER(13)                NOT NULL,
  N_CONTATTO            NUMBER(13)                NOT NULL,
  SKFPG_TIPO_OPERATORE  CHAR(2)                   NOT NULL,
  SKFPG_DATA_APERTURA   DATE                      NOT NULL,  
  SKFPG_OPERATORE       CHAR(10 BYTE),
  SKFPG_DESCR_CONTATTO  CHAR(80 BYTE),
  SKFPG_REFERENTE       CHAR(10 BYTE),
  SKFPG_REFERENTE_DA    DATE,
  SKFPG_INVIATO         CHAR(2 BYTE),
  SKFPG_TIPOUT          CHAR(2 BYTE),
  SKFPG_NOTE            VARCHAR2(2000 BYTE),
  SKFPG_MOTIVO_USCITA   CHAR(3 BYTE),
  SKFPG_DATA_USCITA     DATE,
  SKFPG_TRASFER         CHAR(40 BYTE);
  SKFPG_TIPOLOGIA       CHAR(1 BYTE),
  SKFPG_MOTIVO_TXT      VARCHAR2(70 BYTE),
  SKFPG_DISTRETTO       CHAR(6 BYTE),
  SKFPG_COD_PRESIDIO    CHAR(6 BYTE),
  JDBINTERF_VERSION     NUMBER(13),
  JDBINTERF_LASTCNG     DATE,
  JISAS_UID             NUMBER(13),
  JISAS_GID             NUMBER(13),
  JISAS_MASK            CHAR(9 BYTE),
  PRIMARY KEY
 (N_CARTELLA, N_CONTATTO,SKFPG_TIPO_OPERATORE)
);

CREATE TABLE &user_schema..SKFPG_REFERENTE
(
  N_CARTELLA           NUMBER(13)                NOT NULL,
  N_CONTATTO           NUMBER(13)                NOT NULL,
  SKFPG_TIPO_OPERATORE CHAR(2)                   NOT NULL,
  SKFPG_REFERENTE_DA   DATE                      NOT NULL,
  SKFPG_REFERENTE      CHAR(10 BYTE),
  JDBINTERF_LASTCNG    DATE,
  JDBINTERF_VERSION    NUMBER(13),
  PRIMARY KEY
 (N_CARTELLA, N_CONTATTO,SKFPG_TIPO_OPERATORE, SKFPG_REFERENTE_DA)
);
COMMIT;

CREATE TABLE &user_schema..RM_DIARIO
(
  N_CARTELLA			NUMBER(13)		NOT NULL,
  N_CONTATTO			NUMBER(13)		NOT NULL,
  TIPO_OPERATORE		CHAR(2)			NOT NULL,
  PROGR_INSE			NUMBER(13)		NOT NULL,
  PROGR_MODI			NUMBER(13)		NOT NULL,
  OP_INSE				CHAR(10),
  DATA_INSE				DATE,
  ORA_INSE				CHAR(5),
  OP_MODI				CHAR(10),
  DATA_MODI				DATE,
  ORA_MODI				CHAR(5),
  DATA_DIARIO			DATE,
  OGGETTO				VARCHAR2(1000),   
  TESTO					CLOB,   
  INFO_PRIVATA			CHAR(1),
  ID_SKSO				NUMBER(13),
  PRIMARY KEY
 (N_CARTELLA,N_CONTATTO,TIPO_OPERATORE,PROGR_INSE,PROGR_MODI)
);
COMMIT;


--aggiunto id_skso su skinf,skmedico,skfisio
alter table SKINF add id_skso number(13);
alter table SKMEDICO add id_skso number(13);
alter table SKFIS add id_skso number(13);
alter table SKFPG add id_skso number(13);	

-- aggiunto flag su piano_accessi e piano_assist
ALTER TABLE &user_schema..piano_assist ADD FLAG_STATO CHAR(1);
ALTER TABLE &user_schema..piano_accessi ADD FLAG_STATO CHAR(1);
COMMIT;

