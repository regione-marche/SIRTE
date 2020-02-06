SET DEFINE OFF;

Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINS', 'ABIL_CHECKB_PORT', 'NO', 0, sysdate, 'Se SI, abilita la visualiz. delle checkbox portatore nella maschera dei presidi', 0, sysdate);
   
COMMIT;

 