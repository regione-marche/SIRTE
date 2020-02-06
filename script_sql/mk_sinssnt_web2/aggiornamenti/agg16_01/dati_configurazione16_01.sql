/*
 * URL DEL PDF SERVICE 
 */
SET DEFINE OFF;
Insert into CONF
   (CONF_KPROC, CONF_KEY, CONF_TXT, CONF_NUM, CONF_DATE, 
    CONF_REM, JDBINTERF_VERSION, JDBINTERF_LASTCNG)
 Values
   ('SINSSNT_WEB2', 'URL_PDFSERVICE', 'http://127.0.0.1:8086/PDFService/ws/example', NULL, sysdate, 
    'Url servizio produzione pdf', 1, sysdate);
COMMIT;
 