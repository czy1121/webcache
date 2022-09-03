package me.reezy.cosmo.webcache

import android.os.Parcel
import android.os.Parcelable

data class BundleItem(
//    val id: String,
    val baseUrl: String,
    val uri: String,
    val hash: String,
    val local: Boolean = false): Parcelable {
    constructor(parcel: Parcel) : this(
//        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeString(id)
        parcel.writeString(baseUrl)
        parcel.writeString(uri)
        parcel.writeString(hash)
        parcel.writeByte(if (local) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BundleItem> {
        override fun createFromParcel(parcel: Parcel): BundleItem {
            return BundleItem(parcel)
        }

        override fun newArray(size: Int): Array<BundleItem?> {
            return arrayOfNulls(size)
        }
    }

}