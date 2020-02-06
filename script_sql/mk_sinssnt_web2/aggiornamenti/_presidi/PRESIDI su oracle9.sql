DROP MATERIALIZED VIEW PRESIDI;

CREATE MATERIALIZED VIEW PRESIDI
NOCACHE
LOGGING
NOCOMPRESS
NOPARALLEL
BUILD IMMEDIATE
REFRESH FORCE ON DEMAND
WITH PRIMARY KEY
AS 
SELECT p.codreg, p.codazsan, p.codpres, p.codzon, p.zeri, p.codcom, p.despres,
       p.pubpriv, p.coddistr, p.tipopres, p.indirizzo, p.telefono, p.fax,
       p.email, p.cod_istat_titolare, p.data_inizio, p.data_fine, p.tipo_ass1,
       p.tipo_ass2, p.tipo_ass3, p.tipo_ass4,
       CAST (NULL AS CHAR (6)) codospedale,
       CAST (NULL AS CHAR (2)) cod_tipocds,
       CAST(NULL AS CHAR(1)) rct_tipopres, aster_tipopres,
       CAST (NULL AS CHAR (1)) struttura_ricovero,
       SYSDATE ultimo_aggiornamento, 'N' flg_rct_sts11
  FROM presidi_tab p
 WHERE NOT EXISTS (
          SELECT *
            FROM rct_presidi_sts11 r
           WHERE TRIM (r.cod_regione) = p.codreg
             AND TRIM (r.cod_azienda) = p.codazsan
             AND TRIM (r.cod_struttura) = p.codpres)
   AND p.data_inizio =
          (SELECT MAX (p1.data_inizio)
             FROM presidi_tab p1
            WHERE p1.codreg = p.codreg
              AND p1.codazsan = p.codazsan
              AND p1.codpres = p.codpres)
   AND (   (p.codazsan =
                  (SELECT TRIM(k.conf_txt)
                     FROM conf k
                    WHERE k.conf_kproc = 'SINS' AND k.conf_key = 'codice_usl')
           )
        OR (    (p.codazsan <>
                    (SELECT TRIM(k.conf_txt)
                       FROM conf k
                      WHERE k.conf_kproc = 'SINS'
                            AND k.conf_key = 'codice_usl')
                )
            AND (p.codpres NOT IN (
                    SELECT p1.codpres
                      FROM presidi_tab p1
                     WHERE p1.codazsan =
                              (SELECT TRIM(k.conf_txt)
                                 FROM conf k
                                WHERE k.conf_kproc = 'SINS'
                                  AND k.conf_key = 'codice_usl'))
                )
           )
       )
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
       CAST (NULL AS VARCHAR2 (25)) telefono, CAST (NULL AS VARCHAR2 (25)) fax,
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
 ) TBDISTR on TBDISTR.codice=p.cod_comune
 WHERE p.data_inizio =
          (SELECT MAX (p1.data_inizio)
             FROM rct_presidi_sts11 p1
            WHERE p1.cod_regione = p.cod_regione
              AND p1.cod_azienda = p.cod_azienda
              AND p1.cod_struttura = p.cod_struttura)
   AND (   (p.cod_azienda =
                  (SELECT TRIM(k.conf_txt)
                     FROM conf k
                    WHERE k.conf_kproc = 'SINS' AND k.conf_key = 'codice_usl')
           )
        OR (    (p.cod_azienda <>
                    (SELECT TRIM(k.conf_txt)
                       FROM conf k
                      WHERE k.conf_kproc = 'SINS'
                            AND k.conf_key = 'codice_usl')
                )
            AND (p.cod_struttura NOT IN (
                    SELECT p1.cod_struttura
                      FROM rct_presidi_sts11 p1
                     WHERE p1.cod_azienda =
                              (SELECT TRIM(k.conf_txt)
                                 FROM conf k
                                WHERE k.conf_kproc = 'SINS'
                                  AND k.conf_key = 'codice_usl'))
                )
           )
       );

--COMMENT ON MATERIALIZED VIEW PRESIDI IS 'snapshot table for snapshot SINS_FIRENZE.PRESIDI';

CREATE INDEX I1_PRESIDI ON PRESIDI
(CODDISTR)
LOGGING
NOPARALLEL;

CREATE INDEX I2_PRESIDI ON PRESIDI
(CODPRES)
LOGGING
NOPARALLEL;

CREATE INDEX I3_PRESIDI ON PRESIDI
(DESPRES)
LOGGING
NOPARALLEL;

CREATE INDEX I4_PRESIDI ON PRESIDI
(CODREG, CODAZSAN, CODPRES)
LOGGING
NOPARALLEL;
