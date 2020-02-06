/* SCRIPT DA ESEGUIRE DAL 01/11/2014*/
alter table rm_skso_op_coinvolti add cod_zona char(1byte);
alter table rm_skso add cod_zona_verbale char(1 byte);
alter table rm_skso add cod_distretto_verbale char(6 byte);
alter table rm_skso_mmg add accessi_mmg number(2);
alter table rm_skso_op_coinvolti add cod_presidio char(6 byte) ;
alter table rm_skso add case_manager_mmg char(1 byte);
alter table rm_skso add pv_tp_operatore char(8 byte); // tipo operatore prima visita 
alter table rm_skso add pv_cod_operatore char(10 byte); // codice operatore prima visita 
alter table rm_skso add pv_dt_visita date; // data per la prima visita
alter table skinf add ski_motivo CHAR(3 BYTE);
alter table skmedico add  skm_motivo CHAR(3 BYTE);
alter table skfis add skf_motivo  CHAR(3 BYTE);
alter table skfpg add skfpg_motivo  CHAR(3 BYTE);
alter table skfpg add skfpg_trasfer CHAR(40 BYTE);
ALTER TABLE PIANO_ASSIST ADD (PA_MOTIVO_CHIUSURA VARCHAR2(200) );
ALTER TABLE comuvm MODIFY cm_cod_comm NUMBER(13);
ALTER TABLE comuvm_compo MODIFY cm_cod_comm NUMBER(13);
ALTER TABLE puauvm MODIFY pr_cod_comm NUMBER(13);
/* FINE SCRIPT DA ESEGUIRE DAL 01/11/2014*/

/* SCRIPT DA ESEGUIRE DAL 18/11/2014*/
/* caregiver aggiunti alla richiesta del medico mmg */
alter table rm_rich_mmg add CAREGIVER_NOME CHAR(20 BYTE);
alter table rm_rich_mmg add CAREGIVER_COGNOME CHAR(20 BYTE);
alter table rm_rich_mmg add CAREGIVER_TELEFONO CHAR(20 BYTE);
alter table rm_rich_mmg add CAREGIVER_GRADO_PARENTELA CHAR(8 BYTE);
/* FINE SCRIPT DA ESEGUIRE DAL 18/11/2014*/

/* SCRIPT DA ESEGUIRE DAL 26/11/2014*/
/* aggiungere il campo codice della commissione */
alter table rm_skso add cod_commis_uvm NUMBER(13);
/* MARCHIAMO LA VERSIONE DEL DB*/ 
update conf set conf_txt = 'Vers. 14.02.a' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
/* FINE SCRIPT DA ESEGUIRE DAL 26/11/2014*/


/* SCRIPT DA ESEGUIRE DAL 02/12/2014 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 14.02.b' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
/* viene marcato quando la scheda viene aperta dalla so nel caso di prima visita */
alter table rm_skso add vista_da_so char(1);
alter table rm_skso_op_coinvolti add vista_da_so char(1);
alter table rm_rich_mmg add DIAG1     CHAR(6 BYTE);
alter table rm_rich_mmg add DIAG1_IDS CHAR(1 BYTE);
alter table rm_rich_mmg add DIAG2     CHAR(6 BYTE);
alter table rm_rich_mmg add DIAG2_IDS CHAR(1 BYTE);
alter table rm_rich_mmg add DIAG3     CHAR(6 BYTE);
alter table rm_rich_mmg add DIAG3_IDS CHAR(1 BYTE);
alter table rm_rich_mmg add DIAG4     CHAR(6 BYTE);
alter table rm_rich_mmg add DIAG4_IDS CHAR(1 BYTE);
alter table rm_rich_mmg add DIAG5     CHAR(6 BYTE);
alter table rm_rich_mmg add DIAG5_IDS CHAR(1 BYTE);
alter table rm_rich_mmg add data_diag date;

/* FINE SCRIPT DA ESEGUIRE DAL 02/12/2014*/


/* SCRIPT DA ESEGUIRE DAL 04/12/2014 */
update conf set conf_txt = 'Vers. 14.02.c' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
--
--	DEFINIZIONE COLONNA JISAS_LASTUID
--
ALTER TABLE AGENDANT_INTERV ADD jisas_lastuid NUMBER(13);
ALTER TABLE AGENDANT_INTPRE ADD jisas_lastuid NUMBER(13);
ALTER TABLE AGENDANT_SETT_TIPO ADD jisas_lastuid NUMBER(13);
ALTER TABLE ANAGRA_C ADD jisas_lastuid NUMBER(13);
ALTER TABLE CARTELLA ADD jisas_lastuid NUMBER(13);
ALTER TABLE DIAGNOSI ADD jisas_lastuid NUMBER(13);
ALTER TABLE INTERV ADD jisas_lastuid NUMBER(13);
ALTER TABLE INTMMG ADD jisas_lastuid NUMBER(13);
ALTER TABLE INTPRE ADD jisas_lastuid NUMBER(13);
ALTER TABLE PIANO_ACCESSI ADD jisas_lastuid NUMBER(13);
ALTER TABLE PIANO_ASSIST ADD jisas_lastuid NUMBER(13);
ALTER TABLE PIANO_VERIFICA ADD jisas_lastuid NUMBER(13);
ALTER TABLE RM_PUAUVM_COMMISSIONE ADD jisas_lastuid NUMBER(13);
ALTER TABLE RM_RICH_MMG ADD jisas_lastuid NUMBER(13);
ALTER TABLE RM_SKSO ADD jisas_lastuid NUMBER(13);
ALTER TABLE RM_SKSO_MMG ADD jisas_lastuid NUMBER(13);
ALTER TABLE RM_SKSO_OP_COINVOLTI ADD jisas_lastuid NUMBER(13);
ALTER TABLE RM_SKSO_PROROGHE ADD jisas_lastuid NUMBER(13);
ALTER TABLE RM_SKSO_SOSPENSIONI ADD jisas_lastuid NUMBER(13);
ALTER TABLE RUGIII_HC ADD jisas_lastuid NUMBER(13);
ALTER TABLE SC_BISOGNI ADD jisas_lastuid NUMBER(13);
ALTER TABLE SCL_VALUTAZIONE ADD jisas_lastuid NUMBER(13);
ALTER TABLE SKFIS ADD jisas_lastuid NUMBER(13);
ALTER TABLE SKFPG ADD jisas_lastuid NUMBER(13);
ALTER TABLE SKINF ADD jisas_lastuid NUMBER(13);
ALTER TABLE SKMEDICO ADD jisas_lastuid NUMBER(13);
ALTER TABLE SKMMG_ADI ADD jisas_lastuid NUMBER(13);
ALTER TABLE SKMMG_ADP ADD jisas_lastuid NUMBER(13);
ALTER TABLE SKMMG_ADR ADD jisas_lastuid NUMBER(13);
--
--	VALORIZZAZIONE COLONNA JISAS_LASTUID
--
UPDATE AGENDANT_INTERV SET jisas_lastuid = 0;
UPDATE AGENDANT_INTPRE SET jisas_lastuid = 0;
UPDATE AGENDANT_SETT_TIPO SET jisas_lastuid = 0;
UPDATE ANAGRA_C SET jisas_lastuid = 0;
UPDATE CARTELLA SET jisas_lastuid = 0;
UPDATE DIAGNOSI SET jisas_lastuid = 0;
UPDATE INTERV SET jisas_lastuid = 0;
UPDATE INTMMG SET jisas_lastuid = 0;
UPDATE INTPRE SET jisas_lastuid = 0;
UPDATE PIANO_ACCESSI SET jisas_lastuid = 0;
UPDATE PIANO_ASSIST SET jisas_lastuid = 0;
UPDATE PIANO_VERIFICA SET jisas_lastuid = 0;
UPDATE RM_PUAUVM_COMMISSIONE SET jisas_lastuid = 0;
UPDATE RM_RICH_MMG SET jisas_lastuid = 0;
UPDATE RM_SKSO SET jisas_lastuid = 0;
UPDATE RM_SKSO_MMG SET jisas_lastuid = 0;
UPDATE RM_SKSO_OP_COINVOLTI SET jisas_lastuid = 0;
UPDATE RM_SKSO_PROROGHE SET jisas_lastuid = 0;
UPDATE RM_SKSO_SOSPENSIONI SET jisas_lastuid = 0;
UPDATE RUGIII_HC SET jisas_lastuid = 0;
UPDATE SC_BISOGNI SET jisas_lastuid = 0;
UPDATE SCL_VALUTAZIONE SET jisas_lastuid = 0;
UPDATE SKFIS SET jisas_lastuid = 0;
UPDATE SKFPG SET jisas_lastuid = 0;
UPDATE SKINF SET jisas_lastuid = 0;
UPDATE SKMEDICO SET jisas_lastuid = 0;
UPDATE SKMMG_ADI SET jisas_lastuid = 0;
UPDATE SKMMG_ADP SET jisas_lastuid = 0;
UPDATE SKMMG_ADR SET jisas_lastuid = 0;
COMMIT;
--
--	GENERAZIONE TABELLE OMBRA E TRIGGER
--
BEGIN
MKHISTORY_PKG.MK_HI_ALL;  -- genera tabella ombra e trigger per tutte le tabelle definite nello schema corrente che contengono la colonna JISAS_LASTUID
END;


/*  TABELLA DI CONFIGURAZIONE PER GLI ALERT IN LISTA ATTIVITA */      
CREATE TABLE RM_CONF_ALERT( 
  TIPO_FONTE            CHAR(10) NOT NULL,
  NUM_GG_ALERT_LIV1     NUMBER(13),
  NUM_GG_ALERT_LIV2     NUMBER(13),
  primary key(TIPO_FONTE)
);
Insert into RM_CONF_ALERT (TIPO_FONTE, NUM_GG_ALERT_LIV1, NUM_GG_ALERT_LIV2) Values ('3', 7, 14);
Insert into RM_CONF_ALERT (TIPO_FONTE, NUM_GG_ALERT_LIV1, NUM_GG_ALERT_LIV2) Values ('4', 8, 16);
Insert into RM_CONF_ALERT (TIPO_FONTE, NUM_GG_ALERT_LIV1, NUM_GG_ALERT_LIV2) Values ('2', 6, 12);
Insert into RM_CONF_ALERT (TIPO_FONTE, NUM_GG_ALERT_LIV1, NUM_GG_ALERT_LIV2) Values ('1', 5, 10);
COMMIT;

/* FINE SCRIPT DA ESEGUIRE DAL 04/12/2014*/

/* SCRIPT DA ESEGUIRE DAL 05/12/2014 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 14.02.c' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
/* per la gestione delle intolleranze allergie */
CREATE TABLE rm_intolleranze_allergie(
  N_CARTELLA                 NUMBER(13)         NOT NULL,
  progressivo                NUMBER(13)         NOT NULL,
  data_rilevazione           DATE,
  allergie                   VARCHAR2(2000 BYTE),
  primary key (n_cartella, progressivo, data_rilevazione)
 );
 
 /*  descrizione intolleranze */
CREATE TABLE intolleranze(
  codice                 NUMBER(13)    NOT NULL,
  descrizione            CHAR(50 BYTE),
  primary key (codice)
 );

/*  gestione intolleranze  */
CREATE TABLE rm_intolleranze(
  n_cartella             NUMBER(13)    NOT NULL,
  progressivo            NUMBER(13)    NOT NULL,
  codice                 NUMBER(13)    NOT NULL,
  primary key (n_cartella, progressivo, codice)
 );

COMMIT;

drop table rugIII_hc;

-- Create table
create table RUGIII_HC
(
 N_CARTELLA        NUMBER(13) not null,
  DATA              DATE not null,
  COD_OPERATORE     CHAR(10),
  RUG_A12           CHAR(2),
  RUG_A13_A         CHAR(1),
  RUG_A13_B         CHAR(1),
  RUG_A13_C         CHAR(1),
  RUG_A14           CHAR(1),
  RUG_PUNT          NUMBER(13),
  NOME              VARCHAR2(200),
  DATA_TEST         DATE,
  TEMPO_T           NUMBER(13),
  JDBINTERF_LASTCNG DATE,
  JDBINTERF_VERSION NUMBER(13),
  OPERATORE_MOD     CHAR(10),
  MOD_DATA          DATE,
  G1A               CHAR(1),
  G1D               CHAR(1),
  G1E               CHAR(1),
  G2G               CHAR(1),
  G2H               CHAR(1),
  G2I               CHAR(1),
  G2J               CHAR(1),
  L7                CHAR(1),
  J3J               CHAR(1),
  N2E               CHAR(1),
  N2G               CHAR(1),
  N2J               CHAR(1),
  N2H               CHAR(1),
  K2A               CHAR(1),
  K2B               CHAR(1),
  K3                CHAR(1),
  J3N               CHAR(1),
  J3S_INT           CHAR(1),
  J3S_URI           CHAR(1),
  J3S               CHAR(1),
  N2B               CHAR(1),
  L1                CHAR(1),
  L4                CHAR(1),
  L5                CHAR(1),
  N2K               CHAR(1),
  C1                CHAR(1),
  E3A               CHAR(1),
  E3B               CHAR(1),
  E3C               CHAR(1),
  E3D               CHAR(1),
  E3E               CHAR(1),
  E3F               CHAR(1),
  J3H               CHAR(1),
  J3I               CHAR(1),
  J3R               CHAR(1),
  N2A               CHAR(1),
  N2F               CHAR(1),
  J7                CHAR(1),
  N2I               CHAR(1),
  N2D               CHAR(1)
)
;
-- Create/Recreate primary, unique and foreign key constraints 
alter table RUGIII_HC
  add primary key (N_CARTELLA, DATA)
;

drop table sc_bisogni;
-- Create table
create table SC_BISOGNI
(
  N_CARTELLA              NUMBER(13) not null,
  DATA                    DATE not null,
  BISOGNI_PUNT            NUMBER(13),
  BISOGNI_NOME            VARCHAR2(200),
  COD_OPERATORE           CHAR(10),
  DATA_TEST               DATE,
  ID_SKSO                 NUMBER(13),
  JDBINTERF_LASTCNG       DATE,
  JDBINTERF_VERSION       NUMBER(13),
  OPERATORE_MOD           CHAR(10),
  MOD_DATA                DATE,
  AUTONOMIA               CHAR(2),
  RIAB_NEUROLOGICA        CHAR(1),
  RIAB_ORTPEDICA          CHAR(1),
  RIAB_MANTENIMENTO       CHAR(1),
  RIAB_AFASIA             CHAR(1),
  RESP_TOSSE_SECR         CHAR(1),
  RESP_OSSIGENOTERAPIA    CHAR(1),
  RESP_VENTILOTERAPIA     CHAR(1),
  RESP_PORTATORE_TRACHEO  CHAR(1),
  NUTR_NORMALE            CHAR(1),
  NUTR_DIMAGRIMENTO       CHAR(1),
  NUTR_DISIDRATAZIONE     CHAR(1),
  NUTR_DISFAGIA           CHAR(1),
  GASTR_STIPSI            CHAR(1),
  GASTR_STOMIA            CHAR(1),
  GASTR_SANG              CHAR(1),
  GASTR_VOMITO            CHAR(1),
  GASTR_INCONT            CHAR(1),
  GENURI_INCONT           CHAR(1),
  GENURI_CATETERISMO      CHAR(1),
  GENURI_DIALISI          CHAR(1),
  GENURI_EMATURIA         CHAR(1),
  GENURI_UROSTOMIA        CHAR(1),
  BIS_AUTONOMIA           CHAR(1),
  RIAB_NESSUNA            CHAR(1),
  RESP_NORMALE            CHAR(1),
  GASTR_NORMALE           CHAR(1),
  CUTE_NORMALE            CHAR(1),
  CUTE_ULCERE12           CHAR(1),
  CUTE_ULCERE34           CHAR(1),
  CUTE_PRESSIONE          CHAR(1),
  CUTE_LACERAZIONI        CHAR(1),
  CUTE_ALTRO              CHAR(1),
  CUTE_CURA               CHAR(1),
  COMP_NORMALE            CHAR(1),
  COMP_DIST_COGN_MODERATO CHAR(1),
  COMP_DIST_COGN_GRAVE    CHAR(1),
  COMP_DIST_COMP          CHAR(1),
  COMP_PSICO_SALUTE       CHAR(1),
  RITMO_NORMALE           CHAR(1),
  RISCHIO_ASSENTE         CHAR(1),
  ONCO_NORMALE            CHAR(1),
  ONCO_TERM_NON_ONCO      CHAR(1),
  ONCO_TERM_ONCO          CHAR(1),
  ONCO_ONCOLOGICO         CHAR(1),
  ONCO_CHEMIOTERAPIA      CHAR(1),
  ONCO_RADIOTERAPIA       CHAR(1),
  ONCO_DOLORE             CHAR(1),
  PREST_PRELIEVO          CHAR(1),
  PREST_ECG               CHAR(1),
  PREST_TELEMETRIA        CHAR(1),
  PREST_TRASFUSIONI       CHAR(1),
  PREST_TERAPIA_EV        CHAR(1),
  PREST_TERAPIA_SOTCUT    CHAR(1),
  PREST_GESTIONE_CVC      CHAR(1),
  RISCHIO_FEBBRE          CHAR(1),
  JISAS_LASTUID           NUMBER(13),
  TEMPO_T                 NUMBER(13)
)
;
-- Create/Recreate primary, unique and foreign key constraints 
alter table SC_BISOGNI
  add constraint SC_BISOGNI_PK primary key (N_CARTELLA, DATA)
;
drop table scl_valutazione;
-- Create table
create table SCL_VALUTAZIONE
(
 JDBINTERF_VERSION NUMBER(13),
  JDBINTERF_LASTCNG DATE,
  N_CARTELLA        NUMBER(13) not null,
  DATA              DATE not null,
  PAT_PREV          CHAR(6),
  PAT_CONCO         CHAR(6),
  AUTONOMIA         NUMBER(13),
  MOBILITA          NUMBER(13),
  COGNITIVI         NUMBER(13),
  COMPORTAMENTO     NUMBER(13),
  SUPP_SOCIALE      NUMBER(13),
  RISCHIO_INFETTIVO NUMBER(13),
  LIVELLO           CHAR(1),
  DRENAGGIO         NUMBER(13),
  OSSIGENO_TERAPIA  NUMBER(13),
  VENTILOTERAPIA    NUMBER(13),
  TRACHEOTOMIA      NUMBER(13),
  ALIM_ASSISTITA    NUMBER(13),
  ALIM_ENTERALE     NUMBER(13),
  ALIM_PARENTALE    NUMBER(13),
  STOMIA            NUMBER(13),
  ELIM_URINARIA     NUMBER(13),
  SONNO_VEGLIA      NUMBER(13),
  EDUC_TERAP        NUMBER(13),
  ULCERE12G         NUMBER(13),
  ULCERE34G         NUMBER(13),
  PRELIEVI_VENOSI   NUMBER(13),
  ECG               NUMBER(13),
  TELEMETRIA        NUMBER(13),
  TER_SOTTOCUT      NUMBER(13),
  CATETERE          NUMBER(13),
  TRASFUSIONI       NUMBER(13),
  DOLORE            NUMBER(13),
  TERMINALE_ONC     NUMBER(13),
  TERMINALE_NONONC  NUMBER(13),
  NEUROLOGICO       NUMBER(13),
  ORTOPEDICO        NUMBER(13),
  MANTENIMENTO      NUMBER(13),
  SUPERVISIONE      NUMBER(13),
  ASS_IADL          NUMBER(13),
  ASS_ADL           NUMBER(13),
  CARE_GIVER        NUMBER(13),
  TEMPO_T           NUMBER(13),
  FLAG_SENDED       CHAR(1),
  DATA_SENDED       DATE,
  NOME              VARCHAR2(200),
  DATA_TEST         DATE,
  OPERATORE_MOD     CHAR(10),
  MOD_DATA          DATE,
  ELIM_URINARIA_INT NUMBER(13),
  ELIM_URINARIA_URI NUMBER(13),
  STOMIA_INT        NUMBER(13),
  STOMIA_URI        NUMBER(13),
  CODICE_EVENTO     NUMBER(13)
)
;
-- Create/Recreate primary, unique and foreign key constraints 
alter table SCL_VALUTAZIONE
  add primary key (N_CARTELLA, DATA)
  ;
insert into tab_schede (ID_SCHEDA, DESCRIZIONE, TABELLA, CAMPO_PUNTEGGIO, CAMPO_DATA, CAMPO_NOME, CLASSE, FL_PUNTEGGIO, FL_ATTIVO, JDBINTERF_LASTCNG, JDBINTERF_VERSION)
values (57, 'VALUTAZIONE BISOGNI', 'SC_BISOGNI', 'bisogni_punt', 'data', 'bisogni_nome', 'ScalaBisogniFormCtrl', 'N', 'S', null, null);

-- inserire per ogni figura professionale prevista
insert into tab_schede_cont (ID_SCHEDA, SC_TIPO_OP, JDBINTERF_LASTCNG, JDBINTERF_VERSION)
values (57, '02 ', null, null);


/* SCRIPT DA ESEGUIRE DAL 09/12/2014 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 14.02.d' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
/* per la gestione delle intolleranze allergie */
alter table rm_skso modify vista_da_so varchar2(8);

drop table rm_intolleranze_allergie;
drop table intolleranze;
drop table rm_intolleranze;

CREATE TABLE rm_intolleranze_allergie(
  N_CARTELLA                 NUMBER(13)         NOT NULL,
  progressivo                NUMBER(13)         NOT NULL,
  data_rilevazione           DATE,
  allergie                   VARCHAR2(2000 BYTE),
  p_attivo                   NUMBER(13),
  primary key (n_cartella, progressivo)
 );

 /*  descrizione intolleranze */
CREATE TABLE principi_attivi(
  codice                 NUMBER(13)    NOT NULL,
  descrizione            CHAR(50 BYTE),
  primary key (codice)
 );
 
 alter table SC_BISOGNI add genuri_normale CHAR(1);
 delete from tab_schede where trim(tabella) = 'RUGIII_HC';
 update tab_schede set campo_data = 'siad_data' where trim(classe)='ValutazioneSIADFormCtrl';
 update tab_schede set campo_data = 'bisogni_data' where trim(classe)='ScalaBisogniFormCtrl';
 
 
 commit;
 
 /* FINE SCRIPT DA ESEGUIRE DAL 09/12/2014 */
 
/* SCRIPT DA ESEGUIRE DAL 10/12/2014 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 14.02.e' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
/* per i messaggi sulle tabelle di ricerca UVI */
UPDATE CONF SET CONF_TXT = 'UVI' WHERE conf_key='TITOLO_UVM';
UPDATE CONF SET CONF_TXT = 'UVI' WHERE conf_key='TITOLO_PUA';

ALTER TABLE COMUVM 
ADD (JISAS_UID NUMBER(13) );

ALTER TABLE COMUVM 
ADD (JISAS_GID NUMBER(13) );

ALTER TABLE COMUVM 
ADD (JISAS_MASK CHAR(9) );
 /* FINE SCRIPT DA ESEGUIRE DAL 10/12/2014 */

/* SCRIPT DA ESEGUIRE DAL 12/12/2014 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 14.02.f' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
/* modifica anagra_c per gestione dati STP */
alter table anagra_c add STP CHAR(16);
alter table anagra_c add STP_DT_SCAD DATE;
alter table anagra_c add STP_DT_INIZIO DATE;

-- aggiornamento scala bisogni/RUG/SIAD
alter table RUGIII_HC add A8 CHAR(1);
alter table RUGIII_HC add i1e CHAR(1);
alter table RUGIII_HC add i1f CHAR(1);
alter table RUGIII_HC add i1i CHAR(1);
alter table RUGIII_HC add i1r CHAR(1);
alter table RUGIII_HC add i1u CHAR(1);
alter table RUGIII_HC add i2a CHAR(1);
alter table RUGIII_HC add i2b CHAR(1);

alter table RUGIII_HC rename column RUG_A12 to A12;
alter table RUGIII_HC rename column RUG_A13_A to A13A;
alter table RUGIII_HC rename column RUG_A13_B to A13B;
alter table RUGIII_HC rename column RUG_A13_C to A13C;
alter table RUGIII_HC rename column RUG_A14 to A14;

CREATE TABLE RICHIESTE_CHIUSURA 
(
  N_CARTELLA NUMBER(13, 0) NOT NULL 
, ID_SKSO NUMBER(13, 0) NOT NULL 
, COD_ZONA_RICHIEDENTE CHAR(1 CHAR) NOT NULL 
, DATA_RICHIESTA DATE NOT NULL 
, COD_ZONA_PRESACARICO CHAR(1 CHAR) 
, COD_OPERATORE_RICHIEDENTE CHAR(10 CHAR) 
, MESSAGGIO_RICHIESTA NVARCHAR2(2000) 
, ESITO_RICHIESTA CHAR(1 CHAR) 
, DATA_CHIUSURA DATE 
, COD_OPERATORE_CHIUSURA CHAR(10 CHAR)
);

ALTER TABLE RICHIESTE_CHIUSURA ADD CONSTRAINT RICHIESTE_CHIUSURA_PK PRIMARY KEY 
(
  N_CARTELLA 
, ID_SKSO 
, COD_ZONA_RICHIEDENTE 
, DATA_RICHIESTA 
);

alter table SC_BISOGNI add VALUTAZIONE char(1);
alter table HI_SC_BISOGNI add VALUTAZIONE char(1);

alter table SC_BISOGNI drop column AUTONOMIA;
alter table HI_SC_BISOGNI drop column AUTONOMIA;

BEGIN
MKHISTORY_PKG.MK_HI_TRIGGER('SC_BISOGNI');  -- genera nuovamente il trigger sulla singola tabella applicativa indicata come parametro
END;

insert into rm_conf_alert (TIPO_FONTE, NUM_GG_ALERT_LIV1, NUM_GG_ALERT_LIV2)
values ('5', 4, 8);
/* FINE SCRIPT DA ESEGUIRE DAL 12/12/2014 */

/* SCRIPT DA ESEGUIRE DAL 19/12/2014 */

/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 14.02.f2' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
/*aggiunta la frequenza su skso*/
alter table rm_skso add frequenza number(13);
alter table RUGIII_HC modify K3 CHAR(2);
alter table SC_BISOGNI rename column RITMO_NORMALE to RITMO_ALTERATO;
alter table SC_BISOGNI rename column RISCHIO_ASSENTE to RISCHIO_PRESENTE;
alter table HI_RUGIII_HC modify K3 CHAR(2);
alter table HI_SC_BISOGNI rename column RITMO_NORMALE to RITMO_ALTERATO;
alter table HI_SC_BISOGNI rename column RISCHIO_ASSENTE to RISCHIO_PRESENTE;
BEGIN
MKHISTORY_PKG.MK_HI_TRIGGER('SC_BISOGNI');  -- genera nuovamente il trigger sulla singola tabella applicativa indicata come parametro
MKHISTORY_PKG.MK_HI_TRIGGER('RUGIII_HC');  -- genera nuovamente il trigger sulla singola tabella applicativa indicata come parametro
END;
/* FINE SCRIPT DA ESEGUIRE DAL 19/12/2014 */

/* SCRIPT DA ESEGUIRE DAL 22/12/2014 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 14.02.g' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
/*aggiunta la frequenza sulla richiesta mmg */
alter table rm_rich_mmg add frequenza number(13);
-- aggiunto distretto alle richieste di chiusura
alter table RICHIESTE_CHIUSURA add COD_DISTRETTO_PRESACARICO CHAR(6 BYTE);

-- congelamento del PAI
ALTER TABLE RM_SKSO ADD (ISPIANOCONGELATO CHAR(1) );

/* introduzione della corrispondenza tra bisogni e prestazioni per il pai */
CREATE TABLE PRESTAZ_BISOGNI 
(
  ID NUMBER(13) NOT NULL,
  PREST_COD VARCHAR2(8) NOT NULL,
  BISOGNO VARCHAR2(255) NOT NULL,
  FREQUENZA VARCHAR2(8),
  QUANTITA NUMBER(13),
  CONSTRAINT PRESTAZ_BISOGNI_PK PRIMARY KEY (PREST_COD, BISOGNO) 
);

/*eseguire anche lo script associazionePrestazioneBisogni.sql*/

CREATE TABLE PAI(
  N_CARTELLA NUMBER(13, 0) NOT NULL 
, id_skso NUMBER(13, 0) NOT NULL 
, PAI_PROG NUMBER(13, 0) NOT NULL 
, PAI_DATA_INIZIO DATE 
, PAI_DATA_FINE DATE 
, PREST_COD CHAR(8 BYTE) 
, PREST_QTA NUMBER(13, 0) 
, PAI_FREQ VARCHAR2(8 BYTE) 
, PAI_MODALITA VARCHAR2(2000 BYTE) 
, JDBINTERF_VERSION NUMBER(13, 0) 
, JDBINTERF_LASTCNG DATE 
, JISAS_LASTUID NUMBER(13, 0), 
  primary key(n_cartella, id_skso, PAI_PROG)
);


INSERT INTO conf (
    conf_kproc, conf_key, conf_txt, conf_num, conf_date, 
    conf_rem, jdbinterf_version, jdbinterf_lastcng
) VALUES ('SINS', 'ABL_RIC_ANA_CENT', 'NO', 0, SYSDATE, 
    'Abilitazione ricerca su anagrafica centrale', 0, SYSDATE
);

/* FINE SCRIPT DA ESEGUIRE DAL 22/12/2014 */


/* SCRIPT DA ESEGUIRE DAL 07/01/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 14.02.h' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';

/* modificata la chiave per le autorizzazione */


drop table skmmg_adi;
CREATE TABLE SKMMG_ADI(
  N_CARTELLA            NUMBER(13)              NOT NULL,
  N_CONTATTO            NUMBER(13)              NOT NULL,
  SKADI_DATA            DATE                    NOT NULL,
  SKADI_OPERATORE       CHAR(10 BYTE),
  SKADI_MMGPLS          CHAR(16 BYTE),
  SKADI_APPROVA         CHAR(1 BYTE),
  SKADI_MOTIVO          VARCHAR2(200 BYTE),
  SKADI_DATA_INZIO      DATE,
  SKADI_DATA_FINE       DATE,
  SKADI_VERIFICA        DATE,
  SKADI_DATA_RIESAME    DATE,
  SKADI_ATTIV1          CHAR(1 BYTE),
  SKADI_ATTIV2          CHAR(1 BYTE),
  SKADI_ATTTIV3         CHAR(1 BYTE),
  SKADI_ATTIV4          CHAR(1 BYTE),
  SKADI_ATTIV5          CHAR(1 BYTE),
  SKADI_ATTIV6          CHAR(1 BYTE),
  SKADI_ATTIV7          CHAR(1 BYTE),
  SKADI_ATTIV8          CHAR(1 BYTE),
  SKADI_ATTIV9          CHAR(1 BYTE),
  AKADI_ALTRO           CHAR(100 BYTE),
  SKADI_OPER_INF        CHAR(1 BYTE),
  SKADI_OPER_FIS        CHAR(1 BYTE),
  SKADI_MED             CHAR(1 BYTE),
  SKAD_ASS              CHAR(1 BYTE),
  SKADI_OSS             CHAR(1 BYTE),
  SKADI_SPE             CHAR(1 BYTE),
  SKADI_SPE_DES         CHAR(50 BYTE),
  SKADI_ALT             CHAR(1 BYTE),
  SKADI_ALT_DES         CHAR(50 BYTE),
  SKADI_FREQ            NUMBER(13),
  SKADI_FREQ_ALTRO      CHAR(50 BYTE),
  SKADI_FREQ_MENS       NUMBER(13),
  SKADI_DATA_RICOVERO   DATE,
  SKADI_DATA_RSA        DATE,
  SKADI_SPECIFICA       CHAR(1 BYTE),
  SKADI_FINE_EFFETTIVA  CHAR(1 BYTE),
  JDBINTERF_LASTCNG     DATE,
  JDBINTERF_VERSION     NUMBER(13),
  JISAS_LASTUID         NUMBER(13), 
  primary key(n_cartella, n_contatto, skadi_data)
);

drop table SKMMG_ADP;
CREATE TABLE SKMMG_ADP(
  N_CARTELLA            NUMBER(13)              NOT NULL,
  N_CONTATTO            NUMBER(13)              NOT NULL,
  SKADP_DATA            DATE                    NOT NULL,
  SKADP_OPERATORE       CHAR(10 BYTE),
  SKADP_MMGPLS          CHAR(16 BYTE),
  SKADP_ATTIV1          CHAR(1 BYTE),
  SKADP_ATTIV2          CHAR(1 BYTE),
  SKADP_ATTIV3          CHAR(1 BYTE),
  SKADP_ATTIV3_SPEC     VARCHAR2(200 BYTE),
  SKADP_ATTIV4          CHAR(1 BYTE),
  SKADP_ATTIV5          CHAR(1 BYTE),
  SKADP_ATTIV6          CHAR(1 BYTE),
  SKADP_ATTIV7          CHAR(1 BYTE),
  SKADP_ATTIV8          CHAR(1 BYTE),
  SKADP_ATTIV9          CHAR(1 BYTE),
  SKADP_ATTIV10         CHAR(1 BYTE),
  SKADP_ATTIV11         CHAR(1 BYTE),
  SKADP_ATTIV12         CHAR(1 BYTE),
  SKADP_ATTIV12_SPEC    VARCHAR2(200 BYTE),
  SKADP_APPROVA         CHAR(1 BYTE),
  SKADP_MOTIVO          VARCHAR2(200 BYTE),
  SKADP_DATA_INIZIO     DATE,
  SKADP_DATA_FINE       DATE,
  SKADP_VERIFICA        DATE,
  SKADP_DATA_RIESAME    DATE,
  SKADP_FREQ            NUMBER(13),
  SKADP_ALTRO           CHAR(50 BYTE),
  SKADP_FREQ_MENS       NUMBER(13),
  SKADP_DATA_RICOVERO   DATE,
  SKADP_DATA_RSA        DATE,
  SKADP_SPECIFICA       CHAR(1 BYTE),
  SKADP_FINE_EFFETTIVA  CHAR(1 BYTE),
  JDBINTERF_LASTCNG     DATE,
  JDBINTERF_VERSION     NUMBER(13),
  JISAS_LASTUID         NUMBER(13), 
  primary key(n_cartella, n_contatto, skadp_data)
);

drop table skmmg_adr;
CREATE TABLE SKMMG_ADR(
  N_CARTELLA            NUMBER(13) NOT NULL,
  N_CONTATTO            NUMBER(13) NOT NULL,
  SKADR_DATA            DATE       NOT NULL,
  SKADR_OPERATORE       CHAR(10 BYTE),
  SKADR_MMGPLS          CHAR(16 BYTE),
  SKADR_APPROVA         CHAR(1 BYTE),
  SKADR_MOTIVO          VARCHAR2(200 BYTE),
  SKADR_DATA_INIZIO     DATE,
  SKADR_DATA_FINE       DATE,
  SKADR_FREQ            NUMBER(13),
  SKADR_FREQ_ALTRO      CHAR(50 BYTE),
  SKADR_FREQ_MENS       NUMBER(13),
  SKADR_DATA_RICOVERO   DATE,
  SKADR_DATA_RSA        DATE,
  SKADR_ISTITUTO        CHAR(6 BYTE),
  SKADR_SPECIFICA       CHAR(1 BYTE),
  SKADR_FINE_EFFETTIVA  CHAR(1 BYTE),
  JDBINTERF_LASTCNG     DATE,
  JDBINTERF_VERSION     NUMBER(13),
  JISAS_LASTUID         NUMBER(13),
  primary key(n_cartella, n_contatto, skadr_data)
);

ALTER TABLE SC_BISOGNI RENAME COLUMN RIAB_ORTPEDICA TO RIAB_ORTOPEDICA;

/* FINE SCRIPT DA ESEGUIRE DAL 07/01/2015 */

/* SCRIPT DA ESEGUIRE DAL 14/01/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 14.02.i' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';

/* VANNO DISABILITATI I TRIGGER SIA SULLA TABELLA skmedico che sulla tabella ombra */
alter table skmedico modify skm_medico char(16 byte);  
alter table rm_skso_op_coinvolti modify cod_operatore char(16 byte);    
alter table sc_barthel rename column skbt_data to data;

CREATE TABLE LOG_ERRORI_ANAGRAFICA 
(
  ID NUMBER(13, 0) NOT NULL 
, N_CARTELLA NUMBER(13, 0) 
, COD_USL VARCHAR2(32 BYTE) NOT NULL 
, DATA DATE 
, MOTIVO VARCHAR2(2 BYTE) 
, JDBINTERF_LASTCNG DATE 
, TRACE VARCHAR2(2000 BYTE) 
, ORIGINE VARCHAR2(100 BYTE) 
, PRIMARY KEY(ID)
);

/*
SET DEFINE OFF;
Insert into TAB_VOCI
   (TAB_COD, TAB_VAL, TAB_DESCRIZIONE, TAB_FISS, TAB_NUMERO, 
    TAB_CODREG, JDBINTERF_LASTCNG, JDBINTERF_VERSION)
 Values
   ('RMTPCURA', '3', 'Cure Prestazionali R', NULL, NULL, 
    NULL, NULL, NULL);
COMMIT;
*/
/* aggiunta operatore prima visita Fisioterapista */
SET DEFINE OFF;
Insert into TAB_VOCI
   (TAB_COD, TAB_VAL, TAB_DESCRIZIONE, TAB_FISS, TAB_NUMERO, 
    TAB_CODREG, JDBINTERF_LASTCNG, JDBINTERF_VERSION)
 Values
   ('SOOPPRVS', '04', NULL, NULL, NULL, 
    NULL, NULL, NULL);
COMMIT;

--registrazione estrazioni flussi_siad x marche
create table flussi_siad (
mese number(2),
anno number(4),
progr number(4),
data_estrazione date,
cod_operatore char(10 BYTE),
filename varchar2(100),
log_filename varchar2(100),
primary key(mese,anno,progr));

update tab_schede set descrizione = 'A - VALUTAZIONE BISOGNI' where id_scheda = 57;
update tab_schede set descrizione = 'B - SIAD' where id_scheda = 55;
commit;

/* FINE SCRIPT DA ESEGUIRE DAL 14/01/2015 */

/* SCRIPT DA ESEGUIRE DAL 22/01/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 14.02.l' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
/* gestione delle segnalazione degli operatori */
CREATE TABLE RM_SEGNALAZIONI(
  N_CARTELLA         NUMBER(13)                  NOT NULL,
  PROGRESSIVO        NUMBER(13)                  NOT NULL,
  DATA_SEGNALAZIONE  DATE,
  TIPO_OPERATORE     CHAR(8 BYTE), 
  COD_OPERATORE      CHAR(10 BYTE),
  oggetto            VARCHAR2(60 BYTE),
  segnalazione       VARCHAR2(2000 BYTE),
  ID_SKSO            NUMBER(13),
  vista_so 			 char(1),
  PRIMARY KEY(N_CARTELLA, PROGRESSIVO)
);
COMMIT;



/* scala Barthel Index */
INSERT INTO TAB_SCHEDE VALUES (58,'BARTHEL INDEX','SC_BARTHEL','skbt_punt','skbt_data','skbt_nome','ScalaBarthelFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (58,'02','','');	

COMMIT;

/* scala Braden */
INSERT INTO TAB_SCHEDE VALUES (59,'BRADEN','SC_BRADEN','skb_valutazione','skb_data','skb_nome','ScalaBradenFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (59,'02','','');	
COMMIT;
/* scala Conley */
INSERT INTO TAB_SCHEDE VALUES (60,'CONLEY','SC_CONLEY','conley_punt','conley_data','conley_nome','ScalaConleyFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (60,'02','','');
COMMIT;
/* scala SPMSQ */
INSERT INTO TAB_SCHEDE VALUES (61,'SPMSQ','SCP_SPMSQ','scp_spmsq_punt','scp_spmsq_data','scp_spmsq_nome','ScalaSPMSQFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (61,'02','','');	
COMMIT;
/* scala ADL */
INSERT INTO TAB_SCHEDE VALUES (62,'ADL','SC_BADL','badl_punt','badl_data','badl_nome','ScalaADLIndexFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (62,'02','','');	
COMMIT;
/* scala IADL */
INSERT INTO TAB_SCHEDE VALUES (63,'IADL','SC_IADL','iadl_punt','iadl_data','iadl_nome','ScalaIADLFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (63,'02','','');	
COMMIT;
/* scala KARNOFSKY */
INSERT INTO TAB_SCHEDE VALUES (64,'KARNOFSKY','SC_KPS','kps_valore','kps_data','kps_nome','ScalaKarnofskyFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (64,'02','','');	
COMMIT;
/* scala WOUND BED SCORE*/
INSERT INTO TAB_SCHEDE VALUES (65,'WOUND BED SCORE','SC_WOUND','wound_punt','wound_data','wound_nome','ScalaWoundFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (65,'02','','');	
COMMIT;

SET DEFINE OFF;
ALTER TABLE RM_RICH_MMG ADD ADP CHAR(1 BYTE);
ALTER TABLE RM_RICH_MMG ADD ARD CHAR(1 BYTE);
ALTER TABLE RM_RICH_MMG ADD AID CHAR(1 BYTE);
ALTER TABLE RM_RICH_MMG ADD VSD CHAR(1 BYTE);
COMMIT;

SET DEFINE OFF;
ALTER TABLE RM_SKSO_MMG ADD ADP CHAR(1 BYTE);
ALTER TABLE RM_SKSO_MMG ADD ARD CHAR(1 BYTE);
ALTER TABLE RM_SKSO_MMG ADD AID CHAR(1 BYTE);
ALTER TABLE RM_SKSO_MMG ADD VSD CHAR(1 BYTE);
COMMIT;

SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'OBBL_SO_PV', '#1=SI#2=SI#3=NO#4=NO', 0, sysdate, 
    'so obbligo operatore pv se cdi', 0, sysdate);
COMMIT;


alter table rm_skso_mmg ADD b2 CHAR(1 BYTE);
alter table rm_skso_mmg ADD b3 CHAR(1 BYTE);
alter table rm_skso_mmg ADD b4a CHAR(1 BYTE);
alter table rm_skso_mmg ADD b4b CHAR(1 BYTE);
alter table rm_skso_mmg ADD b4c CHAR(1 BYTE);
alter table rm_skso_mmg ADD b4d CHAR(1 BYTE);
alter table rm_skso_mmg ADD b4e CHAR(1 BYTE);


alter table rm_rich_mmg ADD b2 CHAR(1 BYTE);
alter table rm_rich_mmg ADD b3 CHAR(1 BYTE);
alter table rm_rich_mmg ADD b4a CHAR(1 BYTE);
alter table rm_rich_mmg ADD b4b CHAR(1 BYTE);
alter table rm_rich_mmg ADD b4c CHAR(1 BYTE);
alter table rm_rich_mmg ADD b4d CHAR(1 BYTE);
alter table rm_rich_mmg ADD b4e CHAR(1 BYTE);


ALTER TABLE PAI 
ADD (TIPOCURA NUMBER(2) DEFAULT 1 );

CREATE TABLE AGENDANT_TEMPIKM 
(
  AG_OPER_ESEC CHAR(10) NOT NULL, 
  AG_DATA DATE NOT NULL ,
  AG_KM NUMBER(13), 
  AG_MINUTI NUMBER(13), 
  PRIMARY KEY(AG_OPER_ESEC, AG_DATA)
);

COMMENT ON TABLE AGENDANT_TEMPIKM IS 'Tabella per la memorizzazione dei kilometri percorsi e dei minuti impiegati per gli operatori che effettuano accessi.';
COMMENT ON COLUMN AGENDANT_TEMPIKM.AG_OPER_ESEC IS 'Operatore per il quale si registrano le ore e i chilometri';
COMMENT ON COLUMN AGENDANT_TEMPIKM.AG_DATA IS 'Data in cui sono stati percorsi i chilometri e lavorate le ore';
COMMENT ON COLUMN AGENDANT_TEMPIKM.AG_KM IS 'Kilometri percorsi';
COMMENT ON COLUMN AGENDANT_TEMPIKM.AG_MINUTI IS 'Minuti impiegati';
/* FINE SCRIPT DA ESEGUIRE DAL 22/01/2015 */

/* SCRIPT DA ESEGUIRE DAL 27/01/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 14.02.m' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';

set define off;
alter table rm_skso_op_coinvolti drop column num_acces_set;
commit;
/* rimosso il campo num_acces_set */
--
-- HITRG_RM_SKSO_OP_COINVOLTI  (Trigger) 
--
CREATE OR REPLACE TRIGGER HITRG_RM_SKSO_OP_COINVOLTI BEFORE INSERT OR UPDATE OR DELETE
ON RM_SKSO_OP_COINVOLTI REFERENCING NEW AS n OLD AS o
FOR EACH ROW
DECLARE
    hiopv CHAR(1):=case when inserting then 'I' when updating then 'U' else 'D'end;
    fl NUMBER :=0; 
BEGIN 
--decode(a,b,1,0)=0
--equivale a
--not( (a is null and b is null) or (a = b))
    select case when hiopv='I' or hiopv='D' or
        decode(:n.N_CARTELLA,:o.N_CARTELLA,1,0)=0 or
        decode(:n.ID_SKSO,:o.ID_SKSO,1,0)=0 or
        decode(:n.TIPO_OPERATORE,:o.TIPO_OPERATORE,1,0)=0 or
        decode(:n.COD_DISTRETTO,:o.COD_DISTRETTO,1,0)=0 or
        decode(:n.DT_PRESA_CARICO,:o.DT_PRESA_CARICO,1,0)=0 or
        decode(:n.DT_CHIUSURA,:o.DT_CHIUSURA,1,0)=0 or
        decode(:n.DT_INIZIO_PIANO,:o.DT_INIZIO_PIANO,1,0)=0 or
        decode(:n.DT_FINE_PIANO,:o.DT_FINE_PIANO,1,0)=0 or
    --    decode(:n.NUM_ACCES_SET,:o.NUM_ACCES_SET,1,0)=0 or
        decode(:n.COD_OPERATORE,:o.COD_OPERATORE,1,0)=0 or
        decode(:n.EXTRA,:o.EXTRA,1,0)=0 or
        decode(:n.COD_ZONA,:o.COD_ZONA,1,0)=0 or
        decode(:n.COD_PRESIDIO,:o.COD_PRESIDIO,1,0)=0 or
        decode(:n.VISTA_DA_SO,:o.VISTA_DA_SO,1,0)=0
         then 1 else 0 end into fl from dual;
    IF fl = 1 THEN 
        IF inserting or updating THEN 
            INSERT INTO HI_RM_SKSO_OP_COINVOLTI (
                hi_date, hi_op, N_CARTELLA,ID_SKSO,TIPO_OPERATORE,COD_DISTRETTO,DT_PRESA_CARICO,DT_CHIUSURA,DT_INIZIO_PIANO,DT_FINE_PIANO,COD_OPERATORE,EXTRA,JDBINTERF_VERSION,JDBINTERF_LASTCNG,COD_ZONA,COD_PRESIDIO,VISTA_DA_SO,JISAS_LASTUID
            ) VALUES ( 
                systimestamp,hiopv, :n.N_CARTELLA,:n.ID_SKSO,:n.TIPO_OPERATORE,:n.COD_DISTRETTO,:n.DT_PRESA_CARICO,:n.DT_CHIUSURA,:n.DT_INIZIO_PIANO,:n.DT_FINE_PIANO,:n.COD_OPERATORE,:n.EXTRA,:n.JDBINTERF_VERSION,:n.JDBINTERF_LASTCNG,:n.COD_ZONA,:n.COD_PRESIDIO,:n.VISTA_DA_SO,:n.JISAS_LASTUID
            );
        ELSE
            INSERT INTO HI_RM_SKSO_OP_COINVOLTI (
                hi_date, hi_op, N_CARTELLA,ID_SKSO,TIPO_OPERATORE,COD_DISTRETTO,DT_PRESA_CARICO,DT_CHIUSURA,DT_INIZIO_PIANO,DT_FINE_PIANO,COD_OPERATORE,EXTRA,JDBINTERF_VERSION,JDBINTERF_LASTCNG,COD_ZONA,COD_PRESIDIO,VISTA_DA_SO,JISAS_LASTUID
            ) VALUES ( 
                systimestamp,hiopv, :o.N_CARTELLA,:o.ID_SKSO,:o.TIPO_OPERATORE,:o.COD_DISTRETTO,:o.DT_PRESA_CARICO,:o.DT_CHIUSURA,:o.DT_INIZIO_PIANO,:o.DT_FINE_PIANO,:o.COD_OPERATORE,:o.EXTRA,:o.JDBINTERF_VERSION,:o.JDBINTERF_LASTCNG,:o.COD_ZONA,:o.COD_PRESIDIO,:o.VISTA_DA_SO,:o.JISAS_LASTUID
            );
        END IF;
    END IF;
EXCEPTION 
    WHEN OTHERS THEN 
        RAISE; 
END;


/* FINE SCRIPT DA ESEGUIRE DAL 27/01/2015 */



/* SCRIPT DA ESEGUIRE DAL 28/01/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 14.02.n' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';


/* scala Barthel Index */
INSERT INTO TAB_SCHEDE VALUES (58,'BARTHEL INDEX','SC_BARTHEL','skbt_punt','skbt_data','skbt_nome','ScalaBarthelFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (58,'02','','');	

COMMIT;

/* scala Braden */
INSERT INTO TAB_SCHEDE VALUES (59,'BRADEN','SC_BRADEN','skb_valutazione','skb_data','skb_nome','ScalaBradenFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (59,'02','','');	
COMMIT;
/* scala Conley */
INSERT INTO TAB_SCHEDE VALUES (60,'CONLEY','SC_CONLEY','conley_punt','conley_data','conley_nome','ScalaConleyFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (60,'02','','');
COMMIT;
/* scala SPMSQ */
INSERT INTO TAB_SCHEDE VALUES (61,'SPMSQ','SCP_SPMSQ','scp_spmsq_punt','scp_spmsq_data','scp_spmsq_nome','ScalaSPMSQFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (61,'02','','');	
COMMIT;
/* scala ADL */
INSERT INTO TAB_SCHEDE VALUES (62,'ADL','SC_BADL','badl_punt','badl_data','badl_nome','ScalaADLIndexFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (62,'02','','');	
COMMIT;
/* scala IADL */
INSERT INTO TAB_SCHEDE VALUES (63,'IADL','SC_IADL','iadl_punt','iadl_data','iadl_nome','ScalaIADLFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (63,'02','','');	
COMMIT;
/* scala KARNOFSKY */
INSERT INTO TAB_SCHEDE VALUES (64,'KARNOFSKY','SC_KPS','kps_valore','kps_data','kps_nome','ScalaKarnofskyFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (64,'02','','');	
COMMIT;
/* scala WOUND BED SCORE*/
INSERT INTO TAB_SCHEDE VALUES (65,'WOUND BED SCORE','SC_WOUND','wound_punt','wound_data','wound_nome','ScalaWoundFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (65,'02','','');	
COMMIT;

/* AGGIUNTA DEL CAMPO TEMPO_T e DATA_TEST in SC_BARTHEL */
ALTER TABLE sc_barthel ADD tempo_t NUMBER(13);
ALTER TABLE sc_barthel ADD data_scheda DATE;

alter table sc_barthel rename column skbt_data to data;
/* AGGIUNTA DEL CAMPO TEMPO_T e DATA_TEST in SC_BARTHEL */
ALTER TABLE sc_braden ADD tempo_t NUMBER(13);
ALTER TABLE sc_braden ADD data_scheda DATE;
ALTER TABLE sc_braden ADD data_test DATE;
ALTER TABLE sc_braden ADD nome VARCHAR(200 Byte);
alter table sc_braden rename column skb_data to data;
/* AGGIUNTA DEL CAMPO DATA_SCHEDA in SC_CONLEY */
ALTER TABLE sc_conley ADD data_scheda DATE;
/*AGGIUNTA TABELLA PER LA SCHEDA SPMSQ*/
CREATE TABLE SCP_SPMSQ
(
  N_CARTELLA         NUMBER(13)                 NOT NULL,
  DATA               DATE                       NOT NULL,
  COD_OPERATORE      CHAR(10 BYTE),
  D01                NUMBER(13),
  D02                NUMBER(13),
  D03                NUMBER(13),
  D04                NUMBER(13),
  D11               NUMBER(13),
  D05                NUMBER(13),
  D06                NUMBER(13),
  D07                NUMBER(13),
  D08                NUMBER(13),
  D09                NUMBER(13),
  D10                NUMBER(13),
  SCUOLA             NUMBER(13),
  DATA_TEST          DATE,
  DATA_SCHEDA        DATE,
  NOME               VARCHAR2(200 BYTE),
  SCP_SPMSQ_PUNT     NUMBER(13),
  TEMPO_T            NUMBER(13),
  JDBINTERF_LASTCNG  DATE,
  JDBINTERF_VERSION  NUMBER(13),
  NOTE               VARCHAR2(200 BYTE),
  OPERATORE_MOD      CHAR(10 BYTE),
  MOD_DATA           DATE
);


/* AGGIUNTA DEL CAMPO TEMPO_T in SC_KPS */
ALTER TABLE sc_kps ADD tempo_t  NUMBER(13);
/*AGGIUNTA TABELLA PER LA STAMPA  WOUND BED SCORE*/
CREATE TABLE SC_WOUND
(
  N_CARTELLA         NUMBER(13)                 NOT NULL,
  DATA               DATE                       NOT NULL,
  COD_OPERATORE      CHAR(10 BYTE),
  SEDE               CHAR(1 BYTE),
  SEDE2              CHAR(1 BYTE),
  SEDE3              CHAR(1 BYTE),
  SEDE4              CHAR(1 BYTE),
  SEDE5              CHAR(1 BYTE),
  SEDE6              CHAR(1 BYTE),
  SEDE7              CHAR(1 BYTE),
  SEDE8              CHAR(1 BYTE),
  SEDE9              CHAR(1 BYTE),
  SEDE10             CHAR(1 BYTE),
  SEDE11             CHAR(1 BYTE),
  SEDE12             CHAR(1 BYTE),
  SEDE13             CHAR(1 BYTE),
  SEDE14             CHAR(1 BYTE),
  SEDE15             CHAR(1 BYTE),
  SEDE16             CHAR(1 BYTE),
  SEDE2_SD           CHAR(1 BYTE),  
  SEDE4_SD           CHAR(1 BYTE),  
  SEDE6_SD           CHAR(1 BYTE),  
  SEDE7_SD           CHAR(1 BYTE),  
  SEDE8_SD           CHAR(1 BYTE),  
  SEDE9_SD           CHAR(1 BYTE), 
  SEDE10_SD          CHAR(1 BYTE),  
  SEDE11_SD          CHAR(1 BYTE), 
  SEDE12_TESTO       VARCHAR2(200 BYTE),
  SEDE13_TESTO       VARCHAR2(200 BYTE),
  SEDE14_IE          CHAR(1 BYTE),  
  SEDE15_IE          CHAR(1 BYTE), 
  SEDE16_SD          CHAR(1 BYTE),  
  SEDE1_G            CHAR(1 BYTE),  
  SEDE2_G            CHAR(1 BYTE),  
  SEDE3_G            CHAR(1 BYTE),  
  SEDE4_G            CHAR(1 BYTE),
  SEDE5_G            CHAR(1 BYTE),
  SEDE6_G            CHAR(1 BYTE),
  SEDE7_G            CHAR(1 BYTE),
  SEDE8_G            CHAR(1 BYTE),
  SEDE9_G            CHAR(1 BYTE),
  SEDE10_G           CHAR(1 BYTE),
  SEDE11_G           CHAR(1 BYTE),
  SEDE12_G           CHAR(1 BYTE),
  SEDE13_G           CHAR(1 BYTE),
  SEDE14_G           CHAR(1 BYTE),
  SEDE15_G           CHAR(1 BYTE),
  SEDE16_G           CHAR(1 BYTE),
  LESIONI            CHAR(1 BYTE),
  SEDE17             CHAR(1 BYTE),
  SEDE18             CHAR(1 BYTE),
  SEDE19             CHAR(1 BYTE),
  SEDE20             CHAR(1 BYTE),
  SEDE17_TESTO       VARCHAR2(200 BYTE),
  SEDE18_TESTO       VARCHAR2(200 BYTE),
  SEDE19_IEMA        CHAR(1 BYTE),
  SEDE20_IEMA        CHAR(1 BYTE),
  SEDE17_G           CHAR(1 BYTE),
  SEDE18_G           CHAR(1 BYTE),
  SEDE19_G           CHAR(1 BYTE),
  SEDE20_G           CHAR(1 BYTE),
  ALTRE_LES          CHAR(1 BYTE),
  LESIONE1           VARCHAR2(200 BYTE),
  LESIONE2           VARCHAR2(200 BYTE),
  INFEZIONI          CHAR(1 BYTE),
  DOMANDA1           CHAR(1 BYTE),
  DOMANDA2           CHAR(1 BYTE),
  DOMANDA3           CHAR(1 BYTE),
  DOMANDA4           CHAR(1 BYTE),
  DOMANDA5           CHAR(1 BYTE),
  DOMANDA6           CHAR(1 BYTE),
  DOMANDA7           CHAR(1 BYTE),
  DOMANDA8           CHAR(1 BYTE),  
  DATA_TEST          DATE,  
  NOME               VARCHAR2(200 BYTE),
  WOUND_PUNT         NUMBER(13),
  TEMPO_T            NUMBER(13),
  JDBINTERF_LASTCNG  DATE,
  JDBINTERF_VERSION  NUMBER(13),
  NOTE               VARCHAR2(200 BYTE),
  OPERATORE_MOD      CHAR(10 BYTE),
  MOD_DATA           DATE
);    



/* SCRIPT DA ESEGUIRE DAL 28/01/2015 */


/* SCRIPT DA ESEGUIRE DAL 30/01/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 15.01' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';

SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'GG_VISTA_SEGN', '#1=5#2=4#3=3#4=5', 0, sysdate, 
    'gg fino a quando far vedere le segnalazioni', 0, sysdate);
COMMIT;



alter table rm_skso_op_coinvolti add cod_operatore_pc char(10 byte);
alter table sc_bisogni add convalida_uvi date;

DROP SEQUENCE SEQ_PROGRESSIVO_COD_MEDICO;
CREATE SEQUENCE SEQ_PROGRESSIVO_COD_MEDICO
  START WITH 101906
  MAXVALUE 9999999999999
  MINVALUE 1
  NOCYCLE
  NOCACHE 
  ORDER;

/* FINE SCRIPT DA ESEGUIRE DAL 30/01/2015 */

/* SCRIPT DA ESEGUIRE DAL 02/02/2015 */

INSERT INTO TAB_SCHEDE VALUES (66,'RILEVAZIONE DEL DOLORE','SC_RIL_DOLORE_BAM','dol_punt','dol_data','dol_nome','ScalaDoloreBamFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (66,'02','','');	

INSERT INTO TAB_SCHEDE VALUES (67,'RILEVAZIONE DEL DOLORE','SC_RIL_DOLORE_AD','dol_punt_a','dol_data_a','dol_nome_a','ScalaDoloreAdFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (67,'02','','');	


COMMIT;

CREATE TABLE SC_RIL_DOLORE_BAM
(
  N_CARTELLA         NUMBER(13)                 NOT NULL,
  DATA               DATE                       NOT NULL,
  COD_OPERATORE      CHAR(10 BYTE),
  DOMANDA_B1         CHAR(2 BYTE),
  DOMANDA_B2         CHAR(2 BYTE),
  DOMANDA_B3         CHAR(1 BYTE),
  DOMANDA_B4         CHAR(1 BYTE),
  DOMANDA_B5         CHAR(1 BYTE),
  DOMANDA_B6         CHAR(1 BYTE),
  DOMANDA_B7         CHAR(1 BYTE),    
  DATA_TEST          DATE,  
  NOME               VARCHAR2(200 BYTE),
  DOL_PUNT           NUMBER(13),
  TEMPO_T            NUMBER(13),
  JDBINTERF_LASTCNG  DATE,
  JDBINTERF_VERSION  NUMBER(13),
  NOTE               VARCHAR2(200 BYTE),
  OPERATORE_MOD      CHAR(10 BYTE),
  MOD_DATA           DATE
);    

CREATE TABLE SC_RIL_DOLORE_AD
(
  N_CARTELLA         NUMBER(13)                 NOT NULL,
  DATA               DATE                       NOT NULL,
  COD_OPERATORE      CHAR(10 BYTE),
  TIPO_SCALA         CHAR(1 BYTE),
  DOMANDA_A1         CHAR(2 BYTE),
  DOMANDA_A2         CHAR(1 BYTE),
  DOMANDA_A3         CHAR(1 BYTE),
  DOMANDA_A4         CHAR(1 BYTE),
  DOMANDA_A5         CHAR(1 BYTE),
  DOMANDA_A6         CHAR(1 BYTE),    
  DATA_TEST          DATE,  
  NOME               VARCHAR2(200 BYTE),
  DOL_PUNT_A         NUMBER(13),
  TEMPO_T            NUMBER(13),
  JDBINTERF_LASTCNG  DATE,
  JDBINTERF_VERSION  NUMBER(13),
  NOTE               VARCHAR2(200 BYTE),
  OPERATORE_MOD      CHAR(10 BYTE),
  MOD_DATA           DATE
);    

SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'PROP_DT_SCHEDA', '#1=NO#2=NO#3=SI#4=NO', 0, sysdate, 
    'proporre la data delle scheda SO/RICHMMG', 0, sysdate);
COMMIT;


alter table RM_SKSO add FLAG_SENT number(1);
alter table RM_SKSO add DATA_SENT date;
alter table RM_SKSO add FLAG_CONCL_SENT number(1);
alter table RM_SKSO add DATA_CONCL_SENT date;


alter table RM_SKSO_SOSPENSIONI add FLAG_SENT number(1);
alter table RM_SKSO_SOSPENSIONI add DATA_SENT date;

alter table SC_BISOGNI add FLAG_SENT number(1);
alter table SC_BISOGNI add DATA_SENT date;

alter table INTERV rename column flag_sended to FLAG_SENT;
alter table INTERV rename column data_sended to DATA_SENT;
alter table INTMMG rename column flag_sended to FLAG_SENT;
alter table INTMMG rename column data_sended to DATA_SENT;

alter table SC_BISOGNI add CONFERMA number(1);

update conf set conf_txt = 'SI' where conf_key = 'FLUX_MIN_NOANONI';
commit;

drop table flussi_siad;
-- Create table
create table FLUSSI_SIAD
(
  MESE            NUMBER(2) not null,
  ANNO            NUMBER(4) not null,
  PROGR           NUMBER(4) not null,
  TIPO            NUMBER(1) not null,
  DATA_ESTRAZIONE DATE,
  COD_OPERATORE   CHAR(10 BYTE),
  FILENAME        VARCHAR2(100)
)
;
-- Create/Recreate primary, unique and foreign key constraints 
alter table FLUSSI_SIAD
  add primary key (MESE, ANNO, PROGR, TIPO);

/* FINE SCRIPT DA ESEGUIRE DAL 02/02/2015 */

/* SCRIPT DA ESEGUIRE DAL 06/02/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 15.01.a' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
  
SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'ABL_DT_VAL_BIS', '#1=SI#2=SI#3=SI#4=NO', 0, sysdate, 
    'inserisce la data valutazione nella scala bisogni', 0, sysdate);
COMMIT;

/* FINE SCRIPT DA ESEGUIRE DAL 06/02/2015 */


/* SCRIPT DA ESEGUIRE DAL 09/02/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 15.01.b' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
  
SET DEFINE OFF;
ALTER TABLE SC_BISOGNI  
MODIFY (BIS_AUTONOMIA DEFAULT 1 );

CREATE TABLE OBIETTIVI_BISOGNI 
   (	ID NUMBER NOT NULL ENABLE, 
	OBIETTIVO VARCHAR2(10 BYTE) NOT NULL ENABLE, 
	BISOGNO VARCHAR2(255 BYTE) NOT NULL ENABLE, 
	EDITABILE CHAR(1 BYTE), 
	CONDIZIONEAGG VARCHAR2(30 BYTE), 
	PRIMARY KEY (OBIETTIVO, BISOGNO));

   COMMENT ON TABLE OBIETTIVI_BISOGNI  IS 'Associazione tra i bisogni indicati nella valutazione e gli obiettivi.';

CREATE TABLE OBIETTIVI_PAI 
   (	N_CARTELLA NUMBER(13,0) NOT NULL ENABLE, 
	ID_SKSO NUMBER(13,0) NOT NULL ENABLE, 
	OBIETTIVI_PROG NUMBER(13,0) NOT NULL ENABLE, 
	OBIETTIVO_COD CHAR(8 BYTE), 
	OBIETTIVO VARCHAR2(2000 BYTE), 
	EDITABILE CHAR(1 BYTE), 
	CHECKED CHAR(1 BYTE), 
	JDBINTERF_VERSION NUMBER(13,0), 
	JDBINTERF_LASTCNG DATE, 
	JISAS_LASTUID NUMBER(13,0), 
	 PRIMARY KEY (N_CARTELLA, ID_SKSO, OBIETTIVI_PROG));

   COMMENT ON TABLE OBIETTIVI_BISOGNI  IS 'Obiettivi associati alla scheda id_skso per la cartella n_cartella.';
   COMMIT;

   
alter table INTMMG modify FLAG_SENT default 0;
alter table INTERV modify FLAG_SENT default 0;
alter table RM_SKSO modify FLAG_SENT default 0;
alter table RM_SKSO_SOSPENSIONI modify FLAG_SENT default 0;
alter table SC_BISOGNI modify FLAG_SENT default 0;
alter table RM_SKSO modify FLAG_CONCL_SENT default 0;

   
/* FINE SCRIPT DA ESEGUIRE DAL 09/02/2015 */

drop tabel OBIETTIVI_BISOGNI;
CREATE TABLE OBIETTIVI_BISOGNI 
	(LIVELLO NUMBER(4,0) NOT NULL ENABLE, 
   	OBIETTIVO VARCHAR2(10 BYTE) NOT NULL ENABLE, 
	BISOGNO VARCHAR2(255 BYTE) NOT NULL ENABLE, 
	CONDIZIONEAGG VARCHAR2(30 BYTE), 
	EDITABILE CHAR(1 BYTE), 
 CONSTRAINT OBIETTIVI_BISOGNI_PK PRIMARY KEY (LIVELLO, OBIETTIVO, BISOGNO)
);
COMMENT ON TABLE OBIETTIVI_BISOGNI  IS 'Associazione tra i bisogni indicati nella valutazione e gli obiettivi.';

DROP TABLE OBIETTIVI_PAI;
CREATE TABLE OBIETTIVI_PAI 
   (N_CARTELLA NUMBER(13,0) NOT NULL ENABLE, 
	ID_SKSO NUMBER(13,0) NOT NULL ENABLE, 
	OBIETTIVI_PROG NUMBER(13,0) NOT NULL ENABLE, 
	LIVELLO NUMBER(5,0), 
	OBIETTIVO CHAR(8 BYTE), 
	OBIETTIVO_DESC VARCHAR2(2000 BYTE), 
	CHECKED CHAR(1 BYTE), 
	EDITABILE CHAR(1 BYTE), 
	JDBINTERF_VERSION NUMBER(13,0), 
	JDBINTERF_LASTCNG DATE, 
	JISAS_LASTUID NUMBER(13,0), 
	PRIMARY KEY (N_CARTELLA, ID_SKSO, OBIETTIVI_PROG)
	);

	
update CONF (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values ('SINS', 'ABL_DT_VAL_BIS', '#1=SI#2=SI#3=SI#4=NO', 0, sysdate, 'inserisce la data valutazione nella scala bisogni', 0, sysdate);
 
 update conf set conf_txt = 'Vers. 15.01.c' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
 
 alter table FLUSSI_SIAD add ZONA char(1 BYTE);
alter table FLUSSI_SIAD add CONVALIDA number(1);

alter table INTERV add progr_sent number(13);
alter table sc_bisogni add progr_sent number(13);
alter table intmmg add progr_sent number(13);
alter table rm_skso add progr_sent number(13);
alter table rm_skso add progr_concl_sent number(13);
alter table rm_skso_sospensioni add progr_sent number(13);

ALTER TABLE PAI 
ADD (PIANIFICAZIONE VARCHAR2(20) DEFAULT 'NNNNNNN#NNNNNNN#NNNNNNN#NNNNNNN#' );


/* SCRIPT DA ESEGUIRE DAL 26/02/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 15.01.c' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';

SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'GG_VAL_BIS', '#1=90#2=90#3=90#4=90', 0, sysdate, 
    'gg da far vedere le valutazioni BISOGNI', 0, sysdate);
COMMIT;
/* FINE SCRIPT DA ESEGUIRE DAL 26/02/2015 */


/* SCRIPT DA ESEGUIRE DAL 10/03/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 15.01.d' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';

SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'GG_PREGR_PRS', '#1=10#2=10#3=10#4=20', 0, sysdate, 
    'gg pregressi prestazioni ', 0, sysdate);
COMMIT;
/* FINE SCRIPT DA ESEGUIRE DAL 10/03/2015 */

/* SCRIPT DA ESEGUIRE DAL 11/03/2015 */
/* scala RUGIII */
INSERT INTO TAB_SCHEDE VALUES (56,'RUG III - HC','RUGIII_HC','rug_punt','rug_data','rug_nome','ScalaRUGFormCtrl','N','S','','');
INSERT INTO TAB_SCHEDE_CONT VALUES (56,'02','','');	
INSERT INTO TAB_SCHEDE_CONT VALUES (56,'00','','');	
INSERT INTO TAB_SCHEDE_CONT VALUES (56,'03','','');	
COMMIT;
/* FINE SCRIPT DA ESEGUIRE DAL 11/03/2015 */



/* SCRIPT DA ESEGUIRE DAL 16/03/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 15.01.e' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';

alter table FLUSSI_SIAD add distretto char(6 BYTE);
-- Add/modify columns 
alter table CONF modify CONF_REM CHAR(300 BYTE);
insert into conf (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
values ('SINS', 'SIAD_X_AREA', 'NO', 0, to_date('16-03-2015', 'dd-mm-yyyy'), 'abilita l''estrazione dei flussi siad per area vasta anzichÃ© per distretto.', 0, to_date('16-03-2015', 'dd-mm-yyyy'));


COMMIT;
SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'LA_NO_FONTE', '#1=1,4,5,7,8#2=1,4,5,7,8#3=1,4,5,7,8#4=1,4,5,7,8#5=1,4,5,7,8', 0, sysdate, 
    'fonte da non mostrare per i vari operatori', 0, sysdate);
COMMIT;
/* FINE SCRIPT DA ESEGUIRE DAL 16/03/2015 */

/* SCRIPT DA ESEGUIRE DAL 26/03/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 15.01.f' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
COMMIT;


alter table sc_bisogni add nome CHAR(10 BYTE);

update sc_bisogni set nome = cod_operatore where nome is null;
update scl_valutazione a set nome = (select cod_operatore from sc_bisogni b where a.n_cartella = b.n_cartella and a.data = b.data) where nome is null;
update rugiii_hc set nome = cod_operatore where nome is null;
commit;

/* FINE SCRIPT DA ESEGUIRE DAL 26/03/2015 */

/* SCRIPT DA ESEGUIRE DAL 15/04/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 15.03' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
COMMIT;

alter table rm_skso_op_coinvolti add no_alert CHAR(1 BYTE);
alter table rm_skso_op_coinvolti add op_cognome char(30 byte);
alter table rm_skso_op_coinvolti add op_nome char(30 byte);

COMMIT;
/* SCRIPT DA ESEGUIRE DAL 15/04/2015 */

/* SCRIPT DA ESEGUIRE DAL 20/04/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 15.03.a' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';

SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'CTR_AUTO_WEB', 'SI', 0, sysdate,'controllo sull''autorizzato delle prestazioni MMG', 0, sysdate);
   
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'CTR_SOSP_WEB', 'SI', 0, sysdate,'controllo sulla sospensione delle prestazioni MMG', 0, sysdate);
COMMIT;
/* FINE SCRIPT DA ESEGUIRE DAL 20/04/2015 */

/* SCRIPT DA ESEGUIRE DAL 22/04/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 15.03.b' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
/* date di rivalutazione uvi */
alter table rm_skso_proroghe add dt_proroga_uvi DATE;


update  rm_skso_proroghe 
set dt_proroga_uvi = dt_proroga_fine
where dt_proroga_uvi is null;

ALTER TABLE PAI 
ADD (PAI_COD_OPERATORE VARCHAR2(10) );
COMMIT;
/*FINE SCRIPT DA ESEGUIRE DAL 22/04/2015 */


/* SCRIPT DA ESEGUIRE DAL 12/05/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 15.03.c' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
/* date di rivalutazione uvi */

INSERT INTO conf(conf_kproc, conf_key, conf_txt, conf_num, conf_date, conf_rem, jdbinterf_version, jdbinterf_lastcng)
     VALUES ('SINS', 'ABL_SO_CG_DIS_SD', 'NO', 0, SYSDATE,'abilitazione per il cambio del distretto e sede nella SO', 0,SYSDATE );

--Elimino vecchie transcodifiche
DELETE FROM TRCOD_PIPP WHERE EST_TIPO IN('P','R');
--Inserisco nuove transcodifiche
INSERT INTO TRCOD_PIPP (EST_TIPO,EST_CODI,PIPP_TIPO,PIPP_CODI,JDBINTERF_LASTCNG)
	SELECT p.PIPP_TIPO, p.PIPP_CODI, p.PIPP_TIPO, p.PIPP_CODI,sysdate
	FROM TABPIPP p
	WHERE p.PIPP_TIPO IN(1,2,3);--1 Medico generico, 2 Pediatra     
     
COMMIT;
/*FINE SCRIPT DA ESEGUIRE DAL 22/04/2015 */


/* SCRIPT DA ESEGUIRE DAL 21/05/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 15.04' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
--propagazione dell'intensità assistenziale all'agenda

INSERT INTO CONF (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
VALUES ('SINS', 'PRESTPREL', '''INF6''', '0', TO_DATE('2015-05-26 09:07:54', 'YYYY-MM-DD HH24:MI:SS'), 'Codici delle prestazioni di tipo prelievo separati da virgole es:"''INF140'',''INF6'',''INF8''"', '0', TO_DATE('2015-05-26 09:08:33', 'YYYY-MM-DD HH24:MI:SS'));

--
--	DEFINIZIONE COLONNA JISAS_LASTUID
--
ALTER TABLE inpmmg ADD jisas_lastuid NUMBER(13);
ALTER TABLE medici ADD jisas_lastuid NUMBER(13);
ALTER TABLE RM_DIARIO ADD jisas_lastuid NUMBER(13);
ALTER TABLE RM_INTOLLERANZE_ALLERGIE ADD jisas_lastuid NUMBER(13);
ALTER TABLE RM_SEGNALAZIONI ADD jisas_lastuid NUMBER(13);
--
--	VALORIZZAZIONE COLONNA JISAS_LASTUID
--
UPDATE inpmmg SET jisas_lastuid = 0;
UPDATE medici SET jisas_lastuid = 0;
UPDATE RM_DIARIO SET jisas_lastuid = 0;
UPDATE RM_INTOLLERANZE_ALLERGIE SET jisas_lastuid = 0;
UPDATE RM_SEGNALAZIONI SET jisas_lastuid = 0;

BEGIN
MKHISTORY_PKG.MK_HI_SINGLETAB('inpmmg');
MKHISTORY_PKG.MK_HI_SINGLETAB('medici');
MKHISTORY_PKG.MK_HI_SINGLETAB('PAI');
MKHISTORY_PKG.MK_HI_SINGLETAB('RM_DIARIO');
MKHISTORY_PKG.MK_HI_INDEX('RM_INTOLLERANZE_ALLERGIE','hi_RM_INTOLLERANZE_ALL_IDX');
MKHISTORY_PKG.MK_HI_TRIGGER('RM_INTOLLERANZE_ALLERGIE');
MKHISTORY_PKG.MK_HI_SINGLETAB('RM_SEGNALAZIONI');
END;
COMMIT;
/*FINE SCRIPT DA ESEGUIRE DAL 21/05/2015 */

/*SCRIPT DA ESEGUIRE DAL 09/06/2015 */
alter table sc_wound add sede19_testo CHAR(200 BYTE);
alter table sc_wound add sede20_testo char(200 byte);
/*FINE SCRIPT DA ESEGUIRE DAL 09/06/2015 */

/*SCRIPT DA ESEGUIRE DAL 23/09/2015 */
CREATE TABLE MMG_CERTIF
(
  N_CARTELLA      NUMBER(13)               NOT NULL,
  COD_TIPO_CERTIF CHAR(10)                 NOT NULL,
  PROGR_INSE      NUMBER(13)               NOT NULL,
  PROGR_MODI      NUMBER(13)               NOT NULL,
  MMG_INSE        CHAR(10),
  DATA_INSE       DATE,
  ORA_INSE        CHAR(5),
  MMG_MODI        CHAR(10),
  DATA_MODI       DATE,
  ORA_MODI        CHAR(5),
  DATA_CERTIF     DATE,
  TESTO           CLOB,
  JISAS_LASTUID   NUMBER(13),
  PRIMARY KEY (N_CARTELLA, COD_TIPO_CERTIF, PROGR_INSE, PROGR_MODI)
);
CREATE TABLE MMG_CERTIF_TEMPL
(
  COD_TIPO_CERTIF CHAR(10)                 NOT NULL,
  PROGR_MODI      NUMBER(13)               NOT NULL,
  DESCRIZIONE     VARCHAR2(300),
  OP_INSE         CHAR(10),
  DATA_INSE       DATE,
  ORA_INSE        CHAR(5),
  OP_MODI         CHAR(10),
  DATA_MODI       DATE,
  ORA_MODI        CHAR(5),
  TESTO           CLOB,
  JISAS_LASTUID   NUMBER(13),
  PRIMARY KEY (COD_TIPO_CERTIF, PROGR_MODI)
);
/*FINE SCRIPT DA ESEGUIRE DAL 23/09/2015 */

/* SCRIPT DA ESEGUIRE DAL 10/11/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 15.05' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';

/* per la gestione della residenzialita */
set define off;
CREATE TABLE ZK_RSA_RICHIESTA(
  N_CARTELLA             NUMBER(13),
  ID_DOMANDA             NUMBER(13),
  ID_RICHIESTA           NUMBER(13),
  TIPO_RICOVERO          VARCHAR2(1 BYTE),
  COD_ORG                CHAR(3 BYTE),
  LIVELLO_URGENZA        VARCHAR2(8 BYTE),
  TIPO_ISTITUTO          CHAR(1 BYTE),
  PUNTEGGIO_SOCIALE      NUMBER(13), 
  PUNTEGGIO_SANITARIO    NUMBER(13),   
  PUNTEGGIO_TOTALE       NUMBER(13),
  PUNTEGGIO_FASCIA       NUMBER(13),
  JDBINTERF_VERSION      NUMBER(13),
  JDBINTERF_LASTCNG      DATE,
  PRIMARY KEY(N_CARTELLA, ID_DOMANDA, ID_RICHIESTA)
);
commit;

SET DEFINE OFF;
CREATE TABLE ZK_RSA_PREFERENZE(
  N_CARTELLA         NUMBER(13),
  ID_DOMANDA         NUMBER(13),
  ID_RICHIESTA       NUMBER(13),
  COD_ISTITUTO       CHAR(6 BYTE),
  COD_ORG            CHAR(3 BYTE),
  PRIORITA           CHAR(2 BYTE),
  JDBINTERF_VERSION  NUMBER(13),
  JDBINTERF_LASTCNG  DATE,
  PRIMARY KEY (N_CARTELLA, ID_DOMANDA, ID_RICHIESTA, COD_ISTITUTO, COD_ORG)
);
COMMIT;
/*  prenotazione per l'ingresso a una struttara */
set define off;

CREATE TABLE ZK_RSA_PRENOTAZIONE_INGRESSO(
  N_CARTELLA      NUMBER(13),
  ID_DOMANDA      NUMBER(13),
  ID_RICHIESTA    NUMBER(13),
  COD_ISTITUTO    CHAR(6 BYTE),
  TIPO_ISTITUTO   CHAR(1 BYTE),
  COD_ORG         CHAR(3 BYTE),
  ESITO_CONTATTO  CHAR(1 BYTE),		-- esito del contatto: A accettato, R rifiutato 
  DATA_ESITO      DATE,				-- data esito
  DATA_INGRESSO   DATE,
  NOTE            VARCHAR2(2000 BYTE),
  PRIMARY KEY(N_CARTELLA, ID_DOMANDA, ID_RICHIESTA, COD_ISTITUTO, COD_ORG)
);

commit;

ALTER TABLE ZK_RSA_RICHIESTA ADD FLAG_STATO VARCHAR2(8 BYTE);
ALTER TABLE zk_rsa_richiesta ADD motivo_richiesta VARCHAR2(2000 BYTE);
/* Data di inserimento richiesta */
ALTER TABLE ZK_RSA_RICHIESTA ADD DATA_RICHIESTA DATE;
alter table zk_rsa_preferenze add tipo_istituto CHAR(1 BYTE);
COMMIT;
/* SCRIPT DA ESEGUIRE DAL 10/11/2015 */


UPDATE CONF SET CONF_TXT = 'Vers. 15.05.1' WHERE CONF_KPROC = 'SINS' AND CONF_KEY = 'VERSIONE';
INSERT INTO CONF (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) VALUES ('SINS', 'GG_DA_PROROGA', '30', '0', TO_DATE('2016-02-12 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'giorni prima dei quali nn e possibile prorogare una scheda SO', '0', TO_DATE('2016-02-12 07:29:55', 'YYYY-MM-DD HH24:MI:SS'));



SET DEFINE OFF;
     Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'LA_NO_FONTE_SO', '#1=11,12,13,14#2=11,12,13,14#3=11,12,13,14#4=11,12,13,14#5=11,12,13,14', 0, sysdate, 
    'fonte da non mostrare per SO', 0, sysdate);
COMMIT;


update conf set conf_txt = 'Vers. 15.05.1a' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
/* si esclude la fonte scadenza del piano dalle lista dei vari operatori */
UPDATE conf
   SET conf_txt ='#1=1,4,5,7,8,15#2=1,4,5,7,8,15#3=1,4,5,7,8,15#4=1,4,5,7,8,15#5=1,4,5,7,8,15', 0, sysdate,
WHERE LOWER (conf_key) = 'la_no_fonte'



