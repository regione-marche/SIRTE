/* tabelle a supporto per la lista attivita: 
 *  per la gestione della residenzialita */
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
  tipo_ricovero_dal 	 DATE,
  tipo_ricovero_al  	 DATE,
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
  tipo_ricovero_dal  DATE,
  tipo_ricovero_al   DATE,
  JDBINTERF_VERSION  NUMBER(13),
  JDBINTERF_LASTCNG  DATE,
  PRIMARY KEY (N_CARTELLA, ID_DOMANDA, ID_RICHIESTA, COD_ISTITUTO, COD_ORG)
);
COMMIT;

/*  prenotazione per l'ingresso a una struttara */
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
  ESITO_AMMISSIONE CHAR(1 BYTE),
  DATA_AMMISSIONE DATE,
  NOTE            VARCHAR2(2000 BYTE),
  PRIMARY KEY(N_CARTELLA, ID_DOMANDA, ID_RICHIESTA, COD_ISTITUTO, COD_ORG)
);

/* tabelle a supporto per la lista attivita: 
 *  per la gestione delle dimissioni ospedaliere */
CREATE TABLE PHT2_GENERALE(
  ID_SCHEDA             NUMBER(13) NOT NULL,
  N_CARTELLA            NUMBER(13),
  STATO_SCHEDA          VARCHAR2(20 BYTE),
  DATA_ATTIVAZIONE      DATE,
  DATA_UPDATE           DATE,
  OPERATORE             CHAR(10 BYTE),
  OSPEDALE              CHAR(8 BYTE),
  REPARTO               CHAR(6 BYTE),
  UNITA_OPERATIVA       CHAR(6 BYTE),
  DATA_AMM              DATE,
  DATA_DIM_PREVISTA     DATE,
  DATA_ULTIMA_SEGNA     DATE,
  TIPO_URGENTE          CHAR(1 BYTE),
  TIPO_CASO             CHAR(6 BYTE),
  NOTE                  VARCHAR2(1000 BYTE),
  NUM_SEGNA             NUMBER(13),
  MOTIVO_DIM            CHAR(32 BYTE),
  ORA_ULTIMA_SEGNA      CHAR(5 BYTE),
  DATA_ARCHIVIO         DATE,
  ORA_ARCHIVIO          CHAR(5 BYTE),
  DATA_DIM              DATE,
  ORA_DIM               CHAR(5 BYTE),
  DISTRETTO             CHAR(6 BYTE),
  PERCORSO_RICHIESTO    CHAR(8 BYTE),
  PRESA_CARICO_SINSSNT  CHAR(1 BYTE),
  TEMPO_T               NUMBER(13),
  DT_ACQUISIZIONE       DATE,
  SERVIZIO_RICHIESTO    CHAR(8 BYTE),
  JDBINTERF_VERSION     NUMBER(13),
  JDBINTERF_LASTCNG     DATE,
  PRIMARY KEY (ID_SCHEDA)
);

COMMIT;

SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'LA_NO_FONTE_SO', '#1=11,12,13,14#2=11,12,13,14#3=11,12,13,14#4=11,12,13,14#5=11,12,13,14', 0, sysdate, 
    'fonte da non mostrare per SO', 0, sysdate);
COMMIT;

ALTER TABLE ZK_RSA_RICHIESTA ADD FLAG_STATO VARCHAR2(8 BYTE);
ALTER TABLE zk_rsa_richiesta ADD motivo_richiesta VARCHAR2(2000 BYTE);
/* Data di inserimento richiesta */
ALTER TABLE ZK_RSA_RICHIESTA ADD DATA_RICHIESTA DATE;
alter table zk_rsa_preferenze add tipo_istituto CHAR(1 BYTE);
COMMIT;
/* SCRIPT DA ESEGUIRE DAL 10/11/2015 */

SET DEFINE OFF;
     Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'LA_NO_FONTE_SO', '#1=11,12,13,14#2=11,12,13,14#3=11,12,13,14#4=11,12,13,14#5=11,12,13,14', 0, sysdate, 
    'fonte da non mostrare per SO', 0, sysdate);
COMMIT;

update conf set conf_txt = 'Vers. 16.01a' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
/* si esclude la fonte scadenza del piano dalle lista dei vari operatori */
UPDATE conf
   SET conf_txt ='#1=1,4,5,7,8,15,18', 0, sysdate,
WHERE LOWER (conf_key) = 'la_no_fonte';


update conf set conf_txt = 'Vers. 16.01b' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
/* aggiunta l'informazione sulla scheda so dell'eventuale collegamento con la scheda del pht2 */
ALTER TABLE RM_SKSO ADD ID_SCHEDA_PHT2 NUMBER(13);



/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 16.01c' where conf_kproc = 'SINS' AND CONF_KEY = 'VERSIONE';
/* si esclude la fonte scadenza del piano dalle lista dei vari operatori */

/* RIMUOVERE LA FONTE 19 DAI VARI CONTATTI */
update conf 
set conf_txt = '#1=1,4,5,7,8,19#2=1,4,5,7,8,19#3=1,4,5,7,8,19#4=1,4,5,7,8,19#5=1,4,5,7,8,19'
where conf_key = 'LA_NO_FONTE';



SET DEFINE OFF;
UPDATE CONF SET CONF_TXT = 'Vers. 16.01d' WHERE CONF_KPROC = 'SINS' AND CONF_KEY = 'VERSIONE';

SET DEFINE OFF;
ALTER TABLE RM_SKSO_MMG ADD TIT_STUDIO CHAR(8 BYTE);
ALTER TABLE RM_SKSO_MMG ADD RIC_RICHIEDENTE CHAR(8 BYTE);
ALTER TABLE RM_SKSO_MMG ADD STRUT_PROVENIENZA CHAR(8 BYTE);
ALTER TABLE RM_SKSO_MMG ADD MOT_RICHIESTA CHAR(8 BYTE);
COMMIT;


CREATE TABLE FLUSSI_SIAD_ELAB
   (	TICKET VARCHAR2(20 CHAR), 
	FILENAME VARCHAR2(500 CHAR), 
	ESITO NUMBER(1,0), 
	 PRIMARY KEY ("TICKET"));
   COMMENT ON COLUMN FLUSSI_SIAD_ELAB.ESITO IS '0 = elaborazione completata con successo, 1 = errore durante l''elaborazione';

   
ALTER TABLE piano_assist ADD FLAG_STATO  CHAR(1 BYTE);
ALTER TABLE piano_accessi ADD FLAG_STATO  CHAR(1 BYTE);
UPDATE piano_assist SET FLAG_STATO = '1';
UPDATE piano_accessi SET FLAG_STATO = '1';
COMMIT;
   

