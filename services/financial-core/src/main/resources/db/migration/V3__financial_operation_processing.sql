 ALTER TABLE financial_operations
 DROP CONSTRAINT financial_operations_status_check;

 ALTER TABLE financial_operations
 ADD CONSTRAINT chk_financial_operations_status
 CHECK(status IN('PROCESSING', 'SUCCEEDED', 'DECLINED'));