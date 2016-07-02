package com.sonelli.juicessh.pluginlibrary.parcelables;

import android.os.Parcel;
import android.os.Parcelable;

//import com.jcraft.jsch.ChannelSftp;


public class ParcelableLsEntry implements Parcelable{

    private ParcelableSftpATTRS attrs;
    private String filename;
    private String longname;
    private String toString;

/*    public ParcelableLsEntry(ChannelSftp.LsEntry lsEntry) {
        attrs = new ParcelableSftpATTRS(lsEntry.getAttrs());
        filename = lsEntry.getFilename();
        longname = lsEntry.getLongname();
        toString = lsEntry.toString();
    } */

    public ParcelableSftpATTRS getAttrs() {
        return attrs;
    }

    public String getFilename() {
        return filename;
    }

    public String getLongname() {
        return longname;
    }

    @Override
    public String toString() {
        return toString;
    }

    protected ParcelableLsEntry(Parcel in) {
        attrs = in.readParcelable(ParcelableSftpATTRS.class.getClassLoader());
        filename = in.readString();
        longname = in.readString();
        toString = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(attrs, flags);
        dest.writeString(filename);
        dest.writeString(longname);
        dest.writeString(toString);
    }

    @SuppressWarnings("unused")
    public static final Creator<ParcelableLsEntry> CREATOR = new Creator<ParcelableLsEntry>() {
        @Override
        public ParcelableLsEntry createFromParcel(Parcel in) {
            return new ParcelableLsEntry(in);
        }

        @Override
        public ParcelableLsEntry[] newArray(int size) {
            return new ParcelableLsEntry[size];
        }
    };
}
