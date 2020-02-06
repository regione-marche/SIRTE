UPDATE CONF SET CONF_TXT = 'Vers. 16.01' WHERE CONF_KPROC = 'SINS' AND CONF_KEY = 'VERSIONE';

INSERT INTO CONF (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG) 
VALUES ('SINS', 'GG_DA_PROROGA', '30', '0', TO_DATE('2016-02-12 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'giorni prima dei quali nn e possibile prorogare una scheda SO', '0', TO_DATE('2016-02-12 07:29:55', 'YYYY-MM-DD HH24:MI:SS'));

