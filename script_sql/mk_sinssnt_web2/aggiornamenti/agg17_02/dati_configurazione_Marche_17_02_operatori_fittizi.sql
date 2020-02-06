--- ***********************************************************************
--- SINSSNT_WEB2 integrato con Fascicolo Sanitario Elettronico (Marche)
--- ***********************************************************************


SET DEFINE OFF;

-- creazione degli operatori fittizi per presidio e per tipologia

declare
presidio presidi%rowtype;
opertipo_rec  opertipo%rowtype;
codiceOp char(10);
begin
for presidio in (select * from presidi where codzon = 2 order by codzon)
loop

  dbms_output.put_line('Codice_presidio: ' || presidio.codpres); 
  for opertipo_rec in (select * from opertipo o where o.ISAS_KPROC='SINS_AS' AND o.KEY_SUBSET='ALL' AND O.COD_TIPO='02' order by o.cod_tipo)
  loop
    codiceOp := ('XX' || opertipo_rec.cod_tipo || presidio.codpres);
    dbms_output.put_line('Nuovo operatore: ' || codiceOp); 
   Insert into OPERATORI (JDBINTERF_VERSION,JDBINTERF_LASTCNG,CODICE,COGNOME,NOME,TIPO,COD_ZONA,COD_PRESIDIO, COD_FISCALE,DATA_INIZIO,DATA_FINE,DIPEND_CONV, FLAG_FITTIZIO)
    values (1,sysdate,codiceOp, opertipo_rec.desc_tipo ||' fittizio per:' ,SUBSTR(presidio.DESPRES,0,30) , opertipo_rec.cod_tipo,presidio.codzon,presidio.codpres, 'XXXXXX99X99X999X', to_date('12-DIC-09 00:00:00','DD-MON-RR HH24:MI:SS'),to_date('15-AGO-45 00:00:00','DD-MON-RR HH24:MI:SS'), null,'S');

--    Insert into OPERATORI (JDBINTERF_VERSION,JDBINTERF_LASTCNG,CODICE,COGNOME,NOME,TIPO,COD_ZONA,COD_PRESIDIO, COD_FISCALE,DATA_INIZIO,DATA_FINE,DIPEND_CONV, FLAG_FITTIZIO)
--    values (1,sysdate,codicep, opertipo_rec.desc_tipo ||' fittizio per:' ,SUBSTR(presidio.DESPRES,0,30) , opertipo_rec.cod_tipo,presidio.codzon,presidio.codpres, 'XXXXXX99X99X999X', to_date('12-DIC-09 00:00:00','DD-MON-RR HH24:MI:SS'),to_date('15-AGO-45 00:00:00','DD-MON-RR HH24:MI:SS'), null,'S');

  end loop;
end loop;
end;


-- CREAZIONE DELLE PRESTAZIONI PER NON EROGATO UNA PER TIPOLOGIA
declare
prest prestaz%rowtype;
codiceOp VARCHAR2(4);
begin
for codicePrest in (select prest_tipo, substr(max(prest_cod),0,3) as cprest 
from prestaz 
group by prest_tipo order by cprest)
loop

  dbms_output.put_line('Codice_prest: ' || codicePrest.cprest); 
  Insert into prestaz (PREST_COD,PREST_DES,PREST_DES_DETT,PREST_TIPO) 
  values ('999'||codicePrest.cprest,'NON EROGATO','Accesso effettuato ma prestazione non eseguita',codicePrest.PREST_TIPO);

end loop;
end;


COMMIT;

