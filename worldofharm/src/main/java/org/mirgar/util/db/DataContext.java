package org.mirgar.util.db;

import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import org.jetbrains.annotations.NotNull;
import org.mirgar.model.Appeal;
import org.mirgar.model.Category;

public final class DataContext {

    @NotNull
    public static From getAppeals() {
        return new Select().from(Appeal.class);
    }

    @NotNull
    public static Appeal getAppeal(long id) {
        return getAppeals().where("Id = ?", id).executeSingle();
    }

    @NotNull
    public static From getLocalAppeals() {
        return getAppeals().and("GlobalId ISNULL");
    }

    @NotNull
    public static From getDraftAppeals() {
        return getLocalAppeals().and("IsDraft");
    }


    @NotNull
    public static From getCategories() {
        return new Select().from(Category.class);
    }


    public static Category getCategory(long localId) {
        return getCategories().where("Id = ?", localId).executeSingle();
    }

    @NotNull
    public static From getRootCategories() {
        return getCategories().and("Parent ISNULL");
    }
}
