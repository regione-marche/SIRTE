set define on;
define user_schema_sins=&enter_user_schema_for_SINS;
define user_schema_isas=&enter_user_schema_for_ISAS;

CREATE OR REPLACE FORCE VIEW &user_schema_isas..V_ISASPROCUSER (isas_kproc,
					                                             isas_kuser
					                                            )
AS
   SELECT p.isas_kproc, u.isas_kuser
     FROM isas_user u, isas_proc p, isas_procuser pu
    WHERE u.isas_uid = pu.isas_uid AND pu.isas_pid = p.isas_pid
          WITH READ ONLY;


GRANT SELECT ON &user_schema_isas..V_ISASPROCUSER TO &user_schema_sins.;

COMMIT;