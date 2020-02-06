--- ***********************************************************************
--- SINSSNT_WEB2 integrato con Fascicolo Sanitario Elettronico (Marche)
--- ***********************************************************************


SET DEFINE OFF;
--REGIONE MARCHE

/* Abilitazione inoltro di documenti al fascicolo sanitario elettronico */ 
Insert into CONF (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
values ('SINS', 'ABILIT_INVIO_FSE','NO',0,sysdate,'Abilita invio documenti al fascicolo sanitario elettronico',0,sysdate);
    

COMMIT;

