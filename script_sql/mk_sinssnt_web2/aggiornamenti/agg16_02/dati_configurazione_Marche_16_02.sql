SET DEFINE OFF;

--DEFINIZIONE PUNTEGGIO PER LA SCALA DI VALUTAZIONE DEI BISOGNI
DELETE FROM SC_BISOGNI_PUNT;
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('BIS_AUTONOMIA'			,'AUTONOMIA'		,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('BIS_AUTONOMIA1'			,'AUTONOMIA'		,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('BIS_AUTONOMIA2'			,'AUTONOMIA'		,20 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('BIS_AUTONOMIA3'			,'AUTONOMIA'		,40 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('STATO_VEGETATIVO'		,'AUTONOMIA'		,0  );

Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('RIAB_NESSUNA'			,'RIABILITAZIONE'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('RIAB_AFASIA'			,'RIABILITAZIONE'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('RIAB_MANTENIMENTO'		,'RIABILITAZIONE'	,20 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('RIAB_NEUROLOGICA'		,'RIABILITAZIONE'	,60 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('RIAB_ORTOPEDICA'		,'RIABILITAZIONE'	,40 );

Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('RESP_NORMALE'			,'APP_RESPIRATORIO'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('RESP_OSSIGENOTERAPIA'	,'APP_RESPIRATORIO'	,20 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('RESP_TOSSE_SECR'		,'APP_RESPIRATORIO'	,40 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('RESP_TRACHEOSTOMIA'		,'APP_RESPIRATORIO'	,60 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('RESP_VENTILOTERAPIA'	,'APP_RESPIRATORIO'	,100);

Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('NUTR_NORMALE'			,'STATO_NUTRIZION'	,0);
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('NUTR_DIMAGRIMENTO'		,'STATO_NUTRIZION'	,0);
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('NUTR_DISIDRATAZIONE'	,'STATO_NUTRIZION'	,0);
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('NUTR_DISFAGIA_DETT1'	,'STATO_NUTRIZION'	,40);
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('NUTR_DISFAGIA_DETT2'	,'STATO_NUTRIZION'	,40);
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('NUTR_DISFAGIA_DETT3'	,'STATO_NUTRIZION'	,60);
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('NUTR_DISFAGIA_DETT4'	,'STATO_NUTRIZION'	,20);

Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('GASTR_NORMALE'			,'APP_GASTROINTEST'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('GASTR_INCONT_DETT1'		,'APP_GASTROINTEST'	,40 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('GASTR_INCONT_DETT2'		,'APP_GASTROINTEST'	,20 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('GASTR_SANG'				,'APP_GASTROINTEST'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('GASTR_STIPSI'			,'APP_GASTROINTEST'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('GASTR_STOMIA'			,'APP_GASTROINTEST'	,30 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('GASTR_VOMITO'			,'APP_GASTROINTEST'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('GASTR_DIARREA'			,'APP_GASTROINTEST'	,0  );

Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('GENURI_NORMALE'			,'APP_GENITOURINAR'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('GENURI_CATETERISMO'		,'APP_GENITOURINAR'	,20 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('GENURI_DIALISI'			,'APP_GENITOURINAR'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('GENURI_DIALISI_PERI'	,'APP_GENITOURINAR'	,40 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('GENURI_EMATURIA'		,'APP_GENITOURINAR'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('GENURI_INCONT'			,'APP_GENITOURINAR'	,40 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('GENURI_UROSTOMIA'		,'APP_GENITOURINAR'	,30 );
              
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('CUTE_NORMALE'			,'APP_TEGUMENTARIO'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('CUTE_ALTRO'				,'APP_TEGUMENTARIO'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('CUTE_CURA'				,'APP_TEGUMENTARIO'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('CUTE_LACERAZIONI'		,'APP_TEGUMENTARIO'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('CUTE_ULCERE12'			,'APP_TEGUMENTARIO'	,20 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('CUTE_PRESSIONE'			,'APP_TEGUMENTARIO'	,40 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('CUTE_ULCERE34'			,'APP_TEGUMENTARIO'	,40 );

Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('COMP_NORMALE'			,'COMPORTAMENTO'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('COMP_DIST_COGN_MODERATO','COMPORTAMENTO'	,20 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('COMP_DIST_COGN_GRAVE'	,'COMPORTAMENTO'	,40 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('COMP_DIST_COMP'			,'COMPORTAMENTO'	,60 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('COMP_PSICO_SALUTE'		,'COMPORTAMENTO'	,0  );

Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('RITMO_ALTERATO_DETT1'	,'SONNO_VEGLLIA_INF',0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('RITMO_ALTERATO_DETT2'	,'SONNO_VEGLLIA_INF',20 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('RITMO_ALTERATO_DETT3'	,'SONNO_VEGLLIA_INF',40 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('RISCHIO_PRESENTE'		,'SONNO_VEGLLIA_INF',0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('FEBBRE'					,'SONNO_VEGLLIA_INF',0  );

Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('ONCO_NORMALE'			,'ONCOLOGICO_TERM'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('ONCO_CHEMIOTERAPIA'		,'ONCOLOGICO_TERM'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('ONCO_DOLORE_DETT1'		,'ONCOLOGICO_TERM'	,60 );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('ONCO_DOLORE_DETT2'		,'ONCOLOGICO_TERM'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('ONCO_ONCOLOGICO'		,'ONCOLOGICO_TERM'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('ONCO_RADIOTERAPIA'		,'ONCOLOGICO_TERM'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('ONCO_TERM_NON_ONCO'		,'ONCOLOGICO_TERM'	,0  );
Insert into SC_BISOGNI_PUNT (ID_BISOGNO, DESCR_BISOGNO, PUNTEGGIO) Values ('ONCO_TERM_ONCO'			,'ONCOLOGICO_TERM'	,0  );


--SCALE DI VALUTAZIONE
--aggiungo la scala dei presidi sanitari fra le valutazioni
Insert into TAB_SCHEDE
   (ID_SCHEDA, DESCRIZIONE, TABELLA, CAMPO_PUNTEGGIO, CAMPO_DATA, CAMPO_NOME, CLASSE, FL_PUNTEGGIO, FL_ATTIVO)
 Values
   (72, 'PRESIDI SANITARI', 'SC_PRESIDI_SAN', '', 'data', 'nome', 'ScalaPresidiSanFormCtrl', 'N', 'S');
--assegno la scala dei PRESIDI SANITARI agli infermieri
INSERT INTO TAB_SCHEDE_CONT (ID_SCHEDA, SC_TIPO_OP, JDBINTERF_LASTCNG, JDBINTERF_VERSION)
VALUES (72, '02', NULL, NULL);
--assegno la scala CIRS agli infermieri
INSERT INTO TAB_SCHEDE_CONT (ID_SCHEDA, SC_TIPO_OP, JDBINTERF_LASTCNG, JDBINTERF_VERSION)
VALUES (71, '02', NULL, NULL);
--assegno la scala BRASS agli infermieri
INSERT INTO TAB_SCHEDE_CONT (ID_SCHEDA, SC_TIPO_OP, JDBINTERF_LASTCNG, JDBINTERF_VERSION)
VALUES (70, '02', NULL, NULL);
--assegno la scala dei PRESIDI SANITARI alla segreteria organizzativa
INSERT INTO TAB_SCHEDE_CONT (ID_SCHEDA, SC_TIPO_OP, JDBINTERF_LASTCNG, JDBINTERF_VERSION)
VALUES (72, '00', NULL, NULL);
--assegno la scala CIRS alla segreteria organizzativa
INSERT INTO TAB_SCHEDE_CONT (ID_SCHEDA, SC_TIPO_OP, JDBINTERF_LASTCNG, JDBINTERF_VERSION)
VALUES (71, '00', NULL, NULL);
--assegno la scala BRASS alla segreteria organizzativa
INSERT INTO TAB_SCHEDE_CONT (ID_SCHEDA, SC_TIPO_OP, JDBINTERF_LASTCNG, JDBINTERF_VERSION)
VALUES (70, '00', NULL, NULL);


/* CONFIGURO I CONTROLLI DA PASSARE ALLA LIBRERIA MILLEWIN */
--c'è già Insert into CONF (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values ('SINS', 'CTR_AUTO_WEB', 'SI', 0, sysdate,'controllo sull''autorizzato delle prestazioni MMG da webapp', 0, sysdate);
--c'è già Insert into CONF (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values ('SINS', 'CTR_SOSP_WEB', 'SI', 0, sysdate,'controllo sulla sospensione delle prestazioni MMG da webapp', 0, sysdate);
Insert into CONF (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values ('SINS', 'FORZA_INTMMG_WEB', 'SI', 0, sysdate,'forzo anche su INTMMG (se passano i vari controlli) le prestazioni mmg  da webapp', 0, sysdate);
Insert into CONF (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values ('SINS', 'CTR_AUTO_POR', 'SI', 0, sysdate,'controllo sull''autorizzato delle prestazioni MMG da portale', 0, sysdate);
Insert into CONF (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values ('SINS', 'CTR_SOSP_POR', 'SI', 0, sysdate,'controllo sulla sospensione delle prestazioni MMG da portale', 0, sysdate);
Insert into CONF (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values ('SINS', 'FORZA_INTMMG_POR', 'NO', 0, sysdate,'forzo anche su INTMMG (se passano i vari controlli) le prestazioni mmg  da portale', 0, sysdate);


SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'VERS_DATI', '16.02.A', 0, sysdate, 'agg db dati configurazione', 0, sysdate);
COMMIT;


SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'PHT2_NOME_APP', 'sins_pht_web2', 0, sysdate, 
    'nome pht2 deployata sullo stesso tomcat', 0, sysdate);
COMMIT;

SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'ABILITAZ_RSA', 'SI', 0, sysdate, 
    'Abilita la gestione in rsa', 0, sysdate);
COMMIT;



SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'PHT2_NOME_APP', 'sins_web_pht2', 0, sysdate ,'nome pht2 deployata sullo stesso tomcat', 0, sysdate);
COMMIT;


SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'VERSIONE_DATI', '16.02.A', 0, sysdate, 'agg db dati configurazione sinssnt WEB ', 0, sysdate);
COMMIT;


SET DEFINE OFF;
INSERT INTO conf (conf_kproc, conf_key, conf_txt, conf_num, conf_date,
             conf_rem, jdbinterf_version, jdbinterf_lastcng)
     VALUES ('SINS', 'GRA_OBBL_DST_SNS', 'NO', 0, SYSDATE, 'Distretto obbligatorio per rsa ', 0, SYSDATE);
COMMIT;

SET DEFINE OFF;

delete FROM TAB_VOCI WHERE TAB_COD = 'FTTIPDIM';

INSERT INTO tab_voci
            (tab_cod, tab_val,
             tab_descrizione, tab_fiss, tab_numero,
             tab_codreg, jdbinterf_lastcng, jdbinterf_version
            )
     VALUES ('FTTIPDIM', '#',
             'Tipologia dimissione', '0', 0,NULL, NULL, NULL);

INSERT INTO tab_voci
            (tab_cod, tab_val,
             tab_descrizione, tab_fiss, tab_numero,
             tab_codreg, jdbinterf_lastcng, jdbinterf_version
            )
     VALUES ('FTTIPDIM', '0','.', '0', 0,
             '1', NULL, NULL
            );

INSERT INTO tab_voci
            (tab_cod, tab_val,
             tab_descrizione, tab_fiss, tab_numero,
             tab_codreg, jdbinterf_lastcng, jdbinterf_version
            )
     VALUES ('FTTIPDIM', '1',
             'Dimissione a domicilio senza assistenza', '0', 0,
             '1', NULL, NULL
            );

INSERT INTO tab_voci
            (tab_cod, tab_val,
             tab_descrizione, tab_fiss, tab_numero,
             tab_codreg, jdbinterf_lastcng, jdbinterf_version
            )
     VALUES ('FTTIPDIM', '2',
             'Dimissione a domicilio con assistenza', '0', 0,
             '2', NULL, NULL
            );

INSERT INTO tab_voci
            (tab_cod, tab_val,
             tab_descrizione, tab_fiss, tab_numero,
             tab_codreg, jdbinterf_lastcng, jdbinterf_version
            )
     VALUES ('FTTIPDIM', '3',
             'Trasferimento a struttura ospedaliera per acuti', '0', 0,
             '3', NULL, NULL
            );

INSERT INTO tab_voci
            (tab_cod, tab_val,
             tab_descrizione, tab_fiss, tab_numero,
             tab_codreg, jdbinterf_lastcng, jdbinterf_version
            )
     VALUES ('FTTIPDIM', '4',
             'Dimissione ad altra tipologia di residenza', '0', 0,
             '4', NULL, NULL
            );

INSERT INTO tab_voci
            (tab_cod, tab_val,
             tab_descrizione, tab_fiss, tab_numero,
             tab_codreg, jdbinterf_lastcng, jdbinterf_version
            )
     VALUES ('FTTIPDIM', '5',
             'Decesso', '0', 0,
             '5', NULL, NULL
            );

INSERT INTO tab_voci
            (tab_cod, tab_val,
             tab_descrizione,
             tab_fiss, tab_numero, tab_codreg, jdbinterf_lastcng,
             jdbinterf_version
            )
     VALUES ('FTTIPDIM', '6',
             'Chiusura a seguito di trasferimento amm. altra struttura ASL',
             '0', 0, '6', NULL,
             NULL
            );

INSERT INTO tab_voci
            (tab_cod, tab_val,
             tab_descrizione, tab_fiss,
             tab_numero, tab_codreg, jdbinterf_lastcng, jdbinterf_version
            )
     VALUES ('FTTIPDIM', '7',
             'Dimissione ad altra struttura residenziale/semiresid.', '0',
             0, '7', NULL, NULL
            );

INSERT INTO tab_voci
            (tab_cod, tab_val, tab_descrizione, tab_fiss, tab_numero,
             tab_codreg, jdbinterf_lastcng, jdbinterf_version
            )
     VALUES ('FTTIPDIM', '8', 'Chiusura amministrativa', '0', 0,
             '8', NULL, NULL
            );
COMMIT ;

/* Obbligatorio i dati del pannello sociale per la scheda so */
SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values
   ('SINS', 'OBB_SO_STCIV', '1', 0, sysdate, 'Obbligatorieta'' nella SO Stato Civile', 0, sysdate);
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE,     CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values
   ('SINS', 'OBB_SO_NUMFAM', '1', 0, sysdate,  'Obbligatorieta'' nella SO Numero familiari ', 0, sysdate);
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values
   ('SINS', 'OBB_SO_ASSNFAM', '1', 0, sysdate,  'Obbligatorieta'' nella SO Assistente non familiare', 0, sysdate);
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values
   ('SINS', 'OBB_SO_CONVIV', '', 0, sysdate, 'Obbligatorieta'' nella SO: Con chi vive ', 0, sysdate);
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values
   ('SINS', 'OBB_SO_RAZETN', '', 0, sysdate, 'Obbligatorieta'' nella SO: Razza/Etnia ', 0, sysdate);
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values
   ('SINS', 'OBB_SO_LINGUA', '', 0, sysdate, 'Obbligatorieta'' nella SO: Lingua ', 0, sysdate);
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE,  CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values
   ('SINS', 'OBB_SO_TITSTU', '', 0, sysdate, 'Obbligatorieta'' nella SO: Titolo studio ', 0, sysdate);
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values
   ('SINS', 'OBB_SO_RICDEN', '', 0, sysdate, 'Obbligatorieta'' nella SO: Richiedente ', 0, sysdate);    
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE,  CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values
   ('SINS', 'OBB_SO_STRPROV', '', 0, sysdate, 'Obbligatorieta'' nella SO: Struttura provenienza ', 0, sysdate);    
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) Values
 ('SINS', 'OBB_SO_MOTRIC', '', 0, sysdate,  'Obbligatorieta'' nella SO: Motivo di richiesta ', 0, sysdate);
 
COMMIT;
-- 21/11/2016 ESCLUSIONE DELLA FONTE DEL PUA  
/* RIMOZIONI DELLE FONTE DA RIMUOVERE PER GLI ALTRI OPERATORI */
update conf 
set conf_txt = '#1=1,4,5,7,8,18,19,20#2=1,4,5,7,8,18,19,20,21#3=1,4,5,7,8,18,19,20,21#4=1,4,5,7,8,18,19,20,21#5=1,4,5,7,8,18,19,20,21'
where conf_key = 'LA_NO_FONTE'

INSERT INTO conf
            (conf_kproc, conf_key, conf_txt, conf_num, conf_date,
             conf_rem, jdbinterf_version, jdbinterf_lastcng
            )
     VALUES ('SINS', 'OBL_RIC_PST_LET', 'SI', 0, SYSDATE,
             'rende obbligatorio la selezione del posto letto', 0, SYSDATE
            );

INSERT INTO conf
            (conf_kproc, conf_key, conf_txt, conf_num, conf_date,
             conf_rem, jdbinterf_version, jdbinterf_lastcng
            )
     VALUES ('SINS', 'OBL_RIC_STANZA', 'SI', 0, SYSDATE,
             'rende obbligatorio la selezione della stanza', 0, SYSDATE
            );
COMMIT;


 