--------- Drop report tables ---------

drop table if exists compare;
drop table if exists compare_msg;

--------- Create report tables ---------

create table compare_msg(
	err_code number(2) primary key,
	message varchar(160));

create table compare(
	err_code number(2) references compare_msg,
	obj_name varchar(130));

insert into compare_msg values( 1, 'Table found in sourceDB does not exist in targetDB');
insert into compare_msg values( 2, 'Table found in targetDB does not exist in sourceDB');
insert into compare_msg values( 5, 'Table column in sourceDB does not exist in or match (type, length, precision, nullable) to a column in targetDB');
insert into compare_msg values( 6, 'Table column in targetDB does not exist in sourceDB');
insert into compare_msg values( 7, 'Primary key in sourceDB could not be matched to a primary key in targetDB (names can differ)');
insert into compare_msg values( 8, 'Primary key in targetDB could not be matched to a primary key in sourceDB (names can differ)');
insert into compare_msg values( 9, 'Foreign key in sourceDB could not be matched to a foreign key in targetDB (names can differ)');
insert into compare_msg values(10, 'Foreign key in targetDB could not be matched to a foreign key in sourceDB (names can differ)');
insert into compare_msg values(13, 'Index in sourceDB could not be matched to an index in targetDB (names can differ)');
insert into compare_msg values(14, 'Index in targetDB could not be matched to an index in sourceDB (names can differ)');
insert into compare_msg values(15, 'Stored procedure in sourceDB could not be matched to a procedure in targetDB');
insert into compare_msg values(16, 'Stored procedure in targetDB could not be matched to a procedure in sourceDB');
insert into compare_msg values(17, 'Stored function in sourceDB could not be matched to a function in targetDB');
insert into compare_msg values(18, 'Stored function in targetDB could not be matched to a function in sourceDB');

--------- Compare tables ---------
insert into compare(err_code, obj_name)
select 1, L.table_name
from tables1 L
left join tables2 R on L.table_name = R.table_name and L.table_type = R.table_type
where R.table_name is null
group by L.table_name;

insert into compare(err_code, obj_name)
select 2, L.table_name
from tables2 L
left join tables1 R on L.table_name = R.table_name and L.table_type = R.table_type
where R.table_name is null
group by L.table_name;

--------- Compare table columns ---------

insert into compare(err_code, obj_name)
select 5, L.table_name || '.' || L.column_name
from tab_columns1 L
left join tab_columns2 R on 
	L.table_name = R.table_name and 
	L.column_name = R.column_name and
	L.data_type = R.data_type and
	ifnull(L.column_size, 0) = ifnull(R.column_size, 0) and
	ifnull(L.buffer_length, 0) = ifnull(R.buffer_length, 0) and
	ifnull(L.decimal_digits, 0) = ifnull(R.decimal_digits, 0) and
	ifnull(L.num_prec_radix, 0) = ifnull(R.num_prec_radix, 0) and
	ifnull(L.nullable, -1) = ifnull(R.nullable, -1)
left join compare c on c.obj_name = L.table_name and c.err_code in (1,2)
where R.table_name is null and c.err_code is null
group by L.table_name;

insert into compare(err_code, obj_name)
select 6, L.table_name || '.' || L.column_name
from tab_columns2 L
left join tab_columns1 R on 
	L.table_name = R.table_name and 
	L.column_name = R.column_name and
	L.data_type = R.data_type and
	ifnull(L.column_size, 0) = ifnull(R.column_size, 0) and
	ifnull(L.buffer_length, 0) = ifnull(R.buffer_length, 0) and
	ifnull(L.decimal_digits, 0) = ifnull(R.decimal_digits, 0) and
	ifnull(L.num_prec_radix, 0) = ifnull(R.num_prec_radix, 0) and
	ifnull(L.nullable, -1) = ifnull(R.nullable, -1)
left join compare c on c.obj_name = L.table_name and c.err_code in (1,2)
where R.table_name is null and c.err_code is null
group by L.table_name;

--------- Remove duplicated columns in report ---------
delete from compare where err_code = 6 and obj_name in (select obj_name from compare where err_code = 5);

--------- Compare constraints ---------
-- TODO:

--------- Compare primary keys ---------

insert into compare(err_code, obj_name)
select 7, L.table_name || '.' || ifnull(L.pk_name, '')
from primary_keys1 L
left join primary_keys2 R on 
	L.table_name = R.table_name and 
	L.column_name = R.column_name and
	L.key_seq = R.key_seq
left join compare c on c.obj_name = L.table_name and c.err_code in (1,2)
where R.table_name is null and c.err_code is null
group by L.table_name || '.' || ifnull(L.pk_name, '');

insert into compare(err_code, obj_name)
select 8, L.table_name || '.' || ifnull(L.pk_name, '')
from primary_keys2 L
left join primary_keys1 R on 
	L.table_name = R.table_name and 
	L.column_name = R.column_name and
	L.key_seq = R.key_seq
left join compare c on c.obj_name = L.table_name and c.err_code in (1,2)
where R.table_name is null and c.err_code is null
group by L.table_name || '.' || ifnull(L.pk_name, '');

--------- Compare primary keys ---------

insert into compare(err_code, obj_name)
select 9, L.fktable_name || '.' || ifnull(L.fk_name, '')
from imported_keys1 L
left join imported_keys2 R on 
	L.pktable_name = R.pktable_name and 
	L.pkcolumn_name = R.pkcolumn_name and
	L.fktable_name = R.fktable_name and 
	L.fkcolumn_name = R.fkcolumn_name and
	L.key_seq = R.key_seq and 
	ifnull(L.update_rule, '') = ifnull(R.update_rule, '') and
	ifnull(L.delete_rule, -1) = ifnull(R.delete_rule, -1)
left join compare c on c.obj_name = L.fktable_name and c.err_code in (1,2)
where R.fktable_name is null and c.err_code is null
group by L.fktable_name || '.' || ifnull(L.fk_name, '');

insert into compare(err_code, obj_name)
select 10, L.fktable_name || '.' || ifnull(L.fk_name, '')
from imported_keys2 L
left join imported_keys1 R on 
	L.pktable_name = R.pktable_name and 
	L.pkcolumn_name = R.pkcolumn_name and
	L.fktable_name = R.fktable_name and 
	L.fkcolumn_name = R.fkcolumn_name and
	L.key_seq = R.key_seq and 
	ifnull(L.update_rule, '') = ifnull(R.update_rule, '') and
	ifnull(L.delete_rule, -1) = ifnull(R.delete_rule, -1)
left join compare c on c.obj_name = L.fktable_name and c.err_code in (1,2)
where R.fktable_name is null and c.err_code is null
group by L.fktable_name || '.' || ifnull(L.fk_name, '');

--------- Compare indexes ---------

insert into compare(err_code, obj_name)
select 13, L.table_name || '.' || ifnull(L.index_name, '')
from indexes1 L
left join indexes2 R on 
	L.table_name = R.table_name and 
	ifnull(L.non_unique, -1) = ifnull(R.non_unique, -1) and
	ifnull(L.index_qualifier, '') = ifnull(R.index_qualifier, '') and
	ifnull(L.type, -1) = ifnull(R.type, -1) and
	ifnull(L.ordinal_position, -1) = ifnull(R.ordinal_position, -1) and
	ifnull(L.column_name, '') = ifnull(R.column_name, '')
left join compare c on c.obj_name = L.table_name and c.err_code in (1,2)
where R.table_name is null and c.err_code is null
group by L.table_name || '.' || ifnull(L.index_name, '');

insert into compare(err_code, obj_name)
select 14, L.table_name || '.' || ifnull(L.index_name, '')
from indexes2 L
left join indexes1 R on 
	L.table_name = R.table_name and 
	ifnull(L.non_unique, -1) = ifnull(R.non_unique, -1) and
	ifnull(L.index_qualifier, '') = ifnull(R.index_qualifier, '') and
	ifnull(L.type, -1) = ifnull(R.type, -1) and
	ifnull(L.ordinal_position, -1) = ifnull(R.ordinal_position, -1) and
	ifnull(L.column_name, '') = ifnull(R.column_name, '')
left join compare c on c.obj_name = L.table_name and c.err_code in (1,2)
where R.table_name is null and c.err_code is null
group by L.table_name || '.' || ifnull(L.index_name, '');

--------- Compare stored procedures ---------

insert into compare(err_code, obj_name)
select 15, L.procedure_name
from (
	select p.procedure_cat, p.procedure_type, pc.*
	from procedures1 p
	join proc_columns1 pc on p.procedure_name=pc.procedure_name
	) L
left join (
	select p.procedure_cat, p.procedure_type, pc.*
	from procedures2 p
	join proc_columns2 pc on p.procedure_name=pc.procedure_name
	) R on 
	ifnull(L.procedure_cat, '') = ifnull(R.procedure_cat, '') and
	L.procedure_name = R.procedure_name and 
	L.procedure_type = R.procedure_type and
	ifnull(L.column_name, '') = ifnull(R.column_name, '') and 
	L.column_type = R.column_type and
	L.data_type = R.data_type and
	L.ordinal_position = R.ordinal_position
where R.procedure_name is null
group by L.procedure_name;

insert into compare(err_code, obj_name)
select 16, L.procedure_name
from (
	select p.procedure_cat, p.procedure_type, pc.*
	from procedures2 p
	join proc_columns2 pc on p.procedure_name=pc.procedure_name
	) L
left join (
	select p.procedure_cat, p.procedure_type, pc.*
	from procedures1 p
	join proc_columns1 pc on p.procedure_name=pc.procedure_name
	) R on 
	ifnull(L.procedure_cat, '') = ifnull(R.procedure_cat, '') and
	L.procedure_name = R.procedure_name and 
	L.procedure_type = R.procedure_type and
	ifnull(L.column_name, '') = ifnull(R.column_name, '') and 
	L.column_type = R.column_type and
	L.data_type = R.data_type and
	L.ordinal_position = R.ordinal_position
where R.procedure_name is null
group by L.procedure_name;

--------- Compare stored functions ---------

insert into compare(err_code, obj_name)
select 17, L.function_name
from (
	select f.function_cat, f.function_type, fc.*
	from functions1 f
	join func_columns1 fc on f.function_name = fc.function_name
	) L
left join (
	select f.function_cat, f.function_type, fc.*
	from functions2 f
	join func_columns2 fc on f.function_name = fc.function_name
	) R on 
	ifnull(L.function_cat, '') = ifnull(R.function_cat, '') and
	L.function_name = R.function_name and 
	L.function_type = R.function_type and
	ifnull(L.column_name, '') = ifnull(R.column_name, '') and 
	L.column_type = R.column_type and
	L.data_type = R.data_type and
	L.ordinal_position = R.ordinal_position
where R.function_name is null
group by L.function_name;

insert into compare(err_code, obj_name)
select 18, L.function_name
from (
	select f.function_cat, f.function_type, fc.*
	from functions2 f
	join func_columns2 fc on f.function_name = fc.function_name
	) L
left join (
	select f.function_cat, f.function_type, fc.*
	from functions1 f
	join func_columns1 fc on f.function_name = fc.function_name
	) R on 
	ifnull(L.function_cat, '') = ifnull(R.function_cat, '') and
	L.function_name = R.function_name and 
	L.function_type = R.function_type and
	ifnull(L.column_name, '') = ifnull(R.column_name, '') and 
	L.column_type = R.column_type and
	L.data_type = R.data_type and
	L.ordinal_position = R.ordinal_position
where R.function_name is null
group by L.function_name;

