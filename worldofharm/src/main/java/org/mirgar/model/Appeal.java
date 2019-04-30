package org.mirgar.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;

@Table(name = "Appeals")
public class Appeal extends Model {
    public Appeal() {
        isDraft = true;
    }


    /**
     * Identifier in global database.
     */
    @Column(name = "GlobalId")
    public Long globalId;

    /**
     * Its title.
     */
    @Column(name = "Title")
    public String title;

    /**
     * Its description
     */
    @Column(name = "Desc")
    public String desc;

    /**
     * Link to category. It must be a category of 2`nd level
     * (with non-null {@link Category#parent parent} property)
     */
    @Column(name = "Category")
    public Category category;

    /**
     * Determines type of Appeal is draft
     */
    @Column(name = "IsDraft")
    public boolean isDraft;
    @Column(name = "Address")
    public String address;

    /**
     * Returns photos, that attached to it
     *
     * @return Attached {@link Photo photos}
     */
    public List<Photo> photos() {
        return getMany(Photo.class, "Appeal");
    }
}
