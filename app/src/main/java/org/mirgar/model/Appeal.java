package org.mirgar.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;

@Table(name = "appeals")
public class Appeal extends Model {

    @Column(name = "GlobalId")
    public long globalId;
    @Column(name = "Title")
    public String title;
    @Column(name = "Desc")
    public String desc;
    @Column(name = "Category")
    public Category category;
    @Column(name = "IsDraft")
    public boolean isDraft;

    public List<Photo> Photos() {
        return getMany(Photo.class, "");
    }
}
