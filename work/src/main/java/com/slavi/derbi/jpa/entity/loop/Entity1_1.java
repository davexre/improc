package com.slavi.derbi.jpa.entity.loop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.slavi.derbi.jpa.entity.NullStringConverter;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorValue("E1_1")
public class Entity1_1 extends Entity1 {
	String data1_1;

	@javax.persistence.JoinColumn
	@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, cascade = CascadeType.ALL)
	Entity1 parent;

	@javax.persistence.ManyToMany(fetch = javax.persistence.FetchType.LAZY, cascade = CascadeType.ALL)
	@javax.persistence.OrderColumn(name = "sys_key")
	@javax.persistence.JoinTable(name = "Entity1_1_entity2_t",joinColumns = {@javax.persistence.JoinColumn(name = "sys_oid")},inverseJoinColumns = {@javax.persistence.JoinColumn(name = "sys_val")})
	List<Entity2> entity2 = new ArrayList<>();

	@javax.persistence.Convert(attributeName="value", converter = NullStringConverter.class)   // TODO: Workaround to null values in map.values()
	@javax.persistence.ElementCollection(fetch = javax.persistence.FetchType.LAZY)
	@javax.persistence.MapKeyJoinColumn(name="sys_key")
	@javax.persistence.Column(name="sys_val")
	@javax.persistence.CollectionTable(name="Entity1_1_codes_t", joinColumns=@javax.persistence.JoinColumn(name="sys_oid"))
	Map<Entity2, String> codes;

	@javax.persistence.ManyToMany(fetch = javax.persistence.FetchType.LAZY, cascade = CascadeType.ALL)
	@javax.persistence.JoinTable(name = "Entity1_1_codes1_t",
		joinColumns = @javax.persistence.JoinColumn(name = "sys_oid"),
		inverseJoinColumns = @javax.persistence.JoinColumn(name = "sys_val"))
	@javax.persistence.MapKeyJoinColumn(name = "sys_key")
	Map<Entity1, Entity2> codes1;

	@javax.persistence.ManyToMany(fetch = javax.persistence.FetchType.LAZY, cascade = CascadeType.ALL)
	@javax.persistence.JoinTable(name = "Entity1_1_codes2_t",
		joinColumns = @javax.persistence.JoinColumn(name = "sys_oid"),
		inverseJoinColumns = @javax.persistence.JoinColumn(name = "sys_val"))
	Set<Entity2> codes2 = new HashSet<>();

	@javax.persistence.ElementCollection(fetch = javax.persistence.FetchType.LAZY)
	@javax.persistence.Column(name="sys_val")
	@javax.persistence.CollectionTable(name="Entity1_1_exclFBCodes_t", joinColumns=@javax.persistence.JoinColumn(name="sys_oid"))
	Set<String> exclFBCodes;

	@javax.persistence.ElementCollection(fetch = javax.persistence.FetchType.LAZY)
	@javax.persistence.MapKeyColumn(name="sys_key")
	@javax.persistence.Column(name="sys_val")
	@javax.persistence.CollectionTable(name="Entity1_1_lParams_t", joinColumns=@javax.persistence.JoinColumn(name="sys_oid"))
	Map<String, String> lParams;

	@javax.persistence.ElementCollection(fetch = javax.persistence.FetchType.LAZY)
	@javax.persistence.OrderColumn(name="sys_key")
	@javax.persistence.Column(name="sys_val")
	@javax.persistence.CollectionTable(name="Entity1_1_paramsStr_t", joinColumns=@javax.persistence.JoinColumn(name="sys_oid"))
	List<String> paramsStr = new ArrayList<>();
}
