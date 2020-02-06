
SET DEFINE ON;
-- PID PROCEDURA  SINSSNT WEB2 
DEFINE VAR_ISAS_PID=&ENTER_ISAS_PID_PER_SINSSNT; 
-- PROFILO
DEFINE VAR_ISAS_PROFID=&ENTER_ISAS_PROFID;
--  CHIAVI ISAS PER CONTATTO GENERICO -XX- VA SOSTITUITO OPPORTUNAMENTEO IL CODICE DELLA FIGURA PROFESSIONALE 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG-XX-', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG-XX-', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG-XX-', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPG-XX-', 'CANC', 1, SYSDATE);
--  contatto generico storico 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPGSTO', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPGSTO', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPGSTO', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPGSTO', 'CANC', 1, SYSDATE);
-- contatto generico referente 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPGREF', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPGREF', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPGREF', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFPGREF', 'CANC', 1, SYSDATE);
-- Cartella
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CARTELLA', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CARTELLA', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CARTELLA', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CARTELLA', 'CANC', 1, SYSDATE);
-- Per disabilitare parte dei campi
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ANAGMODI', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ANAGMODI', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ANAGMODI', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ANAGMODI', 'CANC', 1, SYSDATE);
-- Funzione ISAS x chiusura anagrafica
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CLS_ANAG', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CLS_ANAG', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CLS_ANAG', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CLS_ANAG', 'CANC', 1, SYSDATE);
-- chiave per accessi
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'INTERV', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'INTERV', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'INTERV', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'INTERV', 'CANC', 1, SYSDATE);
-- chiave per accessi
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'TABPIPP', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'TABPIPP', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'TABPIPP', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'TABPIPP', 'CANC', 1, SYSDATE);
-- chiave per accessi
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ACCSPE', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ACCSPE', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ACCSPE', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ACCSPE', 'CANC', 1, SYSDATE);
-- Convalida prestazioni MMG ex Millenium (Form Consultazione)
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONS_MIL', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONS_MIL', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONS_MIL', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONS_MIL', 'CANC', 1, SYSDATE);
-- CHIAVI ISAS PER LISTA UTENTI (CONNESSIONI) ATTIVE
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONN_ATT', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONN_ATT', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONN_ATT', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONN_ATT', 'CANC', 1, SYSDATE);
-- CHIAVI ISAS PER LISTA ATTIVITA
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'LSTATTIV', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'LSTATTIV', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'LSTATTIV', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'LSTATTIV', 'CANC', 1, SYSDATE);
-- finestra principale
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RICH_MMG', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RICH_MMG', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RICH_MMG', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RICH_MMG', 'CANC', 1, SYSDATE);
--  pulsante archivia 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ARCH_RIC', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ARCH_RIC', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ARCH_RIC', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ARCH_RIC', 'CANC', 1, SYSDATE);
--  pulsante presa in carico 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'PC_RIC', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'PC_RIC', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'PC_RIC', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'PC_RIC', 'CANC', 1, SYSDATE);
--  pulsante conferma  
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONF_RIC', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONF_RIC', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONF_RIC', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONF_RIC', 'CANC', 1, SYSDATE);
-- CHIAVI ISAS PER CONTATTO INFERMIERISTICO
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKINF', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKINF', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKINF', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKINF', 'CANC', 1, SYSDATE);
--  pulsante storico contatti 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'I_CONSTO', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'I_CONSTO', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'I_CONSTO', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'I_CONSTO', 'CANC', 1, SYSDATE);
-- CHIAVI ISAS PER CONTATTO MEDICO 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKMED', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKMED', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKMED', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKMED', 'CANC', 1, SYSDATE);
--  contatto medico storico 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKMEDSTO', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKMEDSTO', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKMEDSTO', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKMEDSTO', 'CANC', 1, SYSDATE);
--  CHIAVI ISAS PER SEGRETERIA ORGANIZZATIVA 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RASKPUAC', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RASKPUAC', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RASKPUAC', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RASKPUAC', 'CANC', 1, SYSDATE);
--  SO CONTATTO STORICO 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SCHESTOR', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SCHESTOR', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SCHESTOR', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SCHESTOR', 'CANC', 1, SYSDATE);
--  FLUSSI SIAD 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'FLUX_MIN', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'FLUX_MIN', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'FLUX_MIN', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'FLUX_MIN', 'CANC', 1, SYSDATE);
-- CHIAVI ISAS PER CONTATTO FISIOTERAPICO 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFISIO', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFISIO', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFISIO', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKFISIO', 'CANC', 1, SYSDATE);
-- CHIAVI ISAS PER CONTATTO MEDICO PALLIATIVISTA 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKMEDPAL', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKMEDPAL', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKMEDPAL', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SKMEDPAL', 'CANC', 1, SYSDATE);
--  CHIAVI ISAS PER CONTATTO PUAC 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'A_OPPUAC', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'A_OPPUAC', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'A_OPPUAC', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'A_OPPUAC', 'CANC', 1, SYSDATE);
-- CHIAVI ISAS PER L'AGENDA 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ST_AGVIS', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ST_AGVIS', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ST_AGVIS', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ST_AGVIS', 'CANC', 1, SYSDATE);
--  modifica dell'operatore di cui stampare agenda 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AGEMODOP', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AGEMODOP', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AGEMODOP', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AGEMODOP', 'CANC', 1, SYSDATE);
--  registrazioni  prestazioni 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_REG', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_REG', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_REG', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_REG', 'CANC', 1, SYSDATE);
--  carica settimane 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_CAR', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_CAR', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_CAR', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_CAR', 'CANC', 1, SYSDATE);
--  spostamento della pianificazione dell''agenda 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_SPO', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_SPO', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_SPO', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_SPO', 'CANC', 1, SYSDATE);
--  agenda cambio operatore 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_OPE', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_OPE', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_OPE', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'AG_OPE', 'CANC', 1, SYSDATE);
--  POSSIBILITa'' APRIRE AGENDA DI ALTRI OPERATORI NEL GRUPPO DI APPARTENENZA 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'APRIAGGR', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'APRIAGGR', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'APRIAGGR', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'APRIAGGR', 'CANC', 1, SYSDATE);
-- CHIAVI ISAS PER IL DIARIO 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RMDIARIO', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RMDIARIO', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RMDIARIO', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RMDIARIO', 'CANC', 1, SYSDATE);
-- CHIAVE ISAS PER INTOLLERANZA ALLERGIA
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'INTOALLE', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'INTOALLE', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'INTOALLE', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'INTOALLE', 'CANC', 1, SYSDATE);
-- CHIAVE ISAS PER SEGNALAZIONI
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SEGNALAZ', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SEGNALAZ', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SEGNALAZ', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SEGNALAZ', 'CANC', 1, SYSDATE);
-- CHIAVE ISAS PER LA STAMPA DEL RIEPILO ASSISTITI 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RIEPASSI', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RIEPASSI', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RIEPASSI', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'RIEPASSI', 'CANC', 1, SYSDATE);
--  scala bisogno 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SC_BISOG', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SC_BISOG', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SC_BISOG', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SC_BISOG', 'CANC', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'EXFLUSSI', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'EXFLUSSI', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'EXFLUSSI', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'EXFLUSSI', 'CANC', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SCBISSAN', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SCBISSAN', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SCBISSAN', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SCBISSAN', 'CANC', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONV_UVI', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONV_UVI', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONV_UVI', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CONV_UVI', 'CANC', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'FLS21', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'FLS21', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'FLS21', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'FLS21', 'CANC', 1, SYSDATE);
-- Lista preferenze per so 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SOLSPREF', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SOLSPREF', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SOLSPREF', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'SOLSPREF', 'CANC', 1, SYSDATE);
-- Lista graduatoria RSA 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'LISTARSA', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'LISTARSA', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'LISTARSA', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'LISTARSA', 'CANC', 1, SYSDATE);
-- esito della valutazione uvi nella SO 
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ESTVALVU', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ESTVALVU', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ESTVALVU', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ESTVALVU', 'CANC', 1, SYSDATE);

-- CHIUSURA contatti attivi
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CHI_CONT', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CHI_CONT', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CHI_CONT', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'CHI_CONT', 'CANC', 1, SYSDATE);
-- ATTRIBUZIONE NUOVO OPERATORE REFERENTE
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ATRNV_OP', 'CONS', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ATRNV_OP', 'INSE', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ATRNV_OP', 'MODI', 1, SYSDATE);
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ATRNV_OP', 'CANC', 1, SYSDATE);

/* Stampe Altro -> Elenco assistiti con contatto aperto  */
INSERT INTO isas_procprof VALUES (&var_isas_pid., &var_isas_profid., 'ELASSALT', 'CONS', 1, SYSDATE);


COMMIT; 