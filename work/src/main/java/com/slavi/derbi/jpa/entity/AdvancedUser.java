package com.slavi.derbi.jpa.entity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name="ADV_USR")
@PrimaryKeyJoinColumn(name = "aid")
public class AdvancedUser extends User {
	String desc;
}
