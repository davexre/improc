--------- Drop report tables ---------

drop table if exists compare;
drop table if exists compare_msg;

--------- Create report tables ---------

create table compare_msg(
	err_code number(2) primary key,
	message varchar(160));

insert into compare_msg values( 1, 'Table found in sourceDB does not exist in targetDB');
insert into compare_msg values( 2, 'Table found in targetDB does not exist in sourceDB');
insert into compare_msg values( 3, 'Table properties (temporary, secondary, nested, compression, external) do not match');
insert into compare_msg values( 5, 'Table column in sourceDB does not exist in or match (type, length, precision, nullable) to a column in targetDB');
insert into compare_msg values( 6, 'Table column in targetDB does not exist in sourceDB');
insert into compare_msg values( 7, 'Column properties (column order, collation, default_collation, temporary, secondary, nested, external) in sourceDB do not match column properties in targetDB');
insert into compare_msg values( 9, 'Constraint in sourceDB could not be matched to a constraint in targetDB (names can differ)');
insert into compare_msg values(10, 'Constraint in targetDB could not be matched to a constraint in sourceDB (names can differ)');
insert into compare_msg values(11, 'Constraint name in sourceDB does not match the same constraint in targetDB');
insert into compare_msg values(13, 'Index in sourceDB could not be matched to an index in targetDB (names can differ)');
insert into compare_msg values(14, 'Index in targetDB could not be matched to an index in sourceDB (names can differ)');
insert into compare_msg values(15, 'Index name in sourceDB does not match the same index in targetDB');
insert into compare_msg values(17, 'View found in sourceDB does not exist in or match (sql) to a view targetDB');
insert into compare_msg values(18, 'View found in targetDB does not exist in sourceDB');
insert into compare_msg values(19, 'Materialized view found in sourceDB does not exist in or match (sql) to a materialized view targetDB');
insert into compare_msg values(20, 'Materialized view found in targetDB does not exist in sourceDB');
insert into compare_msg values(21, 'Trigger found in sourceDB does not exist in or match (sql) to a trigger targetDB');
insert into compare_msg values(22, 'Trigger found in targetDB does not exist in sourceDB');
insert into compare_msg values(23, 'The source code of (procedure, function, package, trigger) found in sourceDB does not exist in or match (sql) to one in targetDB');
insert into compare_msg values(24, 'The source code of (procedure, function, package, trigger) found in targetDB does not exist in sourceDB');

create table compare(
	err_code number(2) references compare_msg,
	obj_name varchar(130));

--------- Compare tables ---------

insert into compare(err_code, obj_name)
select 1, L.table_name
from tables1 L
left join tables2 R on L.table_name = R.table_name
where R.table_name is null
group by L.table_name;

insert into compare(err_code, obj_name)
select 2, L.table_name
from tables2 L
left join tables1 R on L.table_name = R.table_name
where R.table_name is null
group by L.table_name;

insert into compare(err_code, obj_name)
select 3, L.table_name
from tables1 L
left join tables2 R on 
	L.table_name = R.table_name and 
	L.temporary = R.temporary and 
	L.secondary = R.secondary and 
	L.nested = R.nested and
	L.compression = R.compression and
	L.external = R.external
left join compare c on c.obj_name = L.table_name and c.err_code = 1
where R.table_name is null and c.err_code is null
group by L.table_name;

--------- Compare table columns ---------

insert into compare(err_code, obj_name)
select 5, L.table_name || '.' || L.column_name c
from (
	select tc.*, t.temporary, t.secondary, t.nested, t.compression, t.default_collation, t.external
	from tables1 t
	join tab_columns1 tc on t.table_name=tc.table_name
	) L
left join (
	select tc.*, t.temporary, t.secondary, t.nested, t.compression, t.default_collation, t.external
	from tables2 t
	join tab_columns2 tc on t.table_name=tc.table_name
	) R on 
		L.table_name=R.table_name and
		L.column_name=R.column_name and
		L.data_type = R.data_type and
		ifnull(L.data_length, '<NULL>') = ifnull(R.data_length, '<NULL>') and
		ifnull(L.data_precision, '<NULL>') = ifnull(R.data_precision, '<NULL>') and
		ifnull(L.data_scale, '<NULL>') = ifnull(R.data_scale, '<NULL>') and
		L.nullable = R.nullable
left join compare c on c.obj_name = L.table_name and c.err_code in (1,2)
where R.table_name is null and c.err_code is null
group by L.table_name, L.column_name;

insert into compare(err_code, obj_name)
select 6, L.table_name || '.' || L.column_name c
from (
	select tc.*, t.temporary, t.secondary, t.nested, t.compression, t.default_collation, t.external
	from tables2 t
	join tab_columns2 tc on t.table_name=tc.table_name
	) L
left join (
	select tc.*, t.temporary, t.secondary, t.nested, t.compression, t.default_collation, t.external
	from tables1 t
	join tab_columns1 tc on t.table_name=tc.table_name
	) R on 
		L.table_name=R.table_name and
		L.column_name=R.column_name and
		L.data_type = R.data_type and
		ifnull(L.data_length, '<NULL>') = ifnull(R.data_length, '<NULL>') and
		ifnull(L.data_precision, '<NULL>') = ifnull(R.data_precision, '<NULL>') and
		ifnull(L.data_scale, '<NULL>') = ifnull(R.data_scale, '<NULL>') and
		L.nullable = R.nullable
left join compare c on c.obj_name = L.table_name and c.err_code in (1,2)
where R.table_name is null and c.err_code is null
group by L.table_name, L.column_name;

insert into compare(err_code, obj_name)
select 7, L.table_name || '.' || L.column_name c
from (
	select tc.*, t.temporary, t.secondary, t.nested, t.compression, t.default_collation, t.external
	from tables1 t
	join tab_columns1 tc on t.table_name=tc.table_name
	) L
left join (
	select tc.*, t.temporary, t.secondary, t.nested, t.compression, t.default_collation, t.external
	from tables2 t
	join tab_columns2 tc on t.table_name=tc.table_name
	) R on 
		L.table_name=R.table_name and
		L.column_name=R.column_name and
		L.data_type = R.data_type and
		ifnull(L.data_length, '<NULL>') = ifnull(R.data_length, '<NULL>') and
		ifnull(L.data_precision, '<NULL>') = ifnull(R.data_precision, '<NULL>') and
		ifnull(L.data_scale, '<NULL>') = ifnull(R.data_scale, '<NULL>') and
		L.nullable = R.nullable and
		L.column_id = R.column_id and
		ifnull(L.collation, '<NULL>')=ifnull(R.collation, '<NULL>') and
		L.temporary = R.temporary and
		L.secondary = R.secondary and
		L.nested = R.nested and
		L.compression = R.compression and
		ifnull(L.default_collation, '<NULL>')=ifnull(R.default_collation, '<NULL>') and
		L.external = R.external
left join compare c on c.obj_name = L.table_name and c.err_code in (1,2)
where R.table_name is null and c.err_code is null
group by L.table_name, L.column_name;

--------- Remove duplicated columns in report ---------
delete from compare where err_code = 6 and obj_name in (select obj_name from compare where err_code = 5);
delete from compare where err_code = 7 and obj_name in (select obj_name from compare where err_code = 5);

--------- Compare constraints ---------

insert into compare(err_code, obj_name)
select 9, L.constraint_name
from (
	select cc.*, c.constraint_type, c.search_condition, c.r_owner, c.r_constraint_name, c.delete_rule, c.status, rc.table_name r_table_name, rc.column_name r_column_name
	from CONSTRAINTS1 c
	join CONS_COLUMNS1 cc on c.owner=cc.owner and c.constraint_name=cc.constraint_name
	left join CONS_COLUMNS1 rc on c.r_owner=rc.owner and c.r_constraint_name=rc.constraint_name and ifnull(cc.position, '<NULL>')=ifnull(rc.position, '<NULL>')
) L
left join (
	select cc.*, c.constraint_type, c.search_condition, c.r_owner, c.r_constraint_name, c.delete_rule, c.status, rc.table_name r_table_name, rc.column_name r_column_name
	from CONSTRAINTS2 c
	join CONS_COLUMNS2 cc on c.owner=cc.owner and c.constraint_name=cc.constraint_name
	left join CONS_COLUMNS2 rc on c.r_owner=rc.owner and c.r_constraint_name=rc.constraint_name and ifnull(cc.position, '<NULL>')=ifnull(rc.position, '<NULL>')
) R on 
	L.table_name=R.table_name and
	L.constraint_type = R.constraint_type and
	L.column_name=R.column_name and
	ifnull(L.search_condition, '<NULL>')=ifnull(R.search_condition, '<NULL>') and
	ifnull(L.delete_rule, '<NULL>')=ifnull(R.delete_rule, '<NULL>') and
	ifnull(L.status, '<NULL>')=ifnull(R.status, '<NULL>') and
	ifnull(L.position, '<NULL>')=ifnull(R.position, '<NULL>') and
	ifnull(L.r_table_name, '<NULL>')=ifnull(R.r_table_name, '<NULL>') and
	ifnull(L.r_column_name, '<NULL>')=ifnull(R.r_column_name, '<NULL>')
where R.constraint_name is null
group by L.constraint_name;

insert into compare(err_code, obj_name)
select 10, L.constraint_name
from (
	select cc.*, c.constraint_type, c.search_condition, c.r_owner, c.r_constraint_name, c.delete_rule, c.status, rc.table_name r_table_name, rc.column_name r_column_name
	from CONSTRAINTS2 c
	join CONS_COLUMNS2 cc on c.owner=cc.owner and c.constraint_name=cc.constraint_name
	left join CONS_COLUMNS2 rc on c.r_owner=rc.owner and c.r_constraint_name=rc.constraint_name and ifnull(cc.position, '<NULL>')=ifnull(rc.position, '<NULL>')
) L
left join (
	select cc.*, c.constraint_type, c.search_condition, c.r_owner, c.r_constraint_name, c.delete_rule, c.status, rc.table_name r_table_name, rc.column_name r_column_name
	from CONSTRAINTS1 c
	join CONS_COLUMNS1 cc on c.owner=cc.owner and c.constraint_name=cc.constraint_name
	left join CONS_COLUMNS1 rc on c.r_owner=rc.owner and c.r_constraint_name=rc.constraint_name and ifnull(cc.position, '<NULL>')=ifnull(rc.position, '<NULL>')
) R on 
	L.table_name=R.table_name and
	L.constraint_type = R.constraint_type and
	L.column_name=R.column_name and
	ifnull(L.search_condition, '<NULL>')=ifnull(R.search_condition, '<NULL>') and
	ifnull(L.delete_rule, '<NULL>')=ifnull(R.delete_rule, '<NULL>') and
	ifnull(L.status, '<NULL>')=ifnull(R.status, '<NULL>') and
	ifnull(L.position, '<NULL>')=ifnull(R.position, '<NULL>') and
	ifnull(L.r_table_name, '<NULL>')=ifnull(R.r_table_name, '<NULL>') and
	ifnull(L.r_column_name, '<NULL>')=ifnull(R.r_column_name, '<NULL>')
where R.constraint_name is null
group by L.constraint_name;

insert into compare(err_code, obj_name)
select 11, L.constraint_name
from (
	select cc.*, c.constraint_type, c.search_condition, c.r_owner, c.r_constraint_name, c.delete_rule, c.status, rc.table_name r_table_name, rc.column_name r_column_name
	from CONSTRAINTS1 c
	join CONS_COLUMNS1 cc on c.owner=cc.owner and c.constraint_name=cc.constraint_name
	left join CONS_COLUMNS1 rc on c.r_owner=rc.owner and c.r_constraint_name=rc.constraint_name and ifnull(cc.position, '<NULL>')=ifnull(rc.position, '<NULL>')
) L
left join (
	select cc.*, c.constraint_type, c.search_condition, c.r_owner, c.r_constraint_name, c.delete_rule, c.status, rc.table_name r_table_name, rc.column_name r_column_name
	from CONSTRAINTS2 c
	join CONS_COLUMNS2 cc on c.owner=cc.owner and c.constraint_name=cc.constraint_name
	left join CONS_COLUMNS2 rc on c.r_owner=rc.owner and c.r_constraint_name=rc.constraint_name and ifnull(cc.position, '<NULL>')=ifnull(rc.position, '<NULL>')
) R on 
	L.constraint_name = R.constraint_name and
	L.table_name=R.table_name and
	L.constraint_type = R.constraint_type and
	L.column_name=R.column_name and
	ifnull(L.search_condition, '<NULL>')=ifnull(R.search_condition, '<NULL>') and
	ifnull(L.delete_rule, '<NULL>')=ifnull(R.delete_rule, '<NULL>') and
	ifnull(L.status, '<NULL>')=ifnull(R.status, '<NULL>') and
	ifnull(L.position, '<NULL>')=ifnull(R.position, '<NULL>') and
	ifnull(L.r_constraint_name, '<NULL>') = ifnull(R.r_constraint_name, '<NULL>') and
	ifnull(L.r_table_name, '<NULL>')=ifnull(R.r_table_name, '<NULL>') and
	ifnull(L.r_column_name, '<NULL>')=ifnull(R.r_column_name, '<NULL>')
left join compare c on c.obj_name = L.constraint_name and c.err_code = 9
where R.constraint_name is null and c.err_code is null
group by L.constraint_name;

--------- Compare indexes ---------

insert into compare(err_code, obj_name)
select 13, L.index_name
from (
	select i.index_type, i.uniqueness, i.compression, ic.*
	from indexes1 i
	join ind_columns1 ic on i.index_name=ic.index_name
	) L
left join (
	select i.index_type, i.uniqueness, i.compression, ic.*
	from indexes2 i
	join ind_columns2 ic on i.index_name=ic.index_name
	) R on 
		L.index_type = R.index_type and
		L.table_name = R.table_name and
		L.column_name = R.column_name and
		L.column_position = R.column_position and
		L.descend = R.descend and
		L.uniqueness = R.uniqueness
where R.index_name is null
group by L.index_name;

insert into compare(err_code, obj_name)
select 14, L.index_name
from (
	select i.index_type, i.uniqueness, i.compression, ic.*
	from indexes2 i
	join ind_columns2 ic on i.index_name=ic.index_name
	) L
left join (
	select i.index_type, i.uniqueness, i.compression, ic.*
	from indexes1 i
	join ind_columns1 ic on i.index_name=ic.index_name
	) R on 
		L.index_type = R.index_type and
		L.table_name = R.table_name and
		L.column_name = R.column_name and
		L.column_position = R.column_position and
		L.descend = R.descend and
		L.uniqueness = R.uniqueness
where R.index_name is null
group by L.index_name;

insert into compare(err_code, obj_name)
select 15, L.index_name
from (
	select i.index_type, i.uniqueness, i.compression, ic.*
	from indexes1 i
	join ind_columns1 ic on i.index_name=ic.index_name
	) L
left join (
	select i.index_type, i.uniqueness, i.compression, ic.*
	from indexes2 i
	join ind_columns2 ic on i.index_name=ic.index_name
	) R on 
		L.index_name = R.index_name and
		L.index_type = R.index_type and
		L.table_name = R.table_name and
		L.column_name = R.column_name and
		L.column_position = R.column_position and
		L.descend = R.descend and
		L.uniqueness = R.uniqueness
left join compare c on c.obj_name = L.index_name and c.err_code = 13
where R.index_name is null and c.err_code is null
group by L.index_name;

--------- Compare views ---------

insert into compare(err_code, obj_name)
select 17, L.view_name
from views1 L
left join views2 R on 
	L.view_name = R.view_name and 
	L.text_length = R.text_length and 
	L.text_vc = R.text_vc
where R.view_name is null
group by L.view_name;

insert into compare(err_code, obj_name)
select 18, L.view_name
from views2 L
left join views1 R on 
	L.view_name = R.view_name
where R.view_name is null
group by L.view_name;

--------- Compare materialized views ---------

insert into compare(err_code, obj_name)
select 19, L.mview_name
from mviews1 L
left join mviews2 R on 
	L.mview_name = R.mview_name and 
	L.query_len = R.query_len and 
	L.query = R.query and 
	L.updatable=R.updatable and 
	L.refresh_mode=R.refresh_mode
--	and ifnull(L.default_collation, '<NULL>') = ifnull(R.default_collation, '<NULL>')
where R.mview_name is null
group by L.mview_name;

insert into compare(err_code, obj_name)
select 20, L.mview_name
from mviews2 L
left join mviews1 R on 
	L.mview_name = R.mview_name
where R.mview_name is null
group by L.mview_name;

--------- Compare triggers ---------

insert into compare(err_code, obj_name)
select 21, L.trigger_name
from triggers1 L
left join triggers2 R on 
	L.trigger_name = R.trigger_name and 
	L.trigger_type = R.trigger_type and 
	L.triggering_event = R.triggering_event and 
	L.table_name = R.table_name and 
	ifnull(L.column_name, '<NULL>') = ifnull(R.column_name, '<NULL>') and
	ifnull(L.when_clause, '<NULL>') = ifnull(R.when_clause, '<NULL>') and
	L.status = R.status and 
	L.crossedition = R.crossedition and 
	L.before_statement = R.before_statement and
	L.after_row = R.after_row and
	L.before_row = R.before_row and
	L.after_statement = R.after_statement and
	L.instead_of_row = R.instead_of_row and
	L.fire_once = R.fire_once and
	L.trigger_body = R.trigger_body
where R.trigger_name is null
group by L.trigger_name;

insert into compare(err_code, obj_name)
select 22, L.trigger_name
from triggers2 L
left join triggers1 R on 
	L.trigger_name = R.trigger_name
where R.trigger_name is null
group by L.trigger_name;

--------- Compare source of trigger, packages, etc. ---------

insert into compare(err_code, obj_name)
select 23, L.type || ' ' || L.name
from source1 L
left join source2 R on 
	L.name = R.name and 
	L.type = R.type and 
	L.line = R.line and
	L.text = R.text
where R.name is null
group by L.name;

insert into compare(err_code, obj_name)
select 24, L.type || ' ' || L.name
from source2 L
left join source1 R on 
	L.name = R.name and 
	L.type = R.type
where R.name is null
group by L.name;
