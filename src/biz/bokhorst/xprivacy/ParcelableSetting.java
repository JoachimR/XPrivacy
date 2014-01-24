package biz.bokhorst.xprivacy;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableSetting implements Parcelable {
	public int uid;
	public String name;
	public String value;

	public ParcelableSetting() {
	}

	public static final Parcelable.Creator<ParcelableSetting> CREATOR = new Parcelable.Creator<ParcelableSetting>() {
		public ParcelableSetting createFromParcel(Parcel in) {
			return new ParcelableSetting(in);
		}

		public ParcelableSetting[] newArray(int size) {
			return new ParcelableSetting[size];
		}
	};

	private ParcelableSetting(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(uid);
		out.writeString(name);
		out.writeString(value);
	}

	public void readFromParcel(Parcel in) {
		uid = in.readInt();
		name = in.readString();
		value = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}
}