ALTER TABLE PRESIDI_TAB ADD (ASTER_TIPOPRES  CHAR(1));
COMMIT;


------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------
-- Creazione vista materializzata PRESIDI_DATA con lettura da tabelle RCT_PRESIDI_STS11 e PRESIDI_TAB
------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------
DROP MATERIALIZED VIEW presidi_data;
CREATE MATERIALIZED VIEW presidi_data (codreg,
										codazsan,
										codpres,
										codzon,
										zeri,
										codcom,
										despres,
										pubpriv,
										coddistr,
										tipopres,
										indirizzo,
										telefono,
										fax,
										email,
										cod_istat_titolare,
										data_inizio,
										data_fine,
										tipo_ass1,
										tipo_ass2,
										tipo_ass3,
										tipo_ass4,
										codospedale,
										cod_tipocds,
										rct_tipopres,
										aster_tipopres,
										struttura_ricovero,
										ultimo_aggiornamento,
										flg_rct_sts11
									   )
	NOCACHE
	LOGGING
	NOCOMPRESS
	NOPARALLEL
	BUILD IMMEDIATE
	REFRESH FORCE ON DEMAND
	WITH PRIMARY KEY
AS 
	SELECT p1.codreg, p1.codazsan, p1.codpres,
			p1.codzon, p1.zeri, p1.codcom,
			p1.despres, p1.pubpriv, p1.coddistr,
			p1.tipopres, p1.indirizzo, p1.telefono,
			p1.fax, p1.email, p1.cod_istat_titolare,
			p1.data_inizio, p1.data_fine,
			p1.tipo_ass1, p1.tipo_ass2, p1.tipo_ass3, p1.tipo_ass4, 
			CAST(NULL AS CHAR(6)) codospedale, CAST(NULL AS CHAR(2)) cod_tipocds, 
			CAST(NULL AS CHAR(1)) rct_tipopres, aster_tipopres,
			CAST(NULL AS CHAR(1)) struttura_ricovero, SYSDATE ultimo_aggiornamento, 'N' flg_rct_sts11
    FROM presidi_tab p1
    WHERE NOT EXISTS (
            SELECT *
            FROM rct_presidi_sts11 r
            WHERE TRIM (r.cod_regione) = p1.codreg
            AND TRIM (r.cod_azienda) = p1.codazsan
            AND TRIM (r.cod_struttura) = p1.codpres
	)
UNION
	SELECT CAST (p.cod_regione AS CHAR (3)) codreg,
		CAST (p.cod_azienda AS CHAR (3)) codazsan, 		
        CAST (p.cod_struttura AS CHAR (6)) codpres,
		COALESCE ((SELECT d.cod_zona
			FROM comuni c, distretti d
			WHERE c.codice = p.cod_comune
			AND c.cod_distretto = d.cod_distr
			AND c.cod_reg = (SELECT k.conf_txt
						FROM conf k
						WHERE k.conf_kproc = 'SINS'
						AND k.conf_key = 'codice_regione')
			AND c.cod_usl = (SELECT k.conf_txt
						FROM conf k
						WHERE k.conf_kproc = 'SINS'
						AND k.conf_key = 'codice_usl')), '0') codzon,
		CAST(NULL AS CHAR(3)) zeri,
		CAST (p.cod_comune AS CHAR (6)) codcom,
		CAST (p.descrizione AS CHAR (50)) despres,
		CAST (p.cod_pubblicoprivato AS CHAR (1)) pubpriv,
		COALESCE ((SELECT c.cod_distretto
			FROM comuni c
			WHERE c.codice = p.cod_comune
			AND c.cod_reg = (SELECT k.conf_txt
						FROM conf k
						WHERE k.conf_kproc = 'SINS'
						AND k.conf_key = 'codice_regione')
			AND c.cod_usl = (SELECT k.conf_txt
						FROM conf k
						WHERE k.conf_kproc = 'SINS'
						AND k.conf_key = 'codice_usl')), '0     ') coddistr,
		CAST(NULL AS CHAR(1)) tipopres,
		CAST (NULL AS VARCHAR2 (50)) indirizzo,
        CAST (NULL AS VARCHAR2 (25)) telefono,
        CAST (NULL AS VARCHAR2 (25)) fax,
        CAST (NULL AS VARCHAR2 (100)) email,
        CAST (NULL AS VARCHAR2 (11)) cod_istat_titolare,
		p.data_inizio, p.data_fine,
		p.tipo_ass1, p.tipo_ass2, p.tipo_ass3, p.tipo_ass4, 
		p.codospedale, p.cod_tipocds,
		p.cod_tipopres rct_tipopres, p.aster_tipopres, 
        p.struttura_ricovero, p.ultimo_aggiornamento, 'S' flg_rct_sts11
    FROM rct_presidi_sts11 p;

CREATE INDEX i1_presidi_data ON presidi_data(coddistr);
CREATE INDEX i2_presidi_data ON presidi_data(codpres);
CREATE INDEX i3_presidi_data ON presidi_data(despres);	
CREATE INDEX i4_presidi_data ON presidi_data(data_inizio);	
CREATE INDEX i5_presidi_data ON presidi_data(codreg, codazsan, codpres, data_inizio);	


------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------
-- Creazione vista materializzata PRESIDI con lettura da tabelle RCT_PRESIDI_STS11 e PRESIDI_TAB
------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------
DROP MATERIALIZED VIEW PRESIDI;
CREATE MATERIALIZED VIEW PRESIDI (codreg,
									codazsan,										
									codpres,
									codzon,
									zeri,
									codcom,
									despres,
									pubpriv,
									coddistr,
									tipopres,
									indirizzo,
									telefono,
									fax,
									email,
									cod_istat_titolare,
									data_inizio,
									data_fine,	
									tipo_ass1,
									tipo_ass2,
									tipo_ass3,
									tipo_ass4,
									codospedale,									
									cod_tipocds,
									rct_tipopres,
									aster_tipopres,										
									struttura_ricovero,										
									ultimo_aggiornamento,										
									flg_rct_sts11
								   )
	NOCACHE
	LOGGING
	NOCOMPRESS
	NOPARALLEL
	BUILD IMMEDIATE
	REFRESH FORCE ON DEMAND
	WITH PRIMARY KEY
AS 
	SELECT p.codreg, p.codazsan, p.codpres,
			p.codzon, p.zeri, p.codcom,
			p.despres, p.pubpriv, p.coddistr,
			p.tipopres, p.indirizzo, p.telefono,
			p.fax, p.email, p.cod_istat_titolare,
			p.data_inizio, p.data_fine,
			p.tipo_ass1, p.tipo_ass2, p.tipo_ass3, p.tipo_ass4, 
			CAST(NULL AS CHAR(6)) codospedale, CAST(NULL AS CHAR(2)) cod_tipocds, 
			CAST(NULL AS CHAR(1)) rct_tipopres, aster_tipopres,
			CAST(NULL AS CHAR(1)) struttura_ricovero, SYSDATE ultimo_aggiornamento, 'N' flg_rct_sts11
    FROM presidi_tab p
    WHERE NOT EXISTS (
            SELECT *
            FROM rct_presidi_sts11 r
            WHERE TRIM (r.cod_regione) = p.codreg
            AND TRIM (r.cod_azienda) = p.codazsan
            AND TRIM (r.cod_struttura) = p.codpres
	)
	AND p.data_inizio = (SELECT MAX (p1.data_inizio)
			                FROM presidi_tab p1
							WHERE p1.codreg = p.codreg
			                AND p1.codazsan = p.codazsan
			                AND p1.codpres = p.codpres)
	AND (
		(p.codazsan = (SELECT k.conf_txt
	                    FROM conf k
	                    WHERE k.conf_kproc = 'SINS' AND k.conf_key = 'codice_usl')
		) 
		OR
		(
			(p.codazsan <> (SELECT k.conf_txt
		                    FROM conf k
		                    WHERE k.conf_kproc = 'SINS' AND k.conf_key = 'codice_usl')
			)
			AND
			(
				p.codpres NOT IN (SELECT p1.codpres
									FROM presidi_tab p1
									WHERE p1.codazsan = (SELECT k.conf_txt
															FROM conf k
															WHERE k.conf_kproc = 'SINS' AND k.conf_key = 'codice_usl')
				)
			)
		)
	)
UNION
	SELECT CAST (p.cod_regione AS CHAR (3)) codreg,
		CAST (p.cod_azienda AS CHAR (3)) codazsan, 		
        CAST (p.cod_struttura AS CHAR (6)) codpres,
		COALESCE ((SELECT d.cod_zona
			FROM comuni c, distretti d
			WHERE c.codice = p.cod_comune
			AND c.cod_distretto = d.cod_distr
			AND c.cod_reg = (SELECT k.conf_txt
						FROM conf k
						WHERE k.conf_kproc = 'SINS'
						AND k.conf_key = 'codice_regione')
			AND c.cod_usl = (SELECT k.conf_txt
						FROM conf k
						WHERE k.conf_kproc = 'SINS'
						AND k.conf_key = 'codice_usl')), '0') codzon,
		CAST(NULL AS CHAR(3)) zeri,
		CAST (p.cod_comune AS CHAR (6)) codcom,
		CAST (p.descrizione AS CHAR (50)) despres,
		CAST (p.cod_pubblicoprivato AS CHAR (1)) pubpriv,
		COALESCE ((SELECT c.cod_distretto
			FROM comuni c
			WHERE c.codice = p.cod_comune
			AND c.cod_reg = (SELECT k.conf_txt
						FROM conf k
						WHERE k.conf_kproc = 'SINS'
						AND k.conf_key = 'codice_regione')
			AND c.cod_usl = (SELECT k.conf_txt
						FROM conf k
						WHERE k.conf_kproc = 'SINS'
						AND k.conf_key = 'codice_usl')), '0     ') coddistr,
		CAST(NULL AS CHAR(1)) tipopres,
		CAST (NULL AS VARCHAR2 (50)) indirizzo,
        CAST (NULL AS VARCHAR2 (25)) telefono,
        CAST (NULL AS VARCHAR2 (25)) fax,
        CAST (NULL AS VARCHAR2 (100)) email,
        CAST (NULL AS VARCHAR2 (11)) cod_istat_titolare,
		p.data_inizio, p.data_fine,
		p.tipo_ass1, p.tipo_ass2, p.tipo_ass3, p.tipo_ass4, 
		p.codospedale, p.cod_tipocds,
		p.cod_tipopres rct_tipopres, p.aster_tipopres, 
        p.struttura_ricovero, p.ultimo_aggiornamento, 'S' flg_rct_sts11
    FROM rct_presidi_sts11 p
	WHERE p.data_inizio = (SELECT MAX (p1.data_inizio)
							FROM rct_presidi_sts11 p1
							WHERE p1.cod_regione = p.cod_regione
							AND p1.cod_azienda = p.cod_azienda
							AND p1.cod_struttura = p.cod_struttura)
	AND (
		(p.cod_azienda = (SELECT TRIM(k.conf_txt)
	                    FROM conf k
	                    WHERE k.conf_kproc = 'SINS' AND k.conf_key = 'codice_usl')
		) 
		OR
		(
			(p.cod_azienda <> (SELECT TRIM(k.conf_txt)
		                    FROM conf k
		                    WHERE k.conf_kproc = 'SINS' AND k.conf_key = 'codice_usl')
			)
			AND
			(
				p.cod_struttura NOT IN (SELECT p1.cod_struttura
									FROM rct_presidi_sts11 p1
									WHERE p1.cod_azienda = (SELECT TRIM(k.conf_txt)
															FROM conf k
															WHERE k.conf_kproc = 'SINS' AND k.conf_key = 'codice_usl')
				)
			)
		)
	);
CREATE index i1_presidi ON presidi(coddistr);
CREATE index i2_presidi ON presidi(codpres);
CREATE index i3_presidi ON presidi(despres);
CREATE INDEX i4_presidi ON presidi(codreg, codazsan, codpres);	
COMMIT;

------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------
-- Creazione TRIGGER su tabelle RCT_PRESIDI_STS11 e PRESIDI_TAB per invocazione REFRESH su viste materializzate
------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------
------------------------------------------------------------------------------------------------------------------------------------------------------
CREATE OR REPLACE TRIGGER refresh_mview_on_presidi_rct
AFTER INSERT OR UPDATE OR DELETE ON rct_presidi_sts11
	FOR each row
	DECLARE
			l_job NUMBER;
	BEGIN
		dbms_job.submit( l_job, 'DBMS_MVIEW.REFRESH(''presidi_data, presidi'');');
	END refresh_mview_on_presidi_rct;

CREATE OR REPLACE TRIGGER refresh_mview_on_presidi_tab
AFTER INSERT OR UPDATE OR DELETE on presidi_tab
	FOR each row
	DECLARE
			l_job NUMBER;
	BEGIN
		dbms_job.submit( l_job, 'DBMS_MVIEW.REFRESH(''presidi_data, presidi'');');
	END refresh_mview_on_presidi_tab;
COMMIT;