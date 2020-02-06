UPDATE CONF SET CONF_TXT = 'Vers. 17.03' WHERE CONF_KPROC = 'SINS' AND CONF_KEY = 'VERSIONE';


-- Inserimento delle colonne ESITO e NOTE nella tabella ZK_RSA_RICHIESTA 
ALTER TABLE ZK_RSA_RICHIESTA  ADD (ESITO CHAR(1 BYTE));
ALTER TABLE ZK_RSA_RICHIESTA ADD (NOTE VARCHAR2(2000 BYTE));

-- Inserimento in TAB_VOCI della voce NON IDONEO per lo stato richiesta
INSERT INTO tab_voci
            (tab_cod, tab_val,
             tab_descrizione, tab_fiss, tab_numero,
             tab_codreg, jdbinterf_lastcng, jdbinterf_version
            )
     VALUES ('RPRSASTA', 'NI',
             'NON IDONEO', '0', 0,
             '1', NULL, NULL
            );
COMMIT;


-- inserimento scale di valutazione in TAB_SCHEDE
UPDATE TAB_SCHEDE SET CLASSE = 'ScalaPfeifferFormCtrl' WHERE DESCRIZIONE='Test di Pfeiffer';

SET DEFINE ON;  
DEFINE n_max_schede = "(SELECT (MAX(id_scheda)+1) FROM tab_schede)";

Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'DOW',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'Assistenza persona','sc_valsoc_persona','null','assistenza_persona_data','assistenza_persona_nome','ScalaAssistenzaPersonaFormCtrl','N','S',null,null);

Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'DOW',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'Assistenza ambiente','sc_valsoc_ambiente','null','assistenza_ambiente_data','assistenza_ambiente_nome','ScalaAssistenzaAmbienteFormCtrl','N','S',null,null);

Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'DOW',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'Assistenza relazioni','sc_valsoc_relazioni','null','assistenza_relazioni_data','assistenza_relazioni_nome','ScalaAssistenzaRelazioniFormCtrl','N','S',null,null);

Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'DOW',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'CBI','sc_cbi','cbi_punt','cbi_data','cbi_nome','ScalaCBIFormCtrl','S','S',null,null);

Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'DOW',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'MDS Comportamento','sc_mds_comp','mdsc_punt','mdsc_data','mdsc_nome','ScalaMDSCompFormCtrl','S','S',null,null);

Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'DOW',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'MDS Umore','sc_mds_umore','mdsu_punt','mdsu_data','mdsu_nome','ScalaMDSUmoreFormCtrl','S','S',null,null);

Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'DOW',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'Scheda clinica','sc_clinica','clinica_punt','clinica_data','clinica_nome','ScalaSchedaClinicaFormCtrl','S','S',null,null);

Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'DOW',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'Scheda infermieristica','sc_inferm','inferm_punt','inferm_data','inferm_nome','ScalaSchedaInfermieristicaFormCtrl','S','S',null,null);

Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'DOW',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'Adeguatezza ambientale','sc_valsoc_adeamb','adeamb_punt','adeamb_data','adeamb_nome','ScalaAdeAmbFormCtrl','S','S',null,null);

Insert into TAB_SCHEDE_CONT (ID_SCHEDA,SC_TIPO_OP,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'DOW',null,null);
Insert into TAB_SCHEDE (ID_SCHEDA,DESCRIZIONE,TABELLA,CAMPO_PUNTEGGIO,CAMPO_DATA,CAMPO_NOME,CLASSE,FL_PUNTEGGIO,FL_ATTIVO,JDBINTERF_LASTCNG,JDBINTERF_VERSION) values (&n_max_schede,'Assistenza storia','sc_valsoc_storia','assstoria_punt','assstoria_data','assstoria_nome','ScalaAssistenzaStoriaFormCtrl','N','S',null,null);

COMMIT;