select t1.* 
from tables1 t1
left join tables2 t2 on t1.table_name = t2.table_name
where t2.table_name is null
order by t1.table_name;

select t1.* 
from tables1 t1
left join tables2 t2 on 
	t1.table_name = t2.table_name and 
	t1.temporary = t2.temporary and 
	t1.secondary = t2.secondary and 
	t1.nested = t2.nested and
	t1.compression = t2.compression and
	t1.external = t2.external
where t2.table_name is null
order by t1.table_name;

select t1.* 
from tab_columns1 t1
left join tab_columns2 t2 on 
	t1.table_name = t2.table_name and 
	t1.column_name = t2.column_name and
	t1.data_type = t2.data_type and
	ifnull(t1.data_length, '<NULL>') = ifnull(t2.data_length, '<NULL>') and
	ifnull(t1.data_precision, '<NULL>') = ifnull(t2.data_precision, '<NULL>') and
	ifnull(t1.data_scale, '<NULL>') = ifnull(t2.data_scale, '<NULL>') and
	t1.nullable = t2.nullable
where t2.table_name is null
order by t1.table_name, t1.column_name;

select t1.*
from (
	select tc.*, t.temporary, t.secondary, t.nested, t.compression, t.default_collation, t.external
	from tables1 t
	join tab_columns1 tc on t.table_name=tc.table_name
	) t1
left join (
	select tc.*, t.temporary, t.secondary, t.nested, t.compression, t.default_collation, t.external
	from tables1 t
	join tab_columns1 tc on t.table_name=tc.table_name
	) t2 on 
		t1.table_name=t2.table_name and
		t1.column_name=t2.column_name and
		t1.data_type = t2.data_type and
		ifnull(t1.data_length, '<NULL>') = ifnull(t2.data_length, '<NULL>') and
		ifnull(t1.data_precision, '<NULL>') = ifnull(t2.data_precision, '<NULL>') and
		ifnull(t1.data_scale, '<NULL>') = ifnull(t2.data_scale, '<NULL>') and
		t1.nullable = t2.nullable and
		t1.column_id = t2.column_id and
		ifnull(t1.collation, '<NULL>')=ifnull(t2.collation, '<NULL>') and
		t1.temporary = t2.temporary and
		t1.secondary = t2.secondary and
		t1.nested = t2.nested and
		t1.compression = t2.compression and
		ifnull(t1.default_collation, '<NULL>')=ifnull(t2.default_collation, '<NULL>') and
		t1.external = t2.external
where t2.table_name is null
order by t1.table_name

select t1.* 
from indexes1 t1
left join indexes2 t2 on 
	t1.index_name = t2.index_name and
	t1.index_type = t2.index_type and
	t1.table_name = t2.table_name and
	t1.uniqueness = t2.uniqueness
where t2.index_name is null
order by t1.index_name;

select t1.*
from (
	select i.index_type, i.uniqueness, i.compression, ic.*
	from indexes1 i
	join ind_columns1 ic on i.index_name=ic.index_name
	) t1
left join (
	select i.index_type, i.uniqueness, i.compression, ic.*
	from indexes2 i
	join ind_columns2 ic on i.index_name=ic.index_name
	) t2 on 
--		t1.index_name = t2.index_name and
		t1.index_type = t2.index_type and
		t1.table_name = t2.table_name and
		t1.column_name = t2.column_name and
		t1.column_position = t2.column_position and
		t1.descend = t2.descend and
		t1.uniqueness = t2.uniqueness
where t2.index_name is null
order by t1.index_name;

select t1.*
from (
	select cc.*, c.constraint_type, c.search_condition, c.r_owner, c.r_constraint_name, c.delete_rule, c.status, rc.table_name r_table_name, rc.column_name r_column_name
	from CONSTRAINTS1 c
	join CONS_COLUMNS1 cc on c.owner=cc.owner and c.constraint_name=cc.constraint_name
	left join CONS_COLUMNS1 rc on c.r_owner=rc.owner and c.r_constraint_name=rc.constraint_name and ifnull(cc.position, '<NULL>')=ifnull(rc.position, '<NULL>')
) t1
left join (
	select cc.*, c.constraint_type, c.search_condition, c.r_owner, c.r_constraint_name, c.delete_rule, c.status, rc.table_name r_table_name, rc.column_name r_column_name
	from CONSTRAINTS2 c
	join CONS_COLUMNS2 cc on c.owner=cc.owner and c.constraint_name=cc.constraint_name
	left join CONS_COLUMNS2 rc on c.r_owner=rc.owner and c.r_constraint_name=rc.constraint_name and ifnull(cc.position, '<NULL>')=ifnull(rc.position, '<NULL>')
) t2 on 
--	t1.constraint_name = t2.constraint_name and
	t1.table_name=t2.table_name and
	t1.constraint_type = t2.constraint_type and
	t1.column_name=t2.column_name and
	ifnull(t1.search_condition, '<NULL>')=ifnull(t2.search_condition, '<NULL>') and
	ifnull(t1.delete_rule, '<NULL>')=ifnull(t2.delete_rule, '<NULL>') and
	ifnull(t1.status, '<NULL>')=ifnull(t2.status, '<NULL>') and
	ifnull(t1.position, '<NULL>')=ifnull(t2.position, '<NULL>') and
--	ifnull(t1.r_constraint_name, '<NULL>') = ifnull(t2.r_constraint_name, '<NULL>') and
	ifnull(t1.r_table_name, '<NULL>')=ifnull(t2.r_table_name, '<NULL>') and
	ifnull(t1.r_column_name, '<NULL>')=ifnull(t2.r_column_name, '<NULL>')
where t2.constraint_name is null
order by t1.constraint_name;

select t1.* 
from views1 t1
left join views2 t2 on 
	t1.view_name = t2.view_name and 
	t1.text_length = t2.text_length and 
	t1.text_vc = t2.text_vc
where t2.view_name is null
order by t1.view_name;

select t1.* 
from mviews1 t1
left join mviews2 t2 on 
	t1.mview_name = t2.mview_name and 
	t1.query_len = t2.query_len and 
	t1.query = t2.query and 
	t1.updatable=t2.updatable and 
	t1.refresh_mode=t2.refresh_mode and
	ifnull(t1.default_collation, '<NULL>') = ifnull(t2.default_collation, '<NULL>')
where t2.mview_name is null
order by t1.mview_name;

select t1.* 
from TRIGGERS1 t1
left join TRIGGERS2 t2 on 
	t1.trigger_name = t2.trigger_name and 
	t1.trigger_type = t2.trigger_type and 
	t1.triggering_event = t2.triggering_event and 
	t1.table_name = t2.table_name and 
	ifnull(t1.column_name, '<NULL>') = ifnull(t2.column_name, '<NULL>') and
	ifnull(t1.when_clause, '<NULL>') = ifnull(t2.when_clause, '<NULL>') and
	t1.status = t2.status and 
	t1.crossedition = t2.crossedition and 
	t1.before_statement = t2.before_statement and
	t1.after_row = t2.after_row and
	t1.before_row = t2.before_row and
	t1.after_statement = t2.after_statement and
	t1.instead_of_row = t2.instead_of_row and
	t1.fire_once = t2.fire_once and
	t1.trigger_body = t2.trigger_body
where t2.trigger_name is null
order by t1.trigger_name;
