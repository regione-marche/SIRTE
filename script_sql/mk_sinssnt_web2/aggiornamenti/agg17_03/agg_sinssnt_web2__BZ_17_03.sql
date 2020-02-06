-- Inserimento in TAB_VOCI della voce NON IDONEO per lo stato richiesta. Valido solo per 
-- il database di Bolzano.
INSERT INTO tbl_tab_voci
            (tab_cod, tab_val,
             tab_descrizione, tab_fiss, tab_numero,
             tab_codreg, jdbinterf_lastcng, jdbinterf_version, aster_lang
            )
     VALUES ('RPRSASTA', 'NI',
             'NON IDONEO', '0', 0,
             '1', NULL, NULL,'IT'
            );
			
INSERT INTO tbl_tab_voci
            (tab_cod, tab_val,
             tab_descrizione, tab_fiss, tab_numero,
             tab_codreg, jdbinterf_lastcng, jdbinterf_version, aster_lang
            )
     VALUES ('RPRSASTA', 'NI',
             'UNGEEIGNET', '0', 0,
             '1', NULL, NULL,'DE'
            );
COMMIT;