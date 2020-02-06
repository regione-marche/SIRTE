set define on;
define user_schema_sins=&enter_user_schema_for_SINS;
define user_schema_isas=&enter_user_schema_for_ISAS;

CREATE OR REPLACE FORCE VIEW &user_schema_sins..V_ISASPROCUSER (isas_kproc,
					                                             isas_kuser
					                                            )
AS
   SELECT "ISAS_KPROC", "ISAS_KUSER"
     FROM &user_schema_isas..V_ISASPROCUSER;
     
COMMIT;