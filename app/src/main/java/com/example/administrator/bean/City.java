package com.example.administrator.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2015/3/31.
 */
public class City implements Parcelable{
    private String province;
    private String city;
    private String number;
    private String firstPY;
    private String allPY;
    private String allFirstPY;
    public City(String province,String city,String number,String firstPY,String allPY,String allFirstPY){
        this.province=province;
        this.city=city;
        this.number=number;
        this.firstPY=firstPY;
        this.allFirstPY=allFirstPY;
        this.allPY=allPY;
    }

    public void setAllFirstPY(String allFirstPY) {
        this.allFirstPY = allFirstPY;
    }

    public void setAllPY(String allPY) {
        this.allPY = allPY;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setFirstPY(String firstPY) {
        this.firstPY = firstPY;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getAllFirstPY() {
        return allFirstPY;
    }

    public String getAllPY() {
        return allPY;
    }

    public String getCity() {
        return city;
    }

    public String getFirstPY() {
        return firstPY;
    }

    public String getNumber() {
        return number;
    }

    public String getProvince() {
        return province;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(province);
        dest.writeString(city);
        dest.writeString(number);
        dest.writeString(allFirstPY);
        dest.writeString(allPY);
        dest.writeString(firstPY);
       // dest.writeStringArray(cityArray);
    }
    public static final Creator<City> CREATOR=new Creator<City>() {
        @Override
        public City createFromParcel(Parcel source) {
            City mcity=new City("","","","","","");
            mcity.province=source.readString();
            mcity.city=source.readString();
            mcity.number=source.readString();
            mcity.allFirstPY=source.readString();
            mcity.allPY=source.readString();
            mcity.firstPY=source.readString();
           // mcity.province=source.readStringArray(cityArray);
            return mcity;
        }
        @Override
        public City[] newArray(int size) {
            return new City[size];
        }
    };
}
