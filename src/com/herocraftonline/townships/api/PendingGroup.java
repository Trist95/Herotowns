package com.herocraftonline.townships.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class PendingGroup{

    private static final long serialVersionUID = 842645579092256641L;
    public final String name;
    private String owner;
    private long creationDate;
    private Set<String> members = new HashSet<>();
    
    public PendingGroup(String name) {
        this.name = name;
        creationDate = System.currentTimeMillis();
    }

    public PendingGroup(String name, long time) {
        this.name = name;
        creationDate = time;
    }
    
    public PendingGroup(String name, Collection<? extends String> c) {
        members.addAll(c);
        this.name = name;
        creationDate = System.currentTimeMillis();
    }

    public PendingGroup(String name, Collection<? extends String> c, long time) {
        members.addAll(c);
        this.name = name;
        creationDate = time;
    }

    public Set<String> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public void addMember(String name) {
        members.add(name);
    }

    public void removeMember(String name) {
        members.remove(name);
    }

    public abstract String getGroupType();

    public void setOwner(String newOwner) {
        owner = newOwner;
    }

    public String getOwner() {
        return owner;
    }

    public void setCreationDate(long time) {
        creationDate = time;
    }

    public long getCreationDate() {
        return creationDate;
    }

    @Override
    public String toString() {
        return name;
    }
}
