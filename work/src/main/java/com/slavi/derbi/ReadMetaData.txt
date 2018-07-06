create table compare(
	err_code number,
	obj_name varchar);

select L.* 
from tables1 L
left join tables2 R on L.table_name = R.table_name
where R.table_name is null
order by L.table_name;

select L.* 
from tables1 L
left join tables2 R on 
	L.table_name = R.table_name and 
	L.temporary = R.temporary and 
	L.secondary = R.secondary and 
	L.nested = R.nested and
	L.compression = R.compression and
	L.external = R.external
where R.table_name is null
order by L.table_name;

select L.* 
from tab_columns1 L
left join tab_columns2 R on 
	L.table_name = R.table_name and 
	L.column_name = R.column_name and
	L.data_type = R.data_type and
	ifnull(L.data_length, '<NULL>') = ifnull(R.data_length, '<NULL>') and
	ifnull(L.data_precision, '<NULL>') = ifnull(R.data_precision, '<NULL>') and
	ifnull(L.data_scale, '<NULL>') = ifnull(R.data_scale, '<NULL>') and
	L.nullable = R.nullable
where R.table_name is null
order by L.table_name, L.column_name;

select L.*
from (
	select tc.*, t.temporary, t.secondary, t.nested, t.compression, t.default_collation, t.external
	from tables1 t
	join tab_columns1 tc on t.table_name=tc.table_name
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
		L.nullable = R.nullable and
		L.column_id = R.column_id and
		ifnull(L.collation, '<NULL>')=ifnull(R.collation, '<NULL>') and
		L.temporary = R.temporary and
		L.secondary = R.secondary and
		L.nested = R.nested and
		L.compression = R.compression and
		ifnull(L.default_collation, '<NULL>')=ifnull(R.default_collation, '<NULL>') and
		L.external = R.external
where R.table_name is null
order by L.table_name

select L.* 
from indexes1 L
left join indexes2 R on 
	L.index_name = R.index_name and
	L.index_type = R.index_type and
	L.table_name = R.table_name and
	L.uniqueness = R.uniqueness
where R.index_name is null
order by L.index_name;

select L.*
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
--		L.index_name = R.index_name and
		L.index_type = R.index_type and
		L.table_name = R.table_name and
		L.column_name = R.column_name and
		L.column_position = R.column_position and
		L.descend = R.descend and
		L.uniqueness = R.uniqueness
where R.index_name is null
order by L.index_name;

select L.*
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
--	L.constraint_name = R.constraint_name and
	L.table_name=R.table_name and
	L.constraint_type = R.constraint_type and
	L.column_name=R.column_name and
	ifnull(L.search_condition, '<NULL>')=ifnull(R.search_condition, '<NULL>') and
	ifnull(L.delete_rule, '<NULL>')=ifnull(R.delete_rule, '<NULL>') and
	ifnull(L.status, '<NULL>')=ifnull(R.status, '<NULL>') and
	ifnull(L.position, '<NULL>')=ifnull(R.position, '<NULL>') and
--	ifnull(L.r_constraint_name, '<NULL>') = ifnull(R.r_constraint_name, '<NULL>') and
	ifnull(L.r_table_name, '<NULL>')=ifnull(R.r_table_name, '<NULL>') and
	ifnull(L.r_column_name, '<NULL>')=ifnull(R.r_column_name, '<NULL>')
where R.constraint_name is null
order by L.constraint_name;

select L.* 
from views1 L
left join views2 R on 
	L.view_name = R.view_name and 
	L.text_length = R.text_length and 
	L.text_vc = R.text_vc
where R.view_name is null
order by L.view_name;

select L.* 
from mviews1 L
left join mviews2 R on 
	L.mview_name = R.mview_name and 
	L.query_len = R.query_len and 
	L.query = R.query and 
	L.updatable=R.updatable and 
	L.refresh_mode=R.refresh_mode and
	ifnull(L.default_collation, '<NULL>') = ifnull(R.default_collation, '<NULL>')
where R.mview_name is null
order by L.mview_name;

select L.* 
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
order by L.trigger_name;
