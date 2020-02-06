DROP MATERIALIZED VIEW PRESIDI_DATA;

CREATE MATERIALIZED VIEW PRESIDI_DATA
NOCACHE
LOGGING
NOCOMPRESS
NOPARALLEL
BUILD IMMEDIATE
REFRESH FORCE ON DEMAND
WITH PRIMARY KEY
AS 
SELECT p1.codreg, p1.codazsan, p1.codpres, p1.codzon, p1.zeri, p1.codcom,
       p1.despres, p1.pubpriv, p1.coddistr, p1.tipopres, p1.indirizzo,
       p1.telefono, p1.fax, p1.email, p1.cod_istat_titolare, p1.data_inizio,
       p1.data_fine, p1.tipo_ass1, p1.tipo_ass2, p1.tipo_ass3, p1.tipo_ass4,
       CAST (NULL AS CHAR (6)) codospedale,
       CAST (NULL AS CHAR (2)) cod_tipocds,
       CAST(NULL AS CHAR(1)) rct_tipopres, aster_tipopres,
       CAST (NULL AS CHAR (1)) struttura_ricovero,
       SYSDATE ultimo_aggiornamento, 'N' flg_rct_sts11
  FROM presidi_tab p1
 WHERE NOT EXISTS (
          SELECT *
            FROM rct_presidi_sts11 r
           WHERE TRIM (r.cod_regione) = p1.codreg
             AND TRIM (r.cod_azienda) = p1.codazsan
             AND TRIM (r.cod_struttura) = p1.codpres)
UNION
SELECT CAST (p.cod_regione AS CHAR (3)) codreg,
       CAST (p.cod_azienda AS CHAR (3)) codazsan,
       CAST (p.cod_struttura AS CHAR (6)) codpres,
       TBZONA.codzon,
       CAST (NULL AS CHAR (3)) zeri, CAST (p.cod_comune AS CHAR (6)) codcom,
       CAST (p.descrizione AS CHAR (50)) despres,
       CAST (p.cod_pubblicoprivato AS CHAR (1)) pubpriv,
       TBDISTR.coddistr,
       CAST (NULL AS CHAR (1)) tipopres,
       CAST (NULL AS VARCHAR2 (50)) indirizzo,
       CAST (NULL AS VARCHAR2 (25)) telefono, CAST (NULL AS VARCHAR2 (25))
                                                                          fax,
       CAST (NULL AS VARCHAR2 (100)) email,
       CAST (NULL AS VARCHAR2 (11)) cod_istat_titolare, p.data_inizio,
       p.data_fine, p.tipo_ass1, p.tipo_ass2, p.tipo_ass3, p.tipo_ass4,
       p.codospedale, p.cod_tipocds, p.cod_tipopres rct_tipopres,
       p.aster_tipopres, p.struttura_ricovero, p.ultimo_aggiornamento,
       'S' flg_rct_sts11
  FROM rct_presidi_sts11 p
  left join (
   SELECT c.codice, COALESCE (d.cod_zona,'0') codzon

                    FROM comuni c, distretti d
                   WHERE 0=0--c.codice = p.cod_comune
                     AND c.cod_distretto = d.cod_distr
                     AND c.cod_reg =
                            (SELECT k.conf_txt
                               FROM conf k
                              WHERE k.conf_kproc = 'SINS'
                                AND k.conf_key = 'codice_regione')
                     AND c.cod_usl =
                            (SELECT k.conf_txt
                               FROM conf k
                              WHERE k.conf_kproc = 'SINS'
                                AND k.conf_key = 'codice_usl')
  ) TBZONA on TBZONA.codice=p.cod_comune
  left join (
           SELECT c.codice, COALESCE (c.cod_distretto,'0') coddistr
            FROM comuni c
           WHERE 0=0--c.codice = p.cod_comune
             AND c.cod_reg =
                    (SELECT k.conf_txt
                       FROM conf k
                      WHERE k.conf_kproc = 'SINS'
                        AND k.conf_key = 'codice_regione')
             AND c.cod_usl =
                    (SELECT k.conf_txt
                       FROM conf k
                      WHERE k.conf_kproc = 'SINS'
                        AND k.conf_key = 'codice_usl')  
  ) TBDISTR on TBDISTR.codice=p.cod_comune;
  
  

--COMMENT ON MATERIALIZED VIEW PRESIDI_DATA IS 'snapshot table for snapshot PRESIDI_DATA';

CREATE INDEX I1_PRESIDI_DATA ON PRESIDI_DATA
(CODDISTR)
LOGGING
NOPARALLEL;

CREATE INDEX I2_PRESIDI_DATA ON PRESIDI_DATA
(CODPRES)
LOGGING
NOPARALLEL;

CREATE INDEX I3_PRESIDI_DATA ON PRESIDI_DATA
(DESPRES)
LOGGING
NOPARALLEL;

CREATE INDEX I4_PRESIDI_DATA ON PRESIDI_DATA
(DATA_INIZIO)
LOGGING
NOPARALLEL;

CREATE INDEX I5_PRESIDI_DATA ON PRESIDI_DATA
(CODREG, CODAZSAN, CODPRES, DATA_INIZIO)
LOGGING
NOPARALLEL;
