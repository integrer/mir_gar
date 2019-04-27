package org.mirgar.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;


@Table(name = "Category")
public class Category extends Model {
    @Column(name = "Id")
    public int id;
    @Column(name = "Parent")
    public Category parent;
    @Column(name = "Name")
    public String name;

    public List<Category> subCategories() {
        return getMany(Category.class, "Parent");
    }
}
