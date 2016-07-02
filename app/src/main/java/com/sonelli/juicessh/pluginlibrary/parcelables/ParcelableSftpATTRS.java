package com.sonelli.juicessh.pluginlibrary.parcelables;

import android.os.Parcel;
import android.os.Parcelable;

//import com.jcraft.jsch.SftpATTRS;


public final class ParcelableSftpATTRS implements Parcelable {

    private String getPermissionsString;
    private String getAtimeString;
    private String getMtimeString;
    private boolean isReg;
    private boolean isDir;
    private boolean isChr;
    private boolean isBlk;
    private boolean isFifo;
    private boolean isLink;
    private boolean isSock;
    private int getFlags;
    private long getSize;
    private int getUId;
    private int getGId;
    private int getPermissions;
    private int getATime;
    private int getMTime;
    private String[] getExtended;
    private String toString;

/*    public ParcelableSftpATTRS(SftpATTRS attrs) {
        getPermissionsString = attrs.getPermissionsString();
        getAtimeString = attrs.getAtimeString();
        getMtimeString = attrs.getMtimeString();
        isReg = attrs.isReg();
        isDir = attrs.isDir();
        isChr = attrs.isChr();
        isBlk = attrs.isBlk();
        isFifo = attrs.isFifo();
        isLink = attrs.isLink();
        isSock = attrs.isSock();
        getFlags = attrs.getFlags();
        getSize = attrs.getSize();
        getUId = attrs.getUId();
        getGId = attrs.getGId();
        getPermissions = attrs.getPermissions();
        getATime = attrs.getATime();
        getMTime = attrs.getMTime();
        getExtended = attrs.getExtended();
        toString = attrs.toString();
    } */

    public String getPermissionsString() {
        return getPermissionsString;
    }

    public String getAtimeString() {
        return getAtimeString;
    }

    public String getMtimeString() {
        return getMtimeString;
    }

    public boolean isReg() {
        return isReg;
    }

    public boolean isDir() {
        return isDir;
    }

    public boolean isChr() {
        return isChr;
    }

    public boolean isBlk() {
        return isBlk;
    }

    public boolean isFifo() {
        return isFifo;
    }

    public boolean isLink() {
        return isLink;
    }

    public boolean isSock() {
        return isSock;
    }

    public int getFlags() {
        return getFlags;
    }

    public long getSize() {
        return getSize;
    }

    public int getUId() {
        return getUId;
    }

    public int getGId() {
        return getGId;
    }

    public int getPermissions() {
        return getPermissions;
    }

    public int getATime() {
        return getATime;
    }

    public int getMTime() {
        return getMTime;
    }

    public String[] getExtended() {
        return getExtended;
    }

    @Override
    public String toString() {
        return toString;
    }

    protected ParcelableSftpATTRS(Parcel in) {
        getPermissionsString = in.readString();
        getAtimeString = in.readString();
        getMtimeString = in.readString();
        isReg = in.readByte() != 0x00;
        isDir = in.readByte() != 0x00;
        isChr = in.readByte() != 0x00;
        isBlk = in.readByte() != 0x00;
        isFifo = in.readByte() != 0x00;
        isLink = in.readByte() != 0x00;
        isSock = in.readByte() != 0x00;
        getFlags = in.readInt();
        getSize = in.readLong();
        getUId = in.readInt();
        getGId = in.readInt();
        getPermissions = in.readInt();
        getATime = in.readInt();
        getMTime = in.readInt();
        getExtended = in.createStringArray();
        toString = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getPermissionsString);
        dest.writeString(getAtimeString);
        dest.writeString(getMtimeString);
        dest.writeByte((byte) (isReg ? 0x01 : 0x00));
        dest.writeByte((byte) (isDir ? 0x01 : 0x00));
        dest.writeByte((byte) (isChr ? 0x01 : 0x00));
        dest.writeByte((byte) (isBlk ? 0x01 : 0x00));
        dest.writeByte((byte) (isFifo ? 0x01 : 0x00));
        dest.writeByte((byte) (isLink ? 0x01 : 0x00));
        dest.writeByte((byte) (isSock ? 0x01 : 0x00));
        dest.writeInt(getFlags);
        dest.writeLong(getSize);
        dest.writeInt(getUId);
        dest.writeInt(getGId);
        dest.writeInt(getPermissions);
        dest.writeInt(getATime);
        dest.writeInt(getMTime);
        dest.writeStringArray(getExtended);
        dest.writeString(toString);
    }

    @SuppressWarnings("unused")
    public static final Creator<ParcelableSftpATTRS> CREATOR = new Creator<ParcelableSftpATTRS>() {
        @Override
        public ParcelableSftpATTRS createFromParcel(Parcel in) {
            return new ParcelableSftpATTRS(in);
        }

        @Override
        public ParcelableSftpATTRS[] newArray(int size) {
            return new ParcelableSftpATTRS[size];
        }
    };
}
