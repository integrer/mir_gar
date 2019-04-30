package org.mirgar.model;

import android.location.Location;
import android.net.Uri;
import android.os.Parcel;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.mirgar.util.Files;
import org.mirgar.util.Logger;
import org.mirgar.util.exceptions.ContextUnreachableException;

import java.io.File;
import java.io.IOException;

@Table(name = "Photos")
public class Photo extends Model {

    ///////////////////////////////////////////////////////////////////////////
    // Link to appeal
    ///////////////////////////////////////////////////////////////////////////
    @Column(name = "Appeal")
    public Appeal appeal;


    ///////////////////////////////////////////////////////////////////////////
    // Location field
    ///////////////////////////////////////////////////////////////////////////


    @Column(name = "Location")
    private byte[] location;

    // <editor-fold desc="Location access methods">
    /**
     * Contains pass to file of photo on devise. Must contain string
     * representation of file URI, that disappear in data directory of this
     * application.
     */
    public Location getLocation() {
        Parcel parc = Parcel.obtain();
        parc.unmarshall(location, 0, location.length);
        parc.setDataPosition(0);
        Location loc = Location.CREATOR.createFromParcel(parc);
        parc.recycle();
        return loc;
    }
    //Todo: add checking for location is correct
    public void setLocation(Location loc) {
        Parcel parc = Parcel.obtain();
        loc.writeToParcel(parc, 0);
        location = parc.marshall();
        parc.recycle();
    }
    // </editor-fold>


    ///////////////////////////////////////////////////////////////////////////
    // Its file location
    ///////////////////////////////////////////////////////////////////////////

    @Column(name = "FilePath")
    private String filePath;

    // <editor-fold desc="Photo file access methods">
    public File getFile() {
        return new File(getUri().getPath());
    }

    public void setFile(File file) {
        try {
            if (Files.inDataDirectory(file))
                setUri(Files.fromFile(file));
            else
                Logger.e("Wrong file path. It most be in data directory.");
        } catch (IOException | ContextUnreachableException ex) {
            Logger.wtf(ex);
        }
    }

    /**
     * Get photo URI representation
     *
     * @return URI to photo file
     */
    public Uri getUri() {
        return Uri.parse(filePath);
    }

    public void setUri(Uri uri) {
        filePath = uri.toString();
    }
    // </editor-fold>
}
