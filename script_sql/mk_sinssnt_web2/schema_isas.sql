set define on;
define user_schema_isas=&enter_user_schema_for_ISAS;

-- PID procedura SINSSNT
define var_isas_pid=&enter_ISAS_PID_per_SINSSNT;
-----------------------------

define var_isas_profid=&enter_ISAS_PROFID;
-------------------------------------------------------------------------------------

-- Scheda Infermieristica
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKINF', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKINF', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKINF', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKINF', 'CANC', 1, SYSDATE);

-- Scheda Infermieristica (Storico)
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'I_CONSTO', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'I_CONSTO', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'I_CONSTO', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'I_CONSTO', 'CANC', 1, SYSDATE);

-- Richiesta MMG
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RICH_MMG', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RICH_MMG', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RICH_MMG', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RICH_MMG', 'CANC', 1, SYSDATE);

-- Richiesta MMG funzionalità ARCHIVIA RICHIESTA
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ARCH_RIC', 'INSE', 1, SYSDATE);

-- Richiesta MMG funzionalità CONFERMA RICHIESTA
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONF_RIC', 'INSE', 1, SYSDATE);

-- Richiesta MMG funzionalità PRENDI IN CARICO RICHIESTA
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'PC_RIC', 'INSE', 1, SYSDATE);



/* INIZIO SCRIPT 28/01/2015 */


-- SET DEFINE OFF;
Insert into ISAS_CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION)
 Values
   ('SINS_AS', 'VERSIONE', 'VER. 15.01.a', NULL, NULL, 
    'versione isas per ISAS', 1);
COMMIT;




/* Scala ADL = BADL */
-- SET DEFINE OFF;
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'BADL', 'MODI',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'BADL', 'CANC',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'BADL', 'INSE',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'BADL', 'CONS',1, NULL);
COMMIT ;


/* Scala Barthel = PR_BARTH */
-- SET DEFINE OFF;
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'PR_BARTH', 'MODI',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'PR_BARTH', 'CANC',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'PR_BARTH', 'INSE',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'PR_BARTH', 'CONS',1, NULL);
COMMIT ;

/* Scala Braden = SKIBRADE */
-- SET DEFINE OFF;
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SKIBRADE', 'MODI',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SKIBRADE', 'CANC',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SKIBRADE', 'INSE',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SKIBRADE', 'CONS',1, NULL);
COMMIT ;

/*  per la scala Conley */

-- SET DEFINE OFF;
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'PR_CONLE', 'MODI',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'PR_CONLE', 'CANC',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'PR_CONLE', 'INSE',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'PR_CONLE', 'CONS',1, NULL);
COMMIT ;

/*  Scala IADL = IADL */
-- SET DEFINE OFF;
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'IADL', 'MODI',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'IADL', 'CANC',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'IADL', 'INSE',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'IADL', 'CONS',1, NULL);
COMMIT ;


/* Scala Karnofsky = SCP_KPS */
-- SET DEFINE OFF;
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SCP_KPS', 'MODI',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SCP_KPS', 'CANC',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SCP_KPS', 'INSE',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SCP_KPS', 'CONS',1, NULL);
COMMIT ;

/* Scala SPMSQ = SCPSPMSQ */
-- SET DEFINE OFF;
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SCPSPMSQ', 'MODI',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SCPSPMSQ', 'CANC',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SCPSPMSQ', 'INSE',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SCPSPMSQ', 'CONS',1, NULL);
COMMIT ;

/* Scala Wound Bed Score = SC_WOUND */
-- SET DEFINE OFF;
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SC_WOUND', 'MODI',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SC_WOUND', 'CANC',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SC_WOUND', 'INSE',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SC_WOUND', 'CONS',1, NULL);
COMMIT ;





/* FINE SCRIPT 28/01/2015 */
-- inserimento per abilitazione scala bisogni
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SC_BISOG', 'MODI',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SC_BISOG', 'CANC',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SC_BISOG', 'INSE',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SC_BISOG', 'CONS',1, NULL);
COMMIT ;

--inserimento permessi per estrazione flussi SIAD
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'EXFLUSSI', 'MODI',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'EXFLUSSI', 'CANC',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'EXFLUSSI', 'INSE',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'EXFLUSSI', 'CONS',1, NULL);
COMMIT ;

--inserimento permessi per modifica situazione sanitaria all'interno della scala bisogni (pannello info generali)

INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SCBISSAN', 'MODI',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SCBISSAN', 'CANC',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SCBISSAN', 'INSE',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SCBISSAN', 'CONS',1, NULL);
COMMIT ;

--inserimento permessi per convalida uvi all'interno della scala bisogni
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'CONV_UVI', 'MODI',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'CONV_UVI', 'CANC',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'CONV_UVI', 'INSE',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'CONV_UVI', 'CONS',1, NULL);
COMMIT ;

--inserimento permessi per convalida uvi all'interno della scala bisogni
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'FLS21', 'MODI',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'FLS21', 'CANC',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'FLS21', 'INSE',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'FLS21', 'CONS',1, NULL);
COMMIT ;

/*Inizio Script 02/02/2015 */
/* Rilevazione del dolore nel bambino = DOL_BAM */

INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'DOL_BAM', 'MODI',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'DOL_BAM', 'CANC',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'DOL_BAM', 'INSE',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'DOL_BAM', 'CONS',1, NULL);
COMMIT ;

/* Rilevazione del dolore nell'adulto = DOL_AD */

INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'DOL_AD', 'MODI',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'DOL_AD', 'CANC',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv, jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'DOL_AD', 'INSE',1, NULL);
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'DOL_AD', 'CONS',1, NULL);
COMMIT ;
/*Fine Script 02/02/2015 */

/*Script 12/02/2015 */
/* MARCHIAMO LA VERSIONE DEL DB */ 
update conf set conf_txt = 'Vers. 15.01.b' where conf_kproc = 'SINS_AS' AND CONF_KEY = 'VERSIONE';
/* viene marcato quando la scheda viene aperta dalla so nel caso di prima visita */

/* Stampa riepilogo assistiti */

/* NON è ANCORA FINITA LA STAMPA: SOLO PER LA VERSIONE DI SVILUPPO */ 
  INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'RIEPASSI', 'CONS',1, NULL);
COMMIT;

/* inserire le segnalazioni */
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SEGNALAZ', 'CANC', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SEGNALAZ', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SEGNALAZ', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SEGNALAZ', 'INSE', 1, SYSDATE);
/* Fine Script 12/02/2015 */

/* Inizio Script 11/03/2015 */
INSERT INTO isas_procprof
            (isas_pid, isas_profid, isas_kfunc, isas_kserv,jdbinterf_version, jdbinterf_lastcng)
     VALUES (&var_isas_pid., &var_isas_profid., 'SC_RUG', 'CONS',1, NULL);
COMMIT ;

/* Script 11/03/2015 */

/* Inizio Script 25/03/2015 */
-- Tabella prestaz_bisogni
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'PRE_BI', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'PRE_BI', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'PRE_BI', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'PRE_BI', 'CANC', 1, SYSDATE);
/* Fine Script 25/03/2015 */


/* Inizio Script 06/05/2015 */
-- Convalida delle prestazioni MMG - ex Millenium (Form Consultazione)
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONS_MIL', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONS_MIL', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONS_MIL', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONS_MIL', 'CANC', 1, SYSDATE);
/* Fine Script 06/05/2015 */

/* Inizio Script 14/07/2015 */
-- chiavi di permesso per scheda SOCIALE
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG01', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG01', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG01', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG01', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda PSICOLOGO
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG09', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG09', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG09', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG09', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda DIETISTA
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG11', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG11', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG11', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG11', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda LOGOPEDISTA
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG14', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG14', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG14', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG14', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda OSS
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG16', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG16', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG16', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG16', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda CHIRURGO
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG20', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG20', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG20', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG20', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda ANESTETISTA
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG25', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG25', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG25', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG25', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda NEUROLOG
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG30', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG30', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG30', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG30', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda ONCOLOGO
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG35', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG35', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG35', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG35', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda DERMATOLOGO
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG40', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG40', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG40', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG40', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda INTERNISTA
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG45', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG45', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG45', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG45', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda FISIATRA
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG50', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG50', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG50', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG50', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda PNEUMOLOGO
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG51', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG51', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG51', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG51', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda PALLIATIVISTA
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG52', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG52', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG52', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG52', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda CARDIOLOGO 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG53', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG53', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG53', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG53', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda GERIATRA 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG54', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG54', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG54', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG54', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda ALTRA FIGURA PROFESSIONALE 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG60', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG60', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG60', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG60', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda GUARDIA_MEDICA 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG65', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG65', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG65', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG65', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda TERAPISTA_OCCUPAZIONALE 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG70', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG70', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG70', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG70', 'CANC', 1, SYSDATE);
-- chiavi di permesso per scheda MMG/PLS 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG99', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG99', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG99', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG99', 'CANC', 1, SYSDATE);
/* Fine Script 14/07/2015 */


SET DEFINE OFF;
Insert into ISAS_PROCPROF (ISAS_PID, ISAS_PROFID, ISAS_KFUNC, ISAS_KSERV, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values (&var_isas_pid., &var_isas_profid., 'LISTARSA', 'INSE', 1, sysdate);
Insert into ISAS_PROCPROF (ISAS_PID, ISAS_PROFID, ISAS_KFUNC, ISAS_KSERV, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values (&var_isas_pid., &var_isas_profid., 'LISTARSA', 'CANC', 1, sysdate);
Insert into ISAS_PROCPROF(ISAS_PID, ISAS_PROFID, ISAS_KFUNC, ISAS_KSERV, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values (&var_isas_pid., &var_isas_profid., 'LISTARSA', 'MODI', 1, sysdate);
Insert into ISAS_PROCPROF (ISAS_PID, ISAS_PROFID, ISAS_KFUNC, ISAS_KSERV, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values (&var_isas_pid., &var_isas_profid., 'LISTARSA', 'CONS', 1, sysdate);
COMMIT;


INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKINF', 'INSE', 1, SYSDATE);

-- Lista preferenze rsa
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'LSTAPREF', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'LSTAPREF', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'LSTAPREF', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'LSTAPREF', 'CANC', 1, SYSDATE);

COMMIT;



