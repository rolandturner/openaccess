
/*
 * Copyright (c) 1998 - 2005 Versant Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Versant Corporation - initial API and implementation
 */
package com.versant.core.ejb.junit.ejbtest1.model;

import com.versant.core.ejb.junit.ejbtest1.model.QueryEmployee;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Collection;
import static javax.persistence.GeneratorType.*;
import static javax.persistence.InheritanceType.*;
import static javax.persistence.FetchType.*;

@Entity
@Table(name = "QUERY_TASK")
@Inheritance(strategy = JOINED, discriminatorValue = "P")
@DiscriminatorColumn(name = "PROJ_TYPE")
@NamedQuery(
        name = "findTaskByName",
        queryString = "SELECT OBJECT(task) FROM Task task WHERE task.name = :name"
)
public class Task implements Serializable {
    private Integer id;
    private int version;
    private String name;
    private String description;
    private QueryEmployee teamLeader;
    private Collection<QueryEmployee> teamMembers;

    public Task() {
    }

    @Id(generate = TABLE, generator = "TASK_TABLE_GENERATOR")
    @TableGenerator(name = "TASK_TABLE_GENERATOR", table = @Table(name = "EMPLOYEE_GENERATOR_TABLE"), pkColumnValue = "PROJECT_SEQ")
    @Column(name = "PROJ_ID")
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION")
    public int getVersion() {
        return version;
    }

    protected void setVersion(int version) {
        this.version = version;
    }

    @Column(name = "PROJ_NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "DESCRIP")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "LEADER_ID")
    public QueryEmployee getTeamLeader() {
        return teamLeader;
    }

    public void setTeamLeader(QueryEmployee teamLeader) {
        this.teamLeader = teamLeader;
    }

    @ManyToMany(mappedBy = "projects")
    public Collection<QueryEmployee> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(Collection<QueryEmployee> employees) {
        this.teamMembers = employees;
    }

    public void addTeamMember(QueryEmployee queryEmployee) {
        getTeamMembers().add(queryEmployee);
    }

    public void removeTeamMember(QueryEmployee queryEmployee) {
        getTeamMembers().remove(queryEmployee);
    }

    public String displayString() {
        StringBuffer sbuff = new StringBuffer();
        sbuff.append("Task ").append(getId()).append(": ").append(getName()).append(", ").append(getDescription());

        return sbuff.toString();
    }
}

