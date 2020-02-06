SET DEFINE OFF;

SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'VERS_DATI', '16.03', 0, sysdate, 'agg db dati configurazione', 0, sysdate);
COMMIT;


SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'GG_SCAD_RICOVERI', '20', 0, sysdate, 
    'numero gg successivi alla scad dalla data attuale', 0, sysdate);
COMMIT;

 