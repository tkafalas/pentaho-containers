-- Pentaho Monitoring Datamart

ALTER SESSION SET "_ORACLE_SCRIPT"=true;
CREATE USER pentaho_operations_mart IDENTIFIED BY "password" default tablespace pentaho_tablespace quota unlimited on pentaho_tablespace temporary tablespace temp quota 5M on system;
GRANT RESOURCE, CREATE SESSION TO pentaho_operations_mart;

-- DIM_BATCH

CREATE TABLE  pentaho_operations_mart.dim_batch (
  batch_tk number(22,0) NOT NULL CONSTRAINT DIM_BATCH_PK PRIMARY KEY,
  batch_id number(22,0) NULL,
  logchannel_id varchar2(100) NULL,
  parent_logchannel_id varchar2(100) NULL
);

CREATE INDEX pentaho_operations_mart.I_DIM_BATCH_LOOKUP
ON pentaho_operations_mart.dim_batch(batch_id, logchannel_id, parent_logchannel_id);

-- DIM_DATE

CREATE TABLE  pentaho_operations_mart.dim_date (
  date_tk number(22,0) NULL CONSTRAINT DATE_PK PRIMARY KEY,
  date_field date NULL,
  ymd varchar2(10) NULL,
  ym varchar2(7) NULL,
  year number(22,0) NULL,
  quarter number(22,0) NULL,
  quarter_code char(2) NULL,
  month number(22,0) NULL,
  month_desc varchar2(20) NULL,
  month_code char(15) NULL,
  day number(22,0) NULL,
  day_of_year number(22,0) NULL,
  day_of_week number(22,0) NULL,
  day_of_week_desc varchar2(20) NULL,
  day_of_week_code varchar2(15) NULL,
  week number(22,0) NULL
);

-- DIM_EXECUTION

CREATE TABLE  pentaho_operations_mart.dim_execution (
  execution_tk number(22,0) NOT NULL CONSTRAINT EXECUTION_PK PRIMARY KEY,
  execution_id varchar2(100) NULL,
  server_host varchar2(100) NULL,
  executing_user varchar2(100) NULL,
  execution_status varchar2(30) NULL,
  client varchar2(255) NULL
);

create index pentaho_operations_mart.I_DIM_EXECUTION_LOOKUP
on pentaho_operations_mart.dim_execution(execution_id,server_host,executing_user,client);

-- DIM_EXECUTOR

CREATE TABLE pentaho_operations_mart.dim_executor (
  executor_tk number(22,0) NOT NULL CONSTRAINT DIM_EXECUTOR_PK PRIMARY KEY,
  version number(22,0) NULL,
  date_from date NULL,
  date_to date NULL,
  executor_id varchar2(255) NULL,
  executor_source varchar2(255) NULL,
  executor_environment varchar2(255) NULL,
  executor_type varchar2(255) NULL,
  executor_name varchar2(255) NULL,
  executor_desc varchar2(255)  NULL,
  executor_revision varchar2(255) NULL,
  executor_version_label varchar2(255) NULL,
  exec_enabled_table_logging char(1) NULL,
  exec_enabled_detailed_logging char(1) NULL,
  exec_enabled_perf_logging char(1) NULL,
  exec_enabled_history_logging char(1) NULL,
  last_updated_date date NULL,
  last_updated_user varchar2(255) NULL
);

create index pentaho_operations_mart.I_DIM_EXECUTOR_LOOKUP
on pentaho_operations_mart.dim_executor(executor_id);

-- DIM_LOG_TABLE

CREATE TABLE pentaho_operations_mart.dim_log_table (
  log_table_tk number(22,0) NOT NULL CONSTRAINT DIM_LOG_TABLE_PK PRIMARY KEY,
  object_type varchar2(30) NULL,
  table_connection_name varchar2(255) NULL,
  schema_name varchar2(255) NULL,
  table_name varchar2(255) NULL,
  step_entry_table_conn_name varchar2(255) NULL,
  step_entry_table_name varchar2(255) NULL,
  step_entry_schema_name varchar2(255) NULL,
  perf_table_conn_name varchar2(255) NULL,
  perf_table_name varchar2(255) NULL,
  perf_schema_name varchar2(255) NULL
);

create index pentaho_operations_mart.I_DIM_LOG_TABLE
on pentaho_operations_mart.dim_log_table (object_type, table_connection_name, table_name, schema_name);

create index pentaho_operations_mart.I_dim_log_step_entry
on pentaho_operations_mart.dim_log_table (object_type, step_entry_table_conn_name, step_entry_table_name, schema_name);

create index pentaho_operations_mart.I_dim_log_perf
on pentaho_operations_mart.dim_log_table (object_type, perf_table_conn_name, perf_table_name, perf_schema_name);


--
-- Definition of table pentaho_operations_mart.dim_log_table
--

CREATE TABLE  pentaho_operations_mart.dim_step (
  step_tk number(22,0) NOT NULL CONSTRAINT DIM_STEP_PK PRIMARY KEY,
  step_id varchar2(255) NULL,
  original_step_name varchar2(255) NULL
);

CREATE INDEX pentaho_operations_mart.IDX_DIM_STEP_LOOKUP
ON pentaho_operations_mart.dim_step(step_id);


-- DIM_TIME

CREATE TABLE  pentaho_operations_mart.dim_time (
  time_tk number(22,0) NULL CONSTRAINT DIM_TIME_PK PRIMARY KEY,
  hms varchar2(8) NULL,
  hm varchar2(5) NULL,
  ampm varchar2(8) NULL,
  hour number(22,0) NULL,
  hour12 number(22,0) NULL,
  minute number(22,0) NULL,
  second number(22,0) NULL
);

-- FACT_EXECUTION

CREATE TABLE pentaho_operations_mart.fact_execution (
  execution_date_tk number(22,0) NULL,
  execution_time_tk number(22,0) NULL,
  batch_tk number(22,0) NULL,
  execution_tk number(22,0) NULL,
  executor_tk number(22,0) NULL,
  parent_executor_tk number(22,0) NULL,
  root_executor_tk number(22,0) NULL,
  execution_timestamp TIMESTAMP NULL,
  duration float(24) NULL,
  rows_input number(22,0) NULL,
  rows_output number(22,0) NULL,
  rows_read number(22,0) NULL,
  rows_written number(22,0) NULL,
  rows_rejected number(22,0) NULL,
  errors number(22,0) NULL,
  failed number(1,0) NULL
);

create index pentaho_operations_mart.I_FACT_EXEC_EXEC_DATE_TK 
on pentaho_operations_mart.fact_execution (execution_date_tk);

create index pentaho_operations_mart.I_FACT_EXEC_EXEC_TIME_TK 
on pentaho_operations_mart.fact_execution (execution_time_tk);

create index pentaho_operations_mart.I_FACT_EXEC_BATCH_TK 
on pentaho_operations_mart.fact_execution (batch_tk);

create index pentaho_operations_mart.I_FACT_EXEC_EXECI_TK 
on pentaho_operations_mart.fact_execution (execution_tk);

create index pentaho_operations_mart.I_FACT_EXEC_EXECR_TK 
on pentaho_operations_mart.fact_execution (executor_tk);

create index pentaho_operations_mart.I_FACT_EXEC_PARENT_EXEC_TK 
on pentaho_operations_mart.fact_execution (parent_executor_tk);

create index pentaho_operations_mart.I_FACT_EXEC_ROOT_EXEC_TK 
on pentaho_operations_mart.fact_execution (root_executor_tk);

--
-- Definition of table FACT_STEP_EXECUTION
--

CREATE TABLE  pentaho_operations_mart.fact_step_execution (
  execution_date_tk number(22,0) NULL,
  execution_time_tk number(22,0) NULL,
  batch_tk number(22,0) NULL,
  executor_tk number(22,0) NULL,
  parent_executor_tk number(22,0) NULL,
  root_executor_tk number(22,0) NULL,
  step_tk number(22,0) NULL,
  step_copy number(22,0) NULL,
  execution_timestamp TIMESTAMP NULL,
  rows_input number(22,0) NULL,
  rows_output number(22,0) NULL,
  rows_read number(22,0) NULL,
  rows_written number(22,0) NULL,
  rows_rejected number(22,0) NULL,
  errors number(22,0) NULL
);

CREATE INDEX pentaho_operations_mart.I_FACT_S_EXEC_EXEC_DATE_TK 
ON pentaho_operations_mart.fact_step_execution(execution_date_tk);

CREATE INDEX pentaho_operations_mart.I_FACT_S_EXEC_EXEC_TIME_TK 
ON pentaho_operations_mart.fact_step_execution(execution_time_tk);

CREATE INDEX pentaho_operations_mart.I_FACT_S_EXEC_BATCH_TK 
ON pentaho_operations_mart.fact_step_execution(batch_tk);

CREATE INDEX pentaho_operations_mart.I_FACT_S_EXEC_EXECU_TK 
ON pentaho_operations_mart.fact_step_execution(executor_tk);

CREATE INDEX pentaho_operations_mart.I_FACT_S_EXEC_PARENT_EXEC_TK 
ON pentaho_operations_mart.fact_step_execution(parent_executor_tk);

CREATE INDEX pentaho_operations_mart.I_FACT_S_EXEC_ROOT_EXEC_TK 
ON pentaho_operations_mart.fact_step_execution(root_executor_tk);

CREATE INDEX pentaho_operations_mart.I_FACT_S_EXEC_STEP_TK 
ON pentaho_operations_mart.fact_step_execution(step_tk);

--
-- Definition of table FACT_STEP_EXECUTION
--

CREATE TABLE  pentaho_operations_mart.fact_perf_execution (
  execution_date_tk number(22,0) NULL,
  execution_time_tk number(22,0) NULL,
  batch_tk number(22,0) NULL,
  executor_tk number(22,0) NULL,
  parent_executor_tk number(22,0) NULL,
  root_executor_tk number(22,0) NULL,
  step_tk number(22,0) DEFAULT NULL,
  seq_nr number(22,0) DEFAULT NULL,
  step_copy number(22,0) DEFAULT NULL,
  execution_timestamp TIMESTAMP NULL,
  duration double precision NULL,
  rows_input number(22,0) NULL,
  rows_output number(22,0) NULL,
  rows_read number(22,0) NULL,
  rows_written number(22,0) NULL,
  rows_rejected number(22,0) NULL,
  errors number(22,0) NULL,
  input_buffer_rows number(22,0) NULL,
  output_buffer_rows number(22,0) NULL
);

CREATE INDEX pentaho_operations_mart.I_FACT_P_EXEC_EXECU_DATE_TK 
ON pentaho_operations_mart.fact_perf_execution(execution_date_tk);

CREATE INDEX pentaho_operations_mart.I_FACT_P_EXEC_EXECU_TIME_TK 
ON pentaho_operations_mart.fact_perf_execution(execution_time_tk);

CREATE INDEX pentaho_operations_mart.I_FACT_P_EXEC_BATCH_TK 
ON pentaho_operations_mart.fact_perf_execution(batch_tk);

CREATE INDEX pentaho_operations_mart.I_FACT_P_EXEC_EXECU_TK 
ON pentaho_operations_mart.fact_perf_execution(executor_tk);

CREATE INDEX pentaho_operations_mart.I_FACT_P_EXEC_PARENT_EXECU_TK 
ON pentaho_operations_mart.fact_perf_execution(parent_executor_tk);

CREATE INDEX pentaho_operations_mart.I_FACT_P_EXEC_ROOT_EXECu_TK 
ON pentaho_operations_mart.fact_perf_execution(root_executor_tk);

--
-- Definition of table pentaho_operations_mart.FACT_JOBENTRY_EXECUTION
--

CREATE TABLE pentaho_operations_mart.fact_jobentry_execution (
  execution_date_tk number(22,0) NULL,
  execution_time_tk number(22,0) NULL,
  batch_tk number(22,0) NULL,
  executor_tk number(22,0) NULL,
  parent_executor_tk number(22,0) NULL,
  root_executor_tk number(22,0) NULL,
  step_tk number(22,0) NULL,
  execution_timestamp TIMESTAMP NULL,
  rows_input number(22,0) NULL,
  rows_output number(22,0) NULL,
  rows_read number(22,0) NULL,
  rows_written number(22,0) NULL,
  rows_rejected number(22,0) NULL,
  errors number(22,0) NULL,
  result char(5) NULL,
  nr_result_rows number(22,0) NULL,
  nr_result_files number(22,0) NULL
);

CREATE INDEX pentaho_operations_mart.I_FJE_DATE_TK 
ON pentaho_operations_mart.fact_jobentry_execution(execution_date_tk);

CREATE INDEX pentaho_operations_mart.I_FJE_TIME_TK 
ON pentaho_operations_mart.fact_jobentry_execution(execution_time_tk);

CREATE INDEX pentaho_operations_mart.I_FJE_BATCH_TK 
ON pentaho_operations_mart.fact_jobentry_execution(batch_tk);

CREATE INDEX pentaho_operations_mart.I_FJE_executor_TK 
ON pentaho_operations_mart.fact_jobentry_execution(executor_tk);

CREATE INDEX pentaho_operations_mart.I_FJE_PARENT_EXECUTOR_TK 
ON pentaho_operations_mart.fact_jobentry_execution(parent_executor_tk);

CREATE INDEX pentaho_operations_mart.I_FJE_ROOT_EXECUTOR_TK 
ON pentaho_operations_mart.fact_jobentry_execution(root_executor_tk);

CREATE INDEX pentaho_operations_mart.I_FJE_STEP_TK 
ON pentaho_operations_mart.fact_jobentry_execution(step_tk);


CREATE TABLE  pentaho_operations_mart.DIM_STATE (
  state_tk NUMBER(20) NOT NULL,
  state varchar2(100) NOT NULL,
  PRIMARY KEY (state_tk)
);

CREATE TABLE  pentaho_operations_mart.DIM_SESSION (
  session_tk NUMBER(20) NOT NULL,
  session_id varchar2(200) NOT NULL,
  session_type varchar2(200) NOT NULL,
  username varchar2(200) NOT NULL,
  PRIMARY KEY (session_tk)
);

CREATE TABLE  pentaho_operations_mart.DIM_INSTANCE (
  instance_tk NUMBER(20) NOT NULL,
  instance_id varchar2(200) NOT NULL,
  engine_id varchar2(200) NOT NULL,
  service_id varchar2(200) NOT NULL,
  content_id varchar2(1024) NOT NULL,
  content_detail varchar2(1024),
  PRIMARY KEY (instance_tk)
);

CREATE TABLE pentaho_operations_mart.DIM_COMPONENT (
  component_tk NUMBER(20) NOT NULL,
  component_id varchar2(200) NOT NULL,
  PRIMARY KEY (component_tk)
);

CREATE TABLE pentaho_operations_mart.STG_CONTENT_ITEM (
  gid char(36) NOT NULL,
  parent_gid char(36) DEFAULT NULL,
  fileSize int NOT NULL,
  locale varchar2(5) DEFAULT NULL,
  name varchar2(200) NOT NULL,
  ownerType int NOT NULL,
  path varchar2(1024) NOT NULL,
  title varchar2(255) DEFAULT NULL,
  is_folder char(1) NOT NULL,
  is_hidden char(1) NOT NULL,
  is_locked char(1) NOT NULL,
  is_versioned char(1) NOT NULL,
  date_created timestamp NULL,
  date_last_modified timestamp NULL,
  is_processed char(1) DEFAULT NULL,
  PRIMARY KEY (gid)
); 

CREATE TABLE pentaho_operations_mart.DIM_CONTENT_ITEM (
  content_item_tk number(10) NOT NULL,
  content_item_title VARCHAR2(255) NULL,
  content_item_locale VARCHAR2(255) NULL,
  content_item_size number(10) NULL,
  content_item_path VARCHAR2(1024) NULL,
  content_item_name VARCHAR2(255) NOT NULL,
  content_item_fullname VARCHAR2(1024) NOT NULL,
  content_item_type VARCHAR2(32) NOT NULL,
  content_item_extension VARCHAR2(32) NULL,
  content_item_guid CHAR(36) NOT NULL,
  parent_content_item_guid CHAR(36) NOT NULL,
  parent_content_item_tk number(10) NULL,
  content_item_modified timestamp NOT NULL,
  content_item_valid_from timestamp NULL,
  content_item_valid_to timestamp NULL,
  content_item_state VARCHAR2(16) NULL,
  content_item_version number(10) NOT NULL,
  PRIMARY KEY(content_item_tk)
); 
CREATE INDEX IDX_DIM_CONTENT_ITEM_GUID_FROM ON pentaho_operations_mart.DIM_CONTENT_ITEM (content_item_guid, content_item_valid_from);

CREATE TABLE pentaho_operations_mart.FACT_SESSION (
  start_date_tk NUMBER(10) NOT NULL,
  start_time_tk NUMBER(10) NOT NULL,
  end_date_tk NUMBER(10) NOT NULL,
  end_time_tk NUMBER(10) NOT NULL,
  session_tk NUMBER(20) NOT NULL,
  state_tk NUMBER(20) NOT NULL,
  duration NUMBER(19,3) NOT NULL
);
CREATE INDEX pentaho_operations_mart.IDX_FACT_SESSION_START_DATE_TK 
ON pentaho_operations_mart.FACT_SESSION(start_date_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_SESSION_START_TIME_TK
ON pentaho_operations_mart.FACT_SESSION(start_time_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_SESSION_END_DATE_TK 
ON pentaho_operations_mart.FACT_SESSION(end_date_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_SESSION_END_TIME_TK
ON pentaho_operations_mart.FACT_SESSION(end_time_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_SESSION_SESSION_TK 
ON pentaho_operations_mart.FACT_SESSION(session_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_SESSION_STATE_TK
ON pentaho_operations_mart.FACT_SESSION(state_tk);

CREATE TABLE pentaho_operations_mart.FACT_INSTANCE (
  start_date_tk NUMBER(10) NOT NULL,
  start_time_tk NUMBER(10) NOT NULL,
  end_date_tk NUMBER(10) NOT NULL,
  end_time_tk NUMBER(10) NOT NULL,
  session_tk NUMBER(20) NOT NULL,
  instance_tk NUMBER(20) NOT NULL,
  state_tk NUMBER(20) NOT NULL,
  duration NUMBER(19,3) NOT NULL
);
CREATE INDEX pentaho_operations_mart.IDX_FACT_INSTANCE_ST_DATE_TK 
ON pentaho_operations_mart.FACT_INSTANCE(start_date_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_INSTANCE_ST_TIME_TK 
ON pentaho_operations_mart.FACT_INSTANCE(start_time_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_INSTANCE_END_DATE_TK 
ON pentaho_operations_mart.FACT_INSTANCE(end_date_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_INSTANCE_END_TIME_TK 
ON pentaho_operations_mart.FACT_INSTANCE(end_time_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_INSTANCE_SESSION_TK
ON pentaho_operations_mart.FACT_INSTANCE(session_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_INSTANCE_INSTANCE_TK 
ON pentaho_operations_mart.FACT_INSTANCE(instance_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_INSTANCE_STATE_TK 
ON pentaho_operations_mart.FACT_INSTANCE(state_tk);

CREATE TABLE pentaho_operations_mart.FACT_COMPONENT (
  start_date_tk NUMBER(10) NOT NULL,
  start_time_tk NUMBER(10) NOT NULL,
  end_date_tk NUMBER(10) NOT NULL,
  end_time_tk NUMBER(10) NOT NULL,
  session_tk NUMBER(20) NOT NULL,
  instance_tk NUMBER(20) NOT NULL,
  state_tk NUMBER(20) NOT NULL,
  component_tk NUMBER(20) NOT NULL,
  duration NUMBER(19,3) NOT NULL
);
CREATE INDEX pentaho_operations_mart.IDX_FACT_COMP_START_DATE_TK 
ON pentaho_operations_mart.FACT_COMPONENT(start_date_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_COMP_START_TIME_TK 
ON pentaho_operations_mart.FACT_COMPONENT(start_time_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_COMP_END_DATE_TK 
ON pentaho_operations_mart.FACT_COMPONENT(end_date_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_COMP_END_TIME_TK 
ON pentaho_operations_mart.FACT_COMPONENT(end_time_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_COMP_SESSION_TK
ON pentaho_operations_mart.FACT_COMPONENT(session_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_COMP_INSTANCE_TK
ON pentaho_operations_mart.FACT_COMPONENT(instance_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_COMP_COMP_TK
ON pentaho_operations_mart.FACT_COMPONENT(component_tk);

CREATE INDEX pentaho_operations_mart.IDX_FACT_COMP_STATE_TK
ON pentaho_operations_mart.FACT_COMPONENT(state_tk);

CREATE TABLE pentaho_operations_mart.PRO_AUDIT_STAGING (
   job_id varchar2(200),
   inst_id varchar2(200),
   obj_id varchar2(1024),
   obj_type varchar2(200),
   actor varchar2(200),
   message_type varchar2(200),
   message_name varchar2(200),
   message_text_value varchar2(1024),
   message_num_value NUMBER(19),
   duration NUMBER(19, 3),
   audit_time timestamp
);
CREATE INDEX pentaho_operations_mart.IDX_PRO_AUDIT_STAGING_MSG_TYPE 
ON pentaho_operations_mart.PRO_AUDIT_STAGING(message_type);

CREATE TABLE pentaho_operations_mart.PRO_AUDIT_TRACKER (
   audit_time timestamp
);
CREATE INDEX pentaho_operations_mart.IDX_PRO_AUDIT_TRACKER_TIME 
ON pentaho_operations_mart.PRO_AUDIT_STAGING(audit_time);
INSERT INTO pentaho_operations_mart.PRO_AUDIT_TRACKER values (timestamp '1970-01-01 00:00:01');

