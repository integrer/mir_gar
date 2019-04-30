package org.mirgar.model;

import android.support.annotation.IdRes;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;

import javax.annotation.Nullable;


@Table(name = "Category")
public class Category extends Model {
    @Column(name = "GlobalId")
    public long globalId;

    /**
     * Link to parent category
     */
    @Column(name = "Parent")
    public Category parent;
    @Column(name = "Name")
    public String name;
    @IdRes
    @Nullable
    @Column(name = "IconId")
    public Integer iconId;

    public List<Category> subCategories() {
        return getMany(Category.class, "Parent");
    }
}
